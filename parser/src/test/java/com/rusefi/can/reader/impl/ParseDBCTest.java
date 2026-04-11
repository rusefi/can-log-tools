package com.rusefi.can.reader.impl;

import com.rusefi.can.dbc.Bitness;
import com.rusefi.can.dbc.DbcField;
import com.rusefi.can.dbc.DbcFile;
import com.rusefi.can.dbc.DbcPacket;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

public class ParseDBCTest {
    private static final String RPM_DBC = "VERSION \"\"\n" +
            "\n" +
            "\n" +
            "NS_ :\n" +
            "\tNS_DESC_\n" +
            "\tCM_\n" +
            "\tBA_DEF_\n" +
            "\tBA_\n" +
            "\tVAL_\n" +
            "\tCAT_DEF_\n" +
            "\tCAT_\n" +
            "\tFILTER\n" +
            "\tBA_DEF_DEF_\n" +
            "\tEV_DATA_\n" +
            "\tENVVAR_DATA_\n" +
            "\tSGTYPE_\n" +
            "\tSGTYPE_VAL_\n" +
            "\tBA_DEF_SGTYPE_\n" +
            "\tBA_SGTYPE_\n" +
            "\tSIG_TYPE_REF_\n" +
            "\tVAL_TABLE_\n" +
            "\tSIG_GROUP_\n" +
            "\tSIG_VALTYPE_\n" +
            "\tSIGTYPE_VALTYPE_\n" +
            "\n" +
            "BS_:\n" +
            "\n" +
            "BU_: XXX\n" +
            "\n" +
            "\n" +
            "BO_ 1394 ZAS_1: 8 XXX\n" +
            " SG_ Fehlerspeichereintrag__ZAS_ : 15|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Frei_ZAS_1_3 : 8|7@0+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Frei_ZAS_1_2 : 7|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Klemme_15_SV : 6|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Frei_ZAS_1_1 : 5|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Klemme_P__Parklichtstellung_ : 4|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Klemme_50__Starten_ : 3|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Klemme_X__Startvorgang_ : 2|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Klemme_15__Z_ndung_ein_ : 1|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ S_Kontakt__Schl_ssel_steckt_ : 0|1@1+ (1,0) [0|0] \"\" XXX\n" +
            "\n" +
            "BO_ 1336 Wischer_1: 8 XXX\n" +
            " SG_ Blockierung_Heckwischer_erkannt : 15|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Frei_Wischer_1_2 : 12|3@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Fehlerspeichereintrag__Wischer_ : 11|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Ansteuerung_Scheibenwischer_Hec : 10|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Ansteuerung_Wascher_Heck : 9|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Scheibenwischer_Heck_eingeschal : 8|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Blockierung_Frontwischer_erkann : 7|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Frei_Wischer_1_1 : 6|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Status_Waschduesenheizung : 5|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Parklage_Frontwischer : 4|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Ansteuerung_Frontwischer_Schnel : 3|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Ansteuerung_Frontwischer_Normal : 2|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Ansteuerung_Wascher_Front : 1|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Frontwischer__eingeschaltet : 0|1@1+ (1,0) [0|0] \"\" XXX\n" +
            "\n" +
            TestCases.VAG_MOTOR_1;

        public static final String VAG_MOTOR_1 = RPM_DBC;

    @Test
    public void parse() throws IOException {

        DbcFile dbc = TestCases.readDbc(RPM_DBC);
        assertEquals(3, dbc.size());

        DbcPacket zacPacket = dbc.getPacketByIndexSlow(0);
        assertFalse(zacPacket.getFields().get(0).isBigEndian());
        assertTrue(zacPacket.getFields().get(1).isBigEndian());

        DbcPacket motorPacket = dbc.getPacketByIndexSlow(2);
        assertNotNull(motorPacket);
        assertEquals(640, motorPacket.getId());

        DbcField rpm = motorPacket.find("RPM");
        assertEquals(0.25, rpm.getMult());
        assertEquals("Motor_1", rpm.getCategory());
        assertEquals("XXX", motorPacket.getSource());
    }

    @Test
    public void testSource() throws IOException {
        String dbcText = "BO_ 1394 ZAS_1: 8 XXX\n" +
                " SG_ Field : 0|8@1+ (1,0) [0|0] \"\" XXX";
        DbcFile dbc = TestCases.readDbc(dbcText);
        DbcPacket packet = dbc.findPacket(1394);
        assertEquals("XXX", packet.getSource());
        assertEquals(8, packet.getLength());

        String dbcText2 = "BO_ 100 P: 8 ECM_HS\n" +
                " SG_ OAT : 7|8@0+ (1,0) [0|8] \"deg C\"  VICS";
        DbcFile dbc2 = TestCases.readDbc(dbcText2);
        DbcPacket packet2 = dbc2.findPacket(100);
        assertEquals("ECM_HS", packet2.getSource());
        assertEquals(8, packet2.getLength());
    }

