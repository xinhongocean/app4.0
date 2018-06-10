package net.xinhong.meteoserve.service.domain.origin;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Created by wingsby on 2018/1/4.
 */
public class ZH_TYPH_INT {
    private String BASIN;
    private int CY;
    private Date LDATE;
    private int TECHNUM;
    private String TECH;
    private int TAU;
    private int LATN;
    private int LONE;
    private int VMAX;
    private int MSLP;
    private String TY;
    private int RAD;
    private String WINDCODE;
    private int RAD1;
    private int RAD2;
    private int RAD3;
    private int RAD4;
    private int RADP;
    private int RRP;
    private int MRD;
    private int GUSTS;
    private int EYE;
    private String SUBREGION;
    private int MAXSEAS;
    private String INITIALS;
    private int DIR;
    private int SPEED;
    private String STORMNAME;
    private String DEPTH;
    private int SEAS;
    private String SEASCODE;
    private int SEAS1;
    private int SEAS2;
    private int SEAS3;
    private int SEAS4;
    private String REMARKS;



    public static JSONObject toJSON(List<ZH_TYPH_INT> list) {
        if (list == null || (list != null && list.size() < 1)) return null;
        ZH_TYPH_INT jma = list.get(0);
        JSONObject json = new JSONObject();
//        json.put("ename", jma.getName() != null ? jma.getName() : jma.getINTNUM());
        json.put("ccc", "日本");
        json.put("isfinished", true);
        //todo 'BABJ'-201701
//        int num = Integer.valueOf(jma.getINTNUM());
//        if (num > 25) num += 190000;
//        else num += 200000;
//        String strnum = "JMA" + num;
//        json.put("id", strnum);
        JSONArray array = new JSONArray();
        json.put("points", array);
        for (ZH_TYPH_INT tmp : list) {
            array.add(toJSON(tmp));
        }
        return json;
    }

    //转化为通用台风json格式
    public static JSONObject toJSON(ZH_TYPH_INT jma) {
        // points
        JSONObject point = new JSONObject();
//        point.put("odate", jma.getADATE());
//        JSONObject centerInfo = new JSONObject();
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




    public String getBASIN() {
        return BASIN;
    }

    public void setBASIN(String BASIN) {
        this.BASIN = BASIN;
    }

    public int getCY() {
        return CY;
    }

    public void setCY(int CY) {
        this.CY = CY;
    }

    public Date getLDATE() {
        return LDATE;
    }

    public void setLDATE(Date LDATE) {
        this.LDATE = LDATE;
    }

    public int getTECHNUM() {
        return TECHNUM;
    }

    public void setTECHNUM(int TECHNUM) {
        this.TECHNUM = TECHNUM;
    }

    public String getTECH() {
        return TECH;
    }

    public void setTECH(String TECH) {
        this.TECH = TECH;
    }

    public int getTAU() {
        return TAU;
    }

    public void setTAU(int TAU) {
        this.TAU = TAU;
    }

    public int getLATN() {
        return LATN;
    }

    public void setLATN(int LATN) {
        this.LATN = LATN;
    }

    public int getLONE() {
        return LONE;
    }

    public void setLONE(int LONE) {
        this.LONE = LONE;
    }

    public int getVMAX() {
        return VMAX;
    }

    public void setVMAX(int VMAX) {
        this.VMAX = VMAX;
    }

    public int getMSLP() {
        return MSLP;
    }

    public void setMSLP(int MSLP) {
        this.MSLP = MSLP;
    }

    public String getTY() {
        return TY;
    }

    public void setTY(String TY) {
        this.TY = TY;
    }

    public int getRAD() {
        return RAD;
    }

    public void setRAD(int RAD) {
        this.RAD = RAD;
    }

    public String getWINDCODE() {
        return WINDCODE;
    }

    public void setWINDCODE(String WINDCODE) {
        this.WINDCODE = WINDCODE;
    }

    public int getRAD1() {
        return RAD1;
    }

    public void setRAD1(int RAD1) {
        this.RAD1 = RAD1;
    }

    public int getRAD2() {
        return RAD2;
    }

    public void setRAD2(int RAD2) {
        this.RAD2 = RAD2;
    }

    public int getRAD3() {
        return RAD3;
    }

    public void setRAD3(int RAD3) {
        this.RAD3 = RAD3;
    }

    public int getRAD4() {
        return RAD4;
    }

    public void setRAD4(int RAD4) {
        this.RAD4 = RAD4;
    }

    public int getRADP() {
        return RADP;
    }

    public void setRADP(int RADP) {
        this.RADP = RADP;
    }

    public int getRRP() {
        return RRP;
    }

    public void setRRP(int RRP) {
        this.RRP = RRP;
    }

    public int getMRD() {
        return MRD;
    }

    public void setMRD(int MRD) {
        this.MRD = MRD;
    }

    public int getGUSTS() {
        return GUSTS;
    }

    public void setGUSTS(int GUSTS) {
        this.GUSTS = GUSTS;
    }

    public int getEYE() {
        return EYE;
    }

    public void setEYE(int EYE) {
        this.EYE = EYE;
    }

    public String getSUBREGION() {
        return SUBREGION;
    }

    public void setSUBREGION(String SUBREGION) {
        this.SUBREGION = SUBREGION;
    }

    public int getMAXSEAS() {
        return MAXSEAS;
    }

    public void setMAXSEAS(int MAXSEAS) {
        this.MAXSEAS = MAXSEAS;
    }

    public String getINITIALS() {
        return INITIALS;
    }

    public void setINITIALS(String INITIALS) {
        this.INITIALS = INITIALS;
    }

    public int getDIR() {
        return DIR;
    }

    public void setDIR(int DIR) {
        this.DIR = DIR;
    }

    public int getSPEED() {
        return SPEED;
    }

    public void setSPEED(int SPEED) {
        this.SPEED = SPEED;
    }

    public String getSTORMNAME() {
        return STORMNAME;
    }

    public void setSTORMNAME(String STORMNAME) {
        this.STORMNAME = STORMNAME;
    }

    public String getDEPTH() {
        return DEPTH;
    }

    public void setDEPTH(String DEPTH) {
        this.DEPTH = DEPTH;
    }

    public int getSEAS() {
        return SEAS;
    }

    public void setSEAS(int SEAS) {
        this.SEAS = SEAS;
    }

    public String getSEASCODE() {
        return SEASCODE;
    }

    public void setSEASCODE(String SEASCODE) {
        this.SEASCODE = SEASCODE;
    }

    public int getSEAS1() {
        return SEAS1;
    }

    public void setSEAS1(int SEAS1) {
        this.SEAS1 = SEAS1;
    }

    public int getSEAS2() {
        return SEAS2;
    }

    public void setSEAS2(int SEAS2) {
        this.SEAS2 = SEAS2;
    }

    public int getSEAS3() {
        return SEAS3;
    }

    public void setSEAS3(int SEAS3) {
        this.SEAS3 = SEAS3;
    }

    public int getSEAS4() {
        return SEAS4;
    }

    public void setSEAS4(int SEAS4) {
        this.SEAS4 = SEAS4;
    }

    public String getREMARKS() {
        return REMARKS;
    }

    public void setREMARKS(String REMARKS) {
        this.REMARKS = REMARKS;
    }
}
