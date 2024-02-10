package com.rusefi.can;

import java.io.IOException;

public class KiaSandbox {
    public static void main(String[] args) throws IOException {
        String inputFolderName = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\Kia\\2013-CAN-logs";

        Launcher.main(new String[]{inputFolderName});
    }
}
