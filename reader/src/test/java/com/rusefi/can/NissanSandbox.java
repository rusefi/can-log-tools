package com.rusefi.can;

import java.io.IOException;

public class NissanSandbox {
    public static void main(String[] args) throws IOException {
        String inputFolderName = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\Nissan\\2011_Xterra\\2011-nissan-CAN-June-2021";

        Launcher.main(new String[]{
                inputFolderName,
                Launcher.DBC_FILENAME_PROPERTY,
                "opendbc/nissan_xterra_2011.dbc",
        });
    }
}
