package world.gfi.nfs4j.fs.permission;

import world.gfi.nfs4j.config.PermissionsConfig;
import org.dcache.nfs.vfs.Stat;

import javax.security.auth.Subject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public abstract class AbstractSimplePermissionsMapper<A extends BasicFileAttributes> implements PermissionsMapper<A> {
    protected final PermissionsConfig permissions;

    public AbstractSimplePermissionsMapper(PermissionsConfig permissions) {
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
