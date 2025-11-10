package com.example.edmo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edmo.mapper.WarehouseAdminMapper;
import com.example.edmo.pojo.DTO.QueryPage;
import com.example.edmo.pojo.entity.Warehouse;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseAdminServiceImpl extends ServiceImpl<WarehouseAdminMapper, Warehouse> implements WarehouseAdminService {
    @Resource
    WarehouseAdminMapper warehouseAdminMapper;
    @Override
    public List<Warehouse> findUsersByNameLike(String name) {
        QueryWrapper<Warehouse> wrapper = Wrappers
                .<Warehouse>query()
                .like("name",name)
                .orderByDesc("id");
        return warehouseAdminMapper.selectList(wrapper);
    }

    @Override
    public Page<Warehouse> findUsersByNameLike(QueryPage queryPage) {
        String name=(String) queryPage.getParam().get("name");

        QueryWrapper<Warehouse> wrapper = Wrappers
                .<Warehouse>query()
                .like("name",name)
                .orderByDesc("id");

        Page<Warehouse> page=new Page<>();
        page.setSize(queryPage.getPageSize());
        page.setCurrent(queryPage.getPageNum());

        return warehouseAdminMapper.selectPage(page,wrapper);
    }
}
