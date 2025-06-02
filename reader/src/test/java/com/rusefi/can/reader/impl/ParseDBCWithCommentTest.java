package com.rusefi.can.reader.impl;

import com.rusefi.can.reader.dbc.GapFactory;
import com.rusefi.mlv.LoggingStrategy;
import com.rusefi.can.reader.dbc.DbcField;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.can.reader.dbc.DbcPacket;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;

import static junit.framework.TestCase.*;

public class ParseDBCWithCommentTest {
    private static final String RPM_DBC =
            "\n" +
                    "BO_ 1408 Motor_Flexia: 8 XXX\n" +
                    " SG_ Ansaugsystem m0 : 63|1@1+ (1,0) [0|0] \"\" XXX\n" +
                    " SG_ Hubraum m0 : 56|7@1+ (0.1,0) [0|12.7] \"l\" XXX\n" +
                    " SG_ Steigung_der_Befuellungskennlin m1 : 56|8@1+ (0.001,0) [0|0.255] \"l/mm\" XXX\n" +
                    " SG_ Anzahl_Zylinder m0 : 52|4@1+ (1,0) [0|15] \"Vent./Zyl.\" XXX\n" +
                    " SG_ Bewertungsfaktor_Russindex_Turb m1 : 50|6@1+ (0.1,0) [0|6.3] \"\" XXX\n" +
                    " SG_ Anzahl_Ventile m0 : 49|3@1+ (1,0) [0|7] \"Vent./Zyl.\" XXX\n" +
                    " SG_ Bewertungsfaktor_Verschleissind m1 : 44|6@1+ (0.1,0) [0|6.3] \"\" XXX\n" +
                    " SG_ Hersteller_Code m1 : 40|4@1+ (1,0) [0|15] \"\" XXX\n" +
                    " SG_ Motorleistung m0 : 40|9@1+ (1,0) [0|512] \"KW\" XXX\n" +
                    " SG_ Max_Drehmoment m0 : 32|8@1+ (10,0) [0|2550] \"Nm\" XXX\n" +
                    " SG_ Normierter_Verbrauch m1 : 32|8@1+ (10,0) [0|2550] \"l/Zyl.\" XXX\n" +
                    " SG_ Oelniveauschwelle m1 : 24|8@1+ (0.25,0) [0|63.75] \"cm\" XXX\n" +
                    " SG_ Drehzahl_MaxNorm m0 : 24|8@1+ (100,0) [0|25500] \"U/min\" XXX\n" +
                    " SG_ Verschleissindex : 16|8@1+ (1,0) [0|254] \"\" XXX\n" +
                    " SG_ Russindex : 8|8@1+ (1,0) [0|254] \"\" XXX\n" +
                    " SG_ Verbrennungsart : 7|1@1+ (1,0) [0|0] \"\" XXX\n" +
                    " SG_ Frei_Motor_Flexia_1 : 6|1@1+ (1,0) [0|0] \"\" XXX\n" +
                    " SG_ Warm_Up_Cycle : 5|1@1+ (1,0) [0|0] \"\" XXX\n" +
                    " SG_ Driving_Cycle : 4|1@1+ (1,0) [0|0] \"\" XXX\n" +
                    " SG_ Zaehler_Motor_Flexia : 1|3@1+ (1,0) [0|15] \"\" XXX\n" +
                    " SG_ Multiplex_Schalter_Motor_Flexia M : 0|1@1+ (1,0) [0|0] \"\" XXX\n" +
                    "" +
                    "" +
                    "" +
                    "\n" +
                    "\n" +
                    "CM_ SG_ 1408 Zaehler_Motor_Flexia \"Counter Motor_Flexia\";\n" +
                    "CM_ SG_ 1408 Verbrennungsart \"Type of combustion\";\n" +
                    "CM_ SG_ 1408 Max_Drehmoment \"Maximum torque\";\n" +
                    "CM_ SG_ 1408 Drehzahl_MaxNorm \"RPM of maximum torque\";\n" +
                    "CM_ SG_ 1408 Hubraum \"Displacement\";\n" +
                    "CM_ SG_ 1408 Anzahl_Zylinder \"Number of cylinders\";\n" +
                    "CM_ SG_ 1408 Anzahl_Ventile \"Number of valves\";\n" +
                    "CM_ SG_ 1408 Ansaugsystem \"Induction System\";\n" +
                    "CM_ SG_ 1408 Motorleistung \"Maximum engine power\";\n" +
                    "";

    @Test
    public void parse() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(RPM_DBC));

        DbcFile dbc = new DbcFile(LoggingStrategy.LOG_ONLY_TRANSLATED_FIELDS);
        dbc.read(reader);

        assertEquals(dbc.size(), 1);
        DbcPacket packet = dbc.get(1408);
        DbcField field = packet.find("Number of cylinders");
        assertNotNull(field);
        assertTrue(field.isNiceName());
    }

    @Test
    public void parseMotoTwoBytes() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("BO_ 190 PTEI_BrakeApplyStatus_190_0BE: 6 ECM\n" +
                "   SG_ PSBPI_PTSnBrkPdlPs : 15|8@0+ (0.392157,0) [0|100] \"%\" Vector__XXX\n" +
                "   SG_ AccPos : 23|8@0+ (0.392157,0) [0|100] \"%\" Vector__XXX\n"));

        DbcFile dbc = new DbcFile(false);
        dbc.read(reader);
        assertEquals(dbc.size(), 1);
        DbcPacket p190 = dbc.get(190);
        DbcField signalBrkPdl = p190.getByName("PSBPI_PTSnBrkPdlPs");
        assertEquals(8, signalBrkPdl.getStartOffset());
        assertEquals(8, signalBrkPdl.getLength());

        assertEquals(8, p190.getFields().size());

    }


    @Test
    public void parseOneBit() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("BO_ 190 PTEI_BrakeApplyStatus_190_0BE: 6 ECM\n" +
                "   SG_ AccPos : 23|1@0+ (0.392157,0) [0|100] \"%\" Vector__XXX\n"));

        DbcFile dbc = new DbcFile(false);
        dbc.read(reader);
        assertEquals(dbc.size(), 1);

        DbcPacket p190 = dbc.get(190);
        assertEquals(9, p190.getFields().size());

        DbcField firstByte = p190.findByBitIndex(/*bit index*/3);
        assertEquals(0, firstByte.getStartOffset());
        assertEquals(8, firstByte.getLength());


        DbcField partial = p190.findByBitIndex(/*bit index*/17);
        assertEquals(16, partial.getStartOffset());
        assertEquals(7, partial.getLength());
    }

    @Test
    public void parseMoto() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("    BO_ 398 PTEI_EngineTorqueStatus_398_18E: 8 Vector__XXX\n" +
                "    SG_ CrksftNTrnsRegCmdTq : 51|12@0+ (0.5,-848) [-848|1199.5] \"Nm\"  TCM_HS\n"));

        DbcFile dbc = new DbcFile(false);
        dbc.read(reader);
        assertEquals(dbc.size(), 1);
    }


    @Test
    public void testGapFinder() {
        GapFactory g = new GapFactory(Collections.emptyList(), "1");
        assertEquals(64, g.findUnusedSectionEndIndex(63));
    }
}

