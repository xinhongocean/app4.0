package net.xinhong.meteoserve.service.service;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by xiaoyu on 16/4/19.
 */
public interface WNIFCDataService {

    /**
     * 获取某个点指定时间及预报时效的危险天气数据
     * @param lat
     * @param lng
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param VTI
     * @return
     */
    JSONObject getPointDangerData(float lat, float lng, String year, String month, String day, String hour, String VTI);


    /**
     * 获取多个点指定时间及预报时效的危险天气数据
     * @param strLats
     * @param strLngs
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param VTI
     * @return
     */
    JSONObject getPointsData(String strLats, String strLngs, String year, String month, String day, String hour, String VTI);
    /**
     * 获取wni区域危险天气
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param vti
     * @param type
     * @param height
     * @param sLat
     * @param eLat
     * @param sLng
     * @param eLng
     * @param elem
     * @return
     */
    JSONObject getAreaData(String year,String month,String day,String hour,String vti,
                           String type,String height,float sLat, float eLat, float sLng,
                           float eLng,String elem);

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

}
