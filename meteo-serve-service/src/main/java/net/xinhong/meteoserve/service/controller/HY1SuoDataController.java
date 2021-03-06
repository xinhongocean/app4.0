package net.xinhong.meteoserve.service.controller;

import com.alibaba.fastjson.JSONObject;
import com.xinhong.mids3d.globeterrian.GlobeTerrianTool;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.service.HY1SuoDataService;
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
 * Created by wingsby on 2017/12/28.
 */
@Controller
@RequestMapping(value = "/hy1Suo")
public class HY1SuoDataController {
    private static final Log logger = LogFactory.getLog(HY1SuoDataController.class);
    @Autowired
    private HY1SuoDataService HY1SuoDataService;

    @RequestMapping(value = "/pointdata", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getPointData(HttpServletRequest request, HttpServletResponse response) {
        String strLats = request.getParameter("lat");
        String strLngs = request.getParameter("lng");
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null   || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null  || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if((strLats==null||!strLats.matches("-?\\d+?(.\\d+)?"))||(strLngs==null||!strLngs.matches("-?\\d+?(.\\d+)?"))){
            JSONObject tjson=new JSONObject();
            tjson.put(ResJsonConst.STATUSCODE, ResStatus.PARAM_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, tjson);
            return;
        }
        logger.info("进行单个经纬度" + strLats + " " + strLngs + "附近,时间:" + year + month + day + hour + ", HY1Suo数据查询....");
        try{
           boolean flag = GlobeTerrianTool.isOcean(Float.valueOf(strLats), Float.valueOf(strLngs));
           if(!flag){
               JSONObject tjson=new JSONObject();
               tjson.put(ResJsonConst.STATUSCODE, ResStatus.PARAM_ERROR.getStatusCode());
               tjson.put(ResJsonConst.STATUSMSG,"查询数据为陆地，无数据");
               JSONUtil.writeJSONToResponse(response, tjson);
               return;
           }
        }catch (Exception e){
            logger.error("海陆区域读取失败");
        }

        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = HY1SuoDataService.getPointData(strLats, strLngs, year, month, day, hour);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("单点HY1Suo数据查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }
    @RequestMapping(value = "/img", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getImg(HttpServletRequest request, HttpServletResponse response){
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String depth=request.getParameter("depth");
        String eles=request.getParameter("eles");
        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null   || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null  || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if(depth==null||depth.isEmpty())depth="1";
        if(eles==null||eles.isEmpty())eles="TT";

        logger.info("进行时间:" + year + month + day + hour + ", HY1Suo图查询....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = HY1SuoDataService.getImg(year, month, day, hour,depth,eles);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("HY1Suo图查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }

    }


    @RequestMapping("/spaceprofiledata")
    public void getSpaceProfileData(HttpServletRequest request, HttpServletResponse response) {
        String strLats = request.getParameter("lat");
        String strLngs = request.getParameter("lng");
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null   || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null  || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');

        logger.info("进行多个经纬度" + strLats + " " + strLngs + "附近,时间:" + year + month + day + hour + ", HY1Suo空间剖面数据天气数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = HY1SuoDataService.getSpaceProfileData(strLats, strLngs, year, month, day, hour, "00");
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("HY1Suo空间剖面天气数据查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping("/timeprofiledata")
    public void getTimeProfileData(HttpServletRequest request, HttpServletResponse response) {
        String strLat = request.getParameter("lat");
        String strLng = request.getParameter("lng");
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null   || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null  || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');

        logger.info("进行经纬度" + strLat + " " + strLng + "附近,时间:" + year + month + day + hour + ", HY1Suo未来时间剖面数据天气数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = HY1SuoDataService.getTimeProfileData(strLat, strLng, year, month, day, hour, "00");
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("HY1Suo时间剖面天气数据查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/areadata")
    public void getAreaData(HttpServletRequest request, HttpServletResponse response) {
        float sLat = 0;
        String sLatStr = request.getParameter("sLat");
        if (sLatStr != null){
            sLat = Float.parseFloat(sLatStr);
        }
        float eLat = 0;
        String eLatStr = request.getParameter("eLat");
        if (eLatStr != null){
            eLat = Float.parseFloat(eLatStr);
        }
        float sLng = 0;
        String sLngStr = request.getParameter("sLng");
        if (sLngStr != null){
            sLng = Float.parseFloat(sLngStr);
        }
        float eLng = 0;
        String eLngStr = request.getParameter("eLng");
        if (eLngStr != null){
            eLng = Float.parseFloat(eLngStr);
        }

        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String elem = request.getParameter("elem");
        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (elem==null) elem="MWD";
        elem=elem.toUpperCase();
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = HY1SuoDataService.getAreaData(year, month, day, hour, "00",  sLat, eLat, sLng, eLng, elem);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("GFS区域天气数据查询时失败:", e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }



    @RequestMapping("/isolinedata")
    public void getIsoLineData(HttpServletRequest request, HttpServletResponse response) {
        String strArea = "EN";
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String elem = request.getParameter("elem");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if(elem==null)elem="WH";
        elem=elem.toUpperCase();
        long tt = System.currentTimeMillis();
        try {JSONObject resJSON = HY1SuoDataService.getIsoLineData(year, month, day, hour, "00", elem, strArea);
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("HY1所等值线数据查询时失败:", e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }


}
