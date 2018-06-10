package net.xinhong.meteoserve.service.service;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by liuso on 2017/11/4.
 * 海洋一所预报产品
 */
public interface FIOFCDataService {

    /**
     * 获取某个点指定时间FIO预报数据
     * @param lat
     * @param lng
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return
     */
    JSONObject getPointData(float lat, float lng, String year, String month, String day, String hour, String minute);



    /**
     * 获取多个点指定时间的FIO预报数据
     * @param strLats
     * @param strLngs
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return
     */
    JSONObject getPointsData(String strLats, String strLngs, String year, String month, String day, String hour, String minute);

    /**
     * 获取等值线数据
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @param level
     * @param strArea
     * @return
     */
    JSONObject getIsoLineData(String year, String month, String day, String hour,
                              String minute,String level,String elem, String strArea);



    /**
     * 获取等值面数据(填充等值线瓦片索引)
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @param level
     * @param strArea
     * @return
     */
    JSONObject getIsosurfaceData(String year, String month, String day, String hour, String minute,
                                 String level, String elem, String strArea);

    /**
     * 获取多个点绘制的剖面FIO预报数据
     * @param strLats
     * @param strLngs
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return
     */
    JSONObject getSpaceProfileData(String strLats, String strLngs, String year, String month, String day,
                                   String hour, String minute);


    /**
     * 获取单点绘制的时间剖面FIO预报数据
     * @param strLat
     * @param strLng
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return
     */
    JSONObject getTimeProfileData(String strLat, String strLng, String year, String month, String day,
                                  String hour, String minute);



    /**
     * 获取FIO预报区域数据
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @param level
     * @param sLat
     * @param eLat
     * @param sLng
     * @param eLng
     * @param elem
     * @param strArea
     * @return
     */
    JSONObject getAreaData(String year, String month, String day, String hour, String minute, String level,
                           float sLat, float eLat, float sLng, float eLng, String elem, String strArea);

}
