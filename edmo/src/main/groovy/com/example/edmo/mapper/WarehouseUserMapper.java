package com.example.edmo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.edmo.pojo.entity.WarehouseAndUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WarehouseUserMapper extends BaseMapper<WarehouseAndUser> {
}
