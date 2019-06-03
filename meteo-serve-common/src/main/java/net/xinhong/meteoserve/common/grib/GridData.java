package net.xinhong.meteoserve.common.grib;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.tool.LatLonPoint;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * 格点数据结构
 * @author lxc,sjn
 *
 */
public class GridData {
	private int rowNum = 0;
	private int colNum = 0;
	private float[] latAry1D = null;//纬度数据	
	private float[] lonAry1D = null;//经度数据
	
	private float[][] latAry2D = null;	
	private float[][] lonAry2D = null;
	//经纬度位置信息是否改变
	private boolean isLatsDataChange = false;
	private boolean isLonsDataChange = false;
	private float[][] gridValArr = null;	//数据
	
	private float xstart = 0.0f;
	private float xend = 0.0f;
	private float ystart = 0.0f;
	private float yend = 0.0f;
	private float xdel = 0.0f;
	private float ydel = 0.0f;
	
	private boolean isDistanceEqual = false;
	
	private String dataSource;
	

	
	@Override
	public GridData clone() {
		GridData clone = null;
		clone = new GridData();
		clone.colNum = this.colNum;
		clone.rowNum = this.rowNum;
		clone.xstart = this.xstart;
		clone.xend = this.xend;
		clone.ystart = this.ystart;
		clone.yend = this.yend;
		clone.xdel = this.xdel;
		clone.ydel = this.ydel;
		clone.isDistanceEqual = this.isDistanceEqual;
		clone.isLatsDataChange = this.isLatsDataChange;
		clone.isLonsDataChange = this.isLonsDataChange;
		clone.dataSource = this.dataSource;

		if (this.latAry1D != null && this.latAry1D.length > 0) {
			clone.latAry1D = new float[this.latAry1D.length];
			for (int i = 0; i < this.latAry1D.length; i++) {
				clone.latAry1D[i] = this.latAry1D[i];
			}
		}
		if (this.lonAry1D != null && this.lonAry1D.length > 0) {
			clone.lonAry1D = new float[this.lonAry1D.length];
			for (int i = 0; i < this.lonAry1D.length; i++) {
				clone.lonAry1D[i] = this.lonAry1D[i];
			}
		}
		if (this.latAry2D != null && this.latAry2D.length > 0) {
			clone.latAry2D = new float[this.latAry2D.length][this.latAry2D[0].length];
			for (int i = 0; i < this.latAry2D.length; i++) {
				for (int j = 0; j < this.latAry2D[0].length; j++) {
					clone.latAry2D[i][j] = this.latAry2D[i][j];
				}
			}
		}
		if (this.lonAry2D != null && this.lonAry2D.length > 0) {
			clone.lonAry2D = new float[this.lonAry2D.length][this.lonAry2D[0].length];
			for (int i = 0; i < this.lonAry2D.length; i++) {
				for (int j = 0; j < this.lonAry2D[0].length; j++) {
					clone.lonAry2D[i][j] = this.lonAry2D[i][j];
				}
			}
		}
		if (this.gridValArr != null && this.gridValArr.length > 0) {
			clone.gridValArr = new float[this.gridValArr.length][this.gridValArr[0].length];
			for (int i = 0; i < this.gridValArr.length; i++) {
				for (int j = 0; j < this.gridValArr[0].length; j++) {
					clone.gridValArr[i][j] = this.gridValArr[i][j];
				}
			}
		}
		return clone;
	}
	
