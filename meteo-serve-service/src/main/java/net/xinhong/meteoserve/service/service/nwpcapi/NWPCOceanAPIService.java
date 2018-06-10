package net.xinhong.meteoserve.service.service.nwpcapi;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by xiaoyu on 17/2/21.
 */
public interface NWPCOceanAPIService {

    /**
     * 根据经纬度及时间获取海钓使用的风及海浪数据(未来逐24小时)
     * @param slat
     * @param slng
     * @param year
     * @param month
     * @param day
     * @param isShowDay-true时，返回的时间结构中包含月日，否则只有时
     * @return
     */
    JSONObject getHoursWindWaveDataFromPostion(String slat, String slng, String year, String month, String day,
                                               String hour, boolean isShowDay);


    /**
     * 根据经纬度及时间获取海钓使用的风及海浪数据(未来7天)
     * @param slat
     * @param slng
     * @param year
     * @param month
     * @param day
     * @return
     */
    JSONObject get7DayWindWaveDataFromPostion(String slat, String slng, String year, String month, String day);
}
