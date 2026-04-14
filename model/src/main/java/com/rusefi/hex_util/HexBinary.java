package com.rusefi.hex_util;

import java.util.List;

public class HexBinary {
    public static String printHexBinary(byte[] data) {
        if (data == null)
            return "(null)";
        char[] hexCode = HexUtil.HEX_CHAR_ARRAY;

        StringBuilder r = new StringBuilder(data.length * 3);
        for (int i = 0; i < data.length; i++) {
            byte b = data[i];
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
            if (i < data.length - 1) {
                r.append(' ');
            }
        }
        return r.toString();
    }

    public static String printHexBinary(List<Byte> data) {
        if (data == null)
            return "(null)";
        char[] hexCode = HexUtil.HEX_CHAR_ARRAY;

        StringBuilder r = new StringBuilder(data.size() * 3);
        for (int i = 0; i < data.size(); i++) {
            byte b = data.get(i);
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
            if (i < data.size() - 1) {
                r.append(' ');
            }
        }
        return r.toString();
    }

    public static String printByteArray(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            if (Character.isJavaIdentifierPart(b)) {
                sb.append((char) b);
            } else {
                sb.append(' ');
            }
        }
        return printHexBinary(data) + sb;
    }
}
