package net.xinhong.meteoserve.common.cloudy;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by apple on 16/8/8.
 */
public class CloudyDataProcessing {

    private final static String IMAGETYPE = "png";
    private final Log logger = LogFactory.getLog(CloudyDataProcessing.class);

    private String imageCloudyPathDr = "";

    protected String paletteFilePath = "";
    private String cloudyDataPathSr = "";

    final private static int opacity_default = 120;
    protected int opacity = opacity_default;

    private static DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyyMMddHHmm");

    private float latStart = 0;
    private float latEnd = 0;
    private float lonStart = 0;
    private float lonEnd = 0;

    private YtData cloudData = null;

    private int resWidth = 0;
    private int resHeight = 0;

    private int dataWidth = 0;
    private int dataHeight = 0;


    private int year = 0;
    private int month = 0;
    private int day = 0;
    private int hour = 0;
    private int minute = 0;

    //数据文件文件名称
    private String fileName;

    private String imageName;

    private String dateStr;


    public void setCloudyDataPathSr(String path) {
        cloudyDataPathSr = path;
    }

    public void setImageCloudyPathDr(String path) {
        imageCloudyPathDr = path;
    }

    public void setPaletteFilePath(String path) {
        paletteFilePath = path;
    }

    public int getImageWidth() {
        return resWidth;
    }

    public int getImageHeight() {
        return resHeight;
    }

    public double getLatStart() {
        return latStart;
    }

    public double getLatEnd() {
        return latEnd;
    }

    public double getLonStart() {
        return lonStart;
    }

