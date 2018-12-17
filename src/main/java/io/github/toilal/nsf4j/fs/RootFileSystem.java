package io.github.toilal.nsf4j.fs;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import io.github.toilal.nsf4j.config.PermissionsConfig;
import io.github.toilal.nsf4j.fs.handle.UniqueHandleGenerator;
import io.github.toilal.nsf4j.fs.permission.SimpleLinuxPermissionsMapper;
import org.dcache.nfs.v4.NfsIdMapping;
import org.dcache.nfs.v4.xdr.nfsace4;
import org.dcache.nfs.vfs.AclCheckable;
import org.dcache.nfs.vfs.DirectoryEntry;
import org.dcache.nfs.vfs.DirectoryStream;
import org.dcache.nfs.vfs.FsStat;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.Stat;
import org.dcache.nfs.vfs.VirtualFileSystem;

import javax.security.auth.Subject;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A memory implementation of {@link VirtualFileSystem} that supports attaching others file systems {@link AttachableFileSystem} on given aliases.
 */
public class RootFileSystem implements VirtualFileSystem {
    private final LinuxNioFileSystem mainFs;
    private Map<String, AttachableFileSystem> fileSystems = new LinkedHashMap<>();

    private static Path buildRootPath() {
        return Jimfs.newFileSystem(
                Configuration.unix().toBuilder()
                        .setAttributeViews("basic", "owner", "posix", "unix")
                        .setWorkingDirectory("/")
                        .build())
                .getRootDirectories().iterator().next();
    }

    public RootFileSystem(PermissionsConfig permissions, UniqueHandleGenerator uniqueLongGenerator) {
        mainFs = new LinuxNioFileSystem(buildRootPath(), new SimpleLinuxPermissionsMapper(permissions), uniqueLongGenerator);
    }

    public void attachFileSystem(AttachableFileSystem fs, String path, String... morePath) {
        try {
            Path aliasPath = mainFs.root.getFileSystem().getPath(path, morePath);
            Path directories = Files.createDirectories(aliasPath).normalize();
            fileSystems.put(directories.toString(), fs);
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public Map<String, AttachableFileSystem> getFileSystems() {
        return Collections.unmodifiableMap(fileSystems);
    }

    public AttachableFileSystem detachFileSystem(String path, String... morePath) {
        Path aliasPath = mainFs.root.getFileSystem().getPath(path, morePath).normalize();
        return fileSystems.remove(aliasPath.toString());
    }

    protected AttachableFileSystem delegate(Inode inode) {
        for (AttachableFileSystem fs : fileSystems.values()) {
            if (fs.hasInode(inode)) {
                return fs;
            }
        }
        return mainFs;
    }

    @Override
    public Inode getRootInode() throws IOException {
        Inode rootInode = mainFs.handleRegistry.toInode(mainFs.rootFileHandle);
        Path path = mainFs.handleRegistry.toPath(rootInode);
        VirtualFileSystem fs = fileSystems.get(path.toString());
        if (fs != null) {
            return fs.getRootInode();
        }
        return rootInode;
    }

    @Override
    public Inode lookup(Inode parent, String name) throws IOException {
        AttachableFileSystem delegate = delegate(parent);
        if (delegate == mainFs) {
            Path parentPath = mainFs.handleRegistry.toPath(parent);
            Path path = parentPath.resolve(name).normalize();
            VirtualFileSystem fs = fileSystems.get(path.toString());
            if (fs != null) {
                return fs.getRootInode();
            }
        }
        return delegate.lookup(parent, name);
    }

    @Override
    public DirectoryStream list(Inode inode, byte[] verifier, long cookie) throws IOException {
        AttachableFileSystem delegate = delegate(inode);
        if (delegate == mainFs) {
            Path path = mainFs.handleRegistry.toPath(inode);
            final List<DirectoryEntry> list = new ArrayList<>();
            long verifierLong = Long.MIN_VALUE;
            try (java.nio.file.DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
                long currentCookie = 0;
                for (Path p : ds) {
                    String filename = p.getFileName().toString();
                    verifierLong += filename.hashCode() + currentCookie * 1024;
                    if (currentCookie >= cookie) {
                        AttachableFileSystem fs = fileSystems.get(p.normalize().toString());
                        if (fs == null) {
                            list.add(mainFs.buildDirectoryEntry(p, currentCookie));
                        } else {
                            list.add(fs.buildRootDirectoryEntry(filename, currentCookie));
                        }
                    }
                    currentCookie++;
                }
            }
            return new DirectoryStream(mainFs.toVerifier(verifierLong), list);
        } else {
            return delegate.list(inode, verifier, cookie);
        }
    }

    @Override
    public Stat getattr(Inode inode) throws IOException {
        return delegate(inode).getattr(inode);
    }

    @Override
    public int access(Inode inode, int mode) throws IOException {
        return delegate(inode).access(inode, mode);
    }

    @Override
    public Inode create(Inode parent, Stat.Type type, String path, Subject subject, int mode) throws IOException {
        return delegate(parent).create(parent, type, path, subject, mode);
    }

    @Override
    public FsStat getFsStat() throws IOException {
        return fileSystems.values().iterator().next().getFsStat();
    }

    @Override
    public Inode link(Inode parent, Inode existing, String target, Subject subject) throws IOException {
        return delegate(parent).link(parent, existing, target, subject);
    }

    @Override
    public byte[] directoryVerifier(Inode inode) throws IOException {
        return delegate(inode).directoryVerifier(inode);
    }

    @Override
    public Inode mkdir(Inode parent, String path, Subject subject, int mode) throws IOException {
        return delegate(parent).mkdir(parent, path, subject, mode);
    }

    @Override
    public boolean move(Inode src, String oldName, Inode dest, String newName) throws IOException {
        return delegate(src).move(src, oldName, dest, newName);
    }

    @Override
    public Inode parentOf(Inode inode) throws IOException {
        return delegate(inode).parentOf(inode);
    }

    @Override
    public String readlink(Inode inode) throws IOException {
        return delegate(inode).readlink(inode);
    }

    @Override
    public void remove(Inode parent, String path) throws IOException {
        delegate(parent).remove(parent, path);
    }

    @Override
    public Inode symlink(Inode parent, String linkName, String targetName, Subject subject, int mode) throws IOException {
        return delegate(parent).symlink(parent, linkName, targetName, subject, mode);
    }

    @Override
    public int read(Inode inode, byte[] data, long offset, int count) throws IOException {
        return delegate(inode).read(inode, data, offset, count);
    }

    @Override
    public WriteResult write(Inode inode, byte[] data, long offset, int count, StabilityLevel stabilityLevel) throws IOException {
        return delegate(inode).write(inode, data, offset, count, stabilityLevel);
    }

    @Override
    public void commit(Inode inode, long offset, int count) throws IOException {
        delegate(inode).commit(inode, offset, count);
    }

    @Override
    public void setattr(Inode inode, Stat stat) throws IOException {
        delegate(inode).setattr(inode, stat);
    }

    @Override
    public nfsace4[] getAcl(Inode inode) throws IOException {
        return delegate(inode).getAcl(inode);
    }

    @Override
    public void setAcl(Inode inode, nfsace4[] acl) throws IOException {
        delegate(inode).setAcl(inode, acl);
    }

    @Override
    public boolean hasIOLayout(Inode inode) throws IOException {
        return delegate(inode).hasIOLayout(inode);
    }

    @Override
    public AclCheckable getAclCheckable() {
        return mainFs.getAclCheckable();
    }

    @Override
    public NfsIdMapping getIdMapper() {
        return mainFs.getIdMapper();
    }
}
