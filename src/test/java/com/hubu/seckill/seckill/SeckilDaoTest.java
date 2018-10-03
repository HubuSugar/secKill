package com.hubu.seckill.seckill;

import com.hubu.seckill.dao.SeckillDao;
import com.hubu.seckill.model.Seckill;
import com.hubu.seckill.service.SeckillService;
import com.hubu.seckill.service.SeckillServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SeckilDaoTest {

    @Autowired
    SeckillDao seckilDao;

    @Test
    public void testSelectById(){
        Seckill seckill = seckilDao.querySeckillById(1001);
        System.out.println(seckill);
    }

    @Test
    public void testSelectAll(){
        List<Seckill> seckills = seckilDao.queryAll(1,5);
        for (Seckill seckill:seckills
             ) {
            System.out.println(seckill);
        }
    }

    @Test
    public void testupdate(){
        Seckill seckill = seckilDao.querySeckillById(1001);
        int k = seckilDao.reduceNumber(1000,new Date());
        System.out.println("+++++++++" + k);
    }


    @Autowired
    SeckillServiceImpl seckillServiceImpl;


    @Test
    public  void testProcedure(){

        Calendar cal=Calendar.getInstance();
           //System.out.println(Calendar.DATE);//5
        cal.add(Calendar.DATE,-5);
        Date time=cal.getTime();
        //System.out.println("=========="+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(time));
        Map<String,Object> map = new HashMap<>();
        map.put("seckillId", 1001L);
        map.put("phone", 13129979382L);
        map.put("killTime", time);
        map.put("result", null);
        seckilDao.killByProcedure(map);
        int k = (Integer) map.get("result");
        System.out.println("==============================================="+k);
    }


}
