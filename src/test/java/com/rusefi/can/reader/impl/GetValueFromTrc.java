package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.dbc.DbcField;
import com.rusefi.can.reader.dbc.DbcFile;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static com.rusefi.can.reader.impl.ParseDBCTest.VAG_MOTOR_1;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GetValueFromTrc {

    public static final double EPS = 0.01;

    @Test
    public void test() throws IOException {
        DbcFile dbc = new DbcFile();
        {
            BufferedReader reader = new BufferedReader(new StringReader(VAG_MOTOR_1));
            dbc.read(reader);
        }
        assertNotNull(dbc.findPacket(640));
        assertNull(dbc.findPacket(1640));

        String trcLine = "  3769)      2117.7  Rx         0280  8  01 1D DF 12 1E 00 1A 1E ";

        PcanTrcReader reader = new PcanTrcReader();
        CANPacket packet = reader.readLine(trcLine);
        assertEquals(8, packet.getData().length);
        assertEquals(640, packet.getId());

        assertEquals(0x12DF, DbcField.getBitIndex(packet.getData(), 16, 16));
        assertEquals(0xDF1D, DbcField.getBitIndex(packet.getData(), 8, 16));

        assertEquals(1, DbcField.getBitIndex(packet.getData(), 0, 3));

        assertEquals(0x1D, DbcField.getBitIndex(packet.getData(), 8, 8));
        assertEquals(13 , DbcField.getBitIndex(packet.getData(), 8, 4));


        DbcField bf = dbc.packets.get(0).find("rpm");

        assertEquals(1207.75, bf.getValue(packet), EPS);

        System.out.println(packet);
    }
}
