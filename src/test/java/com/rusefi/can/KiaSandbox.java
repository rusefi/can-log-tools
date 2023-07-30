package com.rusefi.can;

import com.rusefi.can.analysis.ByteRateOfChangeReports;
import com.rusefi.can.reader.ReaderType;
import com.rusefi.can.reader.ReaderTypeHolder;

import java.io.IOException;

public class KiaSandbox {
    public static void main(String[] args) throws IOException {
        ReaderTypeHolder.INSTANCE.type = ReaderType.PCAN2;

        String inputFolderName = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\Kia\\2013-CAN-logs";

        ByteRateOfChangeReports.scanInputFolder(inputFolderName, ".trc");
    }
}
