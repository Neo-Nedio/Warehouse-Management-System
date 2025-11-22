package com.example.edmo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edmo.exception.UserException;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.entity.User;
import com.example.edmo.mapper.UserMapper;
import com.example.edmo.service.Interface.UserService;
import com.example.edmo.service.Interface.WarehouseUserService;
import com.example.edmo.util.Constant.CodeConstant;
import com.example.edmo.util.Constant.UserConstant;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private WarehouseUserService  warehouseUserService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    JavaMailSender sender;


    @Override
    public int CreateCode(String email) {
        if(findUserByEmail( email)==null) return 0;

        // 频率限制检查
        String limitKey = "user:code:limit:" + email;

        // 检查1分钟内是否已发送
        String lastSendTime = stringRedisTemplate.opsForValue().get(limitKey);
        if (lastSendTime != null) {
            throw new UserException(CodeConstant.user, UserConstant.FALSE_GET);
        }



        SecureRandom secureRandom = new SecureRandom();
        int code = secureRandom.nextInt(900000) + 100000;
        SimpleMailMessage message = new SimpleMailMessage();
        //设置邮件标题
        message.setSubject("验证码");
        //设置邮件内容
        message.setText("验证码是" + code + "，有效期为2分钟，请尽快使用");
        //设置邮件发送给谁，可以多个，这里就发给你的QQ邮箱
        message.setTo(email);
        //邮件发送者，这里要与配置文件中的保持一致
        message.setFrom("18276593770@163.com");
        sender.send(message);

        stringRedisTemplate.opsForValue().set(limitKey, String.valueOf(System.currentTimeMillis()),
                1, TimeUnit.MINUTES);

        return code;
    }

    @Override
    public List<User> findUsersByNameLike(String name) {
        QueryWrapper<User> wrapper = Wrappers
                .<User>query()
                .like("name",name)
                .orderByDesc("id");
        return fillManagedWarehouseIds(userMapper.selectList(wrapper));
    }

    @Override
    public List<User> findUsersByNameLike(PageDTO pageDTO) {
        QueryWrapper<User> wrapper = Wrappers
                .<User>query()
                .orderByDesc("id");
        
        //todo如果param不为null且包含name参数，则添加like条件，防止刚进入系统时无条件发生错误
        if (pageDTO.getParam() != null && pageDTO.getParam().containsKey("name")) {
            String name = (String) pageDTO.getParam().get("name");
            if (name != null && !name.isEmpty()) {
                wrapper.like("name", name);
            }
        }

        Page<User> page=new Page<>();
        page.setSize(pageDTO.getPageSize());
        page.setCurrent(pageDTO.getPageNum());

        List<User> users = userMapper.selectPage(page,wrapper).getRecords();

        return fillManagedWarehouseIds(users);
    }

    //用来邮箱验证
    @Override
    public User findUserByEmail(String email) {
        QueryWrapper<User> wrapper=Wrappers
                .<User>query()
                .eq("email",email);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public List<User> findUsersByIds(List<Integer> ids) {
        QueryWrapper<User> wrapper = Wrappers
                .<User>query()
                .in("id", ids)
                .orderByDesc("id");
        return fillManagedWarehouseIds(userMapper.selectList(wrapper));
    }

    //todo
    private List<User> fillManagedWarehouseIds(List<User> users) {
        return users.stream()
                .peek(user -> user.setManagedWarehouseIds(
                        warehouseUserService.findWarehouseIdByUserId(user.getId())
                ))
                .collect(Collectors.toList());
    }
}
