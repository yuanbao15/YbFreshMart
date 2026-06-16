package com.yb.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品同步消息体
 * <p>
 * yb-product 通过 MQ 发送此消息，yb-search 消费后同步到 ES。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSyncMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品（SKU）ID
     */
    private Long productId;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * 操作类型
     */
    private SyncType type;

    public enum SyncType {
        CREATE, UPDATE, DELETE
    }
}
