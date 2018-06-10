package net.xinhong.meteoserve.service.domain.origin;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by wingsby on 2017/10/26.
 */
public class ZH_TYPH_JMA {
    private String INTNUM;
    private Date ADATE;
    private String IND;
    private int GRADE;
    private float CLAT;
    private float CLON;
    private float PRESS = -9999f;  //气压
    private float MAXWS = -9999f;  //风速
    private String WINDRD50;
    private float MAXR50 = -9999f;
    private float MINR50 = -9999f;
    private String WINDRD30;
    private float MAXR30 = -9999f;
    private float MINR30 = -9999f;
    private String PROVINCE;

    //转json用
    private String name;


    public static JSONObject toJSON(List<ZH_TYPH_JMA> list) {
        if (list == null || (list != null && list.size() < 1)) return null;
        ZH_TYPH_JMA jma = list.get(0);
        JSONObject json = new JSONObject();
        json.put("ename", jma.getName() != null ? jma.getName() : jma.getINTNUM());
        json.put("ccc", "日本");
        json.put("isfinished", true);
        //todo 'BABJ'-201701
        int num = Integer.valueOf(jma.getINTNUM()) / 100;
        int month = Integer.valueOf(jma.getINTNUM()) % 100;
        if (num > 25) num += 1900;
        else num += 2000;
        String strnum = "'JMA'-" + (num * 100 + month);
        json.put("id", strnum);
        JSONArray array = new JSONArray();
        json.put("points", array);
        for (ZH_TYPH_JMA tmp : list) {
            array.add(toJSON(tmp));
        }
        return json;
    }

    //转化为通用台风json格式
    public static JSONObject toJSON(ZH_TYPH_JMA jma) {
        // points
        JSONObject point = new JSONObject();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        point.put("odate", sdf.format(jma.getADATE()));
        JSONObject centerInfo = new JSONObject();
        if (jma.getMAXWS() > 0)
            centerInfo.put("cneterWS", jma.getMAXWS() * 0.5144444);
        if (jma.getPRESS() > 900)
            centerInfo.put("centerSLP", jma.getPRESS());
        centerInfo.put("lat", jma.getCLAT());
        centerInfo.put("lng", jma.getCLON());
        point.put("centerInfo", centerInfo);
        JSONArray r30 = new JSONArray();
        JSONObject r1 = new JSONObject();
        if (jma.getMAXR30() > 0)
            r1.put("radius", jma.getMAXR30());
        if (jma.getWINDRD30() != null)
            r1.put("wd", getAngle(jma.getWINDRD30()));
        if (!r1.isEmpty())
            r30.add(r1);
        if (!r30.isEmpty())
            point.put("r30", r30);
        return point;
    }

    private static float getAngle(String windrd30) {
        switch (windrd30) {
            case "N":
                return 360;
            case "E":
                return 90;
            case "S":
                return 180;
            case "W":
                return 270;
            case "NE":
                return 45;
            case "SE":
                return 135;
            case "WS":
                return 225;
            case "NW":
                return 315;
            case "CC":
                return 361;
            default:
                return 361;
        }
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPROVINCE() {
        return PROVINCE;
    }

    public void setPROVINCE(String PROVINCE) {
        this.PROVINCE = PROVINCE;
    }

    public String getINTNUM() {
        return INTNUM;
    }

    public void setINTNUM(String INTNUM) {
        this.INTNUM = INTNUM;
    }

    public Date getADATE() {
        return ADATE;
    }

    public void setADATE(Date ADATE) {
        this.ADATE = ADATE;
    }

    public String getIND() {
        return IND;
    }

    public void setIND(String IND) {
        this.IND = IND;
    }

    public int getGRADE() {
        return GRADE;
    }

    public void setGRADE(int GRADE) {
        this.GRADE = GRADE;
    }

    public float getCLAT() {
        return CLAT;
    }

    public void setCLAT(float CLAT) {
        this.CLAT = CLAT;
    }

    public float getCLON() {
        return CLON;
    }

    public void setCLON(float CLON) {
        this.CLON = CLON;
    }

    public float getPRESS() {
        return PRESS;
    }

    public void setPRESS(float PRESS) {
        this.PRESS = PRESS;
    }

    public float getMAXWS() {
        return MAXWS;
    }

    public void setMAXWS(float MAXWS) {
        this.MAXWS = MAXWS;
    }


    public float getMAXR50() {
        return MAXR50;
    }

    public void setMAXR50(float MAXR50) {
        this.MAXR50 = MAXR50;
    }

    public float getMINR50() {
        return MINR50;
    }

    public void setMINR50(float MINR50) {
        this.MINR50 = MINR50;
    }


    public float getMAXR30() {
        return MAXR30;
    }

    public void setMAXR30(float MAXR30) {
        this.MAXR30 = MAXR30;
    }

    public float getMINR30() {
        return MINR30;
    }

    public void setMINR30(float MINR30) {
        this.MINR30 = MINR30;
    }

    public String getWINDRD50() {
        return WINDRD50;
    }

    public void setWINDRD50(String WINDRD50) {
        this.WINDRD50 = WINDRD50;
    }

    public String getWINDRD30() {
        return WINDRD30;
    }

    public void setWINDRD30(String WINDRD30) {
        this.WINDRD30 = WINDRD30;
    }
}
