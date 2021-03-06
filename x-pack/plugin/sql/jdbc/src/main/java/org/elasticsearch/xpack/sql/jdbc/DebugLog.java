/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */
package org.elasticsearch.xpack.sql.jdbc;

import org.elasticsearch.xpack.sql.client.StringUtils;

import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Locale;

// Logging is done through PrintWriter (not PrintStream which maps to System.err/out) to plug into the JDBC API
final class DebugLog {
    private static final String HEADER = "%tF/%tT.%tL - ";

    final PrintWriter print;
    private boolean flushAlways;

    DebugLog(PrintWriter print, boolean flushAlways) {
        this.print = print;
        this.flushAlways = flushAlways;
    }

    void logMethod(Method m, Object[] args) {
        long time = System.currentTimeMillis();
        print.printf(
            Locale.ROOT,
            HEADER + "Invoke %s#%s(%s)%n",
            time,
            time,
            time,
            // m.getReturnType().getSimpleName(),
            m.getDeclaringClass().getSimpleName(),
            m.getName(),
            // array(m.getParameterTypes()),
            array(args)
        );
        if (flushAlways) {
            print.flush();
        }
    }

    void logResult(Method m, Object[] args, Object r) {
        long time = System.currentTimeMillis();
        print.printf(
            Locale.ROOT,
            HEADER + "%s#%s(%s) returned %s%n",
            time,
            time,
            time,
            // m.getReturnType().getSimpleName(),
            m.getDeclaringClass().getSimpleName(),
            m.getName(),
            // array(m.getParameterTypes()),
            array(args),
            r
        );
        if (flushAlways) {
            print.flush();
        }
    }

    void logException(Method m, Object[] args, Throwable t) {
        long time = System.currentTimeMillis();
        print.printf(
            Locale.ROOT,
            HEADER + "%s#%s(%s) threw ",
            time,
            time,
            time,
            m.getDeclaringClass().getSimpleName(),
            m.getName(),
            array(args)
        );
        t.printStackTrace(print);
        print.flush();
    }

    void logSystemInfo() {
        long time = System.currentTimeMillis();
        print.printf(
            Locale.ROOT,
            HEADER + "OS[%s/%s/%s], JVM[%s/%s/%s/%s]",
            time,
            time,
            time,
            System.getProperty("os.name"),
            System.getProperty("os.version"),
            System.getProperty("os.arch"),
            System.getProperty("java.vm.vendor"),
            System.getProperty("java.vm.name"),
            System.getProperty("java.version"),
            System.getProperty("java.vm.version")
        );
        print.println();
        time = System.currentTimeMillis();
        print.printf(Locale.ROOT, HEADER + "JVM default timezone: %s", time, time, time, java.util.TimeZone.getDefault().toString());
        print.println();
        print.flush();
    }

    private static String array(Object[] a) {
        if (a == null || a.length == 0) {
            return StringUtils.EMPTY;
        }
        if (a.length == 1) {
            return handleArray(a[0]);
        }

        StringBuilder b = new StringBuilder();
        int iMax = a.length - 1;
        for (int i = 0;; i++) {
            b.append(handleArray(a[i]));
            if (i == iMax) {
                return b.toString();
            }
            b.append(", ");
        }
    }

    private static String handleArray(Object o) {
        if (o != null && o.getClass().isArray()) {
            StringBuilder b = new StringBuilder();
            int l = Array.getLength(o);
            int iMax = l - 1;

            if (iMax == -1) return "[]";

            b.append('[');
            for (int i = 0; i < l; i++) {
                b.append(handleArray(Array.get(o, i)));
                if (i == iMax) {
                    return b.append("]").toString();
                }
                b.append(", ");
            }
        }
        return String.valueOf(o);
    }
}
