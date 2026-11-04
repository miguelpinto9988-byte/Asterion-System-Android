package pe.asterionsystem.core.offline;

/** Platform-neutral port; implementation must inspect only local storage. */
public interface LocalStorageCheck {
    LocalStorageReport inspect();
}
