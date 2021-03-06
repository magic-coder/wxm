package com.all580.order.manager;

import com.all580.ep.api.conf.EpConstant;
import com.all580.ep.api.service.EpService;
import com.all580.order.adapter.RefundOrderInterface;
import com.all580.order.api.OrderConstant;
import com.all580.order.api.model.*;
import com.all580.order.dao.*;
import com.all580.order.dto.AccountDataDto;
import com.all580.order.dto.ConsumeDay;
import com.all580.order.dto.RefundDay;
import com.all580.order.dto.RefundOrderApply;
import com.all580.order.entity.*;
import com.all580.order.util.AccountUtil;
import com.all580.payment.api.conf.PaymentConstant;
import com.all580.payment.api.model.BalanceChangeInfo;
import com.all580.payment.api.model.BalanceChangeRsp;
import com.all580.payment.api.service.ThirdPayService;
import com.all580.product.api.consts.ProductConstants;
import com.all580.product.api.model.ProductSearchParams;
import com.all580.product.api.service.ProductSalesPlanRPCService;
import com.all580.voucher.api.model.group.ModifyGroupTicketParams;
import com.all580.voucher.api.service.VoucherRPCService;
import com.framework.common.Result;
import com.framework.common.event.MnsEvent;
import com.framework.common.event.MnsEventAspect;
import com.framework.common.lang.DateFormatUtils;
import com.framework.common.lang.JsonUtils;
import com.framework.common.lang.UUIDGenerator;
import com.framework.common.outside.JobAspect;
import com.framework.common.outside.JobTask;
import com.framework.common.synchronize.SynchronizeDataMap;
import com.framework.common.util.CommonUtil;
import com.framework.common.validate.CheckIdCardUtils;
import com.github.ltsopensource.core.domain.Action;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.lang.exception.ApiException;
import java.util.*;

/**
 * @author zhouxianjun(Alone)
 * @ClassName:
 * @Description: 锁事务分离的处理类
 * @date 2017/1/3 16:59
 */
@Component
@Slf4j
@Transactional(rollbackFor = {Exception.class, RuntimeException.class})
public class LockTransactionManager {
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private ShippingModifyMapper shippingModifyMapper;
    @Autowired
    private ShippingMapper shippingMapper;
    @Autowired
    private VisitorModifyMapper visitorModifyMapper;
    @Autowired
    private OrderItemAccountMapper orderItemAccountMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private RefundOrderMapper refundOrderMapper;
    @Autowired
    private OrderClearanceSerialMapper orderClearanceSerialMapper;
    @Autowired
    private OrderItemDetailMapper orderItemDetailMapper;
    @Autowired
    private RefundVisitorMapper refundVisitorMapper;
    @Autowired
    private VisitorMapper visitorMapper;
    @Autowired
    private RefundSerialMapper refundSerialMapper;
    @Autowired
    private BookingOrderManager bookingOrderManager;
    @Autowired
    private RefundOrderManager refundOrderManager;
    @Autowired
    private ProductSalesPlanRPCService productSalesPlanRPCService;
    @Autowired
    private ThirdPayService thirdPayService;
    @Autowired
    private VoucherRPCService voucherRPCService;
    @Autowired
    private EpService epService;
    @Autowired
    private MnsEventAspect eventManager;
    @Autowired
    private JobAspect jobManager;

