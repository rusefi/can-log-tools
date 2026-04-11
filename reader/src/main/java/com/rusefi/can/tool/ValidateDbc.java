package com.rusefi.can.tool;

import com.rusefi.can.dbc.DbcField;
import com.rusefi.can.dbc.DbcFile;
import com.rusefi.can.dbc.DbcPacket;
import com.rusefi.can.dbc.reader.DbcFileReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidateDbc {
    private static final Pattern SUFFIX_PATTERN = Pattern.compile("_(\\d+)_([0-9A-F]+)$");
    private static final Pattern REVERSED_PATTERN = Pattern.compile("_([0-9A-F]+)_(\\d+)$");

    public static List<String> checkDbc(DbcFile dbc) {
        List<String> errors = new ArrayList<>();

        for (DbcPacket packet : dbc.values()) {
            errors.addAll(checkPacket(packet.getId(), packet.getName()));
            errors.addAll(checkFieldsOverlap(packet));
            errors.addAll(checkFieldsEndianness(packet));
        }

        return errors;
    }

    public static List<String> checkFieldsEndianness(DbcPacket packet) {
        List<String> errors = new ArrayList<>();
        if (packet.getFields().size() < 2) {
            return errors;
        }
        Boolean firstIsBigEndian = null;
        for (DbcField field : packet.getFields()) {
            if (field.getName().contains("_gap_")) {
                continue;
            }
            if (firstIsBigEndian == null) {
                firstIsBigEndian = field.isBigEndian();
            } else if (field.isBigEndian() != firstIsBigEndian) {
                errors.add("Mixed endianness in " + packet.getName() + " (ID " + packet.getId() + "): Field " + field.getName() + " has different endianness than other fields.");
            }
        }
        return errors;
    }

    public static List<String> checkFieldsOverlap(DbcPacket packet) {
        List<String> errors = new ArrayList<>();
        BitSet usedBits = new BitSet();

        for (DbcField field : packet.getFields()) {
            if (field.getName().contains("_gap_")) {
                continue;
            }
            BitSet thisFieldBits = new BitSet();
            DbcField.getUsedBits(thisFieldBits, field.getStartOffset(), field.getLength(), field.isBigEndian());

            for (int bitIndex = thisFieldBits.nextSetBit(0); bitIndex >= 0; bitIndex = thisFieldBits.nextSetBit(bitIndex + 1)) {
                if (usedBits.get(bitIndex)) {
                    errors.add("Overlap in " + packet.getName() + " (ID " + packet.getId() + "): Field " + field.getName() + " uses bit " + bitIndex + " which is already used.");
                }
                usedBits.set(bitIndex);
            }
        }
        return errors;
    }

    public static List<String> checkPacket(long msgIdDec, String msgName) {
        List<String> errors = new ArrayList<>();

        // Flag any message that has hex before decimal (reversed suffix)
        Matcher revMatcher = REVERSED_PATTERN.matcher(msgName);
        if (revMatcher.find()) {
            String hStr = revMatcher.group(1);
            String dStr = revMatcher.group(2);
            try {
                long dVal = Long.parseLong(dStr);
                long hVal = Long.parseLong(hStr, 16);
                boolean hasLetters = false;
                for (char c : hStr.toUpperCase().toCharArray()) {
                    if (c >= 'A' && c <= 'F') {
                        hasLetters = true;
                        break;
                    }
                }
                if (dVal == hVal) {
                    String baseName = msgName.substring(0, revMatcher.start());
                    String properName = baseName + "_" + dStr + "_" + hStr;
                    if (hasLetters) {
                        errors.add("REVERSED suffix: " + msgName + " should be " + properName);
                    } else {
                        errors.add("Wrong order: " + msgName + " (hex before decimal) should be " + properName);
                    }
                    return errors;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        Matcher matcher = SUFFIX_PATTERN.matcher(msgName);
        if (matcher.find()) {
            String decSuffixStr = matcher.group(1);
            String hexSuffixStr = matcher.group(2);

            long decSuffixVal = Long.parseLong(decSuffixStr);
            long hexSuffixVal = Long.parseLong(hexSuffixStr, 16);

            // 1. Validate decimal equals hex
            if (decSuffixVal != hexSuffixVal) {
                errors.add("Value mismatch: " + msgName + " (dec suffix " + decSuffixVal + " != hex suffix " + hexSuffixVal + "=0x" + hexSuffixStr + ") in [" + msgName + "]");
            }

            // 2. Validate decimal suffix matches message ID
            if (decSuffixVal != msgIdDec) {
                errors.add("ID mismatch: " + msgName + " (suffix " + decSuffixVal + " != ID " + msgIdDec + ")");
            }

        }
        return errors;
    }

    public static DbcFile readFromFileWithValidation(String fileName) throws IOException {
        DbcFile dbc = DbcFileReader.readFromFile(fileName);
        List<String> errors = checkDbc(dbc);

        if (!errors.isEmpty()) {
            throw new IllegalStateException(errors.size() + " errors: " + errors);
        }
        return dbc;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: ValidateDbc <dbc_file>");
            System.exit(-1);
        }

        String fileName = args[0];
        validate(fileName);
    }

    public static void validate(String fileName) throws IOException {
        DbcFile dbc = DbcFileReader.readFromFile(fileName);

        List<String> errors = checkDbc(dbc);
        for (String err : errors) {
            System.err.println(err);
        }

        if (errors.isEmpty()) {
            System.out.println("No errors found in " + fileName);
        } else {
            System.exit(1);
        }
    }
}
