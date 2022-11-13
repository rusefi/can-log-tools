package com.rusefi.can.analysis;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

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
}
