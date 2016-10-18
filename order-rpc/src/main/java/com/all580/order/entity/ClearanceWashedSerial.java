package com.all580.order.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Data
public class ClearanceWashedSerial implements Serializable {
    /**
     * 主键
     * 所属表字段为 t_clearance_washed_serial.id
     */
    private Integer id;

    /**
     * 第三方冲正流水
     * 所属表字段为 t_clearance_washed_serial.serial_no
     */
    private String serialNo;

    /**
     * 核销流水
     * 所属表字段为 t_clearance_washed_serial.clearance_serial_no
     */
    private String clearanceSerialNo;

    /**
     * 数量
     * 所属表字段为 t_clearance_washed_serial.quantity
     */
    private Integer quantity;

    /**
     * 哪一天的票
     * 所属表字段为 t_clearance_washed_serial.day
     */
    private Date day;

    /**
     * 冲正时间
     * 所属表字段为 t_clearance_washed_serial.clearance_washed_time
     */
    private Date clearanceWashedTime;

    /**
     * 创建时间
     * 所属表字段为 t_clearance_washed_serial.create_time
     */
    private Date createTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table t_clearance_washed_serial
     *
     * @mbggenerated Fri Sep 30 15:22:08 CST 2016
     */
    private static final long serialVersionUID = 1L;

}