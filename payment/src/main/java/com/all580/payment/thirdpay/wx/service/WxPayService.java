package com.all580.payment.thirdpay.wx.service;

import com.all580.payment.thirdpay.wx.client.ClientResponseHandler;
import com.all580.payment.thirdpay.wx.client.RequestHandler;
import com.all580.payment.thirdpay.wx.client.ResponseHandler;
import com.all580.payment.thirdpay.wx.client.TenpayHttpClient;
import com.all580.payment.thirdpay.wx.model.*;
import com.all580.payment.thirdpay.wx.util.ConstantUtil;
import com.all580.payment.thirdpay.wx.util.WXUtil;
import com.framework.common.Result;
import com.framework.common.lang.DateFormatUtils;
import com.framework.common.lang.JsonUtils;
import com.framework.common.net.IPUtils;
import com.framework.common.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.lang.exception.ApiException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author panyi on 2016/10/13.
 * @since V0.0.1
 */
@Service("wxPayService")
public class WxPayService {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    private Map<Integer, WxProperties> wxPropertiesMap = new HashMap<>();
    @Value("${base.url}")
    private String domain;
    @Value("${wx.pay.callback.url}")
    private String payCallbackUrl;

    @Autowired
    private AccessToken accessToken;

    public String reqPay(long ordCode, int coreEpId, Map<String, Object> params) throws Exception {
        return reqPay(ordCode, coreEpId, ConstantUtil.NATIVE_TRADE_TYPE, params).getCode_url();

    }

    public UnifiedOrderRsp reqPay(long ordCode, int coreEpId, String tradeType, Map<String, Object> params) throws Exception {
        WxProperties wxProperties = wxPropertiesMap.get(coreEpId);
        // payCallbackUrl = "http://core.py.ngrok.wendal.cn/no_auth_api/callback/wx/payment";
        UnifiedOrderReq req = new UnifiedOrderReq();
        req.setAppid(wxProperties.getApp_id());
        req.setMch_id(wxProperties.getMch_id());
        req.setNonce_str(WXUtil.getNonceStr());
        req.setDevice_info("");
        req.setTotal_fee(String.valueOf(params.get("totalFee")));
        //透传参数
        req.setAttach("" + coreEpId);
        req.setTrade_type(tradeType);
        req.setProduct_id(String.valueOf(params.get("prodId")));
        req.setSpbill_create_ip(IPUtils.getRealIp(true));
        req.setFee_type("CNY");
        req.setNotify_url(domain + payCallbackUrl);
        req.setBody(String.valueOf(params.get("prodName")));
        Date now = new Date();
        req.setTime_start(DateFormatUtils.parseDateToDatetimeWXString(now));
        Date afterDate = new Date(now .getTime() + 1000*60*15);
        req.setTime_expire(DateFormatUtils.parseDateToDatetimeWXString(afterDate));
        // req.setGoods_tag();
        req.setOut_trade_no(String.valueOf(ordCode));
        req.setOpenid(CommonUtil.objectParseString(params.get("openid")));
        // req.setSign();

        UnifiedOrderRsp rsp = this.request(ConstantUtil.UNIFIEDORDER, req, UnifiedOrderRsp.class, false, wxProperties);
        rsp.setKey(wxProperties.getMch_key());
        return rsp;
    }

    // 申请退款
    public RefundRsp reqRefund(long ordCode, Map<String, Object> params, int coreEpId) throws Exception {
        WxProperties wxProperties = wxPropertiesMap.get(coreEpId);
        RefundReq req = new RefundReq();
        req.setTransaction_id("");
        req.setOut_trade_no(String.valueOf(ordCode));
        req.setOut_refund_no(String.valueOf(params.get("serialNum")));
        req.setTotal_fee((Integer) params.get("totalFee"));
        req.setRefund_fee((Integer) params.get("refundFee"));
        req.setRefund_fee_type("CNY");
        req.setAppid(wxProperties.getApp_id());
        req.setMch_id(wxProperties.getMch_id());
        req.setNonce_str(WXUtil.getNonceStr());
        req.setOp_user_id(wxProperties.getMch_id());
        req.setDevice_info("");

        return this.request(ConstantUtil.REFUND, req, RefundRsp.class, true, wxProperties);
    }

    public Result<Map<String, String>> payCallback(Map<String, String> params, int coreEpId) {
        Result<Map<String, String>> result = new Result<>();
        WxProperties wxProperties = wxPropertiesMap.get(coreEpId);
        ResponseHandler resHandler = new ResponseHandler(params);
        resHandler.setKey(wxProperties.getMch_key());
        Map<String, String> rsp = new HashMap<>();
        // 判断签名
        if (resHandler.isTenpaySign()) {
            if (null != resHandler.getParameter(ConstantUtil.RETURN_CODE) && resHandler.
                    getParameter(ConstantUtil.RETURN_CODE).equals(ConstantUtil.SUCCESS)) {
                rsp.put("return_code", "SUCCESS");
                rsp.put("return_msg", "OK");
                result.setSuccess();
            } else {
                rsp.put("return_code", "FAIL");
                result.setFail();
            }
        } else {
            rsp.put("return_code", "FAIL");
            result.setFail();
        }
        result.put(rsp);
        return result;
    }

    public AccessTokenBean getAccessToken(Integer coreEpId) {
        WxProperties wxProperties = wxPropertiesMap.get(coreEpId);
        return accessToken.getForWeb(wxProperties);
    }

