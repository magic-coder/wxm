package com.all580.order.dao;

import com.all580.order.entity.Guide;

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
}