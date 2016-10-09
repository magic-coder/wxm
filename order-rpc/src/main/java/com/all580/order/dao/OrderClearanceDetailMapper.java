package com.all580.order.dao;

import com.all580.order.entity.OrderClearanceDetail;
import java.util.List;

public interface OrderClearanceDetailMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_order_clearance_detail
     *
     * @mbggenerated Fri Sep 30 15:22:08 CST 2016
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_order_clearance_detail
     *
     * @mbggenerated Fri Sep 30 15:22:08 CST 2016
     */
    int insert(OrderClearanceDetail record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_order_clearance_detail
     *
     * @mbggenerated Fri Sep 30 15:22:08 CST 2016
     */
    OrderClearanceDetail selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_order_clearance_detail
     *
     * @mbggenerated Fri Sep 30 15:22:08 CST 2016
     */
    List<OrderClearanceDetail> selectAll();

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_order_clearance_detail
     *
     * @mbggenerated Fri Sep 30 15:22:08 CST 2016
     */
    int updateByPrimaryKey(OrderClearanceDetail record);
}