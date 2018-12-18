package world.gfi.nfs4j.fs.permission;

import world.gfi.nfs4j.config.PermissionsConfig;
import org.dcache.nfs.vfs.Stat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributes;

public class SimpleWindowsPermissionsMapper extends AbstractSimplePermissionsMapper<DosFileAttributes> {
    public SimpleWindowsPermissionsMapper(PermissionsConfig permissions) {
        super(permissions);
    }

    @Override
    public void readPermissions(Path path, DosFileAttributes attrs, Stat stat) throws IOException {
        super.readPermissions(path, attrs, stat);

        int type = attrs.isSymbolicLink() ? Stat.S_IFLNK : attrs.isDirectory() ? Stat.S_IFDIR : Stat.S_IFREG;
        int mask = permissions.getMask();
        if (attrs.isDirectory()) {
            mask = mask | 0111;
        }
        if (attrs.isReadOnly()) {
            mask = mask & 0555;
        }
        stat.setMode(type | mask);
    }
}
