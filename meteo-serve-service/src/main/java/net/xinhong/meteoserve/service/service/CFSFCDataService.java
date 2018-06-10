package net.xinhong.meteoserve.service.service;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by xiaoyu on 16/9/19.
 */
public interface CFSFCDataService {

    JSONObject getDataFromLatlng(float lat, float lng, String year, String month, String day, String hour,
                                 String yearFC, String monthFC);

    JSONObject getDataFromCode(String strCode, String year, String month, String day, String hour,
                                 String yearFC, String monthFC);
}
