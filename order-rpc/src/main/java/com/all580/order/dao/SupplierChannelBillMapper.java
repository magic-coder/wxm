package com.all580.order.dao;

import com.all580.order.entity.SupplierChannelBill;

public interface SupplierChannelBillMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_supplier_channel_bill
     *
     * @mbggenerated Wed Dec 14 15:26:52 CST 2016
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_supplier_channel_bill
     *
     * @mbggenerated Wed Dec 14 15:26:52 CST 2016
     */
    int insert(SupplierChannelBill record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_supplier_channel_bill
     *
     * @mbggenerated Wed Dec 14 15:26:52 CST 2016
     */
    int insertSelective(SupplierChannelBill record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_supplier_channel_bill
     *
     * @mbggenerated Wed Dec 14 15:26:52 CST 2016
     */
    SupplierChannelBill selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_supplier_channel_bill
     *
     * @mbggenerated Wed Dec 14 15:26:52 CST 2016
     */
    int updateByPrimaryKeySelective(SupplierChannelBill record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_supplier_channel_bill
     *
     * @mbggenerated Wed Dec 14 15:26:52 CST 2016
     */
    int updateByPrimaryKey(SupplierChannelBill record);
}