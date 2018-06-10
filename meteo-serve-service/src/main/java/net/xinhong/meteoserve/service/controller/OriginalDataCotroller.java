package net.xinhong.meteoserve.service.controller;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.service.service.OriginalDataService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Created by wingsby on 2018/1/3.
 */

@Controller
@RequestMapping(value = "/original")
public class OriginalDataCotroller {

    private Logger logger = Logger.getLogger(OriginalDataCotroller.class);

    @Autowired
    private OriginalDataService service;


    @RequestMapping(value = "/getOriginalIcoadsPages", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getOriginalIcoadsPages(HttpServletRequest request, HttpServletResponse response) {
        String strLats = request.getParameter("lat");
        String strLngs = request.getParameter("lng");
        String syear = request.getParameter("year");
        int grid = -1;
        int year=-1;
        try {
            if (strLats != null && strLngs != null) {
                grid= (int) (Math.floor(Float.valueOf(strLngs) / 0.5) * 1000
                        + Math.floor((Float.valueOf(strLats) + 90) / 0.5));
            }
            if(syear!=null)year=Math.round(Float.valueOf(syear));
            else year=new Date().getYear()-1901;
        } catch (Exception e) {
            logger.error("参数输入错误:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.PARAM_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
            return;
        }
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = service.getICOADSPages(grid, year);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("ICOADS原始记录数据分页情况查询失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/getOriginalIcoads", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getOriginalIcoads(HttpServletRequest request, HttpServletResponse response) {
        String strLats = request.getParameter("lat");
        String strLngs = request.getParameter("lng");
        String syear = request.getParameter("year");
        String pages=request.getParameter("page");
        int grid = -1;
        int pageid=-1;
        int year=-1;
        try {
            if (strLats != null && strLngs != null) {
               grid= (int) (Math.floor(Float.valueOf(strLngs) / 0.5) * 1000
                                       + Math.floor((Float.valueOf(strLats) + 90) / 0.5));
            }
            if(pages!=null)pageid=Math.round(Float.valueOf(pages));
            else pageid=0;
            if(syear!=null)year=Math.round(Float.valueOf(syear));
            else year=new Date().getYear()-1901;
        } catch (Exception e) {
            logger.error("参数输入错误:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.PARAM_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
            return;
        }
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = service.getICOADS(grid, year,pageid);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("ICOADS原始记录数据查询失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/getOriginalTyphoons", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getOriginalTyphoons(HttpServletRequest request, HttpServletResponse response) {
        //注意统计类型为表名+要素名
        String year = request.getParameter("year");
        String source = request.getParameter("source");

        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = service.getTyphoons(Integer.valueOf(year), source);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("台风原始记录数据查询失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/getTyphoonIDX", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getOriginalTyphoonIdx(HttpServletRequest request, HttpServletResponse response) {
        //注意统计类型为表名+要素名
        String year = request.getParameter("year");
        String source = request.getParameter("source");

        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = service.getTyphoonIDX(Integer.valueOf(year), source);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("台风指数查询失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/getOriginalTyphoon", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getOriginalTyphoon(HttpServletRequest request, HttpServletResponse response) {
        //注意统计类型为表名+要素名
        String id = request.getParameter("id");
        String source = request.getParameter("source");

        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = service.getTyphoon(id, source);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("台风数据查询失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/getOriginalGtsppBuoy", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getOriginalGtsppBuoy(HttpServletRequest request, HttpServletResponse response) {
        //注意统计类型为表名+要素名
//        String year = request.getParameter("year");
        String cid = request.getParameter("cid");
        long tt = System.currentTimeMillis();
        try {
            if(cid==null){
                JSONObject resJSON = new JSONObject();
                resJSON.put(ResJsonConst.STATUSCODE, ResStatus.NORESULT.getStatusCode());
                JSONUtil.writeJSONToResponse(response, resJSON);
                return;
            }
//            if(cid.length()<10){
//                char[] chars=new char[10];
//                char[] cchars=cid.toCharArray();
//                int k=0;
//                for(int i=0;i<10;i++){
//                    if(cchars[i]==' '){
//                        for(;k<10-cchars.length;k++){
//                            chars[i]=' ';
//                        }
//                    }else{
//                        chars[i]=cchars[i-k];
//                    }
//                }
//                cid=new String(chars);
//            }
            JSONObject resJSON = service.getGtsppBuoy(cid);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("GTSPP原始记录数据查询失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }


}
