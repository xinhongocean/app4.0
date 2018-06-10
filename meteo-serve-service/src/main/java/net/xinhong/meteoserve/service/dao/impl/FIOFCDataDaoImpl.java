package net.xinhong.meteoserve.service.dao.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ElemGFS;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.dao.FIOFCDataDao;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;

/**
 * Created by liuso on 2017/11/4.
 */
@Repository
public class FIOFCDataDaoImpl implements FIOFCDataDao {
    @Resource
    private JedisCluster jedisCluster;

    @Override
    public JSONObject getPointData(String strLat, String strLng, String year, String month, String day, String hour, String VTI) {
        DateTime date = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                Integer.parseInt(hour), 0, 0);
        String scTime = StringUtils.leftPad(year, 4, "0") + StringUtils.leftPad(month, 2, "0") + StringUtils.leftPad(day, 2, "0")
                + StringUtils.leftPad(hour, 2, "0") + "00_" + StringUtils.leftPad(VTI, 3, "0");
        //这里将给定的时间转换为世界时
        date = date.minusHours(8);
        VTI = StringUtils.leftPad(VTI, 3, "0");
        String field = strLat + "_" + strLng;

        String key = DataTypeConst.FIO_FC_POINT_PREFIX + ":" +date.toString("yyyyMMddHH")+VTI;

        String strData = jedisCluster.hget(key, field);
        if (strData == null || strData.isEmpty()){ //如果当前起报时间没有找到,则向前一个起报时间查找
            int lastVTIsub = 24;
            date = date.minusHours(lastVTIsub);
            VTI = StringUtils.leftPad(Integer.parseInt(VTI)+lastVTIsub+"", 3, "0");
            strData   = jedisCluster.hget(DataTypeConst.FIO_FC_POINT_PREFIX + ":" +date.toString("yyyyMMddHH")+VTI, field);
            date = date.plusHours(8); //返回时间转换为北京时
            scTime = date.toString("yyyy") + date.toString("MM") +  date.toString("dd")
                    +  date.toString("HH") + "00_" + StringUtils.leftPad(VTI, 3, "0");
        }

        JSONObject dataJSON = null;
        if (strData != null)
            dataJSON = JSONObject.parseObject(strData);

        JSONObject resJson = new JSONObject();
        resJson.put(ResJsonConst.TIME, scTime);
        resJson.put(ResJsonConst.DATA, dataJSON);
        return resJson;
    }

    @Override
    public JSONObject getIsolineData(String year, String month, String day, String hour, String VTI, String level, String elem, String strArea) {
        return null;
    }

    @Override
    public JSONObject getAreaData(String ryear, String rmonth, String rday, String rhour, String VTI, String level, ElemGFS elemGFS, String strArea) {
        return null;
    }

    @Override
    public JSONObject getIsosurfaceData(String ryear, String rmonth, String rday, String rhour, String VTI, String level, String elem, String strArea) {
        DateTime date = new DateTime(Integer.parseInt(ryear), Integer.parseInt(rmonth), Integer.parseInt(rday),
                Integer.parseInt(rhour), 0, 0);
        String scTime =  date.toString("yyyyMMddHH") + "00_" + StringUtils.leftPad(VTI, 3, "0");
        //这里将给定的时间转换为世界时
        date = date.minusHours(8);
        VTI = StringUtils.leftPad(VTI, 3, "0");

        String field = level + "_"+ elem + "_" + strArea;
        String key = DataTypeConst.FIO_FC_ISOSURFACE_PREFIX + ":" + date.toString("yyyyMMddHH") + VTI;
        JSONObject isolineJSON = JSONObject.parseObject(jedisCluster.hget(DataTypeConst.FIO_FC_ISOSURFACE_PREFIX + ":" + date.toString("yyyyMMddHH") + VTI, field));

        if (isolineJSON == null || isolineJSON.isEmpty()){ //如果当前起报时间没有找到,则向前一个起报时间查找
            int lastVTIsub = 24;
            date = date.minusHours(lastVTIsub);
            VTI = StringUtils.leftPad(Integer.parseInt(VTI)+lastVTIsub+"", 3, "0");
            field = VTI + "_"+ elem + "_" + level + "_" + strArea;
            isolineJSON = JSONObject.parseObject(jedisCluster.hget(DataTypeConst.FIO_FC_ISOSURFACE_PREFIX + ":" + date.toString("yyyyMMddHH") + VTI, field));

            date = date.plusHours(8); //返回时间转换为北京时
            scTime =  date.toString("yyyyMMddHH") + "00_" + StringUtils.leftPad(VTI, 3, "0");
        }
        if (isolineJSON == null)
            return null;

        JSONObject resJSON = new JSONObject();
        resJSON.put(ResJsonConst.DATA, isolineJSON);
        //返回时间需为北京时
        resJSON.put(ResJsonConst.TIME, scTime);
        return resJSON;
    }
}
