package com.rusefi.can.reader.dbc;

import com.rusefi.can.Launcher;
import com.rusefi.can.dbc.DbcField;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DbcFileReader {
    public static void read(DbcFile dbc, BufferedReader reader) throws IOException {
        DbcPacketBuilder currentPacket = null;
        String line;
        int lineIndex = -1;
        Set<String> fieldNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        while ((line = reader.readLine()) != null) {
            lineIndex++;
            line = line.trim();
            if (line.startsWith("BO_ ")) {
                purgePacket(dbc, currentPacket);
                currentPacket = startNewPacket(line, currentPacket);
            } else if (line.startsWith("CM_ ")) {
                purgePacket(dbc, currentPacket);
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
                com.rusefi.can.dbc.DbcPacket packet = dbc.findPacket((int)id);
                String finalLine = line;
                Objects.requireNonNull(packet, () -> "While parsing CM_ line packet for " + id + finalLine);
                String originalName = tokens[3];
                String niceName = merge(tokens, 4);
                packet.addComment(originalName, niceName);


            } else if (line.startsWith("SG_ ")) {
                DbcField field;
                try {
                    field = DbcFieldParser.parseField(line, currentPacket.getPacketName(), currentPacket.getPacketId());
                } catch (Throwable e) {
                    throw new IllegalStateException("During [" + line + "]", e);
                }
                if (DbcFile.debugEnabled)
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
        purgePacket(dbc, currentPacket);

        System.out.println("DbcFileReader: Total " + dbc.size() + " packets");
    }

    private static DbcPacketBuilder startNewPacket(String line, DbcPacketBuilder currentPacket) {
        line = line.replaceAll(":", "");
        String[] tokens = line.split(" ");
        if (tokens.length < 3) {
            // skipping header line
            return currentPacket;
        }
        long decId = Long.parseLong(tokens[1]) & 0x1FFFFFFF;    // strip ExtID flag if any
        int trimmedId = com.rusefi.can.dbc.J1939Logic.trimSid((int) decId);
        String packetName = tokens[2];
        String source = tokens.length > 4 ? tokens[4] : "";
        currentPacket = new DbcPacketBuilder(trimmedId, packetName, source);
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

    private static void purgePacket(DbcFile dbc, DbcPacketBuilder currentPacket) {
        if (currentPacket != null) {
            if (currentPacket.isConsumed())
                return;
            int sid = currentPacket.getPacketId();
            com.rusefi.can.dbc.DbcPacket existingPacket = dbc.getPacket(sid);
            if (existingPacket != null) {
                //throw new IllegalStateException("We already have " + existingPacket.getName() + " for " + sid);
                currentPacket.markConsumed();
                System.err.println("Packets conflict: " + existingPacket.getName() + " and " + currentPacket.getPacketName() +
                    " have the same ID = " + sid);
            }
            List<DbcField> signals = new com.rusefi.can.dbc.util.GapFactory(currentPacket.getSignals(), currentPacket.getPacketName()).withGaps(sid);
            com.rusefi.can.dbc.DbcPacket packet = new com.rusefi.can.dbc.DbcPacket(sid, currentPacket.getPacketName(), currentPacket.getSource(), signals, dbc);
            dbc.addPacket(packet);
            currentPacket.markConsumed();
        }
    }

    public static String replaceSpecialWithSpaces(String line) {
        line = line.replaceAll("[|+@(,)\\[\\]]", " ");
        line = line.replaceAll(" +", " ");
        return line;
    }

    public static DbcFile readFromFile(String fileName) throws IOException {
        DbcFile dbc = new DbcFile();
        dbc.setFileName(fileName);
        if (fileName == null)
            return dbc;
        System.out.println(new Date() + " Reading DBC file from " + fileName + "..."); {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            read(dbc, reader);
        }
        return dbc;
    }
}
