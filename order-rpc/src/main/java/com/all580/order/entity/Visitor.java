package com.all580.order.entity;

import lombok.*;

import java.io.Serializable;

@Data
public class Visitor implements Serializable {
    /**
     * 关联ID:子订单ID
     * 所属表字段为 t_visitor.ref_id
     */
    private Integer refId;

    /**
     * 身份证号
     * 所属表字段为 t_visitor.sid
     */
    private String sid;

    /**
     * 联系电话
     * 所属表字段为 t_visitor.phone
     */
    private String phone;

    /**
     * 联系人姓名
     * 所属表字段为 t_visitor.name
     */
    private String name;

    /**
     * 票数
     * 所属表字段为 t_visitor.quantity
     */
    private Integer quantity;

    /**
     * 申请退票数量
     * 所属表字段为 t_visitor.pre_return
     */
    private Integer preReturn;

    /**
     * 退票数量
     * 所属表字段为 t_visitor.return_quantity
     */
    private Integer returnQuantity;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table t_visitor
     *
     * @mbggenerated Fri Sep 30 15:22:08 CST 2016
     */
    private static final long serialVersionUID = 1L;

}