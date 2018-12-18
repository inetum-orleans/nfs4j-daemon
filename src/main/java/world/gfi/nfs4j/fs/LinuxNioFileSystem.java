package world.gfi.nfs4j.fs;

import world.gfi.nfs4j.fs.handle.UniqueHandleGenerator;
import world.gfi.nfs4j.fs.permission.PermissionsMapper;
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

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

/**
 * A Linux implementation of {@link org.dcache.nfs.vfs.VirtualFileSystem}.
 */
public class LinuxNioFileSystem extends AbstractNioFileSystem<PosixFileAttributes> {
    private static final Logger LOG = LoggerFactory.getLogger(LinuxNioFileSystem.class);

    private final UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();

    public LinuxNioFileSystem(Path root, PermissionsMapper permissionsMapper, UniqueHandleGenerator handleGenerator) {
        super(root, permissionsMapper, handleGenerator);
    }

    @Override
    protected PosixFileAttributes getFileAttributes(Path path) throws IOException {
        return Files.getFileAttributeView(path, PosixFileAttributeView.class, NOFOLLOW_LINKS).readAttributes();
    }

    @Override
    protected void applyFileAttributesToStat(Stat stat, Path path, PosixFileAttributes attrs) throws IOException {
        super.applyFileAttributesToStat(stat, path, attrs);
        stat.setNlink((Integer) Files.getAttribute(path, "unix:nlink", NOFOLLOW_LINKS));

        this.permissionsMapper.readPermissions(path, attrs, stat);
    }

    @Override
    protected void applyOwnershipAndModeToPath(Path path, Subject subject, int mode) throws IOException {
        this.permissionsMapper.writePermissions(path, subject, mode);
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
