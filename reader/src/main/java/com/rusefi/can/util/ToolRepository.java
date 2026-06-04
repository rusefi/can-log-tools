package com.rusefi.can.util;

import com.rusefi.can.AlwaysSameScanner;
import com.rusefi.can.Launcher;
import com.rusefi.can.TrcToMlqConverterTool;
import com.rusefi.can.analysis.*;
import com.rusefi.can.analysis.checksum.ChecksumScannerTool;
import com.rusefi.can.analysis.counter_scanner.CounterScannerTool;
import com.rusefi.can.analysis.filter.PerSidDumpTool;
import com.rusefi.can.analysis.filter.ReportBySourceTool;
import com.rusefi.can.analysis.groving_values.GrowingValuesScanner;
import com.rusefi.can.mlv.CanToMegaLogViewerConverterTool;
import com.rusefi.can.reader.isotp.IsoTpFileDecoderFolderStrategy;
import com.rusefi.can.render.DbcImageTool;
import com.rusefi.can.tool.ValidateDbc;
import com.rusefi.can.tool.sync.SyncFolder;
import com.rusefi.can.tool.sync.SyncTrcFiles;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class ToolRepository {
    private static final Set<Class<?>> TOOLS = new HashSet<>();
    private static boolean doNotExit;

    public static void registerTool(Class... toolClasses) {
        for (Class<?> tool : toolClasses) {
            TOOLS.add(tool);
        }
    }

    public static Set<Class<?>> getTools() {
        return TOOLS;
    }

    static {
        registerTool(ValidateDbc.class,
                IsoTpFileDecoderFolderStrategy.class,
                TrcToMlqConverterTool.class,
                SyncTrcFiles.class,
                SyncFolder.class,
                ValidateDbc.class,
                Launcher.class,
                DbcImageTool.class,
                PerSidDumpTool.class,
                PacketFrequencyTool.class,
                ReportBySourceTool.class,
                CounterScannerTool.class,
                ChecksumScannerTool.class,
                GrowingValuesScanner.class,
                CanToMegaLogViewerConverterTool.class,
                PacketRatioTool.class,
                FirstPacketTool.class,
                ByteRateOfChange.class,
                AlwaysSameScanner.class
        );
    }

    public static void main(String[] args) throws Exception {
        doNotExit = true;
        for (Class<?> toolClass : TOOLS) {
            System.out.println("We have " + toolClass.getName());
            Method main = toolClass.getDeclaredMethod("main", String[].class);
            main.invoke(null, (Object) new String[0]);
        }
    }

    public static void exitWithErrorCodeUnlessToolRegistry() {
        if (!doNotExit)
            System.exit(-1);
    }
}
