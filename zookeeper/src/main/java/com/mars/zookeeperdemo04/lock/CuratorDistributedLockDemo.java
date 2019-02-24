package com.mars.zookeeperdemo04.lock;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class CuratorDistributedLockDemo {

    static String ipAddr = "192.168.59.129,192.168.59.130,192.168.59.131";
    public static CuratorFramework getConnectionClient() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(ipAddr)
                .sessionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .build();
        client.start();
        return client;
    }

    public static void main(String[] args) {
        CuratorFramework framework = CuratorDistributedLockDemo.getConnectionClient();
        InterProcessMutex interProcessMutex =new InterProcessMutex(framework,"/locks");
        try {
            interProcessMutex.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
