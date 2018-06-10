package net.xinhong.meteoserve.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.dao.StationInfoSurfDao;
import net.xinhong.meteoserve.service.domain.StationInfoSurfBean;
import net.xinhong.meteoserve.service.service.StationInfoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description: <br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 <a href=mailto:liusofttech@sina.com>刘晓昌</a>
 * @version 创建时间：2016/3/7.
 */
@Service
public class StationInfoServiceImpl implements StationInfoService {
    private static final Log logger = LogFactory.getLog(StationDataSurfServiceImpl.class);

    private static final float SURFDELTA = 0.8f;
    private static final float HIGHDELTA = 2.0f;


    @Autowired
    private StationInfoSurfDao stationSurfDao;

    @Override
    public StationInfoSurfBean getNearestStationInfoSurfFromLatLng(float lat, float lng) {
        logger.info("开始获取纬度:" + lat + "经度:" + lng + "周边最近的地面站点......");
        return stationSurfDao.getNearstStaionFromLatLng(lat, lng,"DM", SURFDELTA);
    }



    @Override
    public List<StationInfoSurfBean> getStationInfoSurfFromPy(String py) {
        logger.info("开始获取拼音为:" + py + "站点列表......");
        return this.stationSurfDao.getStationInfoFromPy(py);
    }

    @Override
    public List<StationInfoSurfBean> getStationInfoSurfFromCname(String cname) {
        logger.info("开始获取名称为:" + cname + "站点列表......");
        return this.stationSurfDao.getStationInfoFromCname(cname);
    }

    @Override
    public List<StationInfoSurfBean> getSelectedSurfHighStationInfoList() {
        logger.info("开始获取地面及高空站点列表......");
        return this.stationSurfDao.getSelectedSurfHighStationInfoList();
    }

    @Override
    public JSONObject getStationInfoFromPyNameCode(String param) {
        logger.info("开始进行模糊查询:" + param + "站点列表......");
        //    return this.stationSurfDao.getStationInfoFromCname(param);

        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();
        JSONOperateTool.putJSONNoResult(resJSON);
        if (param == null || param.trim().isEmpty()){
            return resJSON;
        }

        List<StationInfoSurfBean> stationList = this.stationSurfDao.getStationInfoFromPyNameCode(param);
        if (stationList == null || stationList.isEmpty())
            return resJSON;

        JSONOperateTool.putJSONSuccessful(resJSON, JSONObject.toJSON(stationList));
        return resJSON;
    }

    @Override
    public JSONObject getNearestStationInfoHighFromLatLng(float lat, float lng) {
        logger.info("开始获取纬度:" + lat + "经度:" + lng + "周边最近的高空站点......");
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }
        JSONOperateTool.putJSONNoResult(resJSON);

        //1.查询获取数据
        StationInfoSurfBean stationInfo = stationSurfDao.getNearstStaionFromLatLng(lat, lng,"GK", HIGHDELTA);
        if (stationInfo == null)
            return resJSON;

        JSONOperateTool.putJSONSuccessful(resJSON, JSONObject.toJSON(stationInfo));
        return resJSON;
    }

    @Override
    public JSONObject getNearestStationInfoHighFromCode(String strCode) {
        logger.info("开始获取站号:" + strCode+ "周边最近的高空站点......");
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();
        if (strCode == null || strCode.isEmpty()){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }
        JSONOperateTool.putJSONNoResult(resJSON);

        //1.查询获取数据
        StationInfoSurfBean stationInfo = stationSurfDao.getStationInfoFromCode(strCode);
        if (stationInfo == null)
            return resJSON;
        if (stationInfo.getTypep() ==1 || stationInfo.getTypet()==1 ){ //已是高空站,直接返回
            JSONOperateTool.putJSONSuccessful(resJSON, JSONObject.toJSON(stationInfo));
            return resJSON;
        }

        //根据经纬度,查询最近的高空站
        stationInfo = stationSurfDao.getNearstStaionFromLatLng(stationInfo.getLat(), stationInfo.getLng(),"GK", HIGHDELTA);
        if (stationInfo == null)
            return resJSON;

        JSONOperateTool.putJSONSuccessful(resJSON, JSONObject.toJSON(stationInfo));
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


        StationInfoSurfBean stationInfo = this.stationSurfDao.getNearstStaionFromLatLng(lat, lng,"DM", SURFDELTA);
        if (stationInfo == null)
            return resJSON;

        JSONOperateTool.putJSONSuccessful(resJSON, JSONObject.toJSON(stationInfo));
        return resJSON;
    }
}
