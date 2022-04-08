package com.example.seckill.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author jiangfengan
 */
@Aspect
@Component
@Order(1)
public class LockAspect {
    private static ReentrantLock reentrantLock = new ReentrantLock();

    @Pointcut("@annotation(com.example.seckill.aop.Servicelock)")
    public void lockAspect() {

    }

    @Around("lockAspect()")
    public Object around(ProceedingJoinPoint joinPoint) {
        Object o = null;
        reentrantLock.lock();
        try {
            o = joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            reentrantLock.unlock();
        }
        return o;
    }

    /*private static Object object=new Object();

    @Around("lockAspect()")
    public Object around2(ProceedingJoinPoint joinPoint){
        Object o=null;
        synchronized (object){
            try {
                o = joinPoint.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        return o;
    }*/
}
