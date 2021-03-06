package com.all580.ep.service;

import com.all580.ep.api.conf.EpConstant;
import com.all580.ep.api.service.CoreEpChannelService;
import com.all580.ep.api.service.EpBalanceThresholdService;
import com.all580.ep.com.Common;
import com.all580.ep.dao.CoreEpChannelMapper;
import com.all580.ep.dao.EpMapper;
import com.all580.manager.SyncEpData;
import com.all580.payment.api.service.BalancePayService;
import com.framework.common.Result;
import javax.lang.exception.ApiException;
import com.framework.common.util.CommonUtil;
import com.framework.common.validate.ValidRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
@Transactional(rollbackFor = {Exception.class, RuntimeException.class})
@Slf4j
public class CoreEpChannelServiceImpl implements CoreEpChannelService {

    @Autowired
    private CoreEpChannelMapper coreEpChannelMapper;

    @Autowired
    private BalancePayService balancePayService;

    @Autowired
    private EpMapper epMapper;

    @Autowired
    private EpBalanceThresholdService epBalanceThresholdService;

    @Autowired
    private SyncEpData syncEpData;

    @Override   //// TODO: 2016/10/11 0011   上游下游供销关系同步 数据
    public Result<Integer> create(Map<String,Object> params) {
        Result<Integer> result = new Result<>();

        try {
            //     销售
            Integer epId= Common.objectParseInteger(params.get("seller_core_ep_id"));
            Integer coreEpId = Common.objectParseInteger(params.get("supplier_core_ep_id"));
            if(coreEpChannelMapper.selectChannel(params)>0){
                throw new ApiException("通道汇率已经存在");
            }
            if(epId-coreEpId==0){
                throw new ApiException("不需要添加自己给自己的供应");
            }
            result.put(coreEpChannelMapper.create(params));
            result.setSuccess();
            balancePayService.createBalanceAccount(epId,coreEpId);//添加钱包
            params.put("id",epId);
            params.put("core_ep_id",coreEpId);
            params.put("isChannel","true");
            epBalanceThresholdService.createOrUpdate(params);//添加余额阀值
            //同步数据   把销售平台商 同步到供应平台商
                Map<String, Object> tempMap = new HashMap<>();
                tempMap.put("id", epId);
                List<Map<String, String>> listMap = epMapper.selectSingleTable(tempMap);
                Map<String,Object> syncData=  syncEpData.syncEpData(coreEpId, EpConstant.Table.T_EP, listMap);
                result.setSuccess();
                result.putExt(Result.SYNC_DATA, syncData);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ApiException(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public Result<Integer> update(Map<String,Object> params) {
        Result<Integer> result = new Result<>();
        try {
            result.put(coreEpChannelMapper.update(params));
            result.setSuccess();
        } catch (Exception e) {
           log.error("数据库修改出错", e);
            throw new ApiException("数据库修改出错", e);
        }
        return result;
    }

    @Override
    public Result<Integer> cancel(Integer id) {
        Result<Integer> result = new Result<>();
        try {
            result.put(coreEpChannelMapper.cancel(id));
            result.setSuccess();
        } catch (Exception e) {
            log.error("添加汇率通道参数错误", e);
            throw new ApiException("添加汇率通道参数错误", e);
        }
        return result;
    }


    @Override  //EpChannelRep
    public Result<List<Map<String,Object>>> selectSupplierCoreEpId() {
        Result<List<Map<String,Object>>> result = new Result<>();
        try {
            result.put(coreEpChannelMapper.selectSupplierCoreEpId());
            result.setSuccess();
        } catch (Exception e) {
            log.error("数据库查询出错", e);
            throw new ApiException("数据库查询出错", e);
        }
        return result;
    }
    @Override
    public Result<Map<String, Object>> selectById(int id) {
        Result<Map<String,Object>> result = new Result<>();
        try {
            result.put(coreEpChannelMapper.selectById(id));
            result.setSuccess();
        } catch (Exception e) {
            log.error("数据库查询出错", e);
            throw new ApiException("数据库查询出错", e);
        }
        return result;
    }
    @Override  //EpChannelRep
    public Result<Map<String,Object>> select(Map<String,Object> params) {
        Map<String,Object> resultMap = new HashMap<>();
        Result<Map<String,Object>> result = new Result<>();
        try {
            CommonUtil.checkPage(params);
            resultMap.put("list",coreEpChannelMapper.select(params));
            resultMap.put("totalCount",coreEpChannelMapper.selectCount(params));
            result.put(resultMap);
            result.setSuccess();
        } catch (Exception e) {
           log.error("数据库查询出错", e);
            throw new ApiException("数据库查询出错", e);
        }
        return result;
    }
   @Override
   public Result<Integer> selectPlatfromRate(int supplier_core_ep_id,int seller_core_ep_id){
       Result result = new Result(true);
       result.put(coreEpChannelMapper.selectPlatfromRate(supplier_core_ep_id,seller_core_ep_id));
     return result;
    }

//    private Map<String[], ValidRule[]> generateCreateEpChannelValidate() {
//        Map<String[], ValidRule[]> rules = new HashMap<>();
//        // 校验不为空的参数
//        rules.put(new String[]{
//                "supplier_core_ep_id", // '供应商id
//                "seller_core_ep_id", // '销售商id
//                "rate", // 费率
//        }, new ValidRule[]{new ValidRule.NotNull()});
//
//        // 校验整数
//        rules.put(new String[]{
//                "supplier_core_ep_id", //
//                "seller_core_ep_id", //
//        }, new ValidRule[]{new ValidRule.Digits()});
//        return rules;
//    }
//
//    private Map<String[], ValidRule[]> generateUpdateEpChannelValidate() {
//        Map<String[], ValidRule[]> rules = new HashMap<>();
//        // 校验不为空的参数
//        rules.put(new String[]{
//                "id", //
//                "rate", // 费率
//        }, new ValidRule[]{new ValidRule.NotNull()});
//
//        // 校验整数
//        rules.put(new String[]{
//                "id", //
//        }, new ValidRule[]{new ValidRule.Digits()});
//        return rules;
//    }
}
