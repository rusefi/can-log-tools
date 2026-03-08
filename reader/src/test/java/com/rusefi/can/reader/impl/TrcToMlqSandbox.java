package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.dbc.reader.DbcFileReader;
import com.rusefi.mlv.LoggingStrategy;
import com.rusefi.can.dbc.DbcFile;
import com.rusefi.can.reader.impl.TestCases;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static com.rusefi.can.reader.impl.TestCases.VAG_MOTOR_1;

public class TrcToMlqSandbox {

    //    private static final String fileName = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\VAG\\2006-Passat-B6\\passat-b6-stock-ecu-ecu-ptcan-not-running-pedal-up-and-down.trc";
    private static final String trcFileName = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\VAG\\2006-Passat-B6\\passat-b6-stock-ecu-ecu-ptcan-parked-revving.trc";

    public static void main(String[] args) throws IOException {
        DbcFile dbc = new DbcFile();
        LoggingStrategy loggingStrategy = new LoggingStrategy();
        {
            BufferedReader reader = new BufferedReader(new StringReader(VAG_MOTOR_1));
            DbcFileReader.read(dbc, reader);
        }

        List<CANPacket> packets = new PcanTrcReader1_1().readFile(trcFileName);
        System.out.println(packets.size() + " packets");

        loggingStrategy.writeLogByDbc(dbc, packets, "gauges.mlg");
    }

}
