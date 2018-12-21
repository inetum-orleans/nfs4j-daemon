package world.gfi.nfs4j.fs.permission;

import com.sun.jna.LastErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import world.gfi.nfs4j.utils.JnaWindowsUtils;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class WindowsFileIdReader<A extends BasicFileAttributes> extends DefaultFileIdReader<A> implements FileIdReader<A> {
    private static Logger LOG = LoggerFactory.getLogger(WindowsFileIdReader.class);

    @Override
    public long getFileId(Path path, A attrs) {
        try {
            return JnaWindowsUtils.getFileId(path);
        } catch (LastErrorException e) {
            LOG.warn("Can't read File id with JNA. Falling back to creation time.", e);
            return super.getFileId(path, attrs);
        }
    }
}
