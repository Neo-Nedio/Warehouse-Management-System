package com.example.edmo.service.Interface;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService extends IService<User> {
    int CreatCode(String email);

    List<User> findUsersByNameLike(String name);

    List<User> findUsersByNameLike(PageDTO pageDTO);

    User findUserByEmail(String email);

    List<User> findUsersByIds(List<Integer> ids);
}
