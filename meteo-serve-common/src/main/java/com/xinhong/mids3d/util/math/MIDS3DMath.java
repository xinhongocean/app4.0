package com.xinhong.mids3d.util.math;

import com.xinhong.mids3d.datareader.util.DataType;
import com.xinhong.mids3d.util.math.trianglation.Pnt;
import com.xinhong.mids3d.util.math.trianglation.Triangle;
import com.xinhong.mids3d.util.math.trianglation.Triangulation;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import net.xinhong.meteoserve.common.grib.GridData;
import net.xinhong.meteoserve.common.tool.LatLonPoint;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.*;

import static net.xinhong.meteoserve.common.constant.DataTypeConst.NULLVAL;

public final class MIDS3DMath {
	private MIDS3DMath(){}
	
	/**
	 * 利用叉积计算pt3是否在pt1和pt2相连的线段上
	 * @param startPt 起始点
	 * @param endPt 结束点
	 * @param midPt 判断点
	 * @return true存在，false不存在
	 */
	public static <T extends Point2D>boolean isOnLine(T startPt, T endPt, T midPt){
		if(midPt.getX()>=Math.min(startPt.getX(),endPt.getX()) && midPt.getX()<=Math.max(startPt.getX(), endPt.getX()) && //先判断在矩形内避免在延长线上
		   midPt.getY()>=Math.min(startPt.getY(),endPt.getY()) && midPt.getY()<=Math.max(startPt.getY(), endPt.getY())){
			if(Math.abs((startPt.getX()-midPt.getX())*(endPt.getY()-midPt.getY())-(endPt.getX()-midPt.getX())*(startPt.getY()-midPt.getY()))<=1e-6){
				return true;
			}
		}
		//精度问题
		return false;
	}
	
