package com.rusefi.can;

import com.rusefi.can.analysis.ByteUniqueValuesReports;
import com.rusefi.can.reader.ReaderType;
import com.rusefi.can.reader.ReaderTypeHolder;

import java.io.IOException;

public class ByteRateOfChangeNissanSandbox {
    public static void main(String[] args) throws IOException {
        ReaderTypeHolder.INSTANCE.type = ReaderType.PCAN;

        String inputFolderName = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\Nissan\\2011_Xterra\\CAN-Nov-2022";

        ByteUniqueValuesReports.scanInputFolder(inputFolderName, "pcan.trc");
    }
}
