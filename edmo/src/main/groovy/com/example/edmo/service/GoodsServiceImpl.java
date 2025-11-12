package com.example.edmo.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edmo.mapper.GoodsMapper;
import com.example.edmo.pojo.entity.Goods;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;


@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {
    @Resource
    private GoodsMapper goodsMapper;


    @Override
    public boolean updateGoodsInWarehouse(Goods goods) {
        UpdateWrapper<Goods> wrapper = Wrappers
                .<Goods>update()
                .eq("id", goods.getId())
                .set("warehouse_id", goods.getWarehouseId());
        return goodsMapper.update(goods, wrapper) > 0;
    }
}
