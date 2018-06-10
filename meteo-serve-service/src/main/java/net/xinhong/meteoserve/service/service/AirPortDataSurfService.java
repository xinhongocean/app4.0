package net.xinhong.meteoserve.service.service;

import com.alibaba.fastjson.JSONObject;

/**
 * 机场实况数据获取
 * Created by xiaoyu on 16/6/12.
 */
public interface AirPortDataSurfService {

    JSONObject getDataFromCode(String strCode, String year, String month, String day, String hour, String minute);

    JSONObject getAirPortSeqDataSurf(String stationCode, String year, String month,
                                     String day, String hour, String minute, String elem);

    /**
     * 各机场实况及预报索引信息
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return
     */
    JSONObject getAirPortSigmentDataIndexs(String year, String month, String day, String hour, String minute, boolean hasLevel);


    JSONObject getAirPortSigmentData(String strCode, String sigmentType, String year, String month, String day, String hour, String minute);
}
