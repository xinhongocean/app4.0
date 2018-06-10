package net.xinhong.meteoserve.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.common.SunTimes;
import net.xinhong.meteoserve.service.service.ToolsService;
import org.springframework.stereotype.Service;

/**
 * Created by xiaoyu on 16/9/26.
 */
@Service
public class ToolsServiceImpl implements ToolsService {
    @Override
    public JSONObject getSunRiseSetFromLatlng(float lat, float lng, String year, String month, String day) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();

        resJSON.put(ResJsonConst.DATA, "");

        if (year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || lat > 90 || lat < -90
                || lng > 180 || lng < -180) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
        }

        SunTimes sunTimes = new SunTimes(year, month, day, lng, lat);
        String rise = sunTimes.getSunrise();
        String set = sunTimes.getSunset();
        JSONObject dataJSON = new JSONObject();
        dataJSON.put("sunrise", rise);
        dataJSON.put("sunset",  set);

        JSONOperateTool.putJSONSuccessful(resJSON, dataJSON);
        return resJSON;
    }
}
