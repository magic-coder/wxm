package com.all580.order;

import com.all580.order.api.OrderConstant;
import com.all580.order.dto.AccountDataDto;
import com.all580.order.dto.GenerateAccountDto;
import com.all580.order.entity.OrderItemAccount;
import com.all580.product.api.consts.ProductConstants;
import com.all580.product.api.model.EpSalesInfo;
import com.framework.common.lang.JsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhouxianjun(Alone)
 * @ClassName:
 * @Description:
 * @date 2016/10/9 19:38
 */
public class AccountTest {
    public static void main(String[] args) {
        List<List<EpSalesInfo>> daySalesList = new ArrayList<List<EpSalesInfo>>(){{
            add(new ArrayList<EpSalesInfo>(){{
                EpSalesInfo a1 = new EpSalesInfo();
                a1.setSaleEpId(101);
                a1.setBuyEpId(102);
                a1.setPrice(5);
                add(a1);

                EpSalesInfo a2 = new EpSalesInfo();
                a2.setSaleEpId(102);
                a2.setBuyEpId(103);
                a2.setPrice(9);
                add(a2);

                EpSalesInfo a3 = new EpSalesInfo();
                a3.setSaleEpId(103);
                a3.setBuyEpId(201);
                a3.setPrice(15);
                add(a3);

                EpSalesInfo b1 = new EpSalesInfo();
                b1.setSaleEpId(201);
                b1.setBuyEpId(301);
                b1.setPrice(21);
                add(b1);

                EpSalesInfo c1 = new EpSalesInfo();
                c1.setSaleEpId(301);
                c1.setBuyEpId(302);
                c1.setPrice(35);
                add(c1);

                EpSalesInfo c2 = new EpSalesInfo();
                c2.setSaleEpId(302);
                c2.setBuyEpId(303);
                c2.setPrice(38);
                add(c2);

                EpSalesInfo c3 = new EpSalesInfo();
                c3.setSaleEpId(303);
                c3.setBuyEpId(-1);
                c3.setPrice(45);
                add(c3);
            }});
        }};

        ooxx(daySalesList, 1, 1, 5011);
    }

    public static void ooxx(List<List<EpSalesInfo>> daySalesList, int itemId, int quantity, int payType) {
        Map<Integer, Integer> coreEpMap = new HashMap<Integer, Integer>(){{
            put(101, 103);
            put(102, 103);
            put(103, 103);
            put(201, 201);
            put(301, 301);
            put(302, 301);
            put(303, 301);
        }};
        // 每个企业所有天的利润
        Map<Integer, List<AccountDataDto>> daysAccountDataMap = new HashMap<>();
        // 叶子销售商最终卖价
        int salePrice = 0;
        // 遍历每天的销售链
        for (List<EpSalesInfo> infoList : daySalesList) {
            // 一天的利润单价
            Map<Integer, AccountDataDto> dayAccountDataMap = new HashMap<>();
            for (EpSalesInfo info : infoList) {
                Integer buyCoreEpId = null;
                // 叶子销售商门市价销售
                if (info.getBuyEpId() == -1) {
                    buyCoreEpId = getCoreEp(coreEpMap, info.getSaleEpId());
                    salePrice = info.getPrice();
                } else {
                    buyCoreEpId = getCoreEp(coreEpMap, info.getBuyEpId());
                }
                Integer saleCoreEpId = getCoreEp(coreEpMap, info.getSaleEpId());
                // 卖家 == 平台商 && 买家 == 平台商
                if (info.getBuyEpId() == buyCoreEpId && saleCoreEpId == info.getSaleEpId()) {
                    AccountDataDto dto = addDayAccount(dayAccountDataMap, infoList, buyCoreEpId);
                    dto.setSaleCoreEpId(saleCoreEpId);
                    addDayAccount(dayAccountDataMap, infoList, saleCoreEpId);
                }

                // 买家平台商ID == 卖家平台商ID && 卖家ID != 卖家平台商ID
                if (buyCoreEpId.intValue() == saleCoreEpId && info.getSaleEpId() != saleCoreEpId) {
                    AccountDataDto dto = addDayAccount(dayAccountDataMap, infoList, info.getSaleEpId());
                    dto.setSaleCoreEpId(saleCoreEpId);
                }
            }

            // 把一天的利润添加到所有天当中
            for (Integer epId : dayAccountDataMap.keySet()) {
                List<AccountDataDto> accountDataDtoList = daysAccountDataMap.get(epId);
                if (accountDataDtoList == null) {
                    accountDataDtoList = new ArrayList<>();
                    daysAccountDataMap.put(epId, accountDataDtoList);
                }
                accountDataDtoList.add(dayAccountDataMap.get(epId));
            }
        }

        // 把每天的利润集合做分账
        for (Integer epId : daysAccountDataMap.keySet()) {
            Integer coreEpId = getCoreEp(coreEpMap, epId);
            List<AccountDataDto> dataDtos = daysAccountDataMap.get(epId);
            int totalInPrice = 0;
            int totalProfit = 0;
            for (AccountDataDto dataDto : dataDtos) {
                totalInPrice += dataDto.getInPrice();
                totalProfit += dataDto.getProfit();
            }
            GenerateAccountDto accountDto = new GenerateAccountDto();
            // 平台商之间分账(进货价)
            if (coreEpId.intValue() == epId) {
                AccountDataDto dto = dataDtos.get(0);
                if (dto != null && dto.getSaleCoreEpId() != null) {
                    int totalAddProfit = 0;
                    // 卖家平台商每天的利润
                    List<AccountDataDto> saleAccountDataDtoList = daysAccountDataMap.get(dto.getSaleCoreEpId());
                    for (AccountDataDto dataDto : saleAccountDataDtoList) {
                        totalAddProfit += dataDto.getProfit();
                    }
                    // 预付
                    if (payType == ProductConstants.PayType.PREPAY) {
                        accountDto.setSubtractEpId(epId); // 买家 扣钱
                        accountDto.setSubtractCoreId(dto.getSaleCoreEpId()); // 在卖家的平台商
                        accountDto.setAddEpId(dto.getSaleCoreEpId()); // 卖家的平台商收钱
                        accountDto.setAddCoreId(dto.getSaleCoreEpId());
                        accountDto.setMoney(totalInPrice * quantity); // 金额 进货价 * 票数
                        accountDto.setSubtractProfit(totalProfit * quantity); // 买家每天的总利润 * 票数
                        accountDto.setAddProfit(totalAddProfit * quantity); // 卖家每天的总利润 * 票数
                        accountDto.setOrderItemId(itemId);
                        accountDto.setSubtractData(dataDtos); // 买家每天的单价利润
                        accountDto.setAddData(saleAccountDataDtoList); // 卖家每天的单价利润
                    } else {
                        // 到付
                        accountDto.setSubtractEpId(dto.getSaleCoreEpId()); // 卖家 扣钱
                        accountDto.setSubtractCoreId(dto.getSaleCoreEpId()); // 在卖家的平台商
                        accountDto.setAddEpId(epId); // 买家的平台商收钱
                        accountDto.setAddCoreId(dto.getSaleCoreEpId()); // 在卖家平台商的余额
                        accountDto.setMoney((salePrice - totalInPrice) * quantity); // 金额 卖价 * 票数
                        accountDto.setSubtractProfit(totalAddProfit * quantity); // 卖家每天的总利润 * 票数
                        accountDto.setAddProfit(totalProfit * quantity); // 买家每天的总利润 * 票数
                        accountDto.setOrderItemId(itemId);
                        accountDto.setSubtractData(saleAccountDataDtoList); // 卖家每天的单价利润
                        accountDto.setAddData(dataDtos); // 买家每天的单价利润
                    }
                }

            } else {
                // 平台内部企业分账(利润)
                accountDto.setSubtractEpId(coreEpId); // 平台商扣钱给企业分利润
                accountDto.setSubtractCoreId(coreEpId);
                accountDto.setAddEpId(epId); // 收钱企业ID
                accountDto.setAddCoreId(coreEpId);
                accountDto.setMoney(totalProfit * quantity); // 金额 每天的总利润 * 票数
                accountDto.setSubtractProfit(0); // 平台商的利润在平台商之间分账中体现
                accountDto.setAddProfit(totalProfit * quantity); // 买家每天的总利润 * 票数
                accountDto.setOrderItemId(itemId);
                accountDto.setSubtractData(null); // 平台商的利润在平台商之间分账中体现
                accountDto.setAddData(dataDtos); // 每天的单价利润
            }

            if (accountDto.getMoney() != 0) {
                System.out.println(JsonUtils.toJson(generateAccount(accountDto)));
            }
        }
    }

