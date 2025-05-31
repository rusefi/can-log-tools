package com.rusefi.can;

import com.rusefi.can.analysis.ByteRateOfChange;
import com.rusefi.can.analysis.CanMetaDataContext;
import com.rusefi.can.reader.CANLineReader;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.can.reader.dbc.DbcPacket;
import com.rusefi.util.FolderUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AlwaysSameScanner {

    private static final Map<ByteRateOfChange.ByteId, Integer> existingValue = new TreeMap<>();

    public static void run(String inputFolderName, DbcFile dbc) throws IOException {
        File inputFolder = new File(inputFolderName);
        for (String simpleFileName : inputFolder.list()) {
            String fullInputFile = inputFolderName + File.separator + simpleFileName;
            if (new File(fullInputFile).isDirectory()) {
                System.out.println("Recursion " + fullInputFile);
                run(fullInputFile, dbc);
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

    public static void report(DbcFile dbc) {

        for (Map.Entry<ByteRateOfChange.ByteId, Integer> e : existingValue.entrySet()) {

            Integer value = e.getValue();
            if (value == Integer.MAX_VALUE) {
                // not unique byte
                continue;
            }

            int sid = e.getKey().getSid();
            DbcPacket packet = dbc.findPacket(sid);
            String name = packet == null ? Integer.toString(sid) : packet.getName();


            System.out.println(name + " index " + e.getKey().getByteIndex() + " is always same " + value);


        }

    }
}
