package net.xinhong.meteoserve.service.controller;

import com.alibaba.fastjson.JSONObject;
import com.xinhong.mids3d.globeterrian.GlobeTerrianTool;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.service.ToolsService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by xiaoyu on 16/9/26.
 */
@Controller
@RequestMapping(value = "/tools")
public class ToolsController {
    private static final Log logger = LogFactory.getLog(GFSFCDataController.class);

    @Autowired
    private ToolsService toolsService;

    @RequestMapping(value = "/sunrisesetfromlatlng", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void dataFromLatLng(HttpServletRequest request, HttpServletResponse response) {
        float lat = Float.parseFloat(request.getParameter("lat"));
        float lng = Float.parseFloat(request.getParameter("lng"));
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        String day = request.getParameter("day");

        DateTime curDate = DateTime.now();
        if (year==null  || year.isEmpty()) year = StringUtils.leftPad(Integer.toString(curDate.getYear()), 4, '0');
        if (month==null || month.isEmpty()) month = StringUtils.leftPad(Integer.toString(curDate.getMonthOfYear()), 2, '0');;
        if (day==null  || day.isEmpty()) day =  StringUtils.leftPad(Integer.toString(curDate.getDayOfMonth()), 2, '0');

        logger.info("进行经纬度" + lat + "," + lng + "日出日落时间查询....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = toolsService.getSunRiseSetFromLatlng(lat, lng, year, month, day);
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("日出日落时间查询失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }


    @RequestMapping(value = "/fileupload", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void fileupload(HttpServletRequest request, HttpServletResponse response) {
        //获取传入的上传文件信息,并检查正确性
        long tt = System.currentTimeMillis();
        JSONObject resJSON = new JSONObject();
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        if (multipartRequest == null){
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.PARAM_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
            return;
        }
        DateTime curDate = DateTime.now();
        // 得到上传的文件
        MultipartFile mFile = multipartRequest.getFile("file");
        // 得到上传服务器的路径
        final String resourcePath = "/xinhong/meteo_data/duts_data/meteosource"; //nginx配置的静态资源路径
        String path = resourcePath + "/images/feedback/" + curDate.toString("yyyy") + "/";
        String resPath = "/images/feedback/" + curDate.toString("yyyy") + "/";
        String pid = request.getParameter("pid");

        // 得到上传的文件的文件名
        String filename = mFile.getOriginalFilename();

        filename = curDate.toString("yyyyMMddHHmmss") + "_" + filename;
        if (pid != null){
            filename = pid + "_" + filename;
        }
        logger.info("进行" + filename + "文件上传....");
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = mFile.getInputStream();
            path += "/" + filename;
            File file = new File(path);
            if (!file.getParentFile().exists()){
                file.getParentFile().mkdir();
            }

            // 文件流写到服务器端
            outputStream = new FileOutputStream(new File(path));
            //创建一个缓冲区
            byte buffer[] = new byte[1024];
            //判断输入流中的数据是否已经读完的标识
            int len = 0;
            //循环将输入流读入到缓冲区当中，(len=in.read(buffer))>0就表示in里面还有数据
            while ((len = inputStream.read(buffer)) > 0) {
                //使用FileOutputStream输出流将缓冲区的数据写入到指定的目录(path + "/" + filename)当中
                outputStream.write(buffer, 0, len);
            }
            inputStream.close();
            outputStream.close();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SUCCESSFUL.getStatusCode());
            resJSON.put("path", resPath + filename);
            logger.debug("耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);

        }catch (IOException ex){
            logger.error("文件读写错误,上传失败:" + ex);
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }catch (Exception e) {
            logger.error("文件上传失败:" + e);
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("文件读写错误,上传失败:" + e);
                resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
                JSONUtil.writeJSONToResponse(response, resJSON);
            }

        }
    }


    @RequestMapping(value = "/isOcean", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void isOcean(HttpServletRequest request, HttpServletResponse response) {
        long tt=System.currentTimeMillis();
        String lng = request.getParameter("lng");
        String lat = request.getParameter("lat");
        DateTime curDate = DateTime.now();
        if (lat == null || lng == null || (lat != null && !lat.matches("(-\\d+)?(.\\d+)?")) ||
                (lng != null && !lng.matches("(\\d+)?(.\\d+)?"))) {
            logger.error("经纬度输入错误或未输入经纬度");
        }
        boolean flag = false;
        try {
            flag = GlobeTerrianTool.isOcean(Float.valueOf(lat), Float.valueOf(lng));
            JSONObject resJSON = new JSONObject();
            resJSON.put("landtype",flag?"ocean":"land");
            logger.debug("查询耗时:" + (System.currentTimeMillis() - tt));
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("海陆类型查询失败:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }


}
