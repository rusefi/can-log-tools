package com.rusefi.can.reader.dbc;

import com.rusefi.can.dbc.FileNameProvider;
import com.rusefi.mlv.LoggingStrategy;
import com.rusefi.sensor_logs.BinaryLogEntry;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class DbcFile implements FileNameProvider {
    private final LinkedHashMap<Integer, DbcPacket> packets = new LinkedHashMap<>();
    private String fileName;

    @Override
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public static final boolean debugEnabled = false;


    private List<BinaryLogEntry> list;

    boolean logOnlyTranslatedFields;

    public DbcFile(boolean logOnlyTranslatedFields) {
        this.logOnlyTranslatedFields = logOnlyTranslatedFields;
    }

    public int size() {
        return packets.size();
    }

    public static DbcFile readFromFile(String fileName) throws IOException {
        DbcFile dbc = new DbcFile(LoggingStrategy.LOG_ONLY_TRANSLATED_FIELDS);
        dbc.setFileName(fileName);
        if (fileName == null)
            return dbc;
        System.out.println(new Date() + " Reading DBC file from " + fileName + "..."); {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            dbc.read(reader);
        }
        return dbc;
    }

    public void read(BufferedReader reader) throws IOException {
        DbcFileReader.read(this, reader);
    }

    public void addPacket(DbcPacket packet) {
        packets.put(packet.getId(), packet);
    }


    public DbcPacket findPacket(int canId) {
        return packets.get(com.rusefi.can.dbc.J1939Logic.trimSid(canId));
    }

    public List<BinaryLogEntry> getFieldNameEntries(LoggingStrategy.LoggingFilter filter) {
        if (list == null) {
            list = LoggingStrategy.getFieldNameEntries(this, logOnlyTranslatedFields, filter);
        }
        return list;
    }

    public DbcPacket getPacketByIndexSlow(int index) {
        return new ArrayList<>(packets.values()).get(index);
    }

    public DbcPacket getPacket(int sid) {
        int trimmedSid = com.rusefi.can.dbc.J1939Logic.trimSid(sid);
        return packets.computeIfAbsent(trimmedSid, new Function<Integer, DbcPacket>() {
            @Override
            public DbcPacket apply(Integer integer) {
                String packetName = Integer.toHexString(sid) + "_" + sid;
                String packetPrefix = "_unknown_" + sid;
                DbcPacket packet = new DbcPacket(sid, packetName, "unknown", new com.rusefi.can.dbc.util.GapFactory(Collections.emptyList(), packetPrefix).withGaps(sid), DbcFile.this);
                return packet;
            }
        });
    }

    public Collection<DbcPacket> values() {
        return packets.values();
    }

}
