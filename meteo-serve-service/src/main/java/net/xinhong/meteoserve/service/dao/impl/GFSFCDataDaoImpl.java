package net.xinhong.meteoserve.service.dao.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xinhong.mids3d.datareader.util.ElemCode;
import com.xinhong.mids3d.util.IsolinesAttributes;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ElemGFS;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.gfs.GfsJSYBReader;
import net.xinhong.meteoserve.common.tool.GridDataUtil;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.common.ResultDataReprocessor;
import net.xinhong.meteoserve.service.dao.GFSFCDataDao;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;

/**
 * Created by xiaoyu on 16/7/22.
 */
@Repository
public class GFSFCDataDaoImpl implements GFSFCDataDao{
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

        String key = DataTypeConst.GFS_FC_POINT_PREFIX + ":" +date.toString("yyyyMMddHH")+VTI;
        String dangerkey = DataTypeConst.GFS_FC_POINT_SIGMET_PREFIX + ":" +date.toString("yyyyMMddHH")+VTI;

        String strData = jedisCluster.hget(key, field);
        boolean islast = false; //是否向前推一个起报时间
        if (strData == null || strData.isEmpty()){ //如果当前起报时间没有找到,则向前一个起报时间查找
            int lastVTIsub = 12;
            date = date.minusHours(lastVTIsub);
            VTI = StringUtils.leftPad(Integer.parseInt(VTI)+lastVTIsub+"", 3, "0");
            strData   = jedisCluster.hget(DataTypeConst.GFS_FC_POINT_PREFIX + ":" +date.toString("yyyyMMddHH")+VTI, field);
            dangerkey = DataTypeConst.GFS_FC_POINT_SIGMET_PREFIX + ":" +date.toString("yyyyMMddHH")+VTI;
            date = date.plusHours(8); //返回时间转换为北京时
            scTime = date.toString("yyyy") + date.toString("MM") +  date.toString("dd")
                   +  date.toString("HH") + "00_" + StringUtils.leftPad(VTI, 3, "0");
            islast = true;
        }

        JSONObject dataJSON = null;
        if (strData != null)
            dataJSON = JSONObject.parseObject(strData);
        //查找对应的积冰颠簸等危险天气数据
        String strDangerData = jedisCluster.hget(dangerkey, field);

        if (strData != null && (strDangerData == null || strDangerData.isEmpty())){
            if (!islast){  //如果查询到大气数值产品,但没有危险天气产品,且大气数值预报查询没有向前推起报时间,则危险天气向前推起报时间
                int lastVTIsub = 12;
                date = date.minusHours(lastVTIsub);
                VTI = StringUtils.leftPad(Integer.parseInt(VTI)+lastVTIsub+"", 3, "0");
                dangerkey = DataTypeConst.GFS_FC_POINT_SIGMET_PREFIX + ":" + date.toString("yyyyMMddHH")+VTI;
                strDangerData = jedisCluster.hget(dangerkey, field);
            }
        }
        if (strDangerData != null && !strDangerData.isEmpty()){
            if (dataJSON == null){
                dataJSON = new JSONObject();

            }
            dataJSON.put("DANGER", JSONObject.parseObject(strDangerData));
        }

        JSONObject resJson = new JSONObject();

