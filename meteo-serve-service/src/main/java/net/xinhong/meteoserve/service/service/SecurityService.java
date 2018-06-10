package net.xinhong.meteoserve.service.service;

import com.alibaba.fastjson.JSONObject;
import org.joda.time.DateTime;

/**
 * Created by xiaoyu on 16/9/20.
 */
public interface SecurityService {

    /**
     * 存储用户信息
     * @param userPID-用户终端唯一识别ID 必须填写
     * @param clientType-用户终端类型(Android/iOS/H5)
     * @param clientVersion-//用户App版本,两位或三位的数字,例如: 1.2/1.3.1/2.0 必须填写
     * @param operateType-//操作类型,包括:lanch/login/logout/enterbackgroud/enterforeground/terminate
     * @param sLat-经度
     * @param sLng-纬度
     * @return
     */
    JSONObject saveClientinfo(String userPID, String clientType, String clientVersion, String operateType,
                              String sLat, String sLng);


    /**
     * 查询给定日期前dayNum天用户活跃信息（结果按照用户方式返回）
     * @param year
     * @param month
     * @param day
     * @param dayNum
     * @return
     */
    JSONObject searchClientinfo(String year, String month, String day, String dayNum);


    /**
     * 查询给定日期前dayNum天用户活跃信息（结果按照经纬度方式返回）
     * @param year
     * @param month
     * @param day
     * @param strDayNum
     * @param delayseconds--不为空，且>0，则只查询给定秒数以内活跃的用户信息
     * @return
     */
    JSONObject searchClientinfoLatlng(String year, String month, String day, String strDayNum, String delayseconds);


    /**
     * 存储用户反馈信息
     * @param userPID-用户终端唯一识别ID 必须填写
     * @param clientType-用户终端类型(Android/iOS/H5)
     * @param clientVersion-//用户App版本,两位或三位的数字,例如: 1.2/1.3.1/2.0 必须填写
     * @param desc-反馈问题描述 必须填写
     * @param phonenum-电话号码 可选
     * @param picpath-反馈图片路径(图片上传成功后的路径)
     * @return
     */
    JSONObject saveFedbackInfo(String userPID, String clientType, String clientVersion, String desc, String phonenum, String picpath, String email);

    /**
     * 获取Key
     * @param userPID
     * @param clientType
     * @param clientVersion
     * @param sLat
     * @param sLng
     * @return
     */
    JSONObject getKey(String userPID, String clientType, String clientVersion, String sLat, String sLng);


    /**
     * 获取最新的APP版本号
     * @return
     */
    JSONObject getLatestVersion();
}
