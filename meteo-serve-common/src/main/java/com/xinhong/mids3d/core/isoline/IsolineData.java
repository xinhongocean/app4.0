package com.xinhong.mids3d.core.isoline;

import gov.nasa.worldwind.geom.Position;

import java.util.ArrayList;
import java.util.List;


/**
 * 保存一条等值线数据
 */
public class IsolineData {
		
	/** 
	 * 等值线点位置 保存经纬度及高度,经纬度必须指定,高度不能确定时指定为0
	 */
	public List<? super Position> lineList = null;	
	/**
	 * 等值线封闭为true,否则为false,必须指定
	 */
	public boolean isClosed = false;
	 /**
	  * 封闭等值线封闭区域比周边高时则为true,比周边低时则为false, 开放等值线为false,封闭时必须指定
	  */
	public Boolean isHigh = null; 
	// isInner isOuter
	//   T       T      既是最内侧又是最外侧 单条封闭等值线
	//   T       F      最内侧等值线
	//   F       T      最外侧等值线
	//   F       F      中间封闭等值线,既包含其他封闭等值线，又被其他等值线所包含
	public boolean isInner = false;	
	public boolean isOuter = false;
	
	/**
	 * 封闭等值线或开等值线和边界围成的闭合区域的高低中心位置
	 */
	public ArrayList<? super Position> highlowPosList = null;
	
	/**
	 * 封闭等值线或开等值线和边界围成的闭合区域的高低中心的值
	 */
	public float highlowVal = Float.MAX_VALUE;	
	/**
	 * 等值线在给定的level数组中的索引,必须指定
	 */
	public int index;
	/**
	 * 等值线值,必须指定
	 */
	public float val; 
	/**
	 * 等值线上等值点的个数
	 */
	public int num;
	
	public boolean isEmpty() {		
		return !(lineList != null && !lineList.isEmpty());			
		
	}
	
	public void dispose() {
		if (highlowPosList != null){
			highlowPosList.clear();
			highlowPosList = null;
		}		
		if (lineList != null){
			lineList.clear();
			lineList = null;
		}		
	}	
	
	/**
	 * 克隆函数，注意：这里如果this.lineList存储的是PositionVec,则克隆结果不正确!
	 */
	@Override
	public IsolineData clone() {
		IsolineData clone = new IsolineData();
		if (this.lineList != null && !this.lineList.isEmpty()){
			clone.lineList = new ArrayList<Position>(lineList.size());
			for (int i = 0; i < this.lineList.size(); i++){
				Position tmpPos = (Position)(this.lineList.get(i));
				Position pos = Position.fromDegrees(tmpPos.getLatitude().degrees, 
						tmpPos.getLongitude().degrees, tmpPos.getElevation());
				clone.lineList.add(pos);
			}
		}
		if (this.highlowPosList != null && !this.highlowPosList.isEmpty()){
			clone.highlowPosList = new ArrayList<Position>(highlowPosList.size());
			for (int i = 0; i < this.highlowPosList.size(); i++){
				Position tmpPos = (Position)(this.highlowPosList.get(i));
				Position pos = Position.fromDegrees(tmpPos.getLatitude().degrees, 
						tmpPos.getLongitude().degrees, tmpPos.getElevation());
				clone.highlowPosList.add(pos);
			}
		}
		clone.highlowVal = this.highlowVal;
		clone.index = this.index;
		clone.val   = this.val;
		clone.num   = this.num;
		clone.isClosed = this.isClosed;
		clone.isInner  = this.isInner;
		clone.isOuter  = this.isOuter;
		clone.isHigh   = this.isHigh;
		return clone;
	}
}
