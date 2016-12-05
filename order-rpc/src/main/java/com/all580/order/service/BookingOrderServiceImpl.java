package com.all580.order.service;

import com.all580.ep.api.conf.EpConstant;
import com.all580.ep.api.service.EpService;
import com.all580.order.api.OrderConstant;
import com.all580.order.api.service.BookingOrderService;
import com.all580.order.dao.*;
import com.all580.order.dto.LockStockDto;
import com.all580.order.entity.*;
import com.all580.order.manager.BookingOrderManager;
import com.all580.order.manager.RefundOrderManager;
import com.all580.order.manager.SmsManager;
import com.all580.payment.api.conf.PaymentConstant;
import com.all580.payment.api.model.BalanceChangeInfo;
import com.all580.payment.api.service.ThirdPayService;
import com.all580.product.api.model.EpSalesInfo;
import com.all580.product.api.model.ProductSalesDayInfo;
import com.all580.product.api.model.ProductSalesInfo;
import com.all580.product.api.model.ProductSearchParams;
import com.all580.product.api.service.ProductSalesPlanRPCService;
import com.all580.voucher.api.model.ReSendTicketParams;
import com.all580.voucher.api.service.VoucherRPCService;
import com.framework.common.Result;
import com.framework.common.distributed.lock.DistributedLockTemplate;
import com.framework.common.distributed.lock.DistributedReentrantLock;
import com.framework.common.lang.DateFormatUtils;
import com.framework.common.lang.JsonUtils;
import com.framework.common.lang.UUIDGenerator;
import com.framework.common.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.lang.exception.ApiException;
import java.util.*;

/**
 * @author zhouxianjun(Alone)
 * @ClassName:
 * @Description: 订单服务实现
 * @date 2016/9/28 9:23
 */
@Service
@Slf4j
public class BookingOrderServiceImpl implements BookingOrderService {
    @Autowired
    private BookingOrderManager bookingOrderManager;
    @Autowired
    private RefundOrderManager refundOrderManager;
    @Autowired
    private SmsManager smsManager;

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private OrderItemDetailMapper orderItemDetailMapper;
    @Autowired
    private MaSendResponseMapper maSendResponseMapper;
    @Autowired
    private RefundOrderMapper refundOrderMapper;
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private GuideMapper guideMapper;
    @Autowired
    private ShippingMapper shippingMapper;

