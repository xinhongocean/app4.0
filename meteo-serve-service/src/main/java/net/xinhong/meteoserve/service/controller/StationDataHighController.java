package net.xinhong.meteoserve.service.controller;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.service.StationDataHighService;
import net.xinhong.meteoserve.service.service.StationDataSurfService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Description: <br>
 *
 * @author 作者 <a href=ds.lht@163.com>stone</a>
 * @version 创建时间：2016/5/17.
 */
@Controller
@RequestMapping("/stationdata_high")
public class StationDataHighController {

    Logger logger = LoggerFactory.getLogger(StationDataHighController.class);

    @Resource
    StationDataHighService stationDataHighService;

    @Resource
    StationDataSurfService stationDataSurfService;

    //  @ResponseBody
    @RequestMapping(value = "/datafromcode", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void dataFromCode(HttpServletRequest request, HttpServletResponse response) {
        String strCode = request.getParameter("code");
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');


        logger.info("进行站号:" + strCode + ",时间:" + year + month + day + hour  + "高空实况数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = stationDataHighService.getDataFromCode(strCode, year, month, day, hour);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("站点高空实况数据查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/timeprofilefromcode", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void timeProfileFromCode(HttpServletRequest request, HttpServletResponse response){
        String strCode = request.getParameter("code");
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');

        logger.info("进行站号:" + strCode + ",时间:" + year + month + day + hour  + "高空实况时间剖面数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = stationDataHighService.getStationDataHighTimeProfile(strCode, year, month, day, hour);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("站点高空实况剖面数据查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }


    @RequestMapping(value = "/indexfromcode", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void indexFromCode(HttpServletRequest request, HttpServletResponse response){
        String strCode = request.getParameter("code");
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');

        logger.info("进行站号:" + strCode + ",时间:" + year + month + day + hour  + "高空实况时间指数数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = stationDataHighService.getStationDataHighIndex(strCode, year, month, day, hour);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("站点高空实况指数数据查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
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

        logger.info("进行,时间:" + year + month + day + hour + minute + "常用站点高空实况数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = stationDataHighService.getStationListDataHigh(year, month, day, hour, minute);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("常用站点高空实况数据查询时失败:" + e);
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
            logger.error("高空实况等值线数据查询时失败:", e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping("/isosurfacedata")
    public void getIsosurfaceData(HttpServletRequest request, HttpServletResponse response) {
        String strArea = "EN";
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
            JSONObject resJSON = stationDataSurfService.getIsosurfaceData(year, month, day, hour, "00", level, elem, strArea);
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("高空实况等值面数据查询时失败:", e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

//    @RequestMapping("/datafromlatlng")
//    public void dataFromLatLng(HttpServletRequest request, HttpServletResponse response) {
//        float lat = Float.parseFloat(request.getParameter("lat"));
//        float lng = Float.parseFloat(request.getParameter("lng"));
//
//        String year = request.getParameter("year");
//        String month = request.getParameter("month");
//        String day = request.getParameter("day");
//        String hour = request.getParameter("hour");
//        String minute = request.getParameter("minute");
//
////        if (year == null || year.isEmpty()) year = "2016";
////        if (month == null || month.isEmpty()) month = "03";
////        if (day == null || day.isEmpty()) day = "15";
////        if (hour == null || hour.isEmpty()) hour = "08";
////        if (minute == null || minute.isEmpty()) minute = "55";
//        DateTime curDate = DateTime.now();
//        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
//        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
//        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
//        if (hour==null || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
//        if (minute==null || minute.isEmpty()) minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');
//
//
//        try {
//            logger.info("进行经纬度" + lat + "," + lng + "附近,时间:" + year + month + day + hour + minute + "数据查询操作....");
//            long tt = System.currentTimeMillis();
//            JSONObject resJSON = stationDataHighService.getSunRiseSetFromLatlng(lat, lng, year, month, day, hour);
//            logger.debug("查询耗时:{}", (System.currentTimeMillis() - tt));
//            JSONUtil.writeJSONToResponse(response, resJSON);
//        } catch (Exception e) {
//            logger.error("站点数据查询时失败:" + e);
//        }
//    }
}
