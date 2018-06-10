package net.xinhong.meteoserve.service.dao.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ElemCityFC;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.common.ResultDataReprocessor;
import net.xinhong.meteoserve.service.dao.StationDataCityFCDao;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisCluster;

import java.util.List;

/**
 * Description: <br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 <a href=mailto:liusofttech@sina.com>刘晓昌</a>
 * @version 创建时间：2016/3/11.
 */
@Repository
public class StationDataCityFCDaoImpl implements StationDataCityFCDao {

    @Autowired
    private JedisCluster jedisCluster;

    @Override
    public JSONObject getStationCityFC(String stationCode, String year, String month, String day, String hour, String minute, List<ElemCityFC> elems) {
        JSONObject resJson = new JSONObject();
        DateTime date = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                Integer.parseInt(hour), Integer.parseInt(minute), 0);
        DateTime predate = date.minusHours(12);

        //这里将给定的时间转换为世界时
        DateTime dateUTC = date.minusHours(8);
        DateTime predateUTC = predate.minusHours(8);
        year = StringUtils.leftPad(String.valueOf(dateUTC.getYear()), 4, "0");
        month = StringUtils.leftPad(String.valueOf(dateUTC.getMonthOfYear()), 2, "0");
        day = StringUtils.leftPad(String.valueOf(dateUTC.getDayOfMonth()), 2, "0");
        hour = StringUtils.leftPad(String.valueOf(dateUTC.getHourOfDay()), 2, "0");

        String field = stationCode + "_" + year + month + day  + hour;

        //风向中文为转换而得,这里查询时需排除(如果没有WD,因计算需要,需先添加),查询后再转换
        boolean isNeedWDF = false;
        if (elems.contains(ElemCityFC.WDF)){
            elems.remove(ElemCityFC.WDF);
            isNeedWDF = true;
            if (!elems.contains(ElemCityFC.WD))
                elems.add(ElemCityFC.WD);
            elems.add(ElemCityFC.WDF);
        }
        ElemCityFC elemDay = null;
        ElemCityFC elem = elems.get(0);
        if (elem != null && elem.isDay())
            elemDay = elem;

        String useYY = year;
        String useMM = month;
        String useDD = day;
        String useHH = hour;
        if (elem != null && !elem.isDay()) //日要素全部存放在一起，field中无要素标识
            field += "_" + elem;
        String strData = jedisCluster.hget(DataTypeConst.STATIONDATA_CITYFC_PREFIX + ":" + year + month + day, field);


        JSONObject dataJSON = JSONObject.parseObject(strData);

        //返回的时间需要北京时
        String time = date.toString("yyyyMMddHH") +"0000";

        //当前时次没有查询到预报,向前一个时次查询
        if (dataJSON == null || dataJSON.isEmpty()){
            String preyear = StringUtils.leftPad(String.valueOf(predateUTC.getYear()), 4, "0");
            String premonth = StringUtils.leftPad(String.valueOf(predateUTC.getMonthOfYear()), 2, "0");
            String preday = StringUtils.leftPad(String.valueOf(predateUTC.getDayOfMonth()), 2, "0");
            String prehour = StringUtils.leftPad(String.valueOf(predateUTC.getHourOfDay()), 2, "0");

            field = stationCode + "_" + preyear + premonth + preday  + prehour;

            if (elem != null && !elem.isDay()){//日要素全部存放在一起，field中无要素标识
                field += "_" + elem;

            }

            strData = jedisCluster.hget(DataTypeConst.STATIONDATA_CITYFC_PREFIX + ":" + preyear + premonth + preday, field);

            dataJSON = JSONObject.parseObject(strData);
            time = predate.toString("yyyyMMddHH") +"0000";
            useYY = preyear;
            useMM = premonth;
            useDD = preday;
            useHH = prehour;
        }

        JSONObject resDataJSON = new JSONObject();


        if (dataJSON != null
                && !dataJSON.isEmpty()
                && elems.size() > 1){ //多个要素,如果查询到数据, 继续查询
            resDataJSON.put(elem.getEname(), dataJSON);
            for (int i = 1; i<elems.size(); i++){
                elem = elems.get(i);
                field = stationCode + "_" + useYY + useMM + useDD  + useHH;
                if (elem != null && !elem.isDay()) { //日要素全部存放在一起，field中无要素标识
                    field += "_" + elem;
                }
                if (elem != null && elem.isDay() && elemDay == null)
                    elemDay = elem;
                strData = jedisCluster.hget(DataTypeConst.STATIONDATA_CITYFC_PREFIX + ":" + useYY + useMM + useDD, field);

                JSONObject tmpDataJSON = JSONObject.parseObject(strData);

                resDataJSON.put(elem.getEname(), tmpDataJSON);
            }
        } else {
            resDataJSON = dataJSON;
        }
        if (resDataJSON != null && elems != null && elems.size() > 0 && isNeedWDF
                && elems.contains(ElemCityFC.WD)){ //增加风向中文描述
            JSONObject wdJSON = resDataJSON.getJSONObject(ElemCityFC.WD.getEname());
            if (wdJSON != null){
                JSONObject tmpwddJSON = ResultDataReprocessor.procCityFCWD(wdJSON);
                resDataJSON.put(ElemCityFC.WDF.getEname(), tmpwddJSON);
            }
        }
        //todo:转为风速等级后,App端需要适配,暂时不做处理!
//        if (resDataJSON != null && elems != null && elems.size() > 0
//                && (elems.contains(ElemCityFC.WS))){ //WS风速转换为蒲福氏等级
//
//            if (elems.size() == 1){
//                resDataJSON = ResultDataReprocessor.procCityFCWS(resDataJSON);
//            }
//            else{
//                JSONObject wsJSON = resDataJSON.getJSONObject(ElemCityFC.WS.getEname());
//                if (wsJSON != null)
//                    resDataJSON.put(ElemCityFC.WS.getEname(), ResultDataReprocessor.procCityFCWS(wsJSON));
//            }
//        }
//        if (resDataJSON != null && elems != null && elems.size() > 0
//                && elemDay != null){ //WS12风速转换为蒲福氏等级
//            if (elems.size() == 1){
//                resDataJSON = ResultDataReprocessor.procCityFCWS12(resDataJSON);
//            } else {
//                JSONObject wsJSON = resDataJSON.getJSONObject(elemDay.getEname());
//                if (wsJSON != null)
//                    resDataJSON.put(elemDay.getEname(), ResultDataReprocessor.procCityFCWS12(wsJSON));
//            }
//        }

        resJson.put(ResJsonConst.TIME, time);
        resJson.put(ResJsonConst.DATA, resDataJSON);

        return resJson;
    }

    /**
     * Created by wingsby on 2017/12/28.
     */
    public static class HYCOMDaoImpl {
    }
}
