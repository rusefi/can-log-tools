package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import com.rusefi.mlv.LoggingContext;
import com.rusefi.mlv.LoggingStrategy;
import com.rusefi.sensor_logs.BinaryLogEntry;
import com.rusefi.sensor_logs.BinarySensorLog;

import java.io.*;
import java.util.*;

public class ByteRateOfChange {

    static class TraceFileMetaIndex {
        HashMap<ByteId, ByteStatistics> statistics = new HashMap<>();

        Set<Integer> SIDs = new HashSet<>();
    }

    public static TraceReport process(String reportDestinationFolder, String simpleFileName, List<CANPacket> packets) throws IOException {

        PerSidDump.handle(packets, simpleFileName);

        CounterScanner.scanForCounters(packets);

        TraceFileMetaIndex traceFileMetaIndex = calculateByteStatistics(packets);

        writeByteReport(reportDestinationFolder, simpleFileName, traceFileMetaIndex);

        TraceReport traceReport = new TraceReport(packets, simpleFileName, traceFileMetaIndex.statistics);
        traceReport.createMegaLogViewer();
        return traceReport;
    }

    private static void writeByteReport(String reportDestinationFolder, String simpleFileName, TraceFileMetaIndex traceFileMetaIndex) throws FileNotFoundException {
        List<ByteStatistics> allStats = new ArrayList<>(traceFileMetaIndex.statistics.values());
        allStats.sort((o1, o2) -> o2.getUniqueValues() - o1.getUniqueValues());

//        System.out.println(allStats);

        writeByteReport(reportDestinationFolder, simpleFileName, allStats);
    }

    private static void writeByteReport(String reportDestinationFolder, String simpleFileName, List<ByteStatistics> allStats) throws FileNotFoundException {
        String outputFileName = reportDestinationFolder + File.separator + simpleFileName + ".txt";
        System.out.println("Wring byte report to " + outputFileName);
        PrintStream ps = new PrintStream(new FileOutputStream(outputFileName, false));

        for (ByteStatistics byteStatistics : allStats) {
            ByteId key = byteStatistics.key;
            ps.println(dualSid(key.sid) + " byte " + key.index + " has " + byteStatistics.getUniqueValues() + " unique value(s)");
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
                stats.uniqueValues.add((int) packet.getData()[byteIndex]);
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


    public static class ByteId {
        final int sid;
        final int index;

        public ByteId(int sid, int index) {
            this.sid = sid;
            this.index = index;
        }

        private String getLogKey() {
            return dualSid(sid) + "_byte_" + index + "_bit_" + (index * 8);
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
            return getLogKey();
        }
    }

    public static class TraceReport extends HashMap<ByteId, ByteStatistics> {
        private final List<CANPacket> packets;
        private final String simpleFileName;
        private final HashMap<ByteId, ByteStatistics> statistics;
        private final double durationMs;

        public TraceReport(List<CANPacket> packets, String simpleFileName, HashMap<ByteId, ByteStatistics> statistics) {
            this.packets = packets;
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

        public void createMegaLogViewer() {
            Map<ByteId, BinaryLogEntry> entries = new HashMap<>();

            for (ByteId key : statistics.keySet()) {
                entries.put(key, BinaryLogEntry.createFloatLogEntry(key.getLogKey(), Integer.toBinaryString(key.sid)));
            }

            LoggingContext context = new LoggingContext();
            BinarySensorLog<BinaryLogEntry> log = context.getBinaryLogEntryBinarySensorLog(entries.values(), simpleFileName + LoggingStrategy.MLG);


            context.writeLogContent(packets, log, packetContent -> {
                for (int i = 0; i < packetContent.getData().length; i++) {
                    int value = packetContent.getData()[i] & 0xFF;

                    String name = new ByteId(packetContent.getId(), i).getLogKey();
                    context.currentSnapshot.put(name, (double) value);
                }
                return true;
            });
        }
    }
}
