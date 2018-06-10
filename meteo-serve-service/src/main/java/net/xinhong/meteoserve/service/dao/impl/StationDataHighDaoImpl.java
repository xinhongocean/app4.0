package net.xinhong.meteoserve.service.dao.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.common.tool.XmlUtil;
import net.xinhong.meteoserve.common.tool.ZipFileUtil;
import net.xinhong.meteoserve.service.common.DataColumnUtil;
import net.xinhong.meteoserve.service.common.WeatherElemCalTool;
import net.xinhong.meteoserve.service.dao.StationDataHighDao;
import net.xinhong.meteoserve.service.dao.StationInfoSurfDao;
import net.xinhong.meteoserve.service.domain.StationHighData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Description: <br>
 *
 * @author 作者 <a href=ds.lht@163.com>stone</a>
 * @version 创建时间：2016/5/17.
 */
@Repository
public class StationDataHighDaoImpl implements StationDataHighDao {
    protected final Log logger = LogFactory.getLog(this.getClass());
    @Resource
    private JedisCluster jedisCluster;

    @Autowired
    private StationInfoSurfDao stationInfoSurfDao;

    //高空实况数据文件前缀及路径Key
    public static final String GK_DATA_FILE_PREFIX = "GK";
    public static final String GK_DATA_FILE_PATH = "/xinhong/meteo_data/duts_data/dutsdata/GK/";
  //  public static final String GK_DATA_FILE_PATH = "/Users/xiaoyu/GK/";
    private static final String GK_COLUMNS_JSON = "gk_columns.json";

    public JSONObject getStationDataHigh(String stationCode, String year, String month, String day, String hour) {
        year = StringUtils.leftPad(year, 4, "0");
        month = StringUtils.leftPad(month, 2, "0");
        day = StringUtils.leftPad(day, 2, "0");
        hour = StringUtils.leftPad(hour, 2, "0");

        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        DateTime date = DateTime.parse(year + month + day + hour + "0000", dateformat);
        //这里将给定的时间转换为世界时,并获取指定日期及前一日期的年月日
        DateTime curDate = date.minusHours(8);
        DateTime preDate = curDate.minusDays(1);
        String cyear = String.valueOf(curDate.getYear());
        String cmonth = String.valueOf(curDate.getMonthOfYear());
        String cday = String.valueOf(curDate.getDayOfMonth());
        cyear = StringUtils.leftPad(cyear, 4, "0");
        cmonth = StringUtils.leftPad(cmonth, 2, "0");
        cday = StringUtils.leftPad(cday, 2, "0");

        String pyear = String.valueOf(preDate.getYear());
        String pmonth = String.valueOf(preDate.getMonthOfYear());
        String pday = String.valueOf(preDate.getDayOfMonth());
        pyear = StringUtils.leftPad(pyear, 4, "0");
        pmonth = StringUtils.leftPad(pmonth, 2, "0");
        pday = StringUtils.leftPad(pday, 2, "0");

        String fieldCur = stationCode + "_" + cyear + cmonth + cday;
        String fieldPre = stationCode + "_" + pyear + pmonth + pday;

        //然后取出当前日及前一天中所有的数据
        String curDayDataStr = jedisCluster.hget(DataTypeConst.STATIONDATA_HIGH_PREFIX + ":" + cyear + cmonth + cday, fieldCur);
        String preDayDataStr = jedisCluster.hget(DataTypeConst.STATIONDATA_HIGH_PREFIX + ":" + pyear + pmonth + pday, fieldPre);

        JSONObject dayDataPreJSON = null;
//      if (preDayDataStr != null && !preDayDataStr.isEmpty())
            dayDataPreJSON = JSONObject.parseObject(preDayDataStr);
        JSONObject dayDataCurJSON = null;
        if (curDayDataStr != null && !curDayDataStr.isEmpty())
            dayDataCurJSON = JSONObject.parseObject(curDayDataStr);
        if((dayDataCurJSON == null || dayDataCurJSON.isEmpty())
                && (dayDataPreJSON == null || dayDataPreJSON.isEmpty()))
            return null;

        //将查询的结果时间取出，查询出相距最近时次的数据
        List<DateTime> dateimeList = new ArrayList<>(8);
        if (dayDataCurJSON != null){
            for (String curKey : dayDataCurJSON.keySet()){ //key 为时分数据
                dateimeList.add(DateTime.parse(cyear + cmonth + cday + curKey + "00", dateformat));
            }
        }
        if (dayDataPreJSON != null){
            for (String preKey : dayDataPreJSON.keySet()){ //key 为时分数据
                dateimeList.add(DateTime.parse(pyear + pmonth + pday + preKey + "00", dateformat));
            }
        }

        int index = -1;
        long minusSec = 60*60*24; //时差超过24小时，则认为没有查询到数据
        for (int i = 0; i<dateimeList.size(); i++){
            long subSec = Math.abs((new Duration(curDate, dateimeList.get(i))).getStandardSeconds());
            if (subSec < minusSec){
                minusSec = subSec;
                index = i;
            }
        }
        JSONObject resJson = new JSONObject();
        if (index < 0)
            return resJson;

        //找到距离时间最近的一条数据，返回
        DateTime indexDate = dateimeList.get(index);
        JSONObject dataJSON ;
        if (indexDate.getDayOfMonth() == curDate.getDayOfMonth()){
            dataJSON = dayDataCurJSON;
        } else {
            dataJSON = dayDataPreJSON;
        }

        resJson.put(ResJsonConst.DATA, dataJSON.get(StringUtils.leftPad(String.valueOf(indexDate.getHourOfDay()), 2, "0")
                + StringUtils.leftPad(String.valueOf(indexDate.getMinuteOfHour()), 2, "0")));
        //返回的时间需要转换为北京时!
        resJson.put(ResJsonConst.TIME, indexDate.plusHours(8).toString(dateformat));

        return resJson;
    }


