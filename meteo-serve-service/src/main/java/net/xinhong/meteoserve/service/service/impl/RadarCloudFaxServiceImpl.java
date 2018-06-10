package net.xinhong.meteoserve.service.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xinhong.mids3d.util.math.MIDS3DMath;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.dao.RadarCloudFaxPMImageDao;
import net.xinhong.meteoserve.service.domain.StationRadarInfoBean;
import net.xinhong.meteoserve.service.service.RadarCloudFaxService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by xiaoyu on 16/8/8.
 */
@Service
public class RadarCloudFaxServiceImpl implements RadarCloudFaxService {
    private static final Log logger = LogFactory.getLog(RadarCloudFaxServiceImpl.class);
    @Autowired
    private RadarCloudFaxPMImageDao radarCloudFaxDDao;
    @Override
    public JSONObject getRadarInfo(String radarType, String year, String month, String day, String hour, String minute) {
        if (radarType == null || radarType.isEmpty()){
            radarType = "radar";
        }
        return this.getInfo(radarType, year, month, day, hour, minute, null);
    }

    @Override
    public JSONObject getCloudInfo(String cloudType,  String year, String month, String day, String hour, String minute) {
        if (cloudType == null || cloudType.isEmpty()){
            cloudType = "cloud";
        }


        return this.getInfo(cloudType, year, month, day, hour, minute, null);
    }

    @Override
    public JSONObject getStationRadarInfo(String radarType, String year, String month, String day, String hour, String minute,
                                          String radarIDs) {
        if (radarType == null || radarType.isEmpty()){
            radarType = "stationradar";
        }

        return this.getInfo(radarType, year, month, day, hour, minute, radarIDs);
    }

    @Override
    public JSONObject getJPPM2Dot5FCInfo(String pm25Type, String year, String month, String day, String hour, String minute) {
        if (pm25Type == null || pm25Type.isEmpty()){
            pm25Type = "jppm2dot5fc";
        }

        return this.getInfo(pm25Type, year, month, day, hour, minute, null);

    }

    private static ArrayList<String> wxfax08ENameList = new ArrayList<String>(10); //08时起报
    private static ArrayList<String> wxfax08CNameList = new ArrayList<String>(10);

    private static ArrayList<String> wxfax20ENameList = new ArrayList<String>(10);  //20时起报
    private static ArrayList<String> wxfax20CNameList = new ArrayList<String>(10);

    private static ArrayList<String> wxfax20AllENameList = new ArrayList<String>(10); //20时起报,全天都需要查询的
    private static ArrayList<String> wxfax20AllCNameList = new ArrayList<String>(10);

    private static ArrayList<String> ecmwfENameList = new ArrayList<String>(20);
    private static ArrayList<String> ecmwfCNameList = new ArrayList<String>(20);

    private static ArrayList<String> ecmwfIndexNameList = new ArrayList<>(10);
    private static ArrayList<String> ecmwfIndexCNameList = new ArrayList<>(10);
    private static Map<String, JSONObject> ecmwfresMap = new HashMap<>(10);

