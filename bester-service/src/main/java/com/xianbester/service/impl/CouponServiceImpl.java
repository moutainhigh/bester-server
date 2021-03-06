package com.xianbester.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xianbester.api.dto.CouponDTO;
import com.xianbester.api.service.CouponService;
import com.xianbester.service.dao.CouponDao;
import com.xianbester.service.entity.CouponEntity;
import com.xianbester.service.util.BeansListUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author zhangqiang
 * @date 2018-12-18
 */
@Service
@Component
public class CouponServiceImpl implements CouponService {
    @Resource
    private CouponDao couponDao;

    @Override
    public int addCoupon(CouponDTO couponDTO) {
        CouponEntity entity = new CouponEntity();
        entity.setCouponName(couponDTO.getCouponName());
        entity.setMargin(couponDTO.getMargin());
        entity.setCouponType(couponDTO.getCouponType());
        entity.setOfferCash(couponDTO.getOfferCash());
        entity.setOfferDiscount(couponDTO.getOfferDiscount());
        entity.setThreshold(couponDTO.getThreshold());
        entity.setVipLevel(couponDTO.getVipLevel());
        entity.setLimitNum(couponDTO.getLimitNum());
        entity.setValidityPeriod(couponDTO.getValidityPeriod());
        StringBuilder stringBuilder = new StringBuilder();
        if (!CollectionUtils.isEmpty(couponDTO.getAvailable())) {
            List<String> list = couponDTO.getAvailable();
            for (int i = 0; i < list.size(); i++) {
                stringBuilder.append(list.get(i));
                if (i != list.size() - 1) {
                    stringBuilder.append(",");
                }
            }
        }
        entity.setAvailable(stringBuilder.toString());
        entity.setDescription(couponDTO.getDescription());
        return couponDao.addCoupon(entity);
    }

    @Override
    public CouponDTO inquireCouponById(Integer couponId) {
        CouponEntity couponEntity = couponDao.inquireCouponById(couponId);
        CouponDTO dto = new CouponDTO();
        if (couponEntity != null) {
            dto.setId(couponEntity.getId());
            dto.setCouponName(couponEntity.getCouponName());
            String[] shopIdList = couponEntity.getShopId().split(",");
            List<String> shopId = Lists.newArrayList(shopIdList);
            dto.setShopId(shopId);
            dto.setMargin(couponEntity.getMargin());
            dto.setCouponType(couponEntity.getCouponType());
            dto.setOfferCash(couponEntity.getOfferCash());
            dto.setOfferDiscount(couponEntity.getOfferDiscount());
            dto.setThreshold(couponEntity.getThreshold());
            dto.setVipLevel(couponEntity.getVipLevel());
            dto.setLimitNum(couponEntity.getLimitNum());
            dto.setValidityPeriod(couponEntity.getValidityPeriod());
            String[] availableList = couponEntity.getAvailable().split(",");
            List<String> list = Lists.newArrayList(availableList);
            dto.setAvailable(list);
            dto.setDescription(couponEntity.getDescription());
            dto.setAddTime(couponEntity.getAddTime());
            dto.setUpdateTime(couponEntity.getUpdateTime());
        }
        return dto;
    }

    @Override
    public Map<Integer, CouponDTO> batchFindByCouponIds(Collection<Integer> couponIds) {
        Map<Integer, CouponDTO> result = Maps.newHashMap();
        if (CollectionUtils.isEmpty(couponIds)) {
            return result;
        }
        List<CouponEntity> couponEntities = couponDao.batchSelect(couponIds);
        if (!CollectionUtils.isEmpty(couponEntities)) {
            for (CouponEntity entity : couponEntities) {
                CouponDTO dto = new CouponDTO();
                BeanUtils.copyProperties(entity, dto);
                List<String> shopId = Lists.newArrayList(entity.getShopId().split(","));
                List<String> list = Lists.newArrayList(entity.getAvailable().split(","));
                dto.setShopId(shopId);
                dto.setAvailable(list);
                result.put(entity.getId(), dto);
            }
        }
        return result;
    }

    @Override
    public int updateCouponInfo(CouponDTO coupon) {
        if (coupon == null) {
            return 0;
        }
        if (coupon.getId() == null) {
            return 0;
        }
        String shopId = CollectionUtils.isEmpty(coupon.getShopId()) ? null : String.join(",", coupon.getShopId());
        CouponEntity couponEntity = new CouponEntity();
        BeanUtils.copyProperties(coupon, couponEntity);
        couponEntity.setShopId(shopId);
        return couponDao.updateCouponInfo(couponEntity);
    }

    @Override
    public PageInfo<CouponDTO> queryAllCouponInfo(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize, true);
        List<CouponEntity> coupons = couponDao.queryAllCouponInfo();
        return BeansListUtils.copyListPageInfo(coupons, CouponDTO.class);
    }

}