    public JSONObject getStationDataHighTimeProfile(String stationCode, String year, String month, String day, String hour){
        year = StringUtils.leftPad(year, 4, "0");
        month = StringUtils.leftPad(month, 2, "0");
        day = StringUtils.leftPad(day, 2, "0");
        hour = StringUtils.leftPad(hour, 2, "0");

        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        DateTime date = DateTime.parse(year + month + day + hour + "0000", dateformat);
        //这里将给定的时间转换为世界时,并获取指定日期及之前八个主要时次数据,用于绘制高空剖面图
        //todo:这里未来无高空数据情况下,可以用GFS数据代替!
        DateTime curDate = date.minusHours(8);
        JSONObject resDataJSON = new JSONObject();
        JSONObject resJson = new JSONObject();
        List<String> dateimeList = new ArrayList<>(12);
        dateformat = DateTimeFormat.forPattern("yyyyMMddHHmm");
        for (int i = 0; i < 5; i++){
            String cyear = String.valueOf(curDate.getYear());
            String cmonth = String.valueOf(curDate.getMonthOfYear());
            String cday = String.valueOf(curDate.getDayOfMonth());
            cyear = StringUtils.leftPad(cyear, 4, "0");
            cmonth = StringUtils.leftPad(cmonth, 2, "0");
            cday = StringUtils.leftPad(cday, 2, "0");
            String fieldCur = stationCode + "_" + cyear + cmonth + cday;
            String dayDataStr = jedisCluster.hget(DataTypeConst.STATIONDATA_HIGH_PREFIX + ":" + cyear + cmonth + cday, fieldCur);
            JSONObject dayDataCurJSON = null;
            if (dayDataStr != null && !dayDataStr.isEmpty()){
                dayDataCurJSON = JSONObject.parseObject(dayDataStr);
                JSONObject hourDataJSON = null;
                for (String curKey : dayDataCurJSON.keySet()){ //key 为时分数据
                    hourDataJSON = dayDataCurJSON.getJSONObject(curKey);
                    if (hourDataJSON != null){
                        if (curKey.equals("0000") || curKey.equals("1200")
                                || curKey.equals("0800") || curKey.equals("2000")){
                            DateTime tmpTime = DateTime.parse(cyear + cmonth + cday + curKey, dateformat).plusHours(8);
                            resDataJSON.put(tmpTime.toString(dateformat), hourDataJSON);
                            dateimeList.add(tmpTime.toString(dateformat));
                        }
                    }
                }
            }
            curDate = curDate.minusDays(1);
        }
        if (resDataJSON.isEmpty())
            return resJson;
        //显示的时间转换为北京时
        Collections.sort(dateimeList);

        resJson.put(ResJsonConst.DATA, resDataJSON);
        //返回的时间需要转换为北京时!
        resJson.put(ResJsonConst.TIME, dateimeList.get(0) + "_" + dateimeList.get(dateimeList.size()-1));
        return resJson;

    }



