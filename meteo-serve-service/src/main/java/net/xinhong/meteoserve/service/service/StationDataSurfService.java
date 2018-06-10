package net.xinhong.meteoserve.service.service;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.service.domain.StationDataSurfBean;

/**
 * Created by Administrator on 2016/3/3.
 */
public interface StationDataSurfService {

    JSONObject getDataFromLatlng(float lat, float lng, String year, String month, String day, String hour, String minute);


    JSONObject getDataFromCname(String cname, String year, String month, String day, String hour, String minute);


    StationDataSurfBean getStationSurf(String name, String year, String month, String day, String hour, String minute);

    JSONObject getDataFromCode(String strCode, String year, String month, String day, String hour, String minute);

    JSONObject getStationSeqDataSurf(String stationCode, String year, String month,
                                                String day, String hour, String minute, String elem);

    JSONObject getStationAQIData(String stationCode, String year, String month,
                                 String day, String hour, String minute);
    JSONObject getCityIDAQIData(String strCityID, String year, String month,
                                String day, String hour, String minute);

    JSONObject searchAQIDistList(Integer dlevel, String year, String month, String day, String hour, String minute);

    JSONObject getStationListDataSurf(String year, String month, String day, String hour, String minute);


    JSONObject getIsoLineData(String year, String month, String day, String hour, String s, String level, String elem, String strArea);

    JSONObject getIsosurfaceData(String year, String month, String day, String hour, String s, String level, String elem, String strArea);
}
