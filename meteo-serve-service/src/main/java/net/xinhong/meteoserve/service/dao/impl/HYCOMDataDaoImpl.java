package net.xinhong.meteoserve.service.dao.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ElemGFS;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.common.Const;
import net.xinhong.meteoserve.service.dao.HYCOMDataDao;
import org.apache.commons.lang.ObjectUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;

/**
 * Created by wingsby on 2017/12/28.
 */
@Repository
public class HYCOMDataDaoImpl implements HYCOMDataDao {
    @Resource
    private JedisCluster jedisCluster;

    private String getVTI(String VTI) {
        int[] constVTI = new int[]{27, 30, 33, 36, 39, 42, 45, 48, 54, 60, 66, 72, 78, 84, 90, 96, 120, 144, 168, 192, 216, 240};
        for (int i = 0; i < constVTI.length - 1; i++) {
            if (constVTI[i] == Integer.valueOf(VTI)) {
                return VTI;
            } else {
                if (constVTI[i] <= Integer.valueOf(VTI) &&
                        Integer.valueOf(VTI) <= constVTI[i + 1]) {
                    VTI = StringUtils.leftPad(String.valueOf(constVTI[i + 1]), 3, "0");
                    return VTI;
                }
            }
        }
        return null;
    }

    @Override
    public JSONObject getPointData(String strLat, String strLng, String year, String month,
                                   String day, String hour, String VTI) {

        DateTime date = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                Integer.parseInt(hour), 0, 0);
        String scTime = StringUtils.leftPad(year, 4, "0") + StringUtils.leftPad(month, 2, "0") + StringUtils.leftPad(day, 2, "0")
                + StringUtils.leftPad(hour, 2, "0") + "00_" + StringUtils.leftPad(VTI, 3, "0");
        //这里将给定的时间转换为世界时
        date = date.minusHours(8);
        VTI = StringUtils.leftPad(VTI, 3, "0");
        VTI = getVTI(VTI);
        if (VTI == null) return null;
        String field = String.format("%6.2f", Float.valueOf(strLat)).trim() + "_" + String.format("%6.2f", Float.valueOf(strLng)).trim();
        String key = DataTypeConst.HYCOM_POINT_PREFIX + ":" + date.toString("yyyyMMdd") + "_" + VTI;
        String strData = null;
        try {
            strData = jedisCluster.hget(key, field);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean islast = false; //是否向前推一个起报时间
        int looptime = 0;
        while (strData == null || strData.isEmpty()) { //如果当前起报时间没有找到,则向前一个起报时间查找
            int lastVTIsub = 24;
            date = date.minusHours(lastVTIsub);
            VTI = StringUtils.leftPad(Integer.parseInt(VTI) + lastVTIsub + "", 3, "0");
            VTI = getVTI(VTI);
            if (VTI == null) return null;
            try {
                strData = jedisCluster.hget(DataTypeConst.HYCOM_POINT_PREFIX + ":" +
                        date.toString("yyyyMMdd") + "_" + VTI, field);
            } catch (Exception e) {
                e.printStackTrace();
            }
            looptime++;
            if (looptime > 1) break;
        }
        looptime = 0;
        if (strData != null) {
            date = date.plusHours(8); //返回时间转换为北京时
            scTime = date.toString("yyyy") + date.toString("MM") + date.toString("dd")
                    + date.toString("HH") + "00_" + StringUtils.leftPad(VTI, 3, "0");
            islast = true;
        }

        JSONObject dataJSON = new JSONObject();
        if (strData != null) {
            dataJSON.put(ResJsonConst.DATA, JSONObject.parseObject(strData));
        }
        dataJSON.put(ResJsonConst.TIME, date.toString("yyyyMMddHHmm") + "_" + VTI);
        return dataJSON;
    }

    @Override
    public JSONObject getAreaData(String ryear, String rmonth, String rday, String rhour,
                                  String VTI, String level, ElemGFS elemGFS, String strArea) {
        return null;
    }

    @Override
    public JSONObject getImg(String year, String month, String day, String hour, String VTI, String depth, String eles) {
        DateTime date = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                Integer.parseInt(hour), 0, 0);
        String scTime = StringUtils.leftPad(year, 4, "0") + StringUtils.leftPad(month, 2, "0") + StringUtils.leftPad(day, 2, "0")
                + StringUtils.leftPad(hour, 2, "0") + "00_" + StringUtils.leftPad(VTI, 3, "0");
        //这里将给定的时间转换为世界时
        date = date.minusHours(8);
        VTI = StringUtils.leftPad(VTI, 3, "0");
        VTI = getVTI(VTI);
        if (VTI == null) return null;
//        String field =String.format("%6.2f",Float.valueOf(strLat)).trim() + "_" + String.format("%6.2f",Float.valueOf(strLng)).trim();
        String field = depth + "_" + eles + "_EN";
        String key = Const.HYCOME_IMAGE_PREFIX + ":" + date.toString("yyyyMMdd") + "_" + VTI;
        String strData = null;
        try {
            strData = jedisCluster.hget(key, field);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean islast = false; //是否向前推一个起报时间
        int looptime = 0;
        while (strData == null || strData.isEmpty()) { //如果当前起报时间没有找到,则向前一个起报时间查找
            int lastVTIsub = 24;
            date = date.minusHours(lastVTIsub);
            VTI = StringUtils.leftPad(Integer.parseInt(VTI) + lastVTIsub + "", 3, "0");
            VTI = getVTI(VTI);
            if (VTI == null) return null;
            try {
                strData = jedisCluster.hget(Const.HYCOME_IMAGE_PREFIX + ":" + date.toString("yyyyMMdd") + "_" + VTI, field);
            } catch (Exception e) {
                e.printStackTrace();
            }
            looptime++;
            if (looptime > 1) break;
        }
        if (strData != null) {
            date = date.plusHours(8); //返回时间转换为北京时
            scTime = date.toString("yyyy") + date.toString("MM") + date.toString("dd")
                    + date.toString("HH") + "00_" + StringUtils.leftPad(VTI, 3, "0");
            islast = true;
        }
        JSONObject dataJSON = new JSONObject();
        if (strData != null) {
            dataJSON.put(ResJsonConst.DATA, JSONObject.parseObject(strData));
        }
        dataJSON.put(ResJsonConst.TIME, date.toString("yyyyMMddHHmm") + "_" + VTI);
        return dataJSON;
    }


}
