package com.all580.order.api.service.event;

import com.all580.order.api.model.ConsumeTicketEventParam;
import com.framework.common.mns.MnsSubscribeAction;

/**
 * @author zhouxianjun(Alone)
 * @ClassName:
 * @Description: 核销票据事件
 * @date 2017/2/9 11:48
 */
public interface ConsumeTicketEvent extends MnsSubscribeAction<ConsumeTicketEventParam> {
}
