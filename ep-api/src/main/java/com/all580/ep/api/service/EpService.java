package com.all580.ep.api.service;



import com.framework.common.Result;

import java.util.List;
import java.util.Map;

public interface EpService {

    /**
     * 创建平台商
     * @param ep
     * @return
     */
    Result<Map> createPlatform(Map ep);

    /**
     * 创建企业
     * @param map
     * @return
     */
    Result<Map> createEp(Map map);
    Result<List<Map>> select (Map map);

    /**
     * 获取企业基本信息接口
     *
     * @param epids    企业id
     * @param field    企业列    所传的值必须在一下列里面
    id
    name  企业名称
    en_name  企业英文名
    ep_type   10000-畅旅平台商10001平台商10002供应商10003销售商10004自营商10005OTA
    linkman    联系人
    link_phone  联系电话
    address   地址
    code   企业组织机构代码
    license  营业执照
    logo_pic  企业logo
    status  100初始化101-正常\n102-已冻结\n103-已停用
    access_id   运营平台接口访问标识
    access_key  运营平台接口访问密钥
    creator_ep_id    上级企业
    core_ep_id   所属平台商企业id
    add_time
    status_bak    ' 冻结/停用平台商操作时企业当前的状态
    province  省
    city  市
    area  区
    group_id  组ID
    group_name  组名称
    ep_class   10010;//景区10011;//酒店10012;//旅行社10013;//其他
     * @return
     */
    Result<List<Map>>  getEp(Integer [] epids, String[] field);

    Result<List<Map>> all(Map params);

    /**
     * 验证平台商
     * @param params
     * @return
     */
    Result<Map> validate(Map params);
    /**
     * 获取企业状态（包括上级企业）
     *100-未初始化101-正常\n102-已冻结\n103-已停用
     * @param id
     * @return
     */
    Result<Integer> getEpStatus(Integer id);

    /**
     * 根据企业id查询平台商id
     * @param epId
     * @return
     */
    Result<Integer>  selectPlatformId(Integer epId);



    /**
     * 冻结停用企业
     * @param params
     * @return
     */
    Result<Integer> freeze(Map params);
    Result<Integer> disable(Map params);
    Result<Integer> enable(Map params);

    Result<Integer> platformFreeze(Map params);
    Result<Integer> platformDisable(Map params);
    Result<Integer> platformEnable(Map params);
    //    Result<List<Ep>> selectEp(Map map);
    Result<Map> updateEp(Map map);

    /**
     * 下游平台商列表接口
     * @param map
     * @return
     */
    Result<Map> platformListDown(Map map);

    /**
     * 下游
     * @param map
     * @return
     */
    Result<Map> platformListUp(Map map);

    /**
     * 查找创建企业id
     * @param id
     * @return   未找到 -1  平台商0   上级企业id
     */
    Result<Integer> selectCreatorEpId(Integer id);
}