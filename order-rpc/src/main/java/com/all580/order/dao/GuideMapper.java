package com.all580.order.dao;

import com.all580.order.entity.Guide;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface GuideMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_guide
     *
     * @mbggenerated Thu Dec 01 11:25:59 CST 2016
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_guide
     *
     * @mbggenerated Thu Dec 01 11:25:59 CST 2016
     */
    int insert(Guide record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_guide
     *
     * @mbggenerated Thu Dec 01 11:25:59 CST 2016
     */
    int insertSelective(Guide record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_guide
     *
     * @mbggenerated Thu Dec 01 11:25:59 CST 2016
     */
    Guide selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_guide
     *
     * @mbggenerated Thu Dec 01 11:25:59 CST 2016
     */
    int updateByPrimaryKeySelective(Guide record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_guide
     *
     * @mbggenerated Thu Dec 01 11:25:59 CST 2016
     */
    int updateByPrimaryKey(Guide record);

    /**
     * 统计导游数量
     * @param name 团号
     * @param phone 导游姓名
     * @param card 出团开始时间
     * @return
     */
    int countGuideList(@Param("ep_id") Integer core_ep_id,@Param("name") String name, @Param("phone") String phone, @Param("card") String card);

    /**
     * 查询导游列表
     * @param name 团号
     * @param phone 导游姓名
     * @param card 出团开始时间
     * @return
     */
    List<Map> queryGuideList(@Param("ep_id") Integer core_ep_id,@Param("name") String name, @Param("phone") String phone, @Param("card") String card, @Param("record_start") Integer record_start , @Param("record_count") Integer record_count);

    Map queryGuideById(@Param("id") Integer id);

    /**
     * 查询订单团队导游
     * @param orderId 订单ID
     * @return
     */
    List<Map> queryOrderGuideByOrderId(@Param("orderId") Integer orderId);
}