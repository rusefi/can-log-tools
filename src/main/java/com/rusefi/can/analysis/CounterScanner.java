package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;

import java.util.*;

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

        for (Map.Entry<BitStateKey, BitState> e : bitStates.entrySet()) {

            if (e.getValue().couldBeCounter)
                System.out.println("Looks like counter " + e.getKey());
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
            return "BitStateKey{" +
                    "sid=" + sid +
                    ", byteIndex=" + byteIndex +
                    ", bitIndex=" + bitIndex +
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
        boolean isFirst = true;
        boolean couldBeCounter = true;

        boolean previousBitValue;

        public void handle(boolean bitValue) {
            if (isFirst) {
                isFirst = false;
                previousBitValue = bitValue;
                return;
            }
            if (!couldBeCounter)
                return;
            if (previousBitValue == bitValue) {
                couldBeCounter = false;
            }
            previousBitValue = bitValue;
        }
    }
}
