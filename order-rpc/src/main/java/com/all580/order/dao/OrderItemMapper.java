package com.all580.order.dao;

import com.all580.order.entity.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface OrderItemMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_order_item
     *
     * @mbggenerated Mon Dec 12 14:06:41 CST 2016
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_order_item
     *
     * @mbggenerated Mon Dec 12 14:06:41 CST 2016
     */
    int insert(OrderItem record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_order_item
     *
     * @mbggenerated Mon Dec 12 14:06:41 CST 2016
     */
    int insertSelective(OrderItem record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_order_item
     *
     * @mbggenerated Mon Dec 12 14:06:41 CST 2016
     */
    OrderItem selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_order_item
     *
     * @mbggenerated Mon Dec 12 14:06:41 CST 2016
     */
    int updateByPrimaryKeySelective(OrderItem record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_order_item
     *
     * @mbggenerated Mon Dec 12 14:06:41 CST 2016
     */
    int updateByPrimaryKey(OrderItem record);

    /**
     * 根据子产品和身份证获取该身份证在指定时间内次数
     * @param productSubCode 子产品CODE
     * @param sid 身份证
     * @param start 开始时间
     * @param end 结束时间
     * @return
     */
    int countBySidAndProductForDate(@Param("productSubCode") Long productSubCode, @Param("sid") String sid,
                                    @Param("start") Date start, @Param("end") Date end);

    /**
     * 根据订单ID查询子订单
     * @param orderId 订单ID
     * @return
     */
    List<OrderItem> selectByOrderId(@Param("orderId") Integer orderId);

    /**
     * 根据订单ID设置子订单状态
     * @param orderId 订单ID
     * @param status 状态
     * @return
     */
    int setStatusByOrderId(@Param("orderId") Integer orderId, @Param("status") Integer status);

    /**
     * 根据子订单编号获取
     * @param sn 编号
     * @return
     */
    OrderItem selectBySN(@Param("sn") Long sn);

    /**
     * 根据订单ID获取商品名称
     * @param orderId 订单ID
     * @return
     */
    List<String> getProductNamesByOrderId(@Param("orderId") Integer orderId);

    /**
     * 根据订单ID获取商品CODE
     * @param orderId 订单ID
     * @return
     */
    List<Long> getProductIdsByOrderId(@Param("orderId") Integer orderId);

    /**
     * 查询平台商订单列表
     * @param coreEpId 供应侧平台商ID
     * @param startTime 下单开始时间
     * @param endTime   下单结束时间
     * @param orderStatus 订单状态
     * @param orderItemStatus 子订单状态
     * @param phone 联系人手机
     * @param orderItemNum 子订单号
     * @return
     */
    List<Map> selectPlatformBySupplierCoreEpId(@Param("coreEpId") Integer coreEpId,
                                               @Param("startTime") Date startTime,
                                               @Param("endTime") Date endTime,
                                               @Param("orderStatus") Integer orderStatus,
                                               @Param("orderItemStatus") Integer orderItemStatus,
                                               @Param("phone") String phone,
                                               @Param("orderItemNumber") Long orderItemNum,
                                               @Param("record_start") Integer recordStart,
                                               @Param("record_count") Integer recordCount);

    int selectPlatformBySupplierCoreEpIdCount(@Param("coreEpId") Integer coreEpId,
                                               @Param("startTime") Date startTime,
                                               @Param("endTime") Date endTime,
                                               @Param("orderStatus") Integer orderStatus,
                                               @Param("orderItemStatus") Integer orderItemStatus,
                                               @Param("phone") String phone,
                                               @Param("orderItemNumber") Long orderItemNum);

    List<Map> selectBySupplierPlatform(@Param("coreEpId") Integer coreEpId,
                                       @Param("saleCoreEpId") Integer saleCoreEpId,
                                       @Param("dateType") Integer dateType,
                                       @Param("startTime") Date startTime,
                                       @Param("endTime") Date endTime,
                                       @Param("orderStatus") Integer orderStatus,
                                       @Param("orderItemStatus") Integer orderItemStatus,
                                       @Param("phone") String phone,
                                       @Param("orderItemNumber") Long orderItemNum,
                                       @Param("self") Boolean self,
                                       @Param("productSubNumber") Long productSubNumber,
                                       @Param("record_start") Integer recordStart,
                                       @Param("record_count") Integer recordCount);

    int selectBySupplierPlatformCount(@Param("coreEpId") Integer coreEpId,
                                      @Param("saleCoreEpId") Integer saleCoreEpId,
                                      @Param("dateType") Integer dateType,
                                      @Param("startTime") Date startTime,
                                      @Param("endTime") Date endTime,
                                      @Param("orderStatus") Integer orderStatus,
                                      @Param("orderItemStatus") Integer orderItemStatus,
                                      @Param("phone") String phone,
                                      @Param("orderItemNumber") Long orderItemNum,
                                      @Param("self") Boolean self,
                                      @Param("productSubNumber") Long productSubNumber);

    int selectCountByGroup(@Param("groupId") Integer groupId);
}