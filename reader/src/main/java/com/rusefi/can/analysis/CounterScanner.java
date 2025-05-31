package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.can.reader.dbc.DbcPacket;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

public class CounterScanner {

    public static final String COUNTERS_YAML = "counters.yaml";

    public static void scanForCounters(DbcFile dbc, String reportDestinationFolder, String simpleFileName, List<CANPacket> packets) throws IOException {

        String outputFileName = reportDestinationFolder + File.separator + simpleFileName + "_counter_report.txt";
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(outputFileName))) {
            runScanner(dbc, reportDestinationFolder, packets, pw);
        }
    }

    private static void runScanner(DbcFile dbc, String reportDestinationFolder, List<CANPacket> packets, PrintWriter pw) throws IOException {
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

        Map</*sid*/Integer, Map<Integer, Integer>> map = new TreeMap<>();

        pw.println("Here are the founding:");
        for (CounterAggregator.CounterWithWidth counterWithWidth : countersWithWidth) {
            pw.println("Found " + counterWithWidth);

            Map<Integer, Integer> lengthByStartIndex = map.computeIfAbsent(counterWithWidth.getStart().getSid(), integer -> new HashMap<>());

            lengthByStartIndex.put(counterWithWidth.getStart().getTotalBitIndex(), counterWithWidth.getTotalNumberOfBits());
        }
        String yamlCountersReportFileName = reportDestinationFolder + File.separator + COUNTERS_YAML;
        System.out.println(new Date() + " Writing report to " + yamlCountersReportFileName);


        Map<String, Map<Integer, Integer>> report = new TreeMap<>();
        for (Map.Entry<Integer, Map<Integer, Integer>> e : map.entrySet()) {
            Integer sid = e.getKey();
            DbcPacket packet = dbc.getPacket(sid);
            String key = packet == null ? Integer.toString(sid) : packet.getName();
            report.put(key, e.getValue());
        }
        yaml.dump(report, new FileWriter(yamlCountersReportFileName));
    }

    static class BitStateKey implements Comparable {
        private final ByteRateOfChange.ByteId byteId;
        private final int bitIndex;

        public BitStateKey(int sid, int byteIndex, int bitIndex) {
            this.byteId = ByteRateOfChange.ByteId.createByte(sid, byteIndex);
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

    static class Id {

    }
}
