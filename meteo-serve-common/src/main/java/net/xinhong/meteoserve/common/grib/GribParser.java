package net.xinhong.meteoserve.common.grib;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import xinhong_ucar.grib.GribReaderUtil;
import xinhong_ucar.grib.grib1.Grib1Data;
import xinhong_ucar.grib.grib1.Grib1GridDefinitionSection;
import xinhong_ucar.grib.grib1.Grib1ProductDefinitionSection;
import xinhong_ucar.grib.grib1.GridFileParser;
import xinhong_ucar.grib.grib2.Grib2Data;
import xinhong_ucar.unidata.io.RandomAccessFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Description: <br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 邓帅
 * @version 创建时间：2016/4/7 0007.
 */
public class GribParser {

    private static final Log logger = LogFactory.getLog(GribDataReader.class);

    private int cnt = 0;//已读字节数
    //	private static String Mess = "";
//    private static int LengthGribI = 3;
    private static int LengthGribII = 4; //文件长度和段长所占的字节数
    private int GribVersion = 0;
    //获取长度所在的字节数
    private byte[] GribByte = null;
    private byte[] b = new byte[1];
    private RandomAccessFile raf1 = null;






    public GridData getGribData(RandomAccessFile randomAccessFile){

        try{
            while(raf1.isAtEndOfFile() == false){
                byte[] sectGrib2 = new byte[LengthGribII];
                raf1.read(sectGrib2); cnt = cnt + LengthGribII ;
                GribVersion = GribReaderUtil.byteToInt(sectGrib2[3]);
                if(GribVersion == 1){
                    return parserGrib1(randomAccessFile.getLocation());
                }
                else if(GribVersion == 2){
                    return parserGrib2(randomAccessFile.getLocation());
                }else{
                    logger.info(GribParser.class+".getGribData,所解出的GRIB文件既不是GRIB1也不是GRIB2，故解码错误!");
                    raf1.close();
                    return null;
                }
            }
            return null;
        }catch(Exception e){
            logger.info(GribParser.class+".getGribData,读取GRIB文件失败，故解码错误!!");
            try {
                raf1.close();
            } catch (IOException e1) {
            }
            return null;
        }
    }



    /**
     * Grib文件解析
     * 此方法目前可解析GRIB2文件和部分GRIB1文件，是调用JAR包解析，只适合各段不重复的文件，不适合段重复的GRIB文件
     * 不适合解析英国美国格点数据
     * @param filePath 文件名称
     * @return
     */
    public GridData getGribData(String filePath){
        if(filePath == null || filePath.length() < 1)
            return null;
        File f = new File(filePath);
        if(!f.exists())
            return null;
        try {
            String mode = "rw"; // or "r"
            raf1 = new RandomAccessFile(filePath, mode);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException  e) {
            e.printStackTrace();
            return null;
        }

        if(!isGribFile()){
            logger.info(GribParser.class+".getGribData,在GRIB文件中没有获取到【GRIB】开头，故此文件不是GRIB文件");
            return null;
        }
        try{
            while(raf1.isAtEndOfFile() == false){
                byte[] sectGrib2 = new byte[LengthGribII];
                raf1.read(sectGrib2); cnt = cnt + LengthGribII ;
                GribVersion = GribReaderUtil.byteToInt(sectGrib2[3]);
                if(GribVersion == 1){
//                    return parserGrib1(filePath);
                    return new GridFileParser().UnGribFile(filePath, true);
                }
                else if(GribVersion == 2){
                    return parserGrib2(filePath);
                }else{
                    logger.info(GribParser.class+".getGribData,所解出的GRIB文件既不是GRIB1也不是GRIB2，故解码错误!");
                    raf1.close();
                    return null;
                }
            }
            return null;
        }catch(Exception e){
            logger.info(GribParser.class+".getGribData,读取GRIB文件失败，故解码错误!!");
            try {
                raf1.close();
            } catch (IOException e1) {
            }
            return null;
        }
    }

    /**
     * 根据jar包解析GRIB1文件，目前不可解析英国美国格点数据
     * @param filePath
     * @return
     */
    private GridData parserGrib1(String filePath){
        try{
            GridData gridData = new GridData();
            //		int fileLength = GribReaderUtil.Value(sectLength, new int[]{0}, 8 * LengthGribI);
            //读取第一段
            Grib1ProductDefinitionSection gpds = new Grib1ProductDefinitionSection(raf1);
            Grib1GridDefinitionSection gds = new Grib1GridDefinitionSection(raf1);

            Grib1Data data = new Grib1Data(raf1);
            float[] dataAry = data.getData(gpds.getLength() + 8, 0, gpds.bmsExists());
            int row = gds.getNy();
            int col = gds.getNx();
            float lat1 = (float) gds.getLa1();
            float lat2 = (float) gds.getLa2();
            float lon1 = (float) gds.getLo1();
            float lon2 = (float) gds.getLo2();
            float dx = (float) gds.getDx();
            float dy = (float) gds.getDy();

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
            if(lat2 < lat1)
                sign = -1 * sign;
            for(int i=0;i<row;i++)
                lats[i] = lat1 + sign * i * dx;
            for(int i=0;i<col;i++){
                lons[i] = lon1 + i * dy;
                if (lons[i] > 180){
                    lons[i] -= 360.0;
                }
            }
            gridData.setLatAry1D(lats);
            gridData.setLonAry1D(lons);

            float scale = 1.0f;
            float offset = 0.0f;
           /* if(elemCode != null){
                scale = elemCode.getDelta(dataType,null,null);
                offset = elemCode.getOffset(dataType,null,null);
            }*/
            int index = -1; //int index = 0; 为index ++;写在调用前面，故索引开始值赋为-1
            for(int i=0;i<row;i++){
                for(int j=0;j<col;j++){
                    index ++;
                    dataAry2[i][j] = dataAry[index]*scale + offset;
                }
            }
            gridData.setGridData(dataAry2);
            raf1.close();
            return gridData;
        }catch(Exception e){
            String message = "com.xinhong.mids3d.wavewatch.parser.GribParserByJar中" +
                    "getGribData,读取GRIB文件失败，故解码错误";
            System.out.println(message + "\r\n 文件=" + filePath);
            return null;
        }
    }

