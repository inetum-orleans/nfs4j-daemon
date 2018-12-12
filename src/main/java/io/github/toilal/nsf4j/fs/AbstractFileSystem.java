package io.github.toilal.nsf4j.fs;

import org.dcache.nfs.status.BadNameException;
import org.dcache.nfs.status.ExistException;
import org.dcache.nfs.status.NoEntException;
import org.dcache.nfs.status.NotEmptyException;
import org.dcache.nfs.status.NotSuppException;
import org.dcache.nfs.status.PermException;
import org.dcache.nfs.status.ServerFaultException;
import org.dcache.nfs.v4.NfsIdMapping;
import org.dcache.nfs.v4.SimpleIdMap;
import org.dcache.nfs.v4.xdr.nfsace4;
import org.dcache.nfs.vfs.AclCheckable;
import org.dcache.nfs.vfs.DirectoryEntry;
import org.dcache.nfs.vfs.DirectoryStream;
import org.dcache.nfs.vfs.FsStat;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.dcache.nfs.vfs.Stat.Type;
import org.dcache.nfs.vfs.VirtualFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

public abstract class AbstractFileSystem<A extends BasicFileAttributes> implements VirtualFileSystem {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractFileSystem.class);

    private final Path root;
    private final long rootFileHandle;
    private final PathHandleRegistry pathHandleRegistry = new PathHandleRegistry();
    private final NfsIdMapping _idMapper = new SimpleIdMap();
    private final FileSystemReaderWriter fileSystemReaderWriter;

    public AbstractFileSystem(Path root) {
        this.root = root;
        this.rootFileHandle = pathHandleRegistry.add(this.root);
        this.fileSystemReaderWriter = new FileSystemReaderWriter(pathHandleRegistry);
    }

    abstract protected void applyOwnershipAndModeToPath(Path target, Subject subject, int mode);

    abstract protected A getFileAttributes(Path path) throws IOException;

    protected void applyStatToPath(Stat stat, Path path) throws IOException {
        if (stat.isDefined(Stat.StatAttribute.SIZE)) {
            try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw")) {
                raf.setLength(stat.getSize());
            }
        }

        BasicFileAttributeView attributeView = Files.getFileAttributeView(path, BasicFileAttributeView.class, NOFOLLOW_LINKS);

        FileTime lastModifiedTime = null;
        FileTime lastAccessTime = null;
        FileTime createTime = null;

        if (stat.isDefined(Stat.StatAttribute.MTIME)) {
            lastModifiedTime = FileTime.fromMillis(stat.getMTime());
        }

        if (stat.isDefined(Stat.StatAttribute.ATIME)) {
            lastAccessTime = FileTime.fromMillis(stat.getATime());
        }

        if (stat.isDefined(Stat.StatAttribute.CTIME)) {
            createTime = FileTime.fromMillis(stat.getCTime());
        }

        attributeView.setTimes(lastModifiedTime, lastAccessTime, createTime);
    }

    protected void applyFileAttributesToStat(Stat stat, Path path, A attrs) throws IOException {
        stat.setATime(attrs.lastAccessTime().toMillis());
        stat.setCTime(attrs.creationTime().toMillis());
        stat.setMTime(attrs.lastModifiedTime().toMillis());

        stat.setSize(attrs.size());
        stat.setGeneration(attrs.lastModifiedTime().toMillis());

        long fileHandle = pathHandleRegistry.toFileHandle(path);
        stat.setIno((int) fileHandle);
        stat.setFileid((int) fileHandle);

        stat.setDev(17);
        stat.setRdev(17);
    }

    private Inode toInode(long fileHandle) {
        return pathHandleRegistry.toInode(fileHandle);
    }

    private long toFileHandle(Inode inode) {
        return pathHandleRegistry.toFileHandle(inode);
    }

    @Override
    public Inode create(Inode parent, Type type, String path, Subject subject, int mode) throws IOException {
        Path parentPath = pathHandleRegistry.toPath(parent);
        Path newPath = parentPath.resolve(path).normalize();
        try {
            Files.createFile(newPath);
        } catch (FileAlreadyExistsException e) {
            throw new ExistException("path " + newPath);
        }

        applyOwnershipAndModeToPath(newPath, subject, mode);
        return toInode(pathHandleRegistry.toFileHandle(newPath));
    }

    @Override
    public FsStat getFsStat() throws IOException {
        FileStore store = Files.getFileStore(root);
        long total = store.getTotalSpace();
        long free = store.getUsableSpace();
        return new FsStat(total, Long.MAX_VALUE, total - free, pathHandleRegistry.size());
    }

    @Override
    public Inode getRootInode() {
        return toInode(rootFileHandle);
    }

    @Override
    public Inode lookup(Inode parent, String path) throws IOException {
        Path parentPath = pathHandleRegistry.toPath(parent);

        try {
            Path child = parentPath.resolve(path).normalize();
            return toInode(pathHandleRegistry.toFileHandle(child));
        } catch (InvalidPathException e) {
            throw new BadNameException(path);
        }
    }

    @Override
    public Inode link(Inode parent, Inode existing, String target, Subject subject) throws IOException {
        Path parentPath = pathHandleRegistry.toPath(parent);
        Path existingPath = pathHandleRegistry.toPath(existing);

        Path targetPath = parentPath.resolve(target).normalize();

        try {
            Files.createLink(targetPath, existingPath);
        } catch (UnsupportedOperationException e) {
            throw new NotSuppException("Not supported", e);
        } catch (FileAlreadyExistsException e) {
            throw new ExistException("Path exists " + target, e);
        } catch (SecurityException e) {
            throw new PermException("Permission denied: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new ServerFaultException("Failed to create: " + e.getMessage(), e);
        }

        return toInode(pathHandleRegistry.toFileHandle(targetPath));
    }

    private byte[] toVerifier(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        byte[] verifier = new byte[8];
        System.arraycopy(buffer.array(), 0, verifier, 0, 8);
        return verifier;
    }

    @Override
    public DirectoryStream list(Inode inode, byte[] verifier, long cookie) throws IOException {
        Path path = pathHandleRegistry.toPath(inode);
        final List<DirectoryEntry> list = new ArrayList<>();
        long verifierLong = Long.MIN_VALUE;
        try (java.nio.file.DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
            // OperationREADDIR for NFSV4 automatically handles offsets for reserved cookie values,
            // so we can start with 0.
            long currentCookie = 0;
            for (Path p : ds) {
                String filename = p.getFileName().toString();
                verifierLong += filename.hashCode() + currentCookie * 1024;
                if (currentCookie >= cookie) {
                    list.add(new DirectoryEntry(filename, pathHandleRegistry.toInode(p), getStat(p), currentCookie));
                }
                currentCookie++;
            }
        }
        return new DirectoryStream(toVerifier(verifierLong), list);
    }

    @Override
    public byte[] directoryVerifier(Inode inode) throws IOException {
        Path path = pathHandleRegistry.toPath(inode);
        long verifierLong = Long.MIN_VALUE;
        try (java.nio.file.DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
            long currentCookie = 0;
            for (Path p : ds) {
                String filename = p.getFileName().toString();
                verifierLong += filename.hashCode() + currentCookie * 1024;
            }
        }
        return toVerifier(verifierLong);
    }

    @Override
    public Inode mkdir(Inode parent, String path, Subject subject, int mode) throws IOException {
        Path parentPath = pathHandleRegistry.toPath(parent);
        Path newPath = parentPath.resolve(path).normalize();

        try {
            Files.createDirectory(newPath);
        } catch (FileAlreadyExistsException e) {
            throw new ExistException("path " + newPath);
        }

        applyOwnershipAndModeToPath(newPath, subject, mode);
        return toInode(pathHandleRegistry.toFileHandle(newPath));
    }

    @Override
    public boolean move(Inode src, String oldName, Inode dest, String newName) throws IOException {
        Path currentParentPath = pathHandleRegistry.toPath(src);
        Path destPath = pathHandleRegistry.toPath(dest);

        Path currentPath = currentParentPath.resolve(oldName).normalize();
        Path newPath = destPath.resolve(newName).normalize();

        if (destPath.equals(newPath)) {
            return true;
        }

        try {
            Files.move(currentPath, newPath, StandardCopyOption.ATOMIC_MOVE);
        } catch (FileAlreadyExistsException e) {
            throw new ExistException(String.valueOf(newPath));
        }

        pathHandleRegistry.replace(currentPath, newPath);
        return true;
    }

    @Override
    public Inode parentOf(Inode inode) throws IOException {
        Path path = pathHandleRegistry.toPath(inode);
        Path parentPath = path.getParent().normalize();
        return toInode(pathHandleRegistry.toFileHandle(parentPath));
    }

    @Override
    public String readlink(Inode inode) throws IOException {
        Path path = pathHandleRegistry.toPath(inode);
        String linkData = Files.readSymbolicLink(path).normalize().toString();
        if (File.separatorChar != '/') {
            linkData = linkData.replace(File.separatorChar, '/');
        }
        return linkData;
    }

    @Override
    public void remove(Inode parent, String path) throws IOException {
        Path parentPath = pathHandleRegistry.toPath(parent);
        Path targetPath = parentPath.resolve(path).normalize();

        try {
            Files.delete(targetPath);
        } catch (DirectoryNotEmptyException e) {
            throw new NotEmptyException("Directory " + targetPath + " is not empty", e);
        }

        pathHandleRegistry.remove(targetPath);
    }

    @Override
    public Inode symlink(Inode parent, String linkName, String targetName, Subject subject, int mode) throws IOException {
        Path parentPath = pathHandleRegistry.toPath(parent);

        Path link = parentPath.resolve(linkName).normalize();
        Path target = parentPath.resolve(targetName).normalize();

        if (!targetName.startsWith("/")) {
            target = parentPath.relativize(target);
        }

        try {
            Files.createSymbolicLink(link, target);
        } catch (UnsupportedOperationException e) {
            throw new NotSuppException("Not supported", e);
        } catch (FileAlreadyExistsException e) {
            throw new ExistException("Path exists " + linkName, e);
        } catch (SecurityException e) {
            throw new PermException("Permission denied: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new ServerFaultException("Failed to create: " + e.getMessage(), e);
        }

        applyOwnershipAndModeToPath(link, subject, mode);
        return toInode(pathHandleRegistry.toFileHandle(link));
    }

    @Override
    public int read(Inode inode, byte[] data, long offset, int count) throws IOException {
        return fileSystemReaderWriter.read(inode, data, offset, count);
    }

    @Override
    public WriteResult write(Inode inode, byte[] data, long offset, int count, StabilityLevel stabilityLevel) throws IOException {
        return fileSystemReaderWriter.write(inode, data, offset, count, stabilityLevel);
    }

    @Override
    public void commit(Inode inode, long offset, int count) throws IOException {
        fileSystemReaderWriter.commit(inode, offset, count);
    }

    protected Stat getStat(Path p) throws IOException {
        try {
            A attrs = getFileAttributes(p);
            Stat stat = new Stat();
            applyFileAttributesToStat(stat, p, attrs);
            return stat;
        } catch (NoSuchFileException e) {
            throw new NoEntException(p.toString());
        }
    }

    @Override
    public int access(Inode inode, int mode) throws IOException {
        return mode;
    }

    @Override
    public Stat getattr(Inode inode) throws IOException {
        Path path = pathHandleRegistry.toPath(inode);
        return getStat(path);
    }

    @Override
    public void setattr(Inode inode, Stat stat) throws IOException {
        Path path = pathHandleRegistry.toPath(inode);
        applyStatToPath(stat, path);
    }

    @Override
    public nfsace4[] getAcl(Inode inode) throws IOException {
        return new nfsace4[0];
    }

    @Override
    public void setAcl(Inode inode, nfsace4[] acl) throws IOException {
        // NOP
    }

    @Override
    public boolean hasIOLayout(Inode inode) throws IOException {
        return false;
    }

    @Override
    public AclCheckable getAclCheckable() {
        return AclCheckable.UNDEFINED_ALL;
    }

    @Override
    public NfsIdMapping getIdMapper() {
        return _idMapper;
    }
}
