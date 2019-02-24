package com.mars.zookeeperdemo04.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 分布式锁
 */
public class DistributedDemoLock implements Lock, Watcher {

    private ZooKeeper zooKeeper;
    private String ROOT_LOCK ="/locks";
    private String WAT_LOCK; // 等待前一个锁
    private String CURRENT_LOCK; // 表示当前锁
    private CountDownLatch countDownLatch;
    private int sessionTimeOut = 4000;

    public String ipAddr = "192.168.59.129,192.168.59.130,192.168.59.131";

    public DistributedDemoLock() {
        try {
            zooKeeper = new ZooKeeper(ipAddr,sessionTimeOut,this);
            // 判断当前的根节点是否存在
            Stat stat = zooKeeper.exists(ROOT_LOCK,false);
            if(stat == null){
                // 创建一个持久化的根节点
                zooKeeper.create(ROOT_LOCK,"0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void lock() {
        if(this.tryLock()) {
            System.out.println(Thread.currentThread().getName()+"->"+CURRENT_LOCK+"获得锁成功");
            return;
        }
        // 没有获得锁，继续等待获得锁
        try {
            waitForLock(WAT_LOCK);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean waitForLock(String prev) throws KeeperException, InterruptedException {

        // 监听当前节点的上一节点
        Stat stat = zooKeeper.exists(prev,true);
        if(stat != null) {
            System.out.println(Thread.currentThread().getName()+"等待锁"+ROOT_LOCK+"/"+prev+"释放");
            countDownLatch = new CountDownLatch(1);
            countDownLatch.await();
            System.out.println(Thread.currentThread().getName()+"获得锁成功");
        }
        return true;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        try {
            // 创建临时有序节点
            CURRENT_LOCK = zooKeeper.create(ROOT_LOCK+"/","0".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println(Thread.currentThread().getName()+"-->"+CURRENT_LOCK+",尝试竞争锁");

            // 获取根节点的子节点
           List<String> childrenList = zooKeeper.getChildren(ROOT_LOCK,false);
           // 定义一个有序的集合进行排序
            SortedSet<String> sortedSet = new TreeSet<>();
            for (String children:childrenList) {
                sortedSet.add(ROOT_LOCK+"/"+children);
            }
            // 获取最小的节点
            String firstNode = sortedSet.first();
            SortedSet<String> lessThenMe =((TreeSet)sortedSet).headSet(CURRENT_LOCK);
            // 判断获得节点是否是最小的节点
            // 通过当前节点和子节点中最小的节点进行比较,如果相等，表示获得锁成功
            if (CURRENT_LOCK.equals(firstNode)) {
                return true;
            }
            if(!lessThenMe.isEmpty()){
                // 获取比当前节点更小的最后一个节点，设置给WAIT_LOCk
                WAT_LOCK = lessThenMe.last();
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }


    @Override
    public void unlock() {
        System.out.println(Thread.currentThread().getName()+"->释放锁"+CURRENT_LOCK);
        try {
            zooKeeper.delete(CURRENT_LOCK,-1);
            CURRENT_LOCK = null;
            zooKeeper.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (this.countDownLatch != null) {
            this.countDownLatch.countDown();
        }
    }
}
