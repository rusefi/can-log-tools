package com.rusefi.can.mlv;

import com.rusefi.can.CANPacket;
import com.rusefi.can.Launcher;
import com.rusefi.can.reader.impl.AutoFormatReader;

import java.io.IOException;
import java.util.List;

public class CanToMegaLogViewerSandbox {
    public static void main(String[] args) throws IOException {
        String dbcFileName = "C:\\stuff\\can-log-tools\\reader\\src\\test\\resources\\test_motorola.dbc";
        String trcFileName = "C:\\stuff\\can-log-tools\\reader\\src\\test\\resources\\test_motorola.trc";

        Launcher.dbcFileName = dbcFileName;

        List<CANPacket> packets = AutoFormatReader.INSTANCE.readFile(trcFileName);

        CanToMegaLogViewer.createMegaLogViewer(".", packets, "test_motorola");
    }
}
