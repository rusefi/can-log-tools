package com.rusefi.can;

import com.rusefi.can.reader.CANLineReader;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.can.reader.impl.PcanTrcReader1_1;
import com.rusefi.can.reader.impl.ReadFullVagDbc;
import com.rusefi.mlv.LoggingStrategy;

import java.io.IOException;
import java.util.List;

public class VagB6Sandbox {
    public static void main(String[] args) throws IOException {
        DbcFile dbc = DbcFile.readFromFile(ReadFullVagDbc.VAG_DBC_FILE);

        CANLineReader reader = new PcanTrcReader1_1();
        String file = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\VAG\\2006-Passat-B6\\passat-back-and-forth-60-seconds.trc1";

        List<CANPacket> packets = reader.readFile(file);

        String outputFileName = "vag.mlg";
        LoggingStrategy.writeLog(dbc, packets, outputFileName);
    }
}
