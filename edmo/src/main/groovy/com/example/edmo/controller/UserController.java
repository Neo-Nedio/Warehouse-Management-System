package com.example.edmo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.edmo.Constant.CodeConstant;
import com.example.edmo.Constant.UserConstant;
import com.example.edmo.Jwt.JwtUtil;
import com.example.edmo.pojo.DTO.QueryPage;
import com.example.edmo.pojo.DTO.LoginRequest;
import com.example.edmo.pojo.DTO.QueryUser;
import com.example.edmo.pojo.entity.User;
import com.example.edmo.exception.UserException;
import com.example.edmo.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/code")
    public Result createCode(@RequestBody LoginRequest loginRequest,
                                          HttpSession Session) {

        String email = loginRequest.getEmail();

        int code=userService.CreatCode(loginRequest);
        if(code==0) throw new UserException(CodeConstant.user,UserConstant.NULL_USER);

        Session.setAttribute("email",email);
        Session.setAttribute("code",code);

        return Result.success();
    }

    @PostMapping("/loginByPassword")
    public Result loginByPassword(@RequestBody LoginRequest loginRequest){
        // 根据邮箱查找用户
        User user=userService.findUserByEmail(loginRequest);
        if (user == null || !user.getPassword().equals(loginRequest.getPassword()))
            throw new UserException(CodeConstant.user,UserConstant.FALSE_EMAIL_OR_PASSWORD);


        return sendToken(user);
    }

    @PostMapping("/loginByCode")
    public Result loginByCode(@RequestBody LoginRequest loginRequest,
                                        HttpSession session) {

        String email = loginRequest.getEmail();
        int code = loginRequest.getCode();

        // === 关键：安全地获取 Session 属性 ===
        if (session.getAttribute("code") == null ||  session.getAttribute("email") == null) {
            throw new UserException(CodeConstant.user,UserConstant.NEED_CODE);
        }


        int sessionCode = (int) session.getAttribute("code");
        String sessionEmail = (String) session.getAttribute("email");


        if(code!=sessionCode) throw new UserException(CodeConstant.user,UserConstant.FALSE_CODE);
        if(!email.equals(sessionEmail)) throw new UserException(CodeConstant.user,UserConstant.NEED_CODE);

        User user=userService.findUserByEmail(loginRequest);//获取验证码时已经检验用户是否存在
        return sendToken(user);
    }

    @PostMapping("/updatePassword")
    public Result updatePassword(@RequestBody LoginRequest loginRequest,
                                 HttpSession session) {
        String email = loginRequest.getEmail();
        int code = loginRequest.getCode();

        // === 关键：安全地获取 Session 属性 ===
        if (session.getAttribute("code") == null ||  session.getAttribute("email") == null) {
            throw new UserException(CodeConstant.user,UserConstant.NEED_CODE);
        }


        int sessionCode = (int) session.getAttribute("code");
        String sessionEmail = (String) session.getAttribute("email");


        if(code!=sessionCode) throw new UserException(CodeConstant.user,UserConstant.FALSE_CODE);
        if(!email.equals(sessionEmail)) throw new UserException(CodeConstant.user,UserConstant.NEED_CODE);

        String password = loginRequest.getPassword();
        User user=userService.findUserByEmail(loginRequest);
        user.setPassword(password);
        mod(new QueryUser(user));
        return Result.success();
    }


    private Result sendToken(User user){
        // 生成token
        String token = JwtUtil.createToken(user);

        // 返回结果（不包含密码）
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("token", token);

        return Result.success(result);
    }




    //普通权限
    @PostMapping("/findByNameLike")
    public Result nameLike(@RequestParam String name) {
        return Result.success(userService.findUsersByNameLike(name));
    }

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPage queryPage) {
        Page<User> page=userService.findUsersByNameLike(queryPage);
        return Result.success(page.getRecords());
    }

    @PostMapping("/findById")
    public Result findById(@RequestParam Integer id) {
        return Result.success(userService.getById(id));
    }

    //管理员权限
    @PostMapping("/save")
    public Result save(@RequestBody QueryUser  queryUser) {
        try {
            User user = new User(queryUser);
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
    public Result mod(@RequestBody QueryUser  queryUser) {
        try {
            User user = new User(queryUser,queryUser.getId());
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