    @Test
    public void parseMoto() throws IOException {
        String moto = "BO_ 100 P: 8 ECM_HS\n" +
                " SG_ OAT : 7|8@0+ (1,0) [0|8] \"deg C\"  VICS";

        DbcFile dbc = TestCases.readDbc(moto);
        assertEquals(1, dbc.size());
        DbcPacket packet = dbc.findPacket(100);
        assertNotNull(packet);

        DbcField f = packet.getFields().get(0);
        assertEquals(0, f.getStartOffset());
    }

    @Test
    public void crazyMotorola() {
        assertEquals(24, DbcField.crazyMotorolaMath(27, 4, true));
        assertEquals(24, DbcField.crazyMotorolaMath(30, 7, true));
        assertEquals(24, DbcField.crazyMotorolaMath(31, 8, true));
        assertEquals(24, DbcField.crazyMotorolaMath(17, 10, true));

        assertEquals(24, DbcField.crazyMotorolaMath(17, 10, true));
    }

    @Test
    public void signedValue() throws IOException {
        String engStatus =
                "BO_ 201 Engine_General_Status_1: 8 hsCAN\n" +
                " SG_ AccActPos : 32|8@1+ (0.392157,0.0) [0|255] \"%\" Vector__XXX\n" +
                " SG_ EngAirIntBstPr : 56|8@1- (1.0,0.0) [-128|127] \"kPaG\" Vector__XXX";

        DbcFile dbc = TestCases.readDbc(engStatus);
        DbcPacket packet = dbc.findPacket(201);
        assertNotNull(packet);

        DbcField AccActPos = packet.getByName("AccActPos");
        assertNotNull(AccActPos);
        assertFalse(AccActPos.isSigned());

        DbcField EngAirIntBstPr = packet.getByName("EngAirIntBstPr");
        assertNotNull(EngAirIntBstPr);
        assertTrue(EngAirIntBstPr.isSigned());
    }

    @Test(expected = IllegalStateException.class)
    public void testFieldOutOfBounds() throws IOException {
        String dbcText = "BO_ 100 P: 1 ECM_HS\n" +
                " SG_ OAT : 0|16@1+ (1,0) [0|8] \"deg C\"  VICS";
        TestCases.readDbc(dbcText);
    }

    @Test(expected = IllegalStateException.class)
    public void testFieldOutOfBoundsMotorola() throws IOException {
        String dbcText = "BO_ 100 P: 1 ECM_HS\n" +
                " SG_ OAT : 8|16@0+ (1,0) [0|8] \"deg C\"  VICS";
        TestCases.readDbc(dbcText);
    }

    @Test
    public void testParse() throws IOException {
        String dbcInput = "BO_ 1021 EGS_Daten_Anzeige_Getriebestrang_3FD_1021: 5 EGS\n" +
                " SG_ DISP_PO_GRB : 21|3@1+ (1,0) [0|6] \"\" GWS\n";
        TestCases.readDbc(dbcInput);
    }

    @Test
    public void testBitness() throws IOException {
        {
            String intel = "BO_ 100 P: 8 ECM_HS\n" +
                    " SG_ OAT : 7|8@1+ (1,0) [0|8] \"deg C\"  VICS";
            DbcFile dbc = TestCases.readDbc(intel);
            assertEquals(Bitness.Intel, dbc.getBitness());
        }
        {
            String moto = "BO_ 100 P: 8 ECM_HS\n" +
                    " SG_ OAT : 7|8@0+ (1,0) [0|8] \"deg C\"  VICS";
            DbcFile dbc = TestCases.readDbc(moto);
            assertEquals(Bitness.Motorolla, dbc.getBitness());
        }
        {
            String mixed = "BO_ 100 P: 8 ECM_HS\n" +
                    " SG_ OAT : 7|8@1+ (1,0) [0|8] \"deg C\"  VICS\n" +
                    " SG_ OAT2 : 15|8@0+ (1,0) [0|8] \"deg C\"  VICS";
            DbcFile dbc = TestCases.readDbc(mixed);
            assertNull(dbc.getBitness());
        }
    }
}
