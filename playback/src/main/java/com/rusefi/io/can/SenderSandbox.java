package com.rusefi.io.can;

import com.rusefi.can.CANPacket;
import com.rusefi.can.CanPacketSender;
import com.rusefi.can.reader.CANLineReader;
import org.jetbrains.annotations.NotNull;
import peak.can.basic.HackLoadLibraryFlag;
import peak.can.basic.TPCANMsg;
import peak.can.basic.TPCANStatus;
import peak.can.basic.TPCANTimestamp;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class SenderSandbox {
    public static void main(String[] args) throws Exception {
//        String fileName = args.length > 0 ? args[0] : getFullResourceFileName("resources/atlas.trc");
        if (args.length == 0) {
            throw new IllegalArgumentException("No file name provided");
        }
        String fileName = args[0];

        List<CANPacket> packets = CANLineReader.getReader().readFile(fileName);
        System.out.println("Got " + packets.size() + " packet(s)");

        CanSender sender = create();

        AtomicInteger totalReceived = new AtomicInteger();

        new Thread(() -> {
            while (true) {
                if (PCanHelper.pcan != null) {
                    TPCANMsg msg = new TPCANMsg();
                    TPCANTimestamp timestamp = new TPCANTimestamp();
                    while (PCanHelper.pcan.Read(PCanHelper.CHANNEL, msg, timestamp) == TPCANStatus.PCAN_ERROR_OK) {
                        totalReceived.incrementAndGet();
                    }
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }, "Receiver").start();

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
                System.out.println(new Date() + ": Total received " + totalReceived.get());
            }
        }, "Reporting").start();

        while (true) {
            CanPacketSender.sendMessagesOut(packets, sender);
        }
    }

    @NotNull
    public static CanSender create() {
        if (isWindows()) {
            HackLoadLibraryFlag.LOAD_LIBRARY = false;
            try {
                NativeLibraryLoader.loadLibrary("PCANBasic");
                NativeLibraryLoader.loadLibrary("PCANBasic_JNI");
            } catch (IOException e) {
                throw new RuntimeException("Error loading PCAN libraries", e);
            }
        }

        return isWindows() ? PCanHelper.createSender() : SocketCANHelper.create();
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
