package com.example.edmo.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.edmo.service.Interface.WarehouseUserService;
import com.example.edmo.security.RequireAdmin;
import com.example.edmo.util.Constant.CodeConstant;
import com.example.edmo.util.Constant.JwtConstant;
import com.example.edmo.util.Constant.RedisConstant;
import com.example.edmo.util.Constant.UserConstant;
import com.example.edmo.util.Jwt.JwtUtil;
import cn.hutool.json.JSONUtil;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.DTO.LoginRequest;
import com.example.edmo.pojo.DTO.UserDTO;
import com.example.edmo.pojo.entity.User;
import com.example.edmo.exception.UserException;
import com.example.edmo.service.Interface.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

//todo前端登录页面返回结果与后端不一致，前端申请后端在用户无权限时前端无显示，学习在除日志服务层外其他服务层的redis
@Tag(name = "用户管理", description = "用户相关接口，包括登录、注册、用户信息管理等")
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

    @Operation(summary = "发送验证码", description = "向指定邮箱发送登录验证码，验证码有效期为2分钟，存储在Redis中")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "验证码发送成功"),
            @ApiResponse(responseCode = "400", description = "用户不存在")
    })
    @GetMapping("/code")
    public Result createCode(@Parameter(description = "用户邮箱（必须是已注册的邮箱）", required = true, example = "user@example.com")
                             @Email @RequestParam String email) {
        int code=userService.CreateCode(email);
        if(code==0) throw new UserException(CodeConstant.user,UserConstant.NULL_USER);

        //把验证码保存在redis
        stringRedisTemplate.opsForValue().set(RedisConstant.LOGIN_CODE_KEY +email,String.valueOf(code),RedisConstant.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        return Result.success();
    }

    @Operation(summary = "密码登录", description = "使用邮箱和密码进行登录。密码长度2-10字符。登录成功后返回用户信息（包含管理的仓库ID列表）和双Token（accessToken有效期30分钟，refreshToken有效期7天）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功，返回用户信息和token"),
            @ApiResponse(responseCode = "400", description = "邮箱或密码错误")
    })
    @PostMapping("/loginByPassword")
    public Result loginByPassword(@Valid @RequestBody LoginRequest loginRequest){
        // 根据邮箱查找用户
        User user=userService.findUserByEmail(loginRequest.getEmail());
        //匹配用match
        if (user == null || !encoder.matches(loginRequest.getPassword(),user.getPassword())) {
            throw new UserException(CodeConstant.user,UserConstant.FALSE_EMAIL_OR_PASSWORD);
        }
        return sendToken(user);
    }

    @Operation(summary = "验证码登录", description = "使用邮箱和验证码进行登录。验证码有效期为2分钟，需要先调用发送验证码接口获取验证码。登录成功后返回用户信息（包含管理的仓库ID列表）和双Token（accessToken有效期30分钟，refreshToken有效期7天）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功，返回用户信息和token"),
            @ApiResponse(responseCode = "400", description = "验证码错误或已过期（超过2分钟）")
    })
    @PostMapping("/loginByCode")
    public Result loginByCode(@Valid @RequestBody LoginRequest loginRequest) {

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

        //  验证成功后立即删除验证码（防止重复使用）
        stringRedisTemplate.delete(RedisConstant.LOGIN_CODE_KEY + email);
        return sendToken(user);
    }

    @Operation(summary = "更新密码", description = "通过验证码验证后更新用户密码。验证码有效期为2分钟，需要先调用发送验证码接口获取验证码。新密码长度2-10字符，会自动使用BCrypt加密存储")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "密码更新成功"),
            @ApiResponse(responseCode = "400", description = "验证码错误或已过期（超过2分钟）或用户不存在")
    })
    @PostMapping("/updatePassword")
    @Transactional(rollbackFor = Exception.class)
    public Result updatePassword(@Valid @RequestBody LoginRequest loginRequest) {
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
        if (!userService.updateById(user)) {
            throw new UserException(CodeConstant.user, UserConstant.FALSE_MOD);
        }
        //  验证成功后立即删除验证码（防止重复使用）
        stringRedisTemplate.delete(RedisConstant.LOGIN_CODE_KEY + email);

        return Result.success();
    }

    @Operation(summary = "刷新Token", description = "使用RefreshToken刷新AccessToken。RefreshToken有效期为7天，刷新成功后返回新的双Token（accessToken有效期30分钟，refreshToken有效期7天）和用户信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token刷新成功，返回新的token和用户信息"),
            @ApiResponse(responseCode = "400", description = "Token无效或已过期（超过7天）或用户不存在")
    })
    @PostMapping("/refresh")
    public Result refresh(@Parameter(description = "刷新Token（RefreshToken）", required = true)
                          @RequestParam @NotBlank(message = "RefreshToken不能为空") String RefreshToken) {
        try {
            // 验证Refresh Token是否在Redis中存在
            String refreshTokenKey = RedisConstant.REFRESH_TOKEN_KEY + RefreshToken;
            String userIdStr = stringRedisTemplate.opsForValue().get(refreshTokenKey);
            if (userIdStr == null) {
                throw new UserException(CodeConstant.user, UserConstant.REFRESH_TOKEN_INVALID_OR_EXPIRED);
            }

            // 验证Refresh Token的JWT格式
            Algorithm algorithm = Algorithm.HMAC256(JwtConstant.SECRET_KEY);
            DecodedJWT jwt = JWT.require(algorithm).build().verify(RefreshToken);

            Integer userId = Integer.parseInt(userIdStr);
            Integer jwtUserId = jwt.getClaim("id").asInt();
            
            // 验证Redis中的userId与JWT中的userId是否一致
            if (!userId.equals(jwtUserId)) {
                throw new UserException(CodeConstant.user, UserConstant.FALSE_REFRESH_TOKEN);
            }

            // 获取用户信息
            User user = userService.getById(userId);
            if (user == null) throw new UserException(CodeConstant.user,UserConstant.NULL_USER);
            
            // 删除旧的refreshToken
            stringRedisTemplate.delete(refreshTokenKey);
            
            // 生成新的token并存储到Redis
            return sendToken(user);
        }catch (Exception e) {
            throw new UserException(CodeConstant.user,e.getMessage());
        }
    }

    @Operation(summary = "退出登录", description = "用户退出登录，删除Redis中的token和用户信息")
    @ApiResponse(responseCode = "200", description = "退出登录成功")
    @GetMapping("logOut")
    //required = false：如果没有 token 也不报错（可能已过期）
    public Result logout(@RequestHeader(value = "token", required = false) String token) {
        if (token != null && !token.isEmpty()) {
            // 删除Redis中的accessToken
            String tokenKey = RedisConstant.TOKEN_KEY + token;
            stringRedisTemplate.delete(tokenKey);
        }
        
        return Result.success();
    }


    private Result sendToken(User user)  {
        user.setManagedWarehouseIds(warehouseUserService.findWarehouseIdByUserId(user.getId()));
        // 生成token
        String refreshToken = JwtUtil.createToken(user, JwtConstant.REFRESH_TOKEN_EXPIRE);
        String AccessToken = JwtUtil.createToken(user, JwtConstant.ACCESS_TOKEN_EXPIRE);

        // 将user信息存储到Redis中（使用accessToken作为key）
        String tokenKey = RedisConstant.TOKEN_KEY + AccessToken;
        stringRedisTemplate.opsForValue().set(
            tokenKey,
            JSONUtil.toJsonStr(user),
            RedisConstant.TOKEN_TTL,
            TimeUnit.MINUTES
        );

        // 将refreshToken也存储到Redis中（用于刷新时验证）
        String refreshTokenKey = RedisConstant.REFRESH_TOKEN_KEY + refreshToken;
        stringRedisTemplate.opsForValue().set(
            refreshTokenKey,
            String.valueOf(user.getId()),
            RedisConstant.REFRESH_TOKEN_TTL,
            TimeUnit.MINUTES
        );

        // 返回双token和user（不包含密码，密码被jsonIgnore）
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("tokens", Map.of(
                "accessToken",AccessToken ,
                "refreshToken", refreshToken
        ));

        return Result.success(result);
    }




    @Operation(summary = "根据名称模糊查询用户", description = "根据用户名模糊查询用户列表，支持普通用户权限。用户名长度2-10字符")
    @GetMapping("/findByNameLike")
    public Result nameLike(@Parameter(description = "用户名关键字（支持模糊匹配）", example = "张三")
                           @RequestParam String name) {
        return Result.success(userService.findUsersByNameLike(name));
    }

    @Operation(summary = "分页查询用户", description = "根据条件分页查询用户列表，支持普通用户权限。PageDTO包含：pageSize（默认20）、pageNum（默认1）、param（HashMap，可包含name等查询条件）")
    @PostMapping("/listPage")
    public Result listPage(@RequestBody PageDTO pageDTO) {
        List<User> list=userService.findUsersByNameLike(pageDTO);
        return Result.success(list);
    }

    @Operation(summary = "根据ID查询用户", description = "根据用户ID查询用户详细信息，包括用户基本信息（id、name、email、phone、sex、age、roleId）和管理的仓库ID列表（managedWarehouseIds），支持普通用户权限")
    @GetMapping("/findById")
    public Result findById(@Parameter(description = "用户ID（必须大于0）", required = true, example = "1")
                           @Positive(message = "ID必须大于0") @RequestParam Integer id) {
       User user = userService.getById(id);
       if (user == null) {
           return Result.success(null);
       }
       user.setManagedWarehouseIds(warehouseUserService.findWarehouseIdByUserId(user.getId()));
       return Result.success(user);
    }

    @Operation(summary = "创建用户", description = "管理员创建新用户（需要管理员权限）。UserDTO参数：name（2-10字符）、email（邮箱格式）、phone（手机号格式）、password（2-10字符，会自动BCrypt加密）、sex（0-1，0女1男）、age（16-120）、roleId（1-3）。id字段不需要提供")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "用户创建成功"),
            @ApiResponse(responseCode = "400", description = "创建失败或用户已存在或参数验证失败")
    })
    @PostMapping("/save")
    @RequireAdmin
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

    @Operation(summary = "修改用户", description = "管理员修改用户信息（需要管理员权限）。UserDTO参数：id（必填，必须大于0）、name（2-10字符）、email（邮箱格式）、phone（手机号格式）、password（2-10字符，如果提供会自动BCrypt加密）、sex（0-1）、age（16-120）、roleId（1-3）。注意：如果password为空，不会更新密码字段")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "用户修改成功"),
            @ApiResponse(responseCode = "400", description = "修改失败或用户不存在或参数验证失败")
    })
    @PutMapping("/mod")
    @RequireAdmin
    public Result mod(@Valid @RequestBody UserDTO userDTO) {
        try {
            // 如果提供了密码，则加密；如果为空或null，则不更新密码字段
            String password = userDTO.getPassword();
            if (password != null && !password.trim().isEmpty()) {
                // 有密码，加密后更新
                userDTO.setPassword(encoder.encode(password));
                User user = new User(userDTO, userDTO.getId());
                if (userService.updateById(user)) {
                    return Result.success();
                } else {
                    throw new UserException(CodeConstant.user,UserConstant.FALSE_MOD);
                }
            } else {
                // 密码为空，先查询原用户，然后只更新其他字段（不更新密码）
                User existingUser = userService.getById(userDTO.getId());
                if (existingUser == null) {
                    throw new UserException(CodeConstant.user,UserConstant.NULL_USER);
                }
                // 创建新User对象，但保留原密码
                User user = new User(userDTO, userDTO.getId());
                user.setPassword(existingUser.getPassword()); // 保留原密码
                if (userService.updateById(user)) {
                    return Result.success();
                } else {
                    throw new UserException(CodeConstant.user,UserConstant.FALSE_MOD);
                }
            }
        } catch (Exception e) {
            throw new UserException(CodeConstant.user,e.getMessage());
        }
    }


    @Operation(summary = "删除用户", description = "管理员删除用户（需要管理员权限）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "用户删除成功"),
            @ApiResponse(responseCode = "400", description = "删除失败或用户不存在")
    })
    @DeleteMapping("/delete")
    @RequireAdmin
    public Result delete(@Parameter(description = "用户ID", required = true, example = "1")
                         @Positive(message = "ID必须大于0") @RequestParam Integer id) {
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
