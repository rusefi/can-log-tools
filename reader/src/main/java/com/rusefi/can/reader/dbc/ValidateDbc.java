package com.rusefi.can.reader.dbc;

import java.io.IOException;
import java.util.ArrayList;
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
                errors.add("Value mismatch: " + msgName + " (dec suffix " + decSuffixVal + " != hex suffix " + hexSuffixVal + ")");
            }

            // 2. Validate decimal suffix matches message ID
            if (decSuffixVal != msgIdDec) {
                errors.add("ID mismatch: " + msgName + " (suffix " + decSuffixVal + " != ID " + msgIdDec + ")");
            }

        }
        return errors;
    }

    public static DbcFile readFromFileWithValidation(String fileName) throws IOException {
        DbcFile dbc = DbcFile.readFromFile(fileName);
        List<String> errors = checkDbc(dbc);

        if (!errors.isEmpty()) {
            throw new IllegalStateException(errors.toString());
        }
        return dbc;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: ValidateDbc <dbc_file>");
            System.exit(-1);
        }

        String fileName = args[0];
        DbcFile dbc = DbcFile.readFromFile(fileName);

        List<String> errors = checkDbc(dbc);
        for (String err : errors) {
            System.out.println(err);
        }

        if (errors.isEmpty()) {
            System.out.println("No errors found in BO_ suffixes for " + fileName);
        } else {
            System.exit(1);
        }
    }
}
