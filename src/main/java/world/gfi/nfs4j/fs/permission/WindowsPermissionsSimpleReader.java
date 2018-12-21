package world.gfi.nfs4j.fs.permission;

import world.gfi.nfs4j.config.PermissionsConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

public class WindowsPermissionsSimpleReader extends AbstractPermissionsSimpleReader<DosFileAttributes> {
    public WindowsPermissionsSimpleReader(PermissionsConfig config) {
        super(config);
    }

    @Override
    protected int getEffectiveMask(int mask, Path path, DosFileAttributes attrs) throws IOException {
        if (attrs == null) {
            attrs = Files.getFileAttributeView(path, DosFileAttributeView.class, NOFOLLOW_LINKS).readAttributes();
        }
        if (attrs.isDirectory()) {
            mask = mask | 0111;
        }
        if (attrs.isReadOnly()) {
            mask = mask & 0555;
        }
        return mask;
    }
}
