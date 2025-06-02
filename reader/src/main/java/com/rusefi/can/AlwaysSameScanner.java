package com.rusefi.can;

import com.rusefi.can.reader.CANLineReader;
import com.rusefi.can.reader.dbc.DbcField;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.can.reader.dbc.DbcPacket;
import com.rusefi.util.FolderUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AlwaysSameScanner {

    private static final Map<DbcField, Integer> existingValue = new TreeMap<>();

    public static void run(String reportDestinationFolder, String inputFolderName, DbcFile dbc) throws IOException {
        runRecursion(inputFolderName, dbc);
        report(reportDestinationFolder, dbc);
    }

    private static void runRecursion(String inputFolderName, DbcFile dbc) throws IOException {
        File inputFolder = new File(inputFolderName);
        for (String simpleFileName : inputFolder.list()) {
            String fullInputFile = inputFolderName + File.separator + simpleFileName;
            if (new File(fullInputFile).isDirectory()) {
                System.out.println("Recursion " + fullInputFile);
                runRecursion(fullInputFile, dbc);
            }
        }

        FolderUtil.handleFolder(inputFolderName, (simpleFileName, fullInputFileName) -> {
            System.out.println("File " + simpleFileName + " " + fullInputFileName);

            List<CANPacket> logFileContent = CANLineReader.getReader().readFile(fullInputFileName);

            for (CANPacket packet : logFileContent) {

                DbcPacket meta = dbc.getPacket(packet.getId());
                for (DbcField field : meta.getFields()) {

                    int currentValue = field.getRawValue(packet);
                    Integer existing = existingValue.putIfAbsent(field, currentValue);
                    if (existing == null) {
                        // first time we are hitting this byte
                        continue;
                    } else if (existing == currentValue) {
                        // same value, still unit
                        continue;
                    } else {
                        existingValue.put(field, Integer.MAX_VALUE);
                    }
                }
            }
        }, Launcher.fileNameSuffixValue);
    }

    private static void report(String reportDestinationFolder, DbcFile dbc) throws IOException {

        try (Writer w = new FileWriter(reportDestinationFolder + File.separator + "always_same_report.txt")) {

            for (Map.Entry<DbcField, Integer> e : existingValue.entrySet()) {
                DbcField field = e.getKey();
                int sid = field.getSid();
                DbcPacket packet = dbc.findPacket(sid);
                String name = packet.getName();

                Integer value = e.getValue();
                boolean isDynamic = value == Integer.MAX_VALUE;
                if (isDynamic) {
                    // not unique byte
                    w.append(name + " " + field + " is DYNAMIC" + "\n");
                    continue;
                }


                w.append(name + " " + field + " is always same " + value + "\n");
            }
        }

    }
}
