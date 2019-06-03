package com.xinhong.mids3d.core.isoline;

import com.xinhong.mids3d.core.isoline.IsolineUtil.IsolineSrcDataType;
import com.xinhong.mids3d.datareader.util.DataLevel;
import com.xinhong.mids3d.datareader.util.DataType;
import com.xinhong.mids3d.datareader.util.ElemCode;

import javax.management.RuntimeErrorException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.Map.Entry;

/**
 * 等值线运算时需要的属性类,用于设置追踪或者加密等值线属性
 * 
 * @author lxc
 * 
 */
public class IsolineProcessAttr {

	private static final String basePath = IsolineProcessAttr.class.getClassLoader().getResource("").getPath() + "isoline" + File.separator +"isolineprocess"  + File.separator;
	/**
	 * 离散数据网格化方法
	 * @author Administrator
	 *
	 */
	public static enum GridedType{
		/**
		 * 反向距离加权插值
		 */
		INVERSE_DISTANCE("INVERSE_DISTANCE"), 
		/**
		 * 三角剖面插值
		 */
		TRIANGULATION("TRIANGULATION");
    	private String value;
    	GridedType(String v) {
	        value = v;
	    }    	
    	
	    public static GridedType fromValue(String v) {
	        for (GridedType c: GridedType.values()) {
	            if (c.value.equals(v)) {
	                return c;
	            }
	        }
	        throw new IllegalArgumentException(v);
	    }   	
	    
	    @Override
	    public String toString(){
	    	return value;
	    }
	}
	
	GridedType gridedType = GridedType.INVERSE_DISTANCE;
	/**
	 * 
	 * @return 返回插值法方法
	 */
	public GridedType getGridedType() {
		return gridedType;
	}
	/**
	 * 设置离散数据网格化方法
	 * @param gridedType 离散数据网格化方法
	 */
	public void setGridedType(GridedType gridedType) {
		this.gridedType = gridedType;
	}
	
	private IsolineProcessAttr(){
	}
	
	/**
	 * 设置等值线追踪的属性
	 * @param elem 要素
	 * @param srcDatatype 数据类型：GridData\ScatterData
	 * @return
	 */
	public static IsolineProcessAttr createDefaultInstance(ElemCode elem, IsolineSrcDataType srcDatatype) {
		return IsolineProcessAttr.createDefaultInstance(null, elem, null, srcDatatype);
	}	
	
	/**
	 * 设置等值线追踪的属性
	 * @param elem 要素
	 * @param press 层次
	 * @param srcDatatype 数据类型：GridData\ScatterData
	 * @return
	 */
	public static IsolineProcessAttr createDefaultInstance(ElemCode elem, String press, IsolineSrcDataType srcDatatype) {
		return IsolineProcessAttr.createDefaultInstance(null, elem, press, srcDatatype);
	}
	
	/**
	 * 设置等值线追踪的属性
	 * @param elem 要素
	 * @param press 层次
	 * @param srcDatatype 数据类型：GridData\ScatterData
	 * @return
	 */
	public static IsolineProcessAttr createDefaultInstance(ElemCode elem, DataLevel press, IsolineSrcDataType srcDatatype) {
		if(press==null){
			return IsolineProcessAttr.createDefaultInstance(null, elem, null, srcDatatype);
		}else{
			return IsolineProcessAttr.createDefaultInstance(null, elem, press.getNumValue(), srcDatatype);
		}
	}
	
	/**
	 * 设置等值线追踪的属性：主要针对HJ
	 * @param elem 要素
	 * @return
	 */
	public static IsolineProcessAttr createDefaultInstance(ElemCode elem) {
		return IsolineProcessAttr.createDefaultInstance(null, elem, null, null);
	}
	/**
	 * 设置等值线追踪的属性
	 * @param dataType 资料类型：KW、GKQX、ED、HJAtlas等
	 * @param elem 要素
	 * @param press 层次
	 * @param srcDatatype 数据类型：GridData\ScatterData
	 * @return
	 */
	public static IsolineProcessAttr createDefaultInstance(DataType dataType, ElemCode elem, String press, IsolineSrcDataType srcDatatype) {
		return createDefaultInstance(dataType, elem, press, srcDatatype, null);
	}
	
