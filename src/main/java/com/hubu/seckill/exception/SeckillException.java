package com.hubu.seckill.exception;
/**
 * 秒杀业务相关异常
 * @author hxg
 *
 */
public class SeckillException extends RuntimeException {
    public SeckillException(String message){
        super(message);
    }

    public SeckillException(String message,Throwable cause){
        super(message,cause);
    }
}
