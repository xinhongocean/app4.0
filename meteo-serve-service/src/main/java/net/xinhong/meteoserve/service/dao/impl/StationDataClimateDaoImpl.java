package net.xinhong.meteoserve.service.dao.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ElemCityFC;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.common.ResultDataReprocessor;
import net.xinhong.meteoserve.service.dao.StationDataClimateDao;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisCluster;

import java.util.List;

/**
 * Created by xiaoyu on 16/7/19.
 */
@Repository
public class StationDataClimateDaoImpl implements StationDataClimateDao{
    @Autowired
    private JedisCluster jedisCluster;

    @Override
    public JSONObject getStationForYearsData(String stationCode, String year, String month, String day, String hour, String minute) {
        JSONObject resJson = new JSONObject();

        String field;

        //需要查询未来7天的日统计产品数据
        int dayNums = 7;
        DateTime date = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                Integer.parseInt(hour), Integer.parseInt(minute), 0);

        JSONObject resDayJSON = new JSONObject();
        for (int i = 0; i < dayNums; i++){
            month = StringUtils.leftPad(String.valueOf(date.getMonthOfYear()), 2, "0");
            day = StringUtils.leftPad(String.valueOf(date.getDayOfMonth()), 2, "0");
            field = stationCode + "_" + month + day;
            String strDayData = jedisCluster.hget(DataTypeConst.STATIONDATA_STATDAY, field);
            if (strDayData != null && !strDayData.isEmpty())
                resDayJSON.put(month+day, JSONObject.parseObject(strDayData));
            date = date.plusDays(1);
        }

        //查询月旬数据
        String monthField = stationCode + "_M" + month;
        String period = "1";
        int dayNum = Integer.parseInt(day);
        if (dayNum > 10 && dayNum <= 20)
            period = "2";
        else if (dayNum > 20)
            period = "3";
        String periodField = stationCode + "_M" + month + "P" + period;
        String strMData = jedisCluster.hget(DataTypeConst.STATIONDATA_STATYMP, monthField);
        String strPData = jedisCluster.hget(DataTypeConst.STATIONDATA_STATYMP, periodField);

        JSONObject monthJSON = JSONObject.parseObject(strMData);
        JSONObject periodJSON = JSONObject.parseObject(strPData);

        JSONObject resDataJSON = new JSONObject();if (resDayJSON != null  && !resDayJSON.isEmpty()){
            resDataJSON.put("Day", resDayJSON);
        }
        if (monthJSON != null  && !monthJSON.isEmpty()){
            resDataJSON.put("Month", monthJSON);
        }
        if (periodJSON != null  && !periodJSON.isEmpty()){
            resDataJSON.put("Period", periodJSON);
        }

        if (!resDataJSON.isEmpty())
            resJson.put(ResJsonConst.DATA, resDataJSON);
        return resJson;
    }
}
