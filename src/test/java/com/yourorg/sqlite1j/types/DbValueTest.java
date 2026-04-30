package com.yourorg.sqlite1j.types;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DbValueTest {
    @Test
    void createsNullValue() {
        DbValue value = DbValue.nullValue();
        assertTrue(value.isNull());
        assertEquals(DbValueType.NULL, value.type());
    }

    @Test
    void createsScalarValues() {
        assertEquals(42L, DbValue.ofInteger(42).asInteger());
        assertEquals(3.14, DbValue.ofReal(3.14).asReal());
        assertEquals("abc", DbValue.ofText("abc").asText());
    }

    @Test
    void blobIsDefensivelyCopied() {
        byte[] input = new byte[]{1, 2, 3};
        DbValue blob = DbValue.ofBlob(input);
        input[0] = 9;
        assertArrayEquals(new byte[]{1, 2, 3}, blob.asBlob());

        byte[] read = blob.asBlob();
        read[1] = 8;
        assertArrayEquals(new byte[]{1, 2, 3}, blob.asBlob());
    }

    @Test
    void equalityWorksForBlobAndScalar() {
        assertEquals(DbValue.ofInteger(7), DbValue.ofInteger(7));
        assertNotEquals(DbValue.ofInteger(7), DbValue.ofInteger(8));
        assertEquals(DbValue.ofBlob(new byte[]{1, 2}), DbValue.ofBlob(new byte[]{1, 2}));
        assertNotEquals(DbValue.ofBlob(new byte[]{1, 2}), DbValue.ofBlob(new byte[]{2, 1}));
    }

    @Test
    void throwsOnWrongAccessor() {
        DbValue value = DbValue.ofText("x");
        assertThrows(IllegalStateException.class, value::asInteger);
    }
}
