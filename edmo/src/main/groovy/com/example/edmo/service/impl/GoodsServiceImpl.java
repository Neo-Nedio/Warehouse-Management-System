package com.example.edmo.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edmo.mapper.GoodsMapper;
import com.example.edmo.mapper.WarehouseAdminMapper;
import com.example.edmo.pojo.DTO.GoodsDTO;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.VO.GoodsInWarehouseVO;
import com.example.edmo.pojo.entity.Goods;
import com.example.edmo.pojo.entity.Warehouse;
import cn.hutool.json.JSONUtil;
import com.example.edmo.service.Interface.GoodsService;
import com.example.edmo.util.Constant.RedisConstant;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {
    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private WarehouseAdminMapper warehouseAdminMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

@Override
public boolean save(Goods entity) {
    // 调用父类ServiceImpl的save方法保存商品实体
    boolean result = super.save(entity);
    if (result) {
        // 新增商品后，清除仓库商品缓存（因为新商品会出现在仓库商品列表中）
        var warehouseKeys = stringRedisTemplate.keys(RedisConstant.GOODS_WAREHOUSE_KEY + "*");
        if (!warehouseKeys.isEmpty()) {
            stringRedisTemplate.delete(warehouseKeys);
        }
    }
    return result;
}

    //重写 updateById 方法，清除缓存
    @Override
    public boolean updateById(Goods entity) {
        boolean result = super.updateById(entity);
        if (result && entity != null && entity.getId() != null) {
            // :*是通配符
//            同一商品 ID 可能对应不同的 managedWarehouseIds，因此会有多个缓存 key，例如：
//            goods:id:1:warehouses:[1,2]
//            goods:id:1:warehouses:[1,2,3]
//            goods:id:1:warehouses:[2,3]
            var keys = stringRedisTemplate.keys(RedisConstant.GOODS_KEY + "id:" + entity.getId() + ":*");
            if (!keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
            var warehouseKeys = stringRedisTemplate.keys(RedisConstant.GOODS_WAREHOUSE_KEY + "*");
            if (!warehouseKeys.isEmpty()) {
                stringRedisTemplate.delete(warehouseKeys);
            }
        }
        return result;
    }

    //重写 removeById 方法，清除缓存
    @Override
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        if (result && id instanceof Integer) {
            var keys = stringRedisTemplate.keys(RedisConstant.GOODS_KEY + "id:" + id + ":*");
            if (!keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
            var warehouseKeys = stringRedisTemplate.keys(RedisConstant.GOODS_WAREHOUSE_KEY + "*");
            if (!warehouseKeys.isEmpty()) {
                stringRedisTemplate.delete(warehouseKeys);
            }
        }
        return result;
    }

    @Override
    public boolean updateGoodsInWarehouse(Goods goods) {
        UpdateWrapper<Goods> wrapper = Wrappers
                .<Goods>update()
                .eq("id", goods.getId())
                .set("warehouse_id", goods.getWarehouseId());
        boolean result = goodsMapper.update(goods, wrapper) > 0;
        
        // 清除相关缓存
        if (result && goods.getId() != null) {
            var keys = stringRedisTemplate.keys(RedisConstant.GOODS_KEY + "id:" + goods.getId() + ":*");
            if (!keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
            var warehouseKeys = stringRedisTemplate.keys(RedisConstant.GOODS_WAREHOUSE_KEY + "*");
            if (!warehouseKeys.isEmpty()) {
                stringRedisTemplate.delete(warehouseKeys);
            }
        }
        return result;
    }

    @Override
    public boolean loginDeleteGoodsById(GoodsDTO goodsDTO) {
        UpdateWrapper<Goods> wrapper = Wrappers
                .<Goods>update()
                .eq("id", goodsDTO.getId())
                .eq("status",1)
                .set("status", 0)
                .set("update_time",goodsDTO.getUpdateTime())
                .set("update_user",goodsDTO.getUpdateUser());
        boolean result = goodsMapper.update(wrapper) > 0;
        
        // 清除相关缓存
        if (result && goodsDTO.getId() != null) {
            var keys = stringRedisTemplate.keys(RedisConstant.GOODS_KEY + "id:" + goodsDTO.getId() + ":*");
            if (!keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
            var warehouseKeys = stringRedisTemplate.keys(RedisConstant.GOODS_WAREHOUSE_KEY + "*");
            if (!warehouseKeys.isEmpty()) {
                stringRedisTemplate.delete(warehouseKeys);
            }
        }
        return result;
    }

    @Override
    public List<Goods> findGoodsByIds(List<GoodsDTO> goodsDTOList) {
        List<Integer> ids = goodsDTOList.stream()
                .map(GoodsDTO::getId)
                .toList();

        Wrapper<Goods> wrapper = Wrappers
                .<Goods>query().
                in("id", ids)
                .eq("status",1);

        return goodsMapper.selectList(wrapper);
    }

    @Override
    public Goods findGoodsById(Integer id,List<Integer> managedWarehouseIds) {
        // 构建缓存 key
        String cacheKey = RedisConstant.GOODS_KEY + "id:" + id + ":warehouses:" + managedWarehouseIds.toString();
        
        // 尝试从缓存获取
        String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cacheValue != null) {
            // 缓存命中，更新过期时间
            stringRedisTemplate.expire(cacheKey, RedisConstant.GOODS_TTL, TimeUnit.MINUTES);
            return JSONUtil.toBean(cacheValue, Goods.class);
        }

        // 缓存未命中，查询数据库
        Wrapper<Goods> wrapper = Wrappers
                .<Goods>query()
                .eq("id", id)
                .eq("status", 1)
                .in("warehouse_id", managedWarehouseIds);
        Goods goods = goodsMapper.selectOne(wrapper);
        
        // 存入缓存
        if (goods != null) {
            stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(goods), 
                    RedisConstant.GOODS_TTL, TimeUnit.MINUTES);
        }
        return goods;
    }

    @Override
    public List<Goods> findGoodsByNameLike(String name,List<Integer> managedWarehouseIds) {
        // 构建缓存 key
        String cacheKey = RedisConstant.GOODS_WAREHOUSE_KEY + "name:" + name + 
                ":warehouses:" + managedWarehouseIds.toString();
        
        // 尝试从缓存获取
        String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cacheValue != null) {
            // 缓存命中，更新过期时间
            stringRedisTemplate.expire(cacheKey, RedisConstant.GOODS_WAREHOUSE_TTL, TimeUnit.MINUTES);
            return JSONUtil.toList(cacheValue, Goods.class);
        }

        // 缓存未命中，查询数据库
        Wrapper<Goods> wrapper = Wrappers
                .<Goods>query()
                .like("name", name)
                .eq("status", 1)
                .in("warehouse_id", managedWarehouseIds)
                .orderByDesc("id");
        List<Goods> goodsList = goodsMapper.selectList(wrapper);
        
        // 存入缓存
        stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(goodsList), 
                RedisConstant.GOODS_WAREHOUSE_TTL, TimeUnit.MINUTES);
        return goodsList;
    }

    @Override
    public Page<Goods> findGoodsByNameLike(PageDTO pageDTO,List<Integer> managedWarehouseIds) {
        Wrapper<Goods> wrapper = Wrappers
                .<Goods>query()
                .eq("status", 1)
                .in("warehouse_id", managedWarehouseIds)
                .like(pageDTO.getParam() != null && pageDTO.getParam().containsKey("name"), "name", pageDTO.getParam().get("name"))
                .orderByDesc("id");

        Page<Goods> page=new Page<>();
        page.setSize(pageDTO.getPageSize());
        page.setCurrent(pageDTO.getPageNum());

        return goodsMapper.selectPage(page,wrapper);
    }

    @Override
    public GoodsInWarehouseVO findGoodsByWarehouseId(Integer warehouseId, List<Integer> managedWarehouseIds) {
        // 构建缓存 key
        String cacheKey = RedisConstant.GOODS_WAREHOUSE_KEY + warehouseId + ":warehouses:" + managedWarehouseIds.toString();
        
        // 尝试从缓存获取
        String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cacheValue != null) {
            // 缓存命中，更新过期时间
            stringRedisTemplate.expire(cacheKey, RedisConstant.GOODS_WAREHOUSE_TTL, TimeUnit.MINUTES);
            return JSONUtil.toBean(cacheValue, GoodsInWarehouseVO.class);
        }

        // 缓存未命中，查询数据库
        GoodsInWarehouseVO  goodsInWarehouseVO = new GoodsInWarehouseVO();
        BeanUtils.copyProperties(warehouseAdminMapper.selectById(warehouseId),goodsInWarehouseVO);

        Wrapper<Goods> wrapper = Wrappers
                .<Goods>query()
                .eq("warehouse_id", warehouseId)
                .eq("status", 1)
                .in("warehouse_id", managedWarehouseIds)
                .orderByDesc("id");

        goodsInWarehouseVO.setGoods(goodsMapper.selectList(wrapper));
        
        // 存入缓存
        stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(goodsInWarehouseVO), 
                RedisConstant.GOODS_WAREHOUSE_TTL, TimeUnit.MINUTES);
        return goodsInWarehouseVO;
    }

    @Override
    public List<GoodsInWarehouseVO> findGoodsAllByManagedWarehouseIds(List<Integer> managedWarehouseIds) {
        // 构建缓存 key
        String cacheKey = RedisConstant.GOODS_WAREHOUSE_KEY + "all:warehouses:" + managedWarehouseIds.toString();
        
        // 尝试从缓存获取
        String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cacheValue != null) {
            // 缓存命中，更新过期时间
            stringRedisTemplate.expire(cacheKey, RedisConstant.GOODS_WAREHOUSE_TTL, TimeUnit.MINUTES);
            return JSONUtil.toList(cacheValue, GoodsInWarehouseVO.class);
        }

        // 缓存未命中，查询数据库
        QueryWrapper<Warehouse> wrapper2=Wrappers
                .<Warehouse>query()
                //列表用in
                .in("id",managedWarehouseIds)
                .orderByDesc("id");
        List<Warehouse> warehouses = warehouseAdminMapper.selectList(wrapper2);

        List<GoodsInWarehouseVO> result = fillGoods(warehouses, null);
        
        // 存入缓存
        stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(result), 
                RedisConstant.GOODS_WAREHOUSE_TTL, TimeUnit.MINUTES);
        return result;
    }

    @Override
    public List<GoodsInWarehouseVO> findGoodsByNameLikeInByManagedWarehouseIds(String name, List<Integer> managedWarehouseIds) {
        QueryWrapper<Warehouse> wrapper2=Wrappers
                .<Warehouse>query()
                //列表用in
                .in("id",managedWarehouseIds)
                .orderByDesc("id");
        List<Warehouse> warehouses = warehouseAdminMapper.selectList(wrapper2);

        return fillGoods(warehouses, name);
    }

    @Override
    public List<Goods> findGoodsByAnyCondition(GoodsDTO goodsDTO, List<Integer> managedWarehouseIds) {
        Wrapper<Goods> wrapper = Wrappers
                .<Goods>query()
                .eq(goodsDTO.getId()!=null,"id",goodsDTO.getId())
                .like(goodsDTO.getName()!=null,"name", goodsDTO.getName())
                .eq(goodsDTO.getPrice()!=null,"price", goodsDTO.getPrice())
                .eq(goodsDTO.getNumber()!=null,"number", goodsDTO.getNumber())
                .eq(goodsDTO.getWarehouseId()!=null,"warehouse_id", goodsDTO.getWarehouseId())
                .like(goodsDTO.getCreateUser()!=null,"create_user", goodsDTO.getCreateUser())
                .like(goodsDTO.getUpdateUser()!=null,"update_user", goodsDTO.getUpdateUser())

                // 创建时间范围查询
                .ge(goodsDTO.getStartCreateTime() != null, "create_time", goodsDTO.getStartCreateTime())
                .le(goodsDTO.getEndCreateTime() != null, "create_time", goodsDTO.getEndCreateTime())
                // 更新时间范围查询
                .ge(goodsDTO.getStartUpdateTime() != null, "update_time", goodsDTO.getStartUpdateTime())
                .le(goodsDTO.getEndUpdateTime() != null, "update_time", goodsDTO.getEndUpdateTime())

                .eq("status", 1)
                .in("warehouse_id",managedWarehouseIds)
                .orderByDesc("id");
        return goodsMapper.selectList(wrapper);
    }

    private List<GoodsInWarehouseVO> fillGoods(List<Warehouse> warehouses, String goodsName){
        List<GoodsInWarehouseVO> goodsInWarehouseVOS=new ArrayList<>();
        //把名字放入 fillGoods里搜索，判断存不存在
        for(Warehouse warehouse:warehouses){
            Wrapper<Goods> wrapper = Wrappers
                    .<Goods>query()
                    .eq("warehouse_id", warehouse.getId())
                    .eq("status", 1)
                    .like(goodsName != null && !goodsName.trim().isEmpty(),"name", goodsName)
                    .orderByDesc("id");

            GoodsInWarehouseVO goodsInWarehouseVO=new GoodsInWarehouseVO();
            List<Goods> goodsList = goodsMapper.selectList(wrapper);
            BeanUtils.copyProperties(warehouse,goodsInWarehouseVO);
            goodsInWarehouseVO.setGoods(goodsList);
            goodsInWarehouseVOS.add(goodsInWarehouseVO);
        }
        return goodsInWarehouseVOS;
    }

}
