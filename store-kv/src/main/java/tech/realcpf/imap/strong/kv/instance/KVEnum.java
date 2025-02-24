package tech.realcpf.imap.strong.kv.instance;

public enum KVEnum {


    REDIS("redis",new RedisInstance());

    private final String provider;
    private final KVInstance kvInstance;
    KVEnum(String provider, KVInstance kvInstance) {
        this.provider = provider;
        this.kvInstance = kvInstance;
    }

    public KVInstance getKvInstance() {
        return kvInstance;
    }

    public String getProvider() {
        return provider;
    }
    public static KVEnum of(String type) {
        if (REDIS.provider.equals(type)) {
            return REDIS;
        }
        return null;
    }
}
