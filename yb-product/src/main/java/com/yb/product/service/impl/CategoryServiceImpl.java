package com.yb.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yb.common.exception.BizException;
import com.yb.product.dto.CategoryTreeDTO;
import com.yb.product.entity.CategoryEntity;
import com.yb.product.mapper.CategoryMapper;
import com.yb.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.yb.common.enums.ErrorCode.PRODUCT_CATEGORY_NOT_FOUND;

/**
 * 类目服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    @Cacheable(value = "product:categories", key = "'tree'")
    public List<CategoryTreeDTO> getTree() {
        // 查询所有类目
        List<CategoryEntity> allCategories = categoryMapper.selectList(
                new LambdaQueryWrapper<CategoryEntity>()
                        .orderByAsc(CategoryEntity::getSortOrder));

        // 按 parentId 分组
        Map<Long, List<CategoryEntity>> childrenMap = allCategories.stream()
                .filter(c -> c.getParentId() != 0)
                .collect(Collectors.groupingBy(CategoryEntity::getParentId));

        // 构建一级类目树
        List<CategoryTreeDTO> tree = new ArrayList<>();
        for (CategoryEntity category : allCategories) {
            if (category.getParentId() == 0) {
                CategoryTreeDTO dto = toDTO(category);
                dto.setChildren(buildChildren(category.getId(), childrenMap));
                tree.add(dto);
            }
        }
        log.debug("[Product] 类目树查询完成, 一级类目数={}", tree.size());
        return tree;
    }

    @Override
    public CategoryEntity getById(Long id) {
        CategoryEntity entity = categoryMapper.selectById(id);
        if (entity == null) {
            throw new BizException(PRODUCT_CATEGORY_NOT_FOUND);
        }
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "product:categories", key = "'tree'")
    public CategoryEntity save(CategoryEntity entity) {
        categoryMapper.insert(entity);
        log.info("[Product] 新增类目成功, id={}, name={}", entity.getId(), entity.getName());
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "product:categories", key = "'tree'")
    public CategoryEntity update(Long id, CategoryEntity entity) {
        CategoryEntity existing = getById(id);
        if (entity.getName() != null) existing.setName(entity.getName());
        if (entity.getSortOrder() != null) existing.setSortOrder(entity.getSortOrder());
        if (entity.getIcon() != null) existing.setIcon(entity.getIcon());
        categoryMapper.updateById(existing);
        log.info("[Product] 更新类目成功, id={}", id);
        return existing;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "product:categories", key = "'tree'")
    public boolean delete(Long id) {
        getById(id);
        categoryMapper.deleteById(id);
        log.info("[Product] 删除类目成功, id={}", id);
        return true;
    }

    /** 递归构建子类目 */
    private List<CategoryTreeDTO> buildChildren(Long parentId, Map<Long, List<CategoryEntity>> childrenMap) {
        List<CategoryEntity> children = childrenMap.get(parentId);
        if (children == null || children.isEmpty()) {
            return new ArrayList<>();
        }
        List<CategoryTreeDTO> result = new ArrayList<>();
        for (CategoryEntity child : children) {
            CategoryTreeDTO dto = toDTO(child);
            dto.setChildren(buildChildren(child.getId(), childrenMap));
            result.add(dto);
        }
        return result;
    }

    private CategoryTreeDTO toDTO(CategoryEntity entity) {
        CategoryTreeDTO dto = new CategoryTreeDTO();
        dto.setId(entity.getId());
        dto.setParentId(entity.getParentId());
        dto.setName(entity.getName());
        dto.setLevel(entity.getLevel());
        dto.setSortOrder(entity.getSortOrder());
        dto.setIcon(entity.getIcon());
        return dto;
    }
}
