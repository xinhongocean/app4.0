package com.xinhong.mids3d.datareader.util;

/**
 * 等距网格信息
 * @author SJN
 *
 */
public class DEInfo {
	public DEInfo(){
		
	}
		private float[][] latAry;
		private float[][] lonAry;
		
		private float[] lats;
		private float[] lons;
		private float[][] latLonDepth;
		
		private int rowNum = 0;
		private int colNum = 0;
		private boolean isDE = true;
		private float xdel = 0;
		private float ydel = 0;
		
		private boolean isNeedConversion = false;
		/**
		 * lonAry范围是否是从0~360
		 * @return
		 */
		public boolean IsNeedConversion(){
			return  isNeedConversion;
		}
		/**
		 * 获取纬度数据
		 * @return
		 */
		public float[][] getLatAry() 	{		return latAry;	}
		/**
		 * 获取经度数据
		 * @return
		 */
		public float[][] getLonAry() 	{		return lonAry;	}
		/**
		 * 获取行数
		 * @return
		 */
		public int getRowNum() 			{		return rowNum;	}
		/**
		 * 获取列数
		 * @return
		 */
		public int getColNum() 			{		return colNum;	}
		/**
		 * 是否等距网格
		 * @return
		 */
		public boolean isDE() 			{		return isDE;	}
		/**
		 * 获取经度间隔
		 * @return
		 */
		public float getXdel() 			{		return xdel;	}
		/**
		 * 获取纬度间隔
		 * @return
		 */
		public float getYdel() 			{		return ydel;	}
		
		/**
		 * 设置纬度数据
		 * @param latAry
		 */
		public void setLatAry(float[][] latAry) 	{		this.latAry = latAry;	}	
		/**
		 * 设置经度数据
		 * @param lonAry
		 */
		public void setLonAry(float[][] lonAry) 	{		
			this.lonAry = lonAry;	
			if(lonAry != null && lonAry.length > 0){
				if(lonAry[0][0] > 180 || lonAry[lonAry.length - 1][lonAry[0].length - 1] > 180)
					isNeedConversion = true;
			}
		}	
		
		public float[] getLats() {
			return lats;
		}
		public void setLats(float[] lats) {
			this.lats = lats;
			if(lats != null && lats.length > 0){
				if(lats[0] > 180 || lats[lats.length - 1] > 180)
					isNeedConversion = true;
			}
			
		}
		public float[] getLons() {
			return lons;
		}
		public void setLons(float[] lons) {
			this.lons = lons;
		}
		public float[][] getLatLonDepth() {
			return latLonDepth;
		}
		public void setLatLonDepth(float[][] latLonDepth) {
			this.latLonDepth = latLonDepth;
		}
		/**
		 * 设置行数
		 * @param rowNum
		 */
		public void setRowNum(int rowNum) 			{		this.rowNum = rowNum;	}	
		/**
		 * 设置列数
		 * @param colNum
		 */
		public void setColNum(int colNum) 			{		this.colNum = colNum;	}	
		/**
		 * 设置是否等距网格
		 * @param isDE
		 */
		public void setDE(boolean isDE) 			{		this.isDE = isDE;	}		
		/**
		 * 设置经度间隔
		 * @param xdel
		 */
		public void setXdel(float xdel)				{		this.xdel = xdel;	}	
		/**
		 * 设置纬度间隔
		 * @param ydel
		 */
		public void setYdel(float ydel) 			{		this.ydel = ydel;	}
		
	}
