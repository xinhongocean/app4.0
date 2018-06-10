package net.xinhong.meteoserve.service.dao.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.tool.StringUtils;
import net.xinhong.meteoserve.service.dao.CFSFCDataDao;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Repository;

/**
 * Created by xiaoyu on 16/9/19.
 */
@Repository
public class CFSFCDataDaoImpl implements CFSFCDataDao {
    @Override
    public JSONObject getDataFromLatlng(float lat, float lng, String year, String month, String day, String hour, String yearFC, String monthFC) {
        DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHH");
        year = StringUtils.leftPad(year, 4, "0");
        month = StringUtils.leftPad(month, 2, "0");
        day = StringUtils.leftPad(day, 2, "0");
        hour = StringUtils.leftPad(hour, 2, "0");

        DateTime date = DateTime.parse(year + month + day + hour, dateformat);
        //这里将给定的时间转换为世界时,并获取指定日期及前一日期的年月日
        DateTime curDate = date.minusHours(8);
        DateTime preDate = curDate.minusDays(1);
        String cyear = StringUtils.leftPad(String.valueOf(curDate.getYear()), 4, "0");
        String cmonth = StringUtils.leftPad(String.valueOf(curDate.getMonthOfYear()), 2, "0");
        String cday = StringUtils.leftPad(String.valueOf(curDate.getDayOfMonth()), 2, "0");

        String pyear = StringUtils.leftPad(String.valueOf(preDate.getYear()), 4, "0");
        String pmonth = StringUtils.leftPad(String.valueOf(preDate.getMonthOfYear()), 2, "0");
        String pday = StringUtils.leftPad(String.valueOf(preDate.getDayOfMonth()), 2, "0");

        //首先查询给定日期CFS预测结果,如果没有查询到再查询前一天预测结果
        //...


        //拼接结果JSON数据
        JSONObject dataJSON = new JSONObject();
        //...

        JSONObject resJson = new JSONObject();
        resJson.put(ResJsonConst.DATA, dataJSON);
        //返回的时间需要转换为北京时!
        resJson.put(ResJsonConst.TIME, curDate.plusHours(8).toString(dateformat));

        return resJson;
    }
}