    static {
        //日本传真图图片
        wxfax08ENameList.add("fsas_00");
        wxfax08CNameList.add("08时地面分析");

        wxfax08ENameList.add("axfe578_00");
        wxfax08CNameList.add("08时高空分析");

        wxfax08ENameList.add("fxfe502_00");
        wxfax08CNameList.add("08时500hPa24小时预报");

        wxfax08ENameList.add("fxfe504_00");
        wxfax08CNameList.add("08时500hPa48小时预报");

        wxfax08ENameList.add("fxfe507_00");
        wxfax08CNameList.add("08时500hPa72小时预报");

        wxfax08ENameList.add("fxfe5782_00");
        wxfax08CNameList.add("08时高空24小时预报");

        wxfax08ENameList.add("fxfe5784_00");
        wxfax08CNameList.add("08时高空48小时预报");

        wxfax08ENameList.add("fxfe577_00");
        wxfax08CNameList.add("08时高空72小时预报");


        wxfax20ENameList.add("fsas_12");
        wxfax20CNameList.add("20时地面分析");

        wxfax20ENameList.add("axfe578_12");
        wxfax20CNameList.add("20时高空分析");

        wxfax20ENameList.add("fxfe502_12");
        wxfax20CNameList.add("20时500hPa24小时预报");

        wxfax20ENameList.add("fxfe504_12");
        wxfax20CNameList.add("20时500hPa48小时预报");

        wxfax20ENameList.add("fxfe507_12");
        wxfax20CNameList.add("20时500hPa72小时预报");

        wxfax20ENameList.add("fxfe5782_12");
        wxfax20CNameList.add("20时高空24小时预报");

        wxfax20ENameList.add("fxfe5784_12");
        wxfax20CNameList.add("20时高空48小时预报");

        wxfax20ENameList.add("fxfe577_12");
        wxfax20CNameList.add("20时高空72小时预报");


        wxfax20AllENameList.add("feas50_12");
        wxfax20AllCNameList.add("20时500hPa_地面分析");

        wxfax20AllENameList.add("feas502_12");
        wxfax20AllCNameList.add("20时500_地面24小时预报");

        wxfax20AllENameList.add("feas504_12");
        wxfax20AllCNameList.add("20时500_地面48小时预报");

        wxfax20AllENameList.add("feas507_12");
        wxfax20AllCNameList.add("20时500_地面72小时预报");

        wxfax20AllENameList.add("feas509_12");
        wxfax20AllCNameList.add("20时500_地面96小时预报");

        wxfax20AllENameList.add("feas512_12");
        wxfax20AllCNameList.add("20时500_地面120小时预报");

        wxfax20AllENameList.add("feas514_12");
        wxfax20AllCNameList.add("20时500_地面144小时预报");

        wxfax20AllENameList.add("feas516_12");
        wxfax20AllCNameList.add("20时500_地面168小时预报");

        wxfax20AllENameList.add("feas519_12");
        wxfax20AllCNameList.add("20时500_地面192小时预报");

        //欧洲预报图片
        JSONObject json = new JSONObject();
        json.put("z500a_fe_1(分析场)", "");
        json.put("z500a_fe_2(24小时预报)", "");
        json.put("z500a_fe_3(48小时预报)", "");
        json.put("z500a_fe_4(72小时预报)", "");
        json.put("z500a_fe_5(96小时预报)", "");
        json.put("z500a_fe_6(120小时预报)", "");
        json.put("z500a_fe_7(144小时预报)", "");
        json.put("z500a_fe_8(168小时预报)", "");
        ecmwfresMap.put("500hPa位势高度+距平", json);

        json = new JSONObject();
        json.put("z500_mslp_fe_1(分析场)", "");
        json.put("z500_mslp_fe_2(24小时预报)", "");
        json.put("z500_mslp_fe_3(48小时预报)", "");
        json.put("z500_mslp_fe_4(72小时预报)", "");
        json.put("z500_mslp_fe_5(96小时预报)", "");
        json.put("z500_mslp_fe_6(120小时预报)", "");
        json.put("z500_mslp_fe_7(144小时预报)", "");
        json.put("z500_mslp_fe_8(168小时预报)", "");
        ecmwfresMap.put("500hPa位势高度+海平面气压", json);

        json = new JSONObject();
        json.put("T850_fe_1(分析场)", "");
        json.put("T850_fe_2(24小时预报)", "");
        json.put("T850_fe_3(48小时预报)", "");
        json.put("T850_fe_4(72小时预报)", "");
        json.put("T850_fe_5(96小时预报)", "");
        json.put("T850_fe_6(120小时预报)", "");
        json.put("T850_fe_7(144小时预报)", "");
        json.put("T850_fe_8(168小时预报)", "");
        ecmwfresMap.put("850hPa气温+海平面气压中心", json);

        json = new JSONObject();
        json.put("T850a_fe_1(分析场)", "");
        json.put("T850a_fe_2(24小时预报)", "");
        json.put("T850a_fe_3(48小时预报)", "");
        json.put("T850a_fe_4(72小时预报)", "");
        json.put("T850a_fe_5(96小时预报)", "");
        json.put("T850a_fe_6(120小时预报)", "");
        json.put("T850a_fe_7(144小时预报)", "");
        json.put("T850a_fe_8(168小时预报)", "");
        ecmwfresMap.put("850hPa气温+距平分析场", json);

        json = new JSONObject();
        json.put("mslp_uv850_fe_1(分析场)", "");
        json.put("mslp_uv850_fe_2(24小时预报)", "");
        json.put("mslp_uv850_fe_3(48小时预报)", "");
        json.put("mslp_uv850_fe_4(72小时预报)", "");
        json.put("mslp_uv850_fe_5(96小时预报)", "");
        json.put("mslp_uv850_fe_6(120小时预报)", "");
        json.put("mslp_uv850_fe_7(144小时预报)", "");
        json.put("mslp_uv850_fe_8(168小时预报)", "");
        ecmwfresMap.put("850hPa风速+流线+海平面气压中心", json);

        json = new JSONObject();
        json.put("mslpaNormMean_fe_1(00-120小时)", "");
        json.put("mslpaNormMean_fe_2(24-144小时)", "");
        json.put("mslpaNormMean_fe_3(48-168小时)", "");
        json.put("mslpaNormMean_fe_4(72-196小时)", "");
        json.put("mslpaNormMean_fe_5(96-216小时预报)", "");
        json.put("mslpaNormMean_fe_6(120-240小时预报)", "");
        ecmwfresMap.put("平均海平面气压+距平", json);

//        ecmwfENameList.add("z500a_fe_1");
//        ecmwfCNameList.add("500hPa位势高度+距平分析场");//时效：00、24、48、72、96、120、144、168、192、216、240（共11张图）
//        ecmwfENameList.add("z500a_fe_2");
//        ecmwfCNameList.add("500hPa位势高度+距平24小时预报");
//        ecmwfENameList.add("z500a_fe_3");
//        ecmwfCNameList.add("500hPa位势高度+距平48小时预报");
//        ecmwfENameList.add("z500a_fe_4");
//        ecmwfCNameList.add("500hPa位势高度+距平72小时预报");
//        ecmwfENameList.add("z500a_fe_5");
//        ecmwfCNameList.add("500hPa位势高度+距平96小时预报");
//        ecmwfENameList.add("z500a_fe_6");
//        ecmwfCNameList.add("500hPa位势高度+距平120小时预报");
//        ecmwfENameList.add("z500a_fe_7");
//        ecmwfCNameList.add("500hPa位势高度+距平144小时预报");
//        ecmwfENameList.add("z500a_fe_8");
//        ecmwfCNameList.add("500hPa位势高度+距平168小时预报");
//
//
//        ecmwfENameList.add("z500_mslp_fe_1");
//        ecmwfCNameList.add("500hPa位势高度+海平面气压分析场");//时效：00、24、48、72、96、120、144、168、192、216、240（共11张图）
//        ecmwfENameList.add("z500_mslp_fe_2");
//        ecmwfCNameList.add("500hPa位势高度+海平面气压24小时预报");
//        ecmwfENameList.add("z500_mslp_fe_3");
//        ecmwfCNameList.add("500hPa位势高度+海平面气压48小时预报");
//        ecmwfENameList.add("z500_mslp_fe_4");
//        ecmwfCNameList.add("500hPa位势高度+海平面气压72小时预报");
//        ecmwfENameList.add("z500_mslp_fe_5");
//        ecmwfCNameList.add("500hPa位势高度+海平面气压96小时预报");
//        ecmwfENameList.add("z500_mslp_fe_6");
//        ecmwfCNameList.add("500hPa位势高度+海平面气压120小时预报");
//        ecmwfENameList.add("z500_mslp_fe_7");
//        ecmwfCNameList.add("500hPa位势高度+海平面气压144小时预报");
//        ecmwfENameList.add("z500_mslp_fe_8");
//        ecmwfCNameList.add("500hPa位势高度+海平面气压168小时预报");
//
//        ecmwfENameList.add("T850_fe_1");
//        ecmwfCNameList.add("850hPa气温+海平面高(低)压中心分析场");//时效：00、24、48、72、96、120、144、168、192、216、240（共11张图）
//        ecmwfENameList.add("T850_fe_2");
//        ecmwfCNameList.add("850hPa气温+海平面高(低)压中心24小时预报");
//        ecmwfENameList.add("T850_fe_3");
//        ecmwfCNameList.add("850hPa气温+海平面高(低)压中心48小时预报");
//        ecmwfENameList.add("T850_fe_4");
//        ecmwfCNameList.add("850hPa气温+海平面高(低)压中心72小时预报");
//        ecmwfENameList.add("T850_fe_5");
//        ecmwfCNameList.add("850hPa气温+海平面高(低)压中心96小时预报");
//        ecmwfENameList.add("T850_fe_6");
//        ecmwfCNameList.add("850hPa气温+海平面高(低)压中心120小时预报");
//        ecmwfENameList.add("T850_fe_7");
//        ecmwfCNameList.add("850hPa气温+海平面高(低)压中心144小时预报");
//        ecmwfENameList.add("T850_fe_8");
//        ecmwfCNameList.add("850hPa气温+海平面高(低)压中心168小时预报");
//
//        ecmwfIndexCNameList.add("850hPa气温+距平分析场");
//        ecmwfIndexNameList.add("T850a_fe");
//        ecmwfENameList.add("T850a_fe_1");
//        ecmwfCNameList.add("850hPa气温+距平分析场"); //时效：00、24、48、72、96、120、144、168、192、216、240（共11张图）
//        ecmwfENameList.add("T850a_fe_2");
//        ecmwfCNameList.add("850hPa气温+距平24小时预报");
//        ecmwfENameList.add("T850a_fe_3");
//        ecmwfCNameList.add("850hPa气温+距平48小时预报");
//        ecmwfENameList.add("T850a_fe_4");
//        ecmwfCNameList.add("850hPa气温+距平72小时预报");
//        ecmwfENameList.add("T850a_fe_5");
//        ecmwfCNameList.add("850hPa气温+距平96小时预报");
//        ecmwfENameList.add("T850a_fe_6");
//        ecmwfCNameList.add("850hPa气温+距平120小时预报");
//        ecmwfENameList.add("T850a_fe_7");
//        ecmwfCNameList.add("850hPa气温+距平144小时预报");
//        ecmwfENameList.add("T850a_fe_8");
//        ecmwfCNameList.add("850hPa气温+距平168小时预报");
//
//
//        ecmwfIndexCNameList.add("850hPa风速+流线+海平面高(低)中心分析场");
//        ecmwfIndexNameList.add("mslp_uv850_fe");
//        ecmwfENameList.add("mslp_uv850_fe_1");
//        ecmwfCNameList.add("850hPa风速+流线+海平面高(低)中心分析场");//时效：00、24、48、72、96、120、144、168、192、216、240（共11张图）
//        ecmwfENameList.add("mslp_uv850_fe_2");
//        ecmwfCNameList.add("850hPa风速+流线+海平面高(低)中心24小时预报");
//        ecmwfENameList.add("mslp_uv850_fe_3");
//        ecmwfCNameList.add("850hPa风速+流线+海平面高(低)中心48小时预报");
//        ecmwfENameList.add("mslp_uv850_fe_4");
//        ecmwfCNameList.add("850hPa风速+流线+海平面高(低)中心72小时预报");
//        ecmwfENameList.add("mslp_uv850_fe_5");
//        ecmwfCNameList.add("850hPa风速+流线+海平面高(低)中心96小时预报");
//        ecmwfENameList.add("mslp_uv850_fe_6");
//        ecmwfCNameList.add("850hPa风速+流线+海平面高(低)中心120小时预报");
//        ecmwfENameList.add("mslp_uv850_fe_7");
//        ecmwfCNameList.add("850hPa风速+流线+海平面高(低)中心144小时预报");
//        ecmwfENameList.add("mslp_uv850_fe_8");
//        ecmwfCNameList.add("850hPa风速+流线+海平面高(低)中心168小时预报");
//
//        ecmwfIndexCNameList.add("平均海平面气压+距平");
//        ecmwfIndexNameList.add("mslpaNormMean_fe");
//        ecmwfENameList.add("mslpaNormMean_fe_1");
//        ecmwfCNameList.add("平均海平面气压+距平00-120小时");
//        ecmwfENameList.add("mslpaNormMean_fe_2");
//        ecmwfCNameList.add("平均海平面气压+距平24-144小时");
//        ecmwfENameList.add("mslpaNormMean_fe_3");
//        ecmwfCNameList.add("平均海平面气压+距平48-168小时");
//        ecmwfENameList.add("mslpaNormMean_fe_4");
//        ecmwfCNameList.add("平均海平面气压+距平72-192小时");
//        ecmwfENameList.add("mslpaNormMean_fe_5");
//        ecmwfCNameList.add("平均海平面气压+距平96-216小时");
//        ecmwfENameList.add("mslpaNormMean_fe_6");
//        ecmwfCNameList.add("平均海平面气压+距平120-240小时");
//        //共6张图：Days1-5（ 00-120小时平均海平面气压+气压距平）
//        //Days2-6（ 24-144小时平均海平面气压+气压距平）
//        //Days3-7（ 48-168小时平均海平面气压+气压距平）
//        //Days4-8（ 72-192小时平均海平面气压+气压距平）
//        // Days5-8（ 96-216小时平均海平面气压+气压距平）
//        // Days6-10（120-240小时平均海平面气压+气压距平）
    }

