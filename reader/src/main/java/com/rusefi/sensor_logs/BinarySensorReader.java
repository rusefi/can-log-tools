package com.rusefi.sensor_logs;

import java.io.*;
import java.util.*;

/**
 * https://www.efianalytics.com/TunerStudio/docs/MLG_Binary_LogFormat_2.0.pdf
 */
public class BinarySensorReader {
    private static final int FIXED_HEADER_SIZE = 24;

    private static final List<Record> records = new ArrayList<>();


    static class LogLine {

        Map<Record, Float> snapshot = new HashMap<>();

        public LogLine(Map<Record, Float> snapshot) {
            this.snapshot = snapshot;
        }
    }

    private static final List<LogLine> logContent = new ArrayList<>();


    private static int recordCounter = 0;

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
        System.out.println("numberOfFields=" + numberOfFields);

        int fieldsHeaderAreaSize = 89 * numberOfFields;
        System.out.println("fields area size " + fieldsHeaderAreaSize + ", recordLength=" + recordLength);

        int infoBlockExpectedSize = dataBeginIndex - FIXED_HEADER_SIZE - fieldsHeaderAreaSize;
        boolean isInfoBlockExpected = infoBlockExpectedSize > 0;
        if (isInfoBlockExpected) {
            System.out.println("Expecting infoBlock " + infoBlockExpectedSize);
        }


        int lineTotalSize = 0;

        for (int i = 0; i < numberOfFields; i++) {
            int typeCode = bis.readByte();
            String fieldName = readFixedSizeString(bis, 34);
            String units = readFixedSizeString(bis, 10);
            int style = bis.readByte();
            float scale = bis.readFloat();
            float transform = bis.readFloat();
            int digits = bis.readByte();
            String categoryAsArray = readFixedSizeString(bis, 34);
//            System.out.println("fieldName " + fieldName + ", units=[" + units + "]");

            MlqDataType type = MlqDataType.findByOrdinal(typeCode);

            lineTotalSize += type.getRecordSize();

            records.add(new Record(fieldName, type, scale));

        }

        if (isInfoBlockExpected) {
            String infoBlock = readFixedSizeString(bis, infoBlockExpectedSize);
            System.out.println("Skipping infoBlock length=" + infoBlock.length());
            int sizeValidation = dataBeginIndex - infoBlock.length() - fieldsHeaderAreaSize - FIXED_HEADER_SIZE - 1;
            if (sizeValidation != 0)
                throw new IllegalStateException("Size validation failed by " + sizeValidation);
        }


        while (bis.available() > 0) {
            readBlocks(bis);
        }
    }

    private static void readBlocks(DataInputStream bis) throws IOException {

        byte blockType = bis.readByte();
        bis.readByte(); // counter
        if (blockType == 0) {
            readLoggerFieldData(bis);
        } else if (blockType == 1) {
            throw new UnsupportedOperationException("todo support markers");
        } else {
            throw new IllegalStateException("Unexpected " + blockType);
        }
    }

    private static void readLoggerFieldData(DataInputStream bis) throws IOException {
        bis.readShort(); // timestamp
//        System.out.println("Reading " + lineTotalSize + " for " + recordCounter);
//        for (int i = 0; i < lineTotalSize; i++) {
//            bis.readByte();
//        }

        Map<Record, Float> snapshot = new HashMap<>();

        for (Record record : records) {
            float value = record.read(bis);
//            System.out.println(record.getFieldName() + "=" + value);
            snapshot.put(record, value);

        }

        bis.readByte(); // crc
        recordCounter++;

        logContent.add(new LogLine(snapshot));


    }

    private static String readFixedSizeString(DataInputStream bis, int size) throws IOException {
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
