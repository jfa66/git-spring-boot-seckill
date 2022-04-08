package com.example.seckill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author jiangfengan
 */
@SpringBootApplication
public class GitSpringBootSeckillApplication {

    private final static Logger LOGGER = LoggerFactory.getLogger(GitSpringBootSeckillApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(GitSpringBootSeckillApplication.class, args);
        LOGGER.info("启动项目");
    }

}
