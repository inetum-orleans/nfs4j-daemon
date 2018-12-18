package world.gfi.nfs4j.fs.permission;

import world.gfi.nfs4j.config.PermissionsConfig;

public interface PermissionsMapperFactory {
    PermissionsMapper newPermissionsMapper(PermissionsConfig permissions);
}

