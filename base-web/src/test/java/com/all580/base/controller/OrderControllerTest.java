package com.all580.base.controller;

import com.all580.order.api.OrderConstant;
import com.framework.common.lang.JsonUtils;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * @author zhouxianjun(Alone)
 * @ClassName:
 * @Description:
 * @date 2016/10/11 14:15
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class OrderControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.wac).build();
    }

    @Test
    public void createEp()throws Exception{
        Map params = new HashMap();
        params.put("id",1);
        mockMvc.perform(
                post("/api/ep/platform/status/disable").contentType(MediaType.APPLICATION_JSON).
                        content(JsonUtils.toJson(params))
        ).andExpect(
                status().isOk()
        ).andExpect(
                jsonPath("$.code", is(200))
        ).andDo(print());
    }
    @Test
    public void createTest() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>(){{
            put("shipping", new HashMap<String, Object>(){{
                put("name", "周先军");
                put("phone", "15019418143");
            }});
            put("items", new ArrayList<Map>(){{
                add(new HashMap<String, Object>(){{
                    put("visitor", new ArrayList<Map<String, Object>>(){{
                        add(new HashMap<String, Object>(){{
                            put("name", "Alone");
                            put("phone", "15019418143");
                            put("sid", "511702197403222585");
                            put("quantity", "1");
                        }});
                    }});
                    put("product_sub_id", "1");
                    put("start", "2016-10-22 00:00:00");
                    put("days", "1");
                    put("quantity", "1");
                }});
            }});
            put("from", OrderConstant.FromType.NON_TRUST);
            put("ep_id", "54");
            put("operator_id", "1");
            put("operator_name", "xxx");
            put("sale_amount", "0");
            put("remark", "test");
        }};
        mockMvc.perform(
                    post("/api/order/create").contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(params))
                ).andExpect(
                    status().isOk()
                ).andExpect(
                    jsonPath("$.code", is("200"))
                ).andDo(print());
    }

    @Test
    public void createAuditTest() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>(){{
            put("shipping", new HashMap<String, Object>(){{
                put("name", "周先军");
                put("phone", "15019418143");
            }});
            put("items", new ArrayList<Map>(){{
                add(new HashMap<String, Object>(){{
                    put("visitor", new ArrayList<Map<String, Object>>(){{
                        add(new HashMap<String, Object>(){{
                            put("name", "Alone");
                            put("phone", "15019418143");
                            put("sid", "511702197403222585");
                            put("quantity", "1");
                        }});
                    }});
                    put("product_sub_id", "2");
                    put("start", "2016-10-22 00:00:00");
                    put("days", "1");
                    put("quantity", "1");
                }});
            }});
            put("from", OrderConstant.FromType.NON_TRUST);
            put("ep_id", "54");
            put("operator_id", "1");
            put("operator_name", "xxx");
            put("sale_amount", "0");
            put("remark", "test");
        }};
        mockMvc.perform(
                post("/api/order/create").contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(params))
        ).andExpect(
                status().isOk()
        ).andExpect(
                jsonPath("$.code", is("200"))
        ).andDo(print());
    }

    @Test
    public void paymentBalancesTest() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>(){{
            put("order_sn", "1477309875089380");
            put("pay_type", "7111");
        }};
        mockMvc.perform(
                    post("/api/order/payment").contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(params))
                ).andExpect(
                    status().isOk()
                ).andExpect(
                    jsonPath("$.code", is("200"))
                ).andDo(print());
    }
}
