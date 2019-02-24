package com.mars.zookeeperdemo04.node;

import org.apache.curator.framework.CuratorFramework;

public class Test {

    public static void main(String[] args) {
        CuratorFramework client =  CuratorCrud.getConnectionClient();
        try {
            String path = "/curatorTest";
            String data = "2019";
            CuratorCrud.create(client,path,data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            client.close();
        }
    }
}
