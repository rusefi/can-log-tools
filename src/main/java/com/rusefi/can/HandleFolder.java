package com.rusefi.can;

import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.can.reader.impl.PcanTrcReader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class HandleFolder {
    public static void doJob(String dbcFileName, String inputFolderName, String outputFolder) throws IOException {
        System.out.println("Dbc file " + dbcFileName);
        System.out.println("inputFolderName " + inputFolderName);
        System.out.println("outputFolder " + outputFolder);
        DbcFile dbc = DbcFile.readFromFile(dbcFileName);

        File inputFolder = new File(inputFolderName);
        for (String inputFile : Objects.requireNonNull(inputFolder.list((dir, name) -> name.endsWith(".trc")))) {
            System.out.println("Handling " + inputFile);

            String fullInputFile = inputFolderName + File.separator + inputFile;

            List<CANPacket> packets = new PcanTrcReader().readFile(fullInputFile);

            String outputFileName = outputFolder + File.separator + inputFile;

            LoggingStrategy.writeLog(dbc, packets, outputFileName);
        }
    }
}
