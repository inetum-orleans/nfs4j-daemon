package io.github.toilal.nsf4j.fs;

import io.github.toilal.nsf4j.fs.handle.UniqueHandleGenerator;
import org.dcache.auth.GidPrincipal;
import org.dcache.auth.UidPrincipal;
import org.dcache.nfs.vfs.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.security.Principal;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

/**
 * A Linux implementation of {@link org.dcache.nfs.vfs.VirtualFileSystem}.
 */
public class LinuxNioFileSystem extends AbstractNioFileSystem<PosixFileAttributes> {
    private static final Logger LOG = LoggerFactory.getLogger(LinuxNioFileSystem.class);

    private final UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();

    public LinuxNioFileSystem(Path root, UniqueHandleGenerator handleGenerator) {
        super(root, handleGenerator);
    }

    @Override
    protected PosixFileAttributes getFileAttributes(Path path) throws IOException {
        return Files.getFileAttributeView(path, PosixFileAttributeView.class, NOFOLLOW_LINKS).readAttributes();
    }

    @Override
    protected void applyFileAttributesToStat(Stat stat, Path path, PosixFileAttributes attrs) throws IOException {
        super.applyFileAttributesToStat(stat, path, attrs);

        stat.setGid((Integer) Files.getAttribute(path, "unix:gid", NOFOLLOW_LINKS));
        stat.setUid((Integer) Files.getAttribute(path, "unix:uid", NOFOLLOW_LINKS));
        stat.setMode((Integer) Files.getAttribute(path, "unix:mode", NOFOLLOW_LINKS));
        stat.setNlink((Integer) Files.getAttribute(path, "unix:nlink", NOFOLLOW_LINKS));
    }

    @Override
    protected void applyOwnershipAndModeToPath(Path path, Subject subject, int mode) {
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

        if (uid != null) {
            try {
                Files.setAttribute(path, "unix:uid", uid, NOFOLLOW_LINKS);
            } catch (IOException e) {
                LOG.warn("Unable to chown file {}: {}", path, e.getMessage());
            }
        } else {
            LOG.warn("File created without uid: {}", path);
        }
        if (gid != null) {
            try {
                Files.setAttribute(path, "unix:gid", gid, NOFOLLOW_LINKS);
            } catch (IOException e) {
                LOG.warn("Unable to chown file {}: {}", path, e.getMessage());
            }
        } else {
            LOG.warn("File created without gid: {}", path);
        }

        try {
            Files.setAttribute(path, "unix:mode", mode, NOFOLLOW_LINKS);
        } catch (IOException e) {
            LOG.warn("Unable to set mode of file {}: {}", path, e.getMessage());
        }
    }

    @Override
    protected void applyStatToPath(Stat stat, Path path) throws IOException {
        super.applyStatToPath(stat, path);

        PosixFileAttributeView attributeView = Files.getFileAttributeView(path, PosixFileAttributeView.class, NOFOLLOW_LINKS);

        if (stat.isDefined(Stat.StatAttribute.OWNER)) {
            try {
                String uid = String.valueOf(stat.getUid());
                UserPrincipal user = lookupService.lookupPrincipalByName(uid);
                attributeView.setOwner(user);
            } catch (IOException e) {
                throw new UnsupportedOperationException("set uid failed: " + e.getMessage(), e);
            }
        }

        if (stat.isDefined(Stat.StatAttribute.GROUP)) {
            try {
                String gid = String.valueOf(stat.getGid());
                GroupPrincipal group = lookupService.lookupPrincipalByGroupName(gid);
                attributeView.setGroup(group);
            } catch (IOException e) {
                throw new UnsupportedOperationException("set gid failed: " + e.getMessage(), e);
            }
        }

        if (stat.isDefined(Stat.StatAttribute.MODE)) {
            try {
                Files.setAttribute(path, "unix:mode", stat.getMode(), NOFOLLOW_LINKS);
            } catch (IOException e) {
                throw new UnsupportedOperationException("set mode unsupported: " + e.getMessage(), e);
            }
        }
    }
}
