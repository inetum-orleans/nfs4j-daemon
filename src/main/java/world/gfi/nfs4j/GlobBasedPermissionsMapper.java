package world.gfi.nfs4j;

import org.dcache.nfs.vfs.Stat;
import world.gfi.nfs4j.fs.handle.GlobBasedPathHandleRegistryListener;
import world.gfi.nfs4j.fs.handle.PathHandleRegistryListener;
import world.gfi.nfs4j.fs.permission.PermissionsMapper;

import javax.security.auth.Subject;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GlobBasedPermissionsMapper implements PermissionsMapper {
    private final PermissionsMapper defaultPermissionsMapper;
    private final Map<PathMatcher, PermissionsMapper> globPermissionsMapper = new LinkedHashMap<>();

    public GlobBasedPermissionsMapper(PermissionsMapper defaultPermissionMapper, Map<String, PermissionsMapper> globPermissionsMapper) {
        this.defaultPermissionsMapper = defaultPermissionMapper;
        for (Map.Entry<String, PermissionsMapper> entry : globPermissionsMapper.entrySet()) {
            String syntaxAndPattern = entry.getKey();
            if (!syntaxAndPattern.contains(":")) {
                syntaxAndPattern = "glob:" + syntaxAndPattern;
            }
            final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(syntaxAndPattern);
            this.globPermissionsMapper.put(pathMatcher, entry.getValue());
        }
    }

    protected PermissionsMapper getPermissionsMapperDelegate(Path path) {
        for (Map.Entry<PathMatcher, PermissionsMapper> entry : globPermissionsMapper.entrySet()) {
            if (entry.getKey().matches(path)) {
                return entry.getValue();
            }
        }
        return this.defaultPermissionsMapper;
    }

    @Override
    public void readPermissions(Path path, BasicFileAttributes attrs, Stat stat) throws IOException {
        getPermissionsMapperDelegate(path).readPermissions(path, attrs, stat);
    }

    @Override
    public void writePermissions(Path path, Subject subject, int mode) throws IOException {
        getPermissionsMapperDelegate(path).writePermissions(path, subject, mode);
    }

    @Override
    public void writePermissions(Path path, Stat stat) throws IOException {
        getPermissionsMapperDelegate(path).writePermissions(path, stat);
    }

    /**
     * Get the mapper listener for path handle changes.
     *
     * @return
     */
    @Override
    public PathHandleRegistryListener getHandleRegistryListener() {
        return new GlobBasedPathHandleRegistryListener(defaultPermissionsMapper, globPermissionsMapper);
    }

    /**
     * Get closable resources to be invoked when detaching the associated share.
     *
     * @return
     */
    @Override
    public Closeable getCloseable() {
        List<Closeable> closeables = new ArrayList<>();

        if (defaultPermissionsMapper.getCloseable() != null) {
            closeables.add(defaultPermissionsMapper.getCloseable());
        }

        for (PermissionsMapper mapper : globPermissionsMapper.values()) {
            if (mapper.getCloseable() != null) {
                closeables.add(mapper.getCloseable());
            }
        }

        if (closeables.isEmpty()) {
            return null;
        }

        if (closeables.size() == 1) {
            return closeables.get(0);
        }

        return () -> {
            IOException lastException = null;
            for (Closeable closeable : closeables) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    lastException = e;
                }

            }
            if (lastException != null) {
                throw lastException;
            }
        };
    }


}
