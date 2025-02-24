package tech.realcpf.imap.strong.kv.instance;

import io.lettuce.core.codec.RedisCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.apache.seatunnel.engine.serializer.api.Serializer;
import tech.realcpf.imap.strong.kv.bean.KVObjectData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class KVObjectRedisCodec implements RedisCodec<String, KVObjectData> {
    private static final byte[] EMPTY = new byte[0];
    private final Serializer serializer;
    public KVObjectRedisCodec(Serializer serializer) {
        this.serializer = serializer;
    }
    @Override
    public String decodeKey(ByteBuffer byteBuffer) {
        return Unpooled.wrappedBuffer(byteBuffer).toString(StandardCharsets.UTF_8);
    }

    @Override
    public KVObjectData decodeValue(ByteBuffer byteBuffer) {
        try {
            ByteBuf byteBuf = Unpooled.wrappedBuffer(byteBuffer);
            byte[] data = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(data);
            return serializer.deserialize(data ,KVObjectData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ByteBuffer encodeKey(String key) {
        if (key == null) {
            return ByteBuffer.wrap(EMPTY);
        } else {
            ByteBuffer buffer = ByteBuffer.allocate(ByteBufUtil.utf8MaxBytes(key));
            ByteBuf byteBuf = Unpooled.wrappedBuffer(buffer);
            byteBuf.clear();
            ByteBufUtil.writeUtf8(byteBuf,key);
            buffer.limit(byteBuf.writerIndex());
            return buffer;
        }
    }

    @Override
    public ByteBuffer encodeValue(KVObjectData kvObjectData) {
        if (kvObjectData == null) {
            return ByteBuffer.wrap(EMPTY);
        }
        try {
            byte[] data = serializer.serialize(kvObjectData);
            ByteBuffer buffer = ByteBuffer.allocate(data.length);
            ByteBuf byteBuf = Unpooled.wrappedBuffer(buffer);
            byteBuf.clear();
            byteBuf.writeBytes(data);
            buffer.limit(byteBuf.writerIndex());
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
