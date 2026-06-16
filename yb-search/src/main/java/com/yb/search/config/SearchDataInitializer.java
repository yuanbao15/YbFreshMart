package com.yb.search.config;

import com.yb.search.document.ProductDocument;
import com.yb.search.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索测试数据初始化
 * <p>
 * 服务启动时自动创建 ES 索引并写入示例商品数据，方便立即测试。
 * 如果索引已有数据则跳过。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchDataInitializer implements ApplicationRunner {

    private final ProductSearchRepository repository;

    @Override
    public void run(ApplicationArguments args) {
        try {
            long count = repository.count();
            if (count > 0) {
                log.info("ES 索引 product 已有 {} 条数据，跳过初始化", count);
                return;
            }
            log.info("ES 索引 product 为空，开始写入测试数据...");
            repository.saveAll(createSampleData());
            log.info("测试数据写入完成，共 {} 条", repository.count());
        } catch (Exception e) {
            log.warn("ES 测试数据初始化失败（可能 ES 未启动或 ik 插件未安装）: {}", e.getMessage());
        }
    }

    private List<ProductDocument> createSampleData() {
        List<ProductDocument> list = new ArrayList<>();

        // 叶菜类
        list.add(build(10001L, 1001L, "有机菠菜 500g", BigDecimal.valueOf(9.90), 100, 11L, "叶菜类"));
        list.add(build(10002L, 1001L, "有机菠菜 1kg", BigDecimal.valueOf(18.00), 80, 11L, "叶菜类"));
        list.add(build(10003L, 1002L, "新鲜生菜 300g", BigDecimal.valueOf(5.80), 150, 11L, "叶菜类"));
        list.add(build(10004L, 1002L, "新鲜生菜 500g", BigDecimal.valueOf(8.80), 120, 11L, "叶菜类"));

        // 根茎类
        list.add(build(10005L, 1003L, "有机胡萝卜 500g", BigDecimal.valueOf(11.50), 200, 12L, "根茎类"));
        list.add(build(10006L, 1003L, "有机胡萝卜 1kg", BigDecimal.valueOf(19.80), 150, 12L, "根茎类"));
        list.add(build(10007L, 1004L, "新鲜土豆 1kg", BigDecimal.valueOf(6.80), 300, 12L, "根茎类"));

        // 水果类
        list.add(build(10008L, 1005L, "红富士苹果 5斤装", BigDecimal.valueOf(39.90), 100, 21L, "水果"));
        list.add(build(10009L, 1005L, "红富士苹果 10斤装", BigDecimal.valueOf(69.00), 60, 21L, "水果"));
        list.add(build(10010L, 1006L, "进口香蕉 2斤装", BigDecimal.valueOf(15.80), 180, 21L, "水果"));
        list.add(build(10011L, 1007L, "新疆哈密瓜 单果2kg+", BigDecimal.valueOf(29.90), 50, 21L, "水果"));
        list.add(build(10012L, 1008L, "巨峰葡萄 1kg", BigDecimal.valueOf(25.80), 90, 21L, "水果"));

        // 肉禽蛋品
        list.add(build(10013L, 1009L, "散养土鸡蛋 30枚装", BigDecimal.valueOf(45.00), 80, 31L, "肉禽蛋品"));
        list.add(build(10014L, 1010L, "新鲜猪里脊 500g", BigDecimal.valueOf(28.90), 60, 31L, "肉禽蛋品"));
        list.add(build(10015L, 1011L, "澳洲肥牛卷 300g", BigDecimal.valueOf(49.90), 40, 31L, "肉禽蛋品"));

        // 乳制品
        list.add(build(10016L, 1012L, "鲜牛奶 1L装", BigDecimal.valueOf(12.90), 200, 41L, "乳制品"));
        list.add(build(10017L, 1013L, "希腊酸奶 6杯装", BigDecimal.valueOf(36.00), 100, 41L, "乳制品"));

        return list;
    }

    private ProductDocument build(Long id, Long spuId, String name, BigDecimal price,
                                   int stock, Long categoryId, String categoryName) {
        ProductDocument doc = new ProductDocument();
        doc.setId(id);
        doc.setSpuId(spuId);
        doc.setName(name);
        doc.setPrice(price);
        doc.setStock(stock);
        doc.setCategoryId(categoryId);
        doc.setCategoryName(categoryName);
        return doc;
    }
}
