package net.xinhong.meteoserve.service.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.common.FCTimeTool;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.dao.FIOFCDataDao;
import net.xinhong.meteoserve.service.service.FIOFCDataService;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by liuso on 2017/11/4.
 */
@Service
public class FIOFCDataServiceImpl implements FIOFCDataService {

    private static final float delta = 0.2f; //数据分辨率

    @Autowired
    private FIOFCDataDao fiofcDataDao;

    @Override
    public JSONObject getPointData(float lat, float lng, String year, String month, String day, String hour, String minute) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();

        resJSON.put(ResJsonConst.DATA, "");
        if (year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
        }

        //1.根据时间计算起报时间及预报时效
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHH");
        DateTime useDate = DateTime.parse(year + month + day + hour, dateformat); //传入为北京时

        FIOResultFCTime restime = getResultTime(useDate);
        if (restime == null) {
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        DateTime resDate = restime.resDate;
        String VTI = restime.resVTI;

        String ryear = resDate.toString("yyyy");
        String rmonth = resDate.toString("MM");
        String rday = resDate.toString("dd");
        String rhour = resDate.toString("HH");

        String strLat = String.format("%.1f", Math.round(lat / delta) * delta);
        String strLng = String.format("%.1f", Math.round(lng / delta) * delta);
        JSONObject daoJSON = fiofcDataDao.getPointData(strLat, strLng, ryear, rmonth, rday, rhour, VTI);

        //3.根据获取结果拼接结果JSON中数据段
        if (isEmptyJSON(daoJSON)) {
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        resJSON.put(ResJsonConst.TIME, daoJSON.get(ResJsonConst.TIME));
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }


    private static class FIOResultFCTime {
        private DateTime resDate;
        private String resVTI;
    }

    /**
     * 根据传入时间,计算FIO起报时间及预报时效
     *
     * @param useDate
     * @return
     */
    private FIOResultFCTime getResultTime(DateTime useDate) {

        DateTime resDate = null;
        int afterMinutes = 22 * 60; //GFS向后推22小时,如20起报第二天下午6点后使用
        //注意:如果传入的是以前的时间,则用该时间计算起报时间,如果是当前或以后的时间,用当前时间计算起报时间!
        if (useDate.isBeforeNow()) {
            resDate = FCTimeTool.getFCStartTimeOnly20(useDate, afterMinutes);
        } else {
            resDate = FCTimeTool.getFCStartTimeOnly20(DateTime.now(), afterMinutes);
        }
        //计算VTI
        long numVTI = (new Duration(resDate, useDate)).getStandardHours();
        //目前FIO最多预报6天，预报间隔为3小时
        if (numVTI > 6*24) {
            return null;
        }
        numVTI = Math.round(numVTI * 1.0f / 3f) * 3;

        String VTI = StringUtils.leftPad(Integer.toString((int) numVTI), 3, "0");
        FIOResultFCTime restime = new FIOResultFCTime();
        restime.resDate = resDate;
        restime.resVTI = VTI;
        return restime;
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

    @Override
    public JSONObject getPointsData(String strLats, String strLngs, String year, String month, String day, String hour, String minute) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();

        resJSON.put(ResJsonConst.DATA, "");
        if (strLats == null || strLats.isEmpty() || strLngs == null || strLngs.isEmpty()
                || year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        String[] strLatAry = strLats.split(",");
        String[] strLngAry = strLngs.split(",");
        if (strLatAry == null || strLatAry.length == 0
                || strLngAry == null || strLngAry.length == 0
                || strLngAry.length != strLatAry.length) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        //1.根据时间计算起报时间及预报时效
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHH");
        DateTime useDate = DateTime.parse(year + month + day + hour, dateformat); //传入为北京时

        FIOResultFCTime restime = getResultTime(useDate);
        if (restime == null) {
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        DateTime resDate = restime.resDate;
        String VTI = restime.resVTI;

        String ryear = resDate.toString("yyyy");
        String rmonth = resDate.toString("MM");
        String rday = resDate.toString("dd");
        String rhour = resDate.toString("HH");
        //循环获取各个点的数据
        JSONArray resDataJSONAry = new JSONArray();
        boolean hasData = false;
        Object timeObj = null;
        for (int i = 0; i < strLatAry.length; i++) {
            if (i >= 100) //最多取100个点数据
                break;
            float lat = Float.parseFloat(strLatAry[i]);
            float lng = Float.parseFloat(strLngAry[i]);
            if (lng < 0)
                lng = 360 + lng; //数据经度为0-360,这里需转换
            if (lat > 90 || lat < -90 || lng > 360 || lng < -180) { //为保证结果能对应,有一个参数不合法则认为整体参数不合法!
                JSONOperateTool.putJSONParamError(resJSON);
                return resJSON;
            }

            String strLat = String.format("%.1f", Math.round(lat / delta) * delta);
            String strLng = String.format("%.1f", Math.round(lng / delta) * delta);
            JSONObject daoJSON = fiofcDataDao.getPointData(strLat, strLng, ryear, rmonth, rday, rhour, VTI);

            if (this.isEmptyJSON(daoJSON)) {
                resDataJSONAry.add(new JSONObject());
            } else {
                resDataJSONAry.add(daoJSON.get(ResJsonConst.DATA));
                hasData = true;
                timeObj = daoJSON.get(ResJsonConst.TIME);
            }
        }

        if (!hasData) {
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        //3.根据获取结果拼接结果JSON中数据段
        resJSON.put(ResJsonConst.TIME, timeObj);
        JSONOperateTool.putJSONSuccessful(resJSON, resDataJSONAry);
        return resJSON;
    }

    @Override
    public JSONObject getIsoLineData(String year, String month, String day, String hour, String minute, String level, String elem, String strArea) {
        return null;
    }

    @Override
    public JSONObject getIsosurfaceData(String year, String month, String day, String hour, String minute, String level, String elem, String strArea) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();

        resJSON.put(ResJsonConst.DATA, "");
        if (year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2
                || level == null || elem == null) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
        }
        if (strArea == null)
            strArea = "EN"; //todo:默认区域？？
        //1.根据时间计算起报时间及预报时效
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHH");
        DateTime useDate = DateTime.parse(year + month + day + hour, dateformat); //传入为北京时

        FIOResultFCTime restime = getResultTime(useDate);
        if (restime == null) {
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        DateTime resDate = restime.resDate;
        String VTI = restime.resVTI;

        String ryear = resDate.toString("yyyy");
        String rmonth = resDate.toString("MM");
        String rday = resDate.toString("dd");
        String rhour = resDate.toString("HH");

        JSONObject daoJSON = fiofcDataDao.getIsosurfaceData(ryear, rmonth, rday, rhour, VTI, level, elem, strArea);

        //3.根据获取结果拼接结果JSON中数据段
        if (this.isEmptyJSON(daoJSON)) {
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        resJSON.put(ResJsonConst.TIME, daoJSON.get(ResJsonConst.TIME));
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    @Override
    public JSONObject getSpaceProfileData(String strLats, String strLngs, String year, String month, String day, String hour, String minute) {
        return null;
    }

    @Override
    public JSONObject getTimeProfileData(String strLat, String strLng, String year, String month, String day, String hour, String minute) {
        return null;
    }

    @Override
    public JSONObject getAreaData(String year, String month, String day, String hour, String minute, String level, float sLat, float eLat, float sLng, float eLng, String elem, String strArea) {
        return null;
    }
}
