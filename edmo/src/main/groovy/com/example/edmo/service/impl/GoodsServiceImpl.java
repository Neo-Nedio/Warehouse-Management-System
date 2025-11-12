package com.example.edmo.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edmo.mapper.GoodsMapper;
import com.example.edmo.mapper.WarehouseAdminMapper;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.VO.GoodsInWarehouseVO;
import com.example.edmo.pojo.entity.Goods;
import com.example.edmo.pojo.entity.Warehouse;
import com.example.edmo.service.GoodsService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {
    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private WarehouseAdminMapper warehouseAdminMapper;



    @Override
    public boolean updateGoodsInWarehouse(Goods goods) {
        UpdateWrapper<Goods> wrapper = Wrappers
                .<Goods>update()
                .eq("id", goods.getId())
                .set("warehouse_id", goods.getWarehouseId());
        return goodsMapper.update(goods, wrapper) > 0;
    }

    @Override
    public boolean loginDeleteGoodsById(Integer id) {
        UpdateWrapper<Goods> wrapper = Wrappers
                .<Goods>update()
                .eq("id", id)
                .set("status", 0);
        return goodsMapper.update(wrapper) > 0;
    }

    @Override
    public Goods findGoodsById(Integer id,List<Integer> managedWarehouseIds) {
        Wrapper<Goods> wrapper = Wrappers
                .<Goods>query()
                .eq("id", id)
                .eq("status", 1)
                .in("warehouse_id", managedWarehouseIds);
        return goodsMapper.selectOne(wrapper);
    }

    @Override
    public List<Goods> findGoodsByNameLike(String name,List<Integer> managedWarehouseIds) {
        Wrapper<Goods> wrapper = Wrappers
                .<Goods>query()
                .like("name", name)
                .eq("status", 1)
                .in("warehouse_id", managedWarehouseIds)
                .orderByDesc("id");
        return goodsMapper.selectList(wrapper);
    }

    @Override
    public Page<Goods> findGoodsByNameLike(PageDTO pageDTO,List<Integer> managedWarehouseIds) {
        String name=(String) pageDTO.getParam().get("name");
        Wrapper<Goods> wrapper = Wrappers
                .<Goods>query()
                .like("name", name)
                .eq("status", 1)
                .in("warehouse_id", managedWarehouseIds)
                .orderByDesc("id");

        Page<Goods> page=new Page<>();
        page.setSize(pageDTO.getPageSize());
        page.setCurrent(pageDTO.getPageNum());

        return goodsMapper.selectPage(page,wrapper);
    }

    @Override
    public GoodsInWarehouseVO findGoodsByWarehouseId(Integer warehouseId, List<Integer> managedWarehouseIds) {
        GoodsInWarehouseVO  goodsInWarehouseVO = new GoodsInWarehouseVO();
        BeanUtils.copyProperties(warehouseAdminMapper.selectById(warehouseId),goodsInWarehouseVO);

        Wrapper<Goods> wrapper = Wrappers
                .<Goods>query()
                .eq("warehouse_id", warehouseId)
                .eq("status", 1)
                .in("warehouse_id", managedWarehouseIds);

        goodsInWarehouseVO.setGoods(goodsMapper.selectList(wrapper));
        return goodsInWarehouseVO;
    }

    @Override
    public List<GoodsInWarehouseVO> findGoodsAllByManagedWarehouseIds(List<Integer> managedWarehouseIds) {
        QueryWrapper<Warehouse> wrapper2=Wrappers
                .<Warehouse>query()
                //列表用in
                .in("id",managedWarehouseIds)
                .orderByDesc("id");
        List<Warehouse> warehouses = warehouseAdminMapper.selectList(wrapper2);

        return fillGoods(warehouses);
    }

    private List<GoodsInWarehouseVO> fillGoods(List<Warehouse> warehouses){
        List<GoodsInWarehouseVO> goodsInWarehouseVOS=new ArrayList<>();
        for(Warehouse warehouse:warehouses){
            Wrapper<Goods> wrapper = Wrappers
                    .<Goods>query()
                    .in("warehouse_id", warehouse.getId())
                    .eq("status", 1);

            GoodsInWarehouseVO goodsInWarehouseVO=new GoodsInWarehouseVO();
            List<Goods> goodsList = goodsMapper.selectList(wrapper);
            BeanUtils.copyProperties(warehouse,goodsInWarehouseVO);
            goodsInWarehouseVO.setGoods(goodsList);
            goodsInWarehouseVOS.add(goodsInWarehouseVO);
        }
        return goodsInWarehouseVOS;
    }

}
