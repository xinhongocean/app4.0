package net.xinhong.meteoserve.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ElemSurf;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.common.WeatherElemCalTool;
import net.xinhong.meteoserve.service.dao.AirPortDataSurfDao;

import net.xinhong.meteoserve.service.dao.AirPortInfoSurfDao;
import net.xinhong.meteoserve.service.domain.AirPortInfoBean;
import net.xinhong.meteoserve.service.domain.StationInfoSurfBean;
import net.xinhong.meteoserve.service.service.AirPortDataSurfService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Created by xiaoyu on 16/6/12.
 */
@Service
public class AirPortDataSurfServiceImpl implements AirPortDataSurfService {
    private static final Log logger = LogFactory.getLog(StationDataSurfServiceImpl.class);

    @Autowired
    private AirPortDataSurfDao stationSurfDao;
    @Autowired
    AirPortInfoSurfDao airPortSurfDao;

    @Override
    public JSONObject getDataFromCode(String strCode, String year, String month, String day, String hour, String minute) {
        //0.初始化JSON对象，并判断参数的正确性
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
        if (resJSON.get(ResJsonConst.STATUSCODE).equals(ResStatus.PARAM_ERROR.getStatusCode())){
            return resJSON;
        }
        if (strCode == null || strCode.isEmpty()){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

//        List<AirPortInfoBean> airPortInfoBeenList = airPortSurfDao.getStationInfoFromNameIcao3Icao4(strCode);
//        if (airPortInfoBeenList == null || airPortInfoBeenList.isEmpty()){
//            logger.info("给定的三字码或四字码没有找到机场基本信息!");
//            JSONOperateTool.putJSONNoResult(resJSON);
//            return resJSON;
//        }
//        resJSON.put(ResJsonConst.STATIONCNAME, airPortInfoBeenList.get(0).getCname());

        //1.然后获取数据
        JSONObject daoJSON = stationSurfDao.getAirPortDataSurf(strCode, year, month, day, hour, minute);

        //2.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")){
            logger.info("给定的站号" + strCode + "及时间要素没有查询到机场实况数据!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        //民航数据中天气重新编码
        String strWTH1 = daoJSON.getJSONObject(ResJsonConst.DATA).getString("WTH1");
        int WTH1 = 0; //todo:需确认:没有编报天气现象认为是晴天!
        if (strWTH1 != null && !strWTH1.isEmpty()) {
            WTH1 = WeatherElemCalTool.getAirPortWeatherValueFromCode(strWTH1);
        }
        daoJSON.getJSONObject(ResJsonConst.DATA).put("WTH1", WTH1);
        daoJSON.getJSONObject(ResJsonConst.DATA).put("WTH1C", WeatherElemCalTool.getCHNFromCurWWCode(WTH1, null));

        String strWTH2= daoJSON.getJSONObject(ResJsonConst.DATA).getString("WTH2");
        if (strWTH2 != null && !strWTH2.isEmpty()) {
            int WTH2 = WeatherElemCalTool.getAirPortWeatherValueFromCode(strWTH2);
            daoJSON.getJSONObject(ResJsonConst.DATA).put("WTH2", WTH2);
            daoJSON.getJSONObject(ResJsonConst.DATA).put("WTH2C", WeatherElemCalTool.getCHNFromCurWWCode(WTH2, null));
        }

        String strWTH3 = daoJSON.getJSONObject(ResJsonConst.DATA).getString("WTH3");
        if (strWTH3 != null && !strWTH3.isEmpty()) {
            int WTH3 = WeatherElemCalTool.getAirPortWeatherValueFromCode(strWTH3);
            daoJSON.getJSONObject(ResJsonConst.DATA).put("WTH3", WTH3);
            daoJSON.getJSONObject(ResJsonConst.DATA).put("WTH3C", WeatherElemCalTool.getCHNFromCurWWCode(WTH3, null));
        }

        resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }


    @Override
    public JSONObject getAirPortSeqDataSurf(String stationCode, String year, String month, String day, String hour, String minute, String elem) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();

        resJSON.put(ResJsonConst.DATA, "");
        if (year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2){
            JSONOperateTool.putJSONParamError(resJSON);
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
        }
        if (resJSON.get(ResJsonConst.STATUSCODE).equals(ResStatus.PARAM_ERROR.getStatusCode())){
            return resJSON;
        }
        if (elem == null || elem.isEmpty()
                || stationCode == null || stationCode.isEmpty()){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        List<AirPortInfoBean> airPortInfoBeenList = airPortSurfDao.getStationInfoFromNameIcao3Icao4(stationCode);
        if (airPortInfoBeenList == null || airPortInfoBeenList.isEmpty()){
            logger.info("给定的三字码或四字码没有找到机场基本信息!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        resJSON.put(ResJsonConst.STATIONCNAME, airPortInfoBeenList.get(0).getCname());

        //1.然后获取数据
        JSONObject daoJSON = stationSurfDao.getAirPortSeqDataSurf(stationCode, year, month, day, hour, minute, elem);

        //2.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")){
            logger.info("给定的站号" + stationCode + "及时间要素没有查询到机场实况时序数据!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    @Override
    public JSONObject getAirPortSigmentDataIndexs(String year, String month, String day, String hour, String minute, boolean hasLevel) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();

        resJSON.put(ResJsonConst.DATA, "");
        if (year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2  || minute == null || minute.length() != 2){
            JSONOperateTool.putJSONParamError(resJSON);
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
        }
        if (resJSON.get(ResJsonConst.STATUSCODE).equals(ResStatus.PARAM_ERROR.getStatusCode())){
            return resJSON;
        }


        //1.然后获取数据
        JSONObject daoJSON = stationSurfDao.getAirPortSigmentDataIndexs(year, month, day, hour, minute, hasLevel);

        //2.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")){
            logger.info("给定的时间要素没有查询到机场实况及预报分布情况数据!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    @Override
    public JSONObject getAirPortSigmentData(String strCode, String sigmentType, String year, String month, String day, String hour, String minute) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();

        resJSON.put(ResJsonConst.DATA, "");
        if (year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2
                || minute == null || minute.length() != 2 || strCode == null || strCode.isEmpty()){
            JSONOperateTool.putJSONParamError(resJSON);
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
        }
        if (resJSON.get(ResJsonConst.STATUSCODE).equals(ResStatus.PARAM_ERROR.getStatusCode())){
            return resJSON;
        }


        //1.然后获取数据
        JSONObject daoJSON = stationSurfDao.getAirPortSigmentData(strCode, sigmentType, year, month, day, hour, minute);

        //2.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")){
            logger.info("给定的时间要素及机场四字码没有查询到机场实况及预报详细数据!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

}
