package world.gfi.nfs4j.exceptions;

public class AttachException extends Nfs4jDaemonException {
    public AttachException() {
    }

    public AttachException(String message) {
        super(message);
    }

    public AttachException(String message, Throwable cause) {
        super(message, cause);
    }

    public AttachException(Throwable cause) {
        super(cause);
    }

    public AttachException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
