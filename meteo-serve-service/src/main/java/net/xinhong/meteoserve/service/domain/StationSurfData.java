package net.xinhong.meteoserve.service.domain;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ElemSurf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Description: 一个地面实况站点一天的数据<br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 邓帅
 * @version 创建时间：2016/3/8 0008.
 */
public class StationSurfData {
    private static Logger logger = LoggerFactory.getLogger(StationSurfData.class);

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




    /**
     * @param dateStr yyyyMMdd
     */
    public StationSurfData(String dateStr) {
        this.dateStr = dateStr;
    }

    private JSONObject jsonData = new JSONObject();

    public JSONObject getJsonData() {
        return this.jsonData;
    }

    private void init(String[] rowValues, Map<String, JSONObject> columnsIndexMap) {
        //获取列配置信息
        JSONObject fieldJson = columnsIndexMap.get("field");
        JSONObject stationJson = fieldJson.getJSONObject(STATION_CODE);
        JSONObject yearJson = fieldJson.getJSONObject(YEAR);
        JSONObject monthJson = fieldJson.getJSONObject(MONTH);
        JSONObject dayJson = fieldJson.getJSONObject(DAY);
        JSONObject hourJson = fieldJson.getJSONObject(HOUR);
        JSONObject minuteJson = fieldJson.getJSONObject(MINUTE);

        if (stationJson == null) {
            logger.debug("dm_columns.json配置文件中缺少field：{}", STATION_CODE);
            return;
        } else {
            this.station = rowValues[stationJson.getInteger("index")];
        }
        if (yearJson == null) {
            logger.error("dm_columns.json配置文件中缺少field：{}", YEAR);
            return;
        } else if (monthJson == null) {
            logger.error("dm_columns.json配置文件中缺少field：{}", MONTH);
            return;
        } else if (dayJson == null) {
            logger.error("dm_columns.json配置文件中缺少field：{}", DAY);
            return;
        } else if (hourJson == null) {
            logger.error("dm_columns.json配置文件中缺少field：{}", HOUR);
            return;
        } else if (minuteJson == null) {
            logger.error("dm_columns.json配置文件中缺少field：{}", MINUTE);
            return;
        }
        String year = rowValues[yearJson.getInteger("index")];
        if (year != null && !"".equals(year.trim())) {
            this.dateStr = rowValues[yearJson.getInteger("index")] + rowValues[monthJson.getInteger("index")]
                    + rowValues[dayJson.getInteger("index")];
        }
    }

    public void put(String hour, String[] rowValues, Map<String, JSONObject> columnsIndexMap) {
        this.hour = hour;
        if (columnsIndexMap.isEmpty()) {
            logger.error("stationSufrData加载columns配置文件异常！");
            return;
        }
        this.init(rowValues, columnsIndexMap);
        JSONObject dataColJson = columnsIndexMap.get("data");

        JSONObject hourJson = new JSONObject();
        for (ElemSurf elemSurf : ElemSurf.values()) {
            JSONObject col = dataColJson.getJSONObject(elemSurf.getEname());
            if (col == null) {
             //   logger.debug("配置文件中缺少{}要素！", elemSurf.getEname());
                continue;
            }
            String value = rowValues[col.getInteger("index")].replace(MdsConversionSign, ",");
            String format = col.getString("FORMAT");
            if (value == null || "".equals(value.trim())) {
                hourJson.put(elemSurf.getEname(), null);
            } else if (format.indexOf("NUMBER") > -1) {
                if (format.indexOf(",") > -1) {
                    hourJson.put(elemSurf.getEname(), Float.parseFloat(value));
                } else {
                    hourJson.put(elemSurf.getEname(), Integer.parseInt(value));
                }
            }else{
                hourJson.put(elemSurf.getEname(), value);
            }
        }
        this.jsonData =  hourJson;
    }


    public String getStation() {
        return station;
    }

    public String getDateStr() {
        return this.dateStr;
    }

    public String getHour() {
        return hour;
    }

    public String getMinute() {
        return minute;
    }
}
