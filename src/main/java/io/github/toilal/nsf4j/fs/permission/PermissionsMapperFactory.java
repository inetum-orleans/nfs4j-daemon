package io.github.toilal.nsf4j.fs.permission;

import io.github.toilal.nsf4j.config.PermissionsConfig;

public interface PermissionsMapperFactory {
    PermissionsMapper newPermissionsMapper(PermissionsConfig permissions);
}

