package com.rusefi.can;

import java.io.IOException;

public class MotecSandbox {
    public static void main(String[] args) throws IOException {
        String inputFolderName = "C:\\stuff\\fw\\gm-lnf\\motec\\CANbus";
        Launcher.dbcFileName = "C:\\stuff\\fw\\gm-lnf\\motec\\motec-ecu.dbc";

        Launcher.main(new String[]{
                inputFolderName,
                Launcher.DBC_FILENAME_PROPERTY,
                Launcher.dbcFileName,
        });
    }
}
