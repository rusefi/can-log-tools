package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

public class CounterScanner {

    public static final String COUNTERS_YAML = "counters.yaml";

    public static void scanForCounters(String reportDestinationFolder, List<CANPacket> packets) throws IOException {

        String outputFileName = reportDestinationFolder + File.separator + "counter_report.txt";
        PrintWriter pw = new PrintWriter(new FileOutputStream(outputFileName));

        Map<BitStateKey, BitState> bitStates = new TreeMap<>();

        for (CANPacket packet : packets) {
            for (int byteIndex = 0; byteIndex < packet.getData().length; byteIndex++) {
                byte byteValue = packet.getData()[byteIndex];

                for (int bitIndex = 0; bitIndex < 8; bitIndex++) {

                    int bitValue = byteValue >> bitIndex & 1;

                    BitStateKey key = new BitStateKey(packet.getId(), byteIndex, bitIndex);

                    BitState state = bitStates.computeIfAbsent(key, o -> new BitState());

                    state.handle(bitValue == 1);
                }

            }
        }


        LinkedHashMap<BitStateKey, Integer> counters = new LinkedHashMap<>();
        for (Map.Entry<BitStateKey, BitState> e : bitStates.entrySet()) {

            BitState bitState = e.getValue();
            if (bitState.couldBeCounter()) {
                BitStateKey key = e.getKey();
                pw.println("Working: Looks like counter key=" + key + " cycleLength=" + bitState.cycleLength);

                counters.put(key, bitState.cycleLength);
            }
        }

        pw.println("Scanning...");
        List<CounterAggregator.CounterWithWidth> countersWithWidth = CounterAggregator.scan(counters);

        Yaml yaml = new Yaml();
        Map<Integer, Map<Integer, Integer>> map = new TreeMap<>();

        pw.println("Here are the founding:");
        for (CounterAggregator.CounterWithWidth counterWithWidth : countersWithWidth) {
            pw.println("Found " + counterWithWidth);

            Map<Integer, Integer> lengthByStartIndex = map.computeIfAbsent(counterWithWidth.getStart().getSid(), integer -> new HashMap<>());

            lengthByStartIndex.put(counterWithWidth.getStart().getTotalBitIndex(), counterWithWidth.getTotalNumberOfBits());
        }
        String yamlCountersReportFileName = reportDestinationFolder + File.separator + COUNTERS_YAML;
        System.out.println(new Date() + " Writing report to " + yamlCountersReportFileName);
        yaml.dump(map, new FileWriter(yamlCountersReportFileName));
        pw.close();
    }

    static class BitStateKey implements Comparable {
        private final ByteRateOfChange.ByteId byteId;
        private final int bitIndex;

        public BitStateKey(int sid, int byteIndex, int bitIndex) {
            this.byteId = new ByteRateOfChange.ByteId(sid, byteIndex);
            this.bitIndex = bitIndex;
        }

        public int getTotalBitIndex() {
            return byteId.byteIndex * 8 + bitIndex;
        }

        public int getSid() {
            return byteId.sid;
        }

        public int getByteIndex() {
            return byteId.byteIndex;
        }

        public int getBitIndex() {
            return bitIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BitStateKey that = (BitStateKey) o;
            return byteId.equals(that.byteId) && bitIndex == that.bitIndex;
        }

        @Override
        public int hashCode() {
            return Objects.hash(byteId.hashCode(), bitIndex);
        }

        @Override
        public String toString() {
            return "{" +
                    byteId +
                    ", bitIndex " + bitIndex +
                    '}';
        }

        @Override
        public int compareTo(Object o) {
            BitStateKey other = (BitStateKey) o;

            int compareByte = byteId.compareTo(other.byteId);
            if (compareByte != 0)
                return compareByte;
            return bitIndex - other.bitIndex;
        }
    }

    static class BitState {
        int index;
        int cycleLength;

        public boolean couldBeCounter() {
            return state == StateMachine.HAPPY_COUNTER;
        }

        enum StateMachine {
            FIRST_VALUE,
            LOOKING_FOR_FIRST_SWITCHOVER,
            FOUND_FIRST_SWITCHOVER,
            HAPPY_COUNTER,
            NOT_GOOD
        }

        StateMachine state = StateMachine.FIRST_VALUE;

        boolean previousBitValue;

        public void handle(boolean bitValue) {
            if (state == StateMachine.NOT_GOOD) {
                return;
            } else if (state == StateMachine.FIRST_VALUE) {
                previousBitValue = bitValue;
                state = StateMachine.LOOKING_FOR_FIRST_SWITCHOVER;
            } else if (state == StateMachine.LOOKING_FOR_FIRST_SWITCHOVER) {
                if (previousBitValue == bitValue)
                    return;
                previousBitValue = bitValue;
                state = StateMachine.FOUND_FIRST_SWITCHOVER;
            } else if (state == StateMachine.FOUND_FIRST_SWITCHOVER) {
                index++;
                if (previousBitValue != bitValue) {
                    state = StateMachine.HAPPY_COUNTER;
                    cycleLength = index;
                    previousBitValue = bitValue;
                    index = 0;
                }
            } else if (state == StateMachine.HAPPY_COUNTER) {
                index++;

                if (previousBitValue != bitValue) {
                    if (index != cycleLength)
                        state = StateMachine.NOT_GOOD;
                    previousBitValue = bitValue;
                    index = 0;
                }
            } else
                throw new IllegalStateException(state.toString());
        }
    }
}
