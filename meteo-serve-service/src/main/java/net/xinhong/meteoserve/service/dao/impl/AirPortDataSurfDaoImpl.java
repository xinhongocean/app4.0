package net.xinhong.meteoserve.service.dao.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ElemSurf;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.common.AirportInfoReader;
import net.xinhong.meteoserve.service.dao.AirPortDataSurfDao;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisCluster;

import java.util.*;

/**
 * Created by xiaoyu on 16/6/12.
 */
@Repository
public class AirPortDataSurfDaoImpl implements AirPortDataSurfDao {

    @Autowired
    private JedisCluster jedisCluster;

    @Override
    public JSONObject getAirPortSeqDataSurf(String strCode, String year, String month,
                                            String day, String hour, String minute, String elem){

        if (elem == null || elem.isEmpty())
            return null;

        String[] elems = elem.split(",");
        if (elems.length == 0)
            return null;
        boolean isMultiElem = elems.length > 1;


        SearchResult result = this.searchdata(strCode, year, month, day, hour, minute);
        if (result == null || result.dateimeList == null || result.dateimeList.isEmpty())
            return null;

        Collections.sort(result.dateimeList);
        JSONObject resJson = new JSONObject();
        JSONObject resData = new JSONObject();
        DateTimeFormatter dateformat = DateTimeFormat .forPattern("yyyyMMddHHmm");
        DateTime sTime = null;
        DateTime eTime = null;
        for (int i = 0; i < result.dateimeList.size(); i++){
            JSONObject obj = null;
            String strDayHour = StringUtils.leftPad(String.valueOf(result.dateimeList.get(i).getHourOfDay()), 2, "0")
                    + StringUtils.leftPad(String.valueOf(result.dateimeList.get(i).getMinuteOfHour()), 2, "0");
            if (result.dayDataCurJSON != null &&
                    (result.dateimeList.get(i).getDayOfMonth() == result.curDate.getDayOfMonth())){
                obj = result.dayDataCurJSON.getJSONObject(strDayHour);
            } else if (result.dayDataPreJSON != null &&
                    (result.dateimeList.get(i).getDayOfMonth() == result.preDate.getDayOfMonth())){
                obj = result.dayDataPreJSON.getJSONObject(strDayHour);
            }
            if (obj == null)
                continue;

            for (int j = 0; j < elems.length; j++){
                String strElem = elems[j];
                Object elemVal = obj.get(strElem);
                if (elemVal != null) {
                    if (sTime == null)
                        sTime = result.dateimeList.get(i);

                    if (isMultiElem){ //多要素
                        JSONObject multiElemJson = resData.getJSONObject(result.dateimeList.get(i).plusHours(8).toString(dateformat));
                        if (multiElemJson == null){
                            multiElemJson = new JSONObject();
                        }
                        multiElemJson.put(strElem, elemVal);
                        resData.put(result.dateimeList.get(i).plusHours(8).toString(dateformat), multiElemJson);
                    } else {
                        resData.put(result.dateimeList.get(i).plusHours(8).toString(dateformat), elemVal); //时间皆转为北京时
                    }
                }

                eTime = result.dateimeList.get(i);
            }

//            Object elemVal = obj.get(elem);
//            if (elemVal != null){
//                if (sTime == null)
//                    sTime = result.dateimeList.get(i);
//                DateTime time = result.dateimeList.get(i).plusHours(8);
//                resData.put(time.toString(dateformat), elemVal); //时间皆转为北京时
//                eTime = result.dateimeList.get(i);
//            }

        }
        resJson.put(ResJsonConst.DATA, resData);
        if (sTime != null && eTime != null)
            resJson.put(ResJsonConst.TIME, sTime.plusHours(8).toString(dateformat) + "_"
                    + eTime.plusHours(8).toString(dateformat));
        else
            resJson.put(ResJsonConst.TIME, "");

        return resJson;
    }

