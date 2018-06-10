package net.xinhong.meteoserve.service.service;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by xiaoyu on 16/6/12.
 */
public interface AirPortInfoService {
    JSONObject getStationInfoFromNameIcao3Icao4(String param);

    JSONObject getStationInfoFromLatLng(float lat, float lng);

    JSONObject getWaypointFromName(String wayPointName);

    /**
     * 根据航线名称获取航路点列表
     * @param routeidenty
     * @return
     */
    JSONObject getWaypointsFromRouteIdent(String routeidenty);
}
