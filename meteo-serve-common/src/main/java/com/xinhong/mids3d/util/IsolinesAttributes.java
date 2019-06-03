package com.xinhong.mids3d.util;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xinhong.mids3d.core.isoline.VersionManager;
import com.xinhong.mids3d.datareader.util.DataType;
import com.xinhong.mids3d.datareader.util.ElemCode;
import com.xinhong.mids3d.util.ShapeStyleUtil.FillStyle;
import com.xinhong.mids3d.util.ShapeStyleUtil.LineStyle;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.management.RuntimeErrorException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

//import com.xinhong.mids3d.aerodgfc.DataSourseEnum;
//import com.xinhong.mids3d.metafc.MetaFCOpsKeyBridge.MetaFCOpsKey;
//import com.xinhong.mids3d.metafc.MetaFCUtil.MetaFCOpsType;
//import com.xinhong.mids3d.util.math.StrArrayUtil;

/**
 * 等值线属性类,用于等值线整体及各个线条、填充的各类配置
 * @author lxc
 *
 */
public class IsolinesAttributes implements Cloneable
{
	final static String MIDSXML_NODENAME = "IsolinesAttr";

	//private static final String isolineConfigFilePath = System.getProperty("user.dir")+"/config/isoline/isoline.xml";
	//private static final String isolineDefaultConfigFilePath = System.getProperty("user.dir") + "/config/isoline/isoline_default.xml";

	private static final String basePath = IsolinesAttributes.class.getClassLoader().getResource("").getPath() + "isoline" + File.separator;
	private static final String isolineConfigFilePath = basePath+"isoline.xml";
	private static final String isolineDefaultConfigFilePath = basePath+ "isoline_default.xml";

	//预先定义的颜色数组
	private static final Color[] colorAry = new Color[]{
			new Color(255, 25, 25), new Color(25, 255, 25), new Color(25, 25, 255), new Color(255, 255, 25),
			new Color(25, 255, 255), new Color(255, 25, 255), new Color(125, 25, 25), new Color(185, 25, 185),
			new Color(25, 155, 125), new Color(25, 125, 165), new Color(155, 155, 25), new Color(55, 155, 25)
	};
	private static int colorIndex = 0;

	private boolean isFillSurfaceColor = false; //是否进行格点着色方式进行填充
	private SurfaceFillColorMode surfaceFillColorMode = SurfaceFillColorMode.YelloToGreen; //格点着色方式填充颜色
	private boolean isCutFromChinaArea = false; //是否按照中国区域切割绘制等值线

	private Color lineColor;
	/**
	 * 返回值为true时，按照中国区域切割绘制的等值线
	 */
	public boolean isCutFromChinaArea() {
		return isCutFromChinaArea;
	}
	/**
	 * 为true时，按照中国区域切割绘制的等值线
	 * @param isCutFromChinaArea
	 */
	public void setCutFromChinaArea(boolean isCutFromChinaArea) {
		this.isCutFromChinaArea = isCutFromChinaArea;
	}
	public SurfaceFillColorMode getSurfaceFillColorMode() {
		return surfaceFillColorMode;
	}
	/**
	 * 设置格点着色方式填充颜色样式
	 * @param surfaceFillColorMode
	 */
	public void setSurfaceFillColorMode(SurfaceFillColorMode surfaceFillColorMode) {
		this.surfaceFillColorMode = surfaceFillColorMode;
	}
	/**
	 * 是否进行格点着色方式进行填充
	 * @return
	 */
	public boolean isFillSurfaceColor() {
		return isFillSurfaceColor;
	}
	/**
	 * 设置是否进行格点着色方式进行填充
	 * @param isFillSurfaceColor
	 */
	public void setFillSurfaceColor(boolean isFillSurfaceColor) {
		this.isFillSurfaceColor = isFillSurfaceColor;
	}
	//以下用于过滤掉小于最小直径等值线
	protected boolean isFilterMinDiameter = false;

	/** 过滤站点经度列表 */
	protected List<String> filterLonLatAry = null;
	/** 是过滤站点经度*/
	protected boolean isFilterLonLat = false;

	//变压 变温需要改变负值的颜色
	protected boolean isDoubleLineColor = false;

	protected float lineColorSplitValue = 0;
	/** 该属性为线型渐变色分色比例(第一部分比例part1)；第二部分比例part2=1 - part1*/
	protected float lineColorProportion = 0.5f;
	/** 该属性为填充渐变色分色比例(第一部分比例part1)；第二部分比例part2=1 - part1*/
	protected float fillColorProportion = 0.5f;


	public float getLineColorProportion() {
		return lineColorProportion;
	}

	public void setLineColorProportion(float lineColorProportion) {
		this.lineColorProportion = lineColorProportion;
	}

	public float getFillColorProportion() {
		return fillColorProportion;
	}

	public void setFillColorProportion(float fillColorProportion) {
		this.fillColorProportion = fillColorProportion;
	}

	public boolean isFilterLonLat() {
		return isFilterLonLat;
	}

	public void setFilterLonLat(boolean isFilterLonLat) {
		this.isFilterLonLat = isFilterLonLat;
	}

	public List<String> getFilterLonLatAry() {
		return filterLonLatAry;
	}

	public void setFilterLonLatAry(List<String> filterLonLatAry) {
		this.filterLonLatAry = filterLonLatAry;
	}

	public float getLineColorSplitValue() {
		return lineColorSplitValue;
	}
	public void setLineColorSplitValue(float lineColorSplitValue) {
		this.lineColorSplitValue = lineColorSplitValue;
	}
	public boolean isDoubleLineColor() {
		return isDoubleLineColor;
	}
	public void setDoubleLineColor(boolean isDoubleLineColor) {
		this.isDoubleLineColor = isDoubleLineColor;
	}
	/**
	 * 是否过滤掉小于最小直径等值线,最小直径由setFilterMinDiameter设置(0.1-1.0之间)
	 * @return
	 */
	public boolean isFilterMinDiameter() {
		return isFilterMinDiameter;
	}
	public void setIsFilterMinDiameter(boolean isFilterMinDiameter) {
		this.isFilterMinDiameter = isFilterMinDiameter;
	}
	protected float filterMinDiameter = 0.3f;
	public float getFilterMinDiameter() {
		return filterMinDiameter;
	}
	/**
	 * 过滤掉小于最小直径等值线,最小直径由setFilterMinDiameter设置(0.1-1.0之间)
	 * @param filterMinDiameter
	 */
	public void setFilterMinDiameter(float filterMinDiameter) {
		this.filterMinDiameter = filterMinDiameter;
	}
	//过滤掉开放等值线
	protected boolean isFilterOpenLine = false;
	/**
	 * 是否过滤掉开放等值线
	 * @return
	 */
	public boolean isFilterOpenLine() {
		return isFilterOpenLine;
	}
	/**
	 * 设置是否过滤掉开放等值线
	 * @param isFilterOpenLine
	 */
	public void setFilterOpenLine(boolean isFilterOpenLine) {
		this.isFilterOpenLine = isFilterOpenLine;
	}
	protected boolean isSmoothLine = true;
	protected float smoothLevel = 0.0f;
	/**
	 * 是否平滑等值线线条, 平滑度由smoothLevel指定
	 * @return
	 */
	public boolean isSmoothLine() {
		return isSmoothLine;
	}
	public void setSmoothLine(boolean isSmoothLine) {
		this.isSmoothLine = isSmoothLine;
	}
	public float getSmoothLevel() {
		return smoothLevel;
	}
	/**
	 * 平滑等值线线条, 平滑度由smoothLevel指定,(0-10 0不平滑 10最平滑)
	 * @return
	 */
	public void setSmoothLevel(float smoothLevel) {
		this.smoothLevel = smoothLevel;
	}

	protected boolean isDrawLine = true;
	protected Color[] lineColors;       //等值线颜色(存放三个颜色,用于渐变效果,如果只放第一个颜色要素则为单色)
	final static private int lineColorGradeNum = 3;
	protected float lineWidth;       //等值线宽度
	protected LineStyle lineStyle;   //等值线线型
	protected boolean isLabelVal = true;     //是否标注值
	protected String sFormat = "7.1f"; //这里设置不起作用,在ElemCode.getIsolineLabelTextFromVal()方法中设置
	protected boolean isLabelCenter = true;  //是否标注中心
	protected CenterType labelCenterType = CenterType.HighLow;

	//设置四个阈值的限制是否设置，注意：需要修改equal, clone, hashCode及序列化中的内容 ！
	protected boolean isSetLineStartLimit = true;
	protected boolean isSetLineEndLimit = true;
	protected boolean isSetFillStartLimit = true;
	protected boolean isSetFillEndLimit = true;


	public boolean isSetLineStartLimit() {
		return isSetLineStartLimit;
	}
	public void setSetLineStartLimit(boolean isSetLineStartLimit) {
		this.isSetLineStartLimit = isSetLineStartLimit;
	}
	public boolean isSetLineEndLimit() {
		return isSetLineEndLimit;
	}
	public void setSetLineEndLimit(boolean isSetLineEndLimit) {
		this.isSetLineEndLimit = isSetLineEndLimit;
	}
	public boolean isSetFillStartLimit() {
		return isSetFillStartLimit;
	}
	public void setSetFillStartLimit(boolean isSetFillStartLimit) {
		this.isSetFillStartLimit = isSetFillStartLimit;
	}
	public boolean isSetFillEndLimit() {
		return isSetFillEndLimit;
	}
	public void setSetFillEndLimit(boolean isSetFillEndLimit) {
		this.isSetFillEndLimit = isSetFillEndLimit;
	}
	/**
	 * 是否绘制等值线线条
	 * @return
	 */
	public boolean isDrawLine()
	{
		return isDrawLine;
	}
	public void setDrawLine(boolean isDrawLine)
	{
		this.isDrawLine = isDrawLine;
	}

	public CenterType getLabelCenterType()
	{
		return labelCenterType;
	}

	public void setLabelCenterType(CenterType labelCenterType)
	{
		this.labelCenterType = labelCenterType;
	}

	/** 标注中心类型  高低、正负、冷暖**/
	public enum CenterType
	{
		HighLow("HighLow","高低"), PosNeg("PosNeg","正负"), WarmCool("WarmCool","冷暖");

		CenterType(String v,String dn) {
			value = v;
			displayName = dn;
		}
		public String value() {
			return value;
		}
		public static CenterType fromValue(String v) {
			for (CenterType c: CenterType.values()) {
				if (c.value.equals(v)) {
					return c;
				}
			}
			throw new IllegalArgumentException(v);
		}
		@Override
		public String toString() {

			return value;
		}
		public String getDisplayName(){
			return displayName;
		}
		private final String value;
		private final String displayName;
	}

	protected boolean isLineThick;    //是否加粗显示线条
	protected double thickVal=0, thickInteval=12;         //等值线加粗基准值及间隔值
	protected Map<Float, Float> lineThickList = new HashMap<Float, Float>(); //保留不同于lineWidth的线条（线值及线宽度值）
	protected Map<Float, LineStyle> lineStyleList = new HashMap<Float, LineStyle>(); //保留不同于lineStyle线型（线值及线型）
	protected Map<Float, Color>     lineColorList =  new TreeMap<>(new Comparator<Float>(){//保留不同于lineColor颜色（线值及线型）
		/*
         * int compare(Object o1, Object o2) 返回一个基本类型的整型，
         * 返回负数表示：o1 小于o2，
         * 返回0 表示：o1和o2相等，
         * 返回正数表示：o1大于o2。
         */
		public int compare(Float o1, Float o2) {

			//指定排序器按照降序排列
			return o2.compareTo(o1);
		}
	});

	protected boolean isShowSingleLine = false; //是否只显示lineShowList中指定的等值线线条
	protected Map<Float, Boolean> lineShowList = new HashMap<Float, Boolean>();

	protected boolean isHasSetLevels = false; //是否手工指定levels，如果为false，则利用maxVal,minVal, baseVal计算出levels
	public boolean isHasSetLevels()
	{
		return isHasSetLevels;
	}
	public void setHasSetLevels(boolean isHasSetLevels)
	{
		this.isHasSetLevels = isHasSetLevels;
	}
	protected boolean isShowInnerOutterLowLine = false; //是否只绘制最内侧及最外侧封闭等值线线条(低值封闭区)
	protected boolean isShowInnerOutterHighLine = false; //是否只绘制最内侧及最外侧封闭等值线线条(高值封闭区)
	public void setShowInnerOutterLowLine(boolean isShowInnerOutterLowLine)
	{
		this.isShowInnerOutterLowLine = isShowInnerOutterLowLine;
	}
	public void setShowInnerOutterHighLine(boolean isShowInnerOutterHighLine)
	{
		this.isShowInnerOutterHighLine = isShowInnerOutterHighLine;
	}
	public boolean isShowInnerOutterLowLine()
	{
		return isShowInnerOutterLowLine;
	}
	public boolean isShowInnerOutterHighLine()
	{
		return isShowInnerOutterHighLine;
	}

	protected boolean isShowInnerOutterLine = false; //是否只显示最内侧及最外侧封闭等值线线条

	/**
	 * 是否只显示最内侧及最外侧封闭等值线线条
	 */
	public boolean isShowInnerOutterLine() {
		return isShowInnerOutterLine;
	}
	/**
	 * 设置是否只显示最内侧及最外侧封闭等值线线条
	 */

	public void setShowInnerOutterLine(boolean isShowInnerOutterLine)
	{
		this.isShowInnerOutterLine = isShowInnerOutterLine;
	}

	private boolean isFill;         //是否填充等值线
	private float[] levels = null;
	private float valInterval;     //等值线间隔值(间隔、基准及最大最小值可计算出levels)
	private float valBaseVal;      //等值线基准值
	private float minVal = Float.MIN_VALUE, maxVal = Float.MAX_VALUE;

	private boolean isStreamline = false; //是否是流线图

	/**
	 * 获取是否为流线图
	 */
	public boolean isStreamline()
	{
		return isStreamline;
	}

	/**
	 * 设置是否为流线图
	 * @param   isStreamline
	 */
	public void setStreamline(boolean isStreamline)
	{
		this.isStreamline = isStreamline;
	}

	//等值线填充属性
	public static class FillAttr implements Cloneable{

		@Override
		public FillAttr clone(){
			FillAttr clone = null;
			try {
				clone = (FillAttr)super.clone();
				clone.color = new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), this.color.getAlpha());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			return clone;
		}

		public float getsLevel() {
			return slevel;
		}

		public void setsLevel(float level) {
			this.slevel = level;
		}

		public float geteLevel() {
			return elevel;
		}

		public void seteLevel(float level) {
			this.elevel = level;
		}

		public Color getColor() {
			return color;
		}

		public void setColor(Color color) {
			this.color = color;
		}

		public float getAlpha() {
			return alpha;
		}

		public void setAlpha(float alpha) {
			this.alpha = alpha;
		}

		public ShapeStyleUtil.FillStyle getStyle() {
			return style;
		}

		public void setStyle(ShapeStyleUtil.FillStyle style) {
			this.style = style;
		}

		public boolean isFill() {
			return fill;
		}

		public void setFill(boolean fill) {
			this.fill = fill;
		}



		//是否填充此区域
		private boolean fill = true;
		//填充起始值与结束值
		private float slevel;
		private float elevel;
		//填充颜色 透明度与样式
		private Color color = new Color(25, 255, 25);
		private float alpha = 0.2f;
		private ShapeStyleUtil.FillStyle style = ShapeStyleUtil.FillStyle.SOLID;
		public FillAttr(){

		}
		public FillAttr(float slevel, float elevel){
			this.slevel = slevel;
			this.elevel = elevel;
		}
		public FillAttr(float slevel, float elevel, Color color, float alpha, ShapeStyleUtil.FillStyle style){
			this.slevel = slevel;
			this.elevel = elevel;
			this.color = color;
			this.alpha = alpha;
			this.style = style;
		}

