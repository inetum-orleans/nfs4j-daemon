package world.gfi.nfs4j.fs.permission;

import org.dcache.nfs.vfs.Stat;

import javax.security.auth.Subject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class SimplePermissionsMapperRead<A extends BasicFileAttributes> implements PermissionsMapper<A> {
    protected final PermissionsReader reader;

    public SimplePermissionsMapperRead(PermissionsReader reader) {
        this.reader = reader;
    }

    @Override
    public void readPermissions(Path path, A attrs, Stat stat) throws IOException {
        int[] permissions = this.reader.getPermissions(path, attrs);
        stat.setUid(permissions[0]);
        stat.setGid(permissions[1]);

        int type = attrs.isSymbolicLink() ? Stat.S_IFLNK : attrs.isDirectory() ? Stat.S_IFDIR : Stat.S_IFREG;
        stat.setMode(type | permissions[2]);
    }

    @Override
    public void writePermissions(Path path, Subject subject, int mode) throws IOException {
    }

    @Override
    public void writePermissions(Path path, Stat stat) throws IOException {

    }
}