	/**
	 * point是否在边上
	 * @param datax 边上点的x坐标 
	 * @param datay 边上点的y坐标
	 * @param xory 0：x边 1：y边
	 * @param point 点
	 * @return
	 */
	public static Boolean isOnArraySide(float[] datax, float[] datay, int xory, Point2D.Float point){
		int col = 0;
		if(xory==0){
			col = StrArrayUtil.inArrayCol(datax, point.x);
		}else if(xory==1){
			col = StrArrayUtil.inArrayCol(datay, point.y);
		}else{
			return null;
		}
		if(col==99999){
			//判断是否在边上
			if(datax.length!=datay.length){
				System.out.println("x和y的个数不同，无法判断");
				return null;
			}
			int size = datax.length;
			for(int i=0; i<size-1; i++){
				Point2D.Float pt1 = new Point2D.Float();
				pt1.x = datax[i];
				pt1.y = datay[i];
				Point2D.Float pt2 = new Point2D.Float();
				pt2.x = datax[i+1];
				pt2.y = datay[i+1];
				if(isOnLine(pt1,pt2,point)){
					return true;
				}
			}
			return false;
		}else{
			return true;
		}
	}
	/**
	 * 离散数据网格化
	 * @param data 离散数据  \\相当于value值吧（db x）
	 * @param lon 离散数据的x坐标(经度)
	 * @param lat 离散数据的y坐标(纬度)
	 * @param startX 网格的起始x坐标(经度)
	 * @param endX 网格的终止x坐标(经度)
	 * @param startY 网格的起始y坐标(纬度)
	 * @param endY 网格的终止y坐标(纬度)
	 * @param deltX 网格的x方向上(经度)的间隔
	 * @param deltY 网格的y方向上(纬度)的间隔
	 * @param minVal 质量控制的最小值
	 * @param maxVal 质量控制的最大值
	 * @param distR1 距离用于校正非正常数据
	 * @param dataR3 参数用于校正非正常数据
	 * @param validDataR 有效数据半径用于初步网格化
	 * @param gsR 用于调整高斯函数的分母大小
	 * @param dataType 数据类型，高空还是地面
	 * @param elem 要素(主要限制降水的平滑)
	 * @return 网格化好的格点数据
	 */
	public static GridData scatterToGrid(float[] data, float[] lon,
			float[] lat, float startX, float endX, float startY, float endY,
			float deltX, float deltY, float minVal, float maxVal, float distR1,
			float dataR3, float validDataR, float gsR, DataType dataType,
			String elem) {
		if (data.length != lat.length) {
			System.out.println("In ChangeScatterToGrid中scatterToGrid()的data.length()!=lat.length,纬度和数据不一致");
			return null;
		} else if (lat.length != lon.length) {
			System.out.println("In ChangeScatterToGrid中scatterToGrid()的lat.length != lon.length,经纬度不一致");
			return null;
		} else if (data.length != lon.length) {
			System.out.println("In ChangeScatterToGrid中scatterToGrid()的data.length != lon.length,经度和数据不一致");
			return null;
		}

		int sdNum = data.length;
		int rows = (int) (Math.abs((endY - startY)) / deltY + 1);
		int cols = (int) (Math.abs((endX - startX)) / deltX + 1);
		float deltYSign;
		float deltXSign;
		if (endY > startY) {
			deltYSign = deltY;
		} else {
			deltYSign = -1 * deltY;
		}
		if (endX > startX) {
			deltXSign = deltX;
		} else {
			deltXSign = -1 * deltX;
		}
		
		boolean isRain = elem.toUpperCase().startsWith("RAIN");
		
		if (isRain) {
			int num = 0;
			for (int i = 0; i < sdNum; i++) {
				if (data[i] < 0.01f) {
					data[i] = 0.0f;
				} else {
					num++;
				}
			}
			if (num == 0) {
				System.out.println("离散数据网格化时,降水量都小于0.01");
				return null;
			}
		} else {
			// 极值检验
			for (int i = 0; i < sdNum; i++) {
				if (data[i] > maxVal) {
					data[i] = maxVal;
				} else if (data[i] < minVal) {
					data[i] = minVal;
				}
			}
		}

		long ss = System.currentTimeMillis();
		// 校正非正常数据
		// 对取到的八个方向数据点进行求值：
		// D = ( Sum(Di) * (R1-R2)/R1+R2 )/ Sum((R1-R2)/(R1+R2))
		float[] resData = new float[data.length];

		if (isRain) {
			// 降水不需要校正非正常数据
			resData = data;
		} else if (dataType == null) {
			resData = data;
		} else {
			resData = data;
		}
//		else if (dataType.equals(DataType.DMQX)) {
//			resData = data;
////			resData = checkUnnormalData(data, lat, lon, sdNum, distR1, dataR3);
//		} else if (dataType.equals(DataType.GKQX)) {
////			resData = checkUnnormalData(data, lat, lon, sdNum, distR1, dataR3);
//			resData = data;
//		} else if (dataType.equals(DataType.ClimateStat)) {
//			resData = data;
////			resData = checkUnnormalData(data, lat, lon, sdNum, distR1, dataR3);
//		} else {
//			System.out.println("In MIDS3DMath.scatterToGrid()中输入的数据类型不符合条件,不是DMQX也不是GKQX");
//			return null;
//		}

		if (resData.length <= 1) {
			return null;
		}
		System.out.println("八方向：" + (System.currentTimeMillis() - ss));

		// 网格化 插值部分网格
		// SW = Sum(Di*exp(-Ri*Ri/(2*gsR*(delX+delY))))
		// W = Sum(exp(-Ri*Ri/(2*gsR*(delX+delY))))
		// if(w>0.0000001) then D = SW/W
		long s1 = System.currentTimeMillis();
		GridDataFlagInfo gridFlagData = new GridDataFlagInfo(rows, cols);
		float swnew = 0.0f, wnew = 0.0f;
		float sw = 0.0f, w = 0.0f;
		float dd = 0.0f;//在这里 dd表示两点间欧式距离的平方 （db x）********
		boolean flagDistance = false;//理解为可否进行插值的判断（db x）********
		float xd = 0;//插值点到已知点在x方向的距离（db x）********
		float yd = 0;//插值点到已知点在y方向的距离（db x）********
		int nullNum = 0;
		if (isRain) {
			ArrayList<Integer> index = new ArrayList<Integer>();
			ArrayList<Float> dist = new ArrayList<Float>();
			// float rr = 2*gsR*(deltX + deltY);
			float rr = 4.0f * 0.4f * (deltX + deltY) / 2;
			validDataR = 0.8f;//控制条件，方法参数（db x）********
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					flagDistance = false;
					index = new ArrayList<Integer>();
					dist = new ArrayList<Float>();
					sw = 0.0f;
					w = 0.0f;
					gridFlagData.setFlag(i, j, false);
					// gridFlagData.setGridData(i, j, -9999.9f);
					gridFlagData.setGridData(i, j, 0.0f);
					gridFlagData.setSplineTimes(i, j, 0);
					gridFlagData.setSplineRate(i, j, 0);//初始化带标记的网格 为了方便检验插值情况 最后再转给GridDate. （db x）********    
					float mindd = 9999f;
					//对于网格点varrData[i][j] 的经纬度坐标（(startX + j * deltXSign), (startY + i * deltYSign)） （db x）********
					//遍历所有已知数据点（lon[k],lat[k]） ,查找距离xd yd 满足限制的范围条件validDataR的data[k]进行插值（db x）********
					for (int k = 0; k < sdNum; k++) {
						xd = Math.abs(lon[k] - (startX + j * deltXSign));
						yd = Math.abs(lat[k] - (startY + i * deltYSign));

						if (xd > Math.abs(validDataR) || yd > Math.abs(validDataR)) {
							continue;
						}
						dd = xd * xd + yd * yd;//插值点到已知点的欧氏距离平方（db x）********
						if (dd < 0.0f || dd >= validDataR * validDataR) {//dd肯定大于零 第一个判断条件可以舍去（db x）********
							continue;
						}
						if(mindd>dd){//控制最大距离（db x）********
							mindd = dd;
						}
						if (dd > 0.0f && dd < validDataR * validDataR) {
							flagDistance = true;
							if (resData[k] > 0.0f) {
								index.add(k);//满足插值条件的已知数据临时储存在index和 dist中等待插值计算（db x）********
								dist.add(dd);
							}
						} else if (dd == 0.0f) { //首先float相等不能这么表示（bug）,这里想说明如果待插值点正好是已知点（lon[k],lat[k]）那么直接赋值data[k]（db x）********
							gridFlagData.setGridData(i, j, resData[k]);
							gridFlagData.setFlag(i, j, true);
							gridFlagData.setSplineTimes(i, j, 1);//第一次插值得到，后面插值用到时此点的权重应该高点儿
							gridFlagData.setSplineRate(i, j, 1.5f);//最高1.5倍
//							gridFlagData.setSplineRate(i, j, 2f);//最高2倍
							break;
						}
					}// endfor k
					if (index.isEmpty() && flagDistance) {
						gridFlagData.setGridData(i, j, 0.0f);
						gridFlagData.setFlag(i, j, true);
						gridFlagData.setSplineTimes(i,j,1);//第一次插值得到
						if(mindd!=9999 && validDataR!=0){
							float r = getSplineRate(mindd, validDataR);
							gridFlagData.setSplineRate(i, j, r);
						}
					} else if (!index.isEmpty()) {
						float[] val = new float[dist.size()];
						for (int k = 0; k < val.length; k++) {
							val[k] = dist.get(k);
						}
						Collections.sort(dist);//排序，取最近的前三个  //该插值法是取最近三个点进行插值（db x）********
						int kk = 0;
						for (int k = 0; k < val.length; k++) {
							if (kk == 3 || kk == dist.size()) {
								break;
							}
							if (kk < dist.size() && kk < 3) {
								if (dist.get(kk) == val[k]) {									
//									float gsFun = (float) Math.exp((-1) * dist.get(kk) / rr);
									float gsFun = (float) ((-1) * Math.log(dist.get(kk) / rr));
									sw = sw + resData[index.get(k)] * gsFun;
									w = w + gsFun;
									kk++;
									flagDistance = true;
									k = -1;
								}
							}
						}
						if (!gridFlagData.getFlag(i, j)) {
							if (flagDistance) {
								// if (w > 0.0000001f) {
								if (w > (float) 1E-7) {
									gridFlagData.setGridData(i, j, sw / w);
									gridFlagData.setFlag(i, j, true);
									gridFlagData.setSplineTimes(i, j, 1);//第一次插值得到
									if(mindd!=9999 && validDataR!=0){
										float r = getSplineRate(mindd, validDataR);
										gridFlagData.setSplineRate(i, j, r);
									}
								}
							}
						}// endif
					}
				}// endfor j
			}// endfor i
		} else {
			// float rr = 2*gsR*(deltX + deltY);
			float rr = 4.5f * gsR * (deltX + deltY) / 2;
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					flagDistance = false;
					swnew = 0.0f;
					wnew = 0.0f;
//					sw = 0.0f;
//					w = 0.0f;
					gridFlagData.setFlag(i, j, false);
					gridFlagData.setGridData(i, j, Float.NaN);
					gridFlagData.setSplineTimes(i, j, 0);
					gridFlagData.setSplineRate(i, j, 0);
					float mindd = 9999f;
					for (int k = 0; k < sdNum; k++) {
						xd = Math.abs(lon[k] - (startX + j * deltXSign));
						yd = Math.abs(lat[k] - (startY + i * deltYSign));

						if (xd > Math.abs(validDataR) || yd > Math.abs(validDataR)) {
							continue;
						}
						dd = xd * xd + yd * yd;
						if (dd < 0.0f || dd >= validDataR * validDataR) {
							continue;
						}
						if (mindd>dd){
							mindd = dd;
						}
						if (dd > 0.0f && dd < validDataR * validDataR) {
//							float gsFun = (float) Math.exp((-1) * dd / rr);
							float gsFunnew = (float) ((-1) * Math.log(Math.sqrt(dd) / rr));
							if(gsFunnew<0){
								continue;
							}
							swnew = swnew + resData[k] * gsFunnew;
							wnew = wnew + gsFunnew;
//							sw = sw + resData[k] * gsFun;
//							w = w + gsFun;
//							System.out.println("gsFunold = " + gsFun + ", gsFunnew = " + gsFunnew + 
//									", swold=" + sw + ", wold=" + w + ", swnew=" + swnew  + ", wnew="+wnew+
//									", oldval = " + sw/w  +", newval=" + swnew/wnew);
//							System.out.println("realVal =" + resData[k] + ", swnew=" + swnew + ", wnew="+ wnew + ", gsFunnew = " + gsFunnew + ", dd = " + dd );
							flagDistance = true;
						} else if (dd == 0.0f) {//在此依然不适合用==应该用|dd|<10e-5 这里dd是欧式距离的平方可以去掉绝对值（db x）********
//						} else if (dd<=validDataR*validDataR*0.0625 && dd>=0) {
							gridFlagData.setGridData(i, j, resData[k]);
							gridFlagData.setFlag(i, j, true);
							gridFlagData.setSplineTimes(i, j, 1);//第一次插值得到
							gridFlagData.setSplineRate(i, j, 1.5f);
//							gridFlagData.setSplineRate(i, j, 2f);
							break;
						}
					}// endfor k
					
					if (!gridFlagData.getFlag(i, j)) {
						if (flagDistance) {
							// if (w > 0.0000001f) {
							if (wnew > (float) 1E-7) {
//								gridFlagData.setGridData(i, j, sw / w);
								gridFlagData.setGridData(i, j, swnew / wnew);
								gridFlagData.setFlag(i, j, true);
								gridFlagData.setSplineTimes(i, j, 1);//第一次插值得到
								if(mindd!=9999 && validDataR!=0){
									float r = getSplineRate(mindd, validDataR);
									gridFlagData.setSplineRate(i, j, r);
								}
							}
						}
					}// endif

					// 统计gridFlagData共有多少空的
					if (!gridFlagData.getFlag(i, j)) {
						nullNum++;
					}

				}// endfor j
			}// endfor i
		}//分两种情况（isRain）插值部分已经完成（db x）********
		
		System.out.println("网格化部分：" + (System.currentTimeMillis() - s1));
		if (endX != (startX + (cols - 1) * deltXSign)) {
			endX = startX + (cols - 1) * deltXSign;
		}
		if (endY != (startY + (rows - 1) * deltYSign)) {
			endY = startY + (rows - 1) * deltYSign;
		}
		GridData gridData = new GridData();
		gridData.setXStart(startX);
		gridData.setXEnd(endX);
		gridData.setYStart(startY);
		gridData.setYEnd(endY);
		gridData.setXDel(deltX);
		gridData.setYDel(deltY);
		gridData.setColNum(cols);
		gridData.setRowNum(rows);		

		if (isRain) {
			try {
				gridData.setGridData(gridFlagData.getGridData());
				float[][] lonAry = StrArrayUtil.getArrFromSERowCol(startX, endX, deltX, gridData.getRowNum(), gridData.getColNum(), true);
				float[][] latAry = StrArrayUtil.getArrFromSERowCol(startY, endY, deltY, gridData.getRowNum(), gridData.getColNum(), false);
				gridData.setLonAry2D(lonAry);
				gridData.setLatAry2D(latAry);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return gridData;
		}
		float num;
		// float sum;
		double sum;
		int gDataFlag = 1;//为了检验是否格点全部插值完成（db x）
		int splineTimes = 1;
		
		long tickS = System.currentTimeMillis();
		long tickE;
		float bfRate = 3;//第一次插值时 B、F两点的权重比例（db x）
		float diRate = 2;
		float cehjRate = 1.5f;
		float agRate = 0.2f;
		float totalRate = 2*bfRate + 2*diRate + 4*cehjRate + 2*agRate;
		float multiple = 1.1f;//第二次放大倍数
		long tt = System.currentTimeMillis();
		while (gDataFlag > 0 && ( System.currentTimeMillis() - tt) < 10000) {
			GridDataFlagInfo gridFlagDataTemp = gridFlagData.clone();
			gDataFlag = 0;
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
//					System.out.print("old = " + gridFlagData.getGridData(i, j));
					if (!gridFlagData.getFlag(i, j)) {
						num = 0.0f;
						sum = 0.0f;
						//需要判断BDFI四个点是不都为空，都为空时不插值
						if ((i == 0 && j == 0 && !gridFlagDataTemp.getFlag(1, 0) && !gridFlagDataTemp.getFlag(0, 1))// 左下角或左上角
					    || (i == 0 && j == cols - 1 && !gridFlagDataTemp.getFlag(0, cols - 2)  && !gridFlagDataTemp.getFlag(1, cols - 1))// 右下角或右上角
						|| (i == rows - 1 && j == 0 && !gridFlagDataTemp.getFlag(rows - 1, 1) && !gridFlagDataTemp.getFlag(rows - 2, 0))// 左上角或左下角
						|| (i == rows - 1 && j == cols - 1 && !gridFlagDataTemp.getFlag(rows - 1, cols - 2) && !gridFlagDataTemp.getFlag(rows - 2, cols - 1))// 右上角或右下角
						|| (i == 0 && j != 0 && j != cols - 1 && !gridFlagDataTemp.getFlag(0, j - 1) && !gridFlagDataTemp.getFlag(1, j) && !gridFlagDataTemp.getFlag(0, j + 1))// 下边框或上边框
						|| (i != 0 && i != rows - 1 && j == cols - 1 && !gridFlagDataTemp.getFlag(i, j - 1) && !gridFlagDataTemp.getFlag(i + 1, j) && !gridFlagDataTemp.getFlag(i - 1, j))// 右边框
						|| (i == rows - 1 && j != cols - 1 && j != 0 && !gridFlagDataTemp.getFlag(i, j - 1) && !gridFlagDataTemp.getFlag(i, j + 1)  && !gridFlagDataTemp.getFlag(i - 1, j))// 上边框或下边框
						|| (i != 0 && i != rows - 1 && j == 0 && !gridFlagDataTemp.getFlag(i, j + 1) && !gridFlagDataTemp.getFlag(i + 1, j) && !gridFlagDataTemp.getFlag(i - 1, j))// 左边框
					    || (!(i == 0 || j == 0 || i == rows - 1 || j == cols - 1) && !gridFlagDataTemp.getFlag(i, j + 1) 
					    	&& !gridFlagDataTemp.getFlag(i, j - 1) && !gridFlagDataTemp.getFlag(i + 1, j) && !gridFlagDataTemp.getFlag(i - 1, j))) {// 中间
							gridFlagData.setGridData(i, j, Float.NaN);
							gridFlagData.setFlag(i, j, false);
						} else {
							int tmpTimes = 0;
							float rate = 0;
							if (j - 1 >= 0 && gridFlagDataTemp.getFlag(i, j - 1)) {// B点
								tmpTimes = gridFlagDataTemp.getSplineTimes(i, j-1);
								rate = bfRate;
								if(tmpTimes==1){//第一次插值得到
									rate = gridFlagDataTemp.getSplineRate(i,j-1)*rate;//权重放大倍数
								}else if(tmpTimes==2){//第二次插值得到
									rate = multiple*rate;//权重放大1.1倍
								}
								num = num + rate;
								sum = sum + gridFlagData.getGridData(i, j - 1) * rate;
							}
							if (j - 2 >= 0 && gridFlagDataTemp.getFlag(i, j - 2)) {// A点
								tmpTimes = gridFlagDataTemp.getSplineTimes(i, j-2);
								rate = agRate;
								if(tmpTimes==1){
									rate = gridFlagDataTemp.getSplineRate(i,j-2)*rate;
								}else if(tmpTimes==2){
									rate = multiple*rate;
								}
								num = num + rate;
								sum = sum + gridFlagData.getGridData(i, j - 2)*rate;
							}
							if (j + 1 <= cols - 1 && gridFlagDataTemp.getFlag(i, j + 1)) {// F点
								tmpTimes = gridFlagDataTemp.getSplineTimes(i, j+1);
								rate = bfRate;
								if(tmpTimes==1){
									rate = gridFlagDataTemp.getSplineRate(i,j+1)*rate;
								}else if(tmpTimes==2){
									rate = multiple*rate;
								}
								num = num + rate;
								sum = sum + gridFlagData.getGridData(i, j + 1) * rate;
							}
							if (j + 2 <= cols - 1 && gridFlagDataTemp.getFlag(i, j + 2)) {// G点
								tmpTimes = gridFlagDataTemp.getSplineTimes(i, j+2);
								rate = agRate;
								if(tmpTimes==1){
									rate = gridFlagDataTemp.getSplineRate(i,j+2)*rate;
								}else if(tmpTimes==2){
									rate = multiple*rate;
								}
								num = num + rate;
								sum = sum + gridFlagData.getGridData(i, j + 2)*rate;
							}
							if (i + 1 <= rows - 1 && j - 1 >= 0 && gridFlagDataTemp.getFlag(i + 1, j - 1)) {// C点或H
								tmpTimes = gridFlagDataTemp.getSplineTimes(i+1, j-1);
								rate = cehjRate;
								if(tmpTimes==1){
									rate = gridFlagDataTemp.getSplineRate(i+1,j-1)*rate;
								}else if(tmpTimes==2){
									rate = multiple*rate;
								}
								num = num + rate;
								sum = sum + gridFlagData.getGridData(i + 1, j - 1)*rate;
							}
							if (i + 1 <= rows - 1 && gridFlagDataTemp.getFlag(i + 1, j)) {// D点或I
								tmpTimes = gridFlagDataTemp.getSplineTimes(i+1, j);
								rate = diRate;
								if(tmpTimes==1){
									rate = gridFlagDataTemp.getSplineRate(i+1,j)*rate;
								}else if(tmpTimes==2){
									rate = multiple*rate;
								}
								num = num + rate;
								sum = sum + gridFlagData.getGridData(i + 1, j) * rate;
							}
							if (i + 1 <= rows - 1 && j + 1 <= cols - 1 && gridFlagDataTemp.getFlag(i + 1, j + 1)) {// E点或J
								tmpTimes = gridFlagDataTemp.getSplineTimes(i+1, j+1);
								rate = cehjRate;
								if(tmpTimes==1){
									rate = gridFlagDataTemp.getSplineRate(i+1,j+1)*rate;
								}else if(tmpTimes==2){
									rate = multiple*rate;
								}
								num = num + rate;
								sum = sum + gridFlagData.getGridData(i + 1, j + 1)*rate;
							}
							if (i - 1 >= 0 && j - 1 >= 0 && gridFlagDataTemp.getFlag(i - 1, j - 1)) {// H点或C
								tmpTimes = gridFlagDataTemp.getSplineTimes(i-1, j-1);
								rate = cehjRate;
								if(tmpTimes==1){
									rate = gridFlagDataTemp.getSplineRate(i-1,j-1)*rate;
								}else if(tmpTimes==2){
									rate = multiple*rate;
								}
								num = num + rate;
								sum = sum + gridFlagData.getGridData(i - 1, j - 1)*rate;
							}
							if (i - 1 >= 0 && gridFlagDataTemp.getFlag(i - 1, j)) {// I点或D
								tmpTimes = gridFlagDataTemp.getSplineTimes(i-1, j);
								rate = diRate;
								if(tmpTimes==1){
									rate = gridFlagDataTemp.getSplineRate(i-1,j)*rate;
								}else if(tmpTimes==2){
									rate = multiple*rate;
								}
								num = num + rate;
								sum = sum + gridFlagData.getGridData(i - 1, j) * rate;
							}
							if (i - 1 >= 0 && j + 1 <= cols - 1 && gridFlagDataTemp.getFlag(i - 1, j + 1)) {// J点或E
								tmpTimes = gridFlagDataTemp.getSplineTimes(i-1, j+1);
								rate = cehjRate;
								if(tmpTimes==1){
									rate = gridFlagDataTemp.getSplineRate(i-1,j+1)*rate;
								}else if(tmpTimes==2){
									rate = multiple*rate;
								}
								num = num + rate;
								sum = sum + gridFlagData.getGridData(i - 1, j + 1)*rate;
							}
							
							// if (num != 0) {
							if (num > totalRate * 0.25f) {	
//							if (num > 3) {	
								gridFlagData.setGridData(i, j, (float) (sum / num));								
								gridFlagData.setFlag(i, j, true);
								gridFlagData.setSplineTimes(i, j, splineTimes+1);//插值次数得到
								gridFlagData.setSplineRate(i, j, 1);//插值权重
								nullNum--;
							} else {
								gridFlagData.setGridData(i, j, Float.NaN);
								gridFlagData.setFlag(i, j, false);
								gridFlagData.setSplineTimes(i, j, 0);
								gridFlagData.setSplineRate(i, j, 0);
							}
						}
						gDataFlag++;
					}// endif
//					System.out.print(", new = " + gridFlagData.getGridData(i, j) + "; ");
				}// endfor j
//				System.out.println("");
			}// endfor i
			splineTimes++;
			tickE = System.currentTimeMillis() - tickS;
			if (tickE > 4000 && gDataFlag > 0) {
				System.out.println("插值网格数据超时. 等值线分析错误，可能是数据量太少或者数据分布过于不均匀引起");
				return null;
			}
			if (nullNum == 0) {
				break;
			}
			
		}

		try {
			gridData.setGridData(gridFlagData.getGridData());
			float[][] lonAry = StrArrayUtil.getArrFromSERowCol(startX, endX,
					deltX, gridData.getRowNum(), gridData.getColNum(), true);
			float[][] latAry = StrArrayUtil.getArrFromSERowCol(startY, endY,
					deltY, gridData.getRowNum(), gridData.getColNum(), false);
			gridData.setLonAry2D(lonAry);
			gridData.setLatAry2D(latAry);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return gridData;
	}
	/**
	 * 离散数据网格化（利用三角剖分插值）
	 * @param data 离散数据  \\相当于value值吧（db x）
	 * @param lon 离散数据的x坐标(经度)
	 * @param lat 离散数据的y坐标(纬度)
	 * @param startX 网格的起始x坐标(经度)
	 * @param endX 网格的终止x坐标(经度)
	 * @param startY 网格的起始y坐标(纬度)
	 * @param endY 网格的终止y坐标(纬度)
	 * @param deltX 网格的x方向上(经度)的间隔
	 * @param deltY 网格的y方向上(纬度)的间隔
	 * @param minVal 质量控制的最小值
	 * @param maxVal 质量控制的最大值
	 * @param dataType 数据类型，高空还是地面
	 * @param triangle 进行三角剖分的初始三角形
	 * @param elem 要素(主要限制降水的平滑)
	 * @return 网格化好的格点数据
	 */
	public static GridData scatterTriToGrid(float[] data, float[] lon,
			float[] lat, float startX, float endX, float startY, float endY,
			float deltX, float deltY, float minVal, float maxVal, 
			DataType dataType, Triangle triangle, String elem) {
		if (data.length != lat.length) {
			System.out.println("In ChangeScatterToGrid中scatterToGrid()的data.length()!=lat.length,纬度和数据不一致");
			return null;
		} else if (lat.length != lon.length) {
			System.out.println("In ChangeScatterToGrid中scatterToGrid()的lat.length != lon.length,经纬度不一致");
			return null;
		} 

		int rows = (int) (Math.abs((endY - startY)) / deltY + 1);
		int cols = (int) (Math.abs((endX - startX)) / deltX + 1);
		boolean isRain = elem.toUpperCase().startsWith("RAIN");//降水要素（db x）********
		
		if (isRain) {
			int num = 0;
			for (int i = 0; i < data.length; i++) {
				if (data[i] < 0.01f) {
					data[i] = 0.0f;
				} else {
					num++;
				}
			}
			if (num == 0) {
				System.out.println("离散数据网格化时,降水量都小于0.01");
				return null;
			}
		} else {
			// 极值检验
			for (int i = 0; i < data.length; i++) {
				if (data[i] > maxVal) {
					data[i] = maxVal;
				} else if (data[i] < minVal) {
					data[i] = minVal;
				}
			}
		}
		//对降雨情况 针对西北部观测站点较少现象  向离散数据添加伪站点数据
		if(isRain) {
			float pesuResult[][] = selectPseudoStation(startX, endX, startY, endY);
			float[] lons = new float[lon.length + pesuResult.length];
			float[] lats = new float[lat.length + pesuResult.length];
			float[] datas = new float[data.length + pesuResult.length];
			for (int i = 0; i < data.length; i++) {
				lons[i] = lon[i];
				lats[i] = lat[i];
				datas[i] = data[i];
				System.out.println(lon[i] + "***" + lat[i] + "***24小时降雨：" + data[i]);
			}
			for (int i = 0; i < pesuResult.length; i++) {
				lons[lon.length + i] = pesuResult[i][1];
				lats[lat.length + i] = pesuResult[i][0];
				datas[data.length + i] = pesuResult[i][2];
			}
			lon = lons;
			lat = lats;
			data = datas;
			System.out.println("增加的伪站点数据：" + pesuResult.length);
		}
		
		float[] resData = new float[data.length];
		if (isRain) {
			// 降水不需要校正非正常数据
			resData = data;
		} else if (dataType == null) {
			resData = data;
		} else if (dataType.equals(DataType.DMQX)) {
			resData = data;
		} else if (dataType.equals(DataType.GKQX)) {
			resData = data;
		} else if (dataType.equals(DataType.ClimateStat)) {
			resData = data;
//			resData = checkUnnormalData(data, lat, lon, sdNum, distR1, dataR3);
		} else {
			System.out.println("In MIDS3DMath.scatterToGrid()中输入的数据类型不符合条件,不是DMQX也不是GKQX");
			return null;//此方法只适用于降水 或者DMQX,GKQX数据类型 否则到这里就结束了    （db x）********
		}

		if (resData.length <= 1) {
			return null;
		}
		System.out.println("离散数据个数:" + resData.length);
		
		//对网格边缘先进行等距插值 使三角剖分后的格点尽量不落在含有初始三角形中
		final float delt = 5f; //添加边界离散点的间距
		final float RR = 5f;//控制反距离插值范围    当离散数据较密集时尽量把RR的值设置小一些
		float[][] scatterResult = scatterToscatter(resData, lon, lat, startX, endX, startY, endY, delt, RR);
		//把处理边界后的离散数据结果仍然传给原来参数
		data = new float[scatterResult.length];
		lon = new float[scatterResult.length];
		lat = new float[scatterResult.length];
		for (int i = 0; i < scatterResult.length; i++) {
			data[i] = scatterResult[i][0];
			lon[i] = scatterResult[i][1];
			lat[i] = scatterResult[i][2];
		}
		long ss1 = System.currentTimeMillis();
		//根据密度判断时候进行数据预处理，将插值区域面积与离散点个数的比值与0.5比较，如过小于0.5就进行离散数据合并 在不影响插值精度的前提下减少三角形剖分数量
		float avg = Math.abs(endX - startX)*Math.abs(endY - startY)/resData.length;
		if (avg > 0.5) {
			float ruler = 0.5f;//
			float[][] simplScatter = simplifyScatterDate(data, lon, lat, ruler);
			simplScatter = simplifyNScatterDate(simplScatter, ruler, 3);
			float[] simData = new float[simplScatter.length];
			float[] simLon = new float[simplScatter.length];
			float[] simLat = new float[simplScatter.length];
			for (int i = 0; i < simplScatter.length; i++) {
				simData[i] = simplScatter[i][0];
				simLon[i] = simplScatter[i][1];
				simLat[i] = simplScatter[i][2];
			}
			resData = simData;
			lon = simLon;
			lat = simLat;
			data = simData;
			System.out.println("离散数据处理时间：" + (System.currentTimeMillis()-ss1));
			System.out.println("进行离散数据处理后个数:" + data.length);
		}
		// 先进行delaunay网格化 
		long s1 = System.currentTimeMillis();
		Triangulation trilation;
		trilation = new Triangulation(triangle);
		int sdNum = data.length;
		Pnt[] pnt = new Pnt[sdNum];
		Pnt[][] pntDate = new Pnt[rows][cols];
		for (int i = 0; i < sdNum; i++) {
			pnt[i] = new Pnt(lon[i], lat[i]);
			pnt[i].setValue(data[i]);			
			trilation.delaunayPlace(pnt[i]);
		}
		Set<Triangle> setTriangle = trilation.triGraph.nodeSet();//对离散数据完成delaunay网格化，下面处理初始顶点的value值
		
		long s2 = System.currentTimeMillis();
		System.out.println("三角剖分部分用的时间："+(s2-s1));
		System.out.println("剖分后三角形个数:" + setTriangle.size());
		//处理初始三角形的三个顶点的value值，此处通过查找所有包含初始顶点的三角形，利用其所在三角形的其余点的value平均值赋给该顶点，使三角形插值的边缘处理尽可能准确。
		@SuppressWarnings("unchecked")
		List<Triangle>[] lists = new List[3];
		for (int i = 0; i < 3; i++) {
			lists[i] = new ArrayList<Triangle>();
			for (Triangle tri: setTriangle){
				//先进行粗筛选
				if (triangle.getPnt()[i].coord(0) > tri.getMaxX())
					continue;
				if (triangle.getPnt()[i].coord(0) < tri.getMinX())
					continue;
				if (triangle.getPnt()[i].coord(1) > tri.getMaxY())
					continue;
				if (triangle.getPnt()[i].coord(1) < tri.getMinY())
					continue;
				if (triangle.getPnt()[i].isInOnside(tri.getPnt())){
					lists[i].add(tri);
				}
			}
			float val = 0;
			for (Triangle tr: lists[i]){
				val = val + (float)tr.getPnt()[0].getValue() + (float)tr.getPnt()[1].getValue() + (float)tr.getPnt()[2].getValue();
			}
			triangle.getPnt()[i].setValue(val/(2*lists[i].size()));
//			System.out.println("包含顶点" + i + "的三角形有：" + lists[i].size() + "个.");
//			System.out.println("赋给其value值为：" + triangle.getPnt()[i].getValue());
		}
		System.out.println("初始三角形三个顶点处理时间：" + (System.currentTimeMillis() - s2));
		
		//将剖分完成的三角形网分为两块，在检验插值时减少每一点的相对遍历次数，提高插值效率
		Set<Triangle> setTriangle1 = new HashSet<Triangle>(setTriangle.size()/2);
		Set<Triangle> setTriangle2 = new HashSet<Triangle>(setTriangle.size()/2);
		float boundX = (endX + startX)/2;
		for (Triangle tri : setTriangle) {
			if (tri.getMaxX() > boundX)
				setTriangle1.add(tri);
			if (tri.getMinX() < boundX)
				setTriangle2.add(tri);
		}
		//进行三角形插值
		System.out.println("需要插值的格点数：" +rows*cols);
		GridData gridData = new GridData();
		GridDataFlagInfo gridFlagData = new GridDataFlagInfo(rows, cols);
		float[][] lonAry = StrArrayUtil.getArrFromSERowCol(startX, endX, deltX, rows, cols, true);
		float[][] latAry = StrArrayUtil.getArrFromSERowCol(startY, endY, deltY, rows, cols, false);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				gridFlagData.setFlag(i, j, false);//初始化带标记的网格 为了方便检验插值情况 最后再转给GridDate. （db x）********
				pntDate[i][j] = new Pnt(lonAry[i][j], latAry[i][j]);
				if (lonAry[i][j] > boundX)
					for (Triangle tri: setTriangle1){
						//先进行粗筛选
						if (lonAry[i][j] > tri.getMaxX())
							continue;
						if (lonAry[i][j] < tri.getMinX())
							continue;
						if (latAry[i][j] > tri.getMaxY())
							continue;
						if (latAry[i][j] < tri.getMinY())
							continue;
						if (pntDate[i][j].isInOnside(tri.getPnt())){
							gridFlagData.setGridData(i, j, 
									(float)getTnterResult(tri, lonAry[i][j], latAry[i][j]));
							gridFlagData.setFlag(i, j, true);
						}
					}
				else
					for (Triangle tri: setTriangle2){
						//先进行粗筛选
						if (lonAry[i][j] > tri.getMaxX())
							continue;
						if (lonAry[i][j] < tri.getMinX())
							continue;
						if (latAry[i][j] > tri.getMaxY())
							continue;
						if (latAry[i][j] < tri.getMinY())
							continue;
						if (pntDate[i][j].isInOnside(tri.getPnt())){
							gridFlagData.setGridData(i, j, 
									(float)getTnterResult(tri, lonAry[i][j], latAry[i][j]));
							gridFlagData.setFlag(i, j, true);
						}
					}
			}
		}
		gridData.setXStart(startX);
		gridData.setXEnd(endX);
		gridData.setYStart(startY);
		gridData.setYEnd(endY);
		gridData.setXDel(deltX);
		gridData.setYDel(deltY);
		gridData.setColNum(cols);
		gridData.setRowNum(rows);
		try {
			gridData.setGridData(gridFlagData.getGridData());
			gridData.setLonAry2D(lonAry);
			gridData.setLatAry2D(latAry);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("三角插值网格化部分用时：" + (System.currentTimeMillis() - s1));
		return gridData;
	}
