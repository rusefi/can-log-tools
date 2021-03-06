package com.rusefi.can.reader.dbc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DbcFile {
    public final List<DbcPacket> packets = new ArrayList<>();

    private static final boolean debugEnabled = false;

    public static DbcFile readFromFile(String fileName) throws IOException {
        DbcFile dbc = new DbcFile();
        {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            dbc.read(reader);
        }
        return dbc;
    }

    public void read(BufferedReader reader) throws IOException {
        DbcPacket currentState = null;
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("BO_")) {
                if (currentState != null)
                    this.packets.add(currentState);
                line = line.replaceAll(":", "");
                String[] tokens = line.split(" ");
                int decId = Integer.parseInt(tokens[1]);
                String name = tokens[2];
                currentState = new DbcPacket(decId, name);

            } else if (line.startsWith("SG_")) {
                line = line.replaceAll("[|+@(,)\\[\\]]", " ");
                line = line.replaceAll(" +", " ");
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

                DbcField field = new DbcField(name, startOffset, length, mult);
                if (debugEnabled)
                    System.out.println("Found " + field);
                currentState.add(field);

            } else {
                // skipping useless line
            }
        }
        if (currentState != null)
            this.packets.add(currentState);

        System.out.println(getClass().getSimpleName() + ": Total " + packets.size() + " packets");
    }

    // todo: performance optimization SOON
    public DbcPacket findPacket(int i) {
        for (DbcPacket packet : packets) {
            if (packet.getId() == i)
                return packet;
        }
        return null;
    }
}
