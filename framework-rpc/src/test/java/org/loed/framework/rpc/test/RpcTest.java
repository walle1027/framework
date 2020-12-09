package org.loed.framework.rpc.test;

import org.loed.framework.rpc.ServiceProxyScan;
import org.loed.framework.rpc.test.service.StudentService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ServiceProxyScan(basePackageClasses = StudentService.class)
public class RpcTest {
	public static void main(String[] args) {
		SpringApplication.run(RpcTest.class, args);
	}
}