    /**
     * 支付回调
     * @param orderId
     * @param outTransId
     * @param serialNum
     */
    @MnsEvent
    public void paymentCallback(int orderId, String outTransId, String serialNum) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            log.warn("支付成功任务,订单不存在");
            throw new ApiException("订单不存在");
        }

        if (order.getStatus() == OrderConstant.OrderStatus.PAID || order.getStatus() == OrderConstant.OrderStatus.PAID_HANDLING) {
            log.warn("订单号:{} 已支付成功.", order.getNumber());
            return;
        }

        if (order.getStatus() != OrderConstant.OrderStatus.PAYING) {
            log.warn("支付成功任务,订单:{}状态:{}不是支付中", order.getNumber(), order.getStatus());
            throw new ApiException("订单不在待支付状态,当前状态为:" + OrderConstant.OrderStatus.getName(order.getStatus()));
        }
        order.setThird_serial_no(outTransId);
        order.setStatus(OrderConstant.OrderStatus.PAID_HANDLING); // 已支付,处理中
        // 到付 和 余额支付直接支付成功 其它的在分账的时候设置
        if (order.getPayment_type() == null && order.getPay_amount() == 0 ||
                (order.getPayment_type() != null && order.getPayment_type() == PaymentConstant.PaymentType.BALANCE.intValue())) {
            order.setStatus(OrderConstant.OrderStatus.PAID); // 已支付
        }
        orderMapper.updateByPrimaryKeySelective(order);

        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(orderId);
        List<ProductSearchParams> lockParams = new ArrayList<>();
        //是否包含套票产品
        boolean hasPackageProduct = Boolean.FALSE;
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getPro_type() == ProductConstants.ProductType.PACKAGE){
                hasPackageProduct = Boolean.TRUE;
            }
            lockParams.add(bookingOrderManager.parseParams(orderItem));
        }

        // 加已售
        com.framework.common.Result result = productSalesPlanRPCService.addSoldProductStocks(lockParams);
        if (!result.isSuccess()) {
            log.warn("支付成功加已售失败");
            throw new ApiException(result.getError());
        }

        // 子订单状态为未出票
        orderItemMapper.setStatusByOrderId(orderId, OrderConstant.OrderItemStatus.NON_SEND);

        // 触发事件
        eventManager.addEvent(OrderConstant.EventType.PAID, order.getId());

        if (order.getStatus() == OrderConstant.OrderStatus.PAID) {
            if (hasPackageProduct){
                //套票终端订单支付成功，用余额支付所有元素订单
                List<Order> orderList = orderMapper.selectPackageItemOrderById(order.getId());
                if (!CollectionUtils.isEmpty(orderList)){
                    for (Order o : orderList) {
                        Map<String, Object> map = new HashMap<>();
                        map.put(EpConstant.EpKey.EP_ID, o.getBuy_ep_id());
                        map.put(EpConstant.EpKey.CORE_EP_ID, o.getPayee_ep_id());
                        map.put("operator_id", 0);
                        map.put("operator_name", OrderConstant.CREATE_ADAPTER);
                        map.put("order_sn", o.getNumber());
                        map.put("pay_type", PaymentConstant.PaymentType.BALANCE);
                        payment(map, o.getNumber(), PaymentConstant.PaymentType.BALANCE);
                    }
                }
            }
            // 出票
            eventManager.addEvent(OrderConstant.EventType.SPLIT_CREATE_ACCOUNT, order.getId());
        }
    }

    /**
     * 退款回调
     * @param orderId
     * @param ordCode
     * @param serialNum
     * @param success
     * @throws Exception
     */
    @MnsEvent
    @JobTask
    public void refundMoneyCallback(int orderId, Long ordCode, String serialNum, boolean success) throws Exception {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            log.warn("退款成功回调,订单不存在");
            throw new Exception("订单不存在");
        }

        eventManager.addEvent(OrderConstant.EventType.REFUND_MONEY, new RefundMoneyEventParam(orderId, serialNum, success));
        if (order.getStatus() == OrderConstant.OrderStatus.PAID_HANDLING) {
            if (!success) {
                addRefundMoneyJob(ordCode, serialNum);
                return;
            }
            // 已支付,处理中(分账失败)退订 直接取消
            // 记录任务
            Map<String, String> jobParams = new HashMap<>();
            jobParams.put("orderId", order.getId().toString());
            jobManager.addJob(OrderConstant.Actions.CANCEL_CALLBACK, Collections.singleton(jobParams));
            return;
        }

        refundOrderManager.refundMoneyAfter(Long.valueOf(serialNum), success);
        if (!success) {
            addRefundMoneyJob(ordCode, serialNum);
        }
    }

    /**
     * 支付分账
     * @param orderId
     * @return
     */
    @MnsEvent
    public com.github.ltsopensource.tasktracker.Result paymentSplitAccount(int orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            log.warn("支付分账,订单不存在");
            throw new ApiException("订单不存在");
        }

        if (order.getStatus() != OrderConstant.OrderStatus.PAID_HANDLING) {
            log.warn("支付分账,订单:{}状态:{}不是分账中", order.getNumber(), order.getStatus());
            throw new ApiException("订单不在分账中状态");
        }

        // 更新状态已支付
        order.setStatus(OrderConstant.OrderStatus.PAID);
        orderMapper.updateByPrimaryKeySelective(order);
        // 分账
        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(orderId);
        List<BalanceChangeInfo> balanceChangeInfoList = bookingOrderManager.packagingPaySplitAccount(order, orderItems);
        Result<BalanceChangeRsp> result = bookingOrderManager.changeBalances(PaymentConstant.BalanceChangeType.PAY_SPLIT, String.valueOf(order.getNumber()), balanceChangeInfoList);
        if (!result.isSuccess() && (result.getCode() == null || result.getCode().intValue() != Result.UNIQUE_KEY_ERROR)) {
            log.warn("支付分账失败:{}", JsonUtils.toJson(result.get()));
            throw new ApiException(result.getError());
        }

        // 出票
        eventManager.addEvent(OrderConstant.EventType.SPLIT_CREATE_ACCOUNT, order.getId());
        return new com.github.ltsopensource.tasktracker.Result(Action.EXECUTE_SUCCESS);
    }

    /**
     * 申请退订
     * @param params
     * @return
     * @throws Exception
     */
    @MnsEvent
    public Result<?> applyRefund(Map params, long itemNo, RefundOrderInterface refundOrderInterface) throws Exception {
        RefundOrderApply apply = refundOrderInterface.validateAndParseParams(itemNo, params);

        if (!StringUtils.isEmpty(apply.getOuter())) {
            RefundOrder refundOrder = refundOrderMapper.selectByItemIdAndOuter(apply.getItem().getId(), apply.getOuter());
            if (refundOrder != null) {
                throw new ApiException(Result.UNIQUE_KEY_ERROR, "重复操作", refundOrderMapper.selectByItemIdAndOuter(apply.getItem().getId(), apply.getOuter()));
            }
        }

        int count = refundOrderMapper.haveRefunding(apply.getItem().getId());
        Assert.isTrue(count <= 0, "您还有未处理完的退订订单");

        refundOrderInterface.checkAuth(apply, params);

        // 每日订单详情
        List<OrderItemDetail> detailList = orderItemDetailMapper.selectByItemId(apply.getItem().getId());
        Assert.notEmpty(detailList, "每天详情异常");
        refundOrderInterface.canBeRefund(apply, detailList, params);
        // 每日退票详情
        Collection<RefundDay> refundDays = refundOrderInterface.getRefundDays(apply, detailList, params);
        Assert.notEmpty(refundDays, "没有可退的票");
        if (refundDays == null || refundDays.isEmpty()) {
            throw new ApiException(Result.REFUNDABLE_LACK, "没有可退的票");
        }
        refundOrderInterface.validateRefundVisitor(apply, refundDays, params);
        // 总退票数量
        Integer quantity = refundOrderInterface.getRefundQuantity(apply, refundDays, params);
        Assert.notNull(quantity, "退票数量异常");

        int[] calcResult = refundOrderInterface.calcRefundMoneyAndFee(apply, detailList, refundDays, params);
        // 计算退款金额
        int money = calcResult[0];
        // 手续费
        int fee = calcResult[1];

        int[] auditSupplierConfig = refundOrderInterface.getRefundAudit(apply, params);
        int auditTicket = auditSupplierConfig[0];
        int auditMoney = auditSupplierConfig[1];
        // 创建退订订单
        RefundOrder refundOrder = refundOrderInterface.insertRefundOrder(apply, refundDays, quantity, money, fee, auditTicket, auditMoney, params);

        refundOrderInterface.hasRemainAndInsertRefundVisitor(apply, refundOrder, detailList, refundDays, params);

        // 退订分账 到付和0元退订不分帐
        if (apply.getItem().getPayment_flag() != ProductConstants.PayType.PAYS && apply.getOrder().getPay_amount() > 0) {
            refundOrderManager.preRefundSplitAccount(apply.getFrom(), refundOrder.getId(), apply.getOrder(), apply.getDate(), refundOrder.getOrder_item_id(), detailList, refundDays);
        }

        // 设置最后一个退订ID
        apply.getItem().setLast_refund_id(refundOrder.getId());
        orderItemMapper.updateByPrimaryKeySelective(apply.getItem());

        // 触发事件
        eventManager.addEvent(OrderConstant.EventType.ORDER_REFUND_APPLY, refundOrder.getId());

        //todo 处理套票元素订单的退订
        if (apply.getItem().getPro_type() == ProductConstants.ProductType.PACKAGE){
            //获取套票元素订单的子订单，然后依次发起退订申请
            List<OrderItem> orderItemList = refundOrderManager.getOrderItemsForPackageOrder(apply.getOrder());
            if (!CollectionUtils.isEmpty(orderItemList)){
                for (OrderItem orderItem : orderItemList) {
                    //已经使用不可退
                    if(orderItem.getUsed_quantity() != null && orderItem.getUsed_quantity() > 0){
                        throw new ApiException(Result.REFUNDABLE_LACK, "该票据已经使用");
                    }
                    //已经退订的不重复退
                    RefundOrder r = refundOrderMapper.selectByPrimaryKey(orderItem.getLast_refund_id());
                    if (r == null || r.getStatus() == OrderConstant.RefundOrderStatus.FAIL){
                        List<OrderItemDetail> orderItemDetails = orderItemDetailMapper.selectByItemId(orderItem.getId());
                        boolean refund = true;
                        for (OrderItemDetail detail : orderItemDetails) {
                            refund = refund & refundOrderManager.canBeRefund(ProductConstants.RefundEqType.SELLER, detail);
                        }
                        if (!refund){
                            continue;
                        }
                        Map<String, Object> map = new HashMap<>();
                        map.put("order_item_sn", orderItem.getNumber());
                        map.put("quantity", orderItem.getQuantity() * orderItem.getDays());
                        map.put("apply_from", ProductConstants.RefundEqType.SELLER);

                        Order order = orderMapper.selectByPrimaryKey(orderItem.getOrder_id());
                        map.put(EpConstant.EpKey.EP_ID, order.getBuy_ep_id());
                        map.put("operator_id", 0);
                        map.put("operator_name", OrderConstant.REFUND_ADAPTER);
                        applyRefund(map, orderItem.getNumber(), refundOrderInterface);
                    }
                }
            }
        }

        Result<RefundOrder> result = new Result<>(true);
        result.put(refundOrder);
        return result;
    }

    /**
     * 取消已支付未分账的订单
     * @param orderSn
     * @return
     */
    public Result<?> cancelNoSplit(String orderSn) {
        Order order = orderMapper.selectBySN(Long.valueOf(orderSn));
        if (order == null) {
            throw new ApiException("订单不存在");
        }
        if (order.getStatus() != OrderConstant.OrderStatus.PAID_HANDLING) {
            throw new ApiException("订单不在已支付处理中状态,当前状态为:" + OrderConstant.OrderStatus.getName(order.getStatus()));
        }
        // 退款,取消在退款回调中执行
        refundOrderManager.refundMoney(order, order.getPay_amount(), null, -1);
        return new Result<>(true);
    }

    /**
     * 退订审核
     * @param params
     * @param refundSn
     * @return
     * @throws Exception
     */
    @MnsEvent
    public Result<?> auditRefund(Map params, String refundSn) throws Exception {
        RefundOrder refundOrder = refundOrderMapper.selectBySN(Long.valueOf(refundSn));
        if (refundOrder == null) {
            throw new ApiException("退订订单不存在");
        }
        if (refundOrder.getStatus() != OrderConstant.RefundOrderStatus.AUDIT_WAIT) {
            throw new ApiException("退订订单不在待审核状态,当前状态为:" + OrderConstant.RefundOrderStatus.getName(refundOrder.getStatus()));
        }
        if (refundOrder.getAudit_time() != null) {
            throw new ApiException("该退订订单已审核");
        }
        refundOrder.setAudit_time(new Date());
        refundOrder.setAudit_user_id(CommonUtil.objectParseInteger(params.get("operator_id")));
        refundOrder.setAudit_user_name(CommonUtil.objectParseString(params.get("operator_name")));
        OrderItem orderItem = orderItemMapper.selectByPrimaryKey(refundOrder.getOrder_item_id());
        if (orderItem == null) {
            throw new ApiException("订单不存在");
        }
        Order order = orderMapper.selectByPrimaryKey(orderItem.getOrder_id());
        if (order == null) {
            throw new ApiException("订单不存在");
        }
        if (orderItem.getPro_type() == ProductConstants.ProductType.PACKAGE){
            //如果是套票，退款审核为打包商，也就是套票供应商
            if (CommonUtil.objectParseInteger(params.get(EpConstant.EpKey.EP_ID)) != orderItem.getSupplier_ep_id().intValue()){
                throw new ApiException("非法请求:当前企业不能审核该退订订单");
            }
        }else {
            if (!String.valueOf(params.get(EpConstant.EpKey.CORE_EP_ID)).equals(String.valueOf(orderItem.getSupplier_core_ep_id()))) {
                throw new ApiException("非法请求:当前企业不能审核该退订订单");
            }
        }
        boolean status = Boolean.parseBoolean(params.get("status").toString());
        if (status && orderItem.getPro_type() == ProductConstants.ProductType.SCENERY && orderItem.getRefund_quantity() + orderItem.getUsed_quantity() + refundOrder.getQuantity() > orderItem.getQuantity()) {
            throw new ApiException("审核失败:余票不足");
        }

        refundOrderMapper.updateByPrimaryKeySelective(refundOrder);
        eventManager.addEvent(OrderConstant.EventType.ORDER_REFUND_AUDIT, new RefundAuditEventParam(refundOrder.getId(), status));
        return new Result<>(true).putExt(Result.SYNC_DATA, new SynchronizeDataMap("Manual").add("t_refund_order", refundOrderMapper.selectByPrimaryKey(refundOrder.getId())).asMap());
    }

    /**
     * 预定审核
     * @param params
     * @param orderItemSn
     * @return
     */
    @MnsEvent
    @JobTask
    public Result<?> auditBooking(Map params, long orderItemSn) {
        OrderItem orderItem = orderItemMapper.selectBySN(orderItemSn);
        if (orderItem == null) {
            throw new ApiException("订单不存在");
        }
        if (orderItem.getStatus() != OrderConstant.OrderItemStatus.AUDIT_WAIT) {
            throw new ApiException("订单不在待审状态,当前状态为:" + OrderConstant.OrderItemStatus.getName(orderItem.getStatus()));
        }
        if (orderItem.getPro_type() == ProductConstants.ProductType.PACKAGE){
            throw new ApiException("非法请求:该订单不需要审核");
        }

        Order order = orderMapper.selectByPrimaryKey(orderItem.getOrder_id());
        if (order == null) {
            throw new ApiException("订单不存在");
        }
        if (order.getStatus() != OrderConstant.OrderStatus.AUDIT_WAIT) {
            throw new ApiException("订单不在待审状态,当前状态为:" + OrderConstant.OrderStatus.getName(order.getStatus()));
        }
        if (!params.containsKey(EpConstant.EpKey.EP_ID)) {
            throw new ApiException("非法请求:企业ID为空");
        }
        if (!String.valueOf(params.get(EpConstant.EpKey.EP_ID)).equals(String.valueOf(orderItem.getSupplier_ep_id()))) {
            throw new ApiException("非法请求:当前企业不能审核该订单");
        }
        if (orderItem.getAudit_time() != null) {
            throw new ApiException("该订单已审核");
        }

        boolean status = Boolean.parseBoolean(params.get("status").toString());
        orderItem.setAudit_user_id(CommonUtil.objectParseInteger(params.get("operator_id")));
        orderItem.setAudit_user_name(CommonUtil.objectParseString(params.get("operator_name")));
        orderItem.setAudit_time(new Date());
        orderItem.setAudit(status);
        if (status) {
            orderItem.setStatus(OrderConstant.OrderItemStatus.AUDIT_SUCCESS);
            boolean allAudit = bookingOrderManager.isOrderAllAudit(orderItem.getOrder_id(), orderItem.getId());
            if (allAudit) {
                order.setStatus(OrderConstant.OrderStatus.PAY_WAIT);
                order.setAudit_time(new Date());
                // 判断是否需要支付
                if (order.getPay_amount() <= 0) { // 不需要支付
                    bookingOrderManager.addPaymentCallback(order);
                }
                orderMapper.updateByPrimaryKeySelective(order);
            }
        }
        orderItemMapper.updateByPrimaryKeySelective(orderItem);
        eventManager.addEvent(OrderConstant.EventType.ORDER_AUDIT, new OrderAuditEventParam(orderItem.getId(), status));

        // 同步返回数据
        return new Result<>(true).putExt(Result.SYNC_DATA, new SynchronizeDataMap("Manual").add("t_order_item", orderItemMapper.selectByOrderId(order.getId())).asMap());
    }

    /**
     * 支付
     * @param params
     * @param sn
     * @param payType
     * @return
     */
    public Result<?> payment(Map params, Long sn, Integer payType) {
        Order order = orderMapper.selectBySN(sn);
        if (order == null) {
            throw new ApiException("订单不存在");
        }
        if (!params.containsKey(EpConstant.EpKey.EP_ID)) {
            throw new ApiException("非法请求:企业ID为空");
        }
        if (!String.valueOf(params.get(EpConstant.EpKey.EP_ID)).equals(String.valueOf(order.getBuy_ep_id()))) {
            throw new ApiException("非法请求:当前企业不能支付该订单");
        }
        if (order.getPay_amount() <= 0) {
            return new Result<>(true);
        }

        if (order.getStatus() != OrderConstant.OrderStatus.PAY_WAIT &&
                order.getStatus() != OrderConstant.OrderStatus.PAY_FAIL) {
            if (order.getStatus() == OrderConstant.OrderStatus.PAYING && order.getPayment_type().intValue() != PaymentConstant.PaymentType.BALANCE) {
                // 支付中的订单 主动查询第三方机构获取支付状态, 支付成功则更新订单,支付中则不允许再支付,其它则可以继续支付
                Result<Map<String, Object>> result = thirdPayService.getPaidStatus(order.getNumber(), order.getPayee_ep_id(), order.getPayment_type(), order.getThird_serial_no());
                if (result == null || !result.isSuccess()) {
                    log.warn("获取订单:{}支付状态异常", order.getNumber(), result == null ? null : result.getError());
                    throw new ApiException("服务器繁忙");
                }
                Map<String, Object> map = result.get();
                PaymentConstant.ThirdPayStatus payStatus = (PaymentConstant.ThirdPayStatus) map.get("code");
                switch (payStatus) {
                    case SUCCESS:
                        paymentCallback(order.getId(), CommonUtil.objectParseString(map.get("transaction_id")), String.valueOf(order.getNumber()));
                        return new Result<>(true, "该订单已支付成功");
                    case USERPAYING:
                        throw new ApiException("订单正在支付中");
                }
            } else {
                throw new ApiException("订单不在待支付状态,当前状态为:" + OrderConstant.OrderStatus.getName(order.getStatus()));
            }
        }

        order.setStatus(OrderConstant.OrderStatus.PAYING);
        order.setLocal_payment_serial_no(String.valueOf(UUIDGenerator.generateUUID()));
        order.setPayment_type(payType);
        order.setPay_time(new Date());
        order.setPayment_ep_id(CommonUtil.objectParseInteger(params.get(EpConstant.EpKey.EP_ID)));
        order.setPayment_operator_id(CommonUtil.objectParseInteger(params.get("operator_id")));
        order.setPayment_operator_name(CommonUtil.objectParseString(params.get("operator_name")));
        // 获取企业名称
        String epName = null;
        try {
            Result<Map<String, Object>> epResult = epService.selectId(order.getPayment_ep_id());
            if (epResult != null && epResult.isSuccess() && epResult.get() != null) {
                epName = String.valueOf(epResult.get().get("name"));
            }
        } catch (Exception e) {
            log.warn("get payment ep name error", e);
        }
        order.setPayment_ep_name(epName);
        orderMapper.updateByPrimaryKeySelective(order);

        log.info(OrderConstant.LogOperateCode.NAME, bookingOrderManager.orderLog(order.getId(), null,
                params.get(EpConstant.EpKey.EP_ID),  params.get("operator_name"),
                OrderConstant.LogOperateCode.PAID,
                0, String.format("订单支付:参数:%s", JsonUtils.toJson(params)), order.getLocal_payment_serial_no()));

        // 调用支付RPC
        // 余额支付
        if (payType == PaymentConstant.PaymentType.BALANCE.intValue()) {
            // 余额支付需要扣除销售商的钱
            List<OrderItemAccount> accounts = orderItemAccountMapper.selectByOrderAnEp(order.getId(), order.getBuy_ep_id(), order.getPayee_ep_id());
            for (OrderItemAccount account : accounts) {
                if (account.getSale_ep_id().intValue() == account.getEp_id().intValue()){
                    //自己产品
                    account.setMoney(0);
                    account.setProfit(0);
                }else {
                    // 把data JSON 反编译为JAVA类型
                    Collection<AccountDataDto> dataDtoList = AccountUtil.decompileAccountData(account.getData());
                    // 获取总出货价
                    int totalOutPrice = AccountUtil.getTotalOutPrice(dataDtoList);
                    OrderItem orderItem = orderItemMapper.selectByPrimaryKey(account.getOrder_item_id());
                    account.setMoney(account.getMoney() + (-totalOutPrice * orderItem.getQuantity()));
                    account.setProfit(account.getMoney());
                }
                orderItemAccountMapper.updateByPrimaryKeySelective(account);
            }

            // 获取余额变动信息
            List<BalanceChangeInfo> balanceChangeInfoList = bookingOrderManager.packagingPaySplitAccount(order);

            // 支付
            Result result = bookingOrderManager.changeBalances(
                    PaymentConstant.BalanceChangeType.BALANCE_PAY,
                    order.getNumber().toString(), balanceChangeInfoList);
            if (!result.isSuccess() && (result.getCode() == null || result.getCode().intValue() != Result.UNIQUE_KEY_ERROR)) {
                log.warn("余额支付失败:{}", JsonUtils.toJson(result));
                throw new ApiException(result.getError());
            }
            return new Result<>(true);
        }
        // 第三方支付
        // 获取商品名称
        List<String> names = orderItemMapper.getProductNamesByOrderId(order.getId());
        List<Long> ids = orderItemMapper.getProductIdsByOrderId(order.getId());
        Map<String, Object> payParams = new HashMap<>();
        payParams.put("prodName", StringUtils.join(names, ","));
        payParams.put("totalFee", order.getPay_amount());
        payParams.put("serialNum", order.getNumber().toString());
        payParams.put("prodId", StringUtils.join(ids, ","));
        payParams.put("return_url", params.get("return_url"));
        payParams.put("openid", params.get("openid"));

        boolean isWap = BooleanUtils.toBoolean(CommonUtil.objectParseString(params.get("wap")));
        if (isWap) {
            Result result = thirdPayService.wapPay(order.getNumber(), order.getPayee_ep_id(), payType, payParams);
            if (!result.isSuccess()) {
                log.warn("第三方WAP支付异常:{}", JsonUtils.toJson(result));
                throw new ApiException(result.getError());
            }
            return result;
        } else {
            Result result = thirdPayService.reqPay(order.getNumber(), order.getPayee_ep_id(), payType, payParams);
            if (!result.isSuccess()) {
                log.warn("第三方支付异常:{}", JsonUtils.toJson(result));
                throw new ApiException(result.getError());
            }
            return result;
        }
    }

    /**
     * 退票
     * @param info
     * @param procTime
     * @param orderItem
     * @param refundOrder
     * @return
     */
    @MnsEvent
    public Result refundTicket(RefundTicketInfo info, Date procTime, OrderItem orderItem, RefundOrder refundOrder) {
        RefundSerial refundSerial = refundSerialMapper.selectByLocalSn(Long.valueOf(info.getRefId()));
        if (refundSerial == null) {
            return new Result(false, String.format("退订订单:%s,退票流水:%s 错误", refundOrder.getNumber(), info.getRefId()));
        }

        if (refundSerial.getRefund_time() != null) {
            return new Result(false, String.format("退订订单:%s,退票流水:%s 重复操作", refundOrder.getNumber(), info.getRefId()));
        }

        if (orderItem.getQuantity() < orderItem.getUsed_quantity() + orderItem.getRefund_quantity() + refundSerial.getQuantity() && info.isSuccess()) {
            log.error("退订订单:{},退票流水:{} 退票回调异常: 退票人:{} 可退票不足, 小秘书重置为退票失败", new Object[]{refundOrder.getNumber(), info.getRefId(), info.getVisitorSeqId()});
            info.setSuccess(false);
        }
        log.info(OrderConstant.LogOperateCode.NAME, bookingOrderManager.orderLog(null, orderItem.getId(),
                0,  "VOUCHER",
                OrderConstant.LogOperateCode.RECEIVE_REFUND_TICKETING,
                refundOrder.getQuantity(), String.format("退票返回:退票信息:%s", JsonUtils.toJson(info)), info.getRefId()));
        eventManager.addEvent(OrderConstant.EventType.REFUND_TICKET, new RefundTicketEventParam(refundOrder.getId(), info.isSuccess()));
        // 退票失败
        if (!info.isSuccess()) {
            return refundFail(orderItem, refundOrder);
        }

        refundOrder.setRefund_ticket_time(procTime);

        //refundSerial.setRemoteSerialNo(refundSn);
        refundSerial.setRefund_time(procTime);

        // 获取核销人信息
        Visitor visitor = visitorMapper.selectByPrimaryKey(info.getVisitorSeqId());
        visitor.setReturn_quantity(visitor.getReturn_quantity() + refundSerial.getQuantity());
        int ret = visitorMapper.refundQuantity(visitor.getId(), refundSerial.getQuantity());
        if (ret < 1) {
            log.warn("退订订单:{},退票流水:{} 退票回调异常: 退票人:{} 可退票不足 退票信息:{}", new Object[]{refundOrder.getNumber(), info.getRefId(), info.getVisitorSeqId(), JsonUtils.toJson(info)});
            throw new ApiException("没有可退的票");
        }

        RefundVisitor refundVisitor = refundVisitorMapper.selectByRefundIdAndVisitorId(refundOrder.getId(), info.getVisitorSeqId());
        refundVisitor.setReturn_quantity(refundVisitor.getReturn_quantity() + refundSerial.getQuantity());
        refundVisitor.setPre_quantity(0);
        refundVisitorMapper.updateByPrimaryKeySelective(refundVisitor);
        orderItem.setRefund_quantity(orderItem.getRefund_quantity() + refundSerial.getQuantity());
        ret = orderItemMapper.refundQuantity(orderItem.getId(), refundSerial.getQuantity());
        if (ret < 1) {
            log.warn("退订订单:{},退票流水:{} 退票回调异常: 退票人:{} 可退票不足 退票信息:{}", new Object[]{refundOrder.getNumber(), info.getRefId(), info.getVisitorSeqId(), JsonUtils.toJson(info)});
            throw new ApiException("没有可退的票");
        }
        refundSerialMapper.updateByPrimaryKeySelective(refundSerial);
        return new Result(true);
    }

    /**
     * 退票(团队)
     * @param info
     * @param procTime
     * @param orderItem
     * @param refundOrder
     * @return
     */
    @MnsEvent
    public Result refundGroupTicket(RefundGroupTicketInfo info, Date procTime, OrderItem orderItem, RefundOrder refundOrder) {
        RefundSerial refundSerial = refundSerialMapper.selectByLocalSn(Long.valueOf(info.getRefId()));
        if (refundSerial == null) {
            return new Result(false, String.format("退订订单:%s,退票流水:%s 错误", refundOrder.getNumber(), info.getRefId()));
        }

        if (refundSerial.getRefund_time() != null) {
            return new Result(false, String.format("退订订单:%s,退票流水:%s 重复操作", refundOrder.getNumber(), info.getRefId()));
        }
        eventManager.addEvent(OrderConstant.EventType.REFUND_TICKET, new RefundTicketEventParam(refundOrder.getId(), info.isSuccess()));
        // 退票失败
        if (!info.isSuccess()) {
            return refundFail(orderItem, refundOrder);
        }

        refundOrder.setRefund_ticket_time(procTime);

        //refundSerial.setRemoteSerialNo(refundSn);
        refundSerial.setRefund_time(procTime);

        orderItem.setRefund_quantity(orderItem.getRefund_quantity() + refundSerial.getQuantity());
        int ret = orderItemMapper.refundQuantity(orderItem.getId(), refundSerial.getQuantity());
        if (ret < 1) {
            log.warn("退订订单:{},退票流水:{} 退票回调异常: 可退票不足 退票信息:{}", new Object[]{refundOrder.getNumber(), info.getRefId(), JsonUtils.toJson(info)});
            throw new ApiException("没有可退的票");
        }
        refundSerialMapper.updateByPrimaryKeySelective(refundSerial);
        return new Result(true);
    }

    @MnsEvent
    public Result consumeHotelTicket(Map params, long orderItemSn) {
        OrderItem orderItem = orderItemMapper.selectBySN(orderItemSn);
        if (orderItem == null) {
            throw new ApiException("订单不存在");
        }
        if (orderItem.getStatus() != OrderConstant.OrderItemStatus.SEND) {
            throw new ApiException("订单不在可核销状态,当前状态为:" + OrderConstant.OrderItemStatus.getName(orderItem.getStatus()));
        }
        if (!params.containsKey(EpConstant.EpKey.EP_ID)) {
            throw new ApiException("非法请求:企业ID为空");
        }
        if (!String.valueOf(params.get(EpConstant.EpKey.EP_ID)).equals(String.valueOf(orderItem.getSupplier_ep_id()))) {
            throw new ApiException("非法请求:当前企业不能核销该订单");
        }

        Order order = orderMapper.selectByPrimaryKey(orderItem.getOrder_id());
        Assert.notNull(order, "订单不存在");

        Collection<ConsumeDay> consumeDays = AccountUtil.decompileConsumeDay((List) params.get("days"));
        if (consumeDays == null || consumeDays.isEmpty()) {
            throw new ApiException("核销详情为空");
        }

        int count = orderClearanceSerialMapper.selectItemQuantityCount(orderItem.getId());
        if (count > 0) {
            throw new ApiException("该订单已核销");
        }

        List<RefundOrder> refundOrders = refundOrderMapper.selectByItemId(orderItem.getId());
        if (refundOrders != null && refundOrders.size() > 0) {
            throw new ApiException("该订单已退订");
        }

        // 核销
        Date clearanceTime = new Date();
        List<OrderItemDetail> details = orderItemDetailMapper.selectByItemId(orderItem.getId());
        if (details.get(0).getDay().after(clearanceTime)) {
            throw new ApiException("请入住后核销");
        }

        for (OrderItemDetail detail : details) {
            boolean consume = false;
            for (ConsumeDay consumeDay : consumeDays) {
                if (consumeDay.getDay().equals(detail.getDay())) {
                    consume = true;
                    break;
                }
            }
            if (!consume && !AccountUtil.canRefund(detail.getCust_refund_rule())) {
                throw new ApiException("该订单需要全部核销");
            }
        }
        int total = 0;
        for (ConsumeDay consumeDay : consumeDays) {
            OrderItemDetail detail = AccountUtil.getDetailByDay(details, consumeDay.getDay());
            Assert.notNull(detail, String.format("日期: %s 没有订单详情", DateFormatUtils.parseDateToDatetimeString(consumeDay.getDay())));
            orderItemDetailMapper.useQuantity(detail.getId(), consumeDay.getQuantity());
            detail.setUsed_quantity(consumeDay.getQuantity());
            // 保存核销流水
            OrderClearanceSerial serial = bookingOrderManager.saveClearanceSerial(orderItem, order.getPayee_ep_id(), consumeDay.getDay(), consumeDay.getQuantity(), String.valueOf(UUIDGenerator.generateUUID()), clearanceTime);
            total += consumeDay.getQuantity();
            eventManager.addEvent(OrderConstant.EventType.CONSUME_TICKET, new ConsumeTicketEventParam(orderItem.getId(), serial.getId()));
        }
        // 团队最低使用数验证
        if (orderItem.getGroup_id() != null && orderItem.getGroup_id() != 0 &&
                orderItem.getPro_sub_ticket_type() != null && orderItem.getPro_sub_ticket_type() == ProductConstants.TeamTicketType.TEAM) {
            if (orderItem.getLow_quantity() != null && orderItem.getLow_quantity() > total) {
                throw new ApiException("不得小于最低使用数");
            }
        }

        // 修改已使用数量
        int ret = orderItemMapper.useQuantity(orderItem.getId(), total);
        if (ret <= 0) {
            log.warn("酒店核销订单:{} 核销票不足", orderItemSn);
            throw new ApiException("没有可核销的房间");
        }
        if (total < orderItem.getQuantity() * orderItem.getDays()) {
            // 退票
            Collection<RefundDay> refundDays = AccountUtil.parseRefundDayForDetail(details);
            if (refundDays == null || refundDays.isEmpty()) {
                return new Result(true);
            }
            int[] calcResult = refundOrderManager.calcRefundMoneyAndFee(orderItem, order, ProductConstants.RefundEqType.SELLER, refundDays, details, clearanceTime);
            // 计算退款金额
            int money = calcResult[0];
            // 手续费
            int fee = calcResult[1];

            // 获取退订审核配置
            int[] auditSupplierConfig = refundOrderManager.getAuditConfig(order, orderItem);
            int auditTicket = auditSupplierConfig[0];
            int auditMoney = auditSupplierConfig[1];
            // 总退票数
            int totalRefund = AccountUtil.getRefundQuantity(refundDays);
            if (totalRefund + total != orderItem.getQuantity() * orderItem.getDays()) {
                throw new ApiException("核销异常,请联系管理员");
            }
            // 创建退订订单
            RefundOrder refundOrder = refundOrderManager.generateRefundOrder(orderItem, refundDays, totalRefund, money, fee,
                    "核销退订", auditTicket, auditMoney, order.getPayee_ep_id(),
                    CommonUtil.objectParseInteger(params.get("operator_id")), CommonUtil.objectParseString(params.get("operator_name")), null);

            // 更新退订张数
            orderItemDetailMapper.refundRemain(orderItem.getId());

            // 退订分账 到付退订不分帐
            if (orderItem.getPayment_flag() != ProductConstants.PayType.PAYS) {
                refundOrderManager.preRefundSplitAccount(ProductConstants.RefundEqType.SELLER, refundOrder.getId(), order, clearanceTime, refundOrder.getOrder_item_id(), details, refundDays);
            }

            // 触发事件
            eventManager.addEvent(OrderConstant.EventType.ORDER_REFUND_APPLY, refundOrder.getId());
        }
        log.info(OrderConstant.LogOperateCode.NAME, bookingOrderManager.orderLog(null, orderItem.getId(),
                params.get(EpConstant.EpKey.EP_ID),  params.get("operator_name"),
                OrderConstant.LogOperateCode.TICKET_CONSUME_SUCCESS,
                total, String.format("酒店核销:信息:%s", JsonUtils.toJson(params)), null));
        return new Result(true);
    }

    @MnsEvent
    public Result consumeLineTicket(Map params, long orderItemSn) {
        OrderItem orderItem = orderItemMapper.selectBySN(orderItemSn);
        if (orderItem == null) {
            throw new ApiException("订单不存在");
        }
        if (orderItem.getStatus() != OrderConstant.OrderItemStatus.SEND) {
            throw new ApiException("订单不在可核销状态,当前状态为:" + OrderConstant.OrderItemStatus.getName(orderItem.getStatus()));
        }
        if (!params.containsKey(EpConstant.EpKey.EP_ID)) {
            throw new ApiException("非法请求:企业ID为空");
        }
        if (!String.valueOf(params.get(EpConstant.EpKey.EP_ID)).equals(String.valueOf(orderItem.getSupplier_ep_id()))) {
            throw new ApiException("非法请求:当前企业不能核销该订单");
        }

        Order order = orderMapper.selectByPrimaryKey(orderItem.getOrder_id());
        Assert.notNull(order, "订单不存在");

        int count = orderClearanceSerialMapper.selectItemQuantityCount(orderItem.getId());
        if (count > 0) {
            throw new ApiException("该订单已核销");
        }

        // 核销
        Date clearanceTime = new Date();
        List<OrderItemDetail> details = orderItemDetailMapper.selectByItemId(orderItem.getId());
        int total = 0;
        for (OrderItemDetail detail : details) {
            orderItemDetailMapper.useRemain(detail.getId());
            detail.setUsed_quantity(detail.getQuantity() - detail.getRefund_quantity());
            total += detail.getUsed_quantity();
            // 保存核销流水
            OrderClearanceSerial serial = bookingOrderManager.saveClearanceSerial(orderItem, order.getPayee_ep_id(), detail.getDay(), detail.getUsed_quantity(), String.valueOf(UUIDGenerator.generateUUID()), clearanceTime);
            eventManager.addEvent(OrderConstant.EventType.CONSUME_TICKET, new ConsumeTicketEventParam(orderItem.getId(), serial.getId()));
        }

        // 修改已使用数量
        int ret = orderItemMapper.useQuantity(orderItem.getId(), total);
        if (ret <= 0) {
            log.warn("线路核销订单:{} 核销票不足", orderItemSn);
            throw new ApiException("没有可核销的票");
        }

        log.info(OrderConstant.LogOperateCode.NAME, bookingOrderManager.orderLog(null, orderItem.getId(),
                params.get(EpConstant.EpKey.EP_ID),  params.get("operator_name"),
                OrderConstant.LogOperateCode.TICKET_CONSUME_SUCCESS,
                total, String.format("线路核销:信息:%s", JsonUtils.toJson(params)), null));
        return new Result(true);
    }

    public Result modifyTicketForGroup(Map params, long orderItemSn) {
        OrderItem orderItem = orderItemMapper.selectBySN(orderItemSn);
        if (orderItem == null) {
            return new Result(false, "订单不存在");
        }
        if (!(orderItem.getGroup_id() != null && orderItem.getGroup_id() != 0 &&
                orderItem.getPro_sub_ticket_type() != null && orderItem.getPro_sub_ticket_type() == ProductConstants.TeamTicketType.TEAM)) {
            return new Result(false, "该订单不是团队订单");
        }
        // 订单状态不在:已出票/已修改/修改失败 或者 已用大于0 则不可以修改
        if ((orderItem.getStatus() != OrderConstant.OrderItemStatus.SEND &&
                orderItem.getStatus() != OrderConstant.OrderItemStatus.MODIFY &&
                orderItem.getStatus() != OrderConstant.OrderItemStatus.MODIFY_FAIL) ||
                (orderItem.getUsed_quantity() != null && orderItem.getUsed_quantity() > 0)) {
            return new Result(false, "该订单不在可修改状态,当前状态为:" + OrderConstant.OrderItemStatus.getName(orderItem.getStatus()));
        }

        orderItem.setStatus(OrderConstant.OrderItemStatus.MODIFYING);
        orderItemMapper.updateByPrimaryKeySelective(orderItem);
        ModifyGroupTicketParams ticketParams = new ModifyGroupTicketParams();
        ticketParams.setOrderSn(orderItem.getNumber());
        Integer guideId = CommonUtil.objectParseInteger(params.get("guide_id"));
        Date modifyDate = new Date();
        if (guideId != null) {
            Shipping shipping = shippingMapper.selectByPrimaryKey(guideId);
            if (shipping == null) {
                throw new ApiException("导游信息不存在");
            }
            String guideName = CommonUtil.objectParseString(params.get("guide_name"));
            String guidePhone = CommonUtil.objectParseString(params.get("guide_phone"));
            String guideSid = CommonUtil.objectParseString(params.get("guide_sid"));
            guideName = StringUtils.isEmpty(guideName) ? shipping.getName() : guideName;
            guidePhone = StringUtils.isEmpty(guidePhone) ? shipping.getPhone() : guidePhone;
            guideSid = StringUtils.isEmpty(guideSid) ? shipping.getSid() : guideSid;
            ticketParams.setGuideName(guideName);
            ticketParams.setGuidePhone(guidePhone);
            ticketParams.setGuideSid(guideSid);
            ShippingModify shippingModify = new ShippingModify();
            shippingModify.setShipping_id(guideId);
            shippingModify.setOrder_id(orderItem.getOrder_id());
            shippingModify.setCreate_time(modifyDate);
            shippingModify.setStatus(false);
            shippingModify.setShipping_name(guideName);
            shippingModify.setShipping_phone(guidePhone);
            shippingModify.setShipping_sid(guideSid);
            shippingModifyMapper.insertSelective(shippingModify);
        }
        ticketParams.setVisitors(modifyGroupVisitor((List) params.get("visitors"), orderItem.getId()));

        log.info(OrderConstant.LogOperateCode.NAME, bookingOrderManager.orderLog(null, orderItem.getId(),
                params.get(EpConstant.EpKey.EP_ID),  params.get("operator_name"),
                OrderConstant.LogOperateCode.MODIFY_TICKET_SEND,
                0, String.format("团队修改:信息:%s", JsonUtils.toJson(params)), null));
        return voucherRPCService.modifyGroupTicket(orderItem.getEp_ma_id(), ticketParams);
    }

    private List<com.all580.voucher.api.model.Visitor> modifyGroupVisitor(List visitors, int itemId) {
        List<com.all580.voucher.api.model.Visitor> vs = new ArrayList<>();
        if (visitors != null && visitors.size() > 0) {
            List<Integer> vIds = new ArrayList<>();
            for (Object o : visitors) {
                Map visitor = (Map) o;
                Integer id = CommonUtil.objectParseInteger(visitor.get("id"));
                if (id != null) {
                    vIds.add(id);
                }
            }
            if (!vIds.isEmpty()) {
                Set<String> sids = new HashSet<>();
                Date modifyDate = new Date();
                List<Visitor> visitorList = visitorMapper.selectByIds(vIds);
                for (Object o : visitors) {
                    Map vMap = (Map) o;
                    Integer id = CommonUtil.objectParseInteger(vMap.get("id"));
                    if (id != null) {
                        String name = CommonUtil.objectParseString(vMap.get("name"));
                        String phone = CommonUtil.objectParseString(vMap.get("phone"));
                        String sid = CommonUtil.objectParseString(vMap.get("sid"));
                        Integer type = CommonUtil.objectParseInteger(vMap.get("card_type"));
                        if (type != null && type == OrderConstant.CardType.ID) {
                            Assert.isTrue(CheckIdCardUtils.validateCard(sid), "请输入15或18位有效身份证号码");
                        }
                        boolean have = false;
                        for (Visitor visitor : visitorList) {
                            if (id.intValue() == visitor.getId()){
                                name = StringUtils.isEmpty(name) ? visitor.getName() : name;
                                phone = StringUtils.isEmpty(phone) ? visitor.getPhone() : phone;
                                sid = StringUtils.isEmpty(sid) ? visitor.getSid() : sid;
                                type = type == null ? visitor.getCard_type() : type;
                                String newValue = String.format("%s-%s-%s", StringUtils.isEmpty(phone) ? "" : phone, StringUtils.isEmpty(sid) ? "" : sid, type == null ? "" : String.valueOf(type));
                                String oldValue = String.format("%s-%s-%s", StringUtils.isEmpty(visitor.getPhone()) ? "" : visitor.getPhone(), StringUtils.isEmpty(visitor.getSid()) ? "" : visitor.getSid(), visitor.getCard_type() == null ? "" : String.valueOf(visitor.getCard_type()));
                                if (newValue.equals(oldValue)) {
                                    throw new ApiException("游客ID:" + id + "修改值中有重复的数据");
                                }
                                have = true;
                            }
                        }
                        if (!have) {
                            throw new ApiException("游客ID:" + id + "数据异常");
                        }
                        String key = String.format("%s-%d", sid, type == null ? -1 : type);
                        if (sids.contains(key)) {
                            throw new ApiException("游客ID:" + id + "修改值中的身份证重复");
                        }
                        sids.add(key);
                        com.all580.voucher.api.model.Visitor v = new com.all580.voucher.api.model.Visitor();
                        v.setId(id);
                        v.setName(name);
                        v.setPhone(phone);
                        v.setSid(sid);
                        if (type != null) {
                            v.setIdType(type);
                        }
                        vs.add(v);
                        VisitorModify visitorModify = new VisitorModify();
                        visitorModify.setItem_id(itemId);
                        visitorModify.setCreate_time(modifyDate);
                        visitorModify.setStatus(false);
                        visitorModify.setVisitor_card_type(type);
                        visitorModify.setVisitor_id(id);
                        visitorModify.setVisitor_name(name);
                        visitorModify.setVisitor_phone(phone);
                        visitorModify.setVisitor_sid(sid);
                        visitorModifyMapper.insertSelective(visitorModify);
                    }
                }
            }
        }
        return vs;
    }

    private void addRefundMoneyJob(Long ordCode, String serialNum) {
        log.info("退款失败 加入任务处理...");
        // 退款失败回调 记录任务
        Map<String, String> jobParams = new HashMap<>();
        jobParams.put("ordCode", String.valueOf(ordCode));
        jobParams.put("serialNum", serialNum);
        jobManager.addJob(Collections.singleton(bookingOrderManager.createJob(OrderConstant.Actions.REFUND_MONEY, jobParams, true)));
    }

    private Result refundFail(OrderItem orderItem, RefundOrder refundOrder) {
        try {
            refundOrderManager.refundFail(orderItem, refundOrder);
        } catch (Exception e) {
            throw new ApiException("退票失败还原状态异常", e);
        }
        return new Result(true);
    }
}
