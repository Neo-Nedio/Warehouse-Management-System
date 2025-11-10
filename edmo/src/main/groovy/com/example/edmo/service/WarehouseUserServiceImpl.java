package com.example.edmo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edmo.mapper.WarehouseUserMapper;
import com.example.edmo.pojo.entity.WarehouseAndUser;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WarehouseUserServiceImpl extends ServiceImpl<WarehouseUserMapper, WarehouseAndUser> implements WarehouseUserService {
    @Resource
    private WarehouseUserMapper warehouseUserMapper;

    //用来查找用户可以管理的仓库
    //TODO 学习 流
    @Override
    public List<Integer> findWarehouseIdByUserId(Integer userId) {
        QueryWrapper<WarehouseAndUser> wrapper = new QueryWrapper<>();
        wrapper.select("warehouse_id")
                .eq("user_id", userId);

        // selectObjs 返回 List<Object>，转换为 Integer
        List<Object> objects = warehouseUserMapper.selectObjs(wrapper);
        return objects.stream()
                .map(obj -> (Integer) obj)
                .collect(Collectors.toList());

    }

    @Override
    public List<Integer> findUserIdByWarehouseId(Integer warehouseId) {
        QueryWrapper<WarehouseAndUser> wrapper = new QueryWrapper<>();
        wrapper.select("user_id")
                .eq("warehouse_id", warehouseId);

        // selectObjs 返回 List<Object>，转换为 Integer
        List<Object> objects = warehouseUserMapper.selectObjs(wrapper);
        return objects.stream()
                .map(obj -> (Integer) obj)
                .collect(Collectors.toList());
    }
}
