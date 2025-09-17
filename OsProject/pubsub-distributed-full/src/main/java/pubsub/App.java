package pubsub;

import pubsub.roles.*;
import pubsub.util.Log;

import java.util.concurrent.*;

public final class App {
    public static void main(String[] args) {
        Config cfg = Config.fromArgs(args);
        RuntimeState st = new RuntimeState(cfg);

        Log.i(st, "BOOT", "pid=" + st.pid + " redis=" + cfg.host + ":" + cfg.port +
                (cfg.pass != null && !cfg.pass.isEmpty() ? " (auth)" : ""));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try (var j = RedisClient.connect(cfg)) { RedisClient.unregisterSelf(j, st.pid); } catch (Exception ignore) {}
            System.out.printf("[%-10s|SHUT] done%n", st.shortName);
        }));

        ExecutorService pool = Executors.newFixedThreadPool(4);
        pool.submit(new Subscriber(cfg, st));
        pool.submit(new Publisher(cfg, st));
        pool.submit(new Coordinator(cfg, st));
        pool.submit(new Commander(cfg, st));

        try { pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS); } catch (InterruptedException ignored) {}
    }
}