package org.loed.framework.mybatis.test;

import org.loed.framework.mybatis.autoconfigure.EnableSharding;
import org.loed.framework.mybatis.inspector.autoconfigure.DbInspect;
import org.loed.framework.mybatis.test.mapper.UserMapper;
import org.loed.framework.mybatis.test.po.User;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/2/18 9:55 AM
 */
@SpringBootApplication
@MapperScan(basePackageClasses = UserMapper.class)
@DbInspect(basePackageClasses = User.class)
@EnableSharding
public class MybatisTestApplication {
	public static void main(String[] args) {
		SpringApplication.run(MybatisTestApplication.class, args);
	}
}
