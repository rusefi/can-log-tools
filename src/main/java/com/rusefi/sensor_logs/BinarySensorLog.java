package com.rusefi.sensor_logs;

import java.io.*;
import java.util.*;
import java.util.function.Function;

import static com.rusefi.sensor_logs.Fields.MLQ_FIELD_HEADER_NAME_OR_CATEGORY;

/**
 * MLV .mlq binary log file
 * https://www.efianalytics.com/TunerStudio/docs/MLG_Binary_LogFormat_1.0.pdf
 * https://www.efianalytics.com/TunerStudio/docs/MLG_Binary_LogFormat_2.0.pdf
 *
 * </p>
 * Andrey Belomutskiy, (c) 2013-2020
 */
public class BinarySensorLog<T extends BinaryLogEntry> implements SensorLog, AutoCloseable {
    private final Function<T, Double> valueProvider;
    private final Collection<T> entries;
    private final TimeProvider timeProvider;
    private DataOutputStream stream;

    private final String fileName;

    private int counter;

    public BinarySensorLog(Function<T, Double> valueProvider, Collection<T> sensors, TimeProvider timeProvider, String fileName) {
        this.valueProvider = Objects.requireNonNull(valueProvider, "valueProvider");
        this.entries = Objects.requireNonNull(sensors, "entries");
        this.timeProvider = timeProvider;
        this.fileName = fileName;
    }

    public interface TimeProvider {
        long currentTimestamp();
    }

    @Override
    public double getSecondsSinceFileStart() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeSensorLogLine() {
        if (stream == null) {
            System.out.println("Writing to " + fileName);

            try {
                stream = new DataOutputStream(new FileOutputStream(fileName));
                writeHeader();
            } catch (Throwable e) {
                e.printStackTrace();
                stream = null;
            }
        }

        if (stream != null) {
            try {
                stream.write(0);
                stream.write(counter++);
                stream.writeShort((int) (timeProvider.currentTimestamp() * 100));

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);

                for (T sensor : entries) {
                    Double value = valueProvider.apply(sensor);
                    if (value == null)
                        throw new NullPointerException("No value for " + sensor);
                    sensor.writeToLog(dos, value);
                }

                byte[] byteArray = baos.toByteArray();
                byte checkSum = 0;
                for (byte b : byteArray) {
                    checkSum += b;
                }
                stream.write(byteArray);
                stream.write(checkSum);

                if (counter % 20 == 0) {
                    // for not flush on each block of data but still flush
                    stream.flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeHeader() throws IOException {
        String headerText = "\"rusEFI " + "sdsr" + "\"\n" +
                "\"Capture Date: " + new Date() + "\"\n";

        for (char c : "MLVLG\0".toCharArray()) {
            stream.write(c);
        }

        int fieldsDataSize = 0;
        for (BinaryLogEntry entry : entries) {
            fieldsDataSize += entry.getByteSize();
        }

        // 0006h Format version = 02
        stream.writeShort(2);
        // 0008h Timestamp
        stream.writeInt((int) (System.currentTimeMillis() / 1000));
        // 000ch
        int infoDataStart = Fields.MLQ_HEADER_SIZE + Fields.MLQ_FIELD_HEADER_SIZE * entries.size();
        System.out.println("Total " + entries.size() + " fields");
        stream.writeInt(infoDataStart);
        stream.writeInt(infoDataStart + headerText.length());
        // 0012h
        stream.writeShort(fieldsDataSize);
        // 0014h number of fields
        stream.writeShort(entries.size());

        for (BinaryLogEntry sensor : entries) {
            String name = sensor.getName();
            String unit = sensor.getUnit();

            // 0000h type enum
            stream.write(7);
            // 0001h
            writeLine(stream, name, MLQ_FIELD_HEADER_NAME_OR_CATEGORY);
            // 0023h
            writeLine(stream, unit, 10);
            stream.write(0); // Display Style, 0=Float
            // 002Eh 46 scale
            stream.writeFloat(1);
            // 0032h 50 transform
            stream.writeFloat(0);
            // 0036h precision digits
            stream.write(2);
            writeLine(stream, sensor.getCategory(), MLQ_FIELD_HEADER_NAME_OR_CATEGORY);
        }
        if (stream.size() != infoDataStart)
            throw new IllegalStateException("We are doing something wrong :( stream.size=" + stream.size() + "/" + infoDataStart);
        writeLine(stream, headerText, headerText.length());
    }

    @Override
    public void close() {
        close(stream);
        stream = null;
    }

    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            // ignoring
        }
    }

    public String getFileName() {
        return fileName;
    }

    private void writeLine(DataOutputStream stream, String name, int length) throws IOException {
        for (int i = 0; i < Math.min(name.length(), length); i++) {
            stream.write(name.charAt(i));
        }
        for (int i = name.length(); i < length; i++)
            stream.write(0);
    }
}
