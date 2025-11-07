package com.example.edmo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edmo.entity.DTO.QueryPage;
import com.example.edmo.entity.DTO.LoginRequest;
import com.example.edmo.entity.User;
import com.example.edmo.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private UserMapper userMapper;


    @Override
    public List<User> findUsersByNameLike(String name) {
        QueryWrapper<User> wrapper = Wrappers
                .<User>query()
                .like("name",name)
                .orderByDesc("id");
        return userMapper.selectList(wrapper);
    }

    @Override
    public Page<User> findUsersByNameLike(QueryPage queryPage) {
        String name=(String) queryPage.getParam().get("name");

        QueryWrapper<User> wrapper = Wrappers
                .<User>query()
                .like("name",name)
                .orderByDesc("id");

        Page<User> page=new Page<>();
        page.setSize(queryPage.getPageSize());
        page.setCurrent(queryPage.getPageNum());

        return userMapper.selectPage(page,wrapper);
    }

    @Override
    public User findUserByEmailAndPassword(LoginRequest loginRequest) {
        QueryWrapper<User> wrapper=Wrappers
                .<User>query()
                .eq("email",loginRequest.getEmail())
                .eq("password",loginRequest.getPassword());
        return userMapper.selectOne(wrapper);
    }
}
