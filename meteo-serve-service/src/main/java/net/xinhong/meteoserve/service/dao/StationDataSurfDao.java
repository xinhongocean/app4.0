package net.xinhong.meteoserve.service.dao;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.service.domain.StationDataSurfBean;
import net.xinhong.meteoserve.service.domain.StationInfoSurfBean;

import java.util.List;

/**
 * Description: <br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 <a href=mailto:liusofttech@sina.com>刘晓昌</a>
 * @version 创建时间：2016/3/3.
 */
public interface StationDataSurfDao {

    /**
     * todo:获取地面站点实况数据，测试用！
     * @param stationCode
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return
     */
    StationDataSurfBean getStationSurf(String stationCode, String year, String month, String day, String hour, String minute);


    /**
     * 根据站号及时间获取站点地面实况数据
     * @param stationCode
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return-获取实况数据的JSON结构字符串
     */
    JSONObject getStationDataSurf(String stationCode, String year, String month, String day, String hour, String minute);

    /**
     * 根据站号及要素时间获取近两天实况要素值序列
     * @param stationCode
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @param elem
     * @return
     */
    JSONObject getStationSeqDataSurf(String stationCode, String year, String month,
                                            String day, String hour, String minute, String elem);


    /**
     * 获取空气质量数据
     * @param stationCode
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return
     */
    JSONObject searchStationAQIData(String stationCode, String year, String month, String day, String hour, String minute);

    JSONObject searchCityIDAQIData(String stationCode, String year, String month, String day, String hour, String minute);

    /**
     * 查询空气质量分布图
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return
     */
    JSONObject searchAQIDistList(Integer dlevel, String year, String month, String day, String hour, String minute);

    /**
     * 获取选择站点地面实况数据
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return
     */
    JSONObject getSelectedStationListDataSurf(String year, String month, String day, String hour, String minute);

    JSONObject getIsolineData(String year, String month, String day, String hour, String level, String elem, String strArea);

    JSONObject getIsosurfaceData(String year, String month, String day, String hour, String level, String elem, String strArea);
}
