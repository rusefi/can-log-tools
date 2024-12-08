package com.rusefi.can;

import com.rusefi.can.reader.dbc.DbcFile;

import java.io.IOException;

public class AtsSandbox {
    public static void main(String[] args) throws IOException {
        String inputFolderName = "C:\\stuff\\gen5-private\\2018-ats\\can-traces";

        Launcher.dbcFileName = "C:\\stuff\\gen5-private\\2018-ats\\ats.dbc";
        //Launcher.dbcFileName = "C:\\stuff\\gen5-private\\2013-malibu\\malibu3.dbc";
        DbcFile.applyOrderForStartOffset = true;

//        DbcFile dbc = DbcFile.readFromFile(Launcher.dbcFileName);
//        AlwaysSameScanner.run(inputFolderName, dbc);
//        AlwaysSameScanner.report(dbc);


        Launcher.main(new String[]{
                inputFolderName,
                Launcher.DBC_FILENAME_PROPERTY,
                Launcher.dbcFileName,
        });
    }
}
