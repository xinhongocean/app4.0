package net.xinhong.meteoserve.service.controller;

/**
 * Created by xiaoyu on 16/8/14.
 */

import com.alibaba.fastjson.JSONObject;
import com.xinhong.mids3d.datareader.util.ElemCode;
import com.xinhong.mids3d.util.IsolinesAttributes;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by xiaoyu on 16/7/22.
 * 等值线属性获取控制器
 */
@Controller
@RequestMapping(value = "/isolineattr")
public class IsolineAttrController {
    private static final Log logger = LogFactory.getLog(IsolineAttrController.class);

    @RequestMapping(value = "/color", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public final void getIsolineColorAttr(HttpServletRequest request, HttpServletResponse response) {
        String elem = request.getParameter("elem");
        String level = request.getParameter("level");
        logger.info("进行等值线颜色属性查询操作....");
        long tt = System.currentTimeMillis();
        try {
            JSONObject resJSON = new JSONObject();
            ElemCode elemCode = ElemCode.fromValue(elem);
            if (elem == null || level == null || elem.isEmpty() || level.isEmpty() || elemCode == null){
                JSONOperateTool.putJSONParamError(resJSON);

            } else {
                IsolinesAttributes attributes = IsolinesAttributes.createInstance(elemCode, level);
                if (attributes == null){
                    JSONOperateTool.putJSONNoResult(resJSON);
                } else {
                    JSONObject jsonData = attributes.getLinesColorJSON();
                    if (jsonData == null || jsonData.isEmpty()){
                        JSONOperateTool.putJSONNoResult(resJSON);
                    } else {
                        JSONOperateTool.putJSONSuccessful(resJSON, jsonData);
                    }
                }
            }
            resJSON.put("delay", (System.currentTimeMillis() - tt));
            JSONUtil.writeJSONToResponse(response, resJSON);
        } catch (Exception e) {
            logger.error("进行等值线颜色属性查询操作:" + e);
            JSONObject resJSON = new JSONObject();
            resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SEARCH_ERROR.getStatusCode());
            JSONUtil.writeJSONToResponse(response, resJSON);
        }
    }


}
