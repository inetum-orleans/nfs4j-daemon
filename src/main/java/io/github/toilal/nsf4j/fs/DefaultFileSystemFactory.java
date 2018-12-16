package io.github.toilal.nsf4j.fs;

import io.github.toilal.nsf4j.fs.handle.UniqueHandleGenerator;
import io.github.toilal.nsf4j.fs.permission.PermissionsMapper;

import java.nio.file.Path;

/**
 * Default factory of {@link AttachableFileSystem}.
 */
public class DefaultFileSystemFactory implements FileSystemFactory {
    @Override
    public AttachableFileSystem newFileSystem(Path root, PermissionsMapper permissionsMapper, UniqueHandleGenerator uniqueLongGenerator) {
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            return new WindowsNioFileSystem(root, permissionsMapper, uniqueLongGenerator);
        } else {
            return new LinuxNioFileSystem(root, permissionsMapper, uniqueLongGenerator);
        }
    }
}
