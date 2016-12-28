package com.all580.order.task.action;

import com.all580.order.api.OrderConstant;
import com.all580.order.dao.*;
import com.all580.order.entity.*;
import com.all580.order.manager.BookingOrderManager;
import com.all580.product.api.consts.ProductConstants;
import com.all580.voucher.api.conf.VoucherConstant;
import com.all580.voucher.api.model.SendTicketParams;
import com.all580.voucher.api.model.group.SendGroupTicketParams;
import com.all580.voucher.api.service.VoucherRPCService;
import com.framework.common.validate.ParamsMapValidate;
import com.framework.common.validate.ValidRule;
import com.github.ltsopensource.core.domain.Action;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.github.ltsopensource.tasktracker.runner.JobRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhouxianjun(Alone)
 * @ClassName:
 * @Description: 出票任务
 * @date 2016/10/25 11:30
 */
@Component(OrderConstant.Actions.SEND_GROUP_TICKET)
@Slf4j
public class SendGroupTicketAction implements JobRunner {
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private OrderItemDetailMapper orderItemDetailMapper;
    @Autowired
    private VisitorMapper visitorMapper;
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private VoucherRPCService voucherRPCService;

    @Autowired
    private BookingOrderManager bookingOrderManager;

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Result run(JobContext jobContext) throws Throwable {
        Map<String, String> params = jobContext.getJob().getExtParams();
        validateParams(params);

        int orderItemId = Integer.parseInt(params.get("orderItemId"));
        OrderItem orderItem = orderItemMapper.selectByPrimaryKey(orderItemId);
        if (orderItem == null) {
            log.warn("出票任务,子订单不存在");
            throw new Exception("子订单不存在");
        }
        if (!(orderItem.getGroup_id() != null && orderItem.getGroup_id() != 0 &&
                orderItem.getPro_sub_ticket_type() != null && orderItem.getPro_sub_ticket_type() == ProductConstants.TeamTicketType.TEAM)) {
            log.warn("出票任务,该订单不是团队订单");
            throw new Exception("该订单不是团队订单");
        }
        Order order = orderMapper.selectByPrimaryKey(orderItem.getOrder_id());
        if (order == null) {
            log.warn("出票任务,订单不存在");
            throw new Exception("订单不存在");
        }
        Group group = groupMapper.selectByPrimaryKey(orderItem.getGroup_id());
        if (group == null) {
            log.warn("出票任务,团队不存在");
            throw new Exception("团队不存在");
        }
        orderItem.setStatus(OrderConstant.OrderItemStatus.TICKETING);
        orderItemMapper.updateByPrimaryKeySelective(orderItem);

        List<OrderItemDetail> detailList = orderItemDetailMapper.selectByItemId(orderItem.getId());
        OrderItemDetail detail = detailList.get(0); // 景点只有一天
        SendGroupTicketParams sendGroupTicketParams = new SendGroupTicketParams();
        sendGroupTicketParams.setOrderSn(orderItem.getNumber());
        sendGroupTicketParams.setProductSn(orderItem.getPro_sub_number());
        sendGroupTicketParams.setPaymentType(orderItem.getPayment_flag() == ProductConstants.PayType.PREPAY ?
                VoucherConstant.PaymentType.ONLINE : VoucherConstant.PaymentType.LIVE);
        sendGroupTicketParams.setConsumeType(VoucherConstant.ConsumeType.COUNT); // 默认
        sendGroupTicketParams.setValidTime(detail.getEffective_date());
        sendGroupTicketParams.setInvalidTime(detail.getExpiry_date());
        // 设置团队信息
        sendGroupTicketParams.setGroupNumber(group.getNumber());
        sendGroupTicketParams.setGuideName(group.getGuide_name());
        sendGroupTicketParams.setGuidePhone(group.getGuide_phone());
        sendGroupTicketParams.setGuideSid(group.getGuide_sid());
        sendGroupTicketParams.setGuideCard(group.getGuide_card());
        sendGroupTicketParams.setManagerName(group.getManager_name());
        sendGroupTicketParams.setManagerPhone(group.getManager_phone());
        sendGroupTicketParams.setTravelName(group.getTravel_name());
        sendGroupTicketParams.setProvince(group.getProvince());
        sendGroupTicketParams.setCity(group.getCity());
        sendGroupTicketParams.setArea(group.getArea());
        sendGroupTicketParams.setAddress(group.getAddress());

        sendGroupTicketParams.setPrice(orderItem.getSale_amount() / orderItem.getQuantity());
        sendGroupTicketParams.setQuantity(orderItem.getQuantity());
        if (order.getStatus() == OrderConstant.OrderStatus.PAID_HANDLING ||
                order.getStatus() == OrderConstant.OrderStatus.PAID) {
            sendGroupTicketParams.setPayTime(order.getPay_time());
        }
        sendGroupTicketParams.setMaProductId(orderItem.getMa_product_id());
        sendGroupTicketParams.setSendSms(true);

        List<Visitor> visitorList = visitorMapper.selectByOrderDetailId(detail.getId());
        List<com.all580.voucher.api.model.Visitor> contacts = new ArrayList<>();
        for (Visitor visitor : visitorList) {
            com.all580.voucher.api.model.Visitor v = new com.all580.voucher.api.model.Visitor();
            BeanUtils.copyProperties(visitor, v);
            contacts.add(v);
        }
        sendGroupTicketParams.setVisitors(contacts);
        com.framework.common.Result r = voucherRPCService.sendGroupTicket(orderItem.getEp_ma_id(), sendGroupTicketParams);
        if (!r.isSuccess()) {
            log.warn("子订单:{},出票失败:{}", orderItem.getNumber(), r.getError());
            throw new Exception("出票失败:" + r.getError());
        }

        bookingOrderManager.syncSendingData(orderItemId);
        return new Result(Action.EXECUTE_SUCCESS);
    }

    /**
     * 验证参数
     * @param params
     */
    private void validateParams(Map<String, String> params) {
        if (params == null) {
            throw new RuntimeException("出票任务参数为空");
        }
        Map<String[], ValidRule[]> rules = new HashMap<>();
        rules.put(new String[]{
                "orderItemId" // 子订单ID
        }, new ValidRule[]{new ValidRule.NotNull(), new ValidRule.Digits()});

        ParamsMapValidate.validate(params, rules);
    }
}