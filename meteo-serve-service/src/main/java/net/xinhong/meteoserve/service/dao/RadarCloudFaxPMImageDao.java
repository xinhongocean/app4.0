package net.xinhong.meteoserve.service.dao;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.service.domain.StationRadarInfoBean;

import java.util.List;

/**
 * Created by xiaoyu on 16/8/8.
 */
public interface RadarCloudFaxPMImageDao {

    JSONObject getRadarCloudInfo(String radarCloudType, String year, String month, String day, String hour, String minute,
                                 String radarIDs);

    JSONObject getFaxInfo(String year, String month, String day, String hour, String minute);

    JSONObject getHimawari8L2PointData(String strLat, String strLng, String ryear, String rmonth, String rday, String rhour, String rminute);

    JSONObject getECMWFImageInfo(String year, String month, String day, String hour, String minute);

    List<StationRadarInfoBean> getStationRadarDistribInfo();

    void setRadarCLatLng(Float radarCLat, Float radarCLng);

    JSONObject getOceanFCInfo(String type, String year, String month, String day);

    JSONObject getCoastRegionFC(String region, String year, String month, String day);
}
