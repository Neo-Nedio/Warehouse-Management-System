package com.example.edmo.security;

import com.example.edmo.pojo.entity.User;
import com.example.edmo.service.Interface.UserService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    public UserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 这里把 username 当作 email 来查找用户，与当前登录逻辑保持一致
        User user = userService.findUserByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return new UserDetails(user);
    }
}


