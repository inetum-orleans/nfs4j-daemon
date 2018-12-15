package io.github.toilal.nsf4j.fs;

import io.github.toilal.nsf4j.fs.handle.UniqueHandleGenerator;

import java.nio.file.Path;

/**
 * Factory of {@link AttachableFileSystem}.
 */
public interface FileSystemFactory {
    AttachableFileSystem newFileSystem(Path root, UniqueHandleGenerator uniqueLongGenerator);
}
