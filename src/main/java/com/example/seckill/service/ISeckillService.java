package com.example.seckill.service;

import com.example.seckill.entity.Result;
import com.example.seckill.entity.Seckill;

import java.util.List;

public interface ISeckillService {
    /**
     * 查询全部的秒杀记录
     *
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 获取库存数
     * @return
     */
    long getShopNum(Long seckillId);

    /**
     * 查询单个秒杀记录
     *
     * @param seckillId
     * @return
     */
    Seckill getById(Long seckillId);

    /**
     * 商品库存减一
     *
     * @param seckillId
     */
    int subShop(Long seckillId);

    /**
     * 秒杀一：会出现超卖情况
     *
     * @param seckillId
     * @param userId
     * @return
     */
    Result startSeckil(long seckillId, long userId);

    /**
     * 秒杀二
     * 数据库乐观锁，正常，但是当用户量少的时候会出现少买
     *
     * @param seckillId
     * @param userId
     * @return
     */
    Result startSeckilDBOCC(long seckillId, long userId);

    /**
     * 秒杀四：正常
     *
     * @param seckillId
     * @param userId
     * @return
     */
    Result startSeckilReentrantLock(long seckillId, long userId);

    /**
     * 秒杀五：正常
     *
     * @param seckillId
     * @param userId
     * @return
     */
    Result startSeckilReentrantLockAOP(long seckillId, long userId);
}
