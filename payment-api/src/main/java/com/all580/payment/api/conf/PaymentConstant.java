package com.all580.payment.api.conf;

import com.all580.payment.api.model.BalanceChangeInfo;

/**
 * 支付模块常量类
 * @author Created by panyi on 2016/10/8.
 */
public class PaymentConstant {

    /** 余额变动类型 */
    public static class BalanceChangeType{
        public static final Integer BALANCE_PAY = 7001; // 余额支付
        public static final Integer PAY_SPLIT = 7002; // 支付分账
        public static final Integer CONSUME_SPLIT = 7003; // 核销分账
        public static final Integer REVERSE_SPLIT = 7004; // 核销冲正分账
        public static final Integer REFUND_PAY = 7005; // 退票分账
        public static final Integer MANUAL_CHANGE_BALANCE = 7006; // 余额调整
    }
}