    private GridData parserGrib2(String filePath){
        try{
            GridData gridData = new GridData();
            GribByte = new byte[8];
            raf1.read(GribByte);cnt = cnt +8 ;
            GribByte = new byte[LengthGribII];
            while(true){
                raf1.read(GribByte);cnt = cnt +4 ;
                int sectLength = GribReaderUtil.Value(GribByte, new int[]{0}, 8 * LengthGribII);
                raf1.read(b);cnt = cnt +1 ;
                int code = GribReaderUtil.Value(b, new int[]{0}, 8);
                if(code == 3){
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
                    if(lat2 < lat1)
                        sign = -1 * sign;
                    for(int i=0;i<row;i++)
                        lats[i] = lat1 + sign * i * dx;
                    for(int i=0;i<col;i++)
                        lons[i] = lon1 + i * dy;
                    gridData.setLatAry1D(lats);
                    gridData.setLonAry1D(lons);


                    float scale = 1.0f;
                    float offset = 0.0f;

                    //// TODO: 2016/7/25 GFS 要素判断
                    File F = new File(filePath);
                    if(F.getName().indexOf("_TT_") > -1 || F.getName().indexOf("_MXT_")>-1
                            || F.getName().indexOf("_MIT_")>-1){
                        scale = 1.0f;
                        offset = -273.16f;
                    }else if(F.getName().indexOf("_PR_") > -1 ){
                        scale = 0.01f;
                        offset = 0;
                    }else if(F.getName().indexOf("_CN_") > -1 || F.getName().indexOf("_CNL_") > -1
                            || F.getName().indexOf("_RN_")>-1){
                        scale = 0.1f;
                        offset = 0;
                    }
                    /*if(elemCode != null){
                        scale = elemCode.getDelta(dataType,null,null);
                        offset = elemCode.getOffset(dataType,null,null);
                    }*/
                    int index = -1; //int index = 0; 为index ++;写在调用前面，故索引开始值赋为-1
                    for(int i=0;i<row;i++){
                        for(int j=0;j<col;j++){
                            index ++;
                            dataAry2[i][j] = dataAry[index]*scale + offset;
                        }
                    }
                    gridData.setGridData(dataAry2);

                    raf1.close();
                    return gridData;
                }else if(code > 3){
                    System.out.println("当前GRIB2文件未读取到第三段，无法调用jar包进行文件解析\r\n 文件=" + filePath);
                    raf1.close();
                    return null;
                }else if(code > 0 && code < 3){
                    byte[] bb = new byte[sectLength-LengthGribII - 1];
                    raf1.read(bb);
                    cnt = cnt +sectLength-LengthGribII - 1 ;
                }else{
                    System.out.println("当前GRIB2文件段号解析错误，无法解析\r\n 文件=" + filePath );
                    raf1.close();
                    return null;
                }
            }
        }catch(Exception e){
            String message = "com.xinhong.mids3d.wavewatch.parser.GribParserByJar中" +
                    "getGribData,读取GRIB文件失败，故解码错误";
            System.out.println(message + "\r\n 文件=" + filePath);
            try {
                raf1.close();
            } catch (IOException e1) {
//				e1.printStackTrace();
            }
            return null;
        }
    }




    private boolean isGribFile(){
        boolean isGrib = false;
        // 判断文件是否为GRIB文件
        try{
            while(raf1.isAtEndOfFile() == false){
                raf1.read(b);cnt ++;
                char ch = GribReaderUtil.getChar(b,0) ;
                if(ch == 'G'){
                    raf1.read(b);cnt ++;
                    ch =  GribReaderUtil.getChar(b,0) ;
                    if(ch == 'R'){
                        raf1.read(b);cnt ++;
                        ch =  GribReaderUtil.getChar(b,0) ;
                        if(ch == 'I'){
                            raf1.read(b);cnt ++;
                            ch =  GribReaderUtil.getChar(b,0) ;
                            if(ch == 'B'){ //是GRIB文件，跳出，开始处理数据
                                isGrib = true;
                                break;
                            }//end B
                        }//end I
                    }//end R
                }//end G
            }//end while
        }catch(IOException e){
            return false;
        }
        return isGrib;
    }
}
