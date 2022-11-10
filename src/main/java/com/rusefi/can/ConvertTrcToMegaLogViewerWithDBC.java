package com.rusefi.can;

import com.rusefi.can.reader.CANLineReader;
import com.rusefi.can.reader.dbc.DbcFile;
import com.rusefi.util.FolderUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConvertTrcToMegaLogViewerWithDBC {
    public static void doJob(String dbcFileName, String inputFolderName, String outputFolder) throws IOException {
        DbcFile dbc = DbcFile.readFromFile(dbcFileName);

        System.out.println("inputFolderName " + inputFolderName);
        System.out.println("outputFolder " + outputFolder);

        FolderUtil.FileAction fileAction = (simpleFileName, fullFileName) -> {
            List<CANPacket> packets = CANLineReader.getReader().readFile(fullFileName);

            String outputFileName = outputFolder + File.separator + simpleFileName + ".mlg";

            LoggingStrategy.writeLog(dbc, packets, outputFileName);
        };


        FolderUtil.handleFolder(inputFolderName, fileAction);
    }
}
