package com.rusefi.can.reader.impl;

import com.rusefi.can.dbc.DbcFile;
import com.rusefi.can.dbc.reader.DbcFileReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class TestCases {
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

    public static DbcFile readDbc(String text) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(text));
        DbcFile dbc = new DbcFile();
        DbcFileReader.read(dbc, reader);
        return dbc;
    }
}
