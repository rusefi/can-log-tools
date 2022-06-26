package com.rusefi.can;

import com.rusefi.can.reader.dbc.DbcFile;

import java.io.IOException;

public class TrcToMlq {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Two arguments expected - DBC file name and TRC file name");
            System.exit(-1);
        }
        String dbcFileName = args[0];
        String trcFileName = args[1];

        DbcFile.readFromFile(dbcFileName);

    }
}
