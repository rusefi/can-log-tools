package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.mlv.LoggingStrategy;
import com.rusefi.can.reader.dbc.DbcField;
import com.rusefi.can.reader.dbc.DbcFile;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static com.rusefi.can.reader.impl.ParseDBCTest.VAG_MOTOR_1;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.*;

public class GetValueFromTrcTest {

    public static final double EPS = 0.01;

    private static int getBitIndex(byte[] data, int bitIndex, int bitWidth) {
        return DbcField.getBitRange(data, bitIndex, bitWidth, false);
    }

    @Test
    public void testBigEndian() {
        byte[] rpm = {(byte) 0x70, 0x04, 0x1F};
        assertEquals(0x41f, DbcField.getBitRange(rpm, 16, 16, true));

        byte[] data = {(byte) 0xAB, 0x56, 0x34};

        assertEquals(0x56AB, DbcField.getBitRange(data, 0, 16, false));
        assertEquals(0xAB56, DbcField.getBitRange(data, 8, 16, true));

        assertEquals(0xAB, DbcField.getBitRange(data, 0, 8, false));
        assertEquals(0xAB, DbcField.getBitRange(data, 0, 8, true));

        assertEquals(0x56, DbcField.getBitRange(data, 8, 8, false));
        assertEquals(0x56, DbcField.getBitRange(data, 8, 8, true));

        // yes we have a defect for sure, we only touch two bytes at most while in this case we shall touch three bytes
        assertEquals(0x56A, DbcField.getBitRange(data, 4, 16, false));
    }

    @Test
    public void test() throws IOException {
        DbcFile dbc = new DbcFile(LoggingStrategy.LOG_ONLY_TRANSLATED_FIELDS);
        {
            BufferedReader reader = new BufferedReader(new StringReader(VAG_MOTOR_1));
            dbc.read(reader);
        }
        assertNotNull(dbc.findPacket(640));
        assertNull(dbc.findPacket(1640));

        String trcLine = "  3769)      2117.7  Rx         0280  8  01 1D DF 12 1E 00 1A 1E ";

        PcanTrcReader1_1 reader = new PcanTrcReader1_1();
        CANPacket packet = reader.readLine(trcLine);
        assertEquals(8, packet.getData().length);
        assertEquals(640, packet.getId());

        assertEquals(0x12DF, getBitIndex(packet.getData(), 16, 16));
        assertEquals(0xDF1D, getBitIndex(packet.getData(), 8, 16));

        assertEquals(1, getBitIndex(packet.getData(), 0, 3));

        assertEquals(0x1D, getBitIndex(packet.getData(), 8, 8));
        assertEquals(13, getBitIndex(packet.getData(), 8, 4));


        DbcField bf = dbc.getPacketByIndexSlow(0).find("rpm");

        assertEquals(1207.75, bf.getValue(packet), EPS);

        System.out.println(packet);
    }
}
