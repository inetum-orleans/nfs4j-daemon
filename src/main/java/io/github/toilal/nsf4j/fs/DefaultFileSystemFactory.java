package io.github.toilal.nsf4j.fs;

import io.github.toilal.nsf4j.fs.handle.UniqueHandleGenerator;

import java.nio.file.Path;

/**
 * Default factory of {@link AttachableFileSystem}.
 */
public class DefaultFileSystemFactory implements FileSystemFactory {
    @Override
    public AttachableFileSystem newFileSystem(Path root, UniqueHandleGenerator uniqueLongGenerator) {
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            return new WindowsNioFileSystem(root, uniqueLongGenerator);
        } else {
            return new LinuxNioFileSystem(root, uniqueLongGenerator);
        }
    }
}
