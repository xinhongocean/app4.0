package net.xinhong.meteoserve.service.dao.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.dao.TyphVolcaDataDao;
import net.xinhong.meteoserve.service.domain.TyphNameBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Tuple;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by xiaoyu on 16/7/1.
 */
@Repository
public class TyphVolcaDataDaoImpl extends DaoSupport implements TyphVolcaDataDao {
    @Resource
    private JedisCluster jedisCluster;
    private static final Log logger = LogFactory.getLog(WNIFCDataDaoImpl.class);

    private static final String NAMESPACE = "net.xinhong.meteoserve.service.typhvolca";

    @Autowired
    private SqlSessionTemplate sqlSession;

    @Override
    public JSONObject getTyphData(String year, String month, String day, String hour, String minute, int interMinutes, boolean isShowFinish) {

        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmm");
        //计算查询时间范围(30分钟到365天之间)
        if (interMinutes < 30)
            interMinutes = 30;
        if (interMinutes > 365*24*60)
            interMinutes = 365*24*60;

        DateTime edate = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                Integer.parseInt(hour), Integer.parseInt(minute), 0);
        String streDate = edate.toString(dateformat);

        DateTime sdate = edate.minusMinutes(interMinutes);
        String strsDate = sdate.toString(dateformat);


        //查询北京编报中心台风
        Set<String> strDataSet = jedisCluster.zrangeByScore(DataTypeConst.TYPHDATA_BABJ, Double.parseDouble(strsDate),
                Double.parseDouble(streDate));

