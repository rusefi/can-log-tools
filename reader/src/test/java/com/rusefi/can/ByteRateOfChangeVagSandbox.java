package com.rusefi.can;

import com.rusefi.can.analysis.ByteUniqueValuesReports;

import java.io.IOException;

public class ByteRateOfChangeVagSandbox {
    public static void main(String[] args) throws IOException {
        String inputFolderName = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\VAG\\2006-Passat-B6";

        ByteUniqueValuesReports.scanInputFolder(inputFolderName, "fast-acceleration3.trc");
    }
}
