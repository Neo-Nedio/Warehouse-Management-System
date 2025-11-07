package com.example.edmo.config;

import com.example.edmo.filter.SimpleJwtInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Resource
    private SimpleJwtInterceptor simpleJwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(simpleJwtInterceptor)
                .addPathPatterns("/**");  // 拦截所有路径
    }
}
