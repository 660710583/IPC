package pubsub;

import java.util.HashMap;
import java.util.Map;

public final class Config {
    public final String host;
    public final int port;
    public final String pass;   // may be null/empty
    public final String name;   // display name

    public final int HB_TTL_SEC = 3;
    public final long PUBLISH_INTERVAL_MS = 1000;
    public final long CLEAN_INTERVAL_MS   = 1000;
    public final long REMOVE_DELAY_MS     = 20_000;

    public Config(String host, int port, String pass, String name) {
        this.host = host;
        this.port = port;
        this.pass = pass;
        this.name = name;
    }

    public static Config fromArgs(String[] args) {
        String host = "127.0.0.1";
        int    port = 6379;
        String pass = null;
        String name = "unknown";

        Map<String,String> m = new HashMap<>();
        for (int i = 0; i + 1 < args.length; i += 2) {
            if (args[i].startsWith("--")) m.put(args[i], args[i+1]);
        }
        if (m.containsKey("--host")) host = m.get("--host");
        if (m.containsKey("--port")) port = Integer.parseInt(m.get("--port"));
        if (m.containsKey("--pass")) pass = m.get("--pass");
        if (m.containsKey("--name")) name = m.get("--name");
        return new Config(host, port, pass, name);
    }
}