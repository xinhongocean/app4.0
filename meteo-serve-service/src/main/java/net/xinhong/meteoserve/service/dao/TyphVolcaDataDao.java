package net.xinhong.meteoserve.service.dao;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by xiaoyu on 16/7/1.
 * 台风及火山灰读取DAO
 */
public interface TyphVolcaDataDao {

    /**
     * 给定时间,取该时间前interMinutes内台风数据
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @param interMinutes
     * @return
     */
    JSONObject getTyphData(String year, String month, String day, String hour, String minute, int interMinutes, boolean isShowFinish);


    /**
     * 查询给定时间范围台风的最后编报时间,用于客户端判断是否为未查看的台风信息使用
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @param interMinutes
     * @return
     */
    JSONObject getTyphLastTimeData(String year, String month, String day, String hour, String minute, int interMinutes);

    JSONObject getVolcaData(String year, String month, String day, String hour, String minute, int interMinutes);

}
