package net.xinhong.meteoserve.service.dao;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by xiaoyu on 16/6/12.
 */
public interface AirPortDataSurfDao {

    /**
     * 根据站号及时间获取机场地面实况数据
     * @param stationCode
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return-获取实况数据的JSON结构字符串
     */
    JSONObject getAirPortDataSurf(String stationCode, String year, String month, String day, String hour, String minute);

    /**
     * 查询机场数据时序数据
     * @param strCode
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @param elem
     * @return
     */
    JSONObject getAirPortSeqDataSurf(String strCode, String year, String month,
                                            String day, String hour, String minute, String elem);

    JSONObject getAirPortSigmentDataIndexs(String year, String month, String day, String hour, String minute, boolean hasLevel);

    JSONObject getAirPortSigmentData(String strCode, String sigmentType, String year, String month, String day, String hour, String minute);
}
