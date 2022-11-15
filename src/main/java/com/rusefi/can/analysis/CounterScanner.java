package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;

import java.util.*;

import static com.rusefi.can.analysis.ByteRateOfChange.dualSid;

public class CounterScanner {
    public static void scanForCounters(List<CANPacket> packets) {

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
                System.out.println("Looks like counter " + key + " " + bitState.cycleLength);

                counters.put(key, bitState.cycleLength);

            }
        }

        List<CounterAggregator.CounterWithWidth> countersWithWidth = CounterAggregator.scan(counters);

        for (CounterAggregator.CounterWithWidth counterWithWidth : countersWithWidth) {
            System.out.println("Found " + counterWithWidth);
        }
    }

    static class BitStateKey implements Comparable {
        private final int sid;
        private final int byteIndex;
        private final int bitIndex;

        public BitStateKey(int sid, int byteIndex, int bitIndex) {
            this.sid = sid;
            this.byteIndex = byteIndex;
            this.bitIndex = bitIndex;
        }

        public int getSid() {
            return sid;
        }

        public int getByteIndex() {
            return byteIndex;
        }

        public int getBitIndex() {
            return bitIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BitStateKey that = (BitStateKey) o;
            return sid == that.sid && byteIndex == that.byteIndex && bitIndex == that.bitIndex;
        }

        @Override
        public int hashCode() {
            return Objects.hash(sid, byteIndex, bitIndex);
        }

        @Override
        public String toString() {
            return "{" +
                    dualSid(sid) +
                    ", byteIndex " + byteIndex +
                    ", bitIndex " + bitIndex +
                    '}';
        }

        @Override
        public int compareTo(Object o) {
            BitStateKey other = (BitStateKey) o;

            if (other.sid != sid)
                return sid - other.sid;
            if (other.byteIndex != byteIndex)
                return byteIndex - other.byteIndex;
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
