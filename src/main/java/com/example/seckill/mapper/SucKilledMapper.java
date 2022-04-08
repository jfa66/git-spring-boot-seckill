package com.example.seckill.mapper;

import com.example.seckill.entity.SuccessKilled;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SucKilledMapper {

    long getSucCount(Long seckillId);

    void delSucCount(Long seckillId);

    void saveSucSeckill(SuccessKilled successKilled);
}
