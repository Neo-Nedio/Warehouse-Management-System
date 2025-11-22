package com.example.edmo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.edmo.util.Constant.CodeConstant;
import com.example.edmo.util.Constant.ValidationConstant;
import com.example.edmo.util.Constant.WarehouseConstant;
import com.example.edmo.exception.WarehouseException;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.DTO.WarehouseAndUserDTO;
import com.example.edmo.pojo.DTO.WarehouseDTO;
import com.example.edmo.pojo.VO.UserHaveWarehouseVO;
import com.example.edmo.pojo.VO.WarehouseHaveUserVO;
import com.example.edmo.pojo.entity.User;
import com.example.edmo.pojo.entity.Warehouse;
import com.example.edmo.pojo.entity.WarehouseAndUser;
import com.example.edmo.service.Interface.UserService;
import com.example.edmo.service.Interface.WarehouseService;
import com.example.edmo.service.Interface.WarehouseUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "仓库管理", description = "仓库相关接口，包括仓库的增删改查、仓库与用户关系管理等（需要管理员权限）")
@Slf4j
@Validated
@RestController
@RequestMapping("/warehouse/admin")
public class WarehouseAdminController {
    @Resource
    private WarehouseService warehouseService;

    @Resource
    private WarehouseUserService warehouseUserService;

    @Resource
    private UserService userService;

