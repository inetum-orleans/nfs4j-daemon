package io.github.toilal.nsf4j.fs;

import io.github.toilal.nsf4j.fs.handle.UniqueHandleGenerator;
import org.dcache.nfs.vfs.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
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

    public WindowsNioFileSystem(Path root, UniqueHandleGenerator handleGenerator) {
        super(root, handleGenerator);
    }

    @Override
    protected DosFileAttributes getFileAttributes(Path path) throws IOException {
        return Files.getFileAttributeView(path, DosFileAttributeView.class, NOFOLLOW_LINKS).readAttributes();
    }

    @Override
    protected void applyFileAttributesToStat(Stat stat, Path path, DosFileAttributes attrs) throws IOException {
        super.applyFileAttributesToStat(stat, path, attrs);

        stat.setGid(1000);
        stat.setUid(1000);
        int type = attrs.isSymbolicLink() ? Stat.S_IFLNK : attrs.isDirectory() ? Stat.S_IFDIR : Stat.S_IFREG;
        stat.setMode(type | (attrs.isReadOnly() ? 0555 : 0777));
        stat.setNlink(1);
    }

    @Override
    protected void applyOwnershipAndModeToPath(Path target, Subject subject, int mode) {
    }
}
