package net.xinhong.meteoserve.service.domain;

/**
 * Description: <br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 <a href=mailto:liusofttech@sina.com>刘晓昌</a>
 * @version 创建时间：2016/3/3.
 */
public class StationDataSurfBean {
    private String name;
    private String id;
    private Float lat;
    private Float lng;
    private Float PS;
    private Float TT;
    private Float WS;
    private String WD;
    private Float RAIN24;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Float getLat() {
        return lat;
    }

    public void setLat(Float lat) {
        this.lat = lat;
    }

    public Float getLng() {
        return lng;
    }

    public void setLng(Float lng) {
        this.lng = lng;
    }

    public Float getPS() {
        return PS;
    }

    public void setPS(Float PS) {
        this.PS = PS;
    }

    public Float getTT() {
        return TT;
    }

    public void setTT(Float TT) {
        this.TT = TT;
    }

    public Float getWS() {
        return WS;
    }

    public void setWS(Float WS) {
        this.WS = WS;
    }

    public String getWD() {
        return WD;
    }

    public void setWD(String WD) {
        this.WD = WD;
    }

    public Float getRAIN24() {
        return RAIN24;
    }

    public void setRAIN24(Float RAIN24) {
        this.RAIN24 = RAIN24;
    }

    @Override
    public String toString() {
        return "StationDataSurfBean{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", PS=" + PS +
                ", TT=" + TT +
                ", WS=" + WS +
                ", WD='" + WD + '\'' +
                ", RAIN24=" + RAIN24 +
                '}';
    }
}
