package com.github.monsterhxw.rpc.netty.serialize;

import com.github.monsterhxw.rpc.api.spi.ServiceSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
public class SerializeSupport {

    private static final Logger LOG = LoggerFactory.getLogger(SerializeSupport.class);

    private static Map<Class<?>/*序列化对象类型*/, Serializer<?>/*序列化实现*/> SERIALIZER_MAP = new HashMap<>();

    private static Map<Byte/*序列化实现类型*/, Class<?>/*序列化对象类型*/> TYPE_MAP = new HashMap<>();

    static {
        Collection<Serializer> serializers = ServiceSupport.loadAll(Serializer.class);
        for (Serializer serializer : serializers) {
            registerSerializerAndType(serializer.type(), serializer.getSerializeClass(), serializer);
            LOG.info("Found serializer, class: {}, type: {}.", serializer.getSerializeClass().getCanonicalName(), serializer.type());
        }
    }

    private static void registerSerializerAndType(byte type, Class serializeClass, Serializer serializer) {
        SERIALIZER_MAP.put(serializeClass, serializer);
        TYPE_MAP.put(type, serializeClass);
    }

    public static <E> E deserialize(byte[] buffer, int offset, int length, Class<E> clazz) {
        Object entry = SERIALIZER_MAP.get(clazz).deserialize(buffer, offset, length);
        if (clazz.isAssignableFrom(entry.getClass())) {
            return (E) entry;
        }
        throw new SerializeException("Deserialize error, expect class: " + clazz.getCanonicalName() + ", actual class: " + entry.getClass().getCanonicalName());
    }

    public static <E> E deserialize(byte[] buffer, int offset, int length) {
        byte type = parseEntryType(buffer);
        @SuppressWarnings("unchecked")
        Class<E> clazz = (Class<E>) TYPE_MAP.get(type);
        if (null != clazz) {
            return deserialize(buffer, offset + 1, length - 1, clazz);
        }
        throw new SerializeException("Deserialize error, type: " + type);
    }

    public static <E> E deserialize(byte[] buffer) {
        return deserialize(buffer, 0, buffer.length);
    }

    public static <E> byte[] serialize(E entry) {
        @SuppressWarnings("unchecked")
        Serializer<E> serializer = (Serializer<E>) SERIALIZER_MAP.get(entry.getClass());
        if (null != serializer) {
            byte[] bytes = new byte[serializer.size(entry) + 1];
            bytes[0] = serializer.type();
            serializer.serialize(entry, bytes, 1, bytes.length - 1);
            return bytes;
        }
        throw new SerializeException("Serialize error, class: " + entry.getClass().getCanonicalName());
    }

    private static byte parseEntryType(byte[] buffer) {
        return buffer[0];
    }
}
