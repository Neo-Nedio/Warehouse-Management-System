package com.example.edmo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.edmo.annotation.AutoFill;
import com.example.edmo.enumeration.OperationType;
import com.example.edmo.pojo.entity.Goods;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
    @AutoFill(value = OperationType.INSERT)
    @Override
    int insert(Goods entity);


}
