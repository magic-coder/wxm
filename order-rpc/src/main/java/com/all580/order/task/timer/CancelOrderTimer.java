package com.all580.order.task.timer;

import com.all580.order.dao.OrderMapper;
import com.all580.order.entity.Order;
import com.all580.order.manager.RefundOrderManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zhouxianjun(Alone)
 * @ClassName:
 * @Description: 取消订单定时器
 * @date 2016/10/13 16:26
 */
@Component
@Slf4j
public class CancelOrderTimer {
    @Value("${order.pay.timeout}")
    private Integer payTimeOut;
    @Value("${order.audit.timeout}")
    private Integer auditTimeOut;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RefundOrderManager refundOrderManager;

    @Scheduled(fixedDelay = 30000)
    public void payTimerJob() {
        try {
            List<Order> orderList = orderMapper.selectNoPayForMinute(payTimeOut);
            if (orderList != null && !orderList.isEmpty()) {
                boolean retry = true;
                for (Order order : orderList) {
                    try {
                        refundOrderManager.cancel(order.getNumber());
                    } catch (Exception e) {
                        retry = false;
                        log.error("订单:"+ order.getNumber() +"未支付超时取消异常", e);
                    }
                }
                if (retry) {
                    payTimerJob();
                }
            }
        } catch (Exception e) {
            log.error("未支付订单超时取消异常", e);
        }
    }

    @Scheduled(fixedDelay = 30000)
    public void auditTimerJob() {
        try {
            List<Order> orderList = orderMapper.selectAuditWaitForMinute(auditTimeOut);
            if (orderList != null && !orderList.isEmpty()) {
                boolean retry = true;
                for (Order order : orderList) {
                    try {
                        refundOrderManager.cancel(order.getNumber());
                    } catch (Exception e) {
                        retry = false;
                        log.error("订单:"+ order.getNumber() +"待审核超时取消异常", e);
                    }
                }
                if (retry) {
                    auditTimerJob();
                }
            }
        } catch (Exception e) {
            log.error("待审核订单超时取消异常", e);
        }
    }
}
