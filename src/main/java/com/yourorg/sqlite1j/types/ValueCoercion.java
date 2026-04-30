package com.yourorg.sqlite1j.types;

public final class ValueCoercion {
    private ValueCoercion() {
    }

    public static DbValue applyAffinity(DbValue value, TypeAffinity affinity) {
        if (value == null || value.isNull() || affinity == TypeAffinity.NONE) {
            return value;
        }
        switch (affinity) {
            case INTEGER:
                return toInteger(value);
            case REAL:
                return toReal(value);
            case TEXT:
                return toText(value);
            case BLOB:
                return toBlob(value);
            default:
                return value;
        }
    }

    public static DbValue toInteger(DbValue value) {
        switch (value.type()) {
            case INTEGER:
                return value;
            case REAL:
                return DbValue.ofInteger(value.asReal().longValue());
            case TEXT:
                return DbValue.ofInteger(parseLong(value.asText()));
            default:
                throw new IllegalArgumentException("Cannot coerce " + value.type() + " to INTEGER");
        }
    }

    public static DbValue toReal(DbValue value) {
        switch (value.type()) {
            case REAL:
                return value;
            case INTEGER:
                return DbValue.ofReal(value.asInteger().doubleValue());
            case TEXT:
                return DbValue.ofReal(parseDouble(value.asText()));
            default:
                throw new IllegalArgumentException("Cannot coerce " + value.type() + " to REAL");
        }
    }

    public static DbValue toText(DbValue value) {
        switch (value.type()) {
            case TEXT:
                return value;
            case INTEGER:
                return DbValue.ofText(String.valueOf(value.asInteger()));
            case REAL:
                return DbValue.ofText(String.valueOf(value.asReal()));
            case BLOB:
                return DbValue.ofText(bytesToHex(value.asBlob()));
            default:
                throw new IllegalArgumentException("Cannot coerce " + value.type() + " to TEXT");
        }
    }

    public static DbValue toBlob(DbValue value) {
        switch (value.type()) {
            case BLOB:
                return value;
            case TEXT:
                return DbValue.ofBlob(value.asText().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            default:
                throw new IllegalArgumentException("Cannot coerce " + value.type() + " to BLOB");
        }
    }

    private static long parseLong(String input) {
        try {
            return Long.parseLong(input.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer literal: '" + input + "'", e);
        }
    }

    private static double parseDouble(String input) {
        try {
            return Double.parseDouble(input.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid real literal: '" + input + "'", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
