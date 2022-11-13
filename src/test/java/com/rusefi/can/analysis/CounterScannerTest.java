package com.rusefi.can.analysis;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CounterScannerTest {
    @Test
    public void testNotCounter() {
        CounterScanner.BitState state = new CounterScanner.BitState();
        state.handle(true);
        state.handle(false);
        state.handle(false);
        state.handle(true);
        state.handle(false);
        state.handle(false);

        assertFalse(state.couldBeCounter);
    }

    @Test
    public void testCounterLen1() {
        CounterScanner.BitState state = new CounterScanner.BitState();
        state.handle(true);
        state.handle(false);
        state.handle(true);
        state.handle(false);

        assertTrue(state.couldBeCounter);
    }
}
