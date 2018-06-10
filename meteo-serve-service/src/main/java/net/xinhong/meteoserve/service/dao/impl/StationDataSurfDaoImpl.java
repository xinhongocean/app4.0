package net.xinhong.meteoserve.service.dao.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ElemSurf;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.tool.*;
import net.xinhong.meteoserve.service.common.DataColumnUtil;
import net.xinhong.meteoserve.service.common.ResultDataReprocessor;
import net.xinhong.meteoserve.service.dao.StationDataSurfDao;
import net.xinhong.meteoserve.service.dao.StationInfoSurfDao;
import net.xinhong.meteoserve.service.domain.*;
import org.dom4j.Element;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import redis.clients.jedis.JedisCluster;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Description: <br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 <a href=mailto:liusofttech@sina.com>刘晓昌</a>
 * @version 创建时间：2016/3/3.
 */
@Repository
public class StationDataSurfDaoImpl extends DaoSupport implements StationDataSurfDao {
    @Autowired
    private JedisCluster jedisCluster;

    private static final String NAMESPACE = "net.xinhong.meteoserve.service.aqi";

    @Autowired
    private SqlSessionTemplate sqlSession;

    @Autowired
    private StationInfoSurfDao stationInfoSurfDao;

    //地面实况数据文件路径key
    public static final String DM_DATA_FILE_PATH = "/xinhong/meteo_data/duts_data/dutsdata/DM/";
  //  public static final String DM_DATA_FILE_PATH = "/Users/xiaoyu/DM/";
    //地面实况数据文件前缀
    public static final String DM_DATA_FILE_PREFIX = "DM";


    private static final String DM_COLUMNS_JSON = "dm_columns.json";

    @Override
    public StationDataSurfBean getStationSurf(String stationCode, String year, String month, String day, String hour, String minute) {
        StationDataSurfBean stationSurf = new StationDataSurfBean();
        stationSurf.setId("54511");
        stationSurf.setName("北京");
        stationSurf.setLat(42.55f);
        stationSurf.setLng(113.78f);
        stationSurf.setPS(1005.2f);
        stationSurf.setTT(24.5f);
        stationSurf.setRAIN24(10.5f);
        stationSurf.setWD("西北风");

        return stationSurf;
    }

    @Override
    public JSONObject getStationSeqDataSurf(String stationCode, String year, String month,
                                            String day, String hour, String minute, String elem) {

        if (elem == null || elem.trim().isEmpty())
            return null;
        SearchResult result = this.searchdata(stationCode, year, month, day, hour, minute);
        if (result == null || result.dateimeList == null || result.dateimeList.isEmpty())
            return null;

        String[] elems = elem.split(",");
        if (elems.length == 0)
            return null;
        boolean isMultiElem = elems.length > 1;

        Collections.sort(result.dateimeList);
        JSONObject resJson = new JSONObject();
        JSONObject resData = new JSONObject();
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmm");
        DateTime sTime = null;
        DateTime eTime = null;

        for (int i = 0; i < result.dateimeList.size(); i++) {
            JSONObject obj = null;
            String strDayHour = StringUtils.leftPad(String.valueOf(result.dateimeList.get(i).getHourOfDay()), 2, "0")
                    + StringUtils.leftPad(String.valueOf(result.dateimeList.get(i).getMinuteOfHour()), 2, "0");
            if (result.dayDataCurJSON != null &&
                    (result.dateimeList.get(i).getDayOfMonth() == result.curDate.getDayOfMonth())) {
                obj = result.dayDataCurJSON.getJSONObject(strDayHour);
            } else if (result.dayDataPreJSON != null) {
                obj = result.dayDataPreJSON.getJSONObject(strDayHour);
            }
            if (obj == null)
                continue;
            for (int j = 0; j < elems.length; j++){
                String strElem = elems[j];
//                if ( ElemSurf.valueOf(strElem) == null)
//                    continue;
                Object elemVal = obj.get(strElem);
                if (elemVal != null) {
                    if (sTime == null)
                        sTime = result.dateimeList.get(i);

                    //todo:云量为9时,为天空不明(X号),云量为null时,为无观测,改为10!
                    if (strElem.equals(ElemSurf.CN.getEname())
                            || strElem.equals(ElemSurf.CNML.getEname())) {
                        Integer code = obj.getInteger(elem);
                        if (code == null) {
                            elemVal = 10;
                        }
                    }

                    if (isMultiElem){
                        JSONObject elemJson = resData.getJSONObject(result.dateimeList.get(i).plusHours(8).toString(dateformat));
                        if (elemJson == null){
                            elemJson = new JSONObject();
                        }
                        elemJson.put(strElem, elemVal);
                        resData.put(result.dateimeList.get(i).plusHours(8).toString(dateformat), elemJson);
                    } else {
                        resData.put(result.dateimeList.get(i).plusHours(8).toString(dateformat), elemVal); //时间皆转为北京时
                    }
                }

                eTime = result.dateimeList.get(i);
            }
        }
        resJson.put(ResJsonConst.DATA, resData);
        //返回的时间需要转换为北京时!
        if (sTime != null && eTime != null)
            resJson.put(ResJsonConst.TIME, sTime.plusHours(8).toString(dateformat) + "_"
                    + eTime.plusHours(8).toString(dateformat));
        else
            resJson.put(ResJsonConst.TIME, "");

        return resJson;
    }


