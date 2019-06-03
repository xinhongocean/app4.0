package net.xinhong.meteoserve.service.dao.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ElemGFS;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.common.Const;
import net.xinhong.meteoserve.service.dao.HY1SuoDataDao;
import net.xinhong.meteoserve.service.dao.HYCOMDataDao;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;

/**
 * Created by wingsby on 2017/12/28.
 */
@Repository
public class HY1SuoDataDaoImpl implements HY1SuoDataDao {
    @Resource
    private JedisCluster jedisCluster;

    private String getVTI(String VTI) {
//        int[] constVTI = new int[]{27, 30, 33, 36, 39, 42, 45, 48, 54, 60, 66, 72, 78, 84, 90, 96, 120, 144, 168, 192, 216, 240};
        int[]constVTI=new int[40];
        for(int i=0;i<40;i++){
            constVTI[i]=i*3;
        }
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
        String field = String.format("%6.1f", Float.valueOf(strLat)).trim() + "_" + String.format("%6.1f", Float.valueOf(strLng)).trim();
        String key = DataTypeConst.HY1Suo_POINT_PREFIX + ":" + date.toString("yyyyMMddHH")  + VTI;
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
                strData = jedisCluster.hget(DataTypeConst.HY1Suo_POINT_PREFIX + ":" +
                        date.toString("yyyyMMddHH")  + VTI, field);
            } catch (Exception e) {
                e.printStackTrace();
            }
            looptime++;
            if (looptime > 2) break;
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
    public JSONObject getAreaData(String ryear, String rmonth, String rday, String rhour, String VTI,String elem) {
        DateTime date = new DateTime(Integer.parseInt(ryear), Integer.parseInt(rmonth), Integer.parseInt(rday),
                Integer.parseInt(rhour), 0, 0);
        String scTime =  date.toString("yyyyMMddHH") + "00_" + StringUtils.leftPad(VTI, 3, "0");
        //这里将给定的时间转换为世界时
        date = date.minusHours(8);
        VTI = StringUtils.leftPad(VTI, 3, "0");
        String key = Const.HY1Suo_AREA_PREFIX+":"+date.toString("yyyyMMddHH")+"_"+VTI;
        String field=elem;
        String strData=null;
        try {
            strData=jedisCluster.hget(key, field);
        }catch (Exception e){
            e.printStackTrace();
        }

        boolean islast = false; //是否向前推一个起报时间
        int looptime=0;
        while (strData == null || strData.isEmpty()){ //如果当前起报时间没有找到,则向前一个起报时间查找
            int lastVTIsub = 24;
            date = date.minusHours(lastVTIsub);
            VTI = StringUtils.leftPad(Integer.parseInt(VTI)+lastVTIsub+"", 3, "0");
            key = Const.HY1Suo_AREA_PREFIX+":"+date.toString("yyyyMMddHH")+"_"+VTI;
            try {
                strData = jedisCluster.hget(key, field);
            }catch (Exception e){
                e.printStackTrace();
            }
            looptime++;
            if(looptime>4)break;
        }
        if(strData!=null){
            date = date.plusHours(8); //返回时间转换为北京时
            scTime = date.toString("yyyy") + date.toString("MM") +  date.toString("dd")
                    +  date.toString("HH") + "00_" + StringUtils.leftPad(VTI, 3, "0");
            islast = true;
        }
        JSONObject dataJSON = null;
        if (strData != null) {
            dataJSON = JSONObject.parseObject(strData);
        }
        JSONObject resJSON=new JSONObject();
        resJSON.put(ResJsonConst.DATA,dataJSON);
        resJSON.put(ResJsonConst.TIME,scTime);
        return resJSON;
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
        String key = Const.HY1Suo_IMAGE_PREFIX + ":" + date.toString("yyyyMMddHH")  + VTI;
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
                strData = jedisCluster.hget(Const.HY1Suo_IMAGE_PREFIX + ":" + date.toString("yyyyMMddHH")  + VTI, field);
            } catch (Exception e) {
                e.printStackTrace();
            }
            looptime++;
            if (looptime > 2) break;
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

    @Override
    public JSONObject getIsolineData(String year, String month, String day, String hour, String VTI, String elem, String strArea) {
        DateTime date = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                Integer.parseInt(hour), 0, 0);
        String scTime =  date.toString("yyyyMMddHH") + "00_" + StringUtils.leftPad(VTI, 3, "0");
        //这里将给定的时间转换为世界时
        date = date.minusHours(8);
        VTI = StringUtils.leftPad(VTI, 3, "0");
        String key= Const.HY1Suo_ISOLINE_PREFIX;
        String field = date.toString("yyyyMMddHH")+"_"+VTI + "_"+ elem ;
        JSONObject isolineJSON = JSONObject.parseObject(jedisCluster.hget(key, field));
        int looptime=0;
        while (isolineJSON == null || isolineJSON.isEmpty()){ //如果当前起报时间没有找到,则向前一个起报时间查找
            int lastVTIsub = 24;
            date = date.minusHours(lastVTIsub);
            VTI = StringUtils.leftPad(Integer.parseInt(VTI)+lastVTIsub+"", 3, "0");
            field = date.toString("yyyyMMddHH")+"_"+VTI + "_"+ elem;
            isolineJSON = JSONObject.parseObject(jedisCluster.hget(key, field));
            date = date.plusHours(8); //返回时间转换为北京时
            scTime =  date.toString("yyyyMMddHH") + "00_" + StringUtils.leftPad(VTI, 3, "0");
            looptime++;
            if(looptime>2)break;
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
