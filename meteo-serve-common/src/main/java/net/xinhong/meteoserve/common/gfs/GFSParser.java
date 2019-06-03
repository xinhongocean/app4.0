package net.xinhong.meteoserve.common.gfs;

import com.alibaba.fastjson.JSONObject;
import com.xinhong.mids3d.core.isoline.HYIsolineProcess;
import com.xinhong.mids3d.core.isoline.IsolinePolygon;
import com.xinhong.mids3d.datareader.util.DataType;
import com.xinhong.mids3d.datareader.util.ElemCode;
import com.xinhong.mids3d.util.IsolineProcessUtil;
import com.xinhong.mids3d.util.IsolinesAttributes;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.DataTypeElemConfig;
import net.xinhong.meteoserve.common.constant.GFSVTIConfig;
import net.xinhong.meteoserve.common.grib.GribParser;
import net.xinhong.meteoserve.common.grib.GridData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.xinhong.meteoserve.common.constant.DataTypeConst.GFS_POINT_LEVEL_ELEM_LIST;


/**
 * Created by shijunna on 2016/7/19.
 */
public class GFSParser {
    //key = xhgfs_fc_strHH_vti field = lat_lon_strYMDH_vti
    //value = GFS<level<elem:value>>


    private static final String gfsSign = "GFS";
    private final Log logger = LogFactory.getLog(GFSParser.class);
    private String folderPath, strYMDH, vti, strHH;
    private String elem, level;
    private boolean isAreaIsoLine;
    //// TODO: 2016/7/25 GFS入到REDIS数据库的间隔 原数据是0.25*0.25
    //redisScale设为2表示取0.5*0.5,redisScale设为4表示1*1
    private static final int redisScale = 2;
    //gfs点数据
    private JSONObject pointData = new JSONObject();


    //多边型数据
    Map<String, ArrayList<IsolinePolygon>> polygonDataMap = new HashMap<>();

    //gfs点数据
    private JSONObject isoLineData = new JSONObject();

    private Map<String, GridData> uvData = new HashMap<>();

    public JSONObject getPointData() {
        return pointData;
    }

    public JSONObject getIsoLineData() {
        return isoLineData;
    }

    public Map<String, ArrayList<IsolinePolygon>> getPolygonDataMap() {
        return polygonDataMap;
    }

    public void setPolygonDataMap(Map<String, ArrayList<IsolinePolygon>> polygonDataMap) {
        this.polygonDataMap = polygonDataMap;
    }


    public GFSParser(String folderPath, String strYMDH, String vti, boolean isAreaIsoLine) {
        this.folderPath = folderPath;
        this.strYMDH = strYMDH;
        this.vti = vti;
        this.isAreaIsoLine = isAreaIsoLine;
        if (strYMDH != null && strYMDH.length() == 10) {
            strHH = strYMDH.substring(8);
        }
    }


    public GFSParser() {

    }

   /* public ArrayList<IsolinePolygon> ObtainIsolinePolygon(String filepath){
        GribParser parser = new GribParser();
        GridData gribData = parser.getGribData(filepath);//90~-90,0~359.75
        String[] ary = filepath.split("_");
        this.elem = ary[2];
        this.level = ary[3];

        ArrayList<IsolinePolygon> isolinePolygons = IsolineProcessUtil.obtainIsolinePolygonProcess(DataType.MHWNI, level, ElemCode.fromValue(elem), gribData, true);
        return isolinePolygons;
    }*/


