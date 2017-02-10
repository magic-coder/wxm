package com.all580.base.controller.product.hotel;

import com.all580.ep.api.conf.EpConstant;
import com.all580.product.api.hotel.service.HotelService;
import com.framework.common.Result;
import com.framework.common.lang.DateFormatUtils;
import com.framework.common.validate.ParamsMapValidate;
import com.framework.common.validate.ValidRule;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.lang.exception.ParamsMapValidationException;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wxming on 2017/1/17 0017.
 */
@Controller
@RequestMapping(value = "api/product/hotel")
public class HotelController {

    @Autowired
    private HotelService hotelService;
    @RequestMapping(value = "create", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> addHotel(@RequestBody Map params) {
        ParamsMapValidate.validate(params, generateCreateHotelValidate());
        return hotelService.addHotel(params);
    }

    @RequestMapping(value = "select_hotel_name", method = RequestMethod.GET)
    @ResponseBody
    public Result<?> selectHotelName(HttpServletRequest request, String product_name,String ep_id,  Integer record_start,
                                     Integer record_count) {
        Map<String,Object> map = new HashMap();
        map.put("product_name",product_name);
        map.put("ep_id",ep_id);
        map.put("record_start", record_start);
        map.put("record_count", record_count);
        map.put(EpConstant.EpKey.CORE_EP_ID, request.getAttribute(EpConstant.EpKey.CORE_EP_ID));
        return hotelService.selectHotelName(map);
    }

    @RequestMapping(value = "can_sale/list")
    @ResponseBody
    public Result<?> canSaleList(@RequestParam Integer ep_id,
                                 Integer city, String intDate, String outDate, String keyword,
                                 @RequestParam(defaultValue = "0") Integer priceMin, Integer priceMax,
                                 String star, String topic, @RequestParam(defaultValue = "0") Integer personMin,
                                 Integer personMax, @RequestParam(defaultValue = "ASC") String priceSort,
                                 @RequestParam(defaultValue = "ASC") String saleSort,
                                 @RequestParam(defaultValue = "ASC") String createSort,
                                 @RequestParam(defaultValue = "0") Integer record_start,
                                 @RequestParam(defaultValue = "20") Integer record_count) throws Exception {
        Date start_time = DateFormatUtils.converToDate(intDate);
        Date end_time = DateFormatUtils.converToDate(outDate);
        String[] sorts = new String[]{priceSort, saleSort, createSort};
        for (String sort : sorts) {
            if (!sort.equalsIgnoreCase("asc")) {
                throw new ParamsMapValidationException("排序只能为 asc 或 desc");
            }
        }
        return hotelService.selectCanSaleList(ep_id, city, start_time, end_time, keyword, priceMin, priceMax,
                star, topic, personMin, personMax, priceSort, saleSort, createSort, record_start, record_count);
    }



    public Map<String[], ValidRule[]> generateCreateHotelValidate() {
        Map<String[], ValidRule[]> rules = new HashMap<>();
        // 校验不为空的参数
        rules.put(new String[]{
                "name", //
                "team_num", //
                "province", //
                "city", //
                "area", //
                "address", //
                "level_type", //
                "tel", //
                "invoice", //
                "imgs", //
                "server_type", //
                "facility_type", //
                "depot_type", //
                "card_type", //
                "pay_type", //
                "pet_type", //
                "name", //
        }, new ValidRule[]{new ValidRule.NotNull()});

        // 校验整数
        rules.put(new String[]{
                "team_num" ,// 企业id
                "province" ,// 企业id
                "city" ,// 企业id
                "area" ,// 企业id
                "level_type" ,// 企业id
                "invoice" ,// 企业id
                "pet_type" ,// 企业id
        }, new ValidRule[]{new ValidRule.Digits()});
        return rules;
    }
}
