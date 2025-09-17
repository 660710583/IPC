package pubsub;

import redis.clients.jedis.Jedis;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public final class RedisClient {
    private RedisClient() {}

    public static Jedis connect(Config cfg) {
        Jedis j = new Jedis(cfg.host, cfg.port);
        if (cfg.pass != null && !cfg.pass.isEmpty()) j.auth(cfg.pass);
        return j;
    }

    public static void registerSelf(Jedis j, RuntimeState st) {
        j.zadd(Keys.Z_MEMBERS, st.pid, Long.toString(st.pid));
        Map<String,String> fields = new HashMap<>();
        fields.put("name", st.cfg.name);
        fields.put("ts", Long.toString(Instant.now().toEpochMilli()));
        fields.put("role", st.role());
        j.hset(Keys.info(st.pid), fields);
    }

    public static void unregisterSelf(Jedis j, long pid) {
        j.zrem(Keys.Z_MEMBERS, Long.toString(pid));
        j.del(Keys.info(pid));
        j.del(Keys.hb(pid));
    }

    public static Set<Long> allMemberPids(Jedis j) {
        return j.zrange(Keys.Z_MEMBERS, 0, -1)
                .stream().map(Long::parseLong)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static void setHeartbeat(Jedis j, long pid, int ttlSec) {
        j.setex(Keys.hb(pid), ttlSec, Long.toString(System.currentTimeMillis()));
    }

    public static boolean isAlive(Jedis j, long pid) {
        return j.exists(Keys.hb(pid));
    }

    public static long highestAlivePid(Jedis j, Collection<Long> pids) {
        long best = -1;
        for (long p : pids) if (isAlive(j, p)) best = Math.max(best, p);
        return best;
    }

    public static long parseLong(String s, long def) {
        try { return Long.parseLong(s); } catch (Exception e) { return def; }
    }
}
