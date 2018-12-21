package world.gfi.nfs4j.fs.permission;

import world.gfi.nfs4j.config.PermissionsConfig;
import world.gfi.nfs4j.config.ShareConfig;

import java.io.IOException;

public interface PermissionsMapperFactory {
    PermissionsMapper newPermissionsMapper(PermissionsConfig permissions, ShareConfig share, String alias) throws IOException;
}

