package com.yb.product.service;

import com.yb.product.dto.CategoryTreeDTO;
import com.yb.product.entity.CategoryEntity;

import java.util.List;

/**
 * 类目服务接口
 */
public interface CategoryService {

    /** 获取完整类目树 */
    List<CategoryTreeDTO> getTree();

    /** 根据 ID 获取类目 */
    CategoryEntity getById(Long id);

    /** 新增类目 */
    CategoryEntity save(CategoryEntity entity);

    /** 更新类目 */
    CategoryEntity update(Long id, CategoryEntity entity);

    /** 删除类目（逻辑删除） */
    boolean delete(Long id);

    /** 获取类目及其所有子孙类目的 ID 列表 */
    List<Long> getDescendantIds(Long categoryId);
}
