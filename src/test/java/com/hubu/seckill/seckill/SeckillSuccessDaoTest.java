package com.hubu.seckill.seckill;

import com.hubu.seckill.dao.SeckillSuccessDao;
import com.hubu.seckill.model.SeckillSuccess;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SeckillSuccessDaoTest {

    @Autowired
    SeckillSuccessDao seckillSuccessDao;

    @Test
    public void fun(){
       SeckillSuccess seckillSuccess =  seckillSuccessDao.queryByIdWithSeckill(1000,13129979382L);
       System.out.println(seckillSuccess);

    }

    @Test
    public void fun1(){
        int k = seckillSuccessDao.insertSuccessKill(1001,15527241146L);
        System.out.println("========================================" + k);

    }


}
