package com.rusefi.can.reader.dbc;

import org.junit.Test;

import static com.rusefi.can.reader.dbc.DbcFile.compatibilityWithBrokenRusEfiLogic;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DbcFieldTest {
    @Test
    public void testBigEndian() {
        {
            // todo: sorry I do not trust this test :(
            compatibilityWithBrokenRusEfiLogic = false;
            DbcField field = create(true);
            assertFalse(field.coversByte(0));
            assertFalse(field.coversByte(1));
            assertFalse(field.coversByte(2));
            assertFalse(field.coversByte(3));
        }
        {
            compatibilityWithBrokenRusEfiLogic = true;
            DbcField field = create(true);
            assertTrue(field.coversByte(0));
            assertTrue(field.coversByte(1));
            assertFalse(field.coversByte(3));
        }
        compatibilityWithBrokenRusEfiLogic = false;
    }

    private static DbcField create(boolean isBigEndian) {
        int startBit = 8;
        int length = 16;

        startBit = DbcField.crazyMotorolaMath(startBit, length, isBigEndian);
        return new DbcField(-1, "", startBit, length, 1, 0, null, isBigEndian);
    }

    @Test
    public void testLittleEndian() {
        DbcField field = create(false);
        assertFalse(field.coversByte(0));
        assertTrue(field.coversByte(1));
        assertTrue(field.coversByte(2));
        assertFalse(field.coversByte(3));
    }
}
