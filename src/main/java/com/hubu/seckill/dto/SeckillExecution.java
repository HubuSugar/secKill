package com.hubu.seckill.dto;

import com.hubu.seckill.enums.SeckillStatEnum;
import com.hubu.seckill.model.SeckillSuccess;

/**
 * 封装秒杀执行后结果
 * @author hxg
 *
 */
public class SeckillExecution {

    //秒杀物品Id
    private long seckillId;
    //执行结果
    private  int state;
    //状态表示
    private String stateInfo;
    //秒杀成功对象
    private SeckillSuccess seckillSuccess;


    public SeckillExecution(long seckillId, SeckillStatEnum statEnum, SeckillSuccess seckillSuccess) {
        super();
        this.seckillId = seckillId;
        this.state = statEnum.getState();
        this.stateInfo = statEnum.getStateInfo();
        this.seckillSuccess = seckillSuccess;
    }

     /**
     *执行存储过程调用
     */

    public SeckillExecution(long seckillId, SeckillStatEnum statEnum) {
        super();
        this.seckillId = seckillId;
        this.state = statEnum.getState();
        this.stateInfo = statEnum.getStateInfo();
    }


    public long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(long seckillId) {
        this.seckillId = seckillId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public void setStateInfo(String stateInfo) {
        this.stateInfo = stateInfo;
    }

    public SeckillSuccess getSeckillSuccess() {
        return seckillSuccess;
    }

    public void setSeckillSuccess(SeckillSuccess seckillSuccess) {
        this.seckillSuccess = seckillSuccess;
    }
}
