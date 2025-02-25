package tech.realcpf.imap.strong.kv;

import com.google.auto.service.AutoService;
import org.apache.seatunnel.engine.imap.storage.api.IMapStorage;
import org.apache.seatunnel.engine.imap.storage.api.IMapStorageFactory;
import org.apache.seatunnel.engine.imap.storage.api.exception.IMapStorageException;

import java.util.Map;

@AutoService(IMapStorageFactory.class)
public class IMapKVStorageFactory implements IMapStorageFactory {
    @Override
    public String factoryIdentifier() {
        return "kv";
    }

    @Override
    public IMapStorage create(Map<String, Object> map) throws IMapStorageException {
        IMapKVStorage iMapKVStorage = new IMapKVStorage();
        iMapKVStorage.initialize(map);
        return iMapKVStorage;
    }
}