    @Autowired
    private ProductSalesPlanRPCService productSalesPlanRPCService;
    @Autowired
    private EpService epService;
    @Autowired
    private ThirdPayService thirdPayService;
    @Autowired
    private VoucherRPCService voucherRPCService;
    @Autowired
    private DistributedLockTemplate distributedLockTemplate;
    @Value("${lock.timeout}")
    private int lockTimeOut = 3;

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Result<?> create(Map params) throws Exception {
        Integer buyEpId = CommonUtil.objectParseInteger(params.get(EpConstant.EpKey.EP_ID));
        // 获取平台商ID
        Integer coreEpId = CommonUtil.objectParseInteger(params.get(EpConstant.EpKey.CORE_EP_ID));
        Integer from = CommonUtil.objectParseInteger(params.get("from"));
        String remark = CommonUtil.objectParseString(params.get("remark"));
        int totalPrice = 0;
        int totalPayPrice = 0;
        int totalPayShopPrice = 0;

        // 判断销售商状态是否为已冻结
        if (!bookingOrderManager.isEpStatus(epService.getEpStatus(buyEpId), EpConstant.EpStatus.ACTIVE)) {
            throw new ApiException("销售商企业已冻结");
        }
        // 只有销售商可以下单
        Result<Integer> epType = epService.selectEpType(buyEpId);
        if (!bookingOrderManager.isEpType(epType, EpConstant.EpType.SELLER) &&
                !bookingOrderManager.isEpType(epType, EpConstant.EpType.OTA)) {
            throw new ApiException("该企业不能购买产品");
        }
        // 锁定库存集合(统一锁定)
        Map<Integer, LockStockDto> lockStockDtoMap = new HashMap<>();
        List<ProductSearchParams> lockParams = new ArrayList<>();

        // 获取下单企业名称
        String buyEpName = null;
        Result<Map<String, Object>> epResult = epService.selectId(buyEpId);
        if (epResult != null && epResult.isSuccess() && epResult.get() != null) {
            buyEpName = String.valueOf(epResult.get().get("name"));
        }

        // 创建订单
        Order order = bookingOrderManager.generateOrder(coreEpId, buyEpId, buyEpName,
                CommonUtil.objectParseInteger(params.get("operator_id")),
                CommonUtil.objectParseString(params.get("operator_name")), from, remark);

        // 存储游客信息
        Map<Integer, List<Visitor>> visitorMap = new HashMap<>();
        // 获取子订单
        List<Map> items = (List<Map>) params.get("items");
        for (Map item : items) {
            // TODO: 2016/11/2 这里应该是productSubCode
            Integer productSubId = CommonUtil.objectParseInteger(item.get("product_sub_id"));
            Integer quantity = CommonUtil.objectParseInteger(item.get("quantity"));
            Integer days = CommonUtil.objectParseInteger(item.get("days"));
            int visitorQuantity = 0; // 游客总票数
            //预定日期
            Date bookingDate = DateFormatUtils.parseString(DateFormatUtils.DATE_TIME_FORMAT, CommonUtil.objectParseString(item.get("start")));

            // 验证是否可售
            ProductSearchParams searchParams = new ProductSearchParams();
            searchParams.setSubProductId(productSubId);
            searchParams.setStartDate(bookingDate);
            searchParams.setDays(days);
            searchParams.setQuantity(quantity);
            searchParams.setBuyEpId(buyEpId);
            Result<ProductSalesInfo> salesInfoResult = productSalesPlanRPCService.validateProductSalesInfo(searchParams);
            if (!salesInfoResult.isSuccess()) {
                throw new ApiException(salesInfoResult.getError());
            }
            ProductSalesInfo salesInfo = salesInfoResult.get();
            // 判断供应商状态是否为已冻结
            if (!bookingOrderManager.isEpStatus(epService.getEpStatus(salesInfo.getEp_id()), EpConstant.EpStatus.ACTIVE)) {
                throw new ApiException("供应商企业已冻结");
            }

            List<ProductSalesDayInfo> dayInfoList = salesInfo.getDay_info_list();

            if (dayInfoList.size() != days) {
                throw new ApiException("预定天数与获取产品天数不匹配");
            }
            // 验证预定时间限制
            bookingOrderManager.validateBookingDate(bookingDate, dayInfoList);

            // 判断游客信息
            List<Map> visitors = (List<Map>) item.get("visitor");
            if (salesInfo.isRequire_sid()) {
                Result visitorResult = bookingOrderManager.validateVisitor(
                        visitors, salesInfo.getProduct_sub_code(), bookingDate, salesInfo.getSid_day_count(), salesInfo.getSid_day_quantity());
                if (!visitorResult.isSuccess()) {
                    throw new ApiException(visitorResult.getError());
                }
            }

            // 每天的价格
            List<List<EpSalesInfo>> allDaysSales = salesInfo.getSales();

            // 子订单总进货价
            int[] priceArray = bookingOrderManager.calcSalesPrice(allDaysSales, buyEpId, quantity, salesInfo.getPay_type(), from);
            totalPrice += priceArray[0];
            totalPayPrice += priceArray[1];
            totalPayShopPrice += priceArray[2];

            // 创建子订单
            OrderItem orderItem = bookingOrderManager.generateItem(salesInfo, dayInfoList.get(dayInfoList.size() - 1).getEnd_time(), priceArray[0], bookingDate, days, order.getId(), quantity, productSubId);

            List<OrderItemDetail> detailList = new ArrayList<>();
            List<Visitor> visitorList = new ArrayList<>();
            // 创建子订单详情
            int i = 0;
            for (ProductSalesDayInfo dayInfo : dayInfoList) {
                OrderItemDetail orderItemDetail = bookingOrderManager.generateDetail(dayInfo, orderItem.getId(), DateUtils.addDays(bookingDate, i), quantity);
                detailList.add(orderItemDetail);
                // 创建游客信息
                for (Map v : visitors) {
                    Visitor visitor = bookingOrderManager.generateVisitor(v, orderItemDetail.getId());
                    visitorQuantity += visitor.getQuantity();
                    visitorList.add(visitor);
                }
                i++;
            }

            // 判断总张数是否匹配
            if (visitorQuantity != quantity) {
                throw new ApiException("游客票数与总票数不符");
            }
            // 判断最高票数 散客
            if (salesInfo.getMax_buy_quantity() != null && visitorQuantity > salesInfo.getMax_buy_quantity()) {
                throw new ApiException(String.format("超过订单最高购买限制: 当前购买:%d, 最大购买:%d", visitorQuantity, salesInfo.getMax_buy_quantity()));
            }
            lockStockDtoMap.put(orderItem.getId(), new LockStockDto(orderItem, detailList));
            lockParams.add(bookingOrderManager.parseParams(orderItem));
            visitorMap.put(orderItem.getId(), visitorList);

            // 预分账记录
            bookingOrderManager.preSplitAccount(allDaysSales, orderItem.getId(), quantity, salesInfo.getPay_type(), bookingDate);
        }

        // 创建订单联系人
        Map shippingMap = (Map) params.get("shipping");
        bookingOrderManager.generateShipping(shippingMap, order.getId());

        // 锁定库存
        Result<Map<Integer, List<Boolean>>> lockResult = productSalesPlanRPCService.lockProductStocks(lockParams);
        if (!lockResult.isSuccess()) {
            throw new ApiException(lockResult.getError());
        }

        Map<Integer, List<Boolean>> listMap = lockResult.get();
        if (listMap.size() != lockStockDtoMap.size()) {
            throw new ApiException("锁定库存异常");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        // 检查是否待审核
        checkCreateAudit(lockStockDtoMap, listMap, orderItems);

        // 更新订单状态
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getStatus() == OrderConstant.OrderItemStatus.AUDIT_WAIT) {
                order.setStatus(OrderConstant.OrderStatus.AUDIT_WAIT);
                break;
            }
        }

