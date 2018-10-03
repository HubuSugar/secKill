package com.hubu.seckill.dao;

import com.hubu.seckill.model.Seckill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 1.使用注解，访问数据库
 * 2.使用xml文件,主要处理一些复杂的SQL
 */

@Mapper
public interface SeckillDao {

     String TABLE_NAME = "seckill";
     String INSERT_FIELDS = "name,number,starttime,endtime,createtime";
     String  SELECT_FIELDS = "seckillid," + INSERT_FIELDS;

     /**
     * 根据id查询秒杀对象
     * @seckillId 秒杀对象的Id
     */
     @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where seckillid = #{seckillId}"})
     Seckill querySeckillById(long seckillId);


    /**
     * 根据偏移量 查询所有
     * @param offest
     * @param limit
     * @return
     */
     List<Seckill> queryAll(@Param("offset") int offest, @Param("limit") int limit);

    /**
     * 减库存
     * @param seckillId
     * @param killTime
     * @return 如果影响行数>1,表示更新的记录行数
     */
    int reduceNumber(@Param("seckillId")long seckillId, @Param("killTime")Date killTime);


    /**
     * 进一步优化
     * 使用注解调用存储过程
     * 调用秒杀的存储过程
     * 使用map传参数
     */
//    @Select({"call  execute_seckill(#{seckillId,mode=IN,jdbcType=BIGINT},"
//            +"#{phone,mode=IN,jdbcType=BIGINT},"
//            +"#{killTime,mode=IN,jdbcType=TIMESTAMP},"
//            +"#{result,mode=OUT,jdbcType=INTEGER})"})
//     @Options(statementType=StatementType.CALLABLE)
     void killByProcedure(Map<String,Object> objectMap);




}
