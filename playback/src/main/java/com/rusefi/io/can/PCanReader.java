package com.rusefi.io.can;

import com.rusefi.can.CANPacket;
import peak.can.basic.PCANBasic;
import peak.can.basic.TPCANMsg;
import peak.can.basic.TPCANStatus;
import peak.can.basic.TPCANTimestamp;

import java.util.LinkedList;

import static com.rusefi.io.can.PCanHelper.CHANNEL;

public class PCanReader {
    private final Object lock = new Object();
    private final LinkedList<CANPacket> packets = new LinkedList<>();

    public CANPacket read() {
        synchronized (lock) {
            while (packets.isEmpty()) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    return null;
                }
            }
            return packets.removeFirst();
        }
    }

    public void createReaderThread(PCANBasic can) {
        Thread thread = new Thread(() -> {
            while (true) {
                TPCANMsg msg = new TPCANMsg();
                TPCANTimestamp ts = new TPCANTimestamp();
                TPCANStatus status = can.Read(CHANNEL, msg, ts);
                if (status == TPCANStatus.PCAN_ERROR_OK) {
                    double timeMs = ts.getMillis() + ts.getMicros() / 1000.0;
                    CANPacket packet = new CANPacket(timeMs, msg.getID(), msg.getData());
                    synchronized (lock) {
                        packets.add(packet);
                        lock.notifyAll();
                    }
                } else if (status == TPCANStatus.PCAN_ERROR_QRCVEMPTY) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        return;
                    }
                } else {
                    System.out.println("Error reading: " + status);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
