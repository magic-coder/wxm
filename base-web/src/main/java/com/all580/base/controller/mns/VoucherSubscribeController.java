package com.all580.base.controller.mns;

import com.alibaba.fastjson.JSONObject;
import com.all580.voucher.api.service.VoucherCallbackService;
import com.framework.common.BaseController;
import com.framework.common.Result;
import com.framework.common.lang.DateFormatUtils;
import com.framework.common.validate.ParamsMapValidate;
import com.framework.common.validate.ValidRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouxianjun(Alone)
 * @ClassName:
 * @Description: 凭证MNS订阅
 * @date 2016/10/19 15:44
 */
@Controller
@RequestMapping("mns/subscribe")
@Slf4j
public class VoucherSubscribeController extends BaseController {
    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping(value = "voucher", method = RequestMethod.POST)
    public void voucher(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 请求合法性校验, json串的key必须是带双引号的
            String mnsMsgId = request.getHeader("x-mns-message-id");
            String tag = request.getHeader("x-mns-message-tag");
            log.info("MNS MESSAGE ID: {}, TAG: {}", mnsMsgId, tag);
            // 获取请求参数
            String msg = getReqParams(request);
            // 处理消息
            log.info("MNS 消息: {}", msg);
            if (StringUtils.hasText(msg)) {
                Map map = JSONObject.parseObject(msg, Map.class);
                validate(map);
                String action = map.get("action").toString();
                VoucherCallbackService service = applicationContext.getBean(action + "Service", VoucherCallbackService.class);
                if (service == null) {
                    throw new Exception("action is not found");
                }
                String content = map.get("content").toString();
                Date createTime = DateFormatUtils.converToDateTime(map.get("createTime").toString());
                Result result = service.process(mnsMsgId, content, createTime);
                if (result.hasError()) {
                    throw new Exception(result.getError());
                }
            }
            responseWrite(response, "OK");
        } catch (Exception e) {
            log.error("MNS VOUCHER ERROR", e);
            response.setStatus(500);
        }
    }

    private void validate(Map params) {
        Map<String[], ValidRule[]> validRuleMap = new HashMap<>();
        validRuleMap.put(new String[]{"action", "content", "createTime"}, new ValidRule[]{new ValidRule.NotNull()});
        validRuleMap.put(new String[]{"createTime"}, new ValidRule[]{new ValidRule.Date()});
        ParamsMapValidate.validate(params, validRuleMap);
    }
}