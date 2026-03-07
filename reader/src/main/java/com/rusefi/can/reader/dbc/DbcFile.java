package com.rusefi.can.reader.dbc;

import com.rusefi.can.Launcher;
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
        return packets.get(trimSid(canId));
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
        int trimmedSid = trimSid(sid);
        return packets.computeIfAbsent(trimmedSid, new Function<Integer, DbcPacket>() {
            @Override
            public DbcPacket apply(Integer integer) {
                String packetName = Integer.toHexString(sid) + "_" + sid;
                String packetPrefix = "_unknown_" + sid;
                DbcPacket packet = new DbcPacket(sid, packetName, "unknown", new GapFactory(Collections.emptyList(), packetPrefix).withGaps(sid), DbcFile.this);
                return packet;
            }
        });
    }

    public Collection<DbcPacket> values() {
        return packets.values();
    }

    // GMLAN specific: leave the only ArbID, trim priority and sender fields
    // J1939 specific: trim source and destination (if any)
    static public int trimSid(int sid) {
        boolean longId = (sid > 0x7FF);
        if (Launcher.gmlanIgnoreSender && longId)
            return (sid & 0x03FF_FE00);
        else if (Launcher.j1939_mode && longId) {
            int pduFormat = (sid >> 16) & 0xFF;
            if (pduFormat < 0xF0) // PDU1 - peer to peer
                return (sid & 0x03FF_0000);
            else // PDU2 - broadcast
                return (sid & 0x03FF_FF00);
        }
        else
            return sid;
    }
}
