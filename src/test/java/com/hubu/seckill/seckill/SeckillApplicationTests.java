package com.hubu.seckill.seckill;

import com.hubu.seckill.util.JedisAdapter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SeckillApplicationTests {


	@Autowired
	JedisAdapter jedisAdapter;

	@Test
	public void contextLoads() {

	}

}
