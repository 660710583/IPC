package pubsub.util;

import pubsub.RuntimeState;

public final class Log {
    private Log() {}
    public static void i(RuntimeState st, String tag, String msg) {
        System.out.printf("[%-10s|%-4s|%5s] %s%n", st.shortName, tag, st.role(), msg);
    }
    public static void e(RuntimeState st, String msg, Throwable t) {
        i(st, "ERR", msg + ": " + t);
    }
}
