package world.gfi.nfs4j.exceptions;

public class Nfs4jDaemonException extends Exception {
    public Nfs4jDaemonException() {
    }

    public Nfs4jDaemonException(String message) {
        super(message);
    }

    public Nfs4jDaemonException(String message, Throwable cause) {
        super(message, cause);
    }

    public Nfs4jDaemonException(Throwable cause) {
        super(cause);
    }

    public Nfs4jDaemonException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
