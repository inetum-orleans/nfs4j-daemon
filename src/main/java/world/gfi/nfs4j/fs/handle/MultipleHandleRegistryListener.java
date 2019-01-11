package world.gfi.nfs4j.fs.handle;

import java.nio.file.Path;

public class MultipleHandleRegistryListener implements PathHandleRegistryListener {
    private final Iterable<PathHandleRegistryListener> listeners;

    public MultipleHandleRegistryListener(Iterable<PathHandleRegistryListener> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void added(Path path, long fileHandle) {
        for (PathHandleRegistryListener listener : this.listeners) {
            this.added(path, fileHandle);
        }
    }

    @Override
    public void removed(Path path, long fileHandle) {
        for (PathHandleRegistryListener listener : this.listeners) {
            this.removed(path, fileHandle);
        }
    }

    @Override
    public void replaced(Path oldPath, Path newPath) {
        for (PathHandleRegistryListener listener : this.listeners) {
            this.replaced(oldPath, newPath);
        }
    }
}