//**********END
	private static float getSplineRate(float dist1, float dist2){
		if(dist2==0.0){
			throw new ArithmeticException("In getSplineRate() can not / by zero");
		}
		float r = dist1/dist2;
		if(r<=0.2f){
			return 1.5f;
		}else if(r>0.2f && r<=0.4f){
			return 1.4f;
		}else if(r>0.4f && r<=0.6f){
			return 1.3f;
		}else if(r>0.6f && r<=0.8f){
			return 1.2f;
		}else if(r>0.8f && r<=1.0f){
			return 1.1f;
		}else {
			return 0;
		}
	}
	/**
	 * 为了处理非连续物理量(如降雨) 创建中国稀疏区域的伪观测站点数据  并且赋value值为0 
	 * 存放伪站点数据二维数组(3列 lat lon value)
	 */
	public final static float[][] pseudoStation ={{40.2f, 77.1f, -0.1f},
		{39.1f, 78.6f, -0.1f}, {38.6f, 78.7f, -0.1f},{38, 79.1f, -0.1f}, {38.6f, 79.2f, -0.1f},
		{39.4f, 79.7f, -0.1f}, {39.7f, 80.8f, -0.1f}, {38.6f, 80.8f, -0.1f}, {37.5f, 80.9f, -0.1f},
		{37.8f, 82.1f, -0.1f}, {38.4f, 82.3f, -0.1f}, {39.4f, 82.5f, -0.1f}, {40.4f, 82.6f, -0.1f},
		{40.7f, 82.4f, -0.1f}, {39.7f, 84.2f, -0.1f}, {39.2f, 85.8f, -0.1f}, {40.4f, 85.9f, -0.1f},
		{39.6f, 86.8f, -0.1f}, {41.5f, 88.3f, -0.1f}, {40.0f, 89.1f, -0.1f}, {39.1f, 90.2f, -0.1f},
		{40.6f, 90.8f, -0.1f}, {42.0f, 90.8f, -0.1f}, {41.9f, 92.9f, -0.1f}, {40.8f, 93.1f, -0.1f},
		{39.6f, 92.9f, -0.1f}, {35.7f, 77.7f, -0.1f}, {33.8f, 79.8f, -0.1f}, {35.8f, 80.2f, -0.1f},
		{35.9f, 82.6f, -0.1f}, {34.7f, 82.8f, -0.1f}, {33.1f, 82.6f, -0.1f}, {33.1f, 84.1f, -0.1f},
		{34.1f, 84.6f, -0.1f}, {36.2f, 84.2f, -0.1f}, {36.6f, 86.1f, -0.1f}, {34.8f, 86.3f, -0.1f},
		{33.0f, 86.4f, -0.1f}, {37.5f, 88.1f, -0.1f}, {35.0f, 88.1f, -0.1f}, {32.4f, 88.1f, -0.1f},
		{36.7f, 90.7f, -0.1f}, {34.8f, 90.4f, -0.1f}, {33.3f, 90.0f, -0.1f}, {33.0f, 93.0f, -0.1f},
		{31.5f, 81.5f, -0.1f}, {30.5f, 83.7f, -0.1f}, {29.0f, 84.0f, -0.1f}, {29.3f, 85.6f, -0.1f},
		{31.2f, 85.5f, -0.1f}, {30.1f, 87.2f, -0.1f}, 
		};
		
	/**
	 * 根据传入条件 选择在范围内的伪站点进行插值
	 * @param startX 经度起始值
	 * @param endX 经度终止值
	 * @param startY 纬度起始值
	 * @param endY 纬度终止值
	 * @return 设定区域内的站点数据(二维数组  三列以次为 lat lon value)
	 */
	public static float[][] selectPseudoStation(float startX, float endX, float startY, float endY) {
		if (startX > endX || startY > endY) {
			System.out.println("传入区域参数有问题");
			return null;
		}
		float[][] midResult = new float[pseudoStation.length][3];
		int j = 0;
		for (int i = 0; i < pseudoStation.length; i++) {
			if (pseudoStation[i][0] > startY && pseudoStation[i][0] < endY){
				if (pseudoStation[i][1] > startX && pseudoStation[i][1] < endX){
				midResult[j] = pseudoStation[i];
				j++;
				}
			}
		}
		float[][] result = new float[j][3];
		for (int i = 0; i < result.length; i++) {
			result[i][0] = midResult[i][0];
			result[i][1] = midResult[i][1];
			result[i][2] = midResult[i][2];
		}
		return result;
	}
	/**
	 * 对要网格化的离散数据先进行边缘等距插值  目的是使三角剖分后的格点尽量不落在含有初始三角形中
	 * 
	 * @param data 传入的离散数据value值
	 * @param lon 离散数据的x坐标（经度）
	 * @param lat 离散数据的y坐标（纬度）
	 * @param startX 网格起始x坐标
	 * @param endX 网格终止x坐标
	 * @param startY 网格起始y坐标
	 * @param endY 网格终止y坐标
	 * @param delt 离散数据边缘处理间距
	 * @param RR 对边缘点进行离散数据反距离插值控制半径
	 * @return 存放处理后离散数据的二维数组(按列依次存放 data, lon, lat)
	 */
	private static float[][] scatterToscatter(float[] data, float[] lon, float[] lat,
			float startX, float endX, float startY, float endY, float delt, float RR){
//		final float delt = 5f; //添加边界离散点的间距
		int numLon = (int) (Math.abs((endX - startX)) / delt + 1);
		int numLat = (int) (Math.abs((endY - startY)) / delt + 1);
		float[] sideLon = new float[2*(numLon + numLat)-4];
		float[] sideLat = new float[2*(numLon + numLat)-4];
		float[] sideVal = new float[2*(numLon + numLat)-4];
		List<Integer> listID = new ArrayList<Integer>();
		//边界按顺时针方向编号
		for (int i = 0; i < numLon ; i++) {
			sideLon[i] = startX + i*delt;
			sideLat[i] = startY;
		}
		for (int i = numLon; i < numLon + numLat - 1; i++) {
			sideLon[i] = sideLon[numLon-1];
			sideLat[i] = startY + (i - numLon + 1)*delt;
		}
		for (int i = numLon + numLat - 1; i < 2*numLon + numLat - 2; i++) {
			sideLon[i] = sideLon[numLon + numLat-2] - (i - numLon - numLat + 2)*delt;
			sideLat[i] = sideLat[numLon + numLat-2];
		}
		for (int i = 2*numLon + numLat - 2; i < 2*(numLon + numLat) - 4; i++) {
			sideLon[i] = sideLon[2*numLon + numLat - 3];
			sideLat[i] = sideLat[2*numLon + numLat - 3] - (i - 2*numLon - numLat + 3)*delt;
		}
		System.out.println("增加边界离散点数据个数：" + (2*(numLon + numLat)-4));
		for (int i = 0; i < sideVal.length; i++) {
			List<Float> listLon = new ArrayList<Float>();
			List<Float> listLat = new ArrayList<Float>();
			List<Float> listVal = new ArrayList<Float>();
			for (int j = 0; j < data.length; j++) {
				if (Math.abs(lon[j] - sideLon[i]) + Math.abs(lat[j]-sideLat[i]) < RR){
					listLon.add(lon[j]);
					listLat.add(lat[j]);
					listVal.add(data[j]);
				}
			}
			if (listVal.size() == 0) {
				continue;
			} 
			float[] tempLon = new float[listVal.size()];
			float[] tempLat = new float[listVal.size()];
			float[] tempVal = new float[listVal.size()];
			for (int j = 0; j < tempVal.length; j++) {
				tempLon[j] = listLon.get(j);
				tempLat[j] = listLat.get(j);
				tempVal[j] = listVal.get(j);
			}
			sideVal[i] = Shepard(lon[i], lat[i], tempLon, tempLat, tempVal);
			listID.add(i);//标记插值成功的网格边缘点
		}
		int len = data.length + listID.size();
		float[] compLon = new float[len];
		float[] compLat = new float[len];
		float[] compVal = new float[len];
		for (int i = 0; i < data.length; i++) {
			compLon[i] = lon[i];
			compLat[i] = lat[i];
			compVal[i] = data[i];
		}
		int num = 0;
		for (int i = data.length; i < len; i++) {
			compLon[i] = sideLon[listID.get(num)];
			compLat[i] = sideLat[listID.get(num)];
			compVal[i] = sideVal[listID.get(num)];
			num++;
		}
		System.out.println("添加边界离散点且插值成功的个数：" + listID.size());
		float[][] result = new float[compLon.length][3];
		for (int i = 0; i < result.length; i++) {
			result[i][0] = compVal[i];
			result[i][1] = compLon[i];
			result[i][2] = compLat[i];
		}
		return result;
	}
	/**
	  * 对比较密集的离散数据进行预处理，合并距离非常相近的点
	  * @param data 传入数据的Value值
	  * @param lon 传入数据的经度（横坐标）
	  * @param lat 传入数据的纬度（纵坐标）
	  * @param ruler 控制参数 用来判断两点间的距离要进行处理
	  * @return 处理后得到数据
	  */
	private static float[][] simplifyScatterDate(float[] data, float[] lon, float[] lat, float ruler){
		 float[][] result = null;
		 if (data.length != lat.length) {
				System.out.println("In ChangeScatterToGrid中scatterToGrid()的data.length()!=lat.length,纬度和数据不一致");
				return null;
			} else if (lat.length != lon.length) {
				System.out.println("In ChangeScatterToGrid中scatterToGrid()的lat.length != lon.length,经纬度不一致");
				return null;
			}
		 int len = data.length;
		 List<Float> dataList = new ArrayList<Float>(data.length);
		 List<Float> lonList = new ArrayList<Float>(data.length);
		 List<Float> latList = new ArrayList<Float>(data.length);
		 for (int i = 0; i < len; i++) {
			 dataList.add(data[i]);
			 lonList.add(lon[i]);
			 latList.add(lat[i]);
		 }
		 List<Float> dataList2 = new ArrayList<Float>(data.length);
		 List<Float> lonList2 = new ArrayList<Float>(data.length);
		 List<Float> latList2 = new ArrayList<Float>(data.length);
		 //第一次合并
		 while (dataList.size() > 0) {
			 boolean flag = true;
			 for (int i = 1; i < dataList.size(); i++) {
				if ((Math.abs(lonList.get(0) - lonList.get(i)) 
						+ Math.abs(latList.get(0) - latList.get(i))) < ruler){
					float midLon = (lonList.get(0) + lonList.get(i))/2;
					float midLat = (latList.get(0) + latList.get(i))/2;
					float midData = (dataList.get(0) + dataList.get(i))/2;
					dataList2.add(midData);
					lonList2.add(midLon);
					latList2.add(midLat);
					lonList.remove(i);
					lonList.remove(0);
					latList.remove(i);
					latList.remove(0);
					dataList.remove(i);
					dataList.remove(0);
					flag = false;
					break;
				}
			}
			 if (flag) {
				 dataList2.add(dataList.get(0));
					lonList2.add(lonList.get(0));
					latList2.add(latList.get(0));
					lonList.remove(0);
					latList.remove(0);
					dataList.remove(0);
			 }
			 
		}
		result = new float[dataList2.size()][3];
		for (int i = 0; i < result.length; i++) {
			result[i][0] = dataList2.get(i); 
			result[i][1] = lonList2.get(i); 
			result[i][2] = latList2.get(i); 
		}
		return result;
	 }

	 /**
	  * 对方法simplifyStatterData 的进一步改造 使其能够多次处理离散数据，合并距离非常相近的点
	  * @param arryData 传入数据的Value值
	  * @param ruler 控制参数 用来判断两点间的距离要进行处理
	  * @param n 对离散数据合并的次数
	  * @return 处理后的得到的二维数据
	  */
	private static float[][] simplifyNScatterDate(float[][] arryData, float ruler, int n){
		 float[][] result = arryData;
		 if (result.length <= 1)
			 return result;
		 for (int i = 0; i < n; i++) {
			 float[] data = new float[result.length];
			 float[] lon = new float[result.length];
			 float[] lat = new float[result.length];
			 for (int j = 0; j < result.length; j++) {
				data[j] = result[j][0];
				lon[j] = result[j][1];
				lat[j] = result[j][2];
			}
			 result = simplifyScatterDate(data, lon, lat, ruler);
		}
		return result;
	}
	
	/**
	 * 不进行验证是否在三角形内，直接插值
	 * @param tri 插值三角形
	 * @param x 插值点横坐标
	 * @param y 插值点纵坐标
	 */ 
	public  static double getTnterResult(Triangle tri, double x, double y){
		double result = NULLVAL;
		if (tri == null)
			return result;
		double x1 = tri.get(0).coord(0), y1 = tri.get(0).coord(1), z1 = tri.get(0).getValue(),
		x2 = tri.get(1).coord(0), y2 = tri.get(1).coord(1), z2 = tri.get(1).getValue(),
		x3 = tri.get(2).coord(0), y3 = tri.get(2).coord(1), z3 = tri.get(2).getValue();
		double g,u,v,w;
		
		g = x1 * (y3 - y2) + x2 * (y1 - y3) + x3 * (y2 - y1);
		u = (x * (y3 - y2) + (x2 - x3) * y + x3 * y2 - x2 * y3) / g;
		v = (x * (y1 - y3) + (x3 - x1) * y + x1 * y3 - x3 * y1) / g;
		w = (x * (y2 - y1) + (x1 - x2) * y + x2 * y1 - x1 * y2) / g;
		result = u * z1 + v * z2 + w * z3;
		return result;
	}
	
	/**
	 * 	反向距离加权法
	 * @param x-插值点的x坐标
	 * @param y-插值点的y坐标
	 * @param tempLon-sourceDta中的第一列横坐标x值
	 * @param tempLat-sourceDta中的第二列纵坐标y值
	 * @param tempVal-sourceDta中的第三列value值
	 * @return-插值结果
	 */
	public static float Shepard(float x, float y, float[] tempLon,
									float[] tempLat , float[] tempVal){
		if ((tempLon == null || tempLon.length == 0)
			||(tempLat == null || tempLat.length == 0)
			||(tempVal == null || tempVal.length == 0))
			return NULLVAL;
		
		if ((tempLon.length != tempLat.length)
			||(tempLat.length != tempVal.length))
				return NULLVAL;
		float result = NULLVAL;                                 // depth为插值参数
		float[] d = new float[tempVal.length];
		float dd1 = 0, dd2 = 0;		
		for (int i = 0; i < tempVal.length; i++) {
			//如果数据值为null,不进行累加
			if ((tempVal[i] - NULLVAL < 10e-5) && (tempVal[i] - NULLVAL > -10e-5)){
				continue;
			}
			if ((tempLon[i]- x < 10e-5) && (tempLon[i]- x > -10e-5)) {
				if ((tempLat[i]- y < 10e-5) && (tempLat[i]- y > -10e-5)) {
					result = tempVal[i];
					break;
				}
			} else {
				d[i] = (float)Math.sqrt((x - tempLon[i]) * (x - tempLon[i])
						+ (y - tempLat[i]) * (y - tempLat[i]));
				dd1 = dd1 + tempVal[i] / d[i]; //Math.pow(d[i], p); // 当p为整数时,Math.pow(d[i],p)可以用乘法d[i]*…*d[i]
				dd2 = dd2 + 1 / d[i];//Math.pow(d[i], p);               // 来代替以减少计算机内部运算量
			}
			result = dd1 / dd2;
		} 
		return result; 
	}
	/**
	 * 双线性插值或抽稀数据
	 * @param gridData
	 * @param zoomVal
	 * @return
	 */
	public static GridData interpolation(GridData gridData, float zoomVal){
		if(zoomVal<0){
			System.out.println("In MIDS3DMath.interpolation()无法插值且无法抽稀");
			return gridData;
		}
		if(zoomVal>0 && zoomVal<1){
			return sparseData(gridData, zoomVal);
		}
		
		if(zoomVal>=1){
			return bilinearInterpolation(gridData, (int)zoomVal);
		}
		return gridData;
	}
	
	/**
	 * 双线性插值或抽稀数据
	 * @param gridData
	 * @param zoomVal
	 * @return
	 */
	public static float[][] interpolation(float[][] gridData, float zoomVal){
		if(zoomVal<0){
			System.out.println("In MIDS3DMath.interpolation()无法插值且无法抽稀");
			return gridData;
		}
		if(zoomVal>0 && zoomVal<1){
			return sparseData(gridData, zoomVal);
		}
		
		if(zoomVal>=1){
			return bilinearInterpolation(gridData, (int)zoomVal);
		}
		
		return gridData;
	}
	/**
	 * 抽稀数据
	 * @param gridData
	 * @param zoomVal
	 * @return
	 */
	public static float[][] sparseData(float[][] gridData, float zoomVal){
		
		
		int index = 1;
		if (zoomVal > 0.0 && zoomVal <=0.2) // 1/5
			index = 5;
		else if (zoomVal > 0.0 && zoomVal <=0.25) // 1/4
			index = 4;
		else if (zoomVal > 0.25 && zoomVal < 0.34) // 1/3
			index = 3;
		else if (zoomVal >= 0.34 && zoomVal <= 0.5) // 1/2
			index = 2;
		else if (zoomVal >= 0.5 && zoomVal < 0.67){ // 2/3 先加密2，再抽析3
			gridData = bilinearInterpolation(gridData, 2);
			index = 3;
		} else if (zoomVal >= 0.67 && zoomVal < 1 ){ // 3/4 先加密3，再抽析4
			gridData = bilinearInterpolation(gridData, 3);
			index = 4;
		} else {
			System.out.println("In MIDS3DMath.getLeastData()目前只抽稀1/2,1/3,2/3(0.66), 1/4, 3/4(0.75),1/5!");
			return gridData;			
		}		
		
		if(index!=2 && index!=3 && index!=4 && index!=5){
			System.out.println("In MIDS3DMath.getLeastData()暂时只抽稀1/2,1/3,1/4,1/5");
			return gridData;
		}
		int row = gridData.length;
		int col = gridData[0].length;
		
		int newRow = (int)Math.ceil(row/(index*1.0f));
		int newCol = (int)Math.ceil(col/(index*1.0f));
		
		float[][] newData = new float[newRow][newCol];
		
		int newI = 0;
		int newJ = 0;
		for(int i=0; i<row; i++){
			if(i%index!=0){
				continue;
			}
			newJ=0;
			for(int j=0; j<col; j++){
				if(j%index!=0){
					continue;
				}
				newData[newI][newJ++]=gridData[i][j];
			}
			newI++;
		}
		return newData;
	}
	
	/**
	 * 抽稀数据
	 * @param gridData
	 * @param zoomVal
	 * @return
	 */
	public static GridData sparseData(GridData gridData, float zoomVal){
		int row = gridData.getRowNum();
		int col = gridData.getColNum();
		if(row<20 || col<20){
			System.out.println("In MIDS3DMath.getLeastData()抽稀数据时,原数据行列数小于20,暂不处理");
			return gridData;
		}
		
		float[][] gridPoints = gridData.getGridData();
		float[][] lonsAry = gridData.getLonAry2D();
		float[][] latsAry = gridData.getLatAry2D();
		
		float[][] newGridPoints = sparseData(gridPoints, zoomVal);
		float[][] newLonsAry = sparseData(lonsAry, zoomVal);
		float[][] newLatsAry = sparseData(latsAry, zoomVal);

		int newWidth = newGridPoints[0].length; // 新网格化数据的列数
		int newHeight = newGridPoints.length;// 新网格化数据的行数

		float newXStart = newLonsAry[0][0];
		float newXEnd = newLonsAry[0][newLonsAry[0].length-1];
		float newYStart = newLatsAry[0][0];
		float newYEnd = newLatsAry[newLatsAry.length-1][0];
		
		float newXDelta = (float) (Math.abs(newXEnd - newXStart) / (newWidth - 1));
		float newYDelta = (float) (Math.abs(newYEnd - newYStart) / (newHeight - 1));
		
		gridData = new GridData();
		gridData.setColNum(newWidth);
		gridData.setRowNum(newHeight);
		gridData.setXStart(newXStart);
		gridData.setXEnd(newXEnd);
		gridData.setYStart(newYStart);
		gridData.setYEnd(newYEnd);
		gridData.setXDel(newXDelta);
		gridData.setYDel(newYDelta);
		try {
			gridData.setGridData(newGridPoints);
			gridData.setLonAry2D(newLonsAry);
			gridData.setLatAry2D(newLatsAry);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return gridData;
	}
	public static GridData bilinearInterpolation(GridData gridData, int zoomVal) {
		float[][] gridPoints = gridData.getGridData();
		float[][] lonsAry = gridData.getLonAry2D();
		float[][] latsAry = gridData.getLatAry2D();
		float xEnd = gridData.getXEnd();
		float xStart = gridData.getXStart();
		float yEnd = gridData.getYEnd();
		float yStart = gridData.getYStart();
		int col = gridData.getColNum();
		int row = gridData.getRowNum();

		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				if (gridPoints[i][j] == NULLVAL) {
					gridData = bilinearInterpolationNullStr(gridData, zoomVal);
					return gridData;
				}
			}
		}

		float[][] newGridPoints = bilinearInterpolation(gridPoints, zoomVal);
		float[][] newLonsAry = bilinearInterpolation(lonsAry, zoomVal);
		float[][] newLatsAry = bilinearInterpolation(latsAry, zoomVal);

		int newWidth = newGridPoints[0].length; // 新网格化数据的列数
		int newHeight = newGridPoints.length;// 新网格化数据的行数

		float newXDelta = (float) (Math.abs(xEnd - xStart) / (newWidth - 1));
		float newYDelta = (float) (Math.abs(yEnd - yStart) / (newHeight - 1));
		// gridData = new GridData(xStart, xEnd, yStart, yEnd, newXDelta,
		// newYDelta);
		gridData = new GridData();
		gridData.setColNum(newWidth);
		gridData.setRowNum(newHeight);
		gridData.setXStart(xStart);
		gridData.setXEnd(xEnd);
		gridData.setYStart(yStart);
		gridData.setYEnd(yEnd);
		gridData.setXDel(newXDelta);
		gridData.setYDel(newYDelta);
		try {
			gridData.setGridData(newGridPoints);
			gridData.setLonAry2D(newLonsAry);
			gridData.setLatAry2D(newLatsAry);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return gridData;
	}

	/**
	 * 双线性插值
	 * @param srcData 需要插值的二维数组
	 * @param zoomVal 插值点的个数
	 * @return 插值后的二维数组
	 */
	public static float[][] bilinearInterpolation(float[][] srcData, int zoomVal) {
		if (zoomVal == 0) {
			return srcData;
		}
		int row = srcData.length;
		int col = srcData[0].length;
		zoomVal = zoomVal + 1;
		int newWidth = zoomVal * (col - 1) + 1; // 新网格化数据的列数
		int newHeight = zoomVal * (row - 1) + 1;// 新网格化数据的行数
		float[][] decData = new float[newHeight][newWidth];
		float u = 0.0f, v = 0.0f;
		float z1 = 0.0f, z2 = 0.0f, z3 = 0.0f, z4 = 0.0f;

		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				if (srcData[i][j] == NULLVAL) {
					decData = bilinearInterpolationNullStr(srcData, zoomVal);
					return decData;
				}
			}
		}
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				z1 = srcData[i][j];
				int k1 = 0, k2 = 0;
				for (int m = 0; m < zoomVal; m++) {
					for (int n = 0; n < zoomVal; n++) {
						if (i == row - 1 && j == col - 1) {
							k1 = 0;
							k2 = 0;
							z2 = 0.0f;
							z3 = 0.0f;
							z4 = 0.0f;
						} else if (i == row - 1 && j != col - 1) {
							k1 = 0;
							k2 = n;
							z2 = srcData[i][j + 1];
							z3 = 0.0f;
							z4 = 0.0f;
						} else if (j == col - 1 && i != row - 1) {
							k1 = m;
							k2 = 0;
							z2 = 0.0f;
							z3 = srcData[i + 1][j];
							z4 = 0.0f;
						} else if (i != row - 1 && j != col - 1) {
							k1 = m;
							k2 = n;
							z2 = srcData[i][j + 1];
							z3 = srcData[i + 1][j];
							z4 = srcData[i + 1][j + 1];
						}

						u = k1 / (float) zoomVal;
						v = k2 / (float) zoomVal;
						decData[i * zoomVal + k1][j * zoomVal + k2] = (1 - u)
								* (1 - v) * z1 + (1 - u) * v * z2 + u * (1 - v)
								* z3 + u * v * z4;
					}// endfor n
				}// endfor m
			}// endfor j
		}// endfor i
		return decData;
	}
	/**
	 * 存在空值的插值方法,Null--->-99999f
	 * @param gridData 需要插值的格点数据
	 * @param zoomVal 插值点的个数
	 * @return 插值后的格点数据
	 */
	public static GridData bilinearInterpolationNullStr(GridData gridData,
			int zoomVal) {
		float[][] gridPoints = gridData.getGridData();
		float[][] lonsAry = gridData.getLonAry2D();
		float[][] latsAry = gridData.getLatAry2D();
		float xEnd = gridData.getXEnd();
		float xStart = gridData.getXStart();
		float yEnd = gridData.getYEnd();
		float yStart = gridData.getYStart();

		float[][] newGridPoints = bilinearInterpolationNullStr(gridPoints,
				zoomVal);
		float[][] newLonsAry = bilinearInterpolationNullStr(lonsAry, zoomVal);
		float[][] newLatsAry = bilinearInterpolationNullStr(latsAry, zoomVal);

		int newWidth = newGridPoints[0].length; // 新网格化数据的列数
		int newHeight = newGridPoints.length;// 新网格化数据的行数

		float newXDelta = Math.abs(xEnd - xStart) / (newWidth - 1);
		float newYDelta = Math.abs(yEnd - yStart) / (newHeight - 1);
		gridData = new GridData();
		gridData.setColNum(newWidth);
		gridData.setRowNum(newHeight);
		gridData.setXStart(xStart);
		gridData.setXEnd(xEnd);
		gridData.setYStart(yStart);
		gridData.setYEnd(yEnd);
		gridData.setXDel(newXDelta);
		gridData.setYDel(newYDelta);
		try {
			gridData.setGridData(newGridPoints);
			gridData.setLatAry2D(newLatsAry);
			gridData.setLonAry2D(newLonsAry);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return gridData;
	}

	/**
	 * 双线性插值，存在-99999f
	 * @param srcData 需要插值的二维数组
	 * @param zoomVal 插值点的个数
	 * @return 插值后的二维数组
	 */
	public static float[][] bilinearInterpolationNullStr(float[][] srcData,
			int zoomVal) {
		if (zoomVal == 0) {
			return srcData;
		}
		int row = srcData.length;
		int col = srcData[0].length;
		zoomVal = zoomVal + 1;
		int newWidth = zoomVal * (col - 1) + 1; // 新网格化数据的列数
		int newHeight = zoomVal * (row - 1) + 1;// 新网格化数据的行数
		float[][] decData = new float[newHeight][newWidth];
		float u = 0.0f, v = 0.0f;
		float z1 = 0;
		float z2 = 0;
		float z3 = 0;
		float z4 = 0;
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				z1 = srcData[i][j];
				if (z1 == NULLVAL) {
					int k1 = 0, k2 = 0;
					for (int m = 0; m < zoomVal; m++) {
						for (int n = 0; n < zoomVal; n++) {
							if (i == row - 1 && j == col - 1) {
								k1 = 0;
								k2 = 0;
							} else if (i == row - 1 && j != col - 1) {
								k1 = 0;
								k2 = n;
							} else if (j == col - 1 && i != row - 1) {
								k1 = m;
								k2 = 0;
							} else if (i != row - 1 && j != col - 1) {
								k1 = m;
								k2 = n;
							}
							decData[i * zoomVal + k1][j * zoomVal + k2] = NULLVAL;
						}// endfor n
					}// endfor m
				} else {
					int k1 = 0, k2 = 0;
					for (int m = 0; m < zoomVal; m++) {
						for (int n = 0; n < zoomVal; n++) {
							if (i == row - 1 && j == col - 1) {
								decData[i * zoomVal + k1][j * zoomVal + k2] = z1;
							} else if (i == row - 1 && j != col - 1) {
								k1 = 0;
								k2 = n;
								z2 = srcData[i][j + 1];
								if (z2 == NULLVAL) {
									if (n == 0) {
										decData[i * zoomVal + k1][j * zoomVal + k2] = z1;
									} else {
										decData[i * zoomVal + k1][j * zoomVal + k2] = NULLVAL;
									}
								} else {
									v = k2 / (float) zoomVal;
									decData[i * zoomVal + k1][j * zoomVal + k2] = (1 - v) * z1 + v * z2;
								}
							} else if (j == col - 1 && i != row - 1) {
								k1 = m;
								k2 = 0;
								z3 = srcData[i + 1][j];
								if (z3 == NULLVAL) {
									if (m == 0) {
										decData[i * zoomVal + k1][j * zoomVal + k2] = z1;
									} else {
										decData[i * zoomVal + k1][j * zoomVal + k2] = NULLVAL;
									}
								} else {
									u = k1 / (float) zoomVal;
									decData[i * zoomVal + k1][j * zoomVal + k2] = (1 - u) * z1 + u * z3;
								}
							} else if (i != row - 1 && j != col - 1) {
								k1 = m;
								k2 = n;
								z2 = srcData[i][j + 1];
								z3 = srcData[i + 1][j];
								z4 = srcData[i + 1][j + 1];
								u = k1 / (float) zoomVal;
								v = k2 / (float) zoomVal;
								if (z2 == NULLVAL && z3 != NULLVAL && z4 != NULLVAL) {
									if (n == 0) {
										decData[i * zoomVal + k1][j * zoomVal + k2] = (1 - u) * z1 + u * z3;
									} else {
										decData[i * zoomVal + k1][j * zoomVal + k2] = NULLVAL;
									}
								} else if (z2 != NULLVAL && z3 == NULLVAL && z4 != NULLVAL) {
									if (m == 0) {
										decData[i * zoomVal + k1][j * zoomVal + k2] = (1 - v) * z1 + v * z2;
									} else {
										decData[i * zoomVal + k1][j * zoomVal + k2] = NULLVAL;
									}
								} else if (z2 != NULLVAL && z3 != NULLVAL && z4 == NULLVAL) {
									if (m != 0 && n != 0) {
										decData[i * zoomVal + k1][j * zoomVal + k2] = NULLVAL;
									} else if (m == 0) {
										decData[i * zoomVal + k1][j * zoomVal + k2] = (1 - v) * z1 + v * z2;
									} else {// n==0
										decData[i * zoomVal + k1][j * zoomVal + k2] = (1 - u) * z1 + u * z3;
									}
								} else if (z2 == NULLVAL && z3 == NULLVAL
										&& z4 != NULLVAL) {
									if (m == 0 && n == 0) {
										decData[i * zoomVal + k1][j * zoomVal + k2] = z1;
									} else {
										decData[i * zoomVal + k1][j * zoomVal + k2] = NULLVAL;
									}
								} else if (z2 != NULLVAL && z3 == NULLVAL
										&& z4 == NULLVAL) {
									if (m == 0) {
										decData[i * zoomVal + k1][j * zoomVal + k2] = (1 - v) * z1 + v * z2;
									} else {
										decData[i * zoomVal + k1][j * zoomVal + k2] = NULLVAL;
									}
								} else if (z2 == NULLVAL && z3 != NULLVAL
										&& z4 == NULLVAL) {
									if (n == 0) {
										decData[i * zoomVal + k1][j * zoomVal + k2] = (1 - u) * z1 + u * z3;
									} else {
										decData[i * zoomVal + k1][j * zoomVal + k2] = NULLVAL;
									}
								} else if (z2 == NULLVAL && z3 == NULLVAL
										&& z4 == NULLVAL) {
									// 全为空
									if (m == 0 && n == 0) {
										decData[i * zoomVal + k1][j * zoomVal + k2] = z1;
									} else {
										decData[i * zoomVal + k1][j * zoomVal + k2] = NULLVAL;
									}
								} else {
									// 全不为空
									decData[i * zoomVal + k1][j * zoomVal + k2] = (1 - u) * (1 - v) * z1
											+ (1 - u) * v * z2 + u * (1 - v) * z3 + u * v * z4;
								}
							}
						}// endfor n
					}// endfor m
				}
			}// endfor j
		}// endfor i
		return decData;
	}
	/**
	 * 卷积平滑，根据IDL写，存在99999空
	 * @param gridData 需要平滑的二维数组
	 * @param kernel 卷积算子
	 * @param flag
	 *            选用的卷积方法:flag == 0 复制原数据；flag == 1 Truncate；flag == 2 wrap；
	 *            flag == 3 zero;其他：全部补0
	 * @return 平滑后的数据
	 */
	public static float[][] convol(float[][] gridData, float[][] kernel,
			int flag) {
		int dataDimension = gridData.length;
		int kernelDimension = kernel.length;
		if (dataDimension == 1) {
			if (dataDimension != kernelDimension) {
				System.out.println("In MIDS3DMaht.convolNullStr() " +
						"原始数据和卷积核维数不同, 不符合卷积平滑的条件");
				return gridData;
			}
		}

		int rows = gridData.length;
		int cols = gridData[0].length;
		int size1 = rows <= cols ? rows : cols;
		int n = kernel.length;// 行
		int m = kernel[0].length;// 列
		int size2 = m <= n ? m : n;
		if (size1 < size2) {
			System.out.println("In MIDS3DMath.convolNullStr() gridData.length<kernel.length 不符合平滑条件");
			return gridData;
		}
		if (cols < m) {
			System.out.println("In MIDS3DMath.convolNullStr() gridData.length<kernel.length 不符合平滑条件");
			return gridData;
		}
		int size3 = m >= n ? m : n;
		if (size3 < 2) {
			System.out.println("In MIDS3DMath.convolNullStr() kernel.length<2 不符合平滑条件");
			return gridData;
		}

		float[][] tmpData = new float[rows][cols];

		// //分母，总权重
		float sum = 0.0f;
		// for (int i = 0; i < n; i++) {
		// for (int j = 0; j < m; j++) {
		// sum = sum + kernel[i][j];
		// }
		// }
		float[][] tmpKernel = new float[n][m];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				tmpKernel[i][j] = kernel[i][j];
			}
		}

		float sumTmp = 0.0f;
		int mm = (int) (m / 2);
		int nn = (int) (n / 2);

		// 二维
		for (int i = nn; i < rows - (n - (nn + 1)); i++) {
			for (int j = mm; j < cols - (m - (mm + 1)); j++) {
				if (gridData[i][j] == NULLVAL) {
					tmpData[i][j] = NULLVAL;
				} else {
					sum = 0.0f;
					sumTmp = 0.0f;
					for (int k = 0; k < n; k++) {
						for (int t = 0; t < m; t++) {
							tmpKernel[k][t] = kernel[k][t];
						}
					}
					for (int k = 0; k < n; k++) {
						for (int t = 0; t < m; t++) {
							if (gridData[i + k - nn][j + t - mm] == NULLVAL) {
								tmpKernel[k][t] = 0.0f;
							}
							sumTmp = sumTmp + gridData[i + k - nn][j + t - mm]
									* tmpKernel[k][t];
							sum = sum + tmpKernel[k][t];
						}
					}
					tmpData[i][j] = sumTmp / sum;
				}
			}
		}

		// 处理边界
		if (flag == 0) {
			// 复制原数据
			// 左边缘
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < mm; j++) {
					tmpData[i][j] = gridData[i][j];
				}
			}
			// 右边缘
			for (int i = 0; i < rows; i++) {
				for (int j = cols - (m - (mm + 1)); j < cols; j++) {
					tmpData[i][j] = gridData[i][j];
				}
			}
			// 上边缘
			for (int j = 0; j < cols; j++) {
				for (int i = 0; i < nn; i++) {
					tmpData[i][j] = gridData[i][j];
				}
			}
			// 下边缘
			for (int j = 0; j < cols; j++) {
				for (int i = rows - (n - (nn + 1)); i < rows; i++) {
					tmpData[i][j] = gridData[i][j];
				}
			}
		} else if (flag == 1) {
			// Edge_Truncate
			// 左边缘
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < mm; j++) {
					if (gridData[i][j] == NULLVAL) {
						tmpData[i][j] = NULLVAL;
					} else {
						sum = 0.0f;
						sumTmp = 0.0f;
						for (int k = 0; k < n; k++) {
							for (int t = 0; t < m; t++) {
								tmpKernel[k][t] = kernel[k][t];
							}
						}
						for (int k = 0; k < n; k++) {
							int row = i + k - nn;
							for (int t = 0; t < m; t++) {
								int col = j + t - mm;
								if (row < 0) {
									row = 0;
								} else if (row > rows - 1) {
									row = rows - 1;
								}
								if (col < 0) {
									col = 0;
								} else if (col > cols - 1) {
									col = cols - 1;
								}
								if (gridData[row][col] == NULLVAL) {
									tmpKernel[k][t] = 0.0f;
								}
								sumTmp = sumTmp + gridData[row][col]
										* tmpKernel[k][t];
								sum = sum + tmpKernel[k][t];
							}
						}
						tmpData[i][j] = sumTmp / sum;
					}
				}
			}

			// 右边缘
			for (int i = 0; i < rows; i++) {
				for (int j = cols - (m - (mm + 1)); j < cols; j++) {
					if (gridData[i][j] == NULLVAL) {
						tmpData[i][j] = NULLVAL;
					} else {
						sum = 0.0f;
						sumTmp = 0.0f;
						for (int k = 0; k < n; k++) {
							for (int t = 0; t < m; t++) {
								tmpKernel[k][t] = kernel[k][t];
							}
						}
						for (int k = 0; k < n; k++) {
							int row = i + k - nn;
							for (int t = 0; t < m; t++) {
								int col = j + t - mm;
								if (row < 0) {
									row = 0;
								} else if (row > rows - 1) {
									row = rows - 1;
								}
								if (col < 0) {
									col = 0;
								} else if (col > cols - 1) {
									col = cols - 1;
								}
								if (gridData[row][col] == NULLVAL) {
									tmpKernel[k][t] = 0.0f;
								}
								sumTmp = sumTmp + gridData[row][col]
										* tmpKernel[k][t];
								sum = sum + tmpKernel[k][t];
							}
						}
						tmpData[i][j] = sumTmp / sum;
					}
				}
			}

			// 上边缘
			for (int i = 0; i < nn; i++) {
				for (int j = mm; j < cols - (m - (mm + 1)); j++) {
					if (gridData[i][j] == NULLVAL) {
						tmpData[i][j] = NULLVAL;
					} else {
						sum = 0.0f;
						sumTmp = 0.0f;
						for (int k = 0; k < n; k++) {
							for (int t = 0; t < m; t++) {
								tmpKernel[k][t] = kernel[k][t];
							}
						}
						for (int k = 0; k < n; k++) {
							int row = i + k - nn;
							for (int t = 0; t < m; t++) {
								int col = j + t - mm;
								if (row < 0) {
									row = 0;
								} else if (row > rows - 1) {
									row = rows - 1;
								}
								if (col < 0) {
									col = 0;
								} else if (col > cols - 1) {
									col = cols - 1;
								}
								if (gridData[row][col] == NULLVAL) {
									tmpKernel[k][t] = 0.0f;
								}
								sumTmp = sumTmp + gridData[row][col]
										* tmpKernel[k][t];
								sum = sum + tmpKernel[k][t];
							}
						}
						tmpData[i][j] = sumTmp / sum;
					}
				}
			}

			// 下边缘
			for (int i = rows - (n - (nn + 1)); i < rows; i++) {
				for (int j = mm; j < cols - (m - (mm + 1)); j++) {
					if (gridData[i][j] == NULLVAL) {
						tmpData[i][j] = NULLVAL;
					} else {
						sum = 0.0f;
						sumTmp = 0.0f;
						for (int k = 0; k < n; k++) {
							for (int t = 0; t < m; t++) {
								tmpKernel[k][t] = kernel[k][t];
							}
						}
						for (int k = 0; k < n; k++) {
							int row = i + k - nn;
							for (int t = 0; t < m; t++) {
								int col = j + t - mm;
								if (row < 0) {
									row = 0;
								} else if (row > rows - 1) {
									row = rows - 1;
								}
								if (col < 0) {
									col = 0;
								} else if (col > cols - 1) {
									col = cols - 1;
								}
								if (gridData[row][col] == NULLVAL) {
									tmpKernel[k][t] = 0.0f;
								}
								sumTmp = sumTmp + gridData[row][col]
										* tmpKernel[k][t];
								sum = sum + tmpKernel[k][t];
							}
						}
						tmpData[i][j] = sumTmp / sum;
					}
				}
			}
		} else if (flag == 2) {
			// Edge_Wrap
			// 左边缘
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < mm; j++) {
					if (gridData[i][j] == NULLVAL) {
						tmpData[i][j] = NULLVAL;
					} else {
						sum = 0.0f;
						sumTmp = 0.0f;
						for (int k = 0; k < n; k++) {
							for (int t = 0; t < m; t++) {
								tmpKernel[k][t] = kernel[k][t];
							}
						}
						for (int k = 0; k < n; k++) {
							int row = i + k - nn;
							for (int t = m - 1; t >= 0; t--) {
								int col = j + t - mm;
								if (row < 0) {
									row = rows + row;
								} else if (row > rows - 1) {
									row = row - rows;
								}
								if (col < 0) {
									col = cols + col;
								} else if (col > cols - 1) {
									col = col - cols;
								}
								if (gridData[row][col] == NULLVAL) {
									tmpKernel[k][t] = 0.0f;
								}
								sumTmp = sumTmp + gridData[row][col]
										* tmpKernel[k][t];
								sum = sum + tmpKernel[k][t];
							}
						}
						tmpData[i][j] = sumTmp / sum;
					}
				}
			}
			// 右边缘
			for (int i = 0; i < rows; i++) {
				for (int j = cols - (m - (mm + 1)); j < cols; j++) {
					if (gridData[i][j] == NULLVAL) {
						tmpData[i][j] = NULLVAL;
					} else {
						sum = 0.0f;
						sumTmp = 0.0f;
						for (int k = 0; k < n; k++) {
							for (int t = 0; t < m; t++) {
								tmpKernel[k][t] = kernel[k][t];
							}
						}
						for (int k = 0; k < n; k++) {
							int row = i + k - nn;
							for (int t = 0; t < m; t++) {
								int col = j + t - mm;
								if (row < 0) {
									row = rows + row;
								} else if (row > rows - 1) {
									row = row - rows;
								}
								if (col < 0) {
									col = cols + col;
								} else if (col > cols - 1) {
									col = col - cols;
								}
								if (gridData[row][col] == NULLVAL) {
									tmpKernel[k][t] = 0.0f;
								}
								sumTmp = sumTmp + gridData[row][col]
										* tmpKernel[k][t];
								sum = sum + tmpKernel[k][t];
							}
						}
						tmpData[i][j] = sumTmp / sum;
					}
				}
			}
			// 上边缘
			for (int i = 0; i < nn; i++) {
				for (int j = mm; j < cols - (m - (mm + 1)); j++) {
					if (gridData[i][j] == NULLVAL) {
						tmpData[i][j] = NULLVAL;
					} else {
						sum = 0.0f;
						sumTmp = 0.0f;
						for (int k = 0; k < n; k++) {
							for (int t = 0; t < m; t++) {
								tmpKernel[k][t] = kernel[k][t];
							}
						}
						for (int k = 0; k < n; k++) {
							int row = i + k - nn;
							for (int t = 0; t < m; t++) {
								int col = j + t - mm;
								if (row < 0) {
									row = rows + row;
								} else if (row > rows - 1) {
									row = row - rows;
								}
								if (col < 0) {
									col = cols + col;
								} else if (col > cols - 1) {
									col = col - cols;
								}
								if (gridData[row][col] == NULLVAL) {
									tmpKernel[k][t] = 0.0f;
								}
								sumTmp = sumTmp + gridData[row][col]
										* tmpKernel[k][t];
								sum = sum + tmpKernel[k][t];
							}
						}
						tmpData[i][j] = sumTmp / sum;
					}
				}
			}

			// 下边缘
			for (int i = rows - (n - (nn + 1)); i < rows; i++) {
				for (int j = mm; j < cols - (m - (mm + 1)); j++) {
					if (gridData[i][j] == NULLVAL) {
						tmpData[i][j] = NULLVAL;
					} else {
						sum = 0.0f;
						sumTmp = 0.0f;
						for (int k = 0; k < n; k++) {
							for (int t = 0; t < m; t++) {
								tmpKernel[k][t] = kernel[k][t];
							}
						}
						for (int k = 0; k < n; k++) {
							int row = i + k - nn;
							for (int t = 0; t < m; t++) {
								int col = j + t - mm;
								if (row < 0) {
									row = rows + row;
								} else if (row > rows - 1) {
									row = row - rows;
								}
								if (col < 0) {
									col = cols + col;
								} else if (col > cols - 1) {
									col = col - cols;
								}
								if (gridData[row][col] == NULLVAL) {
									tmpKernel[k][t] = 0.0f;
								}
								sumTmp = sumTmp + gridData[row][col]
										* tmpKernel[k][t];
								sum = sum + tmpKernel[k][t];
							}
						}
						tmpData[i][j] = sumTmp / sum;
					}
				}
			}
		} else if (flag == 3) {
			// Edge_Zero
			// 左边缘
			boolean flagzero = false;
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < mm; j++) {
					if (gridData[i][j] == NULLVAL) {
						tmpData[i][j] = NULLVAL;
					} else {
						sum = 0.0f;
						sumTmp = 0.0f;
						for (int k = 0; k < n; k++) {
							for (int t = 0; t < m; t++) {
								tmpKernel[k][t] = kernel[k][t];
							}
						}
						for (int k = 0; k < n; k++) {
							int row = i + k - nn;
							for (int t = m - 1; t >= 0; t--) {
								flagzero = false;
								int col = j + t - mm;
								if (row < 0 || row > rows - 1 || col < 0
										|| col > cols - 1) {
									flagzero = true;
								}
								if (!flagzero) {
									if (gridData[row][col] == NULLVAL) {
										tmpKernel[k][t] = 0.0f;
									}
									sumTmp = sumTmp + gridData[row][col]
											* tmpKernel[k][t];
									sum = sum + tmpKernel[k][t];
								}
							}
						}
						tmpData[i][j] = sumTmp / sum;
					}
				}
			}
			// 右边缘
			flagzero = false;
			for (int i = 0; i < rows; i++) {
				for (int j = cols - (m - (mm + 1)); j < cols; j++) {
					if (gridData[i][j] == NULLVAL) {
						tmpData[i][j] = NULLVAL;
					} else {
						sum = 0.0f;
						sumTmp = 0.0f;
						for (int k = 0; k < n; k++) {
							for (int t = 0; t < m; t++) {
								tmpKernel[k][t] = kernel[k][t];
							}
						}
						for (int k = 0; k < n; k++) {
							int row = i + k - nn;
							for (int t = 0; t < m; t++) {
								flagzero = false;
								int col = j + t - mm;
								if (row < 0 || row > rows - 1 || col < 0
										|| col > cols - 1) {
									flagzero = true;
								}
								if (!flagzero) {
									if (gridData[row][col] == NULLVAL) {
										tmpKernel[k][t] = 0.0f;
									}
									sumTmp = sumTmp + gridData[row][col]
											* tmpKernel[k][t];
									sum = sum + tmpKernel[k][t];
								}
							}
						}
						tmpData[i][j] = sumTmp / sum;
					}
				}
			}
			// 上边缘
			flagzero = false;
			for (int i = 0; i < nn; i++) {
				for (int j = mm; j < cols - (m - (mm + 1)); j++) {
					if (gridData[i][j] == NULLVAL) {
						tmpData[i][j] = NULLVAL;
					} else {
						sum = 0.0f;
						sumTmp = 0.0f;
						for (int k = 0; k < n; k++) {
							for (int t = 0; t < m; t++) {
								tmpKernel[k][t] = kernel[k][t];
							}
						}
						for (int k = 0; k < n; k++) {
							int row = i + k - nn;
							for (int t = 0; t < m; t++) {
								flagzero = false;
								int col = j + t - mm;
								if (row < 0 || row > rows - 1 || col < 0
										|| col > cols - 1) {
									flagzero = true;
								}
								if (!flagzero) {
									if (gridData[row][col] == NULLVAL) {
										tmpKernel[k][t] = 0.0f;
									}
									sumTmp = sumTmp + gridData[row][col]
											* tmpKernel[k][t];
									sum = sum + tmpKernel[k][t];
								}
							}
						}
						tmpData[i][j] = sumTmp / sum;
					}
				}
			}
			// 下边缘
			flagzero = false;
			for (int i = rows - (n - (nn + 1)); i < rows; i++) {
				for (int j = mm; j < cols - (m - (mm + 1)); j++) {
					if (gridData[i][j] == NULLVAL) {
						tmpData[i][j] = NULLVAL;
					} else {
						sum = 0.0f;
						sumTmp = 0.0f;
						for (int k = 0; k < n; k++) {
							for (int t = 0; t < m; t++) {
								tmpKernel[k][t] = kernel[k][t];
							}
						}
						for (int k = 0; k < n; k++) {
							int row = i + k - nn;
							for (int t = 0; t < m; t++) {
								flagzero = false;
								int col = j + t - mm;
								if (row < 0 || row > rows - 1 || col < 0
										|| col > cols - 1) {
									flagzero = true;
								}
								if (!flagzero) {
									if (gridData[row][col] == NULLVAL) {
										tmpKernel[k][t] = 0.0f;
									}
									sumTmp = sumTmp + gridData[row][col]
											* tmpKernel[k][t];
									sum = sum + tmpKernel[k][t];
								}
							}
						}
						tmpData[i][j] = sumTmp / sum;
					}
				}
			}
		} else {
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < mm; j++) {
					tmpData[i][j] = 0.0f;
				}
			}
			// 右边缘
			for (int i = 0; i < rows; i++) {
				for (int j = cols - (m - (mm + 1)); j <= cols - 1; j++) {
					tmpData[i][j] = 0.0f;
				}
			}
			// 上边缘
			for (int i = 0; i < nn; i++) {
				for (int j = 0; j < cols; j++) {
					tmpData[i][j] = 0.0f;
				}
			}
			// 下边缘
			for (int i = rows - (n - (nn + 1)); i <= rows - 1; i++) {
				for (int j = 0; j < cols; j++) {
					tmpData[i][j] = 0.0f;
				}
			}
		}
		return tmpData;
	}
	public static float[] getMaxMinFrom2Arr(float[][] ary, boolean isXDirection){
		float sVal = 0;
		float eVal = 0;
		
		int row = ary.length;
		int col = ary[0].length;
		
		if(isXDirection){
			sVal = ary[0][0];
			eVal = ary[0][col-1];
		}else{
			sVal = ary[0][0];
			eVal = ary[row-1][0];
		}
		
		boolean isDesc = false;
		
		if(sVal<eVal){
			isDesc = false;
		}else if(sVal>eVal){
			isDesc = true;
		}else{
			//起止坐标相等
			System.out.println("In MIDS3DMath.getMaxMinFrom2Arr()中只有一列数据");
			return null;
		}
		
		if(isXDirection){
			if(!isDesc){
				for(int i=0; i<row; i++){
					if(sVal>ary[i][0]){
						sVal = ary[i][0];
					}
						
					if(eVal<ary[i][col-1]){
						eVal = ary[i][col-1];
					}
				}
			}else{
				for(int i=0; i<row; i++){
					if(sVal<ary[i][0]){
						sVal = ary[i][0];
					}
					
					if(eVal>ary[i][col-1]){
						eVal = ary[i][col-1];
					}
				}
			}
		}else{
			if(!isDesc){
				for(int i=0; i<col; i++){
					if(sVal>ary[0][i]){
						sVal = ary[0][i];
					}
					
					if(eVal<ary[row-1][i]){
						eVal = ary[row-1][i];
					}
				}
			}else{
				for(int i=0; i<col; i++){
					if(sVal<ary[0][i]){
						sVal = ary[0][i];
					}
					
					if(eVal>ary[row-1][i]){
						eVal = ary[row-1][i];
					}
				}
			}
		}
		
		float[] resSE = new float[]{sVal, eVal};
		return resSE;
	}
	
	//-------------------------------------2014-11-11 添加
	/**
	 * 转化“度、分、秒”计量到度
	 * @param dg 度
	 * @param mi 分
	 * @param sec 秒
	 * @return
	 */
	public static double LatLonDgToDMS(int dg, int mi, int sec){
		return dg + mi/60.0 + sec/3600.0;
	}
	/**
	 * 转化经纬度计量 到“度、分、秒”
	 * @param val
	 * @return
	 */
	public static int[] DMS2LatLonDg(double val) {
		int[] result = new int[3];
		result[0] = (int)val;
		result[1] = (int)(Math.abs(val - result[0]) * 60);
		result[2] = (int)(((Math.abs(val - result[0]) * 60) - result[1]) * 60);
		return result;
	}
	/**
	 * 根据传入的两个点 进行加密插值
	 * @param startP
	 * @param endP
	 * @param maxNum 控制最大个数(默认不能低于5)
	 * @return
	 */
	public static List<Position> getInsertProfileDatas(Position startP, Position endP, int maxNum) {
		float sX = (float)startP.longitude.degrees;
		float sY = (float)startP.latitude.degrees;
		float eX = (float)endP.longitude.degrees;
		float eY = (float)endP.latitude.degrees;
		float[][] res = getInsertProfileDatas(sX, sY, eX, eY, maxNum);
		List<Position> result = new ArrayList<Position>(res.length);
		for (int i = 0; i < res.length; i++) {
			result.add(Position.fromDegrees(res[i][1], res[i][0]));
		}
		return result;
	}
	/**
	 * 根据传入的两个点 进行加密插值
	 * @param startX 经度
	 * @param startY
	 * @param endX
	 * @param endY
	 * @param maxNum-最多切分多少个点(最小值为4)
	 * @return 第一列为 经度 第二列为纬度
	 */
	public static float[][] getInsertProfileDatas(float startX, float startY, float endX, float endY, int maxNum) {
		if (Math.abs(startX - endX) < 0.01f && Math.abs(startY - endY) < 0.01f) {
			return new float[][]{{startX, startY}, {endX, endY}};
		}
		
		ArrayList<Float> sampleLon = new ArrayList<Float>();
		ArrayList<Float> sampleLat = new ArrayList<Float>();
		if (maxNum <4)
			maxNum = 4;
		int numbers = 0;
		float length = (float)Math.sqrt((startX - endX) * (startX - endX) + (startY - endY) * (startY - endY));
		if (length <= 4) {
			numbers = 4;
			for (int i = 0; i < numbers; i++) {
				float tempLontitude = startX + i* (endX - startX) / (numbers - 1);
				if (tempLontitude > 180)
					tempLontitude -= 360;
				sampleLon.add(tempLontitude);
				sampleLat.add(startY + i* (endY - startY) / (numbers - 1)); 
			}
		} else if (length <= 10) {
			numbers = 10;
			if (numbers > maxNum) {
				numbers = maxNum;
			}
			for (int i = 0; i < numbers; i++) {
				float tempLontitude = startX + i* (endX - startX) / (numbers - 1);
				if (tempLontitude > 180)
					tempLontitude -= 360;
				sampleLon.add(tempLontitude);
				sampleLat.add(startY + i* (endY - startY) / (numbers - 1)); 
			}
		} else if (length <= 20){
			float delt = 0.25f;
			numbers = (int)(length/delt) + 1;
			if (numbers > maxNum)
				numbers = maxNum;
			for (int i = 0; i < numbers; i++) {
				float tempLontitude = startX + i* (endX - startX) / (numbers - 1);
				if (tempLontitude > 180)
					tempLontitude -= 360;
				sampleLon.add(tempLontitude);
				sampleLat.add(startY + i* (endY - startY) / (numbers - 1)); 
			}
		} else if (length > 20){
			float delt = 0.5f;
			numbers = (int)(length/delt) + 1;
			if (numbers > maxNum)
				numbers = maxNum;
			for (int i = 0; i < numbers; i++) {
				float tempLontitude = startX + i* (endX - startX) / (numbers - 1);
				if (tempLontitude > 180)
					tempLontitude -= 360;
				sampleLon.add(tempLontitude);
				sampleLat.add(startY + i* (endY - startY) / (numbers - 1)); 
			}
		}
		float[][] latLons = new float[sampleLat.size()][2];
		for (int i = 0; i < latLons.length; i++) {
			latLons[i][0] = sampleLon.get(i);
			latLons[i][1] = sampleLat.get(i);
		}
		return latLons;
	}
	/**
	 * 根据传入的两个点 进行加密插值 无限制
	 * @param startX 经度
	 * @param startY
	 * @param endX
	 * @param endY
	 * @param number-切分多少个点
	 * @return 第一列为 经度 第二列为纬度
	 */
	public static float[][] getInsertProfileDatas_NO_Limit(float startX, float startY, float endX, float endY, int number) {
		if (Math.abs(startX - endX) < 0.01f && Math.abs(startY - endY) < 0.01f) {
			return new float[][]{{startX, startY}, {endX, endY}};
		}
		
		ArrayList<Float> sampleLon = new ArrayList<Float>();
		ArrayList<Float> sampleLat = new ArrayList<Float>();
		if (number < 0) {
			number = 10;
		}
		int numbers = number;
		for (int i = 0; i < numbers; i++) {
			float tempLontitude = startX + i* (endX - startX) / (numbers - 1);
			if (tempLontitude > 180)
				tempLontitude -= 360;
			sampleLon.add(tempLontitude);
			sampleLat.add(startY + i* (endY - startY) / (numbers - 1)); 
		}
		float[][] latLons = new float[sampleLat.size()][2];
		for (int i = 0; i < latLons.length; i++) {
			latLons[i][0] = sampleLon.get(i);
			latLons[i][1] = sampleLat.get(i);
		}
		return latLons;
	}
	
	/**
	 * 兰勃托逐点计算经纬度(注意使用时传入的纬度像素坐标点从低到高递增，方法内部会将其从高纬到低纬自动翻转)
	 * @param sx 网格横向位置
	 * @param sy 网格纵向位置
	 * @param lat0 纬度0
	 * @param lat1 纬度标准30
	 * @param lat2 纬度标准60
	 * @param lon0 中心经度110
	 * @return 返回存放经纬度的数组 {lon, lat}
	 */
	public static double[] lbtprojToLonLat(int sx, int sy, double lat0, double lat1, double lat2, double lon0, int imageHeight){
		if (imageHeight == 0) {
			imageHeight = 512;
		}
		double a = 6378245;
		double b = 6356863.0188;
		double mypi = Math.PI;	
		double x, y, f, mye, m1, m2, n, t0, t1, t2;
		double sxTemp = -3317465.28335579 + sx * 13000.0;
		double syTemp= 397490.788472539 + (511 - sy) * 13000.0;
		double mylat0 = mypi * lat0 / 180.0;
		double mylat1 = mypi * lat1 / 180.0;
		double mylat2 = mypi * lat2 / 180.0;
		long ddstepnum;
		double lon, lat, ddlatst, ddlatend, maxwucha, r0, myr, myt, myse;
		double mylon = mypi * lon0 / 180.0;
		double[] latWc;
//		maxwucha = 0.000000001d;
		maxwucha = 0.001d;
		//可能有误
//			x = sy;
//			y = sx;
		x = syTemp;
		y = sxTemp;
		mye = Math.sqrt((1 - Math.pow(b / a, 2.0)));
		m1 = Math.cos(mylat1) / Math.sqrt(1 - Math.pow(mye * Math.sin(mylat1), 2));
		m2 = Math.cos(mylat2) / Math.sqrt(1 - Math.pow(mye * Math.sin(mylat2), 2));
		t0 = Math.tan(Math.PI / 4 - mylat0 / 2) / Math.pow((1 - mye * Math.sin(mylat0)) / (1 + mye * Math.sin(mylat0)), mye / 2);
		t1 = Math.tan(Math.PI / 4 - mylat1 / 2) / Math.pow((1 - mye * Math.sin(mylat1)) / (1 + mye * Math.sin(mylat1)), mye / 2);
		t2 = Math.tan(Math.PI / 4 - mylat2 / 2) / Math.pow((1 - mye * Math.sin(mylat2)) / (1 + mye * Math.sin(mylat2)), mye / 2);
		n = Math.log(m1 / m2) / Math.log(t1 / t2);
		f = m1 / (n * Math.pow(t1, n));
		r0 = a * f * (Math.pow(t0, n));
		myr = Math.sqrt(Math.pow(y, 2) + Math.pow(r0 - x, 2));
		myt = Math.pow(myr / (a * f), 1d / n);
		myse = Math.atan(y / (r0 - x));
		lon = myse / n + mylon;
		lon = lon * 180 / mypi;
		//纬度迭代计算，当迭代函数返回最小误差值小于maxwucha时停止迭代
		ddstepnum = 10;
		ddlatst  = -mypi / 2;
		ddlatend = mypi / 2;
		while (true) {
			latWc = latdiedai(ddstepnum, ddlatst, ddlatend, mye, myt);
			if(Math.abs(latWc[0]) < 100) {
				if(latWc[1] > maxwucha){
					ddlatst = latWc[0] - latWc[1];
					ddlatend = latWc[0] + latWc[1];
				} else {
					lat = latWc[0] * 180 / mypi;
					break;
				}
			} else {
				lat = 100;
				break;
			} 
		}
		return new double[]{lon, lat};
	}
	//迭代计算函数输入参数迭代次数，起始值，终止值，t'值，第一偏心率e值，返回值为误差最小的值和最小误差
	//返回结果为 {lat, wucha}
	private static double[] latdiedai(long dtstepnum, double dtlatst, double dtlatend, double mye, double myt){
		double mypi = Math.PI;
		double lat, dtlat, dtlattemp, dtwctemp, dtlatstep;
		long i;
		dtlatstep = (dtlatend - dtlatst) / dtstepnum;
		dtwctemp = 100 * dtlatstep;
		dtlat = 100;
		for(i = 0; i < dtstepnum; i++) {
			lat = dtlatst + i * dtlatstep;
			dtlattemp = mypi / 2 - 2 * Math.atan(myt * Math.pow((1 - mye * Math.sin(lat))/(1 + mye * Math.sin(lat)), mye/2 ));
			if(Math.abs(lat - dtlattemp) < dtwctemp){
				dtwctemp = Math.abs(lat - dtlattemp);
				dtlat = lat;
			}
		}
		return new double[]{dtlat, dtwctemp};
	}
	/**
	 *  麦卡托投影变换
	 * @param sx 网格横向位置
	 * @param sy 网格纵向位置
	 * @param lon0 起始经度
	 * @param deltLon 经度分辨率
	 * @param startPrecent 纵向起始比例(相对于 -85°~85°，非线性比例)
	 * @param eachPrecent 比例分辨率
	 * @return
	 */
	public static double[] mktprojToLonLat(int sx, int sy, double lon0, double deltLon, double startPrecent, double eachPrecent){
		double lat,lon;
		//sy 为（-1， 1）对应(-85°, 85°)
		double precent = startPrecent + eachPrecent * sy;
		lat = Angle.fromRadians(Math.atan(Math.sinh(precent * Math.PI))).degrees;
		lon = sx * deltLon + lon0;
		return new double[]{lon, lat};
	}	
	

	/**
	 * 判断一个点是否在一个封闭区域中
	 * @param curPos
	 * @param areaPositions
	 * @return
	 */
	public static boolean isInArea(Position curPos, List<LatLonPoint> areaPositions) {
		if (curPos == null || areaPositions == null || areaPositions.size() < 3) {
			return false;
		}
		Path2D path = new Path2D.Double();
		int index = 0;
		LatLonPoint first = null;
		for (LatLonPoint point: areaPositions) {
			if (point == null) {
				continue;
			}
			if (index == 0) {
				first = point;
				path.moveTo(point.longitude, point.latitude);
			} else {
				path.lineTo(point.longitude, point.latitude);
			}
			index++;
			if (index == areaPositions.size() && first != null) {
				path.lineTo(first.longitude, first.latitude);
			}
		}
		if (index < 3) {
			return false;
		}
		return path.contains(new Point2D.Double(curPos.longitude.degrees, curPos.latitude.degrees));
	}
	/**
	 * 处理当点列中相邻值距离小于boundary时 抽稀过滤掉
	 * @param x
	 * @param y
	 * @param boundary
	 * @return {x[], y[]}
	 */
	public static float[][] filterPointList(float[] x, float[] y, float boundary) {
		if (x == null || y == null || x.length != y.length || x.length < 2) {
			return new float[][] {x, y};
		}
		ArrayList<Float> xlist = new ArrayList<Float>();
		ArrayList<Float> ylist = new ArrayList<Float>();
		xlist.add(x[0]);
		ylist.add(y[0]);
		for (int i = 0; i < x.length - 1; i++) {
			if (Math.abs(x[i + 1] - x[i]) + Math.abs(y[i + 1] - y[i]) < boundary) {
				continue;
			}
			xlist.add(x[i + 1]);
			ylist.add(y[i + 1]);
		}
		float[] xout = new float[xlist.size()];
		float[] yout = new float[ylist.size()];
		for (int i =0; i < xout.length; i++) {
			xout[i] = xlist.get(i);
			yout[i] = ylist.get(i);
		}
		if (xout.length != x.length) {
			return filterPointList(xout, yout, boundary);
		}
		return new float[][] {xout, yout};
	}
	
	//数组平移
	public static float[] shiftArray(float[] arr, int shift) {
		if (arr == null || arr.length < 2) {
			return arr;
		}
		if (shift == 0) {
			return arr;
		}
		float[] result = new float[arr.length];
		for (int i = 0; i < arr.length; i++) {
			if (i < arr.length - shift) {
				result[i] = arr[i + shift];	
			} else if (i != arr.length - 1) {
				result[i] = arr[i + shift - arr.length];
			} else {
				result[i] = result[0];
			}
		}
		return result;
	}
	/**
	 * 对闭合曲线列表的初始位置进行改变使其起始夹角大于angleBounder(°;度)
	 * 方便平滑后的曲线尽量不出现尖角
	 * @param positionIn 传入的闭合曲线
	 * @param angleBounder 控制起始夹角的阀值
	 * @return 返回处理后的闭合曲线
	 */
	public static List<Position> shiftClosedPositionList(List<Position> positionIn, float angleBounder) {
		if (positionIn == null || positionIn.size() < 4) {
			return positionIn;
		}
		
		if (angleBounder > 180 || angleBounder < 0) {
			angleBounder = 100;
		}
		if (getAngle(positionIn.get(1), positionIn.get(0), positionIn.get(positionIn.size() - 2)) > angleBounder) {
			return positionIn;
		}
		int remark = 1;
		for (remark = 1; remark < positionIn.size() - 1; remark++) {
			if (getAngle(positionIn.get(remark - 1), positionIn.get(remark), positionIn.get(remark + 1)) > angleBounder) {
				break;
			}
		}
		if (remark == positionIn.size()) {
			return positionIn;
		}
		List<Position> resultList = new ArrayList<Position>(positionIn.size());
		List<Position> subList1 = positionIn.subList(remark, positionIn.size());
		if (subList1 != null && !subList1.isEmpty()) {
			resultList.addAll(subList1);
		}
		List<Position> subList2 = positionIn.subList(1, remark);
		if (subList2 != null && !subList2.isEmpty()) {
			resultList.addAll(subList2);
		}
		resultList.add(positionIn.get(remark));
		return resultList;
	}
	//获取三个点的夹角∠p1 vert p2;范围0~180
	private static float getAngle(Position p1, Position vert, Position p2) {
		double x1 = p1.longitude.degrees - vert.longitude.degrees;
		double y1 = p1.latitude.degrees - vert.latitude.degrees;
		
		double x2 = p2.longitude.degrees - vert.longitude.degrees;
		double y2 = p2.latitude.degrees - vert.latitude.degrees;
		
		return getAngle(x1, y1, x2, y2);
	}
	//获取两个向量的夹角 (x1, y1)、(x2, y2);范围0~180
	private static float getAngle(double x1, double y1, double x2, double y2) {
		double angle = Math.acos((x1 * x2 + y1 * y2) / (Math.sqrt(x1 * x1 + y1 * y1) * Math.sqrt(x2 * x2 + y2 * y2)));
		return (float)(angle / Math.PI * 180);
	}
	
	/**
	 * db_x 2013-11-4
	 * 
	 * 处理格点数据的工具
	 * 利用双线性插值把给定的格点数据中空值全部插上结果
	 * @param gd 格点数据
	 * @return 处理后的格点数据
	 */
	public static GridData fillNullGridData(GridData gd) {
		if (gd == null)
			return gd;
		int rows = gd.getRowNum();
		int cols = gd.getColNum();
		float[][] dataArr = gd.getGridData();
		int nullNum = 0;
		for (int i = 0; i < dataArr.length; i++) {
			for (int j = 0; j < dataArr[0].length; j++) {
				if (dataArr[i][j] == NULLVAL)
					nullNum++;
			}
		}
		if (nullNum == rows * cols) {
			System.out.println("传入的格点数据中全为空值");
			return gd;
		}
		float num;
		double sum;
		float bfRate = 3;
		float diRate = 2;
		float cehjRate = 1.5f;
		float agRate = 0.2f;
		float totalRate = 2*bfRate + 2*diRate + 4*cehjRate + 2*agRate;
		while (nullNum > 0) {
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					if (dataArr[i][j] == NULLVAL) {
						num = 0.0f;
						sum = 0.0f;
						//需要判断BDFI四个点是不都为空，都为空时不插值
						if ((i == 0 && j == 0 && dataArr[1][0] == NULLVAL && dataArr[0][1] == NULLVAL)// 左下角或左上角
					    || (i == 0 && j == cols - 1 &&dataArr[0][cols - 2] == NULLVAL && dataArr[1][cols - 1] == NULLVAL)// 右下角或右上角
						|| (i == rows - 1 && j == 0 && dataArr[rows - 1][1] == NULLVAL && dataArr[rows - 2][0] == NULLVAL)// 左上角或左下角
						|| (i == rows - 1 && j == cols - 1 && dataArr[rows - 1][cols - 2] == NULLVAL && dataArr[rows - 2][cols - 1] == NULLVAL)// 右上角或右下角
						|| (i == 0 && j != 0 && j != cols - 1 && dataArr[1][0] == NULLVAL && dataArr[1][j] == NULLVAL && dataArr[0][j + 1] == NULLVAL)// 下边框或上边框
						|| (i != 0 && i != rows - 1 && j == cols - 1 && dataArr[i][j - 1] == NULLVAL && dataArr[i + 1][j] == NULLVAL && dataArr[i - 1][j] == NULLVAL)// 右边框
						|| (i == rows - 1 && j != cols - 1 && j != 0 && dataArr[i][j - 1] == NULLVAL && dataArr[i][j + 1] == NULLVAL  && dataArr[i - 1][j] == NULLVAL)// 上边框或下边框
						|| (i != 0 && i != rows - 1 && j == 0 && dataArr[1][0] == NULLVAL && dataArr[i + 1][j] == NULLVAL && dataArr[i - 1][j] == NULLVAL)// 左边框
					    || (!(i == 0 || j == 0 || i == rows - 1 || j == cols - 1) && dataArr[i][j + 1] == NULLVAL 
					    	&& dataArr[i][j - 1] == NULLVAL && dataArr[i + 1][j] == NULLVAL && dataArr[i - 1][j] == NULLVAL)) {// 中间
							continue;
						} else {
							float rate = 0;
							if (j - 1 >= 0 && dataArr[i][j - 1] != NULLVAL) {// B点
								rate = bfRate;
								num = num + rate;
								sum = sum + dataArr[i][j - 1] * rate;
							}
							if (j - 2 >= 0 && dataArr[i][j - 2] != NULLVAL) {// A点
								rate = agRate;
								num = num + rate;
								sum = sum + dataArr[i][j - 2] * rate;
							}
							if (j + 1 <= cols - 1 && dataArr[i][j +1] != NULLVAL) {// F点
								rate = bfRate;
								num = num + rate;
								sum = sum + dataArr[i][j +1] * rate;
							}
							if (j + 2 <= cols - 1 && dataArr[i][j +2] != NULLVAL) {// G点
								rate = agRate;
								num = num + rate;
								sum = sum + dataArr[i][j +2] * rate;
							}
							if (i + 1 <= rows - 1 && j - 1 >= 0 && dataArr[i + 1][j - 1] != NULLVAL) {// H点
								rate = cehjRate;
								num = num + rate;
								sum = sum + dataArr[i + 1][j - 1] * rate;
							}
							if (i + 1 <= rows - 1 && dataArr[i + 1][j] != NULLVAL) {// I点
								rate = diRate;
								num = num + rate;
								sum = sum + dataArr[i + 1][j] * rate;
							}
							if (i + 1 <= rows - 1 && j + 1 <= cols - 1 && dataArr[i + 1][j + 1] != NULLVAL) {// J点
								rate = cehjRate;
								num = num + rate;
								sum = sum + dataArr[i + 1][j + 1] * rate;
							}
							if (i - 1 >= 0 && j - 1 >= 0 && dataArr[i - 1][j - 1] != NULLVAL) {// C点
								rate = cehjRate;
								num = num + rate;
								sum = sum + dataArr[i - 1][j - 1] * rate;
							}
							if (i - 1 >= 0 && dataArr[i - 1][j] != NULLVAL) {// I点或D
								rate = diRate;
								num = num + rate;
								sum = sum + dataArr[i - 1][j] * rate;
							}
							if (i - 1 >= 0 && j + 1 <= cols - 1 && dataArr[i - 1][j + 1] != NULLVAL) {// E点
								rate = cehjRate;
								num = num + rate;
								sum = sum + dataArr[i - 1][j + 1] * rate;
							}
							
							if (num > totalRate * 0.25f) {	
								dataArr[i][j] = (float) (sum / num);								
								nullNum--;
							}
						}
					}
				}
			}
		}
		gd.setGridData(dataArr);
		return gd;
	}
	
	/**
	 * db_x 2013-3-28
	 * 
	 * 处理格点数据的工具
	 * 利用双线性插值把给定的格点数据中空值全部插上结果
	 * @param gd 格点数据
	 * @param cycleTimes 控制循环次数(上限设为50次 )
	 * @return 处理后的格点数据
	 */
	public static GridData fillNullGridData(GridData gd, int cycleTimes) {
		if (gd == null) {
			return gd;
		}
		if (cycleTimes < 1) {
			cycleTimes = 1;
		}
		if (cycleTimes > 50) {
			cycleTimes = 50;
		} 
		int rows = gd.getRowNum();
		int cols = gd.getColNum();
		float[][] dataArr = gd.getGridData();
		int nullNum = 0;
		for (int i = 0; i < dataArr.length; i++) {
			for (int j = 0; j < dataArr[0].length; j++) {
				if (dataArr[i][j] == NULLVAL)
					nullNum++;
			}
		}
		if (nullNum == rows * cols) {
			System.out.println("传入的格点数据中全为空值");
			return gd;
		}
		float num;
		double sum;
		float bfRate = 3;
		float diRate = 2;
		float cehjRate = 1.5f;
		float agRate = 0.2f;
		float totalRate = 2*bfRate + 2*diRate + 4*cehjRate + 2*agRate;
		int count = 0;
		while (nullNum > 0) {
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					if (dataArr[i][j] == NULLVAL) {
						num = 0.0f;
						sum = 0.0f;
						//需要判断BDFI四个点是不都为空，都为空时不插值
						if ((i == 0 && j == 0 && dataArr[1][0] == NULLVAL && dataArr[0][1] == NULLVAL)// 左下角或左上角
								|| (i == 0 && j == cols - 1 &&dataArr[0][cols - 2] == NULLVAL && dataArr[1][cols - 1] == NULLVAL)// 右下角或右上角
								|| (i == rows - 1 && j == 0 && dataArr[rows - 1][1] == NULLVAL && dataArr[rows - 2][0] == NULLVAL)// 左上角或左下角
								|| (i == rows - 1 && j == cols - 1 && dataArr[rows - 1][cols - 2] == NULLVAL && dataArr[rows - 2][cols - 1] == NULLVAL)// 右上角或右下角
								|| (i == 0 && j != 0 && j != cols - 1 && dataArr[1][0] == NULLVAL && dataArr[1][j] == NULLVAL && dataArr[0][j + 1] == NULLVAL)// 下边框或上边框
								|| (i != 0 && i != rows - 1 && j == cols - 1 && dataArr[i][j - 1] == NULLVAL && dataArr[i + 1][j] == NULLVAL && dataArr[i - 1][j] == NULLVAL)// 右边框
								|| (i == rows - 1 && j != cols - 1 && j != 0 && dataArr[i][j - 1] == NULLVAL && dataArr[i][j + 1] == NULLVAL  && dataArr[i - 1][j] == NULLVAL)// 上边框或下边框
								|| (i != 0 && i != rows - 1 && j == 0 && dataArr[1][0] == NULLVAL && dataArr[i + 1][j] == NULLVAL && dataArr[i - 1][j] == NULLVAL)// 左边框
								|| (!(i == 0 || j == 0 || i == rows - 1 || j == cols - 1) && dataArr[i][j + 1] == NULLVAL 
								&& dataArr[i][j - 1] == NULLVAL && dataArr[i + 1][j] == NULLVAL && dataArr[i - 1][j] == NULLVAL)) {// 中间
							continue;
						} else {
							float rate = 0;
							if (j - 1 >= 0 && dataArr[i][j - 1] != NULLVAL) {// B点
								rate = bfRate;
								num = num + rate;
								sum = sum + dataArr[i][j - 1] * rate;
							}
							if (j - 2 >= 0 && dataArr[i][j - 2] != NULLVAL) {// A点
								rate = agRate;
								num = num + rate;
								sum = sum + dataArr[i][j - 2] * rate;
							}
							if (j + 1 <= cols - 1 && dataArr[i][j +1] != NULLVAL) {// F点
								rate = bfRate;
								num = num + rate;
								sum = sum + dataArr[i][j +1] * rate;
							}
							if (j + 2 <= cols - 1 && dataArr[i][j +2] != NULLVAL) {// G点
								rate = agRate;
								num = num + rate;
								sum = sum + dataArr[i][j +2] * rate;
							}
							if (i + 1 <= rows - 1 && j - 1 >= 0 && dataArr[i + 1][j - 1] != NULLVAL) {// H点
								rate = cehjRate;
								num = num + rate;
								sum = sum + dataArr[i + 1][j - 1] * rate;
							}
							if (i + 1 <= rows - 1 && dataArr[i + 1][j] != NULLVAL) {// I点
								rate = diRate;
								num = num + rate;
								sum = sum + dataArr[i + 1][j] * rate;
							}
							if (i + 1 <= rows - 1 && j + 1 <= cols - 1 && dataArr[i + 1][j + 1] != NULLVAL) {// J点
								rate = cehjRate;
								num = num + rate;
								sum = sum + dataArr[i + 1][j + 1] * rate;
							}
							if (i - 1 >= 0 && j - 1 >= 0 && dataArr[i - 1][j - 1] != NULLVAL) {// C点
								rate = cehjRate;
								num = num + rate;
								sum = sum + dataArr[i - 1][j - 1] * rate;
							}
							if (i - 1 >= 0 && dataArr[i - 1][j] != NULLVAL) {// I点或D
								rate = diRate;
								num = num + rate;
								sum = sum + dataArr[i - 1][j] * rate;
							}
							if (i - 1 >= 0 && j + 1 <= cols - 1 && dataArr[i - 1][j + 1] != NULLVAL) {// E点
								rate = cehjRate;
								num = num + rate;
								sum = sum + dataArr[i - 1][j + 1] * rate;
							}
							
							if (num > totalRate * 0.25f) {	
								dataArr[i][j] = (float) (sum / num);								
								nullNum--;
							}
						}
					}
				}
			}
			if (count++ > cycleTimes) {
				break;
			}
		}
		gd.setGridData(dataArr);
		return gd;
	}	
	
	/**
	 * 西北太平洋-海洋空值填充
	 * @param data
	 * @return
	 */
	public static float[][] fillNullVal(float[][] data) {
		int nullNum = 0;
		int rows = data.length;
		int cols = data[0].length;
		float[][] newData = new float[rows][cols];
		boolean[][] isSourceVal = new boolean[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				newData[i][j] = data[i][j];
				if (newData[i][j] == NULLVAL) {
					nullNum++;
					isSourceVal[i][j] = false;
				} else {
					isSourceVal[i][j] = true;
				}
			}
		}

		// 插值出Null网格数据
		// |----|----|----|----|----|
		// | | | | | |
		// |----C----D----E----------
		// | | | | | |
		// A----B--(3,3)--F----G-----
		// | | | | | |
		// |----H----I----J----------
		// | | | | | |
		// |----|----|----|----|----|
		// Data(3,3) = (B*1 + A + C + H + F*3 + G + E + J + D*1 + I*1)/n
		int scale = 10;
		int num;
		double sum;
		int gDataFlag = 1;
		long tickS = System.currentTimeMillis();
		long tickE;
		while (gDataFlag > 0) {
			gDataFlag = 0;
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					if (newData[i][j] == NULLVAL) {
						num = 0;
						sum = 0.0f;

						if ((i == 0 && j == 0 && newData[1][0] == NULLVAL && newData[0][1] == NULLVAL)// 左下角或左上角
								|| (i == 0 && j == cols - 1
										&& newData[0][cols - 2] == NULLVAL && newData[1][cols - 1] == NULLVAL)// 右下角或右上角
								|| (i == rows - 1 && j == 0
										&& newData[rows - 1][1] == NULLVAL && newData[rows - 2][0] == NULLVAL)// 左上角或左下角
								|| (i == rows - 1 && j == cols - 1
										&& newData[rows - 1][cols - 2] == NULLVAL && newData[rows - 2][cols - 1] == NULLVAL)// 右上角或右下角
								|| (i == 0 && j != 0 && j != cols - 1
										&& newData[0][j - 1] == NULLVAL
										&& newData[1][j] == NULLVAL && newData[0][j + 1] == NULLVAL)// 下边框或上边框
								|| (i != 0 && i != rows - 1 && j == cols - 1
										&& newData[i][j - 1] == NULLVAL
										&& newData[i + 1][j] == NULLVAL && newData[i - 1][j] == NULLVAL)// 右边框
								|| (i == rows - 1 && j != cols - 1 && j != 0
										&& newData[i][j - 1] == NULLVAL
										&& newData[i][j + 1] == NULLVAL && newData[i - 1][j] == NULLVAL)// 上边框或下边框
								|| (i != 0 && i != rows - 1 && j == 0
										&& newData[i][j + 1] == NULLVAL
										&& newData[i + 1][j] == NULLVAL && newData[i - 1][j] == NULLVAL)// 左边框
								|| (!(i == 0 || j == 0 || i == rows - 1 || j == cols - 1)
										&& newData[i][j + 1] == NULLVAL
										&& newData[i][j - 1] == NULLVAL
										&& newData[i + 1][j] == NULLVAL && newData[i - 1][j] == NULLVAL)) {// 中间
							newData[i][j] = NULLVAL;
						} else {
							if (j - 1 >= 0 && newData[i][j - 1] != NULLVAL) {// B点
								int power = 1;
								if (isSourceVal[i][j - 1]) {
									power *= scale;
								}
								num = num + power;
								sum = sum + newData[i][j - 1] * power;
							}
							if (j - 2 >= 0 && newData[i][j - 2] != NULLVAL) {// A点
								int power = 1;
								if (isSourceVal[i][j - 2]) {
									power *= scale;
								}
								num = num + power;
								sum = sum + newData[i][j - 2] * power;
							}
							if (j + 1 <= cols - 1 && newData[i][j + 1] != NULLVAL) {// F点
								int power = 3;
								if (isSourceVal[i][j + 1]) {
									power *= scale;
								}
								num = num + power;
								sum = sum + newData[i][j + 1] * power;
							}
							if (j + 2 <= cols - 1 && newData[i][j + 2] != NULLVAL) {// G点
								int power = 1;
								if (isSourceVal[i][j + 2]) {
									power *= scale;
								}
								num = num + power;
								sum = sum + newData[i][j + 2] * power;
							}
							if (i + 1 <= rows - 1 && j - 1 >= 0
									&& newData[i + 1][j - 1] != NULLVAL) {// C点或H
								int power = 1;
								if (isSourceVal[i + 1][j - 1]) {
									power *= scale;
								}
								num = num + power;
								sum = sum + newData[i + 1][j - 1] * power;
							}
							if (i + 1 <= rows - 1 && newData[i + 1][j] != NULLVAL) {// D点或I
								int power = 1;
								if (isSourceVal[i + 1][j]) {
									power *= scale;
								}
								num = num + power;
								sum = sum + newData[i + 1][j] * power;
							}
							if (i + 1 <= rows - 1 && j + 1 <= cols - 1
									&& newData[i + 1][j + 1] != NULLVAL) {// E点或J
								int power = 1;
								if (isSourceVal[i + 1][j + 1]) {
									power *= scale;
								}
								num = num + power;
								sum = sum + newData[i + 1][j + 1] * power;
							}
							if (i - 1 >= 0 && j - 1 >= 0 && newData[i - 1][j - 1] != NULLVAL) {// H点或C
								int power = 1;
								if (isSourceVal[i - 1][j - 1]) {
									power *= scale;
								}
								num = num + power;
								sum = sum + newData[i - 1][j - 1] * power;
							}
							if (i - 1 >= 0 && newData[i - 1][j] != NULLVAL) {// I点或D
								int power = 1;
								if (isSourceVal[i - 1][j]) {
									power *= scale;
								}
								num = num + power;
								sum = sum + newData[i - 1][j] * power;
							}
							if (i - 1 >= 0 && j + 1 <= cols - 1 && newData[i - 1][j + 1] != NULLVAL) {// J点或E
								int power = 1;
								if (isSourceVal[i - 1][j + 1]) {
									power *= scale;
								}
								num = num + power;
								sum = sum + newData[i - 1][j + 1] * power;
							}
							// if (num != 0) {
							// newData[i][j] = (float) (sum / num);
							// nullNum--;
							// } else {
							// newData[i][j] = NULLVAL;
							// }
							if (num > 3) {
								newData[i][j] = (float) (sum / num);
								nullNum--;
							} else {
								newData[i][j] = NULLVAL;
							}
						}
						gDataFlag++;
					}// endif

				}// endfor j
			}// endfor i
			tickE = System.currentTimeMillis() - tickS;
			if (tickE > 2000 && gDataFlag > 0) {
				System.out.println("插值网格数据超时. 可能是数据量太少或者数据分布过于不均匀引起");
				return null;
			}
			if (nullNum == 0) {
				break;
			}
		}

		return newData;
	}
	/**
	 * 空值填充
	 * @param data
	 * @param cycleTimes 控制循环次数(上限为50)
	 * @return
	 */
	public static float[][] fillNullVal(float[][] data, int cycleTimes) {
		if (data == null) {
			return data;
		}
		if (cycleTimes < 1) {
			cycleTimes = 1;
		}
		if (cycleTimes > 50) {
			cycleTimes = 50;
		} 
		int nullNum = 0;
		int rows = data.length;
		int cols = data[0].length;
		float[][] newData = new float[rows][cols];
		boolean[][] isSourceVal = new boolean[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				newData[i][j] = data[i][j];
				if (newData[i][j] == NULLVAL) {
					nullNum++;
					isSourceVal[i][j] = false;
				} else {
					isSourceVal[i][j] = true;
				}
			}
		}
		
		// 插值出Null网格数据
		// |----|----|----|----|----|
		// | | | | | |
		// |----C----D----E----------
		// | | | | | |
		// A----B--(3,3)--F----G-----
		// | | | | | |
		// |----H----I----J----------
		// | | | | | |
		// |----|----|----|----|----|
		// Data(3,3) = (B*1 + A + C + H + F*3 + G + E + J + D*1 + I*1)/n
		int scale = 10;
		int num;
		double sum;
		int gDataFlag = 1;
		long tickS = System.currentTimeMillis();
		long tickE;
		int count = 0;
		while (gDataFlag > 0) {
			gDataFlag = 0;
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					if (newData[i][j] == NULLVAL) {
						num = 0;
						sum = 0.0f;
						
						if ((i == 0 && j == 0 && newData[1][0] == NULLVAL && newData[0][1] == NULLVAL)// 左下角或左上角
								|| (i == 0 && j == cols - 1
								&& newData[0][cols - 2] == NULLVAL && newData[1][cols - 1] == NULLVAL)// 右下角或右上角
								|| (i == rows - 1 && j == 0
								&& newData[rows - 1][1] == NULLVAL && newData[rows - 2][0] == NULLVAL)// 左上角或左下角
								|| (i == rows - 1 && j == cols - 1
								&& newData[rows - 1][cols - 2] == NULLVAL && newData[rows - 2][cols - 1] == NULLVAL)// 右上角或右下角
								|| (i == 0 && j != 0 && j != cols - 1
								&& newData[0][j - 1] == NULLVAL
								&& newData[1][j] == NULLVAL && newData[0][j + 1] == NULLVAL)// 下边框或上边框
								|| (i != 0 && i != rows - 1 && j == cols - 1
								&& newData[i][j - 1] == NULLVAL
								&& newData[i + 1][j] == NULLVAL && newData[i - 1][j] == NULLVAL)// 右边框
								|| (i == rows - 1 && j != cols - 1 && j != 0
								&& newData[i][j - 1] == NULLVAL
								&& newData[i][j + 1] == NULLVAL && newData[i - 1][j] == NULLVAL)// 上边框或下边框
								|| (i != 0 && i != rows - 1 && j == 0
								&& newData[i][j + 1] == NULLVAL
								&& newData[i + 1][j] == NULLVAL && newData[i - 1][j] == NULLVAL)// 左边框
								|| (!(i == 0 || j == 0 || i == rows - 1 || j == cols - 1)
										&& newData[i][j + 1] == NULLVAL
										&& newData[i][j - 1] == NULLVAL
										&& newData[i + 1][j] == NULLVAL && newData[i - 1][j] == NULLVAL)) {// 中间
							newData[i][j] = NULLVAL;
						} else {
							if (j - 1 >= 0 && newData[i][j - 1] != NULLVAL) {// B点
								int power = 1;
								if (isSourceVal[i][j - 1]) {
									power *= scale;
								}
								num = num + power;
								sum = sum + newData[i][j - 1] * power;
							}
							if (j - 2 >= 0 && newData[i][j - 2] != NULLVAL) {// A点
								int power = 1;
								if (isSourceVal[i][j - 2]) {
									power *= scale;
								}
								num = num + power;
								sum = sum + newData[i][j - 2] * power;
							}
							if (j + 1 <= cols - 1 && newData[i][j + 1] != NULLVAL) {// F点
								int power = 3;
								if (isSourceVal[i][j + 1]) {
									power *= scale;
								}
								num = num + power;
								sum = sum + newData[i][j + 1] * power;
							}
							if (j + 2 <= cols - 1 && newData[i][j + 2] != NULLVAL) {// G点
								int power = 1;
								if (isSourceVal[i][j + 2]) {
									power *= scale;
								}
								num = num + power;
								sum = sum + newData[i][j + 2] * power;
							}
							if (i + 1 <= rows - 1 && j - 1 >= 0
									&& newData[i + 1][j - 1] != NULLVAL) {// C点或H
								int power = 1;
								if (isSourceVal[i + 1][j - 1]) {
									power *= scale;
								}
								num = num + power;
								sum = sum + newData[i + 1][j - 1] * power;
							}
							if (i + 1 <= rows - 1 && newData[i + 1][j] != NULLVAL) {// D点或I
								int power = 1;
								if (isSourceVal[i + 1][j]) {
									power *= scale;
								}
								num = num + power;
								sum = sum + newData[i + 1][j] * power;
							}
							if (i + 1 <= rows - 1 && j + 1 <= cols - 1
									&& newData[i + 1][j + 1] != NULLVAL) {// E点或J
								int power = 1;
								if (isSourceVal[i + 1][j + 1]) {
									power *= scale;
								}
								num = num + power;
								sum = sum + newData[i + 1][j + 1] * power;
							}
							if (i - 1 >= 0 && j - 1 >= 0 && newData[i - 1][j - 1] != NULLVAL) {// H点或C
								int power = 1;
								if (isSourceVal[i - 1][j - 1]) {
									power *= scale;
								}
								num = num + power;
								sum = sum + newData[i - 1][j - 1] * power;
							}
							if (i - 1 >= 0 && newData[i - 1][j] != NULLVAL) {// I点或D
								int power = 1;
								if (isSourceVal[i - 1][j]) {
									power *= scale;
								}
								num = num + power;
								sum = sum + newData[i - 1][j] * power;
							}
							if (i - 1 >= 0 && j + 1 <= cols - 1 && newData[i - 1][j + 1] != NULLVAL) {// J点或E
								int power = 1;
								if (isSourceVal[i - 1][j + 1]) {
									power *= scale;
								}
								num = num + power;
								sum = sum + newData[i - 1][j + 1] * power;
							}
							// if (num != 0) {
							// newData[i][j] = (float) (sum / num);
							// nullNum--;
							// } else {
							// newData[i][j] = NULLVAL;
							// }
							if (num > 3) {
								newData[i][j] = (float) (sum / num);
								nullNum--;
							} else {
								newData[i][j] = NULLVAL;
							}
						}
						gDataFlag++;
					}// endif
					
				}// endfor j
			}// endfor i
			tickE = System.currentTimeMillis() - tickS;
			if (tickE > 2000 && gDataFlag > 0) {
				System.out.println("插值网格数据超时. 可能是数据量太少或者数据分布过于不均匀引起");
				return null;
			}
			if (nullNum == 0) {
				break;
			}
			if (count > cycleTimes) {
				break;
			}
		}
		return newData;
	}
}

