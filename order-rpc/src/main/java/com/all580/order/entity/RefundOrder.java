package com.all580.order.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class RefundOrder implements Serializable {
    /**
     * 所属表字段为 t_refund_order.id
     */
    private Integer id;

    /**
     * 流水编号
     * 所属表字段为 t_refund_order.number
     */
    private Long number;

    /**
     * 子订单id
     * 所属表字段为 t_refund_order.order_item_id
     */
    private Integer order_item_id;

    /**
     * 团队ID 0 则为散客
     * 所属表字段为 t_refund_order.group_id
     */
    private Integer group_id;

    /**
     * 数量
     * 所属表字段为 t_refund_order.quantity
     */
    private Integer quantity;

    /**
     * 退支付者金额
     * 所属表字段为 t_refund_order.money
     */
    private Integer money;

    /**
     * 手续费
     * 所属表字段为 t_refund_order.fee
     */
    private Integer fee;

    /**
     * 通道费率
     * 所属表字段为 t_refund_order.channel_fee
     */
    private Integer channel_fee;

    /**
     * 1-待审核
2-退订失败
3-退票中
4-退款中
5-退款失败
6-退订成功
     * 所属表字段为 t_refund_order.status
     */
    private Integer status;

    /**
     * json 按日期存每天退票数
     * 所属表字段为 t_refund_order.data
     */
    private String data;

    /**
     * 所属表字段为 t_refund_order.create_time
     */
    private Date create_time;

    /**
     * 审核时间
     * 所属表字段为 t_refund_order.audit_time
     */
    private Date audit_time;

    /**
     * 退款时间
     * 所属表字段为 t_refund_order.refund_money_time
     */
    private Date refund_money_time;

    /**
     * 本地退款流水
     * 所属表字段为 t_refund_order.local_payment_serial_no
     */
    private Long local_payment_serial_no;

    /**
     * 退票时间
     * 所属表字段为 t_refund_order.refund_ticket_time
     */
    private Date refund_ticket_time;

    /**
     * 本地退票流水
     * 所属表字段为 t_refund_order.local_refund_serial_no
     */
    private Long local_refund_serial_no;

    /**
     * 退订原因
     * 所属表字段为 t_refund_order.cause
     */
    private String cause;

    /**
     * 退票审核
     * 所属表字段为 t_refund_order.audit_ticket
     */
    private Integer audit_ticket;

    /**
     * 退钱审核
     * 所属表字段为 t_refund_order.audit_money
     */
    private Integer audit_money;

    /**
     * 审核用户ID
     * 所属表字段为 t_refund_order.audit_user_id
     */
    private Integer audit_user_id;

    /**
     * 审核用户名称
     * 所属表字段为 t_refund_order.audit_user_name
     */
    private String audit_user_name;

    /**
     * 审核退款用户ID
     * 所属表字段为 t_refund_order.audit_money_user_id
     */
    private Integer audit_money_user_id;

    /**
     * 退款审核用户名称
     * 所属表字段为 t_refund_order.audit_money_user_name
     */
    private String audit_money_user_name;

    /**
     * 审核退款时间
     * 所属表字段为 t_refund_order.audit_money_time
     */
    private Date audit_money_time;

    /**
     * 申请退订用户ID
     * 所属表字段为 t_refund_order.apply_user_id
     */
    private Integer apply_user_id;

    /**
     * 申请退订用户名
     * 所属表字段为 t_refund_order.apply_user_name
     */
    private String apply_user_name;

    /**
     * 所属表字段为 t_refund_order.update_time
     */
    private String update_time;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table t_refund_order
     *
     * @mbggenerated Tue Feb 14 16:29:07 CST 2017
     */
    private static final long serialVersionUID = 1L;

}