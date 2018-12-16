package io.github.toilal.nsf4j.fs.permission;

import io.github.toilal.nsf4j.config.Permissions;
import org.dcache.nfs.vfs.Stat;

import javax.security.auth.Subject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public abstract class AbstractSimplePermissionsMapper<A extends BasicFileAttributes> implements PermissionsMapper<A> {
    protected final Permissions permissions;

    public AbstractSimplePermissionsMapper(Permissions permissions) {
        this.permissions = permissions;
    }

    @Override
    public void readPermissions(Path path, A attrs, Stat stat) throws IOException {
        stat.setGid(this.permissions.getGid());
        stat.setUid(this.permissions.getUid());
    }

    @Override
    public void writePermissions(Path path, Subject subject, int mode) throws IOException {

    }
}
