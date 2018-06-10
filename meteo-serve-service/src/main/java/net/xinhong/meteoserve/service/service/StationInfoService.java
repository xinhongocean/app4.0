package net.xinhong.meteoserve.service.service;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.service.domain.StationInfoSurfBean;

import java.util.List;

/**
 * Description: <br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 <a href=mailto:liusofttech@sina.com>刘晓昌</a>
 * @version 创建时间：2016/3/7.
 */
public interface StationInfoService {

    StationInfoSurfBean getNearestStationInfoSurfFromLatLng(float lat, float lng);

    JSONObject getNearestStationInfoHighFromLatLng(float lat, float lng);

    JSONObject getNearestStationInfoHighFromCode(String strCode);


    List<StationInfoSurfBean> getStationInfoSurfFromPy(String py);

    List<StationInfoSurfBean> getStationInfoSurfFromCname(String cname);

    List<StationInfoSurfBean> getSelectedSurfHighStationInfoList();

    JSONObject getStationInfoFromPyNameCode(String param);

    JSONObject getStationInfoFromLatLng(float lat, float lng);

}
