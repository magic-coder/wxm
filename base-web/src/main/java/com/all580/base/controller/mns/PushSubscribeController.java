package com.all580.base.controller.mns;

import com.alibaba.fastjson.JSONObject;
import com.all580.base.aop.MnsSubscribeAspect;
import com.all580.base.manager.PushMsgManager;
import com.all580.ep.api.service.EpPushService;
import com.all580.ep.api.service.EpService;
import com.all580.order.api.OrderConstant;
import com.framework.common.Result;
import com.framework.common.lang.JsonUtils;
import com.framework.common.util.CommonUtil;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.jobclient.JobClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.exception.ApiException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zhouxianjun(Alone)
 * @ClassName:
 * @Description: 推送MNS订阅
 * @date 2016/10/19 15:44
 */
@Controller
@RequestMapping("mns/subscribe")
@Slf4j
public class PushSubscribeController extends AbstractSubscribeController {
    @Autowired
    private EpPushService epPushService;
    @Value("${task.tracker}")
    private String taskTracker;

    @Value("${task.maxRetryTimes}")
    private Integer maxRetryTimes;

    @Autowired
    private JobClient jobClient;

    @Autowired
    private EpService epService;

    @Autowired
    private PushMsgManager pushMsgManager;

    @RequestMapping(value = "push", method = RequestMethod.POST)
    public void push(HttpServletResponse response, ModelMap model) {
        String id = (String) model.get(MnsSubscribeAspect.MSG_ID);
        String msg = (String) model.get(MnsSubscribeAspect.MSG);
        if (!StringUtils.hasText(msg)) {
            log.warn("推送信息:{} 推送信息为空", id);
            responseWrite(response, "OK");
            return;
        }
        Map map = JSONObject.parseObject(msg, Map.class);
        push(response, id, msg, map);
    }

    @RequestMapping(value = "manual", method = RequestMethod.POST)
    public void manualPush(HttpServletResponse response, HttpServletRequest request) throws Exception {
        String body = getReqParams(request);
        String sign = request.getHeader("sign");
        if (StringUtils.isEmpty(body) || StringUtils.isEmpty(sign) || !sign.equals(DigestUtils.md5Hex(body + "all580!@#$%^"))) {
            throw new ApiException("签名错误");
        }

        List list = JsonUtils.json2List(body);
        for (Object o : list) {
            try {
                push(response, "MANUAL", JsonUtils.toJson(o), (Map) o);
            } catch (Exception e) {
                log.warn("MANUAL PUSH ERROR", e);
            }
        }
    }

    private void push(HttpServletResponse response, String id, String msg, Map map) {
        String epId = CommonUtil.objectParseString(map.get("ep_id"));
        Integer sourceType=CommonUtil.objectParseInteger(map.get("source_type"));
        if(sourceType!=null&&sourceType- OrderConstant.OrderSourceType.SOURCE_TYPE_B2C==0){//b2c 来源
            //     根据企业来推送给平台
            Result result=epService.selectPlatformId(CommonUtil.objectParseInteger(epId));
            if (result.isFault()) throw new ApiException("B2C查询平台商异常");
            epId=result.get().toString();
        }
        log.info("推送 信息"+map.toString());
        try {
            responseWrite(response, "OK");
            Result<?> result = epPushService.selectByEpId(epId);
            if (result == null || !result.isSuccess()) {
                throw new ApiException("查询企业推送配置异常: " + id + ":" + (result == null ? "null" : result.getError()));
            }
            List list = pushMsgManager.getUrls(epId, id);
            if (list == null || list.isEmpty()) {
                log.warn("推送信息:{} 没有查询到配置", id);
                return;
            }
            List<Job> jobs = new ArrayList<>();
            for (Object o : list) {
                Map m = (Map) o;
                String url = CommonUtil.objectParseString(m.get("url"));
                if (StringUtils.isEmpty(url)) {
                    log.warn("推送信息:{} URL为空", id);
                    continue;
                }
                String type = CommonUtil.objectParseString(m.get("type"));
                // push
                log.debug("推送信息开始:{} URL:{} MSG:{}", new Object[]{id, url, msg});
                pushMsgManager.pushForAddJob(id, epId, url, type, msg, map, m, jobs);
            }
            if (!jobs.isEmpty() && jobs.size() > 0) {
                jobClient.submitJob(jobs);
            }
        } catch (Exception e) {
            log.error("MNS PUSH ERROR", e);
            jobClient.submitJob(pushMsgManager.addJob(id, msg, null, null, epId));
        }
    }
}
