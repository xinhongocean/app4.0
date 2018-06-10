package net.xinhong.meteoserve.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.dao.HYCOMDataDao;
import net.xinhong.meteoserve.service.dao.TyphStatisticDataDao;
import net.xinhong.meteoserve.service.service.TyphStatisticDataService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by wingsby on 2017/12/28.
 */
@Service
public class TyphStatisticDataServiceImpl implements TyphStatisticDataService {
    private static final Log logger = LogFactory.getLog(TyphStatisticDataServiceImpl.class);
    float delta=2.5f;
    @Autowired
    private TyphStatisticDataDao typhStatisticDataDao;

    @Override
    public JSONObject getPointData(String lat, String lon, String year, String month, String table){
        JSONObject resJSON = new JSONObject();
        float flat = Float.parseFloat(lat);
        float flng = Float.parseFloat(lon);
        if (flat > 90 || flat < -90 || flng > 360 || flng < -180) { //为保证结果能对应,有一个参数不合法则认为整体参数不合法!
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }
        String strLat = String.format("%.2f", Math.round(flat / delta) * delta);
        String strLng = String.format("%.2f", Math.round(flng / delta) * delta);

        JSONObject daoJSON=typhStatisticDataDao.getPointData(lat,lon,year,month,table);
        JSONObject dataJSON=new JSONObject();
        dataJSON.put(ResJsonConst.DATA,daoJSON);
        if (dataJSON==null||dataJSON.isEmpty()||dataJSON.getString(ResJsonConst.DATA) == null
                ||dataJSON.getString(ResJsonConst.DATA).isEmpty()){
            JSONOperateTool.putJSONNoResult(dataJSON);
            return dataJSON;
        }
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON);
        return resJSON;
    }
}
