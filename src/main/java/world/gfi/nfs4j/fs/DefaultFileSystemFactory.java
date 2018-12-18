package world.gfi.nfs4j.fs;

import world.gfi.nfs4j.fs.handle.UniqueHandleGenerator;
import world.gfi.nfs4j.fs.permission.PermissionsMapper;

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
