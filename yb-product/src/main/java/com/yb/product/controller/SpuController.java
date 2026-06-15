package com.yb.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yb.common.dto.R;
import com.yb.product.entity.SpuEntity;
import com.yb.product.service.SpuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * SPU 控制器
 */
@RestController
@RequestMapping("/api/product/spu")
@RequiredArgsConstructor
public class SpuController {

    private final SpuService spuService;

    /** 分页查询 SPU */
    @GetMapping("/page")
    public R<Page<SpuEntity>> page(@RequestParam(defaultValue = "1") Long page,
                                    @RequestParam(defaultValue = "10") Long size,
                                    @RequestParam(required = false) Long categoryId,
                                    @RequestParam(required = false) String keyword) {
        Page<SpuEntity> result = spuService.page(page, size, categoryId, keyword);
        return R.ok(result);
    }

    /** 查询 SPU 详情 */
    @GetMapping("/{id}")
    public R<SpuEntity> getById(@PathVariable Long id) {
        SpuEntity entity = spuService.getById(id);
        return R.ok(entity);
    }

    /** 新增 SPU */
    @PostMapping
    public R<SpuEntity> create(@RequestBody SpuEntity entity) {
        SpuEntity saved = spuService.save(entity);
        return R.ok(saved);
    }

    /** 更新 SPU */
    @PutMapping("/{id}")
    public R<SpuEntity> update(@PathVariable Long id, @RequestBody SpuEntity entity) {
        SpuEntity updated = spuService.update(id, entity);
        return R.ok(updated);
    }
}
