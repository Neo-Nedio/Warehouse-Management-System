package com.example.edmo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.edmo.Jwt.JwtUtil;
import com.example.edmo.entity.DTO.QueryPage;
import com.example.edmo.entity.DTO.LoginRequest;
import com.example.edmo.entity.User;
import com.example.edmo.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody LoginRequest loginRequest){
        // 根据邮箱查找用户
        User user=userService.findUserByEmailAndPassword(loginRequest);
        if(user==null) return Result.fail("邮箱或密码错误");

        // 生成token
        String token = JwtUtil.createToken(user);

        // 返回结果（不包含密码）
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("token", token);

        return Result.success(result);
    }


    @PostMapping("/save")
    public Result save(@RequestBody User user) {
        userService.save(user);
        return Result.success();
    }

    @PostMapping("/mod")
    public Result mod(@RequestBody User user) {
        userService.updateById(user);
        return Result.success();
    }

    @PostMapping("/saveOrMod")
    public Result saveOrMod(@RequestBody User user) {
        userService.saveOrUpdate(user);
        return Result.success();
    }

    @PostMapping("/delete")
    public boolean delete(@RequestParam Integer id) {
        return userService.removeById(id);
    }

    @PostMapping("/findByNameLike")
    public List<User> nameLike(@RequestParam String name) {
        return userService.findUsersByNameLike(name);
    }

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPage queryPage) {
        Page<User> page=userService.findUsersByNameLike(queryPage);

        return Result.success(page.getTotal(),page.getRecords());
    }
}
