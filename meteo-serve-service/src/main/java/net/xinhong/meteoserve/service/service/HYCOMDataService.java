package net.xinhong.meteoserve.service.service;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by wingsby on 2017/12/28.
 */
public interface HYCOMDataService {
    public JSONObject getPointData(String lat, String lng, String year, String month, String day,
                            String hour);
    public JSONObject getImg(String year, String month, String day,
                                   String hour,String depth,String eles);

    JSONObject getTimeProfileData(String strLat, String strLng, String year, String month, String day, String hour, String minute);

    JSONObject getSpaceProfileData(String strLats, String strLngs, String year, String month, String day, String hour, String minute);
}
