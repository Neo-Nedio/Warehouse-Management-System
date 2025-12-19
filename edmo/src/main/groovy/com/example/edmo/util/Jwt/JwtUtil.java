package com.example.edmo.util.Jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.edmo.pojo.entity.User;
import com.example.edmo.util.Constant.JwtConstant;

import java.util.Calendar;
import java.util.Date;

public class JwtUtil {


    /**
     * 根据用户信息创建JWT令牌
     */
    public static String createToken(User user,Integer expire) {
        Algorithm algorithm = Algorithm.HMAC256(JwtConstant.SECRET_KEY);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, expire);
        Date now = calendar.getTime();

        //token包括头部（包括算法），载荷（包括过期时间），签名（密钥经过算法处理后的结果）
        return JWT.create()
                .withClaim("id", user.getId())
                .withClaim("name", user.getName())
                .withClaim("roleId",user.getRoleId())
                .withClaim("managedWarehouseIds", user.getManagedWarehouseIds())
                .withExpiresAt(calendar.getTime())  // 过期时间
                .withIssuedAt(now)                  // 签发时间
                .sign(algorithm);
    }

    /**
     * 验证JWT令牌
     */
    public static boolean verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(JwtConstant.SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * 从令牌中解析用户ID
     */
    public static Integer getUserId(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(JwtConstant.SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("id").asInt();
        } catch (JWTVerificationException e) {
            return null;
        }
    }
}
