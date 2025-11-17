package com.example.edmo.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.edmo.exception.JwtException;
import com.example.edmo.service.Interface.WarehouseUserService;
import com.example.edmo.util.Constant.CodeConstant;
import com.example.edmo.util.Constant.JwtConstant;
import com.example.edmo.util.Constant.RedisConstant;
import com.example.edmo.util.Constant.UserConstant;
import com.example.edmo.util.Jwt.JwtUtil;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.DTO.LoginRequest;
import com.example.edmo.pojo.DTO.UserDTO;
import com.example.edmo.pojo.entity.User;
import com.example.edmo.exception.UserException;
import com.example.edmo.service.Interface.UserService;
import com.example.edmo.util.Jwt.UserContext;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Validated
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private WarehouseUserService warehouseUserService;

    @Resource
    private BCryptPasswordEncoder encoder;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/code")
    public Result createCode(@RequestParam  String email) {
        int code=userService.CreatCode(email);
        if(code==0) throw new UserException(CodeConstant.user,UserConstant.NULL_USER);

        //把验证码保存在redis
        stringRedisTemplate.opsForValue().set(RedisConstant.LOGIN_CODE_KEY +email,String.valueOf(code),RedisConstant.LOGIN_CODE_TTL, TimeUnit.MINUTES);


        return Result.success();
    }

    @PostMapping("/loginByPassword")
    public Result loginByPassword(@RequestBody LoginRequest loginRequest){
        // 根据邮箱查找用户
        User user=userService.findUserByEmail(loginRequest.getEmail());
        //匹配用match
        if (user == null || !encoder.matches(loginRequest.getPassword(),user.getPassword())) {
            throw new UserException(CodeConstant.user,UserConstant.FALSE_EMAIL_OR_PASSWORD);
        }
        return sendToken(user);
    }

    @PostMapping("/loginByCode")
    public Result loginByCode(@RequestBody LoginRequest loginRequest) {

        String email = loginRequest.getEmail();
        int code = loginRequest.getCode();

        //todo 获取验证码并检验
        int redisCode = Optional.ofNullable(
                        stringRedisTemplate.opsForValue().get(RedisConstant.LOGIN_CODE_KEY + email)
                )
                .map(Integer::parseInt)
                .orElseThrow(() -> new UserException(CodeConstant.user, UserConstant.NEED_CODE));


        if(code!=redisCode) throw new UserException(CodeConstant.user,UserConstant.FALSE_CODE);

        User user=userService.findUserByEmail(loginRequest.getEmail());//获取验证码时已经检验用户是否存在
        return sendToken(user);
    }

    @PostMapping("/updatePassword")
    public Result updatePassword(@RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        int code = loginRequest.getCode();

        int redisCode = Optional.ofNullable(
                        stringRedisTemplate.opsForValue().get(RedisConstant.LOGIN_CODE_KEY + email)
                )
                .map(Integer::parseInt)
                .orElseThrow(() -> new UserException(CodeConstant.user, UserConstant.NEED_CODE));


        if(code!=redisCode) throw new UserException(CodeConstant.user,UserConstant.FALSE_CODE);

        String password = loginRequest.getPassword();
        User user=userService.findUserByEmail(loginRequest.getEmail());
        user.setPassword(encoder.encode(password));
        mod(new UserDTO(user));
        return Result.success();
    }

    @PostMapping("/refresh")
    public Result refresh(@RequestParam String RefreshToken) {
        try {
            if (RefreshToken == null ) throw new JwtException(CodeConstant.token,JwtConstant.NULL_TOKEN);

            // 验证Refresh Token
            Algorithm algorithm = Algorithm.HMAC256(JwtConstant.SECRET_KEY);
            DecodedJWT jwt = JWT.require(algorithm).build().verify(RefreshToken);

            Integer userId = jwt.getClaim("id").asInt();

            String key = RedisConstant.LOGIN_USER_KEY + userId;

            // 检查Redis中的Refresh Token是否匹配
            String storedRefreshToken = stringRedisTemplate.opsForValue().get(key);

            if (storedRefreshToken == null || !storedRefreshToken.equals(RefreshToken)) {
                throw new UserException(CodeConstant.user,UserConstant.NULL_LOGIN);
            }

            // 获取用户信息
            User user = userService.getById(userId);
            if (user == null) throw new UserException(CodeConstant.user,UserConstant.NULL_USER);
            return sendToken(user);
        }catch (Exception e) {
            throw new UserException(CodeConstant.user,e.getMessage());
        }
    }

    @GetMapping("logOut")
    public Result logout() {
        stringRedisTemplate.delete(RedisConstant.LOGIN_USER_KEY + UserContext.getCurrentUser().getId());
        return Result.success();
    }


    private Result sendToken(User user)  {
        user.setManagedWarehouseIds(warehouseUserService.findWarehouseIdByUserId(user.getId()));
        // 生成token
        String refreshToken = JwtUtil.createToken(user, JwtConstant.REFRESH_TOKEN_EXPIRE);
        String AccessToken = JwtUtil.createToken(user, JwtConstant.ACCESS_TOKEN_EXPIRE);

        // 返回双token和user（不包含密码）
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("tokens", Map.of(
                "accessToken",AccessToken ,
                "refreshToken", refreshToken
        ));

        //通过 AccessToken 为key 把 refreshToken 放入 redis
        String key = RedisConstant.LOGIN_USER_KEY + user.getId();
        stringRedisTemplate.opsForValue().set(key, refreshToken, RedisConstant.LOGIN_USER_TTL, TimeUnit.MINUTES);

        return Result.success(result);
    }




    //普通权限
    @GetMapping("/findByNameLike")
    public Result nameLike(@RequestParam String name) {
        return Result.success(userService.findUsersByNameLike(name));
    }

    @PostMapping("/listPage")
    public Result listPage(@RequestBody  PageDTO pageDTO) {
        List<User> list=userService.findUsersByNameLike(pageDTO);
        return Result.success(list);
    }

    @GetMapping("/findById")
    public Result findById(@Min(value = 1, message = "ID必须大于0") @RequestParam Integer id) {
       User user =userService.getById(id);
       user.setManagedWarehouseIds(warehouseUserService.findWarehouseIdByUserId(user.getId()));
       return Result.success(user);
    }

    //管理员权限
    @PostMapping("/save")
    public Result save(@Valid @RequestBody UserDTO userDTO) {
        try {
            userDTO.setPassword(encoder.encode(userDTO.getPassword()));
            User user = new User(userDTO);
            if (userService.save(user)) {
                return Result.success();
            } else
            {
                throw new UserException(CodeConstant.user,UserConstant.FALSE_SAVE);
            }
        } catch (Exception e) {
            throw new UserException(CodeConstant.user,UserConstant.FALSE_SAVE);
        }
    }

    @PutMapping("/mod")
    public Result mod(@RequestBody UserDTO userDTO) {
        try {
            User user = new User(userDTO, userDTO.getId());
            if (userService.updateById(user)) {
                return Result.success();
            } else {
                throw new UserException(CodeConstant.user,UserConstant.FALSE_MOD);
            }
        } catch (Exception e) {
            throw new UserException(CodeConstant.user,UserConstant.FALSE_MOD);
        }
    }


    @DeleteMapping("/delete")
    public Result delete(@RequestParam Integer id) {
        try {
            if (userService.removeById(id))  {
                return Result.success();
            } else {
                throw new UserException(CodeConstant.user,UserConstant.FALSE_DELETE);
            }
        } catch (Exception e) {
            throw new UserException(CodeConstant.user,UserConstant.FALSE_DELETE);
        }
    }

}
