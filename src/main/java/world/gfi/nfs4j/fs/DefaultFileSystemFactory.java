package world.gfi.nfs4j.fs;

import org.apache.commons.lang3.SystemUtils;
import world.gfi.nfs4j.fs.handle.UniqueHandleGenerator;
import world.gfi.nfs4j.fs.permission.PermissionsMapper;

import java.nio.file.Path;

/**
 * Default factory of {@link AttachableFileSystem}.
 */
public class DefaultFileSystemFactory implements FileSystemFactory {
    @Override
    public AttachableFileSystem newFileSystem(Path root, PermissionsMapper permissionsMapper, UniqueHandleGenerator uniqueLongGenerator) {
        if (SystemUtils.IS_OS_WINDOWS) {
            return new WindowsNioFileSystem(root, permissionsMapper, uniqueLongGenerator);
        } else {
            return new LinuxNioFileSystem(root, permissionsMapper, uniqueLongGenerator);
        }
    }
}
