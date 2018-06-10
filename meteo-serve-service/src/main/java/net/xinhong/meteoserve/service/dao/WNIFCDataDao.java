package net.xinhong.meteoserve.service.dao;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by xiaoyu on 16/4/19.
 * WNI预报数据读取Dao
 */
public interface WNIFCDataDao {

    /**
     * 获取某个点指定时间及预报时效的危险天气数据
     * @param strLat
     * @param strLng
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param VTI
     * @return
     */
    JSONObject getPointDangerData(String strLat, String strLng, String year, String month, String day, String hour, String VTI);

    /**
     * 获取wni 区域危险天气
     * @param path
     * @param el
     * @return
     */
    JSONObject getAreaData(String path,String el);

    /**
     * 获取等值线数据
     * @param freeArea EN东北半球
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param VTI
     * @param level
     * @return
     */
    JSONObject getIsoLineData(String freeArea, String year, String month,
                                 String day, String hour, String VTI,String level,String elem);

    /**
     * WNI 数据中的 ws 和 wd
     * @param dateStr
     * @param vti
     * @param height
     * @param elem
     * @return
     */
    JSONObject getAreaData(String dateStr,String vti,String height,String elem);
}
