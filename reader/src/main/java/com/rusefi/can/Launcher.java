package com.rusefi.can;

import com.rusefi.can.analysis.ByteRateOfChangeReports;
import com.rusefi.can.dbc.J1939Logic;
import com.rusefi.can.dbc.reader.DbcFileReader;
import com.rusefi.can.util.ToolRepository;

import java.io.IOException;
import java.util.Arrays;

public class Launcher {
    public static final String FILENAME_SUFFIX_PROPERTY = "-suffix";
    public static String fileNameSuffixValue = ".trc"; // default value
    public static final String FILENAME_FILTER_PROPERTY = "-filter";
    public static String fileNameFilter;
    public static final String DBC_FILENAME_PROPERTY = "-dbc";
    public static String dbcFileName;
    private static final String DBC_DUP_FIELD_NAMES = "-allow-dup-names";
    public static boolean skipChecksumFields = false;

    // lower 13 bits in GMLAN IDs is a sender address. DBC may contain the only zeros in this field
    private static final String GMLAN_IGNORE_SENDER = "-gmlan-ignore-sender";

    // J1939: lower 8 bits are sender, higher 3 bits - Priority. DBC contains weird 'FE' in these fields
    private static final String J1939_MODE = "-j1939-mode";

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("At least folder name is expected");
            System.err.println("Supported arguments: " + Arrays.toString(new String[]{
                    FILENAME_SUFFIX_PROPERTY,
                    FILENAME_FILTER_PROPERTY,
                    DBC_FILENAME_PROPERTY,
                    DBC_DUP_FIELD_NAMES,
                    GMLAN_IGNORE_SENDER,
                    J1939_MODE
            }));
            ToolRepository.exitWithErrorCodeUnlessToolRegistry();
            return;
        }
        String inputFolderName = args[0];
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case DBC_DUP_FIELD_NAMES:
                    DbcFileReader.allowDuplicateNames = true;
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
                case "-skip-checksum-fields":
                    skipChecksumFields = true;
                    break;
                case GMLAN_IGNORE_SENDER:
                    J1939Logic.gmlanIgnoreSender = true;
                    break;
                case J1939_MODE:
                    J1939Logic.j1939_mode = true;
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
