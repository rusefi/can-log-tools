package com.rusefi.can;

import com.rusefi.can.reader.ReaderType;
import com.rusefi.util.FolderUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Function;

public class ByteRateOfChangeSandbox {
    public static void main(String[] args) throws IOException {
        ReaderTypeHolder.INSTANCE.type = ReaderType.PCAN;

        String folder = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\Nissan\\2011_Xterra\\CAN-Nov-2022";

        String reportDestinationFolder = folder + File.separator + "processed";
        new File(reportDestinationFolder).mkdirs();


        List<ByteRateOfChange.TraceReport> reports = new ArrayList<>();

        FolderUtil.handleFolder(folder, (simpleFileName, fullFileName) -> {
            ByteRateOfChange.TraceReport report = ByteRateOfChange.process(fullFileName, reportDestinationFolder, simpleFileName);
            reports.add(report);
        }, "pcan.trc");


        for (int i = 0; i < reports.size(); i++) {
            for (int j = i + 1; j < reports.size(); j++)
                compare(reports.get(i), reports.get(j));
        }
    }

    private static void compare(ByteRateOfChange.TraceReport traceReport1, ByteRateOfChange.TraceReport traceReport2) {
        Set<ByteRateOfChange.ByteId> allKeys = new HashSet<>();
        allKeys.addAll(traceReport1.getStatistics().keySet());
        allKeys.addAll(traceReport2.getStatistics().keySet());

        PrintStream report = System.out;

        report.println("Comparing " + traceReport1.getSimpleFileName() + " and " + traceReport2.getSimpleFileName());

        List<ByteVariationDifference> differences = new ArrayList<>();

        for (ByteRateOfChange.ByteId id : allKeys) {
            ByteRateOfChange.ByteStatistics s1 = traceReport1.getStatistics().computeIfAbsent(id, byteId -> new ByteRateOfChange.ByteStatistics(byteId));
            ByteRateOfChange.ByteStatistics s2 = traceReport2.getStatistics().computeIfAbsent(id, byteId -> new ByteRateOfChange.ByteStatistics(byteId));

            if (s1.getUniqueValues() != s2.getUniqueValues()) {
                String msg = id + ": " + s1.getUniqueValues() + " vs " + s2.getUniqueValues();
                differences.add(new ByteVariationDifference(Math.abs(s1.getUniqueValues() - s2.getUniqueValues()), msg));
            }
        }
        differences.sort(new Comparator<ByteVariationDifference>() {
            @Override
            public int compare(ByteVariationDifference o1, ByteVariationDifference o2) {
                return o2.deltaCount - o1.deltaCount;
            }
        });

        for (ByteVariationDifference difference : differences)
            report.println(difference.msg);

        report.println(differences.size() + " total differences");
        report.println();
        report.println();
        report.println();
        report.println();
    }

    static class ByteVariationDifference {
        private int deltaCount;
        private String msg;

        public ByteVariationDifference(int deltaCount, String msg) {
            this.deltaCount = deltaCount;
            this.msg = msg;
        }
    }

}
