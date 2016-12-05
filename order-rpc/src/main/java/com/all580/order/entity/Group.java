package com.all580.order.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Group implements Serializable {
    /**
     * 主键
     * 所属表字段为 t_group.id
     */
    private Integer id;

    /**
     * 团号
     * 所属表字段为 t_group.number
     */
    private String number;

    /**
     * 出行日期
     * 所属表字段为 t_group.start_date
     */
    private Date start_date;

    /**
     * 旅行社名称
     * 所属表字段为 t_group.travel_name
     */
    private String travel_name;

    /**
     * 省
     * 所属表字段为 t_group.province
     */
    private Integer province;

    /**
     * 市
     * 所属表字段为 t_group.city
     */
    private Integer city;

    /**
     * 区
     * 所属表字段为 t_group.area
     */
    private Integer area;

    /**
     * 全地址
     * 所属表字段为 t_group.address
     */
    private String address;

    /**
     * 导游姓名
     * 所属表字段为 t_group.guide_name
     */
    private String guide_name;

    /**
     * 导游手机号
     * 所属表字段为 t_group.guide_phone
     */
    private String guide_phone;

    /**
     * 导游身份证
     * 所属表字段为 t_group.guide_sid
     */
    private String guide_sid;

    /**
     * 导游证件号
     * 所属表字段为 t_group.guide_card
     */
    private String guide_card;

    /**
     * 成人数
     * 所属表字段为 t_group.adult
     */
    private Integer adult;

    /**
     * 小孩/儿童 数
     * 所属表字段为 t_group.kid
     */
    private Integer kid;

    /**
     * 经理人
     * 所属表字段为 t_group.manager_name
     */
    private String manager_name;

    /**
     * 经理人手机
     * 所属表字段为 t_group.manager_phone
     */
    private String manager_phone;

    /**
     * 计调
     * 所属表字段为 t_group.tour_name
     */
    private String tour_name;

    /**
     * 计调手机
     * 所属表字段为 t_group.tour_phone
     */
    private String tour_phone;

    /**
     * 创建时间
     * 所属表字段为 t_group.create_time
     */
    private Date create_time;

    /**
     * 创建人ID
     * 所属表字段为 t_group.create_user_id
     */
    private Integer create_user_id;

    /**
     * 创建人名称
     * 所属表字段为 t_group.create_user_name
     */
    private String create_user_name;

    /**
     * 企业ID
     * 所属表字段为 t_group.ep_id
     */
    private Integer ep_id;

    /**
     * 平台商ID
     * 所属表字段为 t_group.core_ep_id
     */
    private Integer core_ep_id;

    /**
     * 所属表字段为 t_group.update_time
     */
    private String update_time;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table t_group
     *
     * @mbggenerated Thu Dec 01 11:25:59 CST 2016
     */
    private static final long serialVersionUID = 1L;

}