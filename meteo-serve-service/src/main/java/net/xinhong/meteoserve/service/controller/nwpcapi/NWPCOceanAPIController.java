package net.xinhong.meteoserve.service.controller.nwpcapi;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.common.tool.ConfigUtil;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.controller.JSONUtil;
import net.xinhong.meteoserve.service.service.nwpcapi.NWPCOceanAPIService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.parsing.PropertyEntry;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by xiaoyu on 17/2/21.
 */
@Controller
@RequestMapping(value = "/api/ocean")
//@PropertySource("classpath:conf/apikeys.properties")
public class NWPCOceanAPIController {
    private static final Log logger = LogFactory.getLog(NWPCOceanAPIController.class);
    @Autowired
    private NWPCOceanAPIService nwpcOceanAPIService;

   @Value("#{prosConfigID['key.api.ocean.appziyadiaoyu']}")
    private String KEYAPPZIYADIAOYU;
    @Value("#{prosConfigID['key.api.ocean.appziyadiaoyu.edate']}")
    private String KEYAPPZIYADIAOYU_EDATESTR;
    private static DateTime appZiyadiaoyuEdate;


    /**
     * 验证Key是否正常
     * @param key
     * @return
     */
    private  JSONObject isValidKey(String key){
        if (key == null || key.isEmpty() || !key.equals(KEYAPPZIYADIAOYU)){
            JSONObject resJSON = new JSONObject();
            JSONOperateTool.putJSONErrorKey(resJSON);
            return resJSON;
        }

        if (appZiyadiaoyuEdate == null){
            appZiyadiaoyuEdate = DateTime.parse(KEYAPPZIYADIAOYU_EDATESTR, DateTimeFormat.forPattern("yyyyMMddHHmm"));
        }
        if (appZiyadiaoyuEdate.isBeforeNow()){
            JSONObject resJSON = new JSONObject();
            JSONOperateTool.putJSONInvalidKey(resJSON);
            return resJSON;
        }
        return null;
    }

    @RequestMapping(value = "/hourswindwavefromposition", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getWindWaveDataFromPostion(HttpServletRequest request, HttpServletResponse response) {
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
        String slat = request.getParameter("lat");
        String slng = request.getParameter("lng");

        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');
        if (hour==null || hour.isEmpty()) hour = StringUtils.leftPad(Integer.toString(curDate.getHourOfDay()), 2, '0');

        logger.info("进行海钓海浪及海面风数据未来24小时数据查询,时间:" + year + month + day + hour + "......");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = nwpcOceanAPIService.getHoursWindWaveDataFromPostion(slat, slng, year, month, day, hour, true);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("海钓海浪及海面风数据查询失败:" + e);
            e.printStackTrace();
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            resJSON.put(ResJsonConst.STATUSMSG, ResStatus.SEARCH_ERROR.getMessage());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/sevendayswindwavefromposition", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void get7DaysWindWaveDataFromPostion(HttpServletRequest request, HttpServletResponse response) {

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
        String slat = request.getParameter("lat");
        String slng = request.getParameter("lng");

        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');

        logger.info("进行海钓海浪及海面风数据未来7天数据查询,时间:" + year + month + day + "......");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = nwpcOceanAPIService.get7DayWindWaveDataFromPostion(slat, slng, year, month, day);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("海钓海浪及海面风未来7天数据查询失败:" + e);
            e.printStackTrace();
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            resJSON.put(ResJsonConst.STATUSMSG, ResStatus.SEARCH_ERROR.getMessage());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

}
