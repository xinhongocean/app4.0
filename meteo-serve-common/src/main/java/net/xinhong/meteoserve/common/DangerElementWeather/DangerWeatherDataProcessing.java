package net.xinhong.meteoserve.common.DangerElementWeather;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by apple on 16/9/9.
 */

public class DangerWeatherDataProcessing {
    public final static String IMAGETYPE = "png";
    private static final Log logger = LogFactory.getLog(DangerWeatherDataProcessing.class);

    private double latStart = 0;
    private double latEnd = 75;
    private double lonStart = 0;
    private double lonEnd = 179.75f;

    private int rowNum = 128;
    private int colNum = 128;

    private String element = "ICE";

    private float[][] valueArray = null;

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    //与是svn唯一的区别
    private String imagePath;

    private float scale = 1.0f;
    private boolean isImage = false;

    public void setElement(String elem){
        this.element = elem;
    }



    public void setLatStart(double latS) {
        latStart = latS;
    }

    public void setLatEnd(double latE) {
        latEnd = latE;
    }

    public void setLonStart(double lonS) {
        lonStart = lonS;
    }

    public void setLonEnd(double lonE) {
        lonEnd = lonE;
    }

    public void setValueArray(float[][] array){
        this.valueArray = array;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public int getResHeight() {
        return resHeight;
    }

    public int getResWidth() {
        return resWidth;
    }

    private int resHeight = 512;
    private int resWidth = 1024;
    public boolean createImage(){

        if(valueArray == null){
            logger.warn("区域危险数据为空!!!");
            return false;
        }

        this.rowNum = valueArray.length;
        this.colNum = valueArray[0].length;

        resHeight = colNum;
        resWidth= rowNum;

        BufferedImage imageTarget = new BufferedImage(rowNum, colNum, BufferedImage.TYPE_4BYTE_ABGR);
        BufferedImage imageTargetMKT = new BufferedImage(resWidth, resHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphic = imageTargetMKT.createGraphics();

        double y0 = lat2Mercator(this.latStart);
        double y1 = lat2Mercator(this.latEnd);
        double dh = y1 - y0;
        double latDel = (latEnd - latStart) / (colNum - 1);
        double lonDel = (lonEnd - lonStart) / (rowNum - 1);


        for (int y = 0; y < colNum; y++){
            for (int x = 0; x < rowNum; x++){
                Color color = getColorByValue(valueArray[x][y]);

//                if(valueArray[x][y] - 0.7> 0.00000001){
//                    System.out.println(valueArray[x][y]);
//                }
                int value =  (color.getAlpha() << 24 | color.getRed() << 16 | color.getGreen() << 8 | color.getBlue() << 0);

                imageTarget.setRGB(x, colNum - 1 - y, color.getRGB());

                //墨卡托投影转换
                double lat = latStart + latDel * (y);
                double disValue =  lat2Mercator(lat);

                double latNext = latStart + latDel * (y + 1);
                double disValueNext =  lat2Mercator(latNext);


                int mkty = (int)Math.round((resHeight - 1) * (disValue - y0)/dh);
                int mkty_next = (int)Math.round((resHeight - 1) * (disValueNext - y0)/dh);

                int mktx = (int)(x * resWidth *1.0f/ rowNum);
                int mktx_next = (int)((x + 1) * resWidth *1.0f/ rowNum);

                int yvalue = mkty;
                int nextyvalue = mkty_next;


                mkty_next = resHeight - 1 - yvalue;
                mkty = resHeight - 1 - nextyvalue;

                graphic.setColor(color);
                Rectangle2D rect = new Rectangle2D.Double(mktx, mkty,(mktx_next - mktx),(mkty_next - mkty));
                graphic.fill(rect);

            }
        }

        if (this.isCreateImage(imageTarget)){
            isImage = true;
            printImage(imageTarget, false);

        }

        if (this.isCreateImage(imageTargetMKT)){
            //isImage = true;
            printImage(imageTargetMKT, true);
        }
        return isImage;
    }

    private void printImage(BufferedImage imageTarget, boolean isMKT){
        Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName(IMAGETYPE);
        ImageWriter writer = it.next();
        File f = null;
        if (isMKT) {
            f =  new File(imagePath + ".mkt.png");
        } else {
            f = new File(imagePath);
        }

        ImageOutputStream ios = null;
        try {
            ios = ImageIO.createImageOutputStream(f);
            writer.setOutput(ios);
            writer.write(imageTarget);
            imageTarget.flush();
            ios.flush();
            ios.close();

        } catch (IOException e) {
            logger.error(e);
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

    private Color getColorByValue(float value){
        Color color = null;
        if(this.element.equals("CAT")){
            //值范围:0-12
            if (value <=5.0){
                color = new Color(0, 0, 0, 0);
            } else if (value <=8.0){
                color = new Color(covert(0.3f), covert(0.3f), covert(0.8f), covert(0.35f));
            } else if (value <=10.0){
                color = new Color(covert(0.8f), covert(0.8f), covert(0.1f), covert(0.6f));
            } else {
                color = new Color(covert(0.8f), covert(0.1f), covert(0.1f),  covert(0.75f));
            }

        }else if(this.element.equals("CB")){
            if (value <=0.2){
                color = new Color(0, 0, 0, 0);
            } else if (value <=0.4){
                color = new Color(covert(0.8f), covert(0.7f), covert(0.35f), covert(0.15f));
            } else if (value <=0.8){
                color = new Color(covert(0.85f), covert(0.2f), covert(0.2f), covert(0.3f));
            } else {
                color = new Color(covert(0.9f), covert(0.1f), covert(0.1f), covert(0.75f));
            }
        }
        else if(this.element.equals("ICE")){
            if (value <= 0.7){
                color = new Color(0, 0, 0, 0);
            } else if (value <= 2){
                color = new Color(0, 236, 236, covert(0.35f));
            } else if (value <= 3.5){
                color = new Color(1, 160, 246, covert(0.6f));
            } else {
                color = new Color(0, 0, 255, covert(0.75f));
            }

        }else if(this.element.equals("TURB")){
            //float val = value*500;
            float val = value;
            if (val <=0.7){
                color = new Color(0, 0, 0, 0);
            } else if (val <=2){
                color = new Color(254, 201, 89, covert(0.25f));
            } else if (val <=3.5){
                color = new Color(235, 111, 19, covert(0.35f));
            } else {
                color = new Color(110, 39, 6, covert(0.45f));
            }
        }
        return color;
    }

    static double lat2Mercator(double lat)
    {
        return (Math.log(Math.tan((90+lat)*Math.PI/360.0))/(Math.PI/180.0))*20037508.34789/180;
    }

    private int covert(float value){
        int result = (int) (value * 255);
        return result;
    }

    private boolean isCreateImage(BufferedImage image) {
        boolean isCreate = false;
        int value = (new Color(0, 0, 0, 0)).getRGB(); //默认空白图片的颜色为(0,0,0,0)
        for (int ia = 0; ia < image.getWidth(); ia++) {
            for (int ja = 0; ja < image.getHeight(); ja++) {
                if (value != image.getRGB(ia, ja)) {
                    isCreate = true;
                    break;
                }
            }
            if (isCreate)
                break;
        }
        return isCreate;
    }
}
