package com.rusefi.can.analysis.counter_scanner;

public class CounterWithWidth {

    public final CounterScanner.BitStateKey start;
    private final int totalNumberOfBits;

    public CounterWithWidth(CounterScanner.BitStateKey start, int totalNumberOfBits) {
        this.start = start;
        this.totalNumberOfBits = totalNumberOfBits;
    }

    public CounterScanner.BitStateKey getStart() {
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