	/**
	 * 设置等值线追踪的属性
	 * @param dataType 资料类型：KW、GKQX、ED、HJAtlas等
	 * @param elem 要素
	 * @param press 层次
	 * @param srcDatatype 数据类型：GridData\ScatterData
	 * @param configFileName 指定配置文件
	 * @return
	 */
	public static IsolineProcessAttr createDefaultInstance(DataType dataType, ElemCode elem, String press, IsolineSrcDataType srcDatatype,
			String configFileName) {
		IsolineProcessAttr attr = new IsolineProcessAttr();		
		attr.setElem(elem);
		if(dataType!=null){
			attr.setDataType(dataType);
		}
		if(press!=null){
			press = IsolineUtil.formatPressto4(press);
		}
		
		if(!elem.isHyElemCode() && srcDatatype==null){
			System.out.println("要素非海洋水文要素且数据类型不明，无法匹配配置文件");
			return null;
		}
		String path = null;
		try {
			path = URLDecoder.decode(basePath,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String fileName = configFileName;
		//根据数据类型不同暂分三个配置文件
		if (fileName == null || !new File(fileName).canRead()){
			if(elem.isHyElemCode()){
				System.out.println("海洋要素读取配置文件IsolineProcessConfig_HY.xml");
//				fileName = "config/isoline/isolineprocess/IsolineProcessConfig_HY.xml";
				fileName = path+"IsolineProcessConfig_HY.xml";
			}else if(srcDatatype.equals(IsolineSrcDataType.GridData)){
				System.out.println("类型：GridData; 读取配置文件IsolineProcessConfig_GridData.xml");
//				fileName = "config/isoline/isolineprocess/IsolineProcessConfig_GridData.xml";
				fileName = path +"IsolineProcessConfig_GridData.xml";
			}else if(srcDatatype.equals(IsolineSrcDataType.ScatterData)){
				System.out.println("类型：ScatterData; 读取配置文件IsolineProcessConfig_ScatterData.xml");
			//	fileName = "config/isoline/isolineprocess/IsolineProcessConfig_ScatterData.xml";
				fileName = path +"IsolineProcessConfig_ScatterData.xml";
			}else{
				System.out.println("此类型" + srcDatatype + "的配置文件不存在");
				return null;
			}		
		}
		float baseLevel = 0;
		float delta = 0;
		boolean isSetBaseLevel = false;
		boolean isSetInterVal = false;
		
		//所有需要从配置文件读取的参数,可初始化
		Map<String, String> configMap = new HashMap<String, String>();
		if(elem.isHyElemCode() || srcDatatype.equals(IsolineSrcDataType.GridData)){//海洋暂时都默认为GridData
			configMap.put("BaseLevel", null);
			configMap.put("InterVal", null);
			configMap.put("BilinearInterVal", "0");
			configMap.put("Spline", "0");	
			configMap.put("SplineTimes", "1");
			configMap.put("MinVal", null);				
			configMap.put("MaxVal", null);		
		}else if(srcDatatype.equals(IsolineSrcDataType.ScatterData)){
			configMap.put("MinVal", null);
			configMap.put("MaxVal", null);
			configMap.put("distR1", null);
			configMap.put("dataR3", null);
			configMap.put("validDataR", null);
			configMap.put("gsR", null);				
			configMap.put("xStart", "0");
			configMap.put("xEnd", "180");
			configMap.put("yStart", "0");
			configMap.put("yEnd", "70");
			configMap.put("xDelta", "1");
			configMap.put("yDelta", "1");
			configMap.put("BaseLevel", null);
			configMap.put("InterVal", null);
			configMap.put("BilinearInterVal", "0");
			configMap.put("Spline", "0");	
			configMap.put("SplineTimes", "1");
			configMap.put("GridedType", "INVERSE_DISTANCE");
		}else{
			throw new RuntimeErrorException(null, "In IsolineProcessAttr中SrcDataType无符合类型");				
		}
		
		//读配置文件,获取默认的配置
		if(!IsolineUtil.loadBaseOnIsolineLevelConfig(configMap, dataType, fileName, elem, press, srcDatatype)){
			throw new RuntimeErrorException(null, "In IsolineProcessAttr中createDefaultInstance()出错");
		}
		
		if(configMap.isEmpty()){
			throw new RuntimeErrorException(null, "In IsolineProcessAttr中createDefaultInstance()出错");
		}else{
			Set<Entry<String, String>> all = configMap.entrySet();
			Iterator<Entry<String, String>> iter = all.iterator();
			while(iter.hasNext()){
				Map.Entry<String, String> me = (Map.Entry<String, String>)iter.next();
				if(me.getKey().equals("BaseLevel")){
					baseLevel = Float.parseFloat(me.getValue().trim());
					isSetBaseLevel = true;
					continue;
				}
				if(me.getKey().equals("InterVal")){
					delta = Float.parseFloat(me.getValue().trim());
					isSetInterVal = true;
					continue;
				}
				if(me.getKey().equals("BilinearInterVal")){
					attr.bilinearInterVal = Float.parseFloat(me.getValue().trim());
					continue;
				}
				if(me.getKey().equals("SplineTimes")){
					attr.gridDataSplineTimes = Integer.parseInt(me.getValue().trim());
					continue;
				}
				if(me.getKey().equals("Spline")){
					String[] level = me.getValue().split(",");
					int[] splinelevelAry =  new int[level.length];
					int num=0;
					for (int i=0; i<level.length; i++){
						Integer splinelevel =  Integer.parseInt(level[i].trim());
						if (splinelevel == null){
							num++;
						}else if (splinelevel <= 0){
							num++;
						}else if (splinelevel >= 10){
							splinelevelAry[i] = 10;
						}else{
							splinelevelAry[i] = splinelevel;
						}
					}
					if (num == level.length){
						attr.isSetGridDataSpline = false;
					}else{
						attr.isSetGridDataSpline = true;
						attr.setGridDataSplineLevelAry(splinelevelAry);
					}
					continue;
				}
				if(me.getKey().equals("MinVal")){
					attr.minVal = Float.parseFloat(me.getValue().trim());
					continue;
				}
				if(me.getKey().equals("MaxVal")){
					attr.maxVal = Float.parseFloat(me.getValue().trim());
					continue;
				}
				if(me.getKey().equals("distR1")){
					attr.distR1 = Float.parseFloat(me.getValue().trim());
					continue;
				}
				if(me.getKey().equals("dataR3")){
					attr.dataR3 = Float.parseFloat(me.getValue().trim());
					continue;
				}
				if(me.getKey().equals("validDataR")){
					attr.validDataR = Float.parseFloat(me.getValue().trim());
					continue;
				}
				if(me.getKey().equals("gsR")){
					attr.gsR = Float.parseFloat(me.getValue().trim());
					continue;
				}
				if(me.getKey().equals("xStart")){
					attr.xStart = Float.parseFloat(me.getValue().trim());
					continue;
				}
				if(me.getKey().equals("xEnd")){
					attr.xEnd = Float.parseFloat(me.getValue().trim());
					continue;
				}
				if(me.getKey().equals("yStart")){
					attr.yStart = Float.parseFloat(me.getValue().trim());
					continue;
				}
				if(me.getKey().equals("yEnd")){
					attr.yEnd = Float.parseFloat(me.getValue().trim());
					continue;
				}
				if(me.getKey().equals("xDelta")){
					attr.xDelta = Float.parseFloat(me.getValue().trim());
					continue;
				}
				if(me.getKey().equals("yDelta")){
					attr.yDelta = Float.parseFloat(me.getValue().trim());
					continue;
				}
				if(me.getKey().equals("GridedType")){
					try{
						attr.gridedType = GridedType.fromValue(me.getValue().trim());						
					}catch(IllegalArgumentException e){
						attr.gridedType = GridedType.INVERSE_DISTANCE;
					}
					continue;
				}
				
			}
		}
		
		if(isSetBaseLevel && isSetInterVal){
			attr.setLevel(IsolineUtil.getLevelsFromMinMaxBaseVal(baseLevel, delta, attr.minVal, attr.maxVal));
		}
		return attr;
	}
	
	
	/**
	 * 设置dataType
	 * @param dataType
	 */
	public void setDataType(DataType dataType){
		this.dataType = dataType;
	}	
	public DataType getDataType(){
		return dataType;
	}
	private DataType dataType = null;
	
	
	/**
	 * 设置数据type:GridData(NCData)、ScatterData
	 * @param srcDataType
	 */
	public void setSrcDataType(IsolineSrcDataType srcDataType){
		this.srcDataType = srcDataType;
	}
	public IsolineSrcDataType getSrcDataType(){
		return srcDataType;
	}
	private IsolineSrcDataType srcDataType = null;
	
	
	/**
	 * 设置高度
	 * @param elev
	 */
	public void setElevation(float elev){
		elevation = elev;
		isSetElevation = true;
	}
	public float getElevation(){
		return elevation;
	}
	private float elevation = 10000;//高度 
	
	/**
	 * 是否设置了高度
	 * @return
	 */
	public boolean isSetElevation(){
		return this.isSetElevation;
	}
	private boolean isSetElevation = false;//是否设置了高度
	
	
	/**
	 * 设置追踪等级
	 * @param level
	 */
	public void setLevel(float[] level){
		if(level!=null){
			this.isSetLevel = true;
			contourLevel = new double[level.length];
			Arrays.sort(level);
			for (int i = 0; i < level.length; i++) {
				contourLevel[i] = level[i];
			}
		}
	}
	public double[] getLevel(){
		return this.contourLevel;
	}
	private double[] contourLevel;	   //等值线级别
	
	/**
	 * 是否设置了追踪等级
	 * @return
	 */
	public boolean isSetLevel() {
		return this.isSetLevel;
	}
	private boolean isSetLevel = false;  //是否已设置等值线追踪的级别	
	
	/**
	 * 设置填充等级
	 * @param fillLevel
	 */
	public void setFillLevel(float[] fillLevel){
		if(fillLevel!=null){
			this.isSetFillLevel = true;
			this.fillLevel = new double[fillLevel.length];
			Arrays.sort(fillLevel);
			for (int i = 0; i < fillLevel.length; i++) {
				this.fillLevel[i] = fillLevel[i];
			}
		}
	}
	public double[] getFillLevel(){
		return this.fillLevel;
	}
	private double[] fillLevel;//填充级别

	/**
	 * 是否设置了填充等级
	 * @return
	 */
	public boolean isSetFillLevel(){
		return this.isSetFillLevel;
	}
	private boolean isSetFillLevel = false;//是否设置填充等级
	
	/**
	 * 双线性插值的点数
	 * @param bilinearInterVal
	 */
	public void setBilinear(float bilinearInterVal) {
		this.bilinearInterVal = bilinearInterVal;
	}
	public float getBilinear() {
		return this.bilinearInterVal;
	}
	private float bilinearInterVal = 0;    // 网格化数据是否需要双线性插值-加密处理(默认为0,不需要加密)
	
	/**
	 * 设置要素，读取配置文件用
	 * @param elem
	 */
	public void setElem(ElemCode elem){
		this.elem = elem;
	}
	public ElemCode getElem(){
		return this.elem;
	}
	private ElemCode elem;//要素
	
	
	//网格化所需变量
	private float maxVal = 9999.9f;
	private float minVal = -9999.9f;
	private float distR1 = 3.0f;
	private float dataR3 = 20.0f;
	private float validDataR = 0.6f;
	private float gsR = 15.0f;
	private float xStart = 0.0f;
	private float xEnd = 180.0f;
	private float yStart = 0.0f;
	private float yEnd = 70.0f;
	private float xDelta = 1.0f;
	private float yDelta = 1.0f;

	
	public void setMaxVal(float maxVal){
		this.maxVal = maxVal;
	}
	public float getMaxVal() {
		return maxVal;
	}
	
	public float getMinVal() {
		return minVal;
	}
	public void setMinVal(float minVal){
		this.minVal = minVal;
	}
	
	public void setDistR1(float distR1){
		this.distR1 = distR1;
	}
	public float getDistR1() {
		return distR1;
	}

	public float getDataR3() {
		return dataR3;
	}
	public void setDataR3(float dataR3){
		this.dataR3 = dataR3;
	}

	public void setvalidDataR(float validDataR){
		this.validDataR = validDataR;
	}
	public float getValidDataR() {
		return validDataR;
	}
	
	public void setGSR(float gsR){
		this.gsR = gsR;
	}
	public float getGsR() {
		return gsR;
	}

	public float getxStart() {
		return xStart;
	}
	public void setxStart(float xStart) {
		this.xStart = xStart;
	}
	
	public float getxEnd() {
		return xEnd;
	}
	public void setxEnd(float xEnd) {
		this.xEnd = xEnd;
	}
	
	public float getyStart() {
		return yStart;
	}
	public void setyStart(float yStart) {
		this.yStart = yStart;
	}

	public float getyEnd() {
		return yEnd;
	}
	public void setyEnd(float yEnd) {
		this.yEnd = yEnd;
	}

	public float getxDelta() {
		return xDelta;
	}
	public void setxDelta(float xDelta) {
		this.xDelta = xDelta;
	}
	
	public float getyDelta() {
		return yDelta;
	}
	public void setyDelta(float yDelta) {
		this.yDelta = yDelta;
	}
	
	private int unSplineLevel = 7;
	private int[] unSplineLevelAry = new int[]{7};
	/**
	 * 设置平滑数据的Level，即设置卷积的卷积算子的核值
	 * 传入为10则平滑度最高，若传入为1则平滑度最低。
	 * @param splineLevel
	 */
	public void setGridDataSplineLevel(int splineLevel){
		int[] splineLevelAry = new int[]{splineLevel};
		this.setGridDataSplineLevelAry(splineLevelAry);
	}
	/**
	 * 获取平滑level
	 * @return
	 */
	public int getGridDataSplineLevel(){
		return this.unSplineLevel;
	}
	
	/**
	 * 设置平滑数据的Level，即设置卷积的卷积算子的核值
	 * @param splineLevelAry
	 */
	public void setGridDataSplineLevelAry(int[] splineLevelAry){
		int num = 0;
		unSplineLevelAry = new int[splineLevelAry.length];
		for(int i=0; i<splineLevelAry.length; i++){
			int usersplineLevel = splineLevelAry[i];
			if (usersplineLevel > 10){
				usersplineLevel = 10;
			}else if(usersplineLevel < 1){
				usersplineLevel = 13;
				num++;
			}
			this.unSplineLevelAry[i] = 13 - usersplineLevel;
		}
		
		if(num!=splineLevelAry.length){
			isSetGridDataSpline = true;
		}else{
			isSetGridDataSpline = false;
		}
		
		//临时
		unSplineLevel = unSplineLevelAry[0];
		if (unSplineLevelAry.length == 1  && unSplineLevel == 0){
			isSetGridDataSpline = false;
		}
	}
	public int[] getGridDataSplineLevelAry(){
		return this.unSplineLevelAry;
	}
	
	/**
	 * 设置格点数据的平滑次数（卷积的次数）
	 * @param gridDataSplineTimes 
	 */
	public void setGridDataSplineTimes(int gridDataSplineTimes){
		if(gridDataSplineTimes == 0){
			isSetGridDataSpline = false;
		}else{
			isSetGridDataSpline = true;
			this.gridDataSplineTimes = gridDataSplineTimes;
		}
	}
	public	int getGridDataSplineTimes(){
		return this.gridDataSplineTimes;
	}
	private int gridDataSplineTimes = 1;
	
	/**
	 * 数据是否需要卷积平滑
	 * @param isSpline
	 */
	public void setIsGridDataSpline(boolean isSpline){
		this.isSetGridDataSpline = isSpline;
	}
	public boolean getIsGridDataSpline(){
		return this.isSetGridDataSpline;
	}
	private boolean isSetGridDataSpline = false; //格点数据是否需要平滑
	
	
	/**
	 * 设置是否需要找最内最外闭合多边形的高低中心
	 * @param closedCenterPosFlag
	 */
	public void setNeedGetClosedCenterPosFlag(boolean closedCenterPosFlag){
		this.closedCenterPosFlag = closedCenterPosFlag;
	}
	private boolean closedCenterPosFlag = true;
	/**
	 * 是否取闭合等值线的高低中心
	 * @return
	 */
	public boolean isNeedGetClosedCenterPos(){
		return closedCenterPosFlag;
	}
	
	/**
	 * 将空值置成val
	 * @param val
	 */
	public void setNull2Val(float val){
		isNeedChangeNullVal = true;
		afterChangedNullVal = val;
	}
	public float getChangedNullVal(){
		return this.afterChangedNullVal;
	}
	public boolean IsNeedChangeNullVal(){
		return this.isNeedChangeNullVal;
	}
	private boolean isNeedChangeNullVal = false;
	private float afterChangedNullVal = -1;
	
	
	/**
	 * 是否需要过滤，主要针对欧洲细网格存在的小的(非)闭合等值线
	 * 个数暂定10个 直径暂定0.25f
	 */
	public void setFilter(){
		this.isFilter = true;
	}
	public boolean isFilter(){
		return this.isFilter;
	}
	private boolean isFilter = false;//是否过滤 "半径"小的点
	
	
	/**
	 * 设置x,y坐标，否则为latlon经纬度坐标
	 * @param isXYCoordinate
	 */
	public void setXYCoordinate(boolean isXYCoordinate){
		this.isXYCoordinate = isXYCoordinate;
	}
	public boolean isXYCoordinate(){
		return this.isXYCoordinate;
	}
	private boolean isXYCoordinate = false;//是否为XY坐标
	
	
	private float xSLon = 60;
	private float xELon = 150;
	private float ySLat = 10;
	private float yELat = 70;
	
	/**
	 * 设置由xy坐标系转换成经纬度坐标时的起始经度
	 * @param xSLon
	 */
	public void setXSLon(float xSLon){
		this.xSLon = xSLon;
	}
	public float getXSLon(){
		return this.xSLon;
	}
	
	/**
	 * 设置由xy坐标系转换成经纬度坐标时的结束经度
	 * @param xELon
	 */
	public void setXElon(float xELon){
		this.xELon = xELon;
	}
	public float getXELon(){
		return this.xELon;
	}
	
	/**
	 * 设置由xy坐标系转换成经纬度坐标时的起始纬度
	 * @param ySLat
	 */
	public void setYSLat(float ySLat){
		this.ySLat = ySLat;
	}
	public float getYSLat(){
		return this.ySLat;
	}
	
	/**
	 * 设置由xy坐标系转换成经纬度坐标时的结束纬度
	 * @param yELat
	 */
	public void setYELat(float yELat){
		this.yELat = yELat;
	}
	public float getYELat(){
		return this.yELat;
	}
	
	/**
	 * 设置存在null值，否则不存在，填充方法有区别
	 * @param isHasNullVal
	 */
	public void setHasNullVal(boolean isHasNullVal){
		this.isHasNullVal = isHasNullVal;
	}
	public boolean isHasNullVal(){
		return this.isHasNullVal;
	}
	private boolean isHasNullVal = false;//是否有空值，主要用于区分HY和KJ
}
