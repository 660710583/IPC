package pubsub.roles;

import pubsub.*;
import pubsub.util.Log;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public final class Subscriber implements Runnable {
    private final Config cfg; private final RuntimeState st;
    public Subscriber(Config cfg, RuntimeState st) { this.cfg = cfg; this.st = st; }

    @Override public void run() {
        try (Jedis j = RedisClient.connect(cfg)) {
            JedisPubSub sub = new JedisPubSub() {
                @Override public void onMessage(String ch, String msg) {
                    switch (ch) {
                        case Channels.BROADCAST:
                            Log.i(st, "BRC", msg); break;
                        case Channels.PRESENCE:
                            String[] parts = msg.split("\\|", 2);
                            if (parts.length == 2) {
                                try { st.leaderPid = Long.parseLong(parts[0]); } catch (NumberFormatException ignore) {}
                            }
                            Log.i(st, "PRS", msg);
                            break;
                        case Channels.CONTROL:
                            Log.i(st, "CTL", msg);
                            if (msg.equals("kill " + st.pid) || msg.equals("kill self")) {
                                st.running = false;
                            }
                            break;
                    }
                }
            };
            j.subscribe(sub, Channels.BROADCAST, Channels.PRESENCE, Channels.CONTROL);
        } catch (Exception e) {
            Log.e(st, "Subscriber stopped", e);
        }
    }
}
