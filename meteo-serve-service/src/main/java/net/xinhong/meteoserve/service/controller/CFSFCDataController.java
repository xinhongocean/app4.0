package net.xinhong.meteoserve.service.controller;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.service.CFSFCDataService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by xiaoyu on 16/9/19.
 */
public class CFSFCDataController {
    private static final Log logger = LogFactory.getLog(GFSFCDataController.class);

    @Autowired
    private CFSFCDataService cfsfcDataService;

    @RequestMapping(value = "/datafromlatlng", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void dataFromLatLng(HttpServletRequest request, HttpServletResponse response) {
        float lat = Float.parseFloat(request.getParameter("lat"));
        float lng = Float.parseFloat(request.getParameter("lng"));
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String yearFC = request.getParameter("yearFC");   //预报年份
        String monthFC = request.getParameter("monthFC"); //预报月份

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');

        logger.info("进行经纬度" + lat + "," + lng + "附近,时间:" + year + month + day + hour  + ", 预测"
                + year + "年" + month + "月数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = cfsfcDataService.getDataFromLatlng(lat, lng, year, month, day, hour, yearFC, monthFC);
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
