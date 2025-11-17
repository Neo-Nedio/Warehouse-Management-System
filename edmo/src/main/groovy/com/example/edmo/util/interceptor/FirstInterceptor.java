package com.example.edmo.util.interceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.edmo.pojo.entity.User;
import com.example.edmo.util.Constant.CodeConstant;
import com.example.edmo.util.Constant.JwtConstant;
import com.example.edmo.util.Jwt.JwtUtil;
import com.example.edmo.util.Jwt.UserContext;
import com.example.edmo.exception.BaseException;
import com.example.edmo.exception.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class FirstInterceptor implements HandlerInterceptor {


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

            //填充数据
            Algorithm algorithm = Algorithm.HMAC256(JwtConstant.SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt =  verifier.verify(token);
            User user = new User();
            user.setId(jwt.getClaim("id").asInt());
            user.setName(jwt.getClaim("name").asString());
            user.setRoleId(jwt.getClaim("roleId").asInt());
            user.setManagedWarehouseIds(jwt.getClaim("managedWarehouseIds").asList(Integer.class));

            UserContext.setCurrentUser(user);

            return true;
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
