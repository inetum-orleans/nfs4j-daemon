package io.github.toilal.nsf4j.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration of a share.
 */
public class Share {
    private Path path;
    private String alias;
    private Permissions permissions;

    public Share() {
    }

    public Share(Path path) {
        this.path = path;
        this.alias = alias;
    }

    public Share(Path path, String alias) {
        this.path = path;
        this.alias = alias;
    }

    public static Share fromString(String share) {
        int lastIndex = share.lastIndexOf(':');
        if (lastIndex > 1) {
            return new Share(Paths.get(share.substring(0, lastIndex)), share.substring(lastIndex + 1));
        } else {
            return new Share(Paths.get(share));
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

    public Permissions getPermissions() {
        return permissions;
    }

    public void setPermissions(Permissions permissions) {
        this.permissions = permissions;
    }
}
