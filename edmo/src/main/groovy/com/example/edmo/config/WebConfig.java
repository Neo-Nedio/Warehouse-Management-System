package com.example.edmo.config;

import com.example.edmo.util.interceptor.FirstInterceptor;
import com.example.edmo.util.interceptor.UserPermissionInterceptor;
import com.example.edmo.util.interceptor.AdminInterceptor;
import com.example.edmo.util.interceptor.GoodsInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Resource
    private FirstInterceptor firstInterceptor;

    @Resource
    private UserPermissionInterceptor userPermissionInterceptor;

    @Resource
    private AdminInterceptor adminInterceptor;

    @Resource
    private GoodsInterceptor goodsInterceptor;
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }



    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(firstInterceptor)
                .addPathPatterns("/**")// 拦截所有路径
                .excludePathPatterns(
                        "/user/code",
                        "/user/loginByPassword",
                        "/user/loginByCode",
                        "/user/updatePassword",
                        "/user/refresh",
                        // Swagger UI 和 OpenAPI 文档路径
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                );


        // User路径权限检查
        registry.addInterceptor(userPermissionInterceptor)
                .addPathPatterns("/user/**")
                .excludePathPatterns(
                        "/user/code",
                        "/user/loginByPassword",
                        "/user/loginByCode",
                        "/user/updatePassword",
                        "/user/refresh"
                );

        //Admin路径权限检查
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/warehouse/admin/**")
                .addPathPatterns("/log/**");

        // goods路径权限检查
        registry.addInterceptor(goodsInterceptor)
                .addPathPatterns("/goods/**");
    }


}
