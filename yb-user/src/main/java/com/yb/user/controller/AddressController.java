package com.yb.user.controller;

import com.yb.common.dto.R;
import com.yb.user.entity.AddressEntity;
import com.yb.user.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址控制器
 */
@RestController
@RequestMapping("/api/user/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /** 获取用户所有地址 */
    @GetMapping("/{userId}/list")
    public R<List<AddressEntity>> listAddress(@PathVariable Long userId) {
        return R.ok(addressService.listByUserId(userId));
    }

    /** 获取地址详情 */
    @GetMapping("/{id}")
    public R<AddressEntity> getAddress(@PathVariable Long id) {
        return R.ok(addressService.getById(id));
    }

    /** 新增地址 */
    @PostMapping
    public R<AddressEntity> createAddress(@Valid @RequestBody AddressEntity address) {
        return R.ok(addressService.save(address));
    }

    /** 更新地址 */
    @PutMapping("/{id}")
    public R<AddressEntity> updateAddress(@PathVariable Long id, @RequestBody AddressEntity address) {
        return R.ok(addressService.update(id, address));
    }

    /** 删除地址 */
    @DeleteMapping("/{id}")
    public R<Void> deleteAddress(@PathVariable Long id) {
        addressService.delete(id);
        return R.ok();
    }

    /** 设置默认地址 */
    @PutMapping("/{userId}/default/{addressId}")
    public R<Void> setDefault(@PathVariable Long userId, @PathVariable Long addressId) {
        addressService.setDefault(userId, addressId);
        return R.ok();
    }
}
