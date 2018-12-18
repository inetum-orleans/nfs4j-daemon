package world.gfi.nfs4j.exceptions;

import world.gfi.nfs4j.fs.AttachableFileSystem;

public class AliasAlreadyExistsException extends AttachException {
    private final String alias;
    private final AttachableFileSystem existingFs;
    private final AttachableFileSystem fs;

    public AliasAlreadyExistsException(String alias, AttachableFileSystem existingFs, AttachableFileSystem fs) {
        super("Alias " + alias + " is already used by another attached share.");
        this.alias = alias;
        this.existingFs = existingFs;
        this.fs = fs;
    }

    public String getAlias() {
        return alias;
    }

    public AttachableFileSystem getExistingFs() {
        return existingFs;
    }

    public AttachableFileSystem getFs() {
        return fs;
    }
}
