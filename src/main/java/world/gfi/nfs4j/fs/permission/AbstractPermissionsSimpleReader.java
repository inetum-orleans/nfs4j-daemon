package world.gfi.nfs4j.fs.permission;

import world.gfi.nfs4j.config.PermissionsConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

abstract public class AbstractPermissionsSimpleReader<A extends BasicFileAttributes> implements PermissionsReader<A> {
    private final PermissionsConfig config;

    public AbstractPermissionsSimpleReader(PermissionsConfig config) {
        this.config = config;
    }

    @Override
    public int getMask(Path path, A attrs) throws IOException {
        int mask = this.config.getMask();
        return getEffectiveMask(mask, path, attrs);
    }

    protected abstract int getEffectiveMask(int mask, Path path, A attrs) throws IOException;

    @Override
    public int getUid(Path path, A attrs) {
        return this.config.getUid();
    }

    @Override
    public int getGid(Path path, A attrs) {
        return this.config.getGid();
    }
}
