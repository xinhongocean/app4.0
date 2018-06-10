package net.xinhong.meteoserve.service.domain;

/**
 * Created by xiaoyu on 16/11/19.
 */
public class WaypointInfoBean {
    private Integer ID;
    private Float lat;
    private Float lng;
    private String name;
    private String wayseque;
    private String routeident;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWayseque() {
        return wayseque;
    }

    public void setWayseque(String wayseque) {
        this.wayseque = wayseque;
    }

    public String getRouteident() {
        return routeident;
    }

    public void setRouteident(String routeident) {
        this.routeident = routeident;
    }

}