    private static Integer getCoreEp(Map<Integer, Integer> coreEpMap, Integer epId) {
        return coreEpMap.get(epId);
    }

    private static AccountDataDto addDayAccount(Map<Integer, AccountDataDto> dayAccountDataMap, List<EpSalesInfo> infoList, Integer epId) {
        AccountDataDto accountDataDto = dayAccountDataMap.get(epId);
        if (accountDataDto == null) {
            accountDataDto = getProfit(infoList, epId);
            dayAccountDataMap.put(epId, accountDataDto);
        }
        return accountDataDto;
    }

    public static AccountDataDto getProfit(List<EpSalesInfo> salesInfoList, int epId) {
        int salePrice = 0;
        int buyPrice = 0;
        for (EpSalesInfo info : salesInfoList) {
            if (info.getSaleEpId() == epId) {
                salePrice = info.getPrice();
            }
            if (info.getBuyEpId() == epId) {
                buyPrice = info.getPrice();
            }
        }
        return new AccountDataDto(salePrice, buyPrice, salePrice - buyPrice, null);
    }

    public static List<OrderItemAccount> generateAccount(GenerateAccountDto dto) {
        List<OrderItemAccount> accounts = new ArrayList<>();
        OrderItemAccount subtractAccount = new OrderItemAccount();
        subtractAccount.setEpId(dto.getSubtractEpId());
        subtractAccount.setCoreEpId(dto.getSubtractCoreId());
        subtractAccount.setOrderItemId(dto.getOrderItemId());
        subtractAccount.setMoney(-dto.getMoney());
        subtractAccount.setProfit(dto.getSubtractProfit());
        subtractAccount.setSettledMoney(0);
        subtractAccount.setStatus(OrderConstant.AccountSplitStatus.NOT);
        subtractAccount.setData(dto.getSubtractData() == null ? null : JsonUtils.toJson(dto.getSubtractData()));
        accounts.add(subtractAccount);

        OrderItemAccount addAccount = new OrderItemAccount();
        addAccount.setEpId(dto.getAddEpId());
        addAccount.setCoreEpId(dto.getAddCoreId());
        addAccount.setOrderItemId(dto.getOrderItemId());
        addAccount.setMoney(dto.getMoney());
        addAccount.setProfit(dto.getAddProfit());
        addAccount.setSettledMoney(0);
        addAccount.setStatus(OrderConstant.AccountSplitStatus.NOT);
        addAccount.setData(dto.getAddData() == null ? null : JsonUtils.toJson(dto.getAddData()));
        accounts.add(addAccount);

        return accounts;
    }
}
