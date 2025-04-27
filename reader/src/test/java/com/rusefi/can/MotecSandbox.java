package com.rusefi.can;

import com.rusefi.can.reader.dbc.DbcFile;

import java.io.IOException;

public class MotecSandbox {
    public static void main(String[] args) throws IOException {
        String inputFolderName = "C:\\stuff\\fw\\gm-lnf\\motec\\CANbus";
        Launcher.dbcFileName = "C:\\stuff\\fw\\gm-lnf\\motec\\motec-ecu.dbc";

        DbcFile.applyOrderForStartOffset = true;
        Launcher.main(new String[]{
                inputFolderName,
                Launcher.DBC_FILENAME_PROPERTY,
                Launcher.dbcFileName,
        });
    }
}
