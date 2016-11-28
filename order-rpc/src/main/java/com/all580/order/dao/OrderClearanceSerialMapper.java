package com.all580.order.dao;

import com.all580.order.entity.OrderClearanceSerial;

public interface OrderClearanceSerialMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_order_clearance_serial
     *
     * @mbggenerated Fri Nov 25 14:51:24 CST 2016
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_order_clearance_serial
     *
     * @mbggenerated Fri Nov 25 14:51:24 CST 2016
     */
    int insert(OrderClearanceSerial record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_order_clearance_serial
     *
     * @mbggenerated Fri Nov 25 14:51:24 CST 2016
     */
    int insertSelective(OrderClearanceSerial record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_order_clearance_serial
     *
     * @mbggenerated Fri Nov 25 14:51:24 CST 2016
     */
    OrderClearanceSerial selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_order_clearance_serial
     *
     * @mbggenerated Fri Nov 25 14:51:24 CST 2016
     */
    int updateByPrimaryKeySelective(OrderClearanceSerial record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_order_clearance_serial
     *
     * @mbggenerated Fri Nov 25 14:51:24 CST 2016
     */
    int updateByPrimaryKey(OrderClearanceSerial record);

    /**
     * 根据流水号获取核销流水
     * @param sn 流水号
     * @return
     */
    OrderClearanceSerial selectBySn(String sn);
}