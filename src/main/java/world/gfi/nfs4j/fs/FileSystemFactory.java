package world.gfi.nfs4j.fs;

import world.gfi.nfs4j.fs.handle.UniqueHandleGenerator;
import world.gfi.nfs4j.fs.permission.PermissionsMapper;

import java.nio.file.Path;

/**
 * Factory of {@link AttachableFileSystem}.
 */
public interface FileSystemFactory {
    AttachableFileSystem newFileSystem(Path root, PermissionsMapper permissionsMapper, UniqueHandleGenerator uniqueLongGenerator);
}
