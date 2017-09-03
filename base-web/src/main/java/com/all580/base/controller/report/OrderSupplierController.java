package com.all580.base.controller.report;

import com.all580.ep.api.conf.EpConstant;
import com.all580.product.api.consts.ProductConstants;
import com.all580.report.api.service.EpOrderService;
import com.framework.common.BaseController;
import com.framework.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wxming on 2017/3/20 0020.
 */
@Controller
@RequestMapping("api/report/order")
public class OrderSupplierController extends BaseController {

    @Autowired
    private EpOrderService epOrderService;

    @RequestMapping(value = "supplier/reserve", method = RequestMethod.GET)
    @ResponseBody
    public Result selectSupplierOrderReserve(Integer ep_id,Integer pro_type,
                                             @RequestParam("statistic_ep_id") Integer statistic_ep_id,
                                             @RequestParam("statistics_type_id") String statistics_type_id,
                                             String start  ,String end,String pro_name,Integer group_id,
                                             String filename,String export_name,
                                             Integer record_start,Integer record_count){
        Map<String,Object> map = new HashMap<>();
        if(null==pro_type){
            pro_type= ProductConstants.ProductType.SCENERY;
        }
        map.put("ep_id",ep_id);
        map.put("pro_type",pro_type);
        map.put("statistic_ep_id",statistic_ep_id);
        map.put("key",statistics_type_id);
        map.put("start",start);
        map.put("end",end);
        map.put("group",group_id);
        map.put("pro_name",pro_name);
        map.put("export_name",export_name);
        map.put("filename",filename);
        map.put("record_start",record_start);
        map.put("record_count",record_count);
        map.put( EpConstant.EpKey.CORE_EP_ID,getAttribute( EpConstant.EpKey.CORE_EP_ID));
        return  epOrderService.selectSupplierOrderReserve(map);
    }

    @RequestMapping(value = "supplier/reserve/detail", method = RequestMethod.GET)
    @ResponseBody
    public Result selectSupplierOrderReserveDetail(Integer ep_id,Integer pro_type,
                                                   @RequestParam("statistic_ep_id") Integer statistic_ep_id,
                                                   String start  ,String end,Integer group_id,
                                                   String date_time  ,String condition,String pro_name,
                                                   String filename,String export_name,
                                                   Integer record_start,Integer record_count){
        Map<String,Object> map = new HashMap<>();
        if(null==pro_type){
            pro_type= ProductConstants.ProductType.SCENERY;
        }
        map.put("ep_id",ep_id);
        map.put("group",group_id);
        map.put("pro_type",pro_type);
        map.put("statistic_ep_id",statistic_ep_id);
        map.put("date_time",date_time);
        map.put("condition",condition);
        map.put("pro_name",pro_name);
        map.put("start",start);
        map.put("end",end);
        map.put("export_name",export_name);
        map.put("filename",filename);
        map.put("record_start",record_start);
        map.put("record_count",record_count);
        map.put( EpConstant.EpKey.CORE_EP_ID,getAttribute( EpConstant.EpKey.CORE_EP_ID));
        return  epOrderService.selectSupplierOrderReserveDetail(map);
    }

    @RequestMapping(value = "supplier/consume", method = RequestMethod.GET)
    @ResponseBody
    public Result selectSupplierOrderConsume(Integer ep_id,Integer pro_type,Integer group_id,
                                             @RequestParam("statistic_ep_id") Integer statistic_ep_id,
                                             @RequestParam("statistics_type_id") String statistics_type_id,
                                             String start  ,String end,String pro_name,
                                             String filename,String export_name,
                                             Integer record_start,Integer record_count){
        Map<String,Object> map = new HashMap<>();
        if(null==pro_type){
            pro_type= ProductConstants.ProductType.SCENERY;
        }
        map.put("ep_id",ep_id);
        map.put("group",group_id);
        map.put("pro_type",pro_type);
        map.put("statistic_ep_id",statistic_ep_id);
        map.put("key",statistics_type_id);
        map.put("start",start);
        map.put("end",end);
        map.put("pro_name",pro_name);
        map.put("export_name",export_name);
        map.put("filename",filename);
        map.put("record_start",record_start);
        map.put("record_count",record_count);
        map.put( EpConstant.EpKey.CORE_EP_ID,getAttribute( EpConstant.EpKey.CORE_EP_ID));
        return  epOrderService.selectSupplierOrderConsume(map);
    }

