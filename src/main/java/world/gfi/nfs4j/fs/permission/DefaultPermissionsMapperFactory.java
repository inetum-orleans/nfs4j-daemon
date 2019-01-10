package world.gfi.nfs4j.fs.permission;

import org.apache.commons.lang3.SystemUtils;
import world.gfi.nfs4j.config.PermissionsConfig;
import world.gfi.nfs4j.config.ShareConfig;

import java.io.IOException;

public class DefaultPermissionsMapperFactory implements PermissionsMapperFactory {
    @Override
    public PermissionsMapper newPermissionsMapper(PermissionsConfig permissions, ShareConfig share, String alias) throws IOException {
        switch (permissions.getType()) {
            case SIMPLE:
                if (SystemUtils.IS_OS_WINDOWS) {
                    return new SimplePermissionsMapperRead(new WindowsPermissionsSimpleReader(permissions));
                } else {
                    return new SimplePermissionsMapperRead(new LinuxPermissionsSimpleReader(permissions));
                }
            case ADVANCED:
                if (SystemUtils.IS_OS_WINDOWS) {
                    PermissionsMapDb permissionsMapDb = new PermissionsMapDb<>(share, alias, new WindowsPermissionsSimpleReader(permissions), new WindowsFileIdReader<>());
                    return new SimplePermissionsMapper(permissionsMapDb, permissionsMapDb);
                } else {
                    PermissionsMapDb permissionsMapDb = new PermissionsMapDb<>(share, alias, new LinuxPermissionsSimpleReader(permissions), new DefaultFileIdReader<>());
                    return new SimplePermissionsMapper(permissionsMapDb, permissionsMapDb);
                }
            case LINUX:
                return new LinuxPermissionsMapper();
        }
        throw new IllegalStateException();
    }
}
