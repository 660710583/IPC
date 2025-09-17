package pubsub;

import java.lang.management.ManagementFactory;
import java.util.Random;

public final class RuntimeState {
    public final Config cfg;
    public final long pid;
    public volatile long leaderPid = -1;
    public volatile boolean running = true;
    public final String shortName;

    public RuntimeState(Config cfg) {
        this.cfg = cfg;
        this.pid = readPid();
        this.shortName = cfg.name.length() > 10 ? cfg.name.substring(0,10) : cfg.name;
    }

    private long readPid() {
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        try { return Long.parseLong(jvmName.split("@")[0]); }
        catch (Exception e) { return new Random().nextInt(100_000) + 10_000; }
    }

    public String role() { return pid == leaderPid ? "BOSS" : "WORKER"; }
}