package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.LoggingStrategy;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.sensor_logs.BinaryLogEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static com.rusefi.can.reader.impl.ParseDBC.VAG_MOTOR_1;

public class TrcToMlqSandbox {

    //    private static final String fileName = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\VAG\\2006-Passat-B6\\passat-b6-stock-ecu-ecu-ptcan-not-running-pedal-up-and-down.trc";
    private static final String trcFileName = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\VAG\\2006-Passat-B6\\passat-b6-stock-ecu-ecu-ptcan-parked-revving.trc";

    public static void main(String[] args) throws IOException {
        DbcFile dbc = new DbcFile();
        {
            BufferedReader reader = new BufferedReader(new StringReader(VAG_MOTOR_1));
            dbc.read(reader);
        }

        List<BinaryLogEntry> entries = LoggingStrategy.getFieldNameEntries(dbc);

        List<CANPacket> packets = new PcanTrcReader().readFile(trcFileName);
        System.out.println(packets.size() + " packets");


        LoggingStrategy.writeLog(dbc, entries, packets);
    }

}
