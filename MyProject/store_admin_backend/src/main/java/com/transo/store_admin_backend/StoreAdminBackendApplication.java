package com.transo.store_admin_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@ComponentScan(value = {"com.transo.store_admin_backend","com.transo.store_tools"})
@EnableJpaRepositories(basePackages = {"com.transo.store_repository.Repository"})
@EntityScan(basePackages = {"com.transo.store_bean.Entity"})
public class StoreAdminBackendApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(StoreAdminBackendApplication.class, args);
    }
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        // 注意这里要指向原先用main方法执行的Application启动类
        return builder.sources(StoreAdminBackendApplication.class);
    }
}
