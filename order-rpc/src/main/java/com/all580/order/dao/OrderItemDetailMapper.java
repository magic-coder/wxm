package com.all580.order.dao;

import com.all580.order.entity.OrderItemDetail;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface OrderItemDetailMapper {
    /**
     *  根据主键删除数据库的记录,t_order_item_detail
     *
     * @param id
     */
    int deleteByPrimaryKey(Integer id);

    /**
     *  新写入数据库记录,t_order_item_detail
     *
     * @param record
     */
    int insert(OrderItemDetail record);

    /**
     *  动态字段,写入数据库记录,t_order_item_detail
     *
     * @param record
     */
    int insertSelective(OrderItemDetail record);

    /**
     *  根据指定主键获取一条数据库记录,t_order_item_detail
     *
     * @param id
     */
    OrderItemDetail selectByPrimaryKey(Integer id);

    /**
     *  动态字段,根据主键来更新符合条件的数据库记录,t_order_item_detail
     *
     * @param record
     */
    int updateByPrimaryKeySelective(OrderItemDetail record);

    /**
     *  根据主键来更新符合条件的数据库记录,t_order_item_detail
     *
     * @param record
     */
    int updateByPrimaryKey(OrderItemDetail record);

    /**
     * 判断是否可退
     * @param itemId 子订单ID
     * @param day 日期
     * @param quantity 张数
     * @return
     */
    boolean canRefund(@Param("itemId") Integer itemId, @Param("day") Date day, @Param("quantity") Integer quantity);

    /**
     * 根据子订单获取每日详情
     * @param itemId 子订单
     * @return
     */
    List<OrderItemDetail> selectByItemId(@Param("itemId") Integer itemId);

    /**
     * 根据订单ID获取所有子订单详情
     * @param orderId 订单ID
     * @return
     */
    List<OrderItemDetail> selectByOrderId(@Param("orderId") Integer orderId);

    /**
     * 退订剩余的所有票
     * @param itemId
     * @return
     */
    int refundRemain(@Param("itemId") Integer itemId);

    /**
     * 还原退订剩余的所有票
     * @param itemId
     * @return
     */
    int resetRefundRemain(@Param("itemId") Integer itemId);
    String selectExpiryMaxDate(@Param("itemId") Integer itemId);

    int useQuantity(@Param("id") Integer id, @Param("quantity") Integer quantity);

    /**
     * 使用剩余所有票
     * @param id
     * @return
     */
    int useRemain(@Param("id") Integer id);
}