package com.example.edmo.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.edmo.entity.DTO.QueryPage;
import com.example.edmo.entity.DTO.LoginRequest;
import com.example.edmo.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService extends IService<User> {
    List<User> findUsersByNameLike(String name);

    Page<User> findUsersByNameLike(QueryPage queryPage);

    User findUserByEmailAndPassword(LoginRequest loginRequest);
}
