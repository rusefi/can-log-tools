package com.rusefi.can;

import com.rusefi.can.analysis.ByteRateOfChange;
import com.rusefi.can.analysis.ByteRateOfChangeReports;
import com.rusefi.can.reader.CANLineReader;
import com.rusefi.can.reader.ReaderType;
import com.rusefi.can.reader.ReaderTypeHolder;
import com.rusefi.util.FolderUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ByteRateOfChangeSandbox {
    public static void main(String[] args) throws IOException {
        ReaderTypeHolder.INSTANCE.type = ReaderType.PCAN;

        String inputFolderName = "C:\\stuff\\rusefi_documentation\\OEM-Docs\\Nissan\\2011_Xterra\\CAN-Nov-2022";

        String reportDestinationFolder = inputFolderName + File.separator + "processed";
        new File(reportDestinationFolder).mkdirs();


        List<ByteRateOfChange.TraceReport> reports = new ArrayList<>();

        FolderUtil.handleFolder(inputFolderName, (simpleFileName, fullInputFileName) -> {

            List<CANPacket> logFileContent = CANLineReader.getReader().readFile(fullInputFileName);

            ByteRateOfChange.TraceReport report = ByteRateOfChange.process(reportDestinationFolder, simpleFileName, logFileContent);
            reports.add(report);
        }, "pcan.trc");


        ByteRateOfChangeReports.compareEachReportAgainstAllOthers(reports);
    }
}
