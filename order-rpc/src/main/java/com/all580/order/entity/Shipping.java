package com.all580.order.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Shipping implements Serializable {
    /**
     * 所属表字段为 t_shipping.id
     */
    private Integer id;

    /**
     * 订单ID
     * 所属表字段为 t_shipping.order_id
     */
    private Integer orderId;

    /**
     * 收货人姓名
     * 所属表字段为 t_shipping.name
     */
    private String name;

    /**
     * 收货人手机号
     * 所属表字段为 t_shipping.phone
     */
    private String phone;

    /**
     * 收货人身份证
     * 所属表字段为 t_shipping.sid
     */
    private String sid;

    /**
     * 收货人地址
     * 所属表字段为 t_shipping.address
     */
    private String address;

    /**
     * 收货人邮编
     * 所属表字段为 t_shipping.zip
     */
    private String zip;

    /**
     * 所属表字段为 t_shipping.update_time
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table t_shipping
     *
     * @mbggenerated Wed Oct 26 16:03:36 CST 2016
     */
    private static final long serialVersionUID = 1L;

}