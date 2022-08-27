package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.dbc.DbcField;
import com.rusefi.can.reader.dbc.DbcPacket;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParseDbcFieldTest {

    private static final double EPS = 0.001;

    @Test
    public void parseIat() {
        String line = "SG_ Ansauglufttemperatur : 8|8@1+ (0.75,-48) [-48|142.5] \"\" XXX";
        DbcPacket parent = new DbcPacket(1, "hello");
        DbcField iatField = DbcField.parseField(parent, line);
        assertEquals("Ansauglufttemperatur", iatField.getName());
        assertEquals(0.75, iatField.getMult(), EPS);
        assertEquals(-48, iatField.getOffset(), EPS);

        CANPacket packet = new PcanTrcReader().readLine("  2197)      1234.8  Rx         0380  8  00 62 FA 00 22 00 00 FA");
        assertEquals(8, packet.getData().length);

        assertEquals(25.5, iatField.getValue(packet), EPS);
    }
}
