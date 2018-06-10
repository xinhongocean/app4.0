package net.xinhong.meteoserve.service.service;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by xiaoyu on 16/9/26.
 */
public interface ToolsService {
    JSONObject getSunRiseSetFromLatlng(float lat, float lng, String year, String month, String day);
}
