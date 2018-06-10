package net.xinhong.meteoserve.service.dao.impl;


import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.grib.GribDataReader;
import net.xinhong.meteoserve.common.tool.DateUtil;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.dao.WNIFCDataDao;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;


/**
 * Created by xiaoyu on 16/4/19.
 */
@Repository
public class WNIFCDataDaoImpl implements WNIFCDataDao {
    @Resource
    private JedisCluster jedisCluster;

    private static final Log logger = LogFactory.getLog(WNIFCDataDaoImpl.class);

    @Override
    public JSONObject getPointDangerData(String strLat, String strLng, String year, String month, String day, String hour, String VTI) {
        DateTime date = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                Integer.parseInt(hour), 0, 0);
        String scTime = StringUtils.leftPad(year, 4, "0") + StringUtils.leftPad(month, 2, "0") + StringUtils.leftPad(day, 2, "0")
                + StringUtils.leftPad(hour, 2, "0") + "00_" + StringUtils.leftPad(VTI, 3, "0");
        //这里将给定的时间转换为世界时
        date = date.minusHours(8);
        year = String.valueOf(date.getYear());
        month = String.valueOf(date.getMonthOfYear());
        day = String.valueOf(date.getDayOfMonth());
        hour = String.valueOf(date.getHourOfDay());

        year = StringUtils.leftPad(year, 4, "0");
        month = StringUtils.leftPad(month, 2, "0");
        day = StringUtils.leftPad(day, 2, "0");
        hour = StringUtils.leftPad(hour, 2, "0");
        VTI = StringUtils.leftPad(VTI, 3, "0");

        String field = strLat + "_" + strLng;

        String strData = jedisCluster.hget(DataTypeConst.AIR_WNI_FC_POINT_PREFIX + ":" +date.toString("yyyyMMdd")+hour+VTI, field);
        System.out.println(DataTypeConst.AIR_WNI_FC_POINT_PREFIX + ":" +date.toString("yyyyMMdd")+hour+VTI);
        JSONObject dataJSON = JSONObject.parseObject(strData);
        String time = year + month + day + hour + "_" + VTI;
        JSONObject resJson = new JSONObject();

        //返回时间转换为北京时
        resJson.put(ResJsonConst.TIME, scTime);
        resJson.put(ResJsonConst.DATA, dataJSON);
        return resJson;
    }

    public JSONObject getAreaData(String path, String el) {
        GribDataReader reader = new GribDataReader();
        JSONObject jsonData = reader.getSmallWniData(path, el);
        return jsonData;
    }

    public JSONObject getAreaData(String dateStr, String vti, String height, String elem) {
        DateTime dateTime = DateUtil.format(dateStr, "yyyyMMddHH");
        String year = String.valueOf(dateTime.getYear());
        String month = StringUtils.leftPad(String.valueOf(dateTime.getMonthOfYear()), 2, "0");
        String day = StringUtils.leftPad(String.valueOf(dateTime.getDayOfMonth()), 2, "0");
        String hour = StringUtils.leftPad(String.valueOf(dateTime.getHourOfDay()), 2, "0");
        String key = DataTypeConst.AIR_WNI_FC_WINDGRID_PREFIX + ":" + year + month + day;
        String time = year + month + day + hour + "_" + StringUtils.leftPad(vti, 3, "0");
        String field = time + "_" + elem + "_" + height;
        String dataStr = jedisCluster.hget(key, field);
        if (dataStr == null)
            return null;

        JSONObject resJSON = JSONObject.parseObject(dataStr);
        if (resJSON == null)
            return null;

        //返回时间转换为北京时
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmm");
        DateTime resTime = dateTime.plusHours(8);
        resJSON.put(ResJsonConst.TIME, resTime.toString(dateformat) + "_" + StringUtils.leftPad(vti, 3, "0"));
        return resJSON;
    }

    /**
     * 获取等值线数据
     *
     * @param freeArea EN东北半球
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param VTI
     * @param level
     * @return
     */
    public JSONObject getIsoLineData(String freeArea, String year, String month,
                                     String day, String hour, String VTI, String level, String elem) {
        StringBuilder field = new StringBuilder();
        String dateStr = DateUtil.dateToUTC(year, month, day, hour);
        String dateKey = year + month + day;
        field.append(dateStr).append("_").append(VTI).append("_").append(elem).append("_").append(level).append("_").append(freeArea);
       // System.out.println(DataTypeConst.AIR_WNI_FC_ISOLINE_PREFIX + ":" + dateKey);
        String time = year + month + day + "_" + StringUtils.leftPad(VTI, 3, "0");
        JSONObject resJSON = JSONObject.parseObject(jedisCluster.hget(DataTypeConst.AIR_WNI_FC_ISOLINE_PREFIX + ":" + dateKey, field.toString()));
        if (resJSON == null)
            return null;

        //返回时间需为北京时
        resJSON.put(ResJsonConst.TIME,  year + month + day + hour + "00_" + StringUtils.leftPad(VTI, 3, "0"));
        return resJSON;
    }
}