		public FillAttr createInstance(Node node) {
			if(node == null){
				return null;
			}
			FillAttr attr = this;
			NodeList list = node.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				Node subNode = list.item(i);
				String content = subNode.getTextContent();
				if(subNode.getNodeName().equals("slevel")){
					attr.slevel = Float.valueOf(content);
				}
				if(subNode.getNodeName().equals("elevel")){
					attr.elevel = Float.valueOf(content);
				}
				if(subNode.getNodeName().equals("color")){
					NodeList colorElemNodes = subNode.getChildNodes();
					int r = 0, g = 0, b = 0, a = 255;
					for (int l = 0; l < colorElemNodes.getLength(); l++) {
						Node colorElemNode = colorElemNodes.item(l);
						if(colorElemNode.getNodeName().equals("red")){
							r = Integer.valueOf(colorElemNode.getTextContent());
						}
						if(colorElemNode.getNodeName().equals("green")){
							g = Integer.valueOf(colorElemNode.getTextContent());
						}
						if(colorElemNode.getNodeName().equals("blue")){
							b = Integer.valueOf(colorElemNode.getTextContent());
						}
						if(colorElemNode.getNodeName().equals("alpha")){
							a = Integer.valueOf(colorElemNode.getTextContent());
						}
					}
					attr.color = new Color(r, g, b, a);
				}
				if(subNode.getNodeName().equals("alpha")){
					attr.alpha = Float.valueOf(content);
				}
				if(subNode.getNodeName().equals("style")){
					attr.style = ShapeStyleUtil.FillStyle.valueOf(content);
				}
			}
			return attr;
		}
	}
	private List<FillAttr> fillAry = new ArrayList<FillAttr>(); //填充用内容
	public List<FillAttr> getFillAttr()
	{
		return this.fillAry;
	}
	public void setFillAttr(List<FillAttr> fillAry)
	{
		this.fillAry = fillAry;

	}
	/**
	 * 获取指定的level span的填充颜色
	 * @param   slevel
	 * @param   elevel
	 * @return
	 */
	public Color getFillColor(float slevel, float elevel)
	{
		int index = getFillIndex(slevel, elevel);
		if (index < 0)
			return new Color(0.1f, 0.1f, 0.5f);
		return this.fillAry.get(index).getColor();
	}
	/**
	 * 获取指定的level span的填充透明度
	 * @param  slevel
	 * @param  elevel
	 * @return
	 */
	public float getFillAlpha(float slevel, float elevel)
	{
		int index = getFillIndex(slevel, elevel);
		if (index < 0)
			return 0.5f;
		return this.fillAry.get(index).getAlpha();
	}
	/**
	 * 获取指定的level span的填充样式
	 * @param   slevel
	 * @param   elevel
	 * @return
	 */
	public FillStyle getFillStyle(float slevel, float elevel)
	{
		int index = getFillIndex(slevel, elevel);
		if (index < 0)
			return FillStyle.SOLID;
		return this.fillAry.get(index).getStyle();
	}
	/**
	 * 获取填充等级(sLevel)
	 * @return
	 */
	public float[] getFillsLevels()
	{
		float[] sLevels = null;
		if (this.fillAry != null && this.fillAry.size() > 0){
			sLevels = new float[fillAry.size()];
			for (int i = 0; i < this.fillAry.size(); i++){
				sLevels[i] = fillAry.get(i).slevel;
			}
		}
		return sLevels;

	}
	/**
	 * 获取填充等级(eLevel)
	 * @return
	 */
	public float[] getFilleLevels()
	{
		float[] eLevels = null;
		if (this.fillAry != null && this.fillAry.size() > 0){
			eLevels = new float[fillAry.size()];
			for (int i = 0; i < this.fillAry.size(); i++){
				eLevels[i] = fillAry.get(i).elevel;
			}
		}
		return eLevels;
	}

	/**
	 * 获取指定的范围是否填充
	 * @param slevel
	 * @param elevel
	 * @return
	 */
	public boolean isFill(float slevel, float elevel)
	{
		int index = getFillIndex(slevel, elevel);
		if (index < 0)
			return false;
		return this.fillAry.get(index).isFill();
	}


	/**
	 * 获取填充的起始结束等级合集的数组
	 * @return
	 */
	public float[] getAllfillLevels()
	{
		float[] sLevels = getFillsLevels();
		float[] eLevels = getFilleLevels();
		if (sLevels != null && eLevels != null && (sLevels.length == eLevels.length)){
			List<Float> fillLevels = new ArrayList<Float>();
			for (int i = 0; i < sLevels.length; i++){
				if (fillLevels.indexOf(sLevels[i]) < 0)
					fillLevels.add(sLevels[i]);
				if (fillLevels.indexOf(eLevels[i]) < 0)
					fillLevels.add(eLevels[i]);
			}
			if (fillLevels.isEmpty())
				return null;
			float[] resLevels = new float[fillLevels.size()];
			for (int i = 0; i < fillLevels.size(); i++){
				resLevels[i] = fillLevels.get(i);
			}
			return resLevels;
		}
		return null;
	}

	private int getFillIndex(float slevel, float elevel)
	{
		int index = -1;
		if (this.fillAry != null && this.fillAry.size() > 0){
			for (int i = 0; i < this.fillAry.size(); i++){
				float fillslevel = this.fillAry.get(i).getsLevel();
				float fillelevel = this.fillAry.get(i).geteLevel();
				if (((fillslevel == slevel) && (fillelevel == elevel))
						|| ((fillslevel == elevel) && (fillelevel == slevel)))
				{
					index = i;
					break;
				}
				//这个判断用于实现指定的level,span与配置中不一致,但实质是读取一个内容(如5,-5其实与0,5的配置是一样的)
//				else if (Math.abs(filllevel - (level + span)) < 0.00001 && Math.abs(fillspan + span)<0.00001){
//					index = i;
//					break;
//				}
			}
		}
		//如果没有找到与slevel,elevel一致的配置，则找最接近的配置!
		if (index < 0){
			int res = getFillIndexProximal(slevel, elevel);
			System.err.print("等值线配置中没有指定slevel =" + slevel + ", elevel = " + elevel + "填充颜色信息!");
			if (res >= 0){
				System.err.print("采用slevel =" + this.fillAry.get(res).getsLevel() + ", elevel = "
						+ this.fillAry.get(res).geteLevel() + "填充配置信息!");
			}
			System.err.println();
			return res;
		}
		return index;
	}

	private int getFillIndexProximal(float slevel, float elevel)
	{
		int index = -1;
		float minleveldiff = 99999;
		if (this.fillAry != null && this.fillAry.size() > 0){
			for (int i = 0; i < this.fillAry.size(); i++){
				float fillslevel = this.fillAry.get(i).getsLevel();
				float fillelevel = this.fillAry.get(i).geteLevel();
				float diff = Math.abs(fillslevel - slevel) + Math.abs(fillelevel - elevel);
				if (diff < minleveldiff){
					minleveldiff = diff;
					index = i;
				}
			}
		}
		return index;
	}

	/**
	 * 是否填充等值线
	 * @return
	 */
	public boolean isFill()
	{
		return isFill;
	}

	/**
	 * 设置是否填充等值线
	 * @param   isFill
	 */
	public void setFill(boolean isFill)
	{
		this.isFill = isFill;
	}


	public IsolinesAttributes()
	{
//		lineColor = colorAry[(colorIndex++)%(colorAry.length)];
		lineColors = new Color[lineColorGradeNum];
		lineColors[0] = new Color(colorAry[(colorIndex++)%(colorAry.length)].getRGB());;

		lineWidth = 1.5f;
		lineStyle = LineStyle.SOLID;
		valInterval = 1.0f;
	}

	/**
	 * 获取线颜色(单色)
	 * @return
	 */
	public Color getLineColor()
	{
		return lineColors[0];
	}

	/**
	 * 获取线颜色(渐变色)
	 * @return
	 */
	public Color[] getLineColors()
	{
		return lineColors;
	}
	/**
	 * 获取指定值线颜色
	 * @param Val
	 * @return
	 */
	public Color getLineColor(float Val)
	{
		if (lineColorList != null && lineColorList.size() > 0 && lineColorList.containsKey(Val))
			return lineColorList.get(Val);
		return lineColors[0];
	}

	public Map<Float, Color> getLineColorList()
	{
		return lineColorList;
	}

	public void setLabelFormat(String sFormat)
	{
		this.sFormat = sFormat;
	}
	public String getLabelFormat()
	{
		return this.sFormat;
	}

	/**
	 * 设置等值线为颜色数组中的下一个颜色
	 */
	public void changeLineColor()
	{
		for (int i = 0; i < this.lineColors.length; i++){
			if (i == 0)
				this.lineColors[i] = new Color(colorAry[(colorIndex++)%(colorAry.length)].getRGB());
			else
				this.lineColors[i] = null;
		}
	}
	/**
	 * 获取颜色数组最大颜色个数
	 * @return
	 */
	public static int getColorArySize()
	{
		return colorAry.length;
	}

	/**
	 * 设置线颜色(设置单色)
	 * @param lineColor
	 */
	public void setLineColor(Color lineColor)
	{
		for (int i = 0; i < this.lineColors.length; i++){
			if (i == 0)
				this.lineColors[i] = lineColor;
			else
				this.lineColors[i] = null;
		}
	}
	/**
	 * 设置线颜色(设置数组，用于渐变)
	 * @param lineColors
	 */
	public void setLineColors(Color[] lineColors)
	{
		if (lineColors == null || lineColors.length == 0)
			return;
		List<Color> tmpColors = new ArrayList<Color>(3);
		for (int i = 0; i < lineColors.length; i++){
			if (lineColors[i] != null)
				tmpColors.add(lineColors[i]);
		}
		int colorNum = tmpColors.size();
		if (colorNum > lineColorGradeNum)
			colorNum = lineColorGradeNum;
		this.lineColors = new Color[lineColorGradeNum];
		for (int i = 0; i < colorNum; i++){
			this.lineColors[i] = new Color(lineColors[i].getRGB());
		}
		//这里需要更新lineColorList!!!
		if (levels != null && levels.length > 0){
			final List<Color> gradualColors = getGradualColors(this.lineColors,
					this.lineColorProportion,levels.length , this.lineColors[0].getAlpha());
			if(gradualColors!=null&&gradualColors.size()>0){
				if (this.lineColorList == null)
					this.lineColorList = new HashMap<Float, Color>(levels.length);
				else if (!this.lineColorList.isEmpty())
					this.lineColorList.clear();
				for(int i=0;i<levels.length;i++){
					this.lineColorList.put(levels[i], gradualColors.get(i));
				}
			}
		}
	}

	/**
	 * 获取线宽度
	 * @return
	 */
	public float getLineWidth()
	{
		return lineWidth;
	}
	/**
	 * 根据给定的值获取线宽
	 * @param Val
	 * @return
	 */
	public float getLineWidth(float Val)
	{
		float width = this.lineWidth;
		if (isLineThick){ //如果是加粗显示,则根据给定的值决定是否进行加粗
			if (lineThickList.containsKey(Val))
				width = lineThickList.get(Val);
			else {
				if ((Val - thickVal)%thickInteval == 0){
					if (width > 1.5)
						width += 1.5;
					else
						width += 1;
					lineThickList.put(Val, width);
				}
			}
		}
		return width;
	}

	/**
	 * 设置线宽度
	 * @param lineWidth
	 */
	public void setLineWidth(float lineWidth)
	{
		this.lineWidth = lineWidth;
	}

	/**
	 * 获取线样型
	 * @return
	 */
	public LineStyle getLineStyle()
	{
		return lineStyle;
	}
	/**
	 * 根据给定的值获取线型
	 * @param Val
	 * @return
	 */
	public LineStyle getLineStyle(float Val)
	{
		LineStyle style = this.lineStyle;
		if (lineStyleList.containsKey(Val))
			style = lineStyleList.get(Val);
		return style;
	}

	/**
	 * 设置线型
	 * @param lineStyle
	 */
	public void setLineStyle(LineStyle lineStyle)
	{
		this.lineStyle = lineStyle;
	}
	/**
	 * 设置给定值的线型
	 * @param lineStyle
	 */
	public void setLineStyle(float Val, LineStyle lineStyle)
	{
		lineStyleList.put(Val, lineStyle);
	}

	/**
	 * 等值线是否标值
	 * @return
	 */
	public boolean isLabelVal()
	{
		return isLabelVal;
	}

	/**
	 * 设置等值线是否标注值
	 * @param   isLabelVal
	 */
	public void setLabelVal(boolean isLabelVal)
	{
		this.isLabelVal = isLabelVal;
	}

	/**
	 * 等值线是否标注中心
	 * @return
	 */
	public boolean isLabelCenter() {
		return isLabelCenter;
	}

	/**
	 * 设置等值线是否标注中心
	 * @param   isLabelCenter
	 */
	public void setLabelCenter(boolean isLabelCenter)
	{
		this.isLabelCenter = isLabelCenter;
	}

	/**
	 * 是否间隔加粗显示等值线
	 */
	public boolean isLineThick()
	{
		return isLineThick;
	}

	/**
	 * 设置间隔加粗显示等值线
	 * @param   isLineThick
	 */
	public void setLineThick(boolean isLineThick)
	{
		this.isLineThick = isLineThick;
	}

	/**
	 * 获取等值线的levels
	 * @return
	 */
	public float[] getLevels()
	{
		return levels;
	}

	/**
	 * 设置等值线加粗的基准值及间隔值
	 * @param  thickVal
	 * @param interval
	 */
	public void setThickVal(float thickVal, float interval)
	{
		this.thickVal = thickVal;
		this.thickInteval = interval;
	}

	/**
	 * 设置等值线的Levels
	 * @param  levels
	 */
	public void setLevels(float[] levels)
	{
		this.levels = levels;
	}

	/**
	 * 设置等值线的最大最小值
	 * @param maxVal
	 * @param minVal
	 */
	public void setMaxMinVal(float maxVal, float minVal)
	{
		this.maxVal = maxVal;
		this.minVal = minVal;
	}

	//*** XML格式输出与读取 ***
	/**
	 * 输出为XML格式的文件,保存等值线的配置信息
	 * @param output
	 * @throws XMLStreamException
	 */
	public void exportAsXML(Object output) throws XMLStreamException
	{
		XMLStreamWriter xmlWriter = null;
		boolean closeWriterWhenFinished = true;
		if (output instanceof XMLStreamWriter)
		{
			xmlWriter = (XMLStreamWriter) output;
			closeWriterWhenFinished = false;
		}
		else
		{
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			if (output instanceof Writer)
			{
				xmlWriter = factory.createXMLStreamWriter((Writer) output);
			}
			else if (output instanceof OutputStream)
			{
				xmlWriter = factory.createXMLStreamWriter((OutputStream) output);
			}
		}
		if (xmlWriter == null)
		{
			String message = "WriteAsXML.UnsupportedOutputObject";
			throw new IllegalArgumentException(message);
		}

		//写标识
		xmlWriter.writeStartElement(IsolinesAttributes.MIDSXML_NODENAME);
		//写线颜色
		xmlWriter.writeStartElement("color");
		if (this.getLineColors() != null){
			String strColors = "";
			for (int i = 0; i < this.getLineColors().length; i++){
				Color clr = this.getLineColors()[i];
				if (clr != null)
					strColors += MidsUtil.encodeColorRGBA(clr) + ",";
			}
			xmlWriter.writeCharacters(strColors);
		}
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement("linewidth"); //线宽
		xmlWriter.writeCharacters(Double.toString(this.getLineWidth()));
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement("linestyle"); //线型
		xmlWriter.writeCharacters(this.getLineStyle().toString());
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement("isdrawLine"); //是否绘制线
		xmlWriter.writeCharacters(booleanToStr(this.isDrawLine()));
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement("isfill"); //是否填充
		xmlWriter.writeCharacters(booleanToStr(this.isFill()));
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement("islabelval"); //是否标注值
		xmlWriter.writeCharacters(booleanToStr(this.isLabelVal()));
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement("islabelcenter"); //是否标注中心
		xmlWriter.writeCharacters(booleanToStr(this.isLabelCenter()));
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement("centertype"); //标注中心类型
		xmlWriter.writeCharacters(this.getLabelCenterType().toString());
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement("islinethick"); //是否加粗显示线条
		xmlWriter.writeCharacters(booleanToStr(this.isLineThick()));
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement("linethickval"); //加粗基准值及间隔值
		xmlWriter.writeCharacters(String.format("%f,%f ", this.thickVal, this.thickInteval));
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement("isstreamline"); //是否是流线图
		xmlWriter.writeCharacters(booleanToStr(this.isStreamline));
		xmlWriter.writeEndElement();
		xmlWriter.writeStartElement("iscutfromchinaarea"); //是否按照中国区切割
		xmlWriter.writeCharacters(booleanToStr(this.isCutFromChinaArea));
		xmlWriter.writeEndElement();

		if (this.levels != null){ //levels
			xmlWriter.writeStartElement("levels");
			for (int i = 0; i < levels.length; i++)
			{
				xmlWriter.writeCharacters(String.format("%f,",levels[i]));
			}
			xmlWriter.writeEndElement();
		}
		xmlWriter.writeEndElement();  //IsolinesAttr

		xmlWriter.flush();
		if (closeWriterWhenFinished)
			xmlWriter.close();
	}

	public static IsolinesAttributes createInstanceFromXML(Object input)
	{
		if (!(input instanceof Element)){
			return null;
		}
		Element elem = (Element)input;
		if (!elem.getTagName().equals(IsolinesAttributes.MIDSXML_NODENAME)){
			System.err.println("In IsolinesAttr createInstanceFromXML...TagName不正确!");
			return null;
		}
		NodeList children = elem.getChildNodes();
		IsolinesAttributes attr = new IsolinesAttributes();
		for (int i = 0; i < children.getLength(); i++){
			Node node = children.item(i);
//			if (node.getNodeType() != Node.TEXT_NODE){
//				//可能不一定是TEXT_NODE, 需要进行处理!
//				continue;
//			}
			String content = node.getTextContent().trim();
			if (content.isEmpty())
				continue;
			if (node.getNodeName().equals("color")){
				String[] strColorAry = content.split(",");
				Color[] colorAry = new Color[3];
				for (int k = 0; k < strColorAry.length; k++){
					colorAry[k] = MidsUtil.decodeColorRGBA(strColorAry[k]);
				}
				attr.setLineColors(colorAry);
			}
			else if (node.getNodeName().equals("linewidth")){
				attr.setLineWidth(Float.valueOf(content));
			}
			else if (node.getNodeName().equals("linestyle")){
				attr.setLineStyle(ShapeStyleUtil.LineStyle.fromValue(content));
			}
			else if (node.getNodeName().equals("islabelval")){
				attr.setLabelVal(MidsUtil.convertStringToBoolean(content));
			}
			else if (node.getNodeName().equals("isdrawline")){
				attr.setDrawLine(MidsUtil.convertStringToBoolean(content));
			}
			else if (node.getNodeName().equals("isfill")){
				attr.setFill(MidsUtil.convertStringToBoolean(content));
			}
			else if (node.getNodeName().equals("islabelcenter")){
				attr.setLabelCenter(MidsUtil.convertStringToBoolean(content));
			}
			else if (node.getNodeName().equals("centertype")){
				attr.setLabelCenterType(IsolinesAttributes.CenterType.fromValue(content));
			}
			else if (node.getNodeName().equals("islinethick")){
				attr.setLineThick(MidsUtil.convertStringToBoolean(content));
			}
			else if (node.getNodeName().equals("linethickval")){
				String[] strAry = content.split(" ");
				if (strAry.length != 2)
					continue;
				attr.setThickVal(Float.valueOf(strAry[0]), Float.valueOf(strAry[1]));
			}
			else if (node.getNodeName().equals("isstreamline")){
				attr.setStreamline(MidsUtil.convertStringToBoolean(content));
			}
			else if (node.getNodeName().equals("iscutfromchinaarea")){
				attr.setCutFromChinaArea(MidsUtil.convertStringToBoolean(content));
			}
			else if (node.getNodeName().equals("levels")){
				String[] strAry = content.split(",");
				if (strAry.length <= 0)
					continue;
				float[] levels = new float[strAry.length];
				for (int k = 0; k < levels.length; k++)
					levels[k] = Float.valueOf(strAry[k]);
				attr.setLevels(levels);
			}
		}
		//这里需要更新lineColorList!!!
		if (attr.levels != null && attr.levels.length > 0){
			final List<Color> gradualColors = getGradualColors(attr.lineColors,
					attr.lineColorProportion,attr.levels.length, attr.lineColors[0].getAlpha());
			if(gradualColors!=null&&gradualColors.size()>0){
				if (attr.lineColorList == null)
					attr.lineColorList = new HashMap<Float, Color>(attr.levels.length);
				else if (!attr.lineColorList.isEmpty())
					attr.lineColorList.clear();
				for(int i=0;i<attr.levels.length;i++){
					attr.lineColorList.put(attr.levels[i], gradualColors.get(i));
				}
			}
		}
		return attr;
	}

//	public static IsolinesAttributes createInstance(ElemCode elem, String press, String filename){
//
//		return createInstance(elem, press, new File(filename));
//	}
//
//
//	public static IsolinesAttributes createInstance(ElemCode elem, String press, File file){
//		IsolinesAttributes attributes = null;
//		if(file == null || !file.exists()){
//			System.out.println("配置文件不存在，创建默认的IsolinesAttributes");
//		}
//		try {
//			file = new File(URLDecoder.decode(isolineConfigFilePath,"UTF-8"));
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
//		if(!file.exists()){
//			System.out.println("配置文件" + isolineConfigFilePath + "不存在，读取默认配置" + isolineDefaultConfigFilePath);
//			file = new File(isolineDefaultConfigFilePath);
//		}
//		if(!file.exists()){
//			attributes = CreateDefaultInstance(elem, press);
//		} else{
//			ConfigKey configKey = new ConfigKey();
//			configKey.setFilename(file.getAbsolutePath());
//			configKey.setElemCode(elem);
//			configKey.setPress(press);
//			attributes = createInstance(configKey);
//		}
//		return attributes;
//	}


	public static IsolinesAttributes createInstance(ElemCode elem, String press){

		return CreateDefaultInstance(elem, press);
	}


//	public static IsolinesAttributes createInstance(ElemCode elem){
//		if (elem == null){
//			throw new RuntimeErrorException(null, "指定的要素为空!");
//		}
//		return createInstance(elem, null);
//	}
//
//
//	private static IsolinesAttributes createInstance(ConfigKey configKey){
//		IsolinesAttributes attr = null;
//		if(configKey == null){
//			System.out.println("配置文件不存在，创建默认的IsolinesAttributes");
//			attr = CreateDefaultInstance(ElemCode.HH, DataLevel.D0000.getFileValue());
//		}
//		String fileName = configKey.getConfigFileName();
//		File target = new File(fileName);
//		IsolineGloableConfig fcGloableConfig = new IsolineGloableConfig();
//		fcGloableConfig = fcGloableConfig.createInstance(target);
//		attr = fcGloableConfig.getConfig(configKey);
//		if(attr == null){
//			System.out.println("配置文件中不存在该要素对应的等值线配置，创建默认的IsolinesAttributes");
//			attr = CreateDefaultInstance(configKey.getElemCode(), configKey.getPress());
//		}
//		return attr;
//	}



	static public float getNullVal()
	{
		return -99999;
	}







	/**
	 * 根据要素层次创建实例,建立基本配置,应该从默认配置文件中读取
	 * @param elem
	 * @param press
	 * @return
	 */
	static public IsolinesAttributes CreateDefaultInstance(ElemCode elem, String press){
		IsolinesAttributes attr = new IsolinesAttributes();

		float valInterval = 1, valBaseVal = getNullVal(), minVal = getNullVal(), maxVal = getNullVal();

		float thickVal = 0, thickInterval = 12;
		attr.setLabelVal(true);
		Color color = Color.BLUE;
		attr.isLabelCenter = false;
		boolean isDoubleLineColor = false; //变压 变温需要改变负值的颜色
		Color negativeColor = null;
		switch (elem) {
			case DI:  //K511 散度
				attr.isHasSetLevels = true;
				attr.setLevels(new float[]{-60, -30, 0});
				attr.isDrawLine = true;
				attr.isFill = true;
				attr.isLabelCenter = false;
				attr.fillAry = new ArrayList<FillAttr>(5);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-1000f, -60f, new Color(50, 155,155),0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-60f, -30f, new Color(100,195,195),0.5f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-30f, 0f, new Color(150,255,255),0.4f, ShapeStyleUtil.FillStyle.SOLID));
				attr.lineWidth = 1.0f;
				attr.setSmoothLevel(5.0f);
				attr.setFilterOpenLine(true);
				attr.setIsFilterMinDiameter(true);
				attr.setFilterMinDiameter(10.0f);
				break;
			case VO:  //K511涡度
				color = new Color(165, 45, 115);
				attr.isHasSetLevels = true;
				attr.setLevels(new float[]{0, 20, 40, 60});
//			attr.setLevels(new float[]{20, 40, 60});
				attr.isDrawLine = true;
				attr.isFill = true;
				attr.lineWidth = 2.0f;
				attr.isLabelCenter = false;
				attr.fillAry = new ArrayList<FillAttr>(5);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0f,  20f, new Color(255,155,255), 0.3f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(20f, 40f, new Color(205,100,205), 0.4f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(40f, 60f, new Color(155, 50,155), 0.5f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(60f, 800f,new Color(200,  0,100), 0.6f, ShapeStyleUtil.FillStyle.SOLID));

				attr.setSmoothLevel(5.0f);
				attr.setIsFilterMinDiameter(true);
				attr.setFilterMinDiameter(10.0f);
				attr.setFilterOpenLine(true);
				break;
			case WW:  //K511垂直速度
				attr.isHasSetLevels = true;
				color = new Color(45, 45, 45);
				attr.setLevels(new float[]{-16, -12, -8, -4, 0});
				attr.isDrawLine = true;
				attr.isFill = true;
				attr.lineWidth = 1.0f;
				attr.isLabelCenter = false;
				attr.fillAry = new ArrayList<FillAttr>(5);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-800f, -12, new Color(120,120, 0 ),0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-12f,  -8f, new Color(155,155, 50),0.5f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-8f,  -4f, new Color(205,205, 100),0.4f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-4f, 0f, new Color(255,255, 150),0.3f, ShapeStyleUtil.FillStyle.SOLID));

				attr.setSmoothLevel(5.0f);
				attr.setFilterOpenLine(true);
				attr.setIsFilterMinDiameter(true);
				attr.setFilterMinDiameter(10.0f);
				break;
			case TH:  //K511 温度露点差
				minVal = 0;
				maxVal = 50;
				attr.setLabelCenter(false);
				attr.setLineThick(false);
				color = new Color(65, 185, 65, 255);
				attr.isHasSetLevels = true;
				attr.setLevels(new float[]{0, 2.0f, 4.0f});
				attr.isFill = true;
				attr.lineWidth = 2.0f;

				attr.fillAry = new ArrayList<FillAttr>(1);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.0f, 2.0f, new Color(85, 180, 85),   0.75f, ShapeStyleUtil.FillStyle.POINT));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(2.0f, 4.0f, new Color(155, 220, 155), 0.4f, ShapeStyleUtil.FillStyle.POINT));

				attr.setFilterOpenLine(true);
				attr.setIsFilterMinDiameter(true);
				attr.setFilterMinDiameter(10.0f);
				break;
			case DH03: //3H变高
//			valInterval = 1;
//			valBaseVal = 0;
//			minVal = -20;
//			maxVal = 20;
				attr.isHasSetLevels = true;
				float[] levels = new float[]{-20, -18, -16, -12, -10, -8, 8, 10, 12, 14, 16, 18, 20};
				color = new Color(65, 65, 195);
				attr.isLabelCenter = false;
				attr.setLineStyle(LineStyle.DASH);
				isDoubleLineColor = true;
				negativeColor = new Color(205, 35, 35);
				break;
			case DH24: //24H变高
				attr.isHasSetLevels = true;
				levels = new float[]{-200, -180, -160, -120, -100, -80, 80, 100, 120, 140, 160, 180, 200};
				color = new Color(65, 65, 195);
				attr.setLevels(levels);
				attr.isLabelCenter = false;
				attr.setLineStyle(LineStyle.DASH);
				isDoubleLineColor = true;
				negativeColor = new Color(205, 35, 35);
				break;
			case DP03: //3小时变压   正值蓝色 负值红色
//			valInterval = 2.5f;
//			valBaseVal = 5;
//			minVal = 2.5f;
//			maxVal = 100;
				attr.isHasSetLevels = true;
				levels = new float[]{-50, -45, -40, -35, -30, -25, -20, -15, -10, -5,
						5,10, 15,20, 25, 30, 35, 40,45, 50};
				attr.setLevels(levels);
				color = new Color(65, 65, 195);
				attr.isLabelCenter = false;
				attr.setLineStyle(LineStyle.DASH);
				isDoubleLineColor = true;
				negativeColor = new Color(205, 35, 35);
				break;
			case DP24: //24小时变压 ，正值蓝色 负值红色
//			valInterval = 2.5f;
//			valBaseVal = 5;
//			minVal = 2.5f;
//			maxVal = 100;
				attr.isHasSetLevels = true;
