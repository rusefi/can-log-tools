package com.rusefi.can.analysis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CounterAggregator {
    static List<CounterWithWidth> scan(LinkedHashMap<CounterScanner.BitStateKey, Integer> counters) {

        ScanState state = new ScanState();

        for (Map.Entry<CounterScanner.BitStateKey, Integer> e : counters.entrySet()) {
            CounterScanner.BitStateKey currentState = e.getKey();
            int counterSize = e.getValue();

//            System.out.println("currentState " + currentState + " counterSize " + counterSize);

            if (state.prev == null) {
                if (counterSize == 1) {
                    state.prev = state.start = currentState;
                    state.totalNumberOfBits = 1;
                }
            } else {
                if ((state.prev.getSid() != currentState.getSid() ||
                        state.prev.getByteIndex() != currentState.getByteIndex() ||
                        state.prev.getBitIndex() + 1 != currentState.getBitIndex()
                )) {

                    state.wrap();
                } else {
                    state.totalNumberOfBits++;
                    state.prev = currentState;
                }
            }
        }
        state.wrap();
        return state.counters;
    }

    static class ScanState {
        CounterScanner.BitStateKey prev = null;

        List<CounterWithWidth> counters = new ArrayList<>();

        CounterScanner.BitStateKey start;
        int totalNumberOfBits;

        void wrap() {
            if (prev != null) {
                counters.add(new CounterWithWidth(start, totalNumberOfBits));
                prev = null;
            }
        }
    }

    static class CounterWithWidth {

        public final CounterScanner.BitStateKey start;
        private final int totalNumberOfBits;

        public CounterWithWidth(CounterScanner.BitStateKey start, int totalNumberOfBits) {
            this.start = start;
            this.totalNumberOfBits = totalNumberOfBits;
        }

        @Override
        public String toString() {
            return "Counter{" +
                    "start at " + start +
                    ", totalNumberOfBits " + totalNumberOfBits +
                    '}';
        }
    }
}