        JSONArray jsonArray = null;
        if (strDataSet != null && strDataSet.size() > 0){
            jsonArray = new JSONArray();
            Iterator it = strDataSet.iterator();
            while(it.hasNext()) {
                JSONObject bjtyphobj = JSONObject.parseObject(it.next() + "");
                boolean isfinished = false;
                if (bjtyphobj == null )
                    continue;
                JSONArray points = bjtyphobj.getJSONArray("points");
                if (points == null || points.isEmpty())
                    continue;
                //取最后一条记录,如果时间晚于现在16小时以上,则认为台风已经消亡!
                JSONObject lastPoint = points.getJSONObject(points.size()-1);
                if (lastPoint == null)
                    continue;

                String strLastDate = lastPoint.getString("odate");
                if (strLastDate == null || strLastDate.isEmpty())
                    continue;
                DateTime lastDate = DateTime.parse(strLastDate, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
                if (lastDate != null) {
                    long lastHours = new Duration(lastDate, DateTime.now()).getStandardHours();
                    if (lastHours>1*16){
                        bjtyphobj.put("isfinished", true);
                        isfinished = true;
                        if (!isShowFinish)
                            continue;
                    } else {
                        bjtyphobj.put("isfinished", false);
                    }
                }

//                if (!isShowFinish){
//                    String strLastDate = lastPoint.getString("odate");
//                    if (strLastDate == null || strLastDate.isEmpty())
//                        continue;
//                    DateTime lastDate = DateTime.parse(strLastDate, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
//                    if (lastDate != null) {
//                        long lastHours = new Duration(lastDate, DateTime.now()).getStandardHours();
//                        if (lastHours>1*16)
//                            continue;
//                    }
//                }

                for (int i  = 0; i < points.size(); i++){
                    JSONObject point = points.getJSONObject(i);
                    //防止中心风速及移动速度显示为null!
                    JSONObject centerWS = point.getJSONObject("centerWS");
                    if (centerWS == null){
                        point.put("centerWS", "");
                    }
                    JSONObject pastWS = point.getJSONObject("pastWS");
                    if (pastWS == null){
                        point.put("pastWS", "");
                    }
                    //去除所有已消亡台风的预报信息，减少返回的数据量
                    if (isfinished){
                        //todo:为保证安卓APP端查询不出错（直接移除foreInfo节点安卓3.0版本由于代码问题报错），这里替换foreInfo节点
                        JSONObject obj0 = new JSONObject();
                        JSONObject obj1 = new JSONObject();
                        obj1.put("n", " ");
                        obj0.put("n", obj1);
                        point.put("foreInfo", obj0);
//                      point.remove("foreInfo");
                    }
                }

                bjtyphobj = this.procTYPHData(bjtyphobj);

                jsonArray.add(bjtyphobj);
            }
        }

        //查询日本编报中心台风
        Set<String> stRJrDataSet = jedisCluster.zrangeByScore(DataTypeConst.TYPHDATA_RJTD, Double.parseDouble(strsDate),
                Double.parseDouble(streDate));
        if (stRJrDataSet != null && stRJrDataSet.size() > 0){
            if (jsonArray == null)
                jsonArray = new JSONArray();
            Iterator it = stRJrDataSet.iterator();
            while(it.hasNext()) {
                JSONObject rjtyphobj = JSONObject.parseObject(it.next() + "");
                if (rjtyphobj == null )
                    continue;
                JSONArray points = rjtyphobj.getJSONArray("points");
                if (points == null || points.isEmpty())
                    continue;
                //取最后一条记录,如果时间晚于现在16小时以上,则认为台风已经消亡!
                JSONObject lastPoint = points.getJSONObject(points.size()-1);

                if (lastPoint == null)
                    continue;

                String strLastDate = lastPoint.getString("odate");
                if (strLastDate == null || strLastDate.isEmpty())
                    continue;
                DateTime lastDate = DateTime.parse(strLastDate, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
                if (lastDate != null) {
                    long lastHours = new Duration(lastDate, DateTime.now()).getStandardHours();
                    if (lastHours>1*16){
                        //日本已消亡的台风不放入传出的数据中！
                        continue;
//                        rjtyphobj.put("isfinished", true);
//                        if (!isShowFinish)
//                            continue;
                    }else {
                        rjtyphobj.put("isfinished", false);
                    }
                }
//                if (!isShowFinish){
//                    String strLastDate = lastPoint.getString("odate");
//                    if (strLastDate == null || strLastDate.isEmpty())
//                        continue;
//                    DateTime lastDate = DateTime.parse(strLastDate, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
//                    if (lastDate != null) {
//                        long lastHours = new Duration(lastDate, DateTime.now()).getStandardHours();
//                        if (lastHours>1*16)
//                            continue;
//                    }
//                }
                //todo:处理8方向,改为4方向,防止APP端闪退!
                JSONArray r70 = lastPoint.getJSONArray("r70");
                JSONArray r50 = lastPoint.getJSONArray("r50");
                JSONArray r30 = lastPoint.getJSONArray("r30");
                if (r70 != null && r70.size() == 8){
                    JSONArray r70tmp = new JSONArray();
                    r70tmp.add(r70.get(0));
                    r70tmp.add(r70.get(2));
                    r70tmp.add(r70.get(5));
                    r70tmp.add(r70.get(7));
                    lastPoint.put("r70", r70tmp);
                }
                if (r50 != null && r50.size() == 8){
                    JSONArray r50tmp = new JSONArray();
                    r50tmp.add(r50.get(0));
                    r50tmp.add(r50.get(2));
                    r50tmp.add(r50.get(5));
                    r50tmp.add(r50.get(7));
                    lastPoint.put("r50", r50tmp);
                }
                if (r30 != null && r30.size() == 8){
                    JSONArray r30tmp = new JSONArray();
                    r30tmp.add(r30.get(0));
                    r30tmp.add(r30.get(2));
                    r30tmp.add(r30.get(5));
                    r30tmp.add(r30.get(7));
                    lastPoint.put("r30", r30tmp);
                }

                //防止中心风速及移动速度显示为null!
                JSONObject centerWS = lastPoint.getJSONObject("centerWS");
                if (centerWS == null){
                    lastPoint.put("centerWS", "");
                }
                JSONObject pastWS = lastPoint.getJSONObject("pastWS");
                if (pastWS == null){
                    lastPoint.put("pastWS", "");
                }

                //日本预报只保留最后一个点数据
                points = new JSONArray();
                points.add(lastPoint);
                rjtyphobj.put("points", points);
                rjtyphobj = this.procTYPHData(rjtyphobj);

                jsonArray.add(rjtyphobj);
            }
        }

        JSONObject resJson = new JSONObject();
        if (jsonArray != null && jsonArray.isEmpty())
            jsonArray = null;

        //返回时间转换为北京时
        resJson.put(ResJsonConst.TIME, strsDate + "_" + streDate);
        resJson.put(ResJsonConst.DATA, jsonArray);
        return resJson;
    }


    @Override
    public JSONObject getTyphLastTimeData(String year, String month, String day, String hour, String minute, int interMinutes) {

        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmm");
        //计算查询时间范围(30分钟到12小时之间)
        if (interMinutes < 30)
            interMinutes = 30;
        if (interMinutes > 365*24*60)
            interMinutes = 365*24*60;
        DateTime edate = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                Integer.parseInt(hour), Integer.parseInt(minute), 0);
        String streDate = edate.toString(dateformat);

        DateTime sdate = edate.minusMinutes(interMinutes);
        String strsDate = sdate.toString(dateformat);

        //查询北京编报中心
        Set<Tuple> dataSet = jedisCluster.zrangeByScoreWithScores(DataTypeConst.TYPHDATA_BABJ, Double.parseDouble(strsDate),
                Double.parseDouble(streDate));
        JSONObject resTimeJSON = new JSONObject();
        //分数存放的即为时间!
        if (dataSet != null && dataSet.size() > 0){
            Iterator it = dataSet.iterator();
            while(it.hasNext()) {
                Tuple tuple = (Tuple) it.next();
                if (tuple == null)
                    continue;
                JSONObject obj = JSONObject.parseObject(tuple.getElement());
                if (obj == null )
                    continue;
                String id = obj.getString("id");
                if (id == null || id.isEmpty())
                    continue;
                Double score = tuple.getScore();
                resTimeJSON.put(id, "" + Long.valueOf(Math.round(score)));
            }
        }
        //查询日本编报中心
        Set<Tuple> dataRJSet = jedisCluster.zrangeByScoreWithScores(DataTypeConst.TYPHDATA_RJTD, Double.parseDouble(strsDate),
                Double.parseDouble(streDate));
        if (dataRJSet != null && dataRJSet.size() > 0){
            Iterator it = dataRJSet.iterator();
            while(it.hasNext()) {
                Tuple tuple = (Tuple) it.next();
                if (tuple == null)
                    continue;
                JSONObject obj = JSONObject.parseObject(tuple.getElement());
                if (obj == null )
                    continue;
                String id = obj.getString("id");
                if (id == null || id.isEmpty())
                    continue;
                Double score = tuple.getScore();
                if (resTimeJSON == null)
                    resTimeJSON = new JSONObject();
                resTimeJSON.put(id, "" + Long.valueOf(Math.round(score)));
            }
        }

        JSONObject resJson = new JSONObject();
        if (resTimeJSON.isEmpty())
            resTimeJSON = null;

        //返回时间转换为北京时
        resJson.put(ResJsonConst.TIME, strsDate + "_" + streDate);
        resJson.put(ResJsonConst.DATA, resTimeJSON);
        return resJson;
    }

