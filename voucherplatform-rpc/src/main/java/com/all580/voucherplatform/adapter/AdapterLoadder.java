package com.all580.voucherplatform.adapter;

import com.all580.voucherplatform.adapter.platform.PlatformAdapterService;
import com.all580.voucherplatform.adapter.supply.SupplyAdapterService;
import com.all580.voucherplatform.dao.PlatformMapper;
import com.all580.voucherplatform.dao.TicketSysMapper;
import com.all580.voucherplatform.entity.Platform;
import com.all580.voucherplatform.entity.Supply;
import com.all580.voucherplatform.entity.TicketSys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Linv2 on 2017-06-08.
 */
@Service
@Slf4j
public class AdapterLoadder {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private TicketSysMapper ticketSysMapper;
    @Autowired
    private PlatformMapper platformMapper;
    private Map<Integer, SupplyAdapterService> supplyServiceMap = new HashMap<>();
    private Map<Integer, PlatformAdapterService> platformSerivceMap = new HashMap<>();


    private synchronized SupplyAdapterService loadTicketSystem(Integer ticketSysId) {
        if (!supplyServiceMap.containsKey(ticketSysId)) {
            TicketSys ticketSys = ticketSysMapper.selectByPrimaryKey(ticketSysId);
            if (ticketSys != null) {
                Class cls = null;
                try {
                    cls = Class.forName(ticketSys.getImplPacket());
                } catch (Exception ex) {
                    log.error("票务{}--{}加载适配器失败", ticketSysId, ticketSys.getImplPacket());
                    return null;
                }
                SupplyAdapterService supplyAdapterService = (SupplyAdapterService) applicationContext.getBean(cls);
                if (supplyAdapterService != null) {
                    supplyServiceMap.put(ticketSysId, supplyAdapterService);
                }
                return supplyAdapterService;
            }
            return null;
        } else {
            return supplyServiceMap.get(ticketSysId);
        }
    }

    private synchronized PlatformAdapterService loadPlatform(Integer platformId) {
        if (!platformSerivceMap.containsKey(platformId)) {
            Platform platform = platformMapper.selectByPrimaryKey(platformId);
            if (platform != null) {
                Class cls = null;
                try {
                    cls = Class.forName(platform.getImplPackage());
                } catch (Exception ex) {
                    log.error("平台商{}--{}加载适配器失败", platform.getName(), platform.getImplPackage());
                    return null;
                }
                PlatformAdapterService platformAdapterService = (PlatformAdapterService) applicationContext.getBean(cls);
                if (platformAdapterService != null) {
                    platformSerivceMap.put(platformId, platformAdapterService);
                }
                return platformAdapterService;
            }
            return null;
        } else {
            return platformSerivceMap.get(platformId);
        }
    }

    public PlatformAdapterService getPlatformAdapterService(Platform platform) {
        if (!platformSerivceMap.containsKey(platform.getId())) {
            return loadPlatform(platform.getId());
        } else {
            return platformSerivceMap.get(platform.getId());
        }
    }

    public PlatformAdapterService getPlatformAdapterService(Integer platformId) {
        if (!platformSerivceMap.containsKey(platformId)) {
            return loadPlatform(platformId);
        } else {
            return platformSerivceMap.get(platformId);
        }
    }

    public SupplyAdapterService getSupplyAdapterService(Supply supply) {
        if (!supplyServiceMap.containsKey(supply.getTicketsys_id())) {
            return loadTicketSystem(supply.getTicketsys_id());
        } else {
            return supplyServiceMap.get(supply.getTicketsys_id());
        }
    }

    public SupplyAdapterService getSupplyAdapterService(Integer ticketSysId) {
        if (!supplyServiceMap.containsKey(ticketSysId)) {
            return loadTicketSystem(ticketSysId);
        } else {
            return supplyServiceMap.get(ticketSysId);
        }
    }

    public <T> T getBean(Class<T> cls) {
        return applicationContext.getBean(cls);
    }
}
