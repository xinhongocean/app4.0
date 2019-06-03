package com.xinhong.mids3d.weatherauxiliary;

import com.alibaba.fastjson.JSONObject;
import com.xinhong.mids3d.syno.util.Station;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Set;

/**
 * 绘制温度对数压力图。可直接获取绘制图片的BufferImage对象，也可以将绘制结果保存到文件中。</br>
 * 使用示例:</p>
 * Station station = new Station();</br>
 * station.setCHNName("北京");</br>
 * station.setID("54511");</br>
 * HighTLogP tlogP = new HighTLogP(station, "2015", "01", "20", "08");</br>
 * tlogP.setImageSaveFile("d:/tlogPimage.png");</br>
 * if (tlogP.searchData()){</br>
 * BufferedImage resImage = tlogP.drawTLogPImage(1200, 800);</br>
 * System.out.println(resImage.toString());</br>
 * }</br>
 *
 * @author liuxc
 */
public class HighTLogP {
    private Station station;
    private String year;
    private String month;
    private String day;
    private String hour;



    //海平面假相当位温
    private  double tb0;

    private double[] pressData = null;
    private double[] HHData = null;
    private double[] ATData = null;
    private double[] TDData = null;
    private double[] WSData = null;
    private double[] WDData = null;


    public double getTb0() {
        return tb0;
    }

    /**
     * 海平面假相当位温
     * @param tb0
     */
    public void setTb0(double tb0) {
        this.tb0 = tb0;
    }
//    protected ScatterDataReader dataReader = null;

    /**
     * @param station-高空站
     * @param year
     * @param month
     * @param day
     * @param hour
     */
    public HighTLogP(Station station, String year, String month, String day, String hour) {
        this.station = station;
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;

        //	searchData();
        //	drawTLogPImage();
    }

    public Station getStation() {
        return this.station;
    }

    public void setStationID(Station station) {
        this.station = station;
    }

