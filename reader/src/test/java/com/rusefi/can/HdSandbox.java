package com.rusefi.can;

import com.rusefi.can.reader.dbc.DbcFile;

import java.io.IOException;

public class HdSandbox {
    public static void main(String[] args) throws IOException {
        String inputFolderName = "C:\\stuff\\hd-iws\\can-traces-2023-st\\N-switch";

        Launcher.dbcFileName = "C:\\stuff\\hd-iws\\hd2021.dbc";
        DbcFile.applyOrderForStartOffset = true;


        Launcher.main(new String[]{
                inputFolderName,
                Launcher.DBC_FILENAME_PROPERTY,
                Launcher.dbcFileName,
        });
    }}
