package com.example.edmo.Jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.edmo.pojo.entity.User;

import java.util.Calendar;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET_KEY = "abcdefghijklmn";

    /**
     * 根据用户信息创建JWT令牌
     */
    public static String createToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.SECOND, 3600 * 24 * 7); // 7天过期

        return JWT.create()
                .withClaim("id", user.getId())
                .withClaim("name", user.getName())
                .withClaim("email", user.getEmail())
                .withClaim("roleId", user.getRoleId())
                .withExpiresAt(calendar.getTime())
                .withIssuedAt(now)
                .sign(algorithm);
    }

    /**
     * 验证JWT令牌
     */
    public static boolean verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
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
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("id").asInt();
        } catch (JWTVerificationException e) {
            return null;
        }
    }
}
