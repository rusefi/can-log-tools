package com.rusefi.can.analysis.counter_scanner;

public class CounterWithWidth {

    public final CounterScannerTool.BitStateKey start;
    private final int totalNumberOfBits;

    public CounterWithWidth(CounterScannerTool.BitStateKey start, int totalNumberOfBits) {
        this.start = start;
        this.totalNumberOfBits = totalNumberOfBits;
    }

    public CounterScannerTool.BitStateKey getStart() {
        return start;
    }

    public int getTotalNumberOfBits() {
        return totalNumberOfBits;
    }

    @Override
    public String toString() {
        return "Counter{" +
                "start at " + start +
                ", totalNumberOfBits " + totalNumberOfBits +
                '}';
    }
}
