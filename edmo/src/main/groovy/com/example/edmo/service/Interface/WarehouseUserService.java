package com.example.edmo.service.Interface;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.edmo.pojo.entity.WarehouseAndUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface WarehouseUserService extends IService<WarehouseAndUser> {

    List<Integer> findWarehouseIdByUserId(Integer userId);

    List<Integer> findUserIdByWarehouseId(Integer warehouseId);

    boolean deleteByWarehouseIdAndUserId(Integer warehouseId, Integer userId);

    WarehouseAndUser findRelationByWarehouseIdAndUserId(Integer warehouseId, Integer userId);
}
