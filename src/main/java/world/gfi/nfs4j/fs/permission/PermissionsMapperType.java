package world.gfi.nfs4j.fs.permission;

public enum PermissionsMapperType {
    /**
     * File permissions are disabled. Best performances, but all files will match default uid, gid and mode.
     */
    DISABLED,
    /**
     * File permissions are emulated using a local database. This may impact performance, but files will keep custom uid gid and mode.
     */
    EMULATED,
    /**
     * File permissions are using native Unix attributes. Good performance, but only supported on Unix file systems.
     */
    UNIX
}
