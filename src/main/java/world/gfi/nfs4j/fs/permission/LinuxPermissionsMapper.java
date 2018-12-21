package world.gfi.nfs4j.fs.permission;

import org.dcache.auth.Subjects;
import org.dcache.nfs.vfs.Stat;

import javax.security.auth.Subject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

public class LinuxPermissionsMapper implements PermissionsMapper {
    @Override
    public void readPermissions(Path path, BasicFileAttributes attrs, Stat stat) throws IOException {
        stat.setGid((Integer) Files.getAttribute(path, "unix:gid", NOFOLLOW_LINKS));
        stat.setUid((Integer) Files.getAttribute(path, "unix:uid", NOFOLLOW_LINKS));
        stat.setMode((Integer) Files.getAttribute(path, "unix:mode", NOFOLLOW_LINKS));
    }

    @Override
    public void writePermissions(Path path, Subject subject, int mode) throws IOException {
        long uid = Subjects.getUid(subject);
        long gid = Subjects.getPrimaryGid(subject);

        Files.setAttribute(path, "unix:uid", (int) uid, NOFOLLOW_LINKS);
        Files.setAttribute(path, "unix:gid", (int) gid, NOFOLLOW_LINKS);
        Files.setAttribute(path, "unix:mode", mode, NOFOLLOW_LINKS);
    }

    @Override
    public void writePermissions(Path path, Stat stat) throws IOException {
        if (stat.isDefined(Stat.StatAttribute.OWNER)) {
            Files.setAttribute(path, "unix:uid", stat.getUid(), NOFOLLOW_LINKS);
        }

        if (stat.isDefined(Stat.StatAttribute.GROUP)) {
            Files.setAttribute(path, "unix:gid", stat.getGid(), NOFOLLOW_LINKS);
        }

        if (stat.isDefined(Stat.StatAttribute.MODE)) {
            Files.setAttribute(path, "unix:mode", stat.getMode(), NOFOLLOW_LINKS);
        }
    }
}
