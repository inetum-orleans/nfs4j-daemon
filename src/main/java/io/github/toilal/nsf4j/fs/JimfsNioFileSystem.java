package io.github.toilal.nsf4j.fs;

import io.github.toilal.nsf4j.fs.handle.UniqueHandleGenerator;
import org.dcache.nfs.vfs.Stat;

import javax.security.auth.Subject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;

public class JimfsNioFileSystem extends LinuxNioFileSystem {
    public JimfsNioFileSystem(Path root, UniqueHandleGenerator handleGenerator) {
        super(root, handleGenerator);
    }

    @Override
    protected void applyFileAttributesToStat(Stat stat, Path path, PosixFileAttributes attrs) throws IOException {
        super.applyFileAttributesToStat(stat, path, attrs);

        stat.setGid(1000);
        stat.setUid(1000);
        int type = attrs.isSymbolicLink() ? Stat.S_IFLNK : attrs.isDirectory() ? Stat.S_IFDIR : Stat.S_IFREG;
        stat.setMode(type | (!attrs.permissions().contains(PosixFilePermission.OWNER_WRITE) ? 0555 : 0777));
        stat.setNlink(1);
    }

    @Override
    protected void applyOwnershipAndModeToPath(Path path, Subject subject, int mode) {
    }
}
