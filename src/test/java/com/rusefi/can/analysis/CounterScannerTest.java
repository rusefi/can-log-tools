package com.rusefi.can.analysis;

import org.junit.Test;

import static org.junit.Assert.*;

public class CounterScannerTest {
    @Test
    public void testNotCounter() {
        CounterScanner.BitState state = new CounterScanner.BitState();
        state.handle(true);
        assertEquals(state.state, CounterScanner.BitState.StateMachine.LOOKING_FOR_FIRST_SWITCHOVER);

        state.handle(false);
        assertEquals(state.state, CounterScanner.BitState.StateMachine.FOUND_FIRST_SWITCHOVER);

        state.handle(false);
        assertEquals(CounterScanner.BitState.StateMachine.FOUND_FIRST_SWITCHOVER, state.state);

        state.handle(true);
        assertEquals(CounterScanner.BitState.StateMachine.HAPPY_COUNTER, state.state);

        state.handle(false);
        state.handle(false);

        assertFalse(state.couldBeCounter());
    }

    @Test
    public void testCounterLen1() {
        CounterScanner.BitState state = new CounterScanner.BitState();
        state.handle(true);
        state.handle(false);
        state.handle(true);
        state.handle(false);

        assertTrue(state.couldBeCounter());
        assertEquals(1, state.cycleLength);
    }


    @Test
    public void testCounterLen2() {
        CounterScanner.BitState state = new CounterScanner.BitState();
        state.handle(true);
        state.handle(false);
        state.handle(false);
        state.handle(true);
        state.handle(true);
        state.handle(false);

        assertTrue(state.couldBeCounter());
        assertEquals(2, state.cycleLength);
    }
}
