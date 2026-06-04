package com.rusefi.can.util;

import com.rusefi.can.TraceToMlqConverterTool;
import com.rusefi.can.analysis.NeverChangingFieldScannerTool;
import com.rusefi.can.Launcher;
import com.rusefi.can.analysis.*;
import com.rusefi.can.analysis.checksum.ChecksumScannerTool;
import com.rusefi.can.analysis.counter_scanner.CounterScannerTool;
import com.rusefi.can.analysis.filter.PerSidDumpTool;
import com.rusefi.can.analysis.filter.ReportBySenderTool;
import com.rusefi.can.analysis.growing_values.GrowingValuesScannerTool;
import com.rusefi.can.mlv.CanToMegaLogViewerConverterTool;
import com.rusefi.can.reader.isotp.IsoTpFolderDecoderTool;
import com.rusefi.can.render.DbcImageTool;
import com.rusefi.can.tool.ValidateDbcTool;
import com.rusefi.can.tool.sync.SyncFolderTool;
import com.rusefi.can.tool.sync.SyncTraceFilesTool;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Central registry of all top-level CAN trace tools (classes with {@code main()}).
 * Running its own {@code main} invokes each registered tool with empty args as a smoke test.
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
        registerTool(ValidateDbcTool.class,
                IsoTpFolderDecoderTool.class,
                TraceToMlqConverterTool.class,
                SyncTraceFilesTool.class,
                SyncFolderTool.class,
                ValidateDbcTool.class,
                Launcher.class,
                DbcImageTool.class,
                PerSidDumpTool.class,
                PacketFrequencyTool.class,
                ReportBySenderTool.class,
                CounterScannerTool.class,
                ChecksumScannerTool.class,
                GrowingValuesScannerTool.class,
                CanToMegaLogViewerConverterTool.class,
                PacketCountDistributionTool.class,
                FirstOccurrencePerIdTool.class,
                ByteVariabilityScannerTool.class,
                NeverChangingFieldScannerTool.class
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
