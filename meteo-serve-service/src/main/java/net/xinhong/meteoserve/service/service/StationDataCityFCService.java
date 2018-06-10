package net.xinhong.meteoserve.service.service;

import com.alibaba.fastjson.JSONObject;

/**
 * Description: 城镇精细化预报业务层<br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 <a href=mailto:liusofttech@sina.com>刘晓昌</a>
 * @version 创建时间：2016/3/11.
 */
public interface StationDataCityFCService {
    /**
     * 查询离指定经纬度最近城镇精细化预报
     * @param lat
     * @param lng
     * @param year-指定年
     * @param month-指定月
     * @param day-指定的日
     * @param hour-指定的时
     * @param minute-指定的分
     * @param elem-指定要素，参考ElemCityFC类中说明
     * @return-获取结果的JSON对象，参考ResJsonConst类说明
     */
    JSONObject getCityFCFromLatlng(float lat, float lng, String year, String month, String day, String hour, String minute, String elem);

    /**
     * 查询离指定中文名城镇精细化预报
     * @param cname
     * @param year-指定年
     * @param month-指定月
     * @param day-指定的日
     * @param hour-指定的时
     * @param minute-指定的分
     * @param elem-指定要素，参考ElemCityFC类中说明
     * @return-获取结果的JSON对象，参考ResJsonConst类说明
     */
    JSONObject getCityFCFromCname(String cname, String year, String month, String day, String hour, String minute, String elem);

    JSONObject getCityFCFromCode(String cname, String year, String month, String day, String hour, String minute, String elem);
}
