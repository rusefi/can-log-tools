package com.rusefi.can;

import com.rusefi.can.reader.CANLineReader;

import java.io.IOException;
import java.util.*;

public class ByteRateOfChange {

    public static void process(String fullFileName) throws IOException {
        List<CANPacket> packets = CANLineReader.getReader().readFile(fullFileName);

        HashMap<ByteId, ByteStatistics> statistics = new HashMap<>();

        for (CANPacket packet : packets) {
            for (int index = 0; index < packet.getData().length; index++) {
                ByteId key = new ByteId(packet.getId(), index);
                ByteStatistics stats = statistics.computeIfAbsent(key, byteId -> new ByteStatistics(key));
                stats.uniqueValues.add((int) packet.getData()[index]);
            }
        }

        List<ByteStatistics> allStats = new ArrayList<>(statistics.values());
        allStats.sort((o1, o2) -> o2.getUniqueValues() - o1.getUniqueValues());

        System.out.println(allStats);


        for (ByteStatistics byteStatistics : allStats) {
            ByteId key = byteStatistics.key;
            System.out.println(dualSid(key.sid) + " at index " + key.index + " has " + byteStatistics.getUniqueValues() + " unique values");
        }


    }

    public static String dualSid(int sid) {
        return String.format("%d/0x%x", sid, sid);
    }

    static class ByteStatistics {
        HashSet<Integer> uniqueValues = new HashSet<>();
        private final ByteId key;

        public ByteStatistics(ByteId key) {
            this.key = key;
        }

        public int getUniqueValues() {
            return uniqueValues.size();
        }

        @Override
        public String toString() {
            return "ByteStatistics{" +
                    "counter=" + uniqueValues.size() +
                    ", key=" + key +
                    '}';
        }
    }


    static class ByteId {
        final int sid;
        final int index;

        public ByteId(int sid, int index) {
            this.sid = sid;
            this.index = index;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ByteId byteId = (ByteId) o;
            return sid == byteId.sid && index == byteId.index;
        }

        @Override
        public int hashCode() {
            return Objects.hash(sid, index);
        }

        @Override
        public String toString() {
            return "ByteId{" +
                    "sid=" + sid +
                    ", index=" + index +
                    '}';
        }
    }

}
