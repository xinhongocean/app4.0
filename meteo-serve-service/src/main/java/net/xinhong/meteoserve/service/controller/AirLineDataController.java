package net.xinhong.meteoserve.service.controller;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.service.service.AirLineDataService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by xiaoyu on 16/4/19.
 */
@Controller
@RequestMapping(value = "/airline")
public class AirLineDataController {
    private static final Log logger = LogFactory.getLog(AirLineDataController.class);

    @Autowired
    private AirLineDataService airLineDataService;


    @RequestMapping(value = "/flightfromumber", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getFlightFromNumber(HttpServletRequest request, HttpServletResponse response) {

        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String flightNumber = request.getParameter("number");

        if (year==null || year.isEmpty()) year = "2015";
        if (month==null || month.isEmpty()) month = "09";
        if (day==null || day.isEmpty()) day = "07";


        logger.info("进行航班号" + flightNumber + ",时间:" + year + month + day + "航班信息查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = airLineDataService.getFlightFromNumber(flightNumber, year, month, day);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("航班信息查询时失败:" + e);
        }
    }

    @RequestMapping(value = "/flightfromdeptarr", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getFlightFromDeptArr(HttpServletRequest request, HttpServletResponse response) {

        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String deptAptCode = request.getParameter("dept");
        String arrAptCode = request.getParameter("arr");

        if (year==null || year.isEmpty()) year = "2015";
        if (month==null || month.isEmpty()) month = "09";
        if (day==null || day.isEmpty()) day = "11";

        logger.info("进行起降机场" + deptAptCode + "," + arrAptCode + ",时间:" + year + month + day + "航班信息查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = airLineDataService.getFlightFromDeptArr(deptAptCode, arrAptCode, year, month, day);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("航班信息查询时失败:" + e);
        }
    }

    @RequestMapping(value = "/airlinelistfromdeptarr", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getAirLineNameListFromDeptArr(HttpServletRequest request, HttpServletResponse response) {

        String deptAptCode = request.getParameter("dept");
        String arrAptCode = request.getParameter("arr");


        logger.info("进行起降机场" + deptAptCode + "," + arrAptCode + ",航线信息列表查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = airLineDataService.getAirLineNameListFromDeptArr(deptAptCode, arrAptCode);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("航线列表信息查询时失败:" + e);
        }
    }


    @RequestMapping(value = "/airlinefromname", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getAirLineFromName(HttpServletRequest request, HttpServletResponse response) {

        String airLineName = request.getParameter("name");

        logger.info("进行航线" + airLineName + "详细信息查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = airLineDataService.getAirLineFromName(airLineName);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("航线信息查询时失败:" + e);
        }
    }

}
