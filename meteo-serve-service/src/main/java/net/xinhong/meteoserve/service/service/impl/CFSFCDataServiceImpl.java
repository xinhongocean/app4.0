package net.xinhong.meteoserve.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.dao.CFSFCDataDao;
import net.xinhong.meteoserve.service.dao.StationInfoSurfDao;
import net.xinhong.meteoserve.service.service.CFSFCDataService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xiaoyu on 16/9/19.
 */
@Service
public class CFSFCDataServiceImpl implements CFSFCDataService {
    private static final Log logger = LogFactory.getLog(StationDataSurfServiceImpl.class);

    @Autowired
    private CFSFCDataDao cfsDataDao;

    @Override
    public JSONObject getDataFromLatlng(float lat, float lng, String year, String month, String day, String hour, String yearFC, String monthFC) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = this.initJSON(lat, lng, year, month, day, hour, yearFC, monthFC);
        if (resJSON.get(ResJsonConst.STATUSCODE).equals(ResStatus.PARAM_ERROR.getStatusCode())){
            return resJSON;
        }

        //2.然后获取数据
        JSONObject daoJSON = cfsDataDao.getDataFromLatlng(lat, lng, year, month, day, hour, yearFC, monthFC);

        //3.根据获取结果拼接结果JSON中数据段
        if (daoJSON != null){
            resJSON.put(ResJsonConst.TIME, daoJSON.getString(ResJsonConst.TIME));
        }
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")){
            logger.info("给定的经纬度:"+lat + "," + lng + "附近及时间要素没有查询到数据!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    @Override
    public JSONObject getDataFromCode(String strCode, String year, String month, String day, String hour, String yearFC, String monthFC) {
        return null;
    }


    private JSONObject initJSON(float lat, float lng, String year, String month, String day, String hour,
                                String yearFC, String monthFC){

        JSONObject resJSON = new JSONObject();
        JSONOperateTool.putJSONNoResult(resJSON);

        resJSON.put(ResJsonConst.DATA, "");
        if (year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2){
            JSONOperateTool.putJSONParamError(resJSON);
        }
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180){
            JSONOperateTool.putJSONParamError(resJSON);
        }
        int yearnum = Integer.parseInt(year);
        int monthnum = Integer.parseInt(month);
        int daynum = Integer.parseInt(day);
        int hournum = Integer.parseInt(hour);

        int yearfcnum = Integer.parseInt(yearFC);
        int monthfcnum = Integer.parseInt(monthFC);
        if (yearnum < 1900 || yearnum > 2100 || monthnum < 1 || monthnum > 12 || daynum < 1 || daynum > 31
                || yearfcnum < 1900 || yearfcnum > 2100 || monthfcnum < 1 || monthfcnum > 12){
            JSONOperateTool.putJSONParamError(resJSON);
        }


        return resJSON;
    }
}
