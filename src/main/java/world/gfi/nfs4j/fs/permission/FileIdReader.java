package world.gfi.nfs4j.fs.permission;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public interface FileIdReader<A extends BasicFileAttributes> {
    long getFileId(Path path, A attrs);
}
