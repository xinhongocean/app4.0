package com.xinhong.mids3d.util;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xinhong.mids3d.core.isoline.*;
import com.xinhong.mids3d.datareader.util.DataType;
import com.xinhong.mids3d.datareader.util.ElemCode;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import net.xinhong.meteoserve.common.grib.GridData;
import net.xinhong.meteoserve.common.tool.GridDataUtil;

import java.io.File;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Description: <br>
 *
 * @author 作者 <a href=ds.lht@163.com>stone</a>
 * @version 创建时间：2016/5/12.
 */
public class IsolineProcessUtil {

    private static final DecimalFormat df = new DecimalFormat(".####");

    /**
     * 等值线追踪
     *
     * @param dataType
     * @param dataLevel
     * @param elemCode
     * @param gridData
     * @param isFree    是否切割
     * @return
     */
    public static JSONObject isoLineProcessForWni(DataType dataType, String dataLevel,
                                                  ElemCode elemCode, GridData gridData, boolean isFree) {

        df.setRoundingMode(RoundingMode.HALF_UP);
        JSONObject resJson = new JSONObject();

        if (gridData == null || gridData.getGridData() == null || gridData.getGridData().length == 0) {
            return null;
        }
        try {
            // TODO: 2016/5/12 东北半球 ， 数据切割
            if (isFree)
                gridData = GridDataUtil.getFreeAreaData(0f, 75f, 0f, 178.75f, gridData);
            HYIsolineProcess isolineProcess = new HYIsolineProcess();

            //  String basePath = ClassLoader.getSystemResource("").getPath() + "isoline" + File.separator + "isoline_minhang.xml";
            String basePath = IsolineProcessUtil.class.getClassLoader().getResource("").getPath() + "isoline" + File.separator + "isoline_minhang.xml";
            // String basePath = IsolineProcessUtil.class.getClassLoader().getResource("").getPath() + "isoline" + File.separator + "isoline_gfs.xml";
            basePath = URLDecoder.decode(basePath, "UTF-8");
            // 读取配置文件，如果attr读取结果为空，则从程序硬编码中读取！
            IsolinesAttributes attr = IsolinesAttributes.createInstance(elemCode, dataLevel);
            IsolineUtil.IsolineSrcDataType type = IsolineUtil.IsolineSrcDataType.GridData;
            IsolineProcessAttr isoLineProcessAtt = IsolineProcessAttr.createDefaultInstance(dataType, elemCode, dataLevel, type);
            isoLineProcessAtt.setHasNullVal(true);
            isoLineProcessAtt.setLevel(attr.getLevels());
            isolineProcess.setAttr(isoLineProcessAtt);
            isolineProcess.setGridData(gridData);
            ArrayList<IsolineData> isoLinesDatas = isolineProcess.getIsolinesData();

            if (isoLinesDatas == null || isoLinesDatas.size() == 0) {
                return null;
            }
            List<IsolinesAttributes.FillAttr> fillAttr = attr.getFillAttr();
            ArrayList<IsolinePolygon> polygonListResult = new ArrayList<>();
            for (IsolinesAttributes.FillAttr fr : fillAttr) {
                ArrayList<IsolinePolygon> polygonList = isolineProcess.getPolygonList(fr.getsLevel(), fr.geteLevel());

                if (polygonList == null) {
                    continue;
                }
                //多边形属性设置进去
                for (IsolinePolygon plg : polygonList) {
                    plg.setFillColor(fr.getColor());
                    plg.setFillOpacity(fr.getAlpha());
                    plg.setLineColor(attr.getLineColor());
                    plg.setFillStyle(fr.getStyle());
                }
                polygonListResult.addAll(polygonList);
            }
            resJson.put("lineNum", isoLinesDatas.size());
            resJson.put("minVal", attr.getMinVal());
            resJson.put("maxVal", attr.getMaxVal());

            resJson.put("lines", createLine(isoLinesDatas));
            if (polygonListResult != null && polygonListResult.size() > 0) {
                resJson.put("polygon", createPolygon(polygonListResult));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return resJson;
    }

    private static JSONArray createLine(ArrayList<IsolineData> isoLinesDatas) {
        JSONArray dataArray = new JSONArray();
        for (IsolineData isoLinesData : isoLinesDatas) {
            JSONObject lineData = new JSONObject();
            lineData.put("pointNum", isoLinesData.num);
            lineData.put("val", isoLinesData.val);
            lineData.put("isClose", isoLinesData.isClosed);
            JSONArray latArray = new JSONArray();
            JSONArray lngArray = new JSONArray();
            for (Object obj : isoLinesData.lineList) {
                Position pos = (Position) obj;
                latArray.add(Float.parseFloat(df.format(pos.latitude.degrees)));
                lngArray.add(Float.parseFloat(df.format(pos.longitude.degrees)));
            }
            lineData.put("lat", latArray);
            lineData.put("lng", lngArray);
            dataArray.add(lineData);
        }
        return dataArray;
    }

    private static JSONArray createPolygon(ArrayList<IsolinePolygon> polygonListResult) {
        JSONArray polygonArr = new JSONArray();
        for (IsolinePolygon polygon : polygonListResult) {
            JSONObject polygonObj = new JSONObject();
            polygonObj.put("slevel", polygon.getsLevel());
            polygonObj.put("elevel", polygon.geteLevel());
            polygonObj.put("initialslevel", polygon.getinitialElevel());
            polygonObj.put("initialelevel", polygon.getinitialElevel());
            polygonObj.put("fc", Integer.toHexString(polygon.getFillColor().getRGB()).replaceAll("^ff", "#"));
            polygonObj.put("lc", Integer.toHexString(polygon.getLineColor().getRGB()).replaceAll("^ff", "#"));
            polygonObj.put("fs", polygon.getFillStyle().toString());
            polygonObj.put("opcity", polygon.getFillOpacity());
            List<Iterable<? extends LatLon>> boundaries = polygon.getBoundaries();
            if (boundaries == null || boundaries.isEmpty()) {
                continue;
            }
            JSONArray boundarArr = new JSONArray();
            JSONArray biAttr = new JSONArray();
            int index = 0;
            JSONObject obj = new JSONObject();
            for (Iterable<? extends LatLon> iteraLatLon : boundaries) {
                if (index == 0) {
                    //添加outterBoundar
                    JSONArray latLons = creatPolygonBoundary(iteraLatLon);
                    obj.put("bo", latLons);
                } else {
                    //添加innerBounder
                    JSONArray latLons = creatPolygonBoundary(iteraLatLon);
                    biAttr.add(latLons);
                }
                index++;
            }
            obj.put("bi", biAttr);
            boundarArr.add(obj);
            polygonObj.put("boundaries", boundarArr);
            polygonArr.add(polygonObj);
        }

        return polygonArr;
    }

    private static JSONArray creatPolygonBoundary(Iterable<? extends LatLon> iteraLatLon) {
        JSONArray latLons = new JSONArray();
        for (LatLon lat_lon : iteraLatLon) {
            JSONArray latLon = new JSONArray();
            //if (!isXYCorrd) {
            latLon.add(Float.parseFloat(String.format("%.3f", lat_lon.getLongitude().degrees)));
            latLon.add(Float.parseFloat(String.format("%.3f", lat_lon.getLatitude().degrees)));
           /* } else {
                if (lat_lon instanceof PositionVec) {
                    PositionVec pos = (PositionVec)lat_lon;
                    if (pos.getVec() == null) {
                        continue;
                    }
                    latLon.add(Float.parseFloat(String.format("%.3f", pos.getVec().x)));
                    latLon.add(Float.parseFloat(String.format("%.3f", pos.getVec().y)));
                }

            }*/
            latLons.add(latLon);
        }
        return latLons;
    }

}