    /**
     * 台风后处理
     * @param typhJSON
     * @return
     */
    private JSONObject procTYPHData(JSONObject typhJSON) {
        if (typhJSON == null)
            return  null;

        //处理名称
        String eName = typhJSON.getString("ename");
        String cName = "";
        if (eName != null && !eName.isEmpty()){
            if (eName.toUpperCase().equals("UNKNOWN")
                    || eName.toUpperCase().equals("'UNKNOWN'")
                    || eName.toUpperCase().equals("NAMELESS")
                    || eName.toUpperCase().equals("'NAMELESS'")){
                cName = "未命名";
            } else if (eName.toUpperCase().equals("TD")){
                cName = "热带低压";
            } else {
                Map<String,Object> params = new HashMap<>();
                params.put("ename", eName.replace("'", "").toUpperCase());
                TyphNameBean typhname = this.sqlSession.selectOne(this.NAMESPACE + ".selectbyename", params);

                if (typhname != null && typhname.getCname() != null && !typhname.getCname().isEmpty()){
                    cName = typhname.getCname();
                }
            }
        }
        typhJSON.put("cname", cName);

        //处理编报中心
        String ccc = typhJSON.getString("ccc");
        if (ccc != null && !ccc.isEmpty()){
            if (ccc.contains("BABJ")){
                ccc = "中国";

            }
            if (ccc.contains("RJ")){
                ccc = "日本";
            }
            typhJSON.put("ccc", ccc);
        }

        return  typhJSON;
    }

    @Override
    protected void checkDaoConfig() throws IllegalArgumentException {
        Assert.notNull(this.sqlSession, "Property 'sqlSessionFactory' or 'sqlSessionTemplate' are required");
    }

    @Override
    public JSONObject getVolcaData(String year, String month, String day, String hour, String minute, int interMinutes) {
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmm");
        //计算查询时间范围
        if (interMinutes < 30)
            interMinutes = 30;
        if (interMinutes > 3*24*60)
            interMinutes = 3*24*60;

        DateTime edate = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                Integer.parseInt(hour), Integer.parseInt(minute), 0);
        String streDate = edate.toString(dateformat);

        DateTime sdate = edate.minusMinutes(interMinutes);
        String strsDate = sdate.toString(dateformat);
        //转换为世界时进行查询
        DateTime edateutc = edate.minusHours(8);
        String streDateUTC = edateutc.toString(dateformat);

        DateTime sdateutc = edateutc.minusMinutes(interMinutes);
        String strsDateUTC = sdateutc.toString(dateformat);

        Set<String> strDataSet = jedisCluster.zrangeByScore("volcanodata", Double.parseDouble(strsDateUTC),
                Double.parseDouble(streDateUTC));

        //zrangebyscore volcanodata 201607010800 201607072000 withscores
        JSONObject dataJSON = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        if (strDataSet != null && strDataSet.size() > 0){
            jsonArray = new JSONArray();
            Iterator it = strDataSet.iterator();
            while(it.hasNext()) {
                JSONObject obj = JSONObject.parseObject(it.next() + "");

                jsonArray.add(obj);
            }
        }
        JSONObject resJson = new JSONObject();

        //返回时间转换为北京时
        resJson.put(ResJsonConst.TIME, strsDate + "_" + streDate);
        resJson.put(ResJsonConst.DATA, jsonArray);
        return resJson;
    }
}
