package com.rusefi.can.reader.impl;

import com.rusefi.can.reader.dbc.DbcFile;

import java.io.IOException;

public class ReadFullVagDbc {
    public static final String VAG_DBC_FILE = "opendbc/vw_golf_mk4.dbc";

    public static void main(String[] args) throws IOException {
        DbcFile.readFromFile(VAG_DBC_FILE);
    }
}
