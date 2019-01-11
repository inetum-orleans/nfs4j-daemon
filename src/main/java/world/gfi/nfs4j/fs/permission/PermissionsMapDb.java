package world.gfi.nfs4j.fs.permission;

import com.sun.jna.platform.FileUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import world.gfi.nfs4j.config.ShareConfig;
import world.gfi.nfs4j.fs.handle.PathHandleRegistryListener;
import world.gfi.nfs4j.utils.FileNameSanitizer;
import world.gfi.nfs4j.utils.JnaWindowsUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ConcurrentMap;

public class PermissionsMapDb<A extends BasicFileAttributes> implements PermissionsReader<A>, PermissionsWriter, PathHandleRegistryListener, Closeable {
    private final DB db;
    private final ConcurrentMap<String, Long> id;
    private final ConcurrentMap<String, Integer> maskMap;
    private final ConcurrentMap<String, Integer> uidMap;
    private final ConcurrentMap<String, Integer> gidMap;
    private final PermissionsReader<A> defaultPermissions;
    private final FileIdReader<A> idReader;
    private final ShareConfig share;

    public static String getFilename(String alias) {
        if (alias == null) {
            alias = "";
        }
        if (alias.startsWith("/")) {
            alias = alias.substring(1);
        }

        alias = FileNameSanitizer.sanitize(alias);

        if (alias.length() > 0) {
            alias += ".permissions.db";
        } else {
            alias = "permissions.db";
        }

        return alias;
    }

    public PermissionsMapDb(ShareConfig share, String alias, PermissionsReader<A> defaultPermissions, FileIdReader<A> idReader) throws IOException {
        this.share = share;
        this.defaultPermissions = defaultPermissions;
        this.idReader = idReader;

        File dbFile;
        if (this.share.isLocalMetadata()) {
            String basePath = this.share.getPath().toString();
            Path directory = Paths.get(basePath, ".nfs4j").normalize();
            Files.createDirectories(directory);
            dbFile = Paths.get(directory.toString(), getFilename(alias)).normalize().toFile();
        } else {
            Path aliasPath = Paths.get(alias);
            Path directory = Paths.get(System.getProperty("user.home"), ".nfs4j", aliasPath.toString());
            Files.createDirectories(directory);
            dbFile = Paths.get(directory.toString(), getFilename(null)).normalize().toFile();
        }

        this.db = DBMaker.fileDB(dbFile)
                .checksumHeaderBypass()
                .closeOnJvmShutdown()
                .fileMmapEnable()
                .fileMmapEnableIfSupported()
                .fileMmapPreclearDisable()
                .cleanerHackEnable()
                .make();

        this.id = db.hashMap("id", Serializer.STRING, Serializer.LONG).createOrOpen();
        this.maskMap = db.hashMap("mask", Serializer.STRING, Serializer.INTEGER).createOrOpen();
        this.uidMap = db.hashMap("uid", Serializer.STRING, Serializer.INTEGER).createOrOpen();
        this.gidMap = db.hashMap("gid", Serializer.STRING, Serializer.INTEGER).createOrOpen();

        this.cleanup();
    }

    public void cleanup() {
        this.id.keySet().removeIf(p -> !new File(p).exists());
        this.maskMap.keySet().removeIf(p -> !new File(p).exists());
        this.uidMap.keySet().removeIf(p -> !new File(p).exists());
        this.gidMap.keySet().removeIf(p -> !new File(p).exists());
    }


    /**
     * This function checks that the given path match the same file object as the one used previously.
     * <p>
     * If it doesn't match, it forget the permissions defined on the path.
     * <p>
     * This uses JNA on windows to get file unique identifier, with a fallback on creation time.
     * <p>
     * Note that deleting and creating a file may not update the creation time on windows, see @{link https://stackoverflow.com/questions/28285523/strange-timestamp-duplication-when-renaming-and-recreating-a-file}
     *
     * @param path
     * @param attrs
     * @see
     */
    protected void checkFileId(Path path, A attrs) {
        long fileId = this.idReader.getFileId(path, attrs);

        String key = path.normalize().toString();

        Long existingId = this.id.get(key);
        if (existingId == null) {
            this.id.put(key, fileId);
        } else if (existingId != fileId) {
            this.removed(path, -1);
        }
    }

    @Override
    public int getMask(Path path, A attrs) throws IOException {
        this.checkFileId(path, attrs);
        return getMaskImpl(path, attrs);
    }

    private int getMaskImpl(Path path, A attrs) throws IOException {
        return this.maskMap.getOrDefault(path.normalize().toString(), this.defaultPermissions.getMask(path, attrs));
    }

    @Override
    public int getUid(Path path, A attrs) throws IOException {
        this.checkFileId(path, attrs);
        return this.getUidImpl(path, attrs);
    }

    private int getUidImpl(Path path, A attrs) throws IOException {
        return this.uidMap.getOrDefault(path.normalize().toString(), this.defaultPermissions.getUid(path, attrs));
    }

    @Override
    public int getGid(Path path, A attrs) throws IOException {
        this.checkFileId(path, attrs);
        return this.getGidImpl(path, attrs);
    }

    private int getGidImpl(Path path, A attrs) throws IOException {
        return this.gidMap.getOrDefault(path.normalize().toString(), this.defaultPermissions.getGid(path, attrs));
    }

    @Override
    public int[] getPermissions(Path path, A attrs) throws IOException {
        this.checkFileId(path, attrs);
        return new int[]{getUidImpl(path, attrs), getGidImpl(path, attrs), getMaskImpl(path, attrs)};
    }

    @Override
    public void setMask(Path path, int mask) throws IOException {
        int defaultMask = this.defaultPermissions.getMask(path, null);
        if (defaultMask == mask) {
            this.maskMap.remove(path.normalize().toString());
        } else {
            this.maskMap.put(path.normalize().toString(), mask);
        }
    }

    @Override
    public void setUid(Path path, int uid) throws IOException {
        int defaultUid = this.defaultPermissions.getUid(path, null);
        if (defaultUid == uid) {
            this.uidMap.remove(path.normalize().toString());
        } else {
            this.uidMap.put(path.normalize().toString(), uid);
        }
    }

    @Override
    public void setGid(Path path, int gid) throws IOException {
        int defaultGid = this.defaultPermissions.getGid(path, null);
        if (defaultGid == gid) {
            this.gidMap.remove(path.normalize().toString());
        } else {
            this.gidMap.put(path.normalize().toString(), gid);
        }
    }

    @Override
    public void added(Path path, long fileHandle) {
    }

    @Override
    public void removed(Path path, long fileHandle) {
        String key = path.normalize().toString();

        this.id.remove(key);
        this.maskMap.remove(key);
        this.uidMap.remove(key);
        this.gidMap.remove(key);
    }

    @Override
    public void replaced(Path oldPath, Path newPath) {
        String oldKey = oldPath.normalize().toString();
        String newKey = newPath.normalize().toString();

        Long oldCreationTime = this.id.remove(oldKey);
        Integer oldMask = this.maskMap.remove(oldKey);
        Integer oldUid = this.uidMap.remove(oldKey);
        Integer oldGid = this.gidMap.remove(oldKey);

        if (oldCreationTime != null) {
            this.id.put(newKey, oldCreationTime);
        }

        if (oldMask != null) {
            this.maskMap.put(newKey, oldMask);
        }

        if (oldUid != null) {
            this.uidMap.put(newKey, oldUid);
        }

        if (oldGid != null) {
            this.gidMap.put(newKey, oldGid);
        }
    }

    @Override
    public void close() throws IOException {
        this.db.close();
    }
}
