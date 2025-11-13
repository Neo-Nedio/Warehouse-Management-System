package com.example.edmo.service.Interface;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.edmo.pojo.DTO.GoodsDTO;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.VO.GoodsInWarehouseVO;
import com.example.edmo.pojo.entity.Goods;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GoodsService extends IService<Goods> {
    boolean updateGoodsInWarehouse(Goods goods);

    boolean loginDeleteGoodsById(GoodsDTO goodsDTO);

    List<Goods> findGoodsByIds(List<GoodsDTO> goodsDTOList);

    Goods findGoodsById(Integer id,List<Integer> managedWarehouseIds);

    List<Goods> findGoodsByNameLike(String name,List<Integer> managedWarehouseIds);

    Page<Goods> findGoodsByNameLike( PageDTO pageDTO,List<Integer> managedWarehouseIds);

    GoodsInWarehouseVO findGoodsByWarehouseId(Integer warehouseId, List<Integer> managedWarehouseIds);

    List<GoodsInWarehouseVO> findGoodsAllByManagedWarehouseIds(List<Integer> managedWarehouseIds);

    List<GoodsInWarehouseVO> findGoodsByNameLikeInByManagedWarehouseIds(String name,List<Integer> managedWarehouseIds);

    List<Goods> findGoodsByAnyCondition(GoodsDTO goodsDTO, List<Integer> managedWarehouseIds);
}
