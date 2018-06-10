package net.xinhong.meteoserve.service.service;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by wingsby on 2017/12/28.
 */
public interface TyphStatisticDataService {
    public JSONObject getPointData(String lat, String lon, String year, String month, String table);
}
