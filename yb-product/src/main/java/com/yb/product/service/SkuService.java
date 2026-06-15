package com.yb.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yb.product.entity.SkuEntity;

/**
 * SKU 服务接口
 */
public interface SkuService {

    /** 根据 ID 查询 SKU */
    SkuEntity getById(Long id);

    /** 分页查询 SKU */
    Page<SkuEntity> page(Long page, Long size);

    /** 根据 SPU ID 查询 SKU 列表 */
    java.util.List<SkuEntity> listBySpuId(Long spuId);

    /** 新增 SKU */
    SkuEntity save(SkuEntity entity);

    /** 更新 SKU */
    SkuEntity update(SkuEntity entity);

    /** 删除 SKU */
    void delete(Long id);

    /** 更新库存 */
    SkuEntity updateStock(Long id, Integer stock);
}
