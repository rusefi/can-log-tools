package com.rusefi.can.reader.dbc;

import com.rusefi.can.dbc.DbcFile;
import com.rusefi.can.dbc.reader.DbcFileReader;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class DbcFileConflictTest {
    @Test(expected = IllegalStateException.class)
    public void testConflict() throws IOException {
        String dbcContent = "BO_ 100 P1: 8 XXX\n" +
                " SG_ S1 : 0|8@1+ (1,0) [0|0] \"\" XXX\n" +
                "BO_ 100 P2: 8 XXX\n" +
                " SG_ S2 : 0|8@1+ (1,0) [0|0] \"\" XXX\n";

        DbcFile dbc = new DbcFile();
        DbcFileReader.read(dbc, new BufferedReader(new StringReader(dbcContent)));
    }
}
