package com.hubu.seckill.service;

import com.hubu.seckill.dao.SeckillDao;
import com.hubu.seckill.dao.SeckillSuccessDao;
import com.hubu.seckill.dto.Exposer;
import com.hubu.seckill.dto.SeckillExecution;
import com.hubu.seckill.enums.SeckillStatEnum;
import com.hubu.seckill.exception.RepeatKillException;
import com.hubu.seckill.exception.SeckillCloseException;
import com.hubu.seckill.exception.SeckillException;
import com.hubu.seckill.model.Seckill;
import com.hubu.seckill.model.SeckillSuccess;
import com.hubu.seckill.util.JedisAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.apache.commons.collections.MapUtils;

import java.util.*;

@Service
public class SeckillServiceImpl  implements SeckillService{
    private static  final Logger logger = LoggerFactory.getLogger(SeckillServiceImpl.class);

    @Autowired
    SeckillDao seckillDao;

    @Autowired
    SeckillSuccessDao seckillSuccessDao;

    @Autowired
    JedisAdapter  jedisAdapter;

    //MD5盐值字符串，用于混淆MD5
    private final String salt = "sdasdsddsDERTTRTR#$%%DR";

    @Override
    public List<Seckill> getSeckillList() {

        return seckillDao.queryAll(0,20);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.querySeckillById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        //优化点:缓存优化:超时的基础上维护一致性
        /**
         * get from cache
         * if null
         * 	get db
         * 	else
         * 		put cache
         * logic
         */
        //1. 访问dedis
        Seckill seckill = jedisAdapter.getSeckill(seckillId);
        if(seckill == null){
             //如果是redis中不存在,那么从数据库中查询
            //2.访问数据库
            seckill = seckillDao.querySeckillById(seckillId);
            if(seckill != null){
                //3.如果数据库存在，将对象加入redis
                jedisAdapter.putSeckill(seckill);
            }else{
                //如果秒杀不存在
                return new Exposer(false, seckillId);
            }
        }
         //缓存redis中存在，获取秒杀开始时间和结束时间
        Date start =seckill.getStartTime();
        Date end = seckill.getEndTime();
        Date now = new Date();
        //秒杀开启或者已经结束
        if(start.getTime() > now.getTime() || end.getTime() < now.getTime()){
            return new Exposer(false, seckillId, now.getTime(),start.getTime(),end.getTime());
        }
        //转换特定字符串的过程，加盐不可逆
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);

    }

    //产生md5
    private String getMD5(long seckillId){
        String base = seckillId + "/" + salt;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    /**
     * 使用注解控制事务方法的优点:
     * 1.开发团队达成一致约定,明确标注事务方法的编程风格
     * 2.保证事务方法的执行时间尽可能短,不要穿插其他网络操作RPC/HTTP请求或者剥离到事务方法外部
     * 3.不是所有的方法都需要事务，如只有一条修改操作，只读操作不需要事务控制。
     */
    @Transactional
    @Override
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, SeckillCloseException, RepeatKillException {
        if(md5 == null || !md5.equals(getMD5(seckillId))){
            throw new SeckillException("seckill data rewrite");
        }
        //执行秒杀过程，减库存，加记录秒杀行为
        Date now = new Date();
        try{
            int insertCount = seckillSuccessDao.insertSuccessKill(seckillId,userPhone);
            if(insertCount <= 0)
                throw  new  RepeatKillException("seckill repeated");
            else{
                //减库存，热点商品竞争
                int updateCount = seckillDao.reduceNumber(seckillId,now);
                if(updateCount <= 0){
                    //没有减少，秒杀结束
                    throw  new SeckillCloseException("Seckill is closed");
                }else{
                    //秒杀成功,返回秒杀结果
                    SeckillSuccess seckillSuccess =  seckillSuccessDao.queryByIdWithSeckill(seckillId,userPhone);
                     return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS,seckillSuccess);
                }
            }
        }catch (SeckillCloseException e1) {
            throw e1;
        }catch (RepeatKillException e2) {
            throw e2;
        }catch (Exception e) {
            logger.error(e.getMessage(),e);
            //所有编译期异常转换成运行期异常
            throw new SeckillException("seckill inner error:"+e.getMessage());
        }
    }


  /**
   * 通过存储过程
   * 执行秒杀过程
  */
  public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5){

       if(md5 == null || !md5.equals(getMD5(seckillId))){
           return new SeckillExecution(seckillId, SeckillStatEnum.DATA_REWRITE);
       }
        Date killTime  = new Date();
        Map<String,Object> objectMap = new HashMap<>();
       objectMap.put("seckillId",seckillId);
       objectMap.put("phone",userPhone);
       objectMap.put("killTime",killTime);
       objectMap.put("result",null);
        //执行存储过程
      try{
          seckillDao.killByProcedure(objectMap);
          int result = MapUtils.getInteger(objectMap, "result",-2);
          if(result == 1){
              //秒杀成功
             SeckillSuccess seckillSuccess = seckillSuccessDao.queryByIdWithSeckill(seckillId,userPhone);
             return new SeckillExecution(seckillId,SeckillStatEnum.SUCCESS,seckillSuccess);
          }else {
               return new SeckillExecution(seckillId,SeckillStatEnum.stateOf(result));
          }
      }catch(Exception e){
          logger.error(e.getMessage());
          return new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
      }
  }

}
