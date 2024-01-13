package com.rusefi.can.analysis;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;

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

    @Test
    public void testAggregateCounterCandidatesPlain() {
        LinkedHashMap<CounterScanner.BitStateKey, Integer> counters = new LinkedHashMap<>();

        counters.put(new CounterScanner.BitStateKey(1, 3, 6), 4);

        counters.put(new CounterScanner.BitStateKey(0, 7, 6), 1);

        List<CounterAggregator.CounterWithWidth> countersWithWidth = CounterAggregator.scan(counters);

        assertEquals(1, countersWithWidth.size());
        assertEquals(7, countersWithWidth.get(0).start.getByteIndex());

        System.out.println(countersWithWidth);
    }

    @Test
    public void testAggregateCounterCandidates() {
        LinkedHashMap<CounterScanner.BitStateKey, Integer> counters = new LinkedHashMap<>();


        counters.put(new CounterScanner.BitStateKey(0, 7, 6), 1);

        counters.put(new CounterScanner.BitStateKey(1, 3, 6), 4);

        counters.put(new CounterScanner.BitStateKey(0, 3, 4), 1);
        counters.put(new CounterScanner.BitStateKey(0, 3, 5), 2);
        counters.put(new CounterScanner.BitStateKey(0, 3, 6), 4);

        counters.put(new CounterScanner.BitStateKey(0, 5, 6), 4);


        List<CounterAggregator.CounterWithWidth> countersWithWidth = CounterAggregator.scan(counters);

        assertEquals(2, countersWithWidth.size());

        System.out.println(countersWithWidth);
    }
}
