package com.example.edmo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.edmo.annotation.AutoFillList;
import com.example.edmo.pojo.entity.User;
import com.example.edmo.service.Interface.OperationLogService;
import com.example.edmo.service.Interface.WarehouseUserService;
import com.example.edmo.util.Constant.CodeConstant;
import com.example.edmo.util.Constant.GoodsConstant;
import com.example.edmo.util.Jwt.UserContext;
import com.example.edmo.annotation.AutoFill;
import com.example.edmo.enumeration.OperationType;
import com.example.edmo.exception.GoodsException;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.DTO.GoodsDTO;
import com.example.edmo.pojo.entity.Goods;
import com.example.edmo.service.Interface.GoodsService;
import com.example.edmo.service.Interface.WarehouseService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//todo 参数校验，实现跨域问题
//todo 检查自动填充加包名为什么不行
//todo 自定义邮箱注解没有效果
@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Resource
    private GoodsService goodsService;

    @Resource
    WarehouseService warehouseService;

    @Resource
    WarehouseUserService warehouseUserService;

    @Resource
    private OperationLogService operationLogService;

    @PostMapping("/save")
    @AutoFill(value = OperationType.INSERT)
    public Result saveGoods(@RequestBody GoodsDTO goodsDTO) {
        try {
            if( warehouseService.getById(goodsDTO.getWarehouseId())==null)
                throw new GoodsException(CodeConstant.goods,GoodsConstant.NULL_WAREHOUSE);
            if(!UserContext.getCurrentUser().getManagedWarehouseIds().contains(goodsDTO.getWarehouseId()))
                throw new GoodsException(CodeConstant.goods,GoodsConstant.NULL_ROLE);

            Goods goods = new Goods(goodsDTO);
            if ( goodsService.save(goods) ){
                //反过来获取id用于插入
                    goodsDTO.setId(goods.getId());
                    operationLogService.addLog(goodsDTO);
                    return Result.success();
                } else {
                    throw new GoodsException(CodeConstant.goods, GoodsConstant.FALSE_SAVE);
                }
            } catch (Exception e) {
                throw new GoodsException(CodeConstant.goods, e.getMessage());
            }
    }

    @PostMapping("/saveListInSameWarehouse")
    @AutoFillList(value = OperationType.INSERT)
    public Result saveGoodsListInSameWarehouse(@RequestBody List<GoodsDTO> goodsDTOList) {
        try {
            //验证
            Integer firstWarehouseId = goodsDTOList.get(0).getWarehouseId();
            for(GoodsDTO goodsDTO:goodsDTOList){
                if(goodsDTO.getWarehouseId() != firstWarehouseId) throw  new GoodsException(CodeConstant.goods,GoodsConstant.WAREHOUSE_ID_NOT_SAME);
            }
            if( warehouseService.getById( goodsDTOList.get(0).getWarehouseId() )==null)
                throw new GoodsException(CodeConstant.goods,GoodsConstant.NULL_WAREHOUSE);
            if(!UserContext.getCurrentUser().getManagedWarehouseIds().contains( goodsDTOList.get(0).getWarehouseId() ) )
                throw new GoodsException(CodeConstant.goods,GoodsConstant.NULL_ROLE);


            List<Goods> goodsList = goodsDTOList.stream()
                    .map(Goods::new)
                    .collect(Collectors.toList());

            //todo
            // 批量插入，ID会自动回填到goodsList中的每个Goods对象
            if (goodsService.saveBatch(goodsList)) {
                for (int i = 0; i < goodsList.size(); i++) {
                    Goods goods = goodsList.get(i);
                    GoodsDTO goodsDTO = goodsDTOList.get(i);
                    goodsDTO.setId(goods.getId());
                }

                // 批量记录操作日志
                operationLogService.batchAddLog(goodsDTOList);
                return Result.success();
            } else {
                throw new GoodsException(CodeConstant.goods, GoodsConstant.FALSE_SAVE);
            }
        } catch (Exception e) {
            throw new GoodsException(CodeConstant.goods, e.getMessage());
        }

    }

    @PutMapping("/mod/message")
    @AutoFill(value = OperationType.UPDATE)
    public Result modMessage(@RequestBody GoodsDTO goodsDTO) {
        try {
            Goods goods = goodsService.getById(goodsDTO.getId());
            if(goods==null)
                throw new GoodsException(CodeConstant.goods,GoodsConstant.NULL_GOODS);
            if( goodsDTO.getWarehouseId()!=null)
                throw new GoodsException(CodeConstant.goods,GoodsConstant.FALSE_OPERATE_UNDATE_WAREHOUSE);
            if(!UserContext.getCurrentUser().getManagedWarehouseIds().contains(goods.getWarehouseId()))
                throw new GoodsException(CodeConstant.goods,GoodsConstant.NULL_ROLE);


            if ( goodsService.updateById(new Goods(goodsDTO))){
                goodsDTO.setWarehouseId(goods.getWarehouseId());
                operationLogService.modMessage(goodsDTO);
                return Result.success();
            } else {
                throw new GoodsException(CodeConstant.goods, GoodsConstant.FALSE_MOD);
            }
        } catch (Exception e) {
            throw new GoodsException(CodeConstant.goods, e.getMessage());
        }
    }

    @PutMapping("/mod/warehouse")
    @AutoFill(value = OperationType.UPDATE)
    public Result modWarehouse(@RequestBody GoodsDTO goodsDTO) {
        try {
            Goods goods = goodsService.getById(goodsDTO.getId());
            if(goods==null)
                throw new GoodsException(CodeConstant.goods,GoodsConstant.NULL_GOODS);
            if( warehouseService.getById(goodsDTO.getWarehouseId())==null)
                throw new GoodsException(CodeConstant.goods,GoodsConstant.NULL_WAREHOUSE);
            List<Integer> requiredIds = Arrays.asList(goodsDTO.getWarehouseId(), goods.getWarehouseId());
            if(!UserContext.getCurrentUser().getManagedWarehouseIds().containsAll(requiredIds))
                throw new GoodsException(CodeConstant.goods,GoodsConstant.NULL_ROLE);

            if ( goodsService.updateGoodsInWarehouse(new Goods(goodsDTO))){
                operationLogService.modWarehouse(goods,goodsDTO);
                return Result.success();
            } else {
                throw new GoodsException(CodeConstant.goods, GoodsConstant.FALSE_MOD);
            }
        } catch (Exception e) {
            throw new GoodsException(CodeConstant.goods, e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    @AutoFill(value = OperationType.UPDATE)
    public Result deleteGoods(@RequestBody GoodsDTO goodsDTO) {
        try {
            Goods goods = goodsService.getById(goodsDTO.getId());
            if(goods==null || goods.getStatus()==0)
                throw new GoodsException(CodeConstant.goods,GoodsConstant.NULL_GOODS);

            Integer warehouseId = goods.getWarehouseId();
            if(!UserContext.getCurrentUser().getManagedWarehouseIds().contains(warehouseId))
                throw new GoodsException(CodeConstant.goods,GoodsConstant.NULL_ROLE);


            if (goodsService.loginDeleteGoodsById(goodsDTO)){
                operationLogService.delete(goods,goodsDTO);
                return Result.success();
            }else  {
                throw new GoodsException(CodeConstant.goods, GoodsConstant.FALSE_DELETE);
            }
        }catch (Exception e){
            throw new GoodsException(CodeConstant.goods, e.getMessage());
        }
    }

    //查找，不需要第二级权限

    //查询商品，只能查自己管理的库
    @GetMapping("/findGoodsByID")
    public Result findGoodsByID(@RequestParam Integer id) {
        Goods goods = goodsService.findGoodsById(id,UserContext.getCurrentUser().getManagedWarehouseIds());
        return Result.success(goods);
    }

    @GetMapping("/findByNameLike")
    public Result findByNameLike(@RequestParam String name) {
        return Result.success(goodsService.findGoodsByNameLike(name,UserContext.getCurrentUser().getManagedWarehouseIds()));
    }

    @PostMapping("/listPage")
    public Result listPage(@RequestBody PageDTO pageDTO) {
        Page<Goods> page=goodsService.findGoodsByNameLike(pageDTO,UserContext.getCurrentUser().getManagedWarehouseIds());
        return Result.success(page.getRecords());
    }

    //根据仓库查找
    @GetMapping("/findGoodsByWarehouseID")
    public Result findGoodsByWarehouseID(@RequestParam Integer id) {
        return Result.success(goodsService.findGoodsByWarehouseId(id,UserContext.getCurrentUser().getManagedWarehouseIds()));
    }

    @GetMapping("/findGoodsAllByManagedWarehouseIds")
    public Result findGoodsAllByManagedWarehouseIds() {
        return Result.success(goodsService.findGoodsAllByManagedWarehouseIds(UserContext.getCurrentUser().getManagedWarehouseIds()));
    }

    @GetMapping("/findGoodsByNameLikeInByManagedWarehouseIds")
    public Result findGoodsByNameLikeInByManagedWarehouseIds(@RequestParam String name) {
        return Result.success(goodsService.findGoodsByNameLikeInByManagedWarehouseIds(name,UserContext.getCurrentUser().getManagedWarehouseIds()));
    }

    @PostMapping("/findGoodsByAnyCondition")
    public Result findGoodsByAnyCondition(@RequestBody GoodsDTO goodsDTO) {
        return Result.success(goodsService.findGoodsByAnyCondition(goodsDTO,UserContext.getCurrentUser().getManagedWarehouseIds()));
    }
}
