package com.rusefi.can;

import com.rusefi.can.reader.ReaderType;
import com.rusefi.can.reader.dbc.DbcFile;

import java.io.IOException;
import java.util.List;

import static com.rusefi.can.reader.CANLineReader.getReader;

public class TrcToMlq {
    public static ReaderType getCurrentReaderType() {
        return ReaderType.PCAN;
    }

    public static void main(String[] args) throws IOException {
        LoggingStrategy.LOG_ONLY_TRANSLATED_FIELDS = true;

        if (args.length == 3) {
            String dbcFileName = args[0];
            String inputFolder = args[1];
            String outputFolder = args[2];
            HandleFolder.doJob(dbcFileName, inputFolder, outputFolder);
        } else if (args.length != 2) {
            System.err.println("Two arguments expected - DBC file name and TRC file name");
            System.exit(-1);
        } else {
            String dbcFileName = args[0];
            String inputFileName = args[1];

            DbcFile dbc = DbcFile.readFromFile(dbcFileName);

            List<CANPacket> packets = getReader().readFile(inputFileName);

            String outputFileName = System.getProperty("mlq_file_name", "gauges.mlg");
            LoggingStrategy.writeLog(dbc, packets, outputFileName);
        }
    }
}