/**
 * 网格化用
 *
 * @author 
 *
 */
 class GridDataFlagInfo {

	 public GridDataFlagInfo(int rows, int cols) {
		 gridData = new float[rows][cols];
		 flag = new boolean[rows][cols];
		 splineTimes = new int[rows][cols];
		 splineRate = new float[rows][cols];
	 }
	 
	 /**
	  * 输入数据
	  * @param gridData
	  */
	 public void setGridData(float[][] gridData) {
//		 this.gridData = new float[gridData.length][gridData[0].length];
		 this.gridData = gridData;
	 }	 
	 public float[][] getGridData() {
		 return this.gridData;
	 }
	 
	 /**
	  * 设置点xy位置的值
	  * @param i
	  * @param j
	  * @param val
	  */
	 public void setGridData(int i, int j, float val) {
		 gridData[i][j] = val;
	 }
	 public float getGridData(int i, int j) {
		 return this.gridData[i][j];
	 }
	 float[][] gridData; // 网格数据
	 
	 /**
	  * 设置点xy位置的标志
	  * @param i
	  * @param j
	  * @param flag
	  */
	 public void setFlag(int i, int j, boolean flag) {
		 this.flag[i][j] = flag;
	 }
	 public boolean getFlag(int i, int j) {
		 return this.flag[i][j];
	 }
	 boolean[][] flag; // 标记网格点插值与否
	 
	 public GridDataFlagInfo clone(){
		 int rows= gridData.length;
		 int cols = gridData[0].length;
		 GridDataFlagInfo gridFlagDataTemp = new GridDataFlagInfo(rows, cols);
			gridFlagDataTemp.gridData = new float[rows][cols];
			gridFlagDataTemp.flag = new boolean[rows][cols];
			gridFlagDataTemp.splineTimes = new int[rows][cols];
			gridFlagDataTemp.splineRate = new float[rows][cols];
			for(int i=0; i<rows; i++){
				for(int j=0; j<cols; j++){
					gridFlagDataTemp.gridData[i][j] = this.gridData[i][j];
					gridFlagDataTemp.flag[i][j] = this.flag[i][j];
					gridFlagDataTemp.splineTimes[i][j] = this.splineTimes[i][j];
					gridFlagDataTemp.splineRate[i][j] = this.splineRate[i][j];
				}
			}
			return gridFlagDataTemp;
	 }
	 
	 /**
	  * 平滑次数
	  * @param i
	  * @param j
	  * @param val
	  */
	 public void setSplineTimes(int i, int j, int val){
		 this.splineTimes[i][j] = val;
	 }
	 public int getSplineTimes(int i, int j){
		 return this.splineTimes[i][j];
	 }
	 int[][] splineTimes; //由第几次插值得到，控制权重
	 
	 public void setSplineRate(int i, int j, float val){
		 this.splineRate[i][j] = val;
	 }
	 public float getSplineRate(int i, int j){
		 return this.splineRate[i][j];
	 }
	 float[][] splineRate;
 }
