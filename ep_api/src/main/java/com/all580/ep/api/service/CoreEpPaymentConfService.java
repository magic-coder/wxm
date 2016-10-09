package com.all580.ep.api.service;


import com.all580.ep.api.entity.CoreEpPaymentConf;
import com.framework.common.Result;

import java.util.List;
import java.util.Map;

public interface CoreEpPaymentConfService {

   /**
    *初始支付方式配置
    * @param
    * @return
     */
    Result<Integer> create(Map params);

    /**
     * 添加收款方式配置
     * @param map
     * @return
     */
   Result<Integer> add(Map map);

   /**
    *修改收款方式配置
    * @param map
    * @return
     */
   Result<Integer> update(Map map);

   /**
    * 更新状态
    * @param map
    * @return
     */
   Result<Integer> updateStatus(Map map);
   CoreEpPaymentConf findById( Integer core_ep_id);
   List<CoreEpPaymentConf> isExists(Map map);
}