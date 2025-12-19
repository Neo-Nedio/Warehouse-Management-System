package com.example.edmo.util.Jwt;

import cn.hutool.json.JSONUtil;
import com.example.edmo.controller.Result;
import com.example.edmo.pojo.entity.User;
import com.example.edmo.util.Constant.CodeConstant;
import com.example.edmo.util.Constant.JwtConstant;
import com.example.edmo.util.Constant.RedisConstant;
import com.example.edmo.exception.BaseException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//从请求头中获取token，验证后设置到Spring Security上下文和UserContext
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final StringRedisTemplate stringRedisTemplate;

    public JwtAuthenticationFilter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 每次请求先清理线程变量
        UserContext.clear();
        
        // 前端在请求头中携带的 JWT
        String token = request.getHeader("token");
        
        // 这里只对“带 token 的请求”做认证；不带 token 的请求后面交给 Spring Security 再根据配置判断是否放行
        if (token != null) {
            try {
                // 校验 token 本身是否正确
                if (!JwtUtil.verifyToken(token)) {
                    throw new BaseException(CodeConstant.token, JwtConstant.FALSE_TOKEN);
                }

                // 根据 token 去 Redis 中查用户信息，保证 token 仍然有效、没有被踢下线/登出
                String tokenKey = RedisConstant.TOKEN_KEY + token;
                String userJson = stringRedisTemplate.opsForValue().get(tokenKey);
                
                if (userJson == null) {
                    // Token 不在 Redis 中，说明已退出登录或已过期（逻辑上的失效）
                    throw new BaseException(CodeConstant.token, JwtConstant.TOKEN_INVALID_OR_EXPIRED);
                }

                //  反序列化出当前登录用户对象
                User user = JSONUtil.toBean(userJson, User.class);
                
                if (user == null || user.getId() == null) {
                    throw new BaseException(CodeConstant.token, JwtConstant.INVALID_USER_INFO);
                }

                //  把当前用户放到自定义的 UserContext（ThreadLocal）
                UserContext.setCurrentUser(user);

                // 5基于 roleId 组装 Spring Security 需要的权限列表
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                if (user.getRoleId() >= 2) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_OPERATOR"));
                }
                if (user.getRoleId() == 3) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                }

                //  创建认证对象并设置到 Spring Security 上下文
                //    这一步相当于告诉 Spring Security：这个请求已经通过了 JWT 认证，当前登录用户是 user，具有什么角色
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, authorities);
                // 绑定一些 Web 相关的额外信息（如 IP、SessionId），可用于日志审计
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // 将认证信息放入 SecurityContextHolder，后续的授权判断（.authenticated() / hasRole 等）都基于这里的信息
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (BaseException e) {
                // 7. 业务异常：token 无效/过期等，此时直接返回 401，整个请求在这里终止，不再继续过滤链
                logger.warn("JWT认证失败: code={}, message={}, uri={}",
                    e.getCode(), e.getMessage(), request.getRequestURI());
                
                // 清理安全上下文与线程变量
                SecurityContextHolder.clearContext();
                UserContext.clear();
                
                // 返回统一格式的未认证响应给前端
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                Result errorResult = Result.fail(e.getCode(), e.getMessage());
                response.getWriter().write(JSONUtil.toJsonStr(errorResult));
                return; // 不再继续执行后续过滤器
                
            } catch (Exception e) {
                // 8. 其他未知异常：记录日志并清理上下文，但不直接返回，交给后续过滤器/全局异常处理器处理
                logger.error("JWT认证过程中发生未知异常: uri={}", request.getRequestURI(), e);
                SecurityContextHolder.clearContext();
                UserContext.clear();
            }
        }

        try {
            //  无论有没有 token（或 token 解析异常但未抛 BaseException），只要没有在上面 return，
            //   都会继续走过滤器链。后面的 Spring Security 会根据 SecurityContext 中是否有认证信息、
            //   以及你在 SecurityConfig 中配置的 .permitAll() / .authenticated() 再决定是否最终放行。
            filterChain.doFilter(request, response);
        } finally {
            //  请求结束后清理 ThreadLocal，防止内存泄漏和线程复用导致的数据串号
            UserContext.clear();
        }
    }
}

