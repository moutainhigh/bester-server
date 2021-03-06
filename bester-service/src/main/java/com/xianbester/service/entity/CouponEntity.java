package com.xianbester.service.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 优惠券实体
 *
 * @author zhangqiang
 * @date 2018-12-17
 */
@Data
public class CouponEntity {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 商户ID
     */
    private String  shopId;

    /**
     * 优惠券图片
     */
    private String imageUrl;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 优惠券余量
     */
    private Integer margin;

    /**
     * 优惠形式（1：指定XX现金，2：折扣;1和2互斥)
     */
    private Integer couponType;

    /**
     * 优惠现金
     */
    private BigDecimal offerCash;

    /**
     * 优惠折扣
     */
    private BigDecimal offerDiscount;

    /**
     * 使用门槛（满XX元可用,为零时则代表使用条件无限制)
     */
    private BigDecimal threshold;

    /**
     * 可领取的会员等级
     */
    private Integer vipLevel;

    /**
     * 是否限制领取 0--不限制 1--限制
     */
    private Integer limitNum;

    /**
     * 有效期（天数）
     */
    private Integer validityPeriod;

    /**
     * 可用商品
     */
    private String available;

    /**
     * 使用说明
     */
    private String description;

    /**
     * 添加时间
     */
    private Date addTime;

    /**
     * 更新时间
     */
    private Date updateTime;


}
