package com.yb.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yb.product.entity.SpuEntity;

/**
 * SPU 服务接口
 */
public interface SpuService {

    /** 分页查询 SPU */
    Page<SpuEntity> page(Long page, Long size, Long categoryId, String keyword);

    /** 根据 ID 查询 SPU */
    SpuEntity getById(Long id);

    /** 新增 SPU */
    SpuEntity save(SpuEntity entity);

    /** 更新 SPU */
    SpuEntity update(Long id, SpuEntity entity);
}
