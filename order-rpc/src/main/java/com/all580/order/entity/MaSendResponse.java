package com.all580.order.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class MaSendResponse implements Serializable {
    /**
     * 所属表字段为 t_ma_send_response.id
     */
    private Integer id;

    /**
     * 子订单ID
     * 所属表字段为 t_ma_send_response.order_item_id
     */
    private Integer orderItemId;

    /**
     * 凭证订单ID
     * 所属表字段为 t_ma_send_response.ma_order_id
     */
    private String maOrderId;

    /**
     * 发码公司中的产品ID
     * 所属表字段为 t_ma_send_response.ma_product_id
     */
    private String maProductId;

    /**
     * 凭证ID
     * 所属表字段为 t_ma_send_response.ep_ma_id
     */
    private Integer epMaId;

    /**
     * 二维码图像链接
     * 所属表字段为 t_ma_send_response.image_url
     */
    private String imageUrl;

    /**
     * 电子凭证号
     * 所属表字段为 t_ma_send_response.voucher_value
     */
    private String voucherValue;

    /**
     * 身份证
     * 所属表字段为 t_ma_send_response.sid
     */
    private String sid;

    /**
     * 手机号码
     * 所属表字段为 t_ma_send_response.phone
     */
    private String phone;

    /**
     * 所属表字段为 t_ma_send_response.create_time
     */
    private Date createTime;

    /**
     * 所属表字段为 t_ma_send_response.update_time
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table t_ma_send_response
     *
     * @mbggenerated Mon Oct 17 10:42:29 CST 2016
     */
    private static final long serialVersionUID = 1L;

}