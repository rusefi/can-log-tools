package com.rusefi.can.reader.dbc;

import com.rusefi.can.dbc.FileNameProvider;

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

    public int size() {
        return packets.size();
    }

    public void addPacket(DbcPacket packet) {
        packets.put(packet.getId(), packet);
    }

    public DbcPacket findPacket(int canId) {
        return packets.get(com.rusefi.can.dbc.J1939Logic.trimSid(canId));
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
