package world.gfi.nfs4j.fs.handle;

public interface HandleRegistryListener<P> {
    void added(P path, long fileHandle);

    void removed(P path, long fileHandle);

    void replaced(P oldPath, P newPath);
}
