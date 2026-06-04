package com.rusefi.can.analysis.counter_scanner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CounterAggregator {
    public static List<CounterWithWidth> scan(LinkedHashMap<CounterScannerTool.BitStateKey, Integer> counters) {

        ScanState state = new ScanState();

        for (Map.Entry<CounterScannerTool.BitStateKey, Integer> e : counters.entrySet()) {
            CounterScannerTool.BitStateKey currentState = e.getKey();
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
        CounterScannerTool.BitStateKey prev = null;

        List<CounterWithWidth> counters = new ArrayList<>();

        CounterScannerTool.BitStateKey start;
        int totalNumberOfBits;

        void wrap() {
            if (prev != null) {
                counters.add(new CounterWithWidth(start, totalNumberOfBits));
                prev = null;
            }
        }
    }

}
