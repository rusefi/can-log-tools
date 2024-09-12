package com.rusefi.can;

import com.rusefi.can.analysis.ByteRateOfChangeReports;
import com.rusefi.can.reader.impl.ReadFullVagDbc;

import java.io.IOException;

public class VagB6Sandbox {
    public static void main(String[] args) throws IOException {
//        DbcFile dbc = DbcFile.readFromFile(ReadFullVagDbc.VAG_DBC_FILE);

        String inputFolderName = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\VAG\\2006-Passat-B6\\";

        ByteRateOfChangeReports.filter = dbcField -> {
            String name = dbcField.getName();
            return name.startsWith("CHECKSUM_") || name.startsWith("COUNTER_");
        };

        Launcher.main(new String[]{inputFolderName,
                Launcher.FILENAME_FILTER_PROPERTY,
                "passat-back-and-forth-60-seconds",
                Launcher.DBC_FILENAME_PROPERTY,
                ReadFullVagDbc.VAG_DBC_FILE
        });

//        {
//            ReaderTypeHolder.INSTANCE.type = ReaderType.PCAN;
//            ByteRateOfChangeReports.scanInputFolder(inputFolderName + "2023.10-tcu-side2", ".trc");
//        }

//        String name = "passat-back-and-forth-60-seconds";
//        String simpleFileName = name + ".trc";


//
//        CANLineReader reader = new AutoFormatReader();
//        String file = inputFolderName + simpleFileName;
//
//        String reportDestinationFolder = createOutputFolder(inputFolderName);
//
//
//        List<CANPacket> packets = reader.readFile(file);
//
//        PerSidDump.handle(reportDestinationFolder, simpleFileName, packets);
//
//
    }
}
