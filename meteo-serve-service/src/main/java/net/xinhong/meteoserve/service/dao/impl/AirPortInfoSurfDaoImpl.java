package net.xinhong.meteoserve.service.dao.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.service.dao.AirPortInfoSurfDao;
import net.xinhong.meteoserve.service.domain.AirPortInfoBean;
import net.xinhong.meteoserve.service.domain.WaypointInfoBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaoyu on 16/6/12.
 */
@Repository
public class AirPortInfoSurfDaoImpl implements AirPortInfoSurfDao{
    private static final Log logger = LogFactory.getLog(StationInfoSurfDaoImpl.class);
    private static final String NAMESPACE = "net.xinhong.meteoserve.service.airportinfosurf";

    @Autowired
    private SqlSessionTemplate sqlSession;


    @Override
    public AirPortInfoBean getNearstStaionFromLatLng(float lat, float lng) {
        Map<String,Object> params = new HashMap<>();
        params.put("slat", lat);
        params.put("slng", lng);
        //默认查询半径1.0度范围站点
        params.put("deltax", 1f);
        params.put("deltay", 1f);
        List<AirPortInfoBean> staionInfoList = this.sqlSession.selectList(this.NAMESPACE + ".selectNearestbyLatLng", params);
        if (staionInfoList == null || staionInfoList.isEmpty())
            return null;
        return staionInfoList.get(0);
    }

    @Override
    public List<AirPortInfoBean> getStationInfoFromNameIcao3Icao4(String param) {
        Map<String,Object> params = new HashMap<>();
        params.put("param", param.toUpperCase());
        params.put("limit", 11);
        return this.sqlSession.selectList(this.NAMESPACE + ".selectbyNameIcao3Icao4", params);
    }

    @Override
    public List<WaypointInfoBean> searchWaypointListFromName(String wayPointName) {
        Map<String,Object> params = new HashMap<>();
        params.put("name", wayPointName);
        params.put("limit", 10);
        return this.sqlSession.selectList(this.NAMESPACE + ".selectWaypointListbyName", params);
    }

    @Override
    public List<WaypointInfoBean> searchWaypointListFromRouteIdent(String routeidenty) {
        Map<String,Object> params = new HashMap<>();
        params.put("routeidenty", routeidenty);
        params.put("limit", 50);
        return this.sqlSession.selectList(this.NAMESPACE + ".selectWaypointListbyRouteIdent", params);
    }

}
