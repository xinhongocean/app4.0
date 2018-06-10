package net.xinhong.meteoserve.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.dao.StationDataSurfDao;
import net.xinhong.meteoserve.service.dao.StationInfoSurfDao;
import net.xinhong.meteoserve.service.domain.StationDataSurfBean;
import net.xinhong.meteoserve.service.domain.StationInfoSurfBean;
import net.xinhong.meteoserve.service.service.StationDataSurfService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description: <br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 <a href=mailto:liusofttech@sina.com>刘晓昌</a>
 * @version 创建时间：2016/3/3.
 */
@Service
public class StationDataSurfServiceImpl implements StationDataSurfService {

    private static final Log logger = LogFactory.getLog(StationDataSurfServiceImpl.class);

    @Autowired
    private StationDataSurfDao stationSurfDao;

    @Autowired
    private StationInfoSurfDao stationInfoSurfDao;

    @Override
    public JSONObject getDataFromCname(String cname, String year, String month, String day, String hour, String minute) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = this.initJSON(year, month, day, hour, minute);
        if (resJSON.get(ResJsonConst.STATUSCODE).equals(ResStatus.PARAM_ERROR.getStatusCode())){
            return resJSON;
        }
        if (cname == null || cname.isEmpty()){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        //1.查询给定中文名站点站号
        List<StationInfoSurfBean> stationInfos = stationInfoSurfDao.getStationInfoFromCname(cname);
        if (stationInfos == null || stationInfos.isEmpty()){
            logger.info("给定的中文名称没有找到站点基本信息!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        //todo:一个名称有多个站点，取第一个！
        StationInfoSurfBean stationInfo = stationInfos.get(0);
        JSONOperateTool.putJSONStation(resJSON, stationInfo);
        //2.然后获取数据
        JSONObject daoJSON = stationSurfDao.getStationDataSurf(stationInfo.getStationCode(), year, month, day, hour, minute);

        //3.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")){
            logger.info("给定的名称" + cname + "及时间要素没有查询到数据!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    @Override
    public JSONObject getDataFromCode(String strCode, String year, String month, String day, String hour, String minute) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = this.initJSON(year, month, day, hour, minute);
        if (resJSON.get(ResJsonConst.STATUSCODE).equals(ResStatus.PARAM_ERROR.getStatusCode())){
            return resJSON;
        }
        if (strCode == null || strCode.isEmpty()){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        //1.查询给定站号站点信息
        // todo:2016-7-21 为提高效率,需删除查询站点信息内容!
        StationInfoSurfBean stationInfo = stationInfoSurfDao.getStationInfoFromCode(strCode);
        if (stationInfo == null){
            logger.info("给定的站号没有找到站点基本信息!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        JSONOperateTool.putJSONStation(resJSON, stationInfo);
       // resJSON.put(ResJsonConst.STATIONCODE, strCode);

        //2.然后获取数据
        JSONObject daoJSON = stationSurfDao.getStationDataSurf(strCode, year, month, day, hour, minute);

        //3.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")){
            logger.info("给定的站号" + strCode + "及时间要素没有查询到数据!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME) + "00");
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    @Override
    public JSONObject getStationSeqDataSurf(String stationCode, String year, String month, String day, String hour, String minute, String elem) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = this.initJSON(year, month, day, hour, minute);
        if (resJSON.get(ResJsonConst.STATUSCODE).equals(ResStatus.PARAM_ERROR.getStatusCode())){
            return resJSON;
        }
        if (elem == null || elem.isEmpty()){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }
        if (stationCode == null || stationCode.isEmpty()){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        //1.查询给定站号站点信息
        // todo:2016-7-21 为提高效率,需删除查询站点信息内容!
        StationInfoSurfBean stationInfo = stationInfoSurfDao.getStationInfoFromCode(stationCode);
        if (stationInfo == null){
            logger.info("给定的站号没有找到站点基本信息!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        JSONOperateTool.putJSONStation(resJSON, stationInfo);
//        resJSON.put(ResJsonConst.STATIONCODE, stationCode);
        //2.然后获取数据
        JSONObject daoJSON = stationSurfDao.getStationSeqDataSurf(stationCode, year, month, day, hour, minute, elem);

        //3.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")){
            logger.info("给定的站号" + stationCode + "及时间要素没有查询到实况时序数据!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    @Override
    public JSONObject getStationAQIData(String stationCode, String year, String month, String day, String hour, String minute) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = this.initJSON(year, month, day, hour, minute);
        if (resJSON.get(ResJsonConst.STATUSCODE).equals(ResStatus.PARAM_ERROR.getStatusCode())){
            return resJSON;
        }

        if (stationCode == null || stationCode.isEmpty()){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        //2.然后获取数据
        JSONObject daoJSON = stationSurfDao.searchStationAQIData(stationCode, year, month, day, hour, minute);

        //3.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")){
            logger.info("给定的站号" + stationCode + "及时间要素没有查询到空气质量数据!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    @Override
    public JSONObject getCityIDAQIData(String strCityID, String year, String month, String day, String hour, String minute) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = this.initJSON(year, month, day, hour, minute);
        if (resJSON.get(ResJsonConst.STATUSCODE).equals(ResStatus.PARAM_ERROR.getStatusCode())){
            return resJSON;
        }

        if (strCityID == null || strCityID.isEmpty()){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        //2.然后获取数据
        JSONObject daoJSON = stationSurfDao.searchCityIDAQIData(strCityID, year, month, day, hour, minute);

        //3.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")){
            logger.info("给定的行政编码" + strCityID + "及时间要素没有查询到空气质量数据!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    @Override
    public JSONObject searchAQIDistList(Integer dlevel, String year, String month, String day, String hour, String minute) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = this.initJSON(year, month, day, hour, minute);
        if (resJSON.get(ResJsonConst.STATUSCODE).equals(ResStatus.PARAM_ERROR.getStatusCode())){
            return resJSON;
        }

        //1.然后获取数据
        JSONObject daoJSON = stationSurfDao.searchAQIDistList(dlevel, year, month, day, hour, minute);

        //2.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")){
            logger.info("给定时间要素没有查询到空气质量分布数据!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    @Override
    public JSONObject getDataFromLatlng(float lat, float lng, String year, String month, String day, String hour, String minute) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = this.initJSON(year, month, day, hour, minute);
        if (resJSON.get(ResJsonConst.STATUSCODE).equals(ResStatus.PARAM_ERROR.getStatusCode())){
            return resJSON;
        }
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        //1.查询给定中文名站点站号
        StationInfoSurfBean stationInfo = stationInfoSurfDao.getNearstStaionFromLatLng(lat, lng,"DM", 0.8f);
        if (stationInfo == null){
            logger.info("给定的经纬度:"+lat + "," + lng + "附近没有找到站点基本信息!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        JSONOperateTool.putJSONStation(resJSON, stationInfo);
        //2.然后获取数据
        JSONObject daoJSON = stationSurfDao.getStationDataSurf(stationInfo.getStationCode(), year, month, day, hour, minute);

        //3.根据获取结果拼接结果JSON中数据段
        if (daoJSON != null){
            resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
        }
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")){
            logger.info("给定的经纬度:"+lat + "," + lng + "附近及时间要素没有查询到数据!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    @Override
    public StationDataSurfBean getStationSurf(String name, String year, String month, String day, String hour, String minute) {

        return stationSurfDao.getStationSurf("54511", year, month, day, hour, "");
    }

    private JSONObject initJSON(String year, String month, String day, String hour, String minute){
        if (minute == null || minute.isEmpty())
            minute = "00";

        JSONObject resJSON = new JSONObject();

        resJSON.put(ResJsonConst.DATA, "");
        if (year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2){
            JSONOperateTool.putJSONParamError(resJSON);
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
        }
        return resJSON;
    }

    @Override
    public JSONObject getStationListDataSurf(String year, String month, String day, String hour, String minute) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = this.initJSON(year, month, day, hour, minute);
        if (resJSON.get(ResJsonConst.STATUSCODE).equals(ResStatus.PARAM_ERROR.getStatusCode())){
            return resJSON;
        }

        //1.然后获取数据
        JSONObject daoJSON = stationSurfDao.getSelectedStationListDataSurf(year, month, day, hour, minute);

        //2.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")){
            logger.info("给定的时间要素没有查询到站点列表的实况数据!");
            JSONOperateTool.putJSONNoResult(resJSON);
            resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
            return resJSON;
        }

        resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    @Override
    public JSONObject getIsoLineData(String year, String month, String day, String hour, String minute, String level, String elem, String strArea) {
        return this.getIsolineInfo(year, month, day, hour, minute, level, elem, strArea, "isoline");
    }

    @Override
    public JSONObject getIsosurfaceData(String year, String month, String day, String hour, String minute, String level, String elem, String strArea) {

        return this.getIsolineInfo(year, month, day, hour, minute, level, elem, strArea, "isosurface");
    }

    private JSONObject getIsolineInfo(String year, String month, String day, String hour, String minute, String level, String elem, String strArea,
                                      String type) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();

        resJSON.put(ResJsonConst.DATA, "");
        if (year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2
                || elem == null) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
        }
        if (strArea == null)
            strArea = "COY"; //默认东北半球数据

        JSONObject daoJSON = new JSONObject();
        if (type.equals("isoline"))
            daoJSON = stationSurfDao.getIsolineData(year, month, day, hour, level, elem, strArea);
        else if (type.equals("isosurface"))
            daoJSON = stationSurfDao.getIsosurfaceData(year, month, day, hour, level, elem, strArea);

        //1.根据获取结果拼接结果JSON中数据段
        if (this.isEmptyJSON(daoJSON)) {
            JSONOperateTool.putJSONNoResult(resJSON);
            resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
            return resJSON;
        }
        resJSON.put(ResJsonConst.TIME, daoJSON.get(ResJsonConst.TIME));
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    private boolean isEmptyJSON(JSONObject daoJSON) {
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).equals("{}")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")) {
            return true;
        }
        return false;
    }

}
