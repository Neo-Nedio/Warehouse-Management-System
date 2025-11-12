package com.example.edmo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.edmo.pojo.entity.Goods;
import org.springframework.stereotype.Service;

@Service
public interface GoodsService extends IService<Goods> {
    boolean updateGoodsInWarehouse(Goods goods);
}
