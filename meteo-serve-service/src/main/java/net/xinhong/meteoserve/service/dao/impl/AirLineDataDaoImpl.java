package net.xinhong.meteoserve.service.dao.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.dao.AirLineDataDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisCluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by xiaoyu on 16/4/19.
 */
@Repository
public class AirLineDataDaoImpl  implements AirLineDataDao{
    @Autowired
    private JedisCluster jedisCluster;

    @Override
    public JSONObject getFlightFromNumber(String flightNumber, String year, String month, String day) {
        JSONObject resJson = new JSONObject();
        if (year.length() != 4)
            year = StringUtils.leftPad(year, 4, "0");
        if (month.length() != 2)
            month = StringUtils.leftPad(month, 2, "0");
        if (day.length() != 2)
            day = StringUtils.leftPad(day, 2, "0");

        String field = year + "-" + month + "-" + day + "_" + flightNumber;
        String strData = jedisCluster.hget(DataTypeConst.FLIGHT_INFO, field);
        String time = year + month + day;
        resJson.put(ResJsonConst.TIME, time);
        resJson.put(ResJsonConst.DATA, JSONObject.parse(strData));
        return resJson;
    }

    @Override
    public JSONObject getFlightFromDeptArr(String deptAptCode, String arrAptCode, String year, String month, String day) {
        JSONObject resJson = new JSONObject();
        if (year.length() != 4)
            year = StringUtils.leftPad(year, 4, "0");
        if (month.length() != 2)
            month = StringUtils.leftPad(month, 2, "0");
        if (day.length() != 2)
            day = StringUtils.leftPad(day, 2, "0");

        String field = year + "-" + month + "-" + day + "_" + deptAptCode + "_" + arrAptCode;
        Set<String> strData = jedisCluster.smembers(field);
        String time = year + month + day;
        resJson.put(ResJsonConst.TIME, time);
        resJson.put(ResJsonConst.DATA, strData);
        return resJson;
    }

    @Override
    public JSONObject getAirLineNameListFromDeptArr(String deptAptCode, String arrAptCode) {
        JSONObject resJson = new JSONObject();

        String field = deptAptCode + arrAptCode;
        Set<String>  strData = jedisCluster.smembers(field);
        resJson.put(ResJsonConst.DATA, strData);
        return resJson;
    }

    @Override
    public JSONObject getAirLineFromName(String airLineName) {
        JSONObject resJson = new JSONObject();
        String field = airLineName;
        String strData = jedisCluster.hget(DataTypeConst.AIR_LINES, field);
        JSONArray jsonArray = JSONArray.parseArray(strData);
        resJson.put(ResJsonConst.DATA, jsonArray);
        return resJson;
    }
}
