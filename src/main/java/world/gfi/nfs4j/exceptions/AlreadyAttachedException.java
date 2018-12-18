package world.gfi.nfs4j.exceptions;

import world.gfi.nfs4j.fs.AttachableFileSystem;

public class AlreadyAttachedException extends AttachException {
    private final AttachableFileSystem fs;

    public AlreadyAttachedException(AttachableFileSystem fs) {
        super("FileSystem is already attached (" + fs.getAlias() + ")");
        this.fs = fs;
    }

    public AttachableFileSystem getFs() {
        return fs;
    }
}
