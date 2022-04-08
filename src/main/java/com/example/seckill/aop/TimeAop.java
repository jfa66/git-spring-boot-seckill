package com.example.seckill.aop;

import java.lang.annotation.*;

/**
 * 自定义注解
 * @author jiangfengan
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TimeAop {
}
