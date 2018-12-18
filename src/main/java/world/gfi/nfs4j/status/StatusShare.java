package world.gfi.nfs4j.status;

public class StatusShare {
    private String alias;
    private String path;

    public StatusShare(String alias, String path) {
        this.alias = alias;
        this.path = path;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