    @Override
    public JSONObject getFaxInfo(String year, String month, String day, String hour, String minute) {
        JSONObject resJSON = new JSONObject();

        resJSON.put(ResJsonConst.DATA, "");

        //1.根据给定的时间调整查询的时间
        DateTime date = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                Integer.parseInt(hour), Integer.parseInt(minute), 0);

        if (date.getHourOfDay() >= 0 && date.getHourOfDay() < 7){ //0-7点前一天08时
            date = date.minusDays(1);
            hour = "08";
        }
        if (date.getHourOfDay() >= 7 && date.getHourOfDay() < 17){ //7-17点前一天20时
            date = date.minusDays(1);
            hour = "20";
        }
        if (date.getHourOfDay() >= 17 && date.getHourOfDay() < 24){ //17-24点当天08时
            hour = "08";
        }

        //2.根据给定时间拼接结果结构
        List<String> resCNameList = new ArrayList<>();
        List<String> resENameList = new ArrayList<>();
        if (hour == "08") {
            for (int i = 0; i < wxfax08CNameList.size(); i++){
                resCNameList.add(wxfax08CNameList.get(i));
                resENameList.add(wxfax08ENameList.get(i));
            }

        } else if (hour == "20"){
            for (int i = 0; i < wxfax20CNameList.size(); i++){
                resCNameList.add(wxfax20CNameList.get(i));
                resENameList.add(wxfax20ENameList.get(i));
            }
        }
        for (int i = 0; i < wxfax20AllCNameList.size(); i++){
            resCNameList.add(wxfax20AllCNameList.get(i));
            resENameList.add(wxfax20AllENameList.get(i));
        }

