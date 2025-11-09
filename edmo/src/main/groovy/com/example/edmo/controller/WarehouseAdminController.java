package com.example.edmo.controller;

import com.example.edmo.Constant.CodeConstant;
import com.example.edmo.Constant.WarehouseConstant;
import com.example.edmo.exception.WarehouseException;
import com.example.edmo.pojo.DTO.QueryWarehouse;
import com.example.edmo.pojo.entity.Warehouse;
import com.example.edmo.service.WarehouseService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/warehouse/admin")
public class WarehouseAdminController {
    @Resource
    private WarehouseService warehouseService;

    @PostMapping("/save")
    public Result save(@RequestBody QueryWarehouse queryWarehouse) {
        try {
            if ( warehouseService.save(new Warehouse(queryWarehouse))) {
                return Result.success();
            } else {
                throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_SAVE);
            }
        } catch (Exception e) {
            throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_SAVE);
        }
    }

    @PostMapping("/mod")
    public Result mod(@RequestBody QueryWarehouse queryWarehouse) {
        try {
            if ( warehouseService.updateById(new Warehouse(queryWarehouse))) {
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
            if (warehouseService.removeById(id)) {
                return Result.success();
            } else {
                throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_DELETE);
            }
        } catch (Exception e) {
            throw new WarehouseException(CodeConstant.warehouse, WarehouseConstant.FALSE_DELETE);
        }
    }


}
