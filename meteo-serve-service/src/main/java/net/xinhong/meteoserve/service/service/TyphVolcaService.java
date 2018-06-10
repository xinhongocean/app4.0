package net.xinhong.meteoserve.service.service;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by xiaoyu on 16/6/28.
 */
public interface TyphVolcaService {

    /**
     * 获取指定时间的前30天且未消亡的台风信息
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute

     * @return
     */
    JSONObject getTyphData(String year, String month, String day, String hour, String minute, String sDayNum, boolean isShowFinish);


    /**
     * 查询给定时间范围台风的最后编报时间,用于客户端判断是否为未查看的台风信息使用
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return
     */
    JSONObject getTyphLastTimeData(String year, String month, String day, String hour, String minute, String dayNum);

    /**
     * 获取指定时间
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute

     * @return
     */
    JSONObject getVolcaData(String year, String month, String day, String hour, String minute);

}
