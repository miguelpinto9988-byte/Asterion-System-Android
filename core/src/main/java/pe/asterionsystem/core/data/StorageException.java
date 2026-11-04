package pe.asterionsystem.core.data;

/** Platform-neutral failure of a storage operation, preserving its cause. */
public final class StorageException extends RuntimeException {
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
