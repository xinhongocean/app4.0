package net.xinhong.meteoserve.service.controller;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.service.service.AirPortInfoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 机场实况站点信息查询
 * Created by xiaoyu on 16/6/12.
 */
@Controller
@RequestMapping(value = "/airport")
public class AirPortInfoController {
    private static final Log logger = LogFactory.getLog(AirPortInfoController.class);


//    @Autowired
    private AirPortInfoService airportInfoService;

    @RequestMapping(value = "/infofromnameicao3icao4", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void dataFromCname(HttpServletRequest request, HttpServletResponse response) {

        String param = request.getParameter("param");
        logger.info("进行机场站点信息模糊查询:" + param);
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = airportInfoService.getStationInfoFromNameIcao3Icao4(param);
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("机场站点信息模糊查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
           // resJSON.put(ResJsonConst.STATUSMSG, e);
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/infofromlatlng", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void dataFromLatlng(HttpServletRequest request, HttpServletResponse response) {

        float lat = Float.parseFloat(request.getParameter("lat"));
        float lng = Float.parseFloat(request.getParameter("lng"));
        logger.info("进行机场站点信息按经纬度查询:" + lat + "," + lng);
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = airportInfoService.getStationInfoFromLatLng(lat, lng);
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("机场站点信息按经纬度查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }


    @RequestMapping(value = "/waypointfromname", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getWaypointFromName(HttpServletRequest request, HttpServletResponse response) {

        String wayPointName = request.getParameter("name");

        logger.info("进行按照航路点名称" + wayPointName + "详细信息查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = airportInfoService.getWaypointFromName(wayPointName);
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("航路点信息查询时失败:" + e);
        }
    }


    @RequestMapping(value = "/waypointsfromrouteidenty", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getWaypointsFromAirlineName(HttpServletRequest request, HttpServletResponse response) {

        String identy = request.getParameter("identy");

        logger.info("进行按照航线名称" + identy + "查询航路点列表查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = airportInfoService.getWaypointsFromRouteIdent(identy);
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("航路点列表信息查询时失败:" + e);
        }
    }

}
