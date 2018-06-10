package net.xinhong.meteoserve.service.service.nwpcapi.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.common.FCTimeTool;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.dao.nwpcapi.NWPCOceanAPIDao;
import net.xinhong.meteoserve.service.service.impl.StationDataSurfServiceImpl;
import net.xinhong.meteoserve.service.service.nwpcapi.NWPCOceanAPIService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by xiaoyu on 17/2/21.
 */
@Service
public class NWPCOceanAPIServiceImpl implements NWPCOceanAPIService {
    private static final Log logger = LogFactory.getLog(StationDataSurfServiceImpl.class);
    private static final float delta = 0.25f; //数据分辨率
    @Autowired
    private NWPCOceanAPIDao nwpcOceanAPIDao;

    //获取未来七天的数据
    @Override
    public JSONObject get7DayWindWaveDataFromPostion(String slat, String slng, String year, String month, String day){

        JSONObject days7ResJSON = new JSONObject();
        //1.传入参数正确性判断
        days7ResJSON.put(ResJsonConst.DATA, "");
        if (year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || slat == null || slat.isEmpty()
                || slng == null || slng.isEmpty()){
            JSONOperateTool.putJSONParamError(days7ResJSON);
            return days7ResJSON;
        }
        DateTime curDate = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day), 0, 0, 0);
        JSONArray dataArray = new JSONArray();
        int dayNum = 7;
        for (int i = 0; i < dayNum; i++){
            JSONObject dayResObj = this.getHoursWindWaveDataFromPostion(slat, slng,
                    curDate.toString("yyyy"), curDate.toString("MM"), curDate.toString("dd"), "00", false);

            days7ResJSON.put(ResJsonConst.STATUSCODE, ResStatus.NORESULT.getStatusCode());
            //如果第一天没取到数据，则认为获取数据失败！
            if (i == 0 && (dayResObj.getInteger(ResJsonConst.STATUSCODE) != ResStatus.SUCCESSFUL.getStatusCode())){
                return dayResObj;
            }
            JSONObject obj = new JSONObject();
            obj.put("date", curDate.toString("yyyyMMdd"));
            obj.put("wind_wave", dayResObj.get(ResJsonConst.DATA));
            dataArray.add(obj);
            curDate = curDate.plusDays(1);
        }
        JSONOperateTool.putJSONSuccessful(days7ResJSON, dataArray);
        return days7ResJSON;
    }


    @Override
    public JSONObject getHoursWindWaveDataFromPostion(String slat, String slng, String year, String month, String day, String hour,
                                                      boolean isShowDay) {

        JSONObject resJSON = new JSONObject();
        //1.传入参数正确性判断
        resJSON.put(ResJsonConst.DATA, "");
        if (year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2
                || slat == null || slat.isEmpty() || slng == null || slng.isEmpty()){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        } else{
            try{
                Integer mm = Integer.parseInt(month);
                Integer dd = Integer.parseInt(day);
                Integer hh = Integer.parseInt(hour);
                Integer yy = Integer.parseInt(year);
                if (mm < 1 || mm > 12 || dd < 1 || dd > 31 || hh < 0 || hh > 23 || yy < 1900 || yy > 2099){
                    JSONOperateTool.putJSONParamError(resJSON);
                    return resJSON;
                }
                else {
                    JSONOperateTool.putJSONNoResult(resJSON);
                }
            }catch (NumberFormatException ex){
                JSONOperateTool.putJSONParamError(resJSON);
                return resJSON;
            }
        }

        Float lat;
        Float lng;
        try{
            lat = Float.parseFloat(slat);
            lng = Float.parseFloat(slng);
        }catch (NumberFormatException ex){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }
        if (lat > 90 || lat < -90 || lng > 180 || lng < -180){ //经纬度限定在-90~90, -180~180之间
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }
        //从DAO层获取数据
        //2.根据时间计算起报时间及预报时效
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHH");
        DateTime useDate = DateTime.parse(year + month + day + hour, dateformat); //传入为北京时

        ResultTime restime = getResultTime(useDate);
        if (restime == null) {
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        DateTime resStartDate = restime.resStartDate;
        String VTI = restime.resVTI;
        Integer VTIInt = Integer.parseInt(VTI);

        //3.进行插值计算逐MaxFCHour小时结果
            //3.1为了进行逐个小时插值计算，这里需要获取多个预报时效的数据
        ArrayList<Integer> vtiList = new ArrayList<>(10);
        final int MaxFCHour = 24; //返回时最长插值预报时效
        final int MaxVTI = 7*24;
        final int Interval6VTI = 72;

        Integer startVTI = VTIInt;
        int interval = 3;
        if (VTIInt > Interval6VTI)//目前VTI:72小时以内为3小时间隔,以上为6小时，最长7天
            interval = 6;

        if (VTIInt >= 3 && resStartDate.plusHours(VTIInt).isAfterNow()){
            //首先获取起始的预报时效
            startVTI = VTIInt - interval;
        }
        vtiList.add(startVTI);
        Integer tmpVTI = startVTI + interval;
        long useHourNum = new Duration(resStartDate, useDate).getStandardHours();
        while ((tmpVTI <= MaxVTI) && ((tmpVTI - startVTI) <= (MaxFCHour + useHourNum))){
            vtiList.add(tmpVTI);
            if (tmpVTI >= Interval6VTI)
                interval = 6;
            tmpVTI += interval;
        }
        if (vtiList.get(vtiList.size()-1) < MaxVTI &&
                (vtiList.get(vtiList.size()-1) - startVTI - useHourNum) < MaxFCHour){
            vtiList.add(vtiList.get(vtiList.size()-1) + interval);
        }
//        logger.debug("vtiList = " + vtiList.toString() + ",resStartDate" + resStartDate.toString() + "," + useDate.toString());
//todo:要素需要改为枚举！
        String ryear  = resStartDate.toString("yyyy");
        String rmonth = resStartDate.toString("MM");
        String rday   = resStartDate.toString("dd");
        String rhour  = resStartDate.toString("HH");

        String strLat = String.format("%.2f", Math.round(lat / delta) * delta);
        String strLng = String.format("%.2f", Math.round(lng / delta) * delta);

        //注意：这里取出的预报时效如果与传入的不一致，则说明取的是上一起报时间的信息，需要修改vtiList（例如：vtiList中69后面为72，但如果取前一预报时效，则69变成了69+12，后面为75+12了）
        JSONObject startVTIDaoJSON = nwpcOceanAPIDao.getWindWaveDataFromPostion(strLat, strLng, ryear, rmonth, rday, rhour, startVTI.toString());
        Integer startDaoVTI = startVTIDaoJSON.getInteger("VTI");
        if (startDaoVTI != null){
            int VTIsub = startDaoVTI - startVTI;
            if (VTIsub > 0){
                for (int i = 0; i < vtiList.size(); i++){
                    vtiList.set(i, vtiList.get(i) + VTIsub);
                }
                //去除超过72小时间隔为3的预报时效
                for (int i = vtiList.size()-1; i >= 0; i--){
                    if ((vtiList.get(i)>MaxVTI) ||
                        (vtiList.get(i)>Interval6VTI && vtiList.get(i)%3==0 && vtiList.get(i)%6!=0 )){
                        vtiList.remove(vtiList.get(i));
                    }
                }
                resStartDate = resStartDate.minusHours(VTIsub);
                ryear  = resStartDate.toString("yyyy");
                rmonth = resStartDate.toString("MM");
                rday   = resStartDate.toString("dd");
                rhour  = resStartDate.toString("HH");
            }
        }
        Map<Integer, JSONObject> vtiFCDataMap = new HashMap<>(vtiList.size());
        JSONObject validVtiDaoJSON = null;
        if (!this.isInvalidDaoJSON(startVTIDaoJSON)) {
            vtiFCDataMap.put(vtiList.get(0), startVTIDaoJSON);
            validVtiDaoJSON = startVTIDaoJSON;
        }

        //3.2获取各个预报时效的预报数据
        if (vtiList.size() == 1){ //处理只有一个预报时效
            if (this.isInvalidDaoJSON(startVTIDaoJSON)) {
                JSONOperateTool.putJSONNoResult(resJSON);
                return resJSON;
            }
         //   resJSON.put(ResJsonConst.TIME, daoJSON.get(ResJsonConst.TIME));
            resJSON.put(ResJsonConst.TIME, useDate.toString("yyyyMMddHH"));
            JSONOperateTool.putJSONSuccessful(resJSON, startVTIDaoJSON.get(ResJsonConst.DATA));
            return resJSON;
            //......
        } else  {
           // String fcTime = "";  //todo:测试起报时间，需删除
            for (int i = 1; i < vtiList.size(); i++){
                JSONObject vtiDaoJSON = nwpcOceanAPIDao.getWindWaveDataFromPostion(strLat, strLng, ryear, rmonth, rday, rhour, vtiList.get(i).toString());
                if (!this.isInvalidDaoJSON(vtiDaoJSON)){
                    vtiFCDataMap.put(vtiList.get(i), vtiDaoJSON);
                    validVtiDaoJSON = vtiDaoJSON;

                //    fcTime = vtiDaoJSON.getString(ResJsonConst.TIME);
                }
            }
            if (vtiFCDataMap.isEmpty()){
                JSONOperateTool.putJSONNoResult(resJSON);
                return resJSON;
            } else if (validVtiDaoJSON != null && vtiFCDataMap.size() == 1){
                //处理只有一个合法预报时效数据
             //   resJSON.put(ResJsonConst.TIME, validVtiDaoJSON.get(ResJsonConst.TIME));
                resJSON.put(ResJsonConst.TIME, useDate.toString("yyyyMMddHH"));
                JSONOperateTool.putJSONSuccessful(resJSON, validVtiDaoJSON.get(ResJsonConst.DATA));
                return resJSON;
            }

            Map<Integer, ResWindWaveData> resFCWindWaveDataMap = new HashMap<>(vtiList.size());
            for (Map.Entry<Integer, JSONObject> entry : vtiFCDataMap.entrySet()){

                ResWindWaveData data = new ResWindWaveData();
                JSONObject dataObj = entry.getValue().getJSONObject(ResJsonConst.DATA);
                Float sWS = dataObj.getFloat("WS");
                if (sWS != null){
                  //  try{
                        data.ws = Math.round(sWS*100.0f)/100.0f;
                  //  }
                  //  catch(NumberFormatException ex){
                  //      logger.error("ws数据转换错误!" + sWS);
                  //      data.ws = null;
                  //  }
                }

                Float sWD = dataObj.getFloat("WD");
                if (sWD != null){
                 //   try{
                        data.wd = Math.round(sWD*100.0f)/100.0f;
                 //   }
                 //   catch(NumberFormatException ex){
                 //       logger.error("wd数据转换错误!" + sWD);
                 //       data.wd = null;
                 //   }
                }

                Float sWAH = dataObj.getFloat("WAH");
                if (sWAH != null){
                    if (sWAH < 10e-10)
                        data.wah = 0.0f;
                    else
                        data.wah = Math.round(sWAH*100.0f)/100.0f;
                }
                resFCWindWaveDataMap.put(entry.getKey(), data);
            }

            //3.3插值到逐小时
            Map<String, JSONObject> resDataJSONMap = new LinkedHashMap<>(MaxFCHour);
            for (int i = 0; i < MaxFCHour; i++){
                DateTime curDate;
                if (i == 0)
                    curDate = useDate;
                else
                    curDate = useDate.plusHours(i);
                long curHourNum = new Duration(resStartDate, curDate).getStandardHours();
                //找到距离最近的两个预报时效，进行线性插值
                Integer[] tmpVTIs = this.nearestVTIs(vtiList, curHourNum);
                java.lang.String tmpKey;
                if (isShowDay)
                    tmpKey = curDate.toString("MMddHH");
                else
                    tmpKey = curDate.toString("HH");
                if (tmpVTIs == null) {
                 //   resDataJSONMap.put(tmpKey, null);
                    continue;
                }
                if (tmpVTIs.length == 1){
                    if (vtiFCDataMap.get(tmpVTIs[0]) != null){
                        JSONObject obj = new JSONObject();
                        obj.put("WS", resFCWindWaveDataMap.get(tmpVTIs[0]).ws);
                        obj.put("WD", resFCWindWaveDataMap.get(tmpVTIs[0]).wd);
                        obj.put("WAH", resFCWindWaveDataMap.get(tmpVTIs[0]).wah);
                        resDataJSONMap.put(tmpKey, obj);
                     //   resDataJSONMap.put(tmpKey, vtiFCDataMap.get(tmpVTIs[0]).getJSONObject(ResJsonConst.DATA));
                    }

                    continue;
                }

                if (resFCWindWaveDataMap.get(tmpVTIs[0]) == null || resFCWindWaveDataMap.get(tmpVTIs[1]) == null){
                    logger.error("dao层操作缺少VTI=" + tmpVTIs.toString() + "数据！");
                    continue;
                }

                JSONObject json = new JSONObject();

                Float ws0 = resFCWindWaveDataMap.get(tmpVTIs[0]).ws;
                Float ws1 = resFCWindWaveDataMap.get(tmpVTIs[1]).ws;
                if (ws0 != null && ws1 != null){
                    float tmp = ws0 + ((ws1 - ws0)*(curHourNum - tmpVTIs[0])*1.0f/(tmpVTIs[1] - tmpVTIs[0]));
                    json.put("WS", Math.round(tmp*100.0f)/100.0f);
                }

                Float wd0 = resFCWindWaveDataMap.get(tmpVTIs[0]).wd;
                Float wd1 = resFCWindWaveDataMap.get(tmpVTIs[1]).wd;
                if (wd0 != null && wd1 != null){
                    float tmp = wd0 + ((wd1 - wd0)*(curHourNum - tmpVTIs[0])*1.0f/(tmpVTIs[1] - tmpVTIs[0]));
                    json.put("WD", Math.round(tmp*100.0f)/100.0f);
                }

                Float wah0 = resFCWindWaveDataMap.get(tmpVTIs[0]).wah;
                Float wah1 = resFCWindWaveDataMap.get(tmpVTIs[1]).wah;

                if (wah0 != null && wah1 != null){
                    float tmp = wah0 + ((wah1 - wah0)*(curHourNum - tmpVTIs[0])*1.0f/(tmpVTIs[1] - tmpVTIs[0]));
                    json.put("WAH", Math.round(tmp*100.0f)/100.0f);
                }

                resDataJSONMap.put(tmpKey, json);

            }

            //4.根据获取结果拼接结果JSON中数据段
            if (resDataJSONMap.isEmpty()){
                JSONOperateTool.putJSONNoResult(resJSON);
                return resJSON;
            }

            resJSON.put(ResJsonConst.TIME, useDate.toString("yyyyMMddHH"));
         //   resJSON.put("fctime", fcTime);
            JSONOperateTool.putJSONSuccessful(resJSON, resDataJSONMap);
            return resJSON;
        }

    }

    private boolean isInvalidDaoJSON(JSONObject daoJSON){
        return (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL"));
    }

    //找到距离给定时次最近的两个预报时效,如果返回一个预报时效，则表示相等
    private Integer[] nearestVTIs(ArrayList<Integer> vtiList, long curHourNum){
        if (vtiList == null || vtiList.size() < 2 ||
                curHourNum < vtiList.get(0) || curHourNum > vtiList.get(vtiList.size()-1))
            return null;

        for (int i = 0; i < vtiList.size()-1; i++){
            if (curHourNum > vtiList.get(i) && curHourNum < vtiList.get(i+1)){
                return new Integer[]{vtiList.get(i), vtiList.get(i+1)};
            } else if (curHourNum == vtiList.get(i)){
                return new Integer[]{vtiList.get(i)};
            } else if (curHourNum == vtiList.get(vtiList.size()-1)){
                return new Integer[]{vtiList.get(vtiList.size()-1)};
            }
        }
        return null;
    }



    private static class ResultTime {
        private DateTime resStartDate;
        private String resVTI;
    }


    private static class ResWindWaveData{
        Float ws;
        Float wd;
        Float wah;
    }


    /**
     * 根据传入时间,计算海钓数据起报时间及预报时效
     *
     * @param useDate
     * @return
     */
    private ResultTime getResultTime(DateTime useDate) {

        DateTime resDate;
        int afterMinutes = 10 * 60; //向后推10小时,如08起报下午6点后使用
        //注意:如果传入的是以前的时间,则用该时间计算起报时间,如果是当前或以后的时间,用当前时间计算起报时间!
        if (useDate.plusMinutes(afterMinutes).isBeforeNow()) {
            resDate = FCTimeTool.getFCStartTime(useDate, 0);
        } else {
            resDate = FCTimeTool.getFCStartTime(DateTime.now(), afterMinutes);
        }
        //计算VTI
        long numVTI = (new Duration(resDate, useDate)).getStandardHours();
        //目前VTI:72小时以内为3小时间隔,以上为6小时，最长7天
        if (numVTI > 168) {
            return null;
        }

        //找到最靠近3小时或6小时间隔的预报时效
        if (numVTI <=72){
            numVTI = (long)(numVTI * 1.0f / 3f) * 3; //取小的预报时效
          //  if (tmpnumVTI > numVTI)

        }

        else
            numVTI = (long)(numVTI * 1.0f / 6f) * 6;

        String VTI = StringUtils.leftPad(Integer.toString((int) numVTI), 3, "0");
        ResultTime restime = new ResultTime();
        restime.resStartDate = resDate;
        restime.resVTI = VTI;
        return restime;
    }
}
