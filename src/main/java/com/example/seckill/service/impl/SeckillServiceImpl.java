package com.example.seckill.service.impl;

import com.example.seckill.aop.Servicelock;
import com.example.seckill.entity.Result;
import com.example.seckill.entity.Seckill;
import com.example.seckill.entity.SuccessKilled;
import com.example.seckill.enums.SeckillStatEnum;
import com.example.seckill.mapper.SeckillMapper;
import com.example.seckill.mapper.SucKilledMapper;
import com.example.seckill.service.ISeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author jiangfengan
 */
@Service
public class SeckillServiceImpl implements ISeckillService {

    @Autowired
    private SeckillMapper seckillMapper;

    @Autowired
    private SucKilledMapper sucKilledMapper;

    /**
     * 查询全部的秒杀记录
     *
     * @return
     */
    @Override
    public List<Seckill> getSeckillList() {
        return seckillMapper.getSeckillList();
    }

    /**
     * 获取库存数
     *
     * @return
     */
    @Override
    public long getShopNum(Long seckillId) {
        return seckillMapper.getShopNum(seckillId);
    }

    /**
     * 查询单个秒杀记录
     *
     * @param seckillId
     * @return
     */
    @Override
    public Seckill getById(Long seckillId) {
        return seckillMapper.getById(seckillId);
    }

    /**
     * 商品库存减一
     *
     * @param seckillId
     */
    @Override
    public int subShop(Long seckillId) {
        return seckillMapper.subShop(seckillId);
    }

    /**
     * 秒杀一：会出现超卖情况
     *
     * @param seckillId
     * @param userId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result startSeckil(long seckillId, long userId) {
        //校验库存
        Long num = seckillMapper.getShopNum(seckillId);
        if (num <= 0) {
            return Result.error(SeckillStatEnum.END.getInfo());
        }
        //扣减库存
        seckillMapper.subShop(seckillId);
        //创建订单
        SuccessKilled successKilled = new SuccessKilled();
        successKilled.setSeckillId(seckillId);
        successKilled.setUserId(userId);
        successKilled.setState("0");
        successKilled.setCreateTime(new Timestamp(System.currentTimeMillis()));
        //入库
        sucKilledMapper.saveSucSeckill(successKilled);
        return Result.ok(SeckillStatEnum.SUCCESS.getInfo());
    }

    /**
     * 秒杀二
     * 数据库乐观锁，不会超卖，但是当用户量少的时候会出现少买
     *
     * @param seckillId
     * @param userId
     * @return
     */
    @Override
    public Result startSeckilDBOCC(long seckillId, long userId) {
        //校验库存
        Seckill seckill = seckillMapper.getById(seckillId);
        if (seckill.getNumber() <= 0) {
            return Result.error(SeckillStatEnum.END.getInfo());
        }
        //扣减库存
        int result = seckillMapper.subShopByVersion(seckillId, seckill.getVersion());
        if (result == 0) {
            return Result.error(SeckillStatEnum.END.getInfo());
        }
        //创建订单
        SuccessKilled successKilled = new SuccessKilled();
        successKilled.setSeckillId(seckillId);
        successKilled.setUserId(userId);
        successKilled.setState("0");
        successKilled.setCreateTime(new Timestamp(System.currentTimeMillis()));
        //入库
        sucKilledMapper.saveSucSeckill(successKilled);
        return Result.ok(SeckillStatEnum.SUCCESS.getInfo());
    }
    /**
     * 秒杀四：会出现超卖情况
     *
     * @param seckillId
     * @param userId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result startSeckilReentrantLock(long seckillId, long userId) {
        //校验库存
        Long num = seckillMapper.getShopNum(seckillId);
        if (num <= 0) {
            return Result.error(SeckillStatEnum.END.getInfo());
        }
        //扣减库存
        seckillMapper.subShop(seckillId);
        //创建订单
        SuccessKilled successKilled = new SuccessKilled();
        successKilled.setSeckillId(seckillId);
        successKilled.setUserId(userId);
        successKilled.setState("0");
        successKilled.setCreateTime(new Timestamp(System.currentTimeMillis()));
        //入库
        sucKilledMapper.saveSucSeckill(successKilled);
        return Result.ok(SeckillStatEnum.SUCCESS.getInfo());
    }

    /**
     * 秒杀五：正常
     *
     * @param seckillId
     * @param userId
     * @return
     */
    @Override
    @Servicelock
    @Transactional(rollbackFor = Exception.class)
    public Result startSeckilReentrantLockAOP(long seckillId, long userId) {
        //校验库存
        Long num = seckillMapper.getShopNum(seckillId);
        if (num <= 0) {
            return Result.error(SeckillStatEnum.END.getInfo());
        }
        //扣减库存
        seckillMapper.subShop(seckillId);
        //创建订单
        SuccessKilled successKilled = new SuccessKilled();
        successKilled.setSeckillId(seckillId);
        successKilled.setUserId(userId);
        successKilled.setState("0");
        successKilled.setCreateTime(new Timestamp(System.currentTimeMillis()));
        //入库
        sucKilledMapper.saveSucSeckill(successKilled);
        return Result.ok(SeckillStatEnum.SUCCESS.getInfo());
    }
}
