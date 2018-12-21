package world.gfi.nfs4j.fs.permission;

import java.io.IOException;
import java.nio.file.Path;

public interface PermissionsWriter {
    void setMask(Path path, int mask) throws IOException;

    void setUid(Path path, int uid) throws IOException;

    void setGid(Path path, int gid) throws IOException;
}
