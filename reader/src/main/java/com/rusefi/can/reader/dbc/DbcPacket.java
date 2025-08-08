package com.rusefi.can.reader.dbc;

import com.rusefi.can.Launcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Packet describes all the fields for specific can ID
 * also known as frame
 */
public class DbcPacket {
    private final int id;
    private final String name;
    private final List<DbcField> fields = new ArrayList<>();

    public DbcPacket(int id, String name, List<DbcField> signals) {
        this.id = id;
        this.name = name;
        fields.addAll(signals);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<DbcField> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return "DbcPacket{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public DbcField find(String name) {
        for (DbcField field : fields) {
            if (field.getName().equalsIgnoreCase(name))
                return field;
        }
        return null;
    }

    public void replaceName(String originalName, String niceName) {
        if (niceName.charAt(niceName.length() - 1) == ';')
            niceName = niceName.substring(0, niceName.length() - 1);
        niceName = unquote(niceName);
        DbcField field = find(originalName);
        if (field == null) {
            System.err.println("Field not found by [" + originalName + "]");
            return;
        }
        Objects.requireNonNull(field, "By " + originalName);
        field.rename(niceName);
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
            if (bitIndex >= field.getStartOffset() && bitIndex < field.getStartOffset() + field.getLength())
                return field;
        }
        return null;
    }

    public boolean isInLog(Set<Integer> sidList) {
        if (Launcher.gmlanIgnoreSender || Launcher.j1939_mode) {
            for (int sid : sidList) {
                if (DbcFile.trimSid(sid) == getId())
                    return true;
            }
            return false;
        }
        else {
            return sidList.contains(getId());
        }
    }
}
