package com.example.edmo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.edmo.pojo.DTO.WarehouseAndUserDTO;
import com.example.edmo.pojo.entity.WarehouseAndUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface WarehouseUserService extends IService<WarehouseAndUser> {
    WarehouseAndUser findUserByWarehouseAndUser(WarehouseAndUserDTO warehouseAndUserDTO);

    List<Integer> findWarehouseIdByUserId(Integer userId);
}
