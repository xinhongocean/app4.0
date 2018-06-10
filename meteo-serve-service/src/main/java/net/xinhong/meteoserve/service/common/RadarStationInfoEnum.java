package net.xinhong.meteoserve.service.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cui on 2016/10/24.
 */
public enum RadarStationInfoEnum {


//    AZ9010("beijing",  "北京","az9010","39.75","116.33", 460f),
//    AZ9210("shanghai", "上海(南汇)","az9210","31.05","121.77", 460f),
//    AZ9250("nanjing",  "南京","az9250","32.0","118.8", 460f),
//    AZ9991("wulumuqi", "乌鲁木齐","az9991","43.78","87.62", 460f),
//    AZ9759("zhanjiang", "湛江","az9759","21.15","110.3", 460f),
//    AZ9200("guangzhou", "广州","az9200","23.17","113.33", 460f),
//    AZ9754("shantou", "汕头","az9754","23.4","116.68", 460f),
//    AZ9592("xiamen", "厦门","az9592","24.48","118.08", 460f),
//    AZ9591("fuzhou", "福州","az9591","26.08","119.28", 460f),
//    AZ9577("wenzhou", "温州","az9577","28.02","120.67", 460f);


    AZ9010("beijing",  "北京","az9010","39.8117","116.4667", 460f),
    AZ9210("shanghai", "上海(南汇)","az9210","31.0014","121.8847", 460f),
    AZ9250("nanjing",  "南京","az9250","32.1908","118.6978", 460f),
    AZ9991("wulumuqi", "乌鲁木齐","az9991","43.9161","87.0508", 460f),
    AZ9759("zhanjiang", "湛江","az9759","21.15","110.3", 460f), //数据库中无！
    AZ9200("guangzhou", "广州","az9200","23.0039","113.355", 460f),
    AZ9754("shantou", "汕头","az9754","23.4","116.68", 460f), //数据库中无！
    AZ9592("xiamen", "厦门","az9592","24.4819","118.0678", 460f),
    AZ9591("fuzhou", "福州","az9591","25.9819","119.5319", 460f),
    AZ9577("wenzhou", "温州","az9577","28.02","120.67", 460f); //数据库中无！

    private String ename;
    private String cname;
    private String code;
    private String lat;
    private String lng;
    private float radius;

    // 构造方法
    RadarStationInfoEnum(String ename, String cname, String code, String lat, String lng, float radius) {
        this.ename = ename;
        this.cname = cname;
        this.code = code;
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
    }

    public static Map<String,String> getLatLngByCode(String code){
        Map<String,String> map = new HashMap<String,String>();
        for (RadarStationInfoEnum c : RadarStationInfoEnum.values()){
            if(code != null){
                if(code.endsWith(c.code)){
                    map.put("lat",c.lat);
                    map.put("lng",c.lng);
                    return map;
                }
            }
        }
        return null;
    }

    //根据经纬度拿到站号
    public static String getCodeByLatLng(String lat, String lng){
        for (RadarStationInfoEnum c : RadarStationInfoEnum.values()){
            if(lat != null && lng != null){
                if(lat.endsWith(c.lat) && lng.endsWith(c.lng)){
                    return c.code;
                }
            }
        }
        return null;
    }

    //根据站号拿到拼音名称
    public static String getEname(String code){
        for (RadarStationInfoEnum c : RadarStationInfoEnum.values()){
            if(code!=null){
                if(code.endsWith(c.code)){
                    return c.ename;
                }
            }
        }
        return null;
    }

    //根据中文拼音拿到站号
    public static String getCode(String ename){
        for (RadarStationInfoEnum c : RadarStationInfoEnum.values()){
            if(ename!=null){
                if(ename.endsWith(c.ename)){
                    return c.code;
                }
            }
        }
        return null;
    }
    //根据站号拿到中文名
    public static String getCname(String code){
        for (RadarStationInfoEnum c : RadarStationInfoEnum.values()){
            if(code!=null){
                if(code.endsWith(c.code)){
                    return c.cname;
                }
            }
        }
        return null;
    }
    public String getEname() {
        return ename;
    }

    public void setEname(String ename) {
        this.ename = ename;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }
    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
    public float getRadius() {return this.radius; }


}
