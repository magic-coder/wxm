package com.all580.order.api.service.event;

import com.all580.order.api.OrderConstant;
import com.all580.order.api.model.OrderAuditEventParam;
import com.framework.common.event.EventService;
import com.framework.common.mns.MnsSubscribeAction;

/**
 * @author zhouxianjun(Alone)
 * @ClassName:
 * @Description: 订单审核事件
 * @date 2017/1/24 14:51
 */
@EventService(OrderConstant.EventType.ORDER_AUDIT)
public interface OrderAuditSyncDataEvent extends MnsSubscribeAction<OrderAuditEventParam> {
}
