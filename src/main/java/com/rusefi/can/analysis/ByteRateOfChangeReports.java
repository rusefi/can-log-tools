package com.rusefi.can.analysis;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.CANLineReader;
import com.rusefi.util.FolderUtil;

import java.io.*;
import java.util.*;

public class ByteRateOfChangeReports {
    /**
     * sweet baby O(n^2)
     */
    public static void compareEachReportAgainstAllOthers(String reportDestinationFolder, List<ByteRateOfChange.TraceReport> reports) throws FileNotFoundException {
        for (int i = 0; i < reports.size(); i++) {
            for (int j = i + 1; j < reports.size(); j++)
                compareTwoReports(reportDestinationFolder, reports.get(i), reports.get(j));
        }
    }

    private static void compareTwoReports(String reportDestinationFolder, ByteRateOfChange.TraceReport traceReport1, ByteRateOfChange.TraceReport traceReport2) throws FileNotFoundException {
        Set<ByteRateOfChange.ByteId> allKeys = new TreeSet<>();
        allKeys.addAll(traceReport1.getStatistics().keySet());
        allKeys.addAll(traceReport2.getStatistics().keySet());

        String comparingFolder = reportDestinationFolder + File.separator + "comparison";
        new File(comparingFolder).mkdirs();

        String outputFileName = comparingFolder + File.separator + traceReport1.getSimpleFileName() + "-vs-" + traceReport2.getSimpleFileName() + ".txt";
        PrintWriter report = new PrintWriter(new FileOutputStream(outputFileName));

        report.println("Comparing unique value count per byte " + traceReport1.getSummary() + " and " + traceReport2.getSummary());

        List<ByteVariationDifference> differences = new ArrayList<>();

        report.println("******************** Sorted by key ********************");


        for (ByteRateOfChange.ByteId id : allKeys) {
            ByteRateOfChange.ByteStatistics s1 = traceReport1.getStatistics().computeIfAbsent(id, ByteRateOfChange.ByteStatistics::new);
            ByteRateOfChange.ByteStatistics s2 = traceReport2.getStatistics().computeIfAbsent(id, ByteRateOfChange.ByteStatistics::new);

            if (s1.getUniqueValuesCount() != s2.getUniqueValuesCount()) {
                String msg = id + ": " + s1.getUniqueValuesCount() + " vs " + s2.getUniqueValuesCount();
                int deltaCount = Math.abs(s1.getUniqueValuesCount() - s2.getUniqueValuesCount());
                differences.add(new ByteVariationDifference(deltaCount, msg));
                report.println(msg + " delta=" + deltaCount);
            }
        }

        report.println("******************** Sorted by delta count ********************");
        differences.sort((o1, o2) -> o2.deltaCount - o1.deltaCount);
        for (ByteVariationDifference difference : differences)
            report.println(difference.msg);

        report.println(differences.size() + " total differences");
        report.println();
        report.println();
        report.println();
        report.println();
        report.close();
    }

    public static String createOutputFolder(String inputFolderName) {
        String reportDestinationFolder = inputFolderName + File.separator + "processed";
        new File(reportDestinationFolder).mkdirs();
        return reportDestinationFolder;
    }

    public static void scanInputFolder(String inputFolderName, String fileNameSuffix) throws IOException {
        String reportDestinationFolder = createOutputFolder(inputFolderName);

        List<ByteRateOfChange.TraceReport> reports = new ArrayList<>();

        FolderUtil.handleFolder(inputFolderName, (simpleFileName, fullInputFileName) -> {

            List<CANPacket> logFileContent = CANLineReader.getReader().readFile(fullInputFileName);

            PerSidDump.handle(reportDestinationFolder, simpleFileName, logFileContent);
            CounterScanner.scanForCounters(reportDestinationFolder, logFileContent);

            CanToMegaLogViewer.createMegaLogViewer(reportDestinationFolder, logFileContent, simpleFileName);

            ByteRateOfChange.TraceReport report = ByteRateOfChange.process(reportDestinationFolder, simpleFileName, logFileContent);
            reports.add(report);
        }, fileNameSuffix);


        System.out.println("Processing " + reports.size() + " report(s)");
        compareEachReportAgainstAllOthers(reportDestinationFolder, reports);
    }

    static class ByteVariationDifference {
        private final int deltaCount;
        private final String msg;

        public ByteVariationDifference(int deltaCount, String msg) {
            this.deltaCount = deltaCount;
            this.msg = msg;
        }
    }
}
