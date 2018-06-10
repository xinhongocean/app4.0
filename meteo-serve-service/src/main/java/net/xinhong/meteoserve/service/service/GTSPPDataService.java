package net.xinhong.meteoserve.service.service;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by wingsby on 2018/1/8.
 */
public interface GTSPPDataService {
    JSONObject getPointData( String year, String month, String day);
}
