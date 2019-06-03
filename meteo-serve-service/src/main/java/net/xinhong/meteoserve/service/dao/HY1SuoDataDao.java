package net.xinhong.meteoserve.service.dao;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ElemGFS;

/**
 * Created by wingsby on 2017/12/28.
 */
public interface HY1SuoDataDao {
    JSONObject getPointData(String strLat, String strLng, String year, String month, String day, String hour, String VTI);

    JSONObject getAreaData(String ryear, String rmonth, String rday, String rhour, String VTI, String ele);

    JSONObject getImg(String ryear, String rmonth, String rday, String rhour, String vti, String depth, String eles);

    JSONObject getIsolineData(String ryear, String rmonth, String rday, String rhour, String vti, String elem, String strArea);
}
