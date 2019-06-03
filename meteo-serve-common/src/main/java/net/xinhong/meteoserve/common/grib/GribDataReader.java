package net.xinhong.meteoserve.common.grib;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.tool.ConfigUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import xinhong_ucar.grib.GribReaderUtil;
import xinhong_ucar.grib.grib2.Grib2Data;
import xinhong_ucar.unidata.io.RandomAccessFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;


/**
 * Description: <br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 邓帅
 * @version 创建时间：2016/4/8 0008.
 */
public class GribDataReader {

    private static final Log logger = LogFactory.getLog(GribDataReader.class);


    public GribDataReader() {

    }

    private static int LengthGribII = 4; //文件长度和段长所占的字节数
    private int GribVersion = 0;
    //获取长度所在的字节数
    private byte[] GribByte = null;
    private byte[] b = new byte[1];
    private RandomAccessFile raf1 = null;


    /**
     * @param dateStr
     * @param
     * @return
     */
    private JSONObject getFileName(String dateStr) {
        JSONObject dataObj = new JSONObject();
        DecimalFormat df=new DecimalFormat("000");
        for (String hour : DataTypeConst.WNI_HOUR) {

            File f = new File(ConfigUtil.getProperty(DataTypeConst.PROCESS_WNI_FILE_PATH) + dateStr + hour + File.separator);
            if (!f.isDirectory()) {
                logger.error(f.getPath()+"不是目录！");
                return dataObj;
            }
            File[] files = f.listFiles();

            lab1:
            for (File file : files) {
                for (String el : DataTypeConst.WNI_ELEMENT) {
                    for (String vti : DataTypeConst.WNI_VTI) {
                        String tmpVti = df.format(Integer.valueOf(vti));
                        String vtiKey = dateStr + hour + "_" + tmpVti;
                        JSONObject vtiObj = dataObj.getJSONObject(vtiKey) == null ? new JSONObject() : dataObj.getJSONObject(vtiKey);
                        if (file.getName().indexOf("_" + el + "_") > -1 && file.getName().endsWith(vti)) {
                            vtiObj.put(el, file.getPath());
                            dataObj.put(vtiKey, vtiObj);
                            continue lab1;
                        }

                    }
                }
            }
        }
        return dataObj;
    }


    /**
     * 获取每一份grib2 文件的的属性
     */
    private String getElementPro(String el) {

        String level = null;
        try {
            byte[] headValue = new byte[31];
            raf1.read(headValue);
            String headStr = BasicDataTypeConversion.byteToChar(headValue);
            int headIndex = headStr.indexOf("KWBC");
            if (headIndex < 0) {
                return level;
            }
            if (isGribFile()) {
                try {
                    byte[] sectGrib2 = new byte[4];
                    raf1.read(sectGrib2);
                    GribVersion = GribReaderUtil.byteToInt(sectGrib2[3]);
                    if (GribVersion != 2) {
                        logger.info("文件不是grib2文件，文件名"+ raf1.getLocation());
                        return level;
                    } else {
                        level = headStr.substring(headIndex - 6 - 1, headIndex - 1);
                        level = level.substring(0, 3) + level.substring(4);

                        GribByte = new byte[8];
                        raf1.read(GribByte);
                        JSONObject elPro = DataTypeConst.WNI_ELEMENT_PROPERTY.getJSONObject(el);
                        if (level != null && !elPro.containsKey(level)) {
                            int fLength = GribReaderUtil.Value(GribByte, new int[]{0}, 8 * 8);
                            //raf1.skipBytes(fLength - 4 - 4);
                            raf1.skipBytes(fLength - 4 - 4 - 4);
                            logger.info(level+"是不需要的解析要素，解析下一要素！");
                            return getElementPro(el);
                        }
                    }
                } catch (Exception e) {
                    logger.info("读取GRIB2文件失败，文件名{}"+raf1.getLocation(), e);
                    try {
                        raf1.close();
                    } catch (IOException e1) {
                    }
                }
            }

        } catch (IOException e) {
            logger.error("解析"+raf1.getLocation()+"文件异常,获取要素属性失败！", e);
        }
        return level;
    }




    /**
     *
     * @param dateStr  format:yyyy-MM-dd
     * @param createSmall 是否生成小文件
     * @return
     */
    public JSONObject getGribData(String dateStr,boolean createSmall) {
        JSONObject resultData = new JSONObject();
        JSONObject filesObj = getFileName(dateStr);
        for (String s1 : filesObj.keySet()) {
            try {
                JSONObject filePathObj = filesObj.getJSONObject(s1);
                JSONObject elObj = new JSONObject();
                for (String s : filePathObj.keySet()) {
                    raf1 = new RandomAccessFile(filePathObj.getString(s), "rw");
                    JSONObject dataObj = new JSONObject();
                    end = 0l;
                    skipBytes = 0l;
                    parserGrib2(dataObj, s,createSmall);
                    elObj.put(s, dataObj);
                }
                resultData.put(s1, elObj);
            } catch (IOException e) {
                logger.error("读取文件异常"+s1 , e);
            }
        }
        return resultData;
    }



