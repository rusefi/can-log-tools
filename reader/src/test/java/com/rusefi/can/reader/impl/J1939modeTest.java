package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.Launcher;
import com.rusefi.can.reader.CANLineReader;
import com.rusefi.can.reader.dbc.DbcField;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.can.reader.dbc.DbcPacket;
import com.rusefi.mlv.LoggingStrategy;
import com.rusefi.sensor_logs.BinaryLogEntry;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;


public class J1939modeTest {

    // xxF004xx
    private static final String dbcEEC = """
    BO_ 2364540158 EEC1: 8 Vector__XXX
        SG_ EngDemandPercentTorque : 56|8@1+ (1,-125) [-125|125] "%" Vector__XXX
        SG_ EngStarterMode : 48|4@1+ (1,0) [0|15] "" Vector__XXX
        SG_ SrcAddrssOfCntrllngDvcForEngCtrl : 40|8@1+ (1,0) [0|253] "" Vector__XXX
        SG_ EngSpeed : 24|16@1+ (0.125,0) [0|8031.875] "rpm" Vector__XXX
        SG_ ActualEngPercentTorque : 16|8@1+ (1,-125) [0|125] "%" Vector__XXX
        SG_ DriversDemandEngPercentTorque : 8|8@1+ (1,-125) [0|125] "%" Vector__XXX
        SG_ ActlEngPrcntTorqueHighResolution : 4|4@1+ (0.125,0) [0|0.875] "%" Vector__XXX
        SG_ EngTorqueMode : 0|4@1+ (1,0) [0|15] "" Vector__XXX""";

    // xxF013xx
    private static final String dbcSSI = """
    BO_ 2364543998 SSI: 8 Vector__XXX
        SG_ RollAndPitchMeasurementLatency : 56|8@1+ (0.5,0) [0|125] "ms" Vector__XXX
        SG_ PitchAndRollCompensated : 54|2@1+ (1,0) [0|3] "" Vector__XXX
        SG_ PitchRateFigureOfMerit : 52|2@1+ (1,0) [0|3] "" Vector__XXX
        SG_ RollAngleFigureOfMerit : 50|2@1+ (1,0) [0|3] "" Vector__XXX
        SG_ PitchAngleFigureOfMerit : 48|2@1+ (1,0) [0|3] "" Vector__XXX
        SG_ PitchRate : 32|16@1+ (0.002,-64) [-64|64.51] "deg/s" Vector__XXX
        SG_ RollAngle : 16|16@1+ (0.002,-64) [-64|64.51] "deg" Vector__XXX
        SG_ PitchAngle : 0|16@1+ (0.002,-64) [-64|64.51] "deg" Vector__XXX""";

//    @Test
    public void readEEC() throws IOException {

        Launcher.j1939_mode = true;
        DbcFile dbc = ParseDBCTest.readDbc(dbcEEC);

        String logLine = "     5)        10.0  Rx     0CF00400  8  FF FF FF E8 25 FF FF 7D \n";
        CANPacket canPacket = new PcanTrcReader1_1().readLine(logLine);

        DbcPacket dbcPkt = dbc.findPacket(canPacket.getId());
        assertNotNull(dbcPkt);

        // bytes 3,4, little endian
        DbcField engSpeed = dbcPkt.find("EngSpeed");
        assertNotNull(engSpeed);

        assertEquals(0x25E8 * 0.125, engSpeed.getValue(canPacket), 0.1);

        Launcher.j1939_mode = false;
    }

    @Test
    public void megaLogFilter() throws IOException {
        Launcher.j1939_mode = true;

        DbcFile dbc = ParseDBCTest.readDbc(dbcEEC + "\n\n" + dbcSSI);

        CANLineReader logReader = new PcanTrcReader1_1();
        List<CANPacket> packets = List.of(
                logReader.readLine("   5)        10.0  Rx     0CF00400  8  FF FF FF E8 25 FF FF 7D"),
                logReader.readLine("  18)        30.0  Rx     0CF00400  8  FF FF FF 8C 26 FF FF 7D"),
                logReader.readLine("  32)        50.0  Rx     0CF00400  8  FF FF FF 40 26 FF FF 7D")
            );

        // dumb copy-paste from LoggingStrategy.writeLogByDbc()

        Set<Integer> allIds = CANPacket.getAllIds(packets);
        // we only log DBC frames if at least one packet is present in the trace
        LoggingStrategy.LoggingFilter filter = packet -> packet.isInLog(allIds);
        List<BinaryLogEntry> entries = dbc.getFieldNameEntries(filter);

        // in the entries we have the only EEC fields, but not SSI
        assertTrue(entries.size() >= 8);    // EEC have 8 fields + 1 gap
        assertTrue(entries.size() < 10);

        Launcher.j1939_mode = false;
    }
}
