package com.example.seckill.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author jiangfengan
 */
@Component
@Aspect
public class TimeAspect {

    @Pointcut("@annotation(com.example.seckill.aop.TimeAop)")
    public void timePointCut(){

    }

    @Around("timePointCut()")
    public Object around(ProceedingJoinPoint joinPoint){
        Object o=null;
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            String start="秒杀开始："+sdf.format(new Date());
            o=joinPoint.proceed();
            System.out.println(start+"\n秒杀结束："+sdf.format(new Date()));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return o;
    }
}
