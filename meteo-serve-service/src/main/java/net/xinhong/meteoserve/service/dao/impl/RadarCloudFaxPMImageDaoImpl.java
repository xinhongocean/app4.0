package net.xinhong.meteoserve.service.dao.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.service.common.RadarStationInfoEnum;
import net.xinhong.meteoserve.service.dao.RadarCloudFaxPMImageDao;
import net.xinhong.meteoserve.service.domain.StationRadarInfoBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Tuple;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by xiaoyu on 16/8/8.
 */
@Repository
public class RadarCloudFaxPMImageDaoImpl implements RadarCloudFaxPMImageDao {
    private static final Log logger = LogFactory.getLog(RadarCloudFaxPMImageDaoImpl.class);
    @Resource
    private JedisCluster jedisCluster;


    private static final String NAMESPACE = "net.xinhong.meteoserve.service.radarcloudfax";

    @Autowired
    private SqlSessionTemplate sqlSession;

    private Float radarCLat;
    private Float radarCLng;

    @Override
    public void setRadarCLatLng(Float radarCLat, Float radarCLng) {
        this.radarCLat = radarCLat;
        this.radarCLng = radarCLng;
    }

    @Override
    public JSONObject getOceanFCInfo(String type, String year, String month, String day) {
        DateTime date = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day), 8, 0, 0);
        String key = "image:"+type;
        String field = date.toString("yyyyMMdd");
        String strData = jedisCluster.hget(key, field);
        JSONObject dataJSON = null;
        if (strData != null) {
            dataJSON = JSONObject.parseObject(strData);
        }else{
            int loop=0;
            while(loop<5) {
                date = date.minusDays(1);
                key = "image:" + type;
                field = date.toString("yyyyMMdd");
                try {
                    strData = jedisCluster.hget(key, field);
                }catch (Exception e){
                    e.printStackTrace();
                }
                if (strData != null) {
                    dataJSON = JSONObject.parseObject(strData);
                    break;
                }
            }
        }
        JSONObject resJson = new JSONObject();
        resJson.put(ResJsonConst.TIME, date.toString("yyyyMMddHH"));
        resJson.put(ResJsonConst.DATA, dataJSON);
        return resJson;
    }

    @Override
    public JSONObject getCoastRegionFC(String region, String year, String month, String day) {
        DateTime date = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day), 8, 0, 0);
        String key = "image:nmefcenvform";
        String field = date.toString("yyyyMMdd");
        String strData = jedisCluster.hget(key, field);
        if(strData==null||(strData!=null&&strData.isEmpty())){
            date=date.minusDays(1);
            field = date.toString("yyyyMMdd");
            strData = jedisCluster.hget(key, field);
        }
        JSONObject dataJSON = null;
        JSONObject finalDataJSON=null;
        if (strData != null) {
            dataJSON = JSONObject.parseObject(strData);
            JSONObject fc24= JSONObject.parseObject((String) dataJSON.get("fc24"));
            JSONObject fc48= JSONObject.parseObject((String) dataJSON.get("fc48"));
            String id=null;
            if(region.contains("|")) {
                String[] str=region.split("\\|");
                id=str[0];
                finalDataJSON = new JSONObject();
                finalDataJSON.put("fc24", fc24.get(id));
                finalDataJSON.put("fc48", fc48.get(id));
            }
        }
        JSONObject resJson = new JSONObject();
        resJson.put(ResJsonConst.TIME, date.toString("yyyyMMdd"));
        resJson.put(ResJsonConst.DATA, finalDataJSON);
        return resJson;
    }

    private List<StationRadarInfoBean> getNearstStationRadarFromLatLng(float lat, float lng, float delta) {
        if (delta > 10)
            delta = 10;
        else if (delta < 0.01)
            delta = 0.01f;
        Map<String, Object> params = new HashMap<>();
        params.put("lat", lat);
        params.put("lng", lng);
        //默认查询半径0.8度范围站点
        params.put("deltax", delta);
        params.put("deltay", delta);
        List<StationRadarInfoBean> staionInfoList = this.sqlSession.selectList(this.NAMESPACE + ".selectNearestbyLatLng", params);

        return staionInfoList;
    }

    @Override
    public List<StationRadarInfoBean> getStationRadarDistribInfo() {

        List<StationRadarInfoBean> staionInfoList = this.sqlSession.selectList(this.NAMESPACE + ".selectAllStationRadarDistribInfo");

        return staionInfoList;
    }


    @Override
    public JSONObject getRadarCloudInfo(String radarCloudType, String year, String month, String day, String hour, String minute,
                                        String radarIDs) {
        DateTime edate = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                Integer.parseInt(hour), Integer.parseInt(minute), 0);

        Set<Tuple> dataSet = null;
        DateTime sdate = null;
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyyMMddHHmm");
        boolean isHimawari8 = false;
        if (radarCloudType.startsWith("radar")
                || radarCloudType.startsWith("stationradar")
                || radarCloudType.startsWith("neareststationradar")) {
            sdate = edate.minusHours(3); //雷达图取最近3个小时
            String radarKeyName = DataTypeConst.IMAGE_RADARMAP;
            if (radarCloudType.startsWith("stationradar")
                    || radarCloudType.startsWith("neareststationradar")) {
                radarKeyName = DataTypeConst.IMAGE_SINGLE_RADARMAP;
            }
            dataSet = jedisCluster.zrangeByScoreWithScores(radarKeyName,
                    Double.parseDouble(sdate.toString("yyyyMMddHHmm")),
                    Double.parseDouble(edate.toString("yyyyMMddHHmm")));
        } else if (radarCloudType.startsWith("cloud")) {
            sdate = edate.minusHours(6); //云图取最近6小时
            dataSet = jedisCluster.zrangeByScoreWithScores(DataTypeConst.IMAGE_CLOUDMAP,
                    Double.parseDouble(sdate.toString("yyyyMMddHHmm")),
                    Double.parseDouble(edate.toString("yyyyMMddHHmm")));
        } else if (radarCloudType.contains("himawari8")) {
            sdate = edate.minusHours(6); //葵花8产品取最近6小时
            dataSet = jedisCluster.zrangeByScoreWithScores(radarCloudType,
                    Double.parseDouble(sdate.toString("yyyyMMddHHmm")),
                    Double.parseDouble(edate.toString("yyyyMMddHHmm")));
            //显示的时间需要为北京时
            sdate = sdate.plusHours(8);
            edate = edate.plusHours(8);
            isHimawari8 = true;
        } else if (radarCloudType.startsWith("jppm2dot5fc")) {
            sdate = edate.minusHours(6); //空气质量取最近6小时及未来5天
            edate = edate.plusHours(24 * 5);
            dataSet = jedisCluster.zrangeByScoreWithScores(DataTypeConst.IMAGE_JPPM2DOT5MAP,
                    Double.parseDouble(sdate.toString("yyyyMMddHHmm")),
                    Double.parseDouble(edate.toString("yyyyMMddHHmm")));
        }
        String scTime = sdate.toString("yyyyMMddHHmmss") + "_" + edate.toString("yyyyMMddHHmmss");

        ArrayList<JSONObject> dataJSON = null;
        //分数存放的即为时间!
        if (dataSet != null && dataSet.size() > 0) {
            Iterator it = dataSet.iterator();
            while (it.hasNext()) {
                Tuple tuple = (Tuple) it.next();
                if (tuple == null)
                    continue;
                JSONObject obj = JSONObject.parseObject(tuple.getElement());
                if (obj == null)
                    continue;

                if (dataJSON == null)
                    dataJSON = new ArrayList(20);

                if (isHimawari8) {
                    //过滤6小时前的数据
                    long lastHours = new Duration(DateTime.parse(obj.getString("date"), format),
                            DateTime.now()).getStandardHours();
                    if (lastHours > 6) {
                        continue;
                    }
                }
                dataJSON.add(obj);

            }
        }
        //单站雷达图像需要按站再进行分组存放
        if (radarCloudType.startsWith("stationradar") && dataJSON != null && !dataJSON.isEmpty()) {
            //1.获取单站雷达图站名列表,查询各个单站雷达图图片索引列表,拼接结果

            if (radarIDs != null && radarIDs.length() > 4) {
                String[] radarIDAry = radarIDs.split(",");

                ArrayList<JSONObject> stationRadarDataAry = new ArrayList<>(20);
                for (int i = 0; i < radarIDAry.length; i++) {
                    JSONArray imagesdata = new JSONArray();
                    for (Object obj : dataJSON) {
                        JSONObject json = (JSONObject) obj;
                        if (json == null)
                            continue;
                        if (json.getString("station") != null && json.getString("station").toLowerCase()
                                .equals(radarIDAry[i].toLowerCase())) {
                            imagesdata.add(json);
                        }
                    }
                    if (!imagesdata.isEmpty()) {
                        JSONObject stationRadarJSON = new JSONObject();
                        stationRadarJSON.put("imagesdata", imagesdata);
                        stationRadarJSON.put("radarID", radarIDAry[i]);
                        stationRadarJSON.put("radius", 400f);
                        stationRadarDataAry.add(stationRadarJSON);
                    }
                }
                dataJSON = stationRadarDataAry;
            } else { //todo:固定站点的单站雷达显示功能APP3.0后已经废弃！
                ArrayList<JSONObject> stationRadarDataAry = new ArrayList<>(20);
                for (RadarStationInfoEnum stationInfo : RadarStationInfoEnum.values()) {

                    JSONArray imagesdata = new JSONArray();
                    for (Object obj : dataJSON) {
                        JSONObject json = (JSONObject) obj;
                        if (json == null)
                            continue;
                        if (json.get("station") != null && json.get("station").equals(stationInfo.getCode())) {
                            imagesdata.add(json);
                        }
                    }
                    if (!imagesdata.isEmpty()) {
                        JSONObject stationRadarJSON = new JSONObject();
                        stationRadarJSON.put("imagesdata", imagesdata);
                        stationRadarJSON.put("cname", stationInfo.getCname());
                        stationRadarJSON.put("latlng", stationInfo.getLat() + "," + stationInfo.getLng());
                        stationRadarJSON.put("ename", stationInfo.getEname());
                        stationRadarJSON.put("radius", stationInfo.getRadius());
                        stationRadarDataAry.add(stationRadarJSON);
                    }
                }
                dataJSON = stationRadarDataAry;
            }
        }

        if (radarCloudType.startsWith("neareststationradar") && radarCLat != null
                && radarCLng != null && dataJSON != null && !dataJSON.isEmpty()) {
            //1.获取最近单站雷达图站名列表,查询各个单站雷达图图片索引列表,拼接结果
            List<StationRadarInfoBean> stationRadarInfoList = this.getNearstStationRadarFromLatLng(radarCLat, radarCLng, 8.0f);
            if (stationRadarInfoList == null || stationRadarInfoList.isEmpty()) {
                return null;
            }

            ArrayList<JSONObject> stationRadarDataAry = new ArrayList<>(20);
            for (StationRadarInfoBean radarInfoBean : stationRadarInfoList) {

                JSONArray imagesdata = new JSONArray();
                for (Object obj : dataJSON) {
                    JSONObject json = (JSONObject) obj;
                    if (json == null)
                        continue;
                    if (json.get("station") != null && json.getString("station").toLowerCase().equals(radarInfoBean.getStationID().toLowerCase())) {
                        imagesdata.add(json);
                    }
                }
                if (!imagesdata.isEmpty()) {
                    JSONObject stationRadarJSON = new JSONObject();
                    stationRadarJSON.put("imagesdata", imagesdata);
                    stationRadarJSON.put("cname", radarInfoBean.getCname());
                    stationRadarJSON.put("latlng", radarInfoBean.getClat() + "," + radarInfoBean.getClng());
                    stationRadarJSON.put("ename", radarInfoBean.getEname());
                    stationRadarJSON.put("radius", radarInfoBean.getRadius());
                    stationRadarDataAry.add(stationRadarJSON);
                }
            }
            dataJSON = stationRadarDataAry;
        }

        if (isHimawari8) { //按照时间升序排列
            if (dataJSON != null && !dataJSON.isEmpty()) {
                Collections.sort(dataJSON, new Comparator<JSONObject>() {
                    public int compare(JSONObject arg0, JSONObject arg1) {
                        return arg0.getString("date").compareTo(arg1.getString("date"));
                    }
                });
            } else {
                JSONObject resJson = new JSONObject();

                resJson.put(ResJsonConst.TIME, scTime);
                return resJson;
            }
        }

        JSONObject resJson = new JSONObject();

        resJson.put(ResJsonConst.TIME, scTime);
        resJson.put(ResJsonConst.DATA, dataJSON);
        return resJson;
    }


    @Override
    public JSONObject getHimawari8L2PointData(String strLat, String strLng, String ryear, String rmonth, String rday, String rhour,
                                              String rminute) {
        DateTime date = new DateTime(Integer.parseInt(ryear), Integer.parseInt(rmonth), Integer.parseInt(rday),
                Integer.parseInt(rhour), Integer.parseInt(rminute), 0).minusHours(8); //转换为世界时

        String key = DataTypeConst.HIMAWARI8L2_POINT_PREFIX + ":" + date.toString("yyyyMMddHHmm");

        String strData = jedisCluster.hget(key, strLat + "_" + strLng);
        JSONObject dataJSON = null;
        if (strData != null)
            dataJSON = JSONObject.parseObject(strData);


        JSONObject resJson = new JSONObject();

        resJson.put(ResJsonConst.TIME, date.plusHours(8).toString("yyyyMMddHHmm"));
        resJson.put(ResJsonConst.DATA, dataJSON);
        return resJson;
    }


    @Override
    public JSONObject getFaxInfo(String year, String month, String day, String hour, String minute) {

        DateTime date = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                Integer.parseInt(hour), 0, 0);
        String scTime = date.toString("yyyyMMddHH") + "00";


        String field = scTime;
        // logger.info("field =" + field);
        JSONArray faxJSON = JSONArray.parseArray(jedisCluster.hget(DataTypeConst.IMAGE_WXFAXMAP, field));

        if (faxJSON == null || faxJSON.isEmpty()) {
            return null;
        }

        JSONObject resJson = new JSONObject();

        resJson.put(ResJsonConst.TIME, scTime);
        resJson.put(ResJsonConst.DATA, faxJSON);
        return resJson;
    }

    @Override
    public JSONObject getECMWFImageInfo(String year, String month, String day, String hour, String minute) {
        DateTime date = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                Integer.parseInt(hour), 0, 0);
        //需要转换为世界时
        String field = date.minusHours(8).toString("yyyyMMddHH");
        //  String resString = jedisCluster.hget(DataTypeConst.IMAGE_ECMWFMAP, field);
        JSONArray ecImageJSON = JSONArray.parseArray(jedisCluster.hget(DataTypeConst.IMAGE_ECMWFMAP, field));

        if (ecImageJSON == null || ecImageJSON.isEmpty()) {
            return null;
        }

        JSONObject resJson = new JSONObject();

        resJson.put(ResJsonConst.TIME, date.toString("yyyyMMddHH") + "00");
        resJson.put(ResJsonConst.DATA, ecImageJSON);
        return resJson;
    }

}
