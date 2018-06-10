package net.xinhong.meteoserve.service.common;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.status.ResStatus;
import net.xinhong.meteoserve.service.domain.StationInfoSurfBean;

/**
 * Description: 返回结果JSON对象操作公用方法<br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 <a href=mailto:liusofttech@sina.com>刘晓昌</a>
 * @version 创建时间：2016/3/14.
 */
public final class JSONOperateTool {
    private JSONOperateTool(){}

    public static void putJSONStation(JSONObject resJSON, StationInfoSurfBean stationInfo){
        resJSON.put(ResJsonConst.STATIONCNAME, stationInfo.getCname());
        resJSON.put(ResJsonConst.STATIONCODE, stationInfo.getStationCode());
        resJSON.put(ResJsonConst.LAT, stationInfo.getLat());
        resJSON.put(ResJsonConst.LNG, stationInfo.getLng());
    }

    public static void putJSONNoResult(JSONObject resJSON){
        resJSON.put(ResJsonConst.STATUSCODE, ResStatus.NORESULT.getStatusCode());
        resJSON.put(ResJsonConst.STATUSMSG, ResStatus.NORESULT.getMessage());
    }

    public static void putJSONParamError(JSONObject resJSON){
        resJSON.put(ResJsonConst.STATUSCODE, ResStatus.PARAM_ERROR.getStatusCode());
        resJSON.put(ResJsonConst.STATUSMSG, ResStatus.PARAM_ERROR.getMessage());
    }

    public static void putJSONErrorKey(JSONObject resJSON){
        resJSON.put(ResJsonConst.STATUSCODE, ResStatus.KEY_ERROR.getStatusCode());
        resJSON.put(ResJsonConst.STATUSMSG,  ResStatus.KEY_ERROR.getMessage());
    }

    public static void putJSONInvalidKey(JSONObject resJSON){
        resJSON.put(ResJsonConst.STATUSCODE, ResStatus.KEY_INVALID.getStatusCode());
        resJSON.put(ResJsonConst.STATUSMSG,  ResStatus.KEY_INVALID.getMessage());
    }

    public static void putJSONSuccessful(JSONObject resJSON, Object strData){
        resJSON.put(ResJsonConst.STATUSCODE, ResStatus.SUCCESSFUL.getStatusCode());
        resJSON.put(ResJsonConst.STATUSMSG, ResStatus.SUCCESSFUL.getMessage());
        resJSON.put(ResJsonConst.DATA, strData);
    }

    public static void putJSONISOProcessError(JSONObject resJSON){
        resJSON.put(ResJsonConst.STATUSCODE, ResStatus.ISO_ERROR.getStatusCode());
        resJSON.put(ResJsonConst.STATUSMSG, ResStatus.ISO_ERROR.getMessage());

    }

    public static void putJSONFreeArea(JSONObject resJSON,float sLat,float eLat,
                                       float sLng,float eLng,int rowNum,
                                       int colNum,float delta){
        resJSON.put(ResJsonConst.SLAT,sLat);
        resJSON.put(ResJsonConst.ELAT,eLat);
        resJSON.put(ResJsonConst.SLNG,sLng);
        resJSON.put(ResJsonConst.ELNG,eLng);
        resJSON.put(ResJsonConst.ROWNUM, rowNum);
        resJSON.put(ResJsonConst.COLNUM, colNum);
        resJSON.put(ResJsonConst.DELTA,delta);
    }
}
