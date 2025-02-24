# seatunnel libs

## store-kv
### redis
```yaml

  map:
    engine*:
      map-store:
        enabled: true
        initial-mode: EAGER
        factory-class-name: tech.realcpf.imap.strong.kv.KVMapStoreFactory
        properties:
          type: redis
          kv.host: 127.0.0.1
          kv.port: 6379
          kv.password: 123456
          kv.database: 0
```