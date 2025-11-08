package com.example.edmo.config;

import com.example.edmo.filter.SimpleJwtInterceptor;
import com.example.edmo.filter.UserPermissionInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Resource
    private SimpleJwtInterceptor simpleJwtInterceptor;

    @Resource
    private UserPermissionInterceptor userPermissionInterceptor;



    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(simpleJwtInterceptor)
                .addPathPatterns("/**")// 拦截所有路径
                .excludePathPatterns(
                        "/user/code",
                        "/user/loginByPassword",
                        "/user/loginByCode",
                        "/user/updatePassword"
                );


        // 第二层：User路径权限检查
        registry.addInterceptor(userPermissionInterceptor)
                .addPathPatterns("/user/**")
                .excludePathPatterns(
                        "/user/code",
                        "/user/loginByPassword",
                        "/user/loginByCode",
                        "/user/updatePassword"
                );

    }


}
