package com.example.edmo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edmo.pojo.DTO.QueryPage;
import com.example.edmo.pojo.DTO.LoginRequest;
import com.example.edmo.pojo.entity.User;
import com.example.edmo.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    JavaMailSender sender;


    @Override
    public int CreatCode(LoginRequest loginRequest) {
        if(findUserByEmail( loginRequest)==null) return 0;

        String email = loginRequest.getEmail();

        Random random = new Random();
        int code = random.nextInt(900000)+100000;
        SimpleMailMessage message = new SimpleMailMessage();
        //设置邮件标题
        message.setSubject("验证码");
        //设置邮件内容
        message.setText("验证码是"+code);
        //设置邮件发送给谁，可以多个，这里就发给你的QQ邮箱
        message.setTo(email);
        //邮件发送者，这里要与配置文件中的保持一致
        message.setFrom("18276593770@163.com");
        sender.send(message);
        return code;
    }

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
    public User findUserByEmail(LoginRequest loginRequest) {
        QueryWrapper<User> wrapper=Wrappers
                .<User>query()
                .eq("email",loginRequest.getEmail());
        return userMapper.selectOne(wrapper);
    }
}
