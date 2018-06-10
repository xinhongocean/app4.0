package net.xinhong.meteoserve.service.controller;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.service.StationDataCityFCService;
import net.xinhong.meteoserve.service.service.StationDataSurfService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Description: <br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 <a href=mailto:liusofttech@sina.com>刘晓昌</a>
 * @version 创建时间：2016/3/11.
 */
@Controller
@RequestMapping(value = "/stationdata_surf")
public class StationDataSurfController {
    private static final Log logger = LogFactory.getLog(StationDataSurfController.class);

    @Autowired
    private StationDataSurfService stationDataSurfService;

    /**
     * 测试页面入口
     *
     * @return
     */
    @RequestMapping(value = "/dataview.do", method = {RequestMethod.POST, RequestMethod.GET}, produces = "text/html;charset=UTF-8")
    public final String surfInfo() {
        return "station/surfsearch";
    }

    //  @ResponseBody
    @RequestMapping(value = "/datafromlatlng", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void dataFromLatLng(HttpServletRequest request, HttpServletResponse response) {
        float lat = Float.parseFloat(request.getParameter("lat"));
        float lng = Float.parseFloat(request.getParameter("lng"));
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute==null || minute.isEmpty()) minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');

        logger.info("进行经纬度" + lat + "," + lng + "附近,时间:" + year + month + day + hour + minute + "数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = stationDataSurfService.getDataFromLatlng(lat, lng, year, month, day, hour, minute);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("站点数据查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    //  @ResponseBody
    @RequestMapping(value = "/datafromcode", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void dataFromCode(HttpServletRequest request, HttpServletResponse response) {

        String strCode = request.getParameter("code");
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute==null || minute.isEmpty()) minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');

        logger.info("进行站号:" + strCode + ",时间:" + year + month + day + hour + minute + "数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = stationDataSurfService.getDataFromCode(strCode, year, month, day, hour, minute);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("站点数据查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/aqidatafromcode", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void AQIDataFromCode(HttpServletRequest request, HttpServletResponse response) {

        String strCode = request.getParameter("code");
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute==null || minute.isEmpty()) minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');

        logger.info("进行站号:" + strCode + ",时间:" + year + month + day + hour + minute + "空气质量数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = stationDataSurfService.getStationAQIData(strCode, year, month, day, hour, minute);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("站点空气质量数据查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/aqidatafromcityid", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void AQIDataFromCityID(HttpServletRequest request, HttpServletResponse response) {

        String strCityID = request.getParameter("cityid");
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute==null || minute.isEmpty()) minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');

        logger.info("进行行政编码:" + strCityID + ",时间:" + year + month + day + hour + minute + "空气质量数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = stationDataSurfService.getCityIDAQIData(strCityID, year, month, day, hour, minute);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("站点空气质量数据查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/aqidistriblist", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void AQIDataDistrib(HttpServletRequest request, HttpServletResponse response) {

        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");
        String strDlevel = request.getParameter("dlevel");
        Integer dlevel = null;
        if (strDlevel != null){
            try{
                dlevel = Integer.parseInt(strDlevel);
            }
            catch (NumberFormatException ex){
                logger.error("给定的dlevel请为整数:" + ex);
                JSONObject resJSON = new JSONObject();
                resJSON.put(ResJsonConst.STATUSCODE, ResStatus.PARAM_ERROR.getStatusCode());
                JSONUtil.writeJSONToResponse(response, resJSON);
                return;
            }
        }

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute==null || minute.isEmpty()) minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');

        logger.info("进行时间:" + year + month + day + hour + minute + "空气质量分布数据查询操作...");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = stationDataSurfService.searchAQIDistList(dlevel, year, month, day, hour, minute);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("空气质量分布数据查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/seqdatafromcode", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void seqDataFromCode(HttpServletRequest request, HttpServletResponse response) {

        String strCode = request.getParameter("code");
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");
        String elem = request.getParameter("elem");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute==null || minute.isEmpty()) minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');

        logger.info("进行站号:" + strCode + ",时间:" + year + month + day + hour + minute + "实况时序数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = stationDataSurfService.getStationSeqDataSurf(strCode, year, month, day, hour, minute, elem);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("站点实况时序数据查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/datafromcname", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void dataFromCname(HttpServletRequest request, HttpServletResponse response) {
        String cname = request.getParameter("cname");
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute==null || minute.isEmpty()) minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');


//        try {
//            cname = new String(cname.getBytes("ISO-8859-1"), "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        logger.info("进行中文名" + cname + "附近,时间:" + year + month + day + hour + minute + "数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = stationDataSurfService.getDataFromCname(cname, year, month, day, hour, minute);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("站点数据查询时失败:" + e);
        }
    }

    @RequestMapping(value = "/datafromselectedstations", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void dataFromAllStation(HttpServletRequest request, HttpServletResponse response) {
        String cname = request.getParameter("cname");
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute==null || minute.isEmpty()) minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');


//        try {
//            cname = new String(cname.getBytes("ISO-8859-1"), "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        logger.info("进行,时间:" + year + month + day + hour + minute + "常用站点地面实况数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = stationDataSurfService.getStationListDataSurf(year, month, day, hour, minute);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("常用站点地面实况数据查询时失败:" + e);
        }
    }

    @RequestMapping("/isolinedata")
    public void getIsoLineData(HttpServletRequest request, HttpServletResponse response) {
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String level = request.getParameter("level");
        String elem = request.getParameter("elem");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');

        long tt = System.currentTimeMillis();
        try {JSONObject resJSON = stationDataSurfService.getIsoLineData(year, month, day, hour, "00", level, elem, null);
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("地面实况等值线数据查询时失败:", e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping("/isosurfacedata")
    public void getIsosurfaceData(HttpServletRequest request, HttpServletResponse response) {
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String level = request.getParameter("level");
        String elem = request.getParameter("elem");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');

        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = stationDataSurfService.getIsosurfaceData(year, month, day, hour, "00", level, elem, null);
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("地面实况等值面数据查询时失败:", e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }




}
