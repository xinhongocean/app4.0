package net.xinhong.meteoserve.service.controller;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.service.GFSFCDataService;
import net.xinhong.meteoserve.service.service.TyphStatisticDataService;
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
@RequestMapping(value = "/typhstatistic")
public class TyphStatisticDataController {
    private static final Log logger = LogFactory.getLog(TyphStatisticDataController.class);

    @Autowired
    private TyphStatisticDataService typhStatisticDataService;

    @RequestMapping(value = "/pointdata", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getPointData(HttpServletRequest request, HttpServletResponse response) {
        String lat = request.getParameter("lat");
        String lng = request.getParameter("lng");
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String table = request.getParameter("table");
        logger.info("进行经纬度" + lat + "," + lng + "附近,时间:" + year==null?"累年":year
                + month==null?"":(month+"月") +", 台风统计数据查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = typhStatisticDataService.getPointData(lat, lng, year, month,table);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("台风统计数据查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }
}
