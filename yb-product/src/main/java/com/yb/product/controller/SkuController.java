package com.yb.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yb.api.dto.resp.ProductResp;
import com.yb.common.dto.PageDTO;
import com.yb.common.dto.R;
import com.yb.product.entity.SkuEntity;
import com.yb.product.service.CategoryService;
import com.yb.product.service.SkuService;
import com.yb.product.service.SpuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SKU 控制器
 * <p>
 * 同时承担 Feign 接口契约：GET /api/product/{skuId} 和 GET /api/product/page
 */
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class SkuController {

    private final SkuService skuService;
    private final SpuService spuService;
    private final CategoryService categoryService;

    /** SKU 详情（Feign 契约：ProductClient.getSkuById） */
    @GetMapping("/{skuId}")
    public R<ProductResp> getSkuById(@PathVariable Long skuId) {
        SkuEntity entity = skuService.getById(skuId);
        ProductResp resp = toProductResp(entity);
        return R.ok(resp);
    }

    /** 分页查询 SKU（Feign 契约：ProductClient.page） */
    @GetMapping("/page")
    public R<PageDTO<ProductResp>> page(@RequestParam(defaultValue = "1") Long page,
                                         @RequestParam(defaultValue = "10") Long size) {
        Page<SkuEntity> result = skuService.page(page, size);
        List<ProductResp> records = result.getRecords().stream()
                .map(this::toProductResp)
                .collect(Collectors.toList());
        PageDTO<ProductResp> pageDTO = new PageDTO<>();
        pageDTO.setTotal(result.getTotal());
        pageDTO.setPages(result.getPages());
        pageDTO.setPage(result.getCurrent());
        pageDTO.setSize(result.getSize());
        pageDTO.setRecords(records);
        return R.ok(pageDTO);
    }

    /** 查询 SPU 下的所有 SKU */
    @GetMapping("/sku/list")
    public R<List<SkuEntity>> listBySpuId(@RequestParam Long spuId) {
        List<SkuEntity> list = skuService.listBySpuId(spuId);
        return R.ok(list);
    }

    /** 新增 SKU */
    @PostMapping("/sku")
    public R<SkuEntity> createSku(@RequestBody SkuEntity entity) {
        SkuEntity created = skuService.save(entity);
        return R.ok(created);
    }

    /** 更新 SKU */
    @PutMapping("/sku/{id}")
    public R<SkuEntity> updateSku(@PathVariable Long id, @RequestBody SkuEntity entity) {
        entity.setId(id);
        SkuEntity updated = skuService.update(entity);
        return R.ok(updated);
    }

    /** 删除 SKU */
    @DeleteMapping("/sku/{id}")
    public R<Void> deleteSku(@PathVariable Long id) {
        skuService.delete(id);
        return R.ok();
    }

    /** 更新库存 */
    @PutMapping("/sku/{id}/stock")
    public R<SkuEntity> updateStock(@PathVariable Long id, @RequestParam Integer stock) {
        SkuEntity updated = skuService.updateStock(id, stock);
        return R.ok(updated);
    }

    private ProductResp toProductResp(SkuEntity entity) {
        ProductResp resp = new ProductResp();
        resp.setId(entity.getId());
        resp.setSpuId(entity.getSpuId());
        resp.setName(entity.getName());
        resp.setImage(entity.getImage());
        resp.setPrice(entity.getPrice());
        resp.setStock(entity.getStock());
        // 从 SPU 获取类目信息
        try {
            resp.setCategoryId(spuService.getById(entity.getSpuId()).getCategoryId());
            resp.setCategoryName(categoryService.getById(resp.getCategoryId()).getName());
        } catch (Exception e) {
            // 忽略类目信息获取失败
        }
        return resp;
    }
}
