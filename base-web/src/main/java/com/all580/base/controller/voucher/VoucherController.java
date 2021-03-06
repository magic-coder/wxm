package com.all580.base.controller.voucher;

import com.all580.base.manager.VoucherValidateManager;
import com.all580.base.util.Utils;
import com.all580.ep.api.conf.EpConstant;
import com.all580.voucher.api.service.VoucherRPCService;
import com.all580.voucherplatform.api.service.OrderService;
import com.framework.common.BaseController;
import com.framework.common.Result;
import com.framework.common.util.CommonUtil;
import com.framework.common.validate.ParamsMapValidate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author zhouxianjun(Alone)
 * @ClassName:
 * @Description: 凭证网关接口
 * @date 2016/10/19 19:51
 */
@Controller
@RequestMapping("api/voucher")
@Slf4j
public class VoucherController extends BaseController {
    @Autowired
    private VoucherValidateManager voucherValidateManager;

    @Autowired
    private VoucherRPCService voucherRPCService;
    @Autowired
    @Qualifier("voucherOrderService")
    private OrderService voucherOrderService;

    @RequestMapping(value = "add", method = RequestMethod.POST)
    @ResponseBody
    public Result add(@RequestBody Map params) {
        // 验证参数
        ParamsMapValidate.validate(params, voucherValidateManager.addValidate());
        return voucherRPCService.addVoucher(params.get("name").toString(), params.get("link").toString(), (String) params.get("memo"), (String) params.get("service"));
    }

    @RequestMapping(value = "get/id")
    @ResponseBody
    public Result getId(@RequestParam Integer id) {
        return voucherRPCService.selectVoucherById(id);
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    @ResponseBody
    public Result update(@RequestBody Map params) {
        // 验证参数
        ParamsMapValidate.validate(params, voucherValidateManager.updateValidate());
        int id = Integer.parseInt(params.get("id").toString());
        return voucherRPCService.updateVoucher(id, params.get("name").toString(), params.get("link").toString(), (String) params.get("memo"), (String) params.get("service"));
    }

    @RequestMapping(value = "merchant/add", method = RequestMethod.POST)
    @ResponseBody
    public Result addMerchantToVoucher(@RequestBody Map params) {
        // 验证参数
        ParamsMapValidate.validate(params, voucherValidateManager.merchantValidate());
        return voucherRPCService.bindMerchant(CommonUtil.objectParseInteger(getAttribute(EpConstant.EpKey.CORE_EP_ID)), params);
    }

    @RequestMapping(value = "list")
    @ResponseBody
    public Result list(@RequestParam(defaultValue = "0") Integer record_start, @RequestParam(defaultValue = "20") Integer record_count) {
        return voucherRPCService.selectVoucherForList(record_start, record_count);
    }

    @RequestMapping(value = "merchant/list")
    @ResponseBody
    public Result merchantList(Integer ep_id, Integer record_start, Integer record_count) {
        return voucherRPCService.selectVoucherOfMerchantForList(ep_id, record_start, record_count);
    }

    @RequestMapping(value = "product/list")
    @ResponseBody
    public Result productList(@RequestParam Integer ep_ma_id) {
        Result result = voucherRPCService.selectTicketProduct(ep_ma_id);
        if (!result.isSuccess()) {
            return result;
        }
        List list = (List) result.getExt("data");
        result.put(list);
        return result;
    }

    @RequestMapping(value = "provider/list")
    @ResponseBody
    public Result providerList(@RequestParam("ep_id") Integer epId) {
        return voucherRPCService.selectSelfProviderList(epId);
    }

    @RequestMapping(value = "sync/consume", method = RequestMethod.POST)
    @ResponseBody
    public Result syncConsume(@RequestBody Map params) {
        // 验证参数
        ParamsMapValidate.validate(params, voucherValidateManager.syncConsumeValidate());
        return voucherRPCService.syncConsume(Integer.parseInt(params.get("ep_ma_id").toString()), params);
    }

    @RequestMapping(value = "sync/consume/list")
    @ResponseBody
    public Result selectConsumeSync(@RequestParam String[] auths,
                                    @RequestParam String start_time,
                                    @RequestParam String end_time,
                                    Integer record_start, Integer record_count) {
        Date[] dates = Utils.checkDate(start_time, end_time);
        return voucherOrderService.selectConsumeSyncByPage(Arrays.asList(auths), dates[0], dates[1], record_start, record_count);
    }
}
