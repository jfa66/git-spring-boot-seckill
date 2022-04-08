package com.example.seckill.controller;


import com.example.seckill.aop.TimeAop;
import com.example.seckill.entity.Result;
import com.example.seckill.entity.SuccessKilled;
import com.example.seckill.enums.SeckillStatEnum;
import com.example.seckill.queue.SeckillBlockQueue;
import com.example.seckill.redis.RedisHelper;
import com.example.seckill.service.ISeckillService;
import com.example.seckill.service.ISucSeckillService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author jiangfengan
 * <p>
 * 秒杀一(超卖)
 * 最简单的查询更新操作，不涉及各种锁，会出现超卖情况。
 * <p>
 * 秒杀二(正常)
 * 数据库乐观锁，通过判断版本号值来控制，当并发量少时会出现少买
 * <p>
 * 秒杀三(少买)
 * 基于阻塞队列LinkedBlockingQueue实现，同步生产、消费
 * <p>
 * 秒杀四(正常)
 * 基于锁ReentrantLock实现(锁必须上移到事务之前，否则会出现超卖情况)
 * <p>
 * 秒杀五(正常)
 * 基于ReentrantLock+AOP实现
 * <p>
 * 秒杀六(正常)
 * 基于redis实现，并发量少会出现少买
 * <p>
 * 秒杀七(超卖)
 * 基于redis list实现，入队之前没有做原子处理，导致出现超卖情况，应该使用lua脚本实现
 */
