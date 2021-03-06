package com.all580.order.adapter;

import com.all580.order.dto.CreateOrder;
import com.all580.order.dto.PriceDto;
import com.all580.order.dto.ValidateProductSub;
import com.all580.order.entity.*;
import com.all580.product.api.model.EpSalesInfo;
import com.all580.product.api.model.ProductSalesInfo;
import com.framework.common.Result;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author zhouxianjun(Alone)
 * @ClassName:
 * @Description:
 * @date 2017/2/7 11:46
 */
public interface CreateOrderInterface {

    CreateOrder parseParams(Map params);

    List<Map> getOrderItemParams(Map params);

    ValidateProductSub parseItemParams(CreateOrder createOrder, Map item);

    Result validate(CreateOrder createOrder, Map params);

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    Order insertOrder(CreateOrder createOrder, Map params);

    ProductSalesInfo validateProductAndGetSales(ValidateProductSub sub, CreateOrder createOrder, Map item);

    void validateBookingDate(ValidateProductSub sub, ProductSalesInfo salesInfo);

    void validateVisitor(ProductSalesInfo salesInfo, ValidateProductSub sub, List<?> visitorList, Map item);

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    OrderItem insertItem(Order order, ValidateProductSub sub, ProductSalesInfo salesInfo, PriceDto price, Map item);

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    List<OrderItemDetail> insertDetail(Order order, CreateOrder createOrder, OrderItem item, ValidateProductSub sub, ProductSalesInfo salesInfo, List<List<EpSalesInfo>> allDaysSales);

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    List<OrderItemSalesChain> insertSalesChain(OrderItem item, ValidateProductSub sub, List<List<EpSalesInfo>> allDaysSales);

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    List<Visitor> insertVisitor(List<?> visitorList, OrderItem orderItem, ProductSalesInfo salesInfo, ValidateProductSub sub, Map item);

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    Shipping insertShipping(Map params, Order order);



    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    boolean after(Map params, Order order);
}
