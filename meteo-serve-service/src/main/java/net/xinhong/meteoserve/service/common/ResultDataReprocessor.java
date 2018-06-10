package net.xinhong.meteoserve.service.common;

import com.alibaba.fastjson.JSONObject;
import com.xinhong.mids3d.datareader.util.ElemCode;
import com.xinhong.mids3d.util.IsolinesAttributes;
import net.xinhong.meteoserve.common.constant.ElemCityFC;
import net.xinhong.meteoserve.common.constant.ElemSurf;
import net.xinhong.meteoserve.service.common.WeatherElemCalTool;
import org.apache.commons.lang.ObjectUtils;

/**
 * Created by xiaoyu on 16/6/23.
 * 对DAO获取的Data内容进行后处理,计算或转换其中的某些自动
 */
public final class ResultDataReprocessor {

    /**
     * 城镇预报后处理(风)
     * @param dataJSON
     * @return
     */
    public static JSONObject procCityFCWD(JSONObject dataJSON){
        if (dataJSON == null)
            return  null;
        //增加风向中文描述
        JSONObject wddJSON = new JSONObject();
        for (String vtiKey : dataJSON.keySet()){
            Float wd = dataJSON.getFloat(vtiKey);
            if (wd == null){
                continue;
            }
            wddJSON.put(vtiKey, WeatherElemCalTool.getWDDescFromWD(wd));
        }

        return  wddJSON;
    }

    public static JSONObject procCityFCWS(JSONObject dataJSON){
        if (dataJSON == null)
            return  null;
        //风速转换为蒲福氏级
        for (String vtiKey : dataJSON.keySet()){
            Float ws = dataJSON.getFloat(vtiKey);
            if (ws == null)
                continue;
            dataJSON.put(vtiKey, WeatherElemCalTool.getWSPuFuFromMS(ws));
        }
        return  dataJSON;
    }

    public static JSONObject procCityFCWS12(JSONObject dataJSON){
        if (dataJSON == null)
            return  null;
        //风速转换为蒲福氏级
        for (String vtiKey : dataJSON.keySet()){
            JSONObject obj = dataJSON.getJSONObject(vtiKey);
            if (obj == null)
                continue;
            Float ws12 = obj.getFloatValue(ElemCityFC.WS12.getEname());
            if (ws12 != null){
                obj.put(ElemCityFC.WS12.getEname(),
                        WeatherElemCalTool.getWSPuFuFromMS(ws12));
            }
            dataJSON.put(vtiKey, obj);
        }
        return  dataJSON;
    }

    /**
     * 地面实况后处理
     * @param dataJSON
     * @return
     */
    public static JSONObject procDataSurf(JSONObject dataJSON) {
        if (dataJSON == null)
            return  null;
        //增加高中低云状描述
        Integer code = dataJSON.getInteger(ElemSurf.CFH.getEname());
        if (code != null ){
            dataJSON.put(ElemSurf.CFHD.getEname(), WeatherElemCalTool.getCFHDescFromCode(code));
        } else {
            dataJSON.put(ElemSurf.CFH.getEname(), null);
        }
        code = dataJSON.getInteger(ElemSurf.CFM.getEname());
        if (code != null ){
            dataJSON.put(ElemSurf.CFMD.getEname(), WeatherElemCalTool.getCFMDescFromCode(code));
        } else {
            dataJSON.put(ElemSurf.CFM.getEname(), null);
        }
        code = dataJSON.getInteger(ElemSurf.CFL.getEname());
        if (code != null ){
            dataJSON.put(ElemSurf.CFLD.getEname(), WeatherElemCalTool.getCFLDescFromCode(code));
        } else {
            dataJSON.put(ElemSurf.CFL.getEname(), null);
        }

        //增加现在天气及过去天气中文描述
        code = dataJSON.getInteger(ElemSurf.WTH.getEname());
        String CN = dataJSON.getString(ElemSurf.CN.getEname());
        if (code != null ){
            dataJSON.put(ElemSurf.WTHC.getEname(), WeatherElemCalTool.getCHNFromCurWWCode(code, CN));
        } else {
            dataJSON.put(ElemSurf.WTHC.getEname(), null);
        }
        code = dataJSON.getInteger(ElemSurf.WTHP1.getEname());
        if (code != null ){
            dataJSON.put(ElemSurf.WTHP1C.getEname(), WeatherElemCalTool.getCHNFromLastWWCode(code));
        } else {
            dataJSON.put(ElemSurf.WTHP1C.getEname(), null);
        }
        code = dataJSON.getInteger(ElemSurf.WTHP2.getEname());
        if (code != null ){
            dataJSON.put(ElemSurf.WTHP2C.getEname(), WeatherElemCalTool.getCHNFromLastWWCode(code));
        } else {
            dataJSON.put(ElemSurf.WTHP2C.getEname(), null);
        }

        //todo:云量为9时,为天空不明,云量为null,改为10,表示缺测!
        //0-0成 1-1成 2-3成 3-4成 4-5成 5-6成 6-8成 7-9成 8-10成 9-不明 10-缺测
        code = dataJSON.getInteger(ElemSurf.CN.getEname());
        if (code == null ){
            dataJSON.put(ElemSurf.CN.getEname(), 10);
        }

        code = dataJSON.getInteger(ElemSurf.CNML.getEname());
        if (code == null){
            dataJSON.put(ElemSurf.CNML.getEname(), 10);
        }

        return  dataJSON;
    }

    /**
     * 计算APP端等值线绘制需标注的线值及间隔
     * @param level
     * @param elem
     * @return
     */
    public static Float[] getIsolineLabelBaseInterVal(String level, String elem){
        ElemCode elemcode = ElemCode.fromValue(elem);
        if (elemcode == null)
            return null;
        IsolinesAttributes attr = IsolinesAttributes.CreateDefaultInstance(elemcode, level);
        if (attr == null)
            return null;

        Float baseVal = attr.getValBaseVal();
        Float interval = attr.getValInterval();
        if (attr.isHasSetLevels()){
            float[] levels = attr.getLevels();
            if (levels == null || levels.length == 0)
                return null;
            interval = Math.abs(levels[0] - levels[1]);
        }

        return new Float[]{baseVal, interval*2};
    }



}