        JSONObject daoJSON0 = radarCloudFaxDDao.getFaxInfo(date.toString("yyyy"), date.toString("MM"), date.toString("dd"), hour, "00");

        //3.根据获取结果拼接结果JSON中数据段
        JSONArray dataArray0 = null;
        if (!(daoJSON0 == null || daoJSON0.isEmpty()
                || (daoJSON0.getString(ResJsonConst.DATA) == null)
                || daoJSON0.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON0.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON0.getString(ResJsonConst.DATA).toUpperCase().equals("NULL"))) {
            dataArray0 = daoJSON0.getJSONArray(ResJsonConst.DATA);
        }

        //如果是08时起报的数据,还需要查询前一天20时起报的500_地面预报图数据
        JSONObject daoJSON1 = null;
        String preDay = null;
        if (hour == "08") {
            DateTime preDate = date.minusDays(1);
            preDay = preDate.toString("dd");
            daoJSON1 = radarCloudFaxDDao.getFaxInfo(preDate.toString("yyyy"),
                    preDate.toString("MM"), preDate.toString("dd"), "20", "00");
        }
        JSONArray dataArray1 = null;
        if (!(daoJSON1 == null || daoJSON1.isEmpty()
                || (daoJSON1.getString(ResJsonConst.DATA) == null)
                || daoJSON1.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON1.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON1.getString(ResJsonConst.DATA).toUpperCase().equals("NULL"))) {
            dataArray1 = daoJSON1.getJSONArray(ResJsonConst.DATA);
        }

        JSONArray resArray = new JSONArray();
        for (int i = 0; i < resCNameList.size(); i++){
            JSONObject jsonData = new JSONObject();
            boolean isFind = false;
            String sDay = date.toString("dd");
            if (dataArray0 != null){
                for (Object urlJSON : dataArray0){
                    String url0 = ((JSONObject)urlJSON).getString("url");
                    if (url0.contains(resENameList.get(i))){
                        isFind = true;
                        jsonData.put(resENameList.get(i).toUpperCase() + "(" + sDay + "日" + resCNameList.get(i) + ")", url0);
                        break;
                    }
                }
            }

            if (preDay != null){
                sDay = preDay;
            }
            if (!isFind && dataArray1 != null){
                for (Object urlJSON : dataArray1){
                    String url1 = ((JSONObject)urlJSON).getString("url");
                    if (url1.contains(resENameList.get(i))){
                        isFind = true;
                        jsonData.put(resENameList.get(i).toUpperCase() + "(" + sDay + "日" + resCNameList.get(i) + ")", url1);
                        break;
                    }
                }
            }
            if (!isFind)
                jsonData.put(resENameList.get(i).toUpperCase() + "(" + sDay + "日" + resCNameList.get(i) + ")", "");
            resArray.add(jsonData);
        }

        resJSON.put(ResJsonConst.TIME, date.toString("yyyyMMdd") + hour + "00");
        JSONOperateTool.putJSONSuccessful(resJSON, resArray);
        return resJSON;
    }


    @Override
    public JSONObject getECMWFImageInfo(String year, String month, String day, String hour, String minute) {
        JSONObject resJSON = new JSONObject();
        resJSON.put(ResJsonConst.DATA, "");
        //1.根据给定的时间调整查询的时间
        DateTime date = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                Integer.parseInt(hour), Integer.parseInt(minute), 0);

        if (date.getHourOfDay() >= 0 && date.getHourOfDay() < 4){ //0-4点前一天08时
            date = date.minusDays(1);
            hour = "08";
        }
        if (date.getHourOfDay() >= 4 && date.getHourOfDay() <= 15){ //7-15点前一天20时
            date = date.minusDays(1);
            hour = "20";
        }
        if (date.getHourOfDay() > 15 && date.getHourOfDay() < 24){ //16-24点当天08时
            hour = "08";
        }


        JSONObject daoJSON = radarCloudFaxDDao.getECMWFImageInfo(date.toString("yyyy"), date.toString("MM"), date.toString("dd"), hour, "00");

        //3.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")) {
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        JSONArray dataArray = daoJSON.getJSONArray(ResJsonConst.DATA);

        String sDay = date.toString("dd");
        Map<String, JSONObject> resMap = new HashMap<>(10);
        for (String key : ecmwfresMap.keySet()){
            JSONObject json = ecmwfresMap.get(key);
            for (String name : json.keySet()){
                json.put(name, "");
                for (Object urlJSON : dataArray){
                    String url = ((JSONObject)urlJSON).getString("url");
                    if (url.contains(name.split("\\(")[0])){
                        json.put(name, url);
                        break;
                    }
                }
            }
            resMap.put(sDay + "日" + hour + "时" + key, json);
        }

        resJSON.put(ResJsonConst.TIME, date.toString("yyyyMMdd") + hour + "00");
        JSONOperateTool.putJSONSuccessful(resJSON, resMap);
        return resJSON;
    }

    private final static float hima8delta = 0.05f; //葵花8二级产品数据分辨率
    private final static float[] hima8latlngs = new float[]{-30, 60, 80, 175}; //葵花8二级产品数据范围

