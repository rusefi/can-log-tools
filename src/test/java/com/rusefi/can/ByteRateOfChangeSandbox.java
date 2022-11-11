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

        String folder = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\Nissan\\2011_Xterra\\2011-nissan-CAN-June-2021";

        String reportDestinationFolder = folder + File.separator + "processed";
        new File(reportDestinationFolder).mkdirs();


        List<ByteRateOfChange.TraceReport> reports = new ArrayList<>();

        FolderUtil.handleFolder(folder, new FolderUtil.FileAction() {
            @Override
            public void doJob(String simpleFileName, String fullFileName) throws IOException {
                ByteRateOfChange.TraceReport report = ByteRateOfChange.process(fullFileName, reportDestinationFolder, simpleFileName);
                reports.add(report);
            }
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

        report.println("Between " + traceReport1.getSimpleFileName() + " and " + traceReport2.getSimpleFileName());

        int totalDifferences = 0;

        for (ByteRateOfChange.ByteId id : allKeys) {
            ByteRateOfChange.ByteStatistics s1 = traceReport1.getStatistics().computeIfAbsent(id, byteId -> new ByteRateOfChange.ByteStatistics(byteId));
            ByteRateOfChange.ByteStatistics s2 = traceReport2.getStatistics().computeIfAbsent(id, byteId -> new ByteRateOfChange.ByteStatistics(byteId));

            if (s1.getUniqueValues() != s2.getUniqueValues()) {
                report.println(id + ": " + s1.getUniqueValues() + " vs " + s2.getUniqueValues());
                totalDifferences++;
            }

        }

        report.println(totalDifferences + " total differences");
        report.println();
        report.println();
        report.println();
        report.println();
    }
}
