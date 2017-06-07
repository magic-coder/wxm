package com.all580.order.service.event;

import com.all580.order.api.OrderConstant;
import com.all580.order.api.model.RefundTicketEventParam;
import com.all580.order.api.service.event.RefundTicketEvent;
import com.all580.order.dao.OrderItemMapper;
import com.all580.order.dao.OrderMapper;
import com.all580.order.dao.RefundOrderMapper;
import com.all580.order.entity.Order;
import com.all580.order.entity.OrderItem;
import com.all580.order.entity.RefundOrder;
import com.all580.order.manager.BookingOrderManager;
import com.all580.order.manager.SmsManager;
import com.framework.common.Result;
import com.framework.common.outside.JobAspect;
import com.framework.common.outside.JobTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouxianjun(Alone)
 * @ClassName:
 * @Description:
 * @date 2017/2/5 9:54
 */
@Service
@Slf4j
public class RefundTicketEventImpl implements RefundTicketEvent {
    @Autowired
    private RefundOrderMapper refundOrderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private SmsManager smsManager;
    @Autowired
    private JobAspect jobManager;
    @Autowired
    private BookingOrderManager bookingOrderManager;

    @Override
    @JobTask
    public Result process(String msgId, RefundTicketEventParam content, Date createDate) {
        RefundOrder refundOrder = refundOrderMapper.selectByPrimaryKey(content.getRefundId());
        Assert.notNull(refundOrder, "退订订单不存在");
        OrderItem orderItem = orderItemMapper.selectByPrimaryKey(refundOrder.getOrder_item_id());
        log.info(OrderConstant.LogOperateCode.NAME, bookingOrderManager.orderLog(null, orderItem.getId(),
                0, "ORDER_EVENT", OrderConstant.LogOperateCode.REFUND_TICKET,
                refundOrder.getQuantity(), String.format("退票%s:%s", content.isStatus() ? "成功" : "失败", refundOrder.getNumber()), String.valueOf(refundOrder.getLocal_refund_serial_no())));
        if (content.isStatus()) {
            // 还库存 记录任务
            Map<String, String> jobParams = new HashMap<>();
            jobParams.put("refundId", String.valueOf(refundOrder.getId()));
            jobManager.addJob(OrderConstant.Actions.REFUND_STOCK, Collections.singleton(jobParams));

            // 退款
            Order order = orderMapper.selectByPrimaryKey(orderItem.getOrder_id());
            Map<String, String> jobRefundMoneyParams = new HashMap<>();
            jobRefundMoneyParams.put("ordCode", String.valueOf(order.getNumber()));
            jobRefundMoneyParams.put("serialNum", String.valueOf(order.getNumber()));
            jobManager.addJob(OrderConstant.Actions.REFUND_MONEY, Collections.singleton(jobRefundMoneyParams));
        } else {
            // 发送短信
            smsManager.sendRefundFailSms(orderItem, refundOrder);
        }
        return new Result(true);
    }
}
