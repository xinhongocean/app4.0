package net.xinhong.meteoserve.service.domain;

/**
 * Created by liuso on 2017/7/2.
 */
public class StationRadarInfoBean {

    String cname;

    public String getEname() {
        return ename;
    }

    public void setEname(String ename) {
        this.ename = ename;
    }

    String ename;
    Float clat;
    Float clng;
    Float slat;
    Float slng;
    Float elat;
    Float elng;

    public Float getRadius() {
        return radius;
    }

    public void setRadius(Float radius) {
        this.radius = radius;
    }

    Float radius = 400f;
    String stationID;
    String province;

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public Float getClat() {
        return clat;
    }

    public void setClat(Float clat) {
        this.clat = clat;
    }

    public Float getClng() {
        return clng;
    }

    public void setClng(Float clng) {
        this.clng = clng;
    }

    public Float getSlat() {
        return slat;
    }

    public void setSlat(Float slat) {
        this.slat = slat;
    }

    public Float getSlng() {
        return slng;
    }

    public void setSlng(Float slng) {
        this.slng = slng;
    }

    public Float getElat() {
        return elat;
    }

    public void setElat(Float elat) {
        this.elat = elat;
    }

    public Float getElng() {
        return elng;
    }

    public void setElng(Float elng) {
        this.elng = elng;
    }

    public String getStationID() {
        return stationID;
    }

    public void setStationID(String stationID) {
        this.stationID = stationID;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }
}
