package com.yourorg.sqlite1j.types;

import java.util.Arrays;
import java.util.Objects;

public final class DbValue {
    private static final DbValue NULL = new DbValue(DbValueType.NULL, null);

    private final DbValueType type;
    private final Object value;

    private DbValue(DbValueType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public static DbValue nullValue() {
        return NULL;
    }

    public static DbValue ofInteger(long value) {
        return new DbValue(DbValueType.INTEGER, value);
    }

    public static DbValue ofReal(double value) {
        return new DbValue(DbValueType.REAL, value);
    }

    public static DbValue ofText(String value) {
        return new DbValue(DbValueType.TEXT, Objects.requireNonNull(value, "text value cannot be null"));
    }

    public static DbValue ofBlob(byte[] value) {
        return new DbValue(DbValueType.BLOB, Arrays.copyOf(Objects.requireNonNull(value, "blob value cannot be null"), value.length));
    }

    public DbValueType type() {
        return type;
    }

    public boolean isNull() {
        return type == DbValueType.NULL;
    }

    public Long asInteger() {
        ensureType(DbValueType.INTEGER);
        return (Long) value;
    }

    public Double asReal() {
        ensureType(DbValueType.REAL);
        return (Double) value;
    }

    public String asText() {
        ensureType(DbValueType.TEXT);
        return (String) value;
    }

    public byte[] asBlob() {
        ensureType(DbValueType.BLOB);
        return Arrays.copyOf((byte[]) value, ((byte[]) value).length);
    }

    private void ensureType(DbValueType expected) {
        if (type != expected) {
            throw new IllegalStateException("Expected " + expected + " but was " + type);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DbValue)) return false;
        DbValue dbValue = (DbValue) o;
        if (type != dbValue.type) return false;
        if (type == DbValueType.BLOB) {
            return Arrays.equals((byte[]) value, (byte[]) dbValue.value);
        }
        return Objects.equals(value, dbValue.value);
    }

    @Override
    public int hashCode() {
        return type == DbValueType.BLOB
                ? 31 * type.hashCode() + Arrays.hashCode((byte[]) value)
                : Objects.hash(type, value);
    }

    @Override
    public String toString() {
        if (type == DbValueType.BLOB) {
            return "DbValue{type=" + type + ", value=<" + ((byte[]) value).length + " bytes>}";
        }
        return "DbValue{type=" + type + ", value=" + value + '}';
    }
}
