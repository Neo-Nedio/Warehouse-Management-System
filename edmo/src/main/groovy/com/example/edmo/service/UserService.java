package com.example.edmo.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.edmo.pojo.DTO.QueryPage;
import com.example.edmo.pojo.DTO.LoginRequest;
import com.example.edmo.pojo.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService extends IService<User> {
    int CreatCode(LoginRequest loginRequest);

    List<User> findUsersByNameLike(String name);

    Page<User> findUsersByNameLike(QueryPage queryPage);

    User findUserByEmail(LoginRequest loginRequest);
}
