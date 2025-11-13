package com.example.edmo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edmo.mapper.WarehouseAdminMapper;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.entity.Warehouse;
import com.example.edmo.service.Interface.WarehouseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseAdminMapper, Warehouse> implements WarehouseService {
    @Resource
    WarehouseAdminMapper warehouseAdminMapper;
    @Override
    public List<Warehouse> findWarehousesByNameLike(String name) {
        QueryWrapper<Warehouse> wrapper = Wrappers
                .<Warehouse>query()
                .like("name",name)
                .orderByDesc("id");
        return warehouseAdminMapper.selectList(wrapper);
    }

    @Override
    public Page<Warehouse> findWarehousesByNameLike(PageDTO pageDTO) {
        String name=(String) pageDTO.getParam().get("name");

        QueryWrapper<Warehouse> wrapper = Wrappers
                .<Warehouse>query()
                .like("name",name)
                .orderByDesc("id");

        Page<Warehouse> page=new Page<>();
        page.setSize(pageDTO.getPageSize());
        page.setCurrent(pageDTO.getPageNum());

        return warehouseAdminMapper.selectPage(page,wrapper);
    }

    @Override
    public List<Warehouse> findWarehousesById(List<Integer> ids) {
        QueryWrapper<Warehouse> wrapper=Wrappers
                .<Warehouse>query()
                //列表用in
                .in("id",ids)
                .orderByDesc("id");
        return warehouseAdminMapper.selectList(wrapper);
    }
}
