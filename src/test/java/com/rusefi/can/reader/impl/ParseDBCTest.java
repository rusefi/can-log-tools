package com.rusefi.can.reader.impl;

import com.rusefi.can.reader.dbc.DbcField;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.can.reader.dbc.DbcPacket;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static junit.framework.TestCase.assertEquals;

public class ParseDBCTest {
    public static final String VAG_MOTOR_1 = "BO_ 640 Motor_1: 8 XXX\n" +
            " SG_ Fahrerwunschmoment : 56|8@1+ (0.39,0) [0|99] \"MDI\" XXX\n" +
            " SG_ mechanisches_Motor_Verlustmomen : 48|8@1+ (0.39,0) [0|99] \"MDI\" XXX\n" +
            " SG_ PPS_TPS : 40|8@1+ (0.4,0) [0|101.6] \"%\" XXX\n" +
            " SG_ inneres_Motor_Moment_ohne_exter : 32|8@1+ (0.39,0) [0|99] \"MDI\" XXX\n" +
            " SG_ RPM : 16|16@1+ (0.25,0) [0|16256] \"U/min\" XXX\n" +
            " SG_ inneres_Motor_Moment : 8|8@1+ (0.39,0) [0|99] \"MDI\" XXX\n" +
            " SG_ Momentenangaben_ungenau : 7|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Fehlerstatus_Getriebe_Momentene : 6|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Fehlerstatus_Brems_Momenteneing : 5|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Time_Out_Bremsen_Botschaft : 4|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Kupplungsschalter : 3|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Kickdownschalter : 2|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Fahrpedalwert_ungenau__Motor_1_ M : 1|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Leergasinformation : 0|1@1+ (1,0) [0|0] \"\" XXX" +
            "";
    public static final String RPM_DBC = "VERSION \"\"\n" +
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
            "BO_ 1394 ZAS_1: 2 XXX\n" +
            " SG_ Fehlerspeichereintrag__ZAS_ : 15|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Frei_ZAS_1_3 : 8|7@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Frei_ZAS_1_2 : 7|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Klemme_15_SV : 6|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Frei_ZAS_1_1 : 5|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Klemme_P__Parklichtstellung_ : 4|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Klemme_50__Starten_ : 3|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Klemme_X__Startvorgang_ : 2|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ Klemme_15__Z_ndung_ein_ : 1|1@1+ (1,0) [0|0] \"\" XXX\n" +
            " SG_ S_Kontakt__Schl_ssel_steckt_ : 0|1@1+ (1,0) [0|0] \"\" XXX\n" +
            "\n" +
            "BO_ 1336 Wischer_1: 2 XXX\n" +
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
            "" +
            "" +
            "" +
            "\n" +
            VAG_MOTOR_1;

    @Test
    public void parse() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(RPM_DBC));

        DbcFile dbc = new DbcFile();
        dbc.read(reader);

        assertEquals(dbc.packets.size(), 3);

        DbcPacket motorPacket = dbc.packets.get(2);
        assertEquals(motorPacket.getId(), 640);

        DbcField rpm = motorPacket.find("RPM");
        assertEquals(0.25, rpm.getMult());
        assertEquals("Motor_1", rpm.getCategory());
    }
}
