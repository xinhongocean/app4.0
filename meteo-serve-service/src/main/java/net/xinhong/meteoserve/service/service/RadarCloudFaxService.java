package net.xinhong.meteoserve.service.service;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by xiaoyu on 16/8/8.
 */
public interface RadarCloudFaxService {
    /**
     * 获取指定类型radarType的时间范围的雷达文件列表及属性信息
     *
     * @param radarType
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return
     */
    JSONObject getRadarInfo(String radarType, String year, String month, String day, String hour, String minute);

    /**
     * 获取指定类型cloudType的时间范围的云图文件列表及属性信息
     *
     * @param cloudType
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return
     */
    JSONObject getCloudInfo(String cloudType, String year, String month, String day, String hour, String minute);

    /**
     * 获取指定类型radarType的时间范围的单站雷达文件列表及属性信息
     *
     * @param radarType
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return
     */
    JSONObject getStationRadarInfo(String radarType, String year, String month, String day, String hour, String minute,
                                   String radarIDs);

    /**
     * 日本PM2.5预报趋势图
     *
     * @param pm25Type
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return
     */
    JSONObject getJPPM2Dot5FCInfo(String pm25Type, String year, String month, String day, String hour, String minute);

    /**
     * 获取传真图信息列表
     *
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return
     */
    JSONObject getFaxInfo(String year, String month, String day, String hour, String minute);

    /**
     * 获取欧洲数值预报图片信息列表
     *
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return
     */
    JSONObject getECMWFImageInfo(String year, String month, String day, String hour, String minute);


    /**
     * 获取葵花8二级产品多个位置同个时间的数据
     *
     * @param strLats
     * @param strLngs
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @param isInterPolate-设置为true时，经纬度插值，否则不插值
     * @return
     */
    JSONObject getHimawari8L2PointSpaceData(String strLats, String strLngs, String year, String month, String day,
                                            String hour, String minute, boolean isInterPolate);


    /**
     * @param strLat
     * @param strLng
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return
     */
    JSONObject getHimawari8L2PointTimeData(String strLat, String strLng, String year, String month, String day, String hour, String minute);


    JSONObject getNearestRadarInfo(String lat, String lng, String year, String month, String day, String hour, String minute);

    /**
     * 获取单站雷达位置分布信息
     *
     * @return
     */
    JSONObject getStationRadarDistribInfo();


    JSONObject getOceanFCImageInfo(String type, String year, String month, String day, String hour, String minute);

    JSONObject getCoastRegionFC(String region, String year, String month, String day, String hour, String minute);
}
