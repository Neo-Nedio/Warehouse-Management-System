package com.example.edmo.service.Interface;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.edmo.pojo.DTO.GoodsDTO;
import com.example.edmo.pojo.DTO.OperationLogDTO;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.DTO.TimeDTO;
import com.example.edmo.pojo.entity.Goods;
import com.example.edmo.pojo.entity.OperationLog;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OperationLogService extends IService<OperationLog> {
    void addLog(GoodsDTO goodsDTO);

    void batchAddLog(List<GoodsDTO> goodsDTOList);

    void modMessage(GoodsDTO goodsDTO);

    void modWarehouse(Goods goods, GoodsDTO goodsDTO);

    void delete(Goods goods,GoodsDTO goodsDTO);

    List<OperationLog> findByType(String type);

    Page<OperationLog> findByType(String type, PageDTO pageDTO);

    List<OperationLog> findByUpdateName(String name);

    List<OperationLog> findAllByTypeAndWarehouseId(String type, int warehouseId);

    List<OperationLog> findByWarehouseId(int warehouseId);

    List<OperationLog> findByGoodsName(String goodsName);

    List<OperationLog> findByGoodsId(Integer goodsId);

    List<OperationLog> findByTime(TimeDTO timeDTO);

    List<OperationLog> findByAnyCondition(OperationLogDTO operationLogDTO);
}
