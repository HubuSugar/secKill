package com.hubu.seckill.util;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.hubu.seckill.model.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


@Component
public class JedisAdapter {

    private static final Logger logger = LoggerFactory.getLogger(JedisAdapter.class);

     //jedis连接池，相当于数据库连接池
     private static volatile JedisPool jedisPool;

     /**
     * 建立连接池的时候把一些参数抽取出来
     */
     private static void createJedisPool(){

        JedisPoolConfig  config = new JedisPoolConfig();
        //设置最大空闲连接
        config.setMaxIdle(10);
        //设置最大等待毫秒数
        config.setMaxWaitMillis(1000);
        //设置最大连接数
        config.setMaxTotal(100);
         //创建连接池
        jedisPool = new JedisPool(config,"47.106.225.58",6379);
    }

     //初始化连接池
     private static void jedisPoolInit(){
        if(jedisPool == null){
            synchronized (JedisAdapter.class){
                if(jedisPool == null){
                    createJedisPool();
                }
            }
        }
    }

     //返回一个连接池对象
     private static Jedis getJedis(){
        Jedis jedis = null;
        if(jedisPool == null){
            jedisPoolInit();
        }
        try{
            jedis = jedisPool.getResource();
            jedis.auth("123456");
            return jedis;
        }catch(Exception e){
            logger.error("读取缓存失败"+ e.getMessage());
        }
        return jedis;
    }

    //归还连接
    private static void returnJedis(Jedis jedis){
        if(jedis != null)
            jedis.close();
    }

   //测试jedis工具
//    public static void main(String[] args) {
//        Jedis jedis = getJedis();
//        System.out.println(jedis.ping());
//        jedis.set("hello","study");
//        System.out.println(jedis.get("hello"));
//    }

     /**
     * 对象的序列化
     */
      private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

      //返回一个秒杀对象
      public Seckill getSeckill(long seckillId){
          try{
                Jedis jedis = getJedis();
              try {
                  String key = "seckill:"+seckillId;
                  //并没有实现内部序列化操作
                  //get->byte[] -> 反序列化 -> Object(Seckill)
                  //采用自定义序列化
                  //prootostuff:pojo(必须是有get(),set()标准的java对象)
                  byte[] bytes = jedis.get(key.getBytes());
                  //缓存获取到
                  if(bytes != null){
                      //空对象
                      Seckill seckill = schema.newMessage();
                      ProtostuffIOUtil.mergeFrom(bytes, seckill, schema);
                      //seckill 被反序列化
                      return seckill;
                  }
              } finally {
                  jedis.close();
              }
          }
          catch(Exception e){
              logger.error(e.getMessage());
          }
         return null;
      }

    /**
     * 被反序列化
     * @param seckill
     * @return
     */
    public String putSeckill(Seckill seckill){
        //set Object(Seckill) -> 序列化 -> byte[]
        try {
            Jedis jedis = getJedis();
            try {
                String key = "seckill:"+seckill.getSeckillId();
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, schema,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                //超时缓存
                int timeout = 60 * 60;//1小时
                String result = jedis.setex(key.getBytes(), timeout, bytes);
                return result;
            } finally {
                jedis.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

}
