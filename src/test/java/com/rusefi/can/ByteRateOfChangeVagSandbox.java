package com.rusefi.can;

import com.rusefi.can.analysis.ByteRateOfChangeReports;
import com.rusefi.can.reader.ReaderType;
import com.rusefi.can.reader.ReaderTypeHolder;

import java.io.IOException;

public class ByteRateOfChangeVagSandbox {
    public static void main(String[] args) throws IOException {
        ReaderTypeHolder.INSTANCE.type = ReaderType.PCAN1_1;

        String inputFolderName = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\VAG\\2006-Passat-B6";

        ByteRateOfChangeReports.scanInputFolder(inputFolderName, "fast-acceleration3.trc");
    }
}
