package net.xinhong.meteoserve.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ElemCityFC;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.service.common.FCTimeTool;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.dao.StationDataCityFCDao;
import net.xinhong.meteoserve.service.dao.StationDataClimateDao;
import net.xinhong.meteoserve.service.service.StationDataClimateService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaoyu on 16/7/19.
 */
@Service
public class StationDataClimateServiceImpl implements StationDataClimateService {
    private static final Log logger = LogFactory.getLog(StationDataSurfServiceImpl.class);

    @Autowired
    private StationDataClimateDao stationDataClimateDao;


    @Override
    public JSONObject getForYearsDataFromCode(String strCode, String year, String month, String day, String hour, String minute) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = this.initJSON(year, month, day, hour);
        if (resJSON.get(ResJsonConst.STATUSCODE).equals(ResStatus.PARAM_ERROR.getStatusCode())){
            return resJSON;
        }
        if (strCode == null || strCode.isEmpty()){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        //1.然后获取数据
        JSONObject daoJSON = stationDataClimateDao.getStationForYearsData(strCode, year, month, day, hour, "00");

        //2.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")){
            logger.info("给定的站号" + strCode + "及时间要素没有查询到统计产品数据!");
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }
    private JSONObject initJSON(String year, String month, String day, String hour){
        JSONObject resJSON = new JSONObject();

        resJSON.put(ResJsonConst.DATA, "");
        if (year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2){
            JSONOperateTool.putJSONParamError(resJSON);
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
        }
        return resJSON;
    }
}
