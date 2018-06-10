package net.xinhong.meteoserve.service.service;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by wingsby on 2017/12/28.
 */
public interface WaveWatchDataService {
    JSONObject getPointData(String lat, String lng, String year, String month,
                            String day, String hour);

    JSONObject getImg(String year, String month, String day, String hour, String eles);

    JSONObject getIsoLineData(String year, String month, String day, String hour, String s, String elem, String strArea);

    JSONObject getTimeProfileData(String strLat, String strLng, String year, String month, String day, String hour, String minute);

    JSONObject getSpaceProfileData(String strLats, String strLngs, String year, String month, String day, String hour, String minute);

    JSONObject getAreaData(String year, String month, String day, String hour, String s,  float sLat, float eLat, float sLng,
                           float eLng, String elem);
}
