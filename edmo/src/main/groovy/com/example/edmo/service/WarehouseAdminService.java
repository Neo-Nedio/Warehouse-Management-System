package com.example.edmo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.edmo.pojo.DTO.QueryPage;
import com.example.edmo.pojo.entity.Warehouse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface WarehouseAdminService extends IService<Warehouse> {
    List<Warehouse> findUsersByNameLike(String name);

    public Page<Warehouse> findUsersByNameLike(QueryPage queryPage);
}