    public String getYear() {
        return this.year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return this.month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return this.day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getHour() {
        return this.hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    private String imageFilePath = null;

    /**
     * 设置绘制结果存放的文件全路径
     *
     * @param imageFilePath
     */
    public void setImageSaveFile(String imageFilePath) {
        this.imageFilePath = imageFilePath;
    }


    public boolean setData(JSONObject dataObj) {
        if(dataObj==null || dataObj.size() == 0){
            return false;
        }
        int dataLength = dataObj.size();
        this.pressData = new double[dataLength];
        this.HHData = new double[dataLength];
        this.ATData = new double[dataLength];
        this.TDData = new double[dataLength];
        this.WSData = new double[dataLength];
        this.WDData = new double[dataLength];
        int index = 0;
        String[] keys = sortKey(dataObj.keySet());

        for (int i = 0; i < keys.length; i++) {
            JSONObject data = dataObj.getJSONObject(keys[i]);
            this.pressData[index] = Double.parseDouble(keys[i]);
            this.HHData[index] = convert2Num(data.getString("HH"));
            this.ATData[index] = convert2Num(data.getString("TT"));
            this.TDData[index] = convert2Num(data.getString("TD"));
            this.WSData[index] = convert2Num(data.getString("WS"));
            this.WDData[index] = convert2Num(data.getString("WD"));
            index++;
        }
        return true;
    }

    private double convert2Num(String str) {
        if (str == null || "".equals(str.trim())) {
            return 9999.0;
        } else {
            return Double.parseDouble(str);
        }
    }

    private String[] sortKey(Set<String> keySet) {
        String[] resKeys = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(resKeys);
        return resKeys;
    }

   /* public boolean searchData() {
        if (this.dataReader == null)
            this.dataReader = new ScatterDataReader();
        this.dataReader.reset();

        this.pressData = null;
        this.HHData = null;
        this.ATData = null;
        this.TDData = null;
        this.WSData = null;
        this.WDData = null;

        String[] selectColumns = new String[]{"PRESS", "HH", "AT", "TD", "WS", "WD"};

        // 获取数据
        ScatterCondition condition = new ScatterCondition();
        condition.setStationList(new String[]{this.station.getID()});
        condition.setDataType(DataType.GKQX);
        condition.setYear(this.year);
        condition.setMonth(this.month);
        condition.setDay(this.day);
        condition.setHour(this.hour);
        condition.setTimeZone(TimeZone.UTC_8);
        condition.setSelectColumns(selectColumns);
        this.dataReader.setCondition(condition);
        ArrayList<MIDSData> resDataList = new ArrayList<MIDSData>();
        if (this.dataReader.getData() == null) {
            return false;
        }
        for (MIDSData md : this.dataReader.getData()) {
            resDataList.add(md);
        }
        ScatterData sd = (ScatterData) resDataList.get(0);
        String[][] dataAry = sd.getAllData();
        if (dataAry == null) {
            return false;
        }

        int pressIndex = sd.getColumIndexFromName("PRESS");
        int HHIndex = sd.getColumIndexFromName("HH");
        int ATIndex = sd.getColumIndexFromName("AT");
        int TDIndex = sd.getColumIndexFromName("TD");
        int WSIndex = sd.getColumIndexFromName("WS");
        int WDIndex = sd.getColumIndexFromName("WD");

        double[] keyAry = new double[dataAry.length];
        HashMap<Double, Integer> sortIndex = new HashMap<Double, Integer>();
        for (int i = 0; i < dataAry.length; i++) {
            if (!dataAry[i][pressIndex].equals("")) {
                keyAry[i] = Double.parseDouble(dataAry[i][pressIndex]);
            } else {
                keyAry[i] = 9999.0;
            }
            sortIndex.put(keyAry[i], i);
        }
        Arrays.sort(keyAry);

        this.pressData = new double[dataAry.length];
        this.HHData = new double[dataAry.length];
        this.ATData = new double[dataAry.length];
        this.TDData = new double[dataAry.length];
        this.WSData = new double[dataAry.length];
        this.WDData = new double[dataAry.length];

        for (int i = 0; i < keyAry.length; i++) {
            int index = sortIndex.get(keyAry[(keyAry.length - 1 - i)]);

            this.pressData[i] = 9999.0;
            if (!dataAry[index][pressIndex].equals("")) {
                this.pressData[i] = Double.parseDouble(dataAry[index][pressIndex]);
            }
            this.HHData[i] = 99990.0;
            if (!dataAry[index][HHIndex].equals("")) {
                this.HHData[i] = Double.parseDouble(dataAry[index][HHIndex]);
            }
            this.ATData[i] = 9999.0;
            if (!dataAry[index][ATIndex].equals("")) {
                this.ATData[i] = Double.parseDouble(dataAry[index][ATIndex]);
            }
            this.TDData[i] = 9999.0;
            if (!dataAry[index][TDIndex].equals("")) {
                this.TDData[i] = Double.parseDouble(dataAry[index][TDIndex]);
            }
            this.WSData[i] = 9999.0;
            if (!dataAry[index][WSIndex].equals("")) {
                this.WSData[i] = Double.parseDouble(dataAry[index][WSIndex]);
            }
            this.WDData[i] = 9999.0;
            if (!dataAry[index][WDIndex].equals("")) {
                this.WDData[i] = Double.parseDouble(dataAry[index][WDIndex]);
            }
        }
        sortIndex.clear();
        sortIndex = null;
        return true;
    }*/

    /**
     * 绘制TLogP图到指定的文件中(为保证图片不变形，一般长宽设置比例为3:2)
     *
     * @param imageWidth-设置图片宽度(800-1600)
     * @param imageHeight-设置图片高度(600-1400)
     * @return-绘制在内存中的图片BufferedImage,如果失败，返回null
     */
    public BufferedImage drawTLogPImage(int imageWidth, int imageHeight) {

        if (this.pressData == null)
            return null;
        if (imageWidth > 1600)
            imageWidth = 1600;
        if (imageWidth < 800)
            imageWidth = 800;
        if (imageHeight > 1400)
            imageHeight = 1400;
        if (imageHeight < 600)
            imageHeight = 600;
        final int IMAGEWIDTH = 1200;
        final int IMAGEHEIGHT = 800;

        BufferedImage bImage = new BufferedImage(IMAGEWIDTH, IMAGEHEIGHT, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = bImage.createGraphics();
        // 背景色
        g.setColor(new Color(240, 255, 255, 255));
        //  填充背景
        g.fillRect(0, 0, IMAGEWIDTH, IMAGEHEIGHT);
        HighTLogPDrawer.DrawTLogP(g, IMAGEWIDTH, IMAGEHEIGHT, this.station, this.year, this.month, this.day, this.hour,
                this.pressData, this.HHData, this.ATData, this.TDData, this.WSData, this.WDData,tb0);

        g.dispose();

        //  保存图片为本地文件
        FileOutputStream out;
        try {
            String imageAddress = null;
            if (imageFilePath == null || imageFilePath.isEmpty()) {
                imageAddress = ClassLoader.getSystemResource("").getPath();
                imageAddress += "com/xinhong/resource/web/tmp/images/";
                imageAddress += "tlogp_" + this.station.getID() + "_" + this.year + this.month + this.day + this.hour + ".png";
            } else {
                imageAddress = imageFilePath;
            }
            File file = new File(imageAddress);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            out = new FileOutputStream(imageAddress);

//			int outWidth = IMAGEWIDTH * 7 / 10;
//			int outHeight = IMAGEHEIGHT * 7 / 10;
            BufferedImage outImage;
            if (imageWidth != IMAGEWIDTH || imageHeight != IMAGEHEIGHT) {
                outImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
                // Image.SCALE_SMOOTH的缩略算法，生成缩略图片的平滑度的优先级比速度高，生成的图片质量比较好，但速度慢。
                outImage.getGraphics().drawImage(bImage.getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH), 0, 0, null);
                //		outImage = (BufferedImage) bImage.getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);
            } else {
                outImage = bImage;
            }
            ImageIO.write(outImage, "png", out);
            out.flush();
            out.close();
            return outImage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 释放内存
//		bImage.flush();
//		bImage = null;
        return null;
    }

    public static void main(String[] args) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Station station = new Station();
        station.setCHNName("北京");
        station.setID("54511");
        HighTLogP tlogP = new HighTLogP(station, "2015", "01", "20", "08");
        tlogP.setTb0(2.2);
        tlogP.setImageSaveFile("d:/tlogPimage.png");
       if (tlogP.setData(new JSONObject())) {
            BufferedImage resImage = tlogP.drawTLogPImage(1200, 800);
            System.out.println(resImage.toString());
        }

    }
   /* public static double getDmTB(Station station, String year, String month, String day, String hour){
        double result = 0.0;
        double slp, at, td;						//SLP -> PR
        ElemCode[] selectCols = new ElemCode[]{ElemCode.SLP, ElemCode.AT, ElemCode.TD};
        // 获取数据
        ScatterCondition condition = new ScatterCondition();
        condition.setStationList(new String[]{station.getID()});
        condition.setDataType(DataType.DMQX);
        condition.setYear(year);
        condition.setMonth(month);
        condition.setDay(day);
        condition.setHour(hour);
        condition.setTimeZone(TimeZone.UTC_8);
        condition.setSelectColumn(selectCols);
        ScatterDataReader dr = new ScatterDataReader();
        dr.setCondition(condition);
        ArrayList<MIDSData> dataList = new ArrayList<MIDSData>();
        if (dr.getData() == null)
        {
            return -9999.0;
        }
        for (MIDSData md : dr.getData())
        {
            dataList.add(md);
        }
        ScatterData sd = (ScatterData)dataList.get(0);
        String[][] sourceData = sd.getAllData();
        String slpstring = sourceData[0][0];
        String atstring = sourceData[0][1];
        String tdstring = sourceData[0][2];
        if(slpstring==null||slpstring.isEmpty()||
                atstring==null||atstring.isEmpty()||
                tdstring==null||tdstring.isEmpty()){
            return -9999.0;
        }
        slp = Double.parseDouble(slpstring);
        at = Double.parseDouble(atstring);
        td = Double.parseDouble(tdstring);
        result = WeatherElemSinglePointCalc.tb(at, slp, td, false);
        return result;
    }*/
}
