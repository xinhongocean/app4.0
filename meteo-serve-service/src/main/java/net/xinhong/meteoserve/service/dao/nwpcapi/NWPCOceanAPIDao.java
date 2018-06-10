package net.xinhong.meteoserve.service.dao.nwpcapi;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by xiaoyu on 17/2/21.
 */
public interface NWPCOceanAPIDao {
    JSONObject getWindWaveDataFromPostion(String strLat, String strLng, String ryear, String rmonth, String rday, String rhour, String vti);
}
