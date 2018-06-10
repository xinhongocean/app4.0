package net.xinhong.meteoserve.service.domain;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ElemHigh;
import net.xinhong.meteoserve.service.dao.StationInfoSurfDao;
import net.xinhong.meteoserve.service.dao.impl.StationInfoSurfDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: <br>
 *
 * @author 作者 <a href=ds.lht@163.com>stone</a>
 * @version 创建时间：2016/5/16.
 */
public class StationHighData {

    private static Logger logger = LoggerFactory.getLogger(StationHighData.class);
    private String station;
    private String dateStr;
    private String hour;
    private String minute = "00";

    private static final String STATION_CODE = "STATION_CODE";
    private static final String YEAR = "YEAR";
    private static final String MONTH = "MONTH";
    private static final String DAY = "DAY";
    private static final String HOUR = "HOUR";
    private static final String MINUTE = "MINUTE";
    private static final String MdsConversionSign = "Ж";

    private StationInfoSurfDao stationInfoSurfDao;

    /**
     * key : 站号_年月日
     */
    private Map<String, JSONObject> data = new HashMap<>();

    public  Map<String, JSONObject> getData(){
        return data;
    }

    /**
     *
     * @param dateStr 起报日期 Format:yyyyMMdd
     */
    public StationHighData(String dateStr, StationInfoSurfDao stationInfoSurfDao) {

        this.dateStr = dateStr;
        this.stationInfoSurfDao = stationInfoSurfDao;
    }

    public void put(String hour, String rowValues, Map<String, JSONObject> columnsIndexMap) {
        this.hour = hour;
        if (columnsIndexMap.isEmpty()) {
            logger.error("stationHighData加载columns配置文件异常！");
            return;
        }

        JSONObject dataColJson = columnsIndexMap.get("data");
        JSONObject fieldColJson = columnsIndexMap.get("field");
        if (fieldColJson == null || fieldColJson.getJSONObject(STATION_CODE) == null) {
            logger.error("czfc_columns.json配置文件中缺少field:{}", STATION_CODE);
            return;
        }
        String[] dataArray = rowValues.split("\n");

        String level ;

        int stationIndex = fieldColJson.getJSONObject(STATION_CODE).getInteger("index");
        int pressIndex = fieldColJson.getJSONObject("PRESS").getIntValue("index");

        for (String valStr : dataArray) {
            if (valStr != null && !valStr.trim().equals("")) {
                String[] rowData = valStr.split(",", -1);

                StationInfoSurfBean info = stationInfoSurfDao.getSelectedStationInfoFromCode(rowData[stationIndex]);
                if (info == null)
                    continue;
                this.station = rowData[stationIndex];
                String key = this.station +"_"+ info.getLng() + "_" + info.getLat();
                level = rowData[pressIndex];
                int tmpLevel = (int)Float.parseFloat(level);
                if(tmpLevel<100 || tmpLevel == 1000){//高空数据只要100百帕以下 1000百帕以上的数据
                    continue;
                }
                level = String.format("%04d", tmpLevel);
                JSONObject hourJson = data.get(key)==null ? new JSONObject() :  data.get(key);
              //  JSONObject processJson = hourJson.getJSONObject(hour + minute) == null ? new JSONObject(): hourJson.getJSONObject(hour + minute);
                JSONObject dataJson = new JSONObject();
                for (ElemHigh elemHigh : ElemHigh.values()) {
                    JSONObject col = dataColJson.getJSONObject(elemHigh.getEname());
                    if (col == null) {
                        logger.debug("配置文件中缺少{}要素！", elemHigh.getEname());
                        continue;
                    }
                    String value = rowData[col.getIntValue("index")].replace(MdsConversionSign, ",");
                    String format = col.getString("FORMAT");
                    if (value == null || value.trim().equals("")) {
                        continue;
                    } else if (format.indexOf("NUMBER") > -1) {
                        if (format.indexOf(",") > -1)
                            dataJson.put(elemHigh.getEname(), Float.parseFloat(value));
                        else {
                            dataJson.put(elemHigh.getEname(), Integer.parseInt(value));
                        }
                    } else {
                        dataJson.put(elemHigh.getEname(), value);
                    }
                }
                hourJson.put(level, dataJson);
                data.put(key,hourJson);
            }
        }
    }

}