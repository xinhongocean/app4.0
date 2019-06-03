package net.xinhong.meteoserve.common.tool;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.common.grib.GridData;

/**
 * Description: <br>
 *
 * @author 作者 <a href=ds.lht@163.com>stone</a>
 * @version 创建时间：2016/5/12.
 */
public class GridDataUtil {

    private static final float delta = 1.25f; //数据分辨率
    /**
     *
     * 根据经纬度范围截取数据
     *
     * @param sLat
     * @param eLat
     * @param sLng
     * @param eLng
     * @param gridData
     * @return
     */
    public static JSONObject getFreeAreaData(float sLat, float eLat, float sLng, float eLng, float[][] gridData) {
        int allRowNum = 144;
        int rowNum = (int) ((eLat - sLat) / delta);
        int colNum = (int) ((eLng - sLng) / delta);
        JSONObject resObj = new JSONObject();
        resObj.put(ResJsonConst.ROWNUM, rowNum);
        resObj.put(ResJsonConst.COLNUM, colNum);
        JSONArray resAry = new JSONArray();
        for (int i = 0; i < rowNum; i++) {
            float curLat = sLat + i * delta - (-90.0f);
            int index_y = allRowNum - (int) (curLat / delta);
            JSONArray rowDataAry = new JSONArray();
            for (int j = 0; j < colNum; j++) {
                int index_x = (int) ((sLng - 0) / delta) + j;
                float val = gridData[index_y][index_x];
                rowDataAry.add(val);
            }
            resAry.add(rowDataAry);
        }
        resObj.put("array", resAry);
        return resObj;
    }

    /**
     * 根据经纬度范围截取数据
     * @param sLat
     * @param eLat
     * @param sLng
     * @param eLng
     * @param gridData
     * @return
     */
    public static GridData getFreeAreaData(float sLat, float eLat, float sLng, float eLng, GridData gridData) {
        try {
            GridData resGridData = new GridData();
            int allRowNum = 144;
            int rowNum = (int) ((eLat - sLat) / delta);
            int colNum = (int) ((eLng - sLng) / delta);
            resGridData.setRowNum(rowNum);
            resGridData.setColNum(colNum);
            float [] latAry = new float[rowNum];
            float [] lngAry = new float[colNum];
            float xStart = DataTypeConst.NULLVAL;
            float yStart = DataTypeConst.NULLVAL;

            float xEnd = DataTypeConst.NULLVAL;
            float yEnd = DataTypeConst.NULLVAL;

            float [][]dataArray = new float[rowNum][colNum];
            for (int i = 0; i < rowNum; i++) {
                float curLat = sLat + i * delta - (-90.0f);
                if(i==0)
                    xStart = curLat;
                if(i==rowNum-1)
                    xEnd = curLat;
                latAry[i] = curLat;
                int index_y = allRowNum - (int) (curLat / delta);
                for (int j = 0; j < colNum; j++) {
                    float curLng = sLng + j * delta;
                    if(j==0)
                        yStart = curLng;
                    if(j==colNum -1)
                        yEnd = curLng;
                    int index_x = (int) ((sLng - 0) / delta) + j;
                    lngAry[j] = curLng;
                    dataArray[i][j] = gridData.getGridData()[index_y][index_x];
                }
            }
            resGridData.setXStart(xStart);
            resGridData.setXEnd(xEnd);
            resGridData.setYStart(yStart);
            resGridData.setYEnd(yEnd);
            resGridData.setXDel(delta);
            resGridData.setYDel(delta);
            resGridData.setLatAry1D(latAry);
            resGridData.setLonAry1D(lngAry);
            resGridData.setGridData(dataArray);
            return resGridData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
