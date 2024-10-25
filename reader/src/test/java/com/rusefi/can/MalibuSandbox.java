package com.rusefi.can;

import com.rusefi.can.reader.dbc.DbcFile;

import java.io.IOException;

public class MalibuSandbox {
    public static void main(String[] args) throws IOException {
        String inputFolderName = "C:\\stuff\\gen5-private\\2013-malibu\\can-traces";
//        String inputFolderName = "C:\\stuff\\gen5-private\\2013-malibu\\can-traces\\diag";
//        String inputFolderName = "C:\\stuff\\gen5-private\\2013-malibu\\can-traces\\tcu";
//        String inputFolderName = "C:\\stuff\\gen5-private\\2013-malibu\\can-traces\\bus3";
//        String inputFolderName = "C:\\stuff\\gen5-private\\2013-malibu\\can-traces\\wip";

        Launcher.dbcFileName = "C:\\stuff\\gen5-private\\2013-malibu\\malibu.dbc";
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
