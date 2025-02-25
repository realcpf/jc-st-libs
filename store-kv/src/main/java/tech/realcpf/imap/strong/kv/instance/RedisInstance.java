package tech.realcpf.imap.strong.kv.instance;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.realcpf.imap.strong.kv.bean.KVObjectData;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static tech.realcpf.imap.strong.kv.common.KVConstants.SEARCH_KEY_FORMATTER;

public class RedisInstance implements KVInstance {
    private static final String REDIS_SINGLE_URL = "kv.host";
    private static final String REDIS_PASSWORD = "kv.password";
    private static final String REDIS_PORT = "kv.port";
    private static final String REDIS_DATABASE = "kv.database";
    private RedisAsyncCommands<String, KVObjectData> commands;
    private RedisClient redisClient;
    private StatefulRedisConnection<String, KVObjectData> connection;

    private static final Logger LOG = LoggerFactory.getLogger(RedisInstance.class);

    @Override
    public void build(Map<String, Object> prop) {
        io.lettuce.core.RedisURI redisURI = RedisURI.Builder
                .redis((String) prop.get(REDIS_SINGLE_URL))
                .withPort(Integer.parseInt(prop.get(REDIS_PORT).toString()))
                .withPassword(prop.get(REDIS_PASSWORD).toString().toCharArray())
                .withDatabase(Integer.parseInt(prop.get(REDIS_DATABASE).toString()))
                .build();
        redisClient = RedisClient.create(redisURI);
        connection = redisClient.connect(new KVObjectRedisCodec(serializer));
        commands = connection.async();
    }

    @Override
    public boolean set(Object key, Object value, String mapName) {
        LOG.debug("redis set {} {} {}",key,value,mapName);
        String strKey = formatKeyStr(key, mapName, KEY_FLAG);
        String strValue = formatKeyStr(key, mapName, VALUE_FLAG);
        KVObjectData objValue = encodeObject(value);
        KVObjectData objKey = encodeObject(key);
        commands.set(strKey, objKey);
        commands.set(strValue, objValue);
        return Boolean.TRUE;
    }

    @Override
    public Object get(Object key, String mapName) {
        LOG.debug("redis get {} {}",key,mapName);
        String strValue = formatKeyStr(key, mapName, VALUE_FLAG);
        RedisFuture<KVObjectData> objValue = commands.get(strValue);
        try {
            KVObjectData innerData = objValue.get(5,TimeUnit.SECONDS);
            if (Objects.isNull(innerData)) {
                return null;
            }
            return serializer.deserialize(innerData.getData(), ClassUtils.getClass(innerData.getClassName()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean del(Object key, String mapName) {
        LOG.debug("redis del {} {}",key,mapName);
        String strKey = formatKeyStr(key, mapName, KEY_FLAG);
        String strValue = formatKeyStr(key, mapName, VALUE_FLAG);
        commands.del(strKey, strValue);
        return Boolean.TRUE;
    }

    @Override
    public List<Object> keys(String mapName) {
        LOG.debug("redis keys {}",mapName);
        RedisFuture<List<String>> keys = commands.keys(String.format(SEARCH_KEY_FORMATTER, mapName, KEY_FLAG));
        try {
            List<Object> objKeys = new LinkedList<>();
            List<String> innerKeys = keys.get(5,TimeUnit.SECONDS);
            if (Objects.isNull(innerKeys)) {
                return objKeys;
            }
            for (String k : innerKeys) {
                RedisFuture<KVObjectData> objKey = commands.get(k);
                KVObjectData innerObjKey = objKey.get(5,TimeUnit.SECONDS);
                if (Objects.isNull(innerObjKey)) {
                    continue;
                }
                objKeys.add(serializer.deserialize(innerObjKey.getData(), ClassUtils.getClass(innerObjKey.getClassName())));
            }
            LOG.debug("keys load {} return size {}",innerKeys.size(),objKeys.size());
            return objKeys;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void shutdown() {
        if (Objects.nonNull(commands)) {
            connection.close();
            redisClient.shutdown();
        }
    }


}
