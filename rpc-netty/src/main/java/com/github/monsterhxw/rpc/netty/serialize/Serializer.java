package com.github.monsterhxw.rpc.netty.serialize;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
public interface Serializer<T> {

    int size(T entry);

    void serialize(T entry, byte[] bytes, int offset, int length);

    T deserialize(byte[] bytes, int offset, int length);

    byte type();

    Class<T> getSerializeClass();
}
