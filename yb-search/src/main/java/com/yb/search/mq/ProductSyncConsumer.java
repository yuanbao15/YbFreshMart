package com.yb.search.mq;

import com.yb.api.client.ProductClient;
import com.yb.api.dto.resp.ProductResp;
import com.yb.common.constant.MqQueue;
import com.yb.common.dto.R;
import com.yb.search.document.ProductDocument;
import com.yb.search.dto.ProductSyncMessage;
import com.yb.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 商品同步消息消费者
 * <p>
 * 监听 PRODUCT_SYNC_SEARCH 队列，处理 yb-product 发来的商品变更消息，
 * 同步到 Elasticsearch。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSyncConsumer {

    private final SearchService searchService;
    private final ProductClient productClient;

    @RabbitListener(queues = MqQueue.PRODUCT_SYNC_SEARCH)
    public void handleProductSync(ProductSyncMessage message) {
        log.info("收到商品同步消息: productId={}, type={}", message.getProductId(), message.getType());

        if (message.getType() == ProductSyncMessage.SyncType.DELETE) {
            searchService.deleteProduct(message.getProductId());
            return;
        }

        // CREATE / UPDATE：通过 Feign 查询最新商品详情
        try {
            R<ProductResp> resp = productClient.getSkuById(message.getProductId());
            if (resp.isSuccess() && resp.getData() != null) {
                ProductDocument doc = toDocument(resp.getData());
                searchService.syncProduct(doc);
            } else {
                log.warn("查询商品详情失败: productId={}, msg={}", message.getProductId(), resp.getMessage());
            }
        } catch (Exception e) {
            log.error("同步商品到 ES 失败: productId={}", message.getProductId(), e);
            // TODO: 阶段四加入死信队列/重试机制
        }
    }

    private ProductDocument toDocument(ProductResp resp) {
        ProductDocument doc = new ProductDocument();
        doc.setId(resp.getId());
        doc.setSpuId(resp.getSpuId());
        doc.setName(resp.getName());
        doc.setImage(resp.getImage());
        doc.setPrice(resp.getPrice());
        doc.setStock(resp.getStock());
        // categoryId 和 categoryName 暂无法从 ProductResp 获取，后续补充
        return doc;
    }
}
