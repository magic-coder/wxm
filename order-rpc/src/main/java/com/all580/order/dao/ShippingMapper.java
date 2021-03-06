package com.all580.order.dao;

import com.all580.order.entity.Shipping;
import org.apache.ibatis.annotations.Param;

public interface ShippingMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_shipping
     *
     * @mbggenerated Fri Nov 25 14:51:24 CST 2016
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_shipping
     *
     * @mbggenerated Fri Nov 25 14:51:24 CST 2016
     */
    int insert(Shipping record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_shipping
     *
     * @mbggenerated Fri Nov 25 14:51:24 CST 2016
     */
    int insertSelective(Shipping record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_shipping
     *
     * @mbggenerated Fri Nov 25 14:51:24 CST 2016
     */
    Shipping selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_shipping
     *
     * @mbggenerated Fri Nov 25 14:51:24 CST 2016
     */
    int updateByPrimaryKeySelective(Shipping record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_shipping
     *
     * @mbggenerated Fri Nov 25 14:51:24 CST 2016
     */
    int updateByPrimaryKey(Shipping record);

    /**
     * 根据订单ID获取订单联系人信息
     * @param orderId 订单ID
     * @return
     */
    Shipping selectByOrder(@Param("orderId") Integer orderId);

    int modify(@Param("orderId") Integer orderId);
}