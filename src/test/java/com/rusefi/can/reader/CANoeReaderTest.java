package com.rusefi.can.reader;


import com.rusefi.can.CANPacket;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class CANoeReaderTest {

    @Test
    public void readLine() {
        String line = "12.961970 2  1AC             Rx   d 4 00 10 00 00  Length = 166000 BitCount = 87 ID = 428";
        CANoeReader reader = new CANoeReader();

        CANPacket packet = reader.readLine(line);

        assertEquals(0x1ac, packet.getId());
        assertEquals(4, packet.getData().length);
    }
}
