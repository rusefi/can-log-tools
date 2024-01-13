package com.rusefi.io.can;

import com.rusefi.can.CANPacket;
import com.rusefi.can.CanPacketSender;
import com.rusefi.can.reader.CANLineReader;
import org.jetbrains.annotations.NotNull;
import peak.can.basic.HackLoadLibraryFlag;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

public class SenderSandbox {
    public static void main(String[] args) throws Exception {
        List<CANPacket> packets = readResource("atlas.trc");
        System.out.println("Got " + packets.size() + " packet(s)");

        if (isWindows()) {
            HackLoadLibraryFlag.LOAD_LIBRARY = false;
            //System.load(getFullResourceFileName("PCANBasic_JNI.dll"));
            System.load(new File("ext/peak-can-basic/src/main/resources/PCANBasic_JNI.dll").getAbsolutePath());
        }

        CanSender sender = isWindows() ? PCanHelper.create() : SocketCANHelper.create();

        while (true) {
            CanPacketSender.sendMessagesOut(packets, sender);
        }
    }

    static List<CANPacket> readResource(String resourceName) throws URISyntaxException, IOException {
        String fullResourceFileName = getFullResourceFileName(resourceName);

        return CANLineReader.getReader().readFile(fullResourceFileName);
    }

    @NotNull
    private static String getFullResourceFileName(String resourceName) throws URISyntaxException {
        URL resource = SenderSandbox.class.getResource("/" + resourceName);
        System.out.println(resource);
        String fullResourceFileName = Paths.get(resource.toURI()).toString();
        return fullResourceFileName;
    }

    private static String getOsName() {
        return System.getProperty("os.name");
    }

    private static boolean isWindows() {
        return getOsName().contains("Windows");
    }
}
