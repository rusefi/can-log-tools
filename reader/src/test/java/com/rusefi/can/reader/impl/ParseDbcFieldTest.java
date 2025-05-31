package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.dbc.DbcField;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParseDbcFieldTest {

    private static final double EPS = 0.001;

    @Test
    public void parseIat() {
        String line = "SG_ Ansauglufttemperatur : 8|8@1+ (0.75,-48) [-48|142.5] \"\" XXX";
        DbcField iatField = DbcField.parseField(line, "hello", -1);
        assertEquals("Ansauglufttemperatur", iatField.getName());
        assertEquals(0.75, iatField.getMult(), EPS);
        assertEquals(-48, iatField.getOffset(), EPS);

        CANPacket packet = new PcanTrcReader1_1().readLine("  2197)      1234.8  Rx         0380  8  00 62 FA 00 22 00 00 FA");
        assertEquals(8, packet.getData().length);

        assertEquals(25.5, iatField.getValue(packet), EPS);
    }

    @Test
    public void testHumanStartIndex() {
        String line = "SG_ CrksftNTrnsRegCmdTq : 51|12@0+ (0.5,-848) [-848|1199.5] \"Nm\"  TCM_HS";
        DbcField field = DbcField.parseField(line, "hello", -1);
        assertEquals(56, field.getStartOffset());
        assertEquals(12, field.getLength());

        assertEquals(52, field.getHumanStartIndex());
    }
}