        resJson.put(ResJsonConst.TIME, scTime);
        resJson.put(ResJsonConst.DATA, dataJSON);
        return resJson;
    }

    @Override
    public JSONObject getIsolineData(String year, String month, String day, String hour, String VTI,
                                     String level, String elem, String strArea) {
        DateTime date = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                Integer.parseInt(hour), 0, 0);
        String scTime =  date.toString("yyyyMMddHH") + "00_" + StringUtils.leftPad(VTI, 3, "0");
        //这里将给定的时间转换为世界时
        date = date.minusHours(8);
        VTI = StringUtils.leftPad(VTI, 3, "0");
        String field = VTI + "_"+ elem + "_" + level + "_" + strArea;
        JSONObject isolineJSON = JSONObject.parseObject(jedisCluster.hget(DataTypeConst.GFS_FC_ISOLINE_PREFIX + ":" + date.toString("yyyyMMddHH"), field));
        if (isolineJSON == null || isolineJSON.isEmpty()){ //如果当前起报时间没有找到,则向前一个起报时间查找
            int lastVTIsub = 12;
            date = date.minusHours(lastVTIsub);
            VTI = StringUtils.leftPad(Integer.parseInt(VTI)+lastVTIsub+"", 3, "0");
            field = VTI + "_"+ elem + "_" + level + "_" + strArea;
            isolineJSON = JSONObject.parseObject(jedisCluster.hget(DataTypeConst.GFS_FC_ISOLINE_PREFIX + ":" + date.toString("yyyyMMddHH"), field));
            date = date.plusHours(8); //返回时间转换为北京时
            scTime =  date.toString("yyyyMMddHH") + "00_" + StringUtils.leftPad(VTI, 3, "0");
        }
        if (isolineJSON == null)
            return null;
        JSONObject resJSON = new JSONObject();
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
    public JSONObject getAreaData(String ryear, String rmonth, String rday, String rhour, String VTI, String level, ElemGFS elemGFS, String strArea) {
        DateTime date = new DateTime(Integer.parseInt(ryear), Integer.parseInt(rmonth), Integer.parseInt(rday),
                Integer.parseInt(rhour), 0, 0);
        String scTime =  date.toString("yyyyMMddHH") + "00_" + StringUtils.leftPad(VTI, 3, "0");
        //这里将给定的时间转换为世界时
        date = date.minusHours(8);
        VTI = StringUtils.leftPad(VTI, 3, "0");

        String ymd = date.toString("yyyyMMdd");
        String hour = date.toString("HH");

        GfsJSYBReader.GFSData data = GfsJSYBReader.readAreaData(ymd, hour, VTI, level, elemGFS);
        if  (data == null || data.getData() == null)
            return null;
        float[][] valarray = data.getData();
        JSONArray jsonDataArray = new JSONArray();
        for (int i = 0; i < valarray.length; i++){
            JSONArray rowArray = new JSONArray();
            for (int j = 0; j < valarray[0].length; j++){
                rowArray.add(valarray[i][j]);
            }
            jsonDataArray.add(rowArray);
        }

        JSONObject resJSON = new JSONObject();
        JSONObject dataJSON = new JSONObject();
        JSONOperateTool.putJSONFreeArea(dataJSON, data.getSlat(), data.getElat(),
                data.getSlng(), data.getElng(), valarray.length, valarray[0].length, data.getScale());
        dataJSON.put("vals", jsonDataArray);
        resJSON.put(ResJsonConst.DATA, dataJSON);
        //返回时间需为北京时
        resJSON.put(ResJsonConst.TIME, scTime);
        return resJSON;
    }

    @Override
    public JSONObject getIsosurfaceData(String ryear, String rmonth, String rday, String rhour, String VTI, String level, String elem, String strArea) {
        DateTime date = new DateTime(Integer.parseInt(ryear), Integer.parseInt(rmonth), Integer.parseInt(rday),
                Integer.parseInt(rhour), 0, 0);
        String scTime =  date.toString("yyyyMMddHH") + "00_" + StringUtils.leftPad(VTI, 3, "0");
        //这里将给定的时间转换为世界时
        date = date.minusHours(8);
        VTI = StringUtils.leftPad(VTI, 3, "0");
        String field = VTI + "_"+ elem + "_" + level + "_" + strArea;
        JSONObject isolineJSON = JSONObject.parseObject(jedisCluster.hget(DataTypeConst.GFS_FC_ISOSURFACE_PREFIX + ":" + date.toString("yyyyMMddHH"), field));
        if (isolineJSON == null || isolineJSON.isEmpty()){ //如果当前起报时间没有找到,则向前一个起报时间查找
            int lastVTIsub = 12;
            date = date.minusHours(lastVTIsub);
            VTI = StringUtils.leftPad(Integer.parseInt(VTI)+lastVTIsub+"", 3, "0");
            field = VTI + "_"+ elem + "_" + level + "_" + strArea;
            isolineJSON = JSONObject.parseObject(jedisCluster.hget(DataTypeConst.GFS_FC_ISOSURFACE_PREFIX + ":" + date.toString("yyyyMMddHH"), field));
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
