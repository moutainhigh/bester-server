package com.xianbester.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xianbester.api.constant.OrderRankType;
import com.xianbester.api.dto.*;
import com.xianbester.api.service.OrderRecordService;
import com.xianbester.service.dao.OrderRecordMapper;
import com.xianbester.service.entity.OrderNumberEntity;
import com.xianbester.service.entity.OrderRecordCountEntity;
import com.xianbester.service.entity.OrderRecordEntity;
import com.xianbester.service.entity.OrderRecordJsonEntity;
import com.xianbester.service.util.BeansListUtils;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author liuwen
 */
@Service
@Component
public class OrderRecordServiceImpl implements OrderRecordService {

    @Resource
    private OrderRecordMapper orderRecordMapper;

    @Override
    public List<ObjectMapDTO<Integer, Object>> findAreaShopRankByType(OrderRecordRequest request) {
        Assert.notNull(request, "请求不能为空！");
        Assert.isTrue(request.getLimit() > 0 && request.getId() > 0, "返回记录数和areaId必须大于0！");
        Assert.isTrue(request.getStartTime().before(request.getEndTime()), "结束时间不得早于开始时间！");
        Assert.isTrue(request.getType() >= 0, "必须指定排序方式！");

        int areaId = request.getId();
        int limit = request.getLimit();
        Date start = request.getStartTime();
        Date end = request.getEndTime();

        List<OrderNumberEntity> rank = Lists.newArrayList();
        if (request.getType() == OrderRankType.ORDER_NUM_COUNT) {
            rank = orderRecordMapper.findAreaShopOrderNumRank(areaId, limit, start, end);
        } else if (request.getType() == OrderRankType.ORDER_PRICE_COUNT) {
            rank = orderRecordMapper.findAreaShopOrderPriceRank(areaId, limit, start, end);
        }
        if (CollectionUtils.isEmpty(rank)) {
            return Lists.newArrayList();
        }
        List<ObjectMapDTO<Integer, Object>> result = Lists.newArrayList();
        rank.forEach(entity -> result.add(new ObjectMapDTO<>(entity.getId(), entity.getResult())));
        return result;
    }

    @Override
    public Map<Integer, Object> findAreaOrderNumber(Date startTime, Date endTime, int type) {
        List<OrderNumberEntity> areaOrderNum = null;
        if (type == OrderRankType.ORDER_NUM_COUNT) {
            areaOrderNum = orderRecordMapper.findAreaOrderNum(startTime, endTime);
        } else if (type == OrderRankType.ORDER_PRICE_COUNT) {
            areaOrderNum = orderRecordMapper.findAreaOrderPrice(startTime, endTime);
        }
        if (CollectionUtils.isEmpty(areaOrderNum)) {
            return Collections.emptyMap();
        }
        return areaOrderNum.stream().collect(Collectors.toMap(OrderNumberEntity::getId, OrderNumberEntity::getResult));
    }

    @Override
    public PageInfo<OrderRecordDTO> pageFindOrderRecordByShopId(int shopId, int pageNum, int pageSize,
                                                                Date startTime, Date endTime) {
        PageHelper.startPage(pageNum, pageSize, true);
        List<OrderRecordEntity> entityList = orderRecordMapper.selectOrderRecordsByShopId(shopId, startTime, endTime);
        return BeansListUtils.copyListPageInfo(entityList, OrderRecordDTO.class);
    }

    @Override
    public PageInfo<OrderRecordDTO> pageFindOrderRecordByUserId(int userId, int pageNum, int pageSize,
                                                                Date startTime, Date endTime) {
        PageHelper.startPage(pageNum, pageSize, true);
        List<OrderRecordEntity> entityList = orderRecordMapper.selectOrderRecordsByUserId(userId, startTime, endTime);
        return BeansListUtils.copyListPageInfo(entityList, OrderRecordDTO.class);
    }

    @Override
    public Map<Integer, Object> findShopIdsRankByOrders(int limit, Date startTime, Date endTime, int type) {
        if (limit <= 0 || startTime.after(endTime)) {
            return Collections.emptyMap();
        }
        List<OrderNumberEntity> orderNumberEntities = null;
        if (type == OrderRankType.ORDER_NUM_COUNT) {
            orderNumberEntities = orderRecordMapper.findOrderNumTop(limit, startTime, endTime);
        } else if (type == OrderRankType.ORDER_PRICE_COUNT) {
            orderNumberEntities = orderRecordMapper.findOrderPriceTop(limit, startTime, endTime);
        }
        if (CollectionUtils.isEmpty(orderNumberEntities)) {
            return Collections.emptyMap();
        }
        return orderNumberEntities.stream().collect(Collectors
                .toMap(OrderNumberEntity::getId, OrderNumberEntity::getResult));
    }

