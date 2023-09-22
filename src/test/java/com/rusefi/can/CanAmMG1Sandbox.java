package com.rusefi.can;

import com.rusefi.can.analysis.ByteRateOfChangeReports;
import com.rusefi.can.reader.CANLineReader;
import com.rusefi.can.reader.ReaderType;
import com.rusefi.can.reader.ReaderTypeHolder;
import com.rusefi.can.reader.impl.PcanTrcReader2_0;

import java.io.IOException;
import java.util.List;

public class CanAmMG1Sandbox {
    public static void main(String[] args) throws IOException {
        ReaderTypeHolder.INSTANCE.type = ReaderType.PCAN1_1;
        CANLineReader reader = PcanTrcReader2_0.INSTANCE;


        String inputFolderName = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\CanAm\\maverick-x3-xrs-turbo-rr-max-2021\\";
//        String revvingCltWentUp = inputFolderName + "engine-revving-CLT-increased.trc";
        //      List<CANPacket> idling = reader.readFile(revvingCltWentUp);
        //printStats(idling, "IDLING");

        ByteRateOfChangeReports.scanInputFolder(inputFolderName, ".trc");
    }
}
