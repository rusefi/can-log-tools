package com.rusefi.can.reader.impl;

import com.rusefi.can.reader.dbc.DbcFile;

import java.io.IOException;

public class ReadFullVagDbc {
    public static void main(String[] args) throws IOException {
        String fileName = "opendbc/vw_golf_mk4.dbc";

        DbcFile.readFromFile(fileName);
    }
}
