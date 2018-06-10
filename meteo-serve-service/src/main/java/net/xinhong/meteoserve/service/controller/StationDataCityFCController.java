package net.xinhong.meteoserve.service.controller;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.service.StationDataCityFCService;
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
 * Description: <br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 <a href=mailto:liusofttech@sina.com>刘晓昌</a>
 * @version 创建时间：2016/3/11.
 */
@Controller
@RequestMapping(value = "/stationdata_cityfc")
public class StationDataCityFCController {
    private static final Log logger = LogFactory.getLog(StationDataCityFCController.class);

    @Autowired
    private StationDataCityFCService stationDataCityFCService;

    /**
     * 测试页面入口
     *
     * @return
     */
    @RequestMapping(value = "/dataview.do", method = {RequestMethod.POST, RequestMethod.GET}, produces = "text/html;charset=UTF-8")
    public final String surfInfo() {
        return "station/cityfcsearch";
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
        String elem = request.getParameter("elem");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');
        if (minute==null || minute.isEmpty()) minute = StringUtils.leftPad(Integer.toString(curDate.getMinuteOfHour()), 2, '0');

        logger.info("进行经纬度" + lat + "," + lng + "附近,时间:" + year + month + day + hour + ", 要素为" + elem + "数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = stationDataCityFCService.getCityFCFromLatlng(lat, lng, year, month, day, hour, "", elem);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("站点数据查询时失败:" + e);
        }
    }

    @RequestMapping(value = "/datafromcname", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void dataFromCname(HttpServletRequest request, HttpServletResponse response) {
        String cname = request.getParameter("cname");
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String elem = request.getParameter("elem");

        if (year == null || year.isEmpty()) year = "2015";
        if (month == null || month.isEmpty()) month = "10";
        if (day == null || day.isEmpty()) day = "27";
        if (hour == null || hour.isEmpty()) hour = "00";
//        try {
//            cname = new String(cname.getBytes("ISO-8859-1"), "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        logger.info("进行中文名" + cname + "附近,时间:" + year + month + day + hour + ", 要素为" + elem + "数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = stationDataCityFCService.getCityFCFromCname(cname, year, month, day, hour, "", elem);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("站点数据查询时失败:" + e);
        }
    }

    @RequestMapping(value = "/datafromcode", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void dataFromCode(HttpServletRequest request, HttpServletResponse response) {
        String code = request.getParameter("code");
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

//        try {
//            cname = new String(cname.getBytes("ISO-8859-1"), "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        logger.info("进行站号" + code + "附近,时间:" + year + month + day + hour + ", 要素为" + elem + "城镇预报查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = stationDataCityFCService.getCityFCFromCode(code, year, month, day, hour, "", elem);
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
}


