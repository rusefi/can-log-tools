package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class GrowingValuesScannerTest {
    @Test
    public void test() {
        int id = 13;

        Map<ByteRateOfChange.ByteId, GrowingValuesScanner.ByteState> result = GrowingValuesScanner.runScanner(Arrays.asList(
                new CANPacket(0, id, new byte[]{3,1,0}),
                new CANPacket(0, id, new byte[]{4,1,0}),
                new CANPacket(0, id, new byte[]{3,2,0})
        ), 1);
        assertEquals(3, result.size());

        List<GrowingValuesScanner.ByteState> list = new ArrayList<>(result.values());

        assertFalse(list.get(0).isIncrementByte());
        assertTrue(list.get(1).isIncrementByte());
        assertFalse(list.get(2).isIncrementByte());
    }

    @Test
    public void testDelta() {
        int id = 13;

        Map<ByteRateOfChange.ByteId, GrowingValuesScanner.ByteState> result = GrowingValuesScanner.runScanner(Arrays.asList(
                new CANPacket(0, id, new byte[]{3, (byte) 250,0}),
                new CANPacket(0, id, new byte[]{4,3,0}),
                new CANPacket(0, id, new byte[]{3,12,0})
        ), 20);
        assertEquals(3, result.size());

        List<GrowingValuesScanner.ByteState> list = new ArrayList<>(result.values());

        assertFalse(list.get(0).isIncrementByte());
        assertTrue(list.get(1).isIncrementByte());
        assertFalse(list.get(2).isIncrementByte());
    }

}
