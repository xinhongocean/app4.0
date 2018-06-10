package net.xinhong.meteoserve.service.service;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by wingsby on 2017/12/25.
 */
public interface ICOADSStatisticService {
    public JSONObject getPointData(String strLats, String strLngs, String month,String fourth, String table, String ele);
    //
}
