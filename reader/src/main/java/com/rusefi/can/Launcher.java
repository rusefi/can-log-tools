package com.rusefi.can;

import com.rusefi.can.analysis.ByteRateOfChangeReports;

import java.io.IOException;
import java.util.Arrays;

public class Launcher {
    public static final String FILENAME_SUFFIX_PROPERTY = "-suffix";
    public static String fileNameSuffixValue = ".trc";
    public static final String FILENAME_FILTER_PROPERTY = "-filter";
    public static String fileNameFilter;
    public static final String DBC_FILENAME_PROPERTY = "-dbc";
    public static String dbcFileName;
    public static final String DBC_DUP_FIELD_NAMES = "-allow-dup-names";
    public static boolean allowDuplicateNames = false;

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("At least folder name is expected");
            System.err.println("Supported arguments: " + Arrays.toString(new String[]{
                    FILENAME_SUFFIX_PROPERTY,
                    FILENAME_FILTER_PROPERTY,
                    DBC_FILENAME_PROPERTY,
                    DBC_DUP_FIELD_NAMES,
            }));
            System.exit(-1);
        }
        String inputFolderName = args[0];
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case DBC_DUP_FIELD_NAMES:
                    allowDuplicateNames = true;
                    break;
                case FILENAME_SUFFIX_PROPERTY:
                    i += 1;
                    fileNameSuffixValue = args[i];
                    break;
                case FILENAME_FILTER_PROPERTY:
                    i += 1;
                    fileNameFilter = args[i];
                    break;
                case DBC_FILENAME_PROPERTY:
                    i += 1;
                    dbcFileName = args[i];
                    break;
                default:
                    throw new IllegalStateException("Unexpected argument " + args[i]);
            }
        }

        System.out.println("Running");
        System.out.println("\tinputFolderName=" + inputFolderName);
        System.out.println("\tfileNameSuffixValue=" + fileNameSuffixValue);
        System.out.println("\tdbcFileName=" + dbcFileName);
        ByteRateOfChangeReports.scanInputFolder(inputFolderName, fileNameSuffixValue);
    }
}
