package com.all580.role.service;

import com.all580.ep.com.Common;
import com.all580.role.api.service.EpRoleService;
import com.all580.role.dao.EpRoleFuncMapper;
import com.all580.role.dao.EpRoleMapper;
import com.framework.common.Result;
import com.framework.common.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.lang.exception.ApiException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wxming on 2016/11/11 0011.
 */
@Service
@Transactional(rollbackFor = {Exception.class, RuntimeException.class})
@Slf4j
public class EpRoleServiceImpl implements EpRoleService {

    @Autowired
    private EpRoleMapper epRoleMapper;

    @Autowired
    private EpRoleFuncMapper epRoleFuncMapper;

    @Override
    public Result addEpRole(Map<String, Object> params) {
        Result result = new Result(true);
        try {
            int ref = epRoleMapper.checkName(params);
            if (ref > 0) {
                log.error("角色名字已存在 {}", params.get("name"));
                throw new ApiException("角色名字已存在");
            }
            epRoleMapper.insertSelective(params);
            Integer ep_role_id = CommonUtil.objectParseInteger(params.get("id"));
            result.put(ep_role_id);
        } catch (ApiException e1) {
            throw new ApiException(e1.getMessage());
        } catch (Exception e) {
            log.error("添加角色出错 {}", e.getMessage());
            return new Result(false, e.getMessage());
        }
        return result;  //addEpRoleFunc
    }

    @Override
    public Result select(Integer id) {
        Result result = new Result(true);
        try {
            result.put(epRoleMapper.select(id));
        } catch (Exception e) {
            log.error("查询角色出错 {}", e.getMessage());
            throw new ApiException("查询角色出错");
        }
        return result;
    }

    @Override
    public Result updateEpRole(Map<String, Object> params) {
        try {
            int ref = epRoleMapper.checkName(params);
            if (ref > 0) {
                log.error("角色名字已存在 {}", params.get("name"));
                return new Result(false, "角色名字已存在");
            }
            epRoleMapper.updateByPrimaryKeySelective(params);
        } catch (Exception e) {
            log.error("修改角色出错 {}", e.getMessage());
            return new Result(false, e.getMessage());
        }
        return new Result(true);
    }

    @Override
    public Result selectepRoleId(int ep_role_id) {
        Result result = new Result(true);
        try {
            result.put(epRoleFuncMapper.selectepRoleId(ep_role_id));
        } catch (Exception e) {
            log.error("查询角色出错 {}", e.getMessage());
            return new Result(false, e.getMessage());
        }
        return result;
    }

    @Override
    public Result selectRoleFunc(int ep_role_id) {
        Result result = new Result(true);
        try {
            result.put(epRoleFuncMapper.selectRoleFunc(ep_role_id));
        } catch (Exception e) {
            log.error("修改角色出错 {}", e.getMessage());
            return new Result(false, e.getMessage());
        }
        return result;
    }

    @Override
    public Result selectList(Map<String, Object> params) {
        Result result = new Result(true);
        Map<String, Object> resultMap = new HashMap<>();
        try {
            int ref = epRoleMapper.selectListCount();
            resultMap.put("totalCount", ref);
            if (ref < 1) {
                resultMap.put("list", new ArrayList<>());
                result.put(resultMap);
                return result;
            }
            Common.checkPage(params);
            List list = epRoleMapper.selectList(params);
            resultMap.put("list", list);
            result.put(resultMap);
        } catch (Exception e) {
            log.error("查询角色列表出错 {}", e.getMessage());
            throw new ApiException("查询角色列表出错");
        }
        return result;
    }

    @Override
    public Result addEpRoleFunc(Map<String, Object> params) {
        Result result = new Result(true);
        try {
            Integer ep_role_id = CommonUtil.objectParseInteger(params.get("ep_role_id"));
            Integer oper_id = CommonUtil.objectParseInteger(params.get("oper_id"));
            List<Integer> func_ids = (List<Integer>) params.get("func_ids");
            epRoleFuncMapper.insertEpRoleFuncBatch(ep_role_id, oper_id, func_ids);
        } catch (Exception e) {
            log.error("添加菜单出错 {}", e.getMessage());
            return new Result(false, e.getMessage());
        }
        return result;  //
    }


    @Override
    public Result updateEpRoleFunc(Map<String, Object> params) {
        try {
            Integer ep_role_id = CommonUtil.objectParseInteger(params.get("ep_role_id"));//角色
            Integer oper_id = CommonUtil.objectParseInteger(params.get("oper_id"));//操作人
            List<Integer> func_ids = (List<Integer>) params.get("func_ids");
            epRoleFuncMapper.updateEpRoleFuncIsDelete(ep_role_id, oper_id);//更新原始的 为delete状态
            List<Integer> initFunc = epRoleFuncMapper.selectEpRoleIdFuncId(ep_role_id, func_ids);
            if (!(null == initFunc || initFunc.isEmpty())) {//添加的已经存在 功能 修改为正常
                epRoleFuncMapper.updateEpRoleFuncIsNotDelete(ep_role_id, initFunc);
                removeAllList(func_ids,initFunc);
            }
            if(!(null == func_ids || func_ids.isEmpty())){
                epRoleFuncMapper.insertEpRoleFuncBatch(ep_role_id, oper_id, func_ids);
            }

        } catch (Exception e) {
            log.error("修改菜单出错 {}", e.getMessage());
            return new Result(false, e.getMessage());
        }
        return new Result(true);
    }

    /**
     * removeAll 前端传的类型不一致   删除
     * @param func_ids
     * @param initFunc
     */
    private void removeAllList(List<Integer> func_ids,List<Integer> initFunc) {
           for(int i= func_ids.size()-1;i>-1;i--){
               Integer id=CommonUtil.objectParseInteger( func_ids.get(i));
               for(Integer temp :initFunc){
                   if(temp.equals(id)){
                       func_ids.remove(i);
                       break;
                   }
               }
           }
    }

}