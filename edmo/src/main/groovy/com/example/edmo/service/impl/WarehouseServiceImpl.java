package com.example.edmo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edmo.mapper.WarehouseAdminMapper;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.entity.Warehouse;
import cn.hutool.json.JSONUtil;
import com.example.edmo.service.Interface.WarehouseService;
import com.example.edmo.util.Constant.RedisConstant;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseAdminMapper, Warehouse> implements WarehouseService {
    @Resource
    WarehouseAdminMapper warehouseAdminMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Warehouse getById(Serializable id) {
        // 尝试从缓存获取
        String cacheKey = RedisConstant.WAREHOUSE_KEY + "id:" + id;
        String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cacheValue != null) {
            // 缓存命中，更新过期时间
            stringRedisTemplate.expire(cacheKey, RedisConstant.WAREHOUSE_TTL, TimeUnit.MINUTES);
            return JSONUtil.toBean(cacheValue, Warehouse.class);
        }

        // 缓存未命中，查询数据库
        Warehouse warehouse = super.getById(id);
        
        // 存入缓存
        if (warehouse != null) {
            stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(warehouse), 
                    RedisConstant.WAREHOUSE_TTL, TimeUnit.MINUTES);
        }
        return warehouse;
    }

    @Override
    public boolean save(Warehouse entity) {
        boolean result = super.save(entity);
        if (result) {
            // 清除单个仓库缓存
            if (entity != null && entity.getId() != null) {
                stringRedisTemplate.delete(RedisConstant.WAREHOUSE_KEY + "id:" + entity.getId());
            }
            // 清除列表缓存（新增仓库后，列表查询结果会变化）
            var keys = stringRedisTemplate.keys(RedisConstant.WAREHOUSE_LIST_KEY + "*");
            if (!keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
        }
        return result;
    }

    @Override
    public List<Warehouse> findWarehousesByNameLike(String name) {
        // 尝试从缓存获取
        String cacheKey = RedisConstant.WAREHOUSE_LIST_KEY + "name:" + name;
        String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cacheValue != null) {
            // 缓存命中，更新过期时间
            stringRedisTemplate.expire(cacheKey, RedisConstant.WAREHOUSE_LIST_TTL, TimeUnit.MINUTES);
            return JSONUtil.toList(cacheValue, Warehouse.class);
        }

        // 缓存未命中，查询数据库
        QueryWrapper<Warehouse> wrapper = Wrappers
                .<Warehouse>query()
                .like("name",name)
                .orderByDesc("id");
        List<Warehouse> warehouses = warehouseAdminMapper.selectList(wrapper);
        
        // 存入缓存
        stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(warehouses), 
                RedisConstant.WAREHOUSE_LIST_TTL, TimeUnit.MINUTES);
        return warehouses;
    }

    @Override
    public Page<Warehouse> findWarehousesByNameLike(PageDTO pageDTO) {
        QueryWrapper<Warehouse> wrapper = Wrappers
                .<Warehouse>query()
                .orderByDesc("id");
        
        //如果param不为null且包含name参数，则添加like条件,因为param和name都有可能不存在
        if (pageDTO.getParam() != null && pageDTO.getParam().containsKey("name")) {
            String name = (String) pageDTO.getParam().get("name");
            if (name != null && !name.isEmpty()) {
                wrapper.like("name", name);
            }
        }

        Page<Warehouse> page=new Page<>();
        page.setSize(pageDTO.getPageSize());
        page.setCurrent(pageDTO.getPageNum());

        return warehouseAdminMapper.selectPage(page,wrapper);
    }

    @Override
    public List<Warehouse> findWarehousesById(List<Integer> ids) {
        // 构建缓存 key
        String cacheKey = RedisConstant.WAREHOUSE_LIST_KEY + "ids:" + ids.toString();
        
        // 尝试从缓存获取
        String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cacheValue != null) {
            // 缓存命中，更新过期时间
            stringRedisTemplate.expire(cacheKey, RedisConstant.WAREHOUSE_LIST_TTL, TimeUnit.MINUTES);
            return JSONUtil.toList(cacheValue, Warehouse.class);
        }

        // 缓存未命中，查询数据库
        QueryWrapper<Warehouse> wrapper=Wrappers
                .<Warehouse>query()
                //列表用in
                .in("id",ids)
                .orderByDesc("id");
        List<Warehouse> warehouses = warehouseAdminMapper.selectList(wrapper);
        
        // 存入缓存
        stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(warehouses), 
                RedisConstant.WAREHOUSE_LIST_TTL, TimeUnit.MINUTES);
        return warehouses;
    }

    //重写 updateById 方法，清除缓存
    @Override
    public boolean updateById(Warehouse entity) {
        boolean result = super.updateById(entity);
        if (result && entity != null) {
            // 清除单个仓库缓存
            if (entity.getId() != null) {
                stringRedisTemplate.delete(RedisConstant.WAREHOUSE_KEY + "id:" + entity.getId());
            }
            // 清除列表缓存
            var keys = stringRedisTemplate.keys(RedisConstant.WAREHOUSE_LIST_KEY + "*");
            if (!keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
        }
        return result;
    }

    //重写 removeById 方法，清除缓存
    @Override
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        if (result) {
            // 清除单个仓库缓存
            stringRedisTemplate.delete(RedisConstant.WAREHOUSE_KEY + "id:" + id);
            // 清除列表缓存
            var keys = stringRedisTemplate.keys(RedisConstant.WAREHOUSE_LIST_KEY + "*");
            if (!keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
        }
        return result;
    }
}
