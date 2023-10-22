package com.rusefi.can.analysis;

public class J1850_SAE_crc8_Calculator {
    byte crc8(byte[] data, int length) {
        byte crc = 0;

        if (data == null)
            return 0;
        crc ^= 0xff;
        int ptr = 0;

        while (length-- > 0) {
            crc ^= data[ptr++];
            for (int k = 0; k < 8; k++)
                crc = (byte) (((crc & 0x80) != 0) ? (crc << 1) ^ 0x1d : crc << 1);
        }
        crc &= 0xff;
        crc ^= 0xff;
        return crc;
    }
}
