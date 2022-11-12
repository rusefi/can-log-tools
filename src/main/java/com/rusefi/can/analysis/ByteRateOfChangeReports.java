package com.rusefi.can.analysis;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ByteRateOfChangeReports {
    /**
     * sweet baby O(n^2)
     */
    public static void compareEachReportAgainstAllOthers(List<ByteRateOfChange.TraceReport> reports) {
        for (int i = 0; i < reports.size(); i++) {
            for (int j = i + 1; j < reports.size(); j++)
                compareTwoReports(reports.get(i), reports.get(j));
        }
    }

    private static void compareTwoReports(ByteRateOfChange.TraceReport traceReport1, ByteRateOfChange.TraceReport traceReport2) {
        Set<ByteRateOfChange.ByteId> allKeys = new HashSet<>();
        allKeys.addAll(traceReport1.getStatistics().keySet());
        allKeys.addAll(traceReport2.getStatistics().keySet());

        PrintStream report = System.out;

        report.println("Comparing unique value count per byte " + traceReport1.getSummary() + " and " + traceReport2.getSummary());

        List<ByteVariationDifference> differences = new ArrayList<>();

        for (ByteRateOfChange.ByteId id : allKeys) {
            ByteRateOfChange.ByteStatistics s1 = traceReport1.getStatistics().computeIfAbsent(id, ByteRateOfChange.ByteStatistics::new);
            ByteRateOfChange.ByteStatistics s2 = traceReport2.getStatistics().computeIfAbsent(id, ByteRateOfChange.ByteStatistics::new);

            if (s1.getUniqueValues() != s2.getUniqueValues()) {
                String msg = id + ": " + s1.getUniqueValues() + " vs " + s2.getUniqueValues();
                differences.add(new ByteVariationDifference(Math.abs(s1.getUniqueValues() - s2.getUniqueValues()), msg));
            }
        }
        differences.sort((o1, o2) -> o2.deltaCount - o1.deltaCount);

        for (ByteVariationDifference difference : differences)
            report.println(difference.msg);

        report.println(differences.size() + " total differences");
        report.println();
        report.println();
        report.println();
        report.println();
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