//			levels = new float[]{-50, -45, -40, -35, -30, -25, -20, -17.5f, -15, -12.5f, -10, -7.5f, -5, -2.5f,
//					2.5f, 5, 7.5f, 10, 12.5f, 15, 17.5f, 20, 25, 30, 35, 40,45, 50};
				levels = new float[]{-50, -45, -40, -35, -30, -25, -20, -15, -10, -5,
						5,10, 15,20, 25, 30, 35, 40,45, 50};
				attr.setLevels(levels);
				color = new Color(65, 65, 195);
				attr.isLabelCenter = false;
				attr.setLineStyle(LineStyle.DASH);
				isDoubleLineColor = true;
				negativeColor = new Color(205, 35, 35);
				break;
			case DT03:
				levels = new float[]{-10, -8, -6, -4, -2, 2, 4, 6, 8, 10};
				attr.setLevels(levels);
				attr.isHasSetLevels = true;
//			valInterval = 2.0f;
//			valBaseVal = 4;
//			minVal = -40.0f;
//			maxVal = 40;
				color = new Color(205, 35, 35);
				negativeColor = new Color(65, 65, 195);
				attr.setLineStyle(LineStyle.DOT);
				attr.isLabelCenter = false;
				isDoubleLineColor = true;
				break;
			case DT24: //24变温  正值红色 负值蓝色
				levels = new float[]{-24, -22, -20, -18, -16, -14, -12, -10, -8, -6, -4, 4, 8, 10, 12, 14, 16, 18, 20, 22, 24};
				attr.setLevels(levels);
				attr.isHasSetLevels = true;
//			valInterval = 2.0f;
//			valBaseVal = 4;
//			minVal = -40.0f;
//			maxVal = 40;
				color = new Color(205, 35, 35);
				negativeColor = new Color(65, 65, 195);
				attr.setLineStyle(LineStyle.DOT);
				attr.isLabelCenter = false;
				isDoubleLineColor = true;
				break;
			case LS: //大尺度降水
			case RAIN03://
			case RAIN06://
			case RAIN12://
			case RAIN24://
			case RAINDAY: //日持续降水，从08时开始后累积降水
			case RAIN36://
			case RAIN48://
			case TR03://
			case TR06://
			case TR12://
			case TR24://
			case RN:
			case RN01:
			case TR: //KJT511 降水,按小雨、中雨、大雨、暴雨、大暴雨、特大暴雨分级填充
//				levels = new float[]{0.1f, 10, 25, 50, 100, 250, 400};
				levels = new float[]{0.1f, 5, 10, 25, 50, 100,250};
