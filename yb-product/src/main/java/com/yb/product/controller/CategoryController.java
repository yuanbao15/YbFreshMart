package com.yb.product.controller;

import com.yb.common.dto.R;
import com.yb.product.dto.CategoryTreeDTO;
import com.yb.product.entity.CategoryEntity;
import com.yb.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 类目控制器
 */
@RestController
@RequestMapping("/api/product/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /** 获取完整类目树 */
    @GetMapping("/tree")
    public R<List<CategoryTreeDTO>> getTree() {
        List<CategoryTreeDTO> tree = categoryService.getTree();
        return R.ok(tree);
    }

    /** 获取类目详情 */
    @GetMapping("/{id}")
    public R<CategoryEntity> getById(@PathVariable Long id) {
        CategoryEntity entity = categoryService.getById(id);
        return R.ok(entity);
    }

    /** 新增类目 */
    @PostMapping
    public R<CategoryEntity> create(@RequestBody CategoryEntity entity) {
        CategoryEntity saved = categoryService.save(entity);
        return R.ok(saved);
    }

    /** 更新类目 */
    @PutMapping("/{id}")
    public R<CategoryEntity> update(@PathVariable Long id, @RequestBody CategoryEntity entity) {
        CategoryEntity updated = categoryService.update(id, entity);
        return R.ok(updated);
    }

    /** 删除类目 */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return R.ok();
    }
}
