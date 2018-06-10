package net.xinhong.meteoserve.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ElemCityFC;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.service.common.FCTimeTool;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.dao.StationDataCityFCDao;
import net.xinhong.meteoserve.service.dao.StationInfoSurfDao;
import net.xinhong.meteoserve.service.domain.StationInfoSurfBean;
import net.xinhong.meteoserve.service.service.StationDataCityFCService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: <br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 <a href=mailto:liusofttech@sina.com>刘晓昌</a>
 * @version 创建时间：2016/3/11.
 */
@Service
public class StationDataCityFCServiceImpl implements StationDataCityFCService {
    private static final Log logger = LogFactory.getLog(StationDataSurfServiceImpl.class);

    @Autowired
    private StationDataCityFCDao stationDataCityFCDao;

    @Autowired
    private StationInfoSurfDao stationInfoSurfDao;


    @Override
    public JSONObject getCityFCFromLatlng(float lat, float lng, String year, String month, String day, String hour, String minute, String elem) {
        //0.初始化JSON对象，并判断参数的正确性
        if (minute == null || minute.isEmpty())
            minute = "00";
        JSONObject resJSON = this.initJSON(year, month, day, hour, minute, elem);
        if (resJSON.get(ResJsonConst.STATUSCODE).equals(ResStatus.PARAM_ERROR.getStatusCode())){
            return resJSON;
        }
        if (lat > 90 || lat < -90 || lng > 180 || lng < -180){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }
        //// TODO: 16/6/17 代码重复多!,需修改!
        //可传入多个要素!
        String[] elems = elem.split(",");
        if (elems == null || elems.length == 0){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }
        List<ElemCityFC> elemList = new ArrayList<>();
        for (int i = 0; i < elems.length; i++){
            ElemCityFC elemCityFC = ElemCityFC.fromValue(elems[i]);
            if (elemCityFC != null)
                elemList.add(elemCityFC);
        }
        if (elemList.isEmpty()){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        //1.查询给定经纬度附近最近的站点站号
        StationInfoSurfBean stationInfo = stationInfoSurfDao.getNearstStaionFromLatLng(lat, lng, "DM", 0.8f);
        if (stationInfo == null){
            logger.info("给定的经纬度位置" + lat + "," + lng + "附近没有找到站点基本信息!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        JSONOperateTool.putJSONStation(resJSON, stationInfo);
        //2.然后获取数据
        //这里需要查找最新的起报时间
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        DateTime date = DateTime.parse(year + month + day + hour + minute + "00", dateformat);
        DateTime useDate = date.plusHours(3); //CZFC需要向后推3时间(CZFZ来的比较早,比起报时间早3小时来)
        DateTime resDate = FCTimeTool.getFCStartTime(useDate, 0);
        String ryear = String.valueOf(resDate.getYear());
        String rmonth = String.valueOf(resDate.getMonthOfYear());
        String rday = String.valueOf(resDate.getDayOfMonth());
        String rhour = String.valueOf(resDate.getHourOfDay());
        String rminute = String.valueOf(resDate.getMinuteOfHour());

        JSONObject daoJSON = stationDataCityFCDao.getStationCityFC(stationInfo.getStationCode(), ryear, rmonth, rday, rhour, rminute, elemList);
        if (daoJSON != null) {
            resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
        }

        //3.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")){
            logger.info("给定的经纬度" + lat + "," + lng + "及时间要素没有查询到数据!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    @Override
    public JSONObject getCityFCFromCname(String cname, String year, String month, String day, String hour, String minute, String elem) {
        if (minute == null || minute.isEmpty())
            minute = "00";
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = this.initJSON(year, month, day, hour, minute, elem);
        if (resJSON.get(ResJsonConst.STATUSCODE).equals(ResStatus.PARAM_ERROR.getStatusCode())){
            return resJSON;
        }
        //可传入多个要素!
        String[] elems = elem.split(",");
        if (elems == null || elems.length == 0){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }
        List<ElemCityFC> elemList = new ArrayList<>();
        for (int i = 0; i < elems.length; i++){
            ElemCityFC elemCityFC = ElemCityFC.fromValue(elems[i]);
            if (elemCityFC != null)
                elemList.add(elemCityFC);
        }
        if (elemList.isEmpty()){
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
        //这里处理时间，将起报时间归并到08及20时！！
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        DateTime date = DateTime.parse(year + month + day + hour + minute + "00", dateformat);
        DateTime useDate = date.plusHours(3); //CZFC需要向后推3时间(CZFZ来的比较早,比起报时间早3小时来)
        DateTime resDate = FCTimeTool.getFCStartTime(useDate, 0);
        String ryear = String.valueOf(resDate.getYear());
        String rmonth = String.valueOf(resDate.getMonthOfYear());
        String rday = String.valueOf(resDate.getDayOfMonth());
        String rhour = String.valueOf(resDate.getHourOfDay());
        String rminute = String.valueOf(resDate.getMinuteOfHour());

        JSONObject daoJSON = stationDataCityFCDao.getStationCityFC(stationInfo.getStationCode(), ryear, rmonth, rday, rhour, rminute, elemList);
        if (daoJSON != null){
            resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
        }

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

        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    @Override
    public JSONObject getCityFCFromCode(String strCode, String year, String month, String day, String hour, String minute, String elem) {
        //0.初始化JSON对象，并判断参数的正确性
        if (minute == null || minute.isEmpty())
            minute = "00";
        JSONObject resJSON = this.initJSON(year, month, day, hour, minute, elem);
        if (resJSON.get(ResJsonConst.STATUSCODE).equals(ResStatus.PARAM_ERROR.getStatusCode())){
            return resJSON;
        }
        //可传入多个要素!
        String[] elems = elem.split(",");
        if (strCode == null || strCode.isEmpty() || elems == null || elems.length == 0){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }
        List<ElemCityFC> elemList = new ArrayList<>();
        for (int i = 0; i < elems.length; i++){
            ElemCityFC elemCityFC = ElemCityFC.fromValue(elems[i]);
            if (elemCityFC != null)
                elemList.add(elemCityFC);
        }
        if (elemList.isEmpty()){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        //1.然后获取数据
        //这里需要查找最新的起报时间
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmmss");

        DateTime date = DateTime.parse(year + month + day + hour + minute + "00", dateformat);
        DateTime useDate = date.plusHours(3); //CZFC需要向后推3时间(CZFZ来的比较早,比起报时间早3小时来)
        DateTime resDate = FCTimeTool.getFCStartTime(useDate, 0);
        String ryear = String.valueOf(resDate.getYear());
        String rmonth = String.valueOf(resDate.getMonthOfYear());
        String rday = String.valueOf(resDate.getDayOfMonth());
        String rhour = String.valueOf(resDate.getHourOfDay());
        String rminute = String.valueOf(resDate.getMinuteOfHour());

        JSONObject daoJSON = stationDataCityFCDao.getStationCityFC(strCode, ryear, rmonth, rday, rhour, rminute, elemList);
        if (daoJSON != null) {
            resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
        }

        //2.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")){
            logger.info("给定的站号" + strCode + "及时间要素没有查询到城镇预报数据!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    private JSONObject initJSON(String year, String month, String day, String hour, String minute, String elem){
        JSONObject resJSON = new JSONObject();

        resJSON.put(ResJsonConst.DATA, "");
        if (year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2
                || elem == null || elem.isEmpty()){
            JSONOperateTool.putJSONParamError(resJSON);
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
        }
        return resJSON;
    }
}
