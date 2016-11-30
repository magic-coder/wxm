package com.all580.order.dao;

import com.all580.order.entity.RefundOrder;
import org.apache.ibatis.annotations.Param;

public interface RefundOrderMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_refund_order
     *
     * @mbggenerated Wed Nov 30 16:11:20 CST 2016
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_refund_order
     *
     * @mbggenerated Wed Nov 30 16:11:20 CST 2016
     */
    int insert(RefundOrder record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_refund_order
     *
     * @mbggenerated Wed Nov 30 16:11:20 CST 2016
     */
    int insertSelective(RefundOrder record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_refund_order
     *
     * @mbggenerated Wed Nov 30 16:11:20 CST 2016
     */
    RefundOrder selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_refund_order
     *
     * @mbggenerated Wed Nov 30 16:11:20 CST 2016
     */
    int updateByPrimaryKeySelective(RefundOrder record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_refund_order
     *
     * @mbggenerated Wed Nov 30 16:11:20 CST 2016
     */
    int updateByPrimaryKey(RefundOrder record);

    /**
     * 根据退订流水编号获取退订订单
     * @param sn 编号
     * @return
     */
    RefundOrder selectBySN(Long sn);

    /**
     * 根据子订单ID以及退票本地流水查询退票订单
     * @param itemId 子订单ID
     * @param refundSn 本地退票流水
     * @return
     */
    RefundOrder selectByItemIdAndRefundSn(@Param("itemId") Integer itemId, @Param("refundSn") Long refundSn);

    /**
     * 根据子订单ID以及退票游客ID查询退票订单
     * @param itemId
     * @param visitorId
     * @return
     */
    RefundOrder selectByItemIdAndVisitor(@Param("itemId") Integer itemId, @Param("visitorId") Integer visitorId);
}