//			levels = new float[]{0.1f, 4, 13, 25, 60, 250, 400};
				attr.setLevels(levels);
				attr.isHasSetLevels = true;
				attr.isFill = true;
				attr.isLabelCenter = false;
				attr.fillAry = new ArrayList<FillAttr>(10);
				attr.setLineWidth(0.5f);
				attr.setSmoothLine(true);
				attr.setSmoothLevel(6.0f);
				color = new Color(45, 45, 245);
				//KJ使用
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(0.1f, 4f, new Color(205,240,205),0.4f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(4f, 13f,  new Color(160,215,165),0.5f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(13f,25f,  new Color(155,185,240),0.6f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(25f, 60f, new Color(105,135,235),0.7f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(60f, 250f, new Color(240,195,240),0.8f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(250f, 400f, new Color(250,100,245),0.9f, ShapeStyleUtil.FillStyle.SOLID));

				//原来使用
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(0.1f, 10f, new Color(126,255,122),0.2f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(10f, 25f,   new Color(0,135,0),    0.3f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(25f, 50f,   new Color(129,123,255),0.4f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(50f, 100f,   new Color(47,90,229),  0.5f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(100f, 250f, new Color(210,1,216),  0.6f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(250f, 400f, new Color(146,1,56),   0.7f, ShapeStyleUtil.FillStyle.SOLID));
				//中央台预报使用
//				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.1f, 10f,  new Color(188,244,217),0.4f, ShapeStyleUtil.FillStyle.SOLID));
//				attr.fillAry.add(new IsolinesAttributes.FillAttr(10f, 25f,   new Color(136,224,228),0.5f, ShapeStyleUtil.FillStyle.SOLID));
//				attr.fillAry.add(new IsolinesAttributes.FillAttr(25f, 50f,   new Color(43,175,223), 0.6f, ShapeStyleUtil.FillStyle.SOLID));
//				attr.fillAry.add(new IsolinesAttributes.FillAttr(50f, 100f,  new Color(36,151,221),  0.7f, ShapeStyleUtil.FillStyle.SOLID));
//				attr.fillAry.add(new IsolinesAttributes.FillAttr(100f, 250f, new Color(37,120,224),  0.8f, ShapeStyleUtil.FillStyle.SOLID));
//				attr.fillAry.add(new IsolinesAttributes.FillAttr(250f, 400f, new Color(20,74,183),   0.9f, ShapeStyleUtil.FillStyle.SOLID));

				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.1f, 5f,  new Color(88,194,227),0.4f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(5f,  10f,   new Color(66,174,208),0.5f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(10f, 25f,   new Color(33,155,203), 0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(25f, 50f,  new Color(26,131,191),  0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(50f, 100f, new Color(27,100,184),  0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(100f, 250f, new Color(10,74,163),   0.9f, ShapeStyleUtil.FillStyle.SOLID));



//				attr.fillAry.add(new IsolinesAttributes.FillAttr(0f, 5f,  new Color(255,0,0),0.9f, ShapeStyleUtil.FillStyle.SOLID));
//				attr.fillAry.add(new IsolinesAttributes.FillAttr(5f, 10f,  new Color(0,255,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
//				attr.fillAry.add(new IsolinesAttributes.FillAttr(10f, 15f, new Color(0,0,255), 0.7f, ShapeStyleUtil.FillStyle.SOLID));


//				attr.fillAry.add(new IsolinesAttributes.FillAttr(0f, 1f,  new Color(255,0,0),0.9f, ShapeStyleUtil.FillStyle.SOLID));
//				attr.fillAry.add(new IsolinesAttributes.FillAttr(1f, 2f,   new Color(0,255,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
//				attr.fillAry.add(new IsolinesAttributes.FillAttr(2f, 3f,   new Color(0,0,255), 0.7f, ShapeStyleUtil.FillStyle.SOLID));
//				attr.fillAry.add(new IsolinesAttributes.FillAttr(3f, 10f,   new Color(0,255,255), 0.7f, ShapeStyleUtil.FillStyle.SOLID));

				break;
			case RAINOUT24://
			case TROUT24://
				levels = new float[]{0.1f, 10, 25, 50, 100, 250, 400};
//			levels = new float[]{0.1f, 4, 13, 25, 60, 250, 400};
				attr.setLevels(levels);
				attr.isHasSetLevels = true;
				attr.isFill = true;
				attr.isLabelCenter = false;
				attr.fillAry = new ArrayList<FillAttr>(10);
				attr.setLineWidth(0.5f);
				attr.setSmoothLine(true);
				attr.setSmoothLevel(6.0f);
				color = new Color(185, 185, 245);
				//中央台预报使用
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.1f, 10f,  new Color(188,244,217),0.4f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(10f, 25f,   new Color(136,224,228),0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(25f, 50f,   new Color(43,175,223), 0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(50f, 100f,  new Color(36,151,221), 0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(100f, 250f, new Color(37,120,224), 0.9f,ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(250f, 400f, new Color(20,74,183),  0.95f,ShapeStyleUtil.FillStyle.SOLID));
				break;
			case STAT_M_LP:
			case STAT_M_SLP:
			case SLP:
			case MPR:
			case PR:
				valInterval = 2.5f;
				valBaseVal = 1010;
				minVal = 900;
				maxVal = 1100;
				thickVal = 1000;
				thickInterval = 20;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLabelCenter(true);
				attr.setLabelCenterType(CenterType.HighLow);
				attr.setThickVal(thickVal, thickInterval);
				attr.setLineThick(true);
				attr.setIsFilterMinDiameter(true);
				attr.setFilterMinDiameter(3.0f);
				attr.setSmoothLine(true);
				attr.setSmoothLevel(6.0f);
				color = new Color(45, 45, 45);
				//APP端用的线颜色及线宽
				//高压用红色,低压用蓝色
				attr.lineColorList = new HashMap<>();
				attr.lineThickList = new HashMap<>();
				float basethick = 1.5f;
				attr.lineColorList.put(900f,   new Color(155, 0, 160, 255));
				attr.lineColorList.put(902.5f, new Color(160, 0, 170, 255));
				attr.lineColorList.put(905f,   new Color(165,  0,180, 255));
				attr.lineColorList.put(907.5f, new Color(175,  0,190, 255));
				attr.lineColorList.put(910f,   new Color(185,  5, 200, 255));
				attr.lineColorList.put(912.5f, new Color(190,  5, 195, 255));
				attr.lineColorList.put(915f,   new Color(195, 15, 190, 255));
				attr.lineColorList.put(917.5f, new Color(190, 15, 195, 255));
				attr.lineColorList.put(920f,   new Color(190, 15, 200, 255));
				attr.lineColorList.put(922.5f, new Color(200, 15, 205, 255));
				attr.lineColorList.put(925f,   new Color(215, 15, 210, 255));
				attr.lineColorList.put(927.5f, new Color(225, 15, 215, 255));
				attr.lineColorList.put(930f,   new Color(235, 25, 220, 255));
				attr.lineColorList.put(932.5f, new Color(235, 25, 200, 255));
				attr.lineColorList.put(935f,   new Color(235, 55, 180, 255));
				attr.lineColorList.put(937.5f, new Color(235, 55, 170, 255));
				attr.lineColorList.put(940f,   new Color(235, 55, 160, 255));
				attr.lineColorList.put(942.5f, new Color(235, 55, 140, 255));
				attr.lineColorList.put(945f,   new Color(235, 65, 120, 255));
				attr.lineColorList.put(947.5f, new Color(235, 65, 100, 255));
				attr.lineColorList.put(950f,   new Color(240, 65, 90, 255));
				attr.lineColorList.put(952.5f, new Color(245, 65, 80, 255));
				attr.lineColorList.put(955f,   new Color(235, 85, 20, 255));
				attr.lineColorList.put(957.5f, new Color(240, 85, 25, 255));
				attr.lineColorList.put(960f,   new Color(245, 95, 30, 255));
				attr.lineColorList.put(962.5f, new Color(245, 100, 30, 255));
				attr.lineColorList.put(965f,   new Color(235, 105, 35, 255));
				attr.lineColorList.put(967.5f, new Color(235, 105, 40, 255));
				attr.lineColorList.put(970f,   new Color(235, 125, 50, 255));
				attr.lineColorList.put(972.5f, new Color(235, 140, 55, 255));
				attr.lineColorList.put(975f,   new Color(225, 165, 60, 255));
				attr.lineColorList.put(977.5f, new Color(200, 170, 60, 255));
				attr.lineColorList.put(980f,   new Color(175, 175, 60, 255));
				attr.lineColorList.put(982.5f, new Color(190, 185, 80, 255));
				attr.lineColorList.put(985f,   new Color(205, 205, 100, 255));
				attr.lineColorList.put(987.5f, new Color(200, 210, 110, 255));
				attr.lineColorList.put(990f,   new Color(195, 215, 120, 255));
				attr.lineColorList.put(992.5f, new Color(175, 215, 140, 255));
				attr.lineColorList.put(995f,   new Color(155, 215, 160, 255));
				attr.lineColorList.put(997.5f, new Color(110, 220, 170, 255));
				attr.lineColorList.put(1000f,  new Color(65, 235, 180, 255));
				attr.lineColorList.put(1002.5f, new Color(35, 235, 180, 255));
				attr.lineColorList.put(1005f,   new Color(5,  235, 180, 255));
				attr.lineColorList.put(1007.5f, new Color(5,  220, 190, 255));
				attr.lineColorList.put(1010f,   new Color(5,  200, 200, 255));
				attr.lineColorList.put(1012.5f, new Color(5,  200, 215, 255));
				attr.lineColorList.put(1015f,   new Color(5,  195, 230, 255));
				attr.lineColorList.put(1017.5f, new Color(5,  175, 235, 255));
				attr.lineColorList.put(1020f,   new Color(5,  155, 240, 255));
				attr.lineColorList.put(1022.5f, new Color(5,  135, 245, 255));
				attr.lineColorList.put(1025f, new Color(5,  115, 250, 255));
				attr.lineColorList.put(1027.5f, new Color(5,  100, 250, 255));
				attr.lineColorList.put(1030f,   new Color(85,  85, 250, 255));
				attr.lineColorList.put(1032.5f, new Color(80,  80, 245, 255));
				attr.lineColorList.put(1035f, new Color(75,  75, 240, 255));
				attr.lineColorList.put(1037.5f, new Color(60,  60, 235, 255));
				attr.lineColorList.put(1040f, new Color(55,  55, 230, 255));
				attr.lineColorList.put(1042.5f, new Color(50,  50, 225, 255));
				attr.lineColorList.put(1045f, new Color(45,  45, 220, 255));
				attr.lineColorList.put(1047.5f, new Color(40,  40, 210, 255));
				attr.lineColorList.put(1050f, new Color(35,  35, 200, 255));
				attr.lineColorList.put(1052.5f, new Color(30,  30, 190, 255));
				attr.lineColorList.put(1055f, new Color(25,  25, 180, 255));
				attr.lineColorList.put(1057.5f, new Color(10,  10, 170, 255));
				attr.lineColorList.put(1060f, new Color(0,  0, 160, 255));
				attr.lineColorList.put(1062.5f, new Color(0,  0, 150, 255));
				attr.lineColorList.put(1065f, new Color(0,  0, 140, 255));
				attr.lineColorList.put(1067.5f, new Color(0,  0, 130, 255));
				attr.lineColorList.put(1070f, new Color(0,  0, 120, 255));
				attr.lineColorList.put(1072.5f, new Color(0,  0, 110, 255));
				attr.lineColorList.put(1075f, new Color(0,  0, 100, 255));
				attr.lineColorList.put(1077.5f, new Color(0,  0, 80, 255));
				attr.lineColorList.put(1080f, new Color(0,  0, 60,  255));
				attr.lineColorList.put(1082.5f, new Color(0,  0, 60,  255));
				attr.lineColorList.put(1085f, new Color(0,  0, 60,  255));
				attr.lineColorList.put(1087.5f, new Color(0,  0, 60,  255));
				attr.lineColorList.put(1090f, new Color(0,  0, 60,  255));
				attr.lineColorList.put(1092.5f, new Color(0,  0, 60,  255));
				attr.lineColorList.put(1095f, new Color(0,  0, 60,  255));
				attr.lineColorList.put(1097.5f, new Color(0,  0, 60,  255));
				attr.lineColorList.put(1100f, new Color(0,  0, 60,  255));

				for (float val = minVal; val <= maxVal; val+=valInterval){
					if (val == valBaseVal){ //1010hPa为基准值
						attr.lineThickList.put(val, basethick + 1.5f);
					} else if (val < valBaseVal){
						attr.lineThickList.put(val, basethick);
					}
				}
				break;
			case STAT_M_HGT: //位势什米
				attr.isHasSetLevels = false;
				attr.lineWidth = 1.0f;
				if (Integer.parseInt(press) == 10){
					valInterval = 8;
					valBaseVal = 1600;
					minVal = 1440;
					maxVal = 1756;
					thickVal = 1600;
					thickInterval = 32;
				} else if (Integer.parseInt(press) == 150){
					valInterval = 8;
					valBaseVal = 1422;
					minVal = 1222;
					maxVal = 1538;
					thickVal = 1422;
					thickInterval = 32;
				}else if (Integer.parseInt(press) == 200){
					valInterval = 8;
					valBaseVal = 1190;
					minVal = 1000;
					maxVal = 1316;
					thickVal = 1190;
					thickInterval = 32;
				} else if (Integer.parseInt(press) == 25){
					valInterval = 8;
					valBaseVal = 1038;
					minVal = 918;
					maxVal = 1234;
					thickVal = 1038;
					thickInterval = 32;
				} else if (Integer.parseInt(press) == 30){
					valInterval = 8;
					valBaseVal = 968;
					minVal = 800;
					maxVal = 1036;
					thickVal = 968;
					thickInterval = 32;
				} else if (Integer.parseInt(press) == 40){
					valInterval = 8;
					valBaseVal = 734;
					minVal = 614;
					maxVal = 812;
					thickVal = 734;
					thickInterval = 32;
				} else if (Integer.parseInt(press) == 500){
					valInterval = 4;
					valBaseVal = 500;
					minVal = 480;
					maxVal = 600;
					thickVal = 588;
					thickInterval = 12;
				} else if (Integer.parseInt(press) == 70){
					valInterval = 2;
					valBaseVal = 300;
					minVal = 240;
					maxVal = 338;
					thickVal = 300;
					thickInterval = 12;
				} else if (Integer.parseInt(press) == 850){
					valInterval = 2;
					valBaseVal = 148;
					minVal = 100;
					maxVal = 198;
					thickVal = 148;
					thickInterval = 12;
				} else if (Integer.parseInt(press) == 925){
					valInterval = 4;
					valBaseVal = 80;
					minVal = 20;
					maxVal = 118;
					thickVal = 80;
					thickInterval = 12;
				} else if (Integer.parseInt(press) == 100){
					valInterval = 4;
					valBaseVal = 10;
					minVal = -20;
					maxVal = 100;
					thickVal = 10;
					thickInterval = 12;
				}
				else{ //这种配置应该不读取!
					valInterval = 8;
					valBaseVal = 1600;
					minVal = 1440;
					maxVal = 1756;
					thickVal = 1600;
					thickInterval = 32;
				}
				attr.setLabelCenterType(CenterType.HighLow);
				attr.setLabelCenter(true);
				attr.setLineThick(true);
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setThickVal(thickVal, thickInterval);
				color = new Color(25, 25, 55);
				break;
			case HH:
				attr.isHasSetLevels = false;
				attr.lineWidth = 1.0f;

				float middleVal;
				if (press.startsWith("P")){
					press = press.substring(1);
				}
				if (Integer.parseInt(press) == 100){
					valInterval = 80;
					valBaseVal = 16000;
					minVal = 14400;
					maxVal = 17560;
					thickVal = 16000;
					thickInterval = 320;
					middleVal = 16000;
				} else if (Integer.parseInt(press) == 150){
					valInterval = 80;
					valBaseVal = 14220;
					minVal = 12220;
					maxVal = 15380;
					thickVal = 14220;
					thickInterval = 320;
					middleVal = 14000;
				}else if (Integer.parseInt(press) == 200){
					valInterval = 80;
					valBaseVal = 12000;
					minVal = 10000;
					maxVal = 13200;
					thickVal = 12000;
					thickInterval = 320;
					middleVal = 12000;
				} else if (Integer.parseInt(press) == 250){
					valInterval = 80;
					valBaseVal = 10380;
					minVal = 9180;
					maxVal = 12340;
					thickVal = 10380;
					thickInterval = 320;
					middleVal = 10000;
				} else if (Integer.parseInt(press) == 300){
					valInterval = 80;
					valBaseVal = 9680;
					minVal = 8000;
					maxVal = 10360;
					thickVal = 9680;
					thickInterval = 320;
					middleVal = 9040;

				} else if (Integer.parseInt(press) == 350){
					valInterval = 80;
					valBaseVal = 9680;
					minVal = 7040;
					maxVal = 10360;
					thickVal = 9680;
					thickInterval = 320;
					middleVal = 8000;
				}
				else if (Integer.parseInt(press) == 400){
					valInterval = 80;
					valBaseVal = 7340;
					minVal = 6140;
					maxVal = 8120;
					thickVal = 7340;
					thickInterval = 320;
					middleVal = 7040;
				} else if (Integer.parseInt(press) == 500){
					valInterval = 40;
					valBaseVal = 5880;
					minVal = 4800;
					maxVal = 6080;
					thickVal = 5880;
					thickInterval = 120;
					middleVal = 5520;
				} else if (Integer.parseInt(press) == 700){
					valInterval = 20;
					valBaseVal = 3000;
					minVal = 2400;
					maxVal = 3380;
					thickVal = 3000;
					thickInterval = 120;
					middleVal = 3000;
				} else if (Integer.parseInt(press) == 850){
					valInterval = 20;
					valBaseVal = 1480;
					minVal = 1000;
					maxVal = 1980;
					thickVal = 1480;
					thickInterval = 120;
					middleVal = 1500;
				} else if (Integer.parseInt(press) == 925){
					valInterval = 40;
					valBaseVal = 800;
					minVal = 200;
					maxVal = 1180;
					thickVal = 800;
					thickInterval = 120;
					middleVal = 800;
				} else if (Integer.parseInt(press) == 1000){
					valInterval = 40;
					valBaseVal = 100;
					minVal = -200;
					maxVal = 1000;
					thickVal = 100;
					thickInterval = 120;
					middleVal = 80;
				}
				else{ //这种配置应该不读取!
					valInterval = 80;
					valBaseVal = 16000;
					minVal = 14400;
					maxVal = 17560;
					thickVal = 16000;
					thickInterval = 320;
					middleVal = minVal + (maxVal - minVal)/2;
				}
				attr.setLabelCenterType(CenterType.HighLow);
				attr.setLabelCenter(true);
				attr.setLineThick(true);
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setSmoothLine(true);
				attr.setSmoothLevel(3.0f);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setThickVal(thickVal, thickInterval);
				color = new Color(25, 25, 55);

				//APP端用的线颜色及线宽
				//高压用红色,低压用蓝色
				attr.lineColorList = new HashMap<>();
				attr.lineThickList = new HashMap<>();
				basethick = 1.5f;

				Color lowHHColor = new Color(100, 185, 245, 255);
				Color highHHColor = new Color(175, 175, 255, 255);
				for (float val = minVal; val <= maxVal; val+=valInterval){

					if (val <= middleVal) //低压浅蓝到橙色
					{
						int rr = (int)(lowHHColor.getRed() + (255 - lowHHColor.getRed()) * (Math.abs(middleVal - val)*1.2 / (middleVal - minVal)) * 1.0);
						if (rr < 90) rr = 90;
						if (rr > 255) rr = 255;
						int gg = (int)(lowHHColor.getGreen() -  (255 - lowHHColor.getGreen())* (Math.abs(middleVal - val)*0.7 / (middleVal - minVal)) * 1.0);
						if (gg < 5)	gg = 5;
						if (gg > 255)	gg = 255;
						int bb = (int)(lowHHColor.getBlue() - lowHHColor.getBlue()* (Math.abs(middleVal - val)*0.8 / (middleVal - minVal)) * 1.0);
						if (bb < 5)	bb = 5;
						if (bb > 255) bb = 255;
						attr.lineColorList.put(val, new Color(rr, gg, bb, 255));
					} else if (val > middleVal){ //高压浅蓝到深蓝
						int rr = (int)(highHHColor.getRed() * (1.0 - Math.abs(middleVal - val)*0.8 / (middleVal - minVal)) * 1.0);
						if (rr < 5)	rr = 5;
						if (rr > 255)	rr = 255;
						int gg = (int)(highHHColor.getGreen() * (1.0 - Math.abs(middleVal - val)*0.8 / (middleVal - minVal)) * 1.0);
						if (gg < 5)	gg = 5;
						if (gg > 255)	gg = 255;
						int bb = (int)(highHHColor.getBlue() * (1.0 - Math.abs(middleVal - val)*0.5 / (middleVal - minVal)) * 1.0);
						if (bb < 90) bb = 90;
						if (bb > 255) bb = 255;
						attr.lineColorList.put(val, new Color(rr, gg, bb, 255));
					}
					if (val == thickVal){
						attr.lineThickList.put(val, basethick + 1.5f);
					} else {
						attr.lineThickList.put(val, basethick);
					}
				}
			//	System.out.println("middleVal=" + middleVal);
				break;
			case STAT_M_AT:
			case STAT_M_MAX_AT:
			case STAT_M_MIN_AT:
			case TT:
			case T0:
			case AT:
			case TS:
				valInterval = 4f;
				valBaseVal = 0;
				minVal = -60;
				maxVal = 48;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLabelCenter(true);
				attr.setLineThick(false);

				//短时增加
				attr.setFilterMinDiameter(2.0f);
				attr.setFilterOpenLine(true);
				attr.setSmoothLine(true);
				attr.setSmoothLevel(6.0f);

				attr.setLabelCenterType(CenterType.WarmCool);
				attr.setThickVal(thickVal, thickInterval);
				if (press != null) 	{
					String tmpPress = press.toString();

					if (tmpPress.startsWith("P")){
						tmpPress = tmpPress.substring(1);
					}

					if (Integer.parseInt(tmpPress) == 850)
					{
						attr.isFill = false;
						attr.fillAry = new ArrayList<FillAttr>(10);
						attr.fillAry.add(new IsolinesAttributes.FillAttr(-50, -40f,    new Color(5,45,180),  0.8f, ShapeStyleUtil.FillStyle.SOLID));
						attr.fillAry.add(new IsolinesAttributes.FillAttr(-40f, -30f,   new Color(5,125,200), 0.7f, ShapeStyleUtil.FillStyle.SOLID));
						attr.fillAry.add(new IsolinesAttributes.FillAttr(-30f, -20f,   new Color(5,155,220), 0.6f, ShapeStyleUtil.FillStyle.SOLID));
						attr.fillAry.add(new IsolinesAttributes.FillAttr(-20f, -10f,   new Color(5,185,240), 0.5f, ShapeStyleUtil.FillStyle.SOLID));
					}
				}
				//APP端用的线颜色及线宽
				//气温从低到高,从深蓝到绿 青, 到黄 橙, 到 红 紫
				attr.lineColorList = new HashMap<>();
				attr.lineThickList = new HashMap<>();
				basethick = 1.5f;
				attr.lineColorList.put(-60.0f,  new Color(5, 45,  160, 255));
				attr.lineColorList.put(-56.0f, new Color(5,  65,  180, 255));
				attr.lineColorList.put(-52.0f, new Color(5,  125, 200, 255));
				attr.lineColorList.put(-48.0f, new Color(5,  165, 220, 255));
				attr.lineColorList.put(-44.0f, new Color(0,  200, 210, 255));
				attr.lineColorList.put(-40.0f, new Color(0,  185, 180, 255));
				attr.lineColorList.put(-36.0f, new Color(0,  195, 150, 255));
				attr.lineColorList.put(-32.0f, new Color(10, 185, 100, 255));
				attr.lineColorList.put(-28.0f, new Color(10, 195, 60,  255));
				attr.lineColorList.put(-24.0f, new Color(10, 235,  10, 255));
				attr.lineColorList.put(-20.0f, new Color(100, 245, 10, 255));
				attr.lineColorList.put(-16.0f, new Color(140, 225, 20, 255));
				attr.lineColorList.put(-12.0f, new Color(180, 235, 10, 255));
				attr.lineColorList.put(-8.0f,  new Color(210, 250, 10, 255));
				attr.lineColorList.put(-4.0f,  new Color(255, 255,  0, 255));
				attr.lineColorList.put(0.0f,  new Color(210,  210,  0, 255));
				attr.lineColorList.put(4.0f,  new Color(240,  185,  0, 255));
				attr.lineColorList.put(8.0f,  new Color(240,  135,  0, 255));
				attr.lineColorList.put(12.0f,  new Color(230, 105,  0, 255));
				attr.lineColorList.put(16.0f,  new Color(230, 65,  0, 255));
				attr.lineColorList.put(20.0f,  new Color(230, 45,  40, 255));
				attr.lineColorList.put(24.0f,  new Color(210, 35,  30, 255));
				attr.lineColorList.put(28.0f,  new Color(210,  5,  30, 255));
				attr.lineColorList.put(32.0f,  new Color(200,  0,  90, 255));
				attr.lineColorList.put(36.0f,  new Color(200,  0,  150, 255));
				attr.lineColorList.put(40.0f,  new Color(170,  0,  150, 255));
				attr.lineColorList.put(44.0f,  new Color(140,  0,  140, 255));
				attr.lineColorList.put(48.0f,  new Color(100,  0,  140, 255));

				for (float val = minVal; val<=maxVal; val+=valInterval){
					if (val == 0 || val == -20 || val == -40 || val == -60 || val == 32 || val == 36){
						attr.lineThickList.put(val, basethick + 1.5f);
					} else {
						attr.lineThickList.put(val, basethick);
					}
				}
			color = new Color(225, 85, 25);
				break;
			case STAT_M_TD: //平均露点温度
			case TD: //露点温度
			case D2: //2米露点温度
				valInterval = 4f;
				valBaseVal = 0;
				minVal = -80;
				maxVal = 50;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLabelCenter(true);
				attr.setLineThick(false);
				attr.setLabelCenterType(CenterType.WarmCool);
				attr.setThickVal(thickVal, thickInterval);
				color = new Color(55, 25, 195);
				break;

			case Q2:
			case RH://相对湿度
			case STAT_M_RH:

				valInterval = 10;
				valBaseVal = 50;
				minVal = 50;
				maxVal = 100;
				color = new Color(25, 255, 25);
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				levels = new float[]{60, 70, 80, 90, 95};
				attr.setLevels(levels);
				attr.isHasSetLevels = true;
				//APP端用的线颜色及线宽
				//湿度从低到高,从浅绿到深绿
				attr.lineColorList = new HashMap<>();
				attr.lineThickList = new HashMap<>();
				basethick = 1.5f;
				attr.lineColorList.put(60.0f,  new Color(125, 255, 120, 255));
				attr.lineColorList.put(70.0f, new Color(65, 245, 60, 255));
				attr.lineColorList.put(80.0f, new Color(25, 215, 20, 255));
				attr.lineColorList.put(90.0f, new Color(0,  175, 0, 255));
				attr.lineColorList.put(95.0f, new Color(0,  135,  0, 255));
				attr.lineThickList.put(60.0f, basethick);
				attr.lineThickList.put(70.0f, basethick);
				attr.lineThickList.put(80.0f, basethick);
				attr.lineThickList.put(90.0f, basethick+1.0f);
				attr.lineThickList.put(95.0f, basethick);

				attr.isFill = true;
				attr.fillAry = new ArrayList<>(2);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(60, 80f,  new Color(105,245,110), 0.6f, ShapeStyleUtil.FillStyle.POINT));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(80f,100f, new Color(20,205,20),   0.7f, ShapeStyleUtil.FillStyle.POINT));
				break;
			case LP://地面气压
			case PS://地面气压
				valInterval = 40;
				valBaseVal = 1000;
				minVal = 480;
				maxVal = 1160;
				color = new Color(25, 25, 25);
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setSmoothLine(true);
				attr.setSmoothLevel(6.0f);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				break;
			case STAT_M_NH:
			case STAT_M_N:
				valInterval = 1f;
				valBaseVal = 0.0f;
				minVal = 0f;
				maxVal = 10f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				color = new Color(25, 25, 125);
				attr.isFill = true;
				attr.isLabelCenter = false;
				attr.fillAry = new ArrayList<FillAttr>(2);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(6f, 8f, new Color(100,205,100),0.5f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(8f, 9f, new Color(150,235,50),  0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(9f, 10f, new Color(200,225,50),  0.7f, ShapeStyleUtil.FillStyle.SOLID));
				break;
			case LCC: //低云量
			case TF://总云量
				valInterval = 0.2f;
				valBaseVal = 0.2f;
				minVal = 0.2f;
				maxVal = 1.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				color = new Color(25, 25, 125);
				attr.isFill = true;
				attr.isLabelCenter = false;
				attr.fillAry = new ArrayList<FillAttr>(2);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.6f, 0.8f, new Color(100,205,100),0.5f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.8f, 1.0f, new Color(250,165,50),  0.7f, ShapeStyleUtil.FillStyle.SOLID));
				break;
			case VIS: //能见度
				valInterval = 5000f;
				valBaseVal = 0f;
				minVal = 5.0f;
				maxVal = 100000.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				color = new Color(25, 25, 125);
				attr.isFill = true;
				attr.isLabelCenter = false;
				attr.fillAry = new ArrayList<FillAttr>(2);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0f, 5000f, new Color(250,55,20),0.4f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(5000f, 10000f, new Color(250,205,20), 0.5f, ShapeStyleUtil.FillStyle.SOLID));
				break;

			//短时 (WRF)
			case SI: //沙氏指数
				valInterval = 2.0f;
				valBaseVal = 0.0f;
				minVal = -15;
				maxVal = 20.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLineStyle(LineStyle.DOT);
				color = new Color(245, 25, 35);
				attr.isFill = false;
				attr.isLabelCenter = false;
				break;
			case KI: //K指数
				valInterval = 5.0f;
				valBaseVal = 0.0f;
				minVal = 20;
				maxVal = 200.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setLineStyle(LineStyle.DASHDOT);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				color = new Color(245, 25, 35);
				attr.isFill = false;
				attr.isLabelCenter = false;
				break;
			case SW: //威胁指数
				valInterval = 20.0f;
				valBaseVal = 0.0f;
				minVal = 280;
				maxVal = 500.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				color = new Color(255, 25, 15);
				attr.setLineStyle(LineStyle.DASHDOUBLEDOT);
				attr.isFill = false;
				attr.isLabelCenter = false;
				break;
			case RI: //理查逊数
				valInterval = 2.0f;
				valBaseVal = 0.0f;
				minVal = -20;
				maxVal = 4.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLineStyle(LineStyle.DASH);
				color = new Color(25, 25, 165);
				attr.isFill = false;
				attr.isLabelCenter = false;
				break;
			case VWINDSHEARWS: //风切变
				valInterval = 1.0f;
				valBaseVal = 0.0f;
				minVal = 1;
				maxVal = 10.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setLineStyle(LineStyle.DASH);
				attr.setValInterval(valInterval);
				color = new Color(185, 185, 25);
				attr.isFill = true;
				attr.isLabelCenter = false;
				attr.fillAry = new ArrayList<FillAttr>(2);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1f, 2f, new Color(100,205,100),0.5f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(3f, 4f, new Color(210,245,50), 0.7f, ShapeStyleUtil.FillStyle.SOLID));
				break;
			case TE: //地面假相当位温
			case TB: //假相当位温
				valInterval = 20.0f;
				valBaseVal = 300.0f;
				minVal = 200;
				maxVal = 500.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLineStyle(LineStyle.DASH);
				color = new Color(245, 25, 35);
				attr.isFill = false;
				attr.isLabelCenter = false;
				if (press != null && !"PSurf".equals(press) && (Integer.parseInt(press) == 500)){
					color = new Color(25, 235, 45);
				}
				break;
			case SH:
			case CAPE: //对流有效位能
				valInterval = 200.0f;
				valBaseVal = 0.0f;
				minVal = 0;
				maxVal = 6000.0f;
				attr.setMinVal(minVal);
				attr.setLineStyle(LineStyle.DASH);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				color = new Color(245, 25, 35);
				attr.isFill = false;
				attr.isLabelCenter = false;
				break;
			case RF: //水汽通量
				valInterval = 20.0f;
				valBaseVal = 0.0f;
				minVal = 0;
				maxVal = 320.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				color = new Color(25, 225, 155);
				attr.isFill = false;
				attr.isLabelCenter = false;
				break;
			case RA: //水汽通量散度
				valInterval = 6f;
				valBaseVal = 0.0f;
				minVal = -48;
				maxVal = 48;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setLineStyle(LineStyle.DASH);
				attr.setValInterval(valInterval);
				color = new Color(25, 225, 155);
				attr.isFill = false;
				attr.isLabelCenter = false;
				break;
			case TC: //温度平流
				valInterval = 10.0f;
				valBaseVal = 300.0f;
				minVal = -100;
				maxVal = 100.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				color = new Color(245, 25, 65);
				attr.isFill = false;
				attr.isLabelCenter = false;
				break;
			case VB: //涡度平流
				valInterval = 100.0f;
				valBaseVal = 0.0f;
				minVal = -2000;
				maxVal = 1800.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				color = new Color(165, 45, 115);
				attr.isFill = false;
				attr.isLabelCenter = false;
				break;
			case HZ: //Z螺旋度
				valInterval = 2.0f;
				valBaseVal = 0.0f;
				minVal = -20;
				maxVal = 20.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				color = new Color(25, 25, 165);
				attr.isFill = false;
				attr.isLabelCenter = false;
				break;
			case DTB03: //3小时变假相当位温
				valInterval = 10.0f;
				valBaseVal = 10.0f;
				minVal = 0;
				maxVal = 100.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				color = new Color(25, 25, 165);
				attr.isFill = false;
				attr.isLabelCenter = false;
				break;
			case DTD03: //3小时变露点温度
				valInterval = 2.0f;
				valBaseVal = 0.0f;
				minVal = -50;
				maxVal = 50.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				color = new Color(25, 25, 165);
				attr.isFill = false;
				attr.isLabelCenter = false;
				break;
			case CONVEI: //位势稳定度
				valInterval = 2.0f;
				valBaseVal = 0.0f;
				minVal = -100;
				maxVal = 100.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				color = new Color(25, 25, 165);
				attr.isFill = false;
				attr.isLabelCenter = false;
				break;
			//KJ解释预报
			case DB:  //颠簸
				valInterval = 1.0f;
				valBaseVal = 0.0f;
				minVal = 0;
				maxVal = 5.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				color = new Color(165, 25, 165);
				attr.isFill = true;
				attr.isLabelCenter = false;
				attr.fillAry = new ArrayList<FillAttr>(3);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1f, 2f, new Color(240,240,10), 0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(2f, 3f, new Color(240,140,190),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(3f, 5f, new Color(235,55, 230),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				break;
			case JB:  //积冰
				valInterval = 1.0f;
				valBaseVal = 0.0f;
				minVal = 0;
				maxVal = 5.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				color = new Color(25, 25, 195);
				attr.isFill = true;
				attr.isLabelCenter = false;
				attr.fillAry = new ArrayList<FillAttr>(3);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1f, 2f, new Color(120,120,210),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(2f, 3f, new Color(220,120,100),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(3f, 5f, new Color(235,255, 20),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				break;
			case JL:  //急流
				valInterval = 0.2f;
				valBaseVal = 0.0f;
				minVal = 0.2f;
				maxVal = 1.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				color = new Color(225, 135, 15);
				attr.isFill = true;
				attr.isLabelCenter = false;
				attr.fillAry = new ArrayList<FillAttr>(3);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.7f, 0.8f, new Color(120,120,210),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.8f, 0.9f, new Color(220,120,100),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.9f, 1.0f, new Color(235,255, 20),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				break;
			case LB:  //雷暴
				valInterval = 0.2f;
				valBaseVal = 0.4f;
				minVal = 0.4f;
				maxVal = 1.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				color = new Color(225, 65, 105);
				attr.isFill = true;
				attr.isLabelCenter = false;
				attr.fillAry = new ArrayList<FillAttr>(3);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.6f, 0.8f, new Color(240,90, 140),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.8f, 1.0f, new Color(220,10,  50),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				break;
			case WQ: //雾区
				valInterval = 0.2f;
				valBaseVal = 0.2f;
				minVal = 0.4f;
				maxVal = 1.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				color = new Color(225, 215, 15);
				attr.isFill = true;
				attr.isLabelCenter = false;
				attr.fillAry = new ArrayList<FillAttr>(3);
				//	attr.fillAry.add(new IsolinesAttributes.FillAttr(0.6f, 0.8f, new Color(220,120,100),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.8f, 1.0f, new Color(235,215, 20),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				break;
			case YQ: //云区
				valInterval = 0.2f;
				valBaseVal = 0.4f;
				minVal = 0.4f;
				maxVal = 1.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				color = new Color(25, 215, 15);
				attr.isFill = true;
				attr.isLabelCenter = false;
				attr.fillAry = new ArrayList<FillAttr>(3);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.6f, 0.8f, new Color(240,220,100),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.8f, 1.0f, new Color(245,155, 20),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				break;
			case BW: //不稳定区
				valInterval = 0.2f;
				valBaseVal = 0.2f;
				minVal = 0.2f;
				maxVal = 1.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				color = new Color(225, 55, 195);
				attr.isFill = true;
				attr.isLabelCenter = false;
				attr.fillAry = new ArrayList<FillAttr>(3);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.6f, 0.8f, new Color(120,120,210),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.8f, 1.0f, new Color(225,105, 20),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				break;
			//预研演示
			case ST:
				valInterval = 1f;
				thickInterval = 8f;
				valBaseVal = 0;
				minVal = -80;
				maxVal = 50;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLabelCenter(false);
				attr.setLineThick(true);
				attr.isFill = true;
				attr.fillAry.add(new IsolinesAttributes.FillAttr(6, 8,  new Color(255,85,0),0.8f,    ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(8,10,  new Color(255,125,0),0.8f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(10,12,  new Color(255,165,0),0.8f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(12,14,  new Color(255,210,0),0.7f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(14,16,  new Color(250,255,0),0.7f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(16,18,  new Color(235,255,0),0.7f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(18,20, new Color(215,255,0),0.7f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(20,22,  new Color(195,255,0),0.7f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(22, 24,    new Color(175,255,0),0.6f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(24, 26,    new Color(150,255,0), 0.6f,  ShapeStyleUtil.FillStyle.SOLID));
				attr.setThickVal(thickVal, thickInterval);
				color = new Color(225, 85, 25);
				break;
			case T2:
				valInterval = 1f;
				valBaseVal = 0;
				minVal = -80;
				maxVal = 50;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLabelCenter(true);
				attr.setLineThick(false);
				attr.setLabelCenterType(CenterType.WarmCool);
				attr.setThickVal(thickVal, thickInterval);
				attr.isFill = false;
				attr.fillAry = new ArrayList<FillAttr>(10);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0, 4f,    new Color(5,45,180),  0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(4f, 8f,   new Color(5,125,200),  0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(8f, 12f,  new Color(5,155,220),0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(12f,16f,  new Color(5,185,240),0.5f, ShapeStyleUtil.FillStyle.SOLID));
				color = new Color(225, 85, 225);
				break;
			case HY_AIRT: //海表气温
			case HY_TEMP:
				valInterval = 1f;
				valBaseVal = 0;
				minVal = -2;
				maxVal = 40;
				thickInterval = 5;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLabelCenter(false);
				attr.setLineThick(true);
				attr.setLabelCenterType(CenterType.WarmCool);
				attr.setThickVal(thickVal, thickInterval);
				attr.setLineWidth(1.0f);
				attr.isFill = true;
				attr.fillAry = new ArrayList<FillAttr>(25);

				if (VersionManager.isHJAtlasBrowser() || VersionManager.isHJAtlasModify()
						|| VersionManager.isHJDX()|| VersionManager.isHJSZHY2()||true
						){
                   /* //HJ定型标准颜色
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(-1, 0f,  new Color(7,30,120),  1.0f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(0, 1f,   new Color(17,49,139),  1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(1, 2f,   new Color(27,68,159),  1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(2, 3f,   new Color(38,87,179),  1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(3, 4f,   new Color(48,106,199),  1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(4, 5f,   new Color(59,126,219),  1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(5f,6f,   new Color(78,138,221), 1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(6f,7f,   new Color(97,150,224),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(7f,8f,   new Color(116,163,226),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(8f,9f,   new Color(135,175,229),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(9f,10f,  new Color(154,196,220),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(10f,11f,  new Color(153,205,208),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(11f,12f,  new Color(152,214,196),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(12f,13f,  new Color(151,232,173),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(13f,14f,  new Color(215,222,126),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(14f,15f,  new Color(234,219,112),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(15f,16f,  new Color(244,217,99),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(16f,17f,  new Color(250,204,79),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(17f,18f,  new Color(247,180,45),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(18f,19f,  new Color(242,155,0),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(19f,20f,  new Color(241,147,3),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(20f,21f,  new Color(240,132,10),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(21f,22f,  new Color(239,117,17),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(22f,23f,  new Color(238,102,24),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(23f,24f,  new Color(238,88,31),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(24f,25f,  new Color(231,75,26),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(25f,26f,  new Color(224,63,22),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(26f,27f,  new Color(217,51,18),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(27f,28f,  new Color(208, 36,14),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(28f,29f,  new Color(194, 0, 3),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(29f,30f,  new Color(181, 1, 9),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(30f,31f,  new Color(169, 2, 16),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(31f,32f,  new Color(138, 5, 25),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(32f,33f,  new Color(111, 0, 21),1f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(33f,40f,  new Color(80,  0, 15),1f, ShapeStyleUtil.FillStyle.SOLID));*/
					//国家海洋预报中心标准
					attr.fillAry.add(new IsolinesAttributes.FillAttr(-1, 0f,  new Color(0,116,255),  1.0f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(0, 1f,   new Color(0,158,255),  1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(1, 2f,   new Color(0,201,255),  1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(2, 3f,   new Color(1,224,240),  1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(3, 4f,   new Color(1,247,226),  1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(4, 5f,   new Color(2,235,189),  1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(5f,6f,   new Color(3,223,153), 1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(6f,7f,   new Color(4,210,113),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(7f,8f,   new Color(6,197,73),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(8f,9f,   new Color(18,191,43),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(9f,10f,  new Color(30,186,14),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(10f,11f,  new Color(70,198,11),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(11f,12f,  new Color(110,210,8),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(12f,13f,  new Color(146,221,6),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(13f,14f,  new Color(182,233,4),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(14f,15f,  new Color(218,242,2),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(15f,16f,  new Color(255,252,0),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(16f,17f,  new Color(255,237,0),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(17f,18f,  new Color(255,222,0),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(18f,19f,  new Color(255,207,0),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(19f,20f,  new Color(255,192,0),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(20f,21f,  new Color(255,179,0),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(21f,22f,  new Color(255,165,0),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(22f,23f,  new Color(255,138,0),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(23f,24f,  new Color(255,110,0),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(24f,25f,  new Color(255,83,0),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(25f,26f,  new Color(255,55,0),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(26f,27f,  new Color(255,27,0),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(27f,28f,  new Color(255,0,0),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(28f,29f,  new Color(229, 0, 20),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(29f,30f,  new Color(204,0,41),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(30f,31f,  new Color(171, 0, 64),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(31f,32f,  new Color(148,0,87),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(32f,33f,  new Color(118, 0, 105),1f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(33f,40f,  new Color(92,0,132),1f, ShapeStyleUtil.FillStyle.SOLID));
				} else {
					//浅色标准
					attr.fillAry.add(new IsolinesAttributes.FillAttr(-4, 0f,  new Color(5,15,180),  0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(0, 4f,   new Color(5,25,200),  0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(4, 6f,   new Color(5,25,200),  0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(6, 8f,   new Color(5,25,200),  0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(8, 10f,  new Color(5,35,220),  0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(10, 12f,  new Color(5,45,220),  0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(12f,14f,  new Color(5,105,220), 0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(14f,16f,  new Color(5,145,230),0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(16f,18f,  new Color(5,185,240),0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(18f,20f,  new Color(25,235,210),0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(20f,22f,  new Color(115,235,120),0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(22f,24f,  new Color(165,235,100),0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(24f,26f,  new Color(205,235,90),0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(26f,28f,  new Color(225,205,90),0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(28f,30f,  new Color(245,215,90),0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(30f,32f,  new Color(245,235,130),0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(32f,34f,  new Color(245,185,140),0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(34f,40f,  new Color(245,155,200),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				}

				color = new Color(65, 65, 65);
				break;
			case HY_SALT:
				String temp=press.substring(1);
				int it=Integer.parseInt(temp);
				if(it<=50){
					valInterval = 0.5f;
					valBaseVal = 0;
					minVal = 15;
					maxVal = 45;
					attr.setMinVal(minVal);
					attr.setMaxVal(maxVal);
					attr.setValBaseVal(valBaseVal);
					attr.setValInterval(valInterval);
					attr.setLabelCenter(false);
					attr.setLineThick(false);
					attr.setLineWidth(1.0f);
					attr.setLabelCenterType(CenterType.WarmCool);
					attr.setThickVal(thickVal, thickInterval);
					attr.isFill = true;
					attr.fillAry = new ArrayList<FillAttr>(15);

					attr.fillAry.add(new IsolinesAttributes.FillAttr(0, 20f,  new Color(0,100,100),  0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(20, 28f,  new Color(0,125,125),  0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(28f,28.5f,  new Color(0,145,145), 0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(28.5f,29f,  new Color(0,165,165),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(29f,29.5f,  new Color(0,185,185),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(29.5f,30f,  new Color(0,205,205),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(30f,30.5f,  new Color(0,225,225),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(30.5f,31f,  new Color(0,225,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(31f,31.5f,  new Color(0,215,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(31.5f,32f,  new Color(0,200,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(32f,32.5f,  new Color(0,190,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(32.5f,33f,  new Color(0,180,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(33f,33.5f,  new Color(0,160,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(33.5f,34f,  new Color(0,140,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(34f,34.5f,  new Color(0,120,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(34.5f,35f,  new Color(0,100,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(35f,35.5f,  new Color(0,80,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(35.5f,36f,  new Color(0,60,255),0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(36f,40f,  new Color(0,40,255),0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(40f,45f,  new Color(0,20,255),0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(45f,50f,  new Color(0,0,255),0.9f, ShapeStyleUtil.FillStyle.SOLID));

					color = new Color(55, 55, 55);
				}else{
					valInterval = 0.2f;
					valBaseVal = 0;
					minVal = 15;
					maxVal = 45;
					attr.setMinVal(minVal);
					attr.setMaxVal(maxVal);
					attr.setValBaseVal(valBaseVal);
					attr.setValInterval(valInterval);
					attr.setLabelCenter(false);
					attr.setLineThick(false);
					attr.setLineWidth(1.0f);
					attr.setLabelCenterType(CenterType.WarmCool);
					attr.setThickVal(thickVal, thickInterval);
					attr.isFill = true;
					attr.fillAry = new ArrayList<FillAttr>(28);

					//国家海洋预报中心标准
                   /* attr.fillAry.add(new IsolinesAttributes.FillAttr(20f,26f,  new Color(0,255,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(26f,28f,  new Color(55,255,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(28f,30f,  new Color(105,255,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(30f,32f,  new Color(155,255,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(32f,33f,  new Color(205,255,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(33f,33.2f,  new Color(255,255,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    //attr.fillAry.add(new IsolinesAttributes.FillAttr(33.1f,33.2f,  new Color(255,242,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(33.2f,33.4f,  new Color(255,229,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    //attr.fillAry.add(new IsolinesAttributes.FillAttr(33.3f,33.4f,  new Color(255,216,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(33.4f,33.6f,  new Color(255,204,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    //attr.fillAry.add(new IsolinesAttributes.FillAttr(33.5f,33.6f,  new Color(255,191,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(33.6f,33.8f,  new Color(255,178,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    //attr.fillAry.add(new IsolinesAttributes.FillAttr(33.7f,33.8f,  new Color(255,165,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(33.8f,34f,  new Color(255,153,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    //attr.fillAry.add(new IsolinesAttributes.FillAttr(33.9f,34f,  new Color(255,140,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(34f,34.2f,  new Color(255,127,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    //attr.fillAry.add(new IsolinesAttributes.FillAttr(34.1f,34.2f,  new Color(255,114,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(34.2f,34.4f,  new Color(255,102,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    //attr.fillAry.add(new IsolinesAttributes.FillAttr(34.3f,34.4f,  new Color(255,89,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(34.4f,34.6f,  new Color(255,76,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    //attr.fillAry.add(new IsolinesAttributes.FillAttr(34.5f,34.6f,  new Color(255,63,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(34.6f,34.8f,  new Color(255,51,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    //attr.fillAry.add(new IsolinesAttributes.FillAttr(34.7f,34.8f,  new Color(255,38,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(34.8f,34.9f,  new Color(255,25,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(34.8f,35f,  new Color(255,12,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(35f,35.2f,  new Color(204,0,41),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(35.2f,35.4f,  new Color(170,0,67),0.9f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(35.4f,35.6f,  new Color(140,0,87),0.9f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(35.6f,35.8f,  new Color(120,0,100),0.9f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(35.8f,40f,  new Color(105,0,111),0.9f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(40f,45f,  new Color(92,0,132),0.9f, ShapeStyleUtil.FillStyle.SOLID));
                    attr.fillAry.add(new IsolinesAttributes.FillAttr(45f,50f,  new Color(70,0,100),0.9f, ShapeStyleUtil.FillStyle.SOLID));
 					*/

					attr.fillAry.add(new IsolinesAttributes.FillAttr(20f,26f, new Color(0,100,100),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(26f,28f, new Color(0,125,125),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(28f,30f,   new Color(0,145,145),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(30f,32f,  new Color(0,165,165),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(32f,33f,  new Color(0,185,185),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(33f,33.2f, new Color(0,205,205),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					//attr.fillAry.add(new IsolinesAttributes.FillAttr(33.1f,33.2f,  new Color(255,242,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(33.2f,33.4f, new Color(0,225,225),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					//attr.fillAry.add(new IsolinesAttributes.FillAttr(33.3f,33.4f,  new Color(255,216,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(33.4f,33.6f,  new Color(0,225,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					//attr.fillAry.add(new IsolinesAttributes.FillAttr(33.5f,33.6f,  new Color(255,191,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(33.6f,33.8f,  new Color(0,215,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					//attr.fillAry.add(new IsolinesAttributes.FillAttr(33.7f,33.8f,  new Color(255,165,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(33.8f,34f,  new Color(0,200,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					//attr.fillAry.add(new IsolinesAttributes.FillAttr(33.9f,34f,  new Color(255,140,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(34f,34.2f,  new Color(0,190,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					//attr.fillAry.add(new IsolinesAttributes.FillAttr(34.1f,34.2f,  new Color(255,114,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(34.2f,34.4f,  new Color(0,180,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					//attr.fillAry.add(new IsolinesAttributes.FillAttr(34.3f,34.4f,  new Color(255,89,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(34.4f,34.6f,  new Color(0,170,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					//attr.fillAry.add(new IsolinesAttributes.FillAttr(34.5f,34.6f,  new Color(255,63,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(34.6f,34.8f,  new Color(0,160,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					//attr.fillAry.add(new IsolinesAttributes.FillAttr(34.7f,34.8f,  new Color(255,38,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(34.8f,34.9f,  new Color(0,150,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(34.8f,35f, new Color(0,140,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(35f,35.2f,  new Color(0,120,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(35.2f,35.4f,  new Color(0,100,255),0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(35.4f,35.6f,  new Color(0,80,255),0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(35.6f,35.8f,  new Color(0,60,255),0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(35.8f,40f, new Color(0,40,255),0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(40f,45f,  new Color(0,20,255),0.9f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(45f,50f,  new Color(0,0,255),0.9f, ShapeStyleUtil.FillStyle.SOLID));

					color = new Color(55, 55, 55);
				}
				break;
			case HY_DENS: //海水密度
				String temp1=press.substring(1);
				int it1=Integer.parseInt(temp1);
				if(it1<125){
					valInterval = 0.5f;
					valBaseVal = 0;
					minVal = 15;
					maxVal = 40;
					thickInterval = 5;
					attr.setMinVal(minVal);
					attr.setMaxVal(maxVal);
					attr.setValBaseVal(valBaseVal);
					attr.setValInterval(valInterval);
					attr.setLabelCenter(false);
					attr.setLineThick(true);
					attr.setLineWidth(1.0f);
					attr.setThickVal(thickVal, thickInterval);
					attr.isFill = true;
					attr.fillAry = new ArrayList<FillAttr>(26);
					//HJ定型标准颜色
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(10, 12f,    new Color(252,253,245),  0.9f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(12, 14f,    new Color(252,253,245),  0.9f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(14, 16f,    new Color(252,253,245),  0.9f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(16, 18f,    new Color(252,253,245),  0.9f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(18, 20f,    new Color(254,250,225),  0.9f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(20, 22f,    new Color(254,237,209),  0.9f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(22, 24f,    new Color(253,208,175),  0.9f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(24f,26f,  new Color(250,189,149), 0.9f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(26f,30f,  new Color(232,174,136), 0.9f, ShapeStyleUtil.FillStyle.SOLID));

					attr.fillAry.add(new IsolinesAttributes.FillAttr(0, 20f,    new Color(0,0,80),  0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(20f,20.5f,  new Color(5,5,95), 0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(20.5f,21f,  new Color(5,25,105), 0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(21f,21.5f,  new Color(5,35,130),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(21.5f,22f,  new Color(5,35,170),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(22f,22.5f,  new Color(5,35,200),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(22.5f,23f,  new Color(5,45,240),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(23f,23.5f,  new Color(5,45,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(23.5f,24f,  new Color(5,105,240),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(24f,24.5f,  new Color(5,155,230),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(24.5f,25f,  new Color(5,215,210),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(25f,25.5f,  new Color(5,245,180),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(25.5f,26f,  new Color(15,255,110),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(26f,26.5f,  new Color(55,205,90),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(26.5f,27f,  new Color(115,195,60),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(27f,27.5f,  new Color(185,155,40),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(27.5f,28f,  new Color(215,165,20),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(28f,29f,    new Color(235,185,20),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(29f,30f,    new Color(245,115,10),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(30f,31f,    new Color(245, 65,10),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					color = new Color(55, 55, 55);
				}else if(it1<=500){
					valInterval = 0.2f;
					valBaseVal = 0;
					minVal = 15;
					maxVal = 40;
					thickInterval = 5;
					attr.setMinVal(minVal);
					attr.setMaxVal(maxVal);
					attr.setValBaseVal(valBaseVal);
					attr.setValInterval(valInterval);
					attr.setLabelCenter(false);
					attr.setLineThick(true);
					attr.setLineWidth(1.0f);
					attr.setThickVal(thickVal, thickInterval);
					attr.isFill = true;
					attr.fillAry = new ArrayList<FillAttr>();
					attr.fillAry.add(new IsolinesAttributes.FillAttr(15, 22f,    new Color(0,0,255),  0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(22f,23f,  new Color(0,15,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(23f,23.2f,  new Color(0,31,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(23.2f,23.4f,  new Color(0,63,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(23.4f,23.6f,  new Color(0,95,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(23.6f,23.8f,  new Color(0,127,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(23.8f,24f,  new Color(0,159,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(24f,24.2f,  new Color(0,191,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(24.2f,24.4f,  new Color(0,223,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(24.4f,24.6f,  new Color(0,255,255),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(24.6f,24.8f,  new Color(31,255,223),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(24.8f,25f,  new Color(63,255,191),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(25f,25.2f,  new Color(95,255,159),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(25.2f,25.4f,  new Color(127,255,127),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(25.4f,25.6f,  new Color(159,255,95),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(25.6f,25.8f,  new Color(191,255,63),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(25.8f,26f,  new Color(223,255,31),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(26f,26.2f,  new Color(255,255,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(26.2f,26.4f,  new Color(255,223,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(26.4f,26.6f,  new Color(255,207,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(26.6f,26.8f,  new Color(255,191,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(26.8f,27f,  new Color(255,159,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(27f,27.5f,  new Color(255,127,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(27.5f,28f,  new Color(255,95,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(28f,29f,    new Color(255,63,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(29f,30f,    new Color(255,31,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(30f,31f,    new Color(255,0,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));


					color = new Color(55, 55, 55);
				}else{
					valInterval = 0.1f;
					valBaseVal = 0;
					minVal = 15;
					maxVal = 40;
					thickInterval = 5;
					attr.setMinVal(minVal);
					attr.setMaxVal(maxVal);
					attr.setValBaseVal(valBaseVal);
					attr.setValInterval(valInterval);
					attr.setLabelCenter(false);
					attr.setLineThick(true);
					attr.setLineWidth(1.0f);
					attr.setThickVal(thickVal, thickInterval);
					attr.isFill = true;
					attr.fillAry = new ArrayList<FillAttr>();

					attr.fillAry.add(new IsolinesAttributes.FillAttr(0, 20f,    new Color(205,205,0),  0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(20f,26.4f,  new Color(205,255,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(26.4f,26.5f,  new Color(255,225,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(26.5f,26.6f,  new Color(255,207,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(26.6f,26.7f,  new Color(255,191,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(26.7f,26.8f,  new Color(255,179,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(26.8f,26.9f,  new Color(255,168,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(26.9f,27f,  new Color(255,159,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(27f,27.1f,  new Color(255,143,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(27.1f,27.2f,  new Color(255,127,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(27.2f,27.3f,  new Color(255,115,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(27.3f,27.4f,  new Color(255,95,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(27.4f,27.5f,  new Color(255,79,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(27.5f,28f,  new Color(255,65,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
					attr.fillAry.add(new IsolinesAttributes.FillAttr(28f,29f,    new Color(255,49,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));


					color = new Color(55, 55, 55);
				}
				break;
			case HY_SOUD: //海水声速
				valInterval = 4f;
				valBaseVal = 0;
				minVal = 1400;
				maxVal = 1800;
				thickInterval = 100;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLabelCenter(false);
				attr.setLineThick(true);
				attr.setLineWidth(1.0f);
				attr.setThickVal(thickVal, thickInterval);
				attr.isFill = true;
				attr.fillAry = new ArrayList<FillAttr>();
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0, 1400f,  new Color(0,0,0),  0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1400, 1408f,  new Color(0,0,80),  0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1408, 1416f,  new Color(5,5,95), 0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1416, 1424f,  new Color(5,25,105), 0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1424, 1432f,  new Color(5,35,130),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1432, 1440f,  new Color(5,35,170),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1440, 1444f,  new Color(5,35,200),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1444, 1448f,  new Color(5,35,219),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1448, 1452f,  new Color(5,45,240),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1452, 1456f,  new Color(0,0,255),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1456, 1460f,  new Color(0,25,255),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1460, 1464f,  new Color(0,51,255),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1464, 1468f,  new Color(0,76,255),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1468, 1472f,  new Color(0,102,255),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1472, 1476f,  new Color(0,127,255),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1476, 1480f,  new Color(0,153,255),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1480, 1484f,  new Color(0,178,255),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1484, 1488f,  new Color(0,204,255),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1488, 1492f,  new Color(0,229,255),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1492, 1496f,  new Color(0,255,255),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1496, 1500f,  new Color(0,255,229),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1400, 1504f,  new Color(0,229,204),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1504, 1508f,  new Color(0,204,178),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1508, 1512f,  new Color(0,204,153),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1512, 1516f,  new Color(0,204,127),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1516, 1520f,  new Color(0,204,102),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1520, 1524f,  new Color(0,184,76),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1524, 1528f,  new Color(0,164,51),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1528, 1532f,  new Color(0,144,25),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1532, 1536f,  new Color(0,124,0),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1536, 1540f,  new Color(25,144,0),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1540, 1544f,  new Color(51,164,0),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1544, 1548f,  new Color(76,184,0),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1548, 1552f,  new Color(102,204,0),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1552, 1556f,  new Color(127,204,0),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1556, 1560f,  new Color(153,204,0),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1560, 1564f,  new Color(178,204,0),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1564, 1568f,  new Color(204,204,0),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1568, 1572f,  new Color(229,204,0),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1572, 1576f,  new Color(255,204,0),0.9f, ShapeStyleUtil.FillStyle.SOLID));

//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1400, 1410f,  new Color(0,0,80),  0.5f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1410, 1420f,  new Color(5,5,95), 0.5f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1420, 1430f,  new Color(5,25,105), 0.5f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1430, 1440f,  new Color(5,35,130),0.55f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1440, 1450f,  new Color(5,35,170),0.55f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1450, 1460f,  new Color(5,35,200),0.6f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1460, 1470f,  new Color(5,45,240),0.65f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1470, 1480f,  new Color(5,45,255),0.65f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1480, 1490f,  new Color(5,105,240),0.7f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1490, 1500f,  new Color(5,155,230),0.75f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1500, 1510f,  new Color(5,215,210),0.75f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1510, 1520f,  new Color(5,245,180),0.8f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1520, 1530f,  new Color(15,255,110),0.8f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1530, 1540f,  new Color(55,205,90),0.8f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1540, 1550f,  new Color(115,195,60),0.8f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1550, 1560f,  new Color(185,155,40),0.8f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1560, 1570f,  new Color(215,165,20),0.8f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1570, 1580f,  new Color(235,185,20),0.8f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1580, 1590f,  new Color(245,115,10),0.8f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1590, 1600f,  new Color(245, 65,10),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				color = new Color(55, 55, 55);
				break;
			case HY_WIND: //海面风
			case HY_WIND_DIR:
			case HY_WIND_VEL:
				valInterval = 2f;
				valBaseVal = 0;
				minVal = 0;
				maxVal = 40;
				thickInterval = 5;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLabelCenter(false);
				attr.setLineThick(true);
				attr.setThickVal(thickVal, thickInterval);
				attr.isFill = true;
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0f, 4f,  new Color(5,35,200),0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(4f, 6f,  new Color(5,45,240),0.65f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(6f, 8f,  new Color(5,45,255),0.65f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(8f,10f,  new Color(5,105,240),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(10f,12f, new Color(5,155,230),0.75f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(12f,14f, new Color(5,215,210),0.75f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(14f,16f, new Color(5,245,180),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(16f,18f, new Color(15,255,110),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(18f,20f, new Color(55,205,90),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(20f,22f, new Color(115,195,60),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(22f,24f, new Color(185,155,40),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(24f,26f, new Color(215,165,20),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(26f,28f, new Color(235,185,20),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(28f,30f, new Color(245,115,10),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(30f,40f, new Color(245, 65,10),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				color = new Color(25, 185, 225);
				break;
			case HY_WAVE: 		//浪
			case HY_WAVE_DIR:
			case HY_WAVE_VEL:
			case HY_YSS: 		//涌
			case HY_YRR: 		//涌
			case HY_STREAM_DIR:
			case HY_STREAM_VEL:
			case HY_HS:
			case HY_WAVE_HEIGHT:
			case WWH:
				valInterval = 0.5f;
				valBaseVal = 0;
				minVal = 0.2f;
				maxVal = 20;
				thickInterval = 5;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLabelCenter(false);
				attr.setLineThick(true);
				attr.setLineWidth(1.0f);
				attr.setThickVal(thickVal, thickInterval);
				attr.setHasSetLevels(true);
				attr.setLevels(new float[]{0.2f, 0.5f, 1f, 1.5f, 1.5f, 2f, 2f, 2.5f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 15f, 20f});
				attr.isFill = true;
				if (elem.equals(ElemCode.WWH)){
					attr.isCutFromChinaArea = true;
					attr.setSmoothLevel(6.0f);
					attr.setSmoothLine(true);
				}
				//attr.fillAry.add(new IsolinesAttributes.FillAttr(0f,  0.25f,  new Color(5,35,200),0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr( 0.2f, 0.5f,  new Color(5,185,245),0.65f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.5f, 1f,  new Color(5,225,235),0.65f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1f,1.5f,  new Color(5,235,200),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1.5f,2f, new Color(5,235,150),0.75f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1.2f,1.5f, new Color(5,235,200),0.75f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1.5f,1.8f, new Color(5,245,180),0.8f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(1.8f,2f, new Color(15,255,110),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(2f,2.5f, new Color(55,205,90),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(2.5f,3f, new Color(95,235,40),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(3f,4f, new Color(135,245,40),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(4f,5f, new Color(185,255,20),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(5f,6f, new Color(225,245,80),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(6f,7f, new Color(245,245,110),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(7f,8f, new Color(255,215,80),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(8f,9f, new Color(255,160,50),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(9f,10f, new Color(255,105,30),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(10f,15f, new Color(255,55,120),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(15f,20f, new Color(225,55,150),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				color = new Color(25, 25, 25);
				break;
			case HY_CURR_U: //海水海流U
			case HY_CURR_V: //海水海流V
			case HY_CURR: //海水海流 cm/s
				valInterval = 10f;
				valBaseVal = 0;
				minVal = 0;
				maxVal = 100;
				thickInterval = 50;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLabelCenter(false);
				attr.setLineThick(true);
				attr.setThickVal(thickVal, thickInterval);
				attr.isFill = true;
				attr.fillAry = new ArrayList<FillAttr>(15);
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(10, 20f,  new Color(0,0,80),  0.2f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(20, 30f,  new Color(5,5,95), 0.3f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(30, 35f,  new Color(5,25,105), 0.4f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(35, 40f,  new Color(5,35,130),0.45f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(40, 45f,  new Color(5,35,170),0.45f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(45, 50f,  new Color(5,35,200),0.5f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(50, 55f,  new Color(5,45,240),0.55f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(55, 60f,  new Color(5,45,255),0.55f, ShapeStyleUtil.FillStyle.SOLID));
				//旧标准
              /*  attr.fillAry.add(new IsolinesAttributes.FillAttr(0,  10f,  new Color(5,45,240),0.5f, ShapeStyleUtil.FillStyle.SOLID));
                attr.fillAry.add(new IsolinesAttributes.FillAttr(10, 20f,  new Color(5,105,240),0.6f, ShapeStyleUtil.FillStyle.SOLID));
                attr.fillAry.add(new IsolinesAttributes.FillAttr(20, 30f,  new Color(5,155,230),0.65f, ShapeStyleUtil.FillStyle.SOLID));
                attr.fillAry.add(new IsolinesAttributes.FillAttr(30, 40f,  new Color(5,215,210),0.65f, ShapeStyleUtil.FillStyle.SOLID));
                attr.fillAry.add(new IsolinesAttributes.FillAttr(40, 50f,  new Color(5,245,180),0.7f, ShapeStyleUtil.FillStyle.SOLID));
                attr.fillAry.add(new IsolinesAttributes.FillAttr(50, 60f,  new Color(15,255,110),0.7f, ShapeStyleUtil.FillStyle.SOLID));
                attr.fillAry.add(new IsolinesAttributes.FillAttr(60, 70f,  new Color(55,205,90),0.7f, ShapeStyleUtil.FillStyle.SOLID));
                attr.fillAry.add(new IsolinesAttributes.FillAttr(70, 80f,  new Color(115,195,60),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                attr.fillAry.add(new IsolinesAttributes.FillAttr(80, 90f,  new Color(185,155,40),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                attr.fillAry.add(new IsolinesAttributes.FillAttr(90, 100f,  new Color(215,165,20),0.8f, ShapeStyleUtil.FillStyle.SOLID));
                attr.fillAry.add(new IsolinesAttributes.FillAttr(100, 110f,  new Color(235,185,20),0.9f, ShapeStyleUtil.FillStyle.SOLID));
                attr.fillAry.add(new IsolinesAttributes.FillAttr(110, 150f,  new Color(245,115,10),0.9f, ShapeStyleUtil.FillStyle.SOLID));
                attr.fillAry.add(new IsolinesAttributes.FillAttr(150, 200f,  new Color(245, 65,10),0.9f, ShapeStyleUtil.FillStyle.SOLID));*/
				//国家海洋预报中心标准
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0,  10f,  new Color(0,153,255),0.5f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(10, 20f,  new Color(0,193,255),0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(20, 30f,  new Color(2,237,197),0.65f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(30, 40f,  new Color(6,202,88),0.65f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(40, 50f,  new Color(52,192,12),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(50, 60f,  new Color(168,228,5),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(60, 70f,  new Color(255,244,0),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(70, 80f,  new Color(255,203,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(80, 90f,  new Color(255,155,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(90, 100f,  new Color(255,75,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(100, 110f,  new Color(250,0,4),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(110, 150f,  new Color(174,0,66),0.9f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(150, 200f,  new Color(92, 0,132),0.9f, ShapeStyleUtil.FillStyle.SOLID));

				color = new Color(5, 235, 155);
				break;
			case HY_SHH: //海面高度
				valInterval = 10f;
				valBaseVal = 0;
				minVal = -150;
				maxVal = 150;
				thickInterval = 50;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLabelCenter(false);
				attr.setLineThick(false);
				attr.setThickVal(thickVal, thickInterval);
				attr.setLineWidth(1.5f);
				attr.setLevels(new float[]{40, 50, 60, 70, 80,85, 90,95, 100,105, 110,140});
				attr.setHasSetLevels(true);

				attr.isFill = false;
				attr.isLabelVal = true;
				attr.fillAry.add(new IsolinesAttributes.FillAttr(80f,85f,  new Color(0,210,255), 0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(80f,90f,  new Color(0,170,255), 0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(90f,95f,  new Color(0,125,255),0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(95f,100f, new Color(0,85,255),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(100f,105f, new Color(0,65,205),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(105f,110f, new Color(0,0,255),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(110f,140f, new Color(0,5,200),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				///	attr.fillAry.add(new IsolinesAttributes.FillAttr(70f,80f,  new Color(255,85,0),0.7f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(90f,140f, new Color(255,45,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));

				color = new Color(65, 195, 65);
				break;
			case T_STRENGDEPTH: //解释应用 温度跃层强度
				valInterval = 10f;
				valBaseVal = 0;
				minVal = -1;
				maxVal = 150;
				thickInterval = 40;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);

				levels = new float[]{ 20, 40, 60, 80, 100, 140};
				attr.setLevels(levels);
				attr.isHasSetLevels = true;
				attr.setLabelCenter(false);
				attr.setLineThick(false);
				attr.setThickVal(thickVal, thickInterval);
				attr.setLineWidth(0.5f);
				attr.isFill = true;
				attr.isLabelVal = false;
				attr.fillAry.add(new IsolinesAttributes.FillAttr(20, 30f,   new Color(150,255,0), 0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(30f,40f,   new Color(215,255,0),0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(40f,50f,   new Color(255,255,0),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(50f,60f,  new Color(255,165,0),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(60f,70f,  new Color(255,125,0),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(70f,80f,  new Color(255,85,0),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(90f,140f, new Color(255,45,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));

//			attr.fillAry.add(new IsolinesAttributes.FillAttr(20, 30f,   new Color(150,255,0), 0.6f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(30f,40f,   new Color(175,255,0),0.6f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(40f,50f,   new Color(195,255,0),0.7f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(50f,60f,  new Color(215,255,0),0.7f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(60f,70f,  new Color(235,255,0),0.7f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(70f,80f,  new Color(250,255,0),0.7f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(80f,90f,  new Color(255,210,0),0.7f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(90f,100f, new Color(255,165,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(100f,110f, new Color(255,125,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(110f,120f, new Color(255,85,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(120f,130f, new Color(255,45,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(130f,140f, new Color(255,5,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				color = new Color(155, 165, 235);
				break;
			case UPDEPTH:
				valInterval = 20f;
				valBaseVal = 0;
				minVal = -200;
				maxVal = 0;
				thickInterval = 100;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.isHasSetLevels = false;
				levels = new float[]{0, 20, 40, 60, 80, 100, 120, 140, 160, 180, 200};
				attr.setLevels(levels);

				attr.setLineThick(false);
				attr.setLineWidth(1.0f);
				attr.isFill = false;
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-200, -180f, new Color(255,85,0),0.8f,    ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-180f,-160f, new Color(255,125,0),0.8f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-160f,-140f, new Color(255,165,0),0.8f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-140f,-120f, new Color(255,210,0),0.7f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-120f,-100f, new Color(250,255,0),0.7f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-100f,-80f,  new Color(235,255,0),0.7f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-80f, -60f,  new Color(215,255,0),0.7f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-60f, -40f,  new Color(195,255,0),0.7f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-40f,-20f,   new Color(175,255,0),0.6f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-20f,0f,     new Color(150,255,0), 0.6f,  ShapeStyleUtil.FillStyle.SOLID));			//	attr.fillAry.add(new IsolinesAttributes.FillAttr(120f,130f, new Color(255,45,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				//	attr.fillAry.add(new IsolinesAttributes.FillAttr(130f,140f, new Color(255,5,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));


				attr.isLabelVal = true;
				color = new Color(155, 195, 235);
				break;
			case DOWNDEPTH:
				valInterval = 200f;
				valBaseVal = -100;
				minVal = -4000;
				maxVal = -100;
				thickInterval = 500;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.isHasSetLevels = true;
				levels = new float[]{-100, -400,-800, -1200, -1600, -2000, -2400, -2800, -3200, -3600, -4000};
				attr.setLevels(levels);

				attr.setLineThick(false);
				attr.setLineWidth(1.0f);
				attr.isFill = true;
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-4000, -3600f,  new Color(255,85,0),0.8f,    ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-3600f,-3200f,  new Color(255,125,0),0.8f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-3200f,-2800f,  new Color(255,165,0),0.8f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-2800f,-2400f,  new Color(255,210,0),0.7f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-2400f,-2000f,  new Color(250,255,0),0.7f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-2000f,-1600f,  new Color(235,255,0),0.7f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-1600f, -1200f, new Color(215,255,0),0.7f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-1200f, -800f,  new Color(195,255,0),0.7f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-800f,-400f,    new Color(175,255,0),0.6f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-400f,-100f,    new Color(150,255,0), 0.6f,  ShapeStyleUtil.FillStyle.SOLID));
				//	attr.fillAry.add(new IsolinesAttributes.FillAttr(120f,130f, new Color(255,45,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				//	attr.fillAry.add(new IsolinesAttributes.FillAttr(130f,140f, new Color(255,5,0),0.8f, ShapeStyleUtil.FillStyle.SOLID));

				attr.isLabelVal = true;
				color = new Color(185, 195, 25);
				break;
			case THICKDEPTH:
				valInterval = 200f;
				valBaseVal = 0;
				minVal = 0;
				maxVal = 4000;
				thickInterval = 1000;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.isHasSetLevels = true;
				levels = new float[]{0, 400, 800, 1200, 1600, 2000, 2400, 2800, 3200, 3600, 4000};
				attr.setLevels(levels);

				attr.setLineThick(false);
				attr.setLineWidth(1.0f);
				attr.isFill = true;
				attr.isLabelVal = true;
				attr.fillAry.add(new IsolinesAttributes.FillAttr(4000, 3600f,  new Color(255,85,0),0.8f,    ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(3600f,3200f,  new Color(255,125,0),0.8f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(3200f,2800f,  new Color(255,165,0),0.8f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(2800f,2400f,  new Color(255,210,0),0.7f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(2400f,2000f,  new Color(250,255,0),0.7f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(2000f,1600f,  new Color(235,255,0),0.7f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1600f,1200f, new Color(215,255,0),0.7f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1200f,800f,  new Color(195,255,0),0.7f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(800f, 400f,    new Color(175,255,0),0.6f,   ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(400f, 0f,    new Color(150,255,0), 0.6f,  ShapeStyleUtil.FillStyle.SOLID));
				color = new Color(195, 225, 25);
				break;
			case ROU_STRENGDEPTH: //密度跃层强度
				valInterval = 2f;
				valBaseVal = 0;
				minVal = -1;
				maxVal = 150;
				thickInterval = 36;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLabelCenter(false);
				attr.setLineThick(true);
				attr.setThickVal(thickVal, thickInterval);
				color = new Color(25, 195, 165);
				break;
			case SOUND_STRENGDEPTH: //声速跃层强度
				valInterval = 10f;
				valBaseVal = 0;
				minVal = -10;
				maxVal = 150;
				thickInterval = 60;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLabelCenter(false);
				attr.setLineThick(false);
				attr.setThickVal(thickVal, thickInterval);
				color = new Color(225, 65, 165);
				break;
			case T_FRONT_STRENG: //海水温度锋面强度
				valInterval = 0.5f;
				valBaseVal = 0;
				minVal = 0;
				maxVal = 20;
				thickInterval = 5;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLabelCenter(false);
				attr.setLineThick(false);
				attr.setThickVal(thickVal, thickInterval);
				color = new Color(225, 65, 65);
				break;
			case SPEEDS: //表层流速场
				valInterval = 200f;
				valBaseVal = 0;
				minVal = 0;
				maxVal = 1500;
				thickInterval = 500;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLabelCenter(false);
				attr.setLineThick(false);
				attr.setThickVal(thickVal, thickInterval);

				attr.setLineWidth(0.3f);
				attr.isFill = true;
				attr.isLabelVal = false;
				attr.fillAry.add(new IsolinesAttributes.FillAttr(200, 400f,   new Color(150,255,0), 0.5f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(400f,600f,   new Color(255,255,0),0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(600f,800f,  new Color(245,165,0),0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(800f,1000f, new Color(255,45,0),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1000f,2000f, new Color(255,5,0),0.7f, ShapeStyleUtil.FillStyle.SOLID));

				color = new Color(5, 245, 195);
				break;
			case ESURF: //海表高度
				valInterval = 10f;
				valBaseVal = 0;
				minVal = -150;
				maxVal = 150;
				thickInterval = 60;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLabelCenter(false);
				attr.setLineThick(false);
				attr.setThickVal(thickVal, thickInterval);
				attr.isFill = false;
				attr.isLabelVal = true;
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(80f,85f,  new Color(0,210,255), 0.6f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(80f,90f,  new Color(0,170,255), 0.6f, ShapeStyleUtil.FillStyle.SOLID));
//			attr.fillAry.add(new IsolinesAttributes.FillAttr(90f,95f,  new Color(0,125,255),0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(95f,100f, new Color(0,85,255),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(100f,105f, new Color(0,65,205),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(105f,110f, new Color(0,0,255),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(110f,140f, new Color(0,5,200),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				color = new Color(65, 165, 225);
				break;
			case STAT_M_WS:
			case UU: //风速等值线
			case VV: //风速等值线
			case WS: //风速等值线
//			valInterval = 4f;
//			valBaseVal = 0;
//			minVal = 0;
//			maxVal = 40;
//			attr.setMinVal(minVal);
//			attr.setMaxVal(maxVal);
//			attr.setValBaseVal(valBaseVal);
//			attr.setValInterval(valInterval);
				attr.setLabelCenter(false);
				attr.setLineThick(false);
				attr.setThickVal(thickVal, thickInterval);
				attr.setLevels(new float[]{5, 10,  15, 20, 25,  30, 35, 40, 45, 50, 55});
				attr.setHasSetLevels(true);
				attr.setMinVal(0);
				attr.setMaxVal(400);
				//APP端用的线颜色及线宽
				//风速从低到高,从浅黄到深黄
				attr.lineColorList = new HashMap<>();
				attr.lineThickList = new HashMap<>();
				basethick = 1.5f;
				attr.lineColorList.put(5.0f,  new Color(125, 245, 100, 255));
				attr.lineColorList.put(10.0f, new Color(175, 245, 120, 255));
				attr.lineColorList.put(15.0f, new Color(235, 245, 100, 255));
				attr.lineColorList.put(20.0f, new Color(235, 205, 20, 255));
				attr.lineColorList.put(25.0f, new Color(245, 145, 0, 255));
				attr.lineColorList.put(30.0f, new Color(215, 105, 0, 255));
				attr.lineColorList.put(35.0f, new Color(240, 50,  0, 255));
				attr.lineColorList.put(40.0f, new Color(210, 10, 0, 255));
				attr.lineColorList.put(45.0f, new Color(200, 0,  100, 255));
				attr.lineColorList.put(50.0f, new Color(215, 0,  140, 255));
				attr.lineColorList.put(55.0f, new Color(185, 0, 180, 255));


				attr.lineThickList.put(5.0f, basethick);
				attr.lineThickList.put(10.0f, basethick+1.0f);
				attr.lineThickList.put(15.0f, basethick);
				attr.lineThickList.put(20.0f, basethick+1.0f);
				attr.lineThickList.put(25.0f, basethick);
				attr.lineThickList.put(30.0f, basethick+1.0f);
				attr.lineThickList.put(35.0f, basethick);
				attr.lineThickList.put(40.0f, basethick+1.0f);
				attr.lineThickList.put(45.0f, basethick);
				attr.lineThickList.put(50.0f, basethick+1.0f);
				attr.lineThickList.put(55.0f, basethick);

				attr.isFill = false;
				attr.fillAry = new ArrayList<FillAttr>(10);

				//	attr.fillAry.add(new IsolinesAttributes.FillAttr(8,   10f,  new Color(60,165,10),  0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(10,  12f,  new Color(115,185,10), 0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(12,  14f,  new Color(175,215,10), 0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(14,  16f,  new Color(235,235,10), 0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(16f, 18f,  new Color(245,175,10), 0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(18,  20f,  new Color(245,120,0),  0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(20f, 22f,  new Color(245,60,0),   0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(22f, 24f,  new Color(250,5,0),    0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(24f, 26f,  new Color(220,20,220), 0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(26f, 28f,  new Color(185,15,200), 0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(28f, 30f,  new Color(165,15,160), 0.8f, ShapeStyleUtil.FillStyle.SOLID));
				color = new Color(175, 175, 225);
				break;
			//HJ解释预报内容
			case YQQ: //云区
				valInterval = 1.0f;
				valBaseVal = 0.0f;
				minVal = 2.0f;
				maxVal = 10.0f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				color = new Color(25, 215, 15);
				attr.isFill = true;
				attr.isLabelCenter = false;
				attr.fillAry = new ArrayList<FillAttr>(4);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(6f, 7f, new Color(40,220,100),0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(7f, 8f, new Color(40,200,220),0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(8f, 9f, new Color(245,195, 20),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(9f, 10f, new Color(245,55, 20),0.8f, ShapeStyleUtil.FillStyle.SOLID));
				break;
			case JYY: //积雨云区
				levels = new float[]{1,2,3};
				attr.setLevels(levels);
				attr.isHasSetLevels = true;
				color = new Color(205, 35, 35);
				attr.isFill = false;
				attr.isFillSurfaceColor = true;
				attr.surfaceFillColorMode = SurfaceFillColorMode.YelloToRed;
				attr.fillAry = new ArrayList<FillAttr>(2);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1,  2,  new Color(225,195,10), 0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(2,  3,  new Color(245,55, 10), 0.7f, ShapeStyleUtil.FillStyle.SOLID));
				break;

			case LBQ: //雷暴
				levels = new float[]{0.8f, 0.95f};
				attr.setLevels(levels);
				attr.isHasSetLevels = true;
				color = new Color(245, 35, 35);
				attr.isFill = false;
				attr.isFillSurfaceColor = true;
				attr.surfaceFillColorMode = SurfaceFillColorMode.YelloToRed;
				attr.fillAry = new ArrayList<FillAttr>(2);
				//attr.fillAry.add(new IsolinesAttributes.FillAttr(0.00f,  0.8f,  new Color(25,185,210), 0.3f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.95f,  1.01f,  new Color(225,35,10), 0.6f, ShapeStyleUtil.FillStyle.SOLID));
				break;
			case FJB: //积冰区
				levels = new float[]{1,2,3};
				attr.setLevels(levels);
				attr.isHasSetLevels = true;
				color = new Color(25, 35, 235);
				attr.isFill = false;
				attr.isFillSurfaceColor = true;
				attr.surfaceFillColorMode = SurfaceFillColorMode.YelloToRed;
				attr.fillAry = new ArrayList<FillAttr>(2);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1,  2,  new Color(25,95,210), 0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(2,  3,  new Color(25,25,230), 0.7f, ShapeStyleUtil.FillStyle.SOLID));
				break;
			case QDB: //晴空颠簸
				levels = new float[]{1,2,3};
				attr.setLevels(levels);
				attr.isHasSetLevels = true;
				color = new Color(225, 235, 25);
				attr.isFill = false;
				attr.isFillSurfaceColor = true;
				attr.surfaceFillColorMode = SurfaceFillColorMode.YelloToRed;
				attr.fillAry = new ArrayList<FillAttr>(2);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(1,  2,  new Color(25,195,20), 0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(2,  3,  new Color(225,225,20), 0.7f, ShapeStyleUtil.FillStyle.SOLID));
				break;
			case LDB: //急流颠簸
				levels = new float[]{0.95f};
				attr.setLevels(levels);
				attr.isHasSetLevels = true;
				color = new Color(245, 25, 185);
				attr.isFill = false;
				attr.isFillSurfaceColor = true;
				attr.surfaceFillColorMode = SurfaceFillColorMode.YelloToRed;
				attr.fillAry = new ArrayList<FillAttr>(1);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.95f,  1f,  new Color(225,195,10), 0.6f, ShapeStyleUtil.FillStyle.POINT));
				break;
			case GJL: //高空急流
				attr.setLabelCenter(false);
				attr.setLineThick(false);
				attr.setThickVal(thickVal, thickInterval);
				attr.setLevels(new float[]{10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40});
				attr.setHasSetLevels(true);
				attr.isFill = true;
				attr.fillAry = new ArrayList<FillAttr>(3);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(20,  30f,  new Color(115,185,10), 0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(30,  40f,  new Color(175,215,10), 0.7f, ShapeStyleUtil.FillStyle.SOLID));
				break;
			case DLH: //对流层顶高
				valInterval = 120;
				valBaseVal = 13000;
				minVal = 13000;
				maxVal = 17200;
				thickInterval = 360;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLabelCenter(false);
				attr.setLineThick(true);
				attr.setThickVal(thickVal, thickInterval);
				color = new Color(25, 25, 225);
				break;
			case DLT: //对流层顶温
				valInterval = 2f;
				valBaseVal = 0;
				minVal = -60;
				maxVal = 20;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLabelCenter(false);
				attr.setLineThick(false);
				attr.setThickVal(thickVal, thickInterval);
				color = new Color(225, 155, 25);
				break;

			case HY_TEMPCLINE_INTENSITY: //跃层强度
				attr.setLabelCenter(false);
				attr.setLineThick(false);
				attr.setLineWidth(1.0f);
				attr.setLevels(new float[]{-0.50f, -0.30f, -0.25f, -0.20f, -0.15f, -0.10f, -0.09f, -0.08f -0.07f, -0.06f,-0.05f,-0.02f});
				attr.isFill = true;
				attr.isLabelVal = true;
				attr.setLabelFormat("6.2f");
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-1.0f, -0.5f,  new Color(185,5,185), 0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-0.5f, -0.3f,  new Color(205,25,205), 0.8f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-0.3f, -0.25f, new Color(215,65,215), 0.7f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-0.25f,-0.20f, new Color(215,105,215), 0.6f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-0.20f,-0.15f, new Color(225,165,225),  0.5f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-0.15f,-0.10f, new Color(235,195,235),  0.4f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-0.10f,-0.05f, new Color(245,215,245),  0.3f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(-0.05f,-0.00f, new Color(255,235,255),  0.0f, ShapeStyleUtil.FillStyle.SOLID));
				color = new Color(65, 65, 65);
				break;
			case HY_TEMPCLINE_UPDEPTH: //跃层上界深度
				valInterval = 20f;
				valBaseVal = 5;
				minVal = 5f;
				maxVal = 200f;
				thickInterval = 50f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLineWidth(1.0f);
				//	attr.setSmoothLevel(10);
				attr.setLabelCenter(false);
				attr.setLineThick(false);
				attr.setThickVal(thickVal, thickInterval);
				//	attr.setLevels(new float[]{-0.3f, -0.25f, -0.2f, -0.15f, -0.10f, -0.09f, -0.08f -0.07f, -0.06f,-0.05f,-0.02f});
				attr.isFill = false;
				attr.isLabelVal = true;
				color = new Color(85, 85, 85);
				break;
			case HY_TEMPCLINE_DOWNDEPTH: //跃层下界深度
				//db_x 2014-6-18添加
				valInterval = 20f;
				valBaseVal = 5;
				minVal = 50f;
				maxVal = 1500f;
				thickInterval = 50f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLineWidth(1.0f);
				//	attr.setSmoothLevel(10);
				attr.setLabelCenter(false);
				attr.setLineThick(false);
				attr.setThickVal(thickVal, thickInterval);
				//	attr.setLevels(new float[]{-0.3f, -0.25f, -0.2f, -0.15f, -0.10f, -0.09f, -0.08f -0.07f, -0.06f,-0.05f,-0.02f});
				attr.isFill = false;
				attr.isLabelVal = true;
				color = new Color(200, 50, 200);
				break;
			case HY_TEMPCLINE_THICKNESS: //跃层厚度
				valInterval = 20f;
				valBaseVal = 10;
				minVal = 5f;
				maxVal = 200f;
				thickInterval = 50f;
				attr.setMinVal(minVal);
				attr.setMaxVal(maxVal);
				attr.setSmoothLevel(8);
				attr.setValBaseVal(valBaseVal);
				attr.setValInterval(valInterval);
				attr.setLabelCenter(false);
				attr.setLineThick(false);
				attr.setLineWidth(1.5f);
				attr.setThickVal(thickVal, thickInterval);
				//	attr.setLevels(new float[]{-0.3f, -0.25f, -0.2f, -0.15f, -0.10f, -0.09f, -0.08f -0.07f, -0.06f,-0.05f,-0.02f});
				attr.isFill = false;
				attr.isLabelVal = true;
				color = new Color(55, 55, 105);
				break;
			case CB_HORIZONTAL: //积雨云（目前为民航WNI数据)
				levels = new float[]{0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f};
				attr.setLevels(levels);
				attr.isHasSetLevels = true;
				color = new Color(205, 35, 35);
				attr.isFill = true;
				attr.isFillSurfaceColor = false;
				attr.isLabelVal = true;
				attr.fillAry = new ArrayList<FillAttr>(2);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.3f,  0.4f,  new Color(245,245,10), 0.4f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.4f,  1.0f,  new Color(245,55, 10), 0.5f, ShapeStyleUtil.FillStyle.SOLID));
				break;
			case ICE_MEAN: //积冰均值（目前为民航WNI数据)
				levels = new float[]{0.3f, 0.4f, 0.5f, 0.6f};
				attr.setLevels(levels);
				attr.isHasSetLevels = true;
				color = new Color(65, 65, 235);
				attr.isFill = true;
				attr.isFillSurfaceColor = false;
				attr.isLabelVal = true;
				attr.fillAry = new ArrayList<FillAttr>(2);
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.3f,  0.4f,  new Color(245,245,10), 0.4f, ShapeStyleUtil.FillStyle.SOLID));
				attr.fillAry.add(new IsolinesAttributes.FillAttr(0.4f,  1.0f,  new Color(245,55, 10), 0.5f, ShapeStyleUtil.FillStyle.SOLID));
				break;
			case ICE_MAX: //积冰最大值（目前为民航WNI数据)
			case CAT_MAX:
			case CAT_MEAN:
			case TURB_MEAN:
			case TURB_MAX:
			case CB_BASE:
			case CB_TOP:

			default:
				throw new RuntimeException("指定的要素" + elem.toString() + ":" + elem.getCHNName() + "没有默认配置!");
		}
		if ((attr.getLevels() == null) && !attr.isHasSetLevels)	{
			attr.setLevels(getLevelsFromMinMaxBaseVal(valBaseVal, valInterval, minVal, maxVal));
		}
		attr.setLineColor(color);
		attr.setDoubleLineColor(isDoubleLineColor);
		if (isDoubleLineColor && attr.getLevels() != null && negativeColor != null){ //改变负值线条颜色
			attr.lineColorList = new HashMap<Float, Color>();
			for (int i = 0; i < attr.getLevels().length; i++){
				float val = attr.getLevels()[i];
				if (val < 0){
					attr.lineColorList.put(Float.valueOf(val), negativeColor);
				}
			}
		}




		return attr;
	}

	public static float[] getLevelsFromMinMaxBaseVal(float valBaseVal, float valInterval, float minVal, float maxVal)
	{
		ArrayList<Float> levelsList = new ArrayList<Float>();
		float tmpVal = valBaseVal;
		int count = 0;
		while (tmpVal >= minVal){
			tmpVal -= Math.abs(valInterval);
			if (tmpVal < minVal)
				break;
			levelsList.add(tmpVal);
			count ++;
			if (count > 10000){
				throw new RuntimeErrorException(null, "给定值计算levels级别太多,请检查是否正确设置最大最小值");
			}
		}
		levelsList.add(valBaseVal);
		tmpVal = valBaseVal;
		count = 0;
		valInterval = Math.abs(valInterval);
		while (tmpVal <= maxVal){
//			tmpVal += Math.abs(valInterval);
			tmpVal = tmpVal + valInterval;
			if (tmpVal > maxVal)
				break;
//            NumberFormat nf=DecimalFormat.getNumberInstance();
			tmpVal = new BigDecimal(tmpVal).setScale(1, BigDecimal.ROUND_HALF_DOWN).floatValue();

			levelsList.add(tmpVal);
			count ++;
			if (count > 10000){
				throw new RuntimeErrorException(null, "给定值计算levels级别太多,请检查是否正确设置最大最小值");
			}
		}
		float[] levels = new float[levelsList.size()];
		for (int i = 0; i < levelsList.size(); i++){
			levels[i] = levelsList.get(i);
		}
		return levels;
	}
	public double getThickVal() {
		return thickVal;
	}
	public void setThickVal(double thickVal) {
		this.thickVal = thickVal;
	}
	public double getThickInteval() {
		return thickInteval;
	}
	public void setThickInteval(double thickInteval) {
		this.thickInteval = thickInteval;
	}
	public float getValInterval() {
		return valInterval;
	}
	public void setValInterval(float valInterval) {
		this.valInterval = valInterval;
	}
	public float getValBaseVal() {
		return valBaseVal;
	}
	public void setValBaseVal(float valBaseVal) {
		this.valBaseVal = valBaseVal;
	}
	public float getMinVal() {
		return minVal;
	}
	public void setMinVal(float minVal) {
		this.minVal = minVal;
	}
	public float getMaxVal() {
		return maxVal;
	}
	public void setMaxVal(float maxVal) {
		this.maxVal = maxVal;
	}

	@Override
	public IsolinesAttributes clone(){
		IsolinesAttributes clone = null;
		try {
			clone = (IsolinesAttributes)super.clone();
			if (this.fillAry != null && this.fillAry.size() > 0){
				clone.fillAry = new ArrayList<FillAttr>(this.fillAry.size());

				for (FillAttr fillattr : this.fillAry){
					clone.fillAry.add(fillattr.clone());
				}
			}
			if (this.lineColorList != null && this.lineColorList.size() > 0){
				clone.lineColorList = new HashMap<Float, Color>(this.lineColorList.size());
				clone.lineColorList.putAll(this.lineColorList);
			}
			if (this.lineColors != null && this.lineColors.length > 0){
				clone.lineColors = new Color[this.lineColors.length];
				for (int i = 0; i < this.lineColors.length; i++){
					if (this.lineColors[i] != null)
						clone.lineColors[i] = new Color(this.lineColors[i].getRGB());
				}
			}
			if (this.filterLonLatAry != null && !filterLonLatAry.isEmpty()){
				List<String> list = new ArrayList<String>();
				for (int i = 0; i < this.filterLonLatAry.size(); i++){
					list.add(filterLonLatAry.get(i));
				}
				clone.setFilterLonLatAry(list);
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return clone;
	}

	@Override
	public boolean equals(Object obj) {
		if(this==obj)
			return true;
		if(obj==null||!(obj instanceof IsolinesAttributes)){
			return false;
		}
		IsolinesAttributes atrr = (IsolinesAttributes)obj;
		if(this.isFilterMinDiameter()!=atrr.isFilterMinDiameter()
				||this.getFilterMinDiameter()!=atrr.getFilterMinDiameter()
				||this.isFilterOpenLine()!=atrr.isFilterOpenLine()
				||this.isSmoothLine()!=atrr.isSmoothLine()
				||this.getSmoothLevel()!=atrr.getSmoothLevel()
				||this.isDrawLine()!=atrr.isDrawLine()
				||!compare(this.getLineColors(), atrr.getLineColors())
				||this.getLineWidth()!=atrr.getLineWidth()
				||!compare(this.getLineStyle(), atrr.getLineStyle())
				||this.isLabelVal()!=atrr.isLabelVal()
				||!compare(this.sFormat, atrr.sFormat)
				||this.isLabelCenter()!=atrr.isLabelCenter()
				||!compare(this.getLabelCenterType(), atrr.getLabelCenterType())
				||this.isLineThick()!=atrr.isLineThick()
				||this.getThickVal()!=atrr.getThickVal()
				||this.getThickInteval()!=atrr.getThickInteval()
				||!compare(this.lineThickList, atrr.lineThickList)
				||!compare(this.lineStyleList, atrr.lineStyleList)
				||!Arrays.equals(this.lineColors, atrr.lineColors)
				||!compare(this.lineColorList, atrr.lineColorList)
				||this.isShowSingleLine!=atrr.isShowSingleLine
				||!compare(this.lineShowList, atrr.lineShowList)
				||this.isHasSetLevels()!=atrr.isHasSetLevels()
				||this.isShowInnerOutterLowLine()!=atrr.isShowInnerOutterLowLine()
				||this.isShowInnerOutterHighLine()!=atrr.isShowInnerOutterHighLine()
				||this.isFill()!=atrr.isFill()
				||!Arrays.equals(this.getLevels(), atrr.getLevels())
				||this.getValInterval()!=atrr.getValInterval()
				||this.getValBaseVal()!=atrr.getValBaseVal()
				||this.getMinVal()!=atrr.getMinVal()
				||this.getMaxVal()!=atrr.getMaxVal()
				||this.isStreamline()!=atrr.isStreamline()
				||!Arrays.equals(this.fillAry.toArray(), atrr.fillAry.toArray())
				||this.isDoubleLineColor!=atrr.isDoubleLineColor
				||this.isSetLineStartLimit!=atrr.isSetLineStartLimit
				||this.isSetLineEndLimit!=atrr.isSetLineEndLimit
				||this.isSetFillStartLimit!=atrr.isSetFillStartLimit
				||this.isSetFillEndLimit!=atrr.isSetFillEndLimit
				||this.isFilterLonLat!=atrr.isFilterLonLat
				||this.lineColorProportion!=atrr.lineColorProportion
				||this.fillColorProportion!=atrr.fillColorProportion)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		int result;

		result = (this.isDrawLine() ? 1 : 0);
		result = 31 * result + (this.isFill() ? 1 : 0);
		result = 31 * result + (this.isSmoothLine() ? 1 : 0);
		result = 31 * result + (this.isFilterMinDiameter() ? 1 : 0);
		result = 31 * result + (this.isFilterOpenLine() ? 1 : 0);
		result = 31 * result + (this.isHasSetLevels() ? 1 : 0);
		result = 31 * result + (this.getLevels() != null ? this.getLevels().length : 0);
		result = 31 * result + (this.getLineColors()[0] != null ? this.getLineColors()[0].hashCode() : 0);

		return result;
	}

	/**
	 * 对比两个引用类型实例
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	private boolean compare(Object obj1,Object obj2){
		if(obj1==null&&obj2!=null)
			return false;
		if(obj1!=null&&obj2==null)
			return false;
		if(obj1==null)
			return true;
		return obj1.equals(obj2);
	}


	/**
	 * 风类型
	 *
	 * @author Zhoucj
	 *
	 */
	public enum WindType
	{
		VECTOR("VECTOR"),    // 风矢量
		VANE("VANE");     // 风羽

		private String value;

		WindType(String value)
		{
			this.value = value;
		}

		public String getValue()
		{
			return this.value;
		}

		public static WindType fromValue(String value)
		{
			String tmpValue = value;
			if (tmpValue.equals("PLUME"))
			{
				tmpValue = "VANE";
			}
			for (WindType k: WindType.values())
			{
				if (k.getValue().equals(tmpValue))
				{
					return k;
				}
			}
			return null;
		}

		@Override
		public String toString()
		{
			return this.value;
		}
	}

	public static String booleanToStr(boolean value) {
		return value ? "1" : "0";
	}

	/**
	 * 获取渐变颜色序列
	 * @param fillColors
	 * @param count
	 * @param fillAlpha
	 * @return
	 */
	public static List<Color> getGradualColors(Color[] fillColors,
											   float splitProportion, int count, float fillAlpha) {
		if (count == 0) {
			return null;
		}
		if (count < 0) {
			count = Math.abs(count);
		}

		/*
		 * 是否使用渐变算法
		 */
		boolean useGlgorithm = false;
		/*
		 * 颜色数组是否已经初始化
		 */
		boolean hasColors = false;
		/*
		 * 有几次过渡过程次数
		 */
		int p = 0;
		//用有效的颜色长度（颜色不为空）
		int colorLength = 0;
		for (int i = 0; i < fillColors.length; i++) {
			if (fillColors[i] != null) {
				colorLength++;
			}
		}
		if (fillColors != null && colorLength > 0) {
			hasColors = true;
			p = colorLength - 1;
			if (count > colorLength && p >= 1) {
				useGlgorithm = !useGlgorithm;
			}
		}
		List<Color> result = new ArrayList<Color>();
		for (int i = 0; i < count; i++) {
			Color tempColor = null;
			if (hasColors) {
				if (useGlgorithm) {
					if (p == 1) {//只有两种颜色的时候,直接使用颜色渐变公式
						Color start = fillColors[0];
						Color end = fillColors[1];
						tempColor = calcColor(start, end, count, i, fillAlpha);
					} else {//超过两种颜色,有多个渐变过程~~~  此处只考虑到有两个渐变过程 即三色渐变
						//int onePartCount = count / p;
						int onePartCount = (int) (Math.max(2f, splitProportion * count));
						Color c1 = fillColors[0];
						Color c2 = fillColors[1];
						Color c3 = fillColors[2];
						for (int p1 = 0; p1 < onePartCount; p1++) {
							tempColor = calcColor(c1, c2, onePartCount, p1,
									fillAlpha);
							result.add(tempColor);
						}
						final int towPartCount = count - onePartCount;
						int tempP = 0;
						for (int p2 = onePartCount; p2 < count; p2++) {
							tempColor = calcColor(c2, c3, towPartCount, tempP,
									fillAlpha);
							result.add(tempColor);
							tempP++;
						}
						break;//跳出最外层循环
					}
				} else {
					tempColor = fillColors[i >= colorLength ? (colorLength - 1)
							: i];//需考虑透明选择项
					//					tempColor = new Color(tempColor.getRed(), tempColor
					//							.getGreen(), tempColor.getBlue(),
					//							(int) (255 * fillAlpha));
					tempColor = new Color(tempColor.getRed(), tempColor
							.getGreen(), tempColor.getBlue());
				}
				result.add(tempColor);//只有一中或者两种颜色的颜色添加
			}

		}
		return result;
	}

	/**
	 * 计算渐变颜色
	 *
	 * @param start 开始
	 * @param end 结束
	 * @param count 渐变次数
	 * @param i 渐变步长
	 * @param fillAlpha 不透明度
	 * @return
	 */
	public static Color calcColor(Color start, Color end, int count, int i,
								  float fillAlpha) {
		Color tempColor;
		//###渐变算法：G=start+(end-start)/count*(i+1)
		int red = start.getRed() + (end.getRed() - start.getRed()) / count
				* (i + 1);
		//        red = red < 0 ? 0 : red;
		//        red = red > Color.RED.getRed() ? Color.RED.getRed() : red;
		int green = start.getGreen() + (end.getGreen() - start.getGreen())
				/ count * (i + 1);
		//        green = green < 0 ? 0 : green;
		//        green = green > Color.GREEN.getGreen() ? Color.GREEN.getGreen() : green;
		int blue = start.getBlue() + (end.getBlue() - start.getBlue()) / count
				* (i + 1);
		//        blue = blue < 0 ? 0 : blue;
		//        blue = blue > Color.BLUE.getBlue() ? Color.BLUE.getBlue() : blue;
		//###
		//		tempColor = new Color(red, green, blue, (int) (fillAlpha * 255));
		red = (red < 0) ? 0 : ((red > 255) ? 255 : red);
		green = (green < 0) ? 0 : ((green > 255) ? 255 : green);
		blue = (blue < 0) ? 0 : ((blue > 255) ? 255 : blue);
		fillAlpha = (blue < 0) ? 0 : ((fillAlpha > 255) ? 255 : fillAlpha);
		if (fillAlpha < 1)
			fillAlpha = fillAlpha * 255;
		tempColor = new Color(red, green, blue,(int)(fillAlpha));
		return tempColor;
	}
	public interface ConfigID {

		String getConfigFileName();
		void setConfigFileName(String configFileName);
	}

	public static class IsolineGloableConfig {
		private static final String NODE_NAME = "isolineGloableConfig";
		private static final String SCHEMES_NAME = "isolinesConfigInfos";

		private static final String DEFUALT_FILE_TAIL = "_default";

		private List<Scheme> schemes = new ArrayList<Scheme>();
		public void addConfigInfo(Scheme scheme) {
			schemes.add(scheme);
		}

		public void removeConfigInfo(Scheme scheme) {
			schemes.remove(scheme);
		}

		public void removeAllConfigInfo() {
			schemes.clear();
		}
		public List<Scheme> getSchemes() {
			return schemes;
		}
		public void setSchemes(List<Scheme> schemes) {
			this.schemes = schemes;
		}
		/**
		 * 通过configKey返回配置信息
		 * @param configKey
		 * @return
		 */
		public IsolinesAttributes getConfig(ConfigKey configKey){
			IsolinesAttributes configAtrr = null;
			for(Scheme scheme : schemes){
				for (ConfigKey tempKey : scheme.getConfigKeys()) {
					//如果传入的configKey中一个成员变量存在并且与tempKey中对应的成员变量不等，则断定两个key不同
					if(configKey.getElemCode() != null &&
							!configKey.getElemCode().equals(tempKey.getElemCode())){
						continue;
					}
					if(configKey.getPress() != null && !configKey.getPress().isEmpty() && !configKey.getPress().equals(tempKey.getPress())){
						continue;
					}
					if(configKey.getDataSource() != null && !configKey.getDataSource().equals(tempKey.getDataSource())){
						continue;
					}
					if(configKey.getDataType() != null && !configKey.getDataType().equals(tempKey.getDataType())){
						continue;
					}
					if(configKey.getSubConditon() != null && !configKey.getSubConditon().isEmpty() && !configKey.getSubConditon().equals(tempKey.getSubConditon())){
						continue;
					}
					if(configKey.getWindType() != null && !configKey.getWindType().equals(tempKey.getWindType())){
						continue;
					}
					configAtrr = scheme.getIsolinesAttributes();
					return configAtrr;
				}
			}
			return configAtrr;
		}


		/**
		 * 通过配置键key获取相关的配置属性，首先从普通配置文件中获取，如果为空，则从默认配置取
		 * @param configKey
		 * @return
		 */
		public IsolinesAttributes getConfigByKey(ConfigKey configKey){
			IsolinesAttributes configAtrr = null;
			configAtrr = getConfigByKey(configKey, 1);
			return configAtrr==null?getConfigByKey(configKey, 0):configAtrr;
		}

		/**
		 * 通过key从配置文件中找到相应的配置属性
		 * @param configKey 配置键实例
		 * @param readType 读取类型 1为读取普通配置，0为读取默认配置
		 * @return
		 */
		public IsolinesAttributes getConfigByKey(ConfigKey configKey,int readType){
			if (configKey == null)
				return null;
			IsolinesAttributes configAtrr = null;
			String configFileName = configKey.getConfigFileName();
			if(readType==0){//如果读取默认配置，则需要修改现在的文件名
				final int lastIndex = configFileName.lastIndexOf(".");
				final String fileName = configFileName.substring(0, lastIndex);
				final String fileExt = configFileName.substring(lastIndex);
				configFileName = fileName+DEFUALT_FILE_TAIL+fileExt;
			}
			//TODO 完善文件路径判断  绝对路径 相对路径
			//final File target = new File(new File("."),configFileName);
			final File target = new File(configFileName);
			if(!target.exists()){//如果文件不存在
				System.err.println("配置文件不存在！文件路径："+target.getAbsolutePath());
				return null;
			}
			IsolineGloableConfig globalConfig = new IsolineGloableConfig().createInstance(target);
			if(globalConfig==null)
				return null;
			for(Scheme scheme : schemes){
				boolean has = scheme.getConfigKeys().contains(configKey);
				if(has){
					configAtrr = scheme.getIsolinesAttributes();
					break;
				}
			}
			return configAtrr;
		}


		/**
		 * 修改配置信息（如果没有配置，则新增配置）
		 * @param configKey
		 * @param configAtrr
		 */
		public void updateOrAddConfig(ConfigKey configKey,IsolinesAttributes configAtrr){
			if(configKey==null||configAtrr==null){
				System.err.println("配置键和配置属性都不能为空！");
				return;
			}
			/* 已经更新标记，默认为false */
			boolean updated = false;
			for(Scheme scheme : schemes){
				List<ConfigKey> keys = scheme.getConfigKeys();
				boolean has = keys.contains(configKey);
				if(has){//对比配置信息是否有改动
					if(!configAtrr.equals(scheme.getIsolinesAttributes())){
						if(keys.size()==1){//只有一个键对应的情况，对比属性配置，变更属性配置
							//属性配置不同，替换旧的配置内容
							scheme.setIsolinesAttributes(configAtrr);
						}else if(keys.size()>1){//有多个键对应的情况
							//移除原配置的键
							keys.remove(configKey);
							//判断所有配置属性差异性
							boolean found = false;
							for(Scheme scheme2: schemes){
								if(configAtrr.equals(scheme2.getIsolinesAttributes())){
									//有相同的属性配置，则把键移动到该配置方案的键列表中
									found = !found;
									scheme2.addKey(configKey);
									break;
								}
							}
							if(!found){//没有相同的配置属性，则新建配置节点
								Scheme e = new Scheme();
								e.addKey(configKey);
								e.setIsolinesAttributes(configAtrr);
								schemes.add(e);
							}
						}
						updated = true;//已经更新
					}else{
						//属性没有发生变化，不需要更新
						return;
					}
					break;
				}
			}
			//如果没有更新，则说明原配置中没有此configKey的配置，
			//此时需要添加配置
			if(!updated){
				boolean found = false;
				for(Scheme scheme2: schemes){
					if(configAtrr.equals(scheme2.getIsolinesAttributes())){
						//有相同的属性配置，则把键移动到该配置方案的键列表中
						found = !found;
						scheme2.addKey(configKey);
						return;
					}
				}

				Scheme e = new Scheme();
				e.addKey(configKey);
				e.setIsolinesAttributes(configAtrr);
				schemes.add(e);
			}
		}

		/**
		 *
		 * @param target
		 * @throws IOException
		 */
		public void exportAsXML(File target) throws IOException{

		}

		public IsolineGloableConfig createInstance(File target){
			IsolineGloableConfig gloableConfig = null;
			DocumentBuilder builder;
			try {
				builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document doc = builder.parse(target);
				Element root = doc.getDocumentElement();
				if(getXMLNodeName().equals(root.getNodeName())){
					gloableConfig = new IsolineGloableConfig();
					NodeList childNodes = root.getChildNodes();
					for(int i=0;i<childNodes.getLength();i++){
						Node cnode = childNodes.item(i);
						String nodeName = cnode.getNodeName();
						if(nodeName.equals(SCHEMES_NAME)){
							List<Scheme> schemes = new ArrayList<Scheme>();
							NodeList childNodes2 = cnode.getChildNodes();
							if(childNodes2!=null){
								for(int j=0;j<childNodes2.getLength();j++){
									Node item = childNodes2.item(j);
									if(!(item instanceof Element)){
										continue;
									}
									Element cnode2 = (Element) item;
									Scheme data = new Scheme();
									data = (Scheme) data.createInstance(cnode2);
									schemes.add(data);
								}
							}
							gloableConfig.setSchemes(schemes);
						}
					}
				}
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return gloableConfig;
		}

		private Object getXMLNodeName() {
			return NODE_NAME;
		}

	}

	public static class Scheme{
		private static final String NODE_NAME = "scheme";
		private static final String CONFIGKEYS_NAME = "configKeys";
		private static final String ISOLINESATTRIBUTES_NAME = "config";

		private List<ConfigKey> configKeys;
		private IsolinesAttributes isolinesAttributes;

		public List<ConfigKey> getConfigKeys() {
			return configKeys;
		}
		public void addKey(ConfigKey configKey) {
			if(configKeys == null){
				configKeys = new ArrayList<ConfigKey>();
			}
			configKeys.add(configKey);
		}
		public void setConfigKeys(List<ConfigKey> configKeys) {
			this.configKeys = configKeys;
		}
		public IsolinesAttributes getIsolinesAttributes() {
			return isolinesAttributes;
		}
		public void setIsolinesAttributes(IsolinesAttributes isolinesAttributes) {
			this.isolinesAttributes = isolinesAttributes;
		}
		public Scheme createInstance(Object input){
			Scheme scheme = null;
			if(input==null){
				return null;
			}
			if(input instanceof Node){
				Element node = (Element) input;
				if(getXMLNodeName().equals(node.getNodeName())){
					scheme = new Scheme();
					NodeList childNodes = node.getChildNodes();
					for(int i=0;i<childNodes.getLength();i++){
						Node cnode = childNodes.item(i);
						String nodeName = cnode.getNodeName();
						if(nodeName.equals(CONFIGKEYS_NAME)){
							List<ConfigKey> configKeys = new ArrayList<ConfigKey>();
							NodeList childNodes2 = cnode.getChildNodes();
							if(childNodes2!=null){
								for(int j=0;j<childNodes2.getLength();j++){
									Node item = childNodes2.item(j);
									if(!(item instanceof Element)){
										continue;
									}
									Element cnode2 = (Element) item;
									//String nodeName2 = cnode2.getNodeName();
									ConfigKey data = new ConfigKey();
									data = (ConfigKey) data.createInstance(cnode2);
									configKeys.add(data);
								}
							}
							scheme.setConfigKeys(configKeys);
						}
						if(nodeName.equals(ISOLINESATTRIBUTES_NAME)){
							IsolinesAttributes attributes = new IsolinesAttributes();
							attributes = (IsolinesAttributes)attributes.createInstance(cnode);
							scheme.setIsolinesAttributes(attributes);
						}
					}
				}
			}
			return scheme;
		}
		public void exportAsXML(){

		}

		public String getXMLNodeName(){
			return NODE_NAME;
		}
	}

	/**
	 * 配置键
	 *
	 * @author LJQ
	 */
	public static class ConfigKey implements ConfigID{
		private static final String NODE_NAME = "configKey";
		private static final String KEYTYPE_NAME = "keyType";
		private static final String DATATYPE_NAME = "dataType";
		private static final String WINDTYPE_NAME = "windType";
		private static final String ELEMCODE_NAME = "elemCode";
		private static final String PRESS_NAME = "press";
		private static final String DATASOURCE_NAME = "dataSource";
		private static final String SUBCONDTION_NAME = "subConditon";
		/**
		 * 配置键类型枚举
		 *
		 * @author LJQ
		 */
		public static enum ConfigKeyType {
			/** 等值线 */
			ISOLINE, OTHERS
		}
		/** 配置键类型 */
		private ConfigKeyType keyType;
		/** 数据集类型 */
		private DataType dataType;
		/** 风类型 */
		private WindType windType;
		/** 　要素编码 */
		private ElemCode elemCode;
		/** 层次类型 */
		private String press;
		/** 数据来源 */
		private String dataSource;
		public String getDataSource() {
			return dataSource;
		}
		public void setDataSource(String dataSource) {
			this.dataSource = dataSource;
		}
		/** 附加配置条件(作用1：区分单层与多层的等值线配置) **/
		private String subConditon;

		private String configFileName = "config\\isoline\\isoline.xml";
		public String getConfigFileName() {
			// 将来文件太大，则分为多个文件
			// 根据DataType、Press、ElemCode选一个对应的配置文件
			// return getFileNameByKeyType(keyType);
			return configFileName;
		}
		public ConfigKey createInstance(
				Object input) {
			ConfigKey configKey = null;
			if(input==null){
				return null;
			}
			if(input instanceof Node){
				Element node = (Element) input;
				if(getXMLNodeName().equals(node.getNodeName())){
					configKey = new ConfigKey();
					NodeList childNodes = node.getChildNodes();
					for(int i=0;i<childNodes.getLength();i++){
						Node cnode = childNodes.item(i);
						String nodeName = cnode.getNodeName();
						String content = cnode.getTextContent();
						if(content == null || content.isEmpty()){
							continue;
						}
						if(nodeName.equals(KEYTYPE_NAME)){
							for(ConfigKeyType configKeyType : ConfigKeyType.values()){
								if(configKeyType.name().equals(content.trim())){
									configKey.setKeyType(configKeyType);
									break;
								}
							}
						}
						if(nodeName.equals(DATATYPE_NAME)){
							configKey.setDataType(DataType.fromValue(content.trim()));
						}
						if(nodeName.equals(WINDTYPE_NAME)){
							configKey.setWindType(WindType.fromValue(content.trim()));
						}
						if(nodeName.equals(ELEMCODE_NAME)){
							configKey.setElemCode(ElemCode.fromValue(content.trim()));
						}
						if(nodeName.equals(PRESS_NAME)){
							configKey.setPress(content.trim());
						}
						if(nodeName.equals(DATASOURCE_NAME)){
							configKey.setDataSource(content.trim());
						}
						if(nodeName.equals(SUBCONDTION_NAME)){
							configKey.setSubConditon(content.trim());
						}
					}
				}
			}
			return configKey;
		}
		public String getXMLNodeName(){
			return NODE_NAME;
		}
		public DataType getDataType() {
			return dataType;
		}
		public ElemCode getElemCode() {
			return elemCode;
		}
		public String getFilename() {
			return configFileName;
		}
		public ConfigKeyType getKeyType() {
			return keyType;
		}
		public String getPress() {
			return press;
		}
		public void setDataType(DataType dataType) {
			this.dataType = dataType;
		}
		public void setElemCode(ElemCode elemCode) {
			this.elemCode = elemCode;
		}
		public void setFilename(String filename) {
			this.configFileName = filename;
		}
		public void setKeyType(ConfigKeyType keyType) {
			this.keyType = keyType;
		}
		public WindType getWindType() {
			return windType;
		}
		public void setWindType(WindType windType) {
			this.windType = windType;
		}
		public void setPress(String press) {
			this.press = press;
		}
		public void setConfigFileName(String configFileName) {
			this.configFileName = configFileName;
		}

		public String getSubConditon() {
			return subConditon;
		}
		public void setSubConditon(String subConditon) {
			this.subConditon = subConditon;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null)
				return false;
			if (this.getClass() != obj.getClass()) {
				return false;
			}
			ConfigKey key = (ConfigKey) obj;
			return new EqualsBuilder()
					.append(this.dataType, key.dataType)
					.append(this.press, key.press)
					.append(this.elemCode, key.elemCode)
					.append(this.windType, key.windType)
					.append(this.dataSource, key.dataSource)
					.append(this.subConditon, key.subConditon).isEquals();
		}
		@Override
		public int hashCode() {
			return new HashCodeBuilder().append(this.dataType)
					.append(this.elemCode)
					.append(this.windType)
					.append(this.press)
					.append(this.dataSource)
					.append(this.subConditon).toHashCode();
		}
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("DataType:" + this.dataType);
			sb.append(",");
			sb.append("ElemCode:" + this.elemCode);
			sb.append(",");
			sb.append("Press:" + press);
			sb.append(",");
			sb.append("DataSourse:" + this.dataSource);
			return sb.toString();
		}
	}
	public IsolinesAttributes createInstance(Object input) {
		if (!(input instanceof Element)){
			return null;
		}
		Element elem = (Element)input;
		if (!elem.getTagName().equals("config")){
			System.err.println("In IsolinesAttr createInstanceFromXML...TagName不正确!");
			return null;
		}
		NodeList children = elem.getChildNodes();
		IsolinesAttributes attr = new IsolinesAttributes();
		for (int i = 0; i < children.getLength(); i++){
			Node node = children.item(i);
//			if (node.getNodeType() != Node.TEXT_NODE){
//				//可能不一定是TEXT_NODE, 需要进行处理!
//				continue;
//			}
			String content = node.getTextContent().trim();
			if (content.isEmpty())
				continue;
			if(node.getNodeName().equals("isFillSurfaceColor")){
				attr.setFillSurfaceColor(MidsUtil.convertStringToBoolean(content));
			}
			if(node.getNodeName().equals("isFilterMinDiameter")){
				attr.setIsFilterMinDiameter(MidsUtil.convertStringToBoolean(content));
			}
			if(node.getNodeName().equals("isFilterLonLat")){
				attr.setFilterLonLat(MidsUtil.convertStringToBoolean(content));
			}
			if(node.getNodeName().equals("isDoubleLineColor")){
				attr.setDoubleLineColor(MidsUtil.convertStringToBoolean(content));
			}
			if(node.getNodeName().equals("lineColorSplitValue")){
				attr.setLineColorSplitValue(Float.valueOf(content));
			}
			if(node.getNodeName().equals("lineColorProportion")){
				attr.setLineColorProportion(Float.valueOf(content));
			}
			if(node.getNodeName().equals("fillColorProportion")){
				attr.setFillColorProportion(Float.valueOf(content));
			}
			if(node.getNodeName().equals("filterMinDiameter")){
				attr.setFilterMinDiameter(Float.valueOf(content));
			}
			if(node.getNodeName().equals("isFilterOpenLine")){
				attr.setFilterOpenLine(MidsUtil.convertStringToBoolean(content));
			}
			if(node.getNodeName().equals("isSmoothLine")){
				attr.setSmoothLine(MidsUtil.convertStringToBoolean(content));
			}
			if(node.getNodeName().equals("smoothLevel")){
				attr.setSmoothLevel(Float.valueOf(content));
			}
			if(node.getNodeName().equals("isDrawLine")){
				attr.setDrawLine(MidsUtil.convertStringToBoolean(content));
			}
			if(node.getNodeName().equals("lineColors")){
				NodeList colorNodes = node.getChildNodes();
				List<Color> colorList = new ArrayList<Color>();
				for (int k = 0; k < colorNodes.getLength(); k++) {
					Node subFloatNode = colorNodes.item(k);
					if(subFloatNode.getNodeName().equals("awt-color")){
						NodeList colorElemNodes = subFloatNode.getChildNodes();
						int r = 0, g = 0, b = 0, a = 255;
						for (int l = 0; l < colorElemNodes.getLength(); l++) {
							Node colorElemNode = colorElemNodes.item(l);
							if(colorElemNode.getNodeName().equals("red")){
								r = Integer.valueOf(colorElemNode.getTextContent());
							}
							if(colorElemNode.getNodeName().equals("green")){
								g = Integer.valueOf(colorElemNode.getTextContent());
							}
							if(colorElemNode.getNodeName().equals("blue")){
								b = Integer.valueOf(colorElemNode.getTextContent());
							}
							if(colorElemNode.getNodeName().equals("alpha")){
								a = Integer.valueOf(colorElemNode.getTextContent());
							}
						}
						colorList.add(new Color(r, g, b, a));
					}
				}
				Color[] colorAry = new Color[colorList.size()];
				for (int k = 0; k < colorAry.length; k++){
					colorAry[k] = colorList.get(k);
				}
				attr.setLineColors(colorAry);
			}
			if(node.getNodeName().equals("lineWidth")){
				attr.setLineWidth(Float.valueOf(content));
			}
			if(node.getNodeName().equals("lineStyle")){
				attr.setLineStyle(ShapeStyleUtil.LineStyle.valueOf(content));
			}
			if(node.getNodeName().equals("isLabelVal")){
				attr.setLabelVal(MidsUtil.convertStringToBoolean(content));
			}
			if(node.getNodeName().equals("sFormat")){
				attr.setLabelFormat(content);
			}
			if(node.getNodeName().equals("isLabelCenter")){
				attr.setLabelCenter(MidsUtil.convertStringToBoolean(content));
			}
			if(node.getNodeName().equals("labelCenterType")){
				attr.setLabelCenterType(IsolinesAttributes.CenterType.fromValue(content));
			}
			if(node.getNodeName().equals("isSetLineStartLimit")){
				attr.setSetLineStartLimit(MidsUtil.convertStringToBoolean(content));
			}
			if(node.getNodeName().equals("isSetLineEndLimit")){
				attr.setSetLineEndLimit(MidsUtil.convertStringToBoolean(content));
			}
			if(node.getNodeName().equals("isSetFillStartLimit")){
				attr.setSetFillStartLimit(MidsUtil.convertStringToBoolean(content));
			}
			if(node.getNodeName().equals("isSetFillEndLimit")){
				attr.setSetFillEndLimit(MidsUtil.convertStringToBoolean(content));
			}
			if(node.getNodeName().equals("isLineThick")){
				attr.setLineThick(MidsUtil.convertStringToBoolean(content));
			}
			if(node.getNodeName().equals("thickVal")){
				String[] strAry = content.split(" ");
				if (strAry.length != 2)
					continue;
				attr.setThickVal(Float.valueOf(strAry[0]), Float.valueOf(strAry[1]));
			}
			if(node.getNodeName().equals("thickInteval")){
				attr.thickInteval = Double.valueOf(content);
			}
			if(node.getNodeName().equals("lineThickList")){

			}
			if(node.getNodeName().equals("lineStyleList")){
				//TODO
			}
			if(node.getNodeName().equals("lineColorList")){
				NodeList subNodes = node.getChildNodes();
				attr.lineColorList = new HashMap<Float, Color>();
				for (int j = 0; j < subNodes.getLength(); j++) {
					Node subNode = subNodes.item(j);
					if(subNode.getNodeName().equals("entry")){
						NodeList floatNodes = subNode.getChildNodes();
						Float key = null;
						Color value = null;
						for (int k = 0; k < floatNodes.getLength(); k++) {
							Node subFloatNode = floatNodes.item(k);
							if(subFloatNode.getNodeName().equals("float")){
								key = Float.valueOf(subFloatNode.getTextContent());
							}
							if(subFloatNode.getNodeName().equals("awt-color")){
								NodeList colorNodes = subFloatNode.getChildNodes();
								int r = 0, g = 0, b = 0, a = 255;
								for (int l = 0; l < colorNodes.getLength(); l++) {
									Node colorNode = colorNodes.item(l);
									if(colorNode.getNodeName().equals("red")){
										r = Integer.valueOf(colorNode.getTextContent());
									}
									if(colorNode.getNodeName().equals("green")){
										g = Integer.valueOf(colorNode.getTextContent());
									}
									if(colorNode.getNodeName().equals("blue")){
										b = Integer.valueOf(colorNode.getTextContent());
									}
									if(colorNode.getNodeName().equals("alpha")){
										a = Integer.valueOf(colorNode.getTextContent());
									}
								}
								value = new Color(r, g, b, a);
							}
						}
						if(key != null){
							attr.lineColorList.put(key, value);
						}
					}
				}
			}
			if(node.getNodeName().equals("isShowSingleLine")){
				attr.isShowSingleLine = MidsUtil.convertStringToBoolean(content);
			}
			if(node.getNodeName().equals("lineShowList")){
				//TODO
			}
			if(node.getNodeName().equals("isHasSetLevels")){
				attr.isHasSetLevels = MidsUtil.convertStringToBoolean(content);
			}
			if(node.getNodeName().equals("isShowInnerOutterLowLine")){
				attr.isShowInnerOutterLowLine = MidsUtil.convertStringToBoolean(content);
			}
			if(node.getNodeName().equals("isShowInnerOutterHighLine")){
				attr.isShowInnerOutterHighLine = MidsUtil.convertStringToBoolean(content);
			}
			if(node.getNodeName().equals("isShowInnerOutterLine")){
				attr.isShowInnerOutterLine = MidsUtil.convertStringToBoolean(content);
			}
			if(node.getNodeName().equals("isFill")){
				attr.isFill = MidsUtil.convertStringToBoolean(content);
			}
			if(node.getNodeName().equals("levels")){
				if(content.contains(",")){
					String[] strAry = content.split(",");
					if (strAry.length <= 0)
						continue;
					float[] levels = new float[strAry.length];
					for (int k = 0; k < levels.length; k++)
						levels[k] = Float.valueOf(strAry[k]);
					attr.setLevels(levels);
				} else{
					NodeList nodeList = node.getChildNodes();
					float[] levels = new float[nodeList.getLength()];
					for (int j = 0; j < nodeList.getLength(); j++) {
						Node node2 = nodeList.item(j);
						String nodeName = node2.getNodeName();
						if(nodeName.equals("float")){
							levels[j] = Float.valueOf(node2.getTextContent().trim());
						}
					}
					attr.setLevels(levels);
				}
			}
			if(node.getNodeName().equals("valInterval")){
				attr.setValInterval(Float.valueOf(content));
			}
			if(node.getNodeName().equals("valBaseVal")){
				attr.setValBaseVal(Float.valueOf(content));
			}
			if(node.getNodeName().equals("minVal")){
				attr.setMinVal(Float.valueOf(content));
			}
			if(node.getNodeName().equals("maxVal")){
				attr.setMaxVal(Float.valueOf(content));
			}
			if(node.getNodeName().equals("isStreamline")){
				attr.setStreamline(MidsUtil.convertStringToBoolean(content));
			}
			if(node.getNodeName().equals("fillAry")){
				//TODO
				NodeList fillAryNodes = node.getChildNodes();
				List<FillAttr> fillAttrList = new ArrayList<FillAttr>();
				for (int k = 0; k < fillAryNodes.getLength(); k++) {
					Node subFloatNode = fillAryNodes.item(k);
					if(subFloatNode.getNodeName().equals("fillAttr")){
						FillAttr attrFillAttr = new FillAttr();
						attrFillAttr.createInstance(subFloatNode);
						fillAttrList.add(attrFillAttr);
					}
				}
				attr.setFillAttr(fillAttrList);
			}
			if (node.getNodeName().equals("isCutFromChinaArea")){
				attr.setCutFromChinaArea(MidsUtil.convertStringToBoolean(content));
			}
		}
		//这里需要更新lineColorList!!!
		if (attr.levels != null && attr.levels.length > 0){
			final List<Color> gradualColors = getGradualColors(attr.lineColors,
					attr.lineColorProportion,attr.levels.length, attr.lineColors[0].getAlpha());
			if(gradualColors!=null&&gradualColors.size()>0){
				if (attr.lineColorList == null)
					attr.lineColorList = new HashMap<Float, Color>(attr.levels.length);
				else if (!attr.lineColorList.isEmpty())
					attr.lineColorList.clear();
				for(int i=0;i<attr.levels.length;i++){
					attr.lineColorList.put(attr.levels[i], gradualColors.get(i));
				}
			}
		}
		return attr;
	}

	public enum SurfaceFillColorMode {
		YelloToGreen, YelloToRed
	}


	public JSONObject getLinesColorJSON(){
		if (this.lineColorList == null || this.lineColorList.isEmpty())
			return null;

		JSONObject json = new JSONObject();
		List<Float> vals = new ArrayList<>();
		JSONArray valArray = new JSONArray();
		JSONArray clrArray = new JSONArray();
		for (Float val : this.lineColorList.keySet()){
			vals.add(val);
		}
		Collections.sort(vals);
		for (int i = 0; i < vals.size(); i++){
			valArray.add(vals.get(i));
			clrArray.add(lineColorList.get(vals.get(i)));
		}
		json.put("vals", valArray);
		json.put("colors", clrArray);
		return json;
	}


	public static void main(String[] args) {
		File target2 = new File(new File("."),"/config/isoline/isoline_kj.xml");
		IsolinesAttributes attributes = IsolinesAttributes.createInstance(ElemCode.HH, "0200");
		Map<Float, Color> colorMap = attributes.lineColorList;
		if (colorMap != null){
			for (Float val : colorMap.keySet()){
				System.out.println(val + ":" + colorMap.get(val));
			}
		}
//		attributes = IsolinesAttributes.createInstance(ElemCode.HH, "0300");
//		colorMap = attributes.lineColorList;
//		if (colorMap != null){
//			for (Float val : colorMap.keySet()){
//				System.out.println(val + ":" + colorMap.get(val));
//			}
//		}
		attributes = IsolinesAttributes.createInstance(ElemCode.HH, "0500");
		colorMap = attributes.lineColorList;
		if (colorMap != null){
			for (Float val : colorMap.keySet()){
				System.out.println(val + ":" + colorMap.get(val));
			}
		}
//		attributes = IsolinesAttributes.createInstance(ElemCode.HH, "0700");
//		colorMap = attributes.lineColorList;
//		if (colorMap != null){
//			for (Float val : colorMap.keySet()){
//				System.out.println(val + ":" + colorMap.get(val));
//			}
//		}
//		attributes = IsolinesAttributes.createInstance(ElemCode.HH, "0850");
//		colorMap = attributes.lineColorList;
//		if (colorMap != null){
//			for (Float val : colorMap.keySet()){
//				System.out.println(val + ":" + colorMap.get(val));
//			}
//		}

	}
}
