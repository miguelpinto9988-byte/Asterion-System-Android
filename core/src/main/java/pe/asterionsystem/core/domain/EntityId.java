package pe.asterionsystem.core.domain;

import java.util.Objects;

/** Generic stable identifier; no feature-specific entity is defined yet. */
public final class EntityId {
    private final String value;

    public EntityId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("EntityId must not be blank");
        }
        this.value = value.trim();
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof EntityId
                && value.equals(((EntityId) other).value));
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
