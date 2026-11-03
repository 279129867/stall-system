package com.example.stallsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 前端路由配置：SPA 单页应用刷新不 404
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 前端路由刷新时，全部转发到 index.html，由 Vue Router 处理
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/login").setViewName("forward:/index.html");
        registry.addViewController("/dashboard/**").setViewName("forward:/index.html");
        registry.addViewController("/seller/**").setViewName("forward:/index.html");
        registry.addViewController("/buyer/**").setViewName("forward:/index.html");
        registry.addViewController("/admin/**").setViewName("forward:/index.html");
    }
}
