package net.xinhong.meteoserve.common.PolygonProcessing;

import com.xinhong.mids3d.core.isoline.IsolinePolygon;
import gov.nasa.worldwind.geom.LatLon;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by apple on 16/8/24.
 */
public class PolygonDrawingInTexture {

    private int resWidth = 2048;
    private int resHeight = 1024;

    private double latStart = 0;
    private double latEnd = 75;
    private double lonStart = 0;
    private double lonEnd = 178.75f;

    //文件路径
    private String imagePolygonPathDr;

    private ArrayList<IsolinePolygon> polygonList = null;

    private double latMax = -9999;
    private double latMin = 9999;
    private double lonMin = 9999;
    private double lonMax = -9999;

    private int numWidthCutting = 4;
    private int numHeightCutting = 2;

    public List<String> getFileNames() {
        return fileNames;
    }

    private List<String> fileNames = new ArrayList<>();

    public PolygonDrawingInTexture() {

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

    public int getResWidth() {
        return resWidth / numWidthCutting;
    }

    public int getResHeight() {
        return resHeight / numHeightCutting;
    }


    public int getNumHeightCutting() {
        return numHeightCutting;
    }

    public int getNumWidthCutting() {
        return numWidthCutting;
    }

    public void setImagePolygonPathDr(String imagepath) {
        this.imagePolygonPathDr = imagepath;
    }

    static double lat2Mercator(double lat) {
        return (Math.log(Math.tan((90 + lat) * Math.PI / 360.0)) / (Math.PI / 180.0)) * 20037508.34789 / 180;
    }

    public void setPolygonList(ArrayList<IsolinePolygon> polygonList) {
        this.polygonList = polygonList;
    }

    public void createTexture() {
        BufferedImage imageTarget = new BufferedImage(resWidth, resHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphic = imageTarget.createGraphics();
        graphic.setColor(new Color(0.0f, 0.0f, 0.0f, 0.0f));
        graphic.fillRect(0, 0, resWidth, resHeight);

        BufferedImage imageTargetMKT = new BufferedImage(resWidth, resHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphicMKT = imageTargetMKT.createGraphics();
        graphicMKT.setColor(new Color(0.0f, 0.0f, 0.0f, 0.0f));
        graphicMKT.fillRect(0, 0, resWidth, resHeight);

        double latDel = latEnd - latStart;
        double lonDel = lonEnd - lonStart;

        double y0 = lat2Mercator(this.latStart);
        double y1 = lat2Mercator(this.latEnd);
        double dh = y1 - y0;

        for (int i = 0; i < polygonList.size(); i++) {

            IsolinePolygon polygon = polygonList.get(i);

            Color fillColor = polygon.getFillColor();
            Color lineColor = polygon.getLineColor();
            float alpha = polygon.getFillOpacity();

            List<Iterable<? extends LatLon>> boundaries = polygon.getBoundaries();

            Area areaPolygon = new Area();
            Area areaPolygonMKT = new Area();
            for (int j = 0; j < boundaries.size(); j++) {

                //Iterable<? extends LatLon> boudaryArray = boundaries.get(j);
                List boudary = (List) boundaries.get(j);
                int size = boudary.size();

                int[] xPoints = new int[size];
                int[] yPoints = new int[size];

                int[] yMKTPoints = new int[size];
                Iterator<? extends LatLon> boudaryIter = boudary.iterator();
                int index = 0;


                while (boudaryIter.hasNext()) {
                    LatLon latlon = boudaryIter.next();
                    double lat = latlon.getLatitude().degrees;
                    double lon = latlon.getLongitude().degrees;

                    if (lat >= 90)
                        lat = lat - 90;

//                    if(latMax < lat) latMax = lat;
//                    if(latMin > lat) latMin = lat;
//                    if(lonMax < lon) lonMax = lon;
//                    if(lonMin > lon) lonMin = lon;

                    int ypos = (int) ((latEnd - lat) * resHeight / latDel);
                    int xpos = (int) ((lon - lonStart) * resWidth / lonDel);

                    xPoints[index] = xpos;
                    yPoints[index] = ypos;


                    //墨卡托投影
                    double disValue = lat2Mercator(lat);
                    int yMktpos = (int) Math.round(resHeight * (disValue - y0) / dh);
                    yMKTPoints[index] = resHeight - 1 - yMktpos;

                    index++;
                }

                Polygon polygonShape = new Polygon(xPoints, yPoints, size);
                Area boudaryArea = new Area(polygonShape);
                if (boundaries.size() == 1) {
                    graphic.setColor(new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), (int) (alpha * 255)));
                    graphic.fill(polygonShape);
                } else {
                    if (j == 0) {
                        areaPolygon = boudaryArea;
                    } else if (j == boundaries.size() - 1) {
                        areaPolygon.subtract(boudaryArea);
                        graphic.setColor(new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), (int) (alpha * 255)));
                        graphic.fill(areaPolygon);
                    } else {
                        areaPolygon.subtract(boudaryArea);
                    }
                }

                Polygon polygonShapeMKT = new Polygon(xPoints, yMKTPoints, size);
                Area boudaryAreaMKT = new Area(polygonShapeMKT);
                if (boundaries.size() == 1) {
                    graphicMKT.setColor(new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), (int) (alpha * 255)));
                    graphicMKT.fill(polygonShapeMKT);
                } else {
                    if (j == 0) {
                        areaPolygonMKT = boudaryAreaMKT;
                    } else if (j == boundaries.size() - 1) {
                        areaPolygonMKT.subtract(boudaryAreaMKT);
                        graphicMKT.setColor(new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), (int) (alpha * 255)));
                        graphicMKT.fill(areaPolygonMKT);
                    } else {
                        areaPolygonMKT.subtract(boudaryAreaMKT);
                    }
                }

