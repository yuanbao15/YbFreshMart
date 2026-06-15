package com.yb.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yb.common.exception.BizException;
import com.yb.product.entity.SpuEntity;
import com.yb.product.mapper.SpuMapper;
import com.yb.product.service.SpuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.yb.common.enums.ErrorCode.PRODUCT_NOT_FOUND;

/**
 * SPU 服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpuServiceImpl implements SpuService {

    private final SpuMapper spuMapper;

    @Override
    public Page<SpuEntity> page(Long pageNum, Long size, Long categoryId, String keyword) {
        LambdaQueryWrapper<SpuEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(categoryId != null, SpuEntity::getCategoryId, categoryId)
                .like(keyword != null, SpuEntity::getName, keyword)
                .orderByDesc(SpuEntity::getCreateTime);
        Page<SpuEntity> page = new Page<>(pageNum, size);
        return spuMapper.selectPage(page, wrapper);
    }

    @Override
    public SpuEntity getById(Long id) {
        SpuEntity entity = spuMapper.selectById(id);
        if (entity == null) {
            throw new BizException(PRODUCT_NOT_FOUND);
        }
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpuEntity save(SpuEntity entity) {
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        spuMapper.insert(entity);
        log.info("[Product] 新增 SPU 成功, id={}, name={}", entity.getId(), entity.getName());
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpuEntity update(Long id, SpuEntity entity) {
        SpuEntity existing = getById(id);
        if (entity.getName() != null) existing.setName(entity.getName());
        if (entity.getDescription() != null) existing.setDescription(entity.getDescription());
        if (entity.getBrand() != null) existing.setBrand(entity.getBrand());
        if (entity.getMainImage() != null) existing.setMainImage(entity.getMainImage());
        if (entity.getImages() != null) existing.setImages(entity.getImages());
        if (entity.getUnit() != null) existing.setUnit(entity.getUnit());
        if (entity.getStatus() != null) existing.setStatus(entity.getStatus());
        spuMapper.updateById(existing);
        log.info("[Product] 更新 SPU 成功, id={}", id);
        return existing;
    }
}
