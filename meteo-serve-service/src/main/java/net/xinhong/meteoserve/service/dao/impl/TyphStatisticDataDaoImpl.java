package net.xinhong.meteoserve.service.dao.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.service.common.Const;
import net.xinhong.meteoserve.service.dao.TyphStatisticDataDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;

/**
 * Created by wingsby on 2017/12/28.
 */
@Repository
public class TyphStatisticDataDaoImpl implements TyphStatisticDataDao {
    @Resource
    private JedisCluster jedisCluster;

    @Override
    public JSONObject getPointData(String lat,String lon, String year, String month, String table){
        String key= Const.TYPH_STATISTIC_REAL_PREFIX + ":" + table;
        String field=String.format("%4.1f_%4.1f",Float.valueOf(lon),Float.valueOf(lat));
        if(month!=null)field+=("_"+month);
        if(year!=null)field+=("_"+year);
        String strdata=jedisCluster.hget(key,field);
        JSONObject dataJSON = null;
        if(strdata!=null)
            dataJSON=JSON.parseObject(strdata);
        return dataJSON;
    }
}
