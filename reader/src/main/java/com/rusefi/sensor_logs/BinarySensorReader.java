package com.rusefi.sensor_logs;

import java.io.*;
import java.util.Date;

public class BinarySensorReader {
    static void read(String fileName) throws IOException {


        DataInputStream bis = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)));
        int header = bis.readInt();
        System.out.println("header " + Integer.toHexString(header));
        if (header != 0x4d4c564c)
            throw new IllegalStateException("header " + header);
        int version = bis.readInt();
        System.out.println("version " + Integer.toHexString(version));
        if (version != 0x47000002)
            throw new IllegalStateException("version " + version);
        int timeStamp = bis.readInt();
        System.out.println("timeStamp " + timeStamp + " " + new Date(timeStamp * 1000L));
        int infoDataState = bis.readInt();
        System.out.println("infoDataState " + Integer.toHexString(infoDataState) + "/" + infoDataState);
        int dataBeginIndex = bis.readInt();
        System.out.println("dataBeginIndex " + Integer.toHexString(dataBeginIndex) + "/" + dataBeginIndex);

        int recordLength = bis.readShort();
        int numberOfFields = bis.readShort();


        System.out.println("fields area size " + (89 * numberOfFields));

        for (int i = 0; i < numberOfFields; i++) {
            int typeCode = bis.readByte();
            String nameAsArray = readArray(bis, 34);
            String unitAsArray = readArray(bis, 10);
            int style = bis.readByte();
            float scale = bis.readFloat();
            float transform = bis.readFloat();
            int digits = bis.readByte();
            String categoryAsArray = readArray(bis, 34);
            System.out.println("nameAsArray " + nameAsArray + ", " + unitAsArray);
        }


    }

    private static String readArray(DataInputStream bis, int size) throws IOException {
        StringBuilder sb = new StringBuilder();

        boolean terminated = false;
        for (int i = 0; i < size; i++) {
            char c = (char) bis.readByte();
            if (c == 0)
                terminated = true;
            if (!terminated)
                sb.append(c);
        }


        return sb.toString();
    }

    public static int swap32(int value) {
        return ((value & 0xFF) << 24) |
                ((value >> 8) & 0xFF) << 16 |
                ((value >> 16) & 0xFF) << 8 |
                ((value >> 24) & 0xFF);
    }
}
