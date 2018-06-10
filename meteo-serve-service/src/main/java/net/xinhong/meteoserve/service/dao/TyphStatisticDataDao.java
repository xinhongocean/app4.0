package net.xinhong.meteoserve.service.dao;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by wingsby on 2017/12/28.
 */
public interface TyphStatisticDataDao {
    public JSONObject getPointData(String lat, String lon, String year, String month, String table);
}
