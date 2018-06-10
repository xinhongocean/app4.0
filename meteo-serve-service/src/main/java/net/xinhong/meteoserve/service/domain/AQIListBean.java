package net.xinhong.meteoserve.service.domain;

/**
 * Created by xiaoyu on 17/1/5.
 */
public class AQIListBean {
    private String stationCode;

    public Integer getDlv() {
        return dlv;
    }

    public void setDlv(Integer dlv) {
        this.dlv = dlv;
    }

    private Integer dlv;
    private Float lat;
    private Float lng;
    private String cityCode;
    private String aqi;

    public String getStationCode() {
        return stationCode;
    }

    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
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

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getAqi() {
        return aqi;
    }

    public void setAqi(String aqi) {
        this.aqi = aqi;
    }
}
