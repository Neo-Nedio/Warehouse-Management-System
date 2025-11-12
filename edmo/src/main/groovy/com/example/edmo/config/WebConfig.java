package com.example.edmo.config;

import com.example.edmo.interceptor.FirstInterceptor;
import com.example.edmo.interceptor.UserPermissionInterceptor;
import com.example.edmo.interceptor.WarehouseAdminInterceptor;
import com.example.edmo.interceptor.goodsInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Resource
    private FirstInterceptor firstInterceptor;

    @Resource
    private UserPermissionInterceptor userPermissionInterceptor;

    @Resource
    private WarehouseAdminInterceptor warehouseAdminInterceptor;

    @Resource
    private goodsInterceptor goodsInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(firstInterceptor)
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

        // 第三层：warehouseAdmin路径权限检查
        registry.addInterceptor(warehouseAdminInterceptor)
                .addPathPatterns("/warehouse/admin/**");

        registry.addInterceptor(goodsInterceptor)
                .addPathPatterns("/goods/**");
    }


}
