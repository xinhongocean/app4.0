package net.xinhong.meteoserve.service.controller;

import com.alibaba.fastjson.JSONObject;
import com.xinhong.mids3d.globeterrian.GlobeTerrianTool;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.service.RadarCloudFaxService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by xiaoyu on 16/8/8.
 */
@Controller
public class RadarCloudFaxController {
    private static final Log logger = LogFactory.getLog(RadarCloudFaxController.class);

    @Autowired
    private RadarCloudFaxService radarCloudFaxService;

    @RequestMapping(value = "/radarmap/info", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getRadarInfo(HttpServletRequest request, HttpServletResponse response) {

        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");

        DateTime curDate = DateTime.now();
        if (year == null || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month == null || month.isEmpty())
            month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');
        ;
        if (day == null || day.isEmpty()) day = StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour == null || hour.isEmpty())
            hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute == null || minute.isEmpty())
            minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');

        logger.info("进行时间:" + year + month + day + hour + minute + ", 雷达图像信息列表查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = radarCloudFaxService.getRadarInfo("", year, month, day, hour, minute);
            if (resJSON != null)
                resJSON.put("delay", (System.currentTimeMillis() - tt));
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("雷达图像信息列表查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/neareststationradarmap/info", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getNearestStationRadarInfo(HttpServletRequest request, HttpServletResponse response) {

        String lat = request.getParameter("lat");
        String lng = request.getParameter("lng");
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");

        DateTime curDate = DateTime.now();
        if (year == null || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month == null || month.isEmpty())
            month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');
        ;
        if (day == null || day.isEmpty()) day = StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour == null || hour.isEmpty())
            hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute == null || minute.isEmpty())
            minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');

        logger.info("进行时间:" + year + month + day + hour + minute + ", 最近位置单站雷达图像信息查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = radarCloudFaxService.getNearestRadarInfo(lat, lng, year, month, day, hour, minute);
            if (resJSON != null)
                resJSON.put("delay", (System.currentTimeMillis() - tt));
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("最近位置单站雷达图像信息查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/stationradarmap/info", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getStationRadarInfo(HttpServletRequest request, HttpServletResponse response) {

        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");
        String radarIDs = request.getParameter("radarIDs");

        DateTime curDate = DateTime.now();
        if (year == null || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month == null || month.isEmpty())
            month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');
        ;
        if (day == null || day.isEmpty()) day = StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour == null || hour.isEmpty())
            hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute == null || minute.isEmpty())
            minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');

        logger.info("进行时间:" + year + month + day + hour + minute + ", 单站雷达图像信息列表查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = radarCloudFaxService.getStationRadarInfo("", year, month, day, hour, minute, radarIDs);
            if (resJSON != null)
                resJSON.put("delay", (System.currentTimeMillis() - tt));
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("单站雷达图像信息列表查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/stationradarmap/distribinfo", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getStationRadarDistribInfo(HttpServletRequest request, HttpServletResponse response) {


        DateTime curDate = DateTime.now();

        logger.info("进行单站雷达位置分布列表查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = radarCloudFaxService.getStationRadarDistribInfo();
            if (resJSON != null)
                resJSON.put("delay", (System.currentTimeMillis() - tt));
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("单站雷达位置分布列表查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }


    @RequestMapping(value = "/himawari8l1map/info", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getHIMAWARI8L1Info(HttpServletRequest request, HttpServletResponse response) {

        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");


//        二级：image:himawari8l2:clot
//        image:himawari8l2:cltt
//        image:himawari8l2:cltype
//        一级：image:himawari8l1:ir
//        image:himawari8l1:vis
//        image:himawari8l1:irwv

        String channel = request.getParameter("channel");
        String type = DataTypeConst.IMAGE_HIMAWARI8L1IRMAP;
        if ("vis".equals(channel)) {
            type = DataTypeConst.IMAGE_HIMAWARI8L1VISMAP;
        } else if ("irwv".equals(channel)) {
            type = DataTypeConst.IMAGE_HIMAWARI8L1IRWVMAP;
        }


        DateTime curDate = DateTime.now();
        if (year == null || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month == null || month.isEmpty())
            month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');
        ;
        if (day == null || day.isEmpty()) day = StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour == null || hour.isEmpty())
            hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute == null || minute.isEmpty())
            minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');

        logger.info("进行时间:" + year + month + day + hour + minute + ", 葵花8云图一级产品图像信息列表查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = radarCloudFaxService.getCloudInfo(type, year, month, day, hour, minute);
            if (resJSON != null)
                resJSON.put("delay", (System.currentTimeMillis() - tt));
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("葵花8云图一级产品图像信息列表查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/himawari8l2map/info", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getHIMAWARI8L2Info(HttpServletRequest request, HttpServletResponse response) {

        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");
        String channel = request.getParameter("channel");
        String type = DataTypeConst.IMAGE_HIMAWARI8L2CLTHMAP;
        if ("cltype".equals(channel.toLowerCase())) {
            type = DataTypeConst.IMAGE_HIMAWARI8L2CLTYPEMAP;
        } else if ("clot".equals(channel.toLowerCase())) {
            type = DataTypeConst.IMAGE_HIMAWARI8L2CLOTMAP;
        } else if ("cltt".equals(channel.toLowerCase())) {
            type = DataTypeConst.IMAGE_HIMAWARI8L2CLTTMAP;
        } else if ("clth".equals(channel.toLowerCase())) {
            type = DataTypeConst.IMAGE_HIMAWARI8L2CLTHMAP;
        }

        DateTime curDate = DateTime.now();
        if (year == null || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month == null || month.isEmpty())
            month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');

        if (day == null || day.isEmpty()) day = StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour == null || hour.isEmpty())
            hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute == null || minute.isEmpty())
            minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');

        logger.info("进行时间:" + year + month + day + hour + minute + ", 葵花8云图二级产品图像信息列表查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = radarCloudFaxService.getCloudInfo(type, year, month, day, hour, minute);
            if (resJSON != null)
                resJSON.put("delay", (System.currentTimeMillis() - tt));
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("葵花8云图二级产品图像信息列表查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }


    @RequestMapping("/himawari8l2/pointspacedata")
    public void getHimawariSpaceProfileData(HttpServletRequest request, HttpServletResponse response) {
        String strLats = request.getParameter("lat");
        String strLngs = request.getParameter("lng");
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");
        String interploate = request.getParameter("interploate");
        boolean isInterploate = true;
        if (interploate != null && (interploate.toLowerCase().equals("false") || interploate.equals("0"))) {
            isInterploate = false;
        }

        DateTime curDate = DateTime.now().minusMinutes(20); //如果用当前时间，则取前20分钟的产品
        if (year == null || year.isEmpty()) year = curDate.toString("yyyy");
        if (month == null || month.isEmpty()) month = curDate.toString("MM");
        if (day == null || day.isEmpty()) day = curDate.toString("dd");
        if (hour == null || hour.isEmpty()) hour = curDate.toString("HH");
        if (minute == null || minute.isEmpty()) minute = curDate.toString("mm");

        logger.info("进行多个经纬度" + strLats + " " + strLngs + "附近,时间:" + year + month + day + hour +
                minute + ", 葵花八二级产品数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = radarCloudFaxService.getHimawari8L2PointSpaceData(strLats, strLngs, year, month, day, hour, minute, isInterploate);

            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("葵花8二级产品空间剖面数据查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping("/himawari8l2/pointtimedata")
    public void getHimawariTimeProfileData(HttpServletRequest request, HttpServletResponse response) {
        String strLat = request.getParameter("lat");
        String strLng = request.getParameter("lng");
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");

        DateTime curDate = DateTime.now().minusMinutes(20); //如果用当前时间，则取前20分钟的产品
        if (year == null || year.isEmpty()) year = curDate.toString("yyyy");
        if (month == null || month.isEmpty()) month = curDate.toString("MM");
        if (day == null || day.isEmpty()) day = curDate.toString("dd");
        if (hour == null || hour.isEmpty()) hour = curDate.toString("HH");
        if (minute == null || minute.isEmpty()) minute = curDate.toString("mm");

        logger.info("进行经纬度" + strLat + " " + strLng + "附近,时间:" + year + month + day + hour + ", 葵花8过去2个小时时间剖面数据天气数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = radarCloudFaxService.getHimawari8L2PointTimeData(strLat, strLng, year, month, day, hour, minute);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("葵花8二级产品时间剖面数据查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }


    @RequestMapping(value = "/cloudmap/info", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getCloudInfo(HttpServletRequest request, HttpServletResponse response) {

        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");

        DateTime curDate = DateTime.now();
        if (year == null || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month == null || month.isEmpty())
            month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');
        ;
        if (day == null || day.isEmpty()) day = StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour == null || hour.isEmpty())
            hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute == null || minute.isEmpty())
            minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');

        logger.info("进行时间:" + year + month + day + hour + minute + ", 云图图像信息列表查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = radarCloudFaxService.getCloudInfo("", year, month, day, hour, minute);
            if (resJSON != null)
                resJSON.put("delay", (System.currentTimeMillis() - tt));
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("云图图像信息列表查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }


    @RequestMapping(value = "/wxfaxmap/info", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getFaxInfo(HttpServletRequest request, HttpServletResponse response) {

        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");

        DateTime curDate = DateTime.now();
        if (year == null || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month == null || month.isEmpty())
            month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');
        ;
        if (day == null || day.isEmpty()) day = StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour == null || hour.isEmpty())
            hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute == null || minute.isEmpty())
            minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');

        logger.info("进行时间:" + year + month + day + hour + minute + ", 传真图信息列表查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = radarCloudFaxService.getFaxInfo(year, month, day, hour, minute);
            if (resJSON != null)
                resJSON.put("delay", (System.currentTimeMillis() - tt));
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("传真图信息列表查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/ecmwfmap/info", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getECMWFMapInfo(HttpServletRequest request, HttpServletResponse response) {
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");

        DateTime curDate = DateTime.now();
        if (year == null || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month == null || month.isEmpty())
            month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');
        ;
        if (day == null || day.isEmpty()) day = StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour == null || hour.isEmpty())
            hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute == null || minute.isEmpty())
            minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');

        logger.info("进行时间:" + year + month + day + hour + minute + ", 欧洲数值预报图片信息列表查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = radarCloudFaxService.getECMWFImageInfo(year, month, day, hour, minute);
            if (resJSON != null)
                resJSON.put("delay", (System.currentTimeMillis() - tt));
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("欧洲数值预报图片信息列表查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }


//    @RequestMapping(value = "/OceanFC/info", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
//    public final void getImocwxWaveInfo(HttpServletRequest request, HttpServletResponse response) {
//        String year = request.getParameter("year");
//        String month = request.getParameter("month");
//        String day = request.getParameter("day");
//        String hour = request.getParameter("hour");
//        String minute = request.getParameter("minute");
//        String type = request.getParameter("type");
//        DateTime curDate = DateTime.now();
//        if (year == null || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
//        if (month == null || month.isEmpty())
//            month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');
//        ;
//        if (day == null || day.isEmpty()) day = StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
//        if (hour == null || hour.isEmpty())
//            hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
//        if (minute == null || minute.isEmpty())
//            minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');
//
//        logger.info("进行时间:" + year + month + day + hour + minute + ", 海洋预报图片列表查询....");
//        long tt = System.currentTimeMillis();
//        try {
//            JSONObject resJSON = radarCloudFaxService.getOceanFCImageInfo(type, year, month, day, hour, minute);
//            if (resJSON != null)
//                resJSON.put("delay", (System.currentTimeMillis() - tt));
//            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
//            JSONUtil.writeJSONToResponse(response, resJSON);
//        } catch (Exception e) {
//            logger.error("日本波浪预报图片列表查询失败:" + e);
//            JSONObject resJSON = new JSONObject();
//            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
//            JSONUtil.writeJSONToResponse(response, resJSON);
//        }
//    }


    @RequestMapping(value = "/JapanOceanFC/info", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getJPOceanFC(HttpServletRequest request, HttpServletResponse response) {
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");
        DateTime curDate = DateTime.now();
        if (year == null || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month == null || month.isEmpty())
            month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');
        ;
        if (day == null || day.isEmpty()) day = StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour == null || hour.isEmpty())
            hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute == null || minute.isEmpty())
            minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');

        logger.info("进行时间:" + year + month + day + hour + minute + ", 海洋预报图片列表查询....");
        long tt = System.currentTimeMillis();
        JSONObject resJSON = new JSONObject();
        try {
//            String[] types = new String[]{"imocwxwave", "imocwxpredict24",
//                    "imocwxpredict48", "imocwxPRCP", "imocwxground", "imocwxHigh",
//                    "imocwx850_700hpa", "imocwx500_300hpa"};
            String[] types = new String[]{"imocwxwave"};
//            String[] chnnames = new String[]{"日本海浪预报", "日本24小时预报", "日本48小时预报",
//                    "日本降水预报", "日本地面预报", "日本高层预报", "日本850-700hpa层预报", "日本500-300hpa层预报"};
            String[] chnnames = new String[]{"海浪预报"};
            JSONObject dataJSON = new JSONObject();
            for (int i = 0; i < types.length; i++) {
                String type = types[i];
                JSONObject tmpJson = radarCloudFaxService.getOceanFCImageInfo(type, year, month, day, hour, minute);
                if (tmpJson != null && tmpJson.get(ResJsonConst.DATA) != null)
                    dataJSON.put(chnnames[i] + "_" + tmpJson.get(ResJsonConst.TIME), tmpJson.get(ResJsonConst.DATA));
            }
            resJSON.put(ResJsonConst.DATA, dataJSON);
            if (resJSON != null)
                resJSON.put("delay", (System.currentTimeMillis() - tt));
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SUCCESSFUL.getStatusCode());
            resJSON.put(ResJsonConst.TIME, curDate.toString("yyyyMMdd"));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("日本波浪预报图片列表查询失败:" + e);
            resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }

    }

    @RequestMapping(value = "/ChinaOceanFC/info", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getCHOceanFC(HttpServletRequest request, HttpServletResponse response) {
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");
        DateTime curDate = DateTime.now();
        if (year == null || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month == null || month.isEmpty())
            month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');
        ;
        if (day == null || day.isEmpty()) day = StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour == null || hour.isEmpty())
            hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute == null || minute.isEmpty())
            minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');

        logger.info("进行时间:" + year + month + day + hour + minute + ", 海洋预报图片列表查询....");
        long tt = System.currentTimeMillis();
        JSONObject resJSON = new JSONObject();
        try {
            String[] types = new String[]{"nmefccur", "nmefcsalt", "nmefcwave",
                    "nmefcwind", "nmefctemp", "nmefcrealtime"};
//            String[] chnnames = new String[]{"海洋局海流预报", "海洋局盐度预报", "海洋局风浪预报",
//                    "海洋局海面风预报","海洋局海表温度预报","西北太平洋海浪、表层海温实况"};
            String[] chnnames = new String[]{"表层海流预报", "表层盐度预报", "海浪预报",
                    "海面风预报", "表层海温预报", "西北太平洋海浪、表层海温实况"};
            JSONObject dataJSON = new JSONObject();
            for (int i = 0; i < types.length; i++) {
                String type = types[i];
                JSONObject tmpJson = radarCloudFaxService.getOceanFCImageInfo(type, year, month, day, hour, minute);
                if (tmpJson != null && tmpJson.get(ResJsonConst.DATA) != null)
                    if (i < 5)
                        dataJSON.put(chnnames[i] + "_" + tmpJson.get(ResJsonConst.TIME), tmpJson.get(ResJsonConst.DATA));
                    else
                        dataJSON.put(chnnames[i], tmpJson.get(ResJsonConst.DATA));
            }
            resJSON.put(ResJsonConst.DATA, dataJSON);
            if (resJSON != null)
                resJSON.put("delay", (System.currentTimeMillis() - tt));
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SUCCESSFUL.getStatusCode());
            resJSON.put(ResJsonConst.TIME, curDate.toString("yyyyMMdd"));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("日本波浪预报图片列表查询失败:" + e);
            resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }

    }


    @RequestMapping(value = "/CoastRegionFC/info", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getCoastRegionFCInfo(HttpServletRequest request, HttpServletResponse response) {
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");
        String type = request.getParameter("type");
        String lng = request.getParameter("lng");
        String lat = request.getParameter("lat");
        DateTime curDate = DateTime.now();
        if (year == null || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month == null || month.isEmpty())
            month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');
        ;
        if (day == null || day.isEmpty()) day = StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour == null || hour.isEmpty())
            hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute == null || minute.isEmpty())
            minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');

        if (lat == null || lng == null || (lat != null && !lat.matches("(-\\d+)?(.\\d+)?")) ||
                (lng != null && !lng.matches("(\\d+)?(.\\d+)?"))) {
            logger.error("经纬度输入错误或未输入经纬度");
        }
        String region = null;
        try {
            region = GlobeTerrianTool.getChinaSeaIDName(Float.valueOf(lat), Float.valueOf(lng));
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("进行时间:" + year + month + day + hour + minute + ", 近海预报图片列表查询....");
        long tt = System.currentTimeMillis();
        try {
            if (region == null) {
                JSONObject resJSON = new JSONObject();
                resJSON.put(ResJsonConst.STATUSCODE, ResStatus.NORESULT.getStatusCode());
                JSONUtil.writeJSONToResponse(response, resJSON);
                return;
            }
            JSONObject resJSON = radarCloudFaxService.getCoastRegionFC(region, year, month, day, hour, minute);
            if (resJSON != null)
                resJSON.put("delay", (System.currentTimeMillis() - tt));
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("近海预报预报图片列表查询失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }


    @RequestMapping(value = "/jppm2dot5fc/info", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getJPPM2Dot5FCInfo(HttpServletRequest request, HttpServletResponse response) {
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");


        DateTime curDate = DateTime.now();
        if (year == null || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month == null || month.isEmpty())
            month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');
        ;
        if (day == null || day.isEmpty()) day = StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour == null || hour.isEmpty())
            hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute == null || minute.isEmpty())
            minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');

        logger.info("进行时间:" + year + month + day + hour + minute + ", 日本PM2.5预报趋势图信息列表查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = radarCloudFaxService.getJPPM2Dot5FCInfo("", year, month, day, hour, minute);
            if (resJSON != null)
                resJSON.put("delay", (System.currentTimeMillis() - tt));
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("日本PM2.5预报趋势图信息列表查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }
}