	@Override
	public boolean equals(Object obj){
		if (this == obj) {
			return true;
		}
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		//比较基本类型
		if (((GridData)obj).colNum != this.colNum) return false;
		if (((GridData)obj).rowNum != this.rowNum) return false;
		if (((GridData)obj).xstart != this.xstart) return false;
		if (((GridData)obj).xend != this.xend) return false;
		if (((GridData)obj).ystart != this.ystart) return false;
		if (((GridData)obj).yend != this.yend) return false;
		if (((GridData)obj).xdel != this.xdel) return false;
		if (((GridData)obj).ydel != this.ydel) return false;
		if (((GridData)obj).isDistanceEqual != this.isDistanceEqual) return false;
		if (((GridData)obj).isLatsDataChange != this.isLatsDataChange) return false;
		if (((GridData)obj).isLonsDataChange != this.isLonsDataChange) return false;
		if (((GridData)obj).dataSource != this.dataSource) return false;
		//比较数组	``
		if (this.latAry1D != null && ((GridData)obj).latAry1D != null){
			if (this.latAry1D.length != ((GridData)obj).latAry1D.length)
				return false;
			for (int i = 0; i < this.latAry1D.length; i++) {
				if (((GridData)obj).latAry1D[i] != this.latAry1D[i])
					return false;
			}			
		}
		else if (this.latAry1D == null && ((GridData)obj).latAry1D != null)
			return false;
		else if (this.latAry1D != null && ((GridData)obj).latAry1D == null)
			return false;
		//
		if (this.lonAry1D != null && ((GridData)obj).lonAry1D != null){
			if (this.lonAry1D.length != ((GridData)obj).lonAry1D.length)
				return false;
			for (int i = 0; i < this.lonAry1D.length; i++) {
				if (((GridData)obj).lonAry1D[i] != this.lonAry1D[i])
					return false;
			}			
		}
		else if (this.lonAry1D == null && ((GridData)obj).lonAry1D != null)
			return false;
		else if (this.lonAry1D != null && ((GridData)obj).lonAry1D == null)
			return false;
		//
		if (this.latAry2D != null && ((GridData)obj).latAry2D != null){
			if (this.latAry2D.length != ((GridData)obj).latAry2D.length
					|| this.latAry2D[0].length != ((GridData)obj).latAry2D[0].length)
				return false;
			for (int i = 0; i < this.latAry2D.length; i++) {
				for (int j = 0; j < this.latAry2D[0].length; j++) {
				if (((GridData)obj).latAry2D[i][j] != this.latAry2D[i][j])
					return false;
				}
			}			
		}
		else if (this.latAry2D == null && ((GridData)obj).latAry2D != null)
			return false;
		else if (this.latAry2D != null && ((GridData)obj).latAry2D == null)
			return false;
		//
		if (this.lonAry2D != null && ((GridData)obj).lonAry2D != null){
			if (this.lonAry2D.length != ((GridData)obj).lonAry2D.length
					|| this.lonAry2D[0].length != ((GridData)obj).lonAry2D[0].length)
				return false;
			for (int i = 0; i < this.lonAry2D.length; i++) {
				for (int j = 0; j < this.lonAry2D[0].length; j++) {
				if (((GridData)obj).lonAry2D[i][j] != this.lonAry2D[i][j])
					return false;
				}
			}			
		}
		else if (this.lonAry2D == null && ((GridData)obj).lonAry2D != null)
			return false;
		else if (this.lonAry2D != null && ((GridData)obj).lonAry2D == null)
			return false;
		//
		if (this.gridValArr != null && ((GridData)obj).gridValArr != null){
			if (this.gridValArr.length != ((GridData)obj).gridValArr.length
					|| this.gridValArr[0].length != ((GridData)obj).gridValArr[0].length)
				return false;
			for (int i = 0; i < this.gridValArr.length; i++) {
				for (int j = 0; j < this.gridValArr[0].length; j++) {
				if (((GridData)obj).gridValArr[i][j] != this.gridValArr[i][j])
					return false;
				}
			}			
		}
		else if (this.gridValArr == null && ((GridData)obj).gridValArr != null)
			return false;
		else if (this.gridValArr != null && ((GridData)obj).gridValArr == null)
			return false;	
		
		return true;
	}

