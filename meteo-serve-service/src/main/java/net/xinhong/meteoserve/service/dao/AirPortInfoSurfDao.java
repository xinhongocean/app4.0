package net.xinhong.meteoserve.service.dao;

import net.xinhong.meteoserve.service.domain.AirPortInfoBean;
import net.xinhong.meteoserve.service.domain.WaypointInfoBean;

import java.util.List;

/**
 * 查询机场站点信息
 * Created by xiaoyu on 16/6/12.
 */
public interface AirPortInfoSurfDao {

    /**
     * 根据给定经纬度位置，查询最近的机场站点
     * @param lat
     * @param lng
     * @return
     */
    AirPortInfoBean getNearstStaionFromLatLng(float lat, float lng);


    /**
     * 根据给定的参数，查询对应的机场站点信息列表（模糊查询站名 拼音及站号匹配站点信息列表）
     * @param param
     * @return
     */
    List<AirPortInfoBean> getStationInfoFromNameIcao3Icao4(String param);

    /**
     * 根据给定的名称，模糊查询对应的航路点名称列表
     * @param wayPointName
     * @return
     */
    List<WaypointInfoBean> searchWaypointListFromName(String wayPointName);

    /**
     * 根据给定的航线标识查询航路点列表
     * @param routeidenty
     * @return
     */
    List<WaypointInfoBean> searchWaypointListFromRouteIdent(String routeidenty);
}
