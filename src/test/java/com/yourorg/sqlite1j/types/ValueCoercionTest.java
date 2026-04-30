package com.yourorg.sqlite1j.types;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValueCoercionTest {
    @Test
    void appliesIntegerAffinityFromTextAndReal() {
        assertEquals(123L, ValueCoercion.applyAffinity(DbValue.ofText("123"), TypeAffinity.INTEGER).asInteger());
        assertEquals(42L, ValueCoercion.applyAffinity(DbValue.ofReal(42.8), TypeAffinity.INTEGER).asInteger());
    }

    @Test
    void appliesRealAffinityFromTextAndInteger() {
        assertEquals(12.5, ValueCoercion.applyAffinity(DbValue.ofText("12.5"), TypeAffinity.REAL).asReal());
        assertEquals(8.0, ValueCoercion.applyAffinity(DbValue.ofInteger(8), TypeAffinity.REAL).asReal());
    }

    @Test
    void appliesTextAndBlobAffinity() {
        assertEquals("abc", ValueCoercion.applyAffinity(DbValue.ofText("abc"), TypeAffinity.TEXT).asText());
        assertArrayEquals("abc".getBytes(java.nio.charset.StandardCharsets.UTF_8),
                ValueCoercion.applyAffinity(DbValue.ofText("abc"), TypeAffinity.BLOB).asBlob());
    }

    @Test
    void keepsNullAndNoneAffinityUntouched() {
        assertEquals(DbValue.nullValue(), ValueCoercion.applyAffinity(DbValue.nullValue(), TypeAffinity.INTEGER));
        assertEquals(DbValue.ofText("x"), ValueCoercion.applyAffinity(DbValue.ofText("x"), TypeAffinity.NONE));
    }

    @Test
    void rejectsInvalidNumericText() {
        assertThrows(IllegalArgumentException.class,
                () -> ValueCoercion.applyAffinity(DbValue.ofText("abc"), TypeAffinity.INTEGER));
        assertThrows(IllegalArgumentException.class,
                () -> ValueCoercion.applyAffinity(DbValue.ofText("12x"), TypeAffinity.REAL));
    }
}
