package com.example.edmo.service.Interface;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.entity.Warehouse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface WarehouseService extends IService<Warehouse> {
    List<Warehouse> findWarehousesByNameLike(String name);

    Page<Warehouse> findWarehousesByNameLike(PageDTO pageDTO);

    List<Warehouse> findWarehousesById(List<Integer> ids);
}
