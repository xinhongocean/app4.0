package xinhong_ucar.grib.grib1;

import com.xinhong.mids3d.datareader.util.DEInfo;
import com.xinhong.mids3d.datareader.util.DataType;
import com.xinhong.mids3d.datareader.util.ElemCode;
import com.xinhong.mids3d.datareader.util.GridArea;
import net.xinhong.meteoserve.common.grib.BasicDataTypeConversion;
import net.xinhong.meteoserve.common.grib.GridData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class GridFileParser {

    private static final Log logger = LogFactory.getLog(DistanceEqualAreaInfo.class);

    public static String Mess = "";
    public static int LengthGribI = 3; //文件长度和段长所占的字节数
    public static int LengthGribII = 4;
    //	protected byte[] LengthGribIByte = new byte[LengthGribI];
//	protected byte[] LengthGribIIByte = new byte[LengthGribII];
    protected int GribVersion = 0;
    //	protected ByteBuffer fileByteBuffer = null;
    InputStream in = null;
    //获取长度所在的字节数
    private byte[] GribByte = null;
    private byte[] GridDataBytes = null;
    //	private float[][] dataAry = null;
//	private float[][] lonAry = null;
//	private float[][] latAry = null;
    private int CompressNum = 0;
    private float fMin = 0;
    private float sLat = 200;
    private float eLat = 200;
    private float sLon = 380;
    private float eLon = 380;
    private int lonCnt = 9999;
    private int latCnt = 9999;
    private float Iincrement = 999;
    private float Jincrement = 999;
    private int High = 0;
    private DataType dataType = null;
    private GridArea gridArea = null;
    private ElemCode elemcode = null; //查询要素
    private float ElemScale = 1.0f;
    private float ElemOffset = 0.0f;
    private String ElemFormat = null;
    private String ElemUnit = null;

    private static float[][] lonColumn = null;
    private int dataCnt = 0;

    protected byte[] b = new byte[1];
    //private GridCondition gridCondition = null; //grid查询条件
    private String gribfilename = ""; //grib文件名
    private boolean isFromFile = false; //是否从文件中查找数据
    private boolean isDenseRowData = false; //是否为英国美国格点数据
    private static double grib2Scale = Math.pow(10.0, 6.0);
    private static int grib1Scale = (int) Math.pow(10.0, 3.0);
    private int tScale = 0;
    private int bScale = 0;
    private boolean isDEArea;
    private boolean isConvertLatLon = true;//是否转换经纬度

    public GridFileParser() {
//		loadToFloat();
    }

    /**
     * 解GRIB文件
     * @return true成功
     */
/*	public GridData UnGribFile(GridCondition grid, ElemCode elemCode, String gribFilename){
        try{
			gribfilename = gribFilename;
			this.dataType = grid.getDataType();
			this.gridArea = grid.getArea();

			if(grid == null)
				return null;
			if(elemCode == null)
				return null;
			isDEArea = isDistanceEqualArea(grid);
			this.elemcode = elemCode;
			this.ElemFormat = this.elemcode.getFormat(grid.getDataType(),grid.getLevel(),grid.getVti());
			this.ElemOffset = this.elemcode.getOffset(grid.getDataType(),grid.getLevel(),grid.getVti());
			this.ElemScale = this.elemcode.getDelta(grid.getDataType(),grid.getLevel(),grid.getVti());
			this.ElemUnit = this.elemcode.getUnit(grid.getDataType(),grid.getLevel(),grid.getVti());
			gridCondition = grid.clone();
			isDenseRowData();
			in = new FileInputStream(gribFilename);

			GridData gridData = getGribData(grid.getDataType(),grid.getArea());
			dispose();
			return gridData;
		}catch(Exception e){
			String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
					"UnGribFile,读取GRIB文件时失败";
			FileLogger.Log(Level.INFO, message,e);
			return null;
		}
	}*/

    /**
     * 解GRIB文件
     *
     * @return true成功
     */
    public GridData UnGribFile(String gribFilename, boolean isConvert) throws IOException {
        isConvertLatLon = isConvert;
        isFromFile = true;
        gribfilename = gribFilename;
        isDenseRowData();
        in = new FileInputStream(gribFilename);
        GridData gridData = getGribData(dataType, gridArea);
        dispose();
        return gridData;
    }

   /* *//**
     * 解GRIB文件
     *
     * @return true成功
     *//*
    public GridData UnGribFile(String gribFilename, DataType dataType, String elemCode) {
        try {
            isFromFile = true;
            gribfilename = gribFilename;
            this.dataType = dataType;
            isDenseRowData();

            in = new FileInputStream(gribFilename);

            File file = new File(gribFilename);
            GridArea gridArea = getGridAreaByFileName(file.getName());
            this.gridArea = gridArea;
            this.elemcode = ElemCode.valueOf(elemCode.toUpperCase());
            this.ElemFormat = this.elemcode.getFormat(dataType, null, null);
            this.ElemOffset = this.elemcode.getOffset(dataType, null, null);
            this.ElemScale = this.elemcode.getDelta(dataType, null, null);
            this.ElemUnit = this.elemcode.getUnit(dataType, null, null);
            GridData gridData = getGribData(dataType, gridArea);
            dispose();
            return gridData;
        } catch (Exception e) {
            String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                    "UnGribFile,读取GRIB文件时失败";

            return null;
        }
    }*/


    /**
     * 解GRIB文件
     *
     * @return true成功
     */
	/*public GridData UnGribFile(GridCondition grid, String gribFilename){
		try{
			if(grid == null){
				return UnGribFile(gribFilename,false);
			}
			gridCondition = grid.clone();
			isConvertLatLon = grid.IsConvertLatLon();
			this.dataType = grid.getDataType();
			this.gridArea = grid.getArea();
			in = new FileInputStream(gribFilename);

			isDEArea = isDistanceEqualArea(grid);
			this.elemcode = grid.getSelectColumn()[0];
			this.ElemFormat = this.elemcode.getFormat(grid.getDataType(),grid.getLevel(),grid.getVti());
			this.ElemOffset = this.elemcode.getOffset(grid.getDataType(),grid.getLevel(),grid.getVti());
			this.ElemScale = this.elemcode.getDelta(grid.getDataType(),grid.getLevel(),grid.getVti());
			this.ElemUnit = this.elemcode.getUnit(grid.getDataType(),grid.getLevel(),grid.getVti());

			GridData gridData = getGribData(grid.getDataType(),grid.getArea());
			dispose();
			return gridData;
		}catch(Exception e){
			String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
					"UnGribFile,读取GRIB文件时失败";
			FileLogger.Log(Level.INFO, message,e);
			return null;
		}
	}*/
    private void dispose() {
        GribByte = null;
        GridDataBytes = null;
        try {
            if (in != null)
                in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DataType getDataTypeByFileName(String fileName) {
        DataType dataType = null;
        if (fileName.indexOf("_") > -1) {
            int index = fileName.indexOf("_");
            String type = fileName.substring(0, index).toUpperCase();
            dataType = DataType.valueOf(type);
            return dataType;
        } else {
            String type = fileName.substring(0, 2).toUpperCase();
            if (type.equals("ECMF"))
                dataType = DataType.ECMF;
            else if (type.equals("ED"))
                dataType = DataType.ED;
            else if (type.equals("KM"))
                dataType = DataType.KM;
            else if (type.equals("KT"))
                dataType = DataType.KT;
            else if (type.equals("KW"))
                dataType = DataType.KW;
            else if (type.equals("BJ"))
                dataType = DataType.BJ;
            else if (type.equals("KJ"))
                dataType = DataType.KJ;
            else if (type.equals("RJ"))
                dataType = DataType.RJ;
            else {
                type = fileName.substring(0, 3).toUpperCase();
                if (type.equals("WRR"))
                    dataType = DataType.UKWRR;
                else if (type.equals("WBC"))
                    dataType = DataType.USWBC;
                else {
                    if (fileName.substring(0, 8).toUpperCase().equals("W_NAFP_C"))
                        dataType = DataType.ECMF;
                }
            }
        }
        this.dataType = dataType;
        return dataType;
    }

    private GridArea getGridAreaByFileName(String fileName) {
        return null;
    }

    private String getElementByFileName(String fileName) {
        return null;
    }

    private GridData getGribData(DataType dataType, GridArea area) {
        GridData gridData = new GridData();
        if (!isGribFile()) {
//			e.printStackTrace();
            String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                    "getGribData,在GRIB文件中没有获取到【GRIB】开头，故此文件不是GRIB文件";
            return null;
        }
        try {
            System.out.println("------------第0段---------------");
//		    while(fileByteBuffer.hasRemaining()){
            while (in.available() != 0) {
                byte[] sectGrib2 = new byte[LengthGribII];
//				ByteBuffer sect2 = fileByteBuffer.get(sectGrib2, 0, LengthGribII);
                in.read(sectGrib2);
                GribVersion = byteToInt(sectGrib2[3]);
//				System.out.println("GRIB版本号：" + GribVersion);
                if (GribVersion == 1) {
                    int fileLength = Value(new byte[]{sectGrib2[0], sectGrib2[1], sectGrib2[2]}, new int[]{0}, 8 * 3);
//					System.out.println("一份GRIB资料的总长度为(包括0段)：" + fileLength);
//					System.out.println("------------第1段---------------");
//					gribI.UnGrib1Sec1();
                    boolean bl = UnGrib1Sec1(gridData);
                    if (bl == false) {
                        String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                                "getGribData,读取GRIB1文件失败";
                        return null;
                    }
                    break;
                } else if (GribVersion == 2) {
                    int temp = Value(new byte[]{sectGrib2[0], sectGrib2[1]}, new int[]{0}, 8 * 2);
                    temp = Value(new byte[]{sectGrib2[2]}, new int[]{0}, 8);

                    sectGrib2 = new byte[8];
//					sect2 = fileByteBuffer.get(sectGrib2, 0, 8);
                    in.read(sectGrib2);
                    int fileLength = Value(sectGrib2, new int[]{0}, 8 * 8);

                    boolean bl = UnGrib2(gridData);
                    if (bl == false) {
                        String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                                "getGribData,读取GRIB2文件失败";
                        return null;
                    }
                    break;
                } else {
                    String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                            "getGribData,所解出的GRIB文件既不是GRIB1也不是GRIB2，故解码错误";
                    return null;
                }
//				break;
            }
        } catch (Exception e) {
//			e.printStackTrace();
            String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                    "getGribData,读取GRIB文件失败，故解码错误";
            return null;
        }

//		System.out.println("解读文件话费的时间为：" + (System.currentTimeMillis() - ll));
        if (gridData.isDistanceEqual())
            return gridData;
        else
            return exchangeGridData(gridData);
    }

    /**
     * 调换GridData 的经纬度和数据信息，使得纬度start<end，经度start<end
     *
     * @param gridData 需要调换的GridData
     * @return 调换过经纬度的GridData
     */
    private GridData exchangeGridData(GridData gridData) {
        if (gridData.getYStart() > gridData.getYEnd()) {//调换纬度信息，使得start<end
            float tmp = gridData.getYStart();
            gridData.setYStart(gridData.getYEnd());
            gridData.setYEnd(tmp);
            float[][] data = gridData.getGridData();
            if (data.length > 1) {
                int rowCnt = data.length;
                int colCnt = data[0].length;
                float[][] newData = new float[rowCnt][colCnt];
                int rowIndex = rowCnt - 1;
                for (int i = 0; i < rowCnt; i++) {
                    for (int j = 0; j < colCnt; j++) {
                        newData[i][j] = data[rowIndex][j];
                    }
                    rowIndex--;
                }
                gridData.setGridData(newData);
                newData = null;
            }
        }
        if (gridData.getXStart() > gridData.getXEnd()) {//调换经度信息，使得start<end
            if (gridData.getXStart() >= 0 && gridData.getXEnd() <= 0) {
                if (gridData.getXStart() == 180f)
                    gridData.setXStart(-180f);
            } else {
                float tmp = gridData.getXStart();
                gridData.setXStart(gridData.getXEnd());
                gridData.setXEnd(tmp);
                float[][] data = gridData.getGridData();
                if (data.length > 1) {
                    int rowCnt = data.length;
                    int colCnt = data[0].length;
                    float[][] newData = new float[rowCnt][colCnt];
                    for (int i = 0; i < rowCnt; i++) {
                        int colIndex = colCnt - 1;
                        for (int j = 0; j < colCnt; j++) {
                            newData[i][j] = data[i][colIndex];
                            colIndex--;
                        }
                    }
                    gridData.setGridData(newData);
                    newData = null;
                }
            }
        }
//		//0~360的转换
//		if((gridData.getXEnd() - gridData.getXStart()) + gridData.getXDel() >= 360 ){
//			if(gridData.getXStart() > 0){
//
//			}
//		}
//		//设置经纬度位置信息
//		if(lonAry != null){
//			for(int i=0;i<lonAry.length;i++)
//				lonAry[i] = null;
//			lonAry = null;
//		}
//		if(latAry != null){
//			for(int i=0;i<latAry.length;i++)
//				latAry[i] = null;
//			latAry = null;
//		}
//		//设置经纬度位置信息
//		lonAry = new float[gridData.getRowNum()][gridData.getColNum()];
//		latAry = new float[gridData.getRowNum()][gridData.getColNum()];
        //设置经纬度位置信息
        float[][] lonAry = new float[gridData.getRowNum()][gridData.getColNum()];
        float[][] latAry = new float[gridData.getRowNum()][gridData.getColNum()];
        for (int i = 0; i < gridData.getRowNum(); i++) {
            for (int j = 0; j < gridData.getColNum(); j++) {
                latAry[i][j] = gridData.getYStart() + gridData.getYDel() * i;
                lonAry[i][j] = gridData.getXStart() + gridData.getXDel() * j;
            }
        }
        gridData.setLonAry2D(lonAry);
        gridData.setLatAry2D(latAry);

        float[] lats = new float[gridData.getRowNum()];
        float[] lons = new float[gridData.getColNum()];
        for (int i = 0; i < gridData.getRowNum(); i++) {
            lats[i] = gridData.getYStart() + gridData.getYDel() * i;
        }
        for (int i = 0; i < gridData.getColNum(); i++) {
            lons[i] = gridData.getXStart() + gridData.getXDel() * i;
        }
        gridData.setLatAry1D(lats);
        gridData.setLonAry1D(lons);
        return gridData;
    }

    /**
     * 判断是否为英国美国格点数据
     */
    private void isDenseRowData() {
//		isYMGD = true;
//		//根据条件判断是否加密纬度数据，英美数据
        if (isFromFile == true) {
            if (gribfilename.length() > 4) {
                if (gribfilename.substring(0, 4).toUpperCase().equals("YGGD") ||
                        gribfilename.substring(0, 4).toUpperCase().equals("MGGD") ||
                        gribfilename.substring(1, 3).toUpperCase().equals("BC") ||
                        gribfilename.substring(1, 3).toUpperCase().equals("RR")) {
                    isDenseRowData = true;
                }
            }
            if (gribfilename.indexOf("_") > -1) {
                int index = gribfilename.indexOf("_");
                String type = gribfilename.substring(0, index);
                if (type.toUpperCase().equals("UKWRR") || type.toUpperCase().equals("USWBC")) {
                    isDenseRowData = true;
                }
            }
        } /*else {
            if (gridCondition.getDataType() == DataType.UKWRR || gridCondition.getDataType() == DataType.USWBC)
                isDenseRowData = true;
        }*/
    }

    private float[][] DataWriteToFile(int length1, byte[] sectGrib, int totalLen, GridData gridData) {
        try {
            int DataByteLen = sectGrib.length;
            int getDataLength = 0;
            float[][] dataAry = new float[gridData.getRowNum()][gridData.getColNum()];
            int[] mask = new int[]{0, 1, 3, 7, 15, 31, 63, 127, 255};
            int[] twoto = new int[]{1, 2, 4, 8, 16, 32, 64, 128, 256};
            int offff = 0;
            int cnt = 0;
            int colIndex = 0; //二维数组列数的索引
            int colYMIndex = 0;
            float[] valueAry = null;//某列英美格点的数据
            for (int i = 0; i < length1; i++) {
//				if(i == 10790 || i == 14900){
//					System.out.println(i);
//				}
                //			//调用函数，正确，速度慢
                ////			int value = Value(sectGrib7, off1, grib.CompressBNum);
                if (i == gridData.getRowNum() * gridData.getColNum())
                    break;
                byte[] fstbyt;
                int bitno, nbytes, nlast;
                int isa;
                int octet;
                int value = 0;
                int offs = offff >> 3;
                bitno = offff & mask[3];
                isa = CompressNum + bitno + 7;
                nbytes = isa >> 3;
                int min = 0;
                if ((offs + nbytes) < (DataByteLen - 1))
                    min = offs + nbytes;
                else
                    min = DataByteLen - 1;
                fstbyt = new byte[min - offs];
                int index = 0;
                for (int j = offs; j < min; j++) {
                    fstbyt[index] = sectGrib[j + totalLen];
                    index++;
                }
                //读取民航数据时，由于数据缺失，故在最后一个数据上先填补上
                if (fstbyt.length < nbytes) {
                    byte[] tmp = new byte[nbytes];
                    for (int t = 0; t < nbytes; t++) {
                        if (t < fstbyt.length)
                            tmp[t] = fstbyt[t];
                        else
                            tmp[t] = 0;
                    }
                    fstbyt = tmp;
                }
                try {
                    if (isa < 16) {
                        octet = byteToInt(fstbyt[0]);
                        value = octet & mask[8 - bitno];
                        value = value >> (15 - isa);
                    } else {
                        nlast = (isa & mask[3]) + 1;
                        for (int j = 0; j < nbytes; j++) {
                            octet = byteToInt(fstbyt[j]);
                            if (j == 0)
                                value = octet & mask[8 - bitno];
                            else if (j < nbytes - 1)
                                value = value * 256 + octet;
                            else if (j == nbytes - 1)
                                value = value * twoto[nlast] + (octet >> (8 - nlast));
                        }
                    }
                    offff += CompressNum;
                } catch (Exception e) {
                    String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                            "DataWriteToFile,读取GRIB数据信息时出错";
                    return null;
                } finally {
                    double d = (fMin + Math.pow(2, bScale) * value) / Math.pow(10, tScale);
                    getDataLength++;
                    if (isDenseRowData == true) {//是英美格点
                        if (colYMIndex == latCnt)
                            break;
                        if (cnt == 0) {
                            valueAry = new float[(int) lonColumn[colYMIndex][1]];
                        }
                        valueAry[cnt] = (float) d;
                        cnt++;

                        if (cnt == lonColumn[colYMIndex][1] || getDataLength == dataCnt) {
                            float[] result = getAllValue(latCnt, valueAry);
                            dataAry[colYMIndex] = result;
                            colYMIndex++;
                            cnt = 0;
                        }
                    } else {
                        int row = i % gridData.getColNum();
                        int residue = i / gridData.getColNum();
                        if (row == 0 && i != 0) {
                            colIndex = 0;
                        }
                        dataAry[residue][colIndex] = (float) (d * this.ElemScale + this.ElemOffset);
                        colIndex++;
                    }
                }
            }
            gridData.setGridData(dataAry);
//			dataAry = null;
            return gridData.getGridData();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 数组加密
     *
     * @param resLength      目标数组长度
     * @param sourceValueAry 原数组
     * @return
     */
    private float[] getAllValue(int resLength, float[] sourceValueAry) {
        if (sourceValueAry == null || resLength < 1)
            return null;
        float[] allValue = new float[resLength];
        if (sourceValueAry.length == 1) {
            float value = sourceValueAry[0] * this.ElemScale + this.ElemOffset;
            for (int i = 0; i < resLength; i++) {
                allValue[i] = value;
            }
            return allValue;
        }
        if (sourceValueAry.length == resLength) {
            for (int i = 0; i < sourceValueAry.length; i++)
                sourceValueAry[i] = sourceValueAry[i] * this.ElemScale + this.ElemOffset;
            return sourceValueAry;
        }

//		if (sourceValueAry.length == 3){
//			for (int i = 0; i < sourceValueAry.length; i++)	{
//				System.out.print(sourceValueAry[i] + ",  ");
//			}
//			System.out.print(" ");
//
//		}

        int sourceLength = sourceValueAry.length;
        float[] sourceScaleAry = new float[sourceLength];
        for (int i = 0; i < sourceScaleAry.length; i++) {
            sourceScaleAry[i] = i * 1.0f / (sourceScaleAry.length - 1);
        }

        for (int i = 0; i < resLength; i++) {
            if (i == 0)
                allValue[i] = (sourceValueAry[0]) * this.ElemScale + this.ElemOffset;
            else if (i == resLength - 1)
                allValue[i] = (sourceValueAry[sourceValueAry.length - 1]) * this.ElemScale + this.ElemOffset;
            else {
//				if (i == resLength - 2){
//					System.out.println(" ");
//				}
                float scale = i * 1.0f / resLength;
                for (int j = 0; j < sourceScaleAry.length - 1; j++) {
                    if (Math.abs(scale - sourceScaleAry[j]) < 10e-5) {
                        allValue[i] = (sourceValueAry[j]) * this.ElemScale + this.ElemOffset;
                        break;
                    } else if (Math.abs(scale - sourceScaleAry[j + 1]) < 10e-5) {
                        allValue[i] = (sourceValueAry[j + 1]) * this.ElemScale + this.ElemOffset;
                        break;
                    } else if ((scale > sourceScaleAry[j] && scale < sourceScaleAry[j + 1])) {
                        allValue[i] = (sourceValueAry[j]
                                + ((scale - sourceScaleAry[j]) / (sourceScaleAry[j + 1] - sourceScaleAry[j]))
                                * (sourceValueAry[j + 1] - sourceValueAry[j])) * this.ElemScale + this.ElemOffset;
                        break;
                    }
                }
            }
        }
//		if (sourceValueAry.length == 3){
//			for (int i = 0; i < allValue.length; i++)	{
//				System.out.print(allValue[i] + ",  ");
//			}
//			System.out.println(" ");
//
//		}
        return allValue;


//		float lonStep = (lonend - lonstart)/(valueAry.length -1);
//		float lon0 = lonstart;
//		float lon1 = lonstart + lonStep;
//		float lon0val = valueAry[0];
//		float lon1val = valueAry[1];
//		float currLonVal = 0.0f;
//		float curlon = lon0;
//
//		int sIndex = 0;
//		int loop = 1;
//		for (int i = 0; i < length; i++)	{
//			if (i == 0){
//				currLonVal = lon0val;
//				allValue[i] = getStandardValue(currLonVal);
//				sIndex = 2;
//				curlon = curlon + step;
//				continue;
//			}
//			else if(curlon == lon1){
//				currLonVal = lon1val;
//				allValue[i] = getStandardValue(currLonVal);
//				if(sIndex == valueAry.length)
//					continue;
//				lon0val = lon1val;
//				lon1val = valueAry[sIndex];
//				lon0 = lon1;
//				lon1 = lon1 + lonStep;
//				sIndex ++;
//				loop = 1;
//				curlon = curlon + step;
//				continue;
//			}else {
//				currLonVal = lon0val + (lon1val - lon0val)/((lon1 - lon0)/step)*loop;
//				loop ++;
//				allValue[i] = getStandardValue(currLonVal);
//			}
//			curlon = curlon + step;
//		}
//		return allValue;
    }

    private boolean isGribFile() {
        boolean isGrib = false;
        // 判断文件是否为GRIB文件
        try {
            //	    while(fileByteBuffer.hasRemaining()){
            while (in.available() != 0) {
                //	    	b[0] = fileByteBuffer.get();
                in.read(b);
                char ch = BasicDataTypeConversion.getChar(b, 0);
                if (ch == 'G') {
                    //		    	b[0] = fileByteBuffer.get();
                    in.read(b);
                    ch = BasicDataTypeConversion.getChar(b, 0);
                    if (ch == 'R') {
                        //			    	b[0] = fileByteBuffer.get();
                        in.read(b);
                        ch = BasicDataTypeConversion.getChar(b, 0);
                        if (ch == 'I') {
                            //				    	b[0] = fileByteBuffer.get();
                            in.read(b);
                            ch = BasicDataTypeConversion.getChar(b, 0);
                            if (ch == 'B') { //是GRIB文件，跳出，开始处理数据
                                isGrib = true;
                                break;
                            }//end B
                        }//end I
                    }//end R
                }//end G
            }//end while
        } catch (IOException e) {
            return false;
        }
        return isGrib;
    }

    private boolean isGribFileOver() {
        boolean isOver = false;
        try {
            //	    while(fileByteBuffer.hasRemaining()){
            while (in.available() != 0) {
                //	    	b[0] = fileByteBuffer.get();
                in.read(b);
                char ch = BasicDataTypeConversion.getChar(b, 0);
                if (ch == '7') {
                    //			    	b[0] = fileByteBuffer.get();
                    in.read(b);
                    ch = BasicDataTypeConversion.getChar(b, 0);
                    if (ch == '7') {
                        //				    	b[0] = fileByteBuffer.get();
                        in.read(b);
                        ch = BasicDataTypeConversion.getChar(b, 0);
                        if (ch == '7') {
                            //				    	b[0] = fileByteBuffer.get();
                            in.read(b);
                            ch = BasicDataTypeConversion.getChar(b, 0);
                            if (ch == '7') { //结束，退出文件
                                System.out.println("文件读取完毕");
                                isOver = true;
                                return isOver;
                            }//end 7
                        }//end 7
                    }//end 7
                }//end 7
            }// end while
        } catch (IOException e) {
            return false;
        }
        return isOver;
    }

    private static void loadToFloat() {
        //测试ZC-WBC时修改
//   		lonColumn[0]  = new float[]{90.00f, 1f};
//		lonColumn[1]  = new float[]{88.75f, 2f};
//		lonColumn[2]  = new float[]{87.50f, 3f};
//		lonColumn[3]  = new float[]{86.25f, 5f};
//		lonColumn[4]  = new float[]{85.00f, 6f};
//		lonColumn[5]  = new float[]{83.75f, 8f};
//		lonColumn[6]  = new float[]{82.50f, 9f};
//		lonColumn[7]  = new float[]{81.25f, 11f};
//		lonColumn[8]  = new float[]{80.00f, 12f};
//		lonColumn[9]  = new float[]{78.75f, 14f};
//		lonColumn[10] = new float[]{77.50f, 16f};
//		lonColumn[11] = new float[]{76.25f, 17f};
//		lonColumn[12] = new float[]{75.00f, 19f};
//		lonColumn[13] = new float[]{73.75f, 20f};
//		lonColumn[14] = new float[]{72.50f, 22f};
//		lonColumn[15] = new float[]{71.25f, 23f};
//		lonColumn[16] = new float[]{70.00f, 25f};
//		lonColumn[17] = new float[]{68.75f, 26f};
//		lonColumn[18] = new float[]{67.50f, 28f};
//		lonColumn[19] = new float[]{66.25f, 29f};
//		lonColumn[20] = new float[]{65.00f, 30f};
//		lonColumn[21] = new float[]{63.75f, 32f};
//		lonColumn[22] = new float[]{62.50f, 33f};
//		lonColumn[23] = new float[]{61.25f, 35f};
//		lonColumn[24] = new float[]{60.00f, 36f};
//		lonColumn[25] = new float[]{58.75f, 38f};
//		lonColumn[26] = new float[]{57.50f, 39f};
//		lonColumn[27] = new float[]{56.25f, 40f};
//		lonColumn[28] = new float[]{55.00f, 42f};
//		lonColumn[29] = new float[]{53.75f, 43f};
//		lonColumn[30] = new float[]{52.50f, 44f};
//		lonColumn[31] = new float[]{51.25f, 45f};
//		lonColumn[32] = new float[]{50.00f, 47f};
//		lonColumn[33] = new float[]{48.75f, 48f};
//		lonColumn[34] = new float[]{47.50f, 49f};
//		lonColumn[35] = new float[]{46.25f, 50f};
//		lonColumn[36] = new float[]{45.00f, 51f};
//		lonColumn[37] = new float[]{43.75f, 52f};
//		lonColumn[38] = new float[]{42.50f, 54f};
//		lonColumn[39] = new float[]{41.25f, 55f};
//		lonColumn[40] = new float[]{40.00f, 56f};
//		lonColumn[41] = new float[]{38.75f, 57f};
//		lonColumn[42] = new float[]{37.50f, 58f};
//		lonColumn[43] = new float[]{36.25f, 59f};
//		lonColumn[44] = new float[]{35.00f, 60f};
//		lonColumn[45] = new float[]{33.75f, 60f};
//		lonColumn[46] = new float[]{32.50f, 61f};
//		lonColumn[47] = new float[]{31.25f, 62f};
//		lonColumn[48] = new float[]{30.00f, 63f};
//		lonColumn[49] = new float[]{28.75f, 64f};
//		lonColumn[50] = new float[]{27.50f, 65f};
//		lonColumn[51] = new float[]{26.25f, 65f};
//		lonColumn[52] = new float[]{25.00f, 66f};
//		lonColumn[53] = new float[]{23.75f, 67f};
//		lonColumn[54] = new float[]{22.50f, 67f};
//		lonColumn[55] = new float[]{21.25f, 68f};
//		lonColumn[56] = new float[]{20.00f, 69f};
//		lonColumn[57] = new float[]{18.75f, 69f};
//		lonColumn[58] = new float[]{17.50f, 70f};
//		lonColumn[59] = new float[]{16.25f, 70f};
//		lonColumn[60] = new float[]{15.00f, 71f};
//		lonColumn[61] = new float[]{13.75f, 71f};
//		lonColumn[62] = new float[]{12.50f, 71f};
//		lonColumn[63] = new float[]{11.25f, 72f};
//		lonColumn[64] = new float[]{10.00f, 72f};
//		lonColumn[65] = new float[]{8.75f,  72f};
//		lonColumn[66] = new float[]{7.50f,  73f};
//		lonColumn[67] = new float[]{6.25f,  73f};
//		lonColumn[68] = new float[]{5.00f,  73f};
//		lonColumn[69] = new float[]{3.75f,  73f};
//		lonColumn[70] = new float[]{2.50f,  73f};
//		lonColumn[71] = new float[]{1.25f,  73f};
//		lonColumn[72] = new float[]{0.00f,  73f};

        lonColumn[72] = new float[]{90.00f, 1f};
        lonColumn[71] = new float[]{88.75f, 2f};
        lonColumn[70] = new float[]{87.50f, 3f};
        lonColumn[69] = new float[]{86.25f, 5f};
        lonColumn[68] = new float[]{85.00f, 6f};
        lonColumn[67] = new float[]{83.75f, 8f};
        lonColumn[66] = new float[]{82.50f, 9f};
        lonColumn[65] = new float[]{81.25f, 11f};
        lonColumn[64] = new float[]{80.00f, 12f};
        lonColumn[63] = new float[]{78.75f, 14f};
        lonColumn[62] = new float[]{77.50f, 16f};
        lonColumn[61] = new float[]{76.25f, 17f};
        lonColumn[60] = new float[]{75.00f, 19f};
        lonColumn[59] = new float[]{73.75f, 20f};
        lonColumn[58] = new float[]{72.50f, 22f};
        lonColumn[57] = new float[]{71.25f, 23f};
        lonColumn[56] = new float[]{70.00f, 25f};
        lonColumn[55] = new float[]{68.75f, 26f};
        lonColumn[54] = new float[]{67.50f, 28f};
        lonColumn[53] = new float[]{66.25f, 29f};
        lonColumn[52] = new float[]{65.00f, 30f};
        lonColumn[51] = new float[]{63.75f, 32f};
        lonColumn[50] = new float[]{62.50f, 33f};
        lonColumn[49] = new float[]{61.25f, 35f};
        lonColumn[48] = new float[]{60.00f, 36f};
        lonColumn[47] = new float[]{58.75f, 38f};
        lonColumn[46] = new float[]{57.50f, 39f};
        lonColumn[45] = new float[]{56.25f, 40f};
        lonColumn[44] = new float[]{55.00f, 42f};
        lonColumn[43] = new float[]{53.75f, 43f};
        lonColumn[42] = new float[]{52.50f, 44f};
        lonColumn[41] = new float[]{51.25f, 45f};
        lonColumn[40] = new float[]{50.00f, 47f};
        lonColumn[39] = new float[]{48.75f, 48f};
        lonColumn[38] = new float[]{47.50f, 49f};
        lonColumn[37] = new float[]{46.25f, 50f};
        lonColumn[36] = new float[]{45.00f, 51f};
        lonColumn[35] = new float[]{43.75f, 52f};
        lonColumn[34] = new float[]{42.50f, 54f};
        lonColumn[33] = new float[]{41.25f, 55f};
        lonColumn[32] = new float[]{40.00f, 56f};
        lonColumn[31] = new float[]{38.75f, 57f};
        lonColumn[30] = new float[]{37.50f, 58f};
        lonColumn[29] = new float[]{36.25f, 59f};
        lonColumn[28] = new float[]{35.00f, 60f};
        lonColumn[27] = new float[]{33.75f, 60f};
        lonColumn[26] = new float[]{32.50f, 61f};
        lonColumn[25] = new float[]{31.25f, 62f};
        lonColumn[24] = new float[]{30.00f, 63f};
        lonColumn[23] = new float[]{28.75f, 64f};
        lonColumn[22] = new float[]{27.50f, 65f};
        lonColumn[21] = new float[]{26.25f, 65f};
        lonColumn[20] = new float[]{25.00f, 66f};
        lonColumn[19] = new float[]{23.75f, 67f};
        lonColumn[18] = new float[]{22.50f, 67f};
        lonColumn[17] = new float[]{21.25f, 68f};
        lonColumn[16] = new float[]{20.00f, 69f};
        lonColumn[15] = new float[]{18.75f, 69f};
        lonColumn[14] = new float[]{17.50f, 70f};
        lonColumn[13] = new float[]{16.25f, 70f};
        lonColumn[12] = new float[]{15.00f, 71f};
        lonColumn[11] = new float[]{13.75f, 71f};
        lonColumn[10] = new float[]{12.50f, 71f};
        lonColumn[9] = new float[]{11.25f, 72f};
        lonColumn[8] = new float[]{10.00f, 72f};
        lonColumn[7] = new float[]{8.75f, 72f};
        lonColumn[6] = new float[]{7.50f, 73f};
        lonColumn[5] = new float[]{6.25f, 73f};
        lonColumn[4] = new float[]{5.00f, 73f};
        lonColumn[3] = new float[]{3.75f, 73f};
        lonColumn[2] = new float[]{2.50f, 73f};
        lonColumn[1] = new float[]{1.25f, 73f};
        lonColumn[0] = new float[]{0.00f, 73f};
    }

    private final static int[] valuemask = new int[]{0, 1, 3, 7, 15, 31, 63, 127, 255};
    private final static int[] valuetwoto = new int[]{1, 2, 4, 8, 16, 32, 64, 128, 256};

    /**
     * @param binaryVal
     * @param off
     * @param width
     * @return
     */
    protected final int Value(byte[] binaryVal, int[] off, int width) {
        byte[] fstbyt;
        int bitno, nbytes, nlast;
        int isa;
        int octet;
        int value = 0;

        if (width == 0)
            return value;

        int[] off1 = new int[]{off[0] >> 3};
        fstbyt = new byte[binaryVal.length - off1[0]];
        for (int i = 0; i < fstbyt.length; i++) {
            fstbyt[i] = binaryVal[off1[0] + i];
        }

        bitno = off[0] & valuemask[3];
        isa = width + bitno + 7;
        if (isa < 16) {
//			octet = Integer.parseInt(String.valueOf(fstbyt[0]));
            octet = byteToInt(fstbyt[0]);
            value = octet & valuemask[8 - bitno];
            value = value >> (15 - isa);
        } else {
            nbytes = isa >> 3;
            nlast = (isa & valuemask[3]) + 1;
            for (int i = 0; i < nbytes; i++) {
//				octet = Integer.parseInt(String.valueOf(fstbyt[i]));
                octet = byteToInt(fstbyt[i]);
                if (i == 0)
                    value = octet & valuemask[8 - bitno];
                else if (i < nbytes - 1)
                    value = value * 256 + octet;
                else if (i == nbytes - 1)
                    value = value * valuetwoto[nlast] + (octet >> (8 - nlast));
            }
        }
        off[0] = off[0] + width;
        return value;
    }

    private static int byteToInt(byte bb) {
        try {
            int byteValue;
            int temp = bb % 256;
            if (bb < 0) {
                //			if(temp <= -128){
                //				int value = 256 + temp;
                //				byteValue = value;
                //			}
                //			else
                byteValue = 256 + temp;
                //			byteValue = (byte)(temp<=-128?256+temp:temp);
            } else {
                byteValue = (temp > 127 ? temp - 256 : temp);
            }
            return byteValue;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private boolean UnGrib1Sec1(GridData gridData) {
        try {
            GribByte = null;
            GridDataBytes = null;
//		    while(fileByteBuffer.hasRemaining()){
            while (in.available() != 0) {
                int[] off1 = new int[]{0};
                GribByte = new byte[LengthGribI];
//				for(int i = 0 ;i< LengthGribI ; i++){
////					GribByte[i] = fileByteBuffer.get();
//					in.read(b);
//					GribByte[i] = b[0];
//				}
                in.read(GribByte);
                int sectLength1 = Value(GribByte, off1, 8 * LengthGribI);

                off1[0] = 0; // 从第一段中获取信息
                int length = sectLength1 - LengthGribI;
                GridDataBytes = new byte[length];
//				ByteBuffer sect1 = fileByteBuffer.get(sectGrib1, 0, length);
                in.read(GridDataBytes);
//				in.skip(8 * 9);//跳过下面9个字节

                int sectCode = Value(GridDataBytes, off1, 8);
                int DataCenterSign = Value(GridDataBytes, off1, 8);
//				if(DataCenterSign == 7){
//					this.isDenseRowData = true;
//				}
                off1[0] = 8 * 9;


//				int DataProcessSign = Value(GridDataBytes, off1, 8);
//				int GridDefine = Value(GridDataBytes, off1, 8);
//				int Sign = Value(GridDataBytes, off1, 8);
//				int Code = Value(GridDataBytes, off1, 8);
//				int DataLevel = Value(GridDataBytes, off1, 8);
//				int High = Value(GridDataBytes, off1, 8 * 2);
                int Century = Value(GridDataBytes, off1, 8);
//				in.skip(8 * 11);//跳过下面11个字节
                off1[0] = off1[0] + 8 * 11;
//				int Month = Value(GridDataBytes, off1, 8);
//				int Day = Value(GridDataBytes, off1, 8);
//				int Hour = Value(GridDataBytes, off1, 8);
//				int Minute = Value(GridDataBytes, off1, 8);
//				int temp = Value(GridDataBytes, off1, 8 * 7);
                int Year = Value(GridDataBytes, off1, 8);
                Year = (Year - 1) * 100 + Century;
//				in.skip(8 * 1);//跳过下面1个字节
                off1[0] = off1[0] + 8 * 1;
//				temp = Value(GridDataBytes, off1, 8);

                int tSign = Value(GridDataBytes, off1, 1);
                int scale10 = Value(GridDataBytes, off1, 8 * 2 - 1);
                if (tSign == 1)
                    scale10 = -1 * scale10;

//				temp = Value(sectGrib1, off1, 8*12);
//				System.out.println("12个保留的八位组:" + temp);
                //保留
//				in.skip(8 * (length - 25));//跳过下面(length - 25)个字节
                off1[0] = off1[0] + 8 * (length - 25);
//				temp = Value(GridDataBytes, off1, 8*(length - 25));

                // 判断是否包括第二段和第三段
                int inc = 0;
                int sect = byteToInt(GridDataBytes[4]);
                if (sect == 128) // 只包含第二段
                    inc = 2;
                else if (sect == 64)// 只包含第三段
                    inc = 3;
                else if (sect == 192)// 包含第二和第三段
                    inc = 1;
                else
                    // 不包括第二段和第三段
                    inc = 0;

//				System.out.println("第一段段长度为：" + sectLength1);
//				System.out.println("Grib表格的版本号:" + sectCode);
//				System.out.println("DataCenterSign资料源/资料加工中心标识:" + DataCenterSign);
//				System.out.println("DataProcessSign资料加工过程（模式）标识号:" + DataProcessSign);
//				System.out.println("GridDefine网格定义:" + GridDefine);
//				System.out.println("Sign标志:" + Sign);
//				System.out.println("Code参数指示码代码表2:" + Code);
//				System.out.println("Level层次类型指示码代码表3:" + DataLevel);
//				System.out.println("High层次的高度、气压代码表3:" + High);
//				System.out.println("Century世纪:" + Century);
//				System.out.println("Day日:" + Day);
//				System.out.println("Hour时:" + Hour);
//				System.out.println("Minute分:" + Minute);
//				System.out.println("跳过7个八位组");
//				System.out.println("Year年:" + Year);
//				System.out.println("子中心标识:" + temp);
//				System.out.println("十进制比例因子:" + scale10 );
//				System.out.println("保留位，供资料源中心使用:" + temp);
                boolean bl = false;
//                if (inc == 2 || inc == 1) {
////					System.out.println();
////					System.out.println("------------第2段---------------");
//                    bl = UnGrib1Sec2(gridData);
//                } else if (inc == 3 || inc == 1) {
////					System.out.println();
////					System.out.println("------------第3段---------------");
//                    bl = UnGrib1Sec3();
//                }
                if(inc ==0){

                }else if(inc ==1){
                    bl = UnGrib1Sec2(gridData);
                    bl = UnGrib1Sec3();
                }else if(inc ==2){
                    bl = UnGrib1Sec2(gridData);
                }else if(inc ==3){
                    bl = UnGrib1Sec3();
                }else{
                    System.out.println("inc error ,inc = " + inc);
                }


                if (bl == false) {
                    String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                            "UnGrib1Sec1,读取UnGrib1Sec2/UnGrib1Sec3时失败,故解码失败";
                    return false;
                }
//				System.out.println();
//				System.out.println("------------第4段---------------");
                bl = UnGrib1Sec4(gridData);
                if (bl == false) {
                    String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                            "UnGrib1Sec1,读取UnGrib1Sec4时失败,故解码失败";
                    return false;
                }
//				System.out.println();
//				System.out.println("------------第5段---------------");
                if (isGribFileOver()) {
                    System.out.println("解码结束,解码成功");
                }
                break;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean UnGrib1Sec2(GridData gridData) {
        try {
            GribByte = null;
            GridDataBytes = null;
            while (in.available() != 0) {
                GribByte = new byte[LengthGribI];
                int[] off2 = new int[]{0};

                // 获取文件的长度
                for (int i = 0; i < LengthGribI; i++) {
                    in.read(b);
                    GribByte[i] = b[0];
                }
                int sectLength2 = Value(GribByte, off2, 8 * 3);
                int length = sectLength2 - LengthGribI;
                GridDataBytes = new byte[length];
                in.read(GridDataBytes);
                off2[0] = 0;
                // 忽略两个字节
                //modified by stone 不跳过，解析NV PV 解决美国格点解析问题
//				in.skip(8 * 2);//跳过下面2个字节
//				off2[0] = 8 * 2;
                int nv = Value(GridDataBytes, off2, 8);
                int pvpl = Value(GridDataBytes, off2, 8);

                // 数据表示类型：0-经纬格点(等距离圆柱投影或Carre平面投影) 4-高斯经纬网格 50-球谐函数系数
                // (可参考代码表6)
                int DataStyle = Value(GridDataBytes, off2, 8);
                if (DataStyle != 50) {
                    lonCnt = Value(GridDataBytes, off2, 8 * 2);
                    latCnt = Value(GridDataBytes, off2, 8 * 2);
                    int sign = Value(GridDataBytes, off2, 1);
                    sLat = (float) (Value(GridDataBytes, off2, 23));
                    if (sign == 1)
                        sLat = sLat * -1;
                    sign = Value(GridDataBytes, off2, 1);
                    sLon = (float) (Value(GridDataBytes, off2, 23));
                    if (sign == 1)
                        sLon = sLon * -1;
                    int HeftSign = Value(GridDataBytes, off2, 8);
                    sign = Value(GridDataBytes, off2, 1);
                    eLat = (float) (Value(GridDataBytes, off2, 23));
                    if (sign == 1)
                        eLat = eLat * -1;
                    sign = Value(GridDataBytes, off2, 1);
                    eLon = (float) (Value(GridDataBytes, off2, 23));
                    if (sign == 1)
                        eLon = eLon * -1;
                    Iincrement = (float) (Value(GridDataBytes, off2, 8 * 2));
                    Jincrement = (float) (Value(GridDataBytes, off2, 8 * 2));
                    int ScanSign = Value(GridDataBytes, off2, 8);
                    off2[0] = off2[0] + 8 * 4;
                    /////////////////////////////////////////////////////////

                } else {// ?
                    int tempValue = 0;
                    for (int i = 0; i < 3; i++)
                        tempValue = Value(GridDataBytes, off2, 8 * 2);
                    for (int i = 0; i < 2; i++)
                        tempValue = Value(GridDataBytes, off2, 8);
                    off2[0] = off2[0] + 8 * 18;
                }//end grib.DataStyle


                if (eLon < 360 && (sLon + latCnt * Iincrement) > 360) {
                    eLon = sLon + lonCnt * Iincrement;
                }

                if (isConvertLatLon) {
                    if (Iincrement == 65535) {
                        Iincrement = (Math.abs(eLat - sLat)) / (latCnt - 1);
                    }

                    sLon = (float) sLon / grib1Scale;
                    eLon = (float) eLon / grib1Scale;
                    sLat = (float) sLat / grib1Scale;
                    eLat = (float) eLat / grib1Scale;

                    Iincrement = (float) Iincrement / grib1Scale;
                    Jincrement = (float) Jincrement / grib1Scale;
                }

                gridData.setRowNum(latCnt);  //设置行列数
                if (lonCnt == 65535) {
                    lonCnt = (int) (Math.abs(eLon - sLon) / Jincrement) + 1;
                }
                gridData.setColNum(lonCnt);
                if (nv == 0 && pvpl != 255) {//没有NV  ，== PL
                    lonColumn = new float[latCnt][2];
                    for (int i = 0; i < latCnt; i++) {
                        lonColumn[i][0] = sLat + i * Iincrement;
                        lonColumn[i][1] = Value(GridDataBytes, off2, 8 * 2);
                        dataCnt = (int) (dataCnt + lonColumn[i][1]);
                    }
                } else if (nv != 0 && pvpl != 255) {//有NV  ，== PV

                } else if (nv == 0 && pvpl == 255) {//

                }

//				//根据条件判断是否为英美数据,即加密纬度数据
//				if(isDenseRowData){
//					//测试ZC-WBC时修改 stone 20141015
//					if(Math.abs(eLat - sLat) % 90 == 0 && (eLon == 65535 || sLon == 65535) ){
//						Iincrement = Jincrement;
//						lonCnt = 73;
//						gridData.setColNum(lonCnt);
//					}else{
//						isDenseRowData = false;
//					}
//				}

                gridData.setXStart(sLon);
                gridData.setXEnd(eLon);
                gridData.setXDel(Iincrement);
                gridData.setYStart(sLat);
                gridData.setYEnd(eLat);
                gridData.setDistanceEqual(false);
                gridData.setYDel(Jincrement);

                float[] lats = new float[gridData.getRowNum()];
                float[] lons = new float[gridData.getColNum()];
                for (int i = 0; i < gridData.getRowNum(); i++) {
                    lats[i] = gridData.getYStart() + gridData.getYDel() * i;
                }
                for (int i = 0; i < gridData.getColNum(); i++) {
                    lons[i] = gridData.getXStart() + gridData.getXDel() * i;
                }
                gridData.setLatAry1D(lats);
                gridData.setLonAry1D(lons);

                if (isDEArea) {
                    DEInfo deInfo = null;
                    if (this.dataType.equals(DataType.KJ))
                        deInfo = DistanceEqualAreaInfo.getGridData(GridArea.UCC);
                    else if (this.dataType.equals(DataType.HMM5))
                        deInfo = DistanceEqualAreaInfo.getGridData(dataType, this.gridArea);
                    else
                        deInfo = DistanceEqualAreaInfo.getGridData(this.gridArea);

                    if (deInfo == null) {
                        String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                                "UnGrib1Sec2,读取MM5配置文件时失败";
                        logger.error(message);
                        return false;
                    } else {
                        gridData.setDistanceEqual(deInfo.isDE());
                        gridData.setColNum(deInfo.getColNum());
                        gridData.setRowNum(deInfo.getRowNum());
                        gridData.setLatAry2D(deInfo.getLatAry());
                        gridData.setLonAry2D(deInfo.getLonAry());
                        gridData.setXDel(deInfo.getXdel());
                        gridData.setYDel(deInfo.getYdel());
                    }
                }

                break;
            }
            return true;
        } catch (Exception e) {
            String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                    "UnGrib1Sec2,读取第二段数据出错";
            logger.error(message);
            logger.error(e);
            return false;
        }
    }

  /*  private boolean isDistanceEqualArea(GridCondition condition) {
        if (dataType.equals(DataType.KW) || dataType.equals(DataType.KM) || dataType.equals(DataType.HMM5))
            return true;
        if (dataType.equals(DataType.KJ)) {
            if (condition.getArea().equals(GridArea.WRF) || condition.getArea().equals(GridArea.MM5)) {
                if (condition.getFreeArea() == null)
                    return true;
                else
                    return false;
            }
        }
        return false;
    }
*/
    private boolean UnGrib1Sec3() {
        try {
            GribByte = null;
            GridDataBytes = null;
//		    while(fileByteBuffer.hasRemaining()){
            while (in.available() != 0) {
                GribByte = new byte[LengthGribI];
                int[] off3 = new int[]{0};

                // 获取文件的长度
                for (int i = 0; i < LengthGribI; i++) {
//					GribByte[i] = fileByteBuffer.get();
                    in.read(b);
                    GribByte[i] = b[0];
                }
                int sectLength3 = Value(GribByte, off3, 8 * 3);
//				System.out.println("第三段段长为：" + sectLength3);

                int length = sectLength3 - LengthGribI;
                GridDataBytes = new byte[length];
//				ByteBuffer sect3 = fileByteBuffer.get(sectGrib3, 0, length);
                in.read(GridDataBytes);
                break;
            }
            return true;
        } catch (Exception e) {
//			 e.printStackTrace();
            String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                    "UnGrib1Sec3,读取UnGrib1Sec3时失败";
            logger.error(message);
            logger.error(e);
            return false;
        }
    }

    private boolean UnGrib1Sec4(GridData gridData) {
        try {
//		    while(fileByteBuffer.hasRemaining()){
            GribByte = null;
            GridDataBytes = null;
            while (in.available() != 0) {
                GribByte = new byte[LengthGribI];
                int[] off4 = new int[]{0};

                // 获取文件的长度
                for (int i = 0; i < LengthGribI; i++) {
//					GribByte[i] = fileByteBuffer.get();
                    in.read(b);
                    GribByte[i] = b[0];
                }
                int sectLength4 = Value(GribByte, off4, 8 * 3);
                int length = sectLength4 - LengthGribI;
                GridDataBytes = new byte[length];
//				ByteBuffer sect4 = fileByteBuffer.get(sectGrib4, 0, length);
                in.read(GridDataBytes);
                off4[0] = 0;
                int totalLen = 0;
                int ReduceSign = Value(GridDataBytes, off4, 8);

                totalLen += 1;
                int Irep = ReduceSign / 128;

                int lnil = ReduceSign % 128;
                System.out.println("lnil标志位后4位：" + lnil);

//				grib.Scale4 = Value(sectGrib4, off4, 8*2);
                int bSign = Value(GridDataBytes, off4, 1);
                bScale = Value(GridDataBytes, off4, 8 * 2 - 1);
                if (bSign == 1)
                    bScale = -1 * bScale;

                totalLen += 2;

                int sign = Value(GridDataBytes, off4, 1);

                int exp = Value(GridDataBytes, off4, 7);

                int mant = Value(GridDataBytes, off4, 8 * 3);

                fMin = (float) (Math.pow(2.0, -24.0) * mant * Math.pow(16.0, (exp - 64)));
                if (sign == 1)
                    fMin = -1 * fMin;


                // 计算基准值
                int normSign = 0;
                if ((bScale == Math.pow(2, 15) - 1) & (exp == Math.pow(2, 8) - 1) & (mant == Math.pow(2, 24) - 1))
                    normSign = 1;
                totalLen += 4;
                if (normSign == 1) {
                    Mess = "Grib报中无第4段数据";
                }
                // 全部被压缩数据所占比特位的位数
                CompressNum = Value(GridDataBytes, off4, 8);
                totalLen += 1;
                if (CompressNum > 32)// IDL中的gt
                {
                    Mess = "Grib报中第4段中每个压缩数据位数超过32.";
                }
                if (CompressNum == 0) {// IDL中的gt
                    Mess = "compressBNum == 0 11位 Grib报中无第4段数据";
                    String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                            "UnGrib1Sec4," + Mess + ",故读取UnGrib1Sec4时失败";
                    logger.error(message);

                    return false;
                }
                int length1 = (GridDataBytes.length * 8 - off4[0]) / CompressNum;

//				System.out.println("第四段段长为：" + sectLength4);
//				System.out.println("ReduceSign标志位：" + ReduceSign);
//				System.out.println("Irep标志位前4位：" + Irep);
//				System.out.println("二进制比例因子：" + scale2);
//				System.out.println("基准值S符号位:" + sign);
//				System.out.println("比特位特征值:" + exp);
//				System.out.println("24个比特位的二进制尾数:" + mant);
//				System.out.println("fMin基准值：" + fMin);

                DataWriteToFile(length1, GridDataBytes, totalLen, gridData);
                break;
            }
            return true;
        } catch (Exception e) {
//			 e.printStackTrace();
            String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                    "UnGrib1Sec4,读取UnGrib1Sec4时失败";
            logger.error(message);
            logger.error(e);
            return false;
        }
    }

    private Grib2Info getSect() {
        try {
            GribByte = null;
            int sectCode = 0;
            int length = 0;
            int[] off1 = new int[]{0};
//		    while(fileByteBuffer.hasRemaining()){
            while (in.available() != 0) {
                GribByte = new byte[LengthGribII];
//				for(int i = 0 ;i< LengthGribII ; i++){
////					GribByte[i] = fileByteBuffer.get();
//					in.read(b);
//					GribByte[i] = b[0];
//				}
                in.read(GribByte);
                int sectLength = Value(GribByte, off1, 8 * LengthGribII);

                off1[0] = 0; // 从第一段中获取信息
                length = sectLength - LengthGribII - 1;
//				sectGrib1 = new byte[length];
//				ByteBuffer sect1 = fileByteBuffer.get(sectGrib1, 0, length);
//				sectCode = Value(sectGrib1, off1, 8);
//				System.out.println("段号:" + sectCode);
//				osw.write("\r\n段号:" + sectCode);  osw.write("\r\n");
//				osw.write("\r\n段号1:" + sectCode);  osw.write("\r\n");
                break;
            }
            in.read(b);
            sectCode = Value(b, off1, 8);
            Grib2Info info = new Grib2Info();
            info.setSectCode(sectCode);
            info.setLength(length);
            return info;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected Grib2Info getSect(InputStream in) {
        try {
            GribByte = null;
            int sectCode = 0;
            int length = 0;
            int[] off1 = new int[]{0};
//		    while(fileByteBuffer.hasRemaining()){
            while (in.available() != 0) {
                GribByte = new byte[LengthGribII];
                in.read(GribByte);
//				for(int i = 0 ;i< LengthGribII ; i++){
////					GribByte[i] = fileByteBuffer.get();
//					in.read(b);
//					GribByte[i] = b[0];
//				}
                int sectLength = Value(GribByte, off1, 8 * LengthGribII);

                off1[0] = 0; // 从第一段中获取信息
                length = sectLength - LengthGribII - 1;
                break;
            }
            in.read(b);
            sectCode = Value(b, off1, 8);
            Grib2Info info = new Grib2Info();
            info.setSectCode(sectCode);
            info.setLength(length);
            return info;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean UnGrib2(GridData gridData) {
        boolean isread = true;
        boolean bl = true;
        while (isread) {
            Grib2Info info = getSect();
            if (info.getSectCode() == 1) {
                bl = UnGrib2Sec1(info.getLength());
                if (bl == false) {
                    String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                            "UnGrib2,读取UnGrib2Sec1时失败，故解码错误";
                    logger.error(message);
                    return false;
                }
                isread = true;
                continue;
            } else if (info.getSectCode() == 2) {
                bl = UnGrib2Sec2(info.getLength());
                if (bl == false) {
                    String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                            "UnGrib2,读取UnGrib2Sec2时失败，故解码错误";
                    logger.error(message);
                    return false;
                }
                isread = true;
                continue;
            } else if (info.getSectCode() == 3) {
                bl = UnGrib2Sec3(info.getLength(), gridData);
                if (bl == false) {
                    String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                            "UnGrib2,读取UnGrib2Sec3时失败，故解码错误";
                    logger.error(message);
                    return false;
                }
                isread = true;
                continue;
            } else if (info.getSectCode() == 4) {
                bl = UnGrib2Sec4(info.getLength());
                if (bl == false) {
                    String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                            "UnGrib2,读取UnGrib2Sec4时失败，故解码错误";
                    logger.error(message);
                    return false;
                }
                isread = true;
                continue;
            } else if (info.getSectCode() == 5) {
                bl = UnGrib2Sec5(info.getLength());
                if (bl == false) {
                    String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                            "UnGrib2,读取UnGrib2Sec5时失败，故解码错误";
                    logger.error(message);
                    return false;
                }
                isread = true;
                continue;
            } else if (info.getSectCode() == 6) {
                bl = UnGrib2Sec6(info.getLength());
                if (bl == false) {
                    String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                            "UnGrib2,读取UnGrib2Sec6时失败，故解码错误";
                    logger.error(message);
                    return false;
                }
                isread = true;
                continue;
            } else if (info.getSectCode() == 7) {
                bl = UnGrib2Sec7(info.getLength(), gridData);
                if (bl == false) {
                    String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                            "UnGrib2,读取UnGrib2Sec7时失败，故解码错误";
                    logger.error(message);
                    return false;
                }
                isread = true;
//				continue;
                if (isGribFileOver()) {
                    System.out.println("解码结束，解码成功");
                } else {
                    String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                            "UnGrib2,解码成功,但未找到结束符号7777";
                    logger.error(message);
                }
                return true;
            }
//			else if(info.getSectCode() == 8){
//
//			}
        }
        return true;

//		System.out.println();
//		System.out.println("------------第1段---------------");
//		System.out.println();
//		System.out.println("------------第2段---------------");
//		System.out.println();
//		System.out.println("------------第3段---------------");
//		System.out.println();
//		System.out.println("------------第4段---------------");
//		System.out.println();
//		System.out.println("------------第5段---------------");
//		System.out.println();
//		System.out.println("------------第6段---------------");
//		System.out.println();
//		System.out.println("------------第7段---------------");
//		System.out.println();
//		System.out.println("------------第8段---------------");
    }

    private boolean UnGrib2Sec1(int length) {
        try {
            GridDataBytes = null;
//			 while(fileByteBuffer.hasRemaining()){
            while (in.available() != 0) {
                int[] off1 = new int[]{0};
//				GribByte = new byte[LengthGribII];
//				for(int i = 0 ;i< LengthGribII ; i++){
//				GribByte[i] = fileByteBuffer.get();
//				in.read(b);
//				GribByte[i] = b[0];
//				}
//				int sectLength1 = Value(GribByte, off1, 8 * LengthGribII);
//				off1[0] = 0; // 从第一段中获取信息
//				int length = sectLength1 - LengthGribII;
                GridDataBytes = new byte[length];
//				ByteBuffer sect1 = fileByteBuffer.get(sectGrib1, 0, length);
                in.read(GridDataBytes);
//				int sectCode = Value(sectGrib1, off1, 8);
//				in.skip(8 * 16);//跳过下面16个字节
                off1[0] = 8 * 16;
//				int DataCenterSign = Value(GridDataBytes, off1, 8*2);
//				int DataProcessSign = Value(GridDataBytes, off1, 8*2);
//				int temp = Value(GridDataBytes, off1, 8);
//				temp = Value(GridDataBytes, off1, 8);
//				temp = Value(GridDataBytes, off1, 8);
//				int Century = Value(GridDataBytes, off1, 8*2);
//				int Month = Value(GridDataBytes, off1, 8);
//				int Day = Value(GridDataBytes, off1, 8);
//				int Hour = Value(GridDataBytes, off1, 8);
//				int Minute = Value(GridDataBytes, off1, 8);
//				temp = Value(GridDataBytes, off1, 8);
//				temp = Value(GridDataBytes, off1, 8);
//				temp = Value(GridDataBytes, off1, 8);
//				//保留
//				in.skip(8 * (length - 17 + 1));//跳过下面(length - 17 + 1)个字节
                off1[0] = off1[0] + 8 * (length - 17 + 1);
//				temp = Value(GridDataBytes, off1, 8*(length - 17 + 1));

//				System.out.println("第一段段长度为：" + sectLength1);
//				System.out.println("段号1:" + sectCode);
//				System.out.println("DataCenterSign资料源/资料加工中心标识:" + DataCenterSign);
//				System.out.println("DataProcessSign资料源/资料加工子中心标识:" + DataProcessSign);
//				System.out.println("GRIB主表版本号:" + temp);
//				System.out.println("GRIB本地表版本号:" + temp);
//				System.out.println("参照时间的含义:" + temp);
//				System.out.println("Century世纪:" + Century);
//				System.out.println("Month月:" + Month);
//				System.out.println("Day日:" + Day);
//				System.out.println("Hour时:" + Hour);
//				System.out.println("Minute分:" + Minute);
//				System.out.println("秒:" + temp);
//				System.out.println("加工数据的产品状态:" + temp);
//				System.out.println("加工数据的类型:" + temp);

                break;
            }
            return true;
        } catch (Exception e) {
//			 e.printStackTrace();
            String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                    "UnGrib2,读取UnGrib2Sec1时失败，故解码错误";
            logger.error(message);
            return false;
        }
    }

    private boolean UnGrib2Sec2(int length) {
        try {
            GridDataBytes = null;
            while (in.available() != 0) {
                int[] off1 = new int[]{0};
                GridDataBytes = new byte[length];
                in.read(GridDataBytes);
                off1[0] = 8 * (length - 1 + 1);
                //本地使用

//				System.out.println("第二段段长度为：" + sectLength2);
//				System.out.println("段号2:" + sectCode);
                break;
            }
            return true;
        } catch (Exception e) {
            String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                    "UnGrib2,读取UnGrib2Sec2时失败，故解码错误";
            logger.error(message);
            return false;
        }
    }

    private boolean UnGrib2Sec3(int length, GridData gridData) {
        try {
            GridDataBytes = null;
            while (in.available() != 0) {
                int[] off1 = new int[]{0};
//				GribByte = new byte[LengthGribII];
//				for(int i = 0 ;i< LengthGribII ; i++){
//				GribByte[i] = fileByteBuffer.get();
//				in.read(b);
//				GribByte[i] = b[0];
//				}
//				int sectLength3 = Value(GribByte, off1, 8 * LengthGribII);
//
//				off1[0] = 0; // 从第一段中获取信息
//				int length = sectLength3 - LengthGribII;
                GridDataBytes = new byte[length];
//				ByteBuffer sect3 = fileByteBuffer.get(sectGrib3, 0, length);
                in.read(GridDataBytes);
//				int sectCode = Value(sectGrib3, off1, 8);
//				in.skip(8 * 7);//跳过下面7个字节
                off1[0] = 8 * 7;
//				int temp = Value(GridDataBytes, off1, 8);
//				int dataCnt = Value(GridDataBytes, off1, 8*4);
//				int temp1 = Value(GridDataBytes, off1, 8);
//				int temp2 = Value(GridDataBytes, off1, 8);
                int GridDefine = Value(GridDataBytes, off1, 8 * 2);
                //网格定义模板
                if (GridDefine == 0) {//等经纬网格
//					in.skip(8 * 16);//跳过下面16个字节.
                    off1[0] = off1[0] + 8 * 16;
                    lonCnt = Value(GridDataBytes, off1, 8 * 4);
                    latCnt = Value(GridDataBytes, off1, 8 * 4);
//					in.skip(8 * 8);//跳过下面8个字节
                    off1[0] = off1[0] + 8 * 8;
                    int sign = Value(GridDataBytes, off1, 1);
                    sLat = (float) (Value(GridDataBytes, off1, 8 * 4 - 1) / grib2Scale);
                    if (sign == 1)
                        sLat = sLat * -1;
                    sLon = (float) (Value(GridDataBytes, off1, 8 * 4) / grib2Scale);
//					in.skip(8 * 1);//跳过下面1个字节
                    off1[0] = off1[0] + 8 * 1;
                    sign = Value(GridDataBytes, off1, 1);
                    eLat = (float) (Value(GridDataBytes, off1, 8 * 4 - 1) / grib2Scale);
                    if (sign == 1)
                        eLat = eLat * -1;
                    eLon = (float) (Value(GridDataBytes, off1, 8 * 4) / grib2Scale);
                    Iincrement = (float) (Value(GridDataBytes, off1, 8 * 4) / grib2Scale);
                    Jincrement = (float) (Value(GridDataBytes, off1, 8 * 4) / grib2Scale);
//					in.skip(8 * 1);//跳过下面1个字节
                    off1[0] = off1[0] + 8 * 1;

                    //本地保留
                    in.skip(8 * (length - 68 + 1));//跳过下面(length - 68 + 1)个字节
                    off1[0] = off1[0] + 8 * (length - 68 + 1);

                    gridData.setRowNum(latCnt);  //设置行列数
                    gridData.setColNum(lonCnt);
//					//根据条件判断是否为英美数据
//					if(isDenseRowData){
//						//测试ZC-WBC是修改 stone 20141015
//						Iincrement = Jincrement;
//						lonCnt = 73;
//						gridData.setColNum(lonCnt);
//					}
                    if (isConvertLatLon) {
                        sLon = (float) sLon / grib1Scale;
                        eLon = (float) eLon / grib1Scale;
                        Iincrement = (float) Iincrement / grib1Scale;
                        sLat = (float) sLat / grib1Scale;
                        eLat = (float) eLat / grib1Scale;
                        Jincrement = (float) Jincrement / grib1Scale;
                    }
                    gridData.setXStart(sLon);
                    gridData.setXEnd(eLon);
                    gridData.setXDel(Iincrement);
                    gridData.setYStart(sLat);
                    gridData.setYEnd(eLat);
                    gridData.setDistanceEqual(false);
                    gridData.setYDel(Jincrement);
//					//设置经纬度位置信息
                    float[] lats = new float[gridData.getRowNum()];
                    float[] lons = new float[gridData.getColNum()];
                    for (int i = 0; i < gridData.getRowNum(); i++) {
                        lats[i] = gridData.getYStart() + gridData.getYDel() * i;
                    }
                    for (int i = 0; i < gridData.getColNum(); i++) {
                        lons[i] = gridData.getXStart() + gridData.getXDel() * i;
                    }
                    gridData.setLatAry1D(lats);
                    gridData.setLonAry1D(lons);
//					float[][] lonAry = new float[gridData.getRowNum()][gridData.getColNum()];
//					float[][] latAry = new float[gridData.getRowNum()][gridData.getColNum()];
//					for(int i=0;i<gridData.getRowNum();i++){
//						for(int j=0; j<gridData.getColNum();j++){
//							latAry[i][j] = gridData.getYStart() + gridData.getYDel() * i;
//							lonAry[i][j] = gridData.getXStart() + gridData.getXDel() * j;
//						}
//					}
//					gridData.setLonAry2D(lonAry);
//					gridData.setLatAry2D(latAry);
                    // 读取第四段
                    if (dataType == null)
                        break;
                    if (isDEArea) {
                        DEInfo deInfo = null;
                        if (this.dataType.equals(DataType.KJ))
                            deInfo = DistanceEqualAreaInfo.getGridData(GridArea.UCC);
                        else if (this.dataType.equals(DataType.HMM5))
                            deInfo = DistanceEqualAreaInfo.getGridData(dataType, this.gridArea);
                        else
                            deInfo = DistanceEqualAreaInfo.getGridData(this.gridArea);
                        if (deInfo == null) {
                            String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                                    "UnGrib1Sec2,读取MM5配置文件时失败";
                            logger.error(message);
                            return false;
                        } else {
                            gridData.setDistanceEqual(deInfo.isDE());
                            gridData.setColNum(deInfo.getColNum());
                            gridData.setRowNum(deInfo.getRowNum());
                            gridData.setLatAry2D(deInfo.getLatAry());
                            gridData.setLonAry2D(deInfo.getLonAry());
                            gridData.setXDel(deInfo.getXdel());
                            gridData.setYDel(deInfo.getYdel());
                        }
                    } else {
                        System.err.println("没有找到网格定义模板  " + GridDefine);
                    }

//					System.out.println("第三段段长度为：" + sectLength3);
//					System.out.println("段号3:" + sectCode);
//					System.out.println("网格定义的来源:" + temp);
//					System.out.println("可选的个点数的数目列表的八位组数目:" + temp1);
//					System.out.println("数据点数:" + dataCnt);
//					System.out.println("对个点数的数目列表的说明:" + temp2);
//					System.out.println("GridDefine网格定义模板号:" + GridDefine);
//					System.out.println("地球的形状:" + temp3);
//					System.out.println("球面地球半径的标尺比数:" + temp4);
//					System.out.println("球面地球半径的标定值:" + temp5);
//					System.out.println("扁球状地球主轴的标尺比数:" + temp6);
//					System.out.println("扁球状地球主轴的标定值:" + temp7);
//					System.out.println("扁球状地球短轴的标尺比数:" + temp8);
//					System.out.println("扁球状地球短轴的标定值:" + temp9);
//					System.out.println("Ni-沿纬圈的个点数:" + latCnt);
//					System.out.println("Nj-沿经圈的个点数:" + lonCnt);
//					System.out.println("初始产品域的基本角度:" + temp10);
//					System.out.println("基本角度的细分，用于定义极端的经度、纬度、以及方向增量:" + temp11);
//					System.out.println("La1-第一个格点的纬度：" + sLat);
//					System.out.println("Lo1-第一个格点的经度:" + sLon);
//					System.out.println("分辨率和分量标志:" + temp12);
//					System.out.println("La2-最后一个格点的纬度：" + eLat);
//					System.out.println("Lo2-最后一个格点的经度:" + eLon);
//					System.out.println("Di-I方向增量：" + Iincrement);
//					System.out.println("Dj-J方向增量：" + Jincrement);
//					System.out.println("扫描方式：" + temp13);
//					System.out.println("可选的个点数的数目列表:" + temp14);

                    break;
                }
            }
            return true;
        } catch (Exception e) {
            String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                    "UnGrib2Sec3,读取第三段数据出错";
            logger.error(message);
            return false;
        }
    }

    private boolean UnGrib2Sec4(int length) {
        try {
            GridDataBytes = null;
//			 while(fileByteBuffer.hasRemaining()){
            while (in.available() != 0) {
                int[] off1 = new int[]{0};
//				GribByte = new byte[LengthGribII];
//				for(int i = 0 ;i< LengthGribII ; i++){
//				GribByte[i] = fileByteBuffer.get();
//				in.read(b);
//				GribByte[i] = b[0];
//				}
//				int sectLength4 = Value(GribByte, off1, 8 * LengthGribII);
//
//				off1[0] = 0; // 从第一段中获取信息
//				int length = sectLength4 - LengthGribII;
                GridDataBytes = new byte[length];
//				ByteBuffer sect4 = fileByteBuffer.get(sectGrib4, 0, length);
                in.read(GridDataBytes);
//				int sectCode = Value(sectGrib4, off1, 8);
//				in.skip(8*2);
                off1[0] = 8 * 2;
//				int cnt = Value(GridDataBytes, off1, 8*2);

                int defineCode = Value(GridDataBytes, off1, 8 * 2);
                //产品定义模板
                //分解数据0和8前34个字节说明相同，后面的无需读取，故产品定义模板0和8在此通用
                if (defineCode == 0 || defineCode == 8 || defineCode == 15) {
//					in.skip(8 * 5);//跳过下面5个字节
                    off1[0] = off1[0] + 8 * 5;
//					int pstyle = Value(GridDataBytes, off1, 8);
//					int pcnt = Value(GridDataBytes, off1, 8);
//					int temp = Value(GridDataBytes, off1, 8);
//					int temp0 = Value(GridDataBytes, off1, 8);
//					int temp1 = Value(GridDataBytes, off1, 8);
                    //说明：当截断时间的小时值大于65534时，编码为65534
                    int temp2 = Value(GridDataBytes, off1, 8 * 2);
                    if (temp2 > 65534)
                        temp2 = 65534;
//					in.skip(8 * 8);//跳过下面8个字节
                    off1[0] = off1[0] + 8 * 8;
//					int temp3 = Value(GridDataBytes, off1, 8);
//					int temp4 = Value(GridDataBytes, off1, 8);
//					int temp5 = Value(GridDataBytes, off1, 8*4);
//					int temp6 = Value(GridDataBytes, off1, 8);
//					int temp7 = Value(GridDataBytes, off1, 8);
                    High = Value(GridDataBytes, off1, 8 * 4);
                    System.out.println("Level:" + High);
//					in.skip(8 * 6);//跳过下面6个字节
                    off1[0] = off1[0] + 8 * 6;
//					int temp8 = Value(GridDataBytes, off1, 8);
//					int temp9 = Value(GridDataBytes, off1, 8);
//					int temp10 = Value(GridDataBytes, off1, 8*4);
//					in.skip(8 * (length - 30 + 1));//跳过下面(length - 30 + 1)个字节
                    off1[0] = off1[0] + 8 * (length - 30 + 1);
//					int temp11 = Value(sectGrib4, off1, 8*(length - 30 + 1));

//					System.out.println("第四段段长度为：" + sectLength4);
//					System.out.println("段号4:" + sectCode);
//					System.out.println("附加在末班之后的坐标值的个数:" + cnt);
//					System.out.println("产品定义模板号:" + defineCode);
//					System.out.println("参数种类:" + pstyle);
//					System.out.println("参数数目:" + pcnt);
//					System.out.println("加工过程的类型:" + temp);
//					System.out.println("背景加工过程标识（由编码中心定义）:" + temp0);
//					System.out.println("分析或预报加工过程标识:" + temp1);
//					System.out.println("在参照时间之后，观测资料截断时间值的小时部分:" + temp2);
//					System.out.println("在参照时间之后，观测资料截断时间值的分钟部分:" + temp3);
//					System.out.println("预报时间，其单位在第18个八位组中定义:" + temp5);
//					System.out.println("时间范围单位指示码:" + temp4);
//					System.out.println("第一个固定面的类型:" + temp6);
//					System.out.println("第一个固定面的标尺比数:" + temp7);
//					System.out.println("第一个固定面的标定值:" + High);
//					System.out.println("第二个固定面的类型:" + temp8);
//					System.out.println("第二个固定面的标尺比数:" + temp9);
//					System.out.println("第二个固定面的标定值:" + temp10);
//					System.out.println("坐标值可选列表:" + temp11);
                } else {
                    System.err.println("没有找到产品定义模板 " + defineCode);
                }
                break;
            }
            return true;
        } catch (Exception e) {
//			 e.printStackTrace();
            String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                    "UnGrib2,读取UnGrib2Sec4时失败，故解码错误";
            logger.error(message);
            return false;
        }
    }

    private boolean UnGrib2Sec5(int length) {
        try {
            GridDataBytes = null;
//			while(fileByteBuffer.hasRemaining()){
            while (in.available() != 0) {
                int[] off1 = new int[]{0};
//				GribByte = new byte[LengthGribII];
//				for(int i = 0 ;i< LengthGribII ; i++){
//				GribByte[i] = fileByteBuffer.get();
//				in.read(b);
//				GribByte[i] = b[0];
//				}
//				int sectLength5 = Value(GribByte, off1, 8 * LengthGribII);
//				off1[0] = 0; // 从第一段中获取信息
//				int length = sectLength5 - LengthGribII;
                GridDataBytes = new byte[length];
//				ByteBuffer sect2 = fileByteBuffer.get(sectGrib5, 0, length);
                in.read(GridDataBytes);
//				int sectCode = Value(sectGrib5, off1, 8);
                int dataCnt = Value(GridDataBytes, off1, 8 * 4);
                int code = Value(GridDataBytes, off1, 8 * 2);
                //数据表示模板
                if (code == 0 || code == 40) {
                    int sign = Value(GridDataBytes, off1, 1);
                    int exp = Value(GridDataBytes, off1, 8);
                    int mant = Value(GridDataBytes, off1, 8 * 3 - 1);
                    if (exp == 0) {
                        fMin = (float) (mant * Math.pow(2, -149));
                    } else if (exp >= 1 && exp <= 254) {
                        double aa = 1.0 + mant * Math.pow(2, -23);
                        fMin = (float) (aa * Math.pow(2, exp - 127));
//						grib.fMin = (1.0 + grib.mant*Math.pow(2.0, -23.0)) * Math.pow(2.0, grib.exp-127);
                    } else if (exp == 255) {
                        if (mant == 0) {
                            if (sign == 1) {
                                fMin = -999999;//sign == 1 负无穷大
                            } else if (sign == 0) {
                                fMin = 999999; //sign == 0 正无穷大
                            }
                        } else if (mant > 0) {
                            fMin = (float) -999999.99;//取值无效
                        }
                    }
                    if (sign == 1)
                        fMin = -1 * fMin;
                    int bSign = Value(GridDataBytes, off1, 1);
                    bScale = Value(GridDataBytes, off1, 8 * 2 - 1);
                    if (bSign == 1)
                        bScale = -1 * bScale;
                    int tSign = Value(GridDataBytes, off1, 1);
                    tScale = Value(GridDataBytes, off1, 8 * 2 - 1);
                    if (tSign == 1)
                        tScale = -1 * tScale;
                    CompressNum = Value(GridDataBytes, off1, 8);
                    int sType = Value(GridDataBytes, off1, 8);

//					System.out.println("第五段段长度为：" + sectLength5);
//					System.out.println("段号5:" + sectCode);
//					System.out.println("如果有位图段，则"+dataCnt+"表示7段中指定的1个或多个取值，如果没有位图段，则"+dataCnt+"表示数据点的总数");
//					System.out.println("数据点数:" + dataCnt);
//					System.out.println("数据表示模板号:" + code);
//					System.out.println("基准值S符号位:" + sign);
//					System.out.println("比特位特征值:" + exp);
//					System.out.println("23个比特位的二进制尾数:" + mant);
//					System.out.println("fMin基准值:" + fMin);
//					System.out.println("二进制比例因子:" + scale2);
//					System.out.println("十进制比例因子:" + scale10);
//					System.out.println("简单压缩的每个压缩值所占的比特数，或复杂压缩或空间差分的每组参照值所占的比特数:" + CompressNum);
//					System.out.println("原始场的值的类型:" + sType);
                } else {
                    System.err.println("没有找到数据表示模板 " + code);
                }
                break;
            }
            return true;
        } catch (Exception e) {
//			 e.printStackTrace();
            String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                    "UnGrib2,读取UnGrib2Sec5时失败，故解码错误";
            logger.error(message);
            return false;
        }
    }

    private boolean UnGrib2Sec6(int length) {
        try {
            GridDataBytes = null;
//			    while(fileByteBuffer.hasRemaining()){
            while (in.available() != 0) {
//				int[] off1 = new int[] { 0 };
//				GribByte = new byte[LengthGribII];
//				for(int i = 0 ;i< LengthGribII ; i++){
//				GribByte[i] = fileByteBuffer.get();
//				in.read(b);
//				GribByte[i] = b[0];
//				}
//				int sectLength6 = Value(GribByte, off1, 8 * LengthGribII);
//				off1[0] = 0; // 从第一段中获取信息
//				int length = sectLength6 - LengthGribII;
                GridDataBytes = new byte[length];
//				ByteBuffer sect6 = fileByteBuffer.get(sectGrib6, 0, length);
                in.read(GridDataBytes);
//				int sectCode = Value(sectGrib6, off1, 8);
//				in.skip(8 * 1);//跳过下面1个字节
//				int Code = Value(GridDataBytes, off1, 8);
                //位图 如果第六个八位组的编码值不为0，则表示本段段长为6
                //并且没有编发第7-nn个八位组
                in.skip(8 * (length - 2 + 1));//跳过下面(length - 2 + 1)个字节
//				int temp = Value(GridDataBytes, off1, 8*(length - 2 + 1));

//				System.out.println("第六段段长度为：" + sectLength6);
//				System.out.println("段号6:" + sectCode);
//				System.out.println("位图指示码:" + Code);
//				System.out.println("位图:" + temp);

                break;
            }
            return true;
        } catch (Exception e) {
//			 e.printStackTrace();
            String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                    "UnGrib2,读取UnGrib2Sec6时失败，故解码错误";
            logger.error(message);
            return false;
        }
    }

    private boolean UnGrib2Sec7(int length, GridData gridData) {
        try {
//			 while(fileByteBuffer.hasRemaining()){
            GridDataBytes = null;
            while (in.available() != 0) {
                int[] off1 = new int[]{0};
//				GribByte = new byte[LengthGribII];
//				for(int i = 0 ;i< LengthGribII ; i++){
//				GribByte[i] = fileByteBuffer.get();
//				in.read(b);
//				GribByte[i] = b[0];
//				}
//				int sectLength7 = Value(GribByte, off1, 8 * LengthGribII);
//				System.out.println("第七段段长度为：" + sectLength7);
//
//
//				off1[0] = 0; // 从第一段中获取信息
//				int length = sectLength7 - LengthGribII;
                GridDataBytes = new byte[length];
//				ByteBuffer sect7 = fileByteBuffer.get(sectGrib7, 0, length);
                in.read(GridDataBytes);
//				int sectCode = Value(sectGrib7, off1, 8);
//				System.out.println("段号7:" + sectCode);
                if (CompressNum == 0) {// IDL中的gt
                    Mess = "compressNum == 0 11位 Grib报中无第4段数据";
                    System.out.println(Mess);
                }
                //写入数据
//				int totalLen = 1;		//前四个为段长，已经读出
                int totalLen = 0;
                if (CompressNum == 0) {
//					return;
                    String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                            "UnGrib2,读取UnGrib2Sec7时," + Mess;
                    logger.error(message);
                    return false;
                }

                int length1 = (GridDataBytes.length * 8 - off1[0]) / CompressNum;
                DataWriteToFile(length1, GridDataBytes, totalLen, gridData);
                break;
            }
            return true;
        } catch (Exception e) {
//			 e.printStackTrace();
            String message = "com.xinhong.mids3d.wavewatch.GridFileParser中" +
                    "UnGrib2,读取UnGrib2Sec2时失败，故解码错误";
            logger.error(message);
            return false;
        }
    }

    public class Grib2Info {
        private int sectCode;
        private int length;

        public int getSectCode() {
            return sectCode;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public void setSectCode(int sectCode) {
            this.sectCode = sectCode;
        }
    }
}
