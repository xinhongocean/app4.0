package net.xinhong.meteoserve.service.dao;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ElemCityFC;
import net.xinhong.meteoserve.service.domain.StationDataSurfBean;

import java.util.List;

/**
 * Description: <br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 <a href=mailto:liusofttech@sina.com>刘晓昌</a>
 * @version 创建时间：2016/3/11.
 */
public interface StationDataCityFCDao {

    /**
     * 根据站号及时间获取城镇精细化预报
     * @param stationCode
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return
     */
    JSONObject getStationCityFC(String stationCode, String year, String month, String day, String hour, String minute, List<ElemCityFC> elems);
}