    @Override
    public JSONObject searchCityIDAQIData(String cityCode, String year, String month, String day, String hour, String minute) {
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmm");
        DateTime date = DateTime.parse(year + month + day + hour + minute, dateformat);
        Map<String, Object> params = new HashMap<>();
        params.put("citycode", cityCode);
        params.put("limit", 12);
        List<AQIDataBean> AQIDataBeanList = this.sqlSession.selectList(this.NAMESPACE + ".selectbycitycode", params);
        if (AQIDataBeanList == null || AQIDataBeanList.isEmpty()) {
            return null;
        }

        for (AQIDataBean data : AQIDataBeanList){
            if (data != null){
                if (data.getPm10().equals("—")) data.setPm10(null);
            }
        }

        String lasttime = AQIDataBeanList.get(0).getIssueTime();
        String curTime = lasttime.substring(0, 4) + lasttime.substring(5, 7) + lasttime.substring(8, 10)
                + lasttime.substring(11, 13) + lasttime.substring(14, 16);

        JSONObject resData = new JSONObject();
        resData.put("cname", AQIDataBeanList.get(0).getCityName());
        resData.put("aqidatalist", AQIDataBeanList);

        JSONObject resJSON = new JSONObject();
        resJSON.put(ResJsonConst.DATA, resData);
        //返回的时间需要转换为北京时!
        resJSON.put(ResJsonConst.TIME, curTime);
        return resJSON;
    }


    @Override
    public JSONObject searchStationAQIData(String stationCode, String year, String month, String day, String hour, String minute) {

        //首先根据站号查询对应的城市编码
        StationInfoSurfBean stationInfo = this.stationInfoSurfDao.getStationInfoFromCode(stationCode);
        if (stationInfo == null || stationInfo.getCityCode() == null || stationInfo.getCityCode().isEmpty()) {
            logger.info("给定的站号" + stationCode + "没有查询到对应的城市编码!");
            return null;
        }

        String cityCode = stationInfo.getCityCode();

        //然后查询对应的站号数据
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmm");
        DateTime date = DateTime.parse(year + month + day + hour + minute, dateformat);
        Map<String, Object> params = new HashMap<>();
        params.put("citycode", cityCode);
        params.put("limit", 12);
        List<AQIDataBean> AQIDataBeanList = this.sqlSession.selectList(this.NAMESPACE + ".selectbycitycode", params);
        if (AQIDataBeanList == null || AQIDataBeanList.isEmpty()) {
            return null;
        }

        for (AQIDataBean data : AQIDataBeanList){
            if (data != null){
                if (data.getPm10().equals("—")) data.setPm10(null);
            }
        }

        String lasttime = AQIDataBeanList.get(0).getIssueTime();
        String curTime = lasttime.substring(0, 4) + lasttime.substring(5, 7) + lasttime.substring(8, 10)
                + lasttime.substring(11, 13) + lasttime.substring(14, 16);

        JSONObject resData = new JSONObject();
        resData.put("cname", stationInfo.getCname());
        resData.put("stationcode", stationInfo.getStationCode());
        resData.put("aqidatalist", AQIDataBeanList);

        JSONObject resJSON = new JSONObject();
        resJSON.put(ResJsonConst.DATA, resData);
        //返回的时间需要转换为北京时!
        resJSON.put(ResJsonConst.TIME, curTime);
        return resJSON;
    }

