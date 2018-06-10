package net.xinhong.meteoserve.service.dao.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.service.common.Const;
import net.xinhong.meteoserve.service.dao.ICOADSStatisticDao;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;

/**
 * Created by wingsby on 2017/12/28.
 */
@Repository
public class ICOADSStatisticDaoImpl implements ICOADSStatisticDao {
    @Resource
    private JedisCluster jedisCluster;

    @Override
    public JSONObject getPointData(String strLats, String strLngs, String month,String fourth, String table,String space) {
        String key= Const.ICOADS_REAL_PREFIX+":"+space+":"+table;
        String field=String.format("%4.1f_%4.1f_%02d",Float.valueOf(strLngs),
                Float.valueOf(strLats),Integer.valueOf(month)).trim();
        if(fourth!=null)field=field+"_"+fourth;
        String strData=jedisCluster.hget(key,field);
        if(strData!=null)
            return JSONObject.parseObject(strData);
        return null;
    }
}