    @Operation(summary = "创建仓库", description = "创建新仓库（需要管理员权限）。WarehouseDTO参数：name（2-10字符，必填）、description（4-30字符，必填）。id字段不需要提供，会自动生成")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "仓库创建成功"),
            @ApiResponse(responseCode = "400", description = "创建失败或参数验证失败")
    })
    @PostMapping("/save")
    public Result save(@Valid @RequestBody WarehouseDTO warehouseDTO) {
        try {
            if ( warehouseService.save(new Warehouse(warehouseDTO))) {
                return Result.success();
            } else {
                throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_SAVE);
            }
        } catch (Exception e) {
            throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_SAVE);
        }
    }

    @Operation(summary = "修改仓库", description = "修改仓库信息（需要管理员权限）。WarehouseDTO参数：id（必填，必须大于0）、name（2-10字符）、description（4-30字符）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "仓库修改成功"),
            @ApiResponse(responseCode = "400", description = "修改失败或仓库不存在或参数验证失败")
    })
    @PutMapping("/mod")
    public Result mod(@Valid @RequestBody WarehouseDTO warehouseDTO) {
        try {
            if ( warehouseService.updateById(new Warehouse(warehouseDTO))) {
                return Result.success();
            } else {
                throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_MOD);
            }
        } catch (Exception e) {
            throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_MOD);
        }
    }


    @Operation(summary = "删除仓库", description = "根据ID删除仓库（需要管理员权限）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "仓库删除成功"),
            @ApiResponse(responseCode = "400", description = "删除失败或仓库不存在")
    })
    @DeleteMapping("/delete")
    public Result delete(@Parameter(description = "仓库ID（必须大于0）", required = true, example = "1")
                        @Positive(message = ValidationConstant.ID) @RequestParam Integer id) {
        try {
            if (warehouseService.removeById(id)) {
                return Result.success();
            } else {
                throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_DELETE);
            }
        } catch (Exception e) {
            throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_DELETE);
        }
    }



    @Operation(summary = "根据名称模糊查询仓库", description = "根据仓库名称模糊查询仓库列表，按id降序排列。仓库名称长度2-10字符")
    @GetMapping("/findByNameLike")
    public Result nameLike(@Parameter(description = "仓库名称关键字（支持模糊匹配）", example = "仓库A")
                           @RequestParam String name) {
        return Result.success(warehouseService.findWarehousesByNameLike(name));
    }

    @Operation(summary = "分页查询仓库", description = "根据条件分页查询仓库列表，按id降序排列。PageDTO参数：pageSize（默认20）、pageNum（默认1）、param（HashMap，可包含name字段用于模糊查询仓库名称）。如果param中包含name且不为空，则按名称模糊查询；否则查询所有仓库")
    @PostMapping("/listPage")
    public Result listPage(@RequestBody PageDTO pageDTO) {
        Page<Warehouse> page= warehouseService.findWarehousesByNameLike(pageDTO);
        return Result.success(page.getRecords());
    }

    @Operation(summary = "根据ID查询仓库", description = "根据仓库ID查询仓库详细信息。返回字段：id、name（2-10字符）、description（4-30字符）")
    @GetMapping("/findById")
    public Result findById(@Parameter(description = "仓库ID（必须大于0）", required = true, example = "1")
                           @Positive(message = ValidationConstant.ID) @RequestParam Integer id) {
        return Result.success(warehouseService.getById(id));
    }


    @Operation(summary = "创建仓库与用户关系", description = "建立仓库与用户的管理关系（需要管理员权限）。WarehouseAndUserDTO参数：warehouseId（正整数，必填，必须是已存在的仓库）、userId（正整数，必填，必须是已存在的用户）。Id字段不需要提供，会自动生成。如果关系已存在会报错（DuplicateKeyException）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "关系创建成功"),
            @ApiResponse(responseCode = "400", description = "创建失败或仓库或用户不存在或关系已存在")
    })
    @PostMapping("/saveRelation")
    @Transactional(rollbackFor = Exception.class)
    public Result saveRelation(@Valid @RequestBody WarehouseAndUserDTO warehouseAndUserDTO) {
        try {
            //检查用户和仓库是不是存在
            if(userService.getById(warehouseAndUserDTO.getUserId()) == null ||
                    warehouseService.getById(warehouseAndUserDTO.getWarehouseId()) == null) {
                throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.NULL_WAREHOUSE_OR_USER);
            }

            if (warehouseUserService.save(new WarehouseAndUser(warehouseAndUserDTO))) {
                return Result.success();
            } else {
                throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_SAVE);
            }
        } catch (DuplicateKeyException e) {
            throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_SAVE);
        } catch (Exception e) {
            throw new WarehouseException(CodeConstant.warehouse, e.getMessage());
        }
    }

    @Operation(summary = "修改仓库与用户关系", description = "修改仓库与用户的管理关系（需要管理员权限）。WarehouseAndUserDTO参数：Id（必填，必须大于0，关系ID）、warehouseId（正整数，必填，必须是已存在的仓库）、userId（正整数，必填，必须是已存在的用户）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "关系修改成功"),
            @ApiResponse(responseCode = "400", description = "修改失败或关系不存在或仓库或用户不存在")
    })
    @PutMapping("/modRelation")
    @Transactional(rollbackFor = Exception.class)
    public Result modRelation(@Valid @RequestBody WarehouseAndUserDTO warehouseAndUserDTO) {
        try {
            //检查用户和仓库是不是存在
            if(userService.getById(warehouseAndUserDTO.getUserId())==null || warehouseService.getById(warehouseAndUserDTO.getWarehouseId())==null)
                throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.NULL_WAREHOUSE_OR_USER);

            if ( warehouseUserService.updateById(new WarehouseAndUser(warehouseAndUserDTO))) {
                    return Result.success();
                } else {
                    throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_MOD);
                }
        } catch (Exception e) {
            throw new WarehouseException(CodeConstant.warehouse, e.getMessage());
        }
    }


    @Operation(summary = "删除仓库与用户关系", description = "删除仓库与用户的管理关系（需要管理员权限）。根据仓库ID和用户ID删除关系")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "关系删除成功"),
            @ApiResponse(responseCode = "400", description = "删除失败或关系不存在")
    })
    @DeleteMapping("/deleteRelation")
    public Result deleteRelation(@Parameter(description = "用户ID（必须大于0）", required = true, example = "1")
                                 @Positive(message = ValidationConstant.ID) @RequestParam Integer userID,
                                 @Parameter(description = "仓库ID（必须大于0）", required = true, example = "1")
                                 @Positive(message = ValidationConstant.ID) @RequestParam Integer warehouseID) {
        try {
            if (warehouseUserService.deleteByWarehouseIdAndUserId(warehouseID, userID)) {
                return Result.success();
            } else {
                throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_DELETE);
            }
        } catch (Exception e) {
            throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_DELETE);
        }
    }

    @Operation(summary = "查询所有关系", description = "查询所有仓库与用户的关系（需要管理员权限）。返回List<WarehouseAndUser>，包含所有关系记录（Id、warehouseId、userId）")
    @GetMapping("findAllRelation")
    public Result findAllRelation() {
        return Result.success(warehouseUserService.list());
    }
    @Operation(summary = "查询指定关系", description = "根据用户ID和仓库ID查询关系（需要管理员权限）。返回WarehouseAndUser对象，如果关系不存在则返回null")
    @GetMapping("findRelation")
    public Result findRelation(@Parameter(description = "用户ID（必须大于0）", required = true, example = "1")
                               @Positive(message = ValidationConstant.ID) @RequestParam Integer userID,
                               @Parameter(description = "仓库ID（必须大于0）", required = true, example = "1")
                               @Positive(message = ValidationConstant.ID) @RequestParam Integer warehouseID) {
        return Result.success(warehouseUserService.findRelationByWarehouseIdAndUserId(warehouseID, userID));
    }

    @Operation(summary = "按用户查看关系", description = "根据用户分页查询，返回用户及其管理的仓库列表（需要管理员权限）。PageDTO参数：pageSize（默认20）、pageNum（默认1）、param（HashMap，可包含name字段用于模糊查询用户名）。返回List<UserHaveWarehouseVO>，每个元素包含用户信息（id、name、email、phone、sex、age、roleId，不包含password）和该用户管理的仓库列表（managedWarehouse）。如果用户没有管理的仓库，则返回空列表（避免SQL错误）")
    @PostMapping("/findRelationForUser")
    public Result findRelationForUser(@RequestBody PageDTO pageDTO) {
        List<User> users = userService.findUsersByNameLike(pageDTO);

        List<UserHaveWarehouseVO> userHaveWarehouseVOS =new ArrayList<>();
        for(User user:users){
            List<Integer> ids= user.getManagedWarehouseIds();
            //todo 修复：检查空列表，避免生成 id IN () 的无效SQL
            List<Warehouse> warehouses;
            if (ids != null && !ids.isEmpty()) {
                warehouses = warehouseService.findWarehousesById(ids);
            } else {
                warehouses = new ArrayList<>(); // 返回空列表
            }
            userHaveWarehouseVOS.add(new UserHaveWarehouseVO(user,warehouses));
        }
        return Result.success(userHaveWarehouseVOS);
    }

    @Operation(summary = "按仓库查看关系", description = "根据仓库分页查询，返回仓库及其关联的用户列表（需要管理员权限）。PageDTO参数：pageSize（默认20）、pageNum（默认1）、param（HashMap，可包含name字段用于模糊查询仓库名称）。返回List<WarehouseHaveUserVO>，每个元素包含仓库信息（id、name、description）和该仓库关联的用户列表（users）。如果仓库没有关联的用户，则返回空列表（避免SQL错误）")
    @PostMapping("/findRelationForWarehouse")
    public Result findRelationForWarehouse(@RequestBody PageDTO pageDTO) {
        List<Warehouse> warehouses =  warehouseService.findWarehousesByNameLike(pageDTO).getRecords();

        List<WarehouseHaveUserVO> warehouseHaveUserVOS =new ArrayList<>();
        for(Warehouse warehouse:warehouses){
            List<Integer> ids = warehouseUserService.findUserIdByWarehouseId(warehouse.getId());
            //todo 修复：检查空列表，避免生成 id IN () 的无效SQL
            List<User> users;
            if (ids != null && !ids.isEmpty()) {
                users = userService.findUsersByIds(ids);
            } else {
                users = new ArrayList<>(); // 返回空列表
            }
            warehouseHaveUserVOS.add(new WarehouseHaveUserVO(warehouse,users));
        }
        return Result.success(warehouseHaveUserVOS);
    }


}
