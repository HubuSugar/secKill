package com.hubu.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


/**
 * 遇到的问题项目启动时会扫描Application同级目录下及子目录下的包,不在同一级目录下
 * 可能会导致bean注入不成功
 * */
@SpringBootApplication
public class SeckillApplication  extends SpringBootServletInitializer{
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(SeckillApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(SeckillApplication.class, args);
	}
}
