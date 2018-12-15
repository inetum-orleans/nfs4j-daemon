package io.github.toilal.nsf4j.fs.handle;

import com.google.common.primitives.Longs;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.cliffc.high_scale_lib.NonBlockingHashMapLong;
import org.dcache.nfs.status.NoEntException;
import org.dcache.nfs.status.StaleException;
import org.dcache.nfs.vfs.Inode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Bidirectionnal mapping of Path to/from File Handle.
 *
 * @param <P>
 */
public abstract class HandleRegistry<P> {
    private static final Logger LOG = LoggerFactory.getLogger(HandleRegistry.class);

    private final NonBlockingHashMapLong<P> fileHandleToPath = new NonBlockingHashMapLong<>();
    private final NonBlockingHashMap<P, Long> pathToFileHandle = new NonBlockingHashMap<>();
    private final UniqueHandleGenerator uniqueLongGenerator;

    public HandleRegistry(UniqueHandleGenerator uniqueLongGenerator) {
        this.uniqueLongGenerator = uniqueLongGenerator;
    }

    abstract protected boolean pathExists(P path);

    abstract public void replace(P oldPath, P newPath) throws IOException;

    public P toPath(Inode inode) throws StaleException {
        return toPath(toFileHandle(inode));
    }

    public boolean hasInode(Inode inode) {
        return hasInode(toFileHandle(inode));
    }

    public boolean hasInode(long fileHandle) {
        return fileHandleToPath.containsKey(fileHandle);
    }

    public P toPath(long fileHandle) throws StaleException {
        P path = fileHandleToPath.get(fileHandle);
        if (path == null) {
            throw new StaleException(String.valueOf(fileHandle));
        }
        return path;
    }

    private long toFileHandle(P path, boolean createIfPathExists) throws NoEntException {
        Long fileHandle = pathToFileHandle.get(path);
        boolean exists = pathExists(path);
        if (exists && createIfPathExists && fileHandle == null) {
            fileHandle = this.add(path);
        } else if (!exists && fileHandle != null) {
            this.remove(path);
        }
        if (!exists) {
            throw new NoEntException(path.toString());
        }
        return fileHandle;
    }

    public long toFileHandle(P path) throws NoEntException {
        return toFileHandle(path, true);
    }

    public long toFileHandle(Inode inode) {
        return Longs.fromByteArray(inode.getFileId());
    }

    public Inode toInode(P path) throws NoEntException {
        return this.toInode(path, true);
    }

    private Inode toInode(P path, boolean createIfPathExists) throws NoEntException {
        return this.toInode(toFileHandle(path, createIfPathExists));
    }

    public Inode toInode(long fileHandle) {
        return Inode.forFile(Longs.toByteArray(fileHandle));
    }

    public long add(P path) {
        return this.add(path, this.uniqueLongGenerator.uniqueHandle());
    }

    private long add(P path, long fileHandle) {
        Long otherFh = pathToFileHandle.putIfAbsent(path, fileHandle);
        if (otherFh != null) {
            throw new IllegalStateException("Can't add FileHandle " + fileHandle + " with Path " + path + ". Path " + path + " is already registered with Inode " + otherFh);
        }

        P existingPath = fileHandleToPath.putIfAbsent(fileHandle, path);
        if (existingPath != null) {
            pathToFileHandle.remove(fileHandle);
            throw new IllegalStateException("Can't add FileHandle " + fileHandle + " with Path " + path + ". FileHandle " + fileHandle + " is already registered with Path " + existingPath);
        }

        return fileHandle;
    }

    protected boolean replaceItem(P oldPath, P newPath) {
        Long fileHandle = pathToFileHandle.remove(oldPath);
        if (fileHandle == null) {
            return false;
        }

        Long existingFileHandle = pathToFileHandle.put(newPath, fileHandle);
        if (existingFileHandle != null && (long) existingFileHandle != fileHandle) {
            // It seems harmless ...
        }

        P removedPath = fileHandleToPath.replace(fileHandle, newPath);
        if (removedPath != null && !removedPath.equals(oldPath)) {
            return false;
        }

        return true;
    }

    public long remove(P path) {
        Long fileHandle = pathToFileHandle.remove(path);
        if (fileHandle == null) {
            throw new IllegalStateException("Can't remove Path " + path + ". Path " + path + " is not registered.");
        }

        P removedPath = fileHandleToPath.remove(fileHandle);
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
