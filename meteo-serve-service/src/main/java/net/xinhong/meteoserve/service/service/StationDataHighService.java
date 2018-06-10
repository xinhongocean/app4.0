package net.xinhong.meteoserve.service.service;

import com.alibaba.fastjson.JSONObject;

/**
 * Description: <br>
 *
 * @author 作者 <a href=ds.lht@163.com>stone</a>
 * @version 创建时间：2016/5/17.
 */
public interface StationDataHighService {

     JSONObject getDataFromLatlng(float lat, float lng, String year, String month, String day, String hour);

     JSONObject getDataFromCode(String strCode, String year, String month, String day, String hour);

     JSONObject getStationDataHighTimeProfile(String stationCode, String year, String month, String day, String hour);

     JSONObject getStationDataHighIndex(String strCode, String year, String month, String day, String hour);

     JSONObject getStationListDataHigh(String year, String month, String day, String hour, String minute);
}
