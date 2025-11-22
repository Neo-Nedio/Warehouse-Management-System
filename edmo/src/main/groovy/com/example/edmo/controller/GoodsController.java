package com.example.edmo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.edmo.annotation.AutoFillList;
import com.example.edmo.pojo.entity.User;
import com.example.edmo.service.Interface.OperationLogService;
import com.example.edmo.service.Interface.WarehouseUserService;
import com.example.edmo.util.Constant.CodeConstant;
import com.example.edmo.util.Constant.GoodsConstant;
import com.example.edmo.util.Constant.ValidationConstant;
import com.example.edmo.util.Jwt.UserContext;
import com.example.edmo.annotation.AutoFill;
import com.example.edmo.enumeration.OperationType;
import com.example.edmo.exception.GoodsException;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.DTO.GoodsDTO;
import com.example.edmo.pojo.entity.Goods;
import com.example.edmo.service.Interface.GoodsService;
import com.example.edmo.service.Interface.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "商品管理", description = "商品相关接口，包括商品的增删改查、批量操作等。所有操作都需要用户有管理对应仓库的权限，只能操作自己管理的仓库中的商品")
@Validated
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

    @Operation(summary = "创建商品", description = "创建新商品，只能在自己管理的仓库中创建。GoodsDTO参数：name（2-10字符，必填）、price（正整数，必填）、number（正整数，必填）、warehouseId（正整数，必填，必须是已存在的仓库且用户有管理权限）。createTime、updateTime、createUser、updateUser会自动填充，id会自动生成。创建成功后会自动记录操作日志（INSERT类型）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "商品创建成功"),
            @ApiResponse(responseCode = "400", description = "创建失败或仓库不存在或无管理权限")
    })
    @PostMapping("/save")
    @AutoFill(value = OperationType.INSERT)
    @Transactional(rollbackFor = Exception.class)
    public Result saveGoods(@Valid @RequestBody GoodsDTO goodsDTO) {
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

    @Operation(summary = "批量创建商品", description = "在同一仓库中批量创建商品，所有商品必须在同一仓库。要求：1. 所有商品的warehouseId必须相同；2. 仓库必须存在；3. 用户必须有管理该仓库的权限。GoodsDTO参数要求同创建商品接口。创建成功后会批量记录操作日志（INSERT类型）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "商品批量创建成功"),
            @ApiResponse(responseCode = "400", description = "创建失败或仓库不一致或仓库不存在或无管理权限")
    })
    @PostMapping("/saveListInSameWarehouse")
    @AutoFillList(value = OperationType.INSERT)
    @Transactional(rollbackFor = Exception.class)
    public Result saveGoodsListInSameWarehouse(@RequestBody @NotEmpty(message = "商品列表不能为空") @Valid List<GoodsDTO> goodsDTOList) {
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

    @Operation(summary = "修改商品信息", description = "修改商品的基本信息（不包括仓库），只能修改自己管理的仓库中的商品。GoodsDTO参数：id（必填，必须大于0）、name（2-10字符）、price（正整数）、number（正整数）。注意：warehouseId不能在此接口修改，如果提供了warehouseId会报错，需要通过mod/warehouse接口修改仓库。updateTime、updateUser会自动填充。修改成功后会记录操作日志（UPDATE类型，操作类型为修改信息）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "商品信息修改成功"),
            @ApiResponse(responseCode = "400", description = "修改失败或商品不存在或商品已删除（status=0）或无管理权限或尝试修改仓库ID")
    })
    @PutMapping("/mod/message")
    @AutoFill(value = OperationType.UPDATE)
    @Transactional(rollbackFor = Exception.class)
    public Result modMessage(@Valid @RequestBody GoodsDTO goodsDTO) {
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

    @Operation(summary = "修改商品仓库", description = "将商品从一个仓库转移到另一个仓库。要求：1. 商品必须存在且未删除（status=1）；2. 原仓库和目标仓库都必须存在；3. 用户必须同时有管理原仓库和目标仓库的权限。GoodsDTO参数：id（必填，必须大于0）、warehouseId（必填，目标仓库ID，必须大于0）。修改成功后会记录操作日志（UPDATE类型，操作类型为修改仓库）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "商品仓库修改成功"),
            @ApiResponse(responseCode = "400", description = "修改失败或商品不存在或仓库不存在或无管理权限（需要同时管理原仓库和目标仓库）")
    })
    @PutMapping("/mod/warehouse")
    @AutoFill(value = OperationType.UPDATE)
    @Transactional(rollbackFor = Exception.class)
    public Result modWarehouse(@Valid @RequestBody GoodsDTO goodsDTO) {
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

    @Operation(summary = "删除商品", description = "逻辑删除商品（将status设为0），只能删除自己管理的仓库中的商品。要求：商品必须存在且未删除（status=1）。GoodsDTO参数：id（必填，必须大于0）。删除成功后会记录操作日志（UPDATE类型，操作类型为删除）。注意：删除后商品不会在查询结果中显示，但数据仍在数据库中")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "商品删除成功"),
            @ApiResponse(responseCode = "400", description = "删除失败或商品不存在或商品已删除（status=0）或无管理权限")
    })
    @DeleteMapping("/delete")
    @AutoFill(value = OperationType.UPDATE)
    @Transactional(rollbackFor = Exception.class)
    public Result deleteGoods(@Valid @RequestBody GoodsDTO goodsDTO) {
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

    @Operation(summary = "根据ID查询商品", description = "根据商品ID查询商品信息，只能查询自己管理的仓库中的商品。只返回未删除的商品（status=1）。返回字段：id、name、price、number、warehouseId、createTime、createUser、updateTime、updateUser、status")
    @GetMapping("/findGoodsByID")
    public Result findGoodsByID(@Parameter(description = "商品ID（必须大于0）", required = true, example = "1")
                                @Positive(message = ValidationConstant.ID) @RequestParam Integer id) {
        Goods goods = goodsService.findGoodsById(id,UserContext.getCurrentUser().getManagedWarehouseIds());
        return Result.success(goods);
    }

    @Operation(summary = "根据名称模糊查询商品", description = "根据商品名称模糊查询，只能查询自己管理的仓库中的商品。只返回未删除的商品（status=1），按id降序排列。商品名称长度2-10字符")
    @GetMapping("/findByNameLike")
    public Result findByNameLike(@Parameter(description = "商品名称关键字（支持模糊匹配）", example = "商品A")
                                @RequestParam String name) {
        return Result.success(goodsService.findGoodsByNameLike(name,UserContext.getCurrentUser().getManagedWarehouseIds()));
    }

    @Operation(summary = "分页查询商品", description = "根据条件分页查询商品列表，只能查询自己管理的仓库中的商品。只返回未删除的商品（status=1），按id降序排列。PageDTO参数：pageSize（默认20）、pageNum（默认1）、param（HashMap，可包含name等查询条件）。返回结果包含：records（商品列表）、total（总记录数）、pages（总页数）、current（当前页）、size（每页大小）")
    @PostMapping("/listPage")
    public Result listPage(@RequestBody PageDTO pageDTO) {
        Page<Goods> page=goodsService.findGoodsByNameLike(pageDTO,UserContext.getCurrentUser().getManagedWarehouseIds());
        //todo 返回分页信息，包含数据列表和分页信息
        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pages", page.getPages());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        return Result.success(result);
    }

    @Operation(summary = "根据仓库ID查询商品", description = "查询指定仓库中的所有商品，只能查询自己管理的仓库。返回GoodsInWarehouseVO对象，包含仓库信息和该仓库下的所有商品列表。只返回未删除的商品（status=1），按id降序排列")
    @GetMapping("/findGoodsByWarehouseID")
    public Result findGoodsByWarehouseID(@Parameter(description = "仓库ID（必须大于0）", required = true, example = "1")
                                         @Positive(message = ValidationConstant.ID) @RequestParam Integer id) {
        return Result.success(goodsService.findGoodsByWarehouseId(id,UserContext.getCurrentUser().getManagedWarehouseIds()));
    }

    @Operation(summary = "查询所有管理的商品", description = "查询当前用户管理的所有仓库中的商品。返回List<GoodsInWarehouseVO>，每个元素包含仓库信息和该仓库下的商品列表。只返回未删除的商品（status=1），按id降序排列")
    @GetMapping("/findGoodsAllByManagedWarehouseIds")
    public Result findGoodsAllByManagedWarehouseIds() {
        return Result.success(goodsService.findGoodsAllByManagedWarehouseIds(UserContext.getCurrentUser().getManagedWarehouseIds()));
    }

    @Operation(summary = "在管理的仓库中按名称查询", description = "在当前用户管理的所有仓库中，根据商品名称模糊查询。返回List<GoodsInWarehouseVO>，每个元素包含仓库信息和匹配的商品列表。只返回未删除的商品（status=1），按id降序排列。商品名称长度2-10字符")
    @GetMapping("/findGoodsByNameLikeInByManagedWarehouseIds")
    public Result findGoodsByNameLikeInByManagedWarehouseIds(@Parameter(description = "商品名称关键字（支持模糊匹配）", example = "商品A")
                                                              @RequestParam String name) {
        return Result.success(goodsService.findGoodsByNameLikeInByManagedWarehouseIds(name,UserContext.getCurrentUser().getManagedWarehouseIds()));
    }

    @Operation(summary = "任意条件查询商品", description = "根据多个条件组合查询商品，只能查询自己管理的仓库中的商品。GoodsDTO查询条件：id（精确匹配）、name（模糊匹配）、price（精确匹配）、number（精确匹配）、warehouseId（精确匹配）、createUser（模糊匹配）、updateUser（模糊匹配）、startCreateTime/endCreateTime（创建时间范围）、startUpdateTime/endUpdateTime（更新时间范围）。只返回未删除的商品（status=1），按id降序排列。所有条件都是可选的，可以任意组合")
    @PostMapping("/findGoodsByAnyCondition")
    public Result findGoodsByAnyCondition(@Valid @RequestBody GoodsDTO goodsDTO) {
        return Result.success(goodsService.findGoodsByAnyCondition(goodsDTO,UserContext.getCurrentUser().getManagedWarehouseIds()));
    }
}