@Api(tags = "秒杀")
@RestController
@RequestMapping("/seckill")
public class SeckillController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillController.class);

    /**
     * 获取java虚拟机可用处理器数量
     */
    private static int corePoolSize = Runtime.getRuntime().availableProcessors();

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, corePoolSize, 1L
            , TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    @Autowired
    private ISucSeckillService sucSeckillService;

    @Autowired
    private ISeckillService seckillService;

    @ApiOperation(value = "秒杀一，无锁情况，会出现超卖", nickname = "Java笔记")
    @PostMapping("kill01")
    public Result kill01(long seckillId) {
        final int skillNum = 1000000;
        final CountDownLatch latch = new CountDownLatch(skillNum);
        final long killId = seckillId;
        //初始化100个商品
        sucSeckillService.delSucSeckill(killId);
        for (int i = 0; i < skillNum; i++) {
            final long userId = i;
            executor.execute(() -> {
                Result result = seckillService.startSeckil(killId, userId);
                LOGGER.info("用户:{}{}", userId, result.get("msg"));
                latch.countDown();
            });
        }
        try {
            latch.await();
            Long sucKillCount = sucSeckillService.getSucSeckillCount(killId);
            LOGGER.info("一共秒杀{}件商品", sucKillCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.error(SeckillStatEnum.INNER_ERROR.getInfo());
        }
        return Result.ok();
    }

    @ApiOperation(value = "秒杀二，数据库乐观锁，并发量少时会出现少买", nickname = "Java笔记")
    @PostMapping("kill02")
    @TimeAop
    public Result kill02(long seckillId) {
        final int skillNum = 1000000;
        final CountDownLatch latch = new CountDownLatch(skillNum);
        final long killId = seckillId;
        //初始化100个商品
        sucSeckillService.delSucSeckill(killId);
        for (int i = 0; i < skillNum; i++) {
            final long userId = i;
            executor.execute(() -> {
                Result result = seckillService.startSeckilDBOCC(killId, userId);
                LOGGER.info("用户:{}{}", userId, result.get("msg"));
                latch.countDown();
            });
        }
        try {
            latch.await();
            Long sucKillCount = sucSeckillService.getSucSeckillCount(killId);
            LOGGER.info("一共秒杀{}件商品", sucKillCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.error(SeckillStatEnum.INNER_ERROR.getInfo());
        }
        return Result.ok();
    }

    @ApiOperation(value = "秒杀三，阻塞队列实现", nickname = "Java笔记")
    @PostMapping("kill03")
    @TimeAop
    public Result kill03(long seckillId) {
        final int skillNum = 1000000;
        final CountDownLatch latch = new CountDownLatch(skillNum);
        final long killId = seckillId;
        //初始化100个商品
        sucSeckillService.delSucSeckill(killId);
        //初始化队列
        SeckillBlockQueue blockQueue = SeckillBlockQueue.getSeckillBlockQueue();
        //秒杀入队
        for (int i = 0; i < skillNum; i++) {
            final long userId = i;
            executor.execute(() -> {
                SuccessKilled successKilled = new SuccessKilled();
                successKilled.setSeckillId(killId);
                successKilled.setUserId(userId);
                boolean result = blockQueue.offer(successKilled);
                if (result) {
                    LOGGER.info("用户:{}{}", userId, "秒杀成功");
                } else {
                    LOGGER.info("用户:{}{}", userId, "秒杀失败");
                }
                latch.countDown();
            });
        }
        try {
            latch.await();
            //秒杀出队
            SuccessKilled successKilled = null;
            while ((successKilled = blockQueue.poll()) != null) {
                seckillService.subShop(killId);
                //创建订单
                successKilled.setState("0");
                successKilled.setCreateTime(new Timestamp(System.currentTimeMillis()));
                //入库
                sucSeckillService.saveSucSeckill(successKilled);
            }
            Long sucKillCount = sucSeckillService.getSucSeckillCount(killId);
            LOGGER.info("一共秒杀{}件商品", sucKillCount);
            return Result.ok();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.error(SeckillStatEnum.INNER_ERROR.getInfo());
        }
    }

    @ApiOperation(value = "秒杀四，ReentrantLock锁", nickname = "Java笔记")
    @PostMapping("kill04")
    @TimeAop
    public Result kill04(long seckillId) {
        final int skillNum = 1000;
        final CountDownLatch latch = new CountDownLatch(skillNum);
        final long killId = seckillId;
        //初始化100个商品
        sucSeckillService.delSucSeckill(killId);
        ReentrantLock reentrantLock = new ReentrantLock();
        for (int i = 0; i < skillNum; i++) {
            final long userId = i;
            executor.execute(() -> {
                reentrantLock.lock();
                Result result;
                try {
                    result = seckillService.startSeckilReentrantLock(killId, userId);
                } finally {
                    reentrantLock.unlock();
                }
                LOGGER.info("用户:{}{}", userId, result.get("msg"));
                latch.countDown();
            });
        }
        try {
            latch.await();
            Long sucKillCount = sucSeckillService.getSucSeckillCount(killId);
            LOGGER.info("一共秒杀{}件商品", sucKillCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.error(SeckillStatEnum.INNER_ERROR.getInfo());
        }
        return Result.ok();
    }

    @ApiOperation(value = "秒杀五，ReentrantLock+AOP", nickname = "Java笔记")
    @PostMapping("kill05")
    @TimeAop
    public Result kill05(long seckillId) {
        final int skillNum = 1000000;
        final CountDownLatch latch = new CountDownLatch(skillNum);
        final long killId = seckillId;
        //初始化100个商品
        sucSeckillService.delSucSeckill(killId);
        for (int i = 0; i < skillNum; i++) {
            final long userId = i;
            executor.execute(() -> {
                Result result = seckillService.startSeckilReentrantLockAOP(killId, userId);
                LOGGER.info("用户:{}{}", userId, result.get("msg"));
                latch.countDown();
            });
        }
        try {
            latch.await();
            Long sucKillCount = sucSeckillService.getSucSeckillCount(killId);
            LOGGER.info("一共秒杀{}件商品", sucKillCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.error(SeckillStatEnum.INNER_ERROR.getInfo());
        }
        return Result.ok();
    }

    @Autowired
    private RedisHelper redisHelper;

    @ApiOperation(value = "秒杀六，基于redis实现，并发量少会出现少买", nickname = "Java笔记")
    @PostMapping("kill06")
    @TimeAop
    public Result kill06(long seckillId) {
        final int skillNum = 1000000;
        final CountDownLatch latch = new CountDownLatch(skillNum);
        final long killId = seckillId;
        //初始化100个商品
        sucSeckillService.delSucSeckill(killId);
        for (int i = 0; i < skillNum; i++) {
            final long userId = i;
            String v = new Random().nextLong() + "";
            executor.execute(() -> {
                if (redisHelper.lock("lock", v)) {
                    Result result = seckillService.startSeckil(killId, userId);
                    redisHelper.unlock("lock", v);
                    LOGGER.info("用户:{}{}", userId, result.get("msg"));
                }
                latch.countDown();
            });
        }
        try {
            latch.await();
            Long sucKillCount = sucSeckillService.getSucSeckillCount(killId);
            LOGGER.info("一共秒杀{}件商品", sucKillCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.error(SeckillStatEnum.INNER_ERROR.getInfo());
        }
        return Result.ok();
    }

    @ApiOperation(value = "秒杀七，基于redis消息队列实现", nickname = "Java笔记")
    @PostMapping("kill07")
    @TimeAop
    public Result kill07(long seckillId) {
        final int skillNum = 1000000;
        final CountDownLatch latch = new CountDownLatch(skillNum);
        final long killId = seckillId;
        final String k="kill_list";
        //初始化100个商品
        sucSeckillService.delSucSeckill(killId);
        int num=(int) seckillService.getShopNum(killId);
        //秒杀入队
        for (int i = 0; i < skillNum; i++) {
            final long userId = i;
            executor.execute(() -> {
                SuccessKilled successKilled = new SuccessKilled();
                successKilled.setSeckillId(killId);
                successKilled.setUserId(userId);
                boolean result = redisHelper.push(num,k,successKilled);
                if (result) {
                    LOGGER.info("用户:{}{}", userId, "秒杀成功");
                } else {
                    LOGGER.info("用户:{}{}", userId, "秒杀失败");
                }
                latch.countDown();
            });
        }
        try {
            latch.await();
            //秒杀出队
            SuccessKilled successKilled = null;
            while ((successKilled = (SuccessKilled)redisHelper.pop(k)) != null) {
                //减库存
                seckillService.subShop(killId);
                //创建订单
                successKilled.setState("0");
                successKilled.setCreateTime(new Timestamp(System.currentTimeMillis()));
                //入库
                sucSeckillService.saveSucSeckill(successKilled);
            }
            Long sucKillCount = sucSeckillService.getSucSeckillCount(killId);
            LOGGER.info("一共秒杀{}件商品", sucKillCount);
            redisHelper.delKey(k);
            return Result.ok();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.error(SeckillStatEnum.INNER_ERROR.getInfo());
        }
    }
}