    public JSONObject getStationDataHighIndex(String stationCode, String year, String month, String day, String hour){

        //1.首先获取Redis中是否有缓存数据,如果有直接返回
        //......


        //2.没有则进行计算,并拼接为JSONObject
        JSONObject resJson = new JSONObject();
        JSONObject highJSON = this.getStationDataHigh(stationCode, year, month, day, hour);
        if (highJSON == null || highJSON.isEmpty())
            return null;
        JSONObject dataJSON = highJSON.getJSONObject(ResJsonConst.DATA);
        if (dataJSON == null || dataJSON.isEmpty())
            return null;

        //整理各个计算所需要的数组
        int length = dataJSON.keySet().size();
        int[] tmpPressDatas = new int[length];
        double[] ttDatas = new double[length];
        double[] hhDatas = new double[length];
        double[] tdDatas = new double[length];
        double[] wsDatas = new double[length];
        double[] wdDatas = new double[length];

        Set<String> keySet = dataJSON.keySet();
        int count = 0;
        for (String strPress : keySet){
            tmpPressDatas[count++] = Integer.parseInt(strPress);
        }
        Arrays.sort(tmpPressDatas);
        int[] pressDatas = new int[length];
        count = 0;
        for (int i = length-1; i>=0; i--){
            pressDatas[count++] = tmpPressDatas[i];
        }


        for (int i = 0; i < length; i++){
            ttDatas[i] = 9999.0;
            hhDatas[i] = 9999.0;
            tdDatas[i] = 9999.0;
            wsDatas[i] = 9999.0;
            wdDatas[i] = 9999.0;
        }

        for (int i = 0; i < length; i++){
            String strPress = String.valueOf(pressDatas[i]);
            if (strPress.length() == 3) strPress = "0" + strPress;
            Double tt = dataJSON.getJSONObject(strPress).getDouble("TT");
            if (tt != null)
                ttDatas[i] = tt;
            Double hh = dataJSON.getJSONObject(strPress).getDouble("HH");
            if (hh != null)
                hhDatas[i] = hh;
            Double td = dataJSON.getJSONObject(strPress).getDouble("TD");
            if (td != null)
                tdDatas[i] = td;
            Double ws = dataJSON.getJSONObject(strPress).getDouble("WS");
            if (ws != null)
                wsDatas[i] = ws;
            Double wd = dataJSON.getJSONObject(strPress).getDouble("WD");
            if (wd != null)
                wdDatas[i] = wd;
        }
        // 确定凝结高度点的位置：TL、PL
        double pl = 0.0;
        double tl = 0.0;
        if (tdDatas[0] >= ttDatas[0])
        {
            pl = pressDatas[0];
            tl = ttDatas[0];
        }
        else
        {
            double[] res = WeatherElemCalTool.tpl(pressDatas[0], ttDatas[0], tdDatas[0]);
            tl = res[0];
            pl = res[1];
        }

        JSONObject resDataJSON = new JSONObject();
        //沙氏指数
        double SSI = WeatherElemCalTool.CalculateSSI(pressDatas, ttDatas, pl, tl);
        if (SSI != -9999.0){
            resDataJSON.put("SSI", SSI);
        }
        //气团指数
        double K = WeatherElemCalTool.CalculateK(pressDatas, ttDatas, tdDatas);
        if (K != -9999.0)
        {
            resDataJSON.put("K", K);
        }
        //强天气威胁指数
        double SW = WeatherElemCalTool.CalculateSW(pressDatas, ttDatas, tdDatas, wsDatas, wdDatas);
        if (SW != -9999.0)
        {
            resDataJSON.put("SW", SW);
        }
        //RI,理查逊数500~850hPa
        double RI = WeatherElemCalTool.CalculateRI(pressDatas, ttDatas, wsDatas, wdDatas);
        if (RI != -9999.0)
        {
            resDataJSON.put("RI8550", RI);
        }

        //CI 位势稳定度指数  500~850hPa
        double CI = WeatherElemCalTool.CalculateWindShearCI(850, 500, pressDatas, ttDatas, tdDatas);
        if (CI != -9999.0)
        {
            resDataJSON.put("CI", CI);
        }

        //强垂直风切变500~300hPa
        double WindShearWS = WeatherElemCalTool.CalculateWindShearWS(500, 300, pressDatas, hhDatas, wsDatas, wdDatas);
        if (WindShearWS != -9999.0)
        {
            resDataJSON.put("WindShearWS5030", WindShearWS);
        }
        double WindShearWD = WeatherElemCalTool.CalculateWindShearWD(500, 300, pressDatas, wsDatas, wdDatas);
        if (WindShearWD != -9999.0)
        {
            resDataJSON.put("WindShearWD5030", WindShearWD);
        }
        //强垂直风切变700~300hPa
        double WindShearWS2 = WeatherElemCalTool.CalculateWindShearWS(700, 300, pressDatas, hhDatas, wsDatas, wdDatas);
        if (WindShearWS2 != -9999.0)
        {
            resDataJSON.put("WindShearWS7030", WindShearWS2);
        }
        double WindShearWD2 = WeatherElemCalTool.CalculateWindShearWD(700, 300, pressDatas, wsDatas, wdDatas);
        if (WindShearWD2 != -9999.0)
        {
            resDataJSON.put("WindShearWD7030", WindShearWD2);
        }
        if  (resDataJSON.isEmpty())
            return null;
        resJson.put(ResJsonConst.DATA, resDataJSON);
        resJson.put(ResJsonConst.TIME, highJSON.get(ResJsonConst.TIME));


        //3.存入Redis,并返回
        //......


        return resJson;
    }