    @Override
    public JSONObject searchAQIDistList(Integer dlevel, String year, String month, String day, String hour, String minute) {

        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmm");
        DateTime date = DateTime.parse(year + month + day + hour + minute, dateformat);

        Map<String, Object> params = new HashMap<>();
        params.put("issue_time", date);
        params.put("dlevel", dlevel);
        List<AQIListBean> AQIDataBeanList = this.sqlSession.selectList(this.NAMESPACE + ".selectaqilist", params);
        if (AQIDataBeanList == null || AQIDataBeanList.isEmpty()) {
            return null;
        }

        //转换为JSON数组
        JSONArray resarray = new JSONArray();
        String desc = "city_code, lat, lng, dlv, aqi";
        for (AQIListBean aqi : AQIDataBeanList){
            JSONArray array = new JSONArray();
            array.add(aqi.getCityCode());
            array.add(aqi.getLat());
            array.add(aqi.getLng());
            array.add(aqi.getDlv());
            array.add(aqi.getAqi());
            resarray.add(array);
        }
        JSONObject data = new JSONObject();
        data.put("desc", desc);
        data.put("array", resarray);

        JSONObject resJSON = new JSONObject();
        resJSON.put(ResJsonConst.DATA, data);
        resJSON.put(ResJsonConst.TIME, date.minusHours(1).toString(dateformat));
        return resJSON;
    }


    private static class SearchResult {
        List<DateTime> dateimeList;
        JSONObject dayDataCurJSON, dayDataPreJSON;
        DateTime curDate, preDate;
    }

    private SearchResult searchdata(String stationCode, String year, String month, String day, String hour, String minute) {

        SearchResult result = new SearchResult();

        year = StringUtils.leftPad(year, 4, "0");
        month = StringUtils.leftPad(month, 2, "0");
        day = StringUtils.leftPad(day, 2, "0");
        hour = StringUtils.leftPad(hour, 2, "0");
        minute = StringUtils.leftPad(minute, 2, "0");

        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmm");
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
        String curDayDataStr = jedisCluster.hget(DataTypeConst.STATIONDATA_SURF_PREFIX + ":" + cyear + cmonth + cday, fieldCur);
        String preDayDataStr = jedisCluster.hget(DataTypeConst.STATIONDATA_SURF_PREFIX + ":" + pyear + pmonth + pday, fieldPre);

        result.dayDataPreJSON = null;

        if (preDayDataStr != null && !preDayDataStr.isEmpty())
            result.dayDataPreJSON = JSONObject.parseObject(preDayDataStr);
        result.dayDataCurJSON = null;
        if (curDayDataStr != null && !curDayDataStr.isEmpty())
            result.dayDataCurJSON = JSONObject.parseObject(curDayDataStr);
        if ((result.dayDataCurJSON == null || result.dayDataCurJSON.isEmpty())
                && (result.dayDataPreJSON == null || result.dayDataPreJSON.isEmpty()))
            return result;

        //将查询的结果时间取出，查询出相距最近时次的数据
        result.dateimeList = new ArrayList<>(20);
        if (result.dayDataCurJSON != null) {
            for (String curKey : result.dayDataCurJSON.keySet()) { //key 为时分数据
                result.dateimeList.add(DateTime.parse(cyear + cmonth + cday + curKey, dateformat));
            }
        }
        if (result.dayDataPreJSON != null) {
            for (String preKey : result.dayDataPreJSON.keySet()) { //key 为时分数据
                result.dateimeList.add(DateTime.parse(pyear + pmonth + pday + preKey, dateformat));
            }
        }
        return result;
    }

