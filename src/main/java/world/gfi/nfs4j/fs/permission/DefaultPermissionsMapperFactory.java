package world.gfi.nfs4j.fs.permission;

import world.gfi.nfs4j.config.PermissionsConfig;
import world.gfi.nfs4j.config.ShareConfig;

import java.io.IOException;

public class DefaultPermissionsMapperFactory implements PermissionsMapperFactory {
    @Override
    public PermissionsMapper newPermissionsMapper(PermissionsConfig permissions, ShareConfig share, String alias) throws IOException {
        switch (permissions.getType()) {
            case SIMPLE:
                if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
                    return new SimplePermissionsMapperRead(new WindowsPermissionsSimpleReader(permissions));
                } else {
                    return new SimplePermissionsMapperRead(new LinuxPermissionsSimpleReader(permissions));
                }
            case ADVANCED:
                if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
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
