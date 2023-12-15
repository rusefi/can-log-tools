package com.rusefi.can.reader.impl;

import com.rusefi.can.CANPacket;
import com.rusefi.can.reader.CANLineReader;

/**
 * example:
 * @ TEXT @ 3 @ 64 @ 0 @ 1301 @ 3250 @ 00:00:03.250 @
 * 17,032236	1	0004	280	8	03 08 00 00 00 00 9F 40	00000000	       @
 * 17,032482	1	0004	2DE	8	00 00 03 00 00 00 03 20	00000000
 * 17,032709	1	0004	2D1	7	00 00 00 02 31 F6 02   	00000000	    1
 * 17,042241	1	0004	2DE	8	00 00 00 00 00 00 03 20	00000000
 * 17,042465	1	0004	2D1	7	00 00 00 03 31 F6 02   	00000000	    1
 *
 * File extension TRC
 */
public enum CanHackerReader implements CANLineReader {
    INSTANCE;

    @Override
    public CANPacket readLine(String line, String fileName, int lineIndex) {
        line = line.trim();
        if (line.startsWith("@"))
            return null;

        String[] tokens = line.split("\\s+");
        double timeStamp = Double.parseDouble(CANLineReader.attemptToFixLocalization(tokens[0]));

        int sid = Integer.parseInt(tokens[3], 16);
        int size = Integer.parseInt(tokens[4]);

        byte[] data = CANLineReader.readHexArray(tokens, 5, size);

        return new CANPacket(timeStamp, sid, data);
    }
}