    @Override
    public JSONObject getSelectedStationListDataHigh(String year, String month, String day, String hour, String minute) {

        //1.对给定的时间进行归并,高空归并到逐12小时
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmm");
        DateTime useDate = DateTime.parse(year + month + day + hour + minute, dateformat);
        useDate = useDate.minusHours(1); //实况按照延迟1个小时来计算
        while ((useDate.getHourOfDay() - 8) % 12 != 0) {
            useDate = useDate.minusHours(1);
        }

        long tt = System.currentTimeMillis();
        Map<String, JSONObject> dataList = this.getXmlData(useDate);

        if (dataList != null)
            logger.info(dataList.size() + "个气象站高空实况查询耗费时间=" + (System.currentTimeMillis() - tt));

        JSONObject resJson = new JSONObject();
        if (dataList == null || dataList.isEmpty()){
            return null;
        }
        resJson.put(ResJsonConst.DATA, dataList);
        resJson.put(ResJsonConst.TIME, useDate.toString("yyyyMMddHH") + "00");

        return resJson;
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

        String filePath = GK_DATA_FILE_PATH + year + File.separator + GK_DATA_FILE_PREFIX + "_"
                + date.minusHours(8).toString("yyyyMMddHH");//-8转北京时
        File file = new File(filePath + "00.mds");
        if (!file.exists()) {
            try {
                ZipFileUtil.unZip(new File(filePath + "00.zip"));
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("解压高空实况数据时出错：" + e.getMessage());
                return null;
            }
        }
        if (!file.exists()) {
            logger.error("读取高空实况数据时找不到指定的文件：" + file.getPath());
            return null;
        }
        XmlUtil xmlUtil = new XmlUtil(file);
        Element el = xmlUtil.getSignNode("//mds/DATA");
        //组织数据文件配置信息
        if (columnsIndexMap == null) {
            JSONObject columnsJson = JSON.parseObject(DataColumnUtil.getColumns(GK_COLUMNS_JSON));
            columnsIndexMap = new HashMap<>();
            JSONObject fieldJson = columnsJson.getJSONObject("field");
            JSONObject dataJson = columnsJson.getJSONObject("data");
            columnsIndexMap.put("field", DataColumnUtil.getFieldColInfo(xmlUtil, fieldJson));
            columnsIndexMap.put("data", DataColumnUtil.getDataColInfo(xmlUtil, dataJson));
        }

        String valueStr = xmlUtil.getNodeValue(el);
        if (valueStr != null && !valueStr.trim().equals("")) {
            StationHighData highData = new StationHighData(date.toString("yyyyMMdd"), this.stationInfoSurfDao) ;
            highData.put(hour, valueStr, columnsIndexMap);
            dataMap =  highData.getData();

        }
        return dataMap;
    }


