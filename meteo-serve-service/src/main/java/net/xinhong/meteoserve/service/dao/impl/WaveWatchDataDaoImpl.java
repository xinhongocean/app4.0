package net.xinhong.meteoserve.service.dao.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xinhong.mids3d.datareader.util.DataLevel;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ElemGFS;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.common.Const;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.common.ResultDataReprocessor;
import net.xinhong.meteoserve.service.dao.WaveWatchDataDao;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;

/**
 * Created by wingsby on 2017/12/28.
 */
@Repository
public class WaveWatchDataDaoImpl implements WaveWatchDataDao {
    @Resource
    private JedisCluster jedisCluster;

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
        String field =String.format("%6.1f",Float.valueOf(strLat)).trim() + "_" + String.format("%6.1f",Float.valueOf(strLng)).trim();
        String key = DataTypeConst.WAVEWATCH3_POINT_PREFIX + ":" + date.toString("yyyyMMdd") +"_"+ VTI;
        String strData = null;
        try {
            strData = jedisCluster.hget(key, field);
        }catch (Exception e){
            e.printStackTrace();
        }
        boolean islast = false; //是否向前推一个起报时间
        int looptime=0;
        while (strData == null || strData.isEmpty()) { //如果当前起报时间没有找到,则向前一个起报时间查找
            int lastVTIsub =24;
            date = date.minusHours(lastVTIsub);
            VTI = StringUtils.leftPad(Integer.parseInt(VTI) + lastVTIsub + "", 3, "0");
            try {
                strData = jedisCluster.hget(DataTypeConst.WAVEWATCH3_POINT_PREFIX + ":" + date.toString("yyyyMMdd")
                        + "_" + VTI, field);
            }catch (Exception e){
                e.printStackTrace();
            }
            looptime++;
           if(looptime>2)break;
        }
        date = date.plusHours(8); //返回时间转换为北京时
        scTime = date.toString("yyyy") + date.toString("MM") + date.toString("dd")
                + date.toString("HH") + "00_" + StringUtils.leftPad(VTI, 3, "0");
        islast = true;
        JSONObject dataJSON = null;
        String[] eles=new String[]{"WS","WD","SWH","SH","WP","D","SP","SD","WH"};
        if (strData != null){
            dataJSON=new JSONObject();
            strData=strData.replace("[","");
            strData=strData.replace("]","");
            String[] strs=strData.split(",");
            for(int i=0;i<strs.length;i++){
                dataJSON.put(eles[i],strs[i]) ;
            }
        }
        JSONObject resJSON=new JSONObject();
        resJSON.put(ResJsonConst.DATA,dataJSON);
        resJSON.put(ResJsonConst.TIME,date.toString("yyyyMMddHHmm")+"_"+VTI);
        return resJSON;
    }



    @Override
    public JSONObject getImg(String year, String month, String day, String hour, String VTI, String eles) {
        DateTime date = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                Integer.parseInt(hour), 0, 0);
        String scTime = StringUtils.leftPad(year, 4, "0") + StringUtils.leftPad(month, 2, "0") + StringUtils.leftPad(day, 2, "0")
                + StringUtils.leftPad(hour, 2, "0") + "00_" + StringUtils.leftPad(VTI, 3, "0");
        //这里将给定的时间转换为世界时
        date = date.minusHours(8);
        VTI = StringUtils.leftPad(VTI, 3, "0");
//        String field =String.format("%6.2f",Float.valueOf(strLat)).trim() + "_" + String.format("%6.2f",Float.valueOf(strLng)).trim();
        String field=eles+"_EN";
        String key = "wavewatch3:image:"+date.toString("yyyyMMdd")+"_"+VTI;
        String strData =null;

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
            try {
                strData = jedisCluster.hget("wavewatch3:image:"+date.toString("yyyyMMdd")+"_"+VTI, field);
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
        if (strData != null)
            dataJSON = JSONObject.parseObject(strData);
        JSONObject resJSON=new JSONObject();
        resJSON.put(ResJsonConst.DATA,dataJSON);
        resJSON.put(ResJsonConst.TIME,date.toString("yyyyMMddHHmm")+"_"+VTI);
        return resJSON;
    }

    @Override
    public JSONObject getIsolineData(String year, String month, String day, String hour, String VTI,  String elem, String strArea) {
        DateTime date = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                Integer.parseInt(hour), 0, 0);
        String scTime =  date.toString("yyyyMMddHH") + "00_" + StringUtils.leftPad(VTI, 3, "0");
        //这里将给定的时间转换为世界时
        date = date.minusHours(8);
        VTI = StringUtils.leftPad(VTI, 3, "0");
        String key= Const.WAVEWATCH3_ISOLINE_PREFIX;
        String field = date.toString("yyyyMMdd")+"_"+VTI + "_"+ elem ;
        JSONObject isolineJSON = JSONObject.parseObject(jedisCluster.hget(key, field));
        if (isolineJSON == null || isolineJSON.isEmpty()){ //如果当前起报时间没有找到,则向前一个起报时间查找
            int lastVTIsub = 24;
            date = date.minusHours(lastVTIsub);
            VTI = StringUtils.leftPad(Integer.parseInt(VTI)+lastVTIsub+"", 3, "0");
            field = date.toString("yyyyMMdd")+"_"+VTI + "_"+ elem;
            isolineJSON = JSONObject.parseObject(jedisCluster.hget(key, field));
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


    @Override
    public JSONObject getAreaData(String ryear, String rmonth, String rday, String rhour, String VTI,String elem) {
        DateTime date = new DateTime(Integer.parseInt(ryear), Integer.parseInt(rmonth), Integer.parseInt(rday),
                Integer.parseInt(rhour), 0, 0);
        String scTime =  date.toString("yyyyMMddHH") + "00_" + StringUtils.leftPad(VTI, 3, "0");
        //这里将给定的时间转换为世界时
        date = date.minusHours(8);
        VTI = StringUtils.leftPad(VTI, 3, "0");
        String key = Const.WAVEWATCH3_AREA_PREFIX+":"+date.toString("yyyyMMdd")+"_"+VTI;
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
            key = Const.WAVEWATCH3_AREA_PREFIX+":"+date.toString("yyyyMMdd")+"_"+VTI;
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



}
