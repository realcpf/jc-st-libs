package tech.realcpf.imap.strong.kv;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.MapLoaderLifecycleSupport;
import com.hazelcast.map.MapStore;
import lombok.SneakyThrows;
import org.apache.seatunnel.engine.common.utils.FactoryUtil;
import org.apache.seatunnel.engine.imap.storage.api.IMapStorage;
import org.apache.seatunnel.engine.imap.storage.api.IMapStorageFactory;
import org.apache.seatunnel.shade.com.google.common.collect.Maps;

import java.util.*;

public class KVMapStore implements MapStore<Object, Object>, MapLoaderLifecycleSupport {
    private IMapStorage mapStorage;

    @Override
    public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
        Map<String, Object> initMap = new HashMap<>(Maps.fromProperties(properties));
        this.mapStorage =
                FactoryUtil.discoverFactory(
                                Thread.currentThread().getContextClassLoader(),
                                IMapStorageFactory.class,
                                (String) initMap.get("type"))
                        .create(initMap);
    }

    @Override
    public void destroy() {
        mapStorage.destroy(false);
    }

    @Override
    public void store(Object key, Object value) {
        mapStorage.store(key, value);
    }

    @Override
    public void storeAll(Map<Object, Object> map) {
        mapStorage.storeAll(map);
    }

    @Override
    public void delete(Object key) {
        mapStorage.delete(key);
    }

    @Override
    public void deleteAll(Collection<Object> keys) {
        mapStorage.deleteAll(keys);
    }
    @SneakyThrows
    @Override
    public Object load(Object key) {
        if (mapStorage instanceof IMapKVStorage) {
            IMapKVStorage kvStorage = (IMapKVStorage) mapStorage;
            return kvStorage.load(key);
        }
        return null;
    }

    @SneakyThrows
    @Override
    public Map<Object, Object> loadAll(Collection<Object> keys) {
        Map<Object, Object> allMap = mapStorage.loadAll();
        Map<Object, Object> retMap = new HashMap<>();
        keys.forEach(key -> retMap.put(key, allMap.get(key)));

        return Collections.unmodifiableMap(retMap);
    }

    @Override
    public Iterable<Object> loadAllKeys() {
        return mapStorage.loadAllKeys();
    }
}
