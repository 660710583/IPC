package pubsub.roles;

import pubsub.*;
import pubsub.util.Log;
import pubsub.util.Sleep;
import redis.clients.jedis.Jedis;

import java.util.Map;

public final class Publisher implements Runnable {
    private final Config cfg; private final RuntimeState st;
    public Publisher(Config cfg, RuntimeState st) { this.cfg = cfg; this.st = st; }

    @Override public void run() {
        try (Jedis j = RedisClient.connect(cfg)) {
            RedisClient.registerSelf(j, st);
            while (st.running) {
                RedisClient.setHeartbeat(j, st.pid, cfg.HB_TTL_SEC);
                j.hset(Keys.info(st.pid), Map.of(
                        "name", st.cfg.name,
                        "ts", Long.toString(System.currentTimeMillis()),
                        "role", st.role()
                ));
                j.publish(Channels.PRESENCE, PresenceFormatter.toPayload(j, st.leaderPid));
                Sleep.ms(cfg.PUBLISH_INTERVAL_MS);
            }
        } catch (Exception e) {
            Log.e(st, "Publisher stopped", e);
        } finally {
            try (Jedis j = RedisClient.connect(cfg)) { RedisClient.unregisterSelf(j, st.pid); }
        }
    }
}
