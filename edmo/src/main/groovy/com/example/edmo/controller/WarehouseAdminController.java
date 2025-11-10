package com.example.edmo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.edmo.Constant.CodeConstant;
import com.example.edmo.Constant.WarehouseConstant;
import com.example.edmo.exception.WarehouseException;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.DTO.WarehouseAndUserDTO;
import com.example.edmo.pojo.DTO.WarehouseDTO;
import com.example.edmo.pojo.VO.UserHaveWarehouseVO;
import com.example.edmo.pojo.VO.WarehouseHaveUserVO;
import com.example.edmo.pojo.entity.User;
import com.example.edmo.pojo.entity.Warehouse;
import com.example.edmo.pojo.entity.WarehouseAndUser;
import com.example.edmo.service.UserService;
import com.example.edmo.service.WarehouseService;
import com.example.edmo.service.WarehouseUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/warehouse/admin")
public class WarehouseAdminController {
    @Resource
    private WarehouseService warehouseService;

    @Resource
    private WarehouseUserService warehouseUserService;

    @Resource
    private UserService userService;

    @PostMapping("/save")
    public Result save(@RequestBody WarehouseDTO warehouseDTO) {
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

    @PostMapping("/mod")
    public Result mod(@RequestBody WarehouseDTO warehouseDTO) {
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



    @PostMapping("/findByNameLike")
    public Result nameLike(@RequestParam String name) {
        return Result.success(warehouseService.findWarehousesByNameLike(name));
    }

    @PostMapping("/listPage")
    public Result listPage(@RequestBody PageDTO pageDTO) {
        Page<Warehouse> page= warehouseService.findWarehousesByNameLike(pageDTO);
        return Result.success(page.getRecords());
    }

    @PostMapping("/findById")
    public Result findById(@RequestParam Integer id) {
        return Result.success(warehouseService.getById(id));
    }




    @PostMapping("/saveRelation")
    public Result saveRelation(@RequestBody WarehouseAndUserDTO warehouseAndUserDTO) {
        try {
            //检查用户和仓库是不是存在
            if(userService.getById(warehouseAndUserDTO.getUserId())!=null
                    && warehouseService.getById(warehouseAndUserDTO.getWarehouseId())!=null){
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
                    && warehouseService.getById(warehouseAndUserDTO.getWarehouseId())!=null)
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

    @PostMapping("/findRelationForUser")
    public Result findRelationForUser(@RequestBody PageDTO pageDTO ) {
        List<User> users = userService.findUsersByNameLike(pageDTO).getRecords();

        List<UserHaveWarehouseVO> userHaveWarehouseVOS =new ArrayList<>();
        for(User user:users){
            List<Integer> ids= warehouseUserService.findWarehouseIdByUserId(user.getId());
            List<Warehouse> warehouses= warehouseService.findWarehousesById(ids);
            userHaveWarehouseVOS.add(new UserHaveWarehouseVO(user,warehouses));
        }
        return Result.success(userHaveWarehouseVOS);
    }

    @PostMapping("/findRelationForWarehouse")
    public Result findRelationForWarehouse(@RequestBody PageDTO pageDTO) {
        List<Warehouse> warehouses =  warehouseService.findWarehousesByNameLike(pageDTO).getRecords();

        List<WarehouseHaveUserVO> warehouseHaveUserVOS =new ArrayList<>();
        for(Warehouse warehouse:warehouses){
            List<Integer> ids = warehouseUserService.findUserIdByWarehouseId(warehouse.getId());
            List<User> users =userService.findUsersByIds(ids);
            warehouseHaveUserVOS.add(new WarehouseHaveUserVO(warehouse,users));
        }
        return Result.success(warehouseHaveUserVOS);
    }
}
