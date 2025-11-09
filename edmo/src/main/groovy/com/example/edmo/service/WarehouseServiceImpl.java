package com.example.edmo.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edmo.mapper.WarehouseMapper;
import com.example.edmo.pojo.entity.Warehouse;
import org.springframework.stereotype.Service;

@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {
}