//todo:以下是按照当前时间向前查，目前由于葵花8数据不稳定，暂时不用该方法！
//    @Override
//    public JSONObject getHimawari8L2PointSpaceDataOld(String strLats, String strLngs, String year,
//                                                   String month, String day, String hour, String minute, boolean isInterPolate) {
//        JSONObject resJSON = new JSONObject();
//
//        resJSON.put(ResJsonConst.DATA, "");
//        if (strLats == null || strLats.isEmpty() || strLngs == null || strLngs.isEmpty()
//                || year == null || year.length() != 4 || month == null || month.length() != 2
//                || day == null || day.length() != 2 || hour == null || hour.length() != 2
//                || minute == null || minute.length() != 2) {
//            JSONOperateTool.putJSONParamError(resJSON);
//            return resJSON;
//        }
//
//        String[] strTmpLatAry = strLats.split(",");
//        String[] strTmpLngAry = strLngs.split(",");
//        if (strTmpLatAry == null || strTmpLatAry.length == 0
//                || strTmpLngAry == null || strTmpLngAry.length == 0
//                || strTmpLatAry.length != strTmpLngAry.length) {
//            JSONOperateTool.putJSONParamError(resJSON);
//            return resJSON;
//        }
//
//        //0.过滤掉所有在数据范围以外的点
//        List<Float> validLatAry = new ArrayList<>();
//        List<Float> validLngAry = new ArrayList<>();
//        for (int i = 0; i < strTmpLatAry.length; i++){
//            float lat,lng;
//            try{
//                lat = Float.parseFloat(strTmpLatAry[i]);
//                lng = Float.parseFloat(strTmpLngAry[i]);
//            } catch (NumberFormatException ex){
//                continue;
//            }
//            if (lat < hima8latlngs[0] || lat > hima8latlngs[1]
//                    || lng < hima8latlngs[2] || lng > hima8latlngs[3]){
//                continue;
//            }
//            validLatAry.add(lat);
//            validLngAry.add(lng);
//        }
//        if (validLatAry.isEmpty()){
//            JSONOperateTool.putJSONNoResult(resJSON);
//            return resJSON;
//        }
//
//        //对经纬度数据进行插值
//        float[][] profileLnglats = null;
//        if (isInterPolate && validLatAry.size() >=2){
//            profileLnglats = this.calcSpaceInterplotePoints(validLatAry, validLngAry);
//            if (profileLnglats == null || profileLnglats.length<2){
//                JSONOperateTool.putJSONNoResult(resJSON);
//                return resJSON;
//            }
//        } else  {
//            profileLnglats = new float[validLatAry.size()][2];
//            for (int i = 0; i < validLatAry.size(); i++){
//                profileLnglats[i][0] = validLngAry.get(i);
//                profileLnglats[i][1] = validLatAry.get(i);
//            }
//        }
//
//        //1.根据时间计算到每10分钟
//        try{
//            Integer mi = (Integer.parseInt(minute)/10)*10;
//            minute = String.format("%02d", mi);
//        } catch (NumberFormatException ex){
//            JSONOperateTool.putJSONParamError(resJSON);
//            return resJSON;
//        }
//        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmm");
//        DateTime resDate = DateTime.parse(year + month + day + hour+minute, dateformat);
//        String ryear = resDate.toString("yyyy");
//        String rmonth = resDate.toString("MM");
//        String rday = resDate.toString("dd");
//        String rhour = resDate.toString("HH");
//        String rminute = resDate.toString("mm");
//        DateTime resDate1 = resDate.minusMinutes(10);
//        String ryear1 = resDate1.toString("yyyy");
//        String rmonth1 = resDate1.toString("MM");
//        String rday1 = resDate1.toString("dd");
//        String rhour1 = resDate1.toString("HH");
//        String rminute1 = resDate1.toString("mm");
//        DateTime resDate2 = resDate.minusMinutes(20);
//        String ryear2 = resDate2.toString("yyyy");
//        String rmonth2 = resDate2.toString("MM");
//        String rday2 = resDate2.toString("dd");
//        String rhour2 = resDate2.toString("HH");
//        String rminute2 = resDate2.toString("mm");
//
//        //循环获取各个点的数据
//        JSONArray resDataJSONAry = new JSONArray();
//        boolean hasData = false;
//        Object timeObj = null;
//
//        //取第一个点，如果当前时间没有取到，则向前推10分钟，如果还没取到，则再向前10分钟
//        Float lng = profileLnglats[0][0];
//        Float lat = profileLnglats[0][1];
//        if (lng < 0)
//            lng = 360 + lng; //数据经度为0-360,这里需转换
//        if (lat > 90 || lat < -90 || lng > 360 || lng < -180) { //为保证结果能对应,有一个参数不合法则认为整体参数不合法!
//            JSONOperateTool.putJSONParamError(resJSON);
//            return resJSON;
//        }
//        String strLat = String.format("%.2f", Math.round(lat / hima8delta) * hima8delta);
//        String strLng = String.format("%.2f", Math.round(lng / hima8delta) * hima8delta);
//        JSONArray resLatlngs = new JSONArray();
//        resLatlngs.add(String.format("%.2f", lat) + "," + String.format("%.2f", lng));
//        JSONObject daoJSON0 = radarCloudFaxDDao.getHimawari8L2PointData(strLat, strLng, ryear, rmonth, rday, rhour, rminute);
//        if (this.isEmptyJSON(daoJSON0)) {
//            daoJSON0 = radarCloudFaxDDao.getHimawari8L2PointData(strLat, strLng, ryear1, rmonth1, rday1, rhour1, rminute1);
//            if (this.isEmptyJSON(daoJSON0)){
//                daoJSON0 = radarCloudFaxDDao.getHimawari8L2PointData(strLat, strLng, ryear2, rmonth2, rday1, rhour2, rminute2);
//                if (this.isEmptyJSON(daoJSON0)){
//                    JSONOperateTool.putJSONNoResult(resJSON);
//                    return resJSON;
//                }else{ //用前20分钟的时间
//                    ryear = ryear2;
//                    rmonth = rmonth2;
//                    rday = rday2;
//                    rhour = rhour2;
//                    rminute = rminute2;
//                }
//            }else{ //用前10分钟的时间
//                ryear = ryear1;
//                rmonth = rmonth1;
//                rday = rday1;
//                rhour = rhour1;
//                rminute = rminute1;
//            }
//        }
//        resDataJSONAry.add(daoJSON0.get(ResJsonConst.DATA));
//        timeObj = daoJSON0.get(ResJsonConst.TIME);
//        hasData = true;
//        for (int i = 1; i < profileLnglats.length; i++) {
//            if (i >= 50) //最多取50个点数据
//                break;
//            lng = profileLnglats[i][0];
//            lat = profileLnglats[i][1];
//            if (lng < 0)
//                lng = 360 + lng; //数据经度为0-360,这里需转换
//            if (lat > 90 || lat < -90 || lng > 360 || lng < -180) { //为保证结果能对应,有一个参数不合法则认为整体参数不合法!
//                JSONOperateTool.putJSONParamError(resJSON);
//                return resJSON;
//            }
//
//            strLat = String.format("%.2f", Math.round(lat / hima8delta) * hima8delta);
//            strLng = String.format("%.2f", Math.round(lng / hima8delta) * hima8delta);
//            resLatlngs.add(String.format("%.2f", lat) + "," + String.format("%.2f", lng));
//            JSONObject daoJSON = radarCloudFaxDDao.getHimawari8L2PointData(strLat, strLng, ryear, rmonth, rday, rhour, rminute);
//
//            if (this.isEmptyJSON(daoJSON)) {
//                resDataJSONAry.add(new JSONObject());
//            } else {
//                resDataJSONAry.add(daoJSON.get(ResJsonConst.DATA));
//            }
//        }
//
//        if (!hasData) {
//            JSONOperateTool.putJSONNoResult(resJSON);
//            return resJSON;
//        }
//
//        //3.根据获取结果拼接结果JSON中数据段
//        resJSON.put(ResJsonConst.TIME, timeObj);
//        JSONObject resData = new JSONObject();
//        resData.put("latlngs", resLatlngs);
//        resData.put("profiledatas", resDataJSONAry);
//        JSONOperateTool.putJSONSuccessful(resJSON, resData);
//        return resJSON;
//    }


