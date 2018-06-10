package net.xinhong.meteoserve.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.dao.TyphVolcaDataDao;
import net.xinhong.meteoserve.service.service.TyphVolcaService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xiaoyu on 16/7/1.
 */
@Service
public class TyphVolcaServiceImpl implements TyphVolcaService{
    private static final Log logger = LogFactory.getLog(StationDataSurfServiceImpl.class);


    @Autowired
    private TyphVolcaDataDao typhVolcaDataDao;


    @Override
    public JSONObject getTyphData(String year, String month, String day, String hour, String minute, String sDayNum, boolean isShowFinish) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();

        resJSON.put(ResJsonConst.DATA, "");

        if (year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2
                || minute == null || minute.length() != 2) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
        }

        //默认获取最近10天的台风
        int days = 10;
        if (sDayNum != null && !sDayNum.isEmpty()){
            try{
                days = Integer.parseInt(sDayNum);
                if (days <= 0)
                    days = 1;
                if (days > 365)
                    days = 365;
            }
            catch (NumberFormatException ex){

            }

        }
        JSONObject daoJSON = typhVolcaDataDao.getTyphData(year, month, day, hour, minute, days*24*60, isShowFinish);
        resJSON.put(ResJsonConst.TIME, daoJSON.get(ResJsonConst.TIME));

        //3.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")) {
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    @Override
    public JSONObject getTyphLastTimeData(String year, String month, String day, String hour, String minute, String dayNum){
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();

        resJSON.put(ResJsonConst.DATA, "");

        if (year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2
                || minute == null || minute.length() != 2) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
        }

        //默认获取最近12小时的台风
        int hourNums = 12;
        if (dayNum != null && !dayNum.isEmpty()){
            try{
                Integer days = Integer.parseInt(dayNum);
                if (days <= 0)
                    days = 1;
                if (days > 365)
                    days = 365;
                hourNums = days*24;
            }
            catch (NumberFormatException ex){

            }

        }

        JSONObject daoJSON = typhVolcaDataDao.getTyphLastTimeData(year, month, day, hour, minute, hourNums*60);
        resJSON.put(ResJsonConst.TIME, daoJSON.get(ResJsonConst.TIME));

        //3.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")) {
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }

    @Override
    public JSONObject getVolcaData(String year, String month, String day, String hour, String minute) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();

        resJSON.put(ResJsonConst.DATA, "");
        if (year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2
                || minute == null || minute.length() != 2) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
        }

        //默认获取最近6H的火山灰
        JSONObject daoJSON = typhVolcaDataDao.getVolcaData(year, month, day, hour, minute, 6*60);
        resJSON.put(ResJsonConst.TIME, daoJSON.get(ResJsonConst.TIME));

        //3.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")) {
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }
}
