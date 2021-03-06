package com.xianbester.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Maps;
import com.xianbester.api.dto.UserPriceCountDTO;
import com.xianbester.api.service.ShopUserAnalysisService;
import com.xianbester.service.dao.ShopUserAnalysisMapper;
import com.xianbester.service.entity.UserFrequencyEntity;
import com.xianbester.service.entity.UserPriceCountEntity;
import com.xianbester.service.entity.UserSexAndAgeEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Component
public class ShopUserAnalysisServiceImpl implements ShopUserAnalysisService {

    @Resource
    private ShopUserAnalysisMapper shopUserAnalysisMapper;

    @Override
    public Map<String, Map<String, Integer>> selectAgeAndSexCount(Integer shopId, Date startTime, Date endTime) {
        final int[] ageArray = {0, 18, 46, 66};
        List<UserSexAndAgeEntity> ageAndSexEntities = shopUserAnalysisMapper.ageAndSexSplit(shopId, startTime, endTime);
        if (CollectionUtils.isEmpty(ageAndSexEntities)) {
            return Maps.newHashMap();
        }
        Map<String, Map<String, Integer>> ageAndSex = Maps.newHashMap();
        Map<String, Integer> maleAgeSplit = Maps.newHashMap();
        Map<String, Integer> femaleAgeSplit = Maps.newHashMap();
        ageAndSex.put("male", maleAgeSplit);
        ageAndSex.put("female", femaleAgeSplit);
        for (UserSexAndAgeEntity userSexAndAgeEntity : ageAndSexEntities) {
            String ageRegion = this.regionName(ageArray, userSexAndAgeEntity.getAge());
            if (userSexAndAgeEntity.getSex() == 1) {
                maleAgeSplit.put(ageRegion, maleAgeSplit.getOrDefault(ageRegion, 0) + userSexAndAgeEntity.getNumberOfPeople());
            } else if (userSexAndAgeEntity.getSex() == 2) {
                femaleAgeSplit.put(ageRegion, femaleAgeSplit.getOrDefault(ageRegion, 0) + userSexAndAgeEntity.getNumberOfPeople());
            }
        }
        return ageAndSex;
    }

    @Override
    public UserPriceCountDTO selectUserPriceCount(Integer shopId, Date startTime, Date endTime) {
        // 统计消费分布上下浮动百分比
        final BigDecimal ratio = new BigDecimal("0.30");
        UserPriceCountDTO userPriceCountDTO = new UserPriceCountDTO();
        Map<String, BigDecimal> priceMap = shopUserAnalysisMapper.averageAndTotalPrice(shopId, startTime, endTime);
        if (CollectionUtils.isEmpty(priceMap)) {
            return userPriceCountDTO;
        }
        BigDecimal averagePrice = priceMap.get("averagePrice").setScale(2, RoundingMode.HALF_UP);
        BigDecimal low = averagePrice.multiply(new BigDecimal("1.00").subtract(ratio));
        BigDecimal high = averagePrice.multiply(new BigDecimal("1.00").add(ratio));
        UserPriceCountEntity userPriceCount = shopUserAnalysisMapper.userPriceCount(shopId, startTime, endTime, low, high);
        userPriceCountDTO.setAveragePrice(averagePrice);
        userPriceCountDTO.setTotalPrice(priceMap.get("totalPrice"));
        BeanUtils.copyProperties(userPriceCount, userPriceCountDTO.getDistributed());
        return userPriceCountDTO;
    }

    @Override
    public Map<String, Object> selectUserFrequencyCount(Integer shopId, Date startTime, Date endTime) {
        // 根据上述百分比将统计分为三个阶段，放在如下数组中
        int[] frequencySplit = {0, 4, 7};
        BigDecimal compared = new BigDecimal("4.00");
        Map<String, Object> userFrequencyCount = Maps.newHashMap();
        Map<String, BigDecimal> frequencyMap = shopUserAnalysisMapper.averageAndTotalFrequency(shopId, startTime, endTime);
        BigDecimal averageFrequency = frequencyMap.get("averageFrequency").setScale(2, RoundingMode.HALF_UP);
        // 平均值超过compared参数时调整分段
        if (averageFrequency != null && averageFrequency.compareTo(compared) >= 0) {
            this.rebuildFrequency(frequencySplit, averageFrequency);
        }
        List<UserFrequencyEntity> userFrequencyEntities = shopUserAnalysisMapper.userFrequencyCount(shopId, startTime, endTime);
        if (CollectionUtils.isEmpty(userFrequencyEntities)) {
            return userFrequencyCount;
        }
        Map<String, Integer> distributed = Maps.newHashMap();
        for (UserFrequencyEntity userFrequencyEntity : userFrequencyEntities) {
            String frequencyName = regionName(frequencySplit, userFrequencyEntity.getFrequency());
            distributed.put(frequencyName,
                    distributed.getOrDefault(frequencyName, 0) + userFrequencyEntity.getNumberOfPeople());
        }
        userFrequencyCount.put("totalFrequency", frequencyMap.get("totalFrequency"));
        userFrequencyCount.put("averageFrequency", averageFrequency);
        userFrequencyCount.put("distributed", distributed);
        return userFrequencyCount;
    }

    private String regionName(int[] array, int value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < array.length; i++) {
            if (value < array[i]) {
                return sb.append(array[i - 1]).append("-").append(array[i] - 1).toString();
            }
        }
        return sb.append(array[array.length - 1]).append("more").toString();
    }

    private void rebuildFrequency(int[] arr, BigDecimal average) {
        // 调整频率分段
        final BigDecimal ratio = new BigDecimal("0.30");
        arr[1] = average.multiply(new BigDecimal("1.00").subtract(ratio))
                .setScale(0, RoundingMode.HALF_UP).intValue() + 1;
        arr[2] = average.multiply(new BigDecimal("1.00").add(ratio))
                .setScale(0, RoundingMode.HALF_UP).intValue() + 1;
    }

}
