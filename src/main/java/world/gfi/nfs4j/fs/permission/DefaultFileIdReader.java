package world.gfi.nfs4j.fs.permission;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class DefaultFileIdReader<A extends BasicFileAttributes> implements FileIdReader<A> {
    @Override
    public long getFileId(Path path, A attrs) {
        return attrs.creationTime().toMillis();
    }
}
