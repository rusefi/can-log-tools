package com.rusefi.can.dbc;

import com.rusefi.can.DualSid;
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

    public void addPacket(com.rusefi.can.dbc.DbcPacket packet) {
        packets.put(packet.getId(), packet);
    }

    public static String getPacketName(DbcFile dbc, int sid) {
        DbcPacket dbcPacket = dbc.findPacket(sid);
        return dbcPacket == null ? DualSid.dualSid(sid) : dbcPacket.getName();
    }

    public com.rusefi.can.dbc.DbcPacket findPacket(int canId) {
        return packets.get(com.rusefi.can.dbc.J1939Logic.trimSid(canId));
    }


    public com.rusefi.can.dbc.DbcPacket getPacketByIndexSlow(int index) {
        return new ArrayList<>(packets.values()).get(index);
    }

    public com.rusefi.can.dbc.DbcPacket getPacket(int sid) {
        int trimmedSid = com.rusefi.can.dbc.J1939Logic.trimSid(sid);
        return packets.computeIfAbsent(trimmedSid, new Function<Integer, com.rusefi.can.dbc.DbcPacket>() {
            @Override
            public com.rusefi.can.dbc.DbcPacket apply(Integer integer) {
                String packetName = DualSid.dualSid(sid);
                String packetPrefix = "_unknown_" + sid;
                return new DbcPacket(sid, packetName, "unknown", 8, new com.rusefi.can.dbc.util.GapFactory(Collections.<DbcField>emptyList(), packetPrefix, 8).withGaps(sid), DbcFile.this);
            }
        });
    }

    public Collection<com.rusefi.can.dbc.DbcPacket> values() {
        return packets.values();
    }

    /**
     * @return null if both Intel and Motorola bitness are detected, otherwise the detected bitness
     */
    public Bitness getBitness() {
        boolean hasIntel = false;
        boolean hasMotorola = false;

        for (DbcPacket packet : packets.values()) {
            for (DbcField field : packet.getFields()) {
                if (field.isGap()) {
                    continue;
                }
                if (field.isBigEndian()) {
                    hasMotorola = true;
                } else {
                    hasIntel = true;
                }
            }
        }

        if (hasIntel && hasMotorola) {
            return null;
        }
        if (hasIntel) {
            return Bitness.Intel;
        }
        if (hasMotorola) {
            return Bitness.Motorolla;
        }
        return null;
    }

}
