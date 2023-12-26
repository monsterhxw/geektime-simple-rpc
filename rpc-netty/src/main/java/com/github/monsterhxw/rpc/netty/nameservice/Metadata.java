package com.github.monsterhxw.rpc.netty.nameservice;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

/**
 * @author huangxuewei
 * @since 2023/12/27
 */
public class Metadata extends HashMap<String/*serviceName*/, List<URI>/*uri list*/> {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Metadata:").append("\n");
        for (Entry<String, List<URI>> entry : entrySet()) {
            sb.append("\t").append("Classname: ")
                    .append(entry.getKey()).append("\n");
            sb.append("\t").append("URIs:").append("\n");
            for (URI uri : entry.getValue()) {
                sb.append("\t\t").append(uri).append("\n");
            }
        }
        return sb.toString();
    }
}
