package com.rusefi.can.reader.dbc;

import com.rusefi.can.LoggingStrategy;
import com.rusefi.sensor_logs.BinaryLogEntry;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class DbcFile {
    public final LinkedHashMap<Integer, DbcPacket> packets = new LinkedHashMap<>();

    private static final boolean debugEnabled = false;

    private List<BinaryLogEntry> list;

    public static DbcFile readFromFile(String fileName) throws IOException {
        DbcFile dbc = new DbcFile();
        {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            dbc.read(reader);
        }
        return dbc;
    }

    public void read(BufferedReader reader) throws IOException {
        DbcPacket currentPacket = null;
        String line;
        int lineIndex = -1;
        while ((line = reader.readLine()) != null) {
            lineIndex++;
            line = line.trim();
            if (line.startsWith("BO_")) {
                purgePacket(currentPacket);
                line = line.replaceAll(":", "");
                String[] tokens = line.split(" ");
                int decId = Integer.parseInt(tokens[1]);
                String packetName = tokens[2];
                currentPacket = new DbcPacket(decId, packetName);
            } else if (line.startsWith("CM_")) {
                purgePacket(currentPacket);
                line = replaceSpecialWithSpaces(line);
                String[] tokens = line.split(" ");
                if (tokens.length == 1) {
                    // skipping header line
                    continue;
                }
                if (tokens.length < 4)
                    throw new IllegalStateException("Failing to parse comment: " + line + " at " + lineIndex);
                int id = Integer.parseInt(tokens[2]);
                DbcPacket packet = packets.get(id);
                Objects.requireNonNull(packet, "packet for " + id);
                String originalName = tokens[3];
                String niceName = merge(tokens, 4);
                packet.replaceName(originalName, niceName);


            } else if (line.startsWith("SG_")) {
                line = replaceSpecialWithSpaces(line);
                String[] tokens = line.split(" ");
                String name = tokens[1];
                int index = 1;
                while (!tokens[index - 1].equals(":"))
                    index++;


                if (debugEnabled)
                    System.out.println(line);
                int startOffset;
                try {
                    startOffset = Integer.parseInt(tokens[index]);
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("While " + line, e);
                }
                int length = Integer.parseInt(tokens[index + 1]);

                double mult = Double.parseDouble(tokens[index + 3]);

                DbcField field = new DbcField(name, startOffset, length, mult, currentPacket.getName());
                if (debugEnabled)
                    System.out.println("Found " + field);
                currentPacket.add(field);

            } else {
                // skipping useless line
            }
        }
        purgePacket(currentPacket);

        System.out.println(getClass().getSimpleName() + ": Total " + packets.size() + " packets");
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

    private void purgePacket(DbcPacket currentPacket) {
        if (currentPacket != null)
            packets.put(currentPacket.getId(), currentPacket);
    }

    private String replaceSpecialWithSpaces(String line) {
        line = line.replaceAll("[|+@(,)\\[\\]]", " ");
        line = line.replaceAll(" +", " ");
        return line;
    }

    public DbcPacket findPacket(int canId) {
        return packets.get(canId);
    }


    public List<BinaryLogEntry> getFieldNameEntries() {
        if (list == null) {
            list = LoggingStrategy.getFieldNameEntries(this);
        }
        return list;
    }
}
