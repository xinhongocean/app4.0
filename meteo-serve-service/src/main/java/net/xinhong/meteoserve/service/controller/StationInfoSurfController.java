package net.xinhong.meteoserve.service.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.service.domain.StationDataSurfBean;
import net.xinhong.meteoserve.service.domain.StationInfoSurfBean;
import net.xinhong.meteoserve.service.service.StationDataSurfService;
import net.xinhong.meteoserve.service.service.StationInfoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: 站点信息查询控制器<br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 <a href=mailto:liusofttech@sina.com>刘晓昌</a>
 * @version 创建时间：2016/3/3.
 */
@Controller
@RequestMapping(value = "/station")
public class StationInfoSurfController {
    private static final Log logger = LogFactory.getLog(StationInfoSurfController.class);

    @Autowired
    private StationDataSurfService stationSurfService;
    @Autowired
    private StationInfoService stationInfoService;

    @RequestMapping(value = "/surfdata.do",method= {RequestMethod.POST, RequestMethod.GET}, produces = "text/html;charset=UTF-8")
    public final String surfData(ModelMap model, @RequestParam(value = "name") String name) {
        logger.debug("进行" + name + "站点实况查询操作....");
        try {
            StationDataSurfBean stationSurfBean = stationSurfService.getStationSurf(name, "2015", "05", "15", "13", "00");
            model.put("stationsurf", stationSurfBean);
            if (stationSurfBean != null){
                logger.debug("进行" + name + "站点实况查询操作成功!");
            }
            return "station/surfdata";
        } catch (Exception e) {
            logger.error("进行" + name + "站查询时失败:" + e.getMessage());
            return "error";
        }
    }

    @RequestMapping(value = "/surfinfo.do",method= {RequestMethod.POST, RequestMethod.GET}, produces = "text/html;charset=UTF-8")
    public final String surfInfo() {
        return "station/surfinfosearch";
    }

    @RequestMapping(value = "/surfinfolatlng.do",method= {RequestMethod.POST, RequestMethod.GET}, produces = "text/html;charset=UTF-8")
    public final String surfInfo(ModelMap model, @RequestParam(value = "lat") float lat, @RequestParam(value = "lng") float lng) {
        logger.debug("进行" + lat + "," + lng + "周边站点信息查询操作....");
        long tt = System.currentTimeMillis();
        try {
            StationInfoSurfBean stationInfoBean = stationInfoService.getNearestStationInfoSurfFromLatLng(lat, lng);
            List<StationInfoSurfBean> stationinfolist = new ArrayList<>();
            if (stationInfoBean != null)
                stationinfolist.add(stationInfoBean);
            model.put("stationinfolist", stationinfolist);
            if (stationInfoBean != null){
                logger.debug("站点信息查询操作成功!");
            }
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            return "station/surfinfo";
        } catch (Exception e) {
            logger.error("站点信息查询时失败:" + e.getMessage());
            return "error";
        }
    }


    @RequestMapping(value = "/surfinfopyorcname.do",method= {RequestMethod.POST, RequestMethod.GET}, produces = "text/html;charset=UTF-8")
    public final String surfInfo(ModelMap model, @RequestParam(value = "py", required = false) String py, @RequestParam(value = "cname", required = false) String cname) {
        logger.debug("进行拼音为" + py + "站点信息查询操作....");
        long tt = System.currentTimeMillis();
        try {
            List<StationInfoSurfBean> stationInfoBeans = null;
            if (py != null && !py.isEmpty()){
                stationInfoBeans = stationInfoService.getStationInfoSurfFromPy(py);
            } else if (cname != null && !cname.isEmpty()) {
                cname = new String(cname.getBytes("ISO-8859-1"), "UTF-8");
                stationInfoBeans = stationInfoService.getStationInfoSurfFromCname(cname);
            }
            model.put("stationinfolist", stationInfoBeans);
            if (stationInfoBeans != null && !stationInfoBeans.isEmpty()){
                logger.debug("站点信息查询操作成功!");
            }
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            return "station/surfinfo";
        } catch (Exception e) {
            logger.error("站点信息查询时失败:" + e.getMessage());
            return "error";
        }
    }

    @RequestMapping(value = "/infofrompynamecode", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void dataFromCname(HttpServletRequest request, HttpServletResponse response) {

        String param = request.getParameter("param");
        logger.info("进行站点信息模糊查询:" + param);
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = stationInfoService.getStationInfoFromPyNameCode(param);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("站点信息模糊查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }


    @RequestMapping(value = "/infofromlatlng", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void dataFromLatlng(HttpServletRequest request, HttpServletResponse response) {

        float lat = Float.parseFloat(request.getParameter("lat"));
        float lng = Float.parseFloat(request.getParameter("lng"));
        logger.info("进行站点信息按经纬度查询:" + lat + "," + lng);
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = stationInfoService.getStationInfoFromLatLng(lat, lng);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("站点信息按经纬度查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/nearesthighinfofromlatlng", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void nearestStationInfoHighFromLatLng(HttpServletRequest request, HttpServletResponse response) {

        float lat = Float.parseFloat(request.getParameter("lat"));
        float lng = Float.parseFloat(request.getParameter("lng"));
        logger.info("进行按经纬度查询最近的高空站:" + lat + "," + lng );
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = stationInfoService.getNearestStationInfoHighFromLatLng(lat, lng);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("最近高空站点信息按经纬度查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

    @RequestMapping(value = "/nearesthighinfofromcode", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void nearestStationInfoHighFromCode(HttpServletRequest request, HttpServletResponse response) {

        String strCode = request.getParameter("code");
        logger.info("进行站点信息按站号查询最近的高空站:" + strCode);
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = stationInfoService.getNearestStationInfoHighFromCode(strCode);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("最近高空站点信息按站号查询时失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }

}
