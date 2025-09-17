package pubsub;

public final class Keys {
    private Keys() {}
    public static final String Z_MEMBERS = "members";
    public static String info(long pid) { return "info:" + pid; }
    public static String hb(long pid)   { return "hb:" + pid; }
}
