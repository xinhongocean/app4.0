package net.xinhong.meteoserve.common.cloudy;

/**
 * 云图数据结构
 * @author sjn
 *
 */
public class YtData extends MIDSData {
	protected int rowNum = 0;
	protected int colNum = 0;
	protected float xstart = 0.0f;
	protected float xend = 0.0f;
	protected float ystart = 0.0f;
	protected float yend = 0.0f;
	protected float xdel = 0.0f;
	protected float ydel = 0.0f;
	
	protected int imageH;
	protected int imageW;
	protected int projection;
	protected float cLon = 0.0f;
	protected float cLat = 0.0f;
	protected float Xexcursion = 0.0f;
	protected float Yexcursion = 0.0f;
	protected int spCode;
	protected int sline; // 起始行号
	protected int eline; // 终止行号
	protected int scol; // 起始列号
	protected int ecol; // 终止列号
	protected int hits; // 采样率
	
	protected float Hresolution; //水平分辨率	
	protected float Vresolution; //垂直分辨率
	protected float standard1; //投影标准纬度1（或标准经度）
	protected float standard2; //投影标准纬度2（或标准经度）
	
	public int[] simpleScalerGrid = null; //简易定位网格
	public int[] spStatus1 = null ;//卫星状态 KJ 9
	public int[] spStatus2 = null; //卫星状态 KJ 51
	public int[] constant = null;  //常数块 KJ 32
	public int[] orbitData = null; //轨道姿态数据块 KJ 1600
	public int[] blank = null;     //空格 KJ 107
	public float[] scalerData = null;//定标数据 KJ 256
	public byte[] ytData = null; //云图数据
	protected String ytFileName = null;//云图文件名
	
	@Override
	public boolean isEmpty()
	{
		return (!(ytData != null && ytData.length>0));
	}	
	
	@Override
	public void dispose() {
		if(simpleScalerGrid != null)
			simpleScalerGrid = null;
		if(spStatus1 != null)
			spStatus1 = null;
		if(spStatus2 != null)
			spStatus2 = null;
		if(constant != null)
			constant = null;
		
		if(orbitData != null)
			orbitData = null;
		if(blank != null)
			blank = null;
		if(scalerData != null)
			scalerData = null;
		if(ytData != null)
			ytData = null;
	}
	
	public byte[] getYtData() {
		return ytData;
	}

	public void setYtData(byte[] ytData) {
		this.ytData = ytData;
	}

	public YtData(){
		
	}
	
	/**
	 * 纬度开始位置
	 * @return
	 */
	public float getXStart()	{return  xstart;}    //
	/**
	 * 纬度结束位置
	 * @return
	 */
	public float getXEnd()  	{return  xend;}		 //
	/**
	 * 经度开始位置
	 * @return
	 */
	public float getYStart()	{return  ystart;}	 //
	/**
	 * 经度结束位置
	 * @return
	 */
	public float getYEnd()  	{return yend;}		 //	
	/**
	 * 获取网格数据有多少行
	 * @return
	 */
	public int getRowNum()		{ return rowNum; }	 //
	/**
	 * 获取网格数据有多少列
	 * @return
	 */
	public int getColNum()		{ return colNum; }	 //
	/**
	 * 纬度间隔
	 * @return
	 */
	public float getXDel()  	{return xdel;}		 //
	/**
	 * 经度间隔	
	 * @return
	 */
	public float getYDel()  	{return ydel;}		 //	
	
