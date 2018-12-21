package world.gfi.nfs4j.fs;

import org.dcache.nfs.vfs.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import world.gfi.nfs4j.fs.handle.UniqueHandleGenerator;
import world.gfi.nfs4j.fs.permission.PermissionsMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

/**
 * A windows implementation of {@link org.dcache.nfs.vfs.VirtualFileSystem}.
 */
public class WindowsNioFileSystem extends AbstractNioFileSystem<DosFileAttributes> {
    private static final Logger LOG = LoggerFactory.getLogger(WindowsNioFileSystem.class);

    public WindowsNioFileSystem(Path root, PermissionsMapper permissionsMapper, UniqueHandleGenerator handleGenerator) {
        super(root, permissionsMapper, handleGenerator);
    }

    @Override
    protected DosFileAttributes getFileAttributes(Path path) throws IOException {
        return Files.getFileAttributeView(path, DosFileAttributeView.class, NOFOLLOW_LINKS).readAttributes();
    }

    @Override
    protected void applyFileAttributesToStat(Stat stat, Path path, DosFileAttributes attrs) throws IOException {
        super.applyFileAttributesToStat(stat, path, attrs);
        stat.setNlink(1);
    }
}
