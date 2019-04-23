package world.gfi.nfs4j.fs.permission;

import org.apache.commons.lang3.SystemUtils;
import world.gfi.nfs4j.config.PermissionsConfig;
import world.gfi.nfs4j.config.ShareConfig;

import java.io.IOException;

public class DefaultPermissionsMapperFactory implements PermissionsMapperFactory {
    @Override
    public PermissionsMapper newPermissionsMapper(PermissionsConfig permissions, ShareConfig share, String alias) throws IOException {
        switch (permissions.getType()) {
            case DISABLED:
                if (SystemUtils.IS_OS_WINDOWS) {
                    return new SimplePermissionsMapperRead(new WindowsPermissionsSimpleReader(permissions));
                } else {
                    return new SimplePermissionsMapperRead(new LinuxPermissionsSimpleReader(permissions));
                }
            case EMULATED:
                PermissionsMapDb permissionsMapDb;
                if (SystemUtils.IS_OS_WINDOWS) {
                    permissionsMapDb = new PermissionsMapDb<>(share, alias, new WindowsPermissionsSimpleReader(permissions), new DefaultFileIdReader<>()); // WindowsFileIdReader is slower.
                } else {
                    permissionsMapDb = new PermissionsMapDb<>(share, alias, new LinuxPermissionsSimpleReader(permissions), new DefaultFileIdReader<>());
                }
                return new SimplePermissionsMapper(permissionsMapDb, permissionsMapDb);
            case UNIX:
                return new LinuxPermissionsMapper();
        }
        throw new IllegalStateException();
    }
}