    @Override
    public JSONObject getStationDataSurf(String stationCode, String year, String month, String day, String hour, String minute) {

        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmm");
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
        if (result.dayDataCurJSON != null) {
            for (String curKey : result.dayDataCurJSON.keySet()) { //key 为时分数据
                result.dateimeList.add(DateTime.parse(cyear + cmonth + cday + curKey, dateformat));
            }
        }
        if (result.dayDataPreJSON != null) {
            for (String preKey : result.dayDataPreJSON.keySet()) { //key 为时分数据
                result.dateimeList.add(DateTime.parse(pyear + pmonth + pday + preKey, dateformat));
            }
        }

        int index = -1;
        long minusSec = 60 * 60 * 24; //时差超过24小时，则认为没有查询到数据
        for (int i = 0; i < result.dateimeList.size(); i++) {
            long subSec = Math.abs((new Duration(result.curDate, result.dateimeList.get(i))).getStandardSeconds());
            if (subSec < minusSec) {
                minusSec = subSec;
                index = i;
            }
        }
        JSONObject resJson = new JSONObject();
        if (index < 0)
            return resJson;

        //找到距离时间最近的一条数据，返回
        DateTime indexDate = result.dateimeList.get(index);
        JSONObject dataJSON;
        if (indexDate.getDayOfMonth() == result.curDate.getDayOfMonth()) {
            dataJSON = result.dayDataCurJSON;
        } else {
            dataJSON = result.dayDataPreJSON;
        }

        JSONObject resData = dataJSON.getJSONObject(StringUtils.leftPad(String.valueOf(indexDate.getHourOfDay()), 2, "0")
                + StringUtils.leftPad(String.valueOf(indexDate.getMinuteOfHour()), 2, "0"));

        resData = ResultDataReprocessor.procDataSurf(resData);

        resJson.put(ResJsonConst.DATA, resData);
        //返回的时间需要转换为北京时!
        resJson.put(ResJsonConst.TIME, indexDate.plusHours(8).toString(dateformat));

        return resJson;
    }

    //按照地面逐三小时要求，对地面时间进行归并
    private DateTime calSurfRealDataTime(DateTime setDate, int interval){
        DateTime useDate = setDate;
        long lastHours = new Duration(useDate, DateTime.now()).getStandardHours();
        if (lastHours < 2){//最近2小时以内的实况按照延迟一个小时来计算
            useDate = useDate.minusHours(1);
        }
        while ((useDate.getHourOfDay() - 8) % interval != 0) {
            useDate = useDate.minusHours(1);
        }
        return useDate;
    }

