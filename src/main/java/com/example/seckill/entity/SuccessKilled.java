package com.example.seckill.entity;

import lombok.Data;

import java.sql.Timestamp;

/**
 * @author jiangfengan
 */
@Data
public class SuccessKilled {
    private long successId;
    private long seckillId;
    private long userId;
    private String state;
    private Timestamp createTime;
}
