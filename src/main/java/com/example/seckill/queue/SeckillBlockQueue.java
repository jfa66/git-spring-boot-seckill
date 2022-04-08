package com.example.seckill.queue;

import com.example.seckill.entity.SuccessKilled;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 秒杀阻塞队列，固定长度
 *
 * @author jiangfengan
 */
public class SeckillBlockQueue {
    private static final int QUEUE_MAX_SIZE = 100;
    private static BlockingQueue<SuccessKilled> blockingQueue = new LinkedBlockingQueue<>(QUEUE_MAX_SIZE);

    /**
     * 私有构造器，使外界无法直接实例化
     */
    private SeckillBlockQueue() {
    }

    /**
     * 静态内部类：与外部类无依赖关系，只有在调用时才会被加载，从而实现懒加载
     */
    private static class SingletonHolder {
        private static SeckillBlockQueue queue = new SeckillBlockQueue();
    }

    public static SeckillBlockQueue getSeckillBlockQueue() {
        return SingletonHolder.queue;
    }

    /**
     * 入队
     *
     * @param killed
     * @return
     */
    public boolean offer(SuccessKilled killed) {
        return blockingQueue.offer(killed);
    }

    /**
     * 出队
     *
     * @return
     */
    public SuccessKilled poll() {
        return blockingQueue.poll();
    }
}