   /* public static void main(String[] args) {

        GribDataReader reader = new GribDataReader();
        JSONObject data = reader.getGribData("20160401");
        int lll= 0 ;
        for (String dateStr : data.keySet()) {
            //某天、某个起报时间、某个VTI、所有格点、所有要素、所有层次的数据
            // JSONObject elObj = new JSONObject();
            Map<String, String> dataMap = new HashMap<>();
            JSONObject dayVtiData = data.getJSONObject(dateStr);
            for (String el : dayVtiData.keySet()) {
                JSONObject proObjs = DataTypeConst.WNI_ELEMENT_PROPERTY.getJSONObject(el);
                JSONObject elData = dayVtiData.getJSONObject(el);
                for (String proKey : proObjs.keySet()) {
                    JSONObject wniData = elData.getJSONObject(proKey);
                    JSONObject proObj = proObjs.getJSONObject(proKey);
                    JSONArray lonArray = wniData.getJSONArray("lonData");
                    JSONArray latArray = wniData.getJSONArray("latData");
                    JSONArray dataArray = wniData.getJSONArray("gridData");
                    String level = proObj.getString("level");
                    String pro = proObj.getString("pro");
                    for (int i = 0; i < lonArray.size(); i++) {
                        for (int l = 0; l < latArray.size(); l++) {
                            if (dataArray.getJSONArray(l).getFloat(i) > 0) {

                                //一个点的某个、某个层次、某个要素的某个属性数据
                                JSONObject levelData = new JSONObject();

                                String key = latArray.getString(l) + "_" + lonArray.getString(i) + "_" + dateStr;

                                JSONObject elData1 = new JSONObject();
                                //   elData1 = elObj.getJSONObject(key)==null ? elObj : elObj.getJSONObject(key);

                                // elData1 = elObj.getJSONObject(key) == null ? elData1 : elObj.getJSONObject(key);
                                elData1 = dataMap.get(key) == null ? elData1 : JSONObject.parseObject(dataMap.get(key));

                                levelData = elData1.getJSONObject(el) == null ? levelData : elData1.getJSONObject(el);
                                if (proObj.getString("level") != null) {
                                    // JSONObject d = pointData.getJSONObject(level) == null ? new JSONObject() : pointData.getJSONObject(level);
                                    JSONObject proData = new JSONObject();
                                    proData = levelData.getJSONObject(level) == null ? proData : levelData.getJSONObject(level);
                                    proData.put(pro, dataArray.getJSONArray(l).getFloat(i));
                                    levelData.put(level, proData);
                                } else {
                                    //CB 没有level(层次)
                                    levelData.put(pro, dataArray.getJSONArray(l).getFloat(i));
                                }

                                elData1.put(el, levelData);
                                //elObj.put(key, elData1);
                                dataMap.put(key, elData1.toJSONString());
                            }
                        }
                    }
                }
            }

            //   todo: 保存某天、某个起报时间、某个VTI、所有格点、所有要素、所有层次的数据
           *//* for (String s : dataMap.keySet()) {
                FileUtil.appendFile("D:\\jsondata\\json"+lll+".jspn",s+":"+dataMap.get(s),true);
            }
            lll++;*//*

        }

    }*/


    private Long end = 0L;
    private Long skipBytes = 0l;


    /**
     * wni 小文件中获取数据
     * @param filePath
     * @param el
     * @return
     */

    public JSONObject getSmallWniData(String filePath,String el){

        JSONObject resultData = new JSONObject();
        try {

            File file = new File(filePath);
            if(!file.exists()){
                logger.info("文件不存在"+filePath);
                return resultData;
            }
            raf1 = new RandomAccessFile(filePath, "rw");
            parserGrib2(resultData, el,false);
        } catch (Exception e) {
           logger.error("解析wni小文件异常"+filePath,e);
        }
        return resultData;
    }


    public static void main(String[] args) {
        GribDataReader reader = new GribDataReader();
        JSONObject obj = reader.getSmallWniData("E:\\air\\2016040400\\FAA_SRF_WIFS_TURB_KWBC_2016040400_36_MEAN_0700.grb", "TURB");
    }

