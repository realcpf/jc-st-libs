package tech.realcpf.imap.strong.kv;

import org.apache.seatunnel.engine.imap.storage.api.IMapStorage;
import tech.realcpf.imap.strong.kv.instance.KVEnum;
import tech.realcpf.imap.strong.kv.instance.KVInstance;

import java.io.IOException;
import java.util.*;

public class IMapKVStorage implements IMapStorage {
    private KVInstance instance;

    private String mapName;
    private static final String KV_PROVIDER_KEY = "kv.provider";

    private static final String MAP_NAME_KEY = "mapName";

    @Override
    public void initialize(Map<String, Object> map) {

        String provider = String.valueOf(map.getOrDefault(KV_PROVIDER_KEY,
                KVEnum.REDIS.getProvider()));
        KVEnum kvEnum = KVEnum.of(provider);
        if (Objects.isNull(kvEnum)) {
            throw new IllegalArgumentException(String.format("kv.provider %s is not support", provider));
        }
        this.instance = kvEnum.getKvInstance();
        this.mapName = (String) map.get(MAP_NAME_KEY);
        this.instance.build(map);
    }

    @Override
    public boolean store(Object key, Object value) {
        return instance.set(key,value,mapName);
    }

    @Override
    public Set<Object> storeAll(Map<Object, Object> map) {
        Set<Object> failures = new HashSet<>();
        for (Map.Entry<Object,Object> entry:map.entrySet()) {
            boolean success = instance.set(entry.getKey(),entry.getValue(),mapName);
            if (!success) {
                failures.add(entry.getKey());
            }
        }
        return failures;
    }

    @Override
    public boolean delete(Object key) {
        return instance.del(key,mapName);
    }

    @Override
    public Set<Object> deleteAll(Collection<Object> keys) {
        Set<Object> failures = new HashSet<>();
        for (Object key:keys) {
            boolean success = instance.del(key,mapName);
            if (!success) {
                failures.add(key);
            }
        }
        return failures;
    }

    public Object load(Object key) {
        return instance.get(key,mapName);
    }

    @Override
    public Map<Object, Object> loadAll() throws IOException {
        Set<Object> keys = loadAllKeys();
        Map<Object,Object> maps = new HashMap<>(keys.size());
        for (Object key:keys) {
            maps.put(key,instance.get(key,mapName));
        }
        return maps;
    }

    @Override
    public Set<Object> loadAllKeys() {
        return new HashSet<>(instance.keys(mapName));
    }

    @Override
    public void destroy(boolean b) {
        if (b) {
            Set<Object> keys = loadAllKeys();
            for (Object key:keys) {
                instance.del(key,mapName);
            }
        }
        instance.shutdown();
    }
}
