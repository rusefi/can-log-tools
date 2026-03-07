package com.rusefi.can.reader.dbc;

import com.rusefi.can.dbc.DbcField;
import com.rusefi.can.dbc.DbcFile;

public class DbcFieldParser {
    public static DbcField parseField(String line, String parentName, int packetId) {
        line = DbcFileReader.replaceSpecialWithSpaces(line);
        String[] tokens = line.split(" ");
        if (tokens.length < 2)
            return null;
        String name = tokens[1];
        int index = 1;
        try {
            while (!tokens[index - 1].equals(":"))
                index++;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalStateException("While parsing [" + line + "]", e);
        }

        if (DbcFile.debugEnabled)
            System.out.println(line);
        int startOffset;
        try {
            startOffset = Integer.parseInt(tokens[index]);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("While " + line, e);
        }
        int length = Integer.parseInt(tokens[index + 1]);
        String endiannessCodeString = tokens[index + 2];
        int endiannessCode = Integer.parseInt(endiannessCodeString.substring(0,1));
        boolean isSigned = endiannessCodeString.endsWith("-");

        if (endiannessCode != 0 && endiannessCode != 1)
            throw new IllegalStateException("Unexpected endiannessCode " + endiannessCodeString);
        boolean isBigEndian = endiannessCode == 0;

        double mult = Double.parseDouble(tokens[index + 3]);
        double offset = Double.parseDouble(tokens[index + 4]);

        return new DbcField(packetId, name, startOffset, length, mult, offset, parentName, isBigEndian, isSigned);
    }
}
