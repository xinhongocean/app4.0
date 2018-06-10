package net.xinhong.meteoserve.service.controller;

import com.alibaba.fastjson.JSONObject;
import com.xinhong.mids3d.globeterrian.GlobeTerrianTool;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.service.HYCOMDataService;
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
@RequestMapping(value = "/hycom")
public class HYCOMDataController {
    private static final Log logger = LogFactory.getLog(HYCOMDataController.class);
    @Autowired
    private HYCOMDataService hycomDataService;

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

        logger.info("进行单个经纬度" + strLats + " " + strLngs + "附近,时间:" + year + month + day + hour + ", HYCOM数据查询....");
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
            JSONObject resJSON = hycomDataService.getPointData(strLats, strLngs, year, month, day, hour);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("单点HYCOM数据查询时失败:" + e);
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
        if(depth==null||depth.isEmpty())depth="0";
        if(eles==null||eles.isEmpty())eles="watertemp";

        logger.info("进行时间:" + year + month + day + hour + ", HYCOM图查询....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = hycomDataService.getImg(year, month, day, hour,depth,eles);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("HYCOM图查询时失败:" + e);
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

        logger.info("进行多个经纬度" + strLats + " " + strLngs + "附近,时间:" + year + month + day + hour + ", HYCOM空间剖面数据天气数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = hycomDataService.getSpaceProfileData(strLats, strLngs, year, month, day, hour, "00");
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("HYCOM空间剖面天气数据查询时失败:" + e);
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

        logger.info("进行经纬度" + strLat + " " + strLng + "附近,时间:" + year + month + day + hour + ", HYCOM未来时间剖面数据天气数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = hycomDataService.getTimeProfileData(strLat, strLng, year, month, day, hour, "00");
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("HYCOM时间剖面天气数据查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }
}
