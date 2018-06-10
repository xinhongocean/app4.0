package net.xinhong.meteoserve.service.dao;

import net.xinhong.meteoserve.service.domain.StationInfoSurfBean;

import java.util.List;

/**
 * Description: 地面站点信息Dao操作类<br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 <a href=mailto:liusofttech@sina.com>刘晓昌</a>
 * @version 创建时间：2016/3/7.
 */
public interface StationInfoSurfDao {

    /**
     * 根据给定经纬度位置，查询最近的站点
     * @param lat
     * @param lng
     * @return
     */
    StationInfoSurfBean getNearstStaionFromLatLng(float lat, float lng,String dataType, float delta);


    /**
     * 根据给定的中文名称，查询对应的站号
     * @param cname
     * @return
     */
    List<String> getStationCodeFromName(String cname);

    /**
     * 根据站号查询站点信息
     * @param stationCode
     * @return
     */
    StationInfoSurfBean getStationInfoFromCode(String stationCode);

    /**
     * 根据给定的中文名称，查询对应的站点信息
     * @param cname
     * @return
     */
    List<StationInfoSurfBean> getStationInfoFromCname(String cname);

    /**
     * 根据给定的拼音缩写，查询对应的站点信息列表（支持模糊查询）
     * @param py
     * @return
     */
    List<StationInfoSurfBean> getStationInfoFromPy(String py);
    /**
     * 根据给定的参数，查询对应的站点信息列表（模糊查询站名 拼音及站号匹配站点信息列表）
     * @param param
     * @return
     */
    List<StationInfoSurfBean> getStationInfoFromPyNameCode(String param);

    /**
     * 获取站点列表(地面及高空选择的站点)
     * @param
     * @return
     */
    List<StationInfoSurfBean> getSelectedSurfHighStationInfoList();


    /**
     * 查询给定站号在选定的站点列表中的站点信息
     * @param stationCode
     * @return-如果没有查询到,则返回null
     */
    StationInfoSurfBean getSelectedStationInfoFromCode(String stationCode);


    /**
     * 根据行政编码查询站号
     * @param cityCode-行政编码
     * @return
     */
    String getStationCodeFromCityCode(String cityCode);
}
