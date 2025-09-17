package pubsub.util;

public final class Sleep {
    private Sleep() {}
    public static void ms(long ms) { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }
}
