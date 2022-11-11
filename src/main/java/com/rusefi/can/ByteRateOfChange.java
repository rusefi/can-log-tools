package com.rusefi.can;

import com.rusefi.can.reader.CANLineReader;
import com.rusefi.sensor_logs.BinaryLogEntry;
import com.rusefi.sensor_logs.BinarySensorLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ByteRateOfChange {

    public static TraceReport process(String fullFileName, String reportDestinationFolder, String simpleFileName) throws IOException {
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

        PrintStream ps = new PrintStream(new FileOutputStream(reportDestinationFolder + File.separator + simpleFileName + ".txt", false));

        for (ByteStatistics byteStatistics : allStats) {
            ByteId key = byteStatistics.key;
            ps.println(dualSid(key.sid) + " at index " + key.index + " has " + byteStatistics.getUniqueValues() + " unique value(s)");
        }

        ps.close();

        return new TraceReport(simpleFileName, statistics);
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

    public static class TraceReport extends HashMap<ByteId, ByteStatistics> {
        private final String simpleFileName;
        private final HashMap<ByteId, ByteStatistics> statistics;

        public TraceReport(String simpleFileName, HashMap<ByteId, ByteStatistics> statistics) {
            this.simpleFileName = simpleFileName;
            this.statistics = statistics;
        }

        public String getSimpleFileName() {
            return simpleFileName;
        }

        public HashMap<ByteId, ByteStatistics> getStatistics() {
            return statistics;
        }

        public void createMegaLogViewer() {
            Map<ByteId, BinaryLogEntry> entries = new HashMap<>();

            for (ByteId key : statistics.keySet()) {
                entries.put(key, BinaryLogEntry.createFloatLogEntry(key.sid + "_" + key.index, Integer.toBinaryString(key.sid)));
            }

            Map<String, Double> values = new HashMap<>();
            AtomicReference<Long> time = new AtomicReference<>();
            BinarySensorLog<BinaryLogEntry> log = new BinarySensorLog<>(o -> {
                Double value = values.get(o.getName());
                if (value == null)
                    return 0.0;
                return value;
            }, entries.values(), time::get, "haha" + LoggingStrategy.MLG);
        }
    }
}
