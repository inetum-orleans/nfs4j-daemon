package io.github.toilal.nsf4j.fs.permission;

import io.github.toilal.nsf4j.config.Permissions;

public interface PermissionsMapperFactory {
    PermissionsMapper newPermissionsMapper(Permissions permissions);
}

