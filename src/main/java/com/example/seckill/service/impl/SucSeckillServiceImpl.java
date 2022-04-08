package com.example.seckill.service.impl;

import com.example.seckill.entity.SuccessKilled;
import com.example.seckill.mapper.SeckillMapper;
import com.example.seckill.mapper.SucKilledMapper;
import com.example.seckill.service.ISucSeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jiangfengan
 */
@Service
public class SucSeckillServiceImpl implements ISucSeckillService {

    @Autowired
    private SeckillMapper seckillMapper;

    @Autowired
    private SucKilledMapper sucKilledMapper;

    /**
     * 查询秒杀售卖商品
     *
     * @param seckillId
     * @return
     */
    @Override
    public Long getSucSeckillCount(long seckillId) {
        return sucKilledMapper.getSucCount(seckillId);
    }

    /**
     * 删除秒杀售卖商品记录
     *
     * @param seckillId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delSucSeckill(long seckillId) {
        sucKilledMapper.delSucCount(seckillId);
        seckillMapper.rollbackShopNum(seckillId);
    }

    /**
     * 保存秒杀成功商品
     *
     * @param successKilled
     */
    @Override
    public void saveSucSeckill(SuccessKilled successKilled) {
        sucKilledMapper.saveSucSeckill(successKilled);
    }
}
