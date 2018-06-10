package net.xinhong.meteoserve.service.dao;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ElemGFS;

/**
 * Created by xiaoyu on 16/7/22.
 */
public interface GFSFCDataDao {
    /**
     * 获取某个点指定时间及预报时效的天气数据
     * @param strLat
     * @param strLng
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param VTI
     * @return
     */
    JSONObject getPointData(String strLat, String strLng, String year, String month, String day, String hour, String VTI);


    /**
     * 获取等值线数据
     * @param strArea EN东北半球
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param VTI
     * @param level
     * @return
     */
    JSONObject getIsolineData(String year, String month,
                              String day, String hour, String VTI, String level, String elem, String strArea);

    /**
     * 获取GFS中的区域数据(直接读文件)
     * @param ryear
     * @param rmonth
     * @param rday
     * @param rhour
     * @param VTI
     * @param level
     * @param elemGFS
     * @param strArea
     * @return
     */
    JSONObject getAreaData(String ryear, String rmonth, String rday, String rhour, String VTI,
                           String level, ElemGFS elemGFS, String strArea);

    JSONObject getIsosurfaceData(String ryear, String rmonth,
                                 String rday, String rhour, String vti, String level, String elem, String strArea);
}
