package com.xianbester.service.dao;

import com.xianbester.service.entity.ChainUserInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * @author zhangqiang
 * @date 2019-07-16
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ChainUserInfoDaoTest {

    @Resource
    private ChainUserInfoDao chainUserInfoDao;

    @Test
    public void testAdd() {
        ChainUserInfo userinfo = new ChainUserInfo();
        String id = UUID.randomUUID().toString();
        userinfo.setId(id);
        userinfo.setUsername("hello");
        userinfo.setPassword("123");
        int i = chainUserInfoDao.addUser(userinfo);
        System.out.println(i);
    }



}
