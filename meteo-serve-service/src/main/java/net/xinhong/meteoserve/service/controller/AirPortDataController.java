package net.xinhong.meteoserve.service.controller;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.service.AirPortDataSurfService;
import net.xinhong.meteoserve.service.service.AirPortInfoService;
import net.xinhong.meteoserve.service.service.StationDataSurfService;
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
 * Created by xiaoyu on 16/6/12.
 */
@Controller
@RequestMapping(value = "/airportdata_surf")
public class AirPortDataController {
    private static final Log logger = LogFactory.getLog(AirPortDataController.class);

//    @Autowired
    private AirPortDataSurfService airPortDataSurfService;
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

        logger.info("进行机场实况站号:" + strCode + ",时间:" + year + month + day + hour + minute + "数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = airPortDataSurfService.getDataFromCode(strCode, year, month, day, hour, minute);
            logger.debug("机场实况查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("机场实况站点数据查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/seqdatafromcode", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void seqDataFromCode(HttpServletRequest request, HttpServletResponse response) {
        String strCode = request.getParameter("code");
        String year    = request.getParameter("year");
        String month   = request.getParameter("month");
        String day     = request.getParameter("day");
        String hour   = request.getParameter("hour");
        String minute = request.getParameter("minute");
        String elem = request.getParameter("elem");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty())  year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty())    day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null || hour.isEmpty())   hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute==null || minute.isEmpty()) minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');

        logger.info("进行机场实况站号:" + strCode + ",时间:" + year + month + day + hour + minute + "时序数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = airPortDataSurfService.getAirPortSeqDataSurf(strCode, year, month, day, hour, minute, elem);
            logger.debug("机场实况时序查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("机场实况站点时序数据查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }


    @RequestMapping(value = "/sigmentdatafromcode", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void sigmentDataFromCode(HttpServletRequest request, HttpServletResponse response) {
        String strCode = request.getParameter("code");
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String minute = request.getParameter("minute");
        String sigmenttype = request.getParameter("sigmenttype");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute==null || minute.isEmpty()) minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');

        logger.info("进行机场实况站号:" + strCode + ",时间:" + year + month + day + hour + minute + "实况及预报数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = airPortDataSurfService.getAirPortSigmentData(strCode, sigmenttype, year, month, day, hour, minute);
            logger.debug("机场实况及预报详细信息查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("机场实况及预报详细信息查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/sigmentdataindexs", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void sigmentDataIndexs(HttpServletRequest request, HttpServletResponse response) {
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

        logger.info("进行机场时间:" + year + month + day + hour + minute + "实况及预报分布索引信息查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = airPortDataSurfService.getAirPortSigmentDataIndexs(year, month, day, hour, minute, false);
            logger.debug("机场实况索引查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("机场实况及预报分布索引信息查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }


    @RequestMapping(value = "/sigmentdataindexslevel", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void sigmentDataIndexsLevel(HttpServletRequest request, HttpServletResponse response) {
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

        logger.info("进行机场时间:" + year + month + day + hour + minute + "实况及预报分布索引信息查询操作(含等级)....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = airPortDataSurfService.getAirPortSigmentDataIndexs(year, month, day, hour, minute, true);
            logger.debug("机场实况索引(含等级)查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("机场实况及预报分布索引(含等级)信息查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }


}
