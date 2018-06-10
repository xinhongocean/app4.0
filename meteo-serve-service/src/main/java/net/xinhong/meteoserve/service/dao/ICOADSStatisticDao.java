package net.xinhong.meteoserve.service.dao;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by wingsby on 2017/12/28.
 */
public interface ICOADSStatisticDao {
    public JSONObject getPointData(String strLats, String strLngs,
                                   String month, String fourth,String table,String space) ;
}
