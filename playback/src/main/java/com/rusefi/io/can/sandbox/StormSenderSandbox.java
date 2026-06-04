package com.rusefi.io.can.sandbox;

import com.rusefi.io.can.CanSender;
import com.rusefi.io.can.CanTracePlayerTool;

public class StormSenderSandbox {
    public static void main(String[] args) throws InterruptedException {
        CanSender sender = CanTracePlayerTool.create();
        int[] ids = {640, 896};

        while (true) {

            for (int id : ids) {
                sender.send(id, new byte[8]);
                Thread.sleep(1);
            }
        }
    }
}
