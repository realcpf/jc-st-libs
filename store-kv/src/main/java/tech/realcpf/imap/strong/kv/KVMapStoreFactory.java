package tech.realcpf.imap.strong.kv;

import com.hazelcast.map.MapLoader;
import com.hazelcast.map.MapStoreFactory;

import java.util.Properties;

public class KVMapStoreFactory implements MapStoreFactory<Object, Object> {
    @Override
    public MapLoader<Object, Object> newMapStore(String mapName, Properties properties) {
        properties.setProperty("mapName", mapName);
        return new KVMapStore();
    }
}
