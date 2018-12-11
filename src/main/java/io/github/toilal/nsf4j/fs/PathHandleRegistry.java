package io.github.toilal.nsf4j.fs;

import com.google.common.primitives.Longs;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.cliffc.high_scale_lib.NonBlockingHashMapLong;
import org.dcache.nfs.status.NoEntException;
import org.dcache.nfs.status.StaleException;
import org.dcache.nfs.vfs.Inode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class PathHandleRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(PathHandleRegistry.class);

    private final NonBlockingHashMapLong<Path> fileHandleToPath = new NonBlockingHashMapLong<>();
    private final NonBlockingHashMap<Path, Long> pathToFileHandle = new NonBlockingHashMap<>();
    private final AtomicLong id = new AtomicLong(1); //numbering starts at 1

    public Path toPath(Inode inode) throws StaleException {
        return toPath(toFileHandle(inode));
    }

    public Path toPath(long fileHandle) throws StaleException {
        Path path = fileHandleToPath.get(fileHandle);
        if (path == null) {
            throw new StaleException(String.valueOf(fileHandle));
        }
        return path;
    }

    private long toFileHandle(Path path, boolean createIfFileExists) throws NoEntException {
        Long fileHandle = pathToFileHandle.get(path);
        boolean exists = Files.exists(path, LinkOption.NOFOLLOW_LINKS);
        if (exists && createIfFileExists && fileHandle == null) {
            fileHandle = this.add(path);
        } else if (!exists && fileHandle != null) {
            this.remove(path);
        }
        if (!exists) {
            throw new NoEntException(path.toString());
        }
        return fileHandle;
    }

    public long toFileHandle(Path path) throws NoEntException {
        return toFileHandle(path, true);
    }

    public long toFileHandle(Inode inode) {
        return Longs.fromByteArray(inode.getFileId());
    }

    public Inode toInode(Path path) throws NoEntException {
        return this.toInode(path, true);
    }

    private Inode toInode(Path path, boolean createIfFileExists) throws NoEntException {
        return this.toInode(toFileHandle(path, createIfFileExists));
    }

    public Inode toInode(long fileHandle) {
        return Inode.forFile(Longs.toByteArray(fileHandle));
    }

    public long add(Path path) {
        return this.add(path, this.id.getAndIncrement());
    }

    private long add(Path path, long fileHandle) {
        Long otherFh = pathToFileHandle.putIfAbsent(path, fileHandle);
        if (otherFh != null) {
            throw new IllegalStateException("Can't add FileHandle " + fileHandle + " with Path " + path + ". Path " + path + " is already registered with Inode " + otherFh);
        }

        Path existingPath = fileHandleToPath.putIfAbsent(fileHandle, path);
        if (existingPath != null) {
            pathToFileHandle.remove(fileHandle);
            throw new IllegalStateException("Can't add FileHandle " + fileHandle + " with Path " + path + ". FileHandle " + fileHandle + " is already registered with Path " + existingPath);
        }

        return fileHandle;
    }

    private boolean replaceItem(Path oldPath, Path newPath) {
        Long fileHandle = pathToFileHandle.remove(oldPath);
        if (fileHandle == null) {
            return false;
        }

        Long existingFileHandle = pathToFileHandle.put(newPath, fileHandle);
        if (existingFileHandle != null && (long) existingFileHandle != fileHandle) {
            // It seems harmless ...
        }

        Path removedPath = fileHandleToPath.replace(fileHandle, newPath);
        if (removedPath != null && !removedPath.equals(oldPath)) {
            return false;
        }

        return true;
    }

    public void replace(Path oldPath, Path newPath) throws IOException {
        this.replaceItem(oldPath, newPath);

        if (Files.isDirectory(newPath, LinkOption.NOFOLLOW_LINKS)) {
            Stream<Path> directoryWalker = Files.walk(newPath);
            try {
                directoryWalker.forEach((item) -> {
                    Path oldItem = oldPath.resolve(newPath.relativize(item)).normalize();
                    Path newItem = item.normalize();
                    this.replaceItem(oldItem, newItem);
                });
            } finally {
                directoryWalker.close();
            }
        }
    }

    public long remove(Path path) {
        Long fileHandle = pathToFileHandle.remove(path);
        if (fileHandle == null) {
            throw new IllegalStateException("Can't remove Path " + path + ". Path " + path + " is not registered.");
        }

        Path removedPath = fileHandleToPath.remove(fileHandle);
        if (!path.equals(removedPath)) {
            pathToFileHandle.put(path, fileHandle);
            throw new IllegalStateException("Can't remove Path " + path + ". Removed Path " + removedPath + " doesn't match expected path " + path + ".");
        }

        return fileHandle;
    }

    public long size() {
        return fileHandleToPath.size();
    }
}
