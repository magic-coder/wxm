package com.all580.ep.service;

import com.all580.ep.api.service.EpBalanceThresholdService;
import com.all580.ep.dao.EpParamMapper;
import com.all580.notice.api.service.BalanceChangeSubscribeService;
import com.framework.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by wxming on 2016/11/23 0023.
 */
@Service
@Transactional(rollbackFor = {Exception.class, RuntimeException.class})
@Slf4j
public class BalanceChangeSubscribeServiceImpl implements BalanceChangeSubscribeService {

    @Autowired
    private EpBalanceThresholdService epBalanceThresholdService;

    @Autowired
    private EpParamMapper epParamMapper;

    @Override
    public Result process(String s, List list, Date date) {
        if (list == null) {
            return new Result(true);
        }
        for (Object o : list) {
            Map map = (Map) o;
//            Map<String,Object> params = new HashMap<>();
//            params.put("id", map.get("ep_id"));
//            params.put(EpConstant.EpKey.CORE_EP_ID, map.get(EpConstant.EpKey.CORE_EP_ID));
//            params.put("balance", map.get("balance"));
//            params.put("history_balance",map.get("history_balance"));

            map.put("id",map.get("ep_id"));
            epBalanceThresholdService.warn(map);
        }
        return new Result(true);
    }
}
