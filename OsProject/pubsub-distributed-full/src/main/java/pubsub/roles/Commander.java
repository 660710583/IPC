package pubsub.roles;

import pubsub.*;
import pubsub.util.Log;
import pubsub.util.Sleep;
import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class Commander implements Runnable {
    private final Config cfg; private final RuntimeState st;
    public Commander(Config cfg, RuntimeState st) { this.cfg = cfg; this.st = st; }

    @Override public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
             Jedis j = RedisClient.connect(cfg)) {
            while (st.running) {
                String line = br.readLine();
                if (line == null) { Sleep.ms(100); continue; }
                line = line.trim();
                if (line.isEmpty()) continue;

                if (st.pid != st.leaderPid) {
                    Log.i(st, "CMD", "ignored (not leader): " + line);
                    continue;
                }

                if (line.equalsIgnoreCase("kill self")) {
                    j.publish(Channels.CONTROL, "kill " + st.pid);
                } else if (line.equalsIgnoreCase("kill random")) {
                    List<Long> pids = new ArrayList<>(RedisClient.allMemberPids(j));
                    if (!pids.isEmpty()) {
                        long target = pids.get(new Random().nextInt(pids.size()));
                        j.publish(Channels.CONTROL, "kill " + target);
                    }
                } else if (line.startsWith("kill ")) {
                    j.publish(Channels.CONTROL, line);
                } else if (line.equalsIgnoreCase("who")) {
                    j.publish(Channels.BROADCAST, "members " + RedisClient.allMemberPids(j));
                } else if (line.equalsIgnoreCase("leader")) {
                    j.publish(Channels.BROADCAST, "leader " + st.leaderPid);
                } else {
                    j.publish(Channels.BROADCAST, "echo " + line);
                }
            }
        } catch (Exception e) {
            Log.e(st, "Commander stopped", e);
        }
    }
}
