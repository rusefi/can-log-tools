package com.rusefi.can;

import com.rusefi.can.analysis.ByteRateOfChange;
import com.rusefi.can.reader.CANLineReader;
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

    private static final Map<ByteRateOfChange.ByteId, Integer> existingValue = new TreeMap<>();

    public static void run(String reportDestinationFolder, String inputFolderName, DbcFile dbc) throws IOException {
        runRecursion(inputFolderName);
        report(reportDestinationFolder, dbc);
    }

    private static void runRecursion(String inputFolderName) throws IOException {
        File inputFolder = new File(inputFolderName);
        for (String simpleFileName : inputFolder.list()) {
            String fullInputFile = inputFolderName + File.separator + simpleFileName;
            if (new File(fullInputFile).isDirectory()) {
                System.out.println("Recursion " + fullInputFile);
                runRecursion(fullInputFile);
            }
        }

        FolderUtil.handleFolder(inputFolderName, (simpleFileName, fullInputFileName) -> {
            System.out.println("File " + simpleFileName + " " + fullInputFileName);

            List<CANPacket> logFileContent = CANLineReader.getReader().readFile(fullInputFileName);

            for (CANPacket packet : logFileContent) {

                for (int i = 0; i < packet.getData().length; i++) {
                    ByteRateOfChange.ByteId id = ByteRateOfChange.ByteId.createByte(packet.getId(), i);

                    int currentValue = packet.getData()[i];
                    Integer existing = existingValue.putIfAbsent(id, currentValue);
                    if (existing == null) {
                        // first time we are hitting this byte
                        continue;
                    } else if (existing == currentValue) {
                        // same value, still unit
                        continue;
                    } else {
                        existingValue.put(id, Integer.MAX_VALUE);
                    }
                }
            }
        }, Launcher.fileNameSuffixValue);
    }

    private static void report(String reportDestinationFolder, DbcFile dbc) throws IOException {

        try (Writer w = new FileWriter(reportDestinationFolder + File.separator + "always_same_report.txt")) {

            for (Map.Entry<ByteRateOfChange.ByteId, Integer> e : existingValue.entrySet()) {
                int sid = e.getKey().getSid();
                DbcPacket packet = dbc.findPacket(sid);
                String name = packet == null ? Integer.toString(sid) : packet.getName();

                Integer value = e.getValue();
                if (value == Integer.MAX_VALUE) {
                    // not unique byte
                    w.append(name + " index " + e.getKey().getByteIndex() + " is DYNAMIC" + "\n");
                    continue;
                }


                w.append(name + " index " + e.getKey().getByteIndex() + " is always same " + value + "\n");
            }
        }

    }
}