    public void parser() throws RuntimeException {

        File folderF = new File(folderPath);
        if (folderPath == null || folderPath.trim().length() < 1 || !folderF.exists()) {
            logger.info("所输入的路径信息不正确或不存在，请检查后重新部署，folderPath = " + folderPath);
            return;
        }
        File[] files = folderF.listFiles();
        if (files == null || files.length < 1) {
            logger.info("路径下的文件列表为空，请检查数据路径是否正确。folderPath = " + folderPath);
            return;
        }

        // TODO: 2016/9/8  可以先判断是否是需要的数据在解码
        for (int i = 0; i < files.length; i++) {
            GribParser parser = new GribParser();
            GridData gribData = parser.getGribData(files[i].getPath());//90~-90,0~359.75
            if (gribData == null) {
                logger.error("根据数据未获取到GridData，请查证，file= " + files[i].getPath());
                return;
            } else {

                String[] ary = files[i].getName().split("_");
                this.elem = ary[2];
                this.level = ary[3];
                List<String> levelList = DataTypeElemConfig.getLevelListFromDataTypeElem(DataType.GFS, ElemCode.fromValue(this.elem));
                //设置gfs点数据  根据要素、层次以及VTI过滤需要的小文件
                for (String level_elem : GFS_POINT_LEVEL_ELEM_LIST) {
                    if (files[i].getName().indexOf(level_elem) > -1 && GFSVTIConfig.isNeedVTI(Integer.parseInt(vti))) {
                        setPointData(files[i].getName(), gribData);
                        break;
                    }
                }
                //设置等值线数据
                if (DataTypeConst.GFS_ISOLINE_ELEMENT_CODE.indexOf(this.elem) > -1
                        && levelList.contains(this.level)) {
                    setIsoLineData(gribData);
                }
                if (DataTypeConst.GFS_ISOLINE_ELEMENT_CODE.indexOf("WS") > -1 &&
                        ("UU".equals(this.elem) || "VV".equals(this.elem)) && levelList.contains(this.level)) {
                    uvData.put(this.vti + "_" + this.elem + "_" + this.level + "_EN", gribData);
                }
            }
        }

    }


    /**
     * 设置默认几个要素等值线isoline
     *
     * @param gridData
     */
    private void setIsoLineData(GridData gridData) throws RuntimeException {
//        if (gridData == null) {
//            return;
//        }
//       /* if (isAreaIsoLine) {
//            FreeArea freeArea = new FreeArea(0f, 75f, 0f, 180f);
//            gridData = GridDataFreeAreaUtil.getFreeArea(gridData, freeArea);
//        }*/
//        IsolinesAttributes attr = IsolinesAttributes.createInstance(ElemCode.fromValue(this.elem), this.level);
//        HYIsolineProcess isolineProcess = IsolineProcessUtil.isolineProcess(DataType.MHDM, this.level,
//                ElemCode.fromValue(this.elem), gridData, true);
//        ArrayList<IsolinePolygon> polygonData = IsolineProcessUtil.getPolygonData(isolineProcess, attr);
//        JSONObject isoJson = IsolineProcessUtil.getIsoLineData(isolineProcess, attr);
//        //  JSONObject isoJson = IsolineProcessUtil.isoLineProcessForWni(DataType.MHWNI, this.level, ElemCode.fromValue(this.elem), gridData, false);
//        String field = this.vti + "_" + this.elem + "_" + this.level + "_EN";
//
//        //等值线
//        if (isoJson != null && !isoJson.isEmpty())
//            this.isoLineData.put(field, isoJson);
//        //需要生成等值线瓦片的要素
//        if (DataTypeConst.GFS_ISOLINE_ELEMENT_CODE_IMAGE.contains(this.elem)
//                && polygonData != null && polygonData.size() > 0) {
//            String field1 = "XHGFS_EN_" + this.elem + "_" + this.level + "_" + this.strYMDH + vti;
//            polygonDataMap.put(field1, polygonData);
//        }
    }

