package com.wms.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// 确保这里的路径指向你的 mapper 包
@MapperScan("com.wms.backend.mapper")
public class WmsBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(WmsBackendApplication.class, args);
	}
}