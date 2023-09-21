package com.rusefi.can;

import com.rusefi.can.reader.ReaderType;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.mlv.LoggingStrategy;

import java.io.IOException;
import java.util.List;

import static com.rusefi.can.reader.CANLineReader.getReader;

public class TrcToMlq {
    public static ReaderType parseCurrentReaderTypeSetting() {
        String property = System.getProperty("TRACE_READER", ReaderType.PCAN2.name());
        ReaderType readerType = ReaderType.valueOf(property);
        System.out.println("getCurrentReaderType: " + readerType + " for [" + property + "]");
        return readerType;
    }

    public static void main(String[] args) throws IOException {
        LoggingStrategy.LOG_ONLY_TRANSLATED_FIELDS = true;

        if (args.length == 3) {
            String dbcFileName = args[0];
            String inputFolder = args[1];
            String outputFolder = args[2];
            ConvertTrcToMegaLogViewerWithDBC.doJob(dbcFileName, inputFolder, outputFolder);
        } else if (args.length != 2) {
            System.err.println("Two or three arguments expected:");
            System.err.println("   either");
            System.err.println("fileName.DBC traceFile.name");
            System.err.println("  it would output to gauges.mlg");
            System.err.println("   or");
            System.err.println("fileName.DBC tracesInputFolder outputFolder");
            System.err.println("");
            System.err.println("");
            System.err.println("By default PCAN format is used");
            System.err.println("For can hacker format:");
            System.err.println("   java -DTRACE_READER=CANHACKER");
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
