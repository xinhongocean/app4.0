package net.xinhong.meteoserve.service.controller;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.service.service.WNIFCDataService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by xiaoyu on 16/4/19.
 */
@Controller
@RequestMapping(value = "/wni")
public class WNIFCDataController {
    private static final Log logger = LogFactory.getLog(WNIFCDataController.class);

    @Autowired
    private WNIFCDataService wnifcDataService;


    @RequestMapping(value = "/pointsdangerdata", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getPointsDangerData(HttpServletRequest request, HttpServletResponse response) {
        String strLat = request.getParameter("lat");
        String strLng = request.getParameter("lng");
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String VTI = request.getParameter("vti");

        if (year == null || year.isEmpty()) year = "2016";
        if (month == null || month.isEmpty()) month = "06";
        if (day == null || day.isEmpty()) day = "12";
        if (hour == null || hour.isEmpty()) hour = "08";
        if (VTI == null || VTI.isEmpty()) VTI = "06";

        VTI = String.format("%03d", Integer.parseInt(VTI));

        logger.info("进行多经纬度" + strLat + " " + strLng + "附近,时间:" + year + month + day + hour + VTI + ", WNI危险天气数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = wnifcDataService.getPointsData(strLat, strLng, year, month, day, hour, VTI);


            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("WNI危险多点天气数据查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/pointdangerdata", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getPointDangerData(HttpServletRequest request, HttpServletResponse response) {
        float lat = Float.parseFloat(request.getParameter("lat"));
        float lng = Float.parseFloat(request.getParameter("lng"));
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String VTI = request.getParameter("vti");

        if (year == null || year.isEmpty()) year = "2016";
        if (month == null || month.isEmpty()) month = "06";
        if (day == null || day.isEmpty()) day = "12";
        if (hour == null || hour.isEmpty()) hour = "08";
        if (VTI == null || VTI.isEmpty()) VTI = "06";

        VTI = String.format("%03d", Integer.parseInt(VTI));

        logger.info("进行经纬度" + lat + "," + lng + "附近,时间:" + year + month + day + hour + VTI + ", WNI危险天气数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = wnifcDataService.getPointDangerData(lat, lng, year, month, day, hour, VTI);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("WNI危险天气数据查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/areaData")
    public void getAreaData(HttpServletRequest request, HttpServletResponse response) {

        float sLat = Float.parseFloat(request.getParameter("sLat"));
        float eLat = Float.parseFloat(request.getParameter("eLat"));
        float sLng = Float.parseFloat(request.getParameter("sLng"));
        float eLng = Float.parseFloat(request.getParameter("eLng"));

        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String vti = request.getParameter("vti");
        String type = request.getParameter("type");
        String height = request.getParameter("height");
        String elem = request.getParameter("elem");


        year = year == null || "".equals(year.trim()) ? "2016" : year;
        month = month == null || "".equals(month.trim()) ? "06" : month;
        day = day == null || "".equals(day.trim()) ? "12" : day;
        hour = hour == null || "".equals(hour.trim()) ? "08" : hour;
        vti = vti == null || "".equals(vti.trim()) ? "06" : vti;
        elem = elem == null || "".equals(elem.trim()) ? "CAT" : elem;
        type = type == null || "".equals(type.trim()) ? "MAX" : type;

        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = wnifcDataService.getAreaData(year, month, day, hour, vti, type, height, sLat, eLat, sLng, eLng, elem);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("WNI危险天气数据查询时失败:", e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping("/isoLineData")
    public void getIsoLineData(HttpServletRequest request, HttpServletResponse response) {
        String freeArea = "EN";
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String vti = request.getParameter("vti");
        String height = request.getParameter("height");
        String elem = request.getParameter("elem");
        year = year == null || "".equals(year.trim()) ? "2016" : year;
        month = month == null || "".equals(month.trim()) ? "06" : month;
        day = day == null || "".equals(day.trim()) ? "12" : day;
        hour = hour == null || "".equals(hour.trim()) ? "08" : hour;
        vti = vti == null || "".equals(vti.trim()) ? "006" : vti;
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = wnifcDataService.getIsoLineData(freeArea, year, month, day, hour, vti, height, elem);
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("WNI等值线数据查询时失败:", e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping("/isoLineDataGzip")
    @ResponseBody
    public JSONObject getIsoLineDataGzip(HttpServletRequest request, HttpServletResponse response) {
        String freeArea = "EN";
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String vti = request.getParameter("vti");
        String height = request.getParameter("height");
        String elem = request.getParameter("elem");
        year = year == null || "".equals(year.trim()) ? "2016" : year;
        month = month == null || "".equals(month.trim()) ? "06" : month;
        day = day == null || "".equals(day.trim()) ? "12" : day;
        hour = hour == null || "".equals(hour.trim()) ? "08" : hour;
        vti = vti == null || "".equals(vti.trim()) ? "00" : vti;
        response.setContentType("text/html;charset=utf-8");
        System.out.println("aaAAa");

        JSONObject resJSON = wnifcDataService.getIsoLineData(freeArea, year, month, day, hour, vti, height, elem);


        return resJSON;

    }
}