    /**
     *
     * @param dataObj
     * @param el
     * @param createSmall 是否生成小文件
     * @return
     */
    private JSONObject parserGrib2(JSONObject dataObj, String el,boolean createSmall) {

        String elProKey = getElementPro(el);
        if (elProKey == null) {
            logger.info("解析要素属性信息失败！，文件名"+ raf1.getLocation());
            return null;
        }
        GridData gridData = new GridData();
        try {

            GribByte = new byte[LengthGribII];

            while (true) {
                raf1.read(GribByte);
                int sectLength = GribReaderUtil.Value(GribByte, new int[]{0}, 8 * LengthGribII);
                raf1.read(b);
                int code = GribReaderUtil.Value(b, new int[]{0}, 8);
                if (code == 3) {
                    Grib2Data data = new Grib2Data(raf1);
                    long gg = raf1.getFilePointer();
                    data.getData1(gg - 5, 0, 0);
                    int row = data.getGds().getNy();
                    int col = data.getGds().getNx();
                    float lat1 = data.getGds().getLa1();
                    float lat2 = data.getGds().getLa2();
                    float lon1 = data.getGds().getLo1();
                    float lon2 = data.getGds().getLo2();
                    float dx = data.getGds().getDx();
                    float dy = data.getGds().getDy();

                    float[] dataAry = data.getDataAry();
                    gridData.setRowNum(row);
                    gridData.setColNum(col);
                    gridData.setXStart(lon1);
                    gridData.setXEnd(lon2);
                    gridData.setYStart(lat1);
                    gridData.setYEnd(lat2);
                    gridData.setXDel(dx);
                    gridData.setYDel(dy);
                    float[] lats = new float[row];
                    float[] lons = new float[col];
                    float[][] dataAry2 = new float[row][col];
                    int sign = 1;
                    if (lat2 < lat1)
                        sign = -1 * sign;
                    for (int i = 0; i < row; i++)
                        lats[i] = lat1 + sign * i * dx;
                    for (int i = 0; i < col; i++)
                        lons[i] = lon1 + i * dy;
                    gridData.setLatAry1D(lats);
                    gridData.setLonAry1D(lons);

                    float scale = 1.0f;
                    float offset = 0.0f;
                    int index = -1; //int index = 0; 为index ++;写在调用前面，故索引开始值赋为-1
                    for (int i = 0; i < row; i++) {
                        for (int j = 0; j < col; j++) {
                            index++;
                            dataAry2[i][j] = dataAry[index] * scale + offset;
                        }
                    }
                    gridData.setGridData(dataAry2);
                   // dataObj.put(elProKey, gridData.dataToJSON());
                    // TODO: 2016/4/27 可修改为以下方式
                     dataObj.put(elProKey, gridData);
                    GribByte = new byte[8];
                    raf1.read(GribByte);
                    if (BasicDataTypeConversion.byteToChar(GribByte).indexOf("7777") > -1) {
                        //是否创建小文件
                        if (createSmall) {
                            skipBytes = end;
                            end = raf1.getFilePointer();
                            String fileNameSuffix = raf1.getLocation().substring(raf1.getLocation().lastIndexOf(File.separator) + 1);
                            String pathPrefix = raf1.getLocation().substring(0, raf1.getLocation().lastIndexOf(File.separator));
                            JSONObject obj = DataTypeConst.WNI_ELEMENT_PROPERTY.getJSONObject(el);
                            JSONObject pro = obj.getJSONObject(elProKey);
                            String fullPath = null;
                            if (pro.get("pro") != null) {
                                fullPath = pathPrefix + File.separator + fileNameSuffix + "_" + pro.get("pro");
                            }
                            if (pro.get("level") != null) {
                                fullPath = fullPath + "_" + pro.get("level");
                            }
                            if (fullPath != null)
                                fullPath += ".grb";
                            createSmallFile(fullPath);
                        }
                        // TODO: 2016/4/12 0012 raf1.isAtEndOfFile() 方法判断不准确
                        if (raf1.getFilePointer() == raf1.getRandomAccessFile().length()) {
                            raf1.close();
                            return dataObj;
                        } else {
                            return parserGrib2(dataObj, el,createSmall);
                        }
                    }
                } else if ((code > 0 && code < 3)) {
                    //直接跳过该段
                    //跳段长-5位
                    byte[] bb = new byte[sectLength - LengthGribII - 1];
                    raf1.read(bb);
                }/*else{
                   第3段到底7段数据在里面已经读取
                }*/
            }
        } catch (Exception e) {
            logger.info("解码失败，文件名"+ raf1.getLocation(), e);
            try {
                raf1.close();
            } catch (IOException e1) {
            }
            return null;
        }
    }

    private void createSmallFile(String path) {
        java.io.RandomAccessFile rf = null;
        FileOutputStream out = null;
        try {
            rf = new java.io.RandomAccessFile(raf1.getLocation(), "rw");
            byte[] byteData = new byte[end.intValue() - skipBytes.intValue()];
            rf.skipBytes(skipBytes.intValue());
            rf.read(byteData);
            out = new FileOutputStream(path);
            out.write(byteData);
        } catch (IOException e) {
            logger.error(path+"文件生成失败", e);
        } finally {
            try {
                if (rf != null)
                    rf.close();
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private boolean isGribFile() {
        boolean isGrib = false;
        byte[] gribs = new byte[4];
        try {
            raf1.read(gribs);
            //cnt += 4;
            if ("GRIB".equals(BasicDataTypeConversion.byteToChar(gribs))) {
                isGrib = true;
            }
        } catch (IOException e) {
            logger.error(raf1.getLocation()+"不是grib 文件！", e);
        }

        return isGrib;
    }

}
