package net.xinhong.meteoserve.common.grib;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: <br>
 *
 * @author 作者 <a href=ds.lht@163.com>stone</a>
 * @version 创建时间：2016/5/17.
 */
public class Main {


    public static void main(String[] args) {
        String[] dateStr = {"20160404", "20160405"};

        List<String> tmpList = new ArrayList<>();
        Main main = new Main();
      //  main.interpolateNormal(dateStr);
        //main.interpolateVTI(dateStr);
    }

 /*   private Map<String, String> interpolateNormal(String ...dateStr){
        Map<String, String> pathMap =getFileName(dateStr);
        Map<String, String> resMap = new HashMap<>();
        for (String key : pathMap.keySet()) {
//            System.out.println(key+":"+pathMap.get(key));
            GribDataReader reader = new GribDataReader();
            JSONObject obj = reader.getSmallWniData(pathMap.get(key), "GRIB2");
            for (String key1 : obj.keySet()) {
                GridData gridData = obj.getObject(key1, GridData.class);
                JSONObject proConf = DataTypeConst.WNI_ELEMENT_PROPERTY.getJSONObject("GRIB2").getJSONObject(key1);
                if (proConf != null) {
                    if (proConf.getString("pro").equals("TT")) {
                        float[][] vals = gridData.getGridData();
                        for (int i = 0; i < vals.length; i++) {
                            for (int j = 0; j < vals.length; j++) {
                                vals[i][j] -= 273.15;
                            }
                        }
                    }
                    JSONObject isoJson = IsolineProcessUtil.isoLineProcessForWni(DataType.MHWNI, proConf.getString("level"), ElemCode.fromValue(proConf.getString("pro")), gridData);
                    if(isoJson!=null){
                        String field = key+"_EN";
                        resMap.put(field, isoJson.toJSONString());
                    }
                }

            }
        }
        return resMap;
    }
*/