//                graphic.setColor(new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), (int)(alpha * 255)));
//                //graphic.draw(polygonShape);
//                //graphic.fill(areaPolygon);
//                graphic.fillPolygon(xPoints, yPoints, size);
//
//
//                graphicMKT.setColor(new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), (int)(alpha * 255)));
//                graphicMKT.fillPolygon(xPoints, yMKTPoints, size);

//                int xoffset = (int)(116 * resWidth / 178.75);
//                int yoffset = (75 - 39)* resHeight / 75;
//                graphic.setColor(Color.red);
//                graphic.draw3DRect(xoffset, yoffset, 10, 10, true);

            }
        }

        cutImage(imageTarget, imageTargetMKT);

    }

    private void cutImage(BufferedImage image, BufferedImage imageMKT) {
        int widthCutting = resWidth / numWidthCutting;
        int heightCutting = resHeight / numHeightCutting;
        if (fileNames != null)
            fileNames.clear();
        for (int i = 0; i < numHeightCutting; i++) {
            for (int j = 0; j < numWidthCutting; j++) {

                BufferedImage imageCutting = image.getSubimage(j * widthCutting, i * heightCutting, widthCutting, heightCutting);
                if (this.isCreateImage(imageCutting))
                    printImage(imageCutting, i, j, false);

                BufferedImage imageMKTCutting = imageMKT.getSubimage(j * widthCutting, i * heightCutting, widthCutting, heightCutting);
                if (this.isCreateImage(imageMKTCutting))
                    printImage(imageMKTCutting, i, j, true);
            }
        }

    }

    private boolean isCreateImage(BufferedImage image){
        boolean isCreate = false;
        int value = (new Color(0,0,0,0)).getRGB(); //默认空白图片的颜色为(0,0,0,0)
        for(int ia = 0; ia < image.getWidth(); ia++){
            for(int ja = 0; ja < image.getHeight(); ja++){
                if(value != image.getRGB(ia, ja)){
                    isCreate = true;
                    break;
                }
            }
            if (isCreate)
                break;
        }
        return isCreate;
    }


    private void printImage(BufferedImage image, int latIndex, int lonIndex, boolean isMKT) {
        ImageOutputStream ios = null;
        try {
            Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName("png");
            ImageWriter writer = it.next();

            File f;
            if (isMKT) {
                f = new File(imagePolygonPathDr + "_" + latIndex + "_" + lonIndex + ".png.mkt");
            } else {
                f = new File(imagePolygonPathDr + "_" + latIndex + "_" + lonIndex + ".png");
            }

            ios = ImageIO.createImageOutputStream(f);
            writer.setOutput(ios);
            writer.write(image);
            image.flush();

            fileNames.add(f.getName());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ios != null) {
                    ios.flush();
                    ios.close();
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }


}

