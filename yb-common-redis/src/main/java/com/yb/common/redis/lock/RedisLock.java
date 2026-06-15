package com.yb.common.redis.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁工具 - 基于 Redisson
 *
 * <pre>
 * 使用示例：
 *   redisLock.tryRun("lock:stock:sku:1001", 3, 10, TimeUnit.SECONDS, () -> {
 *       // 执行业务逻辑
 *       return deductStock(skuId, quantity);
 *   });
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLock {

    private final RedissonClient redissonClient;

    /**
     * 尝试获取锁并执行业务，执行完毕后自动释放锁
     *
     * @param lockKey   锁的 Key
     * @param waitTime  最大等待时间
     * @param leaseTime 锁持有时间（到期自动释放，防止死锁）
     * @param unit      时间单位
     * @param supplier  业务逻辑
     * @return 业务返回结果，获取锁失败返回 null
     */
    public <T> T tryRun(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(waitTime, leaseTime, unit);
            if (acquired) {
                log.debug("[RedisLock] 获取锁成功: {}", lockKey);
                return supplier.get();
            } else {
                log.warn("[RedisLock] 获取锁失败: {}", lockKey);
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[RedisLock] 等待锁被中断: {}", lockKey, e);
            return null;
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("[RedisLock] 释放锁: {}", lockKey);
            }
        }
    }

    /**
     * 尝试获取锁并执行无返回值业务
     */
    public void tryRun(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Runnable runnable) {
        tryRun(lockKey, waitTime, leaseTime, unit, () -> {
            runnable.run();
            return null;
        });
    }

    /** 获取原始 RLock 对象（用于更复杂的锁操作） */
    public RLock getLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }
}
