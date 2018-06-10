package net.xinhong.meteoserve.service.dao.impl;

import net.xinhong.meteoserve.service.dao.StationInfoSurfDao;
import net.xinhong.meteoserve.service.domain.StationInfoSurfBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.*;

/**
 * Description: <br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 <a href=mailto:liusofttech@sina.com>刘晓昌</a>
 * @version 创建时间：2016/3/7.
 */
@Repository
public class StationInfoSurfDaoImpl extends DaoSupport implements StationInfoSurfDao {
    private static final Log logger = LogFactory.getLog(StationInfoSurfDaoImpl.class);
    private static final String NAMESPACE = "net.xinhong.meteoserve.service.stationinfosurf";

    @Autowired
    private SqlSessionTemplate sqlSession;

    private static List<StationInfoSurfBean> selectedSurffHighStationList;
    private static List<String> selectedSurfHighStationCodeList;

    private static Map<String, String> cityStationCodeMap; //行政区划编码与站号映射静态存储

    @Override
    public String getStationCodeFromCityCode(String cityCode){
        if (cityStationCodeMap == null || cityStationCodeMap.isEmpty()){
            cityStationCodeMap = new HashMap<>(2500); //约2200县级市
            List<StationInfoSurfBean> staionInfoList =
                    this.sqlSession.selectList(this.NAMESPACE + ".selectCityStationCodeMap", null);
            if (staionInfoList == null || staionInfoList.isEmpty())
                return null;
            for (int i = 0; i < staionInfoList.size(); i++){
                cityStationCodeMap.put(staionInfoList.get(i).getCityCode(),
                        staionInfoList.get(i).getStationCode());
            }
        }

        if (cityStationCodeMap == null || cityStationCodeMap.isEmpty())
            return null;
        return cityStationCodeMap.get(cityCode);
    }

    @Override
    public StationInfoSurfBean getNearstStaionFromLatLng(float lat, float lng,String dataType, float delta) {
        if (delta > 10)
            delta = 10;
        else if (delta < 0.01)
            delta = 0.01f;
        Map<String,Object> params = new HashMap<>();
        params.put("slat", lat);
        params.put("slng", lng);
        //默认查询半径0.8度范围站点
        params.put("deltax", delta);
        params.put("deltay", delta);
        params.put("dataType", dataType);
        List<StationInfoSurfBean> staionInfoList = this.sqlSession.selectList(this.NAMESPACE + ".selectNearestbyLatLng", params);
        if (staionInfoList == null || staionInfoList.isEmpty())
            return null;
        return staionInfoList.get(0);
    }

    @Override
    public List<String> getStationCodeFromName(String cname) {
        return null;
    }

    @Override
    public StationInfoSurfBean getStationInfoFromCode(String stationCode) {
        Map<String,Object> params = new HashMap<>();
        params.put("stationCode", stationCode);
        List<StationInfoSurfBean> reslist = this.sqlSession.selectList(this.NAMESPACE + ".selectbyStationCode", params);

        if (reslist == null || reslist.isEmpty())
            return null;
        return  reslist.get(0);
    }

    @Override
    public StationInfoSurfBean getSelectedStationInfoFromCode(String stationCode) {
        if (selectedSurfHighStationCodeList == null){
            if (selectedSurffHighStationList == null){
                this.getSelectedSurfHighStationInfoList();
            }
            if (selectedSurffHighStationList == null) //返回内容已按照站号排序
                return null;
            selectedSurfHighStationCodeList = new ArrayList<>(selectedSurffHighStationList.size());
            for (int i = 0; i < selectedSurffHighStationList.size(); i++){
                selectedSurfHighStationCodeList.add(selectedSurffHighStationList.get(i).getStationCode());
            }
        }
        if (selectedSurfHighStationCodeList == null)
            return null;

        int index = Collections.binarySearch(selectedSurfHighStationCodeList, stationCode);
        if (index < 0)
            return null;

        return  selectedSurffHighStationList.get(index);
    }

    @Override
    public List<StationInfoSurfBean> getStationInfoFromCname(String cname) {
        Map<String,Object> params = new HashMap<>();
        params.put("cname", cname);
        params.put("limit", 10);
        List<StationInfoSurfBean> reslist = this.sqlSession.selectList(this.NAMESPACE + ".selectbyCnameOrPy", params);
        return  reslist;
    }

    @Override
    public List<StationInfoSurfBean> getStationInfoFromPy(String py) {
        Map<String,Object> params = new HashMap<>();
        params.put("py", py);
        params.put("limit", 10);
        return this.sqlSession.selectList(this.NAMESPACE + ".selectbyCnameOrPy", params);
    }

    @Override
    protected void checkDaoConfig() throws IllegalArgumentException {
        Assert.notNull(this.sqlSession, "Property 'sqlSessionFactory' or 'sqlSessionTemplate' are required");
    }

    @Override
    public List<StationInfoSurfBean> getStationInfoFromPyNameCode(String param) {
        Map<String,Object> params = new HashMap<>();
        params.put("param", param.toUpperCase());
        params.put("limit", 11);
        return this.sqlSession.selectList(this.NAMESPACE + ".selectbyPyNameCode", params);
    }

    @Override
    public List<StationInfoSurfBean> getSelectedSurfHighStationInfoList() {
        long tt = System.currentTimeMillis();
        if (selectedSurffHighStationList == null){
            Map<String,Object> params = new HashMap<>();
            selectedSurffHighStationList = this.sqlSession.selectList(this.NAMESPACE + ".selecStationCodeList", params);
        }
        logger.info("站点信息查询耗费时间:" + (System.currentTimeMillis() - tt));
        return selectedSurffHighStationList;

    }



}
