package com.all580.order.dao;

import com.all580.order.entity.RefundSerial;
import java.util.List;

public interface RefundSerialMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_refund_serial
     *
     * @mbggenerated Tue Oct 11 19:38:33 CST 2016
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_refund_serial
     *
     * @mbggenerated Tue Oct 11 19:38:33 CST 2016
     */
    int insert(RefundSerial record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_refund_serial
     *
     * @mbggenerated Tue Oct 11 19:38:33 CST 2016
     */
    RefundSerial selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_refund_serial
     *
     * @mbggenerated Tue Oct 11 19:38:33 CST 2016
     */
    List<RefundSerial> selectAll();

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_refund_serial
     *
     * @mbggenerated Tue Oct 11 19:38:33 CST 2016
     */
    int updateByPrimaryKey(RefundSerial record);
}