package world.gfi.nfs4j.fs.permission;

import org.dcache.nfs.vfs.Stat;
import world.gfi.nfs4j.fs.handle.PathHandleRegistryListener;

import javax.security.auth.Subject;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public interface PermissionsMapper<A extends BasicFileAttributes> {
    /**
     * Read permissions from {@link Path} and associated {@link BasicFileAttributes} to {@link Stat} object.
     * <p>
     * PermissionsConfig set to {@link Stat} instance will be applied to the NFS mounted directory.
     *
     * @param path
     * @param attrs
     * @param stat
     */
    void readPermissions(Path path, A attrs, Stat stat) throws IOException;

    /**
     * Write permissions from {@link javax.security.auth.Subject} owner and mode to {@link Path}.
     * <p>
     * PermissionsConfig set to {@link Path} instance will be applied on the local shared directory.
     *
     * @param path
     * @param subject
     * @param mode
     */
    void writePermissions(Path path, Subject subject, int mode) throws IOException;

    /**
     * Write permissions from stat to {@link Path}.
     * <p>
     * PermissionsConfig set to {@link Path} instance will be applied on the local shared directory.
     *
     * @param path
     * @param stat
     * @throws IOException
     */
    void writePermissions(Path path, Stat stat) throws IOException;

    /**
     * Get the mapper listener for path handle changes.
     *
     * @return
     */
    default PathHandleRegistryListener getHandleRegistryListener() {
        return null;
    }

    /**
     * Get closable resources to be invoked when detaching the associated share.
     *
     * @return
     */
    default Closeable getClosable() {
        return null;
    }
}