    /**
     *
     * @param dateStr 多个","分隔 Format:yyyyMMdd
     * @return
     * @throws Exception
     */
//    private Map<String, StationHighData> getXmlData(String... dateStr) throws Exception {
//        JSONObject columnsJson = JSON.parseObject(DataColumnUtil.getColumns(GK_COLUMNS_JSON));
//        Map<String, StationHighData> dataMap = new HashMap();
//        Map<String, JSONObject> columnsIndexMap = new HashMap<>();
//
//        for (int l = 0; l < dateStr.length; l++) {
//            String year = Integer.toString(DateUtil.format(dateStr[l],"yyyyMMdd").getYear());
//            for (int i = 0; i < DataTypeConst.GK_HOURS.length; i++) {
//                String filePath = ConfigUtil.getProperty(Constants.GK_DATA_FILE_PATH)
//                        +year+File.separator
//                        + Constants.GK_DATA_FILE_PREFIX + "_" + dateStr[l] + DataTypeConst.GK_HOURS[i];
//                //   String filePath = "";
//                File file = new File(filePath + "00.mds");
//                if (!file.exists()) {
//                    ZipFileUtil.unZip(new File(filePath + "00.zip"));
//                }
//                if (!file.exists()) {
//                    logger.warn("找不到指定的文件：{}", file.getPath());
//                    continue;
//                }
//                XmlUtil xmlUtil = new XmlUtil(file);
//                Element el = xmlUtil.getSignNode("//mds/DATA");
//                if (columnsIndexMap.isEmpty()) {
//                    JSONObject fieldJson = columnsJson.getJSONObject("field");
//                    JSONObject dataJson = columnsJson.getJSONObject("data");
//                    columnsIndexMap.put("field", DataColumnUtil.getFieldColInfo(xmlUtil, fieldJson));
//                    columnsIndexMap.put("data", DataColumnUtil.getDataColInfo(xmlUtil, dataJson));
//                }
//                String valueStr = xmlUtil.getNodeValue(el);
//                if (valueStr != null && !valueStr.trim().equals("")) {
////                        StationHighData highData = new StationHighData(dateStr[l]);
//                    StationHighData highData = dataMap.get(dateStr[l]) ==null?new StationHighData(dateStr[l]):dataMap.get(dateStr[l]) ;
//                    highData.put(DataTypeConst.GK_HOURS[i],valueStr,columnsIndexMap);
//                    dataMap.put(dateStr[l], highData);
//                }
//            }
//        }
//        return dataMap;
//    }
}