    /**
     * 对ws等值线的特殊处理
     *
     * @param gridData
     * @param vti
     * @param elem
     * @param level
     */
    public void setIsoLineData(GridData gridData, String vti, String elem, String level) {
//        if (gridData == null) {
//            return;
//        }
//       /* if (isAreaIsoLine) {
//            FreeArea freeArea = new FreeArea(0f, 75f, 0f, 180f);
//            gridData = GridDataFreeAreaUtil.getFreeArea(gridData, freeArea);
//        }*/
//        IsolinesAttributes attr = IsolinesAttributes.createInstance(ElemCode.fromValue(elem), this.level);
//        HYIsolineProcess isolineProcess = IsolineProcessUtil.isolineProcess(DataType.MHDM, this.level,
//                ElemCode.fromValue(elem), gridData, true);
//        JSONObject isoJson = IsolineProcessUtil.getIsoLineData(isolineProcess, attr);
//
//        //JSONObject isoJson = IsolineProcessUtil.isoLineProcessForWni(DataType.MHWNI, level, ElemCode.fromValue(elem), gridData, false);
//        String field = vti + "_" + elem + "_" + level + "_EN";
//        isoLineData.put(field, isoJson);

    }

    /**
     * 设置gfs点数据
     *
     * @param fileName
     * @param gribData
     */
    public void setPointData(String fileName, GridData gribData) throws RuntimeException {

        //0~75,0~180
       /* FreeArea freeArea = new FreeArea(0f, 75f, 0f, 180f);
        gribData = GridDataFreeAreaUtil.getFreeArea(gribData, freeArea);
        if (gribData == null) {
            logger.error("根据数据未获取到GridData，请查证，file=" + files[i].getPath());
            return null;
        }*/
        float[] latAry = gribData.getLatAry1D();
        float[] lonAry = gribData.getLonAry1D();
        float[][] dataAry = gribData.getGridData();
        //因原始数据范围是90~-90,0~359.75，故取0~90,0~180的点数据可以去row/2 和 col/2 即可
        //int row = latAry.length / 2 + 1;
        //int col = lonAry.length / 2 + 1;
        //新服务器数据采用全球数据
        int row = latAry.length ;
        int col = lonAry.length ;
        for (int i1 = 0; i1 < row; i1 = i1 + redisScale) {
            for (int i2 = 0; i2 < col; i2 = i2 + redisScale) {
                String field = String.format("%.2f", latAry[i1]) + "_" + String.format("%.2f", lonAry[i2]) /*+ "_" + strYMDH + "_" + vti*/;
                if (pointData.get(field) == null) {//不包含经纬度点
                    JSONObject object2 = new JSONObject();//GFS:level(object3)
                    JSONObject object3 = new JSONObject();//level:elem(object4)
                    JSONObject object4 = new JSONObject();//elem:value
                    object4.put(elem, dataAry[i1][i2]);
                    object3.put(level, object4);
                    object2.put(gfsSign, object3);
                    pointData.put(field, object2);
                } else {//包含经纬度点
                    JSONObject object2 = pointData.getJSONObject(field);//object2(key=GFS)
                    if (object2.get(gfsSign) == null) {//不包含GFS类型
                        JSONObject object3 = new JSONObject();//level:elem(object4)
                        JSONObject object4 = new JSONObject();//elem:value
                        object4.put(elem, dataAry[i1][i2]);
                        object3.put(level, object4);
                        object2.put(gfsSign, object3);
                    } else {//包含GFS类型
                        JSONObject object3 = object2.getJSONObject(gfsSign);//object3(key=level)
                        if (object3.get(level) == null) {//不包含层次
                            JSONObject object4 = new JSONObject();//elem:value
                            object4.put(elem, dataAry[i1][i2]);
                            object3.put(level, object4);
                        } else {//包含层次
                            object3.getJSONObject(level).put(elem, dataAry[i1][i2]);
                        }
                    }
                }
            }
        }
    }


    public Map<String, GridData> getUvData() {
        return uvData;
    }


    public static void main(String[] args) {
       /* String floderPath = "E:/download/gfsdata/2016063012/gfs.t12z.pgrb2.0p25.f006_floder";
        String strYMDH = "2016063012";
        String strVTI = "006";
        Point point = new Point(floderPath, strYMDH, strVTI);
        JSONObject jsonObject = point.getJSONObject();
        System.out.println(jsonObject);*/
    }
}
