package net.xinhong.meteoserve.common.gfs;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ElemGFS;
import net.xinhong.meteoserve.common.tool.ConfigUtil;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import static net.xinhong.meteoserve.common.constant.DataTypeConst.GFS_FC_JSYB_FILEPATH;

/**
 * Created by wingsby on 2016/8/8.
 */
public class GfsJSYBReader {
    //大文件数据行列数
    public final static int latD = 721;
    public static int lonD = 1440;

    //redisScale设为2表示取0.5*0.5,redisScale设为4表示1*1
    private static final int redisScale = 2;

    private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    //等级逆序
    final static String[] leveles = new String[]{
            "0010", "0020", "0030", "0050", "0070", "0100", "0150",
            "0200", "0250", "0300", "0350", "0400", "0450", "0500", "0550", "0600", "0650",
            "0700", "0750", "0800", "0850", "0900", "0925", "0950", "0975", "1000"
    };
    //GFS需要的高度层信息
    private static final String needLevel = "0100,0200,0300,0400,0500,0700,0850,0925,1000";
    private static final String JSYB_FILENAME_PREFIX = "XHGFS_G_";
    private static final String JSYB_EXPANDED_NAME = ".dat";



    public static void main(String[] args) {
        readAreaData("20160906", "12", "022", "0100", ElemGFS.TURB);
    }

    /**
     * 根据日期、vti、要素读取小文件数据（区域 EN）
     *
     * @param ymd   format：yyyyMMdd
     * @param hour
     * @param vti
     * @param press
     * @param elem  现在没有 CAT("CAT", "晴空颠簸"),
     */
    public static GFSData readAreaData(String ymd, String hour, String vti, String press, ElemGFS elem) {
        hour = StringUtils.leftPad(hour, 2, '0');
        vti = StringUtils.leftPad(vti, 3, '0');
        press = StringUtils.leftPad(press, 4, '0');
        RandomAccessFile rf = null;
        String filePath = ConfigUtil.getProperty(GFS_FC_JSYB_FILEPATH) + File.separator + ymd + hour + File.separator
                + JSYB_FILENAME_PREFIX + elem.getFileCode() + "_" + press + "_" + ymd + hour + vti + JSYB_EXPANDED_NAME;
//        String filePath = "/Users/xiaoyu/jsybres" + File.separator + ymd + hour + File.separator
//                + JSYB_FILENAME_PREFIX + elem.getFileCode() + "_" + press + "_" + ymd + hour + vti + JSYB_EXPANDED_NAME;
        if (!(new File(filePath)).exists())
            return null;
        GFSData gfsData = new GFSData();
        try {
            rf = new RandomAccessFile(filePath, "r");
            ByteBuffer byteBuffer = ByteBuffer.allocate(20);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            rf.read(byteBuffer.array());
            float slon = byteBuffer.getFloat();
            float slat = byteBuffer.getFloat();
            float elon = byteBuffer.getFloat();
            float elat = byteBuffer.getFloat();
            float resolution = byteBuffer.getFloat();

            int tmpLat = (int) ((elat - slat) / resolution) + 1;
            int tmplon = (int) ((elon - slon) / resolution) + 1;

            ByteBuffer byteBuffer1 = ByteBuffer.allocate(tmpLat * tmplon * 4);
            byteBuffer1.order(ByteOrder.LITTLE_ENDIAN);
            rf.read(byteBuffer1.array());
            float[][] singleres = new float[tmplon][tmpLat];
            for (int i = 0; i < tmpLat; i++) {
                for (int j = 0; j < tmplon; j++) {
                    singleres[j][i] = byteBuffer1.getFloat();
                }
            }
            gfsData.setData(singleres);
            gfsData.setSlat(slat);
            gfsData.setElat(elat);
            gfsData.setSlng(slon);
            gfsData.setElng(elon);
            gfsData.setScale(resolution);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            //XHGFS_G_WS_0500_2016091400008.dat
            e.printStackTrace();
        } finally {
            try {
                if (rf != null)
                    rf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return gfsData;
    }


    /**
     * 积冰、颠簸全球单点数据
     *
     * @param jbFile
     * @param dbFile
     * @return
     */
    public static Map<String, String> readData(File jbFile, File dbFile) {
        RandomAccessFile jbrf = null;
        RandomAccessFile dbrf = null;
        Map<String, String> dataMap = new HashMap<>();
        try {
            jbrf = new RandomAccessFile(jbFile, "r");
            dbrf = new RandomAccessFile(dbFile, "r");
            float[][] singleres = new float[lonD][latD];
            for (int k = 0; k < leveles.length; k++) {
                if (needLevel.indexOf(leveles[k]) < 0) {
                    continue;
                }
                ByteBuffer jbByteBuffer = ByteBuffer.allocate(latD * lonD * 4);
                ByteBuffer dbByteBuffer = ByteBuffer.allocate(latD * lonD * 4);
                jbByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                jbrf.read(jbByteBuffer.array());
                dbrf.read(dbByteBuffer.array());

                for (int i = 0; i < latD; i += redisScale) {
                    for (int j = 0; j < lonD; j += redisScale) {
                        float jbValue = jbByteBuffer.getFloat();
                        float dbValue = dbByteBuffer.getFloat();
                        if (jbValue <= 0 && dbValue <= 0) {
                            continue;
                        }
                        String filed = decimalFormat.format(i * 0.25) + "_" + decimalFormat.format(j * 0.25);
                        JSONObject levelObj = dataMap.get(filed) == null ? new JSONObject() : JSONObject.parseObject(dataMap.get(filed));
                        JSONObject valObj = new JSONObject();
                        if (jbValue > 1e-9)
                            valObj.put(ElemGFS.ICE.getEname(), jbValue);
                        if (dbValue > 1e-9)
                            valObj.put(ElemGFS.TURB.getEname(), dbValue);
                        levelObj.put(leveles[k], valObj);
                        dataMap.put(filed, levelObj.toJSONString());
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (jbrf != null)
                    jbrf.close();
                if (dbrf != null)
                    dbrf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dataMap;
    }

    public static class GFSData {

        private float[][] data;
        private float slat;
        private float elat;
        private float slng;
        private float elng;
        private float scale;

        public float getScale() {
            return scale;
        }

        public void setScale(float scale) {
            this.scale = scale;
        }

        public float[][] getData() {
            return data;
        }

        public void setData(float[][] data) {
            this.data = data;
        }

        public float getSlat() {
            return slat;
        }

        public void setSlat(float slat) {
            this.slat = slat;
        }

        public float getElat() {
            return elat;
        }

        public void setElat(float elat) {
            this.elat = elat;
        }

        public float getSlng() {
            return slng;
        }

        public void setSlng(float slng) {
            this.slng = slng;
        }

        public float getElng() {
            return elng;
        }

        public void setElng(float elng) {
            this.elng = elng;
        }
    }
}