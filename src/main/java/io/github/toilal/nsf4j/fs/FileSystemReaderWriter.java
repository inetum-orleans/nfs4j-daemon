package io.github.toilal.nsf4j.fs;

import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.VirtualFileSystem;

import java.io.IOError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileSystemReaderWriter {
    private final NonBlockingHashMap<Path, FileChannel> pathToFileChannel = new NonBlockingHashMap<>();
    private final PathHandleRegistry pathHandleRegistry;

    public FileSystemReaderWriter(PathHandleRegistry pathHandleRegistry) {
        this.pathHandleRegistry = pathHandleRegistry;
    }

    public int read(Inode inode, byte[] data, long offset, int count) throws IOException {
        Path path = pathHandleRegistry.toPath(inode);

        ByteBuffer destBuffer = ByteBuffer.wrap(data, 0, count);
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            return channel.read(destBuffer, offset);
        }
    }

    VirtualFileSystem.WriteResult write(Inode inode, byte[] data, long offset, int count, VirtualFileSystem.StabilityLevel stabilityLevel) throws IOException {
        Path path = pathHandleRegistry.toPath(inode);

        ByteBuffer srcBuffer = ByteBuffer.wrap(data, 0, count);
        if (stabilityLevel == VirtualFileSystem.StabilityLevel.UNSTABLE) {
            assert data.length == count;

            FileChannel channel;
            try {
                channel = pathToFileChannel.computeIfAbsent(path, (p) -> {
                    try {
                        return FileChannel.open(p, StandardOpenOption.WRITE);
                    } catch (IOException e) {
                        throw new IOError(e);
                    }
                });
            } catch (IOError e) {
                throw (IOException) e.getCause();
            }

            int bytesWritten = channel.write(srcBuffer, offset);
            return new VirtualFileSystem.WriteResult(VirtualFileSystem.StabilityLevel.UNSTABLE, bytesWritten);
        } else {
            try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.SYNC)) {
                int bytesWritten = channel.write(srcBuffer, offset);
                return new VirtualFileSystem.WriteResult(VirtualFileSystem.StabilityLevel.FILE_SYNC, bytesWritten);
            }
        }
    }

    void commit(Inode inode, long offset, int count) throws IOException {
        Path path = pathHandleRegistry.toPath(inode);

        FileChannel fileChannel = pathToFileChannel.remove(path);
        if (fileChannel != null) {
            fileChannel.close();
        }
    }
}
