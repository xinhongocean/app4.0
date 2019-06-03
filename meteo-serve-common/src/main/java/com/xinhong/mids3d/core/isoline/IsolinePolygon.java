package com.xinhong.mids3d.core.isoline;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.xinhong.mids3d.util.ShapeStyleUtil.FillStyle;

/**
 * 等值线填充用多边形类，代表等值线填充的一个多边形
 * @author lxc
 *
 */
public class IsolinePolygon {
	//填充slevel到elevel之间的区域
	protected float slevel = Float.MIN_VALUE; //填充的一个等值线的起始值
	protected float elevel = Float.MIN_VALUE; //填充的一个等值线的结束值
	//initialSElevel是用户设定的level，而selevel为系统追踪形成的level
	//例如: 散度如果填充0以上内容,则用户设置可能为(0,1000)，但实际填充最大值为210,
	//所以结果实际的selevel为(0,210),而用户初始化的initialSElevel为(0,1000)
	protected float initialSlevel = slevel; 
	protected float initialElevel = elevel; 
	
	private Color fillColor = Color.GREEN;   //填充颜色
	private Color lineColor = Color.YELLOW;  //线颜色
	private float fillOpacity = 0.5f;
	private FillStyle fillStyle = FillStyle.SOLID;
	
	protected List<Iterable<? extends LatLon>> boundaries = new ArrayList<Iterable<? extends LatLon>>();
	//设置获取多边形内外边界对应的等值线
	private List<IsolineData> outterBoudaryIsolineList;
	private List<IsolineData> innerBoudaryIsolineList;
	public List<IsolineData> getOutterBoudaryIsolineList() {
		return outterBoudaryIsolineList;
	}
	public void setOutterBoudaryIsolineList(
			List<IsolineData> outterBoudaryIsolineList) {
		this.outterBoudaryIsolineList = outterBoudaryIsolineList;
	}
	public List<IsolineData> getInnerBoudaryIsolineList() {
		return innerBoudaryIsolineList;
	}
	public void setInnerBoudaryIsolineList(
			List<IsolineData> innerBoudaryIsolineList) {
		this.innerBoudaryIsolineList = innerBoudaryIsolineList;
	}
	
	public IsolinePolygon() {}
	public IsolinePolygon(float slevel, float elevel)
	{
		this.slevel = slevel;
		this.elevel = elevel;		
		this.initialSlevel = slevel;
		this.initialElevel = elevel;		
	}
	
	public void setinitialSElevel(float slevel, float elevel)
	{
		this.initialSlevel = slevel;
		this.initialElevel = elevel;	
	}
	public float getinitialSlevel()
	{
		return this.initialSlevel;
	}
	public float getinitialElevel()
	{
		return this.initialElevel;
	}
	
	/**
	 * 
	 * @param slevel 填充的一个等值线的起始值
	 * @param elevel  填充等值线的结束值
	 * @param corners 点列表(多边形外边缘)
	 */
	public IsolinePolygon(float slevel, float elevel, Iterable<? extends Position> corners)
	{
		this.slevel = slevel;
		this.elevel = elevel;			
		this.initialSlevel = slevel;
		this.initialElevel = elevel;		
		
	}
	
	public float getsLevel(){
		return this.slevel;
	}
	
	public float geteLevel(){
		return this.elevel;
	}
	/**
	* 设置多边形的边界(如果原来有边界则被代替)
	* @param boudaries
	*/
	public void setBoundaries(List<Iterable<? extends LatLon>> boudaries) {
		if (boudaries == null || boudaries.size() == 0)
			return;
		this.boundaries.clear();
		for (int i = 0; i < boudaries.size(); i++) {
			this.boundaries.add(boudaries.get(i));
		}
	}

	public void setOuterBoundary(Iterable<? extends LatLon> iterable) {
		if (iterable == null) {
			throw new IllegalArgumentException("nullValue.IterableIsNull");
		}

		if (this.boundaries.size() > 0)
			this.boundaries.set(0, iterable);
		else
			this.boundaries.add(iterable);
	}
	/**
	 * 设置等值线的起始值及结束值
	 * @param slevel
	 * @param elevel
	 */
	public void setSELevel(float slevel, float elevel)
	{
		this.slevel = slevel;
		this.elevel = elevel;	
	}
	
	public void addInnerBoundary(Iterable<? extends LatLon> iterable) {
		if (iterable == null) {
			throw new IllegalArgumentException("nullValue.IterableIsNull");
		}

		this.boundaries.add(iterable);
	}
	public List<Iterable<? extends LatLon>> getBoundaries() {
		return this.boundaries;
	}
	/**
	 * 获取外多边形最大直径
	 * @return
	 */
	protected float getMaxDiameter()
	{
		return getMaxDiameter(this.getOuterBoundary());
	}
	
	public Iterable<? extends LatLon> getOuterBoundary() {
		return this.boundaries.size() > 0 ? this.boundaries.get(0) : null;
	}
	
	/**
	 * 获取给定的经纬度序列的最大直径
	 * @return
	 */
	private float getMaxDiameter(Iterable<? extends LatLon> posList)
	{
		if (posList == null || !posList.iterator().hasNext()){
			throw new IllegalArgumentException("指定的参数posList不正确");
		}
		double maxlat = -999, minlat = 999;
		double maxlon = -999, minlon = 999;
		for (LatLon position:posList)
		{
			if (position.getLatitude().degrees > maxlat)
				maxlat = position.getLatitude().degrees;
			if (position.getLatitude().degrees < minlat)
				minlat = position.getLatitude().degrees;
			if (position.getLongitude().degrees > maxlon)
				maxlon = position.getLongitude().degrees;
			if (position.getLongitude().degrees < minlon)
				minlon = position.getLongitude().degrees;
		}
		float latDiameter =  (float) (maxlat - minlat);
		float lonDiameter =  (float) (maxlon - minlon);
		if (latDiameter > lonDiameter)
			return latDiameter;
		else
			return lonDiameter;		
	}
	public Color getFillColor() {
		return fillColor;
	}
	public Color getLineColor() {
		return lineColor;
	}
	public float getFillOpacity() {
		return fillOpacity;
	}
	public FillStyle getFillStyle() {
		return fillStyle;
	}
	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}
	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}
	public void setFillOpacity(float fillOpacity) {
		this.fillOpacity = fillOpacity;
	}
	public void setFillStyle(FillStyle fillStyle) {
		this.fillStyle = fillStyle;
	}
	@Override
	public IsolinePolygon clone() {
		IsolinePolygon clone = new IsolinePolygon(slevel, elevel);
		clone.setBoundaries(this.boundaries);
		clone.setFillColor(this.fillColor);
		clone.setFillOpacity(this.fillOpacity);
		clone.setFillStyle(this.fillStyle);
		clone.setInnerBoudaryIsolineList(this.innerBoudaryIsolineList);
		clone.setOutterBoudaryIsolineList(this.outterBoudaryIsolineList);
		clone.setLineColor(this.lineColor);
		return clone;
	}
}
