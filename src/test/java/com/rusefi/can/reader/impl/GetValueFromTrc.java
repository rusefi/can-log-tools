package com.rusefi.can.reader.impl;

import com.rusefi.can.reader.dbc.DbcFile;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static com.rusefi.can.reader.impl.ParseDBC.VAG_MOTOR_1;

public class GetValueFromTrc {
    @Test
    public void test() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(VAG_MOTOR_1));

        DbcFile dbc = new DbcFile();
        dbc.read(reader);


        String trcLine = "  3769)      2117.7  Rx         0280  8  01 1D DF 12 1E 00 1A 1E ";

    }
}
