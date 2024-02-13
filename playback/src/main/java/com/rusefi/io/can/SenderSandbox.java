package com.rusefi.io.can;

import com.rusefi.can.CANPacket;
import com.rusefi.can.CanPacketSender;
import com.rusefi.can.reader.CANLineReader;
import org.jetbrains.annotations.NotNull;
import peak.can.basic.HackLoadLibraryFlag;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class SenderSandbox {
    public static void main(String[] args) throws Exception {
//        String fileName = args.length > 0 ? args[0] : getFullResourceFileName("resources/atlas.trc");
        String fileName = args.length > 0 ? args[0] : getFullResourceFileName("passat-idling.trc");

        List<CANPacket> packets = CANLineReader.getReader().readFile(fileName);
        System.out.println("Got " + packets.size() + " packet(s)");

        CanSender sender = create();

        while (true) {
            CanPacketSender.sendMessagesOut(packets, sender);
        }
    }

    @NotNull
    public static CanSender create() {
        if (isWindows()) {
            HackLoadLibraryFlag.LOAD_LIBRARY = false;
            //System.load(getFullResourceFileName("PCANBasic_JNI.dll"));
            System.load(new File("ext/peak-can-basic/src/main/resources/PCANBasic_JNI.dll").getAbsolutePath());
        }

        return isWindows() ? PCanHelper.create() : SocketCANHelper.create();
    }

    @NotNull
    private static String getFullResourceFileName(String resourceName) throws URISyntaxException {
        URL resource = SenderSandbox.class.getResource("/" + resourceName);
        System.out.println(Objects.requireNonNull(resource, "URL for " + resourceName));
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
