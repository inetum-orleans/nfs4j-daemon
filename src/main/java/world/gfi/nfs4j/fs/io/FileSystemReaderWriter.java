package world.gfi.nfs4j.fs.io;

import world.gfi.nfs4j.fs.handle.HandleRegistry;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.VirtualFileSystem;

import java.io.IOError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Read and write files.
 */
public class FileSystemReaderWriter {
    private final NonBlockingHashMap<Path, FileChannel> pathToFileChannel = new NonBlockingHashMap<>();
    private final HandleRegistry<Path> pathHandleRegistry;

    public FileSystemReaderWriter(HandleRegistry<Path> pathHandleRegistry) {
        this.pathHandleRegistry = pathHandleRegistry;
    }

    public int read(Inode inode, byte[] data, long offset, int count) throws IOException {
        Path path = pathHandleRegistry.toPath(inode);

        ByteBuffer destBuffer = ByteBuffer.wrap(data, 0, count);
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            return channel.read(destBuffer, offset);
        }
    }

    public VirtualFileSystem.WriteResult write(Inode inode, byte[] data, long offset, int count, VirtualFileSystem.StabilityLevel stabilityLevel) throws IOException {
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

    public void commit(Inode inode, long offset, int count) throws IOException {
        Path path = pathHandleRegistry.toPath(inode);
        FileChannel fileChannel = pathToFileChannel.get(path);

        if (fileChannel == null) {
            return;
        }

        long size = fileChannel.size();
        if ((offset == 0 && count == 0) || (size == offset + count)) {
            fileChannel.close();
            pathToFileChannel.remove(path);
        }
    }
}