    @Override
    public JSONObject getSelectedStationListDataSurf(String year, String month, String day, String hour, String minute) {


        //1.对给定的时间进行归并,地面归并到逐3小时
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmm");
//        DateTime useDate = DateTime.parse(year + month + day + hour + minute, dateformat);
//        useDate = useDate.minusHours(1); //实况按照延迟一个小时来计算
//        while ((useDate.getHourOfDay() - 8) % 3 != 0) {
//            useDate = useDate.minusHours(1);
//        }
        DateTime useDate = this.calSurfRealDataTime(DateTime.parse(year + month + day + hour + minute, dateformat), 3);

        long tt = System.currentTimeMillis();
        Map<String, JSONObject> dataList = this.getXmlData(useDate);

//        //拼接站点列表(field列表)
//        StringBuilder filedStrs = new StringBuilder("");
//        JSONArray resDataArray = new JSONArray();
//        String curTime = useDate.minusHours(8).toString("HH") + "00";
//        final int STATIONNUM = stationCodeList.size();
//        long tt = System.currentTimeMillis();
//        for (int i = 0; i < STATIONNUM; i++){
//            int everytimes = 50; //每次取everytimes个field
//            if  ((i + everytimes) >= STATIONNUM){
//                everytimes = stationCodeList.size() - i;
//            }
//            String[] fieldStrAry = new String[everytimes];
//            for (int j=0; j < everytimes; j++){
//                fieldStrAry[j] = stationCodeList.get(i).getStationCode() + "_" + useDate.minusHours(8).toString("yyyyMMdd");
//            }
//
//            List<String> dataStrList = jedisCluster.hmget(DataTypeConst.STATIONDATA_SURF_PREFIX + ":"
//                    + useDate.minusHours(8).toString("yyyyMMdd"), fieldStrAry);
//            if (dataStrList == null || dataStrList.isEmpty())
//                continue;
//            for (int k = 0; k < dataStrList.size(); k++){
//                JSONObject json = JSONObject.parseObject(dataStrList.get(k));
//                if (json == null)
//                    continue;
//                for (String curKey : json.keySet()){ //key 为时分数据
//                    if (curKey == curTime){
//                        json.put("lat", stationCodeList.get(i).getLat());
//                        json.put("lng", stationCodeList.get(i).getLng());
//                        resDataArray.add(json);
//                    }
//                }
//            }
//
////            String field = stationCodeList.get(i).getStationCode() + "_" + useDate.minusHours(8).toString("yyyyMMdd");
////            String dataStr = jedisCluster.hget(DataTypeConst.STATIONDATA_SURF_PREFIX + ":"
////                    + useDate.minusHours(8).toString("yyyyMMdd"), field);
////            if (dataStr == null || dataStr.isEmpty())
////                continue;
////            JSONObject json = JSONObject.parseObject(dataStr);
////            if (json == null)
////                continue;
////            for (String curKey : json.keySet()){ //key 为时分数据
////                if (curKey == curTime){
////                    json.put("lat", stationCodeList.get(i).getLat());
////                    json.put("lng", stationCodeList.get(i).getLng());
////                    resDataArray.add(json);
////                }
////            }
//        }
        if (dataList != null)
            logger.info(dataList.size() + "个气象站地面实况查询耗费时间=" + (System.currentTimeMillis() - tt));

        JSONObject resJson = new JSONObject();
        if (dataList == null || dataList.isEmpty()){
            resJson.put(ResJsonConst.TIME, useDate.toString("yyyyMMddHH") + "00");
            return resJson;
        }

        resJson.put(ResJsonConst.TIME, useDate.toString("yyyyMMddHH") + "00");
        resJson.put(ResJsonConst.DATA, dataList);

        return resJson;
    }

    @Override
    public JSONObject getIsolineData(String year, String month, String day, String hour, String level, String elem, String strArea) {
        DateTime date = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                Integer.parseInt(hour), 0, 0);
        //对给定的时间进行归并,地面归并到逐3小时
        DateTime useDate = this.calSurfRealDataTime(date, 3);
        String scTime =  useDate.toString("yyyyMMddHH") + "00";

        //todo:这里的field中的hour是没有任何意义的
        String field = "0" + useDate.toString("HH") + "_"+ elem + "_" + level + "_" + strArea;
        String key = DataTypeConst.SURFHIGH_REAL_ISOLINE_PREFIX + ":" + useDate.toString("yyyyMMddHH");
        JSONObject isolineJSON = JSONObject.parseObject(jedisCluster.hget(key, field));

        JSONObject resJSON = new JSONObject();
        if (isolineJSON == null){
            resJSON.put(ResJsonConst.TIME, scTime);
            return resJSON;
        }

