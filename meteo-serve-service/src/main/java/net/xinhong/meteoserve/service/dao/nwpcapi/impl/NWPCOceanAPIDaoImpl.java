package net.xinhong.meteoserve.service.dao.nwpcapi.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.dao.nwpcapi.NWPCOceanAPIDao;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;

/**
 * Created by xiaoyu on 17/2/21.
 */
@Repository
public class NWPCOceanAPIDaoImpl implements NWPCOceanAPIDao {
    @Resource
    private JedisCluster jedisCluster;

    @Override
    public JSONObject getWindWaveDataFromPostion(String strLat, String strLng, String ryear, String rmonth, String rday, String rhour, String VTI) {
        DateTime date = new DateTime(Integer.parseInt(ryear), Integer.parseInt(rmonth), Integer.parseInt(rday),
                Integer.parseInt(rhour), 0, 0);
//        String scTime = StringUtils.leftPad(ryear, 4, "0") + StringUtils.leftPad(rmonth, 2, "0") + StringUtils.leftPad(rday, 2, "0")
//                + StringUtils.leftPad(rhour, 2, "0") + "00_" + StringUtils.leftPad(VTI, 3, "0");
        //这里将给定的时间转换为世界时
        date = date.minusHours(8);
        VTI = StringUtils.leftPad(VTI, 3, "0");
        String field = strLat + "_" + strLng;

        String key = DataTypeConst.NWPCOCEAN_FC_POINT_PREFIX + ":" +date.toString("yyyyMMddHH")+VTI;

        String strTmp = DataTypeConst.NWPCOCEAN_FC_POINT_PREFIX + ":" +date.toString("yyyyMMddHH")+VTI + " " + field;
        String strData = jedisCluster.hget(key, field);
        if (strData == null || strData.isEmpty()){ //如果当前起报时间没有找到,则向前一个起报时间查找
            int lastVTIsub = 12;
            date = date.minusHours(lastVTIsub);
            VTI = StringUtils.leftPad(Integer.parseInt(VTI)+lastVTIsub+"", 3, "0");

            strData   = jedisCluster.hget(DataTypeConst.NWPCOCEAN_FC_POINT_PREFIX + ":" +date.toString("yyyyMMddHH")+VTI, field);

            date = date.plusHours(8); //返回时间转换为北京时
//            scTime = date.toString("yyyy") + date.toString("MM") +  date.toString("dd")
//                    +  date.toString("HH") + "00_" + StringUtils.leftPad(VTI, 3, "0");
        }
        JSONObject dataJSON = null;
        if (strData != null)
            dataJSON = JSONObject.parseObject(strData);

        JSONObject resJson = new JSONObject();
        resJson.put("VTI", Integer.parseInt(VTI));
//        resJson.put(ResJsonConst.TIME, scTime);
        resJson.put(ResJsonConst.DATA, dataJSON);
        return resJson;
    }
}
