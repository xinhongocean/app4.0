package net.xinhong.meteoserve.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.dao.AirPortInfoSurfDao;
import net.xinhong.meteoserve.service.domain.AirPortInfoBean;
import net.xinhong.meteoserve.service.domain.WaypointInfoBean;
import net.xinhong.meteoserve.service.service.AirPortInfoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by xiaoyu on 16/6/12.
 */
@Service
public class AirPortInfoServiceImpl implements AirPortInfoService {
    private static final Log logger = LogFactory.getLog(StationDataSurfServiceImpl.class);

    @Autowired
    private AirPortInfoSurfDao airPortSurfDao;

    @Override
    public JSONObject getStationInfoFromNameIcao3Icao4(String param) {
        logger.info("开始进行模糊查询:" + param + "站点列表......");
        //    return this.stationSurfDao.getStationInfoFromCname(param);

        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();
        JSONOperateTool.putJSONNoResult(resJSON);
        if (param == null || param.trim().isEmpty()){
            return resJSON;
        }

        List<AirPortInfoBean> stationList = this.airPortSurfDao.getStationInfoFromNameIcao3Icao4(param);
        if (stationList == null || stationList.isEmpty())
            return resJSON;

        JSONOperateTool.putJSONSuccessful(resJSON, JSONObject.toJSON(stationList));
        return resJSON;
    }

    @Override
    public JSONObject getStationInfoFromLatLng(float lat, float lng) {

        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }
        JSONOperateTool.putJSONNoResult(resJSON);

        AirPortInfoBean stationInfo = this.airPortSurfDao.getNearstStaionFromLatLng(lat, lng);
        if (stationInfo == null)
            return resJSON;

        JSONOperateTool.putJSONSuccessful(resJSON, JSONObject.toJSON(stationInfo));
        return resJSON;
    }

    @Override
    public JSONObject getWaypointFromName(String wayPointName) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();
        resJSON.put(ResJsonConst.DATA, "");
        if (wayPointName == null || wayPointName.isEmpty()){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        //2.然后获取数据
        List<WaypointInfoBean> waypointList = airPortSurfDao.searchWaypointListFromName(wayPointName);

        if (waypointList == null || waypointList.isEmpty()){
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        JSONOperateTool.putJSONSuccessful(resJSON, JSONObject.toJSON(waypointList));
        return resJSON;
    }

    @Override
    public JSONObject getWaypointsFromRouteIdent(String routeidenty) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();
        resJSON.put(ResJsonConst.DATA, "");
        if (routeidenty == null || routeidenty.isEmpty()){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        //2.然后获取数据
        List<WaypointInfoBean> waypointList = airPortSurfDao.searchWaypointListFromRouteIdent(routeidenty);

        if (waypointList == null || waypointList.isEmpty()){
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        JSONOperateTool.putJSONSuccessful(resJSON, JSONObject.toJSON(waypointList));
        return resJSON;
    }
}
