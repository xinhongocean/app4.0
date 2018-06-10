package net.xinhong.meteoserve.service.dao;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by xiaoyu on 16/9/19.
 */
public interface CFSFCDataDao {

    JSONObject getDataFromLatlng(float lat, float lng, String year, String month, String day, String hour, String yearFC, String monthFC);

}
