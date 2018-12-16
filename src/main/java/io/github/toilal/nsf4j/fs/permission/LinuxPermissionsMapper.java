package io.github.toilal.nsf4j.fs.permission;

import org.dcache.auth.GidPrincipal;
import org.dcache.auth.UidPrincipal;
import org.dcache.nfs.vfs.Stat;

import javax.security.auth.Subject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.Principal;

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
        Integer uid = null;
        Integer gid = null;

        for (Principal principal : subject.getPrincipals()) {
            if (principal instanceof UidPrincipal) {
                uid = (int) ((UidPrincipal) principal).getUid();
            }
            if (principal instanceof GidPrincipal) {
                gid = (int) ((GidPrincipal) principal).getGid();
            }
        }

        Files.setAttribute(path, "unix:uid", uid, NOFOLLOW_LINKS);
        Files.setAttribute(path, "unix:gid", gid, NOFOLLOW_LINKS);
        Files.setAttribute(path, "unix:mode", mode, NOFOLLOW_LINKS);
    }
}
