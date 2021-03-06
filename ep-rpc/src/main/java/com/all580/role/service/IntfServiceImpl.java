package com.all580.role.service;

import com.all580.ep.api.conf.EpConstant;
import com.all580.ep.com.Common;
import com.all580.manager.SyncEpData;
import com.all580.role.api.service.IntfService;
import com.all580.role.dao.EpRoleFuncMapper;
import com.all580.role.dao.IntfMapper;
import com.framework.common.Result;
import com.framework.common.io.cache.redis.RedisUtils;
import com.framework.common.util.Auth;
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
 * Created by wxming on 2016/11/10 0010.
 */
@Service
@Transactional(rollbackFor = {Exception.class, RuntimeException.class})
@Slf4j
public class IntfServiceImpl implements IntfService {

    @Autowired
    private IntfMapper intfMapper;

    @Autowired
    private SyncEpData syncEpData;

    @Autowired
    private EpRoleFuncMapper epRoleFuncMapper;

    @Autowired
    private RedisUtils redisUtils;



    public Result<List<String>> authCoreIntf(int CoreEpId){
        Result<List<String>> result=  new Result(true);
        try {
            result.put(intfMapper.authCoreIntf(CoreEpId));
        } catch (Exception e) {
            log.error("添加接口异常", e);
            throw  new ApiException("添加接口异常");
        }
        return result;
    }

    public Result<List<String>> authIntf(int ep_id,int core_id_id){
        Result<List<String>> result=  new Result(true);
        try {
            result.put(intfMapper.authIntf(ep_id,core_id_id));
        } catch (Exception e) {
            log.error("添加接口异常", e);
            throw  new ApiException("添加接口异常");
        }
        return result;
    }
    @Override
    public Result insertInft(Map<String, Object> params) {
        Result result=  new Result(true);
        try {
            intfMapper.insertIntf(params);
            Integer id = CommonUtil.objectParseInteger(params.get("id"));
            result.put(id);
            syncEpData.syncEpAllData(EpConstant.Table.T_INTF,intfMapper.selectByPrimaryKey(id));
            //  更新鉴权数据
            Integer func_id = CommonUtil.objectParseInteger(params.get("func_id"));
           // List<Integer> list= epRoleFuncMapper.selectFuncIdAllEpRole(func_id);
            // 查询出有这个菜单的平台 更新平台下有这个菜单的所有企业，
            //更新有这个菜单的所有企业
            Auth.updateAuthMap(null,redisUtils);
        } catch (Exception e) {
            log.error("添加接口异常", e);
            throw  new ApiException("添加接口异常");
        }
        return result;
    }
    @Override
    public Result selectFuncId(Map<String,Object> params) {
        Result result= new Result(true);
        Map<String,Object> resultMap = new HashMap<>();
        try {
            int ref= intfMapper.selectFuncIdCount(params);
            resultMap.put("totalCount",ref);
            if(ref<1){
                resultMap.put("list",new ArrayList<>());
                result.put(resultMap);
                return  result;
            }
            Common.checkPage(params);
            List list =intfMapper.selectFuncId(params);
            resultMap.put("list",list);
            result.put(resultMap);
        }catch (Exception e){
            log.error("查询接口列表出错 {}",e.getMessage());
            throw   new ApiException("查询接口列表出错");
        }
        return result;
    }
    @Override
    public Result intfList(Map<String,Object> params){
        Result result= new Result(true);
        Map<String,Object> resultMap = new HashMap<>();
        try {
            int ref= intfMapper.intListCount();
            resultMap.put("totalCount",ref);
            if(ref<1){
                resultMap.put("list",new ArrayList<>());
                result.put(resultMap);
                return  result;
            }
            Common.checkPage(params);
            List list =intfMapper.intfList(params);
            resultMap.put("list",list);
            result.put(resultMap);
        }catch (Exception e){
            log.error("查询接口列表出错 {}",e.getMessage());
            throw   new ApiException("查询接口列表出错");
        }
        return result;
    }
    @Override
    public Result deleteInft(int id) {
        try {
            // funcIntfMapper.deleteFuncIntf(id);
            Integer func_id =CommonUtil.objectParseInteger(intfMapper.selectByPrimaryKey(id).get("func_id")) ;
           // List<Integer> list= epRoleFuncMapper.selectFuncIdAllEpRole(func_id);
            Auth.updateAuthMap( null,redisUtils);
            intfMapper.deleteByPrimaryKey(id);
            syncEpData.syncDeleteAllData(EpConstant.Table.T_INTF,id);
        } catch (Exception e) {
            log.error("删除接口异常", e);
            throw    new ApiException("删除接口异常");
        }
        return new Result(true);
    }
}
