package com.all580.ep.service;

import com.all580.ep.api.service.CoreEpAccessService;
import com.all580.ep.api.service.CoreEpChannelService;
import com.all580.ep.api.service.CoreEpPaymentConfService;
import com.all580.ep.api.service.EpService;
import com.all580.ep.com.Common;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @ClassName:
 * @Description:
 * @date 2016/9/28 10:21
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/*.xml"})
public class epServiceTest {
    @Resource
    private EpService epService;
    @Resource
    private CoreEpAccessService coreEpAccessService;

    @Resource
    private CoreEpPaymentConfService coreEpPaymentConfService;

    @Resource
    private CoreEpChannelService coreEpChannelService;

    @Test
    public void createTest() {
        Map map = new HashMap<String,Object>();
        map.put("supplier_name","adsf");
       // System.out.println(coreEpPaymentConfService.add(map)+"      *****");
        //System.out.println(epService.selectPlatformId(2).get());
      // coreEpChannelService.cancle(2);
//        map.put("id",2);
//        map.put("access_id", Common.getAccessId());
       // epService.freeze(map);
        System.out.println(epService.select(map).get().size());
       // map.put("id",11);
//        map.put("access_id","123");
//        map.put("link_phone","13417325939");
//        map.put("id","123");
//
//        map.put("access_key", Common.getAccessKey());
//        map.put("link", "123");
//        coreEpAccessService.create(map);
//        System.out.println(coreEpAccessService.select(map));
//        Ep ep = new Ep();
//        ep.setAccess_id("1212");
//        ep.setAccess_key("key");
//        ep.setAddress("地址");

      //  System.out.println(epService.selectPlatformId(6).get()+"   ***");
        //System.out.print(epService.getEp(new Integer[]{1,2},new String[]{"ep_type","address"}));
//        map.put("access_id","");
//        map.put("core_ep_id","1");
        map.put("access_id","83YL8MD4JJAX");
        map.put("sdfasd","");
//        epService.validate(map);
//        coreEpPaymentConfService.create(map);
//        coreEpPaymentConfService.add(map);
//        coreEpPaymentConfService.update(map);
       // epService.validate(map);
        //epService.findById(1);
//        epService.create(null);
//        epService.all(null);
//        epService.validate(null);
    }
}