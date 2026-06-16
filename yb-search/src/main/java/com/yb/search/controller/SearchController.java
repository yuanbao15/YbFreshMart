package com.yb.search.controller;

import com.yb.common.dto.PageDTO;
import com.yb.common.dto.R;
import com.yb.search.document.ProductDocument;
import com.yb.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 搜索接口
 */
@Tag(name = "搜索服务", description = "商品全文检索")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "搜索商品")
    @GetMapping("/product")
    public R<PageDTO<ProductDocument>> search(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "类目ID（可选筛选）") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") long page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") long size) {
        PageDTO<ProductDocument> result = searchService.search(keyword, categoryId, page, size);
        return R.ok(result);
    }
}
