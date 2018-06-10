package net.xinhong.meteoserve.service.dao;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by wingsby on 2018/1/8.
 */
public interface GTSPPDataDao {
    JSONObject getPointData(String ryear, String rmonth, String rday);
}
