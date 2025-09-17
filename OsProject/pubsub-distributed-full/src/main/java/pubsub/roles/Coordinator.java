package pubsub.roles;

import pubsub.*;
import pubsub.util.Log;
import pubsub.util.Sleep;
import redis.clients.jedis.Jedis;

public final class Coordinator implements Runnable {
    private final Config cfg; private final RuntimeState st;
    public Coordinator(Config cfg, RuntimeState st) { this.cfg = cfg; this.st = st; }

    @Override public void run() {
        try (Jedis j = RedisClient.connect(cfg)) {
            while (st.running) {
                long now = System.currentTimeMillis();
                for (long p : RedisClient.allMemberPids(j)) {
                    String tsStr = j.hget(Keys.info(p), "ts");
                    long ts = RedisClient.parseLong(tsStr, 0L);
                    boolean hb = RedisClient.isAlive(j, p);
                    if (!hb && now - ts > cfg.REMOVE_DELAY_MS) {
                        j.publish(Channels.BROADCAST, "remove " + p);
                        RedisClient.unregisterSelf(j, p);
                    }
                }

                long newLeader = RedisClient.highestAlivePid(j, RedisClient.allMemberPids(j));
                if (newLeader != st.leaderPid) {
                    st.leaderPid = newLeader;
                    j.publish(Channels.BROADCAST, "leader " + newLeader);
                    j.publish(Channels.PRESENCE, PresenceFormatter.toPayload(j, st.leaderPid));
                }

                Sleep.ms(cfg.CLEAN_INTERVAL_MS);
            }
        } catch (Exception e) {
            Log.e(st, "Coordinator stopped", e);
        }
    }
}
