package com.example.seckill.mapper;

import com.example.seckill.entity.Seckill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SeckillMapper {
    /**
     * 查询全部的秒杀记录
     *
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 查询单个秒杀记录
     *
     * @param seckillId
     * @return
     */
    Seckill getById(Long seckillId);


    /**
     * 获取商品库存
     *
     * @param seckillId
     * @return
     */
    long getShopNum(Long seckillId);

    /**
     * 商品库存减一
     *
     * @param seckillId
     */
    int subShop(Long seckillId);

    /**
     * 商品库存减一+版本校验
     *
     * @param seckillId
     */
    int subShopByVersion(@Param("seckillId") Long seckillId, @Param("version") Integer version);

    /**
     * 重置商品库存
     *
     * @param seckillId
     */
    void rollbackShopNum(Long seckillId);
}
