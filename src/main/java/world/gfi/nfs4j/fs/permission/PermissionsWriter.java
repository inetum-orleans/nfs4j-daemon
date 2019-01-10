package world.gfi.nfs4j.fs.permission;

import java.io.IOException;
import java.nio.file.Path;

public interface PermissionsWriter {
    void setMask(Path path, int mask) throws IOException;

    void setUid(Path path, int uid) throws IOException;

    void setGid(Path path, int gid) throws IOException;

    default void setPermissions(Path path, Integer uid, Integer gid, Integer mask) throws IOException {
        if (uid != null) {
            this.setUid(path, uid);
        }
        if (gid != null) {
            this.setGid(path, gid);
        }
        if (mask != null) {
            this.setMask(path, mask);
        }
    }
}
