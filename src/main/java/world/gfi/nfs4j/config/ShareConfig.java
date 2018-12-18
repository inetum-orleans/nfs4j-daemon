package world.gfi.nfs4j.config;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration of a share.
 */
public class ShareConfig {
    private Path path;
    private String alias;
    private boolean appendDefaultAlias;
    private PermissionsConfig permissions;

    public ShareConfig() {
    }

    public ShareConfig(Path path) {
        this.path = path;
        this.alias = alias;
    }

    public ShareConfig(Path path, String alias) {
        this.path = path;
        this.alias = alias;
    }

    public static ShareConfig fromString(String share) {
        int lastIndex = share.lastIndexOf(':');
        if (lastIndex > 1) {
            return new ShareConfig(Paths.get(share.substring(0, lastIndex)), share.substring(lastIndex + 1));
        } else {
            return new ShareConfig(Paths.get(share));
        }
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isAppendDefaultAlias() {
        return appendDefaultAlias;
    }

    public void setAppendDefaultAlias(boolean appendDefaultAlias) {
        this.appendDefaultAlias = appendDefaultAlias;
    }

    public PermissionsConfig getPermissions() {
        return permissions;
    }

    public void setPermissions(PermissionsConfig permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return "ShareConfig{" +
                "path=" + path +
                ", alias='" + alias + '\'' +
                ", permissions=" + permissions +
                '}';
    }

    public String buildDefaultAlias() {
        String defaultAlias = getPath().toAbsolutePath().normalize().toString().replace(":", "").replace(File.separator, "/");
        if (!defaultAlias.startsWith("/")) {
            defaultAlias = "/" + defaultAlias;
        }
        return defaultAlias;
    }
}
