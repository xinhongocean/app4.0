package net.xinhong.meteoserve.service.controller;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.service.service.ICOADSStatisticService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by wingsby on 2017/12/25.
 */
@Controller
@RequestMapping(value = "/icoads")
public class ICOADSStatisticController {
    private static final Log logger = LogFactory.getLog(ICOADSStatisticController.class);

    @Autowired
    private ICOADSStatisticService icoadsStatisticService;

    @RequestMapping(value = "/pointdata/", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getPointData(HttpServletRequest request, HttpServletResponse response) {
        //注意统计类型为表名+要素名
        String strLats = request.getParameter("lat");
        String strLngs = request.getParameter("lng");
        String month = request.getParameter("month");
        String table=request.getParameter("table");
        String fourth=request.getParameter("fourth");
        String ele=request.getParameter("ele");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = icoadsStatisticService.getPointData
                    (strLats, strLngs, month,fourth,table,ele);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("ICOADS统计数据查询失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

}
