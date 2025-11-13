package com.example.edmo.service.Interface;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.DTO.LoginRequest;
import com.example.edmo.pojo.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService extends IService<User> {
    int CreatCode(LoginRequest loginRequest);

    List<User> findUsersByNameLike(String name);

    Page<User> findUsersByNameLike(PageDTO pageDTO);

    User findUserByEmail(LoginRequest loginRequest);

    List<User> findUsersByIds(List<Integer> ids);
}
