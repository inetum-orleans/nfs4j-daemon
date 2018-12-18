package world.gfi.nfs4j.exceptions;

public class NoSuchAliasException extends AttachException {
    private final String alias;

    public NoSuchAliasException(String alias) {
        super("Alias " + alias + " does not exists.");
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }
}
