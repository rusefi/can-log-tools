package com.rusefi.can.reader.dbc;

import com.rusefi.can.Launcher;
import com.rusefi.mlv.LoggingStrategy;
import com.rusefi.sensor_logs.BinaryLogEntry;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class DbcFile {
    private final LinkedHashMap<Integer, DbcPacket> packets = new LinkedHashMap<>();

    public static final boolean debugEnabled = false;
    /**
     * problem: looks like some DBC files which were created by AndreyB manually were invalid
     * this flag is technical debt of conversion from bad DBC into legit DBC
     */
    public static boolean compatibilityWithBrokenRusEfiLogic;

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
        if (fileName == null)
            return dbc;
        System.out.println(new Date() + " Reading DBC file from " + fileName + "..."); {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            dbc.read(reader);
        }
        return dbc;
    }

    public void read(BufferedReader reader) throws IOException {
        DbcPacketBuilder currentPacket = null;
        String line;
        int lineIndex = -1;
        Set<String> fieldNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        while ((line = reader.readLine()) != null) {
            lineIndex++;
            line = line.trim();
            if (line.startsWith("BO_ ")) {
                purgePacket(currentPacket);
                currentPacket = startNewPacket(line, currentPacket);
            } else if (line.startsWith("CM_ ")) {
                purgePacket(currentPacket);
                line = replaceSpecialWithSpaces(line);
                String[] tokens = line.split(" ");
                if (tokens.length == 1) {
                    // skipping header line
                    continue;
                }
                if (!tokens[1].equals("SG_")) {
                    // will parse only signal descriptions
                    continue;
                }
                if (tokens.length < 4)
                    throw new IllegalStateException("Failing to parse comment: " + line + " at " + lineIndex);
                long id = Long.parseLong(tokens[2]) & 0x1FFFFFFF;    // strip ExtID flag if any
                DbcPacket packet = findPacket((int)id);
                Objects.requireNonNull(packet, "packet for " + id);
                String originalName = tokens[3];
                String niceName = merge(tokens, 4);
                packet.addComment(originalName, niceName);


            } else if (line.startsWith("SG_ ")) {
                DbcField field;
                try {
                    field = DbcField.parseField(line, currentPacket.getPacketName(), currentPacket.getPacketId());
                } catch (Throwable e) {
                    throw new IllegalStateException("During [" + line + "]", e);
                }
                if (debugEnabled)
                    System.out.println("Found " + field);
                if (field != null) {
                    if (!fieldNames.add(field.getName()) && !Launcher.allowDuplicateNames)
                        throw new IllegalArgumentException("Let's use unique field names: " + field.getName());
                    currentPacket.add(field);
                }

            } else {
                // skipping useless line
            }
        }
        purgePacket(currentPacket);

        System.out.println(getClass().getSimpleName() + ": Total " + packets.size() + " packets");
    }

    private static DbcPacketBuilder startNewPacket(String line, DbcPacketBuilder currentPacket) {
        line = line.replaceAll(":", "");
        String[] tokens = line.split(" ");
        if (tokens.length < 3) {
            // skipping header line
            return currentPacket;
        }
        long decId = Long.parseLong(tokens[1]) & 0x1FFFFFFF;    // strip ExtID flag if any
        int trimmedId = trimSid((int) decId);
        String packetName = tokens[2];
        currentPacket = new DbcPacketBuilder(trimmedId, packetName);
        return currentPacket;
    }

    private static String merge(String[] tokens, int position) {
        StringBuilder sb = new StringBuilder();
        for (int i = position; i < tokens.length; i++) {
            if (sb.length() > 0)
                sb.append(" ");
            sb.append(tokens[i]);
        }
        return sb.toString();
    }

    private void purgePacket(DbcPacketBuilder currentPacket) {
        if (currentPacket != null) {
            if (currentPacket.isConsumed())
                return;
            int sid = currentPacket.getPacketId();
            DbcPacket existingPacket = packets.get(sid);
            if (existingPacket != null) {
                //throw new IllegalStateException("We already have " + existingPacket.getName() + " for " + sid);
                currentPacket.markConsumed();
                System.err.println("Packets conflict: " + existingPacket.getName() + " and " + currentPacket.getPacketName() +
                    " have the same ID = " + sid);
            }
            List<DbcField> signals = new GapFactory(currentPacket.getSignals(), currentPacket.getPacketName()).withGaps(sid);
            packets.put(sid, new DbcPacket(sid, currentPacket.getPacketName(), signals));
            currentPacket.markConsumed();
        }
    }

    public static String replaceSpecialWithSpaces(String line) {
        line = line.replaceAll("[|+@(,)\\[\\]]", " ");
        line = line.replaceAll(" +", " ");
        return line;
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
                return new DbcPacket(sid, packetName, new GapFactory(Collections.emptyList(), packetPrefix).withGaps(sid));
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
