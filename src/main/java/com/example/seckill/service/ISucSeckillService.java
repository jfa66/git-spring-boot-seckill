package com.example.seckill.service;

import com.example.seckill.entity.SuccessKilled;

/**
 * @author jiangfengan
 */
public interface ISucSeckillService {

    /**
     * 查询秒杀售卖商品
     *
     * @param seckillId
     * @return
     */
    Long getSucSeckillCount(long seckillId);

    /**
     * 删除秒杀售卖商品记录
     *
     * @param seckillId
     * @return
     */
    void delSucSeckill(long seckillId);

    /**
     * 保存秒杀成功商品
     *
     * @param successKilled
     */
    void saveSucSeckill(SuccessKilled successKilled);
}
