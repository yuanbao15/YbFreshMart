package com.yb.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yb.common.exception.BizException;
import com.yb.product.entity.SkuEntity;
import com.yb.product.mapper.SkuMapper;
import com.yb.product.service.SkuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.yb.common.enums.ErrorCode.PRODUCT_SKU_NOT_FOUND;

/**
 * SKU 服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkuServiceImpl implements SkuService {

    private final SkuMapper skuMapper;

    @Override
    @Cacheable(value = "product:detail", key = "#id")
    public SkuEntity getById(Long id) {
        SkuEntity entity = skuMapper.selectById(id);
        if (entity == null) {
            throw new BizException(PRODUCT_SKU_NOT_FOUND);
        }
        return entity;
    }

    @Override
    public Page<SkuEntity> page(Long pageNum, Long size) {
        LambdaQueryWrapper<SkuEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkuEntity::getStatus, 1)
                .orderByDesc(SkuEntity::getSoldCount);
        Page<SkuEntity> page = new Page<>(pageNum, size);
        return skuMapper.selectPage(page, wrapper);
    }

    @Override
    public List<SkuEntity> listBySpuId(Long spuId) {
        LambdaQueryWrapper<SkuEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkuEntity::getSpuId, spuId)
                .orderByAsc(SkuEntity::getPrice);
        return skuMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "product:detail", key = "#id")
    public SkuEntity updateStock(Long id, Integer stock) {
        SkuEntity entity = getById(id);
        entity.setStock(stock);
        skuMapper.updateById(entity);
        log.info("[Product] 更新库存成功, skuId={}, stock={}", id, stock);
        return entity;
    }
}
