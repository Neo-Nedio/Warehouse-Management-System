package com.example.edmo.controller;

import com.example.edmo.exception.BaseException;
import com.example.edmo.service.Interface.WarehouseUserService;
import com.example.edmo.util.Constant.CodeConstant;
import com.example.edmo.util.Constant.RedisConstant;
import com.example.edmo.util.Constant.UserConstant;
import com.example.edmo.util.Jwt.JwtUtil;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.DTO.LoginRequest;
import com.example.edmo.pojo.DTO.UserDTO;
import com.example.edmo.pojo.entity.User;
import com.example.edmo.exception.UserException;
import com.example.edmo.service.Interface.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


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

    @Resource
    ObjectMapper  objectMapper;

    @PostMapping("/code")
    public Result createCode(@RequestBody LoginRequest loginRequest) {

        String email = loginRequest.getEmail();

        int code=userService.CreatCode(loginRequest);
        if(code==0) throw new UserException(CodeConstant.user,UserConstant.NULL_USER);

        //把验证码保存在redis
        stringRedisTemplate.opsForValue().set(RedisConstant.LOGIN_CODE_KEY +email,String.valueOf(code),RedisConstant.LOGIN_CODE_TTL, TimeUnit.MINUTES);


        return Result.success();
    }

    @PostMapping("/loginByPassword")
    public Result loginByPassword(@RequestBody LoginRequest loginRequest){
        // 根据邮箱查找用户
        User user=userService.findUserByEmail(loginRequest);
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

        User user=userService.findUserByEmail(loginRequest);//获取验证码时已经检验用户是否存在
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
        User user=userService.findUserByEmail(loginRequest);
        user.setPassword(encoder.encode(password));
        mod(new UserDTO(user));
        return Result.success();
    }


    private Result sendToken(User user)  {
        user.setManagedWarehouseIds(warehouseUserService.findWarehouseIdByUserId(user.getId()));
        // 生成token
        String token = JwtUtil.createToken(user);

        // 返回结果（不包含密码）
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("token", token);

        //把user放入redis
        String key = RedisConstant.LOGIN_USER_KEY + token;
        String userJson = null;
        try {
            userJson = objectMapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new BaseException(401,e.getMessage());
        }

        stringRedisTemplate.opsForValue().set(key, userJson, RedisConstant.LOGIN_USER_TTL, TimeUnit.MINUTES);

        return Result.success(result);
    }




    //普通权限
    @PostMapping("/findByNameLike")
    public Result nameLike(@RequestParam String name) {
        return Result.success(userService.findUsersByNameLike(name));
    }

    @PostMapping("/listPage")
    public Result listPage(@RequestBody PageDTO pageDTO) {
        List<User> list=userService.findUsersByNameLike(pageDTO);
        return Result.success(list);
    }

    @PostMapping("/findById")
    public Result findById(@RequestParam Integer id) {
        return Result.success(userService.getById(id));
    }

    //管理员权限
    @PostMapping("/save")
    public Result save(@RequestBody UserDTO userDTO) {
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

    @PostMapping("/mod")
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


    @PostMapping("/delete")
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
