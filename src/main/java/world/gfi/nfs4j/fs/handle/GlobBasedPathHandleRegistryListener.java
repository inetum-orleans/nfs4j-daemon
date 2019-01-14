package world.gfi.nfs4j.fs.handle;

import world.gfi.nfs4j.fs.permission.PermissionsMapper;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Map;

public class GlobBasedPathHandleRegistryListener implements PathHandleRegistryListener {
    private final PermissionsMapper defaultPermissionsMapper;
    private final Map<PathMatcher, PermissionsMapper> globPermissionsMapper;

    private PathHandleRegistryListener noop = new PathHandleRegistryListener() {
        @Override
        public void added(Path path, long fileHandle) {
            //Do nothing
        }

        @Override
        public void removed(Path path, long fileHandle) {
            //Do nothing
        }

        @Override
        public void replaced(Path oldPath, Path newPath, long fileHandle) {
            //Do nothing
        }
    };

    public GlobBasedPathHandleRegistryListener(PermissionsMapper defaultPermissionsMapper, Map<PathMatcher, PermissionsMapper> globPermissionsMapper) {
        this.defaultPermissionsMapper = defaultPermissionsMapper;
        this.globPermissionsMapper = globPermissionsMapper;
    }

    protected PathHandleRegistryListener getPathHandleRegistryListenerDelegate(Path path) {
        PermissionsMapper mapper = this.defaultPermissionsMapper;
        for (Map.Entry<PathMatcher, PermissionsMapper> entry : globPermissionsMapper.entrySet()) {
            if (entry.getKey().matches(path)) {
                mapper = entry.getValue();
                break;
            }
        }
        PathHandleRegistryListener registryListener = mapper.getHandleRegistryListener();
        if (registryListener != null) {
            return registryListener;
        }
        return noop;
    }

    @Override
    public void added(Path path, long fileHandle) {
        getPathHandleRegistryListenerDelegate(path).added(path, fileHandle);
    }

    @Override
    public void removed(Path path, long fileHandle) {
        getPathHandleRegistryListenerDelegate(path).removed(path, fileHandle);
    }

    @Override
    public void replaced(Path oldPath, Path newPath, long fileHandle) {
        PathHandleRegistryListener oldHandler = getPathHandleRegistryListenerDelegate(oldPath);
        PathHandleRegistryListener newHandler = getPathHandleRegistryListenerDelegate(newPath);

        if (oldHandler == newHandler) {
            newHandler.replaced(oldPath, newPath, fileHandle);
        } else {
            oldHandler.removed(oldPath, fileHandle);
            newHandler.added(newPath, fileHandle);
        }
    }
}
