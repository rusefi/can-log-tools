package com.rusefi.can;

import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.can.reader.impl.PcanTrcReader;
import com.rusefi.sensor_logs.BinaryLogEntry;

import java.io.IOException;
import java.util.List;

public class TrcToMlq {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Two arguments expected - DBC file name and TRC file name");
            System.exit(-1);
        }
        String dbcFileName = args[0];
        String trcFileName = args[1];

        DbcFile dbc = DbcFile.readFromFile(dbcFileName);

        List<BinaryLogEntry> entries = LoggingStrategy.getFieldNameEntries(dbc);

        List<CANPacket> packets = new PcanTrcReader().readFile(trcFileName);
        System.out.println("Got " + packets.size() + " CAN packets");

        LoggingStrategy.writeLog(dbc, entries, packets);
    }
}
