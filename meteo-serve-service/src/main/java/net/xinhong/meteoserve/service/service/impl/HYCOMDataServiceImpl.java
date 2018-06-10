package net.xinhong.meteoserve.service.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xinhong.mids3d.util.math.MIDS3DMath;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.common.FCTimeTool;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.dao.GFSFCDataDao;
import net.xinhong.meteoserve.service.dao.HYCOMDataDao;
import net.xinhong.meteoserve.service.service.HYCOMDataService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by wingsby on 2017/12/28.
 */
@Service
public class HYCOMDataServiceImpl implements HYCOMDataService {
    private static final Log logger = LogFactory.getLog(HYCOMDataServiceImpl.class);
    static final  float delta=0.24f;
    static final float datasLat=-40f;
    @Autowired
    private HYCOMDataDao hycomDataDao;
    @Override
    public JSONObject getPointData(String lat, String lng, String year, String month,
                                   String day, String hour) {
        JSONObject resJSON = new JSONObject();
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
        float flat = Float.parseFloat(lat);
        float flng = Float.parseFloat(lng);
        if (flat > 90 || flat < -90 || flng > 360 || flng < -180) { //为保证结果能对应,有一个参数不合法则认为整体参数不合法!
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }
        if (flng < 0) flng+=360;
        String strLat = String.format("%.2f",
                Math.round(Math.round((flat-datasLat)*1000)/(delta*1000))*delta+datasLat);
        String strLng = String.format("%.2f",
                Math.round(Math.round(flng*1000)/(delta*1000))*delta);
        JSONObject daoJSON=hycomDataDao.getPointData(strLat,strLng,ryear,rmonth,rday,rhour,VTI);
        if (daoJSON==null||daoJSON.isEmpty()||daoJSON.getString(ResJsonConst.DATA) == null
                ||daoJSON.getString(ResJsonConst.DATA).isEmpty()){
            JSONOperateTool.putJSONNoResult(daoJSON);
            return daoJSON;
        }
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.getJSONObject(ResJsonConst.DATA));
        resJSON.put(ResJsonConst.TIME,daoJSON.get(ResJsonConst.TIME));
        return resJSON;
    }

    @Override
    public JSONObject getImg(String year, String month, String day, String hour, String depth, String eles) {
        JSONObject resJSON = new JSONObject();
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
        JSONObject dataJSON=hycomDataDao.getImg(ryear,rmonth,rday,rhour,VTI,depth,eles);
        if (dataJSON==null||dataJSON.isEmpty()||dataJSON.getString(ResJsonConst.DATA) == null
                ||dataJSON.getString(ResJsonConst.DATA).isEmpty()){
            JSONOperateTool.putJSONNoResult(dataJSON);
            return dataJSON;
        }
        JSONOperateTool.putJSONSuccessful(resJSON, dataJSON.get(ResJsonConst.DATA));
        resJSON.put(ResJsonConst.TIME,dataJSON.get(ResJsonConst.TIME));
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

        HYCOMDataServiceImpl.ResultTime restime = getResultTime(useDate);
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
            String strLat = String.format("%.2f",
                    Math.round(Math.round((lat-datasLat)*1000)/(delta*1000))*delta+datasLat);
            String strLng = String.format("%.2f",
                    Math.round(Math.round(lng*1000)/(delta*1000))*delta);
            JSONObject daoJSON = hycomDataDao.getPointData(strLat, strLng, ryear, rmonth, rday, rhour, VTI);
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

        HYCOMDataServiceImpl.ResultTime restime = getResultTime(useDate);
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
        int hourNum = 180;
        int hourDelta = 3;
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
        for (int i = 0; i < hourNum; i+=3) {
            String resLat = String.format("%.2f",
                    Math.round(Math.round((lat-datasLat)*1000)/(delta*1000))*delta+datasLat);
            String resLng = String.format("%.2f",
                    Math.round(Math.round(lng*1000)/(delta*1000))*delta);
            JSONObject daoJSON = hycomDataDao.getPointData(resLat, resLng, ryear, rmonth, rday, rhour, VTI);
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



    private HYCOMDataServiceImpl.ResultTime getResultTime(DateTime useDate) {

        //  hycome  次日27时生效
        DateTime resDate = null;
        int afterMinutes = 27 * 60; //hycom向后推27小时
        //注意:如果传入的是以前的时间,则用该时间计算起报时间,如果是当前或以后的时间,用当前时间计算起报时间!
        if (useDate.isBeforeNow()) {
            resDate = FCTimeTool.getHYFCStartTime(useDate, afterMinutes);
        } else {
            resDate = FCTimeTool.getHYFCStartTime(DateTime.now(), afterMinutes);
        }
        //计算VTI
        long numVTI = (new Duration(resDate, useDate)).getStandardHours();
        //目前VTI:0-6, 72-96均为3小时间隔,其余为1小时间隔

        if (numVTI > 240) {
            return null;
        }

        if (numVTI <= 48) {
            numVTI = (int)Math.ceil(numVTI * 1.0f / 3f) * 3;
        }
        if (numVTI > 48&&numVTI<96) {
            numVTI = (int)Math.ceil(numVTI * 1.0f / 3f) * 6;
        }
        if (numVTI > 96) {
            numVTI = (int)Math.ceil(numVTI * 1.0f / 3f) * 24;
        }
        String VTI = StringUtils.leftPad(Integer.toString((int) numVTI), 3, "0");
        HYCOMDataServiceImpl.ResultTime restime = new HYCOMDataServiceImpl.ResultTime();
        restime.resDate = resDate;
        restime.resVTI = VTI;
        return restime;
    }

    private static class ResultTime {
        private DateTime resDate;
        private String resVTI;
    }
}
