package com.rusefi.can;

import com.rusefi.can.analysis.ByteRateOfChangeReports;

import java.io.IOException;

public class NissanSandbox {
    public static void main(String[] args) throws IOException {
        String inputFolderName = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\Nissan\\2011_Xterra\\2011-nissan-CAN-June-2021";

        ByteRateOfChangeReports.scanInputFolder(inputFolderName, ".trc");

    }
}
