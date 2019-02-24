package com.mars.zookeeperdemo04.node;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class CuratorCrud {

    static final String ipAddr = "192.168.59.129,192.168.59.130,192.168.59.131";

    public static CuratorFramework getConnectionClient() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(ipAddr)
                .sessionTimeoutMs(1000)
                .retryPolicy(retryPolicy)
                .build();
        client.start();
        return client;
    }

    public static void create(CuratorFramework client,String path,byte[] data) throws Exception {
        client.create().forPath(path,data);
    }
}
