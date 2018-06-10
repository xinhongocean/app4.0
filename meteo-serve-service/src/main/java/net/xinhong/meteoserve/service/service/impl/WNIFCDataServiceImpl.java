package net.xinhong.meteoserve.service.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.common.tool.ConfigUtil;
import net.xinhong.meteoserve.common.tool.DateUtil;
import net.xinhong.meteoserve.common.tool.GridDataUtil;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.dao.WNIFCDataDao;
import net.xinhong.meteoserve.service.service.WNIFCDataService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;

/**
 * Created by xiaoyu on 16/4/19.
 */
@Service
public class WNIFCDataServiceImpl implements WNIFCDataService {
    private static final Log logger = LogFactory.getLog(StationDataSurfServiceImpl.class);


    private static final float delta = 1.25f; //数据分辨率

    @Autowired
    private WNIFCDataDao wnifcDataDao;

    @Override
    public JSONObject getPointsData(String strLats, String strLngs, String year, String month, String day, String hour, String VTI) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();

        //todo:wni接口目前已废弃，为保证APP显示正常，直接返回成功
        //resJSON.put(ResJsonConst.DATA, "");
        resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SUCCESSFUL.getStatusCode());
        resJSON.put(ResJsonConst.STATUSMSG, ResStatus.SUCCESSFUL.getMessage());
        JSONArray jsonarray = new JSONArray();
        JSONObject json = new JSONObject();
        json.put("CBTEMP1", "");
        jsonarray.add(json);
        resJSON.put(ResJsonConst.DATA, jsonarray);

        return resJSON;


//        resJSON.put(ResJsonConst.DATA, "");
//        if  (strLats == null || strLngs.isEmpty() || strLngs == null || strLngs.isEmpty()
//                || year == null || year.length() != 4 || month == null || month.length() != 2
//                || day == null || day.length() != 2 || hour == null || hour.length() != 2){
//            JSONOperateTool.putJSONParamError(resJSON);
//            return resJSON;
//        }
//
//        String[] strLatAry = strLats.split(",");
//        String[] strLngAry = strLngs.split(",");
//        if  (strLatAry == null || strLatAry.length == 0
//                || strLngAry == null || strLngAry.length == 0
//                || strLngAry.length != strLatAry.length){
//            JSONOperateTool.putJSONParamError(resJSON);
//            return resJSON;
//        }
//
//        //1.根据时间计算起报时间及预报时效
//        VTI = StringUtils.leftPad(VTI, 3, "0");
//        //循环获取各个点的数据
//        JSONArray resDataJSONAry = new JSONArray();
//        boolean hasData = false;
//        Object timeObj = null;
//        for (int i = 0; i < strLatAry.length; i++){
//            if (i >= 100) //最多取100个点数据
//                break;
//            float lat = Float.parseFloat(strLatAry[i]);
//            float lng = Float.parseFloat(strLngAry[i]);
//            if (lng < 0)
//                lng = 360 + lng; //数据经度为0-360,这里需转换
//            if (lat > 90 || lat < -90 || lng > 360 || lng < -180){ //为保证结果能对应,有一个参数不合法则认为整体参数不合法!
//                JSONOperateTool.putJSONParamError(resJSON);
//                return resJSON;
//            }
//            String strLat = String.format("%.2f", Math.round(lat / delta) * delta);
//            String strLng = String.format("%.2f", Math.round(lng / delta) * delta);
//            JSONObject daoJSON = wnifcDataDao.getPointDangerData(strLat, strLng, year, month, day, hour, VTI);
//
//            if (daoJSON == null || daoJSON.isEmpty()
//                    || (daoJSON.getString(ResJsonConst.DATA) == null)
//                    || daoJSON.getString(ResJsonConst.DATA).isEmpty()
//                    || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
//                    || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")) {
//                resDataJSONAry.add(new JSONObject());
//            } else {
//                resDataJSONAry.add(daoJSON.get(ResJsonConst.DATA));
//                hasData = true;
//                timeObj = daoJSON.get(ResJsonConst.TIME);
//            }
//        }
//        if (!hasData)
//        {
//            JSONOperateTool.putJSONNoResult(resJSON);
//            return resJSON;
//        }
//
//        //3.根据获取结果拼接结果JSON中数据段
//        resJSON.put(ResJsonConst.TIME, timeObj);
//        JSONOperateTool.putJSONSuccessful(resJSON, resDataJSONAry);
//        return resJSON;
    }

    @Override
    public JSONObject getPointDangerData(float lat, float lng, String year, String month, String day, String hour, String VTI) {
        //0.初始化JSON对象，并判断参数的正确性
        JSONObject resJSON = new JSONObject();

        //todo:wni接口目前已废弃，为保证APP显示正常，直接返回成功
        //resJSON.put(ResJsonConst.DATA, "");
        resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SUCCESSFUL.getStatusCode());
        resJSON.put(ResJsonConst.STATUSMSG, ResStatus.SUCCESSFUL.getMessage());
        JSONObject json = new JSONObject();
        json.put("CBTEMP2", "");
        resJSON.put(ResJsonConst.DATA, json);
        return resJSON;

