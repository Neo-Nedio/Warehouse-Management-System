package com.example.edmo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.VO.GoodsInWarehouseVO;
import com.example.edmo.pojo.entity.Goods;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GoodsService extends IService<Goods> {
    boolean updateGoodsInWarehouse(Goods goods);

    boolean loginDeleteGoodsById(Integer id);

    Goods findGoodsById(Integer id,List<Integer> managedWarehouseIds);

    List<Goods> findGoodsByNameLike(String name,List<Integer> managedWarehouseIds);

    Page<Goods> findGoodsByNameLike( PageDTO pageDTO,List<Integer> managedWarehouseIds);

    GoodsInWarehouseVO findGoodsByWarehouseId(Integer warehouseId, List<Integer> managedWarehouseIds);

    List<GoodsInWarehouseVO> findGoodsAllByManagedWarehouseIds(List<Integer> managedWarehouseIds);
}
