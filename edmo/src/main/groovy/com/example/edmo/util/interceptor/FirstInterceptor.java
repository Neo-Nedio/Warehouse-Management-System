package com.example.edmo.util.interceptor;

import com.example.edmo.util.Constant.CodeConstant;
import com.example.edmo.util.Constant.JwtConstant;
import com.example.edmo.util.Constant.RedisConstant;
import com.example.edmo.util.Jwt.JwtUtil;
import com.example.edmo.util.Jwt.UserContext;
import com.example.edmo.pojo.entity.User;
import com.example.edmo.exception.BaseException;
import com.example.edmo.exception.JwtException;
import com.example.edmo.service.Interface.UserService;
import com.example.edmo.service.Interface.WarehouseUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
public class FirstInterceptor implements HandlerInterceptor {

    @Resource
    private StringRedisTemplate  stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 先清理线程数据
        UserContext.clear();



        //todo
        String token = request.getHeader("token");
        try {
            if (token == null ) throw new JwtException(CodeConstant.token,JwtConstant.NULL_TOKEN);
            // 验证token
            if (!JwtUtil.verifyToken(token)) throw new JwtException(CodeConstant.token,JwtConstant.FALSE_TOKEN);

            // 获取用户信息并存储
            String key = RedisConstant.LOGIN_USER_KEY + token;
            String Json = stringRedisTemplate.opsForValue().get(key);

            if(Json == null){
                throw new JwtException(CodeConstant.token,JwtConstant.NULL_LOGIN);
            }else{
                ObjectMapper objectMapper = new ObjectMapper();
                User user = objectMapper.readValue(Json, User.class);
                UserContext.setCurrentUser(user);
                stringRedisTemplate.expire(key, RedisConstant.LOGIN_USER_TTL, TimeUnit.MINUTES);
                return true;
            }
        } catch (Exception e) {
            throw new BaseException(CodeConstant.token,e.getMessage());
        }



    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler, Exception ex) {
        // 清理ThreadLocal
        UserContext.clear();
    }
}
