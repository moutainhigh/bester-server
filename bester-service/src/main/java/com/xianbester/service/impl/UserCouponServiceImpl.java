package com.xianbester.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.xianbester.api.constant.Coupon;
import com.xianbester.api.dto.UserCouponDTO;
import com.xianbester.api.service.UserCouponService;
import com.xianbester.service.dao.CouponDao;
import com.xianbester.service.dao.UserCouponDao;
import com.xianbester.service.entity.CountEntity;
import com.xianbester.service.entity.CouponEntity;
import com.xianbester.service.entity.UserCouponEntity;
import com.xianbester.service.util.BeansListUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhangqiang
 * @date 2018-12-18
 */
@Service
@Component
public class UserCouponServiceImpl implements UserCouponService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserCouponServiceImpl.class);
    @Resource
    private CouponDao couponDao;
    @Resource
    private UserCouponDao userCouponDao;

    @Override
    public int findCouponCountById(Integer userId, Integer couponId) {
        return userCouponDao.findCouponCountById(userId, couponId);
    }

    @Override
    public List<UserCouponDTO> findUserCouponByStatus(Integer userId, Integer status) {
        List<UserCouponEntity> couponEntities;
        if (Coupon.EXPIRED.equals(status)) {
            couponEntities = userCouponDao.findExpiredCoupon(userId);
        } else {
            couponEntities = userCouponDao.findUnusedAndUsedCoupon(userId, status);
        }
        return BeansListUtils.copyListProperties(couponEntities, UserCouponDTO.class);
    }

    @Override
    public int receiveCoupon(Integer userId, Integer couponId) {
        Assert.isTrue(userId != null && userId > 0, "userId不合法！");
        Assert.isTrue(couponId != null && couponId > 0, "couponId不合法！");
        CouponEntity couponEntity = couponDao.inquireCouponById(couponId);
        if (couponEntity == null || couponEntity.getMargin() <= 0) {
            return 0;
        }
        DateTime today = new DateTime();
        Date failureTime = today.plusDays(couponEntity.getValidityPeriod()).toDate();
        UserCouponEntity userCouponEntity = new UserCouponEntity();
        userCouponEntity.setUserId(userId);
        userCouponEntity.setCouponId(couponId);
        userCouponEntity.setFailureTime(failureTime);
        userCouponEntity.setStatus(Coupon.UNUSED);
        int couponResult = couponDao.updateCouponNum(couponId);
        if (couponResult <= 0) {
            LOGGER.error("优惠券数量减一失败！couponId: {}", couponId);
            return couponResult;
        }
        return userCouponDao.receiveCoupon(userCouponEntity);
    }

    @Override
    public Map<Integer, Integer> selectCouponCount(Integer userId, List<Integer> couponIds) {
        List<CountEntity> entities = userCouponDao.selectCouponCount(userId, couponIds);
        if (CollectionUtils.isEmpty(entities)) {
            return Collections.emptyMap();
        }
        return entities.stream().collect(Collectors.toMap(CountEntity::getId, CountEntity::getResult));
    }
}
