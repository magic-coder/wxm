package com.all580.order.service.event;

import com.all580.order.api.OrderConstant;
import com.all580.order.api.model.RefundAuditEventParam;
import com.all580.order.api.service.event.RefundAuditEvent;
import com.all580.order.dao.OrderItemMapper;
import com.all580.order.dao.OrderMapper;
import com.all580.order.dao.RefundOrderMapper;
import com.all580.order.entity.Order;
import com.all580.order.entity.OrderItem;
import com.all580.order.entity.RefundOrder;
import com.all580.order.manager.RefundOrderManager;
import com.all580.order.manager.SmsManager;
import com.framework.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;

/**
 * @author zhouxianjun(Alone)
 * @ClassName:
 * @Description:
 * @date 2017/2/4 16:29
 */
@Service
@Slf4j
public class RefundAuditEventImpl implements RefundAuditEvent {
    @Autowired
    private RefundOrderMapper refundOrderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RefundOrderManager refundOrderManager;
    @Autowired
    private SmsManager smsManager;

    @Override
    public Result process(String msgId, RefundAuditEventParam content, Date createDate) {
        RefundOrder refundOrder = refundOrderMapper.selectByPrimaryKey(content.getRefundId());
        Assert.notNull(refundOrder, "退订订单不存在");

        log.info(OrderConstant.LogOperateCode.NAME, refundOrderManager.orderLog(null, refundOrder.getOrder_item_id(),
                refundOrder.getAudit_user_id(), refundOrder.getAudit_user_name(),
                content.isStatus() ? OrderConstant.LogOperateCode.REFUND_AUDIT_PASS_SUCCESS : OrderConstant.LogOperateCode.REFUND_AUDIT_REJECT_SUCCESS,
                refundOrder.getQuantity(), String.format("退订申请审核:退订订单:%s", refundOrder.getNumber()), String.valueOf(refundOrder.getNumber())));

        OrderItem orderItem = orderItemMapper.selectByPrimaryKey(refundOrder.getOrder_item_id());
        if (content.isStatus()) {
            // 通过
            Assert.notNull(orderItem, "子订单不存在");
            Order order = orderMapper.selectByPrimaryKey(orderItem.getOrder_id());
            Assert.notNull(order, "订单不存在");
            refundOrderManager.auditSuccess(orderItem, refundOrder, order);
            return new Result(true);
        }

        // 拒绝
        refundOrderManager.refundFail(orderItem, refundOrder);
        smsManager.sendRefundFailSms(orderItem, refundOrder, "拒绝退订");
        return new Result(true);
    }
}