//        if (lng < 0)
//            lng = 360 + lng; //数据经度为0-360,这里需转换
//        if (year == null || year.length() != 4 || month == null || month.length() != 2
//                || day == null || day.length() != 2 || hour == null || hour.length() != 2
//                || VTI == null || VTI.isEmpty() || lat > 90 || lat < -90 || lng > 360 || lng < 0) {
//            JSONOperateTool.putJSONParamError(resJSON);
//            return resJSON;
//        } else {
//            JSONOperateTool.putJSONNoResult(resJSON);
//        }
//        String strLat = String.format("%.2f", Math.round(lat / delta) * delta);
//        String strLng = String.format("%.2f", Math.round(lng / delta) * delta);
//        JSONObject daoJSON = wnifcDataDao.getPointDangerData(strLat, strLng, year, month, day, hour, VTI);
//        resJSON.put(ResJsonConst.TIME, daoJSON.get(ResJsonConst.TIME));
//
//        //3.根据获取结果拼接结果JSON中数据段
//        if (daoJSON == null || daoJSON.isEmpty()
//                || (daoJSON.getString(ResJsonConst.DATA) == null)
//                || daoJSON.getString(ResJsonConst.DATA).isEmpty()
//                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NIL")
//                || daoJSON.getString(ResJsonConst.DATA).toUpperCase().equals("NULL")) {
//            JSONOperateTool.putJSONNoResult(resJSON);
//            return resJSON;
//        }
//        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
//        return resJSON;
    }

    public JSONObject getAreaData(String year, String month, String day, String hour, String vti,
                                  String type, String height, float sLat, float eLat,
                                  float sLng, float eLng, String elem) {
        JSONObject resJSON = new JSONObject();
        resJSON.put(ResJsonConst.DATA, "");
        if (sLng < 0)
            sLng = 360 + sLng; //数据经度为0-360,这里需转换
        if (eLng < 0)
            eLng = 360 + eLng; //数据经度为0-360,这里需转换
        if (sLat > 90 || sLat < -90 || elem == null || "".equals(elem.trim())
                || eLat > 90 || eLat < -90 || sLng > 360 || sLng < 0
                || eLng > 360 || eLng < 0 || (eLat - sLat) < delta || (eLng - sLng) < delta) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
        }
        // TODO: 2016/5/4 数据时间
        resJSON.put(ResJsonConst.DATA, "");
        String sLatStr = String.format("%.2f", Math.floor(sLat / delta) * delta);
        String eLatStr = String.format("%.2f", Math.ceil(eLat / delta) * delta);
        String sLngStr = String.format("%.2f", Math.floor(sLng / delta) * delta);
        String eLngStr = String.format("%.2f", Math.ceil(eLng / delta) * delta);

        float sLatFloat = Float.parseFloat(sLatStr);
        float eLatFloat = Float.parseFloat(eLatStr);
        float sLngFloat = Float.parseFloat(sLngStr);
        float eLngFloat = Float.parseFloat(eLngStr);
        eLngFloat = eLngFloat > 358.75f ? 358.75f : eLngFloat;
        String dateStr = DateUtil.dateToUTC(year, month, day, hour);

        if (elem.equals("WS") || elem.equals("WD")) {
            setResJsonByCache(dateStr, vti, height, elem, sLatFloat, eLatFloat, sLngFloat, eLngFloat, resJSON);
        } else {
            StringBuffer path = new StringBuffer();

            path.append(ConfigUtil.getProperty(DataTypeConst.PROCESS_WNI_FILE_PATH)).append(dateStr)
                    .append(File.separator).append("FAA_SRF_WIFS_").append(elem)
                    .append("_KWBC_").append(dateStr).append("_").append(vti)
                    .append("_").append(type);
            if (height != null && !"".equals(height.trim())) {
                path.append("_").append(height);
            }
            path.append(".grb");
            logger.info("开始纬度{" + sLat + "}，结束纬度{" + eLat + "}，开始经度{" + sLng + "}，结束经度{" + eLng + "}，文件路径{" + path + "}，要素{" + elem + "}");
            setResJsonByFile(path.toString(), elem, sLatFloat, eLatFloat, sLngFloat, eLngFloat, resJSON);
        }
        return resJSON;
    }


    private void setResJsonByCache(String dateStr, String vti,
                                   String height, String elem, float sLat, float eLat,
                                   float sLng, float eLng, JSONObject resJSON) {
        JSONObject dataObj = wnifcDataDao.getAreaData(dateStr, vti, height, elem);
        if (dataObj != null) {
            //  GridData gridData = JSONObject.parseObject(dataObj.toJSONString(), GridData.class);
            JSONArray rowArray = dataObj.getJSONArray("gridData");
            JSONArray cloArray = (JSONArray) rowArray.get(0);
            float[][] gridData = new float[rowArray.size()][cloArray.size()];
            for (int i = 0; i < rowArray.size(); i++) {
                JSONArray arrayrow = (JSONArray) rowArray.get(i);
                for (int j = 0; j < arrayrow.size(); j++) {
                    gridData[i][j] = ((BigDecimal) arrayrow.get(j)).floatValue();
                }
            }
            JSONObject resObj = GridDataUtil.getFreeAreaData(sLat, eLat, sLng, eLng, gridData);
            JSONOperateTool.putJSONFreeArea(resJSON, sLat, eLat,
                    sLng, eLng, resObj.getIntValue(ResJsonConst.ROWNUM),
                    resObj.getIntValue(ResJsonConst.COLNUM), delta);
            JSONOperateTool.putJSONSuccessful(resJSON, resObj.get("array"));
            resJSON.put(ResJsonConst.TIME, dataObj.get(ResJsonConst.TIME));
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
        }
    }

    /**
     * wni 小文件中获取数据
     *
     * @param path
     * @param elem
     * @param sLatFloat
     * @param eLatFloat
     * @param sLngFloat
     * @param eLngFloat
     * @param resJSON
     */
    private void setResJsonByFile(String path, String elem,
                                  float sLatFloat, float eLatFloat,
                                  float sLngFloat, float eLngFloat, JSONObject resJSON) {
        JSONObject jsonData = wnifcDataDao.getAreaData(path, elem);
        JSONObject resObj = null;
        if (jsonData != null) {
            if (jsonData.keySet().size() == 0) {
                JSONOperateTool.putJSONNoResult(resJSON);
            } else {
                for (String key : jsonData.keySet()) {
                    JSONObject data = jsonData.getJSONObject(key);

                    JSONArray array = data.getJSONArray("gridData");
                    JSONArray arrayrow0 = (JSONArray) array.get(0);
                    float[][] gridData = new float[array.size()][arrayrow0.size()];
                    for (int i = 0; i < array.size(); i++) {
                        JSONArray arrayrow = (JSONArray) array.get(i);
                        for (int j = 0; j < arrayrow.size(); j++) {
                            gridData[i][j] = (float) arrayrow.get(j);
                        }
                    }
                    resObj = GridDataUtil.getFreeAreaData(sLatFloat, eLatFloat,
                            sLngFloat, eLngFloat, gridData);
                }
            }
            resJSON.put(ResJsonConst.TIME, jsonData.get(ResJsonConst.TIME));
        } else {
            JSONOperateTool.putJSONNoResult(resJSON);
        }
        if (resObj == null || resObj.getJSONArray("array").size() == 0) {
            JSONOperateTool.putJSONNoResult(resJSON);
        } else {
            JSONOperateTool.putJSONFreeArea(resJSON, sLatFloat, eLatFloat,
                    sLngFloat, eLngFloat,
                    resObj.getIntValue(ResJsonConst.ROWNUM), resObj.getIntValue(ResJsonConst.COLNUM),
                    delta);
            JSONOperateTool.putJSONSuccessful(resJSON, resObj.get("array"));
            // dataJson.put(ResJsonConst.ARRAY, resObj.getJSONArray(ResJsonConst.ARRAY));
        }
    }


    /**
     * 获取等值线数据
     *
     * @param freeArea EN东北半球
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param VTI
     * @param level
     * @return
     */
    public JSONObject getIsoLineData(String freeArea, String year, String month,
                                     String day, String hour, String VTI, String level, String elem) {

        JSONObject resJSON = new JSONObject();
        if (freeArea == null || "".equals(freeArea) || year == null || year.length() != 4 || month == null || month.length() != 2
                || day == null || day.length() != 2 || hour == null || hour.length() != 2
                || VTI == null || VTI.isEmpty() || elem == null || elem.isEmpty()) {
            JSONOperateTool.putJSONParamError(resJSON);
            return resJSON;
        }
        VTI = StringUtils.leftPad(VTI, 3, "0");

        JSONObject daoJSON = wnifcDataDao.getIsoLineData(freeArea, year, month, day, hour, VTI, level, elem);
        if (daoJSON == null || "".equals(daoJSON)) {
            JSONOperateTool.putJSONNoResult(resJSON);
        } else {
            JSONOperateTool.putJSONSuccessful(resJSON, daoJSON);
            resJSON.put(ResJsonConst.TIME, daoJSON.get(ResJsonConst.TIME));
        }
        return resJSON;
    }


}
