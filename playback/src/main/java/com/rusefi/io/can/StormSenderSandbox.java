package com.rusefi.io.can;

public class StormSenderSandbox {
    public static void main(String[] args) throws InterruptedException {
        CanSender sender = SenderSandbox.create();
        int[] ids = {640, 896};

        while (true) {

            for (int id : ids) {
                sender.send(id, new byte[8]);
                Thread.sleep(1);
            }
        }
    }
}
