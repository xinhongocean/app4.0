package net.xinhong.meteoserve.service.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xinhong.mids3d.util.math.MIDS3DMath;
import net.xinhong.meteoserve.common.constant.ElemGFS;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.common.FCTimeTool;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.dao.GFSFCDataDao;
import net.xinhong.meteoserve.service.service.GFSFCDataService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xiaoyu on 16/7/22.
 */
@Service
public class GFSFCDataServiceImpl implements GFSFCDataService {
    private static final Log logger = LogFactory.getLog(StationDataSurfServiceImpl.class);
    private static final float delta = 0.5f; //数据分辨率
    @Autowired
    private GFSFCDataDao gfsfcDataDao;


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

        ResultTime restime = getResultTime(useDate);
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
            String strLat = String.format("%.2f", Math.round(lat / delta) * delta);
            String strLng = String.format("%.2f", Math.round(lng / delta) * delta);
            JSONObject daoJSON = gfsfcDataDao.getPointData(strLat, strLng, ryear, rmonth, rday, rhour, VTI);

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
    public JSONObject getPointHoursConvectionData(float lat, float lng, String year, String month, String day, String hour, int hourNum) {
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

        if  (hourNum < 1) {
            hourNum = 1;
        }
        if (hourNum > 144){
            hourNum = 144;
        }

        //1.根据时间计算起报时间及预报时效
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHH");
        DateTime useDate = DateTime.parse(year + month + day + hour, dateformat); //传入为北京时

        ResultTime restime = getResultTime(useDate);
        if (restime == null) {
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        DateTime resDate = restime.resDate;
        String VTI = restime.resVTI;
        Integer VTINum = Integer.parseInt(VTI);

        String ryear = resDate.toString("yyyy");
        String rmonth = resDate.toString("MM");
        String rday = resDate.toString("dd");
        String rhour = resDate.toString("HH");

        String strLat = String.format("%.2f", Math.round(lat / delta) * delta);
        String strLng = String.format("%.2f", Math.round(lng / delta) * delta);

        boolean hasData = false;
        JSONObject dataJSON = new JSONObject();
        for (int i = 0; i < hourNum; i++){
            JSONObject daoJSON = gfsfcDataDao.getPointData(strLat, strLng, ryear, rmonth, rday, rhour, VTINum.toString());
            //3.根据获取结果拼接结果JSON中数据段
            if (daoJSON == null || daoJSON.isEmpty()
                    || (daoJSON.getString(ResJsonConst.DATA) == null)
                    || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                    || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                    || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")) {
                continue;
            }
            hasData = true;
            //只取地面数据
            JSONObject hourDataJSON = new JSONObject();
            JSONObject gfs = daoJSON.getJSONObject(ResJsonConst.DATA).getJSONObject("GFS");
            if ((gfs != null) && (gfs.getJSONObject("9999") != null)){
                JSONObject tmpJSON = gfs.getJSONObject("9999");
                for (String key : tmpJSON.keySet()){
                    hourDataJSON.put(key, tmpJSON.get(key));
                }
            }
            JSONObject danger = daoJSON.getJSONObject(ResJsonConst.DATA).getJSONObject("DANGER");
            if ((danger != null) && (danger.getJSONObject("9999") != null)){
                JSONObject tmpJSON = danger.getJSONObject("9999");
                for (String key : tmpJSON.keySet()){
                    hourDataJSON.put(key, tmpJSON.get(key));
                }
            }
            String timeKey = daoJSON.getString(ResJsonConst.TIME);
            String timeRes = timeKey.split("_")[0];
            DateTime tmpTime = DateTime.parse(timeRes,
                    DateTimeFormat.forPattern("yyyyMMddHHmm")).plusHours(Integer.parseInt(timeKey.split("_")[1]));
            dataJSON.put(tmpTime.toString("yyyyMMddHHmm"), hourDataJSON);
            VTINum++;
        }
        if (!hasData){
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        resJSON.put(ResJsonConst.TIME, useDate.toString(("yyyyMMddHH")));
        JSONOperateTool.putJSONSuccessful(resJSON, dataJSON);
        return resJSON;
    }

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

        ResultTime restime = getResultTime(useDate);
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

        String strLat = String.format("%.2f", Math.round(lat / delta) * delta);
        String strLng = String.format("%.2f", Math.round(lng / delta) * delta);
        JSONObject daoJSON = gfsfcDataDao.getPointData(strLat, strLng, ryear, rmonth, rday, rhour, VTI);

        //3.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")) {
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        resJSON.put(ResJsonConst.TIME, daoJSON.get(ResJsonConst.TIME));
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    @Override
    public JSONObject getIsoLineData(String year, String month, String day, String hour, String minute, String level, String elem, String strArea) {
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
            strArea = "EN"; //默认东北半球数据
        //1.根据时间计算起报时间及预报时效
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHH");
        DateTime useDate = DateTime.parse(year + month + day + hour, dateformat); //传入为北京时

        ResultTime restime = getResultTime(useDate);
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

        JSONObject daoJSON = gfsfcDataDao.getIsolineData(ryear, rmonth, rday, rhour, VTI, level, elem, strArea);

        //3.根据获取结果拼接结果JSON中数据段
        if (this.isEmptyJSON(daoJSON)) {
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        resJSON.put(ResJsonConst.TIME, daoJSON.get(ResJsonConst.TIME));
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }


    private static class ResultTime {
        private DateTime resDate;
        private String resVTI;
    }

    /**
     * 根据传入时间,计算GFS起报时间及预报时效
     *
     * @param useDate
     * @return
     */
    private ResultTime getResultTime(DateTime useDate) {

        DateTime resDate = null;
        int afterMinutes = 10 * 60; //GFS向后推8小时,如08起报下午6点后使用
        //注意:如果传入的是以前的时间,则用该时间计算起报时间,如果是当前或以后的时间,用当前时间计算起报时间!
        if (useDate.isBeforeNow()) {
            resDate = FCTimeTool.getFCStartTime(useDate, afterMinutes);
        } else {
            resDate = FCTimeTool.getFCStartTime(DateTime.now(), afterMinutes);
        }
        //计算VTI
        long numVTI = (new Duration(resDate, useDate)).getStandardHours();
        //目前VTI:0-6, 72-96均为3小时间隔,其余为1小时间隔
        if (numVTI > 240) {
            return null;
        }
        if (numVTI < 6 || numVTI > 72) {
            numVTI = Math.round(numVTI * 1.0f / 3f) * 3;
        }
        String VTI = StringUtils.leftPad(Integer.toString((int) numVTI), 3, "0");
        ResultTime restime = new ResultTime();
        restime.resDate = resDate;
        restime.resVTI = VTI;
        return restime;
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
            strArea = "EN"; //默认东北半球数据
        //1.根据时间计算起报时间及预报时效
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHH");
        DateTime useDate = DateTime.parse(year + month + day + hour, dateformat); //传入为北京时

        ResultTime restime = getResultTime(useDate);
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

        JSONObject daoJSON = gfsfcDataDao.getIsosurfaceData(ryear, rmonth, rday, rhour, VTI, level, elem, strArea);

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
    public JSONObject getSpaceProfileData(String strLats, String strLngs, String year, String month, String day,
                                          String hour, String minute) {
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
        if (strLatAry == null || strLatAry.length < 2
                || strLngAry == null || strLngAry.length < 2
                || strLngAry.length != strLatAry.length) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }
        //1.根据时间计算起报时间及预报时效
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHH");
        DateTime useDate = DateTime.parse(year + month + day + hour, dateformat); //传入为北京时

        ResultTime restime = getResultTime(useDate);
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

        int maxNum = 25;

        //2.计算剖面各个点的位置

        float[][] profileLnglats = null;
        //如果有多于2个点,则每两个点计算插值点,插值点总数不超过maxNum
        //插值数按照每两个点距离与整体距离之比进行计算
        double totaldist = 0f;
        if (strLatAry.length > 2){
            for (int i = 0; i < strLatAry.length-1; i++){
                float startX = Float.parseFloat(strLngAry[i]);
                float startY = Float.parseFloat(strLatAry[i]);
                float endX = Float.parseFloat(strLngAry[i+1]);
                float endY = Float.parseFloat(strLatAry[i+1]);
                totaldist += Math.sqrt((endX - startX)*(endX - startX) + (endY - startY)*(endY - startY));
            }
        }

        for (int i = 0; i < strLatAry.length-1; i++){
            float startX = Float.parseFloat(strLngAry[i]);
            float startY = Float.parseFloat(strLatAry[i]);
            float endX = Float.parseFloat(strLngAry[i+1]);
            float endY = Float.parseFloat(strLatAry[i+1]);
            int insertNum = maxNum;
            if (totaldist > 0){
                double ratio = Math.sqrt((endX - startX)*(endX - startX) + (endY - startY)*(endY - startY))/totaldist;
                insertNum = (int)Math.round(maxNum * ratio);
                if (insertNum < 2)
                    insertNum = 2;
            }
            float[][] tempprofileLnglats = MIDS3DMath.getInsertProfileDatas(startX, startY, endX, endY, insertNum);

         //   System.out.println(insertNum + "," + tempprofileLnglats.length);
            if (tempprofileLnglats == null || tempprofileLnglats.length < 2) {
                continue;
            }
            int xlen = 0;
            int ylen = tempprofileLnglats[0].length;
            if (profileLnglats == null || profileLnglats.length == 0){
                profileLnglats = new float[tempprofileLnglats.length][tempprofileLnglats[0].length];

            } else {
                xlen = profileLnglats.length-1; //舍去前一个剖面的最后一个点,该点与下一个剖面的点重复
                float[][] oldprofileLnglats = profileLnglats;
                profileLnglats = new float[xlen + tempprofileLnglats.length][ylen];

                for (int m = 0; m < xlen; m++){
                    for (int n = 0; n < ylen; n++){
                        profileLnglats[m][n] = oldprofileLnglats[m][n];
                    }
                }
            }
            for (int m = 0; m < tempprofileLnglats.length; m++){
                for (int n = 0; n < ylen; n++){
                    profileLnglats[xlen + m][n] = tempprofileLnglats[m][n];
                }
            }
        }

        if (profileLnglats == null || profileLnglats.length < 2) {
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }


        //循环获取各个点的数据
        JSONArray resDataJSONAry = new JSONArray();
        boolean hasData = false;
        Object timeObj = null;

        JSONArray resLatLngJSONAry = new JSONArray();
        for (int i = 0; i < profileLnglats.length; i++) {
            if (i >= 100) //最多取100个点数据
                break;
            float lng = profileLnglats[i][0];
            float lat = profileLnglats[i][1];
            if (lng < 0)
                lng = 360 + lng; //数据经度为0-360,这里需转换
            if (lat > 90 || lat < -90 || lng > 360 || lng < -180) { //为保证结果能对应,有一个参数不合法则认为整体参数不合法!
                JSONOperateTool.putJSONParamError(resJSON);
                return resJSON;
            }
            String strLat = String.format("%.2f", Math.round(lat / delta) * delta);
            String strLng = String.format("%.2f", Math.round(lng / delta) * delta);
            JSONObject daoJSON = gfsfcDataDao.getPointData(strLat, strLng, ryear, rmonth, rday, rhour, VTI);

            resLatLngJSONAry.add(String.format("%.2f", lat) + "," + String.format("%.2f", lng));
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

        JSONObject dataJSON = new JSONObject();
        dataJSON.put("latlngs", resLatLngJSONAry);
        dataJSON.put("profiledatas", resDataJSONAry);

        //3.根据获取结果拼接结果JSON中数据段
        resJSON.put(ResJsonConst.TIME, timeObj);
        JSONOperateTool.putJSONSuccessful(resJSON, dataJSON);
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


    @Override
    public JSONObject getTimeProfileData(String strLat, String strLng, String year, String month, String day,
                                         String hour, String minute) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();

        resJSON.put(ResJsonConst.DATA, "");
        if (strLat == null || strLat.isEmpty() || strLng == null || strLng.isEmpty()
                || year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }


        //1.根据时间计算起报时间及预报时效
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHH");
        DateTime useDate = DateTime.parse(year + month + day + hour, dateformat); //传入为北京时

        ResultTime restime = getResultTime(useDate);
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

        //2.计算未来时间序列(未来一天隔1小时)
        int hourNum = 24;
        int hourDelta = 1;
        float lat = Float.parseFloat(strLat);
        float lng = Float.parseFloat(strLng);
        if (lat > 90 || lat < -90 || lng > 360 || lng < -180) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        //3.循环获取各个预报时效的数据
        JSONArray resDataJSONAry = new JSONArray();
        JSONArray resTimeJSONAry = new JSONArray();
        boolean hasData = false;
        Object timeObj = null;
        for (int i = 0; i < hourNum; i++) {
            String resLat = String.format("%.2f", Math.round(lat / delta) * delta);
            String resLng = String.format("%.2f", Math.round(lng / delta) * delta);
            JSONObject daoJSON = gfsfcDataDao.getPointData(resLat, resLng, ryear, rmonth, rday, rhour, VTI);

            DateTime showTime = resDate.plusHours(Integer.parseInt(VTI));
            resTimeJSONAry.add(showTime.toString(dateformat));
            if (daoJSON == null || daoJSON.isEmpty()
                    || (daoJSON.getString(ResJsonConst.DATA) == null)
                    || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                    || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                    || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")) {
                resDataJSONAry.add(new JSONObject());
            } else {
                resDataJSONAry.add(daoJSON.get(ResJsonConst.DATA));
                hasData = true;
                if (timeObj == null)
                    timeObj = daoJSON.get(ResJsonConst.TIME);
            }
            int vtiNum = Integer.parseInt(VTI) + hourDelta;
            VTI = StringUtils.leftPad(vtiNum + "", 3, "0");
        }

        if (!hasData) {
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        JSONObject dataJSON = new JSONObject();
        dataJSON.put("times", resTimeJSONAry);
        dataJSON.put("profiledatas", resDataJSONAry);

        //3.根据获取结果拼接结果JSON中数据段
        resJSON.put(ResJsonConst.TIME, timeObj);
        JSONOperateTool.putJSONSuccessful(resJSON, dataJSON);
        return resJSON;
    }

    @Override
    public JSONObject getAreaData(String year, String month, String day, String hour, String minute, String level,
                                  float sLat, float eLat, float sLng, float eLng, String elem, String strArea) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();

        resJSON.put(ResJsonConst.DATA, "");
        if (sLat > 90 || sLat < -90 || sLng > 360 || sLng < -180 ||
                eLat > 90 || eLat < -90 || eLng > 360 || eLng < -180 ||
                sLat > eLat || sLng > eLng) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        ElemGFS elemGFS = ElemGFS.fromFileCode(elem);
        if (year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2
                || level == null || elem == null || elemGFS == null) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
        }
        if (strArea == null)
            strArea = "EN"; //默认东北半球数据

        //1.根据时间计算起报时间及预报时效
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHH");
        DateTime useDate = DateTime.parse(year + month + day + hour, dateformat); //传入为北京时

        ResultTime restime = getResultTime(useDate);
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

        JSONObject daoJSON = gfsfcDataDao.getAreaData(ryear, rmonth, rday, rhour, VTI, level, elemGFS, strArea);
        if (this.isEmptyJSON(daoJSON)){ //找前一个起报时间

            resDate = resDate.minusHours(12);
            VTI = StringUtils.leftPad(Integer.parseInt(VTI)+12+"", 3, '0');
            ryear = resDate.toString("yyyy");
            rmonth = resDate.toString("MM");
            rday = resDate.toString("dd");
            rhour = resDate.toString("HH");
            daoJSON = gfsfcDataDao.getAreaData(ryear, rmonth, rday, rhour, VTI, level, elemGFS, strArea);
        }

        //3.根据获取结果拼接结果JSON中数据段
        if (this.isEmptyJSON(daoJSON)) {
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        resJSON.put(ResJsonConst.TIME, daoJSON.get(ResJsonConst.TIME));
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }


}
