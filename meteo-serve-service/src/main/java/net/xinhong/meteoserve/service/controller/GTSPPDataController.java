package net.xinhong.meteoserve.service.controller;

/**
 * Created by wingsby on 2018/1/8.
 */

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.service.GTSPPDataService;
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
@RequestMapping(value = "/gtspp")
public class GTSPPDataController {
    private static final Log logger = LogFactory.getLog(GTSPPDataController.class);
    @Autowired
    private GTSPPDataService gtsppDataService;

    @RequestMapping(value = "/pointdata", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getPointData(HttpServletRequest request, HttpServletResponse response) {
//        String strLats = request.getParameter("lat");
//        String strLngs = request.getParameter("lng");
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null   || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');




        logger.info("海域,时间:" + year + month + day  + ", GTSPP数据查询....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = gtsppDataService.getPointData(year, month, day);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("单点GTSPP数据查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }
}