        Float[] baseInterval = ResultDataReprocessor.getIsolineLabelBaseInterVal(level, elem);
        if (baseInterval != null){
            isolineJSON.put("labelBaseVal", baseInterval[0]);
            isolineJSON.put("labelInterVal", baseInterval[1]);
        }
        resJSON.put(ResJsonConst.DATA, isolineJSON);
        //返回时间需为北京时
        resJSON.put(ResJsonConst.TIME, scTime);
        return resJSON;
    }

    @Override
    public JSONObject getIsosurfaceData(String year, String month, String day, String hour, String level, String elem, String strArea) {
        DateTime date = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                Integer.parseInt(hour), 0, 0);

        int interval = 3;
        if (elem.toUpperCase().equals("RN24") || elem.toUpperCase().equals("RN12")
            || elem.toUpperCase().equals("RAIN24") || elem.toUpperCase().equals("RAIN12"))
            interval = 12;

        if (elem.toUpperCase().equals("RN06") || elem.toUpperCase().equals("RAIN06"))
            interval = 6;
        //对给定的时间进行归并,地面归并到逐3小时(24H降水归并到08,20）
        DateTime useDate = this.calSurfRealDataTime(date, interval);
        String scTime =  useDate.toString("yyyyMMddHH") + "00";

        String field = "0" +  useDate.toString("HH") + "_"+ elem + "_" + level + "_" + strArea;
        JSONObject isolineJSON = JSONObject.parseObject(jedisCluster.hget(DataTypeConst.SURFHIGH_REAL_ISOSURFACE_PREFIX + ":" + useDate.toString("yyyyMMddHH"), field));

        JSONObject resJSON = new JSONObject();
        if (isolineJSON == null){
            resJSON.put(ResJsonConst.TIME, scTime);
            return resJSON;
        }

        Float[] baseInterval = ResultDataReprocessor.getIsolineLabelBaseInterVal(level, elem);
        if (baseInterval != null){
            isolineJSON.put("labelBaseVal", baseInterval[0]);
            isolineJSON.put("labelInterVal", baseInterval[1]);
        }
        resJSON.put(ResJsonConst.DATA, isolineJSON);
        //返回时间需为北京时
        resJSON.put(ResJsonConst.TIME, scTime);
        return resJSON;
    }


    private static Map<String, JSONObject> columnsIndexMap;
    /**
     * 加载地面实况数据集数据
     *
     * @param
     */
    private Map<String, JSONObject> getXmlData(DateTime date) {


        Map<String, JSONObject> dataMap = new HashMap();
        String year = date.toString("yyyy");
        String hour = date.toString("HH");

        String filePath = DM_DATA_FILE_PATH + year + File.separator + DM_DATA_FILE_PREFIX + "_"
                + date.minusHours(8).toString("yyyyMMddHH");//-8转北京时
        File file = new File(filePath + "00.mds");
        if (!file.exists()) {
            try {
                ZipFileUtil.unZip(new File(filePath + "00.zip"));
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("解压实况数据时出错：" + e.getMessage());
                return null;
            }
        }
        if (!file.exists()) {
            logger.error("读取实况数据时找不到指定的文件：" + file.getPath());
            return null;
        }
        XmlUtil xmlUtil = new XmlUtil(file);
        Element el = xmlUtil.getSignNode("//mds/DATA");
        //组织数据文件配置信息
        if (columnsIndexMap == null) {
            JSONObject columnsJson = JSON.parseObject(DataColumnUtil.getColumns(DM_COLUMNS_JSON));
            columnsIndexMap = new HashMap<>();
            JSONObject fieldJson = columnsJson.getJSONObject("field");
            JSONObject dataJson = columnsJson.getJSONObject("data");
            columnsIndexMap.put("field", DataColumnUtil.getFieldColInfo(xmlUtil, fieldJson));
            columnsIndexMap.put("data", DataColumnUtil.getDataColInfo(xmlUtil, dataJson));
        }

        String valueStr = xmlUtil.getNodeValue(el);
        if (valueStr != null && !valueStr.trim().equals("")) {
            String[] dataArray = valueStr.split("\n");
            for (int k = 0; k < dataArray.length; k++) {
                if (dataArray[k] != null && !dataArray[k].trim().equals("")) {
                    StationSurfData tmpData = new StationSurfData(date.toString("yyyyMMdd"));
                    String[] value = dataArray[k].split(",", -1);
                    tmpData.put(hour, value, columnsIndexMap);
//                    StationSurfData surfData = dataMap.get(tmpData.getStation() + "_" + tmpData.getDateStr());
//                    surfData = surfData == null ? tmpData : surfData;
//                    surfData.put(hour, value, columnsIndexMap);
                    StationInfoSurfBean info = stationInfoSurfDao.getSelectedStationInfoFromCode(tmpData.getStation());
                    if (info == null)
                        continue;
                    dataMap.put(info.getStationCode() + "_" + info.getLng() + "_" + info.getLat(), tmpData.getJsonData());
                }
            }
        }
        return dataMap;
    }

    @Override
    protected void checkDaoConfig() throws IllegalArgumentException {
        Assert.notNull(this.sqlSession, "Property 'sqlSessionFactory' or 'sqlSessionTemplate' are required");
    }
}
