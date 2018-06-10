package net.xinhong.meteoserve.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.dao.StationDataHighDao;
import net.xinhong.meteoserve.service.dao.StationInfoSurfDao;
import net.xinhong.meteoserve.service.domain.StationInfoSurfBean;
import net.xinhong.meteoserve.service.service.StationDataHighService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Description: <br>
 *
 * @author 作者 <a href=ds.lht@163.com>stone</a>
 * @version 创建时间：2016/5/17.
 */
@Service
public class StationDataHighServiceImpl implements StationDataHighService {

    Logger logger = LoggerFactory.getLogger(StationDataHighServiceImpl.class);
    @Resource
    StationDataHighDao stationHighDao;

    @Resource
    StationInfoSurfDao stationInfoSurfDao;


    public JSONObject getDataFromLatlng(float lat, float lng, String year, String month, String day, String hour) {

        JSONObject resJSON = new JSONObject();
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }
        StationInfoSurfBean stationInfo = stationInfoSurfDao.getNearstStaionFromLatLng(lat, lng, "GK", 2.0f);
        if (stationInfo == null) {
            logger.info("给定的经纬度:" + lat + "," + lng + "附近没有找到站点基本信息!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        JSONOperateTool.putJSONStation(resJSON, stationInfo);

        JSONObject daoJSON = stationHighDao.getStationDataHigh(stationInfo.getStationCode(), year, month, day, hour);
        if (daoJSON != null) {
            resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
            if (daoJSON.get(ResJsonConst.DATA) == null){
                resJSON.put(ResJsonConst.DATA, "");
            }
            JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
            resJSON.put(ResJsonConst.DATA, "");
        }
        return resJSON;
    }

    @Override
    public JSONObject getDataFromCode(String strCode, String year, String month, String day, String hour) {
        JSONObject resJSON = new JSONObject();
        if (strCode == null || strCode.isEmpty()) {
            JSONOperateTool.putJSONParamError(resJSON);
            resJSON.put(ResJsonConst.DATA, "");
            return resJSON;
        }

        // todo:2016-7-21 为提高效率,需删除查询站点信息内容!
        StationInfoSurfBean stationInfo = stationInfoSurfDao.getStationInfoFromCode(strCode);
        if (stationInfo == null){
            logger.info("给定的站号没有找到站点基本信息!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        JSONOperateTool.putJSONStation(resJSON, stationInfo);

        JSONObject daoJSON = stationHighDao.getStationDataHigh(strCode, year, month, day, hour);
        if (daoJSON != null) {
            resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
            JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
            if (daoJSON.get(ResJsonConst.DATA) == null){
                resJSON.put(ResJsonConst.DATA, "");
            }

        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
            resJSON.put(ResJsonConst.DATA, "");
        }
        return resJSON;
    }

    @Override
    public JSONObject getStationDataHighTimeProfile(String stationCode, String year, String month, String day, String hour){
        JSONObject resJSON = new JSONObject();
        if (stationCode == null || stationCode.isEmpty()) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        JSONObject daoJSON = stationHighDao.getStationDataHighTimeProfile(stationCode, year, month, day, hour);
        if (daoJSON != null) {
            resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
            if (daoJSON.get(ResJsonConst.DATA) == null){
                resJSON.put(ResJsonConst.DATA, "");
            }
            JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);

        }
        return resJSON;
    }

    @Override
    public JSONObject getStationDataHighIndex(String stationCode, String year, String month, String day, String hour){
        JSONObject resJSON = new JSONObject();
        if (stationCode == null || stationCode.isEmpty()) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        JSONObject daoJSON = stationHighDao.getStationDataHighIndex(stationCode, year, month, day, hour);
        if (daoJSON != null) {
            resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
            JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
        }
        return resJSON;
    }

    @Override
    public JSONObject getStationListDataHigh(String year, String month, String day, String hour, String minute) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();
        if (year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        } else {
            resJSON.put(ResJsonConst.DATA, "");
            JSONOperateTool.putJSONNoResult(resJSON);
        }

        //2.然后获取数据
        JSONObject daoJSON = stationHighDao.getSelectedStationListDataHigh(year, month, day, hour, minute);

        //3.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")){
            logger.info("给定的时间要素没有查询到站点列表的实况数据!");
            resJSON.put(ResJsonConst.DATA, "");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
        if (daoJSON.get(ResJsonConst.DATA) == null){
            resJSON.put(ResJsonConst.DATA, "");
        }
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }


}
