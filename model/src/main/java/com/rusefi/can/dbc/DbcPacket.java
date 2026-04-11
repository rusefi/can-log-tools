package com.rusefi.can.dbc;

import com.rusefi.can.CANPacket;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;

/**
 * Packet describes all the fields for specific can ID
 * also known as frame
 */
public class DbcPacket implements IDbcPacket {
    private final int id;
    private final String name;
    private final String source;
    private final int length;
    private final List<DbcField> fields = new ArrayList<>();
    private final FileNameProvider parent;

    public DbcPacket(int id, String name, String source, int length, List<DbcField> signals, FileNameProvider parent) {
        this.id = id;
        this.name = name;
        this.source = source;
        this.length = length;
        this.parent = parent;
        fields.addAll(signals);
        for (DbcField field : signals) {
            field.setParentPacket(this);

            BitSet usedBits = field.getUsedBits();
            int lastSetBit = usedBits.previousSetBit(Integer.MAX_VALUE);
            int bitLimit = 8 * length;
            if (lastSetBit > bitLimit) {
                // TODO getLsbBitIndex() may be weird in multi-byte fields
                throw new IllegalStateException("Field " + field.getName() + " is out of bounds in " + name + ": " + field.getLsbBitIndex() + " + " + field.getLength() + " > " + bitLimit + "; " + usedBits.length());
            }
        }
    }

    public FileNameProvider getParent() {
        return parent;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSource() {
        return source;
    }

    public int getLength() {
        return length;
    }

    public List<DbcField> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return "DbcPacket{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", source='" + source + '\'' +
                '}';
    }

    public DbcField find(String name) {
        for (DbcField field : fields) {
            if (field.getName().equalsIgnoreCase(name))
                return field;
        }
        return null;
    }

    public void addComment(String originalName, String niceName) {
        if (niceName.charAt(niceName.length() - 1) == ';')
            niceName = niceName.substring(0, niceName.length() - 1);
        niceName = unquote(niceName);
        DbcField field = find(originalName);
        if (field == null) {
            System.err.println("Field not found by [" + originalName + "]");
            return;
        }

        field.rename(originalName + ": " + niceName);
    }

    private static String unquote(String q) {
        final StringBuilder buf = new StringBuilder();
        final int len = q.length();
        if (len < 2 || q.charAt(0) != '"' || q.charAt(len - 1) != '"')
            return q;
        for (int i = 1; i < len - 1; i++) {
            char c = q.charAt(i);
            if (c == '\\') {
                if (i == len - 2)
                    throw new IllegalArgumentException("Trailing backslash");
                c = q.charAt(++i);
                switch (c) {
                    case 'n':
                        c = '\n';
                        break;
                    case '\\':
                    case '\"':
                    //case '*':
                    //case '?':
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Bad character '" + c + "' after backslash");
                }
            } else {
                switch (c) {
                    //case '*':
                    //case '?':
                    case '\"':
                    case '\n':
                        throw new IllegalArgumentException(
                                "Invalid unescaped character '" + c +
                                        "' in the string to unquote");
                }
            }
            buf.append(c);
        }
        return buf.toString();
    }

    public DbcField getFieldAtByte(int byteIndex) {
        for (DbcField field : fields) {
            if (field.coversByte(byteIndex))
                return field;
        }
        return null;
    }

    public DbcField getByName(String name) {
        for (DbcField field : fields) {
            if (field.getName().equalsIgnoreCase(name))
                return field;
        }
        return null;
    }

    public DbcField findByBitIndex(int bitIndex) {
        for (DbcField field : fields) {
            BitSet usedBits = field.getUsedBits();
            if (usedBits.get(bitIndex))
                return field;
        }
        return null;
    }

    public boolean isInLog(Set<Integer> sidList) {
        if (com.rusefi.can.dbc.J1939Logic.gmlanIgnoreSender || com.rusefi.can.dbc.J1939Logic.j1939_mode) {
            for (int sid : sidList) {
                if (com.rusefi.can.dbc.J1939Logic.trimSid(sid) == getId())
                    return true;
            }
            return false;
        }
        else {
            return sidList.contains(getId());
        }
    }

    public void assertLength(CANPacket packet) {
        if (getLength() != packet.getData().length) {
            throw new IllegalArgumentException("Length mismatch for " + getName() + " (0x" + Integer.toHexString(packet.getId()) + "): DBC says " + getLength() + " but trace has " + packet.getData().length);
        }
    }

    @Override
    public String getFileName() {
        return DbcPacket.this.parent.getFileName();
    }
}
