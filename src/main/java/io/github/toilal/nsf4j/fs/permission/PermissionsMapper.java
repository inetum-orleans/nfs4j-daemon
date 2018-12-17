package io.github.toilal.nsf4j.fs.permission;

import org.dcache.nfs.vfs.Stat;

import javax.security.auth.Subject;
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
}