    public double getLonEnd() {
        return lonEnd;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    /**
     * 以转换为北京时
     *
     * @return yyyyMMddHHmm
     */
    public String getDateStr() {
        return dateStr;
    }

    public void readCloudyData() throws RuntimeException {
        if (cloudyDataPathSr.equals("")) {
            logger.error("云图源文件路径为空！", new FileNotFoundException());
            return;
        }

        if (paletteFilePath.equals("")) {
            paletteFilePath = "/cloudy/26_default.pal";
        }

        if (imageCloudyPathDr.equals("")) {
            // imageCloudyPathDr = System.getProperty("user.dir") + "/image/cloud.png";
            logger.error("云图源文件路径为空！", new FileNotFoundException());
            return;
        }

        try {
            YtNCSNFileParser parser = new YtNCSNFileParser();
            MIDSData midsData = parser.readNCSNfile(cloudyDataPathSr);
            fileName = parser.getFileName().substring(0, parser.getFileName().lastIndexOf("."));
            year = parser.getYear();
            month = parser.getMonth();
            day = parser.getDay();
            hour = parser.getHour();
            minute = parser.getMinute();

            this.dateStr = year + StringUtils.leftPad(String.valueOf(month), 2, '0')
                    + StringUtils.leftPad(String.valueOf(day), 2, '0') +
                    StringUtils.leftPad(String.valueOf(hour), 2, '0')
                    + StringUtils.leftPad(String.valueOf(minute), 2, '0');
            DateTime dateTime = dateformat.parseDateTime(dateStr).plusHours(8);
            this.dateStr = dateTime.toString("yyyyMMddHHmm");
            cloudData = (YtData) midsData;

            if (cloudData == null || cloudData.getYtData().length == 0) {
                logger.warn("没有图像数据，不生成图片文件！");
                return;
            }

            createCloudImage();
        } catch (FileNotFoundException e) {
            logger.error("文件没找到", e);
        }
    }


    private void createCloudImage() {

        lonStart = this.cloudData.getXStart();
        lonEnd = this.cloudData.getXEnd();
        latStart = this.cloudData.getYEnd();
        latEnd = this.cloudData.getYStart();

        if (lonStart > lonEnd) {
            float temp = lonStart;
            lonStart = lonEnd;
            lonEnd = temp;
        }

        if (latStart > latEnd) {
            float temp = latStart;
            latStart = latEnd;
            latEnd = temp;
        }

        // 生成透明度数组
        int[] alphaAry = new int[256];
        float scale = 1.2f;
        for (int i = 0; i < 256; i++) {
            if (i < 140) {
                alphaAry[i] = 0;
            } else if (i >= 140 && i < 200) {
                alphaAry[i] = (int) (scale * ((this.opacity - 100) * (250 - i / 5) / 150 + i / 5));
            } else if (i >= 200 && i < 220) {
                alphaAry[i] = (int) (scale * ((this.opacity - 100) * (250 - (i * 3 - 410)) / 150 + (i * 3 - 410)));
            } else {
                alphaAry[i] = 255;
            }
        }

        BufferedImage cloudImage = CloudPictrueDataRead.readColorBoardFile(this.cloudData, this.paletteFilePath, alphaAry);
        this.imageName = fileName + "_" + getDateStr() + "." + IMAGETYPE;

        String projectType = fileName.substring(2, 3);

        if(projectType.equals("L")){

            // TODO: 2016/8/12 :目前兰伯特投影转换仍有问题！
            //兰勃托投影的云图数据直接保存
            //processBufferedImage(cloudImage);

        }
        else if(projectType.equals("N")){


            processMKTBufferImage(cloudImage);


            //墨卡托投影的云图数据直接保存
            Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName(IMAGETYPE);
            ImageWriter writer = it.next();
            File f = new File(imageCloudyPathDr + File.separator + imageName + ".mkt");
            ImageOutputStream ios = null;
            try {
                ios = ImageIO.createImageOutputStream(f);
                writer.setOutput(ios);
                writer.write(cloudImage);
                cloudImage.flush();
                ios.flush();
                ios.close();

            } catch (IOException e) {
                logger.error("生成图片失败", e);
                throw new RuntimeException(e);
            } finally {
                if (ios != null) {
                    try {
                        ios.flush();
                        ios.close();
                    } catch (IOException e) {

                    }
                }
            }
        }

    }

    private double latMax = -9999;
    private double latMin = 9999;
    private double lonMax = -9999;
    private double lonMin = 9999;
    private void processMKTBufferImage(BufferedImage image){
        dataWidth = image.getWidth();
        dataHeight = image.getHeight();

        resWidth = dataWidth;
        resHeight = dataHeight;

        //resWidth = (int) Math.round(lonE - lonS) * 4;
        //resHeight = (int) Math.round(latE - latS) * 4;

        BufferedImage linearImage = new BufferedImage(resWidth, resHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphic = linearImage.createGraphics();
        graphic.setColor(new Color(0.0f, 0.0f,0.0f, 0.0f));
        graphic.fillRect(0, 0, resWidth, resHeight);

        double startPrecent = Math.log(Math.tan(Math.PI / 4.0 + (this.cloudData.getYEnd() / 180d * Math.PI) / 2.0)) / Math.PI;
        double endPrecent = Math.log(Math.tan(Math.PI / 4.0 + (this.cloudData.getYStart() / 180d * Math.PI) / 2.0)) / Math.PI;
        double eachPrecent = (endPrecent - startPrecent) / dataHeight;

//        double y0 = lat2Mercator(this.latStart);
//        double y1 = lat2Mercator(this.latEnd);
//        double dh = y1 - y0;

        for (int i = 0; i < dataWidth; i++) {
            for (int j = 0; j < dataHeight; j++) {

                double[] lonLatAry = mktprojToLonLat(i, j, cloudData.getXStart(), cloudData.getXDel(), startPrecent, eachPrecent);
                double lon = lonLatAry[0];
                double lat = lonLatAry[1];

                int y = (int) (resHeight * (lat - latS) / (latE - latS));
                int x = (int) (resWidth * (lon - lonS) / (lonE - lonS));

                if (x < 0) x = 0;
                if (x > resWidth - 1) x = resWidth - 1;
                if (y < 0) y = 0;
                if (y > resHeight - 1) y = resHeight - 1;

                linearImage.setRGB(x, resHeight - 1 - y, image.getRGB(i, j));

                if(latMax < lat) latMax = lat;
                if(latMin > lat) latMin = lat;
                if(lonMax < lon) lonMax = lon;
                if(lonMin > lon) lonMin = lon;

//

//                double value = y1 - (j * dh) / resHeight;
//                double latR = mercator2Lat(value);
//               // double mkty = resHeight * (latR - y0) / dh;
//
//
//                if(latMax < latR) latMax = latR;
//                if(latMin > latR) latMin = latR;
////                if(lonMax < lon) lonMax = lon;
////                if(lonMin > lon) lonMin = lon;
//
//
//                int y = (int) Math.round(resHeight * (latR - latS) / (latE - latS));
//
//
//                if (y > resHeight - 1) y = resHeight - 1;
//                if (y < 0) y = 0;
//
//                //int x = i * resWidth / dataWidth;
//
//                //System.out.println("AAA" + y);
//                //System.out.println("AAA" + x);
//
//                linearImage.setRGB(i, resHeight - 1 - y, image.getRGB(i, j));
            }
        }

//        System.out.println(latMin + "latMax:::" + latMax);
//        System.out.println(lonMin + "lonMin:::" + lonMax);

        Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName(IMAGETYPE);
        ImageWriter writer = it.next();
        File f = new File(imageCloudyPathDr + File.separator + imageName);
        ImageOutputStream ios = null;
        try {
            ios = ImageIO.createImageOutputStream(f);
            writer.setOutput(ios);
            writer.write(linearImage);
            linearImage.flush();
            ios.flush();
            ios.close();

        } catch (IOException e) {
            logger.error("生成图片失败", e);
            throw new RuntimeException(e);
        } finally {
            if (ios != null) {
                try {
                    ios.flush();
                    ios.close();
                } catch (IOException e) {

                }
            }
        }
    }

    private double latS = -10;
    private double latE = 60;
    private double lonS = 100;
    private double lonE = 170;

    private void processBufferedImage(BufferedImage image) throws RuntimeException {
        dataWidth = image.getWidth();
        dataHeight = image.getHeight();

        resWidth = (int) Math.round(lonE - lonS) * 4;
        resHeight = (int) Math.round(latE - latS) * 4;

        System.out.println(resWidth+ "!!!!" + resHeight);

        BufferedImage cloudy = new BufferedImage(resWidth, resHeight, BufferedImage.TYPE_4BYTE_ABGR);
        ;
        Graphics2D graphic = cloudy.createGraphics();
        graphic.setColor(new Color(0.0f, 0.0f, 0.0f, 0.0f));
        graphic.fillRect(0, 0, resWidth, resHeight);

        double startPrecent = Math.log(Math.tan(Math.PI / 4.0 + (this.cloudData.getYEnd() / 180d * Math.PI) / 2.0)) / Math.PI;
        double endPrecent = Math.log(Math.tan(Math.PI / 4.0 + (this.cloudData.getYStart() / 180d * Math.PI) / 2.0)) / Math.PI;
        double eachPrecent = (endPrecent - startPrecent) / resHeight;

        BufferedImage mktTarget = new BufferedImage(resWidth, resHeight, BufferedImage.TYPE_4BYTE_ABGR);
        double y0 = lat2Mercator(this.latS);
        double y1 = lat2Mercator(this.latE);
        double dh = y1 - y0;
        double height = dataHeight;

        for (int i = 0; i < dataWidth; i++) {
            for (int j = 0; j < dataHeight; j++) {

                //兰勃托投影转换
                //double[] lonlat = lam_meter_to_latlon(i, j);
                //double[] lonlat = lbtprojToLonLat(j, i, 0, 30, 60, this.cloudData.getcLon(), dataHeight);
                double[] lonlat = lbtprojToLonLat(i, j, 0, 30, 60, this.cloudData.getcLon(), dataHeight);

                double lat = lonlat[1];
                double lon = lonlat[0];


                int y = (int) (resHeight * (lat - latS) / (latE - latS));
                int x = (int) (resWidth * (lon - lonS) / (lonE - lonS));


                if (x < 0) x = 0;
                if (x > resWidth - 1) x = resWidth - 1;
                if (y < 0) y = 0;
                if (y > resHeight - 1) y = resHeight - 1;

                //image.setRGB(x, (image.getHeight() - 1 - y), image.getRGB(i, j));
                cloudy.setRGB(x, (resHeight - 1 - y), image.getRGB(i, j));

                //墨卡托投影转换
                double dis = lat2Mercator(lat);

                double mkty = resHeight * (dis - y0) / dh;

                if (mkty > resHeight - 1) mkty = resHeight - 1;

                if (image.getHeight() - (int) Math.round(mkty) < 0) {
                    //System.out.println("mkty="+mkty);
                }
                if (mkty < 0) {
                    //System.out.println("mkty="+mkty);
                    mkty = 0;
                }
                mktTarget.setRGB(x, resHeight - (int) mkty - 1, image.getRGB(i, j));

            }
        }

        Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName(IMAGETYPE);
        ImageWriter writer = it.next();
        File f = new File(imageCloudyPathDr + File.separator + imageName);
        ImageOutputStream ios = null;
        try {
            ios = ImageIO.createImageOutputStream(f);
            writer.setOutput(ios);
            writer.write(cloudy);
            cloudy.flush();
            ios.flush();
            ios.close();

        } catch (IOException e) {
            logger.error("生成图片失败", e);
            throw new RuntimeException(e);
        } finally {
            if (ios != null) {
                try {
                    ios.flush();
                    ios.close();
                } catch (IOException e) {

                }
            }
        }

        it = ImageIO.getImageWritersByFormatName(IMAGETYPE);
        writer = it.next();
        f = new File(imageCloudyPathDr + File.separator + imageName + ".mkt");

        ios = null;
        try {
            ios = ImageIO.createImageOutputStream(f);
            writer.setOutput(ios);
            writer.write(mktTarget);
            mktTarget.flush();
            ios.flush();
            ios.close();

        } catch (IOException e) {
            logger.error("生成图片失败", e);
            throw new RuntimeException(e);
        } finally {
            if (ios != null) {
                try {
                    ios.flush();
                    ios.close();
                } catch (IOException e) {

                }
            }
        }

    }

    //墨客托根据等纬度的纬度计算
    static double lat2Mercator(double lat){
        return (Math.log(Math.tan((90 + lat) * Math.PI / 360.0)) / (Math.PI / 180.0)) * 20037508.34789 / 180;
    }

    //等经纬度根据墨客托纬度计算
    static double mercator2Lat(double dis) {
        double y = dis * 180 / 20037508.34789;
        double value = Math.atan(Math.exp(y * (Math.PI / 180.0))) * 360 / Math.PI - 90;
        return value;

    }


//    public static double[] Mercator2lonLat(double mercatorX,double mercatorY)
//    {
//        double[] xy = new double[2];
//        double x = mercatorX/20037508.34*180;
//        double y = mercatorY/20037508.34*180;
//        y= 180/M_PI*(2*Math.atan(Math.exp(y*M_PI/180))-M_PI/2);
//        xy[0] = x;
//        xy[1] = y;
//        return xy;
//    }
    /**
     * 兰勃托逐点计算经纬度(注意使用时传入的纬度像素坐标点从低到高递增，方法内部会将其从高纬到低纬自动翻转)
     *
     * @param sx   网格横向位置
     * @param sy   网格纵向位置
     * @param lat0 纬度0
     * @param lat1 纬度标准30
     * @param lat2 纬度标准60
     * @param lon0 中心经度110
     * @return 返回存放经纬度的数组 {lon, lat}
     */
    public static double[] lbtprojToLonLat(int sx, int sy, double lat0, double lat1, double lat2, double lon0, int imageHeight) {
        if (imageHeight == 0) {
            imageHeight = 512;
        }
        double a = 6378245;
        double b = 6356863.0188;
        double mypi = Math.PI;
        double x, y, f, mye, m1, m2, n, t0, t1, t2;
        double sxTemp = -3317465.28335579 + sx * 13000.0;
        double syTemp = 397490.788472539 + (511 - sy) * 13000.0;
        double mylat0 = mypi * lat0 / 180.0;
        double mylat1 = mypi * lat1 / 180.0;
        double mylat2 = mypi * lat2 / 180.0;
        long ddstepnum;
        double lon, lat, ddlatst, ddlatend, maxwucha, r0, myr, myt, myse;
        double mylon = mypi * lon0 / 180.0;
        double[] latWc;
//		maxwucha = 0.000000001d;
        maxwucha = 0.001d;
        //可能有误
//			x = sy;
//			y = sx;
        x = syTemp;
        y = sxTemp;
        mye = Math.sqrt((1 - Math.pow(b / a, 2.0)));
        m1 = Math.cos(mylat1) / Math.sqrt(1 - Math.pow(mye * Math.sin(mylat1), 2));
        m2 = Math.cos(mylat2) / Math.sqrt(1 - Math.pow(mye * Math.sin(mylat2), 2));
        t0 = Math.tan(Math.PI / 4 - mylat0 / 2) / Math.pow((1 - mye * Math.sin(mylat0)) / (1 + mye * Math.sin(mylat0)), mye / 2);
        t1 = Math.tan(Math.PI / 4 - mylat1 / 2) / Math.pow((1 - mye * Math.sin(mylat1)) / (1 + mye * Math.sin(mylat1)), mye / 2);
        t2 = Math.tan(Math.PI / 4 - mylat2 / 2) / Math.pow((1 - mye * Math.sin(mylat2)) / (1 + mye * Math.sin(mylat2)), mye / 2);
        n = Math.log(m1 / m2) / Math.log(t1 / t2);
        f = m1 / (n * Math.pow(t1, n));
        r0 = a * f * (Math.pow(t0, n));
        myr = Math.sqrt(Math.pow(y, 2) + Math.pow(r0 - x, 2));
        myt = Math.pow(myr / (a * f), 1d / n);
        myse = Math.atan(y / (r0 - x));
        lon = myse / n + mylon;
        lon = lon * 180 / mypi;
        //纬度迭代计算，当迭代函数返回最小误差值小于maxwucha时停止迭代
        ddstepnum = 10;
        ddlatst = -mypi / 2;
        ddlatend = mypi / 2;
        while (true) {
            latWc = latdiedai(ddstepnum, ddlatst, ddlatend, mye, myt);
            if (Math.abs(latWc[0]) < 100) {
                if (latWc[1] > maxwucha) {
                    ddlatst = latWc[0] - latWc[1];
                    ddlatend = latWc[0] + latWc[1];
                } else {
                    lat = latWc[0] * 180 / mypi;
                    break;
                }
            } else {
                lat = 100;
                break;
            }
        }
        return new double[]{lon, lat};
    }

    //迭代计算函数输入参数迭代次数，起始值，终止值，t'值，第一偏心率e值，返回值为误差最小的值和最小误差
    //返回结果为 {lat, wucha}
    private static double[] latdiedai(long dtstepnum, double dtlatst, double dtlatend, double mye, double myt) {
        double mypi = Math.PI;
        double lat, dtlat, dtlattemp, dtwctemp, dtlatstep;
        long i;
        dtlatstep = (dtlatend - dtlatst) / dtstepnum;
        dtwctemp = 100 * dtlatstep;
        dtlat = 100;
        for (i = 0; i < dtstepnum; i++) {
            lat = dtlatst + i * dtlatstep;
            dtlattemp = mypi / 2 - 2 * Math.atan(myt * Math.pow((1 - mye * Math.sin(lat)) / (1 + mye * Math.sin(lat)), mye / 2));
            if (Math.abs(lat - dtlattemp) < dtwctemp) {
                dtwctemp = Math.abs(lat - dtlattemp);
                dtlat = lat;
            }
        }
        return new double[]{dtlat, dtwctemp};
    }

    /**
     *  麦卡托投影变换
     * @param sx 网格横向位置
     * @param sy 网格纵向位置
     * @param lon0 起始经度
     * @param deltLon 经度分辨率
     * @param startPrecent 纵向起始比例(相对于 -85°~85°，非线性比例)
     * @param eachPrecent 比例分辨率
     * @return
     */
    private final static double RADIANS_TO_DEGREES = 180d / Math.PI;

    public static double[] mktprojToLonLat(int sx, int sy, double lon0, double deltLon, double startPrecent, double eachPrecent){
        double lat,lon;
        //sy 为（-1， 1）对应(-85°, 85°)
        double precent = startPrecent + eachPrecent * sy;
        lat = RADIANS_TO_DEGREES * Math.atan(Math.sinh(precent * Math.PI));
        lon = sx * deltLon + lon0;
        return new double[]{lon, lat};
    }


}
