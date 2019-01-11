package world.gfi.nfs4j.fs.permission;

import org.dcache.auth.Subjects;
import org.dcache.nfs.vfs.Stat;
import world.gfi.nfs4j.fs.handle.PathHandleRegistryListener;

import javax.security.auth.Subject;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class SimplePermissionsMapper<A extends BasicFileAttributes> extends SimplePermissionsMapperRead<A> {
    protected final PermissionsWriter writer;

    public SimplePermissionsMapper(PermissionsReader reader, PermissionsWriter writer) {
        super(reader);
        this.writer = writer;
    }

    @Override
    public void writePermissions(Path path, Subject subject, int mode) throws IOException {
        long uid = Subjects.getUid(subject);
        long gid = Subjects.getPrimaryGid(subject);

        this.writer.setPermissions(path, (int) uid, (int) gid, mode & 0000777);
    }

    @Override
    public void writePermissions(Path path, Stat stat) throws IOException {
        Integer uid = stat.isDefined(Stat.StatAttribute.OWNER) ? stat.getUid() : null;
        Integer gid = stat.isDefined(Stat.StatAttribute.GROUP) ? stat.getGid() : null;
        Integer mask = stat.isDefined(Stat.StatAttribute.MODE) ? (stat.getMode() & 0000777) : null;

        this.writer.setPermissions(path, uid, gid, mask);
    }

    @Override
    public PathHandleRegistryListener getHandleRegistryListener() {
        if (this.writer instanceof PathHandleRegistryListener) {
            return (PathHandleRegistryListener) this.writer;
        }
        return null;
    }

    @Override
    public Closeable getCloseable() {
        if (this.writer instanceof Closeable) {
            return (Closeable) this.writer;
        }
        return null;
    }
}
