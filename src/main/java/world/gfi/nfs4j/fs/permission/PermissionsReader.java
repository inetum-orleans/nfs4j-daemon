package world.gfi.nfs4j.fs.permission;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public interface PermissionsReader<A extends BasicFileAttributes> {
    /**
     * Get the mask of the given path.
     *
     * @param path  path to read
     * @param attrs file attributes. If null, it should be retrieved from path.
     * @return the mask
     */
    int getMask(Path path, A attrs) throws IOException;

    /**
     * Get the uid of the given path.
     *
     * @param path  path to read
     * @param attrs file attributes. If null, it should be retrieved from path.
     * @return the uid
     */
    int getUid(Path path, A attrs) throws IOException;

    /**
     * The the gid of the given path.
     *
     * @param path  path to read
     * @param attrs file attributes. If null, it should be retrieved from path.
     * @return the gid
     */
    int getGid(Path path, A attrs) throws IOException;
}
