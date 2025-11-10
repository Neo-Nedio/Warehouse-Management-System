package com.example.edmo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.edmo.Constant.CodeConstant;
import com.example.edmo.Constant.WarehouseConstant;
import com.example.edmo.exception.WarehouseException;
import com.example.edmo.pojo.DTO.QueryPage;
import com.example.edmo.pojo.DTO.WarehouseAndUserDTO;
import com.example.edmo.pojo.DTO.WarehouseDTO;
import com.example.edmo.pojo.entity.Warehouse;
import com.example.edmo.pojo.entity.WarehouseAndUser;
import com.example.edmo.service.UserService;
import com.example.edmo.service.WarehouseAdminService;
import com.example.edmo.service.WarehouseUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/warehouse/admin")
public class WarehouseAdminController {
    @Resource
    private WarehouseAdminService warehouseAdminService;

    @Resource
    private WarehouseUserService warehouseUserService;

    @Resource
    private UserService userService;

    @PostMapping("/save")
    public Result save(@RequestBody WarehouseDTO warehouseDTO) {
        try {
            if ( warehouseAdminService.save(new Warehouse(warehouseDTO))) {
                return Result.success();
            } else {
                throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_SAVE);
            }
        } catch (Exception e) {
            throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_SAVE);
        }
    }

    @PostMapping("/mod")
    public Result mod(@RequestBody WarehouseDTO warehouseDTO) {
        try {
            if ( warehouseAdminService.updateById(new Warehouse(warehouseDTO))) {
                return Result.success();
            } else {
                throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_MOD);
            }
        } catch (Exception e) {
            throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_MOD);
        }
    }


    @PostMapping("/delete")
    public Result delete(@RequestParam Integer id) {
        try {
            if (warehouseAdminService.removeById(id)) {
                return Result.success();
            } else {
                throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_DELETE);
            }
        } catch (Exception e) {
            throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_DELETE);
        }
    }



    @PostMapping("/findByNameLike")
    public Result nameLike(@RequestParam String name) {
        return Result.success(warehouseAdminService.findUsersByNameLike(name));
    }

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPage queryPage) {
        Page<Warehouse> page=warehouseAdminService.findUsersByNameLike(queryPage);
        return Result.success(page.getRecords());
    }

    @PostMapping("/findById")
    public Result findById(@RequestParam Integer id) {
        return Result.success(warehouseAdminService.getById(id));
    }




    @PostMapping("/saveRelation")
    public Result saveRelation(@RequestBody WarehouseAndUserDTO warehouseAndUserDTO) {
        try {
            //检查用户和仓库是不是存在
            if(userService.getById(warehouseAndUserDTO.getUserId())!=null
                    && warehouseAdminService.getById(warehouseAndUserDTO.getWarehouseId())!=null){
                if ( warehouseUserService.save(new WarehouseAndUser(warehouseAndUserDTO))) {
                    return Result.success();
                } else {
                    throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_SAVE);
                }
            }throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_SAVE);
        } catch (Exception e) {
            throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_SAVE);
        }
    }

    @PostMapping("/modRelation")
    public Result modRelation(@RequestBody WarehouseAndUserDTO warehouseAndUserDTO) {
        try {
            //检查用户和仓库是不是存在
            if(userService.getById(warehouseAndUserDTO.getUserId())!=null
                    && warehouseAdminService.getById(warehouseAndUserDTO.getWarehouseId())!=null)
            {
                if ( warehouseUserService.updateById(new WarehouseAndUser(warehouseAndUserDTO))) {
                    return Result.success();
                } else {
                    throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_MOD);
                }
            }throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_SAVE);
        } catch (Exception e) {
            throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_MOD);
        }
    }


    @PostMapping("/deleteRelation")
    public Result deleteRelation(@RequestParam Integer id) {
        try {
            if (warehouseUserService.removeById(id)) {
                return Result.success();
            } else {
                throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_DELETE);
            }
        } catch (Exception e) {
            throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_DELETE);
        }
    }
}
