package net.xinhong.meteoserve.service.controller;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.service.SecurityService;
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
 * Created by xiaoyu on 16/9/20.
 * 权限控制
 */
@Controller
@RequestMapping(value = "/security")
public class SecurityVerifyController {

    private static final Log logger = LogFactory.getLog(SecurityVerifyController.class);

    @Autowired
    private SecurityService securityService;

    @RequestMapping(value = "/getlatestversion", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getLatestVersion(HttpServletRequest request, HttpServletResponse response) {

        logger.info("进行了获取最新版本操作.");

        try {
            long tt = System.currentTimeMillis();

            JSONObject resJSON = securityService.getLatestVersion();
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("获取最新版本执行失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            resJSON.put(ResJsonConst.STATUSMSG, ResStatus.SEARCH_ERROR.getMessage());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/getkey", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getKey(HttpServletRequest request, HttpServletResponse response) {
        //1.获取传入的各类用户信息,并判断传入的正确性
        String userPID = request.getParameter("pid");    //用户终端唯一识别ID
        String clientType = request.getParameter("type");   //用户终端类型(Android/iOS/H5)
        String clientVersion = request.getParameter("version"); //两位或三位的数字,例如: 1.2/1.3.1/2.0

        String sLat = request.getParameter("lat");
        String sLng = request.getParameter("lng");
        logger.info("userPID = " + userPID + "进行了获取key操作.");

        try {
            long tt = System.currentTimeMillis();

            JSONObject resJSON = securityService.getKey(userPID, clientType, clientVersion, sLat, sLng);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("getkey执行失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            resJSON.put(ResJsonConst.STATUSMSG, ResStatus.SEARCH_ERROR.getMessage());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }


    @RequestMapping(value = "/clientinfo", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void saveClientInfo(HttpServletRequest request, HttpServletResponse response) {
        //1.获取传入的各类用户信息,并判断传入的正确性
        String userPID = request.getParameter("pid");    //用户终端唯一识别ID
        String clientType = request.getParameter("type");   //用户终端类型(Android/iOS/H5)
        String clientVersion = request.getParameter("version"); //两位或三位的数字,例如: 1.2/1.3.1/2.0

        String operateType = request.getParameter("operatetype"); //操作类型,包括:lanch/login/logout/enterbackgroud/enterforeground/terminate

        String sLat = request.getParameter("lat");
        String sLng = request.getParameter("lng");


        logger.info("userPID = " + userPID + "进行了" + operateType + "操作.");

        try {
            long tt = System.currentTimeMillis();

            JSONObject resJSON = securityService.saveClientinfo(userPID, clientType, clientVersion, operateType, sLat, sLng);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("clientinfo执行失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            resJSON.put(ResJsonConst.STATUSMSG, ResStatus.SEARCH_ERROR.getMessage());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }


    @RequestMapping(value = "/searchclientinfo", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void searchClientInfo(HttpServletRequest request, HttpServletResponse response) {

        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String dayNum = request.getParameter("daynum");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');


        logger.info("进行了用户活跃信息查询操作.");

        try {
            long tt = System.currentTimeMillis();

            JSONObject resJSON = securityService.searchClientinfo(year, month, day, dayNum);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("searchclientinfo执行失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            resJSON.put(ResJsonConst.STATUSMSG, ResStatus.SEARCH_ERROR.getMessage());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/searchclientinfolatlng", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void searchClientInfoLatlng(HttpServletRequest request, HttpServletResponse response) {

        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");
        String dayNum = request.getParameter("daynum");
        String delayseconds = request.getParameter("delayseconds");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');


        logger.info("进行了用户活跃信息查询操作(按经纬度).");

        try {
            long tt = System.currentTimeMillis();

            JSONObject resJSON = securityService.searchClientinfoLatlng(year, month, day, dayNum, delayseconds);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("searchclientinfo执行失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            resJSON.put(ResJsonConst.STATUSMSG, ResStatus.SEARCH_ERROR.getMessage());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }


    @RequestMapping(value = "/fedbackinfo", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void fedbackinfo(HttpServletRequest request, HttpServletResponse response) {
        //1.获取传入的用户反馈信息,并检查正确性
        String userPID = request.getParameter("pid");    //用户终端唯一识别ID
        String clientType = request.getParameter("type");   //用户终端类型(Android/iOS/H5)
        String clientVersion = request.getParameter("version"); //两位或三位的数字,例如: 1.2/1.3.1/2.0

        String desc = request.getParameter("desc"); //问题描述
        String phonenum = request.getParameter("phonenum"); //手机号码
        String picpath = request.getParameter("picpath"); //图片路径
        String email = request.getParameter("email"); //邮箱地址

        logger.info("userPID = " + userPID + "进行了" + desc + "问题反馈.");

        try {
            long tt = System.currentTimeMillis();
            JSONObject resJSON = securityService.saveFedbackInfo(userPID, clientType, clientVersion, desc, phonenum, picpath, email);
            logger.debug("存储耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("fedbackinfo:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            resJSON.put(ResJsonConst.STATUSMSG, ResStatus.SEARCH_ERROR.getMessage());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }

    }



}
