package com.example.edmo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.edmo.Constant.CodeConstant;
import com.example.edmo.Constant.GoodsConstant;
import com.example.edmo.Jwt.UserContext;
import com.example.edmo.annotation.AutoFill;
import com.example.edmo.enumeration.OperationType;
import com.example.edmo.exception.goodsException;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.DTO.goodsDTO;
import com.example.edmo.pojo.entity.Goods;
import com.example.edmo.service.GoodsService;
import com.example.edmo.service.WarehouseService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Resource
    private GoodsService goodsService;

    @Resource
    WarehouseService warehouseService;

    @PostMapping("/save")
    @AutoFill(value = OperationType.INSERT)
    public Result saveGoods(@RequestBody goodsDTO goodsDTO) {
        try {
            if( warehouseService.getById(goodsDTO.getWarehouseId())==null)
                throw new goodsException(CodeConstant.goods,GoodsConstant.NULL_WAREHOUSE);
            if(!UserContext.getCurrentUser().getManagedWarehouseIds().contains(goodsDTO.getWarehouseId()))
                throw new goodsException(CodeConstant.goods,GoodsConstant.NULL_ROLE);

            if ( goodsService.save(new Goods(goodsDTO))){
                    return Result.success();
                } else {
                    throw new goodsException(CodeConstant.goods, GoodsConstant.FALSE_SAVE);
                }
            } catch (Exception e) {
                throw new goodsException(CodeConstant.goods, e.getMessage());
            }

    }

    @PostMapping("/mod/message")
    @AutoFill(value = OperationType.UPDATE)
    public Result modMessage(@RequestBody goodsDTO goodsDTO) {
        try {
            if(goodsService.getById(goodsDTO.getId())==null)
                throw new goodsException(CodeConstant.goods,GoodsConstant.NULL_GOODS);
            if( goodsDTO.getWarehouseId()!=null)
                throw new goodsException(CodeConstant.goods,GoodsConstant.FALSE_OPERATE_UNDATE_WAREHOUSE);
            if(!UserContext.getCurrentUser().getManagedWarehouseIds().contains(goodsDTO.getWarehouseId()))
                throw new goodsException(CodeConstant.goods,GoodsConstant.NULL_ROLE);


            if ( goodsService.updateById(new Goods(goodsDTO))){
                return Result.success();
            } else {
                throw new goodsException(CodeConstant.goods, GoodsConstant.FALSE_MOD);
            }
        } catch (Exception e) {
            throw new goodsException(CodeConstant.goods, e.getMessage());
        }
    }

    @PostMapping("/mod/warehouse")
    @AutoFill(value = OperationType.UPDATE)
    public Result modWarehouse(@RequestBody goodsDTO goodsDTO) {
        try {
            if(goodsService.getById(goodsDTO.getId())==null)
                throw new goodsException(CodeConstant.goods,GoodsConstant.NULL_GOODS);
            if( warehouseService.getById(goodsDTO.getWarehouseId())==null)
                throw new goodsException(CodeConstant.goods,GoodsConstant.NULL_WAREHOUSE);
            if(!UserContext.getCurrentUser().getManagedWarehouseIds().contains(goodsDTO.getWarehouseId()))
                throw new goodsException(CodeConstant.goods,GoodsConstant.NULL_ROLE);


            if ( goodsService.updateGoodsInWarehouse(new Goods(goodsDTO))){
                return Result.success();
            } else {
                throw new goodsException(CodeConstant.goods, GoodsConstant.FALSE_MOD);
            }
        } catch (Exception e) {
            throw new goodsException(CodeConstant.goods, e.getMessage());
        }
    }

    @PostMapping("/delete")
    public Result deleteGoods(@RequestParam Integer id) {
        try {
            if(goodsService.getById(id)==null)
                throw new goodsException(CodeConstant.goods,GoodsConstant.NULL_GOODS);

            Integer warehouseId = goodsService.getById(id).getWarehouseId();
            if(!UserContext.getCurrentUser().getManagedWarehouseIds().contains(warehouseId))
                throw new goodsException(CodeConstant.goods,GoodsConstant.NULL_ROLE);

            if ( goodsService.loginDeleteGoodsById(id)){
                return Result.success();
            }else  {
                throw new goodsException(CodeConstant.goods, GoodsConstant.FALSE_DELETE);
            }
        }catch (Exception e){
            throw new goodsException(CodeConstant.goods, e.getMessage());
        }
    }

    //查找，不需要第二级权限

    //查询商品，只能查自己管理的库
    @PostMapping("/findGoodsByID")
    public Result findGoodsByID(@RequestParam Integer id) {
        Goods goods = goodsService.findGoodsById(id,UserContext.getCurrentUser().getManagedWarehouseIds());
        return Result.success(goods);
    }

    @PostMapping("/findByNameLike")
    public Result findByNameLike(@RequestParam String name) {
        return Result.success(goodsService.findGoodsByNameLike(name,UserContext.getCurrentUser().getManagedWarehouseIds()));
    }

    @PostMapping("/listPage")
    public Result listPage(@RequestBody PageDTO pageDTO) {
        Page<Goods> page=goodsService.findGoodsByNameLike(pageDTO,UserContext.getCurrentUser().getManagedWarehouseIds());
        return Result.success(page.getRecords());
    }

    //根据仓库查找
    @PostMapping("/findGoodsByWarehouseID")
    public Result findGoodsByWarehouseID(@RequestParam Integer id) {
        return Result.success(goodsService.findGoodsByWarehouseId(id,UserContext.getCurrentUser().getManagedWarehouseIds()));
    }

    @GetMapping("/findGoodsAllByManagedWarehouseIds")
    public Result findGoodsAllByManagedWarehouseIds() {
        return Result.success(goodsService.findGoodsAllByManagedWarehouseIds(UserContext.getCurrentUser().getManagedWarehouseIds()));
    }
}
