package com.rusefi.can.reader.dbc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidateDbc {
    private static final Pattern SUFFIX_PATTERN = Pattern.compile("_(\\d+)_([0-9A-F]+)$");
    private static final Pattern REVERSED_PATTERN = Pattern.compile("_([0-9A-F]+)_(\\d+)$");

    public static List<String> checkDbcLines(List<String> lines) {
        List<String> errors = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();
            if (!line.startsWith("BO_ ")) {
                continue;
            }

            String[] parts = line.split("\\s+");
            if (parts.length < 3) {
                continue;
            }

            long msgIdDec;
            try {
                // Remove any non-digit characters from the ID field if they exist, but standard is just digits.
                // The Python script just did int(parts[1]).
                msgIdDec = Long.parseLong(parts[1]) & 0x1FFFFFFF;
            } catch (NumberFormatException e) {
                continue;
            }

            String msgName = parts[2];
            if (msgName.endsWith(":")) {
                msgName = msgName.substring(0, msgName.length() - 1);
            }

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
                        if (hasLetters) {
                            errors.add("REVERSED suffix: " + msgName);
                            continue;
                        } else {
                            errors.add("Wrong order: " + msgName + " (hex before decimal)");
                            continue;
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            Matcher matcher = SUFFIX_PATTERN.matcher(msgName);
            if (matcher.find()) {
                String decSuffixStr = matcher.group(1);
                String hexSuffixStr = matcher.group(2);

                try {
                    long decSuffixVal = Long.parseLong(decSuffixStr);
                    long hexSuffixVal = Long.parseLong(hexSuffixStr, 16);

                    boolean reported = false;
                    // 1. Validate decimal equals hex
                    if (decSuffixVal != hexSuffixVal) {
                        errors.add("Value mismatch: " + msgName + " (dec suffix " + decSuffixVal + " != hex suffix " + hexSuffixVal + ")");
                        reported = true;
                    }

                    // 2. Validate decimal suffix matches message ID
                    if (decSuffixVal != msgIdDec) {
                        errors.add("ID mismatch: " + msgName + " (suffix " + decSuffixVal + " != ID " + msgIdDec + ")");
                        reported = true;
                    }

                    if (reported) {
                        continue;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return errors;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: ValidateDbc <dbc_file>");
            System.exit(-1);
        }

        String fileName = args[0];
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        List<String> errors = checkDbcLines(lines);
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
