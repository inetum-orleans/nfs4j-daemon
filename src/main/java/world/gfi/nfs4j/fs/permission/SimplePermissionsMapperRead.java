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
        stat.setGid(this.reader.getGid(path, attrs));
        stat.setUid(this.reader.getUid(path, attrs));

        int type = attrs.isSymbolicLink() ? Stat.S_IFLNK : attrs.isDirectory() ? Stat.S_IFDIR : Stat.S_IFREG;
        int mask = this.reader.getMask(path, attrs);
        stat.setMode(type | mask);
    }

    @Override
    public void writePermissions(Path path, Subject subject, int mode) throws IOException {
    }

    @Override
    public void writePermissions(Path path, Stat stat) throws IOException {

    }
}
