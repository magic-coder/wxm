package com.all580.role.dao;


import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface PlatFuncMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_plat_func
     *
     * @mbggenerated Wed Jun 28 17:40:48 CST 2017
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_plat_func
     *
     * @mbggenerated Wed Jun 28 17:40:48 CST 2017
     */
    int insert(Map record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_plat_func
     *
     * @mbggenerated Wed Jun 28 17:40:48 CST 2017
     */
    int insertSelective(Map record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_plat_func
     *
     * @mbggenerated Wed Jun 28 17:40:48 CST 2017
     */
    Map selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_plat_func
     *
     * @mbggenerated Wed Jun 28 17:40:48 CST 2017
     */
    int updateByPrimaryKeySelective(Map record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table t_plat_func
     *
     * @mbggenerated Wed Jun 28 17:40:48 CST 2017
     */
    int updateByPrimaryKey(Map record);

    int addPlatFuncList(@Param("core_ep_id") int core_ep_id, @Param("list") List<Integer> list);
    int deletePlatGroup(@Param("core_ep_id") int core_ep_id, @Param("group_ids") List<Integer> group_ids);
    List<Integer> selectPlatGroup(@Param("core_ep_id") int core_ep_id);
    List<Integer> selectPlatGroupList(@Param("core_ep_id") int core_ep_id, @Param("group_ids") List<Integer> group_ids);
    List<Map> selectPlatGroupListAll(@Param("core_ep_id") int core_ep_id, @Param("group_ids") List<Integer> group_ids);
    List<Map> selectPlatGroupListMap(@Param("core_ep_id") int core_ep_id, @Param("group_ids") List<Integer> group_ids);

    /**
     * 根据菜单查询出  平台商id
     * @param func_ids
     * @return
     */
    List<Integer> selectCoreEpIdFunc(@Param("func_ids") List<Integer> func_ids);
    List<Integer> selectCoreEpIdGroupId(Integer func_group_id);
}