package net.xinhong.meteoserve.service.controller.nwpcapi;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.controller.JSONUtil;
import net.xinhong.meteoserve.service.service.GFSFCDataService;
import net.xinhong.meteoserve.service.service.nwpcapi.NWPCOceanAPIService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by xiaoyu on 17/2/21.
 */
@Controller
@RequestMapping(value = "/api/convection")
//@PropertySource("classpath:conf/apikeys.properties")
public class ConvectionDataAPIController {
    private static final Log logger = LogFactory.getLog(ConvectionDataAPIController.class);
    @Autowired
    private GFSFCDataService gfsfcDataService;

   @Value("#{prosConfigID['key.api.convection.appquxinyuan']}")
    private String KEYAPP_CONVECTION_QUXINYUAN;
    @Value("#{prosConfigID['key.api.convection.appquxinyuan.edate']}")
    private String KEYAPP_CONVECTION_QUXINYUAN_EDATESTR;
    private static DateTime appConvectionQuxinyuanEdate;


    /**
     * 验证Key是否正常
     * @param key
     * @return
     */
    private  JSONObject isValidKey(String key){
        if (key == null || key.isEmpty() || !key.equals(KEYAPP_CONVECTION_QUXINYUAN)){
            JSONObject resJSON = new JSONObject();
            JSONOperateTool.putJSONErrorKey(resJSON);
            return resJSON;
        }

        if (appConvectionQuxinyuanEdate == null){
            appConvectionQuxinyuanEdate = DateTime.parse(KEYAPP_CONVECTION_QUXINYUAN_EDATESTR, DateTimeFormat.forPattern("yyyyMMddHHmm"));
        }
        if (appConvectionQuxinyuanEdate.isBeforeNow()){
            JSONObject resJSON = new JSONObject();
            JSONOperateTool.putJSONInvalidKey(resJSON);
            return resJSON;
        }
        return null;
    }

    @RequestMapping(value = "/hoursdatafromposition", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void geHoursDataFromPostion(HttpServletRequest request, HttpServletResponse response) {
        JSONObject res = isValidKey( request.getParameter("key"));
        if (res != null){
            JSONUtil.writeJSONToResponse(response, res);
            return;
        }

        DateTime curDate = DateTime.now();

        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String hour = request.getParameter("hour");
        String shournum = request.getParameter("hournum");
        float lat = -999,lng = -999;
        int hournum = 24;
        try{
            lat = Float.parseFloat(request.getParameter("lat"));
            lng = Float.parseFloat(request.getParameter("lng"));
            hournum = Integer.parseInt(shournum);
        }catch (NumberFormatException ex){
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.PARAM_ERROR.getStatusCode());
            resJSON.put(ResJsonConst.STATUSMSG, ResStatus.PARAM_ERROR.getMessage());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }

        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');

        logger.info("进行外部强对流按位置逐小时数据查询,时间:" + year + month + day + hour + "......");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = gfsfcDataService.getPointHoursConvectionData(lat, lng, year, month, day, hour, hournum);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("外部强对流按位置逐小时数据查询失败:" + e);
            e.printStackTrace();
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            resJSON.put(ResJsonConst.STATUSMSG, ResStatus.SEARCH_ERROR.getMessage());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

}
