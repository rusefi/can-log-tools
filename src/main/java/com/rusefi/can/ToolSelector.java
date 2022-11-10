package com.rusefi.can;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ToolSelector {
    private static final Map<String, Class> TOOLS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        TOOLS.put("trc2mlq", TrcToMlq.class);

        if (args.length == 0 || !TOOLS.containsKey(args[0])) {
            System.err.println("First argument should be tool name, one of: " + TOOLS.keySet());
            System.exit(-1);
        }
        String toolName = args[0];
        Class<?> toolClass = TOOLS.get(toolName);
        System.out.println("Invoking " + toolName + ": " + toolClass);
        List<String> a = Arrays.asList(args).subList(1, args.length);
        System.out.println("Removing tool name argument, invoking with [" + a + "]");
        String[] truncatedArgs = a.toArray(a.toArray(new String[0]));

        Method m = toolClass.getMethod("main", String[].class);
        m.invoke(null, new Object[]{truncatedArgs});
    }
}
