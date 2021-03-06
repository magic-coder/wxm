package com.all580.voucherplatform.manager;

import com.framework.common.validate.ValidRule;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Linv2 on 2017-07-18.
 */
@Component
public class DeviceValidateManager {
    public Map<String[], ValidRule[]> createValidate() {
        Map<String[], ValidRule[]> rules = new HashMap<>();
        // 校验不为空的参数
        rules.put(new String[]{
                "name",
                "supply_id"
        }, new ValidRule[]{new ValidRule.NotNull()});
        rules.put(new String[]{"supply_id"}, new ValidRule[]{new ValidRule.Digits()});
        return rules;
    }

    public Map<String[], ValidRule[]> createDeviceValidate() {
        Map<String[], ValidRule[]> rules = new HashMap<>();
        // 校验不为空的参数
        rules.put(new String[]{
                "name",
                "code",
                "device_group_id"
        }, new ValidRule[]{new ValidRule.NotNull()});
        rules.put(new String[]{"device_group_id"}, new ValidRule[]{new ValidRule.Digits()});
        return rules;
    }

    public Map<String[], ValidRule[]> applyDeviceValidate() {
        Map<String[], ValidRule[]> rules = new HashMap<>();
        // 校验不为空的参数
        rules.put(new String[]{
                "deviceId"
        }, new ValidRule[]{new ValidRule.NotNull()});
        return rules;
    }

    public Map<String[], ValidRule[]> reqDeviceValidate() {
        Map<String[], ValidRule[]> rules = new HashMap<>();
        // 校验不为空的参数
        rules.put(new String[]{
                "action",
                "content",
                "identity"
        }, new ValidRule[]{new ValidRule.NotNull()});
        return rules;
    }

    public Map<String[], ValidRule[]> deviceAuditValidate() {
        Map<String[], ValidRule[]> rules = new HashMap<>();
        // 校验不为空的参数
        rules.put(new String[]{
                "applyId"
        }, new ValidRule[]{new ValidRule.NotNull()});
        rules.put(new String[]{
                "applyId",
                "groupId"
        }, new ValidRule[]{new ValidRule.Digits()});
        return rules;

    }

    public Map<String[], ValidRule[]> renameDeviceValidate() {
        Map<String[], ValidRule[]> rules = new HashMap<>();
        // 校验不为空的参数
        rules.put(new String[]{
                "id",
                "name"
        }, new ValidRule[]{new ValidRule.NotNull()});

        rules.put(new String[]{"id"}, new ValidRule[]{new ValidRule.Digits()});
        return rules;
    }
}
