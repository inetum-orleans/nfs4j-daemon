package world.gfi.nfs4j.fs.permission;

import world.gfi.nfs4j.config.PermissionsConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

public class LinuxPermissionsSimpleReader extends AbstractPermissionsSimpleReader<PosixFileAttributes> {
    public LinuxPermissionsSimpleReader(PermissionsConfig config) {
        super(config);
    }

    @Override
    protected int getEffectiveMask(int mask, Path path, PosixFileAttributes attrs) throws IOException {
        if (attrs.isDirectory()) {
            mask = mask | 0111;
        }
        if (attrs == null) {
            attrs = Files.getFileAttributeView(path, PosixFileAttributeView.class, NOFOLLOW_LINKS).readAttributes();
        }
        if (!attrs.permissions().contains(PosixFilePermission.OWNER_WRITE)) {
            mask = mask & 0555;
        }
        return mask;
    }
}
