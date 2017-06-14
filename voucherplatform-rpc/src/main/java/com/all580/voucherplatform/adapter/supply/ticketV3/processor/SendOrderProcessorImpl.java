package com.all580.voucherplatform.adapter.supply.ticketV3.processor;

import com.all580.voucherplatform.adapter.ProcessorService;
import com.all580.voucherplatform.entity.Supply;
import com.all580.voucherplatform.manager.order.OrderSupplyReceiveManager;
import com.framework.common.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by Linv2 on 2017-06-14.
 */
@Service
@Slf4j
public class SendOrderProcessorImpl implements ProcessorService<Supply> {
    private static final String ACTION = "queryProductRsp";
    @Autowired
    private OrderSupplyReceiveManager orderSupplyReceiveManager;

    @Override
    public Object processor(Supply supply, Map map) {
        String batch = CommonUtil.objectParseString(map.get("batch"));
        List<Map> mapList = (List<Map>) map.get("orders");
        for (Map mapOrder : mapList) {
            String voucherId = CommonUtil.objectParseString(mapOrder.get("voucherId"));
            String ticketOrderId = CommonUtil.objectParseString(mapOrder.get("ticketOrderId"));
            orderSupplyReceiveManager.Receive(voucherId, ticketOrderId);
        }
        return null;
    }

    @Override
    public String getAction() {
        return ACTION;
    }
}
