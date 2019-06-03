package net.xinhong.meteoserve.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.common.FCTimeTool;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.dao.GTSPPDataDao;
import net.xinhong.meteoserve.service.dao.HYCOMDataDao;
import net.xinhong.meteoserve.service.service.GTSPPDataService;
import net.xinhong.meteoserve.service.service.HYCOMDataService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by wingsby on 2017/12/28.
 */
@Service
public class GTSPPDataServiceImpl implements GTSPPDataService {
    private static final Log logger = LogFactory.getLog(GTSPPDataServiceImpl.class);
    float delta=0.5f;
    @Autowired
    private GTSPPDataDao gtsppDataDao;
    @Override
    public JSONObject getPointData( String year, String month,
                                   String day) {
        JSONObject resJSON = new JSONObject();
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMdd");
        DateTime useDate = DateTime.parse(year + month + day , dateformat); //传入为北京时
        DateTime resDate = new DateTime(Integer.valueOf(year),Integer.valueOf(month),Integer.valueOf(day), 8, 0);
//        resDate=resDate.minusDays(3);
        String ryear = resDate.toString("yyyy");
        String rmonth = resDate.toString("MM");
        String rday = resDate.toString("dd");
        JSONObject daoJSON=gtsppDataDao.getPointData(ryear,rmonth,rday);
        JSONObject dataJSON=new JSONObject();
        dataJSON.put(ResJsonConst.DATA,daoJSON);
        if (dataJSON==null||dataJSON.isEmpty()||dataJSON.getString(ResJsonConst.DATA) == null
                ||dataJSON.getString(ResJsonConst.DATA).isEmpty()){
            JSONOperateTool.putJSONNoResult(dataJSON);
            return dataJSON;
        }
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON.get(ResJsonConst.DATA));
        resJSON.put(ResJsonConst.TIME,daoJSON.get(ResJsonConst.TIME));
        return resJSON;
    }


    private GTSPPDataServiceImpl.ResultTime getResultTime(DateTime useDate) {
     //gtspp 周4前往前推8天，否则往前推当前日-周四应该日，麻烦。。
        DateTime resDate = null;
        int afterMinutes = 8 * 60 *24;
        //注意:如果传入的是以前的时间,则用该时间计算起报时间,如果是当前或以后的时间,用当前时间计算起报时间!
        if (useDate.isBeforeNow()) {
            resDate = FCTimeTool.getHYFCStartTime(useDate, afterMinutes);
        } else {
            resDate = FCTimeTool.getHYFCStartTime(DateTime.now(), afterMinutes);
        }
        //计算VTI
        GTSPPDataServiceImpl.ResultTime restime = new GTSPPDataServiceImpl.ResultTime();
        restime.resDate = resDate;
        return restime;
    }

    private static class ResultTime {
        private DateTime resDate;
        private String resVTI;
    }
}
