package com.rusefi.can;

import com.rusefi.can.analysis.ByteRateOfChangeReports;
import com.rusefi.can.analysis.PerSidDump;
import com.rusefi.can.reader.CANLineReader;
import com.rusefi.can.reader.ReaderType;
import com.rusefi.can.reader.ReaderTypeHolder;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.can.reader.impl.PcanTrcReader1_1;
import com.rusefi.can.reader.impl.ReadFullVagDbc;
import com.rusefi.mlv.LoggingStrategy;

import java.io.IOException;
import java.util.List;

import static com.rusefi.can.analysis.ByteRateOfChangeReports.createOutputFolder;

public class VagB6Sandbox {
    public static void main(String[] args) throws IOException {
        DbcFile dbc = DbcFile.readFromFile(ReadFullVagDbc.VAG_DBC_FILE);

        CANLineReader reader = new PcanTrcReader1_1();
        String inputFolderName = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\VAG\\2006-Passat-B6\\";

        {
            ReaderTypeHolder.INSTANCE.type = ReaderType.PCAN;
            ByteRateOfChangeReports.scanInputFolder(inputFolderName + "2023.10-tcu-side2", ".trc");
        }


        String simpleFileName = "passat-back-and-forth-60-seconds.trc1";
        String file = inputFolderName + simpleFileName;

        String reportDestinationFolder = createOutputFolder(inputFolderName);


        List<CANPacket> packets = reader.readFile(file);

        PerSidDump.handle(reportDestinationFolder, simpleFileName, packets);


        String outputFileName = "vag.mlg";
        LoggingStrategy.writeLog(dbc, packets, outputFileName);
    }
}