    @Override
    public OrderRecordDTO findOrderDetailsByOrderId(int orderId) {
        OrderRecordEntity entity = orderRecordMapper.selectById(orderId);
        if (entity == null) {
            return null;
        }
        OrderRecordDTO orderRecordDTO = new OrderRecordDTO();
        BeanUtils.copyProperties(entity, orderRecordDTO);
        return orderRecordDTO;
    }

    @Override
    public List<OrderRecordDTO> findOrdersByUserId(int userId, Date startTime, Date endTime) {
        List<OrderRecordEntity> entityList = orderRecordMapper.selectOrderRecordsByUserId(userId, startTime, endTime);
        if (CollectionUtils.isEmpty(entityList)) {
            return Collections.emptyList();
        }
        return BeansListUtils.copyListProperties(entityList, OrderRecordDTO.class);
    }

    @Override
    public List<OrderRecordDTO> findOrdersByShopId(int shopId, Date startTime, Date endTime) {
        List<OrderRecordEntity> entityList = orderRecordMapper.selectOrderRecordsByShopId(shopId, startTime, endTime);
        if (CollectionUtils.isEmpty(entityList)) {
            return Collections.emptyList();
        }
        return BeansListUtils.copyListProperties(entityList, OrderRecordDTO.class);
    }

    @Override
    public int addOrderInfo(OrderRecordDTO orderRecordDTO) {
        if (orderRecordDTO == null) {
            return 0;
        }
        OrderRecordEntity entity = new OrderRecordEntity();
        BeanUtils.copyProperties(orderRecordDTO, entity);
        orderRecordMapper.addOrderInfo(entity);
        return entity.getOrderId();
    }

    /**
     * 查询该用户所有的订单
     *
     * @param userId 用户ID
     * @return
     */
    @Override
    public List<OrderRecordJsonDTO> findOrderRecordByUserId(String userId, String year, String month) {
        List<OrderRecordJsonEntity> list = orderRecordMapper.findOrderRecordByUserId(userId, year, month);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return BeansListUtils.copyListProperties(list, OrderRecordJsonDTO.class);
    }

    /**
     * 查找大于我的消费额的用户数
     *
     * @param totalPrice
     * @return
     */
    @Override
    public int findUsersWhoAreLargeThanMySpending(BigDecimal totalPrice, String year, String month) {
        return orderRecordMapper.findUsersWhoAreLargeThanMySpending(totalPrice, year, month);
    }

    @Override
    public OrderRecordCountDTO townOrderRecordCount(OrderRecordRequest request) {
        OrderRecordCountEntity orderData = orderRecordMapper.townOrderRecordCount(request.getStartTime(), request.getEndTime());
        OrderRecordCountDTO orderCount = new OrderRecordCountDTO();
        BeanUtils.copyProperties(orderData, orderCount);
        if (orderData.getPeopleNum() != null && !orderData.getPeopleNum().equals(0L)) {
            orderCount.setAveragePrice(orderData.getPrice().divide(BigDecimal.valueOf(orderData.getPeopleNum()), 2, BigDecimal.ROUND_HALF_UP));
        } else {
            orderCount.setAveragePrice(BigDecimal.ZERO);
        }
        return orderCount;
    }

    @Override
    public Map<Object, Integer> selectTypeCount(int day) {
        Date days = new DateTime().minusDays(day).toDate();
        List<OrderNumberEntity> orderNumberEntityList = orderRecordMapper.selectTypeCount(days);
        if (CollectionUtils.isEmpty(orderNumberEntityList)) {
            return null;
        }
        return orderNumberEntityList.stream().collect(Collectors.toMap(OrderNumberEntity::getResult, OrderNumberEntity::getId));
    }

    @Override
    public Map<String, Integer> orderTypeDistribution(Date startTime, Date endTime) {
        if (startTime == null || endTime == null) {
            return Collections.emptyMap();
        }
        List<OrderNumberEntity> entities = orderRecordMapper.orderTypeDistribution(startTime, endTime);
        if (CollectionUtils.isEmpty(entities)) {
            return Collections.emptyMap();
        }
        Map<String, Integer> stringIntegerMap = Maps.newHashMap();
        for (OrderNumberEntity orderNumberEntity : entities) {
            stringIntegerMap.put((String) orderNumberEntity.getResult(), orderNumberEntity.getId());
        }
        return stringIntegerMap;
    }

}
