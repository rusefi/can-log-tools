package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import com.rusefi.can.DualSid;
import com.rusefi.can.reader.dbc.DbcField;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.can.reader.dbc.DbcPacket;

import java.io.*;
import java.util.*;

public class ByteRateOfChange {

    static class TraceFileMetaIndex {
        final HashMap<DbcField, ByteStatistics> statistics = new HashMap<>();

        final Set<Integer> SIDs = new HashSet<>();
    }

    public static TraceReport process(DbcFile dbc, String reportDestinationFolder, String simpleFileName, List<CANPacket> packets) throws IOException {
        TraceFileMetaIndex traceFileMetaIndex = calculateByteStatistics(dbc, packets);

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
            DbcField key = byteStatistics.key;
            ps.println(DualSid.dualSid(key.getSid()) + " byte " + key.getByteIndex() + " has " + byteStatistics.getUniqueValuesCount() + " unique value(s)");
        }

        ps.close();
    }

    private static TraceFileMetaIndex calculateByteStatistics(DbcFile dbc, List<CANPacket> packets) {
        TraceFileMetaIndex traceFileMetaIndex = new TraceFileMetaIndex();

        for (CANPacket payload : packets) {
            int sid = payload.getId();
            traceFileMetaIndex.SIDs.add(sid);
            DbcPacket meta = dbc.getPacket(sid);
            for (DbcField field : meta.getFields()) {
                ByteStatistics stats = traceFileMetaIndex.statistics.computeIfAbsent(field, byteId -> new ByteStatistics(field));
                stats.registerValue((int) field.getValue(payload));
            }
        }
        return traceFileMetaIndex;
    }

    private static double getDurationMs(List<CANPacket> packets) {
        return packets.isEmpty() ? 0 : packets.get(packets.size() - 1).getTimeStamp() - packets.get(0).getTimeStamp();
    }

    public static class ByteStatistics {
        private final HashSet<Integer> uniqueValues = new HashSet<>();
        int totalTransitions;
        private final DbcField key;

        private int previousValue;

        public ByteStatistics(DbcField key) {
            this.key = key;
        }

        public int getUniqueValuesCount() {
            return uniqueValues.size();
        }

        public Set<Integer> getUniqueValues() {
            return Collections.unmodifiableSet(uniqueValues);
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

        private ByteId(int sid, int byteIndex) {
            this.sid = sid;
            this.byteIndex = byteIndex;
        }

        public static ByteId createByte(int sid, int byteIndex) {
            return new ByteId(sid, byteIndex);
        }

        public static ByteId convert(DbcField dbcField) {
            if (dbcField.getLength() != 8 || dbcField.getStartOffset() % 8 !=0)
                return null;
            return createByte(dbcField.getSid(), dbcField.getByteIndex());
        }

        public String getLogKey() {
            return DualSid.dualSid(sid) + "_byte_" + byteIndex + "_bit_" + (byteIndex * 8);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ByteId byteId = (ByteId) o;
            return sid == byteId.sid && byteIndex == byteId.byteIndex;
        }

        public int getSid() {
            return sid;
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

    public static class TraceReport {
        private final String simpleFileName;
        private final HashMap<DbcField, ByteStatistics> statistics;
        private final double durationMs;

        public TraceReport(List<CANPacket> packets, String simpleFileName, HashMap<DbcField, ByteStatistics> statistics) {
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

        public HashMap<DbcField, ByteStatistics> getStatistics() {
            return statistics;
        }

        @Override
        public String toString() {
            return "TraceReport{" +
                    "simpleFileName='" + simpleFileName + '\'' +
                    ", statistics=" + statistics +
                    ", durationMs=" + durationMs +
                    '}';
        }

        public void save(String reportDestinationFolder, String fileName) throws IOException {
            Writer w = new FileWriter(reportDestinationFolder + File.separator + fileName);
            for (Map.Entry<DbcField, ByteStatistics> e : statistics.entrySet()) {
                w.append(e.getKey() + " " + e.getValue() + "\r\n");
            }

        }
    }
}
