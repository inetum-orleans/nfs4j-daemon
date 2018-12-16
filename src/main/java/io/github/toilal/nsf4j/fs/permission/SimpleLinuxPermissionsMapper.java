package io.github.toilal.nsf4j.fs.permission;

import io.github.toilal.nsf4j.config.Permissions;
import org.dcache.nfs.vfs.Stat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;

public class SimpleLinuxPermissionsMapper extends AbstractSimplePermissionsMapper<PosixFileAttributes> {
    public SimpleLinuxPermissionsMapper(Permissions permissions) {
        super(permissions);
    }

    @Override
    public void readPermissions(Path path, PosixFileAttributes attrs, Stat stat) throws IOException {
        super.readPermissions(path, attrs, stat);

        int type = attrs.isSymbolicLink() ? Stat.S_IFLNK : attrs.isDirectory() ? Stat.S_IFDIR : Stat.S_IFREG;
        stat.setMode(type | permissions.getMask());
        if (attrs.isDirectory()) {
            stat.setMode(stat.getMode() | 0111);
        }
        if (!attrs.permissions().contains(PosixFilePermission.OWNER_WRITE)) {
            stat.setMode(stat.getMode() & 0555);
        }
    }
}