    @Override
    public JSONObject getAirPortSigmentDataIndexs(String year, String month, String day, String hour, String minute, boolean hasLevel) {


        year = StringUtils.leftPad(year, 4, "0");
        month = StringUtils.leftPad(month, 2, "0");
        day = StringUtils.leftPad(day, 2, "0");
        hour = StringUtils.leftPad(hour, 2, "0");
        minute = StringUtils.leftPad(minute, 2, "0");

        DateTimeFormatter dateformat = DateTimeFormat .forPattern("yyyyMMddHHmm");
        DateTime date = DateTime.parse(year + month + day + hour + minute, dateformat);


        Set<String> airportSigmentFields = jedisCluster.hkeys(DataTypeConst.AIRPORTSIGMENT_SURF);
        if (airportSigmentFields == null || airportSigmentFields.isEmpty())
            return null;
        Object[] objarray = airportSigmentFields.toArray();
        ArrayList<String> strarray = new ArrayList<String>(objarray.length);
        AirportInfoReader airportInfoReader = new AirportInfoReader();
        for (int i = 0; i < objarray.length; i++){
            String strField = objarray[i].toString();
            if (strField == null)
                continue;

            String[] strRes = strField.split("_");
            if (strRes.length < 1)
                continue;
            if (strRes[0] == null || strRes[0].length() != 4) //第一个为四字码
                continue;

            AirportInfoReader.AirportInfo info = airportInfoReader.getInfoFromCode4(strRes[0]);
            if (info == null)
                continue;
            strField += "_" + info.getLat() + "_" + info.getLng() + "_" + info.getCname();
            if (hasLevel)
                strField += "_" + info.getLevel();
                        strarray.add(strField);
        }

        if (strarray.isEmpty())
            return null;
        JSONObject resJson = new JSONObject();
        resJson.put(ResJsonConst.DATA, strarray);
        resJson.put(ResJsonConst.TIME, date.toString(dateformat));
        return resJson;
    }

    @Override
    public JSONObject getAirPortSigmentData(String strCode, String sigmentType, String year, String month, String day, String hour, String minute) {

        year = StringUtils.leftPad(year, 4, "0");
        month = StringUtils.leftPad(month, 2, "0");
        day = StringUtils.leftPad(day, 2, "0");
        hour = StringUtils.leftPad(hour, 2, "0");
        minute = StringUtils.leftPad(minute, 2, "0");

        if (strCode == null || strCode.isEmpty())  //机场四字码
            return null;
        if (sigmentType == null || sigmentType.isEmpty()) //危险天气类型
            sigmentType = "NONE";

        DateTimeFormatter dateformat = DateTimeFormat .forPattern("yyyyMMddHHmm");
        DateTime date = DateTime.parse(year + month + day + hour + minute, dateformat);


        String  strField = strCode + "_" + date.toString(dateformat) + "00_" + sigmentType;
        String sigmentData = jedisCluster.hget(DataTypeConst.AIRPORTSIGMENT_SURF, strField);
        if (sigmentData == null || sigmentData.isEmpty())
            return null;

        JSONObject resJson = new JSONObject();
        resJson.put(ResJsonConst.DATA, JSONObject.parse(sigmentData));
        resJson.put(ResJsonConst.TIME, date.toString(dateformat));
        return resJson;
    }

    private static class SearchResult{
        List<DateTime> dateimeList;
        JSONObject dayDataCurJSON, dayDataPreJSON;
        DateTime curDate, preDate;
    }

    private SearchResult searchdata(String stationCode, String year, String month, String day, String hour, String minute){

        SearchResult result = new SearchResult();

        year = StringUtils.leftPad(year, 4, "0");
        month = StringUtils.leftPad(month, 2, "0");
        day = StringUtils.leftPad(day, 2, "0");
        hour = StringUtils.leftPad(hour, 2, "0");
        minute = StringUtils.leftPad(minute, 2, "0");

        DateTimeFormatter dateformat = DateTimeFormat .forPattern("yyyyMMddHHmm");
        DateTime date = DateTime.parse(year + month + day + hour + minute, dateformat);
        //这里将给定的时间转换为世界时,并获取指定日期及前一日期的年月日
        result.curDate = date.minusHours(8);
        result.preDate = result.curDate.minusDays(1);
        String cyear = StringUtils.leftPad(String.valueOf(result.curDate.getYear()), 4, "0");
        String cmonth = StringUtils.leftPad(String.valueOf(result.curDate.getMonthOfYear()), 2, "0");
        String cday = StringUtils.leftPad(String.valueOf(result.curDate.getDayOfMonth()), 2, "0");

        String pyear = StringUtils.leftPad(String.valueOf(result.preDate.getYear()), 4, "0");
        String pmonth = StringUtils.leftPad(String.valueOf(result.preDate.getMonthOfYear()), 2, "0");
        String pday = StringUtils.leftPad(String.valueOf(result.preDate.getDayOfMonth()), 2, "0");

        String fieldCur = stationCode + "_" + cyear + cmonth + cday;
        String fieldPre = stationCode + "_" + pyear + pmonth + pday;

        //然后取出当前日及前一天中所有的数据
        String curDayDataStr = jedisCluster.hget(DataTypeConst.AIRPORTDATA_SURF_PREFIX + ":" + cyear + cmonth + cday, fieldCur);
        String preDayDataStr = jedisCluster.hget(DataTypeConst.AIRPORTDATA_SURF_PREFIX + ":" + pyear + pmonth + pday, fieldPre);

        result.dayDataPreJSON = null;

        if (preDayDataStr != null && !preDayDataStr.isEmpty())
            result.dayDataPreJSON = JSONObject.parseObject(preDayDataStr);
        result.dayDataCurJSON = null;
        if (curDayDataStr != null && !curDayDataStr.isEmpty())
            result.dayDataCurJSON = JSONObject.parseObject(curDayDataStr);
        if((result.dayDataCurJSON == null || result.dayDataCurJSON.isEmpty())
                && (result.dayDataPreJSON == null || result.dayDataPreJSON.isEmpty()))
            return result;

        //将查询的结果时间取出，查询出相距最近时次的数据
        result.dateimeList = new ArrayList<>(20);
        if (result.dayDataCurJSON != null){
            for (String curKey : result.dayDataCurJSON.keySet()){ //key 为时分数据
                result.dateimeList.add(DateTime.parse(cyear + cmonth + cday + curKey, dateformat));
            }
        }
        if (result.dayDataPreJSON != null){
            for (String preKey : result.dayDataPreJSON.keySet()){ //key 为时分数据
                result.dateimeList.add(DateTime.parse(pyear + pmonth + pday + preKey, dateformat));
            }
        }
        return result;
    }

