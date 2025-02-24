package tech.realcpf.imap.strong.kv;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.MapLoaderLifecycleSupport;
import com.hazelcast.map.MapStore;
import org.apache.seatunnel.shade.com.google.common.collect.Maps;
import tech.realcpf.imap.strong.kv.instance.KVEnum;
import tech.realcpf.imap.strong.kv.instance.KVInstance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class KVMapStore implements MapStore<Object, Object>, MapLoaderLifecycleSupport {

    private KVInstance instance;

    private String mapName;

    @Override
    public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
        Map<String, Object> initMap = new HashMap<>(Maps.fromProperties(properties));
        this.mapName = mapName;
        instance = KVEnum.of((String) initMap.get("type")).getKvInstance();
        instance.build(initMap);
    }

    @Override
    public void destroy() {
        instance.shutdown();
    }

    @Override
    public void store(Object key, Object value) {
        instance.set(key,value,mapName);
    }

    @Override
    public void storeAll(Map<Object, Object> map) {
        for (Map.Entry<Object,Object> entry:map.entrySet()) {
            instance.set(entry.getKey(),entry.getValue(),mapName);
        }
    }

    @Override
    public void delete(Object key) {
        instance.del(key,mapName);
    }

    @Override
    public void deleteAll(Collection<Object> keys) {
        for (Object key:keys) {
            instance.del(key,mapName);
        }
    }

    @Override
    public Object load(Object key) {
        return instance.get(key,mapName);
    }

    @Override
    public Map<Object, Object> loadAll(Collection<Object> keys) {
        Map<Object,Object> maps = new HashMap<>(keys.size());
        for (Object key:keys) {
            maps.put(key,instance.get(key,mapName));
        }
        return maps;
    }

    @Override
    public Iterable<Object> loadAllKeys() {
        return instance.keys(mapName);
    }
}
