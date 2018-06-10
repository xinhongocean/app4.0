package net.xinhong.meteoserve.service.dao.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ElemGFS;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.dao.GTSPPDataDao;
import net.xinhong.meteoserve.service.dao.HYCOMDataDao;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;
import java.util.GregorianCalendar;
import java.util.Map;

/**
 * Created by wingsby on 2017/12/28.
 */
@Repository
public class GTSPPDataDaoImpl implements GTSPPDataDao {
    @Resource
    private JedisCluster jedisCluster;

    @Override
    public JSONObject getPointData(String year, String month,
                                   String day) {
        DateTime date = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                8, 0, 0);
        //这里将给定的时间转换为世界时
        date = date.minusHours(8);
//        String field =String.format("%6.1f",Float.valueOf(strLat)).trim() + "_" + String.format("%6.1f",Float.valueOf(strLng)).trim();
        String key = DataTypeConst.GTSPP_POINT_PREFIX + ":" +date.toString("yyyyMMdd");
        Map<String,String> strData =null;
        try {
            strData=jedisCluster.hgetAll(key);
        }catch (Exception e){
            e.printStackTrace();
        }
        boolean islast = false; //是否向前推一个起报时间
        int looptime=0;
        while(strData==null||(strData!=null&&strData.size()==0)&&looptime<10){
            date = date.minusDays(1);
            key = DataTypeConst.GTSPP_POINT_PREFIX + ":" +date.toString("yyyyMMdd");
            try {
                strData=jedisCluster.hgetAll(key);
            }catch (Exception e){
                e.printStackTrace();
            }
            looptime++;
        }

        long stime=System.currentTimeMillis();
        JSONObject dataJSON = null;
        if (strData != null){
            dataJSON=new JSONObject();
            for(String skey:strData.keySet()){
                JSONObject obj=JSONObject.parseObject(strData.get(skey));
                JSONObject tmp=new JSONObject();
                tmp.put("lng",obj.get("lng"));
                tmp.put("lat",obj.get("lat"));
                tmp.put("cid",obj.get("cid"));
                dataJSON.put(skey,tmp);
            }
        }
        System.out.println(System.currentTimeMillis()-stime);
        JSONObject resJSON=new JSONObject();
        resJSON.put(ResJsonConst.TIME,date.toString("yyyyMMdd"));
        resJSON.put(ResJsonConst.DATA,dataJSON);
        return resJSON;
    }


}