	/**
	 * 经度开始位置
	 * @return
	 */
	public void setXStart(float xstart){this.xstart = xstart;}
	/**
	 * 经度结束
	 * @return
	 */
	public void setXEnd(float xend)    {this.xend = xend;}
	/**
	 * 
	 * @return
	 */
	public void setYStart(float ystart){this.ystart = ystart;}
	/**
	 * 
	 * @return
	 */
	public void setYEnd(float yend)    {this.yend = yend;}
	/**
	 * 
	 * @return
	 */
	public void setRowNum(int rowNum)  {this.rowNum = rowNum; }
	/**
	 * 
	 * @return
	 */
	public void setColNum(int colNum)  {this.colNum = colNum; }	
	/**
	 * 
	 * @return
	 */
	public void setXDel(float xdel)    {this.xdel = xdel;}
	/**
	 * 
	 * @return
	 */
	public void setYDel(float ydel)    {this.ydel = ydel;}
	/**
	 * 图像高度
	 * @return
	 */
	public int getImageH() 			{ return imageH; }
	/**
	 * 图像宽度
	 * @return
	 */
	public int getImageW() 			{ return imageW; }
	/**
	 * 中心经度
	 * @return
	 */
	public float getcLon() 			{ return cLon; }
	/**
	 * 中心纬度
	 * @return
	 */
	public float getcLat() 			{ return cLat; }
	/**
	 * 终止行号
	 * @return
	 */
	public int getEline() 			{ return eline; }
	/**
	 * 终止列号
	 * @return
	 */
	public int getEcol() 			{ return ecol; }
	/**
	 * 采样率
	 * @return
	 */
	public int getHits() 			{ return hits; }
	/**
	 * 投影方式
	 * @return
	 */
	public int getProjection() 		{ return projection; }
	/**
	 * X轴方向偏移量
	 * @return
	 */
	public float getXexcursion() 	{ return Xexcursion; }
	/**
	 * Y轴方向偏移量
	 * @return
	 */
	public float getYexcursion() 	{ return Yexcursion; }
	/**
	 * 卫星代号
	 * @return
	 */
	public int getSpCode() 			{ return spCode; }
	/**
	 * 开始行号
	 * @return
	 */
	public int getSline() 			{ return sline; }
	/**
	 * 开始列号
	 * @return
	 */
	public int getScol() 			{ return scol; }
	
	/**
	 * 水平分辨率	
	 * @return
	 */
	public float getHresolution() 	{ return Hresolution; }
	/**
	 * 垂直分辨率
	 * @return
	 */
	public float getVresolution() 	{ return Vresolution; }
	/**
	 * 标定纬度1
	 * @return
	 */
	public float getStandard1() 	{ return standard1; }
	/**
	 * 标定纬度2
	 * @return
	 */
	public float getStandard2() 	{ return standard2; }
	/**
	 * 简易定位网格
	 * @return
	 */
	public int[] getSimpleScalerGrid() {return simpleScalerGrid;}
	/**
	 * 云图文件名
	 * @return
	 */
	public String getYtFileName() 	{return ytFileName; }
	
	/**
	 * 获取定标数据
	 * @return
	 */
	public float[] getScalerData() {
		return scalerData;
	}

	public void setScalerData(float[] scalerData) {
		this.scalerData = scalerData;
	}

	public void setImageH(int imageH) 			{ this.imageH = imageH; }	
	public void setImageW(int imageW) 			{ this.imageW = imageW; }	
	public void setcLon(float cLon) 			{ this.cLon = cLon; }	
	public void setcLat(float cLat) 			{ this.cLat = cLat; }	
	public void setEline(int eline) 			{ this.eline = eline; }	
	public void setEcol(int ecol) 				{ this.ecol = ecol; }	
	public void setHits(int hits) 				{ this.hits = hits; }	
	public void setProjection(int projection) 	{ this.projection = projection; }	
	public void setXexcursion(float xexcursion) { Xexcursion = xexcursion; }	
	public void setYexcursion(float yexcursion) { Yexcursion = yexcursion; }	
	public void setSpCode(int spCode) 			{ this.spCode = spCode; }	
	public void setSline(int sline) 			{ this.sline = sline; }	
	public void setScol(int scol) 				{ this.scol = scol; }
	
	public void setHresolution(float hresolution) 	{ Hresolution = hresolution; }	
	public void setVresolution(float vresolution) 	{ Vresolution = vresolution; }
	public void setStandard1(float standard1) 		{ this.standard1 = standard1; }
	public void setStandard2(float standard2) 		{ this.standard2 = standard2; }
	public void setSimpleScalerGrid(int[] simpleScalerGrid) {this.simpleScalerGrid = simpleScalerGrid;}
	public void setYtFileName(String name)	 		{ this.ytFileName = name;	}
}
