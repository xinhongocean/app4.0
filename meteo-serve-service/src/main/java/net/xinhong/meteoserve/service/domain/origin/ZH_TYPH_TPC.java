package net.xinhong.meteoserve.service.domain.origin;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Created by wingsby on 2018/1/4.
 */
public class ZH_TYPH_TPC {
    private String ADV;
    private float LAT;
    private float LON;
    private Date TIME;
    private int WIND;
    private float PR;
    private String STAT;
    private String STORMNAME;
    private long ID;

    //todo
    public static JSONObject toJSON(List<ZH_TYPH_TPC> list) {
        if (list == null || (list != null && list.size() < 1)) return null;
        ZH_TYPH_TPC jma = list.get(0);
        JSONObject json = new JSONObject();
//        json.put("ename", jma.getName() != null ? jma.getName() : jma.getINTNUM());
        json.put("ccc", "日本");
        json.put("isfinished", true);
        //todo 'BABJ'-201701
        int num = Long.valueOf(jma.getID()).intValue();

        if (num > 25) num += 190000;
        else num += 200000;
        String strnum = "JMA" + num;
        json.put("id", strnum);
        JSONArray array = new JSONArray();
        json.put("points", array);
        for (ZH_TYPH_TPC tmp : list) {
            array.add(toJSON(tmp));
        }
        return json;
    }

    //转化为通用台风json格式
    public static JSONObject toJSON(ZH_TYPH_TPC jma) {
        // points
        JSONObject point = new JSONObject();
//        point.put("odate", jma.getADATE());
        JSONObject centerInfo = new JSONObject();
//        if (jma.getMAXWS() > 0)
//            centerInfo.put("cneterWS", jma.getMAXWS());
//        if (jma.getMAXWS() > 900)
//            centerInfo.put("centerSLP", jma.getPRESS());
//        centerInfo.put("lat", jma.getCLAT());
//        centerInfo.put("lng", jma.getCLON());
//        point.put("centerInfo", centerInfo);
//        JSONArray r30 = new JSONArray();
//        JSONObject r1 = new JSONObject();
//        if (jma.getMAXR30() > 0)
//            r1.put("radius", jma.getMAXR30());
//        if (jma.getWINDRD30() != null)
//            r1.put("wd", jma.getWINDRD30());
//        if (!r1.isEmpty())
//            r30.add(r1);
//        if (!r30.isEmpty())
//            point.put("r30", r30);
        return point;
    }


    public String getADV() {
        return ADV;
    }

    public void setADV(String ADV) {
        this.ADV = ADV;
    }

    public float getLAT() {
        return LAT;
    }

    public void setLAT(float LAT) {
        this.LAT = LAT;
    }

    public float getLON() {
        return LON;
    }

    public void setLON(float LON) {
        this.LON = LON;
    }

    public Date getTIME() {
        return TIME;
    }

    public void setTIME(Date TIME) {
        this.TIME = TIME;
    }

    public int getWIND() {
        return WIND;
    }

    public void setWIND(int WIND) {
        this.WIND = WIND;
    }

    public float getPR() {
        return PR;
    }

    public void setPR(float PR) {
        this.PR = PR;
    }

    public String getSTAT() {
        return STAT;
    }

    public void setSTAT(String STAT) {
        this.STAT = STAT;
    }

    public String getSTORMNAME() {
        return STORMNAME;
    }

    public void setSTORMNAME(String STORMNAME) {
        this.STORMNAME = STORMNAME;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }
}
