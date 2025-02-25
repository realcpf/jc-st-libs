package tech.realcpf.imap.strong.kv.instance;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import org.apache.commons.lang3.ClassUtils;
import org.apache.seatunnel.engine.serializer.api.Serializer;
import tech.realcpf.imap.strong.kv.bean.KVObjectData;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EtcdInstance implements KVInstance{
    private static final String ETCD_EDN_POINTS = "kv.host";
    private static final String ETCD_AUTH = "kv.passwd";
    private KV kv;
    private Client client;
    private Serializer serializer;
    @Override
    public void build(Map<String, Object> prop) {
        String hosts = String.valueOf(prop.getOrDefault(ETCD_EDN_POINTS,""));
        String[] hostArr = hosts.split(",");
        String auth = (String) prop.getOrDefault(ETCD_AUTH,"");
        this.client = Client.builder().endpoints(hostArr)
                .authority(auth).build();
        this.kv = client.getKVClient();
    }

    @Override
    public boolean set(Object key, Object value, String mapName) {
        String strKey = formatKeyStr(key,mapName,KEY_FLAG);
        String strValue = formatKeyStr(key,mapName,VALUE_FLAG);
        try {
            byte[] objKey = serializer.serialize(encodeObject(key));
            CompletableFuture<PutResponse> respKey =
                    kv.put(ByteSequence.from(strKey, StandardCharsets.UTF_8),ByteSequence.from(objKey));
            byte[] objValue = serializer.serialize(encodeObject(value));
            CompletableFuture<PutResponse> respValue =
                    kv.put(ByteSequence.from(strValue, StandardCharsets.UTF_8),ByteSequence.from(objValue));
            respKey.get(5,TimeUnit.SECONDS);
            respValue.get(5,TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Boolean.TRUE;
    }

    @Override
    public Object get(Object key, String mapName) {
        String strKey = formatKeyStr(key,mapName,VALUE_FLAG);
        CompletableFuture<GetResponse> resp = kv.get(ByteSequence.from(strKey,StandardCharsets.UTF_8));
        try {
            GetResponse response = resp.get(5,TimeUnit.SECONDS);
            if (Objects.isNull(response)) {
                return null;
            }
            byte[] valueData = response.getKvs().get(0).getValue().getBytes();
            KVObjectData kvObjectData = serializer.deserialize(valueData, KVObjectData.class);
            return serializer.deserialize(kvObjectData.getData(), ClassUtils.getClass(kvObjectData.getClassName()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean del(Object key, String mapName) {
        return false;
    }

    @Override
    public List<Object> keys(String mapName) {
        return Collections.emptyList();
    }

    @Override
    public void shutdown() {
        kv.close();
        client.close();
    }
}
