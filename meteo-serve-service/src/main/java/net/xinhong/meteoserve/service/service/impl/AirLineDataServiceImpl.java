package net.xinhong.meteoserve.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.dao.AirLineDataDao;
import net.xinhong.meteoserve.service.service.AirLineDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xiaoyu   on 16/4/19.
 */
@Service
public class AirLineDataServiceImpl implements AirLineDataService {

    @Autowired
    private AirLineDataDao airLineDataDao;


    @Override
    public JSONObject getFlightFromNumber(String flightNumber, String year, String month, String day) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = this.initJSON(year, month, day);
        if (resJSON.get(ResJsonConst.STATUSCODE).equals(ResStatus.PARAM_ERROR.getStatusCode())){
            return resJSON;
        }

        //2.然后获取数据
        JSONObject daoJSON = airLineDataDao.getFlightFromNumber(flightNumber,year,month,day);
        resJSON.put(ResJsonConst.TIME, daoJSON.get(ResJsonConst.TIME));

        //3.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")){
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    @Override
    public JSONObject getFlightFromDeptArr(String deptAptCode, String arrAptCode, String year, String month, String day) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = this.initJSON(year, month, day);
        if (resJSON.get(ResJsonConst.STATUSCODE).equals(ResStatus.PARAM_ERROR.getStatusCode())){
            return resJSON;
        }

        //2.然后获取数据
        JSONObject daoJSON = airLineDataDao.getFlightFromDeptArr(deptAptCode,arrAptCode,year,month,day);
        resJSON.put(ResJsonConst.TIME, daoJSON.get(ResJsonConst.TIME));

        //3.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")){
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    @Override
    public JSONObject getAirLineNameListFromDeptArr(String deptAptCode, String arrAptCode) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();
        resJSON.put(ResJsonConst.DATA, "");
        if (deptAptCode == null || deptAptCode.isEmpty() || arrAptCode == null || arrAptCode.isEmpty()){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        //2.然后获取数据
        JSONObject daoJSON = airLineDataDao.getAirLineNameListFromDeptArr(deptAptCode,arrAptCode);

        //3.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")){
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    @Override
    public JSONObject getAirLineFromName(String airLineName) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();
        resJSON.put(ResJsonConst.DATA, "");
        if (airLineName == null || airLineName.isEmpty()){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        //2.然后获取数据
        JSONObject daoJSON = airLineDataDao.getAirLineFromName(airLineName);

        //3.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")){
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));

        return resJSON;
    }



    private JSONObject initJSON(String year, String month, String day){
        JSONObject resJSON = new JSONObject();
        resJSON.put(ResJsonConst.DATA, "");
        if (year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 ){
            JSONOperateTool.putJSONParamError(resJSON);
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
        }
        return resJSON;
    }
}