   /* private Map<String, String>  interpolateVTI(String ...dateStr){
        //String[] interpVTIArray = {"007","008","010","011","13","14","16","17","19","20","22","23","25","26","28","29","31","32","34","35"};
        String[] VTIArray = DataTypeConst.WNI_VTI;
        String[] hourArray = DataTypeConst.WNI_HOUR;
        String[] elemArray = {"HH"};
        String[] pressArray = {"0500"};
        Map<String, String> resMap = new HashMap<>();
        //500hPa 高度场插值到逐小时
        String basePath = "E:\\data\\";
        for (int i = 0; i < dateStr.length; i++) {
            for (String hour : hourArray) {
                for (int k = 1; k < VTIArray.length; k++) {
                    int index = k;
                    //1.首先找到需要插值的VTI,并计算线性插值比例
                    int preVTI = Integer.parseInt(VTIArray[index - 1]);
                    int latterVTI = Integer.parseInt(VTIArray[index]);
                    int interpVTINum = latterVTI - preVTI - 1;

                    //2.读取前后VTI数据
                    String datehourStr = dateStr[i] + hour;
                    StringBuilder preDataFileName = new StringBuilder();
                    StringBuilder latterDataFileName = new StringBuilder();
                    preDataFileName.append(basePath).append(datehourStr).append(File.separator)
                            .append("FAA_SRF_WIFS_GRIB2_KWBC_").append(datehourStr)
                            .append("_").append(VTIArray[index - 1]).append("_").append(elemArray[0])
                            .append("_").append(pressArray[0]).append(".grb");

                    latterDataFileName.append(basePath).append(datehourStr).append(File.separator)
                            .append("FAA_SRF_WIFS_GRIB2_KWBC_").append(datehourStr)
                            .append("_").append(VTIArray[index]).append("_").append(elemArray[0])
                            .append("_").append(pressArray[0]).append(".grb");

                    GribDataReader reader = new GribDataReader();
                    JSONObject preObj = reader.getSmallWniData(preDataFileName.toString(), "GRIB2");
                    JSONObject latterObj = reader.getSmallWniData(latterDataFileName.toString(), "GRIB2");
                    if ((preObj.keySet().size() == 0) || (latterObj.keySet().size() == 0))
                        continue;
                    GridData preGridData = preObj.getObject("YHX50", GridData.class);
                    GridData latterGridData = latterObj.getObject("YHX50", GridData.class);
                    //3.对前后VTI之间的VTI进行插值
                    for (int kk = 1; kk <= interpVTINum; kk++){
                        int curVTI = preVTI + kk;
                        float valScale = (curVTI - preVTI) * 1.0f / (latterVTI - preVTI) * 1.0f;
                        GridData gridData = preGridData.clone();
                        if ((preGridData == null) || preGridData.isEmpty()
                                || (preGridData == null) || preGridData.isEmpty())
                            continue;
                        float[][] vals = gridData.getGridData();
                        float[][] vals0 = preGridData.getGridData();
                        float[][] vals1 = latterGridData.getGridData();
                        for (int mm = 0; mm < vals.length; mm++) {
                            for (int nn = 0; nn < vals.length; nn++) {
                                vals[mm][nn] = vals0[mm][nn] + (vals1[mm][nn] - vals0[mm][nn]) * valScale;
                            }
                        }
                        gridData.setGridData(vals);
                        //4.追踪等值线，并保存到redis中
                        JSONObject isoJson = IsolineProcessUtil.isoLineProcessForWni(DataType.MHWNI, pressArray[0], ElemCode.HH, gridData);
                        if(isoJson!=null){
                            String field = datehourStr+"_"+StringUtils.leftPad(curVTI+"",3,"0")+"_HH_0500_EN";
                            resMap.put(field, isoJson.toJSONString());
                        }
                    }
                }
            }
        }
        return resMap;
    }*/

//    private void  interpolateVTI(String ...dateStr){
//        String[] interpVTIArray = {"007","008","010","011","13","14","16","17","19","20","22","23","25","26","28","29","31","32","34","35"};
//        String[] VTIArray = DataTypeConst.WNI_VTI;
//        String[] hourArray = DataTypeConst.WNI_HOUR;
//        String[] elemArray = {"HH"};
//        String[] pressArray = {"0500"};
//
//        //500hPa 高度场插值到逐小时
//        String basePath = "E:\\data\\";
//        for (int i = 0; i < dateStr.length; i++) {
//            for (String hour : hourArray) {
//                for (int k = 0; k < interpVTIArray.length; k++) {
//                    //1.首先找到前后的VTI,并计算线性插值比例
//                    int curVTI = Integer.parseInt(interpVTIArray[k]);
//                    int index = -1;
//                    for (int m = 0; m < VTIArray.length; m++) {
//
//                        if (curVTI < Integer.parseInt(VTIArray[m])) {
//                            index = m;
//                            break;
//                        }
//                    }
//                    if (index <= 0)
//                        continue;
//                    int preVTI = Integer.parseInt(VTIArray[index - 1]);
//                    int latterVTI = Integer.parseInt(VTIArray[index]);
//                    float valScale = (curVTI - preVTI) * 1.0f / (latterVTI - preVTI) * 1.0f;
//
//                    //2.读取前后VTI数据
//                    String datehourStr = dateStr[i] + hour;
//                    StringBuilder preDataFileName = new StringBuilder();
//                    StringBuilder latterDataFileName = new StringBuilder();
//                    preDataFileName.append(basePath).append(datehourStr).append(File.separator)
//                            .append("FAA_SRF_WIFS_GRIB2_KWBC_").append(datehourStr)
//                            .append("_").append(VTIArray[index - 1]).append("_").append(elemArray[0])
//                            .append("_").append(pressArray[0]).append(".grb");
//
//                    latterDataFileName.append(basePath).append(datehourStr).append(File.separator)
//                            .append("FAA_SRF_WIFS_GRIB2_KWBC_").append(datehourStr)
//                            .append("_").append(VTIArray[index]).append("_").append(elemArray[0])
//                            .append("_").append(pressArray[0]).append(".grb");
//
//                    GribDataReader reader = new GribDataReader();
//                    JSONObject preObj = reader.getSmallWniData(preDataFileName.toString(), "GRIB2");
//                    JSONObject latterObj = reader.getSmallWniData(latterDataFileName.toString(), "GRIB2");
//                    if ((preObj.keySet().size() == 0) || (latterObj.keySet().size() == 0))
//                        continue;
//                    GridData preGridData = preObj.getObject("YHX50", GridData.class);
//                    GridData latterGridData = latterObj.getObject("YHX50", GridData.class);
//                    //3.对当前VTI插值
//                    GridData gridData = preGridData.clone();
//                    if ((preGridData == null) || preGridData.isEmpty()
//                            || (preGridData == null) || preGridData.isEmpty())
//                        continue;
//                    float[][] vals = gridData.getGridData();
//                    float[][] vals0 = preGridData.getGridData();
//                    float[][] vals1 = latterGridData.getGridData();
//                    for (int mm = 0; mm < vals.length; mm++) {
//                        for (int nn = 0; nn < vals.length; nn++) {
//                            vals[mm][nn] = vals0[mm][nn] + (vals1[mm][nn] - vals0[mm][nn]) * valScale;
//                        }
//                    }
//                    gridData.setGridData(vals);
//                    //4.追踪等值线，并保存到redis中
//                    JSONObject isoJson = IsolineProcessUtil.isoLineProcessForWni(DataType.MHWNI, pressArray[0], ElemCode.HH, gridData);
//                    if(isoJson!=null){
//                        String key = datehourStr+"_"+interpVTIArray+"_HH_0500_EN";
//                    }
//                    System.out.println("插值结果：" + isoJson);
//                }
//            }
//        }
//    }

  /*  private Map<String, String> getFileName(String... dateStr) {
        String[] elems = DataTypeConst.WNI_ISOLINE_ELEMENT_CODE.split(",");
          String basePath = "E:\\data\\";
        //String basePath = "D:\\";
        Map<String, String> pathMap = new HashMap<>();
        for (String date : dateStr) {
            for (String elem : elems) {
                List<String> levels = DataTypeElemConfig.getLevelListFromDataTypeElem(DataType.MHWNI, ElemCode.HH);
                for (String hour : DataTypeConst.WNI_HOUR) {
                    String pathPrefix = date + hour;
                    for (String vti : DataTypeConst.WNI_VTI) {
                        String vtiKey = pathPrefix + "_" + StringUtils.leftPad(vti, 3, "0") + "_" + elem;
                        StringBuilder path = new StringBuilder();
                        path.append(basePath).append(pathPrefix).append(File.separator)
                                .append("FAA_SRF_WIFS_GRIB2_KWBC_").append(date)
                                .append(hour).append("_").append(vti).append("_").append(elem).append("_");
                        for (String level : levels) {
                            pathMap.put(vtiKey + "_" + level, path + level + ".grb");
                        }
                    }
                }
            }
        }
        return pathMap;
    }*/
}