    public Map getOpenid(Integer coreEpId, String code) {
        WxProperties wxProperties = wxPropertiesMap.get(coreEpId);
        TenpayHttpClient client = new TenpayHttpClient();
        client.setIsHttps(true);
        client.setMethod("get");
        client.setReqContent(String.format("%s?grant_type=authorization_code&appid=%s&secret=%s&code=%s", ConstantUtil.ACCESS_TOKEN_SECOND, wxProperties.getApp_id(), wxProperties.getApp_secret(), code));
        client.setWxProperties(wxProperties);
        if (!client.call()){
            logger.warn("微信获取openid通信异常: {}-{}", client.getResponseCode(), client.getErrInfo());
            throw new RuntimeException("微信获取openid通信失败");
        }
        String response = client.getResContent();
        logger.info("微信获取openid返回: {}", response);
        Map map = JsonUtils.json2Map(response);
        if (map == null || !map.containsKey("openid")) {
            throw new RuntimeException("微信获取openid失败");
        }
        return map;
    }
    public   <T> T request(String url, CommonsReq req, Class<T> cls, boolean certBool,Integer coreEpId ) throws Exception {
        WxProperties wxProperties = wxPropertiesMap.get(coreEpId);
       return  this.request(url,req,cls,certBool,wxProperties);
    }
    /**
     * @param url
     * @param req
     * @param cls
     * @param certBool 是否需要证书
     * @param <T>
     * @return
     * @throws Exception
     */
    private   <T> T request(String url, CommonsReq req, Class<T> cls, boolean certBool, WxProperties wxProperties) throws Exception {
        // 创建请求对象
        RequestHandler queryReq = new RequestHandler(null, null);
        // 通信对象
        TenpayHttpClient httpClient = new TenpayHttpClient();
        // 应答对象
        ClientResponseHandler queryRes = new ClientResponseHandler();

        // 通过通知ID查询，确保通知来至财付通
        queryReq.init();
        queryReq.setKey(wxProperties.getMch_key());
        queryReq.setGateUrl(url);

        req.setAppid(wxProperties.getApp_id());
        req.setMch_id(wxProperties.getMch_id());
        req.setNonce_str(WXUtil.getNonceStr());

        Class clss = req.getClass();
        for (; clss != Object.class; clss = clss.getSuperclass()) {
            Field[] fields = clss.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    if (null != field.get(req)) {
                        queryReq.setParameter(field.getName(), field.get(req).toString());
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        // 通信对象
//        httpClient.setTimeOut(5);
        // 设置请求内容
        httpClient.setReqContent(queryReq.getRequestURL());
        httpClient.setIsHttps(certBool); // 退款使用https
        httpClient.setWxProperties(wxProperties);
        T entity = cls.newInstance();

        // if (certBool) {
        // httpClient.setCaInfo(new File(wxProperties.getRootca_pem()));
        // httpClient.setCertInfo(new File(wxProperties.getApiClientCertP12Str()), wxProperties.getMchId());
        // }

        // 后台调用
        if (httpClient.call()) {
            String returnResult = httpClient.getResContent();
            logger.info("微信返回结果：" + returnResult);
            queryRes.setContent(returnResult);
            SortedMap map = queryRes.getAllParameters();
            // 验证是否失败
            if (map == null) throw new ApiException("请求微信异常");
            if (!map.get(ConstantUtil.RETURN_CODE).equals(ConstantUtil.SUCCESS) || !map.get(ConstantUtil.RESULT_CODE).equals(ConstantUtil.SUCCESS)) {
                String returnMsg = CommonUtil.objectParseString(map.get(ConstantUtil.RETURN_MSG));
                String errorMsg = CommonUtil.objectParseString(map.get(ConstantUtil.ERR_CODE_DES));
                throw new ApiException("微信返回:" + (StringUtils.isEmpty(returnMsg) ? errorMsg : returnMsg));
            }
            for (Object o : map.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                Class clsss = entity.getClass();
                for (; clsss != Object.class; clsss = clsss.getSuperclass()) {
                    Field[] fields = clsss.getDeclaredFields();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        try {
                            if (null != entry && entry.getKey().equals(field.getName())) {
                                if (field.getType().equals(Integer.class)) {
                                    field.set(entity, entry.getValue() != null ? Integer.valueOf(entry.getValue().toString()) : 0);
                                } else {
                                    field.set(entity, entry.getValue());
                                }
                            }
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            e.printStackTrace();
                            logger.error("请求微信封装返回异常:" + e.getMessage(), e);
                        }
                    }
                }
            }
        } else {
            logger.info("后台调用通信失败");
            logger.info("" + httpClient.getResponseCode());
            logger.info(httpClient.getErrInfo());
            throw new Exception("微信后台调用通信失败:" + httpClient.getResponseCode());
        }
        return entity;
    }

    public boolean isPropertiesInit(int coreEpId) {
        return wxPropertiesMap.containsKey(coreEpId);
    }

    public void clear(int coreEpId) {
         wxPropertiesMap.remove(coreEpId);
    }
    public synchronized void initProperties(int coreEpId, String confData, String certP12) {
        WxProperties wxProperties = JsonUtils.fromJson(confData, WxProperties.class);
        wxProperties.setCore_ep_id(coreEpId);
        wxProperties.setApi_client_cert_p12_str(certP12);
        wxPropertiesMap.put(coreEpId, wxProperties);
    }
}
