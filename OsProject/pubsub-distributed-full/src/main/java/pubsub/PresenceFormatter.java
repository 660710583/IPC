package pubsub;

import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PresenceFormatter {
    private PresenceFormatter() {}

    public static String toPayload(Jedis j, long leaderPid) {
        List<String> parts = new ArrayList<>();
        for (long p : RedisClient.allMemberPids(j)) {
            String name = Optional.ofNullable(j.hget(Keys.info(p), "name")).orElse("?");
            boolean alive = RedisClient.isAlive(j, p);
            parts.add(p + ":" + name + ":" + (alive ? "alive" : "dead"));
        }
        return leaderPid + "|" + String.join(",", parts);
    }
}
