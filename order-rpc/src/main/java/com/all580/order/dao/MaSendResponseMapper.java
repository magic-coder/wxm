package com.all580.order.dao;

import com.all580.order.entity.MaSendResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MaSendResponseMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_ma_send_response
     *
     * @mbggenerated Wed Nov 02 16:23:33 CST 2016
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_ma_send_response
     *
     * @mbggenerated Wed Nov 02 16:23:33 CST 2016
     */
    int insert(MaSendResponse record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_ma_send_response
     *
     * @mbggenerated Wed Nov 02 16:23:33 CST 2016
     */
    int insertSelective(MaSendResponse record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_ma_send_response
     *
     * @mbggenerated Wed Nov 02 16:23:33 CST 2016
     */
    MaSendResponse selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_ma_send_response
     *
     * @mbggenerated Wed Nov 02 16:23:33 CST 2016
     */
    int updateByPrimaryKeySelective(MaSendResponse record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_ma_send_response
     *
     * @mbggenerated Wed Nov 02 16:23:33 CST 2016
     */
    int updateByPrimaryKey(MaSendResponse record);

    /**
     * 根据子订单ID获取凭证列表
     * @param itemId 子订单ID
     * @return
     */
    List<MaSendResponse> selectByOrderItemId(Integer itemId);

    /**
     * 根据子订单ID和凭证订单ID获取凭证
     * @param itemId 子订单ID
     * @param maId 凭证出票ID
     * @param epMaId 凭证ID
     * @return
     */
    MaSendResponse selectByOrderItemIdAndMaId(@Param("itemId") Integer itemId, @Param("maId") String maId, @Param("epMaId") Integer epMaId);

    /**
     * 根据子订单ID和游客ID获取凭证
     * @param itemId 子订单ID
     * @param visitorId 游客ID
     * @param epMaId 凭证ID
     * @return
     */
    MaSendResponse selectByVisitorId(@Param("itemId") Integer itemId, @Param("visitorId") Integer visitorId, @Param("epMaId") Integer epMaId);
}