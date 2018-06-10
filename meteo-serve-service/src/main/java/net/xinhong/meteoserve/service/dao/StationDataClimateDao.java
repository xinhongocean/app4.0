package net.xinhong.meteoserve.service.dao;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ElemCityFC;

import java.util.List;

/**
 * Created by xiaoyu on 16/7/19.
 */
public interface StationDataClimateDao {
    /**
     * 根据站号及时间获取城镇基本气候信息
     * @param stationCode
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return
     */
    JSONObject getStationForYearsData(String stationCode, String year, String month, String day, String hour, String minute);


}
