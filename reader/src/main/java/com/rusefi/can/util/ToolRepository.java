package com.rusefi.can.util;

import com.rusefi.can.Launcher;
import com.rusefi.can.reader.isotp.IsoTpFileDecoderFolderStrategy;
import com.rusefi.can.tool.ValidateDbc;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * LOL see ToolSelector TODO unify already
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
                Launcher.class
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
