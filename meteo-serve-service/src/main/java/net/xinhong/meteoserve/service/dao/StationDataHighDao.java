package net.xinhong.meteoserve.service.dao;

import com.alibaba.fastjson.JSONObject;

/**
 * Description: <br>
 *
 * @author 作者 <a href=ds.lht@163.com>stone</a>
 * @version 创建时间：2016/5/17.
 */
public interface StationDataHighDao {

    JSONObject getStationDataHigh(String stationCode, String year, String month, String day, String hour);

    JSONObject getStationDataHighTimeProfile(String stationCode, String year, String month, String day, String hour);

    JSONObject getStationDataHighIndex(String stationCode, String year, String month, String day, String hour);

    public JSONObject getSelectedStationListDataHigh(String year, String month, String day, String hour, String minute);
}
