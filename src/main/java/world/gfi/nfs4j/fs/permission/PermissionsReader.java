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
     * Get the gid of the given path.
     *
     * @param path  path to read
     * @param attrs file attributes. If null, it should be retrieved from path.
     * @return the gid
     */
    int getGid(Path path, A attrs) throws IOException;

    /**
     * Get the uid, gid and mask of given path.
     *
     * @param path
     * @param attrs
     * @return uid, gid and mask
     * @throws IOException
     */
    default int[] getPermissions(Path path, A attrs) throws IOException {
        return new int[]{getUid(path, attrs), getGid(path, attrs), getMask(path, attrs)};
    }
}
