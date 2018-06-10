package net.xinhong.meteoserve.service.domain;

/**
 * Created by xiaoyu on 16/6/12.
 */
public class AirPortInfoBean {

    private Integer ID;
    private Float lat;
    private Float lng;
    private String cname;
    private String ccname;
    private String ccode;
    private String scene;
    private String icao3;
    private String icao4;


    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
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

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getCcname() {
        return ccname;
    }

    public void setCcname(String ccname) {
        this.ccname = ccname;
    }

    public String getCcode() {
        return ccode;
    }

    public void setCcode(String ccode) {
        this.ccode = ccode;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getIcao3() {
        return icao3;
    }

    public void setIcao3(String icao3) {
        this.icao3 = icao3;
    }

    public String getIcao4() {
        return icao4;
    }

    public void setIcao4(String icao4) {
        this.icao4 = icao4;
    }

    @Override
    public String toString() {
        return "AirPortInfoBean{" +
                "ID=" + ID +
                ", lat=" + lat +
                ", lng=" + lng +
                ", cname='" + cname + '\'' +
                ", ccname='" + ccname + '\'' +
                ", ccode='" + ccode + '\'' +
                ", scene='" + scene + '\'' +
                ", icao3='" + icao3 + '\'' +
                ", icao4='" + icao4 + '\'' +
                '}';
    }
}