@Override
public JSONObject getHimawari8L2PointSpaceData(String strLats, String strLngs, String year,
                                               String month, String day, String hour, String minute, boolean isInterPolate) {
    JSONObject resJSON = new JSONObject();

    resJSON.put(ResJsonConst.DATA, "");
    if (strLats == null || strLats.isEmpty() || strLngs == null || strLngs.isEmpty()
            || year == null || year.length() != 4 || month == null || month.length() != 2
            || day == null || day.length() != 2 || hour == null || hour.length() != 2
            || minute == null || minute.length() != 2) {
        JSONOperateTool.putJSONParamError(resJSON);
        return resJSON;
    }

    String[] strTmpLatAry = strLats.split(",");
    String[] strTmpLngAry = strLngs.split(",");
    if (strTmpLatAry == null || strTmpLatAry.length == 0
            || strTmpLngAry == null || strTmpLngAry.length == 0
            || strTmpLatAry.length != strTmpLngAry.length) {
        JSONOperateTool.putJSONParamError(resJSON);
        return resJSON;
    }

    //0.过滤掉所有在数据范围以外的点
    List<Float> validLatAry = new ArrayList<>();
    List<Float> validLngAry = new ArrayList<>();
    for (int i = 0; i < strTmpLatAry.length; i++){
        float lat,lng;
        try{
            lat = Float.parseFloat(strTmpLatAry[i]);
            lng = Float.parseFloat(strTmpLngAry[i]);
        } catch (NumberFormatException ex){
            continue;
        }
        if (lat < hima8latlngs[0] || lat > hima8latlngs[1]
                || lng < hima8latlngs[2] || lng > hima8latlngs[3]){
            continue;
        }
        validLatAry.add(lat);
        validLngAry.add(lng);
    }
    if (validLatAry.isEmpty()){
        JSONOperateTool.putJSONNoResult(resJSON);
        return resJSON;
    }

    //对经纬度数据进行插值
    float[][] profileLnglats = null;
    if (isInterPolate && validLatAry.size() >=2){
        profileLnglats = this.calcSpaceInterplotePoints(validLatAry, validLngAry);
        if (profileLnglats == null || profileLnglats.length<2){
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
    } else  {
        profileLnglats = new float[validLatAry.size()][2];
        for (int i = 0; i < validLatAry.size(); i++){
            profileLnglats[i][0] = validLngAry.get(i);
            profileLnglats[i][1] = validLatAry.get(i);
        }
    }

    DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmm");
    DateTime resDate = DateTime.parse(year + month + day + hour+ minute, dateformat);

    //1.循环获取各个点的数据
    JSONArray resDataJSONAry = new JSONArray();
    boolean hasData = false;
    Object timeObj = null;

    //todo: 取第一个点数据，查询时间序列，然后取最新有数据的时间作为查询时间(这样效率较低，但现在数据不稳定，这样可以保证数据！)
    Float lng = profileLnglats[0][0];
    Float lat = profileLnglats[0][1];
    if (lng < 0)
        lng = 360 + lng; //数据经度为0-360,这里需转换
    if (lat > 90 || lat < -90 || lng > 360 || lng < -180) { //为保证结果能对应,有一个参数不合法则认为整体参数不合法!
        JSONOperateTool.putJSONParamError(resJSON);
        return resJSON;
    }
    String strLat = String.format("%.2f", Math.round(lat / hima8delta) * hima8delta);
    String strLng = String.format("%.2f", Math.round(lng / hima8delta) * hima8delta);

    JSONObject firstPointJSONS = this.getHimawari8L2PointTimeData(strLat, strLng, year, month, day, hour, minute);
    if (this.isEmptyJSON(firstPointJSONS)){
        JSONOperateTool.putJSONNoResult(resJSON);
        return resJSON;
    }

    String lastTime = "";
    String strFirstData = null;
    String strTmpFirstData = null;
    JSONObject firstPointDataJSONS = JSONObject.parseObject(firstPointJSONS.getString(ResJsonConst.DATA));
    for (String time : firstPointDataJSONS.keySet()){
        strTmpFirstData = firstPointDataJSONS.getString(time);
        if (strTmpFirstData == null
                || strTmpFirstData.isEmpty()
                || strTmpFirstData.equals("{}")
                || strTmpFirstData.toUpperCase().equals("NIL")
                || strTmpFirstData.toUpperCase().equals("NULL"))
            continue;
        if (time.compareTo(lastTime) > 0){
            lastTime = time;
            strFirstData = strTmpFirstData;
        }
    }
    if (lastTime == null || lastTime.equals("")
            || lastTime.length() != 6 || strFirstData == null){
        JSONOperateTool.putJSONNoResult(resJSON);
        return resJSON;
    }
    String rminute = lastTime.substring(4, 6);
    String rhour = lastTime.substring(2, 4);
    String rday  = lastTime.substring(0, 2);
    if (!rday.equals(day)){ //如果返回的日与给定的日不同，则表明为前一天!
        resDate = resDate.minusDays(1);
    }
    String ryear = resDate.toString("yyyy");
    String rmonth = resDate.toString("MM");
    rday = resDate.toString("dd");


    JSONArray resLatlngs = new JSONArray();
    resLatlngs.add(String.format("%.2f", lat) + "," + String.format("%.2f", lng));

    resDataJSONAry.add(JSONObject.parseObject(strFirstData));
    timeObj = ryear+rmonth+rday+rhour+rminute;
    hasData = true;
    for (int i = 1; i < profileLnglats.length; i++) {
        if (i >= 50) //最多取50个点数据
            break;
        lng = profileLnglats[i][0];
        lat = profileLnglats[i][1];
        if (lng < 0)
            lng = 360 + lng; //数据经度为0-360,这里需转换
        if (lat > 90 || lat < -90 || lng > 360 || lng < -180) { //为保证结果能对应,有一个参数不合法则认为整体参数不合法!
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        strLat = String.format("%.2f", Math.round(lat / hima8delta) * hima8delta);
        strLng = String.format("%.2f", Math.round(lng / hima8delta) * hima8delta);
        resLatlngs.add(String.format("%.2f", lat) + "," + String.format("%.2f", lng));
        JSONObject daoJSON = radarCloudFaxDDao.getHimawari8L2PointData(strLat, strLng, ryear, rmonth, rday, rhour, rminute);

        if (this.isEmptyJSON(daoJSON)) {
            resDataJSONAry.add(new JSONObject());
        } else {
            resDataJSONAry.add(daoJSON.get(ResJsonConst.DATA));
        }
    }

    if (!hasData) {
        JSONOperateTool.putJSONNoResult(resJSON);
        return resJSON;
    }

    //3.根据获取结果拼接结果JSON中数据段
    resJSON.put(ResJsonConst.TIME, timeObj);
    JSONObject resData = new JSONObject();
    resData.put("latlngs", resLatlngs);
    resData.put("profiledatas", resDataJSONAry);
    JSONOperateTool.putJSONSuccessful(resJSON, resData);
    return resJSON;
}

    private float[][] calcSpaceInterplotePoints(List<Float> latAry, List<Float> lngAry){
        float[][] profileLnglats = null;
        final int maxNum = 25;
        //如果有多于2个点,则每两个点计算插值点,插值点总数不超过maxNum
        //插值数按照每两个点距离与整体距离之比进行计算
        double totaldist = 0f;
        if (latAry.size() > 2){
            for (int i = 0; i < latAry.size()-1; i++){
                float startX = lngAry.get(i);
                float startY = latAry.get(i);
                float endX = lngAry.get(i+1);
                float endY = latAry.get(i+1);
                totaldist += Math.sqrt((endX - startX)*(endX - startX) + (endY - startY)*(endY - startY));
            }
        }

        for (int i = 0; i < latAry.size()-1; i++){
            float startX = lngAry.get(i);
            float startY = latAry.get(i);
            float endX = lngAry.get(i+1);
            float endY = latAry.get(i+1);
            int insertNum = maxNum;
            if (totaldist > 0){
                double ratio = Math.sqrt((endX - startX)*(endX - startX) + (endY - startY)*(endY - startY))/totaldist;
                insertNum = (int)Math.round(maxNum * ratio);
                if (insertNum < 2)
                    insertNum = 2;
            }
            float[][] tempprofileLnglats = MIDS3DMath.getInsertProfileDatas(startX, startY, endX, endY, insertNum);

            if (tempprofileLnglats == null || tempprofileLnglats.length < 2) {
                continue;
            }
            int xlen = 0;
            int ylen = tempprofileLnglats[0].length;
            if (profileLnglats == null || profileLnglats.length == 0){
                profileLnglats = new float[tempprofileLnglats.length][tempprofileLnglats[0].length];

            } else {
                xlen = profileLnglats.length-1; //舍去前一个剖面的最后一个点,该点与下一个剖面的点重复
                float[][] oldprofileLnglats = profileLnglats;
                profileLnglats = new float[xlen + tempprofileLnglats.length][ylen];

                for (int m = 0; m < xlen; m++){
                    for (int n = 0; n < ylen; n++){
                        profileLnglats[m][n] = oldprofileLnglats[m][n];
                    }
                }
            }
            for (int m = 0; m < tempprofileLnglats.length; m++){
                for (int n = 0; n < ylen; n++){
                    profileLnglats[xlen + m][n] = tempprofileLnglats[m][n];
                }
            }
        }

        return profileLnglats;
    }

    @Override
    public JSONObject getHimawari8L2PointTimeData(String strLat, String strLng, String year, String month, String day, String hour, String minute) {
        JSONObject resJSON = new JSONObject();

        resJSON.put(ResJsonConst.DATA, "");
        if (strLat == null || strLat.isEmpty() || strLng == null || strLng.isEmpty()
                || year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2
                || minute == null || minute.length() != 2) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        //1.根据时间计算到每10分钟
        try{
            Integer mi = (Integer.parseInt(minute)/10)*10;
            minute = String.format("%02d", mi);
        } catch (NumberFormatException ex){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmm");
        DateTime resDate = DateTime.parse(year + month + day + hour+minute, dateformat);


        //2.循环获取最近4个小时每10分钟的数据
        JSONObject resDataJSONAry = new JSONObject();
        boolean hasData = false;
        Object timeObj = null;


        float lat = Float.parseFloat(strLat);
        float lng = Float.parseFloat(strLng);
        if (lng < 0)
            lng = 360 + lng; //数据经度为0-360,这里需转换
        if (lat > 90 || lat < -90 || lng > 360 || lng < -180) { //为保证结果能对应,有一个参数不合法则认为整体参数不合法!
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }
        String sLat = String.format("%.2f", Math.round(lat / hima8delta) * hima8delta);
        String sLng = String.format("%.2f", Math.round(lng / hima8delta) * hima8delta);
        int minutesnum = 4*60/10; //取最近4个小时
        for (int i = 0; i < minutesnum; i++) {
            JSONObject daoJSON = radarCloudFaxDDao.getHimawari8L2PointData(sLat, sLng, resDate.toString("yyyy"),
                    resDate.toString("MM"), resDate.toString("dd"), resDate.toString("HH"), resDate.toString("mm"));

            if (i == 0)
                timeObj = daoJSON.get(ResJsonConst.TIME);

            if (this.isEmptyJSON(daoJSON)) {
                resDataJSONAry.put(resDate.toString("ddHHmm"), new JSONObject());
            } else {
                resDataJSONAry.put(resDate.toString("ddHHmm"), daoJSON.get(ResJsonConst.DATA));
                hasData = true;
            }
            resDate = resDate.minusMinutes(10);
        }

        if (!hasData) {
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }

        //3.根据获取结果拼接结果JSON中数据段
        resJSON.put(ResJsonConst.TIME, timeObj);
        JSONOperateTool.putJSONSuccessful(resJSON, resDataJSONAry);
        return resJSON;
    }

    @Override
    public JSONObject getNearestRadarInfo(String slat, String slng, String year, String month, String day, String hour, String minute) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();

        resJSON.put(ResJsonConst.DATA, "");

        if (year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2
                || minute == null || minute.length() != 2 ) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
        }

        Float lat;
        Float lng;
        try{
            lat = Float.parseFloat(slat);
            lng = Float.parseFloat(slng);
        } catch (NumberFormatException ex){
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }
        if (lat > 90 || lat < -90 || lng > 180
                || lng < -180 || lat == 0.0f || lng == 0.0f) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }

        radarCloudFaxDDao.setRadarCLatLng(lat, lng);
        return this.getInfo("neareststationradar", year, month, day, hour, minute, null);
    }

    private static List<StationRadarInfoBean> ALLRADARINFOLIST;
    @Override
    public JSONObject getStationRadarDistribInfo() {
        JSONObject resJSON = new JSONObject();

        if (ALLRADARINFOLIST == null){
            ALLRADARINFOLIST = radarCloudFaxDDao.getStationRadarDistribInfo();
        }

        if (ALLRADARINFOLIST == null || ALLRADARINFOLIST.isEmpty()){
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }


        JSONArray resArray = new JSONArray();
        for (StationRadarInfoBean radarInfo : ALLRADARINFOLIST){
            JSONObject infoJSON = new JSONObject();
            infoJSON.put("cname", radarInfo.getCname());
            infoJSON.put("ename", radarInfo.getEname());
            infoJSON.put("lat", radarInfo.getClat());
            infoJSON.put("lng", radarInfo.getClng());
            infoJSON.put("code", radarInfo.getStationID());
            infoJSON.put("province", radarInfo.getProvince());
            resArray.add(infoJSON);
        }

        JSONOperateTool.putJSONSuccessful(resJSON, resArray);
        return resJSON;
    }

    @Override
    public JSONObject getOceanFCImageInfo(String type,String year, String month, String day, String hour, String minute) {
        JSONObject resJSON = new JSONObject();

        JSONObject daoJSON = radarCloudFaxDDao.getOceanFCInfo(type,year,month,day);
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")) {
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        resJSON.put(ResJsonConst.TIME, daoJSON.get(ResJsonConst.TIME));
        return resJSON;
    }

    @Override
    public JSONObject getCoastRegionFC(String region, String year, String month, String day, String hour, String minute) {
        JSONObject resJSON = new JSONObject();
        JSONObject daoJSON = radarCloudFaxDDao.getCoastRegionFC(region,year,month,day);
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")) {
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        resJSON.put(ResJsonConst.TIME, daoJSON.get(ResJsonConst.TIME));
        return resJSON;
    }


    private boolean isEmptyJSON(JSONObject daoJSON) {
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).equals("{}")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")) {
            return true;
        }
        return false;
    }



    private JSONObject getInfo(String type, String year, String month, String day, String hour, String minute, String radarIDs){
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();

        resJSON.put(ResJsonConst.DATA, "");

        if (year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2
                || minute == null || minute.length() != 2 ) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
        }

        if (type.contains("himawari8l1") || type.contains("himawari8l2")){
            //葵花为世界时，这里将输入的北京时转换为世界时
            DateTime bdate = new DateTime(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),
                    Integer.parseInt(hour), Integer.parseInt(minute), 0).minusHours(8);
            year  = bdate.toString("yyyy");
            month = bdate.toString("MM");
            day   = bdate.toString("dd");
            hour  = bdate.toString("HH");
            minute = bdate.toString("mm");
        }

        JSONObject daoJSON = radarCloudFaxDDao.getRadarCloudInfo(type, year, month, day, hour, minute, radarIDs);

        //1.根据获取结果拼接结果JSON中数据段
        if (daoJSON == null || daoJSON.isEmpty()
                || (daoJSON.getString(ResJsonConst.DATA) == null)
                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")) {
            JSONOperateTool.putJSONNoResult(resJSON);
            return resJSON;
        }
        resJSON.put(ResJsonConst.TIME, daoJSON.get(ResJsonConst.TIME));
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        return resJSON;
    }
}