    @RequestMapping(value = "supplier/consume/detail", method = RequestMethod.GET)
    @ResponseBody
    public Result selectSupplierOrderConsumeDetail(Integer ep_id,Integer pro_type,
                                                   @RequestParam("statistic_ep_id") Integer statistic_ep_id,
                                                   String start  ,String end,Integer group_id,
                                                   String date_time  ,String condition,String pro_name,
                                                   String export_name,String filename,
                                                   Integer record_start,Integer record_count){
        Map<String,Object> map = new HashMap<>();
        if(null==pro_type){
            pro_type= ProductConstants.ProductType.SCENERY;
        }
        map.put("ep_id",ep_id);
        map.put("group",group_id);
        map.put("pro_type",pro_type);
        map.put("statistic_ep_id",statistic_ep_id);
        map.put("date_time",date_time);
        map.put("condition",condition);
        map.put("pro_name",pro_name);
        map.put("start",start);
        map.put("end",end);
        map.put("export_name",export_name);
        map.put("filename",filename);
        map.put("record_start",record_start);
        map.put("record_count",record_count);
        map.put( EpConstant.EpKey.CORE_EP_ID,getAttribute( EpConstant.EpKey.CORE_EP_ID));
        return  epOrderService.selectSupplierOrderConsumeDetail(map);
    }

    @RequestMapping(value = "supplier/refund", method = RequestMethod.GET)
    @ResponseBody
    public Result selectSupplierOrderRefund(Integer ep_id,Integer pro_type,
                                            @RequestParam("statistic_ep_id") Integer statistic_ep_id,
                                            @RequestParam("statistics_type_id") String statistics_type_id,
                                            String start  ,String end,Integer group_id,String pro_name,
                                            String export_name,String filename,
                                            Integer record_start,Integer record_count){
        Map<String,Object> map = new HashMap<>();
        if(null==pro_type){
            pro_type= ProductConstants.ProductType.SCENERY;
        }
        map.put("ep_id",ep_id);
        map.put("group",group_id);
        map.put("pro_type",pro_type);
        map.put("statistic_ep_id",statistic_ep_id);
        map.put("key",statistics_type_id);
        map.put("start",start);
        map.put("end",end);
        map.put("pro_name",pro_name);
        map.put("export_name",export_name);
        map.put("filename",filename);
        map.put("record_start",record_start);
        map.put("record_count",record_count);
        map.put( EpConstant.EpKey.CORE_EP_ID,getAttribute( EpConstant.EpKey.CORE_EP_ID));
        return  epOrderService.selectSupplierOrderRefund(map);
    }

    @RequestMapping(value = "supplier/refund/detail", method = RequestMethod.GET)
    @ResponseBody
    public Result selectSupplierOrderRefund(Integer ep_id,Integer pro_type,
                                            @RequestParam("statistic_ep_id") Integer statistic_ep_id,
                                            String start  ,String end,Integer group_id,
                                            String date_time  ,String condition,String pro_name,
                                            String filename,String export_name,
                                            Integer record_start,Integer record_count){
        Map<String,Object> map = new HashMap<>();
        if(null==pro_type){
            pro_type= ProductConstants.ProductType.SCENERY;
        }
        map.put("ep_id",ep_id);
        map.put("group",group_id);
        map.put("pro_type",pro_type);
        map.put("statistic_ep_id",statistic_ep_id);
        map.put("date_time",date_time);
        map.put("condition",condition);
        map.put("pro_name",pro_name);
        map.put("start",start);
        map.put("end",end);
        map.put("export_name",export_name);
        map.put("filename",filename);
        map.put("record_start",record_start);
        map.put("record_count",record_count);
        map.put( EpConstant.EpKey.CORE_EP_ID,getAttribute( EpConstant.EpKey.CORE_EP_ID));
        return  epOrderService.selectSupplierOrderRefundDetail(map);
    }
}
