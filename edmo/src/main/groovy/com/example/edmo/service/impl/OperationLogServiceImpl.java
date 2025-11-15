package com.example.edmo.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edmo.mapper.OperationLogMapper;
import com.example.edmo.pojo.DTO.GoodsDTO;
import com.example.edmo.pojo.DTO.OperationLogDTO;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.DTO.TimeDTO;
import com.example.edmo.pojo.entity.Goods;
import com.example.edmo.pojo.entity.OperationLog;
import com.example.edmo.service.Interface.OperationLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {
    @Resource
    private OperationLogMapper operationLogMapper;

    @Override
    public void addLog(GoodsDTO goodsDTO) {
        OperationLog operationLog = new OperationLog();
        operationLog.addLog(goodsDTO);
        operationLogMapper.insert(operationLog);
    }

    @Override
    public void batchAddLog(List<GoodsDTO> goodsDTOList) {
        /*List<OperationLog> operationLogList = new ArrayList<>();
        for (GoodsDTO goodsDTO : goodsDTOList) {
            OperationLog operationLog = new OperationLog();
            operationLog.addLog(goodsDTO);
            operationLogList.add(operationLog);
        }*/

        //todo使用流
        List<OperationLog> operationLogList = goodsDTOList.stream()
                .map(goodsDTO -> {
                    OperationLog operationLog = new OperationLog();
                    operationLog.addLog(goodsDTO);
                    return operationLog;
                })
                .collect(Collectors.toList());
        operationLogMapper.insert(operationLogList);
    }

    @Override
    public void modMessage(GoodsDTO goodsDTO) {
        OperationLog operationLog = new OperationLog();
        operationLog.modMessage(goodsDTO);
        operationLogMapper.insert(operationLog);
    }

    @Override
    public void modWarehouse(Goods goods,GoodsDTO goodsDTO) {
        OperationLog operationLog = new OperationLog();
        operationLog.modWarehouse(goods,goodsDTO);
        operationLogMapper.insert(operationLog);
    }

    @Override
    public void delete(Goods  goods,GoodsDTO goodsDTO) {
        OperationLog operationLog = new OperationLog();
        operationLog.delete(goods,goodsDTO);
        operationLogMapper.insert(operationLog);
    }

    @Override
    public List<OperationLog> findByType(String type) {
        Wrapper<OperationLog> wrapper = Wrappers
                .<OperationLog>query()
                .eq("operate_type",type)
                .orderByDesc("id");
        return operationLogMapper.selectList(wrapper);
    }

    @Override
    public Page<OperationLog> findByType(String type, PageDTO pageDTO) {
        Wrapper<OperationLog> wrapper = Wrappers
                .<OperationLog>query()
                .eq("operate_type",type)
                .orderByDesc("id");

        Page<OperationLog> page= new Page<>();
        page.setSize(pageDTO.getPageSize());
        page.setCurrent(pageDTO.getPageNum());
        return operationLogMapper.selectPage(page,wrapper);
    }

    @Override
    public List<OperationLog> findByUpdateName(String name) {
        Wrapper<OperationLog> wrapper = Wrappers
                .<OperationLog>query()
                .eq("update_user",name);
        return operationLogMapper.selectList(wrapper);
    }

    @Override
    public List<OperationLog> findAllByTypeAndWarehouseId(String type, int warehouseId) {
        QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("operate_type", type)
                .and(wrapper -> wrapper
                        .eq("former_warehouse_id", warehouseId)
                        .or()
                        .eq("new_warehouse_id", warehouseId));
        return operationLogMapper.selectList(queryWrapper);
    }

    @Override
    public List<OperationLog> findByWarehouseId(int warehouseId) {
        Wrapper<OperationLog> wrapper = Wrappers
                .<OperationLog>query()
                .eq("former_warehouse_id",warehouseId)
                .or()
                .eq("new_warehouse_id",warehouseId);
        return operationLogMapper.selectList(wrapper);
    }

    @Override
    public List<OperationLog> findByGoodsName(String goodsName) {
        Wrapper<OperationLog> wrapper = Wrappers
                .<OperationLog>query()
                .like("goods_name",goodsName);
        return operationLogMapper.selectList(wrapper);
    }

    @Override
    public List<OperationLog> findByGoodsId(Integer goodsId) {
        Wrapper<OperationLog> wrapper = Wrappers
                .<OperationLog>query()
                .eq("goods_id",goodsId);
        return operationLogMapper.selectList(wrapper);
    }

    @Override
    public List<OperationLog> findByTime(TimeDTO timeDTO) {
        Wrapper<OperationLog> wrapper =Wrappers
                .<OperationLog>query()
                .ge("update_time",timeDTO.getStartTime())
                .le("update_time",timeDTO.getEndTime());
        return operationLogMapper.selectList(wrapper);
    }

    @Override
    public List<OperationLog>  findByAnyCondition(OperationLogDTO operationLogDTO) {
        Wrapper<OperationLog> wrapper =Wrappers
                .<OperationLog>query()
                .eq(operationLogDTO.getId()!=null,"id",operationLogDTO.getId())
                .eq(operationLogDTO.getOperateType()!=null,"operate_type",operationLogDTO.getOperateType())
                .eq(operationLogDTO.getGoodsId()!=null,"goods_id",operationLogDTO.getGoodsId())
                .like(operationLogDTO.getGoodsName()!=null,"goods_name",operationLogDTO.getGoodsName())
                .eq(operationLogDTO.getFormerWarehouseId()!=null,"former_warehouse_id",operationLogDTO.getFormerWarehouseId())
                .eq(operationLogDTO.getNewWarehouseId()!=null,"new_warehouse_id",operationLogDTO.getNewWarehouseId())
                .like(operationLogDTO.getUpdateUser()!=null,"update_user",operationLogDTO.getUpdateUser())
                .ge("update_time",operationLogDTO.getStartTime())
                .le("update_time",operationLogDTO.getEndTime());
        return operationLogMapper.selectList(wrapper);

    }

}