    @Override
    public JSONObject getAirPortDataSurf(String stationCode, String year, String month, String day, String hour, String minute) {
        DateTimeFormatter dateformat = DateTimeFormat .forPattern("yyyyMMddHHmm");
        SearchResult result = this.searchdata(stationCode, year, month, day, hour, minute);
        if (result.dateimeList == null || result.dateimeList.isEmpty())
            return null;
        String cyear = StringUtils.leftPad(String.valueOf(result.curDate.getYear()), 4, "0");
        String cmonth = StringUtils.leftPad(String.valueOf(result.curDate.getMonthOfYear()), 2, "0");
        String cday = StringUtils.leftPad(String.valueOf(result.curDate.getDayOfMonth()), 2, "0");

        String pyear = StringUtils.leftPad(String.valueOf(result.preDate.getYear()), 4, "0");
        String pmonth = StringUtils.leftPad(String.valueOf(result.preDate.getMonthOfYear()), 2, "0");
        String pday = StringUtils.leftPad(String.valueOf(result.preDate.getDayOfMonth()), 2, "0");

        //将查询的结果时间取出，查询出相距最近时次的数据
        if (result.dayDataCurJSON != null){
            for (String curKey : result.dayDataCurJSON.keySet()){ //key 为时分数据
                result.dateimeList.add(DateTime.parse(cyear + cmonth + cday + curKey, dateformat));
            }
        }
        if (result.dayDataPreJSON != null){
            for (String preKey : result.dayDataPreJSON.keySet()){ //key 为时分数据
                result.dateimeList.add(DateTime.parse(pyear + pmonth + pday + preKey, dateformat));
            }
        }

        int index = -1;
        long minusSec = 60*60*24; //时差超过24小时，则认为没有查询到数据
        for (int i = 0; i < result.dateimeList.size(); i++){
            long subSec = Math.abs((new Duration(result.curDate, result.dateimeList.get(i))).getStandardSeconds());
            if (subSec < minusSec){
                minusSec = subSec;
                index = i;
            }
        }
        JSONObject resJson = new JSONObject();
        if (index < 0)
            return resJson;

        //找到距离时间最近的一条数据，返回
        DateTime indexDate = result.dateimeList.get(index);
        JSONObject dataJSON ;
        if (indexDate.getDayOfMonth() == result.curDate.getDayOfMonth()){
            dataJSON = result.dayDataCurJSON;
        } else {
            dataJSON = result.dayDataPreJSON;
        }

        resJson.put(ResJsonConst.DATA, dataJSON.get(StringUtils.leftPad(String.valueOf(indexDate.getHourOfDay()), 2, "0")
                + StringUtils.leftPad(String.valueOf(indexDate.getMinuteOfHour()), 2, "0")));
        //返回的时间需要转换为北京时!
        resJson.put(ResJsonConst.TIME, indexDate.plusHours(8).toString(dateformat)+"00");

        return resJson;
    }
}