	/**
	 * @author liuxc
	 *
	 */
	public enum ResultType{
		TXT,
		NC,
		GRAS,
		DAT
	}
	/**
	 * 将结果保存到文件中
	 * 统一用英文分号表示注释
	 * 保存按照规定格式:
	 * 0~n:描述信息
	 * n+1:LonNum, LonDelta, LonAry, 
	 * n+2:Lat, LatDelta, LatAry
	 * n+3...:数据,用-99999.9表示为Null
	 * @param filename-保存的路径。如果不传，则用户选择文件名
	 * @param descStrAry-0~n描述信息.如果不传，则没有（调用方根据具体保存数据类型进行拼接）
	 * @param type-保存采用的文件格式。注意：目前只能实现文本文件的保存
	 * @param 
	 * @return-成功true,失败false-（如何让调用者知道失败的原因？）
	 */
	public boolean writeResult(String filename, List<String> descStrAry, ResultType type) {
		if(this == null ||this.getGridData() == null
				|| filename == null ||filename.equals("")){
			return false;
		}
		String newFileName = filename;
		if (newFileName.endsWith(".nc")) {
			newFileName = newFileName.split("\\.")[0] + ".txt";
		}
		
		StringBuffer sb = new StringBuffer();
		//
		if(null!=descStrAry){
			for(String descStr:descStrAry){
				sb.append(descStr);
				sb.append("\n");
			}
		}
		//
		int row=this.getRowNum();
		int col=this.getColNum();
		
		//追加经度
		//经度一维数据
		String lonDir = "";
		boolean isHasLats = false;
		boolean isHasLons = false;
		if(this.getLonAry1D() != null){
			lonDir = ";经度一维数组，由东到西\n";
			if(this.getLonAry1D().length > 1){
				if(this.getLonAry1D()[1] - this.getLonAry1D()[0] > 0){
					lonDir = ";经度一维数组，由西到东\n";
				}
			}
			sb.append(lonDir);
			//写经度数据
			sb.append("lonAry:\n");
			for(int i=0;i<this.getLonAry1D().length;i++){
				sb.append(this.getLonAry1D()[i] + ",");
			}
			sb.append("\n");
			isHasLats = true;
			//给二维数组赋值
			if(this.getLonAry2D() == null && (row >=0 && col >= 0)){
				float[][] lonAry = new float[row][col];
				for(int r=0;r<row;r++){
					for(int cl=0;cl<col;cl++){
						lonAry[r][cl] = this.getLonAry1D()[cl];
					}
				}
			}
		}
		//经度二维数据(一维数组没有保存成功后保存二维数组)
		if(!isHasLats && this.getLonAry2D() != null){
			if(lonDir == null || lonDir.length() < 2){
				lonDir = ";经度二维数组，由东到西\n";
				if(this.getLonAry2D().length > 1 && this.getLonAry2D()[0].length > 1){
					if(this.getLonAry2D()[0][1] - this.getLonAry2D()[0][0] > 0){
						lonDir = ";经度二维数组，由西到东\n";
					}
				}
			}
			sb.append(lonDir);
			//写经度数据
			sb.append("lonAry2:\n");
			for(int i=0;i<this.getLonAry2D().length;i++){
				for(int j=0;j<this.getLonAry2D()[0].length;j++){
					sb.append(this.getLonAry2D()[i][j] + ",");
				}
				sb.append("\n");
			}
		}
		
		//追加纬度
		//纬度一维数据
		String latDir = "";
		if(this.getLatAry1D() != null){
			latDir = ";纬度一维数组，由北到南\n";
			if(this.getLatAry1D().length > 1){
				if(this.getLatAry1D()[1] - this.getLatAry1D()[0] > 0){
					latDir = ";纬度一维数组，由南到北\n";
				}
			}
			sb.append(latDir);
			//写纬度数据
			sb.append("latAry:\n");
			for(int i=0;i<this.getLatAry1D().length;i++){
				sb.append(this.getLatAry1D()[i] + ",");
			}
			sb.append("\n");
			isHasLons = true;
			//给二维数组赋值
			if(this.getLatAry2D() == null && (row >=0 && col >= 0)){
				float[][] latAry = new float[row][col];
				for(int cl=0;cl<col;cl++){
					for(int r=0;r<row;r++){						
						latAry[r][cl] = this.getLatAry1D()[r];
					}
				}
			}
		}
		//纬度二维数组(一维数组没有保存成功后保存二维数组)
		if(!isHasLons && this.getLatAry2D() != null){
			if(latDir == null || latDir.length() < 2){
				latDir = ";纬度二维数组，由北到南\n";
				if(this.getLatAry2D().length > 1){
					if(this.getLatAry2D()[1][0] - this.getLatAry2D()[0][0] > 0){
						latDir = ";纬度二维数组，由南到北\n";
					}
				}
			}
			sb.append(latDir);
			//写纬度数据
			sb.append("latAry2:\n");
			for(int i=0;i<this.getLatAry2D().length;i++){
				for(int j=0;j<this.getLatAry2D()[0].length;j++){
					sb.append(this.getLatAry2D()[i][j] + ",");
				}
				sb.append("\n");
			}
		}
		//
		//追加数据
		sb.append(";数据，若经纬度二维数组数据均不为空，则与经纬度数据一一对应，即经纬度点(latAry[i],lonAry[i])对应的数据位置为dataAry[i]。" +
				" 若经纬度一维数组不为空，则每行为同纬度数据，每列为同经度数据，方向与经纬度方向保持一致\n");
		sb.append("dataAry:\n");
		float[][] gridValArr=this.getGridData();
		for(int r=0;r<row;r++){
			for(int c=0;c<col;c++){
				sb.append(gridValArr[r][c]+",");
			}
			sb.append("\n");
		}
		
		try {
			File f = new File(newFileName);
			if(!f.exists()){
				if(f.getParentFile() != null && !f.getParentFile().exists())
					f.getParentFile().mkdirs();
			}
			f.createNewFile();
			FileWriter fw = new FileWriter(f);
			fw.write(sb.toString());
			fw.flush();
			fw.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	


	
	/**
	 * 数据是否为null
	 */
	public boolean isEmpty()
	{
		return (!(gridValArr != null && gridValArr.length>0));
	}
		
	/**
	 * 释放数据
	 */
	public void dispose() {
		if(latAry1D != null)
			latAry1D = null;
		if(latAry2D != null)
			latAry2D = null;
		if(lonAry1D != null)
			lonAry1D = null;
		if(lonAry2D != null)
			lonAry2D = null;
		
		if(gridValArr != null)
			gridValArr = null;

	}
	/**
	 * 格点数据构造函数
	 * @param xstart 开始纬度
	 * @param xend	   结束纬度
	 * @param ystart 开始经度
	 * @param yend	   结束经度
	 * @param xdel	   纬度间隔
	 * @param ydel   经度间隔
	 */
	public GridData(float xstart, float xend, float ystart, float yend, float xdel, float ydel){
		this.xstart = xstart;
		this.xend   = xend;
		this.ystart = ystart;
		this.yend   = yend;
		this.xdel   = xdel;
		this.ydel   = ydel;		
		rowNum = (int) (Math.abs(yend-ystart)/ydel + 1);		
		colNum = (int) (Math.abs(xend-xstart)/xdel + 1);
	}
	public GridData(){
		
	}
	//GRIB兼公用
	public void setXStart(float xstart){this.xstart = xstart;}
	public void setXEnd(float xend)    {this.xend = xend;}
	public void setYStart(float ystart){this.ystart = ystart;}
	public void setYEnd(float yend)    {this.yend = yend;}
	public void setXDel(float xdel)    {this.xdel = xdel;}
	public void setYDel(float ydel)    {this.ydel = ydel;}
	public void setRowNum(int rowNum)  {this.rowNum = rowNum; }
	public void setColNum(int colNum)  {this.colNum = colNum; }	
	/**
	 * 纬度信息--一维数据，当数据为等经纬网格时调用，
	 * 当数据为等距网格时，必须调用setLatAry2D获取纬度信息
	 * @param latAry1D
	 */
	public void setLatAry1D(float[] latAry1D) 		 	{ this.latAry1D = latAry1D; }	
	/**
	 * 经度信息--一维数据，当数据为等经纬网格时调用，
	 * 当数据为等距网格时，必须调用setLonAry2D获取经度信息
	 * @param lonAry1D
	 */
	public void setLonAry1D(float[] lonAry1D) 		 	{ this.lonAry1D = lonAry1D; }
	/**
	 * 纬度信息--二维数据，当数据为等经纬网格时调用，可调用setLatAry1D
	 * 当数据是等距网格时，必须取二维的纬度信息方可获取正确的经纬度信息
	 * @param latAry2D
	 */
	public void setLatAry2D(float[][] latAry2D){ 
		this.latAry2D = latAry2D; 
	}	
	/**
	 * 经度信息--二维数据，当数据为等经纬网格时调用，可调用setLonAry1D
	 * 当数据是等距网格时，必须取二维的经度信息方可获取正确的经纬度信息
	 * @param lonAry2D
	 */
	public void setLonAry2D(float[][] lonAry2D){
		this.lonAry2D = lonAry2D; 
	}
	public boolean isLatsDataChange() 			{ return isLatsDataChange; }
	public boolean isLonsDataChange() 			{ return isLonsDataChange; }
	public void setDistanceEqual(boolean isDistanceEqual)   { this.isDistanceEqual = isDistanceEqual; }	
	
	/**
	 * 获取纬度开始位置
	 * @return
	 */
	public float getXStart(){return xstart;}   
	/**
	 * 获取纬度结束位置
	 * @return
	 */
	public float getXEnd()  {return xend;}		 
	/**
	 * 获取经度开始位置
	 * @return
	 */
	public float getYStart(){return ystart;}	 
	/**
	 * 获取经度结束位置
	 * @return
	 */
	public float getYEnd()  {return yend;}		
	/**
	 * 获取纬度间隔
	 * @return
	 */
	public float getXDel()  {return xdel;}		 
	/**
	 * 获取经度间隔	
	 * @return
	 */
	public float getYDel()  {return ydel;}		 
	/**
	 * 获取纬度范围数据
	 * 范围为从开始~结束
	 * @return
	 */
	public float[] getLatAry1D() 		  { return latAry1D; }
	/**
	 * 获取经度范围数据
	 * 范围为从开始~结束
	 * @return
	 */
	public float[] getLonAry1D() 		  { return lonAry1D; }
	/**
	 * 获取经度范围数据
	 * 二维数组，即网格大小与数据一一对应，即同一索引的经度对应同一索引的数据
	 * @return
	 */
	public float[][] getLatAry2D() 		  { 
		if(latAry2D != null)
			return latAry2D;
		if(this.rowNum > 0 && this.colNum > 0 && latAry1D != null && latAry1D.length > 0){
			float[][] latAry = new float[rowNum][colNum];
			for(int cl=0;cl<colNum;cl++){
				for(int r=0;r<rowNum;r++){						
					latAry[r][cl] = latAry1D[r];
				}
			}
			this.latAry2D = latAry;
		}
		return latAry2D;
	}
		
	/**
	 * 获取纬度范围数据
	 * 二维数组，即网格大小与数据一一对应，即同一索引的纬度对应同一索引的数据
	 * @return
	 */
	public float[][] getLonAry2D() 		  {
		if(lonAry2D != null)
			return lonAry2D;
		if(this.rowNum > 0 && this.colNum > 0 && latAry1D != null && lonAry1D.length > 0){
			float[][] lonAry = new float[rowNum][colNum];
			for(int r=0;r<rowNum;r++){
				for(int cl=0;cl<colNum;cl++){
					lonAry[r][cl] = lonAry1D[cl];
				}
			}
			this.lonAry2D = lonAry;
		}
		return lonAry2D;
	}
	/**
	 * 设置经纬度信息是否发生变化
	 * @param isLatsDataChange
	 */
	public void setLatsDataChange(boolean isLatsDataChange) { this.isLatsDataChange = isLatsDataChange; }
	/**
	 * 经纬度信息是否发生变化
	 * 即获取的经纬度信息是否与设置的经纬度信息保持一致
	 * @param isLonsDataChange true发生变化 false没有变化
	 */
	public void setLonsDataChange(boolean isLonsDataChange) { this.isLonsDataChange = isLonsDataChange; }
	/**
	 * 是否等距离网格
	 * @return true等距网格 false等经纬网格
	 */
	public boolean isDistanceEqual()  { return isDistanceEqual; }
	
	/**
	 * 处理GridData 使之数据在正常经纬度范围内
	 * 即经纬度的为-180~180 -90~90
	 * @return
	 */
	public GridData filterInvalid(){
		if(this.getYStart() >= -90 && this.getYStart() <= 90 
				&& this.getYEnd() >= -90 && this.getYEnd() <= 90
				&& this.getYEnd() >= this.getYStart()
				&& this.getXStart() >= -180 && this.getXStart() <= 180
				&& this.getXEnd() >= -180 && this.getXEnd() <= 180
				&& this.getXEnd() >= this.getXStart())
			return this;
		
		boolean isDefinedRowColCnt = false;
		
		if(this.getYStart() < -90 || this.getYStart() > 90 
				|| this.getYEnd() < -90 || this.getYEnd() > 90
				|| this.getXStart() < -180 && this.getXStart() > 180
				|| this.getXEnd() < -180 && this.getXEnd() > 180)
			isDefinedRowColCnt = true;
		
		int removeRowCnt = 0;
		int removeColCnt = 0;
		
		if(isDefinedRowColCnt){
			for(int i=0;i<this.getLatAry2D().length;i++){
				if(this.getLatAry2D()[i][0] < -90 || this.getLatAry2D()[i][0] > 90)
					removeRowCnt ++;
			}
			for(int i=0;i<this.getLonAry2D()[0].length;i++){
				if(this.getLonAry2D()[0][i] < -180 || this.getLonAry2D()[0][i] > 360 )
					removeColCnt ++;
			}
		}
		
		if(removeRowCnt >0 || removeColCnt > 0){
			lonAry2D = new float[rowNum - removeRowCnt][colNum - removeColCnt];			
			latAry2D = new float[rowNum - removeRowCnt][colNum - removeColCnt];			
			this.setRowNum(rowNum - removeRowCnt);
			this.setColNum(colNum - removeColCnt);
		}
		for(int r=0;r<this.getRowNum();r++){
			for(int cl=0;cl<this.getColNum();cl++){
				if(this.getLonAry1D()[cl]>180 && this.getLonAry1D()[cl]<=360)
					lonAry2D[r][cl] = this.getLonAry1D()[cl] - 360 ;
				else if(this.getLonAry1D()[cl]<=180)
					lonAry2D[r][cl] = this.getLonAry1D()[cl];
				else{
				}
			}
		}
		this.setXEnd(lonAry2D[lonAry2D.length - 1][lonAry2D[lonAry2D.length - 1].length - 1]);
		for(int cl=0;cl<this.getColNum();cl++){
			for(int r=0;r<this.getRowNum();r++){
				if(this.getLatAry1D()[r] >= -90 && this.getLatAry1D()[r] <= 90){
					latAry2D[r][cl] = this.getLatAry1D()[r];
				}else{
				}
			}
		}
		this.setYEnd(latAry2D[latAry2D.length - 1][latAry2D[latAry2D.length - 1].length  - 1]);
		if(removeRowCnt >0 || removeColCnt > 0){
			float[][] dataAry = new float[rowNum][colNum]; 
			for(int r=0;r<this.getRowNum();r++){
				for(int cl=0;cl<this.getColNum();cl++){
					dataAry[r][cl] = gridValArr[r][cl];
				}
			}
			gridValArr = dataAry;
		}
		return this;
	}
	
	

	/**
	 * 获取网格数据行数
	 * @return
	 */
	public int getRowNum(){
		return rowNum;
	}
	/**
	 * 获取网格数据列数
	 * @return
	 */
	public int getColNum(){
		return colNum;
	}
	
	/**
	 * 设置网格数据
	 * @param gridValArr
	 * @return
	 */
	public boolean setGridData(float[][] gridValArr){
//		assert(gridValArr.length*gridValArr[0].length == rowNum*colNum);
		if (gridValArr.length*gridValArr[0].length != rowNum*colNum)		{			
			System.out.println("GridData中的方法setGridData里gridValArr*gridValArr[0].length != rowNum*colNum");
			return false;
		}
		this.gridValArr = gridValArr;
		return true;		
	}
	/**
	 * 获取网格数据
	 * @return
	 */
	public float[][] getGridData(){
		return gridValArr;
	}
	
	/**
	 * 按照给定的经纬度查找该经纬度位置所在网格的行列索引.如果没有查找到，返回[-1, -1]
	 * @param latlon
	 * @return
	 */
	public int[] getLatLonIndex(LatLonPoint latlon){
		int rowIndex = -1;
		int colIndex = -1;
		int[] indexs = new int[]{rowIndex,colIndex};
		try{
			if(latlon == null){
				return indexs;
			}
			if(this.latAry1D == null && this.latAry2D == null)
				return indexs;
			if(this.lonAry1D == null && this.lonAry2D == null)
				return indexs;
			if(this.latAry1D == null && this.latAry2D != null){
				if(this.lonAry2D.length > 0){
					this.latAry1D = new float[this.lonAry2D.length];
					for(int i=0;i<this.lonAry2D.length;i++){
						this.latAry1D[i] = this.lonAry2D[i][0];
					}
				}else
					return indexs;
			}
			if(this.lonAry1D == null && this.lonAry2D != null){
				if(this.lonAry2D.length > 0)
					this.lonAry1D = this.lonAry2D[0];
				else
					return indexs;
			}
			for(int i=0;i<latAry1D.length - 1;i++){
				if((latlon.getLatitude() - latAry1D[i + 1]) * (latlon.getLatitude() - latAry1D[i]) <= 0) {
					if (Math.abs(latlon.getLatitude() - latAry1D[i + 1]) > Math.abs(latlon.getLatitude() - latAry1D[i])) {
						rowIndex = i;
					} else {
						rowIndex = i + 1;
					}
					break;
				}
			}
			for(int i=0;i<lonAry1D.length - 1;i++){
				if((latlon.getLongitude() - lonAry1D[i + 1]) * (latlon.getLongitude() - lonAry1D[i]) <= 0){
					if (Math.abs(latlon.getLongitude() - lonAry1D[i + 1]) > Math.abs(latlon.getLongitude() - lonAry1D[i])) {
						colIndex = i;
					} else {
						colIndex = i + 1;
					}
					break;
				}
			}
			if(rowIndex >=0 && colIndex >= 0){
				indexs[0] = rowIndex;
				indexs[1] = colIndex;
			}
			return indexs;
		}catch(Exception e){
			return indexs;
		}
	}
	/**
	 * 将GridData中的值限制在minValue~maxValue之间
	 * @param minValue
	 * @param maxValue
	 */
	public void limiteValue(float minValue, float maxValue) {
		int row = this.getRowNum();
		int col = this.getColNum();
		float[][] values = this.getGridData();
		float[][] newValues = new float[row][col];
		for (int r = 0; r < row; r++) {
			for (int c = 0; c < col; c++) {
				if (values[r][c] > maxValue) {
					newValues[r][c] = maxValue;
				} else if (values[r][c] < minValue) {
					newValues[r][c] = minValue;
				} 
			}
		}
		this.setGridData(newValues);
	}
	
	
	/**
	 * {@inheritDoc }
	 * JSONObject对象存储格式如下：
	 * isDistanceEqual 是否为等距数据 
	 * row 行数 	 col 列数
	 * xStart 开始经度 	xEnd 结束经度 	xDel 经度间隔
	 * yStart 开始纬度 	yEnd 结束纬度 	yDel 纬度间隔
	 * isLonsDataChange 经度信息是否改变	isLatsDataChange纬度信息是否改变
	 * lonData 经度数据	latData 纬度数据（isDistanceEqual为true时数据为二维数组float[][]类型，否则为一维数组float[]类型）
	 * gridData 格点数据（二维数组,float[][]类型）
	 */
	public JSONObject dataToJSON() {
		JSONArray array = new JSONArray();
		JSONObject obj = new JSONObject();
		if(gridValArr==null || gridValArr.length==0 || this.isEmpty()){
			return obj;
		}

		obj.put("isDistanceEqual", this.isDistanceEqual());
		
		obj.put("row", this.getRowNum());
		obj.put("col", this.getColNum());
		
		obj.put("xStart", this.getXStart());
		obj.put("xEnd", this.getXEnd());
		obj.put("xDel", this.getXDel());
		
		obj.put("yStart", this.getYStart());
		obj.put("yEnd", this.getYEnd());
		obj.put("yDel", this.getYDel());
		
		obj.put("isLonsDataChange", this.isLonsDataChange());
		obj.put("isLatsDataChange", this.isLatsDataChange());
		
		if(this.isDistanceEqual()){
			obj.put("lonData", this.getLonAry2D());
			obj.put("latData", this.getLatAry2D());
		}else{
			obj.put("lonData", this.getLonAry1D());
			obj.put("latData", this.getLatAry1D());
		}
		obj.put("gridData", this.getGridData());
		array.add(obj);
		return obj;
	}
	
	/**
	 * 过滤GridData的二维数据
	 * @param minValue 最小值下限，当值为DataTypeConst.NULLVAL(-99999.9f)为表示不过滤最小值
	 * @param maxValue 最大值上限，当值为DataTypeConst.NULLVAL(-99999.9f)为表示不过滤最大值
	 * @return
	 */
	public GridData getFiltrateData(float minValue, float maxValue){
		GridData gridData = this.clone();
		if(gridData == null || gridData.getGridData() == null || gridValArr.length < 1)
			return gridData;
		if(minValue == DataTypeConst.NULLVAL && maxValue == DataTypeConst.NULLVAL)
			return gridData;
		float[][] newDataAry = new float[gridData.rowNum][gridData.colNum];
		if(minValue != DataTypeConst.NULLVAL && maxValue != DataTypeConst.NULLVAL){
			for(int i=0;i<gridData.rowNum;i++){
				for(int j=0;j<gridData.colNum;j++){
					if(gridData.getGridData()[i][j] >= minValue && gridData.getGridData()[i][j] <= maxValue){
						newDataAry[i][j] = gridData.getGridData()[i][j];
					}else{
						newDataAry[i][j] = DataTypeConst.NULLVAL;
					}
				}
			}
		}
		if(minValue != DataTypeConst.NULLVAL && maxValue == DataTypeConst.NULLVAL){
			for(int i=0;i<gridData.rowNum;i++){
				for(int j=0;j<gridData.colNum;j++){
					if(gridData.getGridData()[i][j] >= minValue){
						newDataAry[i][j] = gridData.getGridData()[i][j];
					}else{
						newDataAry[i][j] = DataTypeConst.NULLVAL;
					}
				}
			}
		}
		if(minValue == DataTypeConst.NULLVAL && maxValue != DataTypeConst.NULLVAL){
			for(int i=0;i<gridData.rowNum;i++){
				for(int j=0;j<gridData.colNum;j++){
					if(gridData.getGridData()[i][j] <= maxValue){
						newDataAry[i][j] = gridData.getGridData()[i][j];
					}else{
						newDataAry[i][j] = DataTypeConst.NULLVAL;
					}
				}
			}
		}
		gridData.setGridData(newDataAry);
		return gridData;
	}



	
}




