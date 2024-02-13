package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;

import java.io.*;
import java.util.*;

public class ByteRateOfChange {

    static class TraceFileMetaIndex {
        final HashMap<ByteId, ByteStatistics> statistics = new HashMap<>();

        final Set<Integer> SIDs = new HashSet<>();
    }

    public static TraceReport process(String reportDestinationFolder, String simpleFileName, List<CANPacket> packets) throws IOException {
        TraceFileMetaIndex traceFileMetaIndex = calculateByteStatistics(packets);

        writeByteReport(reportDestinationFolder, simpleFileName, traceFileMetaIndex);

        return new TraceReport(packets, simpleFileName, traceFileMetaIndex.statistics);
    }

    private static void writeByteReport(String reportDestinationFolder, String simpleFileName, TraceFileMetaIndex traceFileMetaIndex) throws FileNotFoundException {
        String byteChangesFolder = reportDestinationFolder + File.separator + "byte_changes";
        new File(byteChangesFolder).mkdirs();

        List<ByteStatistics> allStats = new ArrayList<>(traceFileMetaIndex.statistics.values());
        allStats.sort((o1, o2) -> o2.getUniqueValuesCount() - o1.getUniqueValuesCount());

        writeByteReport(allStats, byteChangesFolder + File.separator + simpleFileName + "byte_rate_of_changes_sorted_counter.txt");

        allStats.sort(new Comparator<ByteStatistics>() {
            @Override
            public int compare(ByteStatistics o1, ByteStatistics o2) {
                return o1.key.compareTo(o2.key);
            }
        });
        writeByteReport(allStats, byteChangesFolder + File.separator + simpleFileName + "byte_rate_of_changes_sorted_index.txt");
    }

    private static void writeByteReport(List<ByteStatistics> allStats, String fileName) throws FileNotFoundException {
        System.out.println(new Date() + " Writing byte report to " + fileName);
        PrintStream ps = new PrintStream(new FileOutputStream(fileName, false));

        for (ByteStatistics byteStatistics : allStats) {
            ByteId key = byteStatistics.key;
            ps.println(dualSid(key.sid) + " byte " + key.getByteIndex() + " has " + byteStatistics.getUniqueValuesCount() + " unique value(s)");
        }

        ps.close();
    }

    private static TraceFileMetaIndex calculateByteStatistics(List<CANPacket> packets) {
        TraceFileMetaIndex traceFileMetaIndex = new TraceFileMetaIndex();

        for (CANPacket packet : packets) {
            traceFileMetaIndex.SIDs.add(packet.getId());
            for (int byteIndex = 0; byteIndex < packet.getData().length; byteIndex++) {
                ByteId key = new ByteId(packet.getId(), byteIndex);
                ByteStatistics stats = traceFileMetaIndex.statistics.computeIfAbsent(key, byteId -> new ByteStatistics(key));
                stats.registerValue(packet.getData()[byteIndex]);
            }
        }
        return traceFileMetaIndex;
    }

    private static double getDurationMs(List<CANPacket> packets) {
        return packets.isEmpty() ? 0 : packets.get(packets.size() - 1).getTimeStamp() - packets.get(0).getTimeStamp();
    }

    public static String dualSid(int sid) {
        return dualSid(sid, "/");
    }

    public static String dualSid(int sid, String separator) {
        return String.format("%d%s0x%x", sid, separator, sid);
    }

    public static class ByteStatistics {
        private final HashSet<Integer> uniqueValues = new HashSet<>();
        int totalTransitions;
        private final ByteId key;

        private int previousValue;

        public ByteStatistics(ByteId key) {
            this.key = key;
        }

        public int getUniqueValuesCount() {
            return uniqueValues.size();
        }

        public HashSet<Integer> getUniqueValues() {
            return uniqueValues;
        }

        public ByteId getKey() {
            return key;
        }

        @Override
        public String toString() {
            return "ByteStatistics{" +
                    "counter=" + uniqueValues.size() +
                    ", totalTransitions=" + totalTransitions +
                    ", key=" + key +
                    '}';
        }

        public void registerValue(int value) {
            if (!uniqueValues.isEmpty()) {
                if (previousValue != value)
                    totalTransitions++;
            }

            previousValue = value;
            uniqueValues.add(value);
        }
    }


    public static class ByteId implements Comparable<ByteId> {
        final int sid;
        final int byteIndex;

        public ByteId(int sid, int byteIndex) {
            this.sid = sid;
            this.byteIndex = byteIndex;
        }

        public String getLogKey() {
            return dualSid(sid) + "_byte_" + byteIndex + "_bit_" + (byteIndex * 8);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ByteId byteId = (ByteId) o;
            return sid == byteId.sid && byteIndex == byteId.byteIndex;
        }

        public int getByteIndex() {
            return byteIndex;
        }

        @Override
        public int hashCode() {
            return Objects.hash(sid, byteIndex);
        }

        @Override
        public int compareTo(ByteId o) {
            ByteId other = o;
            if (other.sid != sid)
                return sid - other.sid;
            return byteIndex - other.byteIndex;
        }

        @Override
        public String toString() {
            return getLogKey();
        }
    }

    public static class TraceReport extends HashMap<ByteId, ByteStatistics> {
        private final String simpleFileName;
        private final HashMap<ByteId, ByteStatistics> statistics;
        private final double durationMs;

        public TraceReport(List<CANPacket> packets, String simpleFileName, HashMap<ByteId, ByteStatistics> statistics) {
            this.simpleFileName = simpleFileName;
            this.statistics = statistics;
            this.durationMs = getDurationMs(packets);
        }

        String getSummary() {
            return getSimpleFileName() + " (duration=" + (int) (durationMs / 1000) + "secs)";
        }

        public String getSimpleFileName() {
            return simpleFileName;
        }

        public HashMap<ByteId, ByteStatistics> getStatistics() {
            return statistics;
        }

        public void save(String file) throws IOException {
            Writer w = new FileWriter(file);
            for (Map.Entry<ByteId, ByteStatistics> e : statistics.entrySet()) {
                w.append(e.getKey() + " " + e.getValue() + "\r\n");
            }

        }
    }
}
