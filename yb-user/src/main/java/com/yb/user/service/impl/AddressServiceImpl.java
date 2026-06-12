package com.yb.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yb.common.exception.BizException;
import com.yb.user.entity.AddressEntity;
import com.yb.user.mapper.AddressMapper;
import com.yb.user.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.yb.common.enums.ErrorCode.NOT_FOUND;

/**
 * 地址服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    @Override
    public List<AddressEntity> listByUserId(Long userId) {
        LambdaQueryWrapper<AddressEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AddressEntity::getUserId, userId)
                .orderByDesc(AddressEntity::getCreateTime);
        return addressMapper.selectList(wrapper);
    }

    @Override
    public AddressEntity getById(Long id) {
        AddressEntity address = addressMapper.selectById(id);
        if (address == null) {
            throw new BizException(NOT_FOUND.getCode(), "地址不存在");
        }
        return address;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddressEntity save(AddressEntity address) {
        addressMapper.insert(address);
        log.info("[Address] 新增地址, id={}, userId={}", address.getId(), address.getUserId());
        return address;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddressEntity update(Long id, AddressEntity dto) {
        AddressEntity address = getById(id);
        if (dto.getReceiverName() != null) address.setReceiverName(dto.getReceiverName());
        if (dto.getReceiverPhone() != null) address.setReceiverPhone(dto.getReceiverPhone());
        if (dto.getProvince() != null) address.setProvince(dto.getProvince());
        if (dto.getCity() != null) address.setCity(dto.getCity());
        if (dto.getDistrict() != null) address.setDistrict(dto.getDistrict());
        if (dto.getDetail() != null) address.setDetail(dto.getDetail());
        addressMapper.updateById(address);
        return address;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        getById(id);
        addressMapper.deleteById(id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefault(Long userId, Long addressId) {
        // 先取消用户所有默认地址
        LambdaUpdateWrapper<AddressEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressEntity::getUserId, userId)
                .set(AddressEntity::getIsDefault, 0);
        addressMapper.update(wrapper);

        // 设置新默认地址
        AddressEntity address = new AddressEntity();
        address.setId(addressId);
        address.setIsDefault(1);
        addressMapper.updateById(address);

        log.info("[Address] 设置默认地址, userId={}, addressId={}", userId, addressId);
        return true;
    }
}
