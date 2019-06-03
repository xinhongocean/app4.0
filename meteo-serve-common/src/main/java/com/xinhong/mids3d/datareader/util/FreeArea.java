package com.xinhong.mids3d.datareader.util;

public class FreeArea {
	private float sLat;
	private float eLat;
	private float sLon;
	private float eLon;
	
	/**
	 * 自由区域，经度范围需从西往东，纬度范围需从南到北
	 */
	public FreeArea(){
		
	}
	/**
	 * 自由区域，经度范围需从西往东，纬度范围需从南到北
	 * @param sLat	开始纬度
	 * @param eLat	结束纬度
	 * @param sLon	开始经度
	 * @param eLon	结束经度
	 */
	public FreeArea(float sLat, float eLat, float sLon, float eLon){
		this.sLat = sLat;
		this.eLat = eLat;
		this.sLon = sLon;
		this.eLon = eLon;
		checkFreeArea();
	}
	
	public void checkFreeArea(){
		if(sLat <-90 || sLat > 90)
			throw new RuntimeException("所查询的自由区域中纬度的开始值=["+sLat+"]超出正常纬度范围");
		if(eLat <-90 || eLat > 90)
			throw new RuntimeException("所查询的自由区域中纬度的结束值=["+eLat+"]超出正常纬度范围");
		if(sLon < -180 || sLon > 360)
			throw new RuntimeException("所查询的自由区域中经度的开始值=["+sLon+"]超出正常经度范围");
		if(eLon < -180 || eLon > 360)
			throw new RuntimeException("所查询的自由区域中经度的结束值=["+eLon+"]超出正常经度范围");
		
		if(Math.abs(eLat - sLat) > 180)
			throw new RuntimeException("所查询的自由区域的纬度范围sLat=["+sLat+"],eLat=["+eLat+"],超出180");	
		if(Math.abs(eLon - sLon) > 360)
			throw new RuntimeException("所查询的自由区域的经度范围sLon=["+sLon+"],eLon=["+eLon+"],超出360");
		
		if(sLat > eLat){
			throw new RuntimeException("纬度范围需从南到北，请重新设置纬度范围,错误范围为sLat=["+sLat+"],eLat=["+eLat+"]");
		}
		if(sLon > eLon){
			if(eLon < 0){
				eLon = 360 + eLon;
			}
			if(sLon > eLon){
				throw new RuntimeException("经度范围需从西到东，请重新设置经度范围,错误范围为sLon=["+sLon+"],eLon=["+eLon+"]");
			}
		}
	}
	
	public FreeArea clone(){
		FreeArea newFreeArea = new FreeArea();
		newFreeArea.setsLat(this.sLat);
		newFreeArea.seteLat(this.eLat);
		newFreeArea.setsLon(this.sLon);
		newFreeArea.seteLon(this.eLon);
//		newFreeArea.setArea(this.area);
//		newFreeArea.setLevel(this.level);
		checkFreeArea();
		return newFreeArea;
	}
	
	public float getsLat() { return sLat; }	
	public float geteLat() { return eLat; }	
	public float getsLon() { return sLon; }	
	public float geteLon() { return eLon; }	
	
	public void setsLon(float sLon) { this.sLon = sLon; }
	public void seteLat(float eLat) { this.eLat = eLat; }
	public void setsLat(float sLat) { this.sLat = sLat; }
	public void seteLon(float eLon) { this.eLon = eLon; }
}

class FreeAreaInfo extends FreeArea{
	private GridArea area;
	private int level;
	
	public GridArea getArea() 	{ return area; }
	public int getLevel() 		{ return level; }
	
	public void setArea(GridArea area) 	{ this.area = area; }		
	public void setLevel(int level) 	{ this.level = level; }	
}
