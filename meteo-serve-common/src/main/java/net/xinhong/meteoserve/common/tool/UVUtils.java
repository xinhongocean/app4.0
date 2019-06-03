package net.xinhong.meteoserve.common.tool;

import com.xinhong.mids3d.util.math.WindUtil;
import net.xinhong.meteoserve.common.grib.GridData;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: <br>
 *
 * @author 作者 <a href=ds.lht@163.com>stone</a>
 * @version 创建时间：2016/7/26.
 */
public class UVUtils {


    public static Map<String,GridData> processWsAndWd(GridData uuGrid,GridData vvGrid){

        Map<String, GridData> dataMap = new HashMap<>(2);
        GridData wsGridData = uuGrid.clone();
        GridData wdGridData = wsGridData.clone();
        float[][] wsArray = wsGridData.getGridData();
        float[][] wdArray = wdGridData.getGridData();

        float[][] uuDataArray = uuGrid.getGridData();
        float[][] vvDataArray = vvGrid.getGridData();

        for (int i = 0; i < uuDataArray.length; i++) {
            for (int k = 0; k < uuDataArray[i].length; k++) {
                //根据uv分量计算ws，wd
                WindUtil.Wind wind = new WindUtil().uvCalWsAndWd(uuDataArray[i][k], vvDataArray[i][k]);
                wsArray[i][k] = wind.getWs();
                wdArray[i][k] = wind.getWd();
            }
        }
        dataMap.put("WS", wsGridData);
        dataMap.put("WD", wdGridData);
        return dataMap;
    }
}
