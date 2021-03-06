package com.xianbester.service.service;

import com.xianbester.api.constant.PowerSource;
import com.xianbester.api.constant.PowerStatus;
import com.xianbester.service.dao.PowerRecordDao;
import com.xianbester.service.dao.UserAccountDao;
import com.xianbester.service.entity.PowerEntity;
import com.xianbester.service.entity.UserAccountEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author zhangqiang
 * @date 2018/11/15
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class PowerRecordTest {

    @Resource
    private PowerRecordDao powerRecordDao;

    @Resource
    private UserAccountDao userAccountDao;

    @Test
    public void testAddUserPower() {
        PowerEntity powerEntity = new PowerEntity();
        powerEntity.setUserId(1);
        powerEntity.setValid(PowerStatus.VALID);
        powerEntity.setTemporary(PowerStatus.TEMPORARY);
        powerEntity.setPower(10);
        powerEntity.setSource(PowerSource.FICTION);
        int insert = powerRecordDao.addUserPower(powerEntity);
        Assert.assertTrue(insert > 0);
    }

    @Test
    public void testInsert() {
        UserAccountEntity userAccountEntity = new UserAccountEntity();
        int userId = userAccountDao.insertUserAccountInfo(userAccountEntity);
        System.out.println(userAccountEntity.getUserId());
    }

}
