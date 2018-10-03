package com.hubu.seckill.service;

import com.hubu.seckill.dto.Exposer;
import com.hubu.seckill.dto.SeckillExecution;
import com.hubu.seckill.exception.RepeatKillException;
import com.hubu.seckill.exception.SeckillCloseException;
import com.hubu.seckill.exception.SeckillException;
import com.hubu.seckill.model.Seckill;

import java.util.List;

/**
 * 业务接口:站在"使用者"角度设计接口
 * 三个方面:方法定义粒度,参数,返回类型（return类型/异常）
 * @author hxg
 *
 */
public interface SeckillService {

    /**
     * 查询所有秒杀记录
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 查询单个秒杀记录
     * @param seckillId
     * @return
     */
    Seckill getById(long seckillId);

    /**
     * 秒杀开启时输出秒杀接口地址，
     * 否则输出系统时间和秒杀时间
     * @param seckillId
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀操作
     * @param seckillId
     * @param userPhone
     * @param md5
     */
    SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException,SeckillCloseException,RepeatKillException;


}