        // 更新订单金额
        order.setPay_amount(from == OrderConstant.FromType.TRUST ? totalPayShopPrice : totalPayPrice);
        order.setSale_amount(totalPrice);

        // 更新审核时间
        if (order.getStatus() != OrderConstant.OrderStatus.AUDIT_WAIT && order.getAudit_time() == null) {
            order.setAudit_time(new Date());
        }
        orderMapper.updateByPrimaryKeySelective(order);

        // 到付
        addPaymentCallbackJob(order);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("order", JsonUtils.obj2map(order));
        resultMap.put("items", JsonUtils.json2List(JsonUtils.toJson(orderItems)));
        resultMap.put("visitors", JsonUtils.obj2map(visitorMap));
        Result<Object> result = new Result<>(true);
        result.put(resultMap);

        // 同步数据
        Map syncData = bookingOrderManager.syncCreateOrderData(order.getId());
        result.putExt(Result.SYNC_DATA, syncData);
        return result;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Result<?> audit(Map params) {
        String orderItemId = params.get("order_item_id").toString();
        long orderItemSn = Long.valueOf(orderItemId);
        // 分布式锁
        DistributedReentrantLock lock = distributedLockTemplate.execute(orderItemId, lockTimeOut);

        // 锁成功
        try {
            OrderItem orderItem = orderItemMapper.selectBySN(orderItemSn);
            if (orderItem == null) {
                throw new ApiException("订单不存在");
            }
            if (orderItem.getStatus() != OrderConstant.OrderItemStatus.AUDIT_WAIT) {
                throw new ApiException("订单不在待审状态");
            }

            Order order = orderMapper.selectByPrimaryKey(orderItem.getOrder_id());
            if (order == null) {
                throw new ApiException("订单不存在");
            }
            if (order.getStatus() != OrderConstant.OrderStatus.AUDIT_WAIT) {
                throw new ApiException("订单不在待审状态");
            }
            if (!params.containsKey(EpConstant.EpKey.EP_ID)) {
                throw new ApiException("非法请求:企业ID为空");
            }
            if (!String.valueOf(params.get(EpConstant.EpKey.EP_ID)).equals(String.valueOf(orderItem.getSupplier_ep_id()))) {
                throw new ApiException("非法请求:当前企业不能审核该订单");
            }

            orderItem.setAudit_user_id(CommonUtil.objectParseInteger(params.get("operator_id")));
            orderItem.setAudit_user_name(CommonUtil.objectParseString(params.get("operator_name")));
            orderItem.setAudit_time(new Date());
            boolean status = Boolean.parseBoolean(params.get("status").toString());
            // 通过
            if (status) {
                orderItem.setStatus(OrderConstant.OrderItemStatus.AUDIT_SUCCESS);
                orderItemMapper.updateByPrimaryKeySelective(orderItem);
                boolean allAudit = bookingOrderManager.isOrderAllAudit(orderItem.getOrder_id(), orderItem.getId());
                if (allAudit) {
                    order.setStatus(OrderConstant.OrderStatus.PAY_WAIT);
                    order.setAudit_time(new Date());
                    // 判断是否需要支付
                    if (order.getPay_amount() <= 0) { // 不需要支付
                        order.setStatus(OrderConstant.OrderStatus.PAID_HANDLING); // 已支付,处理中
                        // 支付成功回调 记录任务
                        Map<String, String> jobParams = new HashMap<>();
                        jobParams.put("orderId", order.getId().toString());
                        bookingOrderManager.addJob(OrderConstant.Actions.PAYMENT_CALLBACK, jobParams);
                    }
                    orderMapper.updateByPrimaryKeySelective(order);
                    // TODO: 2016/11/16  目前只支持单子订单发送
                    smsManager.sendAuditSuccess(orderItem);
                }
                // 同步数据
                Map syncData = bookingOrderManager.syncOrderAuditAcceptData(order.getId(), orderItem.getId());
                Result<?> result = new Result<>(true);
                result.putExt(Result.SYNC_DATA, syncData);
                return result;
            }
            orderItemMapper.updateByPrimaryKeySelective(orderItem);

            // 不通过
            // 取消订单 会自动同步数据
            return refundOrderManager.cancel(order.getNumber());
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Result<?> payment(Map params) {
        String orderSn = params.get("order_sn").toString();
        Long sn = Long.valueOf(orderSn);
        Integer payType = CommonUtil.objectParseInteger(params.get("pay_type"));

        // 分布式锁
        DistributedReentrantLock lock = distributedLockTemplate.execute(orderSn, lockTimeOut);

        // 锁成功
        try {
            Order order = orderMapper.selectBySN(sn);
            if (order == null) {
                throw new ApiException("订单不存在");
            }
            if (order.getStatus() != OrderConstant.OrderStatus.PAY_WAIT &&
                    order.getStatus() != OrderConstant.OrderStatus.PAY_FAIL &&
                    order.getStatus() != OrderConstant.OrderStatus.PAYING) {
                throw new ApiException("订单不在待支付状态");
            }
            if (order.getPay_amount() <= 0) {
                throw new ApiException("该订单不需要支付");
            }
            if (!params.containsKey(EpConstant.EpKey.EP_ID)) {
                throw new ApiException("非法请求:企业ID为空");
            }
            if (!String.valueOf(params.get(EpConstant.EpKey.EP_ID)).equals(String.valueOf(order.getBuy_ep_id()))) {
                throw new ApiException("非法请求:当前企业不能支付该订单");
            }

            order.setStatus(OrderConstant.OrderStatus.PAYING);
            order.setLocal_payment_serial_no(String.valueOf(UUIDGenerator.generateUUID()));
            order.setPayment_type(payType);
            order.setPay_time(new Date());
            orderMapper.updateByPrimaryKeySelective(order);

            // 调用支付RPC
            // 余额支付
            if (payType == PaymentConstant.PaymentType.BALANCE.intValue()) {
                BalanceChangeInfo payInfo = new BalanceChangeInfo();
                payInfo.setEp_id(order.getBuy_ep_id());
                payInfo.setCore_ep_id(bookingOrderManager.getCoreEpId(bookingOrderManager.getCoreEpId(order.getBuy_ep_id())));
                payInfo.setBalance(-order.getPay_amount());
                payInfo.setCan_cash(-order.getPay_amount());

                BalanceChangeInfo saveInfo = new BalanceChangeInfo();
                saveInfo.setEp_id(payInfo.getCore_ep_id());
                saveInfo.setCore_ep_id(payInfo.getCore_ep_id());
                saveInfo.setBalance(order.getPay_amount());
                // 支付
                Result result = bookingOrderManager.changeBalances(
                        PaymentConstant.BalanceChangeType.BALANCE_PAY,
                        order.getNumber().toString(), payInfo, saveInfo);
                if (!result.isSuccess()) {
                    log.warn("余额支付失败:{}", result.get());
                    throw new ApiException(result.getError());
                }
                // 同步数据
                Map syncData = bookingOrderManager.syncOrderPaymentData(order.getId());
                return new Result<>(true).putExt(Result.SYNC_DATA, syncData);
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

            Result result = thirdPayService.reqPay(order.getNumber(),
                    bookingOrderManager.getCoreEpId(epService.selectPlatformId(order.getBuy_ep_id())),
                    payType, payParams);
            if (!result.isSuccess()) {
                log.warn("第三方支付异常:{}", result);
                throw new ApiException(result.getError());
            }
            // 同步数据
            Map syncData = bookingOrderManager.syncOrderPaymentData(order.getId());
            return result.putExt(Result.SYNC_DATA, syncData);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Result<?> resendTicket(Map params) {
        OrderItem orderItem = orderItemMapper.selectBySN(Long.valueOf(params.get("order_item_sn").toString()));
        if (orderItem == null) {
            throw new ApiException("子订单不存在");
        }
        if (ArrayUtils.indexOf(new int[]{
                OrderConstant.OrderItemStatus.TICKET_FAIL,
                OrderConstant.OrderItemStatus.SEND,
                OrderConstant.OrderItemStatus.NON_SEND
        }, orderItem.getStatus()) < 0) {
            throw new ApiException("子订单不在可重新发票状态");
        }

        Integer visitorId = CommonUtil.objectParseInteger(params.get("visitor_id"));
        RefundOrder refundOrder = refundOrderMapper.selectByItemIdAndVisitor(orderItem.getId(), visitorId);
        if (refundOrder != null) {
            throw new ApiException("该子订单已发起退票");
        }

        MaSendResponse response = maSendResponseMapper.selectByVisitorId(orderItem.getId(), visitorId, orderItem.getEp_ma_id());
        if (orderItem.getStatus() == OrderConstant.OrderItemStatus.SEND && response != null) {
            ReSendTicketParams reSendTicketParams = new ReSendTicketParams();
            reSendTicketParams.setOrderSn(orderItem.getNumber());
            reSendTicketParams.setVisitorId(visitorId);
            reSendTicketParams.setMobile(params.get("phone").toString());
            return voucherRPCService.resendTicket(orderItem.getEp_ma_id(), reSendTicketParams);
        }
        // 出票
        // 记录任务
        Map<String, String> jobParam = new HashMap<>();
        jobParam.put("orderItemId", orderItem.getId().toString());
        bookingOrderManager.addJob(OrderConstant.Actions.SEND_TICKET, jobParam);
        return new Result<>(true);
    }

    @Override
    public Result<?> createForGroup(Map params) throws Exception {
        Integer buyEpId = CommonUtil.objectParseInteger(params.get(EpConstant.EpKey.EP_ID));
        // 获取平台商ID
        Integer coreEpId = CommonUtil.objectParseInteger(params.get(EpConstant.EpKey.CORE_EP_ID));
        Integer from = CommonUtil.objectParseInteger(params.get("from"));
        String remark = CommonUtil.objectParseString(params.get("remark"));
        Integer groupId = CommonUtil.objectParseInteger(params.get("group_id"));
        Integer guideId = CommonUtil.objectParseInteger(params.get("guide_id"));
        int totalPrice = 0;
        int totalPayPrice = 0;
        int totalPayShopPrice = 0;

        // 验证团队
        Group group = groupMapper.selectByPrimaryKey(groupId);
        if (group == null) {
            throw new ApiException("团队不存在");
        }
        if (group.getCore_ep_id().intValue() != coreEpId) {
            throw new ApiException("团队不属于本平台");
        }

        // 验证导游
        Guide guide = guideMapper.selectByPrimaryKey(guideId);
        if (guide == null) {
            throw new ApiException("导游不存在");
        }
        if (guide.getCore_ep_id().intValue() != group.getCore_ep_id().intValue()) {
            throw new ApiException("该导游不属于本平台");
        }

        // 判断销售商状态是否为已冻结
        if (!bookingOrderManager.isEpStatus(epService.getEpStatus(buyEpId), EpConstant.EpStatus.ACTIVE)) {
            throw new ApiException("销售商企业已冻结");
        }
        // 只有销售商可以下单
        Result<Integer> epType = epService.selectEpType(buyEpId);
        if (!bookingOrderManager.isEpType(epType, EpConstant.EpType.SELLER) &&
                !bookingOrderManager.isEpType(epType, EpConstant.EpType.OTA)) {
            throw new ApiException("该企业不能购买产品");
        }

        // 锁定库存集合(统一锁定)
        Map<Integer, LockStockDto> lockStockDtoMap = new HashMap<>();
        List<ProductSearchParams> lockParams = new ArrayList<>();

        // 获取下单企业名称
        String buyEpName = null;
        Result<Map<String, Object>> epResult = epService.selectId(buyEpId);
        if (epResult != null && epResult.isSuccess() && epResult.get() != null) {
            buyEpName = String.valueOf(epResult.get().get("name"));
        }

        // 创建订单
        Order order = bookingOrderManager.generateOrder(coreEpId, buyEpId, buyEpName,
                CommonUtil.objectParseInteger(params.get("operator_id")),
                CommonUtil.objectParseString(params.get("operator_name")), from, remark);

        // 存储游客信息
        Map<Integer, List<Visitor>> visitorMap = new HashMap<>();
        // 获取子订单
        List<Map> items = (List<Map>) params.get("items");
        for (Map item : items) {
            Integer productSubId = CommonUtil.objectParseInteger(item.get("product_sub_code"));
            Integer quantity = CommonUtil.objectParseInteger(item.get("quantity"));
            Integer days = CommonUtil.objectParseInteger(item.get("days"));
            //预定日期
            Date bookingDate = DateFormatUtils.parseString(DateFormatUtils.DATE_TIME_FORMAT, CommonUtil.objectParseString(item.get("start")));
            // 验证出游日期
            if (group.getStart_date().before(bookingDate)) {
                throw new ApiException("团队出游日期不能小于预定日期");
            }

            // 验证是否可售
            ProductSearchParams searchParams = new ProductSearchParams();
            searchParams.setSubProductId(productSubId);
            searchParams.setStartDate(bookingDate);
            searchParams.setDays(days);
            searchParams.setQuantity(quantity);
            searchParams.setBuyEpId(buyEpId);
            Result<ProductSalesInfo> salesInfoResult = productSalesPlanRPCService.validateProductSalesInfo(searchParams);
            if (!salesInfoResult.isSuccess()) {
                throw new ApiException(salesInfoResult.getError());
            }
            ProductSalesInfo salesInfo = salesInfoResult.get();
            // 判断供应商状态是否为已冻结
            if (!bookingOrderManager.isEpStatus(epService.getEpStatus(salesInfo.getEp_id()), EpConstant.EpStatus.ACTIVE)) {
                throw new ApiException("供应商企业已冻结");
            }

            List<ProductSalesDayInfo> dayInfoList = salesInfo.getDay_info_list();

            if (dayInfoList.size() != days) {
                throw new ApiException("预定天数与获取产品天数不匹配");
            }

            // 验证预定时间限制
            bookingOrderManager.validateBookingDate(bookingDate, dayInfoList);

            // 验证最低购票
            if (salesInfo.getMin_buy_quantity() != null && salesInfo.getMin_buy_quantity() > quantity) {
                throw new ApiException("低于最低购买票数");
            }

            // 实名制验证
            List visitors = (List) item.get("visitor");
            Result<List<GroupMember>> validateResult = bookingOrderManager.validateGroupVisitor(visitors, salesInfo.getReal_name(), quantity, groupId);
            if (!validateResult.isSuccess()) {
                throw new ApiException(validateResult.getError());
            }
            List<GroupMember> groupMemberList = validateResult.get();

            // 每天的价格
            List<List<EpSalesInfo>> allDaysSales = salesInfo.getSales();

            // 计算分销价格
            int[] priceArray = bookingOrderManager.calcSalesPrice(allDaysSales, buyEpId, quantity, salesInfo.getPay_type(), from);
            totalPrice += priceArray[0];
            totalPayPrice += priceArray[1];
            totalPayShopPrice += priceArray[2];

            // 创建子订单
            OrderItem orderItem = bookingOrderManager.generateItem(salesInfo, dayInfoList.get(dayInfoList.size() - 1).getEnd_time(), priceArray[0], bookingDate, days, order.getId(), quantity, productSubId);

            List<OrderItemDetail> detailList = new ArrayList<>();
            List<Visitor> visitorList = new ArrayList<>();
            // 创建子订单详情
            int i = 0;
            for (ProductSalesDayInfo dayInfo : dayInfoList) {
                OrderItemDetail orderItemDetail = bookingOrderManager.generateDetail(dayInfo, orderItem.getId(), DateUtils.addDays(bookingDate, i), quantity);
                detailList.add(orderItemDetail);
                // 创建游客信息
                if (groupMemberList != null) {
                    for (GroupMember member : groupMemberList) {
                        visitorList.add(bookingOrderManager.generateGroupVisitor(member, orderItemDetail.getId(), groupId));
                    }
                }
                i++;
            }

            lockStockDtoMap.put(orderItem.getId(), new LockStockDto(orderItem, detailList));
            lockParams.add(bookingOrderManager.parseParams(orderItem));
            visitorMap.put(orderItem.getId(), visitorList);

            // 预分账记录
            bookingOrderManager.preSplitAccount(allDaysSales, orderItem.getId(), quantity, salesInfo.getPay_type(), bookingDate);
        }
        // 创建订单联系人
        Shipping shipping = new Shipping();
        shipping.setOrder_id(order.getId());
        shipping.setName(guide.getName());
        shipping.setPhone(guide.getPhone());
        shipping.setSid(guide.getSid());
        shippingMapper.insertSelective(shipping);

        // 锁定库存
        Result<Map<Integer, List<Boolean>>> lockResult = productSalesPlanRPCService.lockProductStocks(lockParams);
        if (!lockResult.isSuccess()) {
            throw new ApiException(lockResult.getError());
        }

        Map<Integer, List<Boolean>> listMap = lockResult.get();
        if (listMap.size() != lockStockDtoMap.size()) {
            throw new ApiException("锁定库存异常");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        // 检查是否待审核
        checkCreateAudit(lockStockDtoMap, listMap, orderItems);

        // 更新订单状态
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getStatus() == OrderConstant.OrderItemStatus.AUDIT_WAIT) {
                order.setStatus(OrderConstant.OrderStatus.AUDIT_WAIT);
                break;
            }
        }

        // 更新订单金额
        order.setPay_amount(from == OrderConstant.FromType.TRUST ? totalPayShopPrice : totalPayPrice);
        order.setSale_amount(totalPrice);

        // 更新审核时间
        if (order.getStatus() != OrderConstant.OrderStatus.AUDIT_WAIT && order.getAudit_time() == null) {
            order.setAudit_time(new Date());
        }
        orderMapper.updateByPrimaryKeySelective(order);

        // 到付
        addPaymentCallbackJob(order);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("order", JsonUtils.obj2map(order));
        resultMap.put("items", JsonUtils.json2List(JsonUtils.toJson(orderItems)));
        resultMap.put("visitors", JsonUtils.obj2map(visitorMap));
        Result<Object> result = new Result<>(true);
        result.put(resultMap);

        // 同步数据
        Map syncData = bookingOrderManager.syncCreateOrderData(order.getId());
        result.putExt(Result.SYNC_DATA, syncData);
        return result;
    }

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void checkCreateAudit(Map<Integer, LockStockDto> lockStockDtoMap, Map<Integer, List<Boolean>> listMap, List<OrderItem> orderItems) {
        for (Integer itemId : listMap.keySet()) {
            int i = 0;
            // 判断是否需要审核
            LockStockDto lockStockDto = lockStockDtoMap.get(itemId);
            OrderItem item = lockStockDto.getOrderItem();
            List<Boolean> booleanList = listMap.get(itemId);
            List<OrderItemDetail> orderItemDetail = lockStockDto.getOrderItemDetail();
            if (booleanList.size() != orderItemDetail.size()) {
                throw new ApiException(String.format("锁库存天数:%d与购买天数:%d不匹配", booleanList.size(), orderItemDetail.size()));
            }
            for (Boolean oversell : booleanList) {
                OrderItemDetail detail = orderItemDetail.get(i);
                detail.setOversell(oversell);
                // 如果是超卖则把子订单状态修改为待审
                if (oversell && item.getStatus() == OrderConstant.OrderItemStatus.AUDIT_SUCCESS) {
                    item.setStatus(OrderConstant.OrderItemStatus.AUDIT_WAIT);
                    orderItemMapper.updateByPrimaryKeySelective(item);
                    smsManager.sendAuditSms(item);
                }
                // 更新子订单详情
                orderItemDetailMapper.updateByPrimaryKeySelective(detail);
                i++;
            }
            orderItems.add(item);
        }
    }

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void addPaymentCallbackJob(Order order) {
        if (order.getStatus() != OrderConstant.OrderStatus.AUDIT_WAIT && order.getPay_amount() <= 0) {
            order.setStatus(OrderConstant.OrderStatus.PAID_HANDLING); // 已支付,处理中
            // 支付成功回调 记录任务
            Map<String, String> jobParams = new HashMap<>();
            jobParams.put("orderId", order.getId().toString());
            bookingOrderManager.addJob(OrderConstant.Actions.PAYMENT_CALLBACK, jobParams);
        }
    }
}
