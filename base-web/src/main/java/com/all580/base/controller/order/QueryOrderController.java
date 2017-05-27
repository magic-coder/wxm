package com.all580.base.controller.order;

import com.all580.base.util.Utils;
import com.all580.ep.api.conf.EpConstant;
import com.all580.report.api.service.QueryOrderService;
import com.framework.common.BaseController;
import com.framework.common.Result;
import com.framework.common.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * @author zhouxianjun(Gary)
 * @ClassName:
 * @Description: 订单查询网关
 * @date 17-5-26 上午9:35
 */
@Controller
@RequestMapping("api/order/query")
@Slf4j
public class QueryOrderController extends BaseController {
    @Autowired
    private QueryOrderService queryOrderService;

    @RequestMapping(value = "line/list")
    @ResponseBody
    public Result<?> listLine(@RequestParam Integer ep_type,
                                          String start_date,
                                          String end_date,
                                          Integer status,
                                          Integer item_status,
                                          String product_name,
                                          String group_number,
                                          String name,
                                          String phone,
                                          Long number,
                                          @RequestParam(defaultValue = "0") Integer record_start,
                                          @RequestParam(defaultValue = "20") Integer record_count) {
        Integer coreEpId = CommonUtil.objectParseInteger(getAttribute(EpConstant.EpKey.CORE_EP_ID));
        Integer epId = CommonUtil.objectParseInteger(getAttribute(EpConstant.EpKey.EP_ID));
        Date[] dates = Utils.checkDate(start_date, end_date);
        return queryOrderService.queryLineOrderItemList(number, product_name, group_number, status, item_status, name,
                phone, ep_type, coreEpId, epId, dates[0], dates[1], record_start, record_count);
    }
}