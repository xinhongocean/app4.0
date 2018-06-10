package net.xinhong.meteoserve.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.service.common.Const;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.dao.OriginalDataDao;
import net.xinhong.meteoserve.service.domain.origin.GTSPPData;
import net.xinhong.meteoserve.service.domain.origin.ZHS_ICOADS_ELES;
import net.xinhong.meteoserve.service.domain.origin.ZH_TYPH_INT;
import net.xinhong.meteoserve.service.domain.origin.ZH_TYPH_JMA;
import net.xinhong.meteoserve.service.domain.origin.ZH_TYPH_TPC;
import net.xinhong.meteoserve.service.service.OriginalDataService;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by wingsby on 2018/1/3.
 */
@Service
public class OriginalDataServiceImpl implements OriginalDataService {
    @Resource
    private OriginalDataDao dao;

    @Override
    public JSONObject getICOADS(int grid, int year,int pageid) {
        int sline=Const.DEPART_PAGE_LINES*(pageid);
        int eline=Const.DEPART_PAGE_LINES*(pageid+1)-1;
        String sdate=year+"0101";
        String edate=year+"1231";
        List<ZHS_ICOADS_ELES> list = dao.getICOADS(grid, sdate,edate,sline,eline);
        JSONObject resJSON = new JSONObject();
        JSONObject daoJSON = new JSONObject();
        Integer k=0;
        for (ZHS_ICOADS_ELES eles : list) {
            JSONObject obj = eles.toJson();
            daoJSON.put(String.valueOf(k++),obj);
        }
        JSONObject dataJSON=new JSONObject();
        dataJSON.put(ResJsonConst.DATA,daoJSON);
        if (list==null||list.size()==0){
            JSONOperateTool.putJSONNoResult(dataJSON);
            return dataJSON;
        }
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON);
        return resJSON;
    }

    @Override
    public JSONObject getTyphoon(String intnum, String source) {
        //识别不同种类的下载
        List list = null;
        JSONObject res = new JSONObject();
        if (source.equals("JMA")) {
            res=ZH_TYPH_JMA.toJSON(dao.getJMATYPH(intnum));
        } else if (source.equals("INT"))
            res=ZH_TYPH_INT.toJSON(dao.getINTTYPH(intnum));
//            list = dao.getINTTYPH(intnum);
        else if (source.equals("TPC"))
            res=ZH_TYPH_TPC.toJSON(dao.getTPCTYPH(intnum));
//            list = dao.getTPCTYPH(intnum);
//        if (list == null) return null;
//        Map<String, JSONArray> map = new HashMap<>();

//        for (Object obj : list) {
//            String key = null;
//            if (obj instanceof ZH_TYPH_JMA) key = ((ZH_TYPH_JMA) obj).getINTNUM();
//            else if (obj instanceof ZH_TYPH_INT) key = ((ZH_TYPH_INT) obj).getBASIN()
//                    + "_" + ((ZH_TYPH_INT) obj).getLDATE().getYear() + "_" + ((ZH_TYPH_INT) obj).getCY();
//            else if (obj instanceof ZH_TYPH_TPC)
//                key = ((ZH_TYPH_TPC) obj).getTIME().getYear() + "_" + ((ZH_TYPH_TPC) obj).getID();
//            JSONArray tmp = null;
//            if (map.containsKey(key)) {
//                tmp = map.get(key);
//            } else {
//                tmp = new JSONArray();
//            }
//            JSONObject tmpObj = null;
//            if (obj instanceof ZH_TYPH_JMA) tmpObj = toJSONObj(obj, ZH_TYPH_JMA.class);
//            else if (obj instanceof ZH_TYPH_INT) tmpObj = toJSONObj(obj, ZH_TYPH_INT.class);
//            else if (obj instanceof ZH_TYPH_TPC) tmpObj = toJSONObj(obj, ZH_TYPH_TPC.class);
//            if (tmpObj != null) tmp.add(tmpObj);
//            map.put(key, tmp);
//        }
//        res.putAll(map);
        if (res==null||res.isEmpty()){
            JSONOperateTool.putJSONNoResult(res);
            return res;
        }
        JSONObject resJSON = new JSONObject();
        JSONOperateTool.putJSONSuccessful(resJSON, res);
        return resJSON;
    }

    @Override
    public JSONObject getTyphoonIDX(int  year, String source) {
        //识别不同种类的下载
        List list = null;
        if (source.equals("JMA")) {
            list = dao.getJMATYPHIDX(year);
        } else if (source.equals("INT"))
            list = dao.getINTTYPHIDX(year);
        else if (source.equals("TPC"))
            list = dao.getTPCTYPHIDX(year);
        if (list == null) return null;
        Map<String, JSONArray> map = new HashMap<>();
        JSONObject res = new JSONObject();
        res.put(source,list);
        if (res==null||res.isEmpty()){
            JSONOperateTool.putJSONNoResult(res);
            return res;
        }
        JSONObject resJSON = new JSONObject();
        JSONOperateTool.putJSONSuccessful(resJSON, res);
        return resJSON;
    }

    @Override
    public JSONObject getTyphoons(int year, String source) {
        JSONObject json=getTyphoonIDX(year,source);
        JSONObject obj= (JSONObject) json.get(ResJsonConst.DATA);
        List<String> list= (List) obj.get(source);
        JSONArray datajson=new JSONArray();
        for(String str:list){
            JSONObject typhoon=getTyphoon(str,source);
            datajson.add(typhoon.get(ResJsonConst.DATA));
        }
        JSONObject resJSON = new JSONObject();
        JSONOperateTool.putJSONSuccessful(resJSON, datajson);
        return resJSON;
    }


    @Override
    public JSONObject getGtsppBuoy(String id) {
        List<GTSPPData> list = dao.getGtsppBuoy(id);
        JSONObject resJSON = new JSONObject();
        JSONObject daoJSON = new JSONObject();
        Integer k=0;
        GregorianCalendar calendar = new GregorianCalendar(1900, 0, 1);
        long t = calendar.getTime().getTime();
        JSONObject obj =null;
        JSONArray array=null;
        JSONArray timeArray=new JSONArray();
        for (GTSPPData eles : list) {
            long time = Math.round(eles.getTime() * 3600l * 24 * 1000l) + t;
            DateTime dateTime = new DateTime(time);
            String date=dateTime.toString("yyyy-MM-dd HH:mm");
            if(obj==null||(obj!=null&&!obj.get("date").equals(date))) {
                if(obj!=null){
                    obj.put("depthdata",array);
                    timeArray.add(obj);
                }
                obj = new JSONObject();
                obj.put("lng", eles.getLongitude());
                obj.put("lat", eles.getLatitude());
                obj.put("date", date);
                array=new JSONArray();
            }
            // add new depth data
            JSONObject tmp=new JSONObject();
            tmp.put("temp",eles.getTemperature());
            tmp.put("salt",eles.getSalinity());
            tmp.put("depth",eles.getZ());
            if(array!=null)array.add(tmp);
        }
        //循环结束
        if(obj!=null){
            obj.put("depthdata",array);
            timeArray.add(obj);
        }
        JSONObject dataJSON=new JSONObject();
        dataJSON.put(ResJsonConst.DATA,timeArray);
        if (list==null||list.size()==0){
            JSONOperateTool.putJSONNoResult(dataJSON);
            return dataJSON;
        }
        JSONOperateTool.putJSONSuccessful(resJSON, dataJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    @Override
    public JSONObject getICOADSPages(int grid, int year) {
        String sdate=year+"0101";
        String edate=year+"1231";
        int lines = dao.getICOADSPages(grid, sdate,edate);
        JSONObject resJSON = new JSONObject();
        JSONObject daoJSON = new JSONObject();
        daoJSON.put("rows",lines);
        daoJSON.put("pages",lines/Const.DEPART_PAGE_LINES+1);
        JSONObject dataJSON=new JSONObject();
        dataJSON.put(ResJsonConst.DATA,daoJSON);
        if (lines==0){
            JSONOperateTool.putJSONNoResult(dataJSON);
            return dataJSON;
        }
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON);
        return resJSON;
    }

//    private static JSONObject toJSONObj(Object obj, Class cc) {
//        JSONObject json = (JSONObject) JSON.toJSON(obj);
//        JSONObject res = new JSONObject();
//        Field[] fieldss = cc.getDeclaredFields();
//        try {
//            for (Field field : fieldss) {
//                field.setAccessible(true);
//                if (field.get(obj) != null)
//                    res.put(field.getName(), field.get(obj));
//            }
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//        return res;
//    }

}
