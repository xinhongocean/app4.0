package com.xinhong.mids3d.core.isoline;

import com.xinhong.mids3d.core.geom.PositionVec;
import com.xinhong.mids3d.core.isoline.IsolineUtil.IsolineSrcDataType;
import com.xinhong.mids3d.util.math.MIDS3DMath;
import com.xinhong.mids3d.util.math.StrArrayUtil;
import gov.nasa.worldwind.geom.Position;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.grib.GridData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.RuntimeErrorException;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

//import com.xinhong.mids3d.core.isoline.IsolineProcessAttr.GridedType;
//import com.xinhong.mids3d.wavewatch.data.DepgrdData;
//import com.xinhong.mids3d.wavewatch.data.MIDSData;
//import com.xinhong.mids3d.wavewatch.data.NCOriginalData;
//import com.xinhong.mids3d.wavewatch.data.NCSeaData;
//import com.xinhong.mids3d.wavewatch.data.ScatterData;
//import com.xinhong.mids3d.wavewatch.util.ElemCode;
//import com.xinhong.mids3d.util.ArrayOperator;
//import com.xinhong.mids3d.util.math.trianglation.Pnt;
//import com.xinhong.mids3d.util.math.trianglation.Triangle;

/**
 * 等值线处理通用类
 *
 * @author 1
 */
public class HYIsolineProcess {
    private static Log logger = LogFactory.getLog(HYIsolineProcess.class);
    protected IsolineProcessAttr attr; // 等值线属性
    protected ArrayList<GridData> gridDataList; // 格点数据
    //protected ArrayList<ScatterData> scatterDataList; // 离散点数据
    private boolean hasBilinear = false; //是否已双线性差值(加密处理)
    private boolean hasSpline = false; //是否已平滑

    private EdgeIsopointInfo[][] xSide;// 横边
    private EdgeIsopointInfo[][] ySide;// 纵边
    private float[][] gridDataArys;//数据
    private float[][] gridXArys;//经纬度坐标
    private float[][] gridYArys;//经纬度坐标
    private float[][] gridXYCoordX;//xy坐标系x坐标
    private float[][] gridXYCoordY;//xy坐标系y坐标
    private float[] realXSE = null;//xy坐标系x的起止坐标
    private float[] realYSE = null;//xy坐标系y的起止坐标
    private int gridRows;// 格点数据的行数
    private int gridCols;// 格点数据的列数
    //	private float deltX;// x方向上的间隔
    private float deltY;// y方向上的间隔
    private float deltYSign;//y方向上带符号的间隔
    private float gridXTopStart;
    private float gridXTopEnd;
    private float gridXBottomStart;
    private float gridXBottomEnd;
    private float gridYLeftStart;
    private float gridYLeftEnd;
    private float gridYRightStart;
    private float gridYRightEnd;
    private float[] xStartAry;
    private float[] xEndAry;
    private float[] yStartAry;
    private float[] yEndAry;

    private double curContourLevel;// 当前正在追踪的等值线值
    private float contourLevel;//实际追踪的等值线的值
    private ArrayList<Float> allLevels;//所有等级包含contourLevel和fillLevel
    private int isolineIndex = 0;//等值线编号

    private Isopoint preIsopoint; // 前一点
    private Isopoint curIsopoint; // 当前点
    private Isopoint nextIsopoint; // 后一点
    private Vertex[][] curVertex;//顶点值

    private IsolineDataProc procIsolineData = null;// 一条等值线
    private ArrayList<IsolineDataProc> procIsolineDataList = new ArrayList<IsolineDataProc>();// 所有等值线

    //TODO 如果不追踪多边形填充 则可以提高效率 就不用判断标记高低中心了！2014-10-28 db_x
    public ArrayList<IsolinePolygon> resPolygonList = null;//多边形，填充用
    private ArrayList<IsolineData> resIsolineDataList = new ArrayList<IsolineData>();//转成IsolineData后的等值线,包括高低中心
    private ArrayList<IsolineData> resPartIsolineDataList = null;// 追踪后不完全等值线（无高低标注）

    private float shift = IsolineUtil.SHIFT;// 修正值

    private double epsilon1 = IsolineUtil.EPSILON3;//多边形用
    private double epsilon2 = IsolineUtil.EPSILON4;//多边形用，浮点值对比数据
    private float precision = IsolineUtil.PRECISION;// 对比位置
    private float maxVal = -Float.MAX_VALUE;
    private float minVal = Float.MAX_VALUE;

    private float NULLVAL = DataTypeConst.NULLVAL;

    private ArrayList<IsolinePolygonProc> polygonProcList = new ArrayList<IsolinePolygonProc>();// 所有多边形
    private ArrayList<IsolinePolygonProc> polygonClosedProcList = new ArrayList<IsolinePolygonProc>();// 所有闭合等值线所构成的多边形
    private ArrayList<IsolinePolygonContain> polygonContainList = new ArrayList<IsolinePolygonContain>();// 找完最值后的多边形
    private ArrayList<IsolinePolygonContain> closedPolygonContainList = new ArrayList<IsolinePolygonContain>();// 找完最值后的闭合等值线多边形
    private ArrayList<IsolinePolygonContain> polygonList = new ArrayList<IsolinePolygonContain>();//最内最外闭合等值线多边形,标高低中心

    /**
     * 设置等值线处理用属性
     *
     * @param attr 属性
     */
    public void setAttr(IsolineProcessAttr attr) {
        this.attr = attr;
    }

    /**
     * 获取等值线处理用属性
     *
     * @return 属性
     */
    public IsolineProcessAttr getAttr() {
        return attr;
    }

    //读取数据前先清除上次残留的参数，避免多次读取不同数据时数据不变
    private void clearAllParam() {
        isolineIndex = 0;
        if (procIsolineDataList != null && !procIsolineDataList.isEmpty()) {
            procIsolineDataList.clear();
        }
        if (polygonProcList != null && !polygonProcList.isEmpty()) {
            polygonProcList.clear();
        }
        if (polygonClosedProcList != null && !polygonClosedProcList.isEmpty()) {
            polygonClosedProcList.clear();
        }
        if (polygonContainList != null && !polygonContainList.isEmpty()) {
            polygonContainList.clear();
        }
        if (closedPolygonContainList != null && !closedPolygonContainList.isEmpty()) {
            closedPolygonContainList.clear();
        }
        if (resPolygonList != null && !resPolygonList.isEmpty()) {
            resPolygonList.clear();
        }
        if (resIsolineDataList != null && !resIsolineDataList.isEmpty()) {
            resIsolineDataList.clear();
        }
        if (resPartIsolineDataList != null && !resPartIsolineDataList.isEmpty()) {
            resPartIsolineDataList.clear();
        }
        if (polygonList != null && !polygonList.isEmpty()) {
            polygonList.clear();
        }
    }

    /**
     * 输入数据
     *
     * @param
     * @return 输入是否成功, 即输入的数据是否为空
     */
    /*public boolean setSrcData(List<? extends MIDSData> dataList){
        boolean isScatterData = false;//是否为离散数据
		boolean isGridData = false;//格点数据
		boolean isNCOriginalData = false;//
		boolean isNCSeaData = false;//
		boolean isDepgrdData = false;

		clearAllParam();

		if (dataList == null){
			logger.println("In IsolineProcess未传入数据");
			return false;
		}
		if (dataList.isEmpty()){
			logger.println("In IsolineProcess传入的数据为空");
			return false;
		}

		if(dataList.size() ==1 && dataList.get(0)==null){
			logger.println("In IsolineProcess传入的数据为空,无法进行等值线追踪");
			return false;
		}
		if (dataList.get(0) instanceof ScatterData){
			scatterDataList = new ArrayList<ScatterData>();
			this.attr.setSrcDataType(IsolineSrcDataType.ScatterData);
			isScatterData = true;
		}else if(dataList.get(0) instanceof GridData){
			gridDataList = new ArrayList<GridData>();
			this.attr.setSrcDataType(IsolineSrcDataType.GridData);
			isScatterData = false;
			isGridData = true;
		}else if (dataList.get(0) instanceof NCOriginalData){
			gridDataList = new ArrayList<GridData>();
			this.attr.setSrcDataType(IsolineSrcDataType.GridData);
			isNCOriginalData = true;
		}else if(dataList.get(0) instanceof NCSeaData){
			gridDataList = new ArrayList<GridData>();
			this.attr.setSrcDataType(IsolineSrcDataType.GridData);
			isNCSeaData = true;
		}else if(dataList.get(0) instanceof DepgrdData){
			gridDataList = new ArrayList<GridData>();
			this.attr.setSrcDataType(IsolineSrcDataType.GridData);
			isDepgrdData = true;
		}
		else{
			logger.println("In IsolineProcess传入的数据类型错误");
			return false;
		}

		float[][] lonAry = null;
		float[][] latAry = null;

		for (MIDSData md : dataList){
			if(md==null){
				continue;
			}
			if (md.isEmpty()){
				continue;
			}
			if (isScatterData){
				scatterDataList.add((ScatterData) md);
			}else if(isGridData){
				GridData gd = (GridData)md;
				gridDataList.add(gd);
				isGridData = true;//这句话有点多余 否则isGridData若false的话就不会进来！ db_x;
			}else if(isNCOriginalData){//只要不是离散数据就深度复制给具体格点类型GridData下面处理类似   db_x
				NCOriginalData nd = (NCOriginalData)md;
//				GridData gd = new GridData(nd.getXStart(), nd.getXEnd(), nd.getYStart(), nd.getYEnd(), nd.getXDel(), nd.getYDel());
				GridData gd = new GridData();
				gd.setColNum(nd.getColNum());
				gd.setRowNum(nd.getRowNum());
				gd.setXStart(nd.getXStart());
				gd.setXEnd(nd.getXEnd());
				gd.setYStart(nd.getYStart());
				gd.setYEnd(nd.getYEnd());
				float deltX = nd.getXDel();
				float deltY = nd.getYDel();
				if(deltX != 0.0f){
					gd.setXDel(deltX);
				}
				if(deltY!=0.0f){
					gd.setYDel(deltY);
				}

				float[][] data = nd.getOriginalData();//2米温度
				//根据landMask区分陆地海洋 为1表示陆地
				float[][] landMask = nd.getLandMaskData();
				for(int i=0; i<landMask.length; i++){
					for(int j=0; j<landMask[0].length; j++){
						if(landMask[i][j] == 1.0f){ //float型是否可以用等号判断？ db_x
							data[i][j] = NULLVAL;
						}
					}
				}
				lonAry = nd.getLonAry();
				latAry = nd.getLatAry();
				gd.setLonAry2D(lonAry);
				gd.setLatAry2D(latAry);
				gd.setGridData(data);
				gridDataList.add(gd);
				isGridData = true;
			}else if(isNCSeaData){
				NCSeaData nd = (NCSeaData)md;
//				GridData gd = new GridData(nd.getXStart(), nd.getXEnd(), nd.getYStart(), nd.getYEnd(), nd.getXDel(), nd.getYDel());
				GridData gd = new GridData();
				gd.setColNum(nd.getColNum());
				gd.setRowNum(nd.getRowNum());
				gd.setXStart(nd.getXStart());
				gd.setXEnd(nd.getXEnd());
				gd.setYStart(nd.getYStart());
				gd.setYEnd(nd.getYEnd());
				float deltX = nd.getXDel();
				float deltY = nd.getYDel();
				if(deltX != 0.0f){
					gd.setXDel(deltX);
				}
				if(deltY!=0.0f){
					gd.setYDel(deltY);
				}

				float[][] data = nd.getSeaData();
				gd.setGridData(data);
				lonAry = nd.getLonAry();
				latAry = nd.getLatAry();
				gd.setLonAry2D(lonAry);
				gd.setLatAry2D(latAry);
				gridDataList.add(gd);
				isGridData = true;
			}else if(isDepgrdData){
				DepgrdData dd = (DepgrdData)md;
//				GridData gd = new GridData(nd.getXStart(), nd.getXEnd(), nd.getYStart(), nd.getYEnd(), nd.getXDel(), nd.getYDel());
				GridData gd = new GridData();
				gd.setColNum(dd.getColNum());
				gd.setRowNum(dd.getRowNum());
				gd.setXStart(dd.getXStart());
				gd.setXEnd(dd.getXEnd());
				gd.setYStart(dd.getYStart());
				gd.setYEnd(dd.getYEnd());
				float deltX = dd.getXDel();
				float deltY = dd.getYDel();
				if(deltX != 0.0f){
					gd.setXDel(deltX);
				}
				if(deltY!=0.0f){
					gd.setYDel(deltY);
				}
				float[][] data = dd.getDepgrdData();
				gd.setGridData(data);
				lonAry = dd.getLonAry();
				latAry = dd.getLatAry();
				gd.setLonAry2D(lonAry);
				gd.setLatAry2D(latAry);
				gridDataList.add(gd);
				isGridData = true;
			}else{
				logger.println("In IsolineProcess传入的数据类型错误");
				return false;
			}
		}

		if (isScatterData && scatterDataList.isEmpty()){
			scatterDataList = null;
			logger.println("In IsolineProcess传入数据为空");
			return false;
		}
		if ((!isScatterData) && (gridDataList.isEmpty())){
			gridDataList = null;
			logger.println("In IsolineProcess传入数据为空");
			return false;
		}

		if(!isScatterData){
			if(!procGridDataList()){
				logger.println("In IsolineProcess.procGridDataList不成功");
				return false;
			}
		}
		return true;
	}*/
    public boolean setGridData(GridData data) {
        boolean isScatterData = false;//是否为离散数据
        boolean isGridData = false;//是否为格点数据
        boolean isNCOriginalData = false;
        boolean isNCSeaData = false;
        boolean isDepgrdData = false;

        clearAllParam();

        if (data == null || data.isEmpty()) {
            logger.error("In IsolineProcess传入的数据为空");
            return false;
        }

        gridDataList = new ArrayList<GridData>();
        this.attr.setSrcDataType(IsolineSrcDataType.GridData);
        isGridData = true;
        GridData dd = (GridData) data;
        gridDataList.add(dd);


        if ((isGridData) && (gridDataList.isEmpty())) {
            //gridDataList = null;
            logger.error("In IsolineProcess传入数据为空");
            return false;
        }
        return true;

    }
    //其实此方法可以用在上面的方法中处理setSrcData(List<? extends MIDSData>)中的循环中！ db_x

    /**
     * 输入数据
     *
     * @param
     * @return 输入是否成功, 即输入的数据是否为空
     */
    /*public boolean setSrcData(MIDSData data){
        boolean isScatterData = false;//是否为离散数据
		boolean isGridData    = false;//是否为格点数据
		boolean isNCOriginalData = false;
		boolean isNCSeaData = false;
		boolean isDepgrdData = false;

		clearAllParam();

		if (data == null || data.isEmpty()){
			logger.println("In IsolineProcess传入的数据为空");
			return false;
		}

		if (data instanceof ScatterData){
			scatterDataList = new ArrayList<ScatterData>();
			this.attr.setSrcDataType(IsolineSrcDataType.ScatterData);
			isScatterData = true;
		}else if (data instanceof GridData){
			gridDataList = new ArrayList<GridData>();
			this.attr.setSrcDataType(IsolineSrcDataType.GridData);
			isGridData = true;
		}else if (data instanceof NCOriginalData){
			gridDataList = new ArrayList<GridData>();
			this.attr.setSrcDataType(IsolineSrcDataType.GridData);
			isNCOriginalData = true;
		}else if(data instanceof NCSeaData){
			gridDataList = new ArrayList<GridData>();
			this.attr.setSrcDataType(IsolineSrcDataType.GridData);
			isNCSeaData = true;
		}else if(data instanceof DepgrdData){
			gridDataList = new ArrayList<GridData>();
			this.attr.setSrcDataType(IsolineSrcDataType.GridData);
			isDepgrdData = true;
		}
		else{
			logger.println("In IsolineProcess传入的数据类型错误");
			return false;
		}

		if (isScatterData){
			scatterDataList.add((ScatterData) data);
		}else if (isGridData){
			GridData dd = (GridData) data;
			gridDataList.add(dd);
		}else if (isNCOriginalData){
			NCOriginalData nd = (NCOriginalData)data;
//			GridData gd = new GridData(nd.getXStart(), nd.getXEnd(), nd.getYStart(), nd.getYEnd(), nd.getXDel(), nd.getYDel());
			GridData gd = new GridData();
			int row = nd.getRowNum();
			int col = nd.getColNum();
			gd.setColNum(col);
			gd.setRowNum(row);
			gd.setXStart(nd.getXStart());
			gd.setXEnd(nd.getXEnd());
			gd.setYStart(nd.getYStart());
			gd.setYEnd(nd.getYEnd());
			float deltX = nd.getXDel();
			float deltY = nd.getYDel();
			if(deltX != 0.0f){
				gd.setXDel(deltX);
			}
			if(deltY!=0.0f){
				gd.setYDel(deltY);
			}

			float[][] ncdata = nd.getOriginalData();
			//根据landMask指明的陆地海洋来取数据
			float[][] landMask = nd.getLandMaskData();
			for(int i=0; i<landMask.length; i++){
				for(int j=0; j<landMask[0].length; j++){
					if(landMask[i][j] == 1.0f){
						ncdata[i][j] = NULLVAL;
					}
				}
			}
			float[][] lonAry = nd.getLonAry();
			float[][] latAry = nd.getLatAry();
			gd.setLonAry2D(lonAry);
			gd.setLatAry2D(latAry);
			gd.setGridData(ncdata);
			gridDataList.add(gd);
		}else if(isNCSeaData){
			NCSeaData nd = (NCSeaData)data;
//			GridData gd = new GridData(nd.getXStart(), nd.getXEnd(), nd.getYStart(), nd.getYEnd(), nd.getXDel(), nd.getYDel());
			GridData gd = new GridData();
			int row = nd.getRowNum();
			int col = nd.getColNum();
			gd.setColNum(col);
			gd.setRowNum(row);
			gd.setXStart(nd.getXStart());
			gd.setXEnd(nd.getXEnd());
			gd.setYStart(nd.getYStart());
			gd.setYEnd(nd.getYEnd());
			float deltX = nd.getXDel();
			float deltY = nd.getYDel();
			if(deltX != 0.0f){
				gd.setXDel(deltX);
			}
			if(deltY!=0.0f){
				gd.setYDel(deltY);
			}

			float[][] ncdata = nd.getSeaData();
//			for(int i=0; i<ncdata.length; i++){
//				for(int j=0; j<ncdata[0].length; j++){
//					if(ncdata[i][j]!=NULLVAL){
//						ncdata[i][j] = ncdata[i][j] * 100;
//					}
//				}
//			}
			gd.setGridData(ncdata);
			float[][] lonAry = nd.getLonAry();
			float[][] latAry = nd.getLatAry();
			gd.setLonAry2D(lonAry);
			gd.setLatAry2D(latAry);
			gridDataList.add(gd);
		}else if(isDepgrdData){
			DepgrdData dd = (DepgrdData)data;
//			GridData gd = new GridData(nd.getXStart(), nd.getXEnd(), nd.getYStart(), nd.getYEnd(), nd.getXDel(), nd.getYDel());
			GridData gd = new GridData();
			int row = dd.getRowNum();
			int col = dd.getColNum();
			gd.setColNum(col);
			gd.setRowNum(row);
			gd.setXStart(dd.getXStart());
			gd.setXEnd(dd.getXEnd());
			gd.setYStart(dd.getYStart());
			gd.setYEnd(dd.getYEnd());
			float deltX = dd.getXDel();
			float deltY = dd.getYDel();
			if(deltX != 0.0f){
				gd.setXDel(deltX);
			}
			if(deltY!=0.0f){
				gd.setYDel(deltY);
			}

			float[][] Depdata = dd.getDepgrdData();
			gd.setGridData(Depdata);
			float[][] lonAry = dd.getLonAry();
			float[][] latAry = dd.getLatAry();
			gd.setLonAry2D(lonAry);
			gd.setLatAry2D(latAry);
			gridDataList.add(gd);
		}
		else{
			logger.println("In IsolineProcess传入数据为空");
			return false;
		}

		if (isScatterData && scatterDataList.isEmpty()){
			//scatterDataList = null;
			logger.println("In IsolineProcess传入数据为空");
			return false;
		}
		if ((isGridData) && (gridDataList.isEmpty())){
			//gridDataList = null;
			logger.println("In IsolineProcess传入数据为空");
			return false;
		}

		//处理gridDataList
		if(!isScatterData){
			if(!procGridDataList()){
				logger.println("In IsolineProcess.procGridDataList不成功");
				return false;
			}
		}
		return true;
	}
*/
    //对gridData预处理(对坐标系是否为经纬度进行转换)处理后的gridData中只包含格点值和经纬度 db_x
    @SuppressWarnings("unchecked")
    private boolean procGridDataList() {
        if (attr.isXYCoordinate()) {
            gridXYCoordX = gridDataList.get(0).getLonAry2D();
            gridXYCoordY = gridDataList.get(0).getLatAry2D();

            ArrayList<GridData> gridDataListCopy = (ArrayList<GridData>) gridDataList.clone();
            if (gridDataList != null) {
                gridDataList.clear();
                gridDataList = new ArrayList<GridData>();
            }
            for (GridData gd : gridDataListCopy) {
                int row = gd.getRowNum();
                int col = gd.getColNum();
                GridData tmpGridData = new GridData();
                tmpGridData.setColNum(col);
                tmpGridData.setRowNum(row);
                tmpGridData.setGridData(gd.getGridData());
                float[][] newLonAry = IsolineUtil.mergeXYToLonLat(gd.getLonAry2D(), attr.getXSLon(), attr.getXELon(), true);
                float[][] newLatAry = IsolineUtil.mergeXYToLonLat(gd.getLatAry2D(), attr.getYSLat(), attr.getYELat(), false);
                tmpGridData.setLatAry2D(newLatAry);
                tmpGridData.setLonAry2D(newLonAry);
                gridDataList.add(tmpGridData);
            }
        }
        return true;
    }

    /**
     * 找闭合等值线所在多边形直接包含的多边形的Index
     * 并找到最里面和最外面的多边形，标高低中心用
     */
    private void closedIsolinePolygonContainBuild() {
        if (polygonClosedProcList == null || polygonClosedProcList.isEmpty()) {
            if (procIsolineDataList == null || procIsolineDataList.isEmpty()) {
                // 等值线list为空重新追踪
                procIsolineDataList = new ArrayList<IsolineDataProc>();
                if (!getIsolineDataProcList()) {
                    if (logger.isDebugEnabled())
                        logger.debug("In IsolineProcess closedIsolinePolygonContainBuild() 追踪等值线失败");
                    return;
                }
            }

            if (procIsolineDataList.isEmpty()) {
                if (logger.isDebugEnabled())
                    logger.debug("In IsolineProcess closedIsolinePolygonContainBuild() 无符合level条件的等值线");
                return;
            }

            long s = System.currentTimeMillis();
            //若没有构建闭合等值线所在多边形,且未找到高低中心
            if (polygonClosedProcList == null || polygonClosedProcList.isEmpty()) {
                if (!srcClosedPolygonBuild()) {
                    if (logger.isDebugEnabled())
                        logger.debug("In IsolineProcess closedIsolinePolygonContainBuild() 构造初始多边形失败");
                    return;
                }
            }
            long s1 = System.currentTimeMillis();
            if (logger.isDebugEnabled())
                logger.debug("构造闭合等值线多边形用时：" + (s1 - s));
        }

        if (polygonClosedProcList.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("In IsolineProcess closedIsolinePolygonContainBuild() polygonClosedProcList为空");
                logger.debug("无高低中心");
            }
            return;
        }
        int size = polygonClosedProcList.size();
        closedPolygonContainList = new ArrayList<IsolinePolygonContain>(size);
        IsolinePolygonContain isolinePgnContain;
        ArrayList<Integer> indexList = null;
        IsolinePolygonProc isolinePgnProc1;
        IsolinePolygonProc isolinePgnProc2;
        IsolinePolygonProc isolinePgnProc3;
        IsolinePolygonProc isolinePgnProc4;
        ArrayList<IsolinePolygonProc> isolinePgnProcList;
        boolean containFlag1 = false;
        boolean containFlag2 = false;

        for (int i = 0; i < size; i++) {
            containFlag2 = false;
            indexList = new ArrayList<Integer>();
            isolinePgnProcList = new ArrayList<IsolinePolygonProc>();
            isolinePgnProc1 = polygonClosedProcList.get(i);
            isolinePgnContain = new IsolinePolygonContain();
            for (int j = 0; j < size; j++) {
                containFlag1 = false;
                isolinePgnProc2 = polygonClosedProcList.get(j);
                if (j != i) {
                    containFlag1 = isClosedPolyonContainClosedPolygon(isolinePgnProc1, isolinePgnProc2);
                    if (containFlag1) {
                        isolinePgnProcList.add(isolinePgnProc2);//找到isolinePgnProc1包含的多边形isolinePgnProc2列表isolinePgnProcList
                    }
                }
            }

            //找最外最里闭合等值线所在的多边形用   当前多边形isolinePgnProc1是否被其他多边形所包含
            //没有多边形包含当前多边形，表示当前多边形为最外面的多边形
            for (int j = 0; j < size; j++) {
                isolinePgnProc2 = polygonClosedProcList.get(j);
                if (j != i) {
                    containFlag2 = isClosedPolyonContainClosedPolygon(isolinePgnProc2, isolinePgnProc1);
                    if (containFlag2) {
                        break;
                    }
                }
            }

            // 找直接包含的
            if (!isolinePgnProcList.isEmpty()) {
                for (int j = 0; j < isolinePgnProcList.size(); j++) {
                    isolinePgnProc3 = isolinePgnProcList.get(j);
                    for (int k = 0; k < isolinePgnProcList.size(); k++) {
                        isolinePgnProc4 = isolinePgnProcList.get(k);
                        containFlag1 = false;
                        if (j != k) {
                            containFlag1 = isClosedPolyonContainClosedPolygon(isolinePgnProc4, isolinePgnProc3);
                            if (containFlag1) {
                                break;
                            }
                        }
                    }
                    if (!containFlag1) {
                        // 加入indexlist
                        indexList.add(isolinePgnProc3.index);
                    } else {
                        continue;
                    }
                }

                if (!indexList.isEmpty()) {
                    isolinePgnContain.containOnlyPgnList = indexList;
                } else {
                    logger.error("In IsolineProcess中closedIsolinePolygonContainBuild()中indexList为空，即直接包含的多边形不存在,错误");
                    return;
                }
                isolinePgnContain.isInner = false;//存在包含的多边形
            } else {
                isolinePgnContain.containOnlyPgnList = null;
//				isolinePgnContain.isInOutInner = new Boolean(true);//不包含任何多边形,即最里面的多边形
                isolinePgnContain.isInner = true;//不包含任何多边形
            }
            if (!containFlag2) {
//				isolinePgnContain.isInOutInner = new Boolean(false);//不被其他任何多边形所包含，即最外面的多边形
                isolinePgnContain.isOuter = true;//不被其他任何多边形所包含
            } else {
                isolinePgnContain.isOuter = false;//存在其他多边形包含它
            }

            isolinePgnContain.index = isolinePgnProc1.index;
            isolinePgnContain.val = isolinePgnProc1.val;
            isolinePgnContain.maxVal = isolinePgnProc1.maxVal;
            isolinePgnContain.minVal = isolinePgnProc1.minVal;
            isolinePgnContain.maxLat = isolinePgnProc1.maxLat;
            isolinePgnContain.minLat = isolinePgnProc1.minLat;
            isolinePgnContain.maxLon = isolinePgnProc1.maxLon;
            isolinePgnContain.minLon = isolinePgnProc1.minLon;
            isolinePgnContain.polygon = isolinePgnProc1.polygon;
            isolinePgnContain.onIsolineIndex = isolinePgnProc1.onIsolineIndex;//所在等值线的编号
            isolinePgnContain.polygonBoundaryIndexAry = isolinePgnProc1.polygonBoundaryIndexAry;
            isolinePgnContain.direction = isolinePgnProc1.direction;
            closedPolygonContainList.add(isolinePgnContain);
        }
    }

    /**
     * 获取polygonContainList，主要找直接包含的多边形的Index
     */
    private void srcPolygonContainListBuild() {
        if (polygonProcList == null || polygonProcList.isEmpty()) {
            if (procIsolineDataList == null || procIsolineDataList.isEmpty()) {
                // 重新追踪
                procIsolineDataList = new ArrayList<IsolineDataProc>();
                if (!getIsolineDataProcList()) {
                    logger.error("In IsolineProcess srcPolygonContainListBuild() 追踪等值线失败");
                    return;
                }
            }

            if (procIsolineDataList.isEmpty()) {
                if (logger.isDebugEnabled())
                    logger.debug("In IsolineProcess srcPolygonContainListBuild() 无符合level条件的等值线");
                return;
            }
            long s1 = System.currentTimeMillis();
            if (polygonProcList == null || polygonProcList.isEmpty()) {//TODO 进入该块时已经判断  再判断 多余 db_x 2014-11-13
                if (!srcPolygonBuild()) {
                    logger.error("In IsolineProcess srcPolygonContainListBuild() 构造初始多边形失败");
                    return;
                }
            }
            if (logger.isDebugEnabled())
                logger.debug("构造多边形用时：" + (System.currentTimeMillis() - s1) + "ms");
        }

        if (polygonProcList.isEmpty()) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess srcPolygonContainListBuild() polygonProcList为空");
            return;
        }
        int size1 = polygonProcList.size();
        long s = System.currentTimeMillis();
        polygonContainList = new ArrayList<IsolinePolygonContain>(size1);
        IsolinePolygonContain isolinePgnContain;
        ArrayList<Integer> indexList = null;
        IsolinePolygonProc isolinePgnProc1;
        IsolinePolygonProc isolinePgnProc2;
        IsolinePolygonProc isolinePgnProc3;
        IsolinePolygonProc isolinePgnProc4;
        ArrayList<IsolinePolygonProc> isolinePgnProcList = null;
        boolean flag = false;
        int index = 0;
//		logger.println("多边形数：" + size1);
        for (int i = 0; i < size1; i++) {
//			long s2 = System.currentTimeMillis();
            indexList = new ArrayList<Integer>();
            isolinePgnProcList = new ArrayList<IsolinePolygonProc>();
            isolinePgnProc1 = polygonProcList.get(i);
            isolinePgnContain = new IsolinePolygonContain();
            index = isolinePgnProc1.index;
            for (int j = 0; j < size1; j++) {
                if (j == i) {
                    continue;
                }
                flag = false;
                isolinePgnProc2 = polygonProcList.get(j);
                int index1 = isolinePgnProc1.onIsolineIndex;
                int index2 = isolinePgnProc2.onIsolineIndex;
                if (index1 == index2) {//在同一条等值线上的两个多边形互不包含
                    continue;
                }
                if (isolinePgnProc2.neighborhoodIndex == index) {//当前多边形和自己相邻的多边形互不包含
                    continue;
                }
                if (procIsolineDataList.get(index1).isClosed && !procIsolineDataList.get(index2).isClosed) {
                    continue;
                }
                if (procIsolineDataList.get(index1).isClosed || procIsolineDataList.get(index2).isClosed) {
                    flag = isolinePgnProc1.polygon.contains(isolinePgnProc2.polygon.xpoints[0], isolinePgnProc2.polygon.ypoints[0]);
                } else {
                    flag = isPolygonContainPolygon(isolinePgnProc1.polygon, isolinePgnProc2.polygon, isolinePgnProc1.index, isolinePgnProc2.index);
                }
                if (flag) {
                    index = isolinePgnProc2.index;
                    isolinePgnProcList.add(isolinePgnProc2);//找到isolinePgnProc1包含的多边形isolinePgnProc2列表isolinePgnProcList
                }
            }
//			long s3 = System.currentTimeMillis();
//			logger.print(i+" : " + (s3-s2));
            // 找直接包含的
            if (!isolinePgnProcList.isEmpty()) {//TODO 寻找直接被isolinePgnProc1包含，且不被其他包含的多边形 确定其内边界 一次递归处理就完成所以包含关系了 db_x 2014-11-12
                for (int j = 0; j < isolinePgnProcList.size(); j++) {
                    isolinePgnProc3 = isolinePgnProcList.get(j);
//					for (int k = 0; k < isolinePgnProcList.size(); k++) {
//						if(k==j){
//							continue;
//						}
                    for (int k = isolinePgnProcList.size() - 1; k >= 0; k--) {
                        isolinePgnProc4 = isolinePgnProcList.get(k);
                        flag = false;
                        int index3 = isolinePgnProc3.onIsolineIndex;
                        int index4 = isolinePgnProc4.onIsolineIndex;
                        if (index3 == index4) {
                            continue;
                        }
                        if (procIsolineDataList.get(index4).isClosed && !procIsolineDataList.get(index3).isClosed) {
                            continue;
                        }
                        if (procIsolineDataList.get(index4).isClosed || procIsolineDataList.get(index3).isClosed) {
                            flag = isolinePgnProc4.polygon.contains(isolinePgnProc3.polygon.xpoints[0], isolinePgnProc3.polygon.ypoints[0]);
                        } else {
                            flag = isPolygonContainPolygon(isolinePgnProc4.polygon, isolinePgnProc3.polygon, isolinePgnProc4.index, isolinePgnProc3.index);
                        }
//							flag = isPolygonContainPolygon(isolinePgnProc4.polygon,isolinePgnProc3.polygon, isolinePgnProc4.index, isolinePgnProc3.index);
                        if (flag) {
                            break;
                        }
                    }
                    if (!flag) {
                        // 加入indexlist
                        indexList.add(isolinePgnProc3.index);
                    } else {
                        continue;
                    }
                }

                if (!indexList.isEmpty()) {
                    isolinePgnContain.containOnlyPgnList = indexList;
                } else {
                    logger.error("In IsolineProcess中srcPolygonContainListBuild()中indexList为空，即直接包含的多边形不存在,错误");
                    return;
                }
            } else {
                isolinePgnContain.containOnlyPgnList = null;
            }
//			logger.println(", "+ (System.currentTimeMillis()-s3));
            isolinePgnContain.index = isolinePgnProc1.index;
            isolinePgnContain.val = isolinePgnProc1.val;
            isolinePgnContain.isDown = isolinePgnProc1.isDown;
            isolinePgnContain.maxVal = isolinePgnProc1.maxVal;
            isolinePgnContain.minVal = isolinePgnProc1.minVal;
            isolinePgnContain.maxLat = isolinePgnProc1.maxLat;
            isolinePgnContain.minLat = isolinePgnProc1.minLat;
            isolinePgnContain.maxLon = isolinePgnProc1.maxLon;
            isolinePgnContain.minLon = isolinePgnProc1.minLon;
            isolinePgnContain.polygon = isolinePgnProc1.polygon;
            isolinePgnContain.polygonBoundaryIndexAry = isolinePgnProc1.polygonBoundaryIndexAry;
            isolinePgnContain.direction = isolinePgnProc1.direction;
            isolinePgnContain.isSESameSide = isolinePgnProc1.isSESameSide;
            isolinePgnContain.onIsolineIndex = isolinePgnProc1.onIsolineIndex;
            isolinePgnContain.neighborhoodIndex = isolinePgnProc1.neighborhoodIndex;
            polygonContainList.add(isolinePgnContain);

        }
        if (logger.isDebugEnabled())
            logger.debug("找直接包含：" + (System.currentTimeMillis() - s) + "ms");
    }

    //下面一些列关于判断点与多边形和多边形与多边形之间位置的方法应该放在多边形类中，这样便于通过对象调用，也符合面向对象编程原理！ dbx 2013-10-29

    /**
     * 一个多边形是否包含另一个多边形，即另一个多边形是否在那个多边形的里面
     *
     * @param outerP 外围多边形
     * @param innerP 里面多边形
     * @return 包含则为true，否则为false
     */
    private boolean isPolygonContainPolygon(Polygon outerP, Polygon innerP, int outerIndex, int innerIndex) {
        if (outerP.npoints == 0 || innerP.npoints == 0) {
            if (logger.isDebugEnabled()) logger.debug("In IsolineProcess containPolygon()判断多边形包含关系输入的多边形有空");
            return false;
        }

        // 先由经纬度判断
        IsolinePolygonProc outerIsolinePolygon = polygonProcList.get(outerIndex);
        IsolinePolygonProc innerIsolinePolygon = polygonProcList.get(innerIndex);

        if (outerIsolinePolygon.maxLat < innerIsolinePolygon.maxLat) {
            return false;
        }
        if (outerIsolinePolygon.minLat > innerIsolinePolygon.minLat) {
            return false;
        }
        if (outerIsolinePolygon.maxLon < innerIsolinePolygon.maxLon) {
            return false;
        }
        if (outerIsolinePolygon.minLon > innerIsolinePolygon.minLon) {
            return false;
        }

        //如果外面多边形的一个点在里面多边形里面，则说明outerP不包含innerP
        ArrayList<Integer> outerAddPtIndexAry = outerIsolinePolygon.polygonBoundaryIndexAry;
        ArrayList<Integer> innerAddPtIndexAry = innerIsolinePolygon.polygonBoundaryIndexAry;
        boolean outerFlag = outerAddPtIndexAry.contains(1);
        boolean innerFlag = innerAddPtIndexAry.contains(1);
        if (isPolygonContainPoint(outerP.xpoints[1], outerP.ypoints[1], outerFlag, innerP)) {
            return false;
        } else {
            if (isPolygonContainPoint(innerP.xpoints[1], innerP.ypoints[1], innerFlag, outerP)) {
                return true;
            } //因为等值线不能交叉，可这样判断，只能用于等值线判断!
            else {
                return false;
            }
        }
    }


    /**
     * 一个闭合等值线所在多边形是否包含另一个闭合等值线所在的多边形，即另一个多边形是否在那个多边形的里面
     *
     * @param outerPolygon 外围多边形
     * @param innerPolygon 里面多边形
     * @return 包含则为true，否则为false
     */
    private boolean isClosedPolyonContainClosedPolygon(IsolinePolygonProc outerPolygon, IsolinePolygonProc innerPolygon) {
        Polygon outerP = outerPolygon.polygon;
        Polygon innerP = innerPolygon.polygon;

        if (outerP.npoints == 0 || innerP.npoints == 0) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess isClosedPolyonContainClosedPolygon()判断多边形包含关系输入的多边形有空");
            return false;
        }

        // 先由经纬度判断
        if (outerPolygon.maxLat < innerPolygon.maxLat) {
            return false;
        }
        if (outerPolygon.minLat > innerPolygon.minLat) {
            return false;
        }
        if (outerPolygon.maxLon < innerPolygon.maxLon) {
            return false;
        }
        if (outerPolygon.minLon > innerPolygon.minLon) {
            return false;
        }

        //判断闭合等值线所在多边形outer是否包含inner的一个点，如包含 则inner在outer内
        //不需要判断所有点
        if (!outerP.contains(innerP.xpoints[0], innerP.ypoints[0])) {
            return false;
        }
//		for(int i=0; i<innerP.npoints; i++){
//			if(!outerP.contains(innerP.xpoints[i], innerP.ypoints[i])){
//				return false;
//			}
//		}
        return true;
    }

    private boolean isClosedPolygonContainClosedPolygon(HYIsolinePolygonProc outerPolygon, HYIsolinePolygonProc innerPolygon) {
        Polygon outerP = outerPolygon.polygon;
        Polygon innerP = innerPolygon.polygon;

        if (outerP.npoints == 0 || innerP.npoints == 0) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess isClosedPolyonContainClosedPolygon()判断多边形包含关系输入的多边形有空");
            return false;
        }

        // 先由经纬度判断
        if (outerPolygon.maxLat < innerPolygon.maxLat) {
            return false;
        }
        if (outerPolygon.minLat > innerPolygon.minLat) {
            return false;
        }
        if (outerPolygon.maxLon < innerPolygon.maxLon) {
            return false;
        }
        if (outerPolygon.minLon > innerPolygon.minLon) {
            return false;
        }

        //判断闭合等值线所在多边形outer是否包含inner的一个点，如包含 则inner在outer内
        //不需要判断所有点
        if (!outerP.contains(innerP.xpoints[0], innerP.ypoints[0])) {
            return false;
        }
        return true;
    }


    /**
     * 判断点是否在多边形里面或边界上
     *
     * @param x       点
     * @param y
     * @param isAddPt 是否为新增加的点 若为新增加的点即为边上的点则用斜率先判断是在边上
     * @param poly    多边形
     * @return true表示多边形包含此点，否则为false
     */
    private boolean isPolygonContainPoint(int x, int y, boolean isAddPt, Polygon poly) {
        if (isAddPt) {
            for (int i = 0; i < poly.npoints; i++) {
                int j = i + 1;
                if (j == poly.npoints) {
                    j = 0;
                }

                if (x == poly.xpoints[i] && y == poly.ypoints[i]) {
                    return true;
                }//同一个点
                else {
                    Point2D.Float pt = new Point2D.Float();
                    pt.x = (float) (x * epsilon2);
                    pt.y = (float) (y * epsilon2);
                    Point2D.Float pt1 = new Point2D.Float();
                    pt1.x = (float) (poly.xpoints[i] * epsilon2);
                    pt1.y = (float) (poly.ypoints[i] * epsilon2);
                    Point2D.Float pt2 = new Point2D.Float();
                    pt2.x = (float) (poly.xpoints[j] * epsilon2);
                    pt2.y = (float) (poly.ypoints[j] * epsilon2);
                    if (MIDS3DMath.isOnLine(pt1, pt2, pt)) {
                        return true;
                    }
                }
            }
        }
        if (poly.contains(x, y)) {
            return true;
        } // 里面
        else {
            return false;
        }
    }

    private boolean isPointInPolygon(int x, int y, Polygon poly, ArrayList<Integer> pointIndex) {
        for (int i = 0; i < poly.npoints; i++) {
            int j = i + 1;
            if (j == poly.npoints) {
                j = 0;
            }
            if (x != poly.xpoints[i] && y != poly.ypoints[i]) {
                Point2D.Float pt = new Point2D.Float();
                pt.x = (float) (x * epsilon2);
                pt.y = (float) (y * epsilon2);
                Point2D.Float pt1 = new Point2D.Float();
                pt1.x = (float) (poly.xpoints[i] * epsilon2);
                pt1.y = (float) (poly.ypoints[i] * epsilon2);
                Point2D.Float pt2 = new Point2D.Float();
                pt2.x = (float) (poly.xpoints[j] * epsilon2);
                pt2.y = (float) (poly.ypoints[j] * epsilon2);
                if (MIDS3DMath.isOnLine(pt1, pt2, pt)) {
                    return true;
                }
            }
        }

        if (poly.contains(x, y)) {
            if (pointIndex != null && pointIndex.size() >= 1 && pointIndex.get(0) != -1) {
                for (int i = 0; i < pointIndex.size(); i++) {
                    if (x == poly.xpoints[pointIndex.get(i)] && y == poly.ypoints[pointIndex.get(i)]) {
                        return true;
                    }
                }
            }
            for (int i = 0; i < poly.npoints; i++) {
                if (x == poly.xpoints[i] && y == poly.ypoints[i]) {
                    return false;
                }//同一个点
            }
            return true;
        } else {
            if (pointIndex != null && pointIndex.size() >= 1 && pointIndex.get(0) != -1) {
                for (int i = 0; i < pointIndex.size(); i++) {
                    if (x == poly.xpoints[pointIndex.get(i)] && y == poly.ypoints[pointIndex.get(i)]) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * 获取等值线追踪完成后的形成的各个多边形
     *
     * @param sLevel 等值线要填充的起始值
     * @param eLevel 等值线要填充的结束值
     * @return 多边形
     */
    public ArrayList<IsolinePolygon> getPolygonList(float sLevel, float eLevel) {
        if (sLevel == eLevel) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess getPolygonList()获取多边形时填充起始值和结束值相等, 输入的等值线为一条,无法构成多边形");
            return null;
        }

        // 追踪是否成功
        if (procIsolineDataList.isEmpty()) {
            if (!getIsolineDataProcList()) {
                logger.error("In IsolineProcess中getIsolinesData()时procIsolineDataList为空时获取等值线失败");
                return null;
            }
        }

        if (procIsolineDataList.isEmpty()) {
            if (logger.isDebugEnabled()) logger.debug("In IsolineProcess中getIsolinesData() 无符合level条件的等值线");
            return null;
        }

        resPolygonList = new ArrayList<IsolinePolygon>();
        float srcSLevel = sLevel;
        float srcELevel = eLevel;

        //找到所有sLevel和eLevel的等值线
        //从小到大构造闭合多边形（暂不考虑有包含的闭合的多边形）
        if (attr.isHasNullVal()) {
            long s = System.currentTimeMillis();
            //		 处理所有值为level的多边形
            polygonListBuild(sLevel, eLevel);
            if (logger.isDebugEnabled())
                logger.debug("填充用时：" + (System.currentTimeMillis() - s));
            //		//复位已填充
            //		for(int t=0; t<polygonContainList.size(); t++){
            //			polygonContainList.get(t).isAddToFillPolygon = false;
            //		}
        } else {
            //1、首先判断sLevel和eLevel在追踪完成后的等值线中存不存在
            //A、都存在，不做任何处理，直接填充
            //B、都不存在：
            //      a:设置了fillLevel
            //			aa:sLevel 和 eLevel都包含在fillLevel中，
            //                aaa:fillLevel也没有经过处理，此level的等值线就是不存在,直接返回空
            //				  bbb:且此时fillLevel的值不是最初的，而是都经过处理的(变成了maxVal，minVal)
            //				               此时将sLevel和eLevel变成minVal和maxVal,判断maxVal 和minVal 在等值线中存在与否(肯定存在)
            //			bb:一个存在，另一个不存在,直接返回空
            //			cc:都不包含在fillLevel中直接返回空
            //		b:未设置fillLevel 直接返回空
            //C:一个存在，另一个不存在
            //	   a:设置了fillLevel：
            //			aa:不存在的那个包含在fillLevel中
            //				  aaa:fillLevel没有经过处理,直接返回空
            //                bbb:fillLevel进过处理加工,看经过加工的那个是否在等值线中存在
            //			bb:不存在的那个不包含在fillLevel中直接返回空
            //	   b:未设置fillLevel 直接返回空

            boolean isContainSLevel = isIsolineContainLevel(sLevel);
            boolean isContainELevel = isIsolineContainLevel(eLevel);
            boolean isContainMin = false;
            boolean isContainMax = false;
            if (!isContainSLevel || !isContainELevel) {
                isContainMin = isIsolineContainLevel(minVal);
                isContainMax = isIsolineContainLevel(maxVal);
            }
            boolean isFillContainSLevel = false;
            boolean isFillContainELevel = false;
            if (!isContainSLevel && !isContainELevel) {//都不存在
                if (this.attr.isSetFillLevel()) {
                    isFillContainSLevel = isFillLevelContainLevel(sLevel);
                    isFillContainELevel = isFillLevelContainLevel(eLevel);
                    if (isFillContainSLevel && isFillContainELevel) {
                        if (sLevel < minVal) {
                            sLevel = minVal;
                            if (!isContainMin) {
                                if (logger.isDebugEnabled())
                                    logger.debug("In IsolineProcess getPolygonList() (" + srcSLevel + ", " + srcELevel + ") 获取的多边形为空");
                                return null;
                            }
                        }//endif
                        if (sLevel > maxVal) {
                            sLevel = maxVal;
                            if (!isContainMax) {
                                if (logger.isDebugEnabled())
                                    logger.debug("In IsolineProcess getPolygonList() (" + srcSLevel + ", " + srcELevel + ") 获取的多边形为空");
                                return null;
                            }
                        }
                        if (eLevel < minVal) {
                            eLevel = minVal;
                            if (!isContainMin) {
                                if (logger.isDebugEnabled())
                                    logger.debug("In IsolineProcess getPolygonList() (" + srcSLevel + ", " + srcELevel + ") 获取的多边形为空");
                                return null;
                            }
                        }
                        if (eLevel > maxVal) {
                            eLevel = maxVal;
                            if (!isContainMax) {
                                if (logger.isDebugEnabled())
                                    logger.debug("In IsolineProcess getPolygonList() (" + srcSLevel + ", " + srcELevel + ") 获取的多边形为空");
                                return null;
                            }
                        }
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug("In IsolineProcess getPolygonList() (" + srcSLevel + ", " + srcELevel + ") 获取的多边形为空");
                        return null;
                    }
                } else {
                    if (logger.isDebugEnabled())
                        logger.debug("In IsolineProcess getPolygonList() (" + srcSLevel + ", " + srcELevel + ") 获取的多边形为空");
                    return null;
                }
            } else if (!isContainSLevel && isContainELevel) {//sLevel不存在
                if (this.attr.isSetFillLevel()) {
                    isFillContainSLevel = isFillLevelContainLevel(sLevel);
                    if (isFillContainSLevel) {
                        if (sLevel >= minVal && sLevel <= maxVal) {
                            if (logger.isDebugEnabled())
                                logger.debug("In IsolineProcess getPolygonList() (" + srcSLevel + ", " + srcELevel + ") 获取的多边形为空");
                            return null;
                        } else if (sLevel < minVal) {
                            sLevel = minVal;
                            if (!isContainMin) {
                                if (logger.isDebugEnabled())
                                    logger.debug("In IsolineProcess getPolygonList() (" + srcSLevel + ", " + srcELevel + ") 获取的多边形为空");
                                return null;
                            }
                        } else {
                            sLevel = maxVal;
                            if (!isContainMax) {
                                if (logger.isDebugEnabled())
                                    logger.debug("In IsolineProcess getPolygonList() (" + srcSLevel + ", " + srcELevel + ") 获取的多边形为空");
                                return null;
                            }
                        }
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug("In IsolineProcess getPolygonList() (" + srcSLevel + ", " + srcELevel + ") 获取的多边形为空");
                        return null;
                    }
                } else {
                    if (logger.isDebugEnabled())
                        logger.debug("In IsolineProcess getPolygonList() (" + srcSLevel + ", " + srcELevel + ") 获取的多边形为空");
                    return null;
                }
            } else if (isContainSLevel && !isContainELevel) {//eLevel不存在
                if (this.attr.isSetFillLevel()) {
                    isFillContainELevel = isFillLevelContainLevel(eLevel);
                    if (isFillContainELevel) {
                        if (eLevel >= minVal && eLevel <= maxVal) {
                            if (logger.isDebugEnabled())
                                logger.debug("In IsolineProcess getPolygonList() (" + srcSLevel + ", " + srcELevel + ") 获取的多边形为空");
                            return null;
                        } else if (eLevel < minVal) {
                            eLevel = minVal;
                            if (!isContainMin) {
                                if (logger.isDebugEnabled())
                                    logger.debug("In IsolineProcess getPolygonList() (" + srcSLevel + ", " + srcELevel + ") 获取的多边形为空");
                                return null;
                            }
                        } else {
                            eLevel = maxVal;
                            if (!isContainMax) {
                                if (logger.isDebugEnabled())
                                    logger.debug("In IsolineProcess getPolygonList() (" + srcSLevel + ", " + srcELevel + ") 获取的多边形为空");
                                return null;
                            }
                        }
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug("In IsolineProcess getPolygonList() (" + srcSLevel + ", " + srcELevel + ") 获取的多边形为空");
                        return null;
                    }
                } else {
                    if (logger.isDebugEnabled())
                        logger.debug("In IsolineProcess getPolygonList() (" + srcSLevel + ", " + srcELevel + ") 获取的多边形为空");
                    return null;
                }
            }

            try {
                if (polygonContainList == null || polygonContainList.isEmpty()) {
                    srcPolygonContainListBuild();
                }
            } catch (Exception e) {
//				logger.println(e.getMessage());
                e.printStackTrace();
                return null;
            }

            int sIndex = allLevels.indexOf(sLevel);
            int eIndex = allLevels.indexOf(eLevel);
            int i = sIndex < eIndex ? sIndex : eIndex;//小
            int j = sIndex > eIndex ? sIndex : eIndex;//大

            if (i == j) {//起止值相等
                long s = System.currentTimeMillis();
                //处理所有值为level的多边形
                polygonListBuild(allLevels.get(i), allLevels.get(j), srcSLevel, srcELevel);
                if (logger.isDebugEnabled())
                    logger.debug("填充用时：" + (System.currentTimeMillis() - s));
                //复位已填充
                for (int t = 0; t < polygonContainList.size(); t++) {
                    polygonContainList.get(t).isAddToFillPolygon = false;
                }
            } else {
                for (int k = i; k < j; k++) {
                    long s = System.currentTimeMillis();
                    //		 处理所有值为level的多边形
                    polygonListBuild(allLevels.get(k), allLevels.get(k + 1), srcSLevel, srcELevel);
                    //		处理所有值为level+delta多边形
                    polygonListBuild(allLevels.get(k + 1), allLevels.get(k), srcSLevel, srcELevel);
                    if (logger.isDebugEnabled())
                        logger.debug("填充用时：" + (System.currentTimeMillis() - s));
                    //复位已填充
                    for (int t = 0; t < polygonContainList.size(); t++) {
                        polygonContainList.get(t).isAddToFillPolygon = false;
                    }
                }
            }
        }

        if (resPolygonList.isEmpty()) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess getPolygonList() (" + srcSLevel + ", " + srcELevel + ") 获取的多边形为空");
            return null;
        }
        return resPolygonList;
    }

    /**
     * 追踪完成后的所有等值线，是否存在level级的等值线
     *
     * @param level
     * @return 包含与否等级为level的等值线
     */
    private boolean isIsolineContainLevel(float level) {
        if (procIsolineDataList == null || procIsolineDataList.isEmpty()) {
            // 重新追踪
            procIsolineDataList = new ArrayList<IsolineDataProc>();
            if (!getIsolineDataProcList()) {
                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess isIsolineContainLevel() 不存在任何等级的等值线");
                return false;
            }
        }

        if (procIsolineDataList.isEmpty()) {
            if (logger.isDebugEnabled()) logger.debug("In IsolineProcess isIsolineContainLevel() 不存在任何等级的等值线");
            return false;
        }

        boolean flag = false;
//		if(!allLevels.contains(level)){
//			return flag;
//		}

        for (int i = 0; i < procIsolineDataList.size(); i++) {
            if (Math.abs(level - procIsolineDataList.get(i).val) < precision) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 传入的填充等级是否包含level
     *
     * @param level
     * @return
     */
    private boolean isFillLevelContainLevel(float level) {
        boolean flag = false;
        if (!this.attr.isSetFillLevel()) {
            return flag;
        }
        for (int i = 0; i < this.attr.getFillLevel().length; i++) {
            if (Math.abs(level - attr.getFillLevel()[i]) < precision) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 判断posInnerList是加入内边界，还是外边界(首尾相连避免交叉,需要排序)
     *
     * @param outerDirection
     * @param innerDirection
     * @param innerSESide
     * @param posOuterList
     * @param posInnerList
     * @return
     */
    private int[] isOuterBoundry(int outerDirection, int innerDirection, int innerSESide, List<PositionVec> posOuterList, List<PositionVec> posInnerList) {
        //判断posInnerList1的边界点是否在grid边界,不在则addInner
        //方向为0的不予考虑
        int[] flagIndex = new int[]{0, 0};
        if (outerDirection == 0 || innerDirection == 0) {
            return flagIndex;
        } else {
            int latlon = 0;
            if (innerSESide == 1 || innerSESide == 3) {
                latlon = 1;//经度同  按纬度排序
            } else if (innerSESide == 2 || innerSESide == 4) {
                latlon = 2;//纬度同  按经度排序
            } else {
                return flagIndex;
            }
            double latSInner = posInnerList.get(0).latitude.degrees;
            double lonSInner = posInnerList.get(0).longitude.degrees;
            double latEInner = posInnerList.get(posInnerList.size() - 1).latitude.degrees;
            double lonEInner = posInnerList.get(posInnerList.size() - 1).longitude.degrees;

            int flag = 0;
            int indexInner = 0;
            int size = posOuterList.size();
            ArrayList<Double> latList = new ArrayList<Double>(size);
            ArrayList<Double> lonList = new ArrayList<Double>(size);

            float[] xleft = xStartAry;
            float[] xright = xEndAry;
            float[] xbottom = StrArrayUtil.getOneRowFromArray(gridXArys, 0);
            float[] xtop = StrArrayUtil.getOneRowFromArray(gridXArys, gridRows - 1);
            float[] yleft = StrArrayUtil.getOneColFromArray(gridYArys, 0);
            float[] yright = StrArrayUtil.getOneColFromArray(gridYArys, gridCols - 1);
            float[] ybottom = yStartAry;
            float[] ytop = yEndAry;
            float[] xleftMinMax = StrArrayUtil.getMinMaxVal(xStartAry);
            float[] xrightMinMax = StrArrayUtil.getMinMaxVal(xEndAry);
            float[] ybottomMinMax = StrArrayUtil.getMinMaxVal(yStartAry);
            float[] ytopMinMax = StrArrayUtil.getMinMaxVal(yEndAry);
            ArrayList<Integer> indexList = new ArrayList<Integer>();
            if (latlon == 1) {
                //1找到posOuterList中同经度的点，按纬度排序index， index+1
                if (innerSESide == 1) {
                    for (int k = 0; k < size; k++) {
                        double outerLon = posOuterList.get(k).longitude.degrees;
                        if (outerLon + (1e-5) >= xleftMinMax[0] && outerLon - (1e-5) <= xleftMinMax[1]) {
                            double outerLat = posOuterList.get(k).latitude.degrees;
                            Point2D.Float pt = new Point2D.Float();
                            pt.x = (float) outerLon;
                            pt.y = (float) outerLat;
                            if (MIDS3DMath.isOnArraySide(xleft, yleft, 0, pt)) {
                                latList.add(outerLat);
                                indexList.add(k);
                            }
                        }
                    }
                } else if (innerSESide == 3) {//right
                    for (int k = 0; k < size; k++) {
                        double outerLon = posOuterList.get(k).longitude.degrees;
                        if (outerLon + (1e-5) >= xrightMinMax[0] && outerLon - (1e-5) <= xrightMinMax[1]) {
                            double outerLat = posOuterList.get(k).latitude.degrees;
                            Point2D.Float pt = new Point2D.Float();
                            pt.x = (float) outerLon;
                            pt.y = (float) outerLat;
                            if (MIDS3DMath.isOnArraySide(xright, yright, 0, pt)) {
                                latList.add(outerLat);
                                indexList.add(k);
                            }
                        }
                    }
                } else {
                    logger.error("In IsolineProcess isOuterBoundry()出错");
                }

                Collections.sort(latList);

                if (gridYLeftEnd > gridYLeftStart && gridYRightEnd > gridYRightStart) {
                    if (innerSESide == 1) {//开始
                        if (outerDirection == 1) {
                            for (int i = 0; i < latList.size() - 1; i++) {
                                if (latSInner > latList.get(i) && latSInner < latList.get(i + 1)) {
                                    indexInner = indexList.get(i) + 1;
                                    flag = 1;
                                    break;
                                }//endif
                            }//endfor
                        }//endif
                        else if (outerDirection == 2) {
                            for (int i = 0; i < latList.size() - 1; i++) {
                                if (latSInner > latList.get(i + 1) && latSInner < latList.get(i)) {
                                    indexInner = indexList.get(i) + 1;
                                    flag = 1;
                                    break;
                                }//endif
                            }//endfor
                        }//end elseif
                        else {
                            throw new RuntimeErrorException(null, "In IsolineProcess() isOuterBoundry() 出错");
                        }//endelse
                    }//endif
                    else if (innerSESide == 3) {//结束
                        if (outerDirection == 1) {
                            for (int i = 0; i < latList.size() - 1; i++) {
                                if (latSInner > latList.get(i + 1) && latSInner < latList.get(i)) {
                                    indexInner = indexList.get(i) + 1;
                                    flag = 1;
                                    break;
                                }//endif
                            }//endfor
                        }//endif
                        else if (outerDirection == 2) {
                            for (int i = 0; i < latList.size() - 1; i++) {
                                if (latSInner > latList.get(i) && latSInner < latList.get(i + 1)) {
                                    indexInner = indexList.get(i) + 1;
                                    flag = 1;
                                    break;
                                }//endif
                            }//endfor
                        }//end elseif
                        else {
                            throw new RuntimeErrorException(null, "In IsolineProcess() isOuterBoundry() 出错");
                        }//endelse
                    }//end elseif
                    else {
                        throw new RuntimeErrorException(null, "In IsolineProcess() isOuterBoundry() 出错");
                    }//endelse
                }//endif
                else {
                    if (innerSESide == 1) {
                        if (outerDirection == 1) {
                            for (int i = 0; i < latList.size() - 1; i++) {
                                if (latSInner > latList.get(i + 1) && latSInner < latList.get(i)) {
                                    indexInner = indexList.get(i) + 1;
                                    flag = 1;
                                    break;
                                }//endif
                            }//endfor
                        }//endif
                        else if (outerDirection == 2) {
                            for (int i = 0; i < latList.size() - 1; i++) {
                                if (latSInner > latList.get(i) && latSInner < latList.get(i + 1)) {
                                    indexInner = indexList.get(i) + 1;
                                    flag = 1;
                                    break;
                                }//endif
                            }//endfor
                        }//end elseif
                        else {
                            throw new RuntimeErrorException(null, "In IsolineProcess() isOuterBoundry() 出错");
                        }//endelse
                    }//endif
                    else if (innerSESide == 3) {
                        if (outerDirection == 1) {
                            for (int i = 0; i < latList.size() - 1; i++) {
                                if (latSInner > latList.get(i) && latSInner < latList.get(i + 1)) {
                                    indexInner = indexList.get(i) + 1;
                                    flag = 1;
                                    break;
                                }//endif
                            }//endfor
                        }//endif
                        else if (outerDirection == 2) {
                            for (int i = 0; i < latList.size() - 1; i++) {
                                if (latSInner > latList.get(i + 1) && latSInner < latList.get(i)) {
                                    indexInner = indexList.get(i) + 1;
                                    flag = 1;
                                    break;
                                }//endif
                            }//endfor
                        }//end elseif
                        else {
                            throw new RuntimeErrorException(null, "In IsolineProcess() isOuterBoundry() 出错");
                        }//endelse
                    }//end elseif
                    else {
                        throw new RuntimeErrorException(null, "In IsolineProcess() isOuterBoundry() 出错");
                    }//endelse
                }//endelse
            } else if (latlon == 2) {
                //2找到posOuterList中同纬度的点，按纬度排序index， index+1
                if (innerSESide == 2) {//bottom
                    for (int k = 0; k < size; k++) {
                        double outerLat = posOuterList.get(k).latitude.degrees;
                        if (outerLat + (1e-5) >= ybottomMinMax[0] && outerLat - (1e-5) <= ybottomMinMax[1]) {
                            double outerLon = posOuterList.get(k).longitude.degrees;
                            Point2D.Float pt = new Point2D.Float();
                            pt.x = (float) outerLon;
                            pt.y = (float) outerLat;
                            if (MIDS3DMath.isOnArraySide(xbottom, ybottom, 1, pt)) {
                                lonList.add(outerLon);
                                indexList.add(k);
                            }
                        }
                    }
                } else if (innerSESide == 4) {//top
                    for (int k = 0; k < size; k++) {
                        double outerLat = posOuterList.get(k).latitude.degrees;
                        if (outerLat + (1e-5) >= ytopMinMax[0] && outerLat - (1e-5) <= ytopMinMax[1]) {
                            double outerLon = posOuterList.get(k).longitude.degrees;
                            Point2D.Float pt = new Point2D.Float();
                            pt.x = (float) outerLon;
                            pt.y = (float) outerLat;
                            if (MIDS3DMath.isOnArraySide(xtop, ytop, 1, pt)) {
                                lonList.add(outerLon);
                                indexList.add(k);
                            }
                        }
                    }
                } else {
                    logger.error("In IsolineProcess isOuterBoundry()出错");
                }
                Collections.sort(lonList);

                if (gridXTopEnd > gridXTopStart && gridXBottomEnd > gridXBottomStart) {
                    if (innerSESide == 2) {
                        if (outerDirection == 1) {
                            for (int i = 0; i < lonList.size() - 1; i++) {
                                if (lonSInner > lonList.get(i + 1) && lonSInner < lonList.get(i)) {
                                    indexInner = indexList.get(i) + 1;
                                    flag = 1;
                                    break;
                                }//endif
                            }//endfor
                        }//endif
                        else if (outerDirection == 2) {
                            for (int i = 0; i < lonList.size() - 1; i++) {
                                if (lonSInner > lonList.get(i) && lonSInner < lonList.get(i + 1)) {
                                    indexInner = indexList.get(i) + 1;
                                    flag = 1;
                                    break;
                                }//endif
                            }//endfor
                        }//end elseif
                        else {
                            throw new RuntimeErrorException(null, "In IsolineProcess() isOuterBoundry() 出错");
                        }//endelse
                    }//endif
                    else if (innerSESide == 4) {
                        if (outerDirection == 1) {
                            for (int i = 0; i < lonList.size() - 1; i++) {
                                if (lonSInner > lonList.get(i) && lonSInner < lonList.get(i + 1)) {
                                    indexInner = indexList.get(i) + 1;
                                    flag = 1;
                                    break;
                                }//endif
                            }//endfor
                        }//endif
                        else if (outerDirection == 2) {
                            for (int i = 0; i < lonList.size() - 1; i++) {
                                if (lonSInner > lonList.get(i + 1) && lonSInner < lonList.get(i)) {
                                    indexInner = indexList.get(i) + 1;
                                    flag = 1;
                                    break;
                                }//endif
                            }//endfor
                        }//end elseif
                        else {
                            throw new RuntimeErrorException(null, "In IsolineProcess() isOuterBoundry() 出错");
                        }//endelse
                    }//end elseif
                    else {
                        throw new RuntimeErrorException(null, "In IsolineProcess() isOuterBoundry() 出错");
                    }//endelse
                }//endif
                else {
                    if (innerSESide == 2) {
                        if (outerDirection == 1) {
                            for (int i = 0; i < lonList.size() - 1; i++) {
                                if (lonSInner > lonList.get(i) && lonSInner < lonList.get(i + 1)) {
                                    indexInner = indexList.get(i) + 1;
                                    flag = 1;
                                    break;
                                }//endif
                            }//endfor
                        }//endif
                        else if (outerDirection == 2) {
                            for (int i = 0; i < lonList.size() - 1; i++) {
                                if (lonSInner > lonList.get(i + 1) && lonSInner < lonList.get(i)) {
                                    indexInner = indexList.get(i) + 1;
                                    flag = 1;
                                    break;
                                }//endif
                            }//endfor
                        }//end elseif
                        else {
                            throw new RuntimeErrorException(null, "In IsolineProcess() isOuterBoundry() 出错");
                        }//endelse
                    }//endif
                    else if (innerSESide == 4) {
                        if (outerDirection == 1) {
                            for (int i = 0; i < lonList.size() - 1; i++) {
                                if (lonSInner > lonList.get(i + 1) && lonSInner < lonList.get(i)) {
                                    indexInner = indexList.get(i) + 1;
                                    flag = 1;
                                    break;
                                }//endif
                            }//endfor
                        }//endif
                        else if (outerDirection == 2) {
                            for (int i = 0; i < lonList.size() - 1; i++) {
                                if (lonSInner > lonList.get(i) && lonSInner < lonList.get(i + 1)) {
                                    indexInner = indexList.get(i) + 1;
                                    flag = 1;
                                    break;
                                }//endif
                            }//endfor
                        }//end elseif
                        else {
                            throw new RuntimeErrorException(null, "In IsolineProcess() isOuterBoundry() 出错");
                        }//endelse
                    }//end elseif
                    else {
                        throw new RuntimeErrorException(null, "In IsolineProcess() isOuterBoundry() 出错");
                    }//endelse
                }//endelse
            } else {
                // logger.error("latSInner: " + latSInner + "; lonSInner: " + lonSInner + "; latEInner: " + latEInner + "; lonEInner: " + lonEInner);
                throw new RuntimeErrorException(null, "In IsolineProcess() isOuterBoundry() 出错");
            }

            flagIndex[0] = flag;
            flagIndex[1] = indexInner;
            return flagIndex;
        }
    }

    /**
     * 是否为前一点
     *
     * @param curPt
     * @param prePt
     * @return
     */
    private boolean isPrePt(Isopoint curPt, Isopoint prePt) {
        if (curPt.equals(prePt)) {
            return true;
        }
        return false;
    }

    /**
     * 八个方向上是否存在边界
     *
     * @param curPt
     * @return
     */
    private boolean isSecondEdgePt(Isopoint curPt) {
        int row = curPt.getRow();
        int col = curPt.getCol();
        List<Isopoint> ptList = new ArrayList<Isopoint>();
        Isopoint pt = null;
        float[][] gridData = gridDataList.get(0).getGridData();
        //正下方bottom
        if (row - 1 >= 0 && gridData[row - 1][col] == NULLVAL) {
            pt = new Isopoint();
            pt.setAll(row - 1, col, null);
            ptList.add(pt);
        }
        //正上方top
        if (row + 1 <= gridRows - 1 && gridData[row + 1][col] == NULLVAL) {
            pt = new Isopoint();
            pt.setAll(row + 1, col, null);
            ptList.add(pt);
        }
        //左边left
        if (col - 1 >= 0 && gridData[row][col - 1] == NULLVAL) {
            pt = new Isopoint();
            pt.setAll(row, col - 1, null);
            ptList.add(pt);
        }
        //右边right
        if (col + 1 <= gridCols - 1 && gridData[row][col + 1] == NULLVAL) {
            pt = new Isopoint();
            pt.setAll(row, col + 1, null);
            ptList.add(pt);
        }
        //左下方leftBottom
        if (row - 1 >= 0 && col - 1 >= 0 && gridData[row - 1][col - 1] == NULLVAL) {
            pt = new Isopoint();
            pt.setAll(row - 1, col - 1, null);
            ptList.add(pt);
        }
        //右下方rightBottom
        if (row - 1 >= 0 && col + 1 <= gridCols - 1 && gridData[row - 1][col + 1] == NULLVAL) {
            pt = new Isopoint();
            pt.setAll(row - 1, col + 1, null);
            ptList.add(pt);
        }
        //左上方leftTop
        if (row + 1 <= gridRows - 1 && col - 1 >= 0 && gridData[row + 1][col - 1] == NULLVAL) {
            pt = new Isopoint();
            pt.setAll(row + 1, col - 1, null);
            ptList.add(pt);
        }
        //右上方rightTop
        if (row + 1 <= gridRows - 1 && col + 1 <= gridCols - 1 && gridData[row + 1][col + 1] == NULLVAL) {
            pt = new Isopoint();
            pt.setAll(row + 1, col + 1, null);
            ptList.add(pt);
        }
        //上下左右边界
        if (row == 0 || row == gridRows - 1 || col == 0 || col == gridCols - 1) {
            pt = new Isopoint();
            pt.setAll(row, col, null);
            ptList.add(pt);
        }
        if (ptList != null && !ptList.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否存在唯一的联通点，须考虑是否存在等值点,所在等值线上的点只有一个时另考虑
     *
     * @param curPt
     * @param level1
     * @param level2
     * @param allIsolineList
     * @param isConsiderIsoline
     * @return
     */
    private boolean isTheOnlyEdge(Isopoint curPt, float level1, float level2, List<IsolineDataProc> allIsolineList, boolean isConsiderIsoline) {
        int row = curPt.getRow();
        int col = curPt.getCol();

        if (isConsiderIsoline) {
            if (isHasIsopointNonEnd(curPt, level1, level2, allIsolineList)) {
                return false;
            }
        }

        List<Isopoint> ptList = new ArrayList<Isopoint>();
        Isopoint pt = null;
        float[][] gridData = gridDataList.get(0).getGridData();
        //正下方bottom
        if (row - 1 >= 0 && gridData[row - 1][col] != NULLVAL) {
            pt = new Isopoint();
            pt.setAll(row - 1, col, null);
            if (!pt.equals(curIsopoint) && !isContainPt(pt) && isBetweenTwoVals(gridData[row - 1][col], level1, level2)) {
                ptList.add(pt);
            }
        }
        //正上方top
        if (row + 1 <= gridRows - 1 && gridData[row + 1][col] != NULLVAL) {
            pt = new Isopoint();
            pt.setAll(row + 1, col, null);
            if (!pt.equals(curIsopoint) && !isContainPt(pt) && isBetweenTwoVals(gridData[row + 1][col], level1, level2)) {
                ptList.add(pt);
            }
        }
        //左边left
        if (col - 1 >= 0 && gridData[row][col - 1] != NULLVAL) {
            pt = new Isopoint();
            pt.setAll(row, col - 1, null);
            if (!pt.equals(curIsopoint) && !isContainPt(pt) && isBetweenTwoVals(gridData[row][col - 1], level1, level2)) {
                ptList.add(pt);
            }
        }
        //右边right
        if (col + 1 <= gridCols - 1 && gridData[row][col + 1] != NULLVAL) {
            pt = new Isopoint();
            pt.setAll(row, col + 1, null);
            if (!pt.equals(curIsopoint) && !isContainPt(pt) && isBetweenTwoVals(gridData[row][col + 1], level1, level2)) {
                ptList.add(pt);
            }
        }
        //左下方leftBottom
        if (row - 1 >= 0 && col - 1 >= 0 && gridData[row - 1][col - 1] != NULLVAL) {
            pt = new Isopoint();
            pt.setAll(row - 1, col - 1, null);
            if (!pt.equals(curIsopoint) && !isContainPt(pt) && isBetweenTwoVals(gridData[row - 1][col - 1], level1, level2)) {
                ptList.add(pt);
            }
        }
        //右下方rightBottom
        if (row - 1 >= 0 && col + 1 <= gridCols - 1 && gridData[row - 1][col + 1] != NULLVAL) {
            pt = new Isopoint();
            pt.setAll(row - 1, col + 1, null);
            if (!pt.equals(curIsopoint) && !isContainPt(pt) && isBetweenTwoVals(gridData[row - 1][col + 1], level1, level2)) {
                ptList.add(pt);
            }
        }
        //左上方leftTop
        if (row + 1 <= gridRows - 1 && col - 1 >= 0 && gridData[row + 1][col - 1] != NULLVAL) {
            pt = new Isopoint();
            pt.setAll(row + 1, col - 1, null);
            if (!pt.equals(curIsopoint) && !isContainPt(pt) && isBetweenTwoVals(gridData[row + 1][col - 1], level1, level2)) {
                ptList.add(pt);
            }
        }
        //右上方rightTop
        if (row + 1 <= gridRows - 1 && col + 1 <= gridCols - 1 && gridData[row + 1][col + 1] != NULLVAL) {
            pt = new Isopoint();
            pt.setAll(row + 1, col + 1, null);
            if (!pt.equals(curIsopoint) && !isContainPt(pt) && isBetweenTwoVals(gridData[row + 1][col + 1], level1, level2)) {
                ptList.add(pt);
            }
        }
        //上下左右边界
        if (row == 0 || row == gridRows - 1 || col == 0 || col == gridCols - 1) {
            pt = new Isopoint();
            pt.setAll(row, col, null);
        }
        if (ptList != null && !ptList.isEmpty() && ptList.size() >= 1) {
            return false;
        }
        return true;
    }

    /**
     * 四方向上是否存在边界
     *
     * @return
     */
    private boolean isBoundaryEdge(Isopoint curPt) {
        return isEdgePt(curPt);
    }

    //四个方向上是否存在边界
    private boolean isEdgePt(Isopoint curPt) {
        int row = curPt.getRow();
        int col = curPt.getCol();
        List<Isopoint> ptList = new ArrayList<Isopoint>();
        Isopoint pt1 = new Isopoint();
        Isopoint pt2 = new Isopoint();
        Isopoint pt3 = new Isopoint();
        Isopoint pt4 = new Isopoint();
        Isopoint pt5 = new Isopoint();
        float[][] gridData = gridDataList.get(0).getGridData();
        //正下方bottom
        if (row - 1 >= 0 && gridData[row - 1][col] == NULLVAL) {
            pt1.setAll(row - 1, col, null);
            ptList.add(pt1);
        }
        //正上方top
        if (row + 1 <= gridRows - 1 && gridData[row + 1][col] == NULLVAL) {
            pt2.setAll(row + 1, col, null);
            ptList.add(pt2);
        }
        //左边left
        if (col - 1 >= 0 && gridData[row][col - 1] == NULLVAL) {
            pt3.setAll(row, col - 1, null);
            ptList.add(pt3);
        }
        //右边right
        if (col + 1 <= gridCols - 1 && gridData[row][col + 1] == NULLVAL) {
            pt4.setAll(row, col + 1, null);
            ptList.add(pt4);
        }
        //上下左右边界
        if (row == 0 || row == gridRows - 1 || col == 0 || col == gridCols - 1) {
            pt5.setAll(row, col, null);
            ptList.add(pt5);
        }
        if (ptList != null && !ptList.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * 当前的等值点的值是否在填充的两个等级之间
     *
     * @param val
     * @param val1
     * @param val2
     * @return
     */
    private boolean isBetweenTwoVals(float val, float val1, float val2) {
        float minVal = Math.min(val1, val2);
        float maxVal = Math.max(val1, val2);
        if (val >= minVal && val <= maxVal) {
            return true;
        }
        return false;
    }

    //当前的等值点的值是否在填充的两个等级之间
    private boolean isBetweenTwoVals(Isopoint curPt, float val1, float val2) {
        float[][] gridData = gridDataList.get(0).getGridData();
        int row = curPt.getRow();
        int col = curPt.getCol();
        float minVal = Math.min(val1, val2);
        float maxVal = Math.max(val1, val2);
        if (gridData[row][col] >= minVal && gridData[row][col] <= maxVal) {
            return true;
        }
        return false;
    }

    //list中是否包含当前点
    private boolean isContainPt(List<Point2D.Double> ptsList, Isopoint curPt) {
        Point2D.Double pt = getPos(curPt.getRow(), curPt.getCol());
        for (int i = 0; i < ptsList.size(); i++) {
            if (ptsList.get(i).equals(pt)) {
                return true;
            }
        }
        return false;
    }

    //fillIsopointList是否包含当前点
    private boolean isContainPt(Isopoint curPt) {
        if (fillIsopointList == null || fillIsopointList.isEmpty()) {
            return false;
        }
        for (int i = 0; i < fillIsopointList.size(); i++) {
            Isopoint tmpPt = fillIsopointList.get(i);
            if (tmpPt.getRow() == curPt.getRow() && tmpPt.getCol() == curPt.getCol() &&
                    tmpPt.getIsHorizon() == curPt.getIsHorizon()) {
                return true;
            }
        }
        return false;
    }

    //获取当前点上的值
    private float getIsopointVal(Isopoint curPt) {
        int row = curPt.getRow();
        int col = curPt.getCol();
        Boolean isHorizon = curPt.getIsHorizon();
        if (isHorizon != null) {
            return NULLVAL;
        }
        return gridDataList.get(0).getGridData()[row][col];
    }

    private static Isopoint nextIsoptOnLine = new Isopoint();

    //获取相邻两点之间的距离,不相邻 return -1
    private float getDistance(Isopoint pt1, Isopoint pt2) {
        int row1 = pt1.getRow();
        int col1 = pt1.getCol();
        Boolean isH1 = pt1.getIsHorizon();
        int row2 = pt2.getRow();
        int col2 = pt2.getCol();
        Boolean isH2 = pt2.getIsHorizon();
        if (isH1 != null || isH2 != null) {
            return -1;
        }

        if ((row1 == row2 && Math.abs(col1 - col2) == 1) || (col1 == col2 && Math.abs(row1 - row2) == 1)) {
            return 1;
        } else if ((row1 == row2 && Math.abs(col1 - col2) == 2) || (col1 == col2 && Math.abs(row1 - row2) == 2)) {
            return 2;
        } else if (Math.abs(row1 - row2) == 1 && Math.abs(col1 - col2) == 1) {
            return 1.414f;//sqrt(2)
        } else if ((Math.abs(col1 - col2) == 1 && Math.abs(row1 - row2) == 2) || (Math.abs(row1 - row2) == 1 && Math.abs(col1 - col2) == 2)) {
            return 2.236f;//sqrt(5)
        } else if (Math.abs(row1 - row2) == 2 && Math.abs(col1 - col2) == 2) {
            return 2.929f;//2*sqrt(2)
        } else {
            return -1;
        }
    }

    //获取距离最远的那个点
    private Isopoint getFurtherPt(List<Isopoint> curPt, Isopoint prePt) {
        int row = prePt.getRow();
        int col = prePt.getCol();
        double dis = Math.pow(row - curPt.get(0).getRow(), 2) + Math.pow(col - curPt.get(0).getCol(), 2);
        int index = 0;
        for (int i = 0; i < curPt.size(); i++) {
            Isopoint pt = curPt.get(i);
            int tmpRow = pt.getRow();
            int tmpCol = pt.getCol();
            double tmpDis = Math.pow(row - tmpRow, 2) + Math.pow(col - tmpCol, 2);
            if (tmpDis > dis) {
                dis = tmpDis;
                index = i;
            }
        }
        return curPt.get(index);
    }

    private boolean canConnect(IsolineDataProc isoline) {
        if (fillLineIndexList != null && !fillLineIndexList.isEmpty()) {
            int size = fillLineIndexList.size();
            int lastIndex = fillLineIndexList.get(size - 1);

            if (lastIndex == isoline.indexProc && size > 1) {
                return false;
            }
        }

        return true;
    }


    //获取两个点之间是否存在等值线上的点,考虑只有两个点的等值线连起点还是终点
    private List<Point2D.Double> getIsopointOnline(Isopoint nextPt, Isopoint curPt, List<IsolineDataProc> allIsolineList) {
        int nextRow = nextPt.getRow();
        int nextCol = nextPt.getCol();

        int curRow = curPt.getRow();
        int curCol = curPt.getCol();

        int preRow = preIsopoint.getRow();
        int preCol = preIsopoint.getCol();
        Boolean preH = preIsopoint.getIsHorizon();

        Boolean isHorizon = false;
        onlineIndex = -1;
        if (nextRow != curRow && nextCol != curCol) {
//			logger.println("不在同一边上");
            return null;
        } else if (nextRow == curRow && nextCol != curCol) {//考虑先连接起点还是连接终点
            if (allIsolineList == null || allIsolineList.isEmpty()) {
                throw new RuntimeException("In IsolineProcess.getIsopointOnline()中allIsolineList为空");
            }

            isHorizon = true;
            if (nextCol > curCol) {
                for (int i = 0; i < allIsolineList.size(); i++) {
                    IsolineDataProc isoline = allIsolineList.get(i);
                    if (isoline.isClosed) {
                        continue;
                    }

                    if (backFillLineIndexList != null && !backFillLineIndexList.isEmpty()) {
                        boolean isFind = false;
                        for (int j = 0; j < backFillLineIndexList.size(); j++) {
                            if (backFillLineIndexList.get(j) == isoline.indexProc) {
                                isFind = true;
                                break;
                            }
                        }
                        if (isFind) {
                            continue;
                        }
                    }
                    //处理当填充到lastIndex等值线时能否连通闭合
                    if (!canConnect(isoline)) {
                        continue;
                    }

                    List<Integer> rowList = isoline.rowsList;
                    List<Integer> colList = isoline.colsList;
                    List<Boolean> startHList = isoline.isHorizonList;
                    int startR = rowList.get(0);
                    int startC = colList.get(0);
                    Boolean startH = startHList.get(0);
                    int size = rowList.size();
                    int endR = rowList.get(size - 1);
                    int endC = colList.get(size - 1);
                    Boolean endH = startHList.get(size - 1);
                    if ((startR == curRow && startC == curCol && startH == isHorizon) && !(startR == preRow && startC == preCol && startH == preH)) {
                        nextIsoptOnLine = new Isopoint();
                        nextIsoptOnLine.setAll(endR, endC, endH);
                        fillLineDirectionList.add(1);
                        fillLineIndexList.add(isoline.indexProc);
                        fillLineValList.add(isoline.val);
                        onlineIndex = 0;
                        return isoline.lineList2D;
                    } else if ((endR == curRow && endC == curCol && endH == isHorizon) && !(endR == preRow && endC == preCol && endH == preH)) {
                        List<Point2D.Double> ptList = new ArrayList<Point2D.Double>();
                        for (int j = isoline.num - 1; j >= 0; j--) {
                            ptList.add(isoline.lineList2D.get(j));
                        }
                        nextIsoptOnLine = new Isopoint();
                        nextIsoptOnLine.setAll(startR, startC, startH);
                        fillLineDirectionList.add(-1);
                        fillLineIndexList.add(isoline.indexProc);
                        fillLineValList.add(isoline.val);
                        onlineIndex = isoline.num - 1;
                        return ptList;
                    } else {
                        continue;
                    }
                }
            } else {
                for (int i = 0; i < allIsolineList.size(); i++) {
                    IsolineDataProc isoline = allIsolineList.get(i);
                    if (isoline.isClosed) {
                        continue;
                    }

                    if (backFillLineIndexList != null && !backFillLineIndexList.isEmpty()) {
                        boolean isFind = false;
                        for (int j = 0; j < backFillLineIndexList.size(); j++) {
                            if (backFillLineIndexList.get(j) == isoline.indexProc) {
                                isFind = true;
                                break;
                            }
                        }
                        if (isFind) {
                            continue;
                        }
                    }
                    if (!canConnect(isoline)) {
                        continue;
                    }
                    List<Integer> rowList = isoline.rowsList;
                    List<Integer> colList = isoline.colsList;
                    List<Boolean> startHList = isoline.isHorizonList;
                    int startR = rowList.get(0);
                    int startC = colList.get(0);
                    Boolean startH = startHList.get(0);
                    int size = rowList.size();
                    int endR = rowList.get(rowList.size() - 1);
                    int endC = colList.get(colList.size() - 1);
                    Boolean endH = startHList.get(size - 1);
                    if ((startR == nextRow && startC == nextCol && startH == isHorizon) && !(startR == preRow && startC == preCol && startH == preH)) {
                        nextIsoptOnLine = new Isopoint();
                        nextIsoptOnLine.setAll(endR, endC, endH);
                        fillLineDirectionList.add(1);
                        fillLineIndexList.add(isoline.indexProc);
                        fillLineValList.add(isoline.val);
                        onlineIndex = 0;
                        return isoline.lineList2D;
                    } else if ((endR == nextRow && endC == nextCol && endH == isHorizon) && !(endR == preRow && endC == preCol && endH == preH)) {
                        //反向
                        List<Point2D.Double> ptList = new ArrayList<Point2D.Double>();
                        for (int j = isoline.num - 1; j >= 0; j--) {
                            ptList.add(isoline.lineList2D.get(j));
                        }
                        nextIsoptOnLine = new Isopoint();
                        nextIsoptOnLine.setAll(startR, startC, startH);
                        fillLineDirectionList.add(-1);
                        fillLineIndexList.add(isoline.indexProc);
                        fillLineValList.add(isoline.val);
                        onlineIndex = isoline.num - 1;
                        return ptList;
                    } else {
                        continue;
                    }
                }
            }
        } else if (nextRow != curRow && nextCol == curCol) {
            isHorizon = false;
            if (allIsolineList == null || allIsolineList.isEmpty()) {
                throw new RuntimeException("In IsolineProcess.getIsopointOnline()中allIsolineList为空");
            }
            if (nextRow > curRow) {
                for (int i = 0; i < allIsolineList.size(); i++) {
                    IsolineDataProc isoline = allIsolineList.get(i);
                    if (isoline.isClosed) {
                        continue;
                    }

                    if (backFillLineIndexList != null && !backFillLineIndexList.isEmpty()) {
                        boolean isFind = false;
                        for (int j = 0; j < backFillLineIndexList.size(); j++) {
                            if (backFillLineIndexList.get(j) == isoline.indexProc) {
                                isFind = true;
                                break;
                            }
                        }
                        if (isFind) {
                            continue;
                        }
                    }
                    if (!canConnect(isoline)) {
                        continue;
                    }
                    List<Integer> rowList = isoline.rowsList;
                    List<Integer> colList = isoline.colsList;
                    List<Boolean> startHList = isoline.isHorizonList;
                    int startR = rowList.get(0);
                    int startC = colList.get(0);
                    Boolean startH = startHList.get(0);
                    int size = rowList.size();
                    int endR = rowList.get(size - 1);
                    int endC = colList.get(size - 1);
                    Boolean endH = startHList.get(size - 1);
                    if ((startR == curRow && startC == curCol && startH == isHorizon) && !(startR == preRow && startC == preCol && startH == preH)) {
                        nextIsoptOnLine = new Isopoint();
                        nextIsoptOnLine.setAll(endR, endC, endH);
                        fillLineDirectionList.add(1);
                        fillLineIndexList.add(isoline.indexProc);
                        fillLineValList.add(isoline.val);
                        onlineIndex = 0;
                        return isoline.lineList2D;
                    } else if ((endR == curRow && endC == curCol && endH == isHorizon) && !(endR == preRow && endC == preCol && endH == preH)) {
                        List<Point2D.Double> ptList = new ArrayList<Point2D.Double>();
                        for (int j = isoline.num - 1; j >= 0; j--) {
                            ptList.add(isoline.lineList2D.get(j));
                        }
                        nextIsoptOnLine = new Isopoint();
                        nextIsoptOnLine.setAll(startR, startC, startH);
                        fillLineDirectionList.add(-1);
                        fillLineIndexList.add(isoline.indexProc);
                        fillLineValList.add(isoline.val);
                        onlineIndex = isoline.num - 1;
                        return ptList;
                    } else {
                        continue;
                    }
                }
            } else {
                for (int i = 0; i < allIsolineList.size(); i++) {
                    IsolineDataProc isoline = allIsolineList.get(i);
                    if (isoline.isClosed) {
                        continue;
                    }
                    if (backFillLineIndexList != null && !backFillLineIndexList.isEmpty()) {
                        boolean isFind = false;
                        for (int j = 0; j < backFillLineIndexList.size(); j++) {
                            if (backFillLineIndexList.get(j) == isoline.indexProc) {
                                isFind = true;
                                break;
                            }
                        }
                        if (isFind) {
                            continue;
                        }
                    }

                    if (!canConnect(isoline)) {
                        continue;
                    }

                    List<Integer> rowList = isoline.rowsList;
                    List<Integer> colList = isoline.colsList;
                    List<Boolean> startHList = isoline.isHorizonList;
                    int startR = rowList.get(0);
                    int startC = colList.get(0);
                    Boolean startH = startHList.get(0);
                    int size = rowList.size();
                    int endR = rowList.get(rowList.size() - 1);
                    int endC = colList.get(colList.size() - 1);
                    Boolean endH = startHList.get(size - 1);
                    if ((startR == nextRow && startC == nextCol && startH == isHorizon) && !(startR == preRow && startC == preCol && startH == preH)) {
                        nextIsoptOnLine = new Isopoint();
                        nextIsoptOnLine.setAll(endR, endC, endH);
                        fillLineDirectionList.add(1);
                        fillLineIndexList.add(isoline.indexProc);
                        fillLineValList.add(isoline.val);
                        onlineIndex = 0;
                        return isoline.lineList2D;
                    } else if ((endR == nextRow && endC == nextCol && endH == isHorizon) && !(endR == preRow && endC == preCol && endH == preH)) {
                        List<Point2D.Double> ptList = new ArrayList<Point2D.Double>();
                        for (int j = isoline.num - 1; j >= 0; j--) {
                            ptList.add(isoline.lineList2D.get(j));
                        }
                        nextIsoptOnLine = new Isopoint();
                        nextIsoptOnLine.setAll(startR, startC, startH);
                        fillLineDirectionList.add(-1);
                        fillLineIndexList.add(isoline.indexProc);
                        fillLineValList.add(isoline.val);
                        onlineIndex = isoline.num - 1;
                        return ptList;
                    } else {
                        continue;
                    }
                }
            }
        } else if (nextRow == curCol && nextCol == curCol) {
            if (logger.isDebugEnabled())
                logger.debug("为同一个点");
            return null;
        }
        return null;
    }

    private boolean isHasline(float level, int row, int col, Boolean isH, List<IsolineDataProc> allIsolineList) {
        if (allIsolineList == null || allIsolineList.isEmpty()) {
            throw new RuntimeException("In IsolineProcess.isHasline()中allIsolineList为空");
        }
        for (int i = 0; i < allIsolineList.size(); i++) {
            IsolineDataProc isoline = allIsolineList.get(i);
            if (level != isoline.val) {
                continue;
            }
            if (isoline.num == 1) {
                continue;
            }
            if (isoline.isClosed) {
                continue;
            }

            if (backFillLineIndexList != null && !backFillLineIndexList.isEmpty()) {
                boolean isFind = false;
                for (int j = 0; j < backFillLineIndexList.size(); j++) {
                    if (backFillLineIndexList.get(j) == isoline.indexProc) {
                        isFind = true;
                        break;
                    }
                }
                if (isFind) {
                    continue;
                }
            }

            //处理当填充到lastIndex等值线时能否连通闭合
            if (!canConnect(isoline)) {
                continue;
            }

            List<Integer> rowList = isoline.rowsList;
            List<Integer> colList = isoline.colsList;
            List<Boolean> startHList = isoline.isHorizonList;
            int startR = rowList.get(0);
            int startC = colList.get(0);
            Boolean startH = startHList.get(0);
            int size = rowList.size();
            int endR = rowList.get(size - 1);
            int endC = colList.get(size - 1);
            Boolean endH = startHList.get(size - 1);
            if ((startC == col && startH == isH && startR == row) || (endC == col && endH == isH && endR == row)) {
//				if(isTwoPt){
                if (isoline.indexProc == fillLineIndexList.get(0)) {
                    return true;
                }
                if (isoline.num == 2) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isHasIsopointNonEnd(Isopoint curPt, float level1, float level2, List<IsolineDataProc> allIsolineList) {
        int row = curPt.getRow();
        int col = curPt.getCol();
        List<Isopoint> ptList = new ArrayList<Isopoint>();
        Isopoint pt = null;
        float[][] gridData = gridDataList.get(0).getGridData();
        if (allIsolineList == null || allIsolineList.isEmpty()) {
            throw new RuntimeException("In IsolineProcess.isHasIsopointNonEnd()中allIsolineList为空");
        }

        //正下方bottom
        if (row - 1 >= 0 && gridData[row - 1][col] != NULLVAL) {
            pt = new Isopoint();
            pt.setAll(row - 1, col, null);
            if (isBetweenTwoVals(level1, gridData[row][col], gridData[row - 1][col])) {
                if (isHasline(level1, row - 1, col, false, allIsolineList)) {
                    ptList.add(pt);
                }
            } else if (isBetweenTwoVals(level2, gridData[row][col], gridData[row - 1][col])) {
                if (isHasline(level2, row - 1, col, false, allIsolineList)) {
                    ptList.add(pt);
                }
            }
        }

        //正上方top
        if (row + 1 <= gridRows - 1 && gridData[row + 1][col] != NULLVAL) {
            pt = new Isopoint();
            pt.setAll(row + 1, col, null);
            if (isBetweenTwoVals(level1, gridData[row][col], gridData[row + 1][col])) {
                if (isHasline(level1, row, col, false, allIsolineList)) {
                    ptList.add(pt);
                }
            } else if (isBetweenTwoVals(level2, gridData[row][col], gridData[row + 1][col])) {
                if (isHasline(level2, row, col, false, allIsolineList)) {
                    ptList.add(pt);
                }
            }
        }
        //左边left
        if (col - 1 >= 0 && gridData[row][col - 1] != NULLVAL) {
            pt = new Isopoint();
            pt.setAll(row, col - 1, null);
            if (isBetweenTwoVals(level1, gridData[row][col], gridData[row][col - 1])) {
                if (isHasline(level1, row, col - 1, true, allIsolineList)) {
                    ptList.add(pt);
                }
            } else if (isBetweenTwoVals(level2, gridData[row][col], gridData[row][col - 1])) {
                if (isHasline(level2, row, col - 1, true, allIsolineList)) {
                    ptList.add(pt);
                }
            }
        }
        //右边right
        if (col + 1 <= gridCols - 1 && gridData[row][col + 1] != NULLVAL) {
            pt = new Isopoint();
            pt.setAll(row, col + 1, null);
            if (isBetweenTwoVals(level1, gridData[row][col], gridData[row][col + 1])) {
                if (isHasline(level1, row, col, true, allIsolineList)) {
                    ptList.add(pt);
                }
            } else if (isBetweenTwoVals(level2, gridData[row][col], gridData[row][col + 1])) {
                if (isHasline(level2, row, col, true, allIsolineList)) {
                    ptList.add(pt);
                }
            }
        }

        if (ptList == null || ptList.isEmpty()) {
            return false;
        }
        return true;
    }

    //计算上下左右边界的时候不要重复计算
    private int getEdgeNum(Isopoint curPt) {
        int row = curPt.getRow();
        int col = curPt.getCol();
        float[][] gridData = gridDataList.get(0).getGridData();
        int num = 0;
        int constN1 = 1;
        int constN2 = 2;

        //四个角
        if (row == 0 && col == 0) {//左下角
            num += constN2;
        } else if (row == gridRows - 1 && col == 0) {//左上角
            num += constN2;
        } else if (row == 0 && col == gridCols - 1) {//右下角
            num += constN2;
        } else if (row == gridRows - 1 && col == gridCols - 1) {//右上角
            num += constN2;
        } else if (row == gridRows - 1 && col > 0 && col < gridCols - 1) {//上边界
            num += constN1;
        } else if (row == 0 && col > 0 && col < gridCols - 1) {//下边界
            num += constN1;
        } else if (col == 0 && row > 0 && row < gridRows - 1) {//左边界
            num += constN1;
        } else if (col == gridCols - 1 && row > 0 && row < gridRows - 1) {//右边界
            num += constN1;
        }

        //正下方bottom
        if (row - 1 >= 0 && gridData[row - 1][col] == NULLVAL) {
            num += constN1;
        }
        //正上方top
        if (row + 1 <= gridRows - 1 && gridData[row + 1][col] == NULLVAL) {
            num += constN1;
        }
        //左边left
        if (col - 1 >= 0 && gridData[row][col - 1] == NULLVAL) {
            num += constN1;
        }
        //右边right
        if (col + 1 <= gridCols - 1 && gridData[row][col + 1] == NULLVAL) {
            num += constN1;
        }
        //左下方leftBottom
        if (row - 1 >= 0 && col - 1 >= 0 && gridData[row - 1][col - 1] == NULLVAL) {
            num += constN1;
        }
        //右下方rightBottom
        if (row - 1 >= 0 && col + 1 <= gridCols - 1 && gridData[row - 1][col + 1] == NULLVAL) {
            num += constN1;
        }
        //左上方leftTop
        if (row + 1 <= gridRows - 1 && col - 1 >= 0 && gridData[row + 1][col - 1] == NULLVAL) {
            num += constN1;
        }
        //右上方rightTop
        if (row + 1 <= gridRows - 1 && col + 1 <= gridCols - 1 && gridData[row + 1][col + 1] == NULLVAL) {
            num += constN1;
        }

        return num;
    }

    //当前点是否加入了列表中
    private boolean isAdd(Isopoint curPt) {
        int row = curPt.getRow();
        int col = curPt.getCol();
        Boolean isH = curPt.getIsHorizon();

        for (int i = 0; i < level2IsopointList.size(); i++) {
            Isopoint tmpPt = level2IsopointList.get(i);
            if (tmpPt.getRow() != row) {
                continue;
            }
            if (tmpPt.getCol() != col) {
                continue;
            }
            if (tmpPt.getIsHorizon() != isH) {
                continue;
            }
            return true;
        }
        return false;
    }

    //获取4个方向的边界点数
    private int getBorderNum(Isopoint curPt) {
        int row = curPt.getRow();
        int col = curPt.getCol();
        float[][] gridData = gridDataList.get(0).getGridData();
        int borderNum = 0;
        int constN1 = 1;//20130609
        int constN2 = 2;
        //四个角
        if (row == 0 && col == 0) {
            borderNum += constN2;
        } else if (row == gridRows - 1 && col == 0) {
            borderNum += constN2;
        } else if (row == 0 && col == gridCols - 1) {
            borderNum += constN2;
        } else if (row == gridRows - 1 && col == gridCols - 1) {
            borderNum += constN2;
        } else if ((row == 0 || row == gridRows - 1) && col >= 1 && col <= gridCols - 1) {
            borderNum += constN1;
        } else if ((col == 0 || col == gridCols - 1) && row >= 1 && row <= gridRows - 1) {
            borderNum += constN1;
        }

        //正下方bottom
        if (row - 1 >= 0 && gridData[row - 1][col] == NULLVAL) {
            borderNum += constN1;
        }
        //正上方top
        if (row + 1 <= gridRows - 1 && gridData[row + 1][col] == NULLVAL) {
            borderNum += constN1;
        }
        //左边left
        if (col - 1 >= 0 && gridData[row][col - 1] == NULLVAL) {
            borderNum += constN1;
        }
        //右边right
        if (col + 1 <= gridCols - 1 && gridData[row][col + 1] == NULLVAL) {
            borderNum += constN1;
        }
        return borderNum;
    }

    //获取对角线方向的边界点数
    private int getCornerNum(Isopoint curPt) {
        int row = curPt.getRow();
        int col = curPt.getCol();
        float[][] gridData = gridDataList.get(0).getGridData();
        int cornerNum = 0;
        int constN1 = 1;//20130609
        int constN2 = 2;//
        if (row == 0 && col == 0) {//四个角
            cornerNum += constN2;
        } else if (row == gridRows - 1 && col == 0) {
            cornerNum += constN2;
        } else if (row == 0 && col == gridCols - 1) {
            cornerNum += constN2;
        } else if (row == gridRows - 1 && col == gridCols - 1) {
            cornerNum += constN2;
        } else if ((row == gridRows - 1 || row == 0) && col > 0 && col < gridCols - 1) {//上下边界
            cornerNum += constN1;
        } else if ((col == 0 || col == gridCols - 1) && row > 0 && row < gridRows - 1) {//左右边界
            cornerNum += constN1;
        }

        //左下方leftBottom
        if (row - 1 >= 0 && col - 1 >= 0 && gridData[row - 1][col - 1] == NULLVAL) {
            cornerNum += constN1;
        }
        //右下方rightBottom
        if (row - 1 >= 0 && col + 1 <= gridCols - 1 && gridData[row - 1][col + 1] == NULLVAL) {
            cornerNum += constN1;
        }
        //左上方leftTop
        if (row + 1 <= gridRows - 1 && col - 1 >= 0 && gridData[row + 1][col - 1] == NULLVAL) {
            cornerNum += constN1;
        }
        //右上方rightTop
        if (row + 1 <= gridRows - 1 && col + 1 <= gridCols - 1 && gridData[row + 1][col + 1] == NULLVAL) {
            cornerNum += constN1;
        }

        return cornerNum;
    }

    //获取八个方向上非空点
    private List<Isopoint> get8DirectionPts(Isopoint curPt) {
        int row = curPt.getRow();
        int col = curPt.getCol();
        float[][] gridData = gridDataList.get(0).getGridData();

        //八个方向，考虑大小、距离因素
        Isopoint topPt = null;
        Isopoint leftPt = null;
        Isopoint rightPt = null;
        Isopoint bottomPt = null;

        Isopoint leftTopPt = null;
        Isopoint rightTopPt = null;
        Isopoint leftBottomPt = null;
        Isopoint rightBottomPt = null;
        if (row - 1 >= 0) {
            if (gridData[row - 1][col] != NULLVAL) {
                bottomPt = new Isopoint();
                bottomPt.setAll(row - 1, col, null);
            }
            if (col - 1 >= 0 && gridData[row - 1][col - 1] != NULLVAL) {
                leftBottomPt = new Isopoint();
                leftBottomPt.setAll(row - 1, col - 1, null);
            }
            if (col + 1 <= gridCols - 1 && gridData[row - 1][col + 1] != NULLVAL) {
                rightBottomPt = new Isopoint();
                rightBottomPt.setAll(row - 1, col + 1, null);
            }
        }
        if (row + 1 <= gridRows - 1) {
            if (gridData[row + 1][col] != NULLVAL) {
                topPt = new Isopoint();
                topPt.setAll(row + 1, col, null);
            }
            if (col - 1 >= 0 && gridData[row + 1][col - 1] != NULLVAL) {
                leftTopPt = new Isopoint();
                leftTopPt.setAll(row + 1, col - 1, null);
            }
            if (col + 1 <= gridCols - 1 && gridData[row + 1][col + 1] != NULLVAL) {
                rightTopPt = new Isopoint();
                rightTopPt.setAll(row + 1, col + 1, null);
            }
        }
        if (col - 1 >= 0 && gridData[row][col - 1] != NULLVAL) {
            leftPt = new Isopoint();
            leftPt.setAll(row, col - 1, null);
        }
        if (col + 1 <= gridCols - 1 && gridData[row][col + 1] != NULLVAL) {
            rightPt = new Isopoint();
            rightPt.setAll(row, col + 1, null);
        }

        List<Isopoint> nonNullPts = new ArrayList<Isopoint>();
        if (leftTopPt != null) {
            nonNullPts.add(leftTopPt);
        }
        if (rightTopPt != null) {
            nonNullPts.add(rightTopPt);
        }
        if (leftBottomPt != null) {
            nonNullPts.add(leftBottomPt);
        }
        if (rightBottomPt != null) {
            nonNullPts.add(rightBottomPt);
        }
        if (topPt != null) {
            nonNullPts.add(topPt);
        }
        if (leftPt != null) {
            nonNullPts.add(leftPt);
        }
        if (rightPt != null) {
            nonNullPts.add(rightPt);
        }
        if (bottomPt != null) {
            nonNullPts.add(bottomPt);
        }
        return nonNullPts;
    }

    //找下一个点
    private Point2D.Double getNextPt(List<Point2D.Double> ptsList, float sLevel, float eLevel, float curVal, Isopoint startPt) {
        Boolean isHorizon = curIsopoint.getIsHorizon();
        nextIsopoint = new Isopoint();
        int col = curIsopoint.getCol();
        int row = curIsopoint.getRow();
        if (col < 0 || col > gridCols - 1 || row < 0 || row > gridRows - 1) {
            if (logger.isDebugEnabled()) logger.debug("In IsolineProcess() getNextPt()当前点超出网格范围");
            return null;
        }

        boolean isFind = false;
        if (isHorizon == null) {//在顶点
            //八个方向，考虑大小、距离因素
            while (true && !isFind) {
                //1、八方向非空
                List<Isopoint> nonNullPts = get8DirectionPts(curIsopoint);

                //2、排除前一点preIsopoint
                List<Isopoint> nonPrePts = new ArrayList<Isopoint>();
                if (nonNullPts != null && !nonNullPts.isEmpty()) {
                    for (int i = 0; i < nonNullPts.size(); i++) {
                        if (!isPrePt(nonNullPts.get(i), preIsopoint)) {
                            nonPrePts.add(nonNullPts.get(i));
                        }
                    }
                } else {
                    break;
                }

                //3、排除不在之间的点
                List<Isopoint> betweenSELevelPts = new ArrayList<Isopoint>();
                if (nonPrePts != null && !nonPrePts.isEmpty()) {
                    for (int i = 0; i < nonPrePts.size(); i++) {
                        if (isBetweenTwoVals(nonPrePts.get(i), sLevel, eLevel)) {
                            betweenSELevelPts.add(nonPrePts.get(i));
                        }
                    }
                } else {
                    break;
                }


                //4、排除非边界
                List<Isopoint> firstEdgePts = new ArrayList<Isopoint>();
                List<Isopoint> secondEdgePts = new ArrayList<Isopoint>();
                if (betweenSELevelPts != null && !betweenSELevelPts.isEmpty()) {
                    for (int i = 0; i < betweenSELevelPts.size(); i++) {
                        if (isEdgePt(betweenSELevelPts.get(i))) {
                            firstEdgePts.add(betweenSELevelPts.get(i));
                        } else {
                            if (isSecondEdgePt(betweenSELevelPts.get(i))) {
                                secondEdgePts.add(betweenSELevelPts.get(i));
                            }
                        }
                    }
                } else {
                    firstEdgePts = betweenSELevelPts;
                    secondEdgePts = betweenSELevelPts;
                }

                //5、排除存在Map中的点
                List<Isopoint> nonInPtList1 = new ArrayList<Isopoint>();
                List<Isopoint> tmpNonInPtList1 = new ArrayList<Isopoint>();
                if (firstEdgePts != null && !firstEdgePts.isEmpty()) {
                    for (int i = 0; i < firstEdgePts.size(); i++) {
                        if (!isContainPt(ptsList, firstEdgePts.get(i))) {
                            nonInPtList1.add(firstEdgePts.get(i));
                            tmpNonInPtList1.add(firstEdgePts.get(i));
                        }
                    }
                } else {
                    nonInPtList1 = firstEdgePts;
                }

                List<Isopoint> nonInPtList2 = new ArrayList<Isopoint>();
                List<Isopoint> tmpNonInPtList2 = new ArrayList<Isopoint>();
                if (secondEdgePts != null && !secondEdgePts.isEmpty()) {
                    for (int i = 0; i < secondEdgePts.size(); i++) {
                        if (!isContainPt(ptsList, secondEdgePts.get(i))) {
                            nonInPtList2.add(secondEdgePts.get(i));
                            tmpNonInPtList2.add(secondEdgePts.get(i));
                        }
                    }
                } else {
                    secondEdgePts = nonInPtList2;
                }

                //6、去除联通点只有一个的点
                List<Isopoint> moreConnectPt1 = new ArrayList<Isopoint>();
                if (nonInPtList1 != null && !nonInPtList1.isEmpty()) {
                    for (int i = 0; i < nonInPtList1.size(); i++) {
                        if (!isTheOnlyEdge(nonInPtList1.get(i), sLevel, eLevel, null, false)) {
                            moreConnectPt1.add(nonInPtList1.get(i));
                        }
                    }
                } else {
                    moreConnectPt1 = nonInPtList1;
                }

                List<Isopoint> moreConnectPt2 = new ArrayList<Isopoint>();
                if (nonInPtList2 != null && !nonInPtList2.isEmpty()) {
                    for (int i = 0; i < nonInPtList2.size(); i++) {
                        if (!isTheOnlyEdge(nonInPtList2.get(i), sLevel, eLevel, null, false)) {
                            moreConnectPt2.add(nonInPtList2.get(i));
                        }
                    }
                } else {
                    moreConnectPt2 = nonInPtList2;
                }

                //7、找边界最多的点,去除已经被别的线走过的点
                List<Integer> index1 = new ArrayList<Integer>();
                List<Isopoint> mostEdgt1 = new ArrayList<Isopoint>();
                if (moreConnectPt1 != null && !moreConnectPt1.isEmpty() && moreConnectPt1.size() > 1) {
                    int edgtNum = 1;
                    for (int i = 0; i < moreConnectPt1.size(); i++) {
                        if (isAdd(moreConnectPt1.get(i))) {
                            continue;
                        }
                        int tmpNum = getEdgeNum(moreConnectPt1.get(i));
                        if (edgtNum < tmpNum) {
                            edgtNum = tmpNum;
                            if (index1 != null && !index1.isEmpty()) {
                                index1.clear();
                            }
                            index1.add(i);
                        } else if (edgtNum == tmpNum) {
                            edgtNum = tmpNum;
                            index1.add(i);
                        }
                    }
                    if (index1 != null && !index1.isEmpty()) {
                        for (int i = 0; i < index1.size(); i++) {
                            mostEdgt1.add(moreConnectPt1.get(index1.get(i)));
                        }
                    }
                }
                if (mostEdgt1 == null || mostEdgt1.isEmpty()) {
                    for (int i = 0; i < moreConnectPt1.size(); i++) {
                        if (isAdd(moreConnectPt1.get(i))) {
                            continue;
                        }
                        mostEdgt1.add(moreConnectPt1.get(i));
                    }
                }

                List<Integer> index2 = new ArrayList<Integer>();
                List<Isopoint> mostEdgt2 = new ArrayList<Isopoint>();
                if (moreConnectPt2 != null && !moreConnectPt2.isEmpty() && moreConnectPt2.size() > 1) {
                    int edgtNum = 1;
                    for (int i = 0; i < moreConnectPt2.size(); i++) {
                        if (isAdd(moreConnectPt2.get(i))) {
                            continue;
                        }
                        int tmpNum = getEdgeNum(moreConnectPt2.get(i));
                        if (edgtNum < tmpNum) {
                            edgtNum = tmpNum;
                            if (index2 != null && !index2.isEmpty()) {
                                index2.clear();
                            }
                            index2.add(i);
                        } else if (edgtNum == tmpNum) {
                            edgtNum = tmpNum;
                            index2.add(i);
                        }
                    }
                    if (index2 != null && !index2.isEmpty()) {
                        for (int i = 0; i < index2.size(); i++) {
                            mostEdgt2.add(moreConnectPt2.get(index2.get(i)));
                        }
                    }
                }
                if (mostEdgt2 == null || mostEdgt2.isEmpty()) {
                    for (int i = 0; i < moreConnectPt2.size(); i++) {
                        if (isAdd(moreConnectPt2.get(i))) {
                            continue;
                        }
                        mostEdgt2.add(moreConnectPt2.get(i));
                    }
                }


                //8、找上下左右联通点最多的点，若上下左右和相等找最大最小值
                List<Integer> indexList = new ArrayList<Integer>();
                List<Isopoint> mostConnectPt1 = new ArrayList<Isopoint>();
                if (mostEdgt1 != null && !mostEdgt1.isEmpty() && mostEdgt1.size() > 1) {
                    int borderNum = 0;
                    for (int i = 0; i < mostEdgt1.size(); i++) {
                        int tmpNum = getBorderNum(mostEdgt1.get(i));
                        if (tmpNum > borderNum) {
                            borderNum = tmpNum;
                            if (indexList != null && !indexList.isEmpty()) {
                                indexList.clear();
                            }
                            indexList.add(i);
                        } else if (tmpNum == borderNum) {
                            borderNum = tmpNum;
                            indexList.add(i);
                        }
                    }
                    if (indexList != null && !indexList.isEmpty()) {
                        for (int i = 0; i < indexList.size(); i++) {
                            mostConnectPt1.add(mostEdgt1.get(indexList.get(i)));
                        }
                    }
                }

                if (mostConnectPt1 == null || mostConnectPt1.isEmpty()) {
                    mostConnectPt1 = mostEdgt1;
                }

                indexList = new ArrayList<Integer>();
                List<Isopoint> mostConnectPt2 = new ArrayList<Isopoint>();
                if (mostEdgt2 != null && !mostEdgt2.isEmpty() && mostEdgt2.size() > 1) {
                    int cornerNum = 0;
                    for (int i = 0; i < mostEdgt2.size(); i++) {
                        int tmpNum = getCornerNum(mostEdgt2.get(i));
                        if (tmpNum > cornerNum) {
                            cornerNum = tmpNum;
                            if (indexList != null && !indexList.isEmpty()) {
                                indexList.clear();
                            }
                            indexList.add(i);
                        } else if (tmpNum == cornerNum) {
                            cornerNum = tmpNum;
                            indexList.add(i);
                        }
                    }
                    if (indexList != null && !indexList.isEmpty()) {
                        for (int i = 0; i < indexList.size(); i++) {
                            mostConnectPt2.add(mostEdgt2.get(indexList.get(i)));
                        }
                    }
                }

                if (mostConnectPt2 == null || mostConnectPt2.isEmpty()) {
                    mostConnectPt2 = mostEdgt2;
                }

                float minVal = Math.min(sLevel, eLevel);
                float maxVal = Math.max(sLevel, eLevel);

                //11、找距离最近的那一个
                indexList = new ArrayList<Integer>();
                List<Isopoint> nearstPt1 = new ArrayList<Isopoint>();
                if (mostConnectPt1 != null && !mostConnectPt1.isEmpty() && mostConnectPt1.size() > 1) {
                    float dis = getDistance(curIsopoint, mostConnectPt1.get(0));
                    for (int i = 0; i < mostConnectPt1.size(); i++) {
                        float val = getDistance(curIsopoint, mostConnectPt1.get(i));
                        if (val != -1 && dis > val) {
                            dis = val;
                            if (indexList != null && !indexList.isEmpty()) {
                                indexList.clear();
                            }
                            indexList.add(i);
                        } else if (val == dis) {
                            dis = val;
                            indexList.add(i);
                        }
                    }
                    for (int i = 0; i < indexList.size(); i++) {
                        nearstPt1.add(mostConnectPt1.get(indexList.get(i)));
                    }
                }
                if (nearstPt1 == null || nearstPt1.isEmpty()) {
                    nearstPt1 = mostConnectPt1;
                }

                indexList = new ArrayList<Integer>();
                List<Isopoint> nearstPt2 = new ArrayList<Isopoint>();
                if (mostConnectPt2 != null && !mostConnectPt2.isEmpty() && mostConnectPt2.size() > 1) {
                    float dis = getDistance(curIsopoint, mostConnectPt2.get(0));
                    for (int i = 0; i < mostConnectPt2.size(); i++) {
                        float val = getDistance(curIsopoint, mostConnectPt2.get(i));
                        if (val != -1 && dis > val) {
                            dis = val;
                            if (indexList != null && !indexList.isEmpty()) {
                                indexList.clear();
                            }
                            indexList.add(i);
                        } else if (val == dis) {
                            dis = val;
                            indexList.add(i);
                        }
                    }
                    for (int i = 0; i < indexList.size(); i++) {
                        nearstPt2.add(mostConnectPt2.get(indexList.get(i)));
                    }
                }
                if (nearstPt2 == null || nearstPt2.isEmpty()) {
                    nearstPt2 = mostConnectPt2;
                }

                //9、找最大或最小的那个点
                indexList = new ArrayList<Integer>();//防止存在一样大的值
                List<Isopoint> biggerPt1 = new ArrayList<Isopoint>();
                if (nearstPt1 != null && !nearstPt1.isEmpty() && nearstPt1.size() > 1) {
                    float curL = curVal;
                    for (int i = 0; i < nearstPt1.size(); i++) {
                        float val = getIsopointVal(nearstPt1.get(i));
                        if (curVal == minVal) {
                            if (val != NULLVAL && curL < val) {
                                curL = val;
                                if (indexList != null && !indexList.isEmpty()) {
                                    indexList.clear();
                                }
                                indexList.add(i);
                            } else if (val == curL) {
                                curL = val;
                                indexList.add(i);
                            }
                        } else if (curVal == maxVal) {
                            if (val != NULLVAL && curL > val) {
                                curL = val;
                                if (indexList != null && !indexList.isEmpty()) {
                                    indexList.clear();
                                }
                                indexList.add(i);
                            } else if (val == curL) {
                                curL = val;
                                indexList.add(i);
                            }
                        } else {
                            if (logger.isDebugEnabled()) logger.debug("In HYIsolineProcess.getNextPt()找最大最小值出错");
                            break;
                        }
                    }
                    for (int i = 0; i < indexList.size(); i++) {
                        biggerPt1.add(nearstPt1.get(indexList.get(i)));
                    }
                }
                if (biggerPt1 == null || biggerPt1.isEmpty()) {
                    biggerPt1 = nearstPt1;
                }

                indexList = new ArrayList<Integer>();
                List<Isopoint> biggerPt2 = new ArrayList<Isopoint>();
                if (nearstPt2 != null && !nearstPt2.isEmpty() && nearstPt2.size() > 1) {
                    float curL = curVal;
                    for (int i = 0; i < nearstPt2.size(); i++) {
                        float val = getIsopointVal(nearstPt2.get(i));
                        if (curVal == minVal) {
                            if (val != NULLVAL && curL < val) {
                                curL = val;
                                if (indexList != null && !indexList.isEmpty()) {
                                    indexList.clear();
                                }
                                indexList.add(i);
                            } else if (val == curL) {
                                curL = val;
                                indexList.add(i);
                            }
                        } else if (curVal == maxVal) {
                            if (val != NULLVAL && curL > val) {
                                curL = val;
                                if (indexList != null && !indexList.isEmpty()) {
                                    indexList.clear();
                                }
                                indexList.add(i);
                            } else if (val == curL) {
                                curL = val;
                                indexList.add(i);
                            }
                        } else {
                            if (logger.isDebugEnabled()) logger.debug("In HYIsolineProcess.getNextPt()找最大最小值出错");
                            break;
                        }
                    }
                    for (int i = 0; i < indexList.size(); i++) {
                        biggerPt2.add(nearstPt2.get(indexList.get(i)));
                    }
                }

                if (biggerPt2 == null || biggerPt2.isEmpty()) {
                    biggerPt2 = nearstPt2;
                }

                //10、若找到则nextPt =
                if (biggerPt1 != null && !biggerPt1.isEmpty()) {
                    nextIsopoint = new Isopoint();
                    //找与prePoint点离的最远的那一点
                    if (biggerPt1.size() == 1) {
                        nextIsopoint = biggerPt1.get(0);
                    } else {
                        nextIsopoint = getFurtherPt(biggerPt1, preIsopoint);
                    }
                    isFind = true;
                } else if (biggerPt2 != null && !biggerPt2.isEmpty()) {
                    nextIsopoint = new Isopoint();
                    //找与prePoint点离的最远的那一点
                    if (biggerPt2.size() == 1) {
                        nextIsopoint = biggerPt2.get(0);
                    } else {
                        nextIsopoint = getFurtherPt(biggerPt2, preIsopoint);
                    }
                    isFind = true;
                } else if (biggerPt1 == null || biggerPt2 == null || biggerPt1.isEmpty() || biggerPt2.isEmpty()) {
                    if (firstEdgePts != null && firstEdgePts.size() == 1 && (tmpNonInPtList1 == null || tmpNonInPtList1.isEmpty())
                            && isTheSameIsopt(firstEdgePts.get(0), startPt)) {
                        nextIsopoint.setAll(startPt.getRow(), startPt.getCol(), null);
                    } else if (secondEdgePts != null && secondEdgePts.size() == 1 && (tmpNonInPtList2 == null || tmpNonInPtList2.isEmpty())
                            && isTheSameIsopt(secondEdgePts.get(0), startPt)) {
                        nextIsopoint.setAll(startPt.getRow(), startPt.getCol(), null);
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug("不存在符合条件的点,需要前推");
                        nextIsopoint = null;
                    }
                    break;
                }
                break;
            }
        }
        if (nextIsopoint != null) {
            return getPos(nextIsopoint.getRow(), nextIsopoint.getCol());
        } else {
            return null;
        }
    }

    //是不是同一个点
    private boolean isTheSameIsopt(Isopoint pt1, Isopoint pt2) {
        int row1 = pt1.getRow();
        int col1 = pt1.getCol();
        Boolean isH1 = pt1.getIsHorizon();
        int row2 = pt2.getRow();
        int col2 = pt2.getCol();
        Boolean isH2 = pt2.getIsHorizon();

        if (row1 == row2 && col1 == col2 && isH1 == isH2) {
            return true;
        }
        return false;
    }

    //获取等值线
    private List<Point2D.Double> getIsopointOnline(IsolineDataProc isoline, Boolean isHorizon, List<IsolineDataProc> allIsolineList) {
        if (allIsolineList == null || allIsolineList.isEmpty()) {
            throw new RuntimeException("In IsolineProcess.getIsopointOnline()中allIsolineList为空");
        }

        int curRow = curIsopoint.getRow();
        int curCol = curIsopoint.getCol();
        Boolean curH = curIsopoint.getIsHorizon();

        int preRow = preIsopoint.getRow();
        int preCol = preIsopoint.getCol();
        Boolean preH = preIsopoint.getIsHorizon();

        onlineIndex = -1;
        for (int i = 0; i < allIsolineList.size(); i++) {
            IsolineDataProc tmpIsoline = allIsolineList.get(i);
            if (tmpIsoline.indexProc == isoline.indexProc || tmpIsoline.val == isoline.val) {
                continue;
            }
            if (tmpIsoline.isClosed) {
                continue;
            }
            if (backFillLineIndexList != null && !backFillLineIndexList.isEmpty()) {
                boolean isFind = false;
                for (int j = 0; j < backFillLineIndexList.size(); j++) {
                    if (backFillLineIndexList.get(j) == tmpIsoline.indexProc) {
                        isFind = true;
                        break;
                    }
                }
                if (isFind) {
                    continue;
                }
            }
            //处理当填充到lastIndex等值线时能否连通闭合
            if (!canConnect(tmpIsoline)) {
                continue;
            }

            List<Integer> rowList = tmpIsoline.rowsList;
            List<Integer> colList = tmpIsoline.colsList;
            List<Boolean> startHList = tmpIsoline.isHorizonList;
            int startR = rowList.get(0);
            int startC = colList.get(0);
            Boolean startH = startHList.get(0);
            int size = rowList.size();
            int endR = rowList.get(size - 1);
            int endC = colList.get(size - 1);
            Boolean endH = startHList.get(size - 1);
            if (startR == curRow && startC == curCol && startH == curH && startR == preRow && startC == preCol && startH == preH && startH == isHorizon) {
                nextIsoptOnLine = new Isopoint();
                nextIsoptOnLine.setAll(endR, endC, endH);
                fillLineDirectionList.add(1);
                fillLineIndexList.add(tmpIsoline.indexProc);
                fillLineValList.add(tmpIsoline.val);
                onlineIndex = 0;
                return tmpIsoline.lineList2D;
            } else if (endR == curRow && endC == curCol && endH == isHorizon && endR == preRow && endC == preCol && endH == preH && endH == isHorizon) {
                List<Point2D.Double> ptList = new ArrayList<Point2D.Double>();
                for (int j = tmpIsoline.num - 1; j >= 0; j--) {
                    ptList.add(tmpIsoline.lineList2D.get(j));
                }
                nextIsoptOnLine = new Isopoint();
                nextIsoptOnLine.setAll(startR, startC, startH);
                fillLineDirectionList.add(-1);
                fillLineIndexList.add(tmpIsoline.indexProc);
                fillLineValList.add(tmpIsoline.val);
                onlineIndex = tmpIsoline.num - 1;
                return ptList;
            } else {
                continue;
            }
        }
        return null;
    }

    private Isopoint getAllNonNullBetweenPts(Isopoint pt, float curLevel, float anotherLevel, List<Point2D.Double> ptsList, List<IsolineDataProc> allIsolineList) {
        //1八方向
        List<Isopoint> nonNullPts = get8DirectionPts(pt);
        //2、排除不在之间的点
        List<Isopoint> betweenSELevelPts = new ArrayList<Isopoint>();
        if (nonNullPts != null && !nonNullPts.isEmpty()) {
            for (int i = 0; i < nonNullPts.size(); i++) {
                if (isBetweenTwoVals(nonNullPts.get(i), curLevel, anotherLevel)) {
                    betweenSELevelPts.add(nonNullPts.get(i));
                }
            }
        }

        //3、排除非边界
        List<Isopoint> firstEdgePts = new ArrayList<Isopoint>();
        List<Isopoint> secondEdgePts = new ArrayList<Isopoint>();
        if (betweenSELevelPts != null && !betweenSELevelPts.isEmpty()) {
            for (int i = 0; i < betweenSELevelPts.size(); i++) {
                if (isEdgePt(betweenSELevelPts.get(i))) {
                    firstEdgePts.add(betweenSELevelPts.get(i));
                } else {
                    if (isSecondEdgePt(betweenSELevelPts.get(i))) {
                        secondEdgePts.add(betweenSELevelPts.get(i));
                    }
                }
            }
        }

        //4、排除存在Map中的点
        List<Isopoint> nonInPtList1 = new ArrayList<Isopoint>();
        if (firstEdgePts != null && !firstEdgePts.isEmpty()) {
            for (int i = 0; i < firstEdgePts.size(); i++) {
                if (!isContainPt(ptsList, firstEdgePts.get(i))) {
                    nonInPtList1.add(firstEdgePts.get(i));
                }
            }
        }

        List<Isopoint> nonInPtList2 = new ArrayList<Isopoint>();
        if (secondEdgePts != null && !secondEdgePts.isEmpty()) {
            for (int i = 0; i < secondEdgePts.size(); i++) {
                if (!isContainPt(ptsList, secondEdgePts.get(i))) {
                    nonInPtList2.add(secondEdgePts.get(i));
                }
            }
        }

        //5、找边界最多的点,去除已经被别的线走过的点
        List<Integer> index1 = new ArrayList<Integer>();
        List<Isopoint> mostEdgt1 = new ArrayList<Isopoint>();
        if (nonInPtList1 != null && !nonInPtList1.isEmpty() && nonInPtList1.size() > 1) {
            int edgtNum = 1;
            for (int i = 0; i < nonInPtList1.size(); i++) {
                if (isAdd(nonInPtList1.get(i))) {
                    continue;
                }
                int tmpNum = getEdgeNum(nonInPtList1.get(i));
                if (edgtNum < tmpNum) {
                    edgtNum = tmpNum;
                    if (index1 != null && !index1.isEmpty()) {
                        index1.clear();
                    }
                    index1.add(i);
                } else if (edgtNum == tmpNum) {
                    edgtNum = tmpNum;
                    index1.add(i);
                }
            }
            if (index1 != null && !index1.isEmpty()) {
                for (int i = 0; i < index1.size(); i++) {
                    mostEdgt1.add(nonInPtList1.get(index1.get(i)));
                }
            }
        }
        if (mostEdgt1 == null || mostEdgt1.isEmpty()) {
            for (int i = 0; i < nonInPtList1.size(); i++) {
                if (isAdd(nonInPtList1.get(i))) {
                    continue;
                }
                mostEdgt1.add(nonInPtList1.get(i));
            }
        }

        List<Integer> index2 = new ArrayList<Integer>();
        List<Isopoint> mostEdgt2 = new ArrayList<Isopoint>();
        if (nonInPtList2 != null && !nonInPtList2.isEmpty() && nonInPtList2.size() > 1) {
            int edgtNum = 1;
            for (int i = 0; i < nonInPtList2.size(); i++) {
                if (isAdd(nonInPtList2.get(i))) {
                    continue;
                }
                int tmpNum = getEdgeNum(nonInPtList2.get(i));
                if (edgtNum < tmpNum) {
                    edgtNum = tmpNum;
                    if (index2 != null && !index2.isEmpty()) {
                        index2.clear();
                    }
                    index2.add(i);
                } else if (edgtNum == tmpNum) {
                    edgtNum = tmpNum;
                    index2.add(i);
                }
            }
            if (index2 != null && !index2.isEmpty()) {
                for (int i = 0; i < index2.size(); i++) {
                    mostEdgt2.add(nonInPtList2.get(index2.get(i)));
                }
            }
        }
        if (mostEdgt2 == null || mostEdgt2.isEmpty()) {
            for (int i = 0; i < nonInPtList2.size(); i++) {
                if (isAdd(nonInPtList2.get(i))) {
                    continue;
                }
                mostEdgt2.add(nonInPtList2.get(i));
            }
        }


        //6、找存在等值点的那些点。邻近等值线处的判断
        List<Isopoint> hasIsopointNonEnd1 = new ArrayList<Isopoint>();
        if (mostEdgt1 != null && !mostEdgt1.isEmpty() && mostEdgt1.size() > 1) {
            for (int i = 0; i < mostEdgt1.size(); i++) {
                boolean isHasIsopoint = isHasIsopointNonEnd(mostEdgt1.get(i), curLevel, anotherLevel, allIsolineList);
                if (isHasIsopoint) {
                    hasIsopointNonEnd1.add(mostEdgt1.get(i));
                }
            }
        }

        if (hasIsopointNonEnd1 == null || hasIsopointNonEnd1.isEmpty()) {
            hasIsopointNonEnd1 = mostEdgt1;
        }

        List<Isopoint> hasIsopointNonEnd2 = new ArrayList<Isopoint>();
        if (mostEdgt2 != null && !mostEdgt2.isEmpty() && mostEdgt2.size() > 1) {
            for (int i = 0; i < mostEdgt2.size(); i++) {
                boolean isHasIsopoint = isHasIsopointNonEnd(mostEdgt2.get(i), curLevel, anotherLevel, allIsolineList);
                if (isHasIsopoint) {
                    hasIsopointNonEnd2.add(mostEdgt2.get(i));
                }
            }
        }

        if (hasIsopointNonEnd2 == null || hasIsopointNonEnd2.isEmpty()) {
            hasIsopointNonEnd2 = mostEdgt2;
        }

        //7、找上下左右联通点最多的点，若上下左右和相等找最大最小值
        List<Integer> indexList = new ArrayList<Integer>();
        List<Isopoint> mostConnectPt1 = new ArrayList<Isopoint>();
        if (hasIsopointNonEnd1 != null && !hasIsopointNonEnd1.isEmpty() && hasIsopointNonEnd1.size() > 1) {
            int borderNum = 0;
            for (int i = 0; i < hasIsopointNonEnd1.size(); i++) {
                int tmpNum = getBorderNum(hasIsopointNonEnd1.get(i));
                if (tmpNum > borderNum) {
                    borderNum = tmpNum;
                    if (indexList != null && !indexList.isEmpty()) {
                        indexList.clear();
                    }
                    indexList.add(i);
                } else if (tmpNum == borderNum) {
                    borderNum = tmpNum;
                    indexList.add(i);
                }
            }
            if (indexList != null && !indexList.isEmpty()) {
                for (int i = 0; i < indexList.size(); i++) {
                    mostConnectPt1.add(hasIsopointNonEnd1.get(indexList.get(i)));
                }
            }
        }

        if (mostConnectPt1 == null || mostConnectPt1.isEmpty()) {
            mostConnectPt1 = hasIsopointNonEnd1;
        }

        indexList = new ArrayList<Integer>();
        List<Isopoint> mostConnectPt2 = new ArrayList<Isopoint>();
        if (hasIsopointNonEnd2 != null && !hasIsopointNonEnd2.isEmpty() && hasIsopointNonEnd2.size() > 1) {
            int cornerNum = 0;
            for (int i = 0; i < hasIsopointNonEnd2.size(); i++) {
                int tmpNum = getCornerNum(hasIsopointNonEnd2.get(i));
                if (tmpNum > cornerNum) {
                    cornerNum = tmpNum;
                    if (indexList != null && !indexList.isEmpty()) {
                        indexList.clear();
                    }
                    indexList.add(i);
                } else if (tmpNum == cornerNum) {
                    cornerNum = tmpNum;
                    indexList.add(i);
                }
            }
            if (indexList != null && !indexList.isEmpty()) {
                for (int i = 0; i < indexList.size(); i++) {
                    mostConnectPt2.add(hasIsopointNonEnd2.get(indexList.get(i)));
                }
            }
        }

        if (mostConnectPt2 == null || mostConnectPt2.isEmpty()) {
            mostConnectPt2 = hasIsopointNonEnd2;
        }


        indexList = new ArrayList<Integer>();//防止存在一样大的值
        List<Isopoint> biggerPt1 = new ArrayList<Isopoint>();
        if (mostConnectPt1 != null && !mostConnectPt1.isEmpty() && mostConnectPt1.size() > 1) {
            float curL = curLevel;
            for (int i = 0; i < mostConnectPt1.size(); i++) {
                float val = getIsopointVal(mostConnectPt1.get(i));
                if (curLevel < anotherLevel) {
                    if (val != NULLVAL && curL < val) {
                        curL = val;
                        if (indexList != null && !indexList.isEmpty()) {
                            indexList.clear();
                        }
                        indexList.add(i);
                    } else if (val == curL) {
                        curL = val;
                        indexList.add(i);
                    }
                } else {
                    if (val != NULLVAL && curL > val) {
                        curL = val;
                        if (indexList != null && !indexList.isEmpty()) {
                            indexList.clear();
                        }
                        indexList.add(i);
                    } else if (val == curL) {
                        curL = val;
                        indexList.add(i);
                    }
                }
            }
            for (int i = 0; i < indexList.size(); i++) {
                biggerPt1.add(mostConnectPt1.get(indexList.get(i)));
            }
        }
        if (biggerPt1 == null || biggerPt1.isEmpty()) {
            biggerPt1 = mostConnectPt1;
        }

        indexList = new ArrayList<Integer>();
        List<Isopoint> biggerPt2 = new ArrayList<Isopoint>();
        if (mostConnectPt2 != null && !mostConnectPt2.isEmpty() && mostConnectPt2.size() > 1) {
            float curL = curLevel;
            for (int i = 0; i < mostConnectPt2.size(); i++) {
                float val = getIsopointVal(mostConnectPt2.get(i));
                if (curLevel < anotherLevel) {
                    if (val != NULLVAL && curL < val) {
                        curL = val;
                        if (indexList != null && !indexList.isEmpty()) {
                            indexList.clear();
                        }
                        indexList.add(i);
                    } else if (val == curL) {
                        curL = val;
                        indexList.add(i);
                    }
                } else {
                    if (val != NULLVAL && curL > val) {
                        curL = val;
                        if (indexList != null && !indexList.isEmpty()) {
                            indexList.clear();
                        }
                        indexList.add(i);
                    } else if (val == curL) {
                        curL = val;
                        indexList.add(i);
                    }
                }
            }
            for (int i = 0; i < indexList.size(); i++) {
                biggerPt2.add(mostConnectPt2.get(indexList.get(i)));
            }
        }

        if (biggerPt2 == null || biggerPt2.isEmpty()) {
            biggerPt2 = nonInPtList2;
        }

        if ((biggerPt1 == null || biggerPt1.isEmpty()) && (biggerPt2 == null || biggerPt2.isEmpty())) {
            return null;
        }

        if (biggerPt1 != null && !biggerPt1.isEmpty() && biggerPt1.size() == 1) {
            return biggerPt1.get(0);
        }

        if (biggerPt2 != null && !biggerPt1.isEmpty() && biggerPt1.size() == 1) {
            return biggerPt2.get(0);
        }
        return null;
    }

    /**
     * 等值线的起始和结束是否为同一点
     *
     * @param isoline
     * @param val1
     * @param val2
     * @return
     */
    private boolean isSESamePt(IsolineDataProc isoline, float val1, float val2) {
        Boolean sH = isoline.isHorizonList.get(0);
        int sRow = isoline.rowsList.get(0);
        int sCol = isoline.colsList.get(0);

        int num = isoline.num;
        Boolean eH = isoline.isHorizonList.get(num - 1);
        int eRow = isoline.rowsList.get(num - 1);
        int eCol = isoline.colsList.get(num - 1);

        float[][] gridData = gridDataList.get(0).getGridData();

        Isopoint pt1 = new Isopoint();
        Isopoint pt2 = new Isopoint();
        float tmpVal1 = gridData[sRow][sCol];
        float tmpVal2 = gridData[eRow][eCol];
        if (sH != null) {
            if (sH) {//在横边
                if (isBetweenTwoVals(gridData[sRow][sCol], val1, val2)) {
                    pt1.setAll(sRow, sCol, null);
                } else if (isBetweenTwoVals(gridData[sRow][sCol + 1], val1, val2)) {
                    pt1.setAll(sRow, sCol + 1, null);
                    tmpVal1 = gridData[sRow][sCol + 1];
                } else {
                    pt1 = null;
                }
            } else {//在纵边
                if (isBetweenTwoVals(gridData[sRow][sCol], val1, val2)) {
                    pt1.setAll(sRow, sCol, null);
                } else if (isBetweenTwoVals(gridData[sRow + 1][sCol], val1, val2)) {
                    pt1.setAll(sRow + 1, sCol, null);
                    tmpVal1 = gridData[sRow + 1][sCol];
                } else {
                    pt1 = null;
                }
            }
        }

        if (eH != null) {
            if (eH) {//在横边
                if (isBetweenTwoVals(gridData[eRow][eCol], val1, val2)) {
                    pt2.setAll(eRow, eCol, null);
                } else if (isBetweenTwoVals(gridData[eRow][eCol + 1], val1, val2)) {
                    pt2.setAll(eRow, eCol + 1, null);
                    tmpVal2 = gridData[eRow][eCol + 1];
                } else {
                    pt2 = null;
                }
            } else {//在纵边
                if (isBetweenTwoVals(gridData[eRow][eCol], val1, val2)) {
                    pt2.setAll(eRow, eCol, null);
                } else if (isBetweenTwoVals(gridData[eRow + 1][eCol], val1, val2)) {
                    pt2.setAll(eRow + 1, eCol, null);
                    tmpVal2 = gridData[eRow + 1][eCol];
                } else {
                    pt2 = null;
                }
            }
        }

        if (pt1 != null && pt2 != null) {
            if (pt1.getRow() == pt2.getRow() && pt1.getCol() == pt2.getCol() && tmpVal1 == tmpVal2) {
                return true;
            }
        }
        return false;
    }

    private List<Isopoint> get4DirectionPts(Isopoint curPt) {
        int row = curPt.getRow();
        int col = curPt.getCol();
        float[][] gridData = gridDataList.get(0).getGridData();

        //八个方向，考虑大小、距离因素
        Isopoint topPt = null;
        Isopoint leftPt = null;
        Isopoint rightPt = null;
        Isopoint bottomPt = null;

        if (row - 1 >= 0 && gridData[row - 1][col] != NULLVAL) {
            bottomPt = new Isopoint();
            bottomPt.setAll(row - 1, col, null);
        }
        if (row + 1 <= gridRows - 1 && gridData[row + 1][col] != NULLVAL) {
            topPt = new Isopoint();
            topPt.setAll(row + 1, col, null);
        }
        if (col - 1 >= 0 && gridData[row][col - 1] != NULLVAL) {
            leftPt = new Isopoint();
            leftPt.setAll(row, col - 1, null);
        }
        if (col + 1 <= gridCols - 1 && gridData[row][col + 1] != NULLVAL) {
            rightPt = new Isopoint();
            rightPt.setAll(row, col + 1, null);
        }

        List<Isopoint> nonNullPts = new ArrayList<Isopoint>();
        if (topPt != null) {
            nonNullPts.add(topPt);
        }
        if (leftPt != null) {
            nonNullPts.add(leftPt);
        }
        if (rightPt != null) {
            nonNullPts.add(rightPt);
        }
        if (bottomPt != null) {
            nonNullPts.add(bottomPt);
        }
        return nonNullPts;
    }

    private boolean hasAnotherLine(Isopoint curPt, List<IsolineDataProc> allIsolineList) {
        List<Isopoint> nonNullPts = get4DirectionPts(curPt);
        int preR = preIsopoint.getRow();
        int preC = preIsopoint.getCol();
        Boolean preH = preIsopoint.getIsHorizon();

        int curR = curPt.getRow();
        int curC = curPt.getCol();

        Boolean isH = null;
        int r = curR;
        int c = curC;
        for (int j = 0; j < nonNullPts.size(); j++) {
            Isopoint tmpPt = nonNullPts.get(j);
            int tmpR = tmpPt.getRow();
            int tmpC = tmpPt.getCol();

            if (tmpR == curR && Math.abs(tmpC - curC) == 1) {
                isH = true;
                c = Math.min(tmpC, curC);
                r = curR;
            } else if (tmpC == curC && Math.abs(tmpR - curR) == 1) {
                isH = false;
                c = curC;
                r = Math.min(tmpR, curR);
            } else {
                continue;
            }
            for (int i = 0; i < allIsolineList.size(); i++) {
                IsolineDataProc tmpIsoline = allIsolineList.get(i);
                if (tmpIsoline.isClosed) {
                    continue;
                }
                if (backFillLineIndexList != null && !backFillLineIndexList.isEmpty()) {
                    boolean isFind = false;
                    for (int k = 0; k < backFillLineIndexList.size(); k++) {
                        if (backFillLineIndexList.get(k) == tmpIsoline.indexProc) {
                            isFind = true;
                            break;
                        }
                    }
                    if (isFind) {
                        continue;
                    }
                }
                //处理当填充到lastIndex等值线时能否连通闭合
                if (!canConnect(tmpIsoline)) {
                    continue;
                }

                List<Integer> rowList = tmpIsoline.rowsList;
                List<Integer> colList = tmpIsoline.colsList;
                List<Boolean> startHList = tmpIsoline.isHorizonList;
                int startR = rowList.get(0);
                int startC = colList.get(0);
                Boolean startH = startHList.get(0);
                int size = rowList.size();
                int endR = rowList.get(size - 1);
                int endC = colList.get(size - 1);
                Boolean endH = startHList.get(size - 1);
                if ((startR == r && startC == c && startH == isH) && !(startR == preR && startC == preC && startH == preH)) {
                    return true;
                } else if ((endR == r && endC == c && endH == isH) && !(endR == preR && endC == preC && endH == preH)) {
                    return true;
                } else {
                    continue;
                }
            }
        }
        return false;
    }

    private List<Point2D.Double> getLineFromDIV(int index, int direction, List<IsolineDataProc> allIsolineList) {
        if (allIsolineList == null || allIsolineList.isEmpty()) {
            return null;
        }
        for (int i = 0; i < allIsolineList.size(); i++) {
            IsolineDataProc isoline = allIsolineList.get(i);
            if (isoline.indexProc != index) {
                continue;
            }
            if (direction == 1) {
                return isoline.lineList2D;
            } else if (direction == -1) {
                List<Point2D.Double> ptList = new ArrayList<Point2D.Double>();
                for (int j = isoline.num - 1; j >= 0; j--) {
                    ptList.add(isoline.lineList2D.get(j));
                }
                return ptList;
            } else {
                if (logger.isDebugEnabled())
                    logger.debug("In IsolineProcess.getLineFromDIV()输入的方向不存在");
                return null;
            }
        }
        return null;
    }

    private List<Point2D.Double> getNextPts(IsolineDataProc isoline, List<Point2D.Double> ptsList, float otherLevel, List<IsolineDataProc> allIsolineList, boolean isRecall) {
//	private List<Point2D.Double> getNextPt(IsolineDataProc isoline, boolean isBigger, List<Point2D.Double> ptsList, float level, List<IsolineDataProc> allIsolineList){
        List<Point2D.Double> resPtsList = new ArrayList<Point2D.Double>();

        Boolean isHorizon = curIsopoint.getIsHorizon();
        nextIsopoint = new Isopoint();
        int col = curIsopoint.getCol();
        int row = curIsopoint.getRow();
        if (col < 0 || col > gridCols - 1 || row < 0 || row > gridRows - 1) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess() getNextPts()当前点超出网格范围");
            return null;
        }
        float[][] gridData = gridDataList.get(0).getGridData();
        if (preIsopoint.equals(curIsopoint)) {//起始点
            if (isHorizon != null) {//非顶点
                Isopoint tmpPt1 = new Isopoint();
                Isopoint tmpPt2 = new Isopoint();
//				nextIsopoint = new Isopoint();
                if (isHorizon) {//在横边
                    if (isBetweenTwoVals(gridData[row][col], isoline.val, otherLevel)) {
                        nextIsopoint.setAll(row, col, null);
                    } else if (isBetweenTwoVals(gridData[row][col + 1], isoline.val, otherLevel)) {
                        nextIsopoint.setAll(row, col + 1, null);
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug("左右两点均不满足条件,找其它点或线");
                        nextIsopoint = null;
//						return null;
                    }
                } else {//在纵边
                    if (isBetweenTwoVals(gridData[row][col], isoline.val, otherLevel)) {
                        nextIsopoint.setAll(row, col, null);
                    } else if (isBetweenTwoVals(gridData[row + 1][col], isoline.val, otherLevel)) {
                        nextIsopoint.setAll(row + 1, col, null);
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug("上下两点均不满足条件,找其它点或线");
                        nextIsopoint = null;
//						return null;
                    }
                }

                //nextIsopoint为空 考虑四个角上的值
                //考虑导致nextIsopoint为空的原因是在同一边上同时存在两条等值线
                if (nextIsopoint == null) {
                    List<Point2D.Double> tmpPts = getIsopointOnline(isoline, isHorizon, allIsolineList);
                    if (tmpPts == null) {
                        tmpPt1.setAll(row, col, null);
                        Isopoint allNonNullBetweenPts1 = getAllNonNullBetweenPts(tmpPt1, isoline.val, otherLevel, ptsList, allIsolineList);
                        if (allNonNullBetweenPts1 != null) {
                            nextIsopoint = new Isopoint();
                            nextIsopoint.setAll(allNonNullBetweenPts1.getRow(), allNonNullBetweenPts1.getCol(), allNonNullBetweenPts1.getIsHorizon());
                        } else {
                            if (isHorizon) {
                                tmpPt2.setAll(row, col + 1, null);
                            } else {
                                tmpPt2.setAll(row + 1, col, null);
                            }
                            Isopoint allNonNullBetweenPts2 = getAllNonNullBetweenPts(tmpPt2, isoline.val, otherLevel, ptsList, allIsolineList);
                            if (allNonNullBetweenPts2 != null) {
                                nextIsopoint = new Isopoint();
                                nextIsopoint.setAll(allNonNullBetweenPts2.getRow(), allNonNullBetweenPts2.getCol(), allNonNullBetweenPts2.getIsHorizon());
                            }
                        }
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug("找到同一边的线");
                        return tmpPts;
                    }
                }

                if (nextIsopoint != null && isContainPt(nextIsopoint)) {
                    if (procIsolineDataList.get(fillLineIndexList.get(fillLineIndexList.size() - 1)).num == 2
                            && fillLineIndexList.size() != 1) {

                        //当为中间的联通等值线，且只有两个点时，处理
                        tmpPt1.setAll(nextIsopoint.getRow(), nextIsopoint.getCol(), null);
                        Isopoint allNonNullBetweenPts1 = getAllNonNullBetweenPts(tmpPt1, isoline.val, otherLevel, ptsList, allIsolineList);
                        if (allNonNullBetweenPts1 != null) {
                            nextIsopoint = new Isopoint();
                            nextIsopoint.setAll(allNonNullBetweenPts1.getRow(), allNonNullBetweenPts1.getCol(), allNonNullBetweenPts1.getIsHorizon());
                        }
                    } else {
                        nextIsopoint = null;
                        return null;
                    }
                }

                if (!isTwoPt) {
                    //如果是等值线的两端取点为同一个点，则不需考虑联通点是否唯一
                    if (!isSESamePt(isoline, isoline.val, otherLevel)) {
                        //如果此点和其他点之间存在等值线 且不为当前的等值线 则不需考虑联通点是否唯一
                        if (nextIsopoint != null) {
                            if (!hasAnotherLine(nextIsopoint, allIsolineList)) {
                                //考虑联通点是否唯一
                                if (isTheOnlyEdge(nextIsopoint, isoline.val, otherLevel, allIsolineList, false)) {
                                    List<Isopoint> tmpPtList = get8DirectionPts(nextIsopoint);
                                    List<Point2D.Double> tmpPts = getLineBetweenOtherPts(tmpPtList, allIsolineList, false);
                                    if (tmpPts == null) {
                                        nextIsopoint = null;
                                        //如果唯一则其他点之间是否存在等值线
                                        List<Isopoint> nonNullPts = get8DirectionPts(curIsopoint);
                                        return getLineBetweenOtherPts(nonNullPts, allIsolineList, true);
                                        //						return null;
                                    }
                                }
                            }
                        }
                    }
                }
            } else {//起始点在顶点出错
                if (logger.isDebugEnabled()) logger.debug("In HYIsolineProcess.getNextPt()起始点在顶点出错");
                return null;
            }
        } else {//非起始点
            boolean isFind = false;
//			nextIsopoint = new Isopoint();
            if (isHorizon == null) {//在顶点
                //八个方向，考虑大小、距离因素
                boolean isFirst = true;
                //八方向
                while (true && !isFind) {
                    //1、排除null
                    List<Isopoint> nonNullPts = get8DirectionPts(curIsopoint);

                    //2、排除前一点preIsopoint
                    List<Isopoint> nonPrePts = new ArrayList<Isopoint>();
                    if (nonNullPts != null && !nonNullPts.isEmpty()) {
                        for (int i = 0; i < nonNullPts.size(); i++) {
                            if (!isPrePt(nonNullPts.get(i), preIsopoint)) {
                                nonPrePts.add(nonNullPts.get(i));
                            }
                        }
                    } else {
                        break;
                    }

                    List<Isopoint> nonInPtList1 = new ArrayList<Isopoint>();
                    List<Isopoint> nonInPtList2 = new ArrayList<Isopoint>();
                    List<Isopoint> moreConnectPt1 = new ArrayList<Isopoint>();
                    List<Isopoint> moreConnectPt2 = new ArrayList<Isopoint>();

                    boolean isFindLine = false;
                    if (isFirst) {
                        List<Isopoint> hasNonIsopoint = new ArrayList<Isopoint>();
                        if (!isTwoPt && !isRecall) {
                            //					3、和当前点连线边上是否存在等值点
                            //						1)、若存在则看是否为某个等值线的起点或终点
                            //						2)、若为终点或起点 排除存在Map中的点 则nextPt=起点或终点 连接
                            //						3)、若为中点 则排除continue 下面
                            isFindLine = true;
                            List<Point2D.Double> startEndPts = new ArrayList<Point2D.Double>();
                            int num = 0;
                            List<Isopoint> isopointList = new ArrayList<Isopoint>();
                            Isopoint tmpIsopoint = null;
                            List<Integer> tmpIndexList = new ArrayList<Integer>();
                            List<Integer> tmpOnlineIndexList = new ArrayList<Integer>();
                            List<Integer> allLineIndexList = new ArrayList<Integer>();
                            if (nonPrePts != null && !nonPrePts.isEmpty()) {
                                for (int i = 0; i < nonPrePts.size(); i++) {
                                    num = startEndPts.size();
                                    List<Point2D.Double> tmpPts = getIsopointOnline(nonPrePts.get(i), curIsopoint, allIsolineList);
                                    tmpIsopoint = null;
                                    if (tmpPts != null) {
                                        startEndPts.addAll(tmpPts);//存在等值点,且为终点或起点
                                        tmpIsopoint = new Isopoint();
                                        tmpIsopoint.setAll(nextIsoptOnLine.getRow(), nextIsoptOnLine.getCol(), nextIsoptOnLine.getIsHorizon());
                                        tmpOnlineIndexList.add(onlineIndex);
                                    } else {
                                        startEndPts.add(null);
                                        tmpOnlineIndexList.add(-1);
                                    }
                                    isopointList.add(tmpIsopoint);
                                    if (startEndPts != null && !startEndPts.isEmpty() && startEndPts.get(startEndPts.size() - 1) != null) {
                                        if (!isContainPt(ptsList, nonPrePts.get(i))) {
                                            tmpIndexList.add(i);
                                            if (fillLineIndexList != null && !fillLineIndexList.isEmpty() && fillLineIndexList.size() > 1) {
                                                allLineIndexList.add(fillLineIndexList.get(fillLineIndexList.size() - 1));
                                            }
                                        } else {
                                            for (int j = num; j < startEndPts.size(); j++) {
                                                startEndPts.remove(startEndPts.get(j));
                                            }
                                        }
                                    } else {
                                        hasNonIsopoint.add(nonPrePts.get(i));
                                    }
                                }
                            } else {
                                break;
                            }

                            if (tmpIndexList != null && !tmpIndexList.isEmpty()) {
                                int tmpIndex = -1;
                                if (tmpIndexList.size() != 1) {
                                    if (logger.isDebugEnabled())
                                        logger.debug("存在多个等值点");//尤其是两个点的等值线
                                    //如果存在多个，则考虑用哪个
                                    //1、优先考虑fillLineIndexList.get(0)
                                    //2、考虑距离问题
                                    if (allLineIndexList != null && !allLineIndexList.isEmpty()) {
                                        for (int j = 0; j < allLineIndexList.size(); j++) {
                                            if (allLineIndexList.get(j) == fillLineIndexList.get(0)) {
                                                tmpIndex = tmpIndexList.get(j);
                                                break;
                                            }
                                        }
                                    }

                                    if (tmpIndex == -1) {
                                        Isopoint tmpPrept = new Isopoint();
                                        tmpPrept.setAll(preIsopoint.getRow(), preIsopoint.getCol(), null);
                                        float distance = 3;
                                        for (int j = 0; j < tmpIndexList.size(); j++) {
                                            float dis = getDistance(tmpPrept, nonPrePts.get(tmpIndexList.get(j)));
                                            if (distance > dis) {
                                                distance = dis;
                                                tmpIndex = tmpIndexList.get(j);
                                            } else if (distance == dis) {
                                                tmpIndex = tmpIndexList.get(j);
                                            }
                                        }
                                    }
                                    int lineDSize = fillLineDirectionList.size();
                                    int lineISize = fillLineIndexList.size();
                                    int lineVSize = fillLineValList.size();
                                    for (int j = tmpIndexList.size() - 1; j >= 0; j--) {
                                        if (tmpIndex != tmpIndexList.get(j)) {
                                            fillLineDirectionList.remove(lineDSize - 1);
                                            fillLineIndexList.remove(lineISize - 1);
                                            fillLineValList.remove(lineVSize - 1);
                                        }
                                        lineDSize--;
                                        lineISize--;
                                        lineVSize--;
                                    }
                                } else {
                                    tmpIndex = tmpIndexList.get(0);
                                }
                                Isopoint tmpPt = isopointList.get(tmpIndex);
                                nextIsoptOnLine = new Isopoint();
                                nextIsoptOnLine.setAll(tmpPt.getRow(), tmpPt.getCol(), tmpPt.getIsHorizon());
                                onlineIndex = tmpOnlineIndexList.get(tmpIndex);
                                return getLineFromDIV(fillLineIndexList.get(fillLineIndexList.size() - 1),
                                        fillLineDirectionList.get(fillLineDirectionList.size() - 1), allIsolineList);
                            }
                        } else {
                            hasNonIsopoint = nonPrePts;
                        }

                        //3、排除不在之间的点
                        List<Isopoint> betweenSELevelPts = new ArrayList<Isopoint>();
                        if (hasNonIsopoint != null && !hasNonIsopoint.isEmpty()) {
                            for (int i = 0; i < hasNonIsopoint.size(); i++) {
                                if (isBetweenTwoVals(hasNonIsopoint.get(i), isoline.val, otherLevel)) {
                                    betweenSELevelPts.add(hasNonIsopoint.get(i));
                                }
                            }
                        } else {
                            if (isTwoPt) {
                                betweenSELevelPts = null;
                            } else {
                                break;
                            }
                        }
                        //5、排除非边界
                        List<Isopoint> firstEdgePts = new ArrayList<Isopoint>();
                        List<Isopoint> secondEdgePts = new ArrayList<Isopoint>();
                        //					if(isFirst){
                        if (betweenSELevelPts != null && !betweenSELevelPts.isEmpty()) {
                            for (int i = 0; i < betweenSELevelPts.size(); i++) {
                                if (isEdgePt(betweenSELevelPts.get(i))) {
                                    firstEdgePts.add(betweenSELevelPts.get(i));
                                } else {
                                    if (isSecondEdgePt(betweenSELevelPts.get(i))) {
                                        secondEdgePts.add(betweenSELevelPts.get(i));
                                    }
                                }
                            }
                        } else {
                            firstEdgePts = betweenSELevelPts;
                            secondEdgePts = betweenSELevelPts;
                            //							break;
                        }
                        //6、排除存在Map中的点
//						List<Isopoint> nonInPtList1 = new ArrayList<Isopoint>();
                        if (firstEdgePts != null && !firstEdgePts.isEmpty()) {
                            for (int i = 0; i < firstEdgePts.size(); i++) {
                                if (!isContainPt(ptsList, firstEdgePts.get(i))) {
                                    nonInPtList1.add(firstEdgePts.get(i));
                                }
                            }
                        } else {
                            nonInPtList1 = firstEdgePts;
                        }

//						List<Isopoint> nonInPtList2 = new ArrayList<Isopoint>();
                        if (secondEdgePts != null && !secondEdgePts.isEmpty()) {
                            for (int i = 0; i < secondEdgePts.size(); i++) {
                                if (!isContainPt(ptsList, secondEdgePts.get(i))) {
                                    nonInPtList2.add(secondEdgePts.get(i));
                                }
                            }
                        } else {
                            secondEdgePts = nonInPtList2;
                        }
                        //7、去除联通点只有一个的点
//						List<Isopoint> moreConnectPt1 = new ArrayList<Isopoint>();
                        if (nonInPtList1 != null && !nonInPtList1.isEmpty()) {
                            for (int i = 0; i < nonInPtList1.size(); i++) {
                                if (!isTheOnlyEdge(nonInPtList1.get(i), isoline.val, otherLevel, allIsolineList, true)) {
                                    moreConnectPt1.add(nonInPtList1.get(i));
                                }
                            }
                        } else {
                            moreConnectPt1 = nonInPtList1;
                        }

//						List<Isopoint> moreConnectPt2 = new ArrayList<Isopoint>();
                        if (nonInPtList2 != null && !nonInPtList2.isEmpty()) {
                            for (int i = 0; i < nonInPtList2.size(); i++) {
                                if (!isTheOnlyEdge(nonInPtList2.get(i), isoline.val, otherLevel, allIsolineList, true)) {
                                    moreConnectPt2.add(nonInPtList2.get(i));
                                }
                            }
                        } else {
                            moreConnectPt2 = nonInPtList2;
                        }

                        //8、找边界最多的点,去除已经被别的线走过的点
                        List<Integer> index1 = new ArrayList<Integer>();
                        List<Isopoint> mostEdgt1 = new ArrayList<Isopoint>();
                        if (moreConnectPt1 != null && !moreConnectPt1.isEmpty() && moreConnectPt1.size() > 1) {
                            int edgtNum = 1;
                            for (int i = 0; i < moreConnectPt1.size(); i++) {
                                if (isAdd(moreConnectPt1.get(i))) {
                                    continue;
                                }
                                int tmpNum = getEdgeNum(moreConnectPt1.get(i));
                                if (edgtNum < tmpNum) {
                                    edgtNum = tmpNum;
                                    if (index1 != null && !index1.isEmpty()) {
                                        index1.clear();
                                    }
                                    index1.add(i);
                                } else if (edgtNum == tmpNum) {
                                    edgtNum = tmpNum;
                                    index1.add(i);
                                }
                            }
                            if (index1 != null && !index1.isEmpty()) {
                                for (int i = 0; i < index1.size(); i++) {
                                    mostEdgt1.add(moreConnectPt1.get(index1.get(i)));
                                }
                            }
                        }
                        if (mostEdgt1 == null || mostEdgt1.isEmpty()) {
                            for (int i = 0; i < moreConnectPt1.size(); i++) {
                                if (isAdd(moreConnectPt1.get(i))) {
                                    continue;
                                }
                                mostEdgt1.add(moreConnectPt1.get(i));
                            }
                        }

                        List<Integer> index2 = new ArrayList<Integer>();
                        List<Isopoint> mostEdgt2 = new ArrayList<Isopoint>();
                        if (moreConnectPt2 != null && !moreConnectPt2.isEmpty() && moreConnectPt2.size() > 1) {
                            int edgtNum = 1;
                            for (int i = 0; i < moreConnectPt2.size(); i++) {
                                if (isAdd(moreConnectPt2.get(i))) {
                                    continue;
                                }
                                int tmpNum = getEdgeNum(moreConnectPt2.get(i));
                                if (edgtNum < tmpNum) {
                                    edgtNum = tmpNum;
                                    if (index2 != null && !index2.isEmpty()) {
                                        index2.clear();
                                    }
                                    index2.add(i);
                                } else if (edgtNum == tmpNum) {
                                    edgtNum = tmpNum;
                                    index2.add(i);
                                }
                            }
                            if (index2 != null && !index2.isEmpty()) {
                                for (int i = 0; i < index2.size(); i++) {
                                    mostEdgt2.add(moreConnectPt2.get(index2.get(i)));
                                }
                            }
                        }
                        if (mostEdgt2 == null || mostEdgt2.isEmpty()) {
                            for (int i = 0; i < moreConnectPt2.size(); i++) {
                                if (isAdd(moreConnectPt2.get(i))) {
                                    continue;
                                }
                                mostEdgt2.add(moreConnectPt2.get(i));
                            }
                        }


                        //9、找存在等值点的那些点。邻近等值线处的判断
                        List<Isopoint> hasIsopointNonEnd1 = new ArrayList<Isopoint>();
                        if (!isTwoPt) {
                            if (mostEdgt1 != null && !mostEdgt1.isEmpty() && mostEdgt1.size() > 1) {
                                for (int i = 0; i < mostEdgt1.size(); i++) {
                                    boolean isHasIsopoint = isHasIsopointNonEnd(mostEdgt1.get(i), isoline.val, otherLevel, allIsolineList);
                                    if (isHasIsopoint) {
                                        hasIsopointNonEnd1.add(mostEdgt1.get(i));
                                    }
                                }
                            }
                        }

                        if (hasIsopointNonEnd1 == null || hasIsopointNonEnd1.isEmpty()) {
                            hasIsopointNonEnd1 = mostEdgt1;
                        }

                        List<Isopoint> hasIsopointNonEnd2 = new ArrayList<Isopoint>();
                        if (!isTwoPt) {
                            if (mostEdgt2 != null && !mostEdgt2.isEmpty() && mostEdgt2.size() > 1) {
                                for (int i = 0; i < mostEdgt2.size(); i++) {
                                    boolean isHasIsopoint = isHasIsopointNonEnd(mostEdgt2.get(i), isoline.val, otherLevel, allIsolineList);
                                    if (isHasIsopoint) {
                                        hasIsopointNonEnd2.add(mostEdgt2.get(i));
                                    }
                                }
                            }
                        }

                        if (hasIsopointNonEnd2 == null || hasIsopointNonEnd2.isEmpty()) {
                            hasIsopointNonEnd2 = mostEdgt2;
                        }

                        //10、找上下左右联通点最多的点，若上下左右和相等找最大最小值
                        List<Integer> indexList = new ArrayList<Integer>();
                        List<Isopoint> mostConnectPt1 = new ArrayList<Isopoint>();
                        if (hasIsopointNonEnd1 != null && !hasIsopointNonEnd1.isEmpty() && hasIsopointNonEnd1.size() > 1) {
                            int borderNum = 0;
                            for (int i = 0; i < hasIsopointNonEnd1.size(); i++) {
                                int tmpNum = getBorderNum(hasIsopointNonEnd1.get(i));
                                if (tmpNum > borderNum) {
                                    borderNum = tmpNum;
                                    if (indexList != null && !indexList.isEmpty()) {
                                        indexList.clear();
                                    }
                                    indexList.add(i);
                                } else if (tmpNum == borderNum) {
                                    borderNum = tmpNum;
                                    indexList.add(i);
                                }
                            }
                            if (indexList != null && !indexList.isEmpty()) {
                                for (int i = 0; i < indexList.size(); i++) {
                                    mostConnectPt1.add(hasIsopointNonEnd1.get(indexList.get(i)));
                                }
                            }
                        }

                        if (mostConnectPt1 == null || mostConnectPt1.isEmpty()) {
                            mostConnectPt1 = hasIsopointNonEnd1;
                        }

                        indexList = new ArrayList<Integer>();
                        List<Isopoint> mostConnectPt2 = new ArrayList<Isopoint>();
                        if (hasIsopointNonEnd2 != null && !hasIsopointNonEnd2.isEmpty() && hasIsopointNonEnd2.size() > 1) {
                            int cornerNum = 0;
                            for (int i = 0; i < hasIsopointNonEnd2.size(); i++) {
                                int tmpNum = getCornerNum(hasIsopointNonEnd2.get(i));
                                if (tmpNum > cornerNum) {
                                    cornerNum = tmpNum;
                                    if (indexList != null && !indexList.isEmpty()) {
                                        indexList.clear();
                                    }
                                    indexList.add(i);
                                } else if (tmpNum == cornerNum) {
                                    cornerNum = tmpNum;
                                    indexList.add(i);
                                }
                            }
                            if (indexList != null && !indexList.isEmpty()) {
                                for (int i = 0; i < indexList.size(); i++) {
                                    mostConnectPt2.add(hasIsopointNonEnd2.get(indexList.get(i)));
                                }
                            }
                        }

                        if (mostConnectPt2 == null || mostConnectPt2.isEmpty()) {
                            mostConnectPt2 = hasIsopointNonEnd2;
                        }

                        //11、找距离最近的那一个
                        indexList = new ArrayList<Integer>();
                        List<Isopoint> nearstPt1 = new ArrayList<Isopoint>();
                        if (mostConnectPt1 != null && !mostConnectPt1.isEmpty() && mostConnectPt1.size() > 1) {
                            float dis = getDistance(curIsopoint, mostConnectPt1.get(0));
                            for (int i = 0; i < mostConnectPt1.size(); i++) {
                                float val = getDistance(curIsopoint, mostConnectPt1.get(i));
                                if (val != -1 && dis > val) {
                                    dis = val;
                                    if (indexList != null && !indexList.isEmpty()) {
                                        indexList.clear();
                                    }
                                    indexList.add(i);
                                } else if (val == dis) {
                                    dis = val;
                                    indexList.add(i);
                                }
                            }
                            for (int i = 0; i < indexList.size(); i++) {
                                nearstPt1.add(mostConnectPt1.get(indexList.get(i)));
                            }
                        }
                        if (nearstPt1 == null || nearstPt1.isEmpty()) {
                            nearstPt1 = mostConnectPt1;
                        }

                        indexList = new ArrayList<Integer>();
                        List<Isopoint> nearstPt2 = new ArrayList<Isopoint>();
                        if (mostConnectPt2 != null && !mostConnectPt2.isEmpty() && mostConnectPt2.size() > 1) {
                            float dis = getDistance(curIsopoint, mostConnectPt2.get(0));
                            for (int i = 0; i < mostConnectPt2.size(); i++) {
                                float val = getDistance(curIsopoint, mostConnectPt2.get(i));
                                if (val != -1 && dis > val) {
                                    dis = val;
                                    if (indexList != null && !indexList.isEmpty()) {
                                        indexList.clear();
                                    }
                                    indexList.add(i);
                                } else if (val == dis) {
                                    dis = val;
                                    indexList.add(i);
                                }
                            }
                            for (int i = 0; i < indexList.size(); i++) {
                                nearstPt2.add(mostConnectPt2.get(indexList.get(i)));
                            }
                        }
                        if (nearstPt2 == null || nearstPt2.isEmpty()) {
                            nearstPt2 = mostConnectPt2;
                        }

                        //12、找和prePt距离最近的一点
                        indexList = new ArrayList<Integer>();
                        List<Isopoint> nearstPrePt1 = new ArrayList<Isopoint>();
                        Isopoint tmpPrept = new Isopoint();
                        tmpPrept.setAll(preIsopoint.getRow(), preIsopoint.getCol(), null);
                        if (nearstPt1 != null && !nearstPt1.isEmpty() && nearstPt1.size() > 1) {
                            float dis = getDistance(tmpPrept, nearstPt1.get(0));
                            for (int i = 0; i < nearstPt1.size(); i++) {
                                float val = getDistance(tmpPrept, nearstPt1.get(i));
                                if (val != -1 && dis > val) {
                                    dis = val;
                                    if (indexList != null && !indexList.isEmpty()) {
                                        indexList.clear();
                                    }
                                    indexList.add(i);
                                } else if (val == dis) {
                                    dis = val;
                                    indexList.add(i);
                                }
                            }
                            for (int i = 0; i < indexList.size(); i++) {
                                nearstPrePt1.add(nearstPt1.get(indexList.get(i)));
                            }
                        }
                        if (nearstPrePt1 == null || nearstPrePt1.isEmpty()) {
                            nearstPrePt1 = nearstPt1;
                        }

                        indexList = new ArrayList<Integer>();
                        List<Isopoint> nearstPrePt2 = new ArrayList<Isopoint>();
                        if (nearstPt2 != null && !nearstPt2.isEmpty() && nearstPt2.size() > 1) {
                            float dis = getDistance(tmpPrept, nearstPt2.get(0));
                            for (int i = 0; i < nearstPt2.size(); i++) {
                                float val = getDistance(tmpPrept, nearstPt2.get(i));
                                if (val != -1 && dis > val) {
                                    dis = val;
                                    if (indexList != null && !indexList.isEmpty()) {
                                        indexList.clear();
                                    }
                                    indexList.add(i);
                                } else if (val == dis) {
                                    dis = val;
                                    indexList.add(i);
                                }
                            }
                            for (int i = 0; i < indexList.size(); i++) {
                                nearstPrePt2.add(nearstPt2.get(indexList.get(i)));
                            }
                        }
                        if (nearstPrePt2 == null || nearstPrePt2.isEmpty()) {
                            nearstPrePt2 = nearstPt2;
                        }


                        //12、找最大或最小的那个点
                        indexList = new ArrayList<Integer>();//防止存在一样大的值
                        List<Isopoint> biggerPt1 = new ArrayList<Isopoint>();
                        if (nearstPrePt1 != null && !nearstPrePt1.isEmpty() && nearstPrePt1.size() > 1) {
                            float curL = isoline.val;
                            for (int i = 0; i < nearstPrePt1.size(); i++) {
                                float val = getIsopointVal(nearstPrePt1.get(i));
                                if (isoline.val < otherLevel) {
                                    if (val != NULLVAL && curL < val) {
                                        curL = val;
                                        if (indexList != null && !indexList.isEmpty()) {
                                            indexList.clear();
                                        }
                                        indexList.add(i);
                                    } else if (val == curL) {
                                        curL = val;
                                        indexList.add(i);
                                    }
                                } else {
                                    if (val != NULLVAL && curL > val) {
                                        curL = val;
                                        if (indexList != null && !indexList.isEmpty()) {
                                            indexList.clear();
                                        }
                                        indexList.add(i);
                                    } else if (val == curL) {
                                        curL = val;
                                        indexList.add(i);
                                    }
                                }
                            }
                            for (int i = 0; i < indexList.size(); i++) {
                                biggerPt1.add(nearstPrePt1.get(indexList.get(i)));
                            }
                        }
                        if (biggerPt1 == null || biggerPt1.isEmpty()) {
                            biggerPt1 = nearstPrePt1;
                        }

                        indexList = new ArrayList<Integer>();
                        List<Isopoint> biggerPt2 = new ArrayList<Isopoint>();
                        if (nearstPrePt2 != null && !nearstPrePt2.isEmpty() && nearstPrePt2.size() > 1) {
                            float curL = isoline.val;
                            for (int i = 0; i < nearstPrePt2.size(); i++) {
                                float val = getIsopointVal(nearstPrePt2.get(i));
                                if (isoline.val < otherLevel) {
                                    if (val != NULLVAL && curL < val) {
                                        curL = val;
                                        if (indexList != null && !indexList.isEmpty()) {
                                            indexList.clear();
                                        }
                                        indexList.add(i);
                                    } else if (val == curL) {
                                        curL = val;
                                        indexList.add(i);
                                    }
                                } else {
                                    if (val != NULLVAL && curL > val) {
                                        curL = val;
                                        if (indexList != null && !indexList.isEmpty()) {
                                            indexList.clear();
                                        }
                                        indexList.add(i);
                                    } else if (val == curL) {
                                        curL = val;
                                        indexList.add(i);
                                    }
                                }
                            }
                            for (int i = 0; i < indexList.size(); i++) {
                                biggerPt2.add(nearstPrePt2.get(indexList.get(i)));
                            }
                        }

                        if (biggerPt2 == null || biggerPt2.isEmpty()) {
                            biggerPt2 = nearstPrePt2;
                        }

                        if ((biggerPt1 == null || biggerPt1.isEmpty()) && (biggerPt2 == null || biggerPt2.isEmpty()) && isTwoPt && !isFindLine && !isRecall) {
                            List<Point2D.Double> startEndPts = new ArrayList<Point2D.Double>();
                            int num = 0;
                            List<Isopoint> isopointList = new ArrayList<Isopoint>();
                            Isopoint tmpIsopoint = null;
                            List<Integer> tmpIndexList = new ArrayList<Integer>();
                            List<Integer> tmpOnlineIndexList = new ArrayList<Integer>();
                            List<Integer> allLineIndexList = new ArrayList<Integer>();
//							List<Isopoint> hasIsopoint = new ArrayList<Isopoint>();
                            if (nonPrePts != null && !nonPrePts.isEmpty()) {
                                for (int i = 0; i < nonPrePts.size(); i++) {
                                    num = startEndPts.size();
                                    List<Point2D.Double> tmpPts = getIsopointOnline(nonPrePts.get(i), curIsopoint, allIsolineList);
                                    tmpIsopoint = null;
                                    if (tmpPts != null) {
                                        startEndPts.addAll(tmpPts);//存在等值点,且为终点或起点
                                        tmpIsopoint = new Isopoint();
                                        tmpIsopoint.setAll(nextIsoptOnLine.getRow(), nextIsoptOnLine.getCol(), nextIsoptOnLine.getIsHorizon());
                                        tmpOnlineIndexList.add(onlineIndex);
                                    } else {
                                        startEndPts.add(null);
                                        tmpOnlineIndexList.add(-1);
                                    }
                                    isopointList.add(tmpIsopoint);
                                    if (startEndPts != null && !startEndPts.isEmpty() && startEndPts.get(startEndPts.size() - 1) != null) {
                                        if (!isContainPt(ptsList, nonPrePts.get(i))) {
                                            tmpIndexList.add(i);
                                            if (fillLineIndexList != null && !fillLineIndexList.isEmpty() && fillLineIndexList.size() > 1) {
                                                allLineIndexList.add(fillLineIndexList.get(fillLineIndexList.size() - 1));
                                            }
                                        } else {
                                            for (int j = num; j < startEndPts.size(); j++) {
                                                startEndPts.remove(startEndPts.get(j));
                                            }
                                        }
                                    }
                                }
                            } else {
                                break;
                            }

                            if (tmpIndexList != null && !tmpIndexList.isEmpty()) {
                                int tmpIndex = -1;
                                if (tmpIndexList.size() != 1) {
                                    if (logger.isDebugEnabled())
                                        logger.debug("存在多个等值点");//尤其是两个点的等值线
                                    //如果存在多个，则考虑用哪个
                                    //1、优先考虑fillLineIndexList.get(0)
                                    //2、考虑距离问题
                                    if (allLineIndexList != null && !allLineIndexList.isEmpty()) {
                                        for (int j = 0; j < allLineIndexList.size(); j++) {
                                            if (allLineIndexList.get(j) == fillLineIndexList.get(0)) {
                                                tmpIndex = tmpIndexList.get(j);
                                                break;
                                            }
                                        }
                                    }

                                    if (tmpIndex == -1) {
                                        float distance = 3;
                                        for (int j = 0; j < tmpIndexList.size(); j++) {
                                            float dis = getDistance(tmpPrept, nonPrePts.get(tmpIndexList.get(j)));
                                            if (distance > dis) {
                                                distance = dis;
                                                tmpIndex = tmpIndexList.get(j);
                                            } else if (distance == dis) {
                                                tmpIndex = tmpIndexList.get(j);
                                            }
                                        }
                                        int lineDSize = fillLineDirectionList.size();
                                        int lineISize = fillLineIndexList.size();
                                        int lineVSize = fillLineValList.size();
                                        for (int j = tmpIndexList.size() - 1; j >= 0; j--) {
                                            if (tmpIndex != tmpIndexList.get(j)) {
                                                fillLineDirectionList.remove(lineDSize - 1);
                                                fillLineIndexList.remove(lineISize - 1);
                                                fillLineValList.remove(lineVSize - 1);
                                            }
                                            lineDSize--;
                                            lineISize--;
                                            lineVSize--;
                                        }
                                    }

                                } else {
                                    tmpIndex = tmpIndexList.get(0);
                                }
                                Isopoint tmpPt = isopointList.get(tmpIndex);
                                nextIsoptOnLine = new Isopoint();
                                nextIsoptOnLine.setAll(tmpPt.getRow(), tmpPt.getCol(), tmpPt.getIsHorizon());
                                onlineIndex = tmpOnlineIndexList.get(tmpIndex);
                                return getLineFromDIV(fillLineIndexList.get(fillLineIndexList.size() - 1),
                                        fillLineDirectionList.get(fillLineDirectionList.size() - 1), allIsolineList);
                            }
                        }

                        if (isTwoPt) {
                            isTwoPt = false;
                        }

                        //13、若找到则nextPt =
                        if (biggerPt1 != null && !biggerPt1.isEmpty()) {
                            nextIsopoint = new Isopoint();
                            //找与prePoint点离的最远的那一点
                            if (biggerPt1.size() == 1) {
                                nextIsopoint = biggerPt1.get(0);
                            } else {
                                nextIsopoint = getFurtherPt(biggerPt1, preIsopoint);
                            }
                            isFind = true;
                        } else if (biggerPt2 != null && !biggerPt2.isEmpty()) {
                            nextIsopoint = new Isopoint();
                            //找与prePoint点离的最远的那一点
                            if (biggerPt2.size() == 1) {
                                nextIsopoint = biggerPt2.get(0);
                            } else {
                                nextIsopoint = getFurtherPt(biggerPt2, preIsopoint);
                            }
                            isFind = true;
                        } else if (biggerPt1 == null || biggerPt2 == null || biggerPt1.isEmpty() || biggerPt2.isEmpty()) {
                            if (logger.isDebugEnabled())
                                logger.debug("不存在符合条件的点,需要前推,或寻找等值线");
                            nextIsopoint = null;
                            isFirst = false;
//	 						break;
                        }
                    }

                    if (!isFirst) {
                        if (nonInPtList1 != null && nonInPtList1.size() == 1 && (moreConnectPt1 == null ||
                                moreConnectPt1.isEmpty())) {
                            //如果唯一则其他点之间是否存在等值线
                            List<Isopoint> tmpPtList = get8DirectionPts(nonInPtList1.get(0));
                            List<Point2D.Double> tmpPts = getLineBetweenOtherPts(tmpPtList, allIsolineList, false);
                            if (tmpPts != null) {
                                nextIsopoint = new Isopoint();
                                nextIsopoint.setAll(nonInPtList1.get(0).getRow(), nonInPtList1.get(0).getCol(), null);
                            }
                        }
                        if (nonInPtList2 != null && nonInPtList2.size() == 1 && (moreConnectPt2 == null ||
                                moreConnectPt2.isEmpty())) {
                            //如果唯一则其他点之间是否存在等值线
                            List<Isopoint> tmpPtList = get8DirectionPts(nonInPtList2.get(0));
                            List<Point2D.Double> tmpPts = getLineBetweenOtherPts(tmpPtList, allIsolineList, false);
                            if (tmpPts != null) {
                                nextIsopoint = new Isopoint();
                                nextIsopoint.setAll(nonInPtList2.get(0).getRow(), nonInPtList2.get(0).getCol(), null);
                            }
                        }
                        //去除前一点和list中的点
                        if (nextIsopoint == null) {
                            return getLineBetweenOtherPts(nonPrePts, allIsolineList, true);
                        }
                    }
                    break;
                }
            } else {//非顶点
                if (isHorizon) {//横边
                    Isopoint tmpPoint = new Isopoint();
                    if (isBetweenTwoVals(gridData[row][col], isoline.val, otherLevel)) {
                        tmpPoint.setAll(row, col, null);
                    } else if (isBetweenTwoVals(gridData[row][col + 1], isoline.val, otherLevel)) {
                        tmpPoint.setAll(row, col + 1, null);
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug("In HYIsolineProcess.getNextPt()非顶点时在横边上不在填充值的范围内");
                        return null;
                    }
                    //排除在list上的点
                    if (!isContainPt(ptsList, tmpPoint)) {//未加入到list
                        nextIsopoint = tmpPoint;
                    } else {//已加入到list，则向上或向下找
                        int tmpRow = tmpPoint.getRow();
                        int tmpCol = tmpPoint.getCol();
                        Isopoint tmpPoint1 = new Isopoint();
                        Isopoint tmpPoint2 = new Isopoint();
                        if (tmpRow + 1 <= gridRows - 1 && gridData[tmpRow + 1][tmpCol] != NULLVAL &&
                                gridData[tmpRow + 1][tmpCol] >= isoline.val && gridData[tmpRow + 1][tmpCol] <= otherLevel) {
                            tmpPoint1.setAll(tmpRow + 1, tmpCol, null);
                        }
                        if (tmpRow - 1 >= 0 && gridData[tmpRow - 1][tmpCol] != NULLVAL &&
                                gridData[tmpRow - 1][tmpCol] >= isoline.val && gridData[tmpRow - 1][tmpCol] <= otherLevel) {
                            tmpPoint2.setAll(tmpRow - 1, tmpCol, null);
                        }

                        if (tmpPoint1 != null && tmpPoint2 != null) {
                            //找最大的点
                            float data1 = gridData[tmpRow + 1][tmpCol];
                            float data2 = gridData[tmpRow - 1][tmpCol];
                            if (data1 > data2) {
                                nextIsopoint = tmpPoint1;
                            } else if (data1 < data2) {
                                nextIsopoint = tmpPoint2;
                            } else {
                                int preRow = isoline.rowsList.get(isoline.num - 2);
                                if (preRow >= row) {
                                    nextIsopoint = tmpPoint2;
                                } else {
                                    nextIsopoint = tmpPoint1;
                                }
                            }
                        } else if (tmpPoint1 == null && tmpPoint2 != null) {
                            nextIsopoint = tmpPoint2;
                        } else if (tmpPoint1 != null && tmpPoint2 == null) {
                            nextIsopoint = tmpPoint1;
                        } else {
                            if (logger.isDebugEnabled())
                                logger.debug("In HYIsolineProcess.getNextPt()都为null，未找到满足条件的nextIsopoint");
                            return null;
                        }
                    }
                } else {//在纵边
                    Isopoint tmpPoint = new Isopoint();
                    if (isBetweenTwoVals(gridData[row][col], isoline.val, otherLevel)) {
                        tmpPoint.setAll(row, col, null);
                    } else if (isBetweenTwoVals(gridData[row + 1][col], isoline.val, otherLevel)) {
                        tmpPoint.setAll(row + 1, col, null);
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug("In HYIsolineProcess.getNextPt()在纵边上不在填充的范围内");
                        return null;
                    }
                    //排除在list上的点
                    if (!isContainPt(ptsList, tmpPoint)) {//未加入list
                        nextIsopoint = tmpPoint;
                    } else {//已加入list，则向上向下找
                        int tmpRow = tmpPoint.getRow();
                        int tmpCol = tmpPoint.getCol();
                        Isopoint tmpPoint1 = new Isopoint();
                        Isopoint tmpPoint2 = new Isopoint();
                        if (tmpCol + 1 <= gridCols - 1 && gridData[tmpRow][tmpCol + 1] != NULLVAL &&
                                gridData[tmpRow][tmpCol + 1] >= isoline.val && gridData[tmpRow][tmpCol + 1] <= otherLevel) {
                            tmpPoint1.setAll(tmpRow, tmpCol + 1, null);
                        }
                        if (tmpCol - 1 >= 0 && gridData[tmpRow][tmpCol - 1] != NULLVAL &&
                                gridData[tmpRow][tmpCol - 1] >= isoline.val && gridData[tmpRow][tmpCol - 1] <= otherLevel) {
                            tmpPoint2.setAll(tmpRow, tmpCol - 1, null);
                        }

                        if (tmpPoint1 != null && tmpPoint2 != null) {
                            //找最大的点
                            float data1 = gridData[tmpRow][tmpCol + 1];
                            float data2 = gridData[tmpRow][tmpCol - 1];
                            if (data1 > data2) {
                                nextIsopoint = tmpPoint1;
                            } else if (data1 < data2) {
                                nextIsopoint = tmpPoint2;
                            } else {
                                int preCol = isoline.colsList.get(isoline.num - 2);
                                if (preCol >= col) {
                                    nextIsopoint = tmpPoint2;
                                } else {
                                    nextIsopoint = tmpPoint1;
                                }
                            }
                        } else if (tmpPoint1 == null && tmpPoint2 != null) {
                            nextIsopoint = tmpPoint2;
                        } else if (tmpPoint1 != null && tmpPoint2 == null) {
                            nextIsopoint = tmpPoint1;
                        } else {
                            if (logger.isDebugEnabled())
                                logger.debug("In HYIsolineProcess.getNextPt()都为null,未找到nextIsopoint");
                            return null;
                        }
                    }
                }
            }
        }

        if (nextIsopoint == null) {
            return null;
        }
        Point2D.Double resPt = getPos(nextIsopoint.getRow(), nextIsopoint.getCol());
        resPtsList.add(resPt);
        return resPtsList;
    }

    //当没有找到nextPt时，从八个点之间找是否存在等值线
    //暂时只考虑只找一个
    private List<Point2D.Double> getLineBetweenOtherPts(List<Isopoint> pts, List<IsolineDataProc> allIsolineList, boolean isFindLine) {
        float[][] gridData = gridDataList.get(0).getGridData();

        for (int i = 0; i < pts.size(); i++) {
            for (int j = 0; j < pts.size(); j++) {
                if (i == j) {
                    continue;
                }
                Isopoint pt1 = pts.get(i);
                Isopoint pt2 = pts.get(j);
                int row1 = pt1.getRow();
                int col1 = pt1.getCol();
                int row2 = pt2.getRow();
                int col2 = pt2.getCol();
                if (gridData[row1][col1] == NULLVAL || gridData[row2][col2] == NULLVAL) {
                    continue;
                }
                if (row1 == row2 && Math.abs(col1 - col2) == 1) {
                    List<Point2D.Double> tmpPts = getLineBetween2Pts(pt1, pt2, true, allIsolineList, isFindLine);
                    if (tmpPts == null || tmpPts.isEmpty()) {
                        continue;
                    } else {
                        return tmpPts;
                    }
                } else if (col1 == col2 && Math.abs(row1 - row2) == 1) {
                    List<Point2D.Double> tmpPts = getLineBetween2Pts(pt1, pt2, false, allIsolineList, isFindLine);
                    if (tmpPts == null || tmpPts.isEmpty()) {
                        continue;
                    } else {
                        return tmpPts;
                    }
                } else {
                    continue;
                }
            }
        }
        return null;
    }

    private boolean isOnLineAndAdd(Isopoint curPt) {
        int row = curPt.getRow();
        int col = curPt.getCol();
        Boolean isH = curPt.getIsHorizon();

        if (fillIsopointList == null || fillIsopointList.isEmpty()) {
            return false;
        }
        for (int i = 0; i < fillIsopointList.size(); i++) {
            Isopoint pt = fillIsopointList.get(i);
            Boolean tmpH = pt.getIsHorizon();
            int tmpR = pt.getRow();
            int tmpC = pt.getCol();
            if (tmpH != isH) {
                continue;
            }
            if (tmpR != row) {
                continue;
            }
            if (tmpC != col) {
                continue;
            }
            if (i == 0) {
                return false;
            }
            return true;
        }
        return false;
    }

    //获取以两个点为边的等值线
    private List<Point2D.Double> getLineBetween2Pts(Isopoint pt1, Isopoint pt2, Boolean isH, List<IsolineDataProc> allIsolineList, boolean isFindLine) {
        int row1 = pt1.getRow();
        int col1 = pt1.getCol();
        int row2 = pt2.getRow();
        int col2 = pt2.getCol();
        onlineIndex = -1;
        if (row1 != row2 && col1 != col2) {
            return null;
        } else if (row1 == row2 && Math.abs(col1 - col2) == 1 && isH) {//考虑先连接起点还是连接终点
            if (allIsolineList == null || allIsolineList.isEmpty()) {
                throw new RuntimeException("In IsolineProcess.getIsopointOnline()中allIsolineList为空");
            }
            int col = Math.min(col1, col2);
            int row = row1;
            for (int i = 0; i < allIsolineList.size(); i++) {
                IsolineDataProc isoline = allIsolineList.get(i);
                if (isoline.isClosed) {
                    continue;
                }
                if (backFillLineIndexList != null && !backFillLineIndexList.isEmpty()) {
                    boolean isFind = false;
                    for (int j = 0; j < backFillLineIndexList.size(); j++) {
                        if (backFillLineIndexList.get(j) == isoline.indexProc) {
                            isFind = true;
                            break;
                        }
                    }
                    if (isFind) {
                        continue;
                    }
                }

                if (!canConnect(isoline)) {
                    continue;
                }
                List<Integer> rowList = isoline.rowsList;
                List<Integer> colList = isoline.colsList;
                List<Boolean> startHList = isoline.isHorizonList;
                int startR = rowList.get(0);
                int startC = colList.get(0);
                Boolean startH = startHList.get(0);
                int size = rowList.size();
                int endR = rowList.get(size - 1);
                int endC = colList.get(size - 1);
                Boolean endH = startHList.get(size - 1);

                if (fillLineIndexList != null && !fillLineIndexList.isEmpty()) {
                    if (fillLineIndexList.contains(isoline.indexProc)) {
                        //处理判断
                        Isopoint tmpPt = new Isopoint();
                        tmpPt.setAll(row, col, isH);

                        if (fillLineIndexList.get(0) == isoline.indexProc &&
                                fillIsopointList.get(0).getRow() == row &&
                                fillIsopointList.get(0).getCol() == col &&
                                fillIsopointList.get(0).getIsHorizon() == isH) {
                            if (isFindLine) {//只有找线时才加入fillLine,当为找点时不加入,只用来判断唯一点时
                                if (isolineReverse != null && !isolineReverse.isEmpty()) {
                                    Boolean isReverse = isolineReverse.get(isoline.indexProc);
                                    if (isReverse != null) {
                                        if (isReverse == true) {
                                            nextIsoptOnLine = new Isopoint();
                                            nextIsoptOnLine.setAll(startR, startC, startH);
                                            fillLineDirectionList.add(-1);
                                            fillLineIndexList.add(isoline.indexProc);
                                            fillLineValList.add(isoline.val);
                                            onlineIndex = isoline.num - 1;
                                            List<Point2D.Double> ptList = new ArrayList<Point2D.Double>();
                                            for (int j = isoline.num - 1; j >= 0; j--) {
                                                ptList.add(isoline.lineList2D.get(j));
                                            }
                                            return ptList;
                                        }
                                    }
                                }
                                nextIsoptOnLine = new Isopoint();
                                nextIsoptOnLine.setAll(endR, endC, endH);
                                fillLineDirectionList.add(1);
                                fillLineIndexList.add(isoline.indexProc);
                                fillLineValList.add(isoline.val);
                                onlineIndex = 0;
                                return isoline.lineList2D;
                            }
                        }
                        //是否已存在fillIsopointList中
                        if (isOnLineAndAdd(tmpPt)) {
                            continue;
                        }
                    }
                }
                if (startR == row && startC == col && startH == isH) {
                    if (isFindLine) {
                        nextIsoptOnLine = new Isopoint();
                        nextIsoptOnLine.setAll(endR, endC, endH);
                        fillLineDirectionList.add(1);
                        fillLineIndexList.add(isoline.indexProc);
                        fillLineValList.add(isoline.val);
                        onlineIndex = 0;
                    }
                    return isoline.lineList2D;
                } else if (endR == row && endC == col && endH == isH) {
                    List<Point2D.Double> ptList = new ArrayList<Point2D.Double>();
                    for (int j = isoline.num - 1; j >= 0; j--) {
                        ptList.add(isoline.lineList2D.get(j));
                    }
                    if (isFindLine) {
                        nextIsoptOnLine = new Isopoint();
                        nextIsoptOnLine.setAll(startR, startC, startH);
                        fillLineDirectionList.add(-1);
                        fillLineIndexList.add(isoline.indexProc);
                        fillLineValList.add(isoline.val);
                        onlineIndex = isoline.num - 1;
                    }
                    return ptList;
                } else {
                    continue;
                }
            }
        } else if (Math.abs(row1 - row2) == 1 && col1 == col2 && !isH) {
            if (allIsolineList == null || allIsolineList.isEmpty()) {
                throw new RuntimeException("In IsolineProcess.getIsopointOnline()中allIsolineList为空");
            }
            int row = Math.min(row1, row2);
            int col = col1;
            for (int i = 0; i < allIsolineList.size(); i++) {
                IsolineDataProc isoline = allIsolineList.get(i);
                if (isoline.isClosed) {
                    continue;
                }

                if (backFillLineIndexList != null && !backFillLineIndexList.isEmpty()) {
                    boolean isFind = false;
                    for (int j = 0; j < backFillLineIndexList.size(); j++) {
                        if (backFillLineIndexList.get(j) == isoline.indexProc) {
                            isFind = true;
                            break;
                        }
                    }
                    if (isFind) {
                        continue;
                    }
                }

                if (!canConnect(isoline)) {
                    continue;
                }
                List<Integer> rowList = isoline.rowsList;
                List<Integer> colList = isoline.colsList;
                List<Boolean> startHList = isoline.isHorizonList;
                int startR = rowList.get(0);
                int startC = colList.get(0);
                Boolean startH = startHList.get(0);
                int size = rowList.size();
                int endR = rowList.get(size - 1);
                int endC = colList.get(size - 1);
                Boolean endH = startHList.get(size - 1);
                if (fillLineIndexList != null && !fillLineIndexList.isEmpty()) {
                    if (fillLineIndexList.contains(isoline.indexProc)) {
                        //处理判断
                        Isopoint tmpPt = new Isopoint();
                        tmpPt.setAll(row, col, isH);

                        if (fillLineIndexList.get(0) == isoline.indexProc &&
                                fillIsopointList.get(0).getRow() == row &&
                                fillIsopointList.get(0).getCol() == col &&
                                fillIsopointList.get(0).getIsHorizon() == isH) {
                            if (isFindLine) {
                                if (isolineReverse != null && !isolineReverse.isEmpty()) {
                                    Boolean isReverse = isolineReverse.get(isoline.indexProc);
                                    if (isReverse != null) {
                                        if (isReverse == true) {
                                            nextIsoptOnLine = new Isopoint();
                                            nextIsoptOnLine.setAll(startR, startC, startH);
                                            fillLineDirectionList.add(-1);
                                            fillLineIndexList.add(isoline.indexProc);
                                            fillLineValList.add(isoline.val);
                                            onlineIndex = isoline.num - 1;
                                            List<Point2D.Double> ptList = new ArrayList<Point2D.Double>();
                                            for (int j = isoline.num - 1; j >= 0; j--) {
                                                ptList.add(isoline.lineList2D.get(j));
                                            }
                                            return ptList;
                                        }

                                    }
                                }
                                nextIsoptOnLine = new Isopoint();
                                nextIsoptOnLine.setAll(endR, endC, endH);
                                fillLineDirectionList.add(1);
                                fillLineIndexList.add(isoline.indexProc);
                                fillLineValList.add(isoline.val);
                                onlineIndex = 0;
                                return isoline.lineList2D;
                            }
                        }
                        //是否已存在fillIsopointList中
                        if (isOnLineAndAdd(tmpPt)) {
                            continue;
                        }
                    }
                }
                if (startR == row && startC == col && startH == isH) {
                    if (isFindLine) {
                        nextIsoptOnLine = new Isopoint();
                        nextIsoptOnLine.setAll(endR, endC, endH);
                        fillLineDirectionList.add(1);
                        fillLineIndexList.add(isoline.indexProc);
                        fillLineValList.add(isoline.val);
                        onlineIndex = 0;
                    }
                    return isoline.lineList2D;
                } else if (endR == row && endC == col && endH == isH) {
                    List<Point2D.Double> ptList = new ArrayList<Point2D.Double>();
                    for (int j = isoline.num - 1; j >= 0; j--) {
                        ptList.add(isoline.lineList2D.get(j));
                    }
                    if (isFindLine) {
                        nextIsoptOnLine = new Isopoint();
                        nextIsoptOnLine.setAll(startR, startC, startH);
                        fillLineDirectionList.add(-1);
                        fillLineIndexList.add(isoline.indexProc);
                        fillLineValList.add(isoline.val);
                        onlineIndex = isoline.num - 1;
                    }
                    return ptList;
                } else {
                    continue;
                }
            }
        } else if (row1 == col2 && col1 == col2) {
            if (logger.isDebugEnabled())
                logger.debug("为同一个点");
            return null;
        }

        return null;
    }

    /**
     * 当前点是否为null
     *
     * @param pt
     * @return
     */
    private boolean isPtNull(Isopoint pt) {
        float[][] gridData = gridDataList.get(0).getGridData();
        int r = pt.getRow();
        int c = pt.getCol();
        if (gridData[r][c] == NULLVAL) {
            return true;
        }
        return false;
    }

    /**
     * 获取当前点的值
     *
     * @param pt
     * @return
     */
    private float getPtVal(Isopoint pt) {
        float[][] gridData = gridDataList.get(0).getGridData();
        int r = pt.getRow();
        int c = pt.getCol();
        return gridData[r][c];
    }

    private int getIsoptIndex(Isopoint pt1, Isopoint pt2, float curVal, float sLevel, float eLevel, boolean isNull) {
        float minVal = Math.min(sLevel, eLevel);
        float maxVal = Math.max(sLevel, eLevel);

        if (isNull) {
            boolean isPt1Null = isPtNull(pt1);
            boolean isPt2Null = isPtNull(pt2);
            if (isPt1Null && isPt2Null) {
                return 0;
            } else if (isPt1Null && !isPt2Null) {
                return 2;
            } else if (!isPt1Null && isPt2Null) {
                return 1;
            } else {
                int num1 = getEdgeNum(pt1);
                int num2 = getEdgeNum(pt2);
                if (num1 > num2) {
                    return 1;
                } else if (num1 < num2) {
                    return 2;
                } else {
                    float v1 = getPtVal(pt1);
                    float v2 = getPtVal(pt2);
                    if (!isBetweenTwoVals(v1, sLevel, eLevel) || !isBetweenTwoVals(v2, sLevel, eLevel)) {
                        if (logger.isDebugEnabled())
                            logger.debug("两个点都没有在填充范围内,必存在一条等值线近平行的等值线");
                        return -1;
                    }
                    if (curVal == minVal) {
                        if (v1 > v2) {
                            return 1;
                        } else if (v1 < v2) {
                            return 2;
                        } else {
                            return 0;
                        }
                    } else if (curVal == maxVal) {
                        if (v1 < v2) {
                            return 1;
                        } else if (v1 > v2) {
                            return 2;
                        } else {
                            return 0;
                        }
                    } else {
                        return -1;
                    }
                }
            }

        } else {
            int num1 = getEdgeNum(pt1);
            int num2 = getEdgeNum(pt2);
            if (num1 > num2) {
                return 1;
            } else if (num1 < num2) {
                return 2;
            } else {
                float v1 = getPtVal(pt1);
                float v2 = getPtVal(pt2);
                if (!isBetweenTwoVals(v1, sLevel, eLevel) || !isBetweenTwoVals(v2, sLevel, eLevel)) {
                    if (logger.isDebugEnabled())
                        logger.debug("两个点都没有在填充范围内,必存在一条等值线近平行的等值线");
                    return -1;
                }
                if (curVal == minVal) {
                    if (v1 > v2) {
                        return 1;
                    } else if (v1 < v2) {
                        return 2;
                    } else {
                        return 0;
                    }
                } else if (curVal == maxVal) {
                    if (v1 < v2) {
                        return 1;
                    } else if (v1 > v2) {
                        return 2;
                    } else {
                        return 0;
                    }
                } else {
                    return -1;
                }
            }
        }
    }

    //翻转等值线
    private IsolineDataProc reverseIsoline(IsolineDataProc isoline) {
        IsolineDataProc newIsoline = new IsolineDataProc();
        int num = isoline.num;
        newIsoline.num = num;
        newIsoline.val = isoline.val;
        newIsoline.index = isoline.index;
        newIsoline.indexProc = isoline.indexProc;
        newIsoline.isClosed = isoline.isClosed;
        newIsoline.maxX = isoline.maxX;
        newIsoline.minX = isoline.minX;
        newIsoline.maxY = isoline.maxY;
        newIsoline.minY = isoline.minY;
        Isopoint curPt = new Isopoint();
        curPt.setAll(isoline.rowsList.get(num - 1), isoline.colsList.get(num - 1), isoline.isHorizonList.get(num - 1));
        newIsoline.startLineType = getLineType(curPt);

        newIsoline.colsList = new ArrayList<Integer>();
        newIsoline.rowsList = new ArrayList<Integer>();
        newIsoline.isHorizonList = new ArrayList<Boolean>();
        newIsoline.lineList2D = new ArrayList<Point2D.Double>();
        newIsoline.isopointList = new ArrayList<Isopoint>();

        for (int i = num - 1; i >= 0; i--) {
            newIsoline.colsList.add(isoline.colsList.get(i));
            newIsoline.rowsList.add(isoline.rowsList.get(i));
            newIsoline.isHorizonList.add(isoline.isHorizonList.get(i));
            newIsoline.lineList2D.add(isoline.lineList2D.get(i));
            newIsoline.isopointList.add(isoline.isopointList.get(i));
        }
        return newIsoline;
    }

    //一个方法写了900行而且里面很多结构相似   应该可以重构 db_x 2013-10-29
    private IsolineDataProc getIsolineAfterProcess(IsolineDataProc isoline, float sLevel, float eLevel) {
        int num = isoline.num;
        if (num != 2) {
            if (logger.isDebugEnabled())
                logger.debug("不符合条件，不必处理");
            return isoline;
        }
        int sR = isoline.rowsList.get(0);
        int sC = isoline.colsList.get(0);
        Boolean sH = isoline.isHorizonList.get(0);

        int eR = isoline.rowsList.get(1);
        int eC = isoline.colsList.get(1);
        Boolean eH = isoline.isHorizonList.get(1);

        float[][] gridData = gridDataList.get(0).getGridData();

        isolineReverse.put(isoline.indexProc, false);
        //一个点，两个点，不存在符合条件的点

        if (sR == eR && sC == eC && sH == false && eH == true) {
//		if(sR==eR && sC==eC && sH==false){
            if (isBetweenTwoVals(gridData[sR][sC], sLevel, eLevel)) {
                if (sC - 1 >= 0 && sR - 1 >= 0) {
                    Isopoint pt1 = new Isopoint();
                    pt1.setAll(sR, sC - 1, null);
                    Isopoint pt2 = new Isopoint();
                    pt2.setAll(sR - 1, sC, null);
                    int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, true);
                    if (index == 1) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else if (index == 2) {
                        return isoline;
                    } else {
                        return isoline;//任意
                    }
                } else if (sC - 1 >= 0 && sR == 0) {
                    return isoline;
                } else if (sC == 0 && sR - 1 >= 0) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else {
                    return isoline;
                }
            } else if (isBetweenTwoVals(gridData[sR + 1][sC], sLevel, eLevel) && isBetweenTwoVals(gridData[eR][eC + 1], sLevel, eLevel)) {
                if (sR + 1 < gridRows - 1 && sC + 1 < gridCols - 1) {
                    //两个点
                    Isopoint pt1 = new Isopoint();
                    pt1.setAll(eR, eC + 1, null);
                    Isopoint pt2 = new Isopoint();
                    pt2.setAll(sR + 1, sC, null);
                    int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                    if (index == 1) {
                        return isoline;
                    } else if (index == 2) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else {
                        return isoline;//任意，可重新考虑
                    }
                } else if (sR + 1 == gridRows - 1 && sC + 1 < gridCols - 1) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else if (sC + 1 == gridCols - 1 && sR + 1 < gridRows - 1) {
                    return isoline;
                } else {
                    return isoline;//任意，可重新考虑
                }
            } else if (isBetweenTwoVals(gridData[sR + 1][sC], sLevel, eLevel) && !isBetweenTwoVals(gridData[eR][eC + 1], sLevel, eLevel)) {
                isolineReverse.put(isoline.indexProc, true);
                return reverseIsoline(isoline);
            } else if (!isBetweenTwoVals(gridData[sR + 1][sC], sLevel, eLevel) && isBetweenTwoVals(gridData[eR][eC + 1], sLevel, eLevel)) {
                return isoline;
            } else {
                //根据对角线
                if (sR + 1 <= gridRows - 1 && sC - 1 >= 0 && eR - 1 >= 0 && eC + 1 <= gridCols - 1) {
                    if (isBetweenTwoVals(gridData[sR + 1][sC - 1], sLevel, eLevel) && isBetweenTwoVals(gridData[eR - 1][eC + 1], sLevel, eLevel)) {
                        //根据边界
                        Isopoint pt1 = new Isopoint();
                        pt1.setAll(sR + 1, sC - 1, null);
                        Isopoint pt2 = new Isopoint();
                        pt2.setAll(eR - 1, eC + 1, null);
                        int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                        if (index == 1) {
                            isolineReverse.put(isoline.indexProc, true);
                            return reverseIsoline(isoline);
                        } else if (index == 2) {
                            return isoline;
                        } else {
                            return isoline;//任意，可重新考虑
                        }
                    } else if (isBetweenTwoVals(gridData[sR + 1][sC - 1], sLevel, eLevel) && !isBetweenTwoVals(gridData[eR - 1][eC + 1], sLevel, eLevel)) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else if (!isBetweenTwoVals(gridData[sR + 1][sC - 1], sLevel, eLevel) && isBetweenTwoVals(gridData[eR - 1][eC + 1], sLevel, eLevel)) {
                        return isoline;
                    } else {
                        return isoline;
                    }
                } else if (sR + 1 <= gridRows - 1 && sC - 1 >= 0 && eR == 0) {
                    if (isBetweenTwoVals(gridData[sR + 1][sC - 1], sLevel, eLevel)) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else {
                        return isoline;
                    }
                } else if (sC == 0 && eR - 1 >= 0 && eC + 1 <= gridCols - 1) {
//					if(isBetweenTwoVals(gridData[sR-1][sC+1], sLevel, eLevel)){
                    return isoline;
//					}else{
//
//					}
                } else {
                    return isoline;
                }
            }
        } else if (sR == eR && sC == eC && sH == true && eH == false) {
            if (isBetweenTwoVals(gridData[sR][sC], sLevel, eLevel)) {
                if (sC - 1 >= 0 && sR - 1 >= 0) {
                    Isopoint pt1 = new Isopoint();
                    pt1.setAll(sR, sC - 1, null);
                    Isopoint pt2 = new Isopoint();
                    pt2.setAll(sR - 1, sC, null);
                    int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, true);
                    if (index == 1) {
                        return isoline;
                    } else if (index == 2) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else {
                        return isoline;//任意
                    }
                } else if (sC - 1 >= 0 && sR == 0) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else if (sC == 0 && sR - 1 >= 0) {
                    return isoline;
                } else {
                    return isoline;
                }
            } else if (isBetweenTwoVals(gridData[eR + 1][eC], sLevel, eLevel) && isBetweenTwoVals(gridData[sR][sC + 1], sLevel, eLevel)) {
                if (eR + 1 < gridRows - 1 && sC + 1 < gridCols - 1) {
                    //两个点
                    Isopoint pt1 = new Isopoint();
                    pt1.setAll(sR, sC + 1, null);
                    Isopoint pt2 = new Isopoint();
                    pt2.setAll(eR + 1, eC, null);
                    int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                    if (index == 1) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else if (index == 2) {
                        return isoline;
                    } else {
                        return isoline;//任意
                    }
                } else if (eR + 1 < gridRows - 1 && sC + 1 == gridCols) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else if (eR + 1 == gridRows - 1 && sC + 1 < gridCols - 1) {
                    return isoline;
                } else {
                    return isoline;
                }
            } else if (isBetweenTwoVals(gridData[eR + 1][eC], sLevel, eLevel) && !isBetweenTwoVals(gridData[sR][sC + 1], sLevel, eLevel)) {
                return isoline;
            } else if (!isBetweenTwoVals(gridData[eR + 1][eC], sLevel, eLevel) && isBetweenTwoVals(gridData[sR][sC + 1], sLevel, eLevel)) {
                isolineReverse.put(isoline.indexProc, true);
                return reverseIsoline(isoline);
            } else {
                if (eR + 1 <= gridRows - 1 && eC - 1 >= 0 && sR - 1 >= 0 && sC + 1 <= gridCols - 1) {
                    if (isBetweenTwoVals(gridData[eR + 1][eC - 1], sLevel, eLevel) && isBetweenTwoVals(gridData[sR - 1][sC + 1], sLevel, eLevel)) {
                        //根据边界
                        Isopoint pt1 = new Isopoint();
                        pt1.setAll(eR + 1, eC - 1, null);
                        Isopoint pt2 = new Isopoint();
                        pt2.setAll(sR - 1, sC + 1, null);
                        int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                        if (index == 1) {
                            return isoline;
                        } else if (index == 2) {
                            isolineReverse.put(isoline.indexProc, true);
                            return reverseIsoline(isoline);
                        } else {
                            return isoline;//任意，可重新考虑
                        }
                    } else if (isBetweenTwoVals(gridData[eR + 1][eC - 1], sLevel, eLevel) && !isBetweenTwoVals(gridData[sR - 1][sC + 1], sLevel, eLevel)) {
                        return isoline;
                    } else if (!isBetweenTwoVals(gridData[eR + 1][eC - 1], sLevel, eLevel) && isBetweenTwoVals(gridData[sR - 1][sC + 1], sLevel, eLevel)) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else {
                        return isoline;
                    }
                } else if (eR + 1 <= gridRows - 1 && eC - 1 >= 0 && sR == 0) {
//					if(isBetweenTwoVals(gridData[eR+1][eC-1], sLevel, eLevel)){
                    return isoline;
//					}else{
//
//					}
                } else if (eC == 0 && sR - 1 >= 0 && sC + 1 <= gridCols - 1) {
                    if (isBetweenTwoVals(gridData[sR - 1][sC + 1], sLevel, eLevel)) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else {
                        return isoline;
                    }
                } else {
                    return isoline;
                }
            }
        } else if (sR == eR && sC == eC - 1 && eH == false && sH == true) {
            if (isBetweenTwoVals(gridData[eR][eC], sLevel, eLevel)) {
                if (eR - 1 >= 0 && eC + 1 <= gridCols - 1) {
                    Isopoint pt1 = new Isopoint();
                    pt1.setAll(eR, eC + 1, null);
                    Isopoint pt2 = new Isopoint();
                    pt2.setAll(eR - 1, eC, null);
                    int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, true);
                    if (index == 1) {
                        return isoline;
                    } else if (index == 2) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else {
                        return isoline;//任意
                    }
                } else if (eR - 1 >= 0 && eC == gridCols - 1) {
                    return isoline;
                } else if (eR == 0 && eC + 1 <= gridCols - 1) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else {
                    return isoline;//任意
                }
            } else if (isBetweenTwoVals(gridData[eR + 1][eC], sLevel, eLevel) && isBetweenTwoVals(gridData[sR][sC], sLevel, eLevel)) {
                if (eR + 1 < gridRows - 1 && sR > 0) {
                    Isopoint pt1 = new Isopoint();
                    pt1.setAll(eR + 1, eC, null);
                    Isopoint pt2 = new Isopoint();
                    pt2.setAll(sR, sC, null);
                    int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                    if (index == 1) {
                        return isoline;
                    } else if (index == 2) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else {
                        return isoline;//任意
                    }
                } else if (eR + 1 < gridRows - 1 && sR == 0) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else if (eR + 1 == gridRows - 1 && sR > 0) {
                    return isoline;
                } else {
                    return isoline;
                }
            } else if (isBetweenTwoVals(gridData[eR + 1][eC], sLevel, eLevel) && !isBetweenTwoVals(gridData[sR][sC], sLevel, eLevel)) {
                return isoline;
            } else if (!isBetweenTwoVals(gridData[eR + 1][eC], sLevel, eLevel) && isBetweenTwoVals(gridData[sR][sC], sLevel, eLevel)) {
                isolineReverse.put(isoline.indexProc, true);
                return reverseIsoline(isoline);
            } else {
                if (sR - 1 >= 0 && sC >= 0 && eR + 1 <= gridRows - 1 && eC + 1 <= gridCols - 1) {
                    if (isBetweenTwoVals(gridData[sR - 1][sC], sLevel, eLevel) && isBetweenTwoVals(gridData[eR + 1][eC + 1], sLevel, eLevel)) {
                        //根据边界
                        Isopoint pt1 = new Isopoint();
                        pt1.setAll(eR + 1, eC + 1, null);
                        Isopoint pt2 = new Isopoint();
                        pt2.setAll(sR - 1, sC, null);
                        int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                        if (index == 1) {
                            return isoline;
                        } else if (index == 2) {
                            isolineReverse.put(isoline.indexProc, true);
                            return reverseIsoline(isoline);
                        } else {
                            return isoline;//任意，可重新考虑
                        }
                    } else if (isBetweenTwoVals(gridData[sR - 1][sC], sLevel, eLevel) && !isBetweenTwoVals(gridData[eR + 1][eC + 1], sLevel, eLevel)) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else if (!isBetweenTwoVals(gridData[sR - 1][sC], sLevel, eLevel) && isBetweenTwoVals(gridData[eR + 1][eC + 1], sLevel, eLevel)) {
                        return isoline;
                    } else {
                        return isoline;
                    }
                } else if (sR == 0 && eR + 1 <= gridRows - 1 && eC + 1 <= gridCols - 1) {
                    return isoline;
                } else if (eC == gridCols - 1 && sR - 1 >= 0) {
                    if (isBetweenTwoVals(gridData[sR - 1][sC], sLevel, eLevel)) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else {
                        return isoline;
                    }
                } else {
                    return isoline;
                }
            }
        } else if (sR == eR && sC == eC + 1 && sH == false && eH == true) {
            if (isBetweenTwoVals(gridData[sR][sC], sLevel, eLevel)) {
                if (sR - 1 >= 0 && sC + 1 <= gridCols - 1) {
                    Isopoint pt1 = new Isopoint();
                    pt1.setAll(sR, sC + 1, null);
                    Isopoint pt2 = new Isopoint();
                    pt2.setAll(sR - 1, sC, null);
                    int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, true);
                    if (index == 1) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else if (index == 2) {
                        return isoline;
                    } else {
                        return isoline;//任意
                    }
                } else if (sR - 1 >= 0 && sC == gridCols - 1) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else if (sR == 0 && sC + 1 <= gridCols - 1) {
                    return isoline;
                } else {
                    return isoline;//任意
                }
            } else if (isBetweenTwoVals(gridData[eR][eC], sLevel, eLevel) && isBetweenTwoVals(gridData[sR + 1][sC], sLevel, eLevel)) {
                if (eC > 0 && sR + 1 < gridRows - 1) {
                    //两个点
                    Isopoint pt1 = new Isopoint();
                    pt1.setAll(eR, eC, null);
                    Isopoint pt2 = new Isopoint();
                    pt2.setAll(sR + 1, sC, null);
                    int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                    if (index == 1) {
                        return isoline;
                    } else if (index == 2) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else {
                        return isoline;//任意
                    }
                } else if (eC == 0 && sR + 1 < gridRows - 1) {
                    return isoline;
                } else if (eC > 0 && sR + 1 == gridRows - 1) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else {
                    return isoline;
                }
            } else if (isBetweenTwoVals(gridData[eR][eC], sLevel, eLevel) && !isBetweenTwoVals(gridData[sR + 1][sC], sLevel, eLevel)) {
                return isoline;
            } else if (!isBetweenTwoVals(gridData[eR][eC], sLevel, eLevel) && isBetweenTwoVals(gridData[sR + 1][sC], sLevel, eLevel)) {
                isolineReverse.put(isoline.indexProc, true);
                return reverseIsoline(isoline);
            } else {
                if (eR - 1 >= 0 && eC >= 0 && sR + 1 <= gridRows - 1 && sC + 1 <= gridCols - 1) {
                    if (isBetweenTwoVals(gridData[eR - 1][eC], sLevel, eLevel) && isBetweenTwoVals(gridData[sR + 1][sC + 1], sLevel, eLevel)) {
                        Isopoint pt1 = new Isopoint();
                        pt1.setAll(eR - 1, eC, null);
                        Isopoint pt2 = new Isopoint();
                        pt2.setAll(sR + 1, sC + 1, null);
                        int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                        if (index == 1) {
                            return isoline;
                        } else if (index == 2) {
                            isolineReverse.put(isoline.indexProc, true);
                            return reverseIsoline(isoline);
                        } else {
                            return isoline;
                        }
                    } else if (isBetweenTwoVals(gridData[eR - 1][eC], sLevel, eLevel) && !isBetweenTwoVals(gridData[sR + 1][sC + 1], sLevel, eLevel)) {
                        return isoline;
                    } else if (!isBetweenTwoVals(gridData[eR - 1][eC], sLevel, eLevel) && isBetweenTwoVals(gridData[sR + 1][sC + 1], sLevel, eLevel)) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else {
                        return isoline;
                    }
                } else if (eC == 0 && sR + 1 <= gridRows - 1 && sC + 1 <= gridCols - 1) {
                    return isoline;
                } else if (eR - 1 >= 0 && sC == gridCols - 1) {
                    if (isBetweenTwoVals(gridData[sR + 1][sC + 1], sLevel, eLevel)) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else {
                        return isoline;
                    }
                } else {
                    return isoline;
                }
            }
        } else if (sR == eR - 1 && sC == eC && eH == true && sH == false) {
            if (isBetweenTwoVals(gridData[eR][eC], sLevel, eLevel)) {
                if (eR + 1 <= gridRows - 1 && eC - 1 >= 0) {
                    Isopoint pt1 = new Isopoint();
                    pt1.setAll(eR, eC - 1, null);
                    Isopoint pt2 = new Isopoint();
                    pt2.setAll(eR + 1, eC, null);
                    int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, true);
                    if (index == 1) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else if (index == 2) {
                        return isoline;
                    } else {
                        return isoline;//任意
                    }
                } else if (eR == gridRows - 1 && eC - 1 >= 0) {
                    return isoline;
                } else if (eC == 0 && eR + 1 <= gridRows - 1) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else {
                    return isoline;//任意
                }
            } else if (isBetweenTwoVals(gridData[sR][sC], sLevel, eLevel) && isBetweenTwoVals(gridData[eR][eC + 1], sLevel, eLevel)) {
                if (sR > 0 && eC + 1 < gridRows - 1) {
                    //两个点
                    Isopoint pt1 = new Isopoint();
                    pt1.setAll(eR, eC + 1, null);
                    Isopoint pt2 = new Isopoint();
                    pt2.setAll(sR, sC, null);
                    int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                    if (index == 1) {
                        return isoline;
                    } else if (index == 2) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else {
                        return isoline;//任意
                    }
                } else if (sR == 0 && eC + 1 < gridRows - 1) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else if (sR > 0 && eC + 1 == gridRows - 1) {
                    return isoline;
                } else {
                    return isoline;
                }
            } else if (isBetweenTwoVals(gridData[sR][sC], sLevel, eLevel) && !isBetweenTwoVals(gridData[eR][eC + 1], sLevel, eLevel)) {
                isolineReverse.put(isoline.indexProc, true);
                return reverseIsoline(isoline);
            } else if (!isBetweenTwoVals(gridData[sR][sC], sLevel, eLevel) && isBetweenTwoVals(gridData[eR][eC + 1], sLevel, eLevel)) {
                return isoline;
            } else {
                if (sR >= 0 && sC - 1 >= 0 && eR + 1 <= gridRows - 1 && eC + 1 <= gridCols - 1) {
                    if (isBetweenTwoVals(gridData[sR][sC - 1], sLevel, eLevel) && isBetweenTwoVals(gridData[eR + 1][eC + 1], sLevel, eLevel)) {
                        Isopoint pt1 = new Isopoint();
                        pt1.setAll(eR + 1, eC + 1, null);
                        Isopoint pt2 = new Isopoint();
                        pt2.setAll(sR, sC - 1, null);
                        int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                        if (index == 1) {
                            return isoline;
                        } else if (index == 2) {
                            isolineReverse.put(isoline.indexProc, true);
                            return reverseIsoline(isoline);
                        } else {
                            return isoline;
                        }
                    } else if (isBetweenTwoVals(gridData[sR][sC - 1], sLevel, eLevel) && !isBetweenTwoVals(gridData[eR + 1][eC + 1], sLevel, eLevel)) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else if (!isBetweenTwoVals(gridData[sR][sC - 1], sLevel, eLevel) && isBetweenTwoVals(gridData[eR + 1][eC + 1], sLevel, eLevel)) {
                        return isoline;
                    } else {
                        return isoline;
                    }
                } else if (sC == 0 && eR + 1 < gridRows - 1 && eC + 1 <= gridCols - 1) {
                    return isoline;
                } else if (sC - 1 >= 0 && eR == gridRows - 1) {
                    if (isBetweenTwoVals(gridData[sR][sC - 1], sLevel, eLevel)) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else {
                        return isoline;
                    }
                } else {
                    return isoline;
                }
            }
        } else if (sR == eR + 1 && sC == eC && sH == true && eH == false) {
            if (isBetweenTwoVals(gridData[sR][sC], sLevel, eLevel)) {
                if (sR + 1 <= gridRows - 1 && sC - 1 >= 0) {
                    Isopoint pt1 = new Isopoint();
                    pt1.setAll(sR, sC - 1, null);
                    Isopoint pt2 = new Isopoint();
                    pt2.setAll(sR + 1, sC, null);
                    int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, true);
                    if (index == 1) {
                        return isoline;
                    } else if (index == 2) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else {
                        return isoline;//任意
                    }
                } else if (sR == gridRows - 1 && sC - 1 >= 0) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else if (sC == 0 && sR + 1 <= gridRows - 1) {
                    return isoline;
                } else {
                    return isoline;//任意
                }
            } else if (isBetweenTwoVals(gridData[sR][sC + 1], sLevel, eLevel) && isBetweenTwoVals(gridData[eR][eC], sLevel, eLevel)) {
                if (sC + 1 < gridCols - 1 && eR > 0) {
                    //两个点
                    Isopoint pt1 = new Isopoint();
                    pt1.setAll(sR, sC + 1, null);
                    Isopoint pt2 = new Isopoint();
                    pt2.setAll(eR, eC, null);
                    int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                    if (index == 1) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else if (index == 2) {
                        return isoline;
                    } else {
                        return isoline;//任意
                    }
                } else if (sC + 1 == gridCols - 1 && eR > 0) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else if (sC + 1 < gridCols - 1 && eR == 0) {
                    return isoline;
                } else {
                    return isoline;
                }
            } else if (isBetweenTwoVals(gridData[sR][sC + 1], sLevel, eLevel) && !isBetweenTwoVals(gridData[eR][eC], sLevel, eLevel)) {
                isolineReverse.put(isoline.indexProc, true);
                return reverseIsoline(isoline);
            } else if (!isBetweenTwoVals(gridData[sR][sC + 1], sLevel, eLevel) && isBetweenTwoVals(gridData[eR][eC], sLevel, eLevel)) {
                return isoline;
            } else {
                if (eC - 1 >= 0 && sR + 1 <= gridRows - 1 && sC + 1 <= gridCols - 1) {
                    if (isBetweenTwoVals(gridData[eR][eC - 1], sLevel, eLevel) && isBetweenTwoVals(gridData[sR + 1][sC + 1], sLevel, eLevel)) {
                        Isopoint pt1 = new Isopoint();
                        pt1.setAll(eR, eC - 1, null);
                        Isopoint pt2 = new Isopoint();
                        pt2.setAll(sR + 1, sC + 1, null);
                        int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                        if (index == 1) {
                            return isoline;
                        } else if (index == 2) {
                            isolineReverse.put(isoline.indexProc, true);
                            return reverseIsoline(isoline);
                        } else {
                            return isoline;
                        }
                    } else if (isBetweenTwoVals(gridData[eR][eC - 1], sLevel, eLevel) && !isBetweenTwoVals(gridData[sR + 1][sC + 1], sLevel, eLevel)) {
                        return isoline;
                    } else if (!isBetweenTwoVals(gridData[eR][eC - 1], sLevel, eLevel) && isBetweenTwoVals(gridData[sR + 1][sC + 1], sLevel, eLevel)) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else {
                        return isoline;
                    }
                } else if (eC == 0 && sR + 1 <= gridRows - 1 && sC + 1 <= gridCols - 1) {
                    if (isBetweenTwoVals(gridData[sR + 1][sC + 1], sLevel, eLevel)) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else {
                        return isoline;
                    }
                } else if (eC - 1 >= 0 && sR == gridRows - 1) {
                    return isoline;
                } else {
                    return isoline;
                }
            }
        } else if (sR == eR - 1 && sC == eC + 1 && sH == false && eH == true) {
            if (isBetweenTwoVals(gridData[eR][sC], sLevel, eLevel)) {
                if (eR + 1 <= gridRows - 1 && sC + 1 <= gridCols - 1) {
                    Isopoint pt1 = new Isopoint();
                    pt1.setAll(eR, sC + 1, null);
                    Isopoint pt2 = new Isopoint();
                    pt2.setAll(eR + 1, sC, null);
                    int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, true);
                    if (index == 1) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else if (index == 2) {
                        return isoline;
                    } else {
                        return isoline;//任意
                    }
                } else if (eR == gridRows - 1 && sC + 1 <= gridCols - 1) {
                    return isoline;
                } else if (sC == 0 && eR + 1 <= gridRows - 1) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else {
                    return isoline;//任意
                }
            } else if (isBetweenTwoVals(gridData[eR][eC], sLevel, eLevel) && isBetweenTwoVals(gridData[sR][sC], sLevel, eLevel)) {
                if (eC > 0 && sR > 0) {
                    //两个点
                    Isopoint pt1 = new Isopoint();
                    pt1.setAll(eR, eC, null);
                    Isopoint pt2 = new Isopoint();
                    pt2.setAll(sR, sC, null);
                    int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                    if (index == 1) {
                        return isoline;
                    } else if (index == 2) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else {
                        return isoline;//任意
                    }
                } else if (eC == 0 && sR > 0) {
                    return isoline;
                } else if (eC > 0 && sR == 0) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else {
                    return isoline;
                }
            } else if (isBetweenTwoVals(gridData[eR][eC], sLevel, eLevel) && !isBetweenTwoVals(gridData[sR][sC], sLevel, eLevel)) {
                return isoline;
            } else if (!isBetweenTwoVals(gridData[eR][eC], sLevel, eLevel) && isBetweenTwoVals(gridData[sR][sC], sLevel, eLevel)) {
                isolineReverse.put(isoline.indexProc, true);
                return reverseIsoline(isoline);
            } else {
                if (eR + 1 <= gridRows - 1 && eC - 1 >= 0 && sC + 1 <= gridCols - 1 && sR >= 0) {
                    if (isBetweenTwoVals(gridData[eR + 1][eC - 1], sLevel, eLevel) && isBetweenTwoVals(gridData[sR][sC + 1], sLevel, eLevel)) {
                        Isopoint pt1 = new Isopoint();
                        pt1.setAll(eR + 1, eC - 1, null);
                        Isopoint pt2 = new Isopoint();
                        pt2.setAll(sR, sC + 1, null);
                        int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                        if (index == 1) {
                            return isoline;
                        } else if (index == 2) {
                            isolineReverse.put(isoline.indexProc, true);
                            return reverseIsoline(isoline);
                        } else {
                            return isoline;
                        }
                    } else if (isBetweenTwoVals(gridData[eR + 1][eC - 1], sLevel, eLevel) && !isBetweenTwoVals(gridData[sR][sC + 1], sLevel, eLevel)) {
                        return isoline;
                    } else if (!isBetweenTwoVals(gridData[eR + 1][eC - 1], sLevel, eLevel) && isBetweenTwoVals(gridData[sR][sC + 1], sLevel, eLevel)) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else {
                        return isoline;
                    }
                } else if (eR == gridRows - 1 && sC + 1 <= gridCols - 1) {
                    if (isBetweenTwoVals(gridData[sR][sC + 1], sLevel, eLevel)) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else {
                        return isoline;
                    }
                } else if (sC == gridCols - 1 && eR + 1 <= gridRows - 1 && eC - 1 >= 0) {
                    return isoline;
                } else {
                    return isoline;
                }
            }
        } else if (sR == eR + 1 && sC == eC - 1 && sH == true && eH == false) {
            if (isBetweenTwoVals(gridData[eR][eC], sLevel, eLevel)) {
                if (sR + 1 <= gridRows - 1 && eC + 1 <= gridCols - 1) {
                    Isopoint pt1 = new Isopoint();
                    pt1.setAll(sR, eC + 1, null);
                    Isopoint pt2 = new Isopoint();
                    pt2.setAll(sR + 1, eC, null);
                    int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, true);
                    if (index == 1) {
                        return isoline;
                    } else if (index == 2) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else {
                        return isoline;//任意
                    }
                } else if (sR == gridRows - 1 && eC + 1 <= gridCols - 1) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else if (eC == 0 && sR + 1 <= gridRows - 1) {
                    return isoline;
                } else {
                    return isoline;//任意
                }
            } else if (isBetweenTwoVals(gridData[sR][sC], sLevel, eLevel) && isBetweenTwoVals(gridData[eR][eC], sLevel, eLevel)) {
                if (sC > 0 && eR > 0) {
                    //两个点
                    Isopoint pt1 = new Isopoint();
                    pt1.setAll(sR, sC, null);
                    Isopoint pt2 = new Isopoint();
                    pt2.setAll(eR, eC, null);
                    int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                    if (index == 1) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else if (index == 2) {
                        return isoline;
                    } else {
                        return isoline;//任意
                    }
                } else if (sC == 0 && eR > 0) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else if (sC > 0 && eR == 0) {
                    return isoline;
                } else {
                    return isoline;
                }
            } else if (isBetweenTwoVals(gridData[sR][sC], sLevel, eLevel) && !isBetweenTwoVals(gridData[eR][eC], sLevel, eLevel)) {
                isolineReverse.put(isoline.indexProc, true);
                return reverseIsoline(isoline);
            } else if (!isBetweenTwoVals(gridData[sR][sC], sLevel, eLevel) && isBetweenTwoVals(gridData[eR][eC], sLevel, eLevel)) {
                return isoline;
            } else {
                if (sR + 1 <= gridRows - 1 && sC - 1 >= 0 && eC + 1 <= gridCols - 1 && eR >= 0) {
                    if (isBetweenTwoVals(gridData[sR + 1][sC - 1], sLevel, eLevel) && isBetweenTwoVals(gridData[eR][eC + 1], sLevel, eLevel)) {
                        Isopoint pt1 = new Isopoint();
                        pt1.setAll(sR + 1, sC - 1, null);
                        Isopoint pt2 = new Isopoint();
                        pt2.setAll(eR, eC + 1, null);
                        int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                        if (index == 1) {
                            isolineReverse.put(isoline.indexProc, true);
                            return reverseIsoline(isoline);
                        } else if (index == 2) {
                            return isoline;
                        } else {
                            return isoline;
                        }
                    } else if (isBetweenTwoVals(gridData[sR + 1][sC - 1], sLevel, eLevel) && !isBetweenTwoVals(gridData[eR][eC + 1], sLevel, eLevel)) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else if (!isBetweenTwoVals(gridData[sR + 1][sC - 1], sLevel, eLevel) && isBetweenTwoVals(gridData[eR][eC + 1], sLevel, eLevel)) {
                        return isoline;
                    } else {
                        return isoline;
                    }
                } else if (sR == gridRows - 1 && eC + 1 <= gridCols - 1) {
                    if (isBetweenTwoVals(gridData[eR][eC + 1], sLevel, eLevel)) {
                        return isoline;
                    }
                } else if (eC == gridCols - 1 && sR + 1 <= gridRows - 1 && sC - 1 >= 0) {
                    if (isBetweenTwoVals(gridData[sR + 1][sC - 1], sLevel, eLevel)) {
                        isolineReverse.put(isoline.indexProc, true);
                        return reverseIsoline(isoline);
                    } else {
                        return isoline;
                    }
                } else {
                    return isoline;
                }
            }
        } else if (sR == eR && sC == eC - 1 && sH == false && eH == false) {
            if (isBetweenTwoVals(gridData[sR][sC], sLevel, eLevel) && isBetweenTwoVals(gridData[eR][eC], sLevel, eLevel)) {
                Isopoint pt1 = new Isopoint();
                pt1.setAll(sR, sC, null);
                Isopoint pt2 = new Isopoint();
                pt2.setAll(eR, eC, null);
                int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                if (index == 1) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else if (index == 2) {
                    return isoline;
                } else {
                    return isoline;//任意
                }
            } else if (isBetweenTwoVals(gridData[sR + 1][sC], sLevel, eLevel) && isBetweenTwoVals(gridData[eR + 1][eC], sLevel, eLevel)) {
                Isopoint pt1 = new Isopoint();
                pt1.setAll(sR + 1, sC, null);
                Isopoint pt2 = new Isopoint();
                pt2.setAll(eR + 1, eC, null);
                int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                if (index == 1) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else if (index == 2) {
                    return isoline;
                } else {
                    return isoline;//任意
                }
            } else {
                return null;
            }
        } else if (sR == eR && eC == sC - 1 && sH == false && eH == false) {
            if (isBetweenTwoVals(gridData[sR][sC], sLevel, eLevel) && isBetweenTwoVals(gridData[eR][eC], sLevel, eLevel)) {
                Isopoint pt1 = new Isopoint();
                pt1.setAll(eR, eC, null);
                Isopoint pt2 = new Isopoint();
                pt2.setAll(sR, sC, null);
                int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                if (index == 1) {
                    return isoline;
                } else if (index == 2) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else {
                    return isoline;//任意
                }
            } else if (isBetweenTwoVals(gridData[sR + 1][sC], sLevel, eLevel) && isBetweenTwoVals(gridData[eR + 1][eC], sLevel, eLevel)) {
                Isopoint pt1 = new Isopoint();
                pt1.setAll(eR + 1, eC, null);
                Isopoint pt2 = new Isopoint();
                pt2.setAll(sR + 1, sC, null);
                int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                if (index == 1) {
                    return isoline;
                } else if (index == 2) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else {
                    return isoline;//任意
                }
            } else {
                return null;
            }
        } else if (sR == eR + 1 && sC == eC && sH == true && eH == true) {
            if (isBetweenTwoVals(gridData[sR][sC], sLevel, eLevel) && isBetweenTwoVals(gridData[eR][eC], sLevel, eLevel)) {
                Isopoint pt1 = new Isopoint();
                pt1.setAll(sR, sC, null);
                Isopoint pt2 = new Isopoint();
                pt2.setAll(eR, eC, null);
                int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                if (index == 1) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else if (index == 2) {
                    return isoline;
                } else {
                    return isoline;//任意
                }
            } else if (isBetweenTwoVals(gridData[sR][sC + 1], sLevel, eLevel) && isBetweenTwoVals(gridData[eR][eC + 1], sLevel, eLevel)) {
                Isopoint pt1 = new Isopoint();
                pt1.setAll(sR, sC + 1, null);
                Isopoint pt2 = new Isopoint();
                pt2.setAll(eR, eC + 1, null);
                int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                if (index == 1) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else if (index == 2) {
                    return isoline;
                } else {
                    return isoline;//任意
                }
            } else {
                return null;
            }
        } else if (sR == eR - 1 && sC == eC && sH == true && eH == true) {
            if (isBetweenTwoVals(gridData[sR][sC], sLevel, eLevel) && isBetweenTwoVals(gridData[eR][eC], sLevel, eLevel)) {
                Isopoint pt1 = new Isopoint();
                pt1.setAll(sR, sC, null);
                Isopoint pt2 = new Isopoint();
                pt2.setAll(eR, eC, null);
                int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                if (index == 1) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else if (index == 2) {
                    return isoline;
                } else {
                    return isoline;//任意
                }
            } else if (isBetweenTwoVals(gridData[sR][sC + 1], sLevel, eLevel) && isBetweenTwoVals(gridData[eR][eC + 1], sLevel, eLevel)) {
                Isopoint pt1 = new Isopoint();
                pt1.setAll(sR, sC + 1, null);
                Isopoint pt2 = new Isopoint();
                pt2.setAll(eR, eC + 1, null);
                int index = getIsoptIndex(pt1, pt2, isoline.val, sLevel, eLevel, false);
                if (index == 1) {
                    isolineReverse.put(isoline.indexProc, true);
                    return reverseIsoline(isoline);
                } else if (index == 2) {
                    return isoline;
                } else {
                    return isoline;//任意
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
        return isoline;
    }

    private Map<Integer, Boolean> isolineReverse = new HashMap<Integer, Boolean>();
    private List<Integer> fillLineDirectionList = null;//正向从起点开始为1；反向从终点开始为-1。
    private List<Integer> fillLineIndexList = null;
    private List<Float> fillLineValList = null;
    private List<Isopoint> fillIsopointList = null;
    private List<Isopoint> fillIsopointListBak = null;
    private List<Isopoint> level2IsopointList = null;
    private int onlineIndex = -1;
    private boolean isTwoPt = false;
    private List<Integer> backFillLineIndexList = null;

    //一个方法写了1300行 试着看能不能重构 db_x
    private void polygonListBuild(float sLevel, float eLevel) {
        float outerElevation = 10;
        // 1.找到所有值为sLevel和eLevel的等值线
        List<IsolineDataProc> sIsolineListTmp = new ArrayList<IsolineDataProc>();
        List<IsolineDataProc> eIsolineListTmp = new ArrayList<IsolineDataProc>();
        List<IsolineDataProc> allIsolineListTmp = new ArrayList<IsolineDataProc>();
        List<HYIsolinePolygonProc> isolinePolyProcList = new ArrayList<HYIsolinePolygonProc>();
        List<Integer> allIndexList = new ArrayList<Integer>();
        List<Integer> allFillIndexList = new ArrayList<Integer>();

        int allSize = procIsolineDataList.size();

        for (int i = 0; i < allSize; i++) {
            IsolineDataProc tmpIsoline = procIsolineDataList.get(i);
            if (tmpIsoline.num > 1) {
                if (Math.abs(tmpIsoline.val - sLevel) < precision) {
                    sIsolineListTmp.add(tmpIsoline);
                    allIndexList.add(tmpIsoline.indexProc);
                    allIsolineListTmp.add(tmpIsoline);
                }
                if (Math.abs(tmpIsoline.val - eLevel) < precision) {
                    eIsolineListTmp.add(tmpIsoline);
                    allIndexList.add(tmpIsoline.indexProc);
                    allIsolineListTmp.add(tmpIsoline);
                }
            }
        }

        int sizeS = sIsolineListTmp.size();
        int sizeE = eIsolineListTmp.size();
        if (sizeS < 1 && sizeE < 1) {
            if (logger.isDebugEnabled())
                logger.debug("In polygonListBuild() 无符合条件的等值线");
            return;
        }

        level2IsopointList = new ArrayList<Isopoint>();
        fillLineDirectionList = new ArrayList<Integer>();
        fillLineIndexList = new ArrayList<Integer>();
        fillLineValList = new ArrayList<Float>();
        fillIsopointList = new ArrayList<Isopoint>();
        fillIsopointListBak = new ArrayList<Isopoint>();
        backFillLineIndexList = new ArrayList<Integer>();
        isolineReverse = new HashMap<Integer, Boolean>();
        boolean isRecall = false;

        List<Integer> sClosedLineIndexList = new ArrayList<Integer>();
        List<Integer> eClosedLineIndexList = new ArrayList<Integer>();
        isTwoPt = false;

        boolean isXYCoordinate = attr.isXYCoordinate();
        float sX = 0;
        float eX = 0;
        float sY = 0;
        float eY = 0;
        if (isXYCoordinate) {
            if (realXSE == null || realYSE == null) {
                getRealXYSE();
            }
            sX = realXSE[0];
            eX = realXSE[1];
            sY = realYSE[0];
            eY = realYSE[1];
        }

        int fillNum = 0;
        for (int i = 0; i < sizeS; i++) {
            IsolineDataProc sIsolineTmp = sIsolineListTmp.get(i);
            if (allFillIndexList != null && !allFillIndexList.isEmpty()) {
                if (allFillIndexList.contains(sIsolineTmp.indexProc)) {
                    continue;
                }
            }
            if (fillLineIndexList != null && !fillLineIndexList.isEmpty()) {
                if (fillLineIndexList.contains(sIsolineTmp.indexProc)) {
                    continue;
                }
            }
            isolineReverse = new HashMap<Integer, Boolean>();
            isRecall = false;

            if (sIsolineTmp.num == 2) {
                //处理：
                //注意：翻转之后的等值线，最后连接的时候的连接点
                if (sIsolineTmp.isHorizonList.get(0) != sIsolineTmp.isHorizonList.get(1)) {
                    sIsolineTmp = getIsolineAfterProcess(sIsolineTmp, sLevel, eLevel);
                    isTwoPt = true;
                }
            }

            HYIsolinePolygonProc onePolygon = new HYIsolinePolygonProc();
            fillLineDirectionList = new ArrayList<Integer>();
            fillLineIndexList = new ArrayList<Integer>();
            fillLineValList = new ArrayList<Float>();
            fillIsopointList = new ArrayList<Isopoint>();
            fillIsopointListBak = new ArrayList<Isopoint>();
            backFillLineIndexList = new ArrayList<Integer>();

            if (!sIsolineTmp.isClosed) {
                fillLineDirectionList.add(1);
                fillLineIndexList.add(sIsolineTmp.indexProc);
                fillLineValList.add(sIsolineTmp.val);
                fillIsopointList.addAll(sIsolineTmp.isopointList);
                fillIsopointListBak.addAll(sIsolineTmp.isopointList);
                List<Isopoint> delIsopointList = new ArrayList<Isopoint>();
                List<Isopoint> delAllIsopointList = new ArrayList<Isopoint>();
                List<Point2D.Double> delTmpPtList = new ArrayList<Point2D.Double>();

                List<Integer> sColList = sIsolineTmp.colsList;
                List<Integer> sRowList = sIsolineTmp.rowsList;
                List<Boolean> sIsHorizonList = sIsolineTmp.isHorizonList;
                List<Point2D.Double> sPtList = sIsolineTmp.lineList2D;
                List<Point2D.Double> tmpPtList = new ArrayList<Point2D.Double>();
                List<Point2D.Double> tmpPtListBak = new ArrayList<Point2D.Double>();
                int sNum = sIsolineTmp.num;

                tmpPtList.addAll(sPtList);
                tmpPtListBak.addAll(sPtList);
                preIsopoint = new Isopoint(); //前一点
                curIsopoint = new Isopoint(); // 当前点
                nextIsopoint = new Isopoint(); // 后一点
                curIsopoint.setAll(sRowList.get(sNum - 1), sColList.get(sNum - 1), sIsHorizonList.get(sNum - 1));
                preIsopoint.setAll(sRowList.get(sNum - 1), sColList.get(sNum - 1), sIsHorizonList.get(sNum - 1));
                int tmpNum = 0;
                int indexNum = fillLineIndexList.size();
                IsolineDataProc sIsoline = sIsolineTmp;
                float tmpLevel = eLevel;
                boolean isError = false;
                while (true && !isError) {
                    tmpNum = tmpPtList.size();
                    /*if (tmpPtList.get(tmpNum - 1) == null) {
                        logger.info("aaaa");
                    }*/
                    if (fillLineIndexList != null || !fillLineIndexList.isEmpty()) {
                        if (indexNum != fillLineIndexList.size()) {
                            IsolineDataProc tmpLine = procIsolineDataList.get(fillLineIndexList.get(fillLineIndexList.size() - 1));
                            sIsoline = tmpLine;
                            if (tmpLine.val == eLevel) {
                                tmpLevel = sLevel;
                            } else if (tmpLine.val == sLevel) {
                                tmpLevel = eLevel;
                            } else {
                                isError = true;
                                if (logger.isDebugEnabled())
                                    logger.debug("In HYIsolineProcess.polygonListBuild()当前等值线的值不等于填充的起始值或结束值");
                                break;
                            }
                        }
                        indexNum = fillLineIndexList.size();
                    }

                    List<Point2D.Double> nextPtList = getNextPts(sIsoline, tmpPtList, tmpLevel, allIsolineListTmp, isRecall);
                    isRecall = false;
                    if (nextPtList == null) {
                        while (true) {
                            if (fillIsopointListBak != null && !fillIsopointListBak.isEmpty()) {
                                int size = fillIsopointListBak.size();
                                int curC = curIsopoint.getCol();
                                int curR = curIsopoint.getRow();
                                Boolean curH = curIsopoint.getIsHorizon();
                                int preC = preIsopoint.getCol();
                                int preR = preIsopoint.getRow();
                                Boolean preH = preIsopoint.getIsHorizon();
                                if (curC == preC && curR == preR && curH == preH) {
                                    if (fillLineIndexList != null && !fillLineIndexList.isEmpty()) {
                                        IsolineDataProc tmpLine = procIsolineDataList.get(fillLineIndexList.get(fillLineIndexList.size() - 1));
                                        fillLineDirectionList.remove(fillLineDirectionList.size() - 1);
                                        backFillLineIndexList.add(fillLineIndexList.get(fillLineIndexList.size() - 1));//前推时避免当前填充再次找到
                                        fillLineIndexList.remove(fillLineIndexList.size() - 1);
                                        fillLineValList.remove(fillLineValList.size() - 1);
                                        int tmpN = tmpLine.num;
                                        int ptN = tmpPtListBak.size() - 1;
                                        for (int j = 0; j < tmpN; j++) {
                                            size = fillIsopointListBak.size();
                                            Isopoint tmpDelPt = fillIsopointListBak.get(size - 1);
                                            delIsopointList.add(tmpDelPt);
                                            fillIsopointListBak.remove(size - 1);
                                            delTmpPtList.add(tmpPtListBak.get(ptN));
                                            tmpPtListBak.remove(ptN--);
                                        }
                                        size = fillIsopointListBak.size();
                                        if (size < 1) {
                                            if (fillLineIndexList == null || fillLineIndexList.isEmpty()) {
                                                if (delIsopointList != null && !delIsopointList.isEmpty()) {
                                                    for (int k = 0; k < delIsopointList.size(); k++) {
                                                        fillIsopointList.remove(delIsopointList.get(k));
                                                    }
                                                }
                                                if (delAllIsopointList != null && !delAllIsopointList.isEmpty()) {
                                                    for (int k = 0; k < delAllIsopointList.size(); k++) {
                                                        level2IsopointList.remove(delAllIsopointList.get(k));
                                                    }
                                                }

                                                if (delTmpPtList != null && !delTmpPtList.isEmpty()) {
                                                    for (int k = 0; k < delTmpPtList.size(); k++) {
                                                        tmpPtList.remove(delTmpPtList.get(k));
                                                    }
                                                }
                                                isError = true;
                                                if (logger.isDebugEnabled())
                                                    logger.debug("无等值线增加到fillLineIndexList中,当前填充失败");
                                                break;
                                            }
                                        }
                                        isRecall = true;
                                    } else {
                                        isError = true;
                                        if (logger.isDebugEnabled()) logger.debug("无等值线增加到fillLineIndexList中,当前填充失败");
                                        break;
                                    }
                                } else {
                                    Isopoint tmpDelPt = fillIsopointListBak.get(size - 1);
                                    delIsopointList.add(tmpDelPt);
                                    delAllIsopointList.add(tmpDelPt);

                                    fillIsopointListBak.remove(size - 1);
                                    delTmpPtList.add(tmpPtListBak.get(tmpPtListBak.size() - 1));
                                    tmpPtListBak.remove(tmpPtListBak.size() - 1);
                                }
                                size = fillIsopointListBak.size();
                                if (size >= 2) {
                                    Isopoint tmp1 = fillIsopointListBak.get(size - 1);
                                    curIsopoint.setAll(tmp1.getRow(), tmp1.getCol(), tmp1.getIsHorizon());
                                    IsolineDataProc tmpLine = procIsolineDataList.get(fillLineIndexList.get(fillLineIndexList.size() - 1));
                                    int tmpI = 0;
                                    if (fillLineDirectionList == null || fillLineDirectionList.isEmpty()) {
                                        throw new RuntimeException("fillLineDirectionList为null,无法回溯");
                                    }
                                    int tmpD = fillLineDirectionList.get(fillLineDirectionList.size() - 1);
                                    if (tmpD == -1) {
                                        tmpI = 0;
                                    } else if (tmpD == 1) {
                                        tmpI = tmpLine.num - 1;
                                    } else {
                                        if (logger.isDebugEnabled())
                                            logger.debug("fillLineDirectionList中最后一条等值线方向不明");
                                        break;
                                    }
                                    if (tmp1.getRow() == tmpLine.rowsList.get(tmpI) && tmp1.getCol() == tmpLine.colsList.get(tmpI) &&
                                            tmp1.getIsHorizon() == tmpLine.isHorizonList.get(tmpI)) {
                                        preIsopoint.setAll(tmp1.getRow(), tmp1.getCol(), tmp1.getIsHorizon());
                                        continue;
                                    } else {
                                        Isopoint tmp2 = fillIsopointListBak.get(size - 2);
                                        preIsopoint.setAll(tmp2.getRow(), tmp2.getCol(), tmp2.getIsHorizon());
                                        break;
                                    }
                                } else if (size == 1) {
                                    Isopoint tmp = fillIsopointListBak.get(size - 1);
                                    curIsopoint.setAll(tmp.getRow(), tmp.getCol(), tmp.getIsHorizon());
                                    preIsopoint.setAll(tmp.getRow(), tmp.getCol(), tmp.getIsHorizon());
                                    continue;
                                } else {
                                    isError = true;
                                    if (logger.isDebugEnabled()) logger.debug("前推出错");
                                    break;
                                }
                            }
                        }
                        continue;
                    }
                    if (tmpPtList.containsAll(nextPtList)) {
                        tmpPtList.add(nextPtList.get(0));
                        tmpPtListBak.add(nextPtList.get(0));
                        if (nextPtList.size() > 1) {
                            fillLineDirectionList.remove(fillLineDirectionList.size() - 1);
                            fillLineIndexList.remove(fillLineIndexList.size() - 1);
                            fillLineValList.remove(fillLineValList.size() - 1);
                        }
                        break;
                    }
                    tmpPtList.addAll(nextPtList);
                    tmpPtListBak.addAll(nextPtList);
                    if (tmpNum + 1 == tmpPtList.size()) {
                        if (tmpPtList.get(tmpNum).equals(tmpPtList.get(0))) {
                            break;
                        }
                        preIsopoint.setAll(curIsopoint.getRow(), curIsopoint.getCol(), curIsopoint.getIsHorizon());
                        curIsopoint.setAll(nextIsopoint.getRow(), nextIsopoint.getCol(), nextIsopoint.getIsHorizon());
                        Isopoint pt = new Isopoint();
                        pt.setAll(nextIsopoint.getRow(), nextIsopoint.getCol(), nextIsopoint.getIsHorizon());
                        fillIsopointList.add(pt);
                        fillIsopointListBak.add(pt);
                        level2IsopointList.add(pt);
                    } else {
                        if (tmpPtList.get(tmpNum).equals(tmpPtList.get(0))) {
                            break;
                        }
                        if (nextIsoptOnLine != null) {
                            int tmpR = nextIsoptOnLine.getRow();
                            int tmpC = nextIsoptOnLine.getCol();
                            Boolean tmpH = nextIsoptOnLine.getIsHorizon();
                            preIsopoint.setAll(tmpR, tmpC, tmpH);
                            curIsopoint.setAll(tmpR, tmpC, tmpH);
                            if (fillLineIndexList != null && !fillLineIndexList.isEmpty()) {
                                if (onlineIndex != -1) {
                                    IsolineDataProc tmpLine = procIsolineDataList.get(fillLineIndexList.get(fillLineIndexList.size() - 1));
                                    int tmpIndex = tmpLine.num;
                                    if (onlineIndex == 0) {
                                        fillIsopointList.addAll(tmpLine.isopointList);
                                        fillIsopointListBak.addAll(tmpLine.isopointList);
                                    } else if (onlineIndex == tmpIndex - 1) {
                                        List<Isopoint> tmpList1 = tmpLine.isopointList;
                                        for (int k = tmpList1.size() - 1; k >= 0; k--) {
                                            fillIsopointList.add(tmpList1.get(k));
                                            fillIsopointListBak.add(tmpList1.get(k));
                                        }
                                    } else {
                                        isError = true;
                                        if (logger.isDebugEnabled()) logger.debug("onlineIndex值不符合条件,出错");
                                    }
                                } else {
                                    isError = true;
                                    if (logger.isDebugEnabled()) logger.debug("onlineIndex==-1,出错");
                                }
                            }
                        } else {
                            isError = true;
                            if (logger.isDebugEnabled()) logger.debug("nextIsoptOnline为null,出错");
                        }
                    }
                    if (tmpPtList.get(tmpPtList.size() - 1).equals(sPtList.get(0))) {
                        break;
                    }
                }

                if (isError) {
                    if (logger.isDebugEnabled())
                        logger.debug("In IsolineProcess.polygonListBuild(" + sLevel + ", " + eLevel + ")填充出错");
                    continue;
                }

                if (fillLineIndexList == null || fillLineIndexList.isEmpty()) {
                    continue;
                }
                if (delIsopointList != null && !delIsopointList.isEmpty()) {
                    for (int k = 0; k < delIsopointList.size(); k++) {
                        fillIsopointList.remove(delIsopointList.get(k));
                    }
                }

                if (delAllIsopointList != null && !delAllIsopointList.isEmpty()) {
                    for (int k = 0; k < delAllIsopointList.size(); k++) {
                        level2IsopointList.remove(delAllIsopointList.get(k));
                    }
                }
                if (delTmpPtList != null && !delTmpPtList.isEmpty()) {
                    for (int k = 0; k < delTmpPtList.size(); k++) {
                        tmpPtList.remove(delTmpPtList.get(k));
                    }
                }

                onePolygon.index = fillNum++;
                onePolygon.onIsolineIndexList = fillLineIndexList;
                onePolygon.valList = fillLineValList;
                onePolygon.sLevel = sLevel;
                onePolygon.eLevel = eLevel;
//						onePolygon.srcSLevel = srcSLevel;
//						onePolygon.srcELevel = srcELevel;
                onePolygon.isInnerProc = false;
                onePolygon.isInnerEnd = null;
                //处理点
                onePolygon.polygon = new Polygon();
                onePolygon.positionList = new ArrayList<PositionVec>();
                float minX = Float.MAX_VALUE;
                float maxX = Float.MIN_VALUE;
                float minY = Float.MAX_VALUE;
                float maxY = Float.MIN_VALUE;
                for (int j = 0; j < tmpPtList.size(); j++) {
                    Point2D.Double pt = tmpPtList.get(j);
                    onePolygon.polygon.addPoint((int) (pt.x * epsilon1), (int) (pt.y * epsilon1));
//					onePolygon.positionList.add(PositionVec.fromDegrees(tmpPtList.get(j).y,tmpPtList.get(j).x, outerElevation));
                    PositionVec pos = PositionVec.fromDegrees(pt.y, pt.x, outerElevation);
                    if (isXYCoordinate) {
                        Point2D.Double tmpPt = IsolineUtil.mergeLonLatToXY(pt.x, pt.y, sX, eX, sY, eY,
                                attr.getXSLon(), attr.getXELon(), attr.getYSLat(), attr.getYELat());
                        pos.setXY(tmpPt.x, tmpPt.y);
                    }
                    onePolygon.positionList.add(pos);
                    //找最大最小值，做包含判断用
                    if (minX > pt.x) {
                        minX = (float) pt.x;
                    }
                    if (maxX < pt.x) {
                        maxX = (float) pt.x;
                    }

                    if (maxY < pt.y) {
                        maxY = (float) pt.y;
                    }
                    if (minY > pt.y) {
                        minY = (float) pt.y;
                    }
                }
                onePolygon.minLon = minX;
                onePolygon.maxLon = maxX;
                onePolygon.minLat = minY;
                onePolygon.maxLat = maxY;
                onePolygon.isOnLineClosed = false;
                onePolygon.isIndependently = true;

                for (int j = 0; j < fillLineIndexList.size(); j++) {
                    if (allFillIndexList != null && !allFillIndexList.isEmpty()) {
                        if (!allFillIndexList.contains(fillLineIndexList.get(j))) {
                            allFillIndexList.add(fillLineIndexList.get(j));
                        }
                    } else {
                        allFillIndexList.add(fillLineIndexList.get(j));
                    }
                }
            } else {
                //闭合
                Boolean isDown = null;
                if (sLevel > eLevel) {
                    isDown = true;
                } else if (sLevel < eLevel) {
                    isDown = false;
                } else {//相等
                    isDown = null;
                }
                Boolean tmpIsDown = isDown(sIsolineTmp);
                if (isDown == tmpIsDown) {
                    //加入
                    fillLineDirectionList.add(1);
                    fillLineIndexList.add(sIsolineTmp.indexProc);
                    fillLineValList.add(sIsolineTmp.val);
                    fillIsopointList.addAll(sIsolineTmp.isopointList);
                    fillIsopointListBak.addAll(sIsolineTmp.isopointList);

                    onePolygon.index = fillNum++;
                    onePolygon.onIsolineIndexList = fillLineIndexList;
                    onePolygon.valList = fillLineValList;
                    onePolygon.sLevel = sLevel;
                    onePolygon.eLevel = eLevel;
//							onePolygon.srcSLevel = srcSLevel;
//							onePolygon.srcELevel = srcELevel;
                    onePolygon.isInnerProc = false;
                    onePolygon.isInnerEnd = null;
                    //处理点
                    onePolygon.polygon = new Polygon();
                    onePolygon.positionList = new ArrayList<PositionVec>();
                    float minX = Float.MAX_VALUE;
                    float maxX = Float.MIN_VALUE;
                    float minY = Float.MAX_VALUE;
                    float maxY = Float.MIN_VALUE;
                    for (int j = 0; j < sIsolineTmp.lineList2D.size(); j++) {
                        Point2D.Double pt = sIsolineTmp.lineList2D.get(j);
                        onePolygon.polygon.addPoint((int) (pt.x * epsilon1), (int) (pt.y * epsilon1));
//						onePolygon.positionList.add(PositionVec.fromDegrees(sIsolineTmp.lineList2D.get(j).y, sIsolineTmp.lineList2D.get(j).x, outerElevation));

                        PositionVec pos = PositionVec.fromDegrees(pt.y, pt.x, outerElevation);
                        if (isXYCoordinate) {
                            Point2D.Double tmpPt = IsolineUtil.mergeLonLatToXY(pt.x, pt.y, sX, eX, sY, eY,
                                    attr.getXSLon(), attr.getXELon(), attr.getYSLat(), attr.getYELat());
                            pos.setXY(tmpPt.x, tmpPt.y);
                        }
                        onePolygon.positionList.add(pos);
                        //找最大最小值，做包含判断用
                        if (minX > pt.x) {
                            minX = (float) pt.x;
                        }
                        if (maxX < pt.x) {
                            maxX = (float) pt.x;
                        }

                        if (maxY < pt.y) {
                            maxY = (float) pt.y;
                        }
                        if (minY > pt.y) {
                            minY = (float) pt.y;
                        }
                    }
                    onePolygon.minLon = minX;
                    onePolygon.maxLon = maxX;
                    onePolygon.minLat = minY;
                    onePolygon.maxLat = maxY;
                    onePolygon.isOnLineClosed = true;
                    onePolygon.isIndependently = true;
                    for (int j = 0; j < fillLineIndexList.size(); j++) {
                        if (allFillIndexList != null && !allFillIndexList.isEmpty()) {
                            if (!allFillIndexList.contains(fillLineIndexList.get(j))) {
                                allFillIndexList.add(fillLineIndexList.get(j));
                            }
                        } else {
                            allFillIndexList.add(fillLineIndexList.get(j));
                        }
                    }
                } else {
//					continue;
                    onePolygon = null;
                    sClosedLineIndexList.add(i);
                }
            }
            if (onePolygon != null) {
                isolinePolyProcList.add(onePolygon);
            }
        }//endfor

        isTwoPt = false;
        //如果没有遍历所有符合条件的等值线，则从eLevel遍历
        if (allIndexList.size() != allFillIndexList.size() && sizeE > 0) {
            fillLineDirectionList = new ArrayList<Integer>();
            fillLineIndexList = new ArrayList<Integer>();
            fillLineValList = new ArrayList<Float>();
            fillIsopointList = new ArrayList<Isopoint>();
            fillIsopointListBak = new ArrayList<Isopoint>();
            backFillLineIndexList = new ArrayList<Integer>();
            isolineReverse = new HashMap<Integer, Boolean>();
            isRecall = false;

            for (int i = 0; i < sizeE; i++) {
                IsolineDataProc eIsolineTmp = eIsolineListTmp.get(i);
                if (allFillIndexList != null && !allFillIndexList.isEmpty()) {
                    if (allFillIndexList.contains(eIsolineTmp.indexProc)) {
                        continue;
                    }
                }

                if (fillLineIndexList != null && !fillLineIndexList.isEmpty()) {
                    if (fillLineIndexList.contains(eIsolineListTmp.get(i).indexProc)) {
                        continue;
                    }
                }

                isRecall = false;
                isolineReverse = new HashMap<Integer, Boolean>();
                if (eIsolineTmp.num == 2) {
//					//处理：
//					eIsolineTmp = getIsolineAfterProcess(eIsolineTmp,sLevel,eLevel);
//					isTwoPt = true;
                    if (eIsolineTmp.isHorizonList.get(0) != eIsolineTmp.isHorizonList.get(1)) {
                        eIsolineTmp = getIsolineAfterProcess(eIsolineTmp, sLevel, eLevel);
                        isTwoPt = true;
                    }
                }

                HYIsolinePolygonProc onePolygon = new HYIsolinePolygonProc();
                fillLineDirectionList = new ArrayList<Integer>();
                fillLineIndexList = new ArrayList<Integer>();
                fillLineValList = new ArrayList<Float>();
                fillIsopointList = new ArrayList<Isopoint>();
                fillIsopointListBak = new ArrayList<Isopoint>();
                backFillLineIndexList = new ArrayList<Integer>();

                if (!eIsolineTmp.isClosed) {
                    fillLineDirectionList.add(1);
                    fillLineIndexList.add(eIsolineTmp.indexProc);
                    fillLineValList.add(eIsolineTmp.val);
                    fillIsopointList.addAll(eIsolineTmp.isopointList);
                    fillIsopointListBak.addAll(eIsolineTmp.isopointList);

                    List<Isopoint> delIsopointList = new ArrayList<Isopoint>();
                    List<Isopoint> delAllIsopointList = new ArrayList<Isopoint>();
                    List<Point2D.Double> delTmpPtList = new ArrayList<Point2D.Double>();

                    List<Integer> eColList = eIsolineTmp.colsList;
                    List<Integer> eRowList = eIsolineTmp.rowsList;
                    List<Boolean> eIsHorizonList = eIsolineTmp.isHorizonList;
                    List<Point2D.Double> ePtList = eIsolineTmp.lineList2D;
                    List<Point2D.Double> tmpPtList = new ArrayList<Point2D.Double>();
                    List<Point2D.Double> tmpPtListBak = new ArrayList<Point2D.Double>();
                    int eNum = eIsolineTmp.num;

                    tmpPtList.addAll(ePtList);
                    tmpPtListBak.addAll(ePtList);
                    preIsopoint = new Isopoint(); //前一点
                    curIsopoint = new Isopoint(); // 当前点
                    nextIsopoint = new Isopoint(); // 后一点
                    curIsopoint.setAll(eRowList.get(eNum - 1), eColList.get(eNum - 1), eIsHorizonList.get(eNum - 1));
                    preIsopoint.setAll(eRowList.get(eNum - 1), eColList.get(eNum - 1), eIsHorizonList.get(eNum - 1));
                    int tmpNum = 0;
                    boolean isError = false;
                    while (true && !isError) {
                        tmpNum = tmpPtList.size();
                        if (tmpPtList.get(tmpNum - 1) == null) {
                            if (logger.isDebugEnabled())
                                logger.debug("In IsolineProcess.polygonListBuild() tmpPtList为null");
                            isError = true;
                            break;
                        }

                        List<Point2D.Double> nextPtList = getNextPts(eIsolineTmp, tmpPtList, sLevel, allIsolineListTmp, isRecall);
                        isRecall = false;
                        if (nextPtList == null) {
                            while (true) {
                                if (fillIsopointListBak != null && !fillIsopointListBak.isEmpty()) {
                                    int size = fillIsopointListBak.size();
                                    int curC = curIsopoint.getCol();
                                    int curR = curIsopoint.getRow();
                                    Boolean curH = curIsopoint.getIsHorizon();
                                    int preC = preIsopoint.getCol();
                                    int preR = preIsopoint.getRow();
                                    Boolean preH = preIsopoint.getIsHorizon();
                                    if (curC == preC && curR == preR && curH == preH) {
                                        if (fillLineIndexList != null && !fillLineIndexList.isEmpty()) {
                                            IsolineDataProc tmpLine = procIsolineDataList.get(fillLineIndexList.get(fillLineIndexList.size() - 1));
                                            fillLineDirectionList.remove(fillLineDirectionList.size() - 1);
                                            backFillLineIndexList.add(fillLineIndexList.get(fillLineIndexList.size() - 1));//前推时避免当前填充再次找到
                                            fillLineIndexList.remove(fillLineIndexList.size() - 1);
                                            fillLineValList.remove(fillLineValList.size() - 1);
                                            int tmpN = tmpLine.num;
                                            int ptN = tmpPtListBak.size() - 1;
                                            for (int j = 0; j < tmpN; j++) {
                                                size = fillIsopointListBak.size();
                                                Isopoint tmpDelPt = fillIsopointListBak.get(size - 1);
                                                delIsopointList.add(tmpDelPt);
                                                fillIsopointListBak.remove(size - 1);
                                                //													tmpPtList.remove(tmpPtList.size()-1);
                                                delTmpPtList.add(tmpPtListBak.get(ptN));
                                                tmpPtListBak.remove(ptN--);
                                            }
                                            size = fillIsopointListBak.size();
                                            if (size < 1) {
                                                if (fillLineIndexList == null || fillLineIndexList.isEmpty()) {
                                                    if (delIsopointList != null && !delIsopointList.isEmpty()) {
                                                        for (int k = 0; k < delIsopointList.size(); k++) {
                                                            fillIsopointList.remove(delIsopointList.get(k));
                                                        }
                                                    }
                                                    if (delAllIsopointList != null && !delAllIsopointList.isEmpty()) {
                                                        for (int k = 0; k < delAllIsopointList.size(); k++) {
                                                            level2IsopointList.remove(delAllIsopointList.get(k));
                                                        }
                                                    }

                                                    if (delTmpPtList != null && !delTmpPtList.isEmpty()) {
                                                        for (int k = 0; k < delTmpPtList.size(); k++) {
                                                            tmpPtList.remove(delTmpPtList.get(k));
                                                        }
                                                    }
                                                    isError = true;
                                                    if (logger.isDebugEnabled())
                                                        logger.debug("无等值线增加到fillLineIndexList中,当前填充失败");
                                                    break;
                                                }
                                            }
                                            isRecall = true;
                                        } else {
                                            isError = true;
                                            if (logger.isDebugEnabled())
                                                logger.debug("无等值线增加到fillLineIndexList中,当前填充失败");
                                            break;
                                        }
                                    } else {
                                        Isopoint tmpDelPt = fillIsopointListBak.get(size - 1);
                                        delIsopointList.add(tmpDelPt);
                                        delAllIsopointList.add(tmpDelPt);

                                        fillIsopointListBak.remove(size - 1);
                                        delTmpPtList.add(tmpPtListBak.get(tmpPtListBak.size() - 1));
                                        tmpPtListBak.remove(tmpPtListBak.size() - 1);
                                    }
                                    size = fillIsopointListBak.size();
                                    if (size >= 2) {
                                        Isopoint tmp1 = fillIsopointListBak.get(size - 1);
                                        curIsopoint.setAll(tmp1.getRow(), tmp1.getCol(), tmp1.getIsHorizon());
                                        IsolineDataProc tmpLine = procIsolineDataList.get(fillLineIndexList.get(fillLineIndexList.size() - 1));
                                        int tmpI = 0;
                                        if (fillLineDirectionList == null || fillLineDirectionList.isEmpty()) {
                                            throw new RuntimeException("fillLineDirectionList为null,无法回溯");
                                        }
                                        int tmpD = fillLineDirectionList.get(fillLineDirectionList.size() - 1);
                                        if (tmpD == -1) {
                                            tmpI = 0;
                                        } else if (tmpD == 1) {
                                            tmpI = tmpLine.num - 1;
                                        } else {
                                            if (logger.isDebugEnabled())
                                                logger.debug("fillLineDirectionList中最后一条等值线方向不明");
                                            break;
                                        }

                                        if (tmp1.getRow() == tmpLine.rowsList.get(tmpI) && tmp1.getCol() == tmpLine.colsList.get(tmpI) &&
                                                tmp1.getIsHorizon() == tmpLine.isHorizonList.get(tmpI)) {
                                            preIsopoint.setAll(tmp1.getRow(), tmp1.getCol(), tmp1.getIsHorizon());
                                            continue;
                                        } else {
                                            Isopoint tmp2 = fillIsopointListBak.get(size - 2);
                                            preIsopoint.setAll(tmp2.getRow(), tmp2.getCol(), tmp2.getIsHorizon());
                                            break;
                                        }
                                    } else if (size == 1) {
                                        Isopoint tmp = fillIsopointListBak.get(size - 1);
                                        curIsopoint.setAll(tmp.getRow(), tmp.getCol(), tmp.getIsHorizon());
                                        preIsopoint.setAll(tmp.getRow(), tmp.getCol(), tmp.getIsHorizon());
                                        continue;
                                    } else {
                                        isError = true;
                                        if (logger.isDebugEnabled()) logger.debug("前推出错");
                                        break;
                                    }
                                }
                            }
                            continue;
                        }
                        if (tmpPtList.containsAll(nextPtList)) {
                            tmpPtList.add(nextPtList.get(0));
                            tmpPtListBak.add(nextPtList.get(0));
                            if (nextPtList.size() > 1) {//如果存在多个点，说明加入的一条等值线，则应该删除
                                fillLineDirectionList.remove(fillLineDirectionList.size() - 1);
                                fillLineIndexList.remove(fillLineIndexList.size() - 1);
                                fillLineValList.remove(fillLineValList.size() - 1);
                            }
                            break;
                        }
                        tmpPtList.addAll(nextPtList);
                        tmpPtListBak.addAll(nextPtList);
                        if (tmpNum + 1 == tmpPtList.size()) {
                            if (tmpPtList.get(tmpNum).equals(tmpPtList.get(0))) {
                                break;
                            }
                            preIsopoint.setAll(curIsopoint.getRow(), curIsopoint.getCol(), curIsopoint.getIsHorizon());
                            curIsopoint.setAll(nextIsopoint.getRow(), nextIsopoint.getCol(), nextIsopoint.getIsHorizon());
                            Isopoint pt = new Isopoint();
                            pt.setAll(nextIsopoint.getRow(), nextIsopoint.getCol(), nextIsopoint.getIsHorizon());
                            fillIsopointList.add(pt);
                            fillIsopointListBak.add(pt);
                            level2IsopointList.add(pt);
                        } else {
                            if (tmpPtList.get(tmpNum).equals(tmpPtList.get(0))) {
                                break;
                            }
                            if (nextIsoptOnLine != null) {
                                int tmpR = nextIsoptOnLine.getRow();
                                int tmpC = nextIsoptOnLine.getCol();
                                Boolean tmpH = nextIsoptOnLine.getIsHorizon();
                                preIsopoint.setAll(tmpR, tmpC, tmpH);
                                curIsopoint.setAll(tmpR, tmpC, tmpH);
                                if (fillLineIndexList != null && !fillLineIndexList.isEmpty()) {
                                    if (onlineIndex != -1) {
                                        IsolineDataProc tmpLine = procIsolineDataList.get(fillLineIndexList.get(fillLineIndexList.size() - 1));
                                        int tmpIndex = tmpLine.num;
                                        if (onlineIndex == 0) {
                                            fillIsopointList.addAll(tmpLine.isopointList);
                                            fillIsopointListBak.addAll(tmpLine.isopointList);
                                        } else if (onlineIndex == tmpIndex - 1) {
                                            List<Isopoint> tmpList = tmpLine.isopointList;
                                            for (int k = tmpList.size() - 1; k >= 0; k--) {
                                                fillIsopointList.add(tmpList.get(k));
                                                fillIsopointListBak.add(tmpList.get(k));
                                            }
                                        } else {
                                            isError = true;
                                            if (logger.isDebugEnabled()) logger.debug("onlineIndex值不符合条件,出错");
                                        }
                                    } else {
                                        isError = true;
                                        if (logger.isDebugEnabled()) logger.debug("onlineIndex==-1,出错");
                                    }
                                }
                            } else {
                                isError = true;
                                if (logger.isDebugEnabled()) logger.debug("nextIsoptOnline为null,出错");
                            }
                        }
                        if (tmpPtList.get(tmpPtList.size() - 1).equals(ePtList.get(0))) {
                            break;
                        }
                    }

                    if (isError) {
                        if (logger.isDebugEnabled())
                            logger.debug("In IsolineProcess.polygonListBuild(" + sLevel + ", " + eLevel + ")填充出错");
                        continue;
                    }

                    if (fillLineIndexList == null || fillLineIndexList.isEmpty()) {
                        continue;
                    }

                    if (delIsopointList != null && !delIsopointList.isEmpty()) {
                        for (int k = 0; k < delIsopointList.size(); k++) {
                            fillIsopointList.remove(delIsopointList.get(k));
                        }
                    }
                    if (delAllIsopointList != null && !delAllIsopointList.isEmpty()) {
                        for (int k = 0; k < delAllIsopointList.size(); k++) {
                            level2IsopointList.remove(delAllIsopointList.get(k));
                        }
                    }
                    if (delTmpPtList != null && !delTmpPtList.isEmpty()) {
                        for (int k = 0; k < delTmpPtList.size(); k++) {
                            tmpPtList.remove(delTmpPtList.get(k));
                        }
                    }

                    onePolygon.index = fillNum++;
                    onePolygon.onIsolineIndexList = fillLineIndexList;
                    onePolygon.valList = fillLineValList;
                    onePolygon.sLevel = sLevel;
                    onePolygon.eLevel = eLevel;
//							onePolygon.srcSLevel = srcSLevel;
//							onePolygon.srcELevel = srcELevel;
                    onePolygon.isInnerProc = false;
                    onePolygon.isInnerEnd = null;
                    //处理点
                    onePolygon.polygon = new Polygon();
                    onePolygon.positionList = new ArrayList<PositionVec>();
                    float minX = Float.MAX_VALUE;
                    float maxX = Float.MIN_VALUE;
                    float minY = Float.MAX_VALUE;
                    float maxY = Float.MIN_VALUE;
                    for (int j = 0; j < tmpPtList.size(); j++) {
                        Point2D.Double pt = tmpPtList.get(j);
                        onePolygon.polygon.addPoint((int) (pt.x * epsilon1), (int) (pt.y * epsilon1));
//						onePolygon.positionList.add(PositionVec.fromDegrees(tmpPtList.get(j).y, tmpPtList.get(j).x, outerElevation));
                        PositionVec pos = PositionVec.fromDegrees(pt.y, pt.x, outerElevation);
                        if (isXYCoordinate) {
                            Point2D.Double tmpPt = IsolineUtil.mergeLonLatToXY(pt.x, pt.y, sX, eX, sY, eY,
                                    attr.getXSLon(), attr.getXELon(), attr.getYSLat(), attr.getYELat());
                            pos.setXY(tmpPt.x, tmpPt.y);
                        }
                        onePolygon.positionList.add(pos);
                        //找最大最小值，做包含判断用
                        if (minX > pt.x) {
                            minX = (float) pt.x;
                        }
                        if (maxX < pt.x) {
                            maxX = (float) pt.x;
                        }

                        if (maxY < pt.y) {
                            maxY = (float) pt.y;
                        }
                        if (minY > pt.y) {
                            minY = (float) pt.y;
                        }
                    }
                    onePolygon.minLon = minX;
                    onePolygon.maxLon = maxX;
                    onePolygon.minLat = minY;
                    onePolygon.maxLat = maxY;
                    onePolygon.isOnLineClosed = false;
                    onePolygon.isIndependently = true;

                    for (int j = 0; j < fillLineIndexList.size(); j++) {
                        if (allFillIndexList != null && !allFillIndexList.isEmpty()) {
                            if (!allFillIndexList.contains(fillLineIndexList.get(j))) {
                                allFillIndexList.add(fillLineIndexList.get(j));
                            }
                        } else {
                            allFillIndexList.add(fillLineIndexList.get(j));
                        }
                    }

                } else {
                    //闭合
                    Boolean isDown = null;
                    if (sLevel < eLevel) {
                        isDown = true;
                    } else if (sLevel > eLevel) {
                        isDown = false;
                    } else {//相等
                        isDown = null;
                    }
                    Boolean tmpIsDown = isDown(eIsolineTmp);
                    if (isDown == tmpIsDown) {
                        //加入
                        fillLineDirectionList.add(1);
                        fillLineIndexList.add(eIsolineTmp.indexProc);
                        fillLineValList.add(eIsolineTmp.val);
                        fillIsopointList.addAll(eIsolineTmp.isopointList);
                        fillIsopointListBak.addAll(eIsolineTmp.isopointList);

                        onePolygon.index = fillNum++;
                        onePolygon.onIsolineIndexList = fillLineIndexList;
                        onePolygon.valList = fillLineValList;
                        onePolygon.sLevel = sLevel;
                        onePolygon.eLevel = eLevel;
//								onePolygon.srcSLevel = srcSLevel;
//								onePolygon.srcELevel = srcELevel;
                        onePolygon.isInnerProc = false;
                        onePolygon.isInnerEnd = null;
                        //处理点
                        onePolygon.polygon = new Polygon();
                        onePolygon.positionList = new ArrayList<PositionVec>();
                        float minX = Float.MAX_VALUE;
                        float maxX = Float.MIN_VALUE;
                        float minY = Float.MAX_VALUE;
                        float maxY = Float.MIN_VALUE;
                        for (int j = 0; j < eIsolineTmp.lineList2D.size(); j++) {
                            Point2D.Double pt = eIsolineTmp.lineList2D.get(j);
                            onePolygon.polygon.addPoint((int) (pt.x * epsilon1), (int) (pt.y * epsilon1));
//							onePolygon.positionList.add(PositionVec.fromDegrees(eIsolineTmp.lineList2D.get(j).y, eIsolineTmp.lineList2D.get(j).x, outerElevation));
                            PositionVec pos = PositionVec.fromDegrees(pt.y, pt.x, outerElevation);
                            if (isXYCoordinate) {
                                Point2D.Double tmpPt = IsolineUtil.mergeLonLatToXY(pt.x, pt.y, sX, eX, sY, eY,
                                        attr.getXSLon(), attr.getXELon(), attr.getYSLat(), attr.getYELat());
                                pos.setXY(tmpPt.x, tmpPt.y);
                            }
                            onePolygon.positionList.add(pos);
                            //找最大最小值，做包含判断用
                            if (minX > pt.x) {
                                minX = (float) pt.x;
                            }
                            if (maxX < pt.x) {
                                maxX = (float) pt.x;
                            }

                            if (maxY < pt.y) {
                                maxY = (float) pt.y;
                            }
                            if (minY > pt.y) {
                                minY = (float) pt.y;
                            }
                        }
                        onePolygon.minLon = minX;
                        onePolygon.maxLon = maxX;
                        onePolygon.minLat = minY;
                        onePolygon.maxLat = maxY;
                        onePolygon.isOnLineClosed = true;
                        onePolygon.isIndependently = true;

                        for (int j = 0; j < fillLineIndexList.size(); j++) {
                            if (allFillIndexList != null && !allFillIndexList.isEmpty()) {
                                if (!allFillIndexList.contains(fillLineIndexList.get(j))) {
                                    allFillIndexList.add(fillLineIndexList.get(j));
                                }
                            } else {
                                allFillIndexList.add(fillLineIndexList.get(j));
                            }
                        }
                    } else {
//						continue;
                        onePolygon = null;
                        eClosedLineIndexList.add(i);
                    }
                }
                if (onePolygon != null) {
                    isolinePolyProcList.add(onePolygon);
                }
            }//endfor
        }//endif

        //单独处理闭合等值线：基本都是作为外边界，也有可能有内边界
        //前面已经处理一部分内边界和外边界
        int[] sizeSE = new int[]{sClosedLineIndexList.size(), eClosedLineIndexList.size()};
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < sizeSE[j]; i++) {
                IsolineDataProc isolineTmp = new IsolineDataProc();
                if (j == 0) {
                    isolineTmp = sIsolineListTmp.get(sClosedLineIndexList.get(i));
                } else if (j == 1) {
                    isolineTmp = eIsolineListTmp.get(eClosedLineIndexList.get(i));
                } else {
                    continue;
                }
                if (!isolineTmp.isClosed) {
                    continue;
                }
                fillLineIndexList = new ArrayList<Integer>();
                fillLineValList = new ArrayList<Float>();

                if (allFillIndexList != null && !allFillIndexList.isEmpty()) {
                    if (allFillIndexList.contains(isolineTmp.indexProc)) {
                        continue;
                    }
                }
                HYIsolinePolygonProc onePolygon = new HYIsolinePolygonProc();
                List<Integer> sColList = isolineTmp.colsList;
                List<Integer> sRowList = isolineTmp.rowsList;
                List<Boolean> sIsHorizonList = isolineTmp.isHorizonList;
                preIsopoint = new Isopoint(); //前一点
                curIsopoint = new Isopoint(); // 当前点
                Isopoint tmpNextIsopoint = new Isopoint(); // 后一点

                Boolean isHorizon = sIsHorizonList.get(0);
                int col = sColList.get(0);
                int row = sRowList.get(0);
                float[][] gridData = gridDataList.get(0).getGridData();
                int direction = 0;//1-left,2-right,3-bottom,4-top
                if (isHorizon) {//在横边
                    if (isBetweenTwoVals(gridData[row][col], sLevel, eLevel)) {
                        tmpNextIsopoint.setAll(row, col, null);
                        direction = 1;
                    } else if (isBetweenTwoVals(gridData[row][col + 1], sLevel, eLevel)) {
                        tmpNextIsopoint.setAll(row, col + 1, null);
                        direction = 2;
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug("In HYIsolineProcess.polygonListBuild()在横边上时不在填充的起止范围内");
                        continue;
                    }
                } else {//在纵边
                    if (isBetweenTwoVals(gridData[row][col], sLevel, eLevel)) {
                        tmpNextIsopoint.setAll(row, col, null);
                        direction = 3;
                    } else if (isBetweenTwoVals(gridData[row + 1][col], sLevel, eLevel)) {
                        tmpNextIsopoint.setAll(row + 1, col, null);
                        direction = 4;
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug("In HYIsolineProcess.polygonListBuild()在纵边上时不在填充的起止范围内");
                        continue;
                    }
                }
                boolean isFinish = true;
                while (true) {
                    int tmpRow = tmpNextIsopoint.getRow();
                    int tmpCol = tmpNextIsopoint.getCol();
                    if (isBetweenTwoVals(tmpRow, 0, gridRows - 1) && isBetweenTwoVals(tmpCol, 0, gridCols - 1)) {
                        if (isBetweenTwoVals(gridData[tmpRow][tmpCol], sLevel, eLevel)) {
                            if (isBoundaryEdge(tmpNextIsopoint)) {
                                break;
                            }
                            //						if(isCornerEdge(tmpNextIsopoint)){
                            //							break;
                            //						}
                        } else {
                            //出现了等值线,所以不用考虑此闭合等值线的边界了，即不用考虑多边形的外边界了 //但有可能是某个多边形的内边界
                            isFinish = false;
                            break;
                        }
                    } else {
                        isFinish = false;
                        if (logger.isDebugEnabled())
                            logger.debug("In HYIsolineProcess.polygonListBuild()行或列超出范围");
                        break;
                    }
                    switch (direction) {
                        case 1:
                            //一直向左直到遇到边界停止 此边界作为起始点开始追踪多边形
                            tmpNextIsopoint.setAll(tmpRow, tmpCol - 1, null);
                            break;
                        case 2:
                            //一直向右
                            tmpNextIsopoint.setAll(tmpRow, tmpCol + 1, null);
                            break;
                        case 3:
                            //一直向下
                            tmpNextIsopoint.setAll(tmpRow - 1, tmpCol, null);
                            break;
                        case 4:
                            //一直向上
                            tmpNextIsopoint.setAll(tmpRow + 1, tmpCol, null);
                            break;
                        default:
                            isFinish = false;
                            if (logger.isDebugEnabled())
                                logger.debug("In HYIsolineProcess().polygonListBuild()闭合向外扩时出错");
                            break;

                    }
                }
                if (isFinish) {
                    preIsopoint.setAll(tmpNextIsopoint.getRow(), tmpNextIsopoint.getCol(), null);
                    curIsopoint.setAll(tmpNextIsopoint.getRow(), tmpNextIsopoint.getCol(), null);
                    if (isAdd(curIsopoint)) {
                        if (allFillIndexList.contains(isolineTmp.indexProc)) {
                            continue;
                        }
                        fillLineIndexList.add(isolineTmp.indexProc);
                        fillLineValList.add(isolineTmp.val);

                        onePolygon.index = fillNum++;
                        onePolygon.onIsolineIndexList = fillLineIndexList;
                        onePolygon.valList = fillLineValList;
                        onePolygon.sLevel = sLevel;
                        onePolygon.eLevel = eLevel;
//								onePolygon.srcSLevel = srcSLevel;
//								onePolygon.srcELevel = srcELevel;
                        onePolygon.isInnerProc = true;
                        onePolygon.isInnerEnd = true;
                        //处理点
                        onePolygon.polygon = new Polygon();
                        onePolygon.positionList = new ArrayList<PositionVec>();
                        float minX = Float.MAX_VALUE;
                        float maxX = Float.MIN_VALUE;
                        float minY = Float.MAX_VALUE;
                        float maxY = Float.MIN_VALUE;
                        for (int k = 0; k < isolineTmp.lineList2D.size(); k++) {
                            Point2D.Double pt = isolineTmp.lineList2D.get(k);
                            onePolygon.polygon.addPoint((int) (pt.x * epsilon1), (int) (pt.y * epsilon1));
//							onePolygon.positionList.add(PositionVec.fromDegrees(isolineTmp.lineList2D.get(k).y, isolineTmp.lineList2D.get(k).x, outerElevation));
                            PositionVec pos = PositionVec.fromDegrees(isolineTmp.lineList2D.get(k).y, isolineTmp.lineList2D.get(k).x, outerElevation);
                            if (isXYCoordinate) {
                                Point2D.Double tmpPt = IsolineUtil.mergeLonLatToXY(isolineTmp.lineList2D.get(k).x, isolineTmp.lineList2D.get(k).y, sX, eX, sY, eY,
                                        attr.getXSLon(), attr.getXELon(), attr.getYSLat(), attr.getYELat());
                                pos.setXY(tmpPt.x, tmpPt.y);
                            }
                            onePolygon.positionList.add(pos);
                            //找最大最小值，做包含判断用
                            if (minX > pt.x) {
                                minX = (float) pt.x;
                            }
                            if (maxX < pt.x) {
                                maxX = (float) pt.x;
                            }

                            if (maxY < pt.y) {
                                maxY = (float) pt.y;
                            }
                            if (minY > pt.y) {
                                minY = (float) pt.y;
                            }
                        }
                        onePolygon.minLon = minX;
                        onePolygon.maxLon = maxX;
                        onePolygon.minLat = minY;
                        onePolygon.maxLat = maxY;
                        onePolygon.isOnLineClosed = true;
                        onePolygon.isIndependently = false;

                        if (allFillIndexList != null && !allFillIndexList.isEmpty()) {
                            if (!allFillIndexList.contains(isolineTmp.indexProc)) {
                                allFillIndexList.add(isolineTmp.indexProc);
                            }
                        }
                        if (onePolygon != null) {
                            isolinePolyProcList.add(onePolygon);
                        }
                        continue;
                    }
                } else {
                    if (allFillIndexList.contains(isolineTmp.indexProc)) {
                        continue;
                    }
                    fillLineIndexList.add(isolineTmp.indexProc);
                    fillLineValList.add(isolineTmp.val);

                    onePolygon.index = fillNum++;
                    onePolygon.onIsolineIndexList = fillLineIndexList;
                    onePolygon.valList = fillLineValList;
                    onePolygon.sLevel = sLevel;
                    onePolygon.eLevel = eLevel;
//							onePolygon.srcSLevel = srcSLevel;
//							onePolygon.srcELevel = srcELevel;
                    onePolygon.isInnerProc = true;
                    onePolygon.isInnerEnd = true;
                    //处理点
                    onePolygon.polygon = new Polygon();
                    onePolygon.positionList = new ArrayList<PositionVec>();
                    float minX = Float.MAX_VALUE;
                    float maxX = Float.MIN_VALUE;
                    float minY = Float.MAX_VALUE;
                    float maxY = Float.MIN_VALUE;
                    for (int k = 0; k < isolineTmp.lineList2D.size(); k++) {
                        Point2D.Double pt = isolineTmp.lineList2D.get(k);
                        onePolygon.polygon.addPoint((int) (pt.x * epsilon1), (int) (pt.y * epsilon1));
//						onePolygon.positionList.add(PositionVec.fromDegrees(isolineTmp.lineList2D.get(k).y, isolineTmp.lineList2D.get(k).x, outerElevation));
                        PositionVec pos = PositionVec.fromDegrees(isolineTmp.lineList2D.get(k).y, isolineTmp.lineList2D.get(k).x, outerElevation);
                        if (isXYCoordinate) {
                            Point2D.Double tmpPt = IsolineUtil.mergeLonLatToXY(isolineTmp.lineList2D.get(k).x, isolineTmp.lineList2D.get(k).y, sX, eX, sY, eY,
                                    attr.getXSLon(), attr.getXELon(), attr.getYSLat(), attr.getYELat());
                            pos.setXY(tmpPt.x, tmpPt.y);
                        }
                        onePolygon.positionList.add(pos);
                        //找最大最小值，做包含判断用
                        if (minX > pt.x) {
                            minX = (float) pt.x;
                        }
                        if (maxX < pt.x) {
                            maxX = (float) pt.x;
                        }

                        if (maxY < pt.y) {
                            maxY = (float) pt.y;
                        }
                        if (minY > pt.y) {
                            minY = (float) pt.y;
                        }
                    }
                    onePolygon.minLon = minX;
                    onePolygon.maxLon = maxX;
                    onePolygon.minLat = minY;
                    onePolygon.maxLat = maxY;
                    onePolygon.isOnLineClosed = true;
                    onePolygon.isIndependently = false;
                    if (allFillIndexList != null && !allFillIndexList.isEmpty()) {
                        if (!allFillIndexList.contains(isolineTmp.indexProc)) {
                            allFillIndexList.add(isolineTmp.indexProc);
                        }
                    }
                    if (onePolygon != null) {
                        isolinePolyProcList.add(onePolygon);
                    }
                    continue;
                }

                List<Point2D.Double> tmpPtList = new ArrayList<Point2D.Double>();
                tmpPtList.add(getPos(curIsopoint.getRow(), curIsopoint.getCol()));
                Isopoint startPt = new Isopoint();
                startPt.setAll(curIsopoint.getRow(), curIsopoint.getCol(), null);
                isFinish = true;

                while (true) {
                    //
                    Point2D.Double tmpPt = getNextPt(tmpPtList, sLevel, eLevel, sLevel, startPt);
                    if (tmpPt == null) {
                        if (logger.isDebugEnabled())
                            logger.debug("In HYIsolineProcess().polygonListBuild()闭合向外扩未找到下一个点，需前推");//前推
                        break;
                    }
                    if (tmpPtList.contains(tmpPt)) {
                        tmpPtList.add(tmpPt);
                        break;
                    }
                    tmpPtList.add(tmpPt);
                    preIsopoint.setAll(curIsopoint.getRow(), curIsopoint.getCol(), curIsopoint.getIsHorizon());
                    curIsopoint.setAll(nextIsopoint.getRow(), nextIsopoint.getCol(), nextIsopoint.getIsHorizon());
                }

                if (isFinish) {
                    onePolygon.index = fillNum++;
                    onePolygon.onIsolineIndexList = null;//没有在等值线上
                    onePolygon.valList = null;
                    onePolygon.sLevel = sLevel;
                    onePolygon.eLevel = eLevel;
                    //							onePolygon.srcSLevel = srcSLevel;
                    //							onePolygon.srcELevel = srcELevel;
                    onePolygon.isInnerEnd = null;
                    onePolygon.isInnerProc = false;
                    //处理点
                    onePolygon.polygon = new Polygon();
                    onePolygon.positionList = new ArrayList<PositionVec>();
                    float minX = Float.MAX_VALUE;
                    float maxX = Float.MIN_VALUE;
                    float minY = Float.MAX_VALUE;
                    float maxY = Float.MIN_VALUE;
                    for (int k = 0; k < tmpPtList.size(); k++) {
                        Point2D.Double pt = tmpPtList.get(k);
                        onePolygon.polygon.addPoint((int) (pt.x * epsilon1), (int) (pt.y * epsilon1));
//						onePolygon.positionList.add(PositionVec.fromDegrees(tmpPtList.get(k).y,tmpPtList.get(k).x, outerElevation));
                        PositionVec pos = PositionVec.fromDegrees(pt.y, pt.x, outerElevation);
                        if (isXYCoordinate) {
                            Point2D.Double tmpPt = IsolineUtil.mergeLonLatToXY(pt.x, pt.y, sX, eX, sY, eY,
                                    attr.getXSLon(), attr.getXELon(), attr.getYSLat(), attr.getYELat());
                            pos.setXY(tmpPt.x, tmpPt.y);
                        }
                        onePolygon.positionList.add(pos);
                        //找最大最小值，做包含判断用
                        if (minX > pt.x) {
                            minX = (float) pt.x;
                        }
                        if (maxX < pt.x) {
                            maxX = (float) pt.x;
                        }

                        if (maxY < pt.y) {
                            maxY = (float) pt.y;
                        }
                        if (minY > pt.y) {
                            minY = (float) pt.y;
                        }
                    }
                    onePolygon.minLon = minX;
                    onePolygon.maxLon = maxX;
                    onePolygon.minLat = minY;
                    onePolygon.maxLat = maxY;
                    onePolygon.isOnLineClosed = false;
                    onePolygon.isIndependently = true;
                } else {
                    break;
                }
                if (onePolygon != null) {
                    isolinePolyProcList.add(onePolygon);
                }
            }
        }

        //处理包含关系
        HYIsolinePolygonProc onePoly1;
        HYIsolinePolygonProc onePoly2;
        List<Integer> addInnerList = new ArrayList<Integer>();
        //单独处理闭合等值线所在的多边形
        for (int i = 0; i < isolinePolyProcList.size(); i++) {
            onePoly1 = isolinePolyProcList.get(i);
            if (!onePoly1.isOnLineClosed) {
                continue;
            }
            isolinePolyProcList.get(i).containPolyIndexList = new ArrayList<Integer>();
            for (int j = 0; j < isolinePolyProcList.size(); j++) {
                if (i == j) {
                    continue;
                }
                onePoly2 = isolinePolyProcList.get(j);
                if (!onePoly2.isOnLineClosed) {
                    continue;
                }
                boolean isContainFlag = isClosedPolygonContainClosedPolygon(onePoly1, onePoly2);
                if (isContainFlag) {
                    isolinePolyProcList.get(i).containPolyIndexList.add(onePoly2.index);
                    addInnerList.add(onePoly2.index);
                }
            }
        }

        for (int i = 0; i < isolinePolyProcList.size(); i++) {
            onePoly1 = isolinePolyProcList.get(i);
            if (onePoly1.isOnLineClosed) {
                continue;
            }
            isolinePolyProcList.get(i).containPolyIndexList = new ArrayList<Integer>();

            for (int j = 0; j < isolinePolyProcList.size(); j++) {
                if (j == i) {
                    continue;
                }
                onePoly2 = isolinePolyProcList.get(j);
                if (!onePoly2.isOnLineClosed) {
                    continue;
                }
                if (addInnerList != null && !addInnerList.isEmpty()) {
                    if (addInnerList.contains(onePoly2.index)) {
                        continue;
                    }
                }

                boolean isContainFlag = isClosedPolygonContainClosedPolygon(onePoly1, onePoly2);
                if (isContainFlag) {
                    isolinePolyProcList.get(i).containPolyIndexList.add(onePoly2.index);
                    addInnerList.add(onePoly2.index);
                }
            }
        }

        HYIsolinePolygonProc onePoly;
        if (isolinePolyProcList != null && !isolinePolyProcList.isEmpty()) {
            for (int i = 0; i < isolinePolyProcList.size(); i++) {
                onePoly = isolinePolyProcList.get(i);
                if (!onePoly.isIndependently) {
                    continue;
                }
                IsolinePolygon onePolygon = new IsolinePolygon();
                //TODO 防止多边形不闭合 db_x 2014-11-13
                if (isolinePolyProcList.get(i).positionList.size() > 2) {
                    if (!isolinePolyProcList.get(i).positionList.get(0).equals(isolinePolyProcList.get(i).positionList.get(isolinePolyProcList.get(i).positionList.size() - 1))) {
                        isolinePolyProcList.get(i).positionList.add(isolinePolyProcList.get(i).positionList.get(0));
                    }
                }
                if (isolinePolyProcList.get(i).positionList.size() < 4) {
                    continue;
                }
                onePolygon.setOuterBoundary(isolinePolyProcList.get(i).positionList);
                if (onePoly.containPolyIndexList != null && !onePoly.containPolyIndexList.isEmpty()) {
                    List<Integer> indexList = onePoly.containPolyIndexList;
                    for (int j = 0; j < indexList.size(); j++) {
                        //防止多边形不闭合 db_x 2014-11-13
                        List<PositionVec> positionList = isolinePolyProcList.get(indexList.get(j)).positionList;
                        if (positionList.size() > 2) {
                            if (!positionList.get(0).equals(positionList.get(positionList.size() - 1))) {
                                positionList.add(positionList.get(0));
                            }
                        }
                        if (positionList.size() < 4) {
                            continue;
                        }
                        onePolygon.addInnerBoundary(positionList);
                    }
                }

                onePolygon.setSELevel(sLevel, eLevel);
                onePolygon.setinitialSElevel(sLevel, eLevel);
                resPolygonList.add(onePolygon);
            }
        }
    }


    private Boolean isDown(IsolineDataProc anIsoline) {
        float curVal = anIsoline.val;
        List<Point2D.Double> pointList = anIsoline.lineList2D;
        boolean isClosed = anIsoline.isClosed;
        Boolean flag = null;
        int size = pointList.size();
        if (isClosed) {
            //构造多边形
            Polygon poly = new Polygon();
            for (int i = 0; i < size; i++) {
                poly.addPoint((int) (pointList.get(i).x * epsilon1), (int) (pointList.get(i).y * epsilon1));
            }
            for (int i = 0; i < size; i++) {
                int col = anIsoline.colsList.get(i);
                int row = anIsoline.rowsList.get(i);
                Point2D.Double pt = getPos(row, col);
                if (isPolygonContainPoint((int) (pt.x * epsilon1), (int) (pt.y * epsilon1), false, poly)) {
                    if (gridDataList.size() == 1) {
                        if (gridDataList.get(0).getGridData()[row][col] < curVal) {
                            flag = true;
                            break;
                        } else if (gridDataList.get(0).getGridData()[row][col] == curVal) {
                            if (i == size - 1 && flag == null) {
                                throw new RuntimeErrorException(null, "In IsolineProcess计算isDown()时出错");
                            }
                            continue;
                        } else {
                            flag = false;
                            break;
                        }
                    } else {
                        throw new RuntimeErrorException(null, "计算多边形downhill时因为gridDataList.size()不为1,无法取得当前等值点所在边的端点的值");
                    }
                } else {
                    if (gridDataList.size() == 1) {
                        if (gridDataList.get(0).getGridData()[row][col] < curVal) {
                            flag = false;
                            break;
                        } else if (gridDataList.get(0).getGridData()[row][col] == curVal) {
                            if (i == size - 1 && flag == null) {
                                throw new RuntimeErrorException(null, "In IsolineProcess计算isDown()时出错");
                            }
                            continue;
                        } else {
                            flag = true;
                            break;
                        }
                    } else {
                        throw new RuntimeErrorException(null, "计算多边形downhill时因为gridDataList.size()不为1,无法取得当前等值点所在边的端点的值");
                    }
                }
            }
        }
        return flag;
    }

    /**
     * 获取多边形
     *
     * @param sLevel 等值线要填充的起始level
     * @param eLevel 等值线要填充的结束level
     */
    private void polygonListBuild(float sLevel, float eLevel, float srcSLevel, float srcELevel) {
        float outerElevation = 10;
        float innerElevation = 10;
        boolean isXYCoordinate = attr.isXYCoordinate();
        float sX = 0;
        float eX = 0;
        float sY = 0;
        float eY = 0;
        if (isXYCoordinate) {
            if (realXSE == null || realYSE == null) {
                getRealXYSE();
            }
            sX = realXSE[0];
            eX = realXSE[1];
            sY = realYSE[0];
            eY = realYSE[1];
        }

        // 1.找到所有值为level多边形
        int size = polygonContainList.size();
        ArrayList<IsolinePolygonContain> polygonLevelList = new ArrayList<IsolinePolygonContain>(size);
        IsolinePolygonContain polygonContain = new IsolinePolygonContain();
        ArrayList<Integer> polyIndexAry = new ArrayList<Integer>(size);
        for (int i = 0; i < size; i++) {
            polygonContain = polygonContainList.get(i);
            if (Math.abs(polygonContain.val - sLevel) < precision) {
                polygonLevelList.add(polygonContain);
                polyIndexAry.add(polygonContain.index);
            }
        }
        //2.根据经纬度去除一部分

        boolean canFillPolygon = false;
        IsolinePolygon onePolygon;
        // 3.找到所有直接包含的值为level+delta的多边形
        if (!polygonLevelList.isEmpty()) {
            List<Integer> pgnIndexList;
            IsolinePolygonContain isolineContain3;
            List<PositionVec> posOuterList;
            List<PositionVec> posInnerList1;
            List<List<PositionVec>> posInnerList;
            List<PositionVec> posInnerList2;
            for (int i = 0; i < polygonLevelList.size(); i++) {
                canFillPolygon = false;
                if (polygonLevelList.get(i).isAddToFillPolygon) {
                    continue;
                }
                posOuterList = new ArrayList<PositionVec>();
                onePolygon = new IsolinePolygon();
                // outer:polygonLevelList.get(i).polygon
                for (int k = 0; k < polygonLevelList.get(i).polygon.npoints; k++) {
                    PositionVec pos = PositionVec.fromDegrees(polygonLevelList
                            .get(i).polygon.ypoints[k] * epsilon2, polygonLevelList
                            .get(i).polygon.xpoints[k] * epsilon2, outerElevation);
                    if (isXYCoordinate) {
                        Point2D.Double tmpPt = IsolineUtil.mergeLonLatToXY(polygonLevelList.get(i).polygon.xpoints[k] * epsilon2,
                                polygonLevelList.get(i).polygon.ypoints[k] * epsilon2, sX, eX, sY, eY,
                                attr.getXSLon(), attr.getXELon(), attr.getYSLat(), attr.getYELat());
                        pos.setXY(tmpPt.x, tmpPt.y);
                    }
                    posOuterList.add(pos);
                }// endfor
                pgnIndexList = polygonLevelList.get(i).containOnlyPgnList;
                if (pgnIndexList != null) {
                    if (!pgnIndexList.isEmpty()) {
                        posInnerList = new ArrayList<List<PositionVec>>();
                        if (eLevel > sLevel) {
                            if (polygonLevelList.get(i).isDown) {
                                continue;
                            }
                            for (int j = pgnIndexList.size() - 1; j >= 0; j--) {
                                isolineContain3 = polygonContainList.get(pgnIndexList.get(j));
                                if (isolineContain3.isAddToFillPolygon) {
                                    continue;
                                }
                                int index = 0;
                                posInnerList1 = new ArrayList<PositionVec>(isolineContain3.polygon.npoints);
                                posInnerList2 = new ArrayList<PositionVec>(isolineContain3.polygon.npoints);
                                if (Math.abs(isolineContain3.val - eLevel) < precision) {
                                    // inner:isolineContain3.polygon
                                    boolean hasRemovePoint1 = false;
                                    //先判断方向
                                    if (polygonLevelList.get(i).direction == isolineContain3.direction) {
                                        //方向相同时，反方向增加
                                        for (int k = isolineContain3.polygon.npoints - 1; k >= 0; k--) {
                                            PositionVec pos = PositionVec.fromDegrees(isolineContain3.polygon.ypoints[k] * epsilon2, isolineContain3.polygon.xpoints[k] * epsilon2, innerElevation);
                                            if (isXYCoordinate) {
                                                Point2D.Double tmpPt = IsolineUtil.mergeLonLatToXY(isolineContain3.polygon.xpoints[k] * epsilon2,
                                                        isolineContain3.polygon.ypoints[k] * epsilon2, sX, eX, sY, eY,
                                                        attr.getXSLon(), attr.getXELon(), attr.getYSLat(), attr.getYELat());
                                                pos.setXY(tmpPt.x, tmpPt.y);
                                            }
                                            posInnerList1.add(pos);
                                        }
                                    } else {
                                        for (int k = 0; k < isolineContain3.polygon.npoints; k++) {
                                            PositionVec pos = PositionVec.fromDegrees(isolineContain3.polygon.ypoints[k] * epsilon2, isolineContain3.polygon.xpoints[k] * epsilon2, innerElevation);
                                            if (isXYCoordinate) {
                                                Point2D.Double tmpPt = IsolineUtil.mergeLonLatToXY(isolineContain3.polygon.xpoints[k] * epsilon2,
                                                        isolineContain3.polygon.ypoints[k] * epsilon2, sX, eX, sY, eY,
                                                        attr.getXSLon(), attr.getXELon(), attr.getYSLat(), attr.getYELat());
                                                pos.setXY(tmpPt.x, tmpPt.y);
                                            }
                                            posInnerList1.add(pos);
                                        }
                                    }

                                    if (procIsolineDataList.get((polygonLevelList.get(i).onIsolineIndex)).isClosed ||
                                            procIsolineDataList.get((isolineContain3.onIsolineIndex)).isClosed) {
                                        hasRemovePoint1 = false;
                                    } else {
                                        for (int k = 0; k < posOuterList.size(); k++) {
                                            for (int m = 0; m < posInnerList1.size(); m++) {
                                                if (posOuterList.get(k).getLatitude().equals(posInnerList1.get(m).getLatitude()) &&
                                                        posOuterList.get(k).getLongitude().equals(posInnerList1.get(m).getLongitude())) {
                                                    posOuterList.remove(k);
                                                    posInnerList1.remove(m);
                                                    if (!hasRemovePoint1) {
                                                        index = k;
                                                    }
                                                    k = k - 1;
                                                    m = m - 1;
                                                    hasRemovePoint1 = true;
                                                }
                                            }
                                        }
                                    }

                                    if (hasRemovePoint1) {
                                        posOuterList.addAll(index, posInnerList1);
                                    } else {
                                        int[] flagIndex = new int[2];
                                        flagIndex = isOuterBoundry(polygonLevelList.get(i).direction, isolineContain3.direction, isolineContain3.isSESameSide, posOuterList, posInnerList1);
                                        if (flagIndex[0] != 0) {
                                            posOuterList.addAll(flagIndex[1], posInnerList1);
                                            //在posOuterList的index位置插入posInnerList1
                                        } else {
                                            posInnerList.add(posInnerList1);
                                        }
                                    }//end else
                                    canFillPolygon = true;
                                    isolineContain3.isAddToFillPolygon = true;
                                    polygonContainList.get(isolineContain3.index).isAddToFillPolygon = true;
                                    if (isolineContain3.neighborhoodIndex != -1) {
                                        polygonContainList.get(isolineContain3.neighborhoodIndex).isAddToFillPolygon = true;
                                        int num = 0;
//										for(int k=0; k<polyIndexAry.size(); k++){
                                        for (int k = polyIndexAry.size() - 1; k >= 0; k--) {//避免重复填充，已填充过多边形的index = 无有
                                            if (polyIndexAry.get(k) == isolineContain3.neighborhoodIndex) {
                                                polygonLevelList.get(k).isAddToFillPolygon = true;
                                                num++;
                                            }
                                            if (polyIndexAry.get(k) == isolineContain3.index) {
                                                polygonLevelList.get(k).isAddToFillPolygon = true;
                                                num++;
                                            }
                                            if (num == 2) {
                                                break;
                                            }
                                        }
                                    } else {
                                        for (int k = polyIndexAry.size() - 1; k >= 0; k--) {//避免重复填充，已填充过多边形的index = 无有
                                            if (polyIndexAry.get(k) == isolineContain3.index) {
                                                polygonLevelList.get(k).isAddToFillPolygon = true;
                                                break;
                                            }
                                        }
                                    }
                                }//endif

                                if (Math.abs(isolineContain3.val - sLevel) < precision) {
                                    //先判断方向
                                    if (polygonLevelList.get(i).direction == isolineContain3.direction) {
                                        //方向相同时，反方向增加
                                        for (int k = isolineContain3.polygon.npoints - 1; k >= 0; k--) {
                                            PositionVec pos = PositionVec.fromDegrees(isolineContain3.polygon.ypoints[k] * epsilon2, isolineContain3.polygon.xpoints[k] * epsilon2, innerElevation);
                                            if (isXYCoordinate) {
                                                Point2D.Double tmpPt = IsolineUtil.mergeLonLatToXY(isolineContain3.polygon.xpoints[k] * epsilon2,
                                                        isolineContain3.polygon.ypoints[k] * epsilon2, sX, eX, sY, eY,
                                                        attr.getXSLon(), attr.getXELon(), attr.getYSLat(), attr.getYELat());
                                                pos.setXY(tmpPt.x, tmpPt.y);
                                            }
                                            posInnerList2.add(pos);
                                        }
                                    } else {
                                        for (int k = 0; k < isolineContain3.polygon.npoints; k++) {
                                            PositionVec pos = PositionVec.fromDegrees(isolineContain3.polygon.ypoints[k] * epsilon2, isolineContain3.polygon.xpoints[k] * epsilon2, innerElevation);
                                            if (isXYCoordinate) {
                                                Point2D.Double tmpPt = IsolineUtil.mergeLonLatToXY(isolineContain3.polygon.xpoints[k] * epsilon2,
                                                        isolineContain3.polygon.ypoints[k] * epsilon2, sX, eX, sY, eY,
                                                        attr.getXSLon(), attr.getXELon(), attr.getYSLat(), attr.getYELat());
                                                pos.setXY(tmpPt.x, tmpPt.y);
                                            }
                                            posInnerList2.add(pos);
                                        }
                                    }
                                    boolean hasRemovePoint2 = false;
                                    if (procIsolineDataList.get((polygonLevelList.get(i).onIsolineIndex)).isClosed ||
                                            procIsolineDataList.get((isolineContain3.onIsolineIndex)).isClosed) {
                                        hasRemovePoint2 = false;
                                    } else {
                                        for (int k = 0; k < posOuterList.size(); k++) {
                                            for (int m = 0; m < posInnerList2.size(); m++) {
                                                if (posOuterList.get(k).getLatitude().equals(posInnerList2.get(m).getLatitude()) &&
                                                        posOuterList.get(k).getLongitude().equals(posInnerList2.get(m).getLongitude())) {
                                                    posOuterList.remove(k);
                                                    posInnerList2.remove(m);
                                                    if (!hasRemovePoint2) {
                                                        index = k;
                                                    }
                                                    k = k - 1;
                                                    m = m - 1;
                                                    hasRemovePoint2 = true;
                                                }
                                            }
                                        }
                                    }
                                    if (hasRemovePoint2) {
                                        posOuterList.addAll(index, posInnerList2);
                                    } else {
                                        int[] flagIndex = new int[2];
                                        flagIndex = isOuterBoundry(polygonLevelList.get(i).direction, isolineContain3.direction, isolineContain3.isSESameSide, posOuterList, posInnerList2);
                                        if (flagIndex[0] != 0) {
                                            posOuterList.addAll(flagIndex[1], posInnerList2);
                                        } else {
                                            posInnerList.add(posInnerList2);
                                        }
                                    }
                                    canFillPolygon = true;
                                    isolineContain3.isAddToFillPolygon = true;
                                    polygonContainList.get(isolineContain3.index).isAddToFillPolygon = true;
                                    if (isolineContain3.neighborhoodIndex != -1) {
                                        polygonContainList.get(isolineContain3.neighborhoodIndex).isAddToFillPolygon = true;
                                        int num = 0;
//										for(int k=0; k<polyIndexAry.size(); k++){
                                        for (int k = polyIndexAry.size() - 1; k >= 0; k--) {
                                            if (polyIndexAry.get(k) == isolineContain3.neighborhoodIndex) {
                                                polygonLevelList.get(k).isAddToFillPolygon = true;
                                                num++;
                                            }
                                            if (polyIndexAry.get(k) == isolineContain3.index) {
                                                polygonLevelList.get(k).isAddToFillPolygon = true;
                                                num++;
                                            }
                                            if (num == 2) {
                                                break;
                                            }
                                        }
                                    } else {
                                        for (int k = polyIndexAry.size() - 1; k >= 0; k--) {
                                            if (polyIndexAry.get(k) == isolineContain3.index) {
                                                polygonLevelList.get(k).isAddToFillPolygon = true;
                                                break;
                                            }
                                        }
                                    }
                                }//endif
                            }// endfor pgnIndexList.size()
                        } else {//delta<0
                            if (!polygonLevelList.get(i).isDown) {
                                continue;
                            }
                            for (int j = 0; j < pgnIndexList.size(); j++) {
                                isolineContain3 = polygonContainList.get(pgnIndexList.get(j));
                                if (isolineContain3.isAddToFillPolygon) {
                                    continue;
                                }
                                int index = 0;
                                posInnerList1 = new ArrayList<PositionVec>(isolineContain3.polygon.npoints);
                                posInnerList2 = new ArrayList<PositionVec>(isolineContain3.polygon.npoints);
                                if (Math.abs(isolineContain3.val - eLevel) < precision) {
                                    // inner:isolineContain3.polygon
                                    boolean hasRemovePoint1 = false;
                                    //先判断方向
                                    if (polygonLevelList.get(i).direction == isolineContain3.direction) {
                                        //方向相同时，反方向增加
                                        for (int k = isolineContain3.polygon.npoints - 1; k >= 0; k--) {
                                            PositionVec pos = PositionVec.fromDegrees(isolineContain3.polygon.ypoints[k] * epsilon2, isolineContain3.polygon.xpoints[k] * epsilon2, innerElevation);
                                            if (isXYCoordinate) {
                                                Point2D.Double tmpPt = IsolineUtil.mergeLonLatToXY(isolineContain3.polygon.xpoints[k] * epsilon2,
                                                        isolineContain3.polygon.ypoints[k] * epsilon2, sX, eX, sY, eY,
                                                        attr.getXSLon(), attr.getXELon(), attr.getYSLat(), attr.getYELat());
                                                pos.setXY(tmpPt.x, tmpPt.y);
                                            }
                                            posInnerList1.add(pos);
                                        }
                                    } else {
                                        for (int k = 0; k < isolineContain3.polygon.npoints; k++) {
                                            PositionVec pos = PositionVec.fromDegrees(isolineContain3.polygon.ypoints[k] * epsilon2, isolineContain3.polygon.xpoints[k] * epsilon2, innerElevation);
                                            if (isXYCoordinate) {
                                                Point2D.Double tmpPt = IsolineUtil.mergeLonLatToXY(isolineContain3.polygon.xpoints[k] * epsilon2,
                                                        isolineContain3.polygon.ypoints[k] * epsilon2, sX, eX, sY, eY,
                                                        attr.getXSLon(), attr.getXELon(), attr.getYSLat(), attr.getYELat());
                                                pos.setXY(tmpPt.x, tmpPt.y);
                                            }
                                            posInnerList1.add(pos);
                                        }
                                    }
                                    if (procIsolineDataList.get((polygonLevelList.get(i).onIsolineIndex)).isClosed ||
                                            procIsolineDataList.get((isolineContain3.onIsolineIndex)).isClosed) {
                                        hasRemovePoint1 = false;
                                    } else {
                                        for (int k = 0; k < posOuterList.size(); k++) {
                                            for (int m = 0; m < posInnerList1.size(); m++) {
                                                if (posOuterList.get(k).getLatitude().equals(posInnerList1.get(m).getLatitude()) &&
                                                        posOuterList.get(k).getLongitude().equals(posInnerList1.get(m).getLongitude())) {
                                                    posOuterList.remove(k);
                                                    posInnerList1.remove(m);
                                                    if (!hasRemovePoint1) {
                                                        index = k;
                                                    }
                                                    k = k - 1;
                                                    m = m - 1;
                                                    hasRemovePoint1 = true;
                                                }
                                            }
                                        }
                                    }

                                    if (hasRemovePoint1) {
                                        posOuterList.addAll(index, posInnerList1);
                                    } else {
                                        int[] flagIndex = new int[2];
                                        flagIndex = isOuterBoundry(polygonLevelList.get(i).direction, isolineContain3.direction, isolineContain3.isSESameSide, posOuterList, posInnerList1);
                                        if (flagIndex[0] != 0) {
                                            posOuterList.addAll(flagIndex[1], posInnerList1);
                                        } else {
                                            posInnerList.add(posInnerList1);
                                        }
                                    }// end else
                                    canFillPolygon = true;
                                    isolineContain3.isAddToFillPolygon = true;
                                    polygonContainList.get(isolineContain3.index).isAddToFillPolygon = true;
                                    if (isolineContain3.neighborhoodIndex != -1) {
                                        polygonContainList.get(isolineContain3.neighborhoodIndex).isAddToFillPolygon = true;
                                        int num = 0;
                                        for (int k = 0; k < polyIndexAry.size(); k++) {
                                            if (polyIndexAry.get(k) == isolineContain3.neighborhoodIndex) {
                                                polygonLevelList.get(k).isAddToFillPolygon = true;
                                                num++;
                                            }
                                            if (polyIndexAry.get(k) == isolineContain3.index) {
                                                polygonLevelList.get(k).isAddToFillPolygon = true;
                                                num++;
                                            }
                                            if (num == 2) {
                                                break;
                                            }
                                        }
                                    } else {
                                        for (int k = 0; k < polyIndexAry.size(); k++) {
                                            if (polyIndexAry.get(k) == isolineContain3.index) {
                                                polygonLevelList.get(k).isAddToFillPolygon = true;
                                                break;
                                            }
                                        }
                                    }
                                }//endif
                                if (Math.abs(isolineContain3.val - sLevel) < precision) {
                                    //先判断方向
                                    if (polygonLevelList.get(i).direction == isolineContain3.direction) {
                                        //方向相同时，反方向增加
                                        for (int k = isolineContain3.polygon.npoints - 1; k >= 0; k--) {
                                            PositionVec pos = PositionVec.fromDegrees(isolineContain3.polygon.ypoints[k] * epsilon2, isolineContain3.polygon.xpoints[k] * epsilon2, innerElevation);
                                            if (isXYCoordinate) {
                                                Point2D.Double tmpPt = IsolineUtil.mergeLonLatToXY(isolineContain3.polygon.xpoints[k] * epsilon2,
                                                        isolineContain3.polygon.ypoints[k] * epsilon2, sX, eX, sY, eY,
                                                        attr.getXSLon(), attr.getXELon(), attr.getYSLat(), attr.getYELat());
                                                pos.setXY(tmpPt.x, tmpPt.y);
                                            }
                                            posInnerList2.add(pos);
                                        }
                                    } else {
                                        for (int k = 0; k < isolineContain3.polygon.npoints; k++) {
                                            PositionVec pos = PositionVec.fromDegrees(isolineContain3.polygon.ypoints[k] * epsilon2, isolineContain3.polygon.xpoints[k] * epsilon2, innerElevation);
                                            if (isXYCoordinate) {
                                                Point2D.Double tmpPt = IsolineUtil.mergeLonLatToXY(isolineContain3.polygon.xpoints[k] * epsilon2,
                                                        isolineContain3.polygon.ypoints[k] * epsilon2, sX, eX, sY, eY,
                                                        attr.getXSLon(), attr.getXELon(), attr.getYSLat(), attr.getYELat());
                                                pos.setXY(tmpPt.x, tmpPt.y);
                                            }
                                            posInnerList2.add(pos);
                                        }
                                    }
                                    boolean hasRemovePoint2 = false;
                                    if (procIsolineDataList.get((polygonLevelList.get(i).onIsolineIndex)).isClosed ||
                                            procIsolineDataList.get((isolineContain3.onIsolineIndex)).isClosed) {
                                        hasRemovePoint2 = false;
                                    } else {
                                        for (int k = 0; k < posOuterList.size(); k++) {
                                            for (int m = 0; m < posInnerList2.size(); m++) {
                                                if (posOuterList.get(k).getLatitude().equals(posInnerList2.get(m).getLatitude()) &&
                                                        posOuterList.get(k).getLongitude().equals(posInnerList2.get(m).getLongitude())) {
                                                    posOuterList.remove(k);
                                                    posInnerList2.remove(m);
                                                    if (!hasRemovePoint2) {
                                                        index = k;
                                                    }
                                                    k = k - 1;
                                                    m = m - 1;
                                                    hasRemovePoint2 = true;
                                                }
                                            }
                                        }
                                    }
                                    if (hasRemovePoint2) {
                                        posOuterList.addAll(index, posInnerList2);
                                    } else {
                                        int[] flagIndex = new int[2];
                                        flagIndex = isOuterBoundry(polygonLevelList.get(i).direction, isolineContain3.direction, isolineContain3.isSESameSide, posOuterList, posInnerList2);
                                        if (flagIndex[0] != 0) {
                                            posOuterList.addAll(flagIndex[1], posInnerList2);
                                        } else {
                                            posInnerList.add(posInnerList2);
                                        }
                                    }
                                    canFillPolygon = true;
                                    isolineContain3.isAddToFillPolygon = true;
                                    polygonContainList.get(isolineContain3.index).isAddToFillPolygon = true;
                                    if (isolineContain3.neighborhoodIndex != -1) {
                                        polygonContainList.get(isolineContain3.neighborhoodIndex).isAddToFillPolygon = true;
                                        int num = 0;
                                        for (int k = 0; k < polyIndexAry.size(); k++) {
                                            if (polyIndexAry.get(k) == isolineContain3.neighborhoodIndex) {
                                                polygonLevelList.get(k).isAddToFillPolygon = true;
                                                num++;
                                            }
                                            if (polyIndexAry.get(k) == isolineContain3.index) {
                                                polygonLevelList.get(k).isAddToFillPolygon = true;
                                                num++;
                                            }
                                            if (num == 2) {
                                                break;
                                            }
                                        }
                                    } else {
                                        for (int k = 0; k < polyIndexAry.size(); k++) {
                                            if (polyIndexAry.get(k) == isolineContain3.index) {
                                                polygonLevelList.get(k).isAddToFillPolygon = true;
                                                break;
                                            }
                                        }
                                    }
                                }//endif
                            }//endfor pgnIndexList.size()
                        }//endelse delta<0
//
                        if (!canFillPolygon) {
                            continue;
                        }

                        polygonLevelList.get(i).isAddToFillPolygon = true;
                        if (polygonLevelList.get(i).neighborhoodIndex != -1) {
                            int num = 0;
                            for (int k = 0; k < polyIndexAry.size(); k++) {
                                if (polygonLevelList.get(k).index == polygonLevelList.get(i).neighborhoodIndex) {
                                    polygonLevelList.get(k).isAddToFillPolygon = true;
                                    num++;
                                }
                                if (polyIndexAry.get(k) == polygonLevelList.get(i).neighborhoodIndex) {
                                    polygonContainList.get(polyIndexAry.get(k)).isAddToFillPolygon = true;
                                    num++;
                                }
                                if (polyIndexAry.get(k) == polygonLevelList.get(i).index) {
                                    polygonContainList.get(polyIndexAry.get(k)).isAddToFillPolygon = true;
                                    num++;
                                }
                                if (num == 3) {
                                    break;
                                }
                            }
                        } else {
                            for (int k = 0; k < polyIndexAry.size(); k++) {
                                if (polygonContainList.get(polyIndexAry.get(k)).onIsolineIndex == polygonLevelList.get(i).onIsolineIndex) {
                                    polygonContainList.get(polyIndexAry.get(k)).isAddToFillPolygon = true;
                                    break;
                                }
                            }
                        }
                        //防止多边形不闭合 db_x 2014-11-13
                        if (posOuterList.size() > 2) {
                            if (!posOuterList.get(0).equals(posOuterList.get(posOuterList.size() - 1))) {
                                posOuterList.add(posOuterList.get(0));
                            }
                        }
                        if (posOuterList.size() < 4) {
                            continue;
                        }
                        onePolygon.setOuterBoundary(posOuterList);
                        if (posInnerList != null && !posInnerList.isEmpty()) {
                            for (int t = 0; t < posInnerList.size(); t++) {
                                List<PositionVec> innerBoundary = posInnerList.get(t);
                                if (innerBoundary.size() > 2) {
                                    if (!innerBoundary.get(0).equals(innerBoundary.get(innerBoundary.size() - 1))) {
                                        innerBoundary.add(innerBoundary.get(0));
                                    }
                                }
                                onePolygon.addInnerBoundary(innerBoundary);
                            }
                        }
                        onePolygon.setSELevel(sLevel, eLevel);
                        onePolygon.setinitialSElevel(srcSLevel, srcELevel);
                        resPolygonList.add(onePolygon);
                    }// endif
                }//endif
                else {
                    if (eLevel > sLevel) {
                        if (polygonLevelList.get(i).isDown) {
                            continue;
                        }
                    } else {
                        if (!polygonLevelList.get(i).isDown) {
                            continue;
                        }
                    }
                    polygonLevelList.get(i).isAddToFillPolygon = true;//此多边形本身已经形成了填充多边形
                    int num = 0;
                    for (int k = 0; k < polyIndexAry.size(); k++) {
                        if (polygonLevelList.get(k).index == polygonLevelList.get(i).neighborhoodIndex) {
                            polygonLevelList.get(k).isAddToFillPolygon = true;//和此多边形相邻的多边形,即在同一条等值线上的多边形,已经也不能再构成填充多边形
                            num++;
                        }
                        if (polyIndexAry.get(k) == polygonLevelList.get(i).neighborhoodIndex) {
                            polygonContainList.get(polyIndexAry.get(k)).isAddToFillPolygon = true;//包含的多边形
                            num++;
                        }
                        if (polyIndexAry.get(k) == polygonLevelList.get(i).index) {
                            polygonContainList.get(polyIndexAry.get(k)).isAddToFillPolygon = true;
                            num++;
                        }

                        if (num == 3) {
                            break;
                        }
                    }
                    //防止多边形不闭合 db_x 2014-11-13
                    if (posOuterList.size() > 2) {
                        if (!posOuterList.get(0).equals(posOuterList.get(posOuterList.size() - 1))) {
                            posOuterList.add(posOuterList.get(0));
                        }
                    }
                    if (posOuterList.size() < 4) {
                        continue;
                    }
                    onePolygon.setOuterBoundary(posOuterList);
                    onePolygon.setSELevel(sLevel, eLevel);
                    onePolygon.setinitialSElevel(srcSLevel, srcELevel);
                    resPolygonList.add(onePolygon);
                }
            }// endfor
        }// endif 3
    }

    /**
     * 离散数据网格化
     *
     * @return true为离散数据网格化成功
     */
    private boolean scatterDataToGrid() {
        /*int latCol = ArrayOperator.getIndexFromAry(scatterDataList.get(0).getElemName(), ElemCode.LAT.getFileValue());
        int lonCol = ArrayOperator.getIndexFromAry(scatterDataList.get(0).getElemName(),ElemCode.LON.getFileValue());
		int dataCol = ArrayOperator.getIndexFromAry(scatterDataList.get(0).getElemName(),attr.getElem().getFileValue());
		Integer[] index = new Integer[]{latCol,lonCol};
		String[][] scatterdata1 = StrArrayUtil.uniqueRowAsColumns(scatterDataList.get(0).getAllData(),index);//排重
		if(scatterdata1==null){
			logger.println("In IsolineProcess() scatterDataToGrid() 去重后数据为null");
			scatterDataList.clear();
			return false;
		}
		Integer[] nullIndex = new Integer[]{dataCol};
		String[] values = new String[]{""};
		String[][] scatterdata = StrArrayUtil.arrayFilter(scatterdata1,nullIndex,values);
		if(scatterdata==null || scatterdata.length<=0){
			logger.println("In IsolineProcess() scatterDataToGrid() 所有点的数据都为null");
			scatterDataList.clear();
			return false;
		}
		float[] tmpLat = new float[scatterdata.length];
		float[] tmpLon = new float[scatterdata.length];
		float[] tmpData = new float[scatterdata.length];
		for(int i =0; i<scatterdata.length; i++){
			if(!scatterdata[i][dataCol].equals("")){
				tmpLat[i] = Float.parseFloat(scatterdata[i][latCol]);
				tmpLon[i] = Float.parseFloat(scatterdata[i][lonCol]);
				tmpData[i] = Float.parseFloat(scatterdata[i][dataCol]);
			}
		}
		if(tmpData==null || tmpData.length<=0){
			logger.println("In IsolineProcess() scatterDataToGrid() 所有点的数据都为null");
			scatterDataList.clear();
			return false;
		}

		gridDataList = new ArrayList<GridData>();
		//网格化
		if (this.attr.getGridedType().equals(GridedType.INVERSE_DISTANCE)){

			GridData gridData = MIDS3DMath.scatterToGrid(tmpData, tmpLon, tmpLat, attr.getxStart(), attr.getxEnd(), attr.getyStart(), attr.getyEnd(), attr.getxDelta(), attr.getyDelta(),
					attr.getMinVal(), attr.getMaxVal(), attr.getDistR1(), attr.getDataR3(), attr.getValidDataR(), attr.getGsR(), attr.getDataType(), attr.getElem().toString());
			if (gridData == null || gridData.isEmpty()) {
				System.err.println("离散数据网格化失败");
				return false;
			}
			gridDataList.add(gridData);
		} else if (this.attr.getGridedType().equals(GridedType.TRIANGULATION)){
			Triangle triangle = new Triangle(new Pnt(0.0f, 100000f), new Pnt(100000f, 0.0f), new Pnt(-100000f, -100000f));
			GridData data = MIDS3DMath.scatterTriToGrid(tmpData, tmpLon, tmpLat, attr.getxStart(), attr.getxEnd(), attr.getyStart(), attr.getyEnd(), attr.getxDelta(), attr.getyDelta(),
					attr.getMinVal(), attr.getMaxVal(), attr.getDataType(), triangle, attr.getElem().toString());
			if (data == null || data.isEmpty()){
				logger.println("数据为空 ");
				return false;
			}
			gridDataList.add(data);
		} else {
			System.err.println("给定的GridedType错误!");
			return false;
		}*/
        return true;
    }

    private boolean srcClosedPolygonBuild() {
        if (procIsolineDataList == null || procIsolineDataList.isEmpty()) {
            if (!getIsolineDataProcList()) {
                if (logger.isDebugEnabled())
                    logger.debug("In IsolineProcess orignalPolygonBuild() 等值线追踪失败");
                return false;
            }
        }
        if (procIsolineDataList.isEmpty()) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess orignalPolygonBuild() 无符合level条件的等值线");
            return false;
        }
        ArrayList<Point2D.Double> lineList = null;
        Polygon p = null;
        double maxLat, minLat, maxLon, minLon;
        polygonClosedProcList = new ArrayList<IsolinePolygonProc>();
        IsolinePolygonProc isolinePgnProc = null;
        int index = 0;
        ArrayList<Integer> indexAry = new ArrayList<Integer>(1);
        indexAry.add(-1);
        boolean isFilter = this.attr.isFilter();
        //旋转方向,0:无，1：顺时针方向；2：逆时针方向(xEnd>xStart; yEnd>yStart)
        //旋转方向,0：无，1：逆时针，2：顺时针(xEnd>xStart; yStart>yEnd)
        for (int i = 0; i < procIsolineDataList.size(); i++) {
            int num = procIsolineDataList.get(i).num;
            int firstDirection = 0;
            if (!procIsolineDataList.get(i).isClosed) {
                continue;
            }
            lineList = procIsolineDataList.get(i).lineList2D;
            p = new Polygon();
            maxLat = lineList.get(0).y;
            minLat = lineList.get(0).y;
            maxLon = lineList.get(0).x;
            minLon = lineList.get(0).x;

            Point2D.Double point;
            for (int j = 0; j < lineList.size(); j++) {
                point = lineList.get(j);
                p.addPoint((int) (point.x * epsilon1), (int) (point.y * epsilon1));
                if (maxLat < point.y) {
                    maxLat = point.y;
                }
                if (minLat > point.y) {
                    minLat = point.y;
                }
                if (maxLon < point.x) {
                    maxLon = point.x;
                }
                if (minLon > point.x) {
                    minLon = point.x;
                }
            }
            if (isFilter) {
                if (num < 10 || (Math.max(Math.abs(maxLat - minLat), Math.abs(maxLon - minLon)) < 0.25f)) {
                    continue;
                }
            }

            // 计算一个多边形包含的网格点的最值
//			MaxMinValPos resVal = getPolygonMaxMinVal(p, minLon, maxLon, minLat, maxLat);
            MaxMinValPos resVal = getPolygonMaxMinVal(p, procIsolineDataList.get(i).colsList, procIsolineDataList.get(i).rowsList);

            procIsolineDataList.get(i).firstMax = resVal.maxVal;
            procIsolineDataList.get(i).firstMin = resVal.minVal;
            procIsolineDataList.get(i).firstMaxPos = resVal.maxValPosList;
            procIsolineDataList.get(i).firstMinPos = resVal.minValPosList;
            procIsolineDataList.get(i).polygonFirst = p;
            procIsolineDataList.get(i).polygonFirstMaxLat = maxLat;
            procIsolineDataList.get(i).polygonFirstMinLat = minLat;
            procIsolineDataList.get(i).polygonFirstMaxLon = maxLon;
            procIsolineDataList.get(i).polygonFirstMinLon = minLon;
            procIsolineDataList.get(i).firstPolygonDirection = firstDirection;

            isolinePgnProc = new IsolinePolygonProc();
            isolinePgnProc.val = procIsolineDataList.get(i).val;
            isolinePgnProc.onIsolineIndex = procIsolineDataList.get(i).indexProc;
            isolinePgnProc.index = index;
            isolinePgnProc.polygon = p;
            isolinePgnProc.maxLat = maxLat;
            isolinePgnProc.minLat = minLat;
            isolinePgnProc.maxLon = maxLon;
            isolinePgnProc.minLon = minLon;
            isolinePgnProc.maxVal = resVal.maxVal;
            isolinePgnProc.minVal = resVal.minVal;
            isolinePgnProc.direction = firstDirection;
            isolinePgnProc.polygonBoundaryIndexAry = indexAry;
            polygonClosedProcList.add(isolinePgnProc);
            index++;
        }
        return true;
    }

    private Boolean isDown(IsolineDataProc anIsoline, Polygon poly) {
        float curVal = anIsoline.val;
        ArrayList<Point2D.Double> pointList = anIsoline.lineList2D;
        boolean isClosed = anIsoline.isClosed;
        Boolean flag = null;
        int size = pointList.size();
        if (isClosed) {
            for (int i = 0; i < size; i++) {
                int col = anIsoline.colsList.get(i);
                int row = anIsoline.rowsList.get(i);
                Point2D.Double pt = getPos(row, col);
                if (isPointInPolygon((int) (pt.x * epsilon1), (int) (pt.y * epsilon1), poly, null)) {
                    if (gridDataList.size() == 1) {
                        if (gridDataList.get(0).getGridData()[row][col] < curVal) {
                            flag = true;
                            break;
                        } else if (gridDataList.get(0).getGridData()[row][col] == curVal) {
                            if (i == size - 1 && flag == null) {
                                throw new RuntimeErrorException(null, "In IsolineProcess计算isDown()时出错");
                            }
                            continue;
                        } else {
                            flag = false;
                            break;
                        }
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug("计算多边形downhill时因为gridDataList.size()不为1,无法取得当前等值点所在边的端点的值");
                        return null;
                    }
                } else {
                    if (gridDataList.size() == 1) {
                        if (gridDataList.get(0).getGridData()[row][col] < curVal) {
                            flag = false;
                            break;
                        } else if (gridDataList.get(0).getGridData()[row][col] == curVal) {
                            if (i == size - 1 && flag == null) {
                                throw new RuntimeErrorException(null, "In IsolineProcess计算isDown()时出错");
                            }
                            continue;
                        } else {
                            flag = true;
                            break;
                        }
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug("计算多边形downhill时因为gridDataList.size()不为1,无法取得当前等值点所在边的端点的值");
                        return null;
                    }
                }
            }
        } else {
            ArrayList<Integer> addPointIndex = anIsoline.pgFirstBoundaryIndexAry;
            for (int i = 0; i < size; i++) {
                int col = anIsoline.colsList.get(i);
                int row = anIsoline.rowsList.get(i);
                Point2D.Double pt = getPos(row, col);
                if (isPointInPolygon((int) (pt.x * epsilon1), (int) (pt.y * epsilon1), poly, addPointIndex)) {
                    if (gridDataList.size() == 1) {
                        if (gridDataList.get(0).getGridData()[row][col] < curVal) {
                            flag = true;
                            break;
                        } else if (gridDataList.get(0).getGridData()[row][col] == curVal) {
                            continue;
                        } else {
                            flag = false;
                            break;
                        }
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug("计算多边形downhill时因为gridDataList.size()不为1,无法取得当前等值点所在边的端点的值");
                        return null;
                    }
                } else {
                    if (gridDataList.size() == 1) {
                        if (gridDataList.get(0).getGridData()[row][col] < curVal) {
                            flag = false;
                            break;
                        } else if (gridDataList.get(0).getGridData()[row][col] == curVal) {
                            continue;
                        } else {
                            flag = true;
                            break;
                        }
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug("计算多边形downhill时因为gridDataList.size()不为1,无法取得当前等值点所在边的端点的值");
                        return null;
                    }
                }
            }

            if (flag == null) {
                if (isClosed) {
                    throw new RuntimeErrorException(null, "In IsolineProcess计算isDown()时出错");
                } else {
                    int sLineType = anIsoline.startLineType;
                    int eLineType = anIsoline.endLineType;
                    int sCol = anIsoline.colsList.get(0);
                    int sRow = anIsoline.rowsList.get(0);
                    int eCol = anIsoline.colsList.get(size - 1);
                    int eRow = anIsoline.rowsList.get(size - 1);
                    int row1 = 0, col1 = 0;
                    int row2 = 0, col2 = 0;
                    int row3 = 0, col3 = 0;
                    int row4 = 0, col4 = 0;
                    switch (sLineType) {
                        case 1:
                            switch (eLineType) {
                                case 1:
                                    row1 = sRow;
                                    col1 = sCol;
                                    row2 = eRow + 1;
                                    col2 = eCol;
                                    row3 = sRow + 1;
                                    col3 = sCol;
                                    row4 = eRow;
                                    col4 = eCol;
                                    break;
                                case 2:
                                    row1 = sRow + 1;
                                    col1 = sCol;
                                    row2 = eRow;
                                    col2 = eCol + 1;
                                    row3 = sRow;
                                    col3 = sCol;
                                    row4 = eRow;
                                    col4 = eCol;
                                    break;
                                case 3:
                                    row1 = sRow + 1;
                                    col1 = sCol;
                                    row2 = eRow + 1;
                                    col2 = eCol;
                                    row3 = sRow;
                                    col3 = sCol;
                                    row4 = eRow;
                                    col4 = eCol;
                                    break;
                                case 4:
                                    row1 = sRow + 1;
                                    col1 = sCol;
                                    row2 = eRow;
                                    col2 = eCol;
                                    row3 = sRow;
                                    col3 = sCol;
                                    row4 = eRow;
                                    col4 = eCol + 1;
                                    break;

                                default:
                                    throw new RuntimeErrorException(null, "In IsolineProcess 计算isDown时出错");
                            }
                            break;
                        case 2:
                            switch (eLineType) {
                                case 1:
                                    row1 = sRow;
                                    col1 = sCol + 1;
                                    row2 = eRow + 1;
                                    col2 = eCol;
                                    row3 = sRow;
                                    col3 = sCol;
                                    row4 = eRow;
                                    col4 = eCol;
                                    break;
                                case 2:
                                    row1 = sRow;
                                    col1 = sCol + 1;
                                    row2 = eRow;
                                    col2 = eCol + 1;
                                    row3 = sRow;
                                    col3 = sCol;
                                    row4 = eRow;
                                    col4 = eCol;
                                    break;
                                case 3:
                                    row1 = sRow;
                                    col1 = sCol + 1;
                                    row2 = eRow + 1;
                                    col2 = eCol;
                                    row3 = sRow;
                                    col3 = sCol;
                                    row4 = eRow;
                                    col4 = eCol;
                                    break;
                                case 4:
                                    row1 = sRow;
                                    col1 = sCol + 1;
                                    row2 = eRow;
                                    col2 = eCol + 1;
                                    row3 = sRow;
                                    col3 = sCol;
                                    row4 = eRow;
                                    col4 = eCol;
                                    break;

                                default:
                                    throw new RuntimeErrorException(null, "In IsolineProcess 计算isDown时出错");
                            }
                            break;
                        case 3:
                            switch (eLineType) {
                                case 1:
                                    row1 = sRow + 1;
                                    col1 = sCol;
                                    row2 = eRow + 1;
                                    col2 = eCol;
                                    row3 = sRow;
                                    col3 = sCol;
                                    row4 = eRow;
                                    col4 = eCol;
                                    break;
                                case 2:
                                    row1 = sRow + 1;
                                    col1 = sCol;
                                    row2 = eRow;
                                    col2 = eCol + 1;
                                    row3 = sRow;
                                    col3 = sCol;
                                    row4 = eRow;
                                    col4 = eCol;
                                    break;
                                case 3:
                                    row1 = sRow + 1;
                                    col1 = sCol;
                                    row2 = eRow + 1;
                                    col2 = eCol;
                                    row3 = sRow;
                                    col3 = sCol;
                                    row4 = eRow;
                                    col4 = eCol;
                                    break;
                                case 4:
                                    row1 = sRow + 1;
                                    col1 = sCol;
                                    row2 = eRow;
                                    col2 = eCol + 1;
                                    row3 = sRow;
                                    col3 = sCol;
                                    row4 = eRow;
                                    col4 = eCol;
                                    break;

                                default:
                                    throw new RuntimeErrorException(null, "In IsolineProcess 计算isDown时出错");
                            }
                            break;
                        case 4:
                            switch (eLineType) {
                                case 1:
                                    row1 = sRow;
                                    col1 = sCol + 1;
                                    row2 = eRow + 1;
                                    col2 = eCol;
                                    row3 = sRow;
                                    col3 = sCol;
                                    row4 = eRow;
                                    col4 = eCol;
                                    break;
                                case 2:
                                    row1 = sRow;
                                    col1 = sCol + 1;
                                    row2 = eRow;
                                    col2 = eCol + 1;
                                    row3 = sRow;
                                    col3 = sCol;
                                    row4 = eRow;
                                    col4 = eCol;
                                    break;
                                case 3:
                                    row1 = sRow;
                                    col1 = sCol + 1;
                                    row2 = eRow + 1;
                                    col2 = eCol;
                                    row3 = sRow;
                                    col3 = sCol;
                                    row4 = eRow;
                                    col4 = eCol;
                                    break;
                                case 4:
                                    row1 = sRow;
                                    col1 = sCol + 1;
                                    row2 = eRow;
                                    col2 = eCol + 1;
                                    row3 = sRow;
                                    col3 = sCol;
                                    row4 = eRow;
                                    col4 = eCol;
                                    break;

                                default:
                                    throw new RuntimeErrorException(null, "In IsolineProcess 计算isDown时出错");
                            }
                            break;

                        default:
                            throw new RuntimeErrorException(null, "In IsolineProcess 计算isDown时出错");
                    }

                    int[] rows = new int[]{row1, row2, row3, row4};
                    int[] cols = new int[]{col1, col2, col3, col4};

                    for (int k = 0; k < 4; k++) {
                        boolean findFlag = false;
                        while (!findFlag && flag == null) {
                            findFlag = true;
                            Point2D.Double pt = getPos(rows[k], cols[k]);
                            if (isPointInPolygon((int) (pt.x * epsilon1), (int) (pt.y * epsilon1), poly, addPointIndex)) {
                                if (gridDataList.size() == 1) {
                                    if (gridDataList.get(0).getGridData()[rows[k]][cols[k]] < curVal) {
                                        flag = true;
                                    } else if (gridDataList.get(0).getGridData()[rows[k]][cols[k]] == curVal) {
                                        continue;
                                    } else {
                                        flag = false;
                                    }
                                } else {
                                    if (logger.isDebugEnabled())
                                        logger.debug("计算多边形downhill时因为gridDataList.size()不为1,无法取得当前等值点所在边的端点的值");
                                    return null;
                                }
                            } else {
                                if (gridDataList.size() == 1) {
                                    if (gridDataList.get(0).getGridData()[rows[k]][cols[k]] < curVal) {
                                        flag = false;
                                    } else if (gridDataList.get(0).getGridData()[rows[k]][cols[k]] == curVal) {
                                        continue;
                                    } else {
                                        flag = true;
                                    }
                                } else {
                                    if (logger.isDebugEnabled())
                                        logger.debug("计算多边形downhill时因为gridDataList.size()不为1,无法取得当前等值点所在边的端点的值");
                                    return null;
                                }
                            }
                        }
                        if (flag != null) {
                            break;
                        }
                    }
                }
            }
        }
        return flag;
    }

    //逆时针
    private Point2D.Float[] getAllPtsSide() {
        int num = gridRows * 2 + gridCols * 2 - 4;
        Point2D.Float[] pts = new Point2D.Float[num];
        int sub1 = 0;
        int sub2 = 1;
        int sub3 = gridCols - 2;
        int sub4 = gridRows - 2;
        for (int i = 0; i < num; i++) {
            pts[i] = new Point2D.Float();
            if (i < gridCols) {
                pts[i].x = gridXArys[0][sub1];
                pts[i].y = gridYArys[0][sub1];
                sub1++;
            } else if (i >= gridCols && i < gridRows + gridCols - 1) {
                pts[i].x = gridXArys[sub2][gridCols - 1];
                pts[i].y = gridYArys[sub2][gridCols - 1];
                sub2++;
            } else if (i >= gridRows + gridCols - 1 && i < 2 * gridCols + gridRows - 2) {
                pts[i].x = gridXArys[gridRows - 1][sub3];
                pts[i].y = gridYArys[gridRows - 1][sub3];
                sub3--;
            } else {
                pts[i].x = gridXArys[sub4][0];
                pts[i].y = gridYArys[sub4][0];
                sub4--;
            }
        }
        return pts;
    }

    private int[] getAllPointsFromSideExceptPt(Point2D.Float[] allPtsSide, int[] exceptPtIndex, int sPtSide, boolean direction) {
        ArrayList<Integer> arr1 = new ArrayList<Integer>();
        ArrayList<Integer> arr2 = new ArrayList<Integer>();
        int[] indexArr = null;
        int size = allPtsSide.length;
        int num = 0;
        int[] allIndex = new int[size];
        for (int i = 0; i < size; i++) {
            allIndex[i] = i;
        }
        int resSize = size - exceptPtIndex.length;
        int k = 0;
        int[] resIndexArr = new int[resSize];
        resIndexArr = StrArrayUtil.arrSubtractArr(allIndex, exceptPtIndex);
        for (int i = 0; i < resSize; i++) {
            if (resIndexArr[i] == i) {
                arr1.add(resIndexArr[i]);
            } else {
                arr2.add(resIndexArr[i]);
            }
        }
        switch (sPtSide) {
            case 1://left
                if (!direction) {
                    num = arr1.size() + arr2.size();
                    indexArr = new int[num];
                    if (arr1 != null && !arr1.isEmpty()) {
                        for (int i = arr1.size() - 1; i >= 0; i--) {
                            indexArr[k++] = arr1.get(i);
                        }
                    }
                    if (arr2 != null && !arr2.isEmpty()) {
                        for (int i = arr2.size() - 1; i >= 0; i--) {
                            indexArr[k++] = arr2.get(i);
                        }
                    }
                } else {
                    if (arr2 != null && !arr2.isEmpty()) {
                        num = arr1.size() + arr2.size();
                    } else {
                        num = arr1.size();
                    }
                    indexArr = new int[num];
                    if (arr2 != null && !arr2.isEmpty()) {
                        for (int i = 0; i <= arr2.size() - 1; i++) {
                            indexArr[k++] = arr2.get(i);
                        }
                    }
                    if (arr1 != null && !arr1.isEmpty()) {
                        for (int i = 0; i <= arr1.size() - 1; i++) {
                            indexArr[k++] = arr1.get(i);
                        }
                    }
                }
                break;
            case 2://bottom
                if (!direction) {
                    if (arr2 != null && !arr2.isEmpty()) {
                        num = arr1.size() + arr2.size();
                    } else {
                        num = arr1.size();
                    }

                    indexArr = new int[num];
                    if (arr1 != null && !arr1.isEmpty()) {
                        for (int i = arr1.size() - 1; i >= 0; i--) {
                            indexArr[k++] = arr1.get(i);
                        }
                    }
                    if (arr2 != null && !arr2.isEmpty()) {
                        for (int i = arr2.size() - 1; i >= 0; i--) {
                            indexArr[k++] = arr2.get(i);
                        }
                    }
                } else {
                    num = arr1.size() + arr2.size();
                    indexArr = new int[num];
                    if (arr2 != null && !arr2.isEmpty()) {
                        for (int i = 0; i < arr2.size(); i++) {
                            indexArr[k++] = arr2.get(i);
                        }
                    }
                    if (arr1 != null && !arr1.isEmpty()) {
                        for (int i = 0; i < arr1.size(); i++) {
                            indexArr[k++] = arr1.get(i);
                        }
                    }
                }
                break;
            case 3://right
                if (arr2 != null && !arr2.isEmpty()) {
                    num = arr1.size() + arr2.size();
                } else {
                    num = arr1.size();
                }
                indexArr = new int[num];
                if (direction) {
                    if (arr2 != null && !arr2.isEmpty()) {
                        for (int i = 0; i < arr2.size(); i++) {
                            indexArr[k++] = arr2.get(i);
                        }
                    }
                    if (arr1 != null && !arr1.isEmpty()) {
                        for (int i = 0; i < arr1.size(); i++) {
                            indexArr[k++] = arr1.get(i);
                        }
                    }
                } else {
                    if (arr1 != null && !arr1.isEmpty()) {
                        for (int i = arr1.size() - 1; i >= 0; i--) {
                            indexArr[k++] = arr1.get(i);
                        }
                    }
                    if (arr2 != null && !arr2.isEmpty()) {
                        for (int i = arr2.size() - 1; i >= 0; i--) {
                            indexArr[k++] = arr2.get(i);
                        }
                    }
                }
                break;
            case 4://top
                if (!direction) {
                    num = arr1.size() + arr2.size();
                    indexArr = new int[num];
                    if (arr1 != null && !arr1.isEmpty()) {
                        for (int i = arr1.size() - 1; i >= 0; i--) {
                            indexArr[k++] = arr1.get(i);
                        }
                    }
                    if (arr2 != null && !arr2.isEmpty()) {
                        for (int i = arr2.size() - 1; i >= 0; i--) {
                            indexArr[k++] = arr2.get(i);
                        }
                    }
                } else {
                    if (arr2 != null && !arr2.isEmpty()) {
                        num = arr1.size() + arr2.size();
                    } else {
                        num = arr1.size();
                    }
                    indexArr = new int[num];
                    if (arr2 != null && !arr2.isEmpty()) {
                        for (int i = 0; i < arr2.size(); i++) {
                            indexArr[k++] = arr2.get(i);
                        }
                    }
                    if (arr1 != null && !arr1.isEmpty()) {
                        for (int i = 0; i < arr1.size(); i++) {
                            indexArr[k++] = arr1.get(i);
                        }
                    }
                }
                break;
            default:
                break;
        }

        arr1.clear();
        arr2.clear();
        return indexArr;
    }

    private int[] getAllPtsFromSide(int sRow, int sCol, int eRow, int eCol, int sPtSide, boolean direction, Point2D.Float[] allPtsSide) {
        int[] indexArr = null;
        int size = allPtsSide.length - 1;
        int num = 0;
        int sIndex1 = 0;
        int eIndex1 = 0;
        int sIndex2 = 0;
        int eIndex2 = 0;
        int k = 0;
        switch (sPtSide) {
            case 1://left
                if (sCol != 0) {
                    if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                    return null;
                }
                if (direction) {
                    if (eCol == 0) {//left left
                        sIndex1 = size - (eRow - 1);
                        eIndex1 = size - (sRow - 1);
                    } else if (eCol == gridCols - 1) {//left right
                        sIndex1 = gridCols - 1 + eRow;
                        eIndex1 = size - (sRow - 1);
                    } else if (eRow == 0) {//left bottom
                        if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                        return null;
                    } else if (eRow == gridRows - 1) {//left top
                        sIndex1 = 2 * gridCols + gridRows - 3 - eCol;
                        eIndex1 = size - (sRow - 1);
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                        return null;
                    }
                    num = Math.abs(eIndex1 - sIndex1) + 1;
                    if (num > 0) {
                        indexArr = new int[num];
                        if (eIndex1 >= sIndex1) {
                            for (int i = sIndex1; i <= eIndex1; i++) {
                                indexArr[k++] = i;
                            }
                        } else {
                            for (int i = sIndex1; i >= eIndex1; i--) {
                                indexArr[k++] = i;
                            }
                        }
//					for(int i=sIndex1; i<=eIndex1; i++){
//						indexArr[k++] = i;
//					}
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                        return null;
                    }
                } else {
                    if (eRow != 0) {//left bottom
                        if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                        return null;
                    }
                    sIndex1 = eCol;
                    eIndex1 = 0;
                    int num1 = Math.abs(sIndex1 - eIndex1) + 1;
                    int num2 = 0;
                    if (sRow > 0) {
                        sIndex2 = size;
                        eIndex2 = size - (sRow - 1);
                        num2 = Math.abs(sIndex2 - eIndex2) + 1;

                    }
                    num = num1 + num2;
                    if (num < 1) {
                        if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                        return null;
                    }
                    indexArr = new int[num];
                    if (num1 > 0) {
                        for (int i = sIndex1; i >= eIndex1; i--) {
                            indexArr[k++] = i;
                        }
                    }
                    if (num2 > 0) {
                        for (int i = sIndex2; i >= eIndex2; i--) {
                            indexArr[k++] = i;
                        }
                    }
                }
                break;
            case 2://bottom
                if (sRow != 0) {
                    if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                    return null;
                }
                if (!direction) {
                    if (eRow == 0) {
                        sIndex1 = eCol;
                        eIndex1 = sCol;
                    } else if (eCol == gridCols - 1) {
                        sIndex1 = gridCols - 1 + eRow;
                        eIndex1 = sCol;
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                        return null;
                    }
                    num = Math.abs(sIndex1 - eIndex1) + 1;
                    indexArr = new int[num];
                    if (num > 0) {
                        if (eIndex1 >= sIndex1) {
                            for (int i = sIndex1; i <= eIndex1; i++) {
                                indexArr[k++] = i;
                            }
                        } else {
                            for (int i = sIndex1; i >= eIndex1; i--) {
                                indexArr[k++] = i;
                            }
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                        return null;
                    }
                } else {
                    int num1 = 0;
                    if (eCol == 0) {
                        sIndex1 = size - (eRow - 1);
                        eIndex1 = size;
                    } else if (eRow == gridRows - 1) {
                        sIndex1 = 2 * gridCols + gridRows - 3 - eCol;
                        eIndex1 = size;
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                        return null;
                    }
                    if (sIndex1 <= eIndex1) {
                        num1 = Math.abs(sIndex1 - eIndex1) + 1;
                    }

                    sIndex2 = 0;
                    eIndex2 = sCol;

                    int num2 = Math.abs(sIndex2 - eIndex2) + 1;
                    num = num1 + num2;
                    if (num < 1) {
                        if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                        return null;
                    }
                    indexArr = new int[num];
                    if (num1 > 0) {
                        for (int i = sIndex1; i <= eIndex1; i++) {
                            indexArr[k++] = i;
                        }
                    }
                    if (num2 > 0) {
                        for (int i = sIndex2; i <= eIndex2; i++) {
                            indexArr[k++] = i;
                        }
                    }
                }
                break;
            case 3://right
                if (sCol != gridCols - 1) {
                    if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                    return null;
                }
                if (direction) {
                    if (eRow != 0) {
                        if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                        return null;
                    }
                    sIndex1 = eCol;
                    eIndex1 = gridCols - 1 + sRow;
                    num = Math.abs(sIndex1 - eIndex1) + 1;
                    if (num > 0) {
                        indexArr = new int[num];
                        for (int i = sIndex1; i <= eIndex1; i++) {
                            indexArr[k++] = i;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                        return null;
                    }
                } else {
                    if (eCol == 0) {
                        sIndex1 = size - (eRow - 1);
                        eIndex1 = gridCols - 1 + sRow;
                    } else if (eCol == gridCols - 1) {
                        sIndex1 = gridCols - 1 + eRow;
                        eIndex1 = gridCols - 1 + sRow;
                    } else if (eRow == gridRows - 1) {
                        sIndex1 = 2 * gridCols + gridRows - 3 - eCol;
                        eIndex1 = gridCols - 1 + sRow;
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                        return null;
                    }
                    num = Math.abs(sIndex1 - eIndex1) + 1;
                    if (num > 0) {
                        indexArr = new int[num];
                        if (eIndex1 <= sIndex1) {
                            for (int i = sIndex1; i >= eIndex1; i--) {
                                indexArr[k++] = i;
                            }
                        } else {
                            for (int i = sIndex1; i <= eIndex1; i++) {
                                indexArr[k++] = i;
                            }
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                        return null;
                    }
                }
                break;
            case 4://top
                if (sRow != gridRows - 1) {
                    if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                    return null;
                }
                if (direction) {
                    if (eCol == gridCols - 1) {
                        sIndex1 = gridCols - 1 + eRow;
                        eIndex1 = 2 * gridCols + gridRows - 3 - sCol;
                    } else if (eRow == gridRows - 1) {
                        sIndex1 = 2 * gridCols + gridRows - 3 - eCol;
                        eIndex1 = 2 * gridCols + gridRows - 3 - sCol;
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                        return null;
                    }
                    num = Math.abs(sIndex1 - eIndex1) + 1;
                    if (num > 0) {
                        indexArr = new int[num];
                        if (eIndex1 >= sIndex1) {
                            for (int i = sIndex1; i <= eIndex1; i++) {
                                indexArr[k++] = i;
                            }
                        } else {
                            for (int i = sIndex1; i >= eIndex1; i--) {
                                indexArr[k++] = i;
                            }
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                        return null;
                    }
                } else {
                    if (eCol == 0) {
                        sIndex1 = size - (eRow - 1);
                        eIndex1 = 2 * gridCols + gridRows - 3 - sCol;
                        num = Math.abs(sIndex1 - eIndex1) + 1;
                        if (num > 0) {
                            indexArr = new int[num];
                            for (int i = sIndex1; i >= eIndex1; i--) {
                                indexArr[k++] = i;
                            }
                        } else {
                            if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                            return null;
                        }
                    } else if (eRow == 0) {
                        sIndex1 = eCol;
                        eIndex1 = 0;
                        int num1 = Math.abs(sIndex1 - eIndex1) + 1;
                        sIndex2 = size;
                        eIndex2 = 2 * gridCols + gridRows - 3 - sCol;
                        int num2 = Math.abs(sIndex2 - eIndex2) + 1;
                        num = num1 + num2;
                        if (num < 1) {
                            if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                            return null;
                        }
                        indexArr = new int[num];
                        if (num1 > 0) {
                            for (int i = sIndex1; i >= eIndex1; i--) {
                                indexArr[k++] = i;
                            }
                        }
                        if (num2 > 0) {
                            for (int i = sIndex2; i >= eIndex2; i--) {
                                indexArr[k++] = i;
                            }
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("增加点时出错");
                        return null;
                    }
                }
                break;
            default:
                break;
        }

        return indexArr;
    }


    /**
     * 构造原始多边形3
     *
     * @return 构造成功返回true，否则为false
     */
    private boolean srcPolygonBuild() {
        if (procIsolineDataList == null || procIsolineDataList.isEmpty()) {
            if (!getIsolineDataProcList()) {
                logger.error("In IsolineProcess orignalPolygonBuild() 等值线追踪失败");
                return false;
            }
        }
        if (procIsolineDataList.isEmpty()) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess orignalPolygonBuild() 无符合level条件的等值线");
            return false;
        }
        //顺时针存储
        Point2D.Float[] allPtsSide = getAllPtsSide();//TODO 方法内部注释及代码是逆时针啊！2014-11-12 db_x
        ArrayList<Point2D.Double> lineList = null;
        Polygon p1 = null;
        Polygon p2 = null;
        double maxLat1, minLat1, maxLon1, minLon1;
        double maxLat2, minLat2, maxLon2, minLon2;
        ArrayList<Integer> p1BoundaryIndexAry = null;
        ArrayList<Integer> p2BoundaryIndexAry = null;
        polygonProcList = new ArrayList<IsolinePolygonProc>();
        IsolinePolygonProc isolinePgnProc = null;
        int index = 0;
        Boolean downHill = null;
        int isSESameSide = 0;
        boolean isFilter = this.attr.isFilter();
        //旋转方向,0:无，1：顺时针方向；2：逆时针方向(xEnd>xStart; yEnd>yStart)
        //旋转方向,0：无，1：逆时针，2：顺时针(xEnd>xStart; yStart>yEnd)
        for (int i = 0; i < procIsolineDataList.size(); i++) {
            int firstDirection = 0;
            int secondDirection = 0;
            int num = procIsolineDataList.get(i).num;
            lineList = procIsolineDataList.get(i).lineList2D;
            p1 = new Polygon();
            p2 = new Polygon();
            maxLat1 = lineList.get(0).y;
            minLat1 = lineList.get(0).y;
            maxLon1 = lineList.get(0).x;
            minLon1 = lineList.get(0).x;
            Point2D.Double point;
//			for(Point2D.Float point:lineList){
            for (int j = 0; j < lineList.size(); j++) {
                point = lineList.get(j);
                p1.addPoint((int) (point.x * epsilon1), (int) (point.y * epsilon1));
                p2.addPoint((int) (point.x * epsilon1), (int) (point.y * epsilon1));
                if (maxLat1 < point.y) {
                    maxLat1 = point.y;
                }
                if (minLat1 > point.y) {
                    minLat1 = point.y;
                }
                if (maxLon1 < point.x) {
                    maxLon1 = point.x;
                }
                if (minLon1 > point.x) {
                    minLon1 = point.x;
                }
            }
            maxLat2 = maxLat1;
            minLat2 = minLat1;
            maxLon2 = maxLon1;
            minLon2 = minLon1;
//			if(procIsolineDataList.get(i).isClosed && this.attr.isFilter()){
            if (isFilter) {
                if (num < 10 || (Math.max(Math.abs(maxLat1 - minLat1), Math.abs(maxLon1 - minLon1)) < 0.25f)) {
//					logger.println("填充过滤第"+i+"条等值线");
                    continue;
                }
            }

            //构造多边形
            p1BoundaryIndexAry = new ArrayList<Integer>();
            p2BoundaryIndexAry = new ArrayList<Integer>();
            boolean isClosed = procIsolineDataList.get(i).isClosed;
            int[] ptsIndex1 = null;
            int[] ptsIndex2 = null;
            float[] minmaxxy1 = null;
            float[] minmaxxy2 = null;
            int sRow = 0;
            int sCol = 0;
            int eRow = 0;
            int eCol = 0;
            if (!isClosed) {
                switch (procIsolineDataList.get(i).startLineType) {
                    case 1:// left
//					switch (getLineType(end)) {
                        switch (procIsolineDataList.get(i).endLineType) {
                            case 1:// left
                                firstDirection = 2;//逆
                                secondDirection = 1;//顺
                                isSESameSide = 1;//left
                                sRow = procIsolineDataList.get(i).rowsList.get(0) + 1;
                                sCol = 0;
                                eRow = procIsolineDataList.get(i).rowsList.get(num - 1);
                                eCol = 0;
                                ptsIndex1 = getAllPtsFromSide(sRow, sCol, eRow, eCol, 1, true, allPtsSide);
//						ptsIndex1 = getAllPointsFromSide(allPtsSide, lineList.get(0), lineList.get(lineList.size()-1), 1, true);
                                for (int t = 0; t < ptsIndex1.length; t++) {
                                    p1.addPoint((int) (allPtsSide[ptsIndex1[t]].x * epsilon1), (int) (allPtsSide[ptsIndex1[t]].y * epsilon1));
                                    p1BoundaryIndexAry.add(p1.npoints - 1);
                                }
                                if (sRow - 1 < eRow) {
                                    ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 1, false);
                                } else if (sRow - 1 == eRow) {
                                    logger.error("In IsolineProcess orignalPolygonBuild() 由于之前追踪有问题，使得left left增加点时出错，导致构造原始多边形失败");
                                    return false;
                                } else {
                                    ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 1, true);
                                }
//						ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 1, false);
                                for (int t = 0; t < ptsIndex2.length; t++) {
                                    p2.addPoint((int) (allPtsSide[ptsIndex2[t]].x * epsilon1), (int) (allPtsSide[ptsIndex2[t]].y * epsilon1));
                                    p2BoundaryIndexAry.add(p2.npoints - 1);
                                }
                                break;
                            case 2:// bottom
                                firstDirection = 1;
                                secondDirection = 2;
                                sRow = procIsolineDataList.get(i).rowsList.get(0);
                                sCol = 0;
                                eRow = 0;
                                eCol = procIsolineDataList.get(i).colsList.get(num - 1);
                                ptsIndex1 = getAllPtsFromSide(sRow, sCol, eRow, eCol, 1, false, allPtsSide);
//						ptsIndex1 = getAllPointsFromSide(allPtsSide, lineList.get(0), lineList.get(lineList.size()-1), 1, false);
                                for (int t = 0; t < ptsIndex1.length; t++) {
                                    p1.addPoint((int) (allPtsSide[ptsIndex1[t]].x * epsilon1), (int) (allPtsSide[ptsIndex1[t]].y * epsilon1));
                                    p1BoundaryIndexAry.add(p1.npoints - 1);
                                }

                                ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 1, true);
                                for (int t = 0; t < ptsIndex2.length; t++) {
                                    p2.addPoint((int) (allPtsSide[ptsIndex2[t]].x * epsilon1), (int) (allPtsSide[ptsIndex2[t]].y * epsilon1));
                                    p2BoundaryIndexAry.add(p2.npoints - 1);
                                }
                                break;
                            case 3:// right
                                firstDirection = 2;
                                secondDirection = 1;
                                sRow = procIsolineDataList.get(i).rowsList.get(0) + 1;
                                sCol = 0;
                                eRow = procIsolineDataList.get(i).rowsList.get(num - 1) + 1;
                                eCol = gridCols - 1;
                                ptsIndex1 = getAllPtsFromSide(sRow, sCol, eRow, eCol, 1, true, allPtsSide);
//						ptsIndex1 = getAllPointsFromSide(allPtsSide, lineList.get(0), lineList.get(lineList.size()-1), 1, true);
                                for (int t = 0; t < ptsIndex1.length; t++) {
                                    p1.addPoint((int) (allPtsSide[ptsIndex1[t]].x * epsilon1), (int) (allPtsSide[ptsIndex1[t]].y * epsilon1));
                                    p1BoundaryIndexAry.add(p1.npoints - 1);
                                }

                                ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 1, false);
                                for (int t = 0; t < ptsIndex2.length; t++) {
                                    p2.addPoint((int) (allPtsSide[ptsIndex2[t]].x * epsilon1), (int) (allPtsSide[ptsIndex2[t]].y * epsilon1));
                                    p2BoundaryIndexAry.add(p2.npoints - 1);
                                }
                                break;
                            case 4:// top
                                firstDirection = 2;
                                secondDirection = 1;
                                sRow = procIsolineDataList.get(i).rowsList.get(0) + 1;
                                sCol = 0;
                                eRow = gridRows - 1;
                                eCol = procIsolineDataList.get(i).colsList.get(num - 1);
                                ptsIndex1 = getAllPtsFromSide(sRow, sCol, eRow, eCol, 1, true, allPtsSide);
                                for (int t = 0; t < ptsIndex1.length; t++) {
                                    p1.addPoint((int) (allPtsSide[ptsIndex1[t]].x * epsilon1), (int) (allPtsSide[ptsIndex1[t]].y * epsilon1));
                                    p1BoundaryIndexAry.add(p1.npoints - 1);
                                }

                                ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 1, false);
                                for (int t = 0; t < ptsIndex2.length; t++) {
                                    p2.addPoint((int) (allPtsSide[ptsIndex2[t]].x * epsilon1), (int) (allPtsSide[ptsIndex2[t]].y * epsilon1));
                                    p2BoundaryIndexAry.add(p2.npoints - 1);
                                }
                                break;
                            default:
                                break;
                        }
                        break;
                    case 2:// bottom
//					switch (getLineType(end)) {
                        switch (procIsolineDataList.get(i).endLineType) {
                            case 1:// left
                                firstDirection = 2;
                                secondDirection = 1;
                                sRow = 0;
                                sCol = procIsolineDataList.get(i).colsList.get(0);
                                eRow = procIsolineDataList.get(i).rowsList.get(num - 1);
                                eCol = 0;
                                ptsIndex1 = getAllPtsFromSide(sRow, sCol, eRow, eCol, 2, true, allPtsSide);
                                for (int t = 0; t < ptsIndex1.length; t++) {
                                    p1.addPoint((int) (allPtsSide[ptsIndex1[t]].x * epsilon1), (int) (allPtsSide[ptsIndex1[t]].y * epsilon1));
                                    p1BoundaryIndexAry.add(p1.npoints - 1);
                                }
                                ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 2, false);
                                for (int t = 0; t < ptsIndex2.length; t++) {
                                    p2.addPoint((int) (allPtsSide[ptsIndex2[t]].x * epsilon1), (int) (allPtsSide[ptsIndex2[t]].y * epsilon1));
                                    p2BoundaryIndexAry.add(p2.npoints - 1);
                                }
                                break;
                            case 2:// bottom
                                firstDirection = 1;
                                isSESameSide = 2;//bottom
                                secondDirection = 2;
                                sRow = 0;
                                sCol = procIsolineDataList.get(i).colsList.get(0) + 1;
                                eRow = 0;
                                eCol = procIsolineDataList.get(i).colsList.get(num - 1);
                                ptsIndex1 = getAllPtsFromSide(sRow, sCol, eRow, eCol, 2, false, allPtsSide);
//						ptsIndex1 = getAllPointsFromSide(allPtsSide, lineList.get(0), lineList.get(lineList.size()-1), 2, false);
                                for (int t = 0; t < ptsIndex1.length; t++) {
                                    p1.addPoint((int) (allPtsSide[ptsIndex1[t]].x * epsilon1), (int) (allPtsSide[ptsIndex1[t]].y * epsilon1));
                                    p1BoundaryIndexAry.add(p1.npoints - 1);
                                }
                                if (sCol - 1 < eCol) {
                                    ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 2, true);
                                } else if (sCol - 1 == eCol) {
                                    logger.error("In IsolineProcess orignalPolygonBuild() 由于之前追踪有问题，使得bottom bottom增加点时出错，导致构造原始多边形失败");
                                    return false;
                                } else {
                                    ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 2, false);
                                }
//						ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 2, true);
                                for (int t = 0; t < ptsIndex2.length; t++) {
                                    p2.addPoint((int) (allPtsSide[ptsIndex2[t]].x * epsilon1), (int) (allPtsSide[ptsIndex2[t]].y * epsilon1));
                                    p2BoundaryIndexAry.add(p2.npoints - 1);
                                }
                                break;
                            case 3:// right
                                firstDirection = 1;
                                secondDirection = 2;
                                sRow = 0;
                                sCol = procIsolineDataList.get(i).colsList.get(0) + 1;
                                eRow = procIsolineDataList.get(i).rowsList.get(num - 1);
                                eCol = gridCols - 1;
                                ptsIndex1 = getAllPtsFromSide(sRow, sCol, eRow, eCol, 2, false, allPtsSide);
//						ptsIndex1 = getAllPointsFromSide(allPtsSide, lineList.get(0), lineList.get(lineList.size()-1), 2, false);
                                for (int t = 0; t < ptsIndex1.length; t++) {
                                    p1.addPoint((int) (allPtsSide[ptsIndex1[t]].x * epsilon1), (int) (allPtsSide[ptsIndex1[t]].y * epsilon1));
                                    p1BoundaryIndexAry.add(p1.npoints - 1);
                                }
                                ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 2, true);
                                for (int t = 0; t < ptsIndex2.length; t++) {
                                    p2.addPoint((int) (allPtsSide[ptsIndex2[t]].x * epsilon1), (int) (allPtsSide[ptsIndex2[t]].y * epsilon1));
                                    p2BoundaryIndexAry.add(p2.npoints - 1);
                                }
                                break;
                            case 4:// top
                                firstDirection = 2;
                                secondDirection = 1;
                                sRow = 0;
                                sCol = procIsolineDataList.get(i).colsList.get(0);
                                eRow = gridRows - 1;
                                eCol = procIsolineDataList.get(i).colsList.get(num - 1);
                                ptsIndex1 = getAllPtsFromSide(sRow, sCol, eRow, eCol, 2, true, allPtsSide);
//						ptsIndex1 = getAllPointsFromSide(allPtsSide, lineList.get(0), lineList.get(lineList.size()-1), 2, true);
                                for (int t = 0; t < ptsIndex1.length; t++) {
                                    p1.addPoint((int) (allPtsSide[ptsIndex1[t]].x * epsilon1), (int) (allPtsSide[ptsIndex1[t]].y * epsilon1));
                                    p1BoundaryIndexAry.add(p1.npoints - 1);
                                }
                                ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 2, false);
                                for (int t = 0; t < ptsIndex2.length; t++) {
                                    p2.addPoint((int) (allPtsSide[ptsIndex2[t]].x * epsilon1), (int) (allPtsSide[ptsIndex2[t]].y * epsilon1));
                                    p2BoundaryIndexAry.add(p2.npoints - 1);
                                }
                                break;
                            default:
                                break;
                        }
                        break;
                    case 3:// right
                        switch (procIsolineDataList.get(i).endLineType) {
                            case 1:// left
                                firstDirection = 1;
                                secondDirection = 2;
                                sRow = procIsolineDataList.get(i).rowsList.get(0) + 1;
                                sCol = gridCols - 1;
                                eRow = procIsolineDataList.get(i).rowsList.get(num - 1) + 1;
                                eCol = 0;
                                ptsIndex1 = getAllPtsFromSide(sRow, sCol, eRow, eCol, 3, false, allPtsSide);
//						ptsIndex1 = getAllPointsFromSide(allPtsSide, lineList.get(0), lineList.get(lineList.size()-1), 3, false);
                                for (int t = 0; t < ptsIndex1.length; t++) {
                                    p1.addPoint((int) (allPtsSide[ptsIndex1[t]].x * epsilon1), (int) (allPtsSide[ptsIndex1[t]].y * epsilon1));
                                    p1BoundaryIndexAry.add(p1.npoints - 1);
                                }
                                ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 3, true);
                                for (int t = 0; t < ptsIndex2.length; t++) {
                                    p2.addPoint((int) (allPtsSide[ptsIndex2[t]].x * epsilon1), (int) (allPtsSide[ptsIndex2[t]].y * epsilon1));
                                    p2BoundaryIndexAry.add(p2.npoints - 1);
                                }
                                break;
                            case 2:// bottom
                                firstDirection = 2;
                                secondDirection = 1;
                                sRow = procIsolineDataList.get(i).rowsList.get(0);
                                sCol = gridCols - 1;
                                eRow = 0;
                                eCol = procIsolineDataList.get(i).colsList.get(num - 1) + 1;
                                ptsIndex1 = getAllPtsFromSide(sRow, sCol, eRow, eCol, 3, true, allPtsSide);
//						ptsIndex1 = getAllPointsFromSide(allPtsSide, lineList.get(0), lineList.get(lineList.size()-1), 3, true);
                                for (int t = 0; t < ptsIndex1.length; t++) {
                                    p1.addPoint((int) (allPtsSide[ptsIndex1[t]].x * epsilon1), (int) (allPtsSide[ptsIndex1[t]].y * epsilon1));
                                    p1BoundaryIndexAry.add(p1.npoints - 1);
                                }
                                ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 3, false);
                                for (int t = 0; t < ptsIndex2.length; t++) {
                                    p2.addPoint((int) (allPtsSide[ptsIndex2[t]].x * epsilon1), (int) (allPtsSide[ptsIndex2[t]].y * epsilon1));
                                    p2BoundaryIndexAry.add(p2.npoints - 1);
                                }
                                break;
                            case 3:// right
                                firstDirection = 1;
                                isSESameSide = 3;//right
                                secondDirection = 2;
                                sRow = procIsolineDataList.get(i).rowsList.get(0) + 1;
                                sCol = gridCols - 1;
                                eRow = procIsolineDataList.get(i).rowsList.get(num - 1);
                                eCol = gridCols - 1;
                                ptsIndex1 = getAllPtsFromSide(sRow, sCol, eRow, eCol, 3, false, allPtsSide);
//						ptsIndex1 = getAllPointsFromSide(allPtsSide, lineList.get(0), lineList.get(lineList.size()-1), 3, false);
                                for (int t = 0; t < ptsIndex1.length; t++) {
                                    p1.addPoint((int) (allPtsSide[ptsIndex1[t]].x * epsilon1), (int) (allPtsSide[ptsIndex1[t]].y * epsilon1));
                                    p1BoundaryIndexAry.add(p1.npoints - 1);
                                }
                                if (sRow - 1 < eRow) {
                                    ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 3, true);
                                } else if (sRow - 1 == eRow) {
                                    logger.error("In IsolineProcess orignalPolygonBuild() 由于之前追踪有问题，使得right right增加点时出错，导致构造原始多边形失败");
                                    return false;
                                } else {
                                    ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 3, false);
                                }

                                for (int t = 0; t < ptsIndex2.length; t++) {
                                    p2.addPoint((int) (allPtsSide[ptsIndex2[t]].x * epsilon1), (int) (allPtsSide[ptsIndex2[t]].y * epsilon1));
                                    p2BoundaryIndexAry.add(p2.npoints - 1);
                                }
                                break;
                            case 4:// top
                                firstDirection = 1;
                                secondDirection = 2;
                                sRow = procIsolineDataList.get(i).rowsList.get(0) + 1;
                                sCol = gridCols - 1;
                                eRow = gridRows - 1;
                                eCol = procIsolineDataList.get(i).colsList.get(num - 1) + 1;
                                ptsIndex1 = getAllPtsFromSide(sRow, sCol, eRow, eCol, 3, false, allPtsSide);
//						ptsIndex1 = getAllPointsFromSide(allPtsSide, lineList.get(0), lineList.get(lineList.size()-1), 3, false);
                                for (int t = 0; t < ptsIndex1.length; t++) {
                                    p1.addPoint((int) (allPtsSide[ptsIndex1[t]].x * epsilon1), (int) (allPtsSide[ptsIndex1[t]].y * epsilon1));
                                    p1BoundaryIndexAry.add(p1.npoints - 1);
                                }
                                ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 3, true);
                                for (int t = 0; t < ptsIndex2.length; t++) {
                                    p2.addPoint((int) (allPtsSide[ptsIndex2[t]].x * epsilon1), (int) (allPtsSide[ptsIndex2[t]].y * epsilon1));
                                    p2BoundaryIndexAry.add(p2.npoints - 1);
                                }
                                break;
                            default:
                                break;
                        }
                        break;
                    case 4:// top
//					switch (getLineType(end)) {
                        switch (procIsolineDataList.get(i).endLineType) {
                            case 1:// left
                                firstDirection = 1;
                                secondDirection = 2;
                                sRow = gridRows - 1;
                                sCol = procIsolineDataList.get(i).colsList.get(0);
                                eRow = procIsolineDataList.get(i).rowsList.get(num - 1) + 1;
                                eCol = 0;
                                ptsIndex1 = getAllPtsFromSide(sRow, sCol, eRow, eCol, 4, false, allPtsSide);
//						ptsIndex1 = getAllPointsFromSide(allPtsSide, lineList.get(0), lineList.get(lineList.size()-1), 4, false);
                                for (int t = 0; t < ptsIndex1.length; t++) {
                                    p1.addPoint((int) (allPtsSide[ptsIndex1[t]].x * epsilon1), (int) (allPtsSide[ptsIndex1[t]].y * epsilon1));
                                    p1BoundaryIndexAry.add(p1.npoints - 1);
                                }
                                ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 4, true);
                                for (int t = 0; t < ptsIndex2.length; t++) {
                                    p2.addPoint((int) (allPtsSide[ptsIndex2[t]].x * epsilon1), (int) (allPtsSide[ptsIndex2[t]].y * epsilon1));
                                    p2BoundaryIndexAry.add(p2.npoints - 1);
                                }
                                break;
                            case 2:// bottom
                                firstDirection = 2;
                                secondDirection = 1;
                                sRow = gridRows - 1;
                                sCol = procIsolineDataList.get(i).colsList.get(0);
                                eRow = 0;
                                eCol = procIsolineDataList.get(i).colsList.get(num - 1);
                                ptsIndex1 = getAllPtsFromSide(sRow, sCol, eRow, eCol, 4, false, allPtsSide);
//						ptsIndex1 = getAllPointsFromSide(allPtsSide, lineList.get(0), lineList.get(lineList.size()-1), 4, false);
                                for (int t = 0; t < ptsIndex1.length; t++) {
                                    p1.addPoint((int) (allPtsSide[ptsIndex1[t]].x * epsilon1), (int) (allPtsSide[ptsIndex1[t]].y * epsilon1));
                                    p1BoundaryIndexAry.add(p1.npoints - 1);
                                }
                                ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 4, true);
                                for (int t = 0; t < ptsIndex2.length; t++) {
                                    p2.addPoint((int) (allPtsSide[ptsIndex2[t]].x * epsilon1), (int) (allPtsSide[ptsIndex2[t]].y * epsilon1));
                                    p2BoundaryIndexAry.add(p2.npoints - 1);
                                }
                                break;
                            case 3:// right
                                firstDirection = 2;
                                secondDirection = 1;
                                sRow = gridRows - 1;
                                sCol = procIsolineDataList.get(i).colsList.get(0) + 1;
                                eRow = procIsolineDataList.get(i).rowsList.get(num - 1) + 1;
                                eCol = gridCols - 1;
                                ptsIndex1 = getAllPtsFromSide(sRow, sCol, eRow, eCol, 4, true, allPtsSide);
//						ptsIndex1 = getAllPointsFromSide(allPtsSide, lineList.get(0), lineList.get(lineList.size()-1), 4, true);
                                for (int t = 0; t < ptsIndex1.length; t++) {
                                    p1.addPoint((int) (allPtsSide[ptsIndex1[t]].x * epsilon1), (int) (allPtsSide[ptsIndex1[t]].y * epsilon1));
                                    p1BoundaryIndexAry.add(p1.npoints - 1);
                                }
                                ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 4, false);
                                for (int t = 0; t < ptsIndex2.length; t++) {
                                    p2.addPoint((int) (allPtsSide[ptsIndex2[t]].x * epsilon1), (int) (allPtsSide[ptsIndex2[t]].y * epsilon1));
                                    p2BoundaryIndexAry.add(p2.npoints - 1);
                                }
                                break;
                            case 4:// top
                                firstDirection = 2;
                                isSESameSide = 4;//top
                                secondDirection = 1;
                                sRow = gridRows - 1;
                                sCol = procIsolineDataList.get(i).colsList.get(0) + 1;
                                eRow = gridRows - 1;
                                eCol = procIsolineDataList.get(i).colsList.get(num - 1);
                                ptsIndex1 = getAllPtsFromSide(sRow, sCol, eRow, eCol, 4, true, allPtsSide);
//						ptsIndex1 = getAllPointsFromSide(allPtsSide, lineList.get(0), lineList.get(lineList.size()-1), 4, true);
                                for (int t = 0; t < ptsIndex1.length; t++) {
                                    p1.addPoint((int) (allPtsSide[ptsIndex1[t]].x * epsilon1), (int) (allPtsSide[ptsIndex1[t]].y * epsilon1));
                                    p1BoundaryIndexAry.add(p1.npoints - 1);
                                }
                                if (sCol - 1 < eCol) {
                                    ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 4, false);
                                } else if (sCol - 1 == eCol) {
                                    logger.error("In IsolineProcess orignalPolygonBuild() 由于之前追踪有问题，使得top top增加点时出错，导致构造原始多边形失败");
                                    return false;
                                } else {
                                    ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 4, true);
                                }
//						ptsIndex2 = getAllPointsFromSideExceptPt(allPtsSide, ptsIndex1, 4, false);
                                for (int t = 0; t < ptsIndex2.length; t++) {
                                    p2.addPoint((int) (allPtsSide[ptsIndex2[t]].x * epsilon1), (int) (allPtsSide[ptsIndex2[t]].y * epsilon1));
                                    p2BoundaryIndexAry.add(p2.npoints - 1);
                                }
                                break;
                            default:
                                break;
                        }
                        break;
                    default:
                        break;
                }
            }
            if (ptsIndex1 != null && ptsIndex2 != null) {
                minmaxxy1 = getMaxMin(allPtsSide, ptsIndex1, lineList);//TODO 上面已经有lineList的边界了 可以直接用来比较没必要方法内再循环求一次了 ！db_x 2014-11-12
                minLon1 = minmaxxy1[0];
                maxLon1 = minmaxxy1[1];
                minLat1 = minmaxxy1[2];
                maxLat1 = minmaxxy1[3];
                minmaxxy2 = getMaxMin(allPtsSide, ptsIndex2, lineList);
                minLon2 = minmaxxy2[0];
                maxLon2 = minmaxxy2[1];
                minLat2 = minmaxxy2[2];
                maxLat2 = minmaxxy2[3];
            }

            procIsolineDataList.get(i).polygonFirst = p1;
            procIsolineDataList.get(i).polygonFirstMaxLat = maxLat1;
            procIsolineDataList.get(i).polygonFirstMinLat = minLat1;
            procIsolineDataList.get(i).polygonFirstMaxLon = maxLon1;
            procIsolineDataList.get(i).polygonFirstMinLon = minLon1;

            if (isClosed) {
                procIsolineDataList.get(i).pgFirstBoundaryIndexAry.add(-1);
            } else {
                procIsolineDataList.get(i).pgFirstBoundaryIndexAry = p1BoundaryIndexAry;
            }
            procIsolineDataList.get(i).firstPolygonDirection = firstDirection;

            downHill = isDown(procIsolineDataList.get(i), p1);//当前等值线是down 还是hill
            if (downHill == null) {
                if (logger.isDebugEnabled())
                    logger.debug("In IsolineProcess orignalPolygonBuild() 第" + i + "条等值线获取downHill为null, 构造原始多边形失败");
                return false;
            }
            isolinePgnProc = new IsolinePolygonProc();
            isolinePgnProc.val = procIsolineDataList.get(i).val;
            isolinePgnProc.index = index;
            isolinePgnProc.isDown = downHill;
            isolinePgnProc.polygon = p1;
            isolinePgnProc.maxLat = maxLat1;
            isolinePgnProc.minLat = minLat1;
            isolinePgnProc.maxLon = maxLon1;
            isolinePgnProc.minLon = minLon1;
            isolinePgnProc.direction = firstDirection;
            isolinePgnProc.isSESameSide = isSESameSide;
            isolinePgnProc.onIsolineIndex = procIsolineDataList.get(i).indexProc;
            isolinePgnProc.polygonBoundaryIndexAry = procIsolineDataList.get(i).pgFirstBoundaryIndexAry;
            if (isClosed) {
                isolinePgnProc.neighborhoodIndex = -1;
            } else {
                isolinePgnProc.neighborhoodIndex = index + 1;
            }
            polygonProcList.add(isolinePgnProc);
            index++;

            if (procIsolineDataList.get(i).isClosed) {
                procIsolineDataList.get(i).polygonSecond = null;
                procIsolineDataList.get(i).polygonSecondMaxLat = Double.MAX_VALUE;
                procIsolineDataList.get(i).polygonSecondMinLat = Double.MIN_VALUE;
                procIsolineDataList.get(i).polygonSecondMaxLon = Double.MAX_VALUE;
                procIsolineDataList.get(i).polygonSecondMinLon = Double.MIN_VALUE;
                procIsolineDataList.get(i).pgSecondBoundaryIndexAry = null;
                procIsolineDataList.get(i).secondPolygonDirection = 0;
            } else {
                procIsolineDataList.get(i).polygonSecond = p2;
                procIsolineDataList.get(i).polygonSecondMaxLat = maxLat2;
                procIsolineDataList.get(i).polygonSecondMinLat = minLat2;
                procIsolineDataList.get(i).polygonSecondMaxLon = maxLon2;
                procIsolineDataList.get(i).polygonSecondMinLon = minLon2;
                procIsolineDataList.get(i).pgSecondBoundaryIndexAry = p2BoundaryIndexAry;
                procIsolineDataList.get(i).secondPolygonDirection = secondDirection;

                isolinePgnProc = new IsolinePolygonProc();
                isolinePgnProc.val = procIsolineDataList.get(i).val;
                isolinePgnProc.index = index;
                isolinePgnProc.isDown = !downHill;
                isolinePgnProc.polygon = p2;
                isolinePgnProc.maxLat = maxLat2;
                isolinePgnProc.minLat = minLat2;
                isolinePgnProc.maxLon = maxLon2;
                isolinePgnProc.minLon = minLon2;
                isolinePgnProc.polygonBoundaryIndexAry = p2BoundaryIndexAry;
                isolinePgnProc.direction = secondDirection;
                isolinePgnProc.isSESameSide = isSESameSide;
                isolinePgnProc.onIsolineIndex = procIsolineDataList.get(i).indexProc;
                isolinePgnProc.neighborhoodIndex = index - 1;
                polygonProcList.add(isolinePgnProc);
                index++;
            }
        }
        return true;
    }


    private float[] getMaxMin(Point2D.Float[] allPtsSide, int[] ptsIndex, ArrayList<Point2D.Double> lineList) {
        float maxX1 = allPtsSide[ptsIndex[0]].x;
        float minX1 = maxX1;
        float maxY1 = allPtsSide[ptsIndex[0]].y;
        float minY1 = maxY1;
        for (int i = 0; i < ptsIndex.length; i++) {
            if (allPtsSide[ptsIndex[i]].x > maxX1) {
                maxX1 = allPtsSide[ptsIndex[i]].x;
            }
            if (allPtsSide[ptsIndex[i]].x < minX1) {
                minX1 = allPtsSide[ptsIndex[i]].x;
            }
            if (allPtsSide[ptsIndex[i]].y > maxY1) {
                maxY1 = allPtsSide[ptsIndex[i]].y;
            }
            if (allPtsSide[ptsIndex[i]].y < minY1) {
                minY1 = allPtsSide[ptsIndex[i]].y;
            }
        }

        float maxX2 = (float) lineList.get(0).x;
        float minX2 = maxX2;
        float maxY2 = (float) lineList.get(0).y;
        float minY2 = maxY2;
        for (int i = 0; i < lineList.size(); i++) {
            if (lineList.get(i).x > maxX2) {
                maxX2 = (float) lineList.get(i).x;
            }
            if (lineList.get(i).x < minX2) {
                minX2 = (float) lineList.get(i).x;
            }
            if (lineList.get(i).y > maxY2) {
                maxY2 = (float) lineList.get(i).y;
            }
            if (lineList.get(i).y < minY2) {
                minY2 = (float) lineList.get(i).y;
            }
        }
        float[] minmaxxy = new float[4];
        minmaxxy[0] = minX1 < minX2 ? minX1 : minX2;
        minmaxxy[1] = maxX1 > maxX2 ? maxX1 : maxX2;
        minmaxxy[2] = minY1 < minY2 ? minY1 : minY2;
        minmaxxy[3] = maxY1 > maxY2 ? maxY1 : maxY2;

        return minmaxxy;
    }

    /**
     * 计算多边形内的最值
     *
     * @param p1       多边形
     * @param colsList 列list
     * @param rowsList 行list
     * @return 最大最小值及位置
     */
    @SuppressWarnings("unchecked")
    private MaxMinValPos getPolygonMaxMinVal(Polygon p1, ArrayList<Integer> colsList, ArrayList<Integer> rowsList) {
        MaxMinValPos maxminValPos = new MaxMinValPos();
        ArrayList<Integer> colsListClone = (ArrayList<Integer>) colsList.clone();
        ArrayList<Integer> rowsListClone = (ArrayList<Integer>) rowsList.clone();
        Collections.sort(colsListClone);
        Collections.sort(rowsListClone);

        int colMax = colsListClone.get(colsListClone.size() - 1);
        int colMin = colsListClone.get(0);
        int rowMax = rowsListClone.get(rowsListClone.size() - 1);
        int rowMin = rowsListClone.get(0);

        float pMax = Float.MAX_VALUE;
        float pMin = Float.MIN_VALUE;
        float tmpMinVal = Float.MIN_VALUE;
        float tmpMaxVal = Float.MAX_VALUE;
        boolean flag = false;
        Point2D.Double point = null;
        Point2D.Double pointRowCol = null;
        int num = (rowMax - rowMin) * (colMax - colMin);
        ArrayList<Point2D.Double> maxPosList = new ArrayList<Point2D.Double>(num);
        ArrayList<Point2D.Double> minPosList = new ArrayList<Point2D.Double>(num);
        for (int i = rowMin; i <= rowMax; i++) {
            for (int j = colMin; j <= colMax; j++) {
                point = getPos(i, j);
                boolean isSidePt = (i == rowMin || i == rowMax || j == colMin || j == colMax);
                if (isPolygonContainPoint((int) (point.x * epsilon1), (int) (point.y * epsilon1), isSidePt, p1)) {
                    pointRowCol = new Point2D.Double();
                    pointRowCol.x = i;
                    pointRowCol.y = j;
                    if (!flag) {
                        tmpMinVal = gridDataArys[i][j];
                        tmpMaxVal = gridDataArys[i][j];
                        pMax = tmpMaxVal;
                        pMin = tmpMinVal;
                        maxPosList.add(pointRowCol);
                        minPosList.add(pointRowCol);
                        flag = true;
                    } else {
                        pMax = tmpMaxVal;
                        pMin = tmpMinVal;

                        if (Math.abs(pMax - gridDataArys[i][j]) < precision) {
                            maxPosList.add(pointRowCol);
                        } else if (pMax < gridDataArys[i][j]) {
                            maxPosList.clear();
                            pMax = gridDataArys[i][j];
                            tmpMaxVal = pMax;
                            maxPosList.add(pointRowCol);
                        }

                        if (Math.abs(pMin - gridDataArys[i][j]) < precision) {
                            minPosList.add(pointRowCol);
                        } else if (pMin > gridDataArys[i][j]) {
                            minPosList.clear();
                            pMin = gridDataArys[i][j];
                            tmpMinVal = pMin;
                            minPosList.add(pointRowCol);
                        }
                    }
                }
            }
        }
        maxminValPos.maxValPosList = maxPosList;
        maxminValPos.minValPosList = minPosList;
        maxminValPos.maxVal = pMax;
        maxminValPos.minVal = pMin;
        return maxminValPos;
    }


    /**
     * 获取给定行列的网格数据的位置
     *
     * @param row 行号
     * @param col 列号
     * @return 所在行列的经纬度
     */
    private Point2D.Double getPos(int row, int col) {
        Point2D.Double pos = new Point2D.Double();
        pos.x = gridXArys[row][col];
        pos.y = gridDataList.get(0).getLatAry2D()[row][col];
        return pos;
    }

    /**
     * 获取给定边界点的起始位置(类型)//存在空值的数据不能构成多边形，不能填充
     *
     * @param point 点的坐标
     * @return 1：left；2：bottom；3：right；4：top；0：center
     */
    private int getLineType(Isopoint point) {
        int lineType = 0;
        Boolean isHorizon = point.getIsHorizon();
        if (isHorizon != null) {
            if (point.getRow() == 0 && isHorizon) {
                lineType = 2;
            } else if (point.getCol() == 0 && !isHorizon) {
                lineType = 1;
            } else if (point.getRow() == gridRows - 1 && isHorizon) {
                lineType = 4;
            } else if (point.getCol() == gridCols - 1 && !isHorizon) {
                lineType = 3;
            } else {
                lineType = 0;
            }
        } else {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess() getLineType()未找到点所在边的类型");
            return 9999;
        }
        return lineType;
    }


    /**
     * 是否存在空值
     *
     * @param prePoint
     * @param curPoint
     * @return
     */
    private Boolean isHasNullPoint(Isopoint prePoint, Isopoint curPoint) {
        int row = curPoint.getRow();
        int col = curPoint.getCol();
        Boolean isHorizon = curPoint.getIsHorizon();
        if (isHorizon != null) {
            if (isHorizon) {//横边
                if (curPoint.getRow() > prePoint.getRow()) {//自下向上
                    if (row > gridRows - 1) {
                        if (logger.isDebugEnabled())
                            logger.debug("判断是否存在空值时，已超过边界,可能追踪出错");
                        return null;
                    }
                    if (row == gridRows - 1) {
                        if (logger.isDebugEnabled())
                            logger.debug("判断是否存在空值时，已到达边界");
                        return true;//结束追踪
                    }
                    float val1 = gridDataArys[row + 1][col];
                    float val2 = gridDataArys[row + 1][col + 1];
                    if (val1 == NULLVAL && val2 == NULLVAL) {
                        return true;//结束追踪
                    } else if (val1 == NULLVAL && val2 != NULLVAL) {
                        if (!ySide[row][col + 1].isHasIsopoint()) {
                            return true;//结束
                        }
                    } else if (val1 != NULLVAL && val2 == NULLVAL) {
                        if (!ySide[row][col].isHasIsopoint()) {
                            return true;//结束
                        }
                    } else {
                        return false;
                    }
                } else {//自上向下
                    if (row < 0) {
                        if (logger.isDebugEnabled())
                            logger.debug("判断是否存在空值时，已超过边界,可能追踪出错");
                        return null;
                    }
                    if (row == 0) {
                        if (logger.isDebugEnabled())
                            logger.debug("判断是否存在空值时，已到达边界");
                        return true;
                    }
                    float val1 = gridDataArys[row - 1][col];
                    float val2 = gridDataArys[row - 1][col + 1];
                    if (val1 == NULLVAL && val2 == NULLVAL) {
                        return true;//结束追踪
                    } else if (val1 == NULLVAL && val2 != NULLVAL) {
                        if (!ySide[row - 1][col + 1].isHasIsopoint()) {
                            return true;//结束
                        }
                    } else if (val1 != NULLVAL && val2 == NULLVAL) {
                        if (!ySide[row - 1][col].isHasIsopoint()) {
                            return true;//结束
                        }
                    } else {
                        return false;
                    }
                }
            } else {//纵边
                if (curPoint.getCol() > prePoint.getCol()) {//自左向右
                    if (col > gridCols - 1) {
                        if (logger.isDebugEnabled())
                            logger.debug("判断是否存在空值时，已超过边界,可能追踪出错");
                        return null;
                    }
                    if (col == gridCols - 1) {
                        if (logger.isDebugEnabled())
                            logger.debug("判断是否存在空值时，已到达边界");
                        return true;
                    }
                    float val1 = gridDataArys[row][col + 1];
                    float val2 = gridDataArys[row + 1][col + 1];
                    if (val1 == NULLVAL && val2 == NULLVAL) {
                        return true;//结束追踪
                    } else if (val1 == NULLVAL && val2 != NULLVAL) {
                        if (!xSide[row + 1][col].isHasIsopoint()) {
                            return true;//结束
                        }
                    } else if (val1 != NULLVAL && val2 == NULLVAL) {
                        if (!xSide[row][col].isHasIsopoint()) {
                            return true;//结束
                        }
                    } else {
                        return false;
                    }
                } else {//自右向左
                    if (col < 0) {
                        if (logger.isDebugEnabled())
                            logger.debug("判断是否存在空值时，已超过边界,可能追踪出错");
                        return null;
                    }
                    if (col == 0) {
                        if (logger.isDebugEnabled())
                            logger.debug("判断是否存在空值时，已到达边界");
                        return true;
                    }
                    float val1 = gridDataArys[row][col - 1];
                    float val2 = gridDataArys[row + 1][col - 1];
                    if (val1 == NULLVAL && val2 == NULLVAL) {
                        return true;//结束追踪
                    } else if (val1 == NULLVAL && val2 != NULLVAL) {
                        if (!xSide[row + 1][col - 1].isHasIsopoint()) {
                            return true;//结束
                        }
                    } else if (val1 != NULLVAL && val2 == NULLVAL) {
                        if (!xSide[row][col - 1].isHasIsopoint()) {
                            return true;//结束
                        }
                    } else {
                        return false;
                    }
                }
            }
        } else {
            if (logger.isDebugEnabled()) logger.debug("In IsolineProcess() isHasNullPoint()出错");
            return null;
        }

        return false;
    }

    /**
     * 追踪等值线，获取等直线链表
     *
     * @return true表示追踪成功，false失败
     */
    @SuppressWarnings("unchecked")
    private boolean getIsolineDataProcList() {
        if (attr == null) {
            if (logger.isDebugEnabled())
                logger.debug("等值线追踪前请先设置属性或从配置文件读取属性");
            return false;
        }

		/*if (this.attr.getSrcDataType() == IsolineSrcDataType.ScatterData) {
            if (this.scatterDataList.isEmpty()) {
				logger.println("In IsolineProcess getIsllineDataProcList()读取离散数据失败");
				return false;
			}

			if (scatterDataToGrid()) {// 离散点网格化
				this.attr.setSrcDataType(IsolineSrcDataType.GridData);
			}
			else{
				logger.println("In IsolineProcess中getIsolineDataProcList()时输入数据为离散数据，离散数据网格化失败");
				return false;
			}
		}*/

        //删除同行MM5、WRF//经纬度相同
        if (!deleteSameData()) {
            if (logger.isDebugEnabled()) logger.debug("In IsolineProcess中处理相同经纬度数据失败");
            return false;
        }

        if (this.attr.getSrcDataType() == IsolineSrcDataType.GridData) {
            if (!this.attr.isSetLevel()) {
                float[] minmaxVal = StrArrayUtil.getMinMaxVal(gridDataList.get(0).getGridData(), NULLVAL);
                float minVal = minmaxVal[0];
                float maxVal = minmaxVal[1];
                float[] level = new float[10];
                for (int i = 0; i < 10; i++) {
                    level[i] = minVal + i * (maxVal - minVal) / 9;
                }
                Arrays.sort(level);
                this.attr.setLevel(level);
                setAttr(attr);
            }

            //双线性插值
            if (!hasBilinear && this.attr.getBilinear() != 0) {
                ArrayList<GridData> gridDataListCopy = (ArrayList<GridData>) gridDataList.clone();
                if (gridDataList != null) {
                    gridDataList.clear();
                    gridDataList = new ArrayList<GridData>();
                }
                // 加密
                for (GridData gd : gridDataListCopy) {
                    GridData tmpGridData = MIDS3DMath.interpolation(gd, this.attr.getBilinear());
                    gridDataList.add(tmpGridData);
                }
                if (gridDataList == null || gridDataList.isEmpty()) {
                    if (logger.isDebugEnabled())
                        logger.debug("In IsolineProcess中getIsolineDataProcList()的interpolation()格点数据加密失败");
                    return false;
                }
                hasBilinear = true;
            }
            //平滑
            if (!hasSpline && this.attr.getIsGridDataSpline()) {
                float[][] kernel = new float[][]{{1, 1, 1}, {1, 1, 1}, {1, 1, 1}};
                int r = (int) (kernel.length / 2);
                int c = (int) (kernel[0].length / 2);//此处没必要强制转型  整数做完除法本身就是整数
                int[] gridSplineLevelAry = attr.getGridDataSplineLevelAry();
                int edge = 1;
                for (int i = 0; i < this.attr.getGridDataSplineTimes(); i++) {
                    if (i < gridSplineLevelAry.length) {
                        kernel[r][c] = gridSplineLevelAry[i];
                    } else {
                        kernel[r][c] = gridSplineLevelAry[gridSplineLevelAry.length - 1];
                    }
                    if (kernel[r][c] < 1) {
                        continue;
                    }
                    ArrayList<GridData> gridDataListCopy = (ArrayList<GridData>) gridDataList.clone();
                    if (gridDataList != null) {
                        gridDataList.clear();
                        gridDataList = new ArrayList<GridData>();
                    }
                    for (GridData gd : gridDataListCopy) {
                        int row = gd.getRowNum();
                        int col = gd.getColNum();
                        float[][] tmpData = new float[row][col];
                        tmpData = MIDS3DMath.convol(gd.getGridData(), kernel, edge);
                        GridData tmpGridData = new GridData();
                        tmpGridData.setColNum(col);
                        tmpGridData.setRowNum(row);
                        tmpGridData.setXStart(gd.getXStart());
                        tmpGridData.setXEnd(gd.getXEnd());
                        tmpGridData.setYStart(gd.getYStart());
                        tmpGridData.setYEnd(gd.getYEnd());
                        float deltX = gd.getXDel();
                        float deltY = gd.getYDel();
                        if (deltX != 0.0f) {
                            tmpGridData.setXDel(deltX);
                        }
                        if (deltY != 0.0f) {
                            tmpGridData.setYDel(deltY);
                        }

                        tmpGridData.setGridData(tmpData);
                        tmpGridData.setLatAry2D(gd.getLatAry2D());
                        tmpGridData.setLonAry2D(gd.getLonAry2D());
                        gridDataList.add(tmpGridData);
                    }
                }

                if (gridDataList == null || gridDataList.isEmpty()) {
                    if (logger.isDebugEnabled()) logger.debug("In IsolineProcess中getIsolineDataProcList()的格点数据加密失败");
                    return false;
                }
                hasSpline = true;
            }

            if (this.attr.IsNeedChangeNullVal()) {
                GridData gd = gridDataList.get(0);
                int row = gd.getRowNum();
                int col = gd.getColNum();
                for (int i = 0; i < row; i++) {
                    for (int j = 0; j < col; j++) {
                        if (gd.getGridData()[i][j] == NULLVAL) {
                            gd.getGridData()[i][j] = this.attr.getChangedNullVal();
                        }
                    }
                }
            }

            gridRows = gridDataList.get(0).getRowNum();// 网格数据的行数
            gridCols = gridDataList.get(0).getColNum();// 网格数据的列数
//			deltX = gridDataList.get(0).getXDel();// 网格数据x方向上的间隔
            deltY = gridDataList.get(0).getYDel();// 网格数据y方向上的间隔

            gridDataArys = gridDataList.get(0).getGridData();
            if (gridDataArys == null) {
                if (logger.isDebugEnabled())
                    logger.debug("In IsolineProcess() 获取gridDataArys为空");
                return false;
            }

            gridXArys = gridDataList.get(0).getLonAry2D();
            if (gridXArys == null) {
                if (logger.isDebugEnabled())
                    logger.debug("In IsolineProcess() 获取gridXArys为空");
                return false;
            }

            gridYArys = gridDataList.get(0).getLatAry2D();
            if (gridYArys == null) {
                if (logger.isDebugEnabled())
                    logger.debug("In IsolineProcess() 或取gridYArys为空");
                return false;
            }

            xStartAry = StrArrayUtil.getOneColFromArray(gridXArys, 0);
            xEndAry = StrArrayUtil.getOneColFromArray(gridXArys, gridXArys[0].length - 1);
            yStartAry = StrArrayUtil.getOneRowFromArray(gridYArys, 0);
            yEndAry = StrArrayUtil.getOneRowFromArray(gridYArys, gridYArys.length - 1);

            gridXBottomStart = gridXArys[0][0];
            gridXBottomEnd = gridXArys[0][gridCols - 1];
            gridXTopStart = gridXArys[gridRows - 1][0];
            gridXTopEnd = gridXArys[gridRows - 1][gridCols - 1];
            gridYLeftStart = gridYArys[0][0];
            gridYRightStart = gridYArys[0][gridCols - 1];
            gridYLeftEnd = gridYArys[gridRows - 1][0];
            gridYRightEnd = gridYArys[gridRows - 1][gridCols - 1];

            // 追踪
            if (trace()) {
                return true;
            } else {
                logger.error("In IsolineProcess中trace()追踪等值线失败");
                return false;
            }
        } else {
            logger.error("In IsolineProcess中 getIsolineDataProcList()获取格点数据失败或离散数据网格化失败");
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean deleteSameData() {
        ArrayList<GridData> gridDataListCopy = (ArrayList<GridData>) gridDataList.clone();
        if (gridDataList != null || !gridDataList.isEmpty()) {
            gridDataList.clear();
            gridDataList = new ArrayList<GridData>();
        }
        for (GridData gd : gridDataListCopy) {
            int row = gd.getRowNum();
            int col = gd.getColNum();
            float[][] tmpData = new float[row][col];
            tmpData = gd.getGridData();
            float[][] lonAry = gd.getLonAry2D();
            float[][] latAry = gd.getLatAry2D();
            ArrayList<Integer> indexRowList = new ArrayList<Integer>();
            int k = 0;
            for (int i = 0; i < row - 1; i++) {
                for (int j = 0; j < col; j++) {
                    if (lonAry[i][j] == lonAry[i + 1][j] && latAry[i][j] == latAry[i + 1][j]) {
                        k++;
                    }
                }
                if (k == col) {
                    indexRowList.add(i);
                }
            }
            if (indexRowList != null && !indexRowList.isEmpty()) {
                int newRow = row - indexRowList.size();
                int newCol = col;
                float[][] newData = new float[newRow][newCol];
                float[][] newLonAry = new float[newRow][newCol];
                float[][] newLatAry = new float[newRow][newCol];
                newData = StrArrayUtil.removeRowByIndexList(tmpData, indexRowList);
                newLonAry = StrArrayUtil.removeRowByIndexList(lonAry, indexRowList);
                newLatAry = StrArrayUtil.removeRowByIndexList(latAry, indexRowList);
                tmpData = newData;
                lonAry = new float[newRow][newCol];
                latAry = new float[newRow][newCol];
                lonAry = newLonAry;
                latAry = newLatAry;
                row = newRow;
                col = newCol;
                ArrayList<Integer> indexColList = new ArrayList<Integer>();
                k = 0;
                for (int j = 0; j < col - 1; j++) {
                    for (int i = 0; i < row; i++) {
                        if (lonAry[i][j] == lonAry[i][j + 1] && latAry[i][j] == latAry[i][j + 1]) {
                            k++;
                        }
                    }
                    if (k == row) {
                        indexColList.add(j);
                    }
                }
                if (indexColList != null && !indexColList.isEmpty()) {
                    newRow = row;
                    newCol = col - indexColList.size();
                    newData = new float[newRow][newCol];
                    newLonAry = new float[newRow][newCol];
                    newLatAry = new float[newRow][newCol];
                    newData = StrArrayUtil.removeColByIndexList(tmpData, indexColList);
                    newLonAry = StrArrayUtil.removeColByIndexList(lonAry, indexColList);
                    newLatAry = StrArrayUtil.removeColByIndexList(latAry, indexColList);
                    tmpData = newData;
                    lonAry = new float[newRow][newCol];
                    latAry = new float[newRow][newCol];
                    lonAry = newLonAry;
                    latAry = newLatAry;
                    row = newRow;
                    col = newCol;
                }
            }
            GridData tmpGridData = new GridData();
            tmpGridData.setColNum(col);
            tmpGridData.setRowNum(row);
            tmpGridData.setGridData(tmpData);
            tmpGridData.setLatAry2D(latAry);
            tmpGridData.setLonAry2D(lonAry);
            gridDataList.add(tmpGridData);
        }
        return true;
    }

    private boolean getPolygonInnerOrOuter() {
        if (closedPolygonContainList == null || closedPolygonContainList.isEmpty()) {
            closedIsolinePolygonContainBuild();
        }

        if (closedPolygonContainList.isEmpty()) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess中获取IsolineOnlyPgnListBuild()中closedPolygonContainList为空");
            return false;
        }

        if (polygonList.isEmpty()) {
            for (int i = 0; i < closedPolygonContainList.size(); i++) {
                IsolinePolygonContain polygonContain = closedPolygonContainList.get(i);
//				if(polygonContain.direction==0 && polygonContain.isInOutInner!=null){
//					polygonList.add(polygonContain);
//				}
                //找最外最内多边形 isInner,isOuter一个为true 则为最内或最外 或为单个多边形
                if (polygonContain.direction == 0 && (polygonContain.isOuter || polygonContain.isInner)) {
                    polygonList.add(polygonContain);
                }
            }
        }
        return true;
    }

    /**
     * 根据最内最外多边形的最值找所在等值线的高低中心
     *
     * @param isolineData
     * @return
     */
    private IsolineOnlyPgn IsolineOnlyPgnListBuild(IsolineDataProc isolineData) {
        boolean isFind = getPolygonInnerOrOuter();
        if (isFind) {
            IsolineOnlyPgn polygon = new IsolineOnlyPgn();
            //把最外最内多边形的最值赋值给所在的等值线
            Point2D.Double pos = null;
            for (int j = 0; j < polygonList.size(); j++) {
                if (isolineData.indexProc == polygonList.get(j).onIsolineIndex) {
                    if (polygonList.get(j).maxVal <= isolineData.val) {
                        polygon.highlowVal = isolineData.firstMin;
                        polygon.highlowPosList = new ArrayList<Position>(isolineData.firstMinPos.size() + 1);
                        for (int k = 0; k < isolineData.firstMinPos.size(); k++) {
                            pos = getPos((int) (isolineData.firstMinPos.get(k).x), (int) (isolineData.firstMinPos.get(k).y));
                            polygon.highlowPosList.add(Position.fromDegrees(pos.y, pos.x));
                        }
                        polygon.isHigh = false;
                    } else if (polygonList.get(j).minVal >= isolineData.val) {
                        polygon.highlowVal = isolineData.firstMax;
                        polygon.highlowPosList = new ArrayList<Position>(isolineData.firstMaxPos.size() + 1);
                        for (int k = 0; k < isolineData.firstMaxPos.size(); k++) {
                            pos = getPos((int) (isolineData.firstMaxPos.get(k).x), (int) (isolineData.firstMaxPos.get(k).y));
                            polygon.highlowPosList.add(Position.fromDegrees(pos.y, pos.x));
                        }
                        polygon.isHigh = true;
                    }// end else if
                    else {
                        polygon.highlowVal = Float.MAX_VALUE;
                        polygon.highlowPosList = null;
                        polygon.isHigh = null;
                    }
                    polygon.isInner = polygonList.get(j).isInner;
                    polygon.isOuter = polygonList.get(j).isOuter;
                    break;
                }
            }
            return polygon;
        } else {
            return null;
        }
    }


    /**
     * 获取网格数据列表(离散数据网格化结果或者是给定的网格抽析或加密结果)
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public ArrayList<GridData> getGridData() {
        if (gridDataList == null || gridDataList.isEmpty()) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess中getGridData()时不存在网格数据,也不存在离散数据");
            return null;
        } else {
            if (!hasBilinear && attr.getBilinear() != 0) {
                // 加密
                GridData tmpGridData = MIDS3DMath.interpolation(this.gridDataList.get(0), attr.getBilinear());
                gridDataList = new ArrayList<GridData>();
                gridDataList.add(tmpGridData);
                if (gridDataList.isEmpty()) {
                    if (logger.isDebugEnabled())
                        logger.debug("In IsolineProcess中getGridData()时网格数据加密后为空");
                    return null;
                }
                hasBilinear = true;
            }
            if (!hasSpline && attr.getIsGridDataSpline()) {
                float[][] kernel = new float[][]{{1, 1, 1}, {1, 1, 1}, {1, 1, 1}};
                int r = (int) (kernel.length / 2);
                int c = (int) (kernel[0].length / 2);
                int[] gridSplineLevelAry = attr.getGridDataSplineLevelAry();
                int edge = 1;
                for (int i = 0; i < this.attr.getGridDataSplineTimes(); i++) {
                    if (i < gridSplineLevelAry.length) {
                        kernel[r][c] = gridSplineLevelAry[i];
                    } else {
                        kernel[r][c] = gridSplineLevelAry[gridSplineLevelAry.length - 1];
                    }
                    if (kernel[r][c] < 1) {
                        continue;
                    }
                    ArrayList<GridData> gridDataListCopy = (ArrayList<GridData>) gridDataList.clone();
                    if (gridDataList != null) {
                        gridDataList.clear();
                        gridDataList = new ArrayList<GridData>();
                    }
                    for (GridData gd : gridDataListCopy) {
                        int row = gd.getRowNum();
                        int col = gd.getColNum();
                        float[][] tmpData = new float[row][col];
                        tmpData = MIDS3DMath.convol(gd.getGridData(), kernel, edge);
                        GridData tmpGridData = new GridData();
                        tmpGridData.setColNum(col);
                        tmpGridData.setRowNum(row);
                        tmpGridData.setXStart(gd.getXStart());
                        tmpGridData.setXEnd(gd.getXEnd());
                        tmpGridData.setYStart(gd.getYStart());
                        tmpGridData.setYEnd(gd.getYEnd());
                        float deltX = gd.getXDel();
                        float deltY = gd.getYDel();
                        if (deltX != 0.0f) {
                            tmpGridData.setXDel(deltX);
                        }
                        if (deltY != 0.0f) {
                            tmpGridData.setYDel(deltY);
                        }
                        tmpGridData.setGridData(tmpData);
                        tmpGridData.setLonAry2D(gd.getLonAry2D());
                        tmpGridData.setLatAry2D(gd.getLatAry2D());
                        gridDataList.add(tmpGridData);
                    }
                }

                if (gridDataList == null || gridDataList.isEmpty()) {
                    if (logger.isDebugEnabled()) logger.debug("In IsolineProcess中getGridData()的格点数据加密失败");
                    return null;
                }
                hasSpline = true;
            }
            return gridDataList;
        }
    }

    /**
     * 获取网格数据列表(离散数据网格化结果或者是给定的网格抽析或加密结果)
     *
     * @return
     */
    @SuppressWarnings("unchecked")
/*	public ArrayList<GridData> getGridData() {
		if (gridDataList == null || gridDataList.isEmpty()) {
			if (scatterDataList == null || scatterDataList.isEmpty()) {
				logger.println("In IsolineProcess中getGridData()时不存在网格数据,也不存在离散数据");
				return null;
			} else {
				boolean flag = scatterDataToGrid();
				if (!flag) {
					logger.println("In IsolineProcess中getGridData()时离散数据网格化不成功");
					return null;
				} else {
					if (!hasBilinear && attr.getBilinear() != 0) {
						// 加密
						if(gridDataList==null || gridDataList.isEmpty()){
							logger.println("In IsolineProcess中getGridData()时离散数据网格化后,网格数据依然为空");
							return null;
						}
						GridData tmpGridData = MIDS3DMath.interpolation(this.gridDataList.get(0), attr.getBilinear());
						gridDataList = new ArrayList<GridData>();
						gridDataList.add(tmpGridData);
						if(gridDataList.isEmpty()){
							logger.println("In IsolineProcess中getGridData()时网格数据加密后为空");
							return null;
						}
						hasBilinear = true;
					}
					if(!hasSpline && attr.getIsGridDataSpline()){
						if(gridDataList==null || gridDataList.isEmpty()){
							logger.println("In IsolineProcess中getGridData()时离散数据网格化后,网格数据依然为空");
							return null;
						}

						float[][] kernel = new float[][]{{1,1,1},{1,1,1},{1,1,1}};
						int r = (int) (kernel.length / 2);
						int c = (int) (kernel[0].length / 2);
						int[] gridSplineLevelAry = attr.getGridDataSplineLevelAry();
						int edge = 1;
						for(int i=0; i<this.attr.getGridDataSplineTimes(); i++){
							if(i < gridSplineLevelAry.length){
								kernel[r][c] = gridSplineLevelAry[i];
							}else{
								kernel[r][c] = gridSplineLevelAry[gridSplineLevelAry.length-1];
							}
							if(kernel[r][c]<1){
								continue;
							}
							ArrayList<GridData> gridDataListCopy = (ArrayList<GridData>) gridDataList.clone();
							if(gridDataList!=null){
								gridDataList.clear();
								gridDataList = new ArrayList<GridData>();
							}
							for(GridData gd:gridDataListCopy){
								int row = gd.getRowNum();
								int col = gd.getColNum();
								float[][] tmpData = new float[row][col];
								tmpData = MIDS3DMath.convol(gd.getGridData(), kernel, edge);
								//							GridData tmpGridData = new GridData(gd.getXStart(), gd.getXEnd(), gd.getYStart(), gd.getYEnd(), gd.getXDel(), gd.getYDel());
								GridData tmpGridData = new GridData();
								tmpGridData.setColNum(col);
								tmpGridData.setRowNum(row);
								tmpGridData.setXStart(gd.getXStart());
								tmpGridData.setXEnd(gd.getXEnd());
								tmpGridData.setYStart(gd.getYStart());
								tmpGridData.setYEnd(gd.getYEnd());
								float deltX = gd.getXDel();
								float deltY = gd.getYDel();
								if(deltX != 0.0f){
									tmpGridData.setXDel(deltX);
								}
								if(deltY!=0.0f){
									tmpGridData.setYDel(deltY);
								}
								tmpGridData.setGridData(tmpData);
								tmpGridData.setLonAry2D(gd.getLonAry2D());
								tmpGridData.setLatAry2D(gd.getLatAry2D());
								gridDataList.add(tmpGridData);
							}
						}

						if(gridDataList == null || gridDataList.isEmpty()){
							logger.println("In IsolineProcess中getIsolineDataProcList()的格点数据加密失败");
							return null;
						}
						hasSpline = true;
					}

					return gridDataList;
				}
			}
		} else {
			if (!hasBilinear && attr.getBilinear() != 0) {
				// 加密
				GridData tmpGridData = MIDS3DMath.interpolation(this.gridDataList.get(0), attr.getBilinear());
				gridDataList = new ArrayList<GridData>();
				gridDataList.add(tmpGridData);
				if(gridDataList.isEmpty()){
					logger.println("In IsolineProcess中getGridData()时网格数据加密后为空");
					return null;
				}
				hasBilinear = true;
			}
			if(!hasSpline && attr.getIsGridDataSpline()){
				float[][] kernel = new float[][]{{1,1,1},{1,1,1},{1,1,1}};
				int r = (int) (kernel.length / 2);
				int c = (int) (kernel[0].length / 2);
				int[] gridSplineLevelAry = attr.getGridDataSplineLevelAry();
				int edge = 1;
				for(int i=0; i<this.attr.getGridDataSplineTimes(); i++){
					if(i < gridSplineLevelAry.length){
						kernel[r][c] = gridSplineLevelAry[i];
					}else{
						kernel[r][c] = gridSplineLevelAry[gridSplineLevelAry.length-1];
					}
					if(kernel[r][c]<1){
						continue;
					}
					ArrayList<GridData> gridDataListCopy = (ArrayList<GridData>) gridDataList.clone();
					if(gridDataList!=null){
						gridDataList.clear();
						gridDataList = new ArrayList<GridData>();
					}
					for(GridData gd:gridDataListCopy){
						int row = gd.getRowNum();
						int col = gd.getColNum();
						float[][] tmpData = new float[row][col];
						tmpData = MIDS3DMath.convol(gd.getGridData(), kernel, edge);
						GridData tmpGridData = new GridData();
						tmpGridData.setColNum(col);
						tmpGridData.setRowNum(row);
						tmpGridData.setXStart(gd.getXStart());
						tmpGridData.setXEnd(gd.getXEnd());
						tmpGridData.setYStart(gd.getYStart());
						tmpGridData.setYEnd(gd.getYEnd());
						float deltX = gd.getXDel();
						float deltY = gd.getYDel();
						if(deltX != 0.0f){
							tmpGridData.setXDel(deltX);
						}
						if(deltY!=0.0f){
							tmpGridData.setYDel(deltY);
						}
						tmpGridData.setGridData(tmpData);
						tmpGridData.setLonAry2D(gd.getLonAry2D());
						tmpGridData.setLatAry2D(gd.getLatAry2D());
						gridDataList.add(tmpGridData);
					}
				}

				if(gridDataList == null || gridDataList.isEmpty()){
					logger.println("In IsolineProcess中getGridData()的格点数据加密失败");
					return null;
				}
				hasSpline = true;
			}
			return gridDataList;
		}
	}*/


    /**
     * 等值线属性发生变化时，重新处理
     */
    public void resetAttr(IsolineProcessAttr attr) {
        setAttr(attr);
    }


    ///////////////追踪///////////////////////////////////////////////////

    /**
     * 追踪等值线
     */
    private boolean trace() {
        if (deltY == 0) {
            if (gridYLeftEnd > gridYLeftStart && gridYRightEnd > gridYLeftStart) {
                deltYSign = 99999.9f;
            } else {
                deltYSign = -99999.9f;
            }
        } else {
            if (gridYLeftEnd > gridYLeftStart && gridYRightEnd > gridYRightStart) {
                deltYSign = deltY;
            } else {
                deltYSign = -1 * deltY;
            }
        }
        preIsopoint = new Isopoint();// 前一点
        curIsopoint = new Isopoint();// 当前点
        nextIsopoint = new Isopoint();// 后一点
        curVertex = new Vertex[gridRows][gridCols];//顶点

        procIsolineDataList = new ArrayList<IsolineDataProc>();//等值线链表

        xSide = new EdgeIsopointInfo[gridRows][gridCols - 1];// x边
        ySide = new EdgeIsopointInfo[gridRows - 1][gridCols];// y边

        float[] minmaxVal = StrArrayUtil.getMinMaxVal(gridDataList.get(0).getGridData(), NULLVAL);
        minVal = minmaxVal[0];
        maxVal = minmaxVal[1];
        double[] contourLevels;//level 和 fillLevel
        int levelSize = attr.getLevel().length;
        if (this.attr.isSetFillLevel()) {
            //合并fillLevel 和 level
            int fillLevelSize = attr.getFillLevel().length;
            int size = levelSize + fillLevelSize;
            contourLevels = new double[size];
            for (int i = 0; i < levelSize; i++) {
                contourLevels[i] = attr.getLevel()[i];
            }
            for (int i = 0; i < fillLevelSize; i++) {
                if ((float) attr.getFillLevel()[i] < minVal) {
                    contourLevels[i + levelSize] = minVal;
                } else if ((float) attr.getFillLevel()[i] > maxVal) {
                    contourLevels[i + levelSize] = maxVal;
                } else {
                    contourLevels[i + levelSize] = attr.getFillLevel()[i];
                }
            }
            Arrays.sort(contourLevels);//排序,从大到小
        } else {
            contourLevels = new double[levelSize];
            for (int i = 0; i < levelSize; i++) {
                contourLevels[i] = attr.getLevel()[i];//有序,从大到小
            }
        }

        int size = contourLevels.length;
        allLevels = new ArrayList<Float>(size);
        int k = 0;
        for (int i = 0; i < size; i++) {
            curContourLevel = contourLevels[i];// 追踪的当前等值线的值
            contourLevel = (float) curContourLevel;
            if (contourLevel < minVal || contourLevel > maxVal) {
                continue;
            }
            allLevels.add(contourLevel);
            if (k > 0 && Math.abs(allLevels.get(k) - allLevels.get(k - 1)) < precision) {
                allLevels.remove(k);
                continue;
            }
            k++;
            if (!interpolateTracingValue()) {
                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess中trace()时计算横纵边上等值点失败");
                return false;
            }

            // 追踪开等值线
            if (!traceNonClosedIsoline()) {
                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess中trace()时追踪开等值线失败");
                return false;
            }

            // 追踪封闭等值线
            if (!traceClosedIsoline()) {
                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess中trace()时追踪闭合等值线失败");
                return false;
            }
        }
        return true;
    }

    /**
     * 扫描并计算横、边纵上等值点的情况
     */
    private boolean interpolateTracingValue() {
        if (xSide == null) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess中interpolateTracingValue()时xSide为空");
            return false;
        }
        if (ySide == null) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess中interpolateTracingValue()时ySide为空");
            return false;
        }
        float h0, h1;//考虑为99999即存在空的情况
        double flag;
        float[][] gridPoints = gridDataArys;
        ////追踪优先级：-1：不存在等值点； 0：边上两端的值相等且等于当前追踪的等值线的值；1：边上一端等于当前追踪的等值线的值；2：边上存在等值点且不在端点上
        //追踪优先级：-1：不存在等值点、边上两端的值相等且等于当前追踪的等值线的值； 0：起始等值点；1：边上一端等于当前追踪的等值线的值；2：边上存在等值点且不在端点上
        double epsilon = 1e-11;//取到很小，避免存在一正一负很小的数计算出来相等

        // 横向
        for (int i = 0; i < gridRows; i++) {
            for (int j = 0; j < gridCols - 1; j++) {
                h0 = gridPoints[i][j];
                h1 = gridPoints[i][j + 1];
                xSide[i][j] = new EdgeIsopointInfo();// 必须先初始化
                // float类型相减有误差所以使用了epsilon
                if (Math.abs(h0 - NULLVAL) < epsilon || Math.abs(h1 - NULLVAL) < epsilon) {
                    xSide[i][j].setRate(-2.0);
                    xSide[i][j].setHasIsopoint(false);
                    xSide[i][j].setLevel(-1);//不能追踪
// 					hasNullVal = true;
                } else if (Math.abs(h0 - h1) < epsilon) {//相等
                    xSide[i][j].setRate(-2.0);
                    xSide[i][j].setHasIsopoint(false);
                    xSide[i][j].setLevel(-1);//不能追踪
                } else {
                    flag = (curContourLevel - h0) * (curContourLevel - h1);
                    if (flag > 0) {
                        xSide[i][j].setRate(-2.0);
                        xSide[i][j].setHasIsopoint(false);
                        xSide[i][j].setLevel(-1);//不能追踪
                    } else if (flag < 0) {
                        xSide[i][j].setRate((curContourLevel - h0) / (h1 - h0));
                        xSide[i][j].setHasIsopoint(true);
                        xSide[i][j].setLevel(2);//优先级最高
                        assert (xSide[i][j].getRate() > 0.0 && xSide[i][j].getRate() < 1.0);
                    } else if (flag == 0) {
                        //和当前等值线的值进行比较，一端相等，则看另一端的大小来决定当前端进行加减，从而在此边上存在此level的等值线
                        //有问题，暂不使用
                        boolean zeroFlag = false;
                        if (Math.abs(curContourLevel - h0) < epsilon && Math.abs(curContourLevel - h1) < epsilon) {
                            zeroFlag = true;
                        } else if (Math.abs(curContourLevel - h0) < epsilon) {
                            if (h1 > curContourLevel) {
                                h0 -= shift;
                            } else {
                                h0 += shift;
                            }
                        } else {
                            if (h0 > curContourLevel) {
                                h1 -= shift;
                            } else {
                                h1 += shift;
                            }
                        }
                        if (!zeroFlag) {
                            xSide[i][j].setRate((curContourLevel - h0) / (h1 - h0));
                            if (xSide[i][j].getRate() <= 0.0 || xSide[i][j].getRate() >= 1.0) {
                                xSide[i][j].setHasIsopoint(false);
                                xSide[i][j].setLevel(-1);
                            } else {
                                xSide[i][j].setHasIsopoint(true);
                                xSide[i][j].setLevel(1);
                            }
                        } else {
                            xSide[i][j].setHasIsopoint(false);
                            xSide[i][j].setRate(0);
                            xSide[i][j].setLevel(-1);
                        }
                    } else {
                        assert false;
                    }
                }
            }
        }

        // 纵向
        for (int i = 0; i < gridRows - 1; i++) {
            for (int j = 0; j < gridCols; j++) {
                h0 = gridPoints[i][j];
                h1 = gridPoints[i + 1][j];

                ySide[i][j] = new EdgeIsopointInfo();
                if (Math.abs(h0 - NULLVAL) < epsilon || Math.abs(h1 - NULLVAL) < epsilon) {
                    ySide[i][j].setRate(-2.0f);
                    ySide[i][j].setHasIsopoint(false);
                    ySide[i][j].setLevel(-1);
// 					hasNullVal = true;
                } else if (Math.abs(h0 - h1) < epsilon) {
                    ySide[i][j].setRate(-2.0);
                    ySide[i][j].setHasIsopoint(false);
                    ySide[i][j].setLevel(-1);
                } else {
                    flag = (curContourLevel - h0) * (curContourLevel - h1);
                    if (flag > 0) {
                        ySide[i][j].setRate(-2.0);
                        ySide[i][j].setHasIsopoint(false);
                        ySide[i][j].setLevel(-1);
                    } else if (flag < 0) {
                        ySide[i][j].setRate((curContourLevel - h0) / (h1 - h0));
                        ySide[i][j].setHasIsopoint(true);
                        ySide[i][j].setLevel(2);
                        assert ySide[i][j].getRate() > 0.0 && ySide[i][j].getRate() < 1.0;
                    } else if (flag == 0) {
                        //和当前等值线的值进行比较，一端相等，则看另一端的大小来决定当前端进行加减，从而在此边上存在此level的等值线
                        //有问题，暂不使用
                        boolean zeroFlag = false;
                        if (Math.abs(curContourLevel - h0) < epsilon && Math.abs(curContourLevel - h1) < epsilon) {
                            zeroFlag = true;
                        } else if (Math.abs(curContourLevel - h0) < epsilon) {
                            if (h1 > curContourLevel) {
                                h0 -= shift;
                            } else {
                                h0 += shift;
                            }
                        } else {
                            if (h0 > curContourLevel) {
                                h1 -= shift;
                            } else {
                                h1 += shift;
                            }
                        }
                        if (!zeroFlag) {
                            ySide[i][j].setRate((curContourLevel - h0) / (h1 - h0));
                            if (ySide[i][j].getRate() <= 0.0 || ySide[i][j].getRate() >= 1.0) {
                                ySide[i][j].setHasIsopoint(false);
                                ySide[i][j].setLevel(-1);
                            } else {
                                ySide[i][j].setHasIsopoint(true);
                                ySide[i][j].setLevel(1);
                            }
                        } else {
                            ySide[i][j].setHasIsopoint(false);
                            ySide[i][j].setRate(0);
                            ySide[i][j].setLevel(-1);
                        }
                    } else {
                        assert false;
                    }
                }
            }
        }

        for (int i = 0; i < gridRows; i++) {
            for (int j = 0; j < gridCols; j++) {
                curVertex[i][j] = new Vertex();
                curVertex[i][j].setRow(i);
                curVertex[i][j].setCol(j);
                curVertex[i][j].setVal(gridDataList.get(0).getGridData()[i][j]);
                boolean canTrace = false;
                if (i == 0 && j == 0) {
                    canTrace = xSide[i][j].isHasIsopoint() || ySide[i][j].isHasIsopoint();
                } else if (i == 0 && j > 0 && j < gridCols - 1) {
                    canTrace = xSide[i][j].isHasIsopoint() || ySide[i][j].isHasIsopoint() || xSide[i][j - 1].isHasIsopoint();
                } else if (i == 0 && j == gridCols - 1) {
                    canTrace = xSide[i][j - 1].isHasIsopoint() || ySide[i][j].isHasIsopoint();
                } else if (i == gridRows - 1 && j == 0) {
                    canTrace = xSide[i][j].isHasIsopoint() || ySide[i - 1][j].isHasIsopoint();
                } else if (i == gridRows - 1 && j > 0 && j < gridCols - 1) {
                    canTrace = xSide[i][j].isHasIsopoint() || ySide[i - 1][j].isHasIsopoint() || xSide[i][j - 1].isHasIsopoint();
                } else if (i == gridRows - 1 && j == gridCols - 1) {
                    canTrace = xSide[i][j - 1].isHasIsopoint() || ySide[i - 1][j].isHasIsopoint();
                } else if (i > 0 && i < gridRows - 1 && j == 0) {
                    canTrace = xSide[i][j].isHasIsopoint() || ySide[i][j].isHasIsopoint() || ySide[i - 1][j].isHasIsopoint();
                } else if (i > 0 && i < gridRows - 1 && j == gridCols - 1) {
                    canTrace = xSide[i][j - 1].isHasIsopoint() || ySide[i][j].isHasIsopoint() || ySide[i - 1][j].isHasIsopoint();
                } else {
                    canTrace = xSide[i][j].isHasIsopoint() || ySide[i][j].isHasIsopoint() || ySide[i - 1][j].isHasIsopoint() || xSide[i][j - 1].isHasIsopoint();
                }
                curVertex[i][j].setAsStartP(canTrace);
            }
        }
        return true;
    }

    /**
     * 追踪开等值线
     */
    private boolean traceNonClosedIsoline() {
        // 追踪底边框
        for (int j = 0; j < gridCols - 1; j++) {
            if (xSide[0][j].isHasIsopoint()) {
                if (!curVertex[0][j].isAsStartP() || !curVertex[0][j + 1].isAsStartP()) {
                    continue;
                }
                preIsopoint.setAll(-1, j, true);
                curIsopoint.setAll(0, j, true);
                if (!traceOneNonClosedIsoline()) {
                    if (logger.isDebugEnabled())
                        logger.debug("In IsolineProcess中traceNonClosedIsoline()从底边追踪一条开等值线col=" + j + "失败");
                    continue;//如果追踪出错，不退出，继续追踪下一条等值线
                }
            }
        }

        // 追踪左边框
        for (int i = 0; i < gridRows - 1; i++) {
            if (ySide[i][0].isHasIsopoint()) {
                if (!curVertex[i][0].isAsStartP() || !curVertex[i + 1][0].isAsStartP()) {
                    continue;
                }
                preIsopoint.setAll(i, -1, false);
                curIsopoint.setAll(i, 0, false);
                if (!traceOneNonClosedIsoline()) {
                    if (logger.isDebugEnabled())
                        logger.debug("In IsolineProcess中traceNonClosedIsoline()从左边追踪一条开等值线row=" + i + "失败");
                    continue;
                }
            }
        }

        // 追踪上边框
        for (int j = 0; j < gridCols - 1; j++) {
            if (xSide[gridRows - 1][j].isHasIsopoint()) {
                if (!curVertex[gridRows - 1][j].isAsStartP() || !curVertex[gridRows - 1][j + 1].isAsStartP()) {
                    continue;
                }
                preIsopoint.setAll(gridRows, j, true);
                curIsopoint.setAll(gridRows - 1, j, true);
                if (!traceOneNonClosedIsoline()) {
                    if (logger.isDebugEnabled())
                        logger.debug("In IsolineProcess中traceNonClosedIsoline()从上边追踪一条开等值线col=" + j + "失败");
                    continue;
                }
            }
        }

        // 追踪右边框
        for (int i = 0; i < gridRows - 1; i++) {
            if (ySide[i][gridCols - 1].isHasIsopoint()) {
                if (!curVertex[i][gridCols - 1].isAsStartP() || !curVertex[i + 1][gridCols - 1].isAsStartP()) {
                    continue;
                }
                preIsopoint.setAll(i, gridCols, false);
                curIsopoint.setAll(i, gridCols - 1, false);
                if (!traceOneNonClosedIsoline()) {
                    if (logger.isDebugEnabled())
                        logger.debug("In IsolineProcess中traceNonClosedIsoline()从右边追踪一条开等值线row=" + i + "失败");
                    continue;
                }
            }
        }
        return true;
    }

    private void addIsoline() {
        boolean isLevel = false;
        for (int i = 0; i < attr.getLevel().length; i++) {
            if (Math.abs(curContourLevel - attr.getLevel()[i]) < precision) {
                procIsolineData.index = i;
                isLevel = true;
                break;
            }
        }
        if (!isLevel) {
            procIsolineData.index = -1;//是填充的等级
        }
        procIsolineData.indexProc = isolineIndex++;
        procIsolineDataList.add(procIsolineData);
    }

    private void setCurVertex(Isopoint point1, Isopoint point2) {
        int cRow = point1.getRow();
        int cCol = point1.getCol();
        Boolean cIsHorizon = point1.getIsHorizon();
        int pRow = point2.getRow();
        int pCol = point2.getCol();
        Boolean pIsHorizon = point2.getIsHorizon();

        int i = 0;
        int ii = 0;
        int j = 0;
        int jj = 0;//当前点所在边的两个顶点
        boolean flag = true;
        boolean flag2 = false;

        int i1 = 0, i2 = 0, i3 = 0, i4 = 0; //当前点和前一点所在的两条边的4个顶点
        int j1 = 0, j2 = 0, j3 = 0, j4 = 0;

        if (cIsHorizon != null && cIsHorizon.equals(pIsHorizon)) {
            flag = false;
        }
        if (flag) {
            if (cIsHorizon != null) {
                if (cIsHorizon) {
                    if (deltYSign < 0) {
                        if (cRow == pRow && cCol == pCol) {//上左
                            i = cRow;
                            j = cCol;
                            ii = cRow;
                            jj = cCol + 1;
                        } else if (cRow == pRow && (cCol + 1) == pCol) {//上右
                            i = pRow;
                            j = pCol;
                            ii = cRow;
                            jj = cCol;
                        } else if (cCol == pCol && (cRow - 1) == pRow) {//下左
                            i = cRow;
                            j = cCol;
                            ii = cRow;
                            jj = cCol + 1;
                        } else if ((cCol + 1) == pCol && (cRow - 1) == pRow) {//下右
                            i = cRow;
                            j = pCol;
                            ii = cRow;
                            jj = cCol;
                        }
                    } else {
                        if ((cRow - 1) == pRow && cCol == pCol) {//上左
                            i = cRow;
                            j = cCol;
                            ii = cRow;
                            jj = cCol + 1;
                        } else if ((cRow - 1) == pRow && (cCol + 1) == pCol) {//上右
                            i = cRow;
                            j = pCol;
                            ii = cRow;
                            jj = cCol;
                        } else if (cCol == pCol && cRow == pRow) {//下左
                            i = cRow;
                            j = cCol;
                            ii = cRow;
                            jj = cCol + 1;
                        } else if ((cCol + 1) == pCol && cRow == pRow) {//下右
                            i = pRow;
                            j = pCol;
                            ii = cRow;
                            jj = cCol;
                        }
                    }
                } else {
                    if (deltYSign < 0) {
                        if (cRow == pRow && cCol == pCol) {//左上
                            i = cRow;
                            j = cCol;
                            ii = cRow + 1;
                            jj = cCol;
                        } else if (cRow == pRow && (cCol - 1) == pCol) {//右上
                            i = cRow;
                            j = cCol;
                            ii = cRow + 1;
                            jj = cCol;
                        } else if (cCol == pCol && (cRow + 1) == pRow) {//左下
                            i = pRow;
                            j = pCol;
                            ii = cRow;
                            jj = cCol;
                        } else if ((cCol - 1) == pCol && (cRow + 1) == pRow) {//右下
                            i = pRow;
                            j = cCol;
                            ii = cRow;
                            jj = cCol;
                        }
                    } else {
                        if ((cRow + 1) == pRow && cCol == pCol) {//左上
                            i = pRow;
                            j = pCol;
                            ii = cRow;
                            jj = cCol;
                        } else if ((cRow + 1) == pRow && (cCol - 1) == pCol) {//右上
                            i = pRow;
                            j = cCol;
                            ii = cRow;
                            jj = cCol;
                        } else if (cCol == pCol && cRow == pRow) {//左下
                            i = cRow;
                            j = cCol;
                            ii = cRow + 1;
                            jj = cCol;
                        } else if ((cCol - 1) == pCol && cRow == pRow) {//右下
                            i = cRow;
                            j = cCol;
                            ii = cRow + 1;
                            jj = cCol;
                        }
                    }
                }
            } else {
                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess() setCurVertex()出错");
                return;
            }
        }

        if (!flag) {
            if (cIsHorizon != null) {
                if (cIsHorizon) {
                    if (deltYSign < 0) {
                        flag2 = true;
                        i1 = pRow;
                        j1 = pCol;
                        i2 = pRow;
                        j2 = pCol + 1;
                        i3 = cRow;
                        j3 = cCol;
                        i4 = cRow;
                        j4 = cCol + 1;
                    } else {
                        flag2 = true;
                        i1 = pRow;
                        j1 = pCol;
                        i2 = pRow;
                        j2 = pCol + 1;
                        i3 = cRow;
                        j3 = cCol;
                        i4 = cRow;
                        j4 = cCol + 1;
                    }
                } else {
                    if (deltYSign < 0) {
                        flag2 = true;
                        i1 = pRow;
                        j1 = pCol;
                        i2 = pRow + 1;
                        j2 = pCol;
                        i3 = cRow;
                        j3 = cCol;
                        i4 = cRow + 1;
                        j4 = cCol;
                    } else {
                        flag2 = true;
                        i1 = pRow;
                        j1 = pCol;
                        i2 = pRow + 1;
                        j2 = pCol;
                        i3 = cRow;
                        j3 = cCol;
                        i4 = cRow + 1;
                        j4 = cCol;
                    }
                }
            } else {
                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess() setCurVertex()出错");
                return;
            }
        }

        if (flag) {
            if (curVertex[i][j].getVal() == contourLevel) {
                curVertex[i][j].setAsStartP(false);
            }
            if (curVertex[ii][jj].getVal() == contourLevel) {
                curVertex[ii][jj].setAsStartP(false);
            }
        } else if (flag2) {
            if (curVertex[i1][j1].getVal() == contourLevel) {
                curVertex[i1][j1].setAsStartP(false);
            }
            if (curVertex[i2][j2].getVal() == contourLevel) {
                curVertex[i2][j2].setAsStartP(false);
            }
            if (curVertex[i3][j3].getVal() == contourLevel) {
                curVertex[i3][j3].setAsStartP(false);
            }
            if (curVertex[i4][j4].getVal() == contourLevel) {
                curVertex[i4][j4].setAsStartP(false);
            }
        }
    }

    /**
     * 追踪一条开等值线
     */
    private boolean traceOneNonClosedIsoline() {
        if (curIsopoint.getRow() != 0 && curIsopoint.getCol() != 0 && curIsopoint.getRow() != gridRows - 1 && curIsopoint.getCol() != gridCols - 1) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess中traceOneNonClosedIsoline()当前点不在边框上不满足追踪一条开等值线的条件");
            return false;
        }

        procIsolineData = new IsolineDataProc();
        procIsolineData.lineList2D = new ArrayList<Point2D.Double>();
        procIsolineData.val = contourLevel;
        procIsolineData.isClosed = false;
        procIsolineData.colsList = new ArrayList<Integer>();
        procIsolineData.rowsList = new ArrayList<Integer>();
        procIsolineData.isHorizonList = new ArrayList<Boolean>();
        procIsolineData.isopointList = new ArrayList<Isopoint>();

        procIsolineData.startLineType = getLineType(curIsopoint);
        procIsolineData.maxX = Float.MIN_VALUE;
        procIsolineData.minX = Float.MAX_VALUE;
        procIsolineData.maxY = Float.MIN_VALUE;
        procIsolineData.minY = Float.MAX_VALUE;

        if (!calcCoord(curIsopoint.getRow(), curIsopoint.getCol(), curIsopoint.getIsHorizon())) {
            if (logger.isDebugEnabled()) logger.debug("In IsolineProcess中traceOneNonClosedIsoline()计算当前追踪点的坐标失败");
            return false;
        }

        Boolean isNullPoint = isHasNullPoint(preIsopoint, curIsopoint);
        if (isNullPoint != null && isNullPoint) {
            addIsoline();
            resetXYSide();
//			hasNullVal = true;
            return true;
        }
        Boolean isHorizon = curIsopoint.getIsHorizon();
        if (isHorizon != null) {
            if (curIsopoint.getIsHorizon()) {
                xSide[curIsopoint.getRow()][curIsopoint.getCol()].setHasIsopoint(false);
                xSide[curIsopoint.getRow()][curIsopoint.getCol()].setRate(-2.0);
                xSide[curIsopoint.getRow()][curIsopoint.getCol()].setLevel(-1);
            } else {
                ySide[curIsopoint.getRow()][curIsopoint.getCol()].setHasIsopoint(false);
                ySide[curIsopoint.getRow()][curIsopoint.getCol()].setRate(-2.0);
                ySide[curIsopoint.getRow()][curIsopoint.getCol()].setLevel(-1);
            }
        } else {
            if (logger.isDebugEnabled()) logger.debug("In IsolineProcess() traceOneNonClosedIsoline() 出错");
            return false;
        }
//		setCurVertex(curIsopoint, preIsopoint);
        // 追踪下一个点
        if (!traceNextPoint()) {
            if (logger.isDebugEnabled()) logger.debug("In IsolineProcess中traceOneNonClosedIsoline()追踪下一条等值线失败");
            resetXYSide();
            procIsolineData = null;
            return false;
        }
        preIsopoint.setAll(curIsopoint.getRow(), curIsopoint.getCol(), curIsopoint.getIsHorizon());
        curIsopoint.setAll(nextIsopoint.getRow(), nextIsopoint.getCol(), nextIsopoint.getIsHorizon());
        isNullPoint = isHasNullPoint(preIsopoint, curIsopoint);
        if (isNullPoint != null && isNullPoint) {
            addIsoline();
            //可有可无
            isHorizon = curIsopoint.getIsHorizon();
            if (isHorizon != null) {
                if (curIsopoint.getIsHorizon()) {
                    xSide[curIsopoint.getRow()][curIsopoint.getCol()].setHasIsopoint(false);
                    xSide[curIsopoint.getRow()][curIsopoint.getCol()].setRate(-2.0);
                    xSide[curIsopoint.getRow()][curIsopoint.getCol()].setLevel(-1);
                } else {
                    ySide[curIsopoint.getRow()][curIsopoint.getCol()].setHasIsopoint(false);
                    ySide[curIsopoint.getRow()][curIsopoint.getCol()].setRate(-2.0);
                    ySide[curIsopoint.getRow()][curIsopoint.getCol()].setLevel(-1);
                }
            } else {
                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess() traceOneNonClosedIsoline() 出错");
                return false;
            }
            resetXYSide();
            return true;
        }
        isHorizon = curIsopoint.getIsHorizon();
        boolean isFinish = (curIsopoint.getRow() == 0 && isHorizon != null && isHorizon)
                || (curIsopoint.getCol() == 0 && isHorizon != null && !isHorizon)
                || (curIsopoint.getRow() == gridRows - 1)
                || (curIsopoint.getCol() == gridCols - 1);
        while (!isFinish) {
            if (!traceNextPoint()) {
                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess中traceOneNonClosedIsoline()追踪下一个等值线失败");
                resetXYSide();
                procIsolineData = null;
                return false;
            }
            preIsopoint.setAll(curIsopoint.getRow(), curIsopoint.getCol(), curIsopoint.getIsHorizon());
            curIsopoint.setAll(nextIsopoint.getRow(), nextIsopoint.getCol(), nextIsopoint.getIsHorizon());
            isHorizon = curIsopoint.getIsHorizon();
            isFinish = (curIsopoint.getRow() == 0 && isHorizon != null && isHorizon)
                    || (curIsopoint.getCol() == 0 && isHorizon != null && !isHorizon)
                    || (curIsopoint.getRow() == gridRows - 1)
                    || (curIsopoint.getCol() == gridCols - 1);
            if (!isFinish) {
                isNullPoint = isHasNullPoint(preIsopoint, curIsopoint);
                if (isNullPoint != null && isNullPoint) {
                    isFinish = true;
                }
            }
        }

        addIsoline();
        //可有可无
        isHorizon = curIsopoint.getIsHorizon();
        if (isHorizon != null) {
            if (isHorizon) {
                xSide[curIsopoint.getRow()][curIsopoint.getCol()].setHasIsopoint(false);
                xSide[curIsopoint.getRow()][curIsopoint.getCol()].setRate(-2.0);
                xSide[curIsopoint.getRow()][curIsopoint.getCol()].setLevel(-1);
            } else {
                ySide[curIsopoint.getRow()][curIsopoint.getCol()].setHasIsopoint(false);
                ySide[curIsopoint.getRow()][curIsopoint.getCol()].setRate(-2.0);
                ySide[curIsopoint.getRow()][curIsopoint.getCol()].setLevel(-1);
            }
        } else {
            if (logger.isDebugEnabled()) logger.debug("In IsolineProcess() traceOneNonClosedIsoline() 出错");
            return false;
        }
        resetXYSide();
        procIsolineData.endLineType = getLineType(curIsopoint);
        return true;
    }

    private void resetXYSide() {
        if (procIsolineData.isHorizonList.get(0)) {
            xSide[procIsolineData.rowsList.get(0)][procIsolineData.colsList.get(0)].setHasIsopoint(false);
            xSide[procIsolineData.rowsList.get(0)][procIsolineData.colsList.get(0)].setRate(-2.0);
            xSide[procIsolineData.rowsList.get(0)][procIsolineData.colsList.get(0)].setLevel(-1);
        } else {
            ySide[procIsolineData.rowsList.get(0)][procIsolineData.colsList.get(0)].setHasIsopoint(false);
            ySide[procIsolineData.rowsList.get(0)][procIsolineData.colsList.get(0)].setRate(-2.0);
            ySide[procIsolineData.rowsList.get(0)][procIsolineData.colsList.get(0)].setLevel(-1);
        }
    }

    /**
     * 求当前点的横纵坐标
     *
     * @param row       当前点所在行
     * @param col       当前点所在列
     * @param isHorizon 是横边还是纵边
     * @return false计算不成功，true计算成功
     */
    private boolean calcCoord(int row, int col, boolean isHorizon) {
        Point2D.Double resIsopoint = new Point2D.Double();
        if (isHorizon) {
            resIsopoint.x = gridXArys[row][col] + (gridXArys[row][col + 1] - gridXArys[row][col]) * xSide[row][col].getRate();
            resIsopoint.y = gridYArys[row][col] + (gridYArys[row][col + 1] - gridYArys[row][col]) * xSide[row][col].getRate();
        } else {
            resIsopoint.x = gridXArys[row][col] + (gridXArys[row + 1][col] - gridXArys[row][col]) * ySide[row][col].getRate();
            resIsopoint.y = gridYArys[row][col] + (gridYArys[row + 1][col] - gridYArys[row][col]) * ySide[row][col].getRate();
        }
        procIsolineData.num++;
        procIsolineData.colsList.add(col);
        procIsolineData.rowsList.add(row);
        procIsolineData.isHorizonList.add(isHorizon);
        procIsolineData.lineList2D.add(resIsopoint);
        Isopoint isopt = new Isopoint();
        isopt.setAll(row, col, isHorizon);
        procIsolineData.isopointList.add(isopt);
        float maxX = procIsolineData.maxX;
        float minX = procIsolineData.minX;
        float maxY = procIsolineData.maxY;
        float minY = procIsolineData.minY;
        if (maxX < resIsopoint.x) {
            procIsolineData.maxX = (float) resIsopoint.x;
        }
        if (minX > resIsopoint.x) {
            procIsolineData.minX = (float) resIsopoint.x;
        }
        if (maxY < resIsopoint.y) {
            procIsolineData.maxY = (float) resIsopoint.y;
        }
        if (minY > resIsopoint.y) {
            procIsolineData.minY = (float) resIsopoint.y;
        }
        return true;
    }

    /**
     * 追踪下一个点
     */
    private boolean traceNextPoint() {
        try {
            Boolean isHorizon = curIsopoint.getIsHorizon();
            if (curIsopoint.getRow() > preIsopoint.getRow()) {
                if (!traceFromBottom2Top()) {
                    if (logger.isDebugEnabled())
                        logger.debug("In IsolineProcess中traceNextPoint()时traceFromBottom2Top()失败");
                    return false;
                }
            } else if (curIsopoint.getCol() > preIsopoint.getCol()) {
                if (!traceFromLeft2Right()) {
                    if (logger.isDebugEnabled())
                        logger.debug("In IsolineProcess中traceNextPoint()时traceFromLeft2Right()失败");
                    return false;
                }
            } else if (isHorizon != null && isHorizon) {
                if (curIsopoint.getRow() > preIsopoint.getRow() || curIsopoint.getCol() > preIsopoint.getCol()) {
                    if (logger.isDebugEnabled())
                        logger.debug("In IsolineProcess中traceNextPoint中当前点的横坐标或者纵坐标大于前一点的横坐标或纵坐标不满足自上向下追踪等值线的条件");
                    return false;
                }
                if (!traceFromTop2Bottom()) {
                    if (logger.isDebugEnabled())
                        logger.debug("In IsolineProcess中traceNextPoint()时traceFromTop2Bottom()失败");
                    return false;
                }
            } else {
                if (isHorizon != null && isHorizon) {
                    if (logger.isDebugEnabled())
                        logger.debug("In IsolineProcess中traceNextPoint中当前点在横坐标上不满足自右向左追踪等值线的条件");
                    return false;
                }
                if (curIsopoint.getRow() > preIsopoint.getRow() || curIsopoint.getCol() > preIsopoint.getCol()) {
                    if (logger.isDebugEnabled())
                        logger.debug("In IsolineProcess中traceNextPoint中当前点的横坐标或纵坐标大于前一点的横坐标或纵坐标不满足自右向左追踪等值线的条件");
                    return false;
                }
                if (!traceFromRight2Left()) {
                    if (logger.isDebugEnabled())
                        logger.debug("In IsolineProcess中traceNextPoint()时traceFromRight2Left()失败");
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private int maxvalPos(int val1, int val2, int val3) {
        if (val1 == val2 && val1 == val3) {
            return 0;//三个数相等
        }
        if (val1 > val2 && val1 > val3) {
            return 1;//第一个最大
        }
        if (val2 > val1 && val2 > val3) {
            return 2;
        }
        if (val3 > val1 && val3 > val2) {
            return 3;
        }

        if (val1 == val2) {
            if (val1 > val3) {
                return 12;
            } else {
                return 3;
            }
        }
        if (val2 == val3) {
            if (val2 > val1) {
                return 23;
            } else {
                return 1;
            }
        }

        if (val1 == val3) {
            if (val1 > val2) {
                return 13;
            } else {
                return 2;
            }
        }
        return -1;
    }


    /**
     * 自下向上追踪
     */
    private boolean traceFromBottom2Top() {
        if (curIsopoint.getRow() <= preIsopoint.getRow()) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess traceFromBottom2Top()自下向上追踪等值线时当前点的行小于前一点的行数");
            return false;
        }
        Boolean isHorizon = curIsopoint.getIsHorizon();
        if (isHorizon == null || (isHorizon != null && !isHorizon)) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess traceFromBottom2Top() 自下向上追踪等值线时当前点没在水平线上");
            return false;
        }

        int row = curIsopoint.getRow();
        int col = curIsopoint.getCol();

        //直接由优先级来判断
        int left = ySide[row][col].getLevel();
        int right = ySide[row][col + 1].getLevel();
        int top = xSide[row + 1][col].getLevel();

        if (left < 0 && right < 0 && top < 0) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess traceFromBottom2Top() 自下向上追踪等值线时3个方向均无等值点");
            return false;
        }

        //当两个边上或三个边上等级相同时处理：根据下一步是否有等值点来判断(等级高低)
        int r1 = row;
        int r2 = row + 1;
        int r3 = row + 2;
        int c1 = col;
        int c2 = col - 1;
        int c3 = col + 1;
        int c4 = col + 2;
        boolean isLeft = false;
        boolean isRight = false;
        boolean isTop = false;
        //当三个方向或两个方向的level相同时，分别获取下个网格内的最大level来确定此步向哪个方向追踪
        int leftMaxLevel = -1;
        int topMaxLevel = -1;
        int rightMaxLevel = -1;

        //分六种情况处理：3个方向 考虑left、top、right;top、right;left、top;left、right;right;left
        //中间
        if (row >= 0 && row <= gridRows - 3 && col >= 1 && col <= gridCols - 3) {
            //left
            int a = xSide[r1][c2].getLevel();
            int b = ySide[r1][c2].getLevel();
            int c = xSide[r2][c2].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && left >= 0) {
                leftMaxLevel = Math.max(Math.max(a, b), c);//下一步最大等级的优先追踪
                isLeft = true;
            }
            if (!isLeft) {//考虑遇到null值
                if (left >= 0 && (gridDataList.get(0).getGridData()[r1][c2] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r2][c2] == NULLVAL)) {
                    isLeft = true;
                }
            }
            //top
            a = ySide[r2][c1].getLevel();
            b = ySide[r2][c3].getLevel();
            c = xSide[r3][c1].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && top >= 0) {
                topMaxLevel = Math.max(Math.max(a, b), c);
                isTop = true;
            }
            if (!isTop) {
                if (top >= 0 && (gridDataList.get(0).getGridData()[r3][c1] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r3][c3] == NULLVAL)) {
                    isTop = true;
                }
            }

            //right
            a = xSide[r1][c3].getLevel();
            b = xSide[r2][c3].getLevel();
            c = ySide[r1][c4].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && right >= 0) {
                rightMaxLevel = Math.max(Math.max(a, b), c);
                isRight = true;
            }
            if (!isRight) {
                if (right >= 0 && (gridDataList.get(0).getGridData()[r1][c4] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r2][c4] == NULLVAL)) {
                    isRight = true;
                }
            }
        }

        //左边界
        if (row >= 0 && row <= gridRows - 3 && col == 0) {
            //left
            if (left >= 0) {
                leftMaxLevel = left;//
                isLeft = true;
            }
            //top
            int a = ySide[r2][c1].getLevel();
            int b = ySide[r2][c3].getLevel();
            int c = xSide[r3][c1].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && top >= 0) {
                topMaxLevel = Math.max(Math.max(a, b), c);
                isTop = true;
            }
            if (!isTop) {
                if (top >= 0 && (gridDataList.get(0).getGridData()[r3][c1] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r3][c3] == NULLVAL)) {
                    isTop = true;
                }
            }
            //right
            a = -1;
            b = -1;
            c = -1;
            if (c3 <= gridCols - 2) {
                a = xSide[r1][c3].getLevel();
                b = xSide[r2][c3].getLevel();
            }
            if (c4 <= gridCols - 1) {
                c = ySide[r1][c4].getLevel();
            }
            if ((a >= 0 || b >= 0 || c >= 0) && right >= 0) {
                rightMaxLevel = Math.max(Math.max(a, b), c);
                isRight = true;
            }

            if (!isRight) {
                if (right >= 0) {
                    if (c4 <= gridCols - 1) {
                        if (gridDataList.get(0).getGridData()[r2][c4] == NULLVAL ||
                                gridDataList.get(0).getGridData()[r1][c4] == NULLVAL) {
                            isRight = true;
                        }
                    } else {
                        isRight = true;
                    }
                }
            }
        }

        //右边界
        if (row >= 0 && row <= gridRows - 3 && col == gridCols - 2) {
            //left
            int a = -1;
            int b = -1;
            int c = -1;
            if (c2 >= 0) {
                a = xSide[r1][c2].getLevel();
                b = ySide[r1][c2].getLevel();
                c = xSide[r2][c2].getLevel();
            }
            if ((a >= 0 || b >= 0 || c >= 0) && left >= 0) {
                leftMaxLevel = Math.max(Math.max(a, b), c);
                isLeft = true;
            }
            if (!isLeft) {
                if (left >= 0) {
                    if (c2 >= 0) {
                        if (gridDataList.get(0).getGridData()[r1][c2] == NULLVAL ||
                                gridDataList.get(0).getGridData()[r2][c2] == NULLVAL) {
                            isLeft = true;
                        }
                    } else {
                        isLeft = true;
                    }
                }
            }
            //top
            a = ySide[r2][c1].getLevel();
            b = ySide[r2][c3].getLevel();
            c = xSide[r3][c1].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && top >= 0) {
                topMaxLevel = Math.max(Math.max(a, b), c);
                isTop = true;
            }
            if (!isTop) {
                if (top >= 0 && (gridDataList.get(0).getGridData()[r3][c1] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r3][c3] == NULLVAL)) {
                    isTop = true;
                }
            }

            //right
            if (right >= 0) {
                rightMaxLevel = right;//
                isRight = true;
            }

        }
        //上边界
        if (row == gridRows - 2 && col >= 1 && col <= gridCols - 3) {
            //left
            int a = xSide[r1][c2].getLevel();
            int b = ySide[r1][c2].getLevel();
            int c = xSide[r2][c2].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && left >= 0) {
                leftMaxLevel = Math.max(Math.max(a, b), c);
                isLeft = true;
            }
            if (!isLeft) {
                if (left >= 0 && (gridDataList.get(0).getGridData()[r2][c2] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r1][c2] == NULLVAL)) {
                    isLeft = true;
                }
            }
            //top
            if (top >= 0) {
                topMaxLevel = top;
                isTop = true;
            }
            //right
            a = xSide[r1][c3].getLevel();
            b = xSide[r2][c3].getLevel();
            c = ySide[r1][c4].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && right >= 0) {
                rightMaxLevel = Math.max(Math.max(a, b), c);
                isRight = true;
            }
            if (!isRight) {
                if (right >= 0 && (gridDataList.get(0).getGridData()[r2][c4] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r1][c4] == NULLVAL)) {
                    isRight = true;
                }
            }
        }
        //左上角
        if (row == gridRows - 2 && col == 0) {
            if (left >= 0) {
                leftMaxLevel = left;
                isLeft = true;
            }
            if (top >= 0) {
                topMaxLevel = top;
                isTop = true;
            }
            int a = -1;
            int b = -1;
            int c = -1;
            if (c3 <= gridCols - 2) {
                a = xSide[r1][c3].getLevel();
                b = xSide[r2][c3].getLevel();

            }
            if (c4 <= gridCols - 1) {
                c = ySide[r1][c4].getLevel();
            }
            if ((a >= 0 || b >= 0 || c >= 0) && right >= 0) {
                rightMaxLevel = Math.max(Math.max(a, b), c);
                isRight = true;
            }
            if (!isRight) {
                if (right >= 0) {
                    if (c4 <= gridCols - 1) {
                        if (gridDataList.get(0).getGridData()[r2][c4] == NULLVAL ||
                                gridDataList.get(0).getGridData()[r1][c4] == NULLVAL) {
                            isRight = true;
                        }
                    } else {
                        isRight = true;
                    }
                }
            }
        }

        //右上角
        if (row == gridRows - 2 && col == gridCols - 2) {
            int a = -1;
            int b = -1;
            int c = -1;
            if (c2 >= 0) {
                a = xSide[r1][c2].getLevel();
                b = ySide[r1][c2].getLevel();
                c = xSide[r2][c2].getLevel();
            }
            if ((a >= 0 || b >= 0 || c >= 0) && left >= 0) {
                leftMaxLevel = Math.max(Math.max(a, b), c);
                isLeft = true;
            }
            if (!isLeft) {
                if (left >= 0) {
                    if (c2 >= 0) {
                        if (gridDataList.get(0).getGridData()[r2][c2] == NULLVAL ||
                                gridDataList.get(0).getGridData()[r1][c2] == NULLVAL) {
                            isLeft = true;
                        }
                    } else {
                        isLeft = true;
                    }
                }
            }
            if (top >= 0) {
                topMaxLevel = top;
                isTop = true;
            }
            if (right >= 0) {
                rightMaxLevel = right;
                isRight = true;
            }
        }

        if (left == 2) {
            if (right == 2) {
                if (ySide[row][col].getRate() < ySide[row][col + 1].getRate()) {
                    if (!handingAfterNextPointFound(row, col, false)) {
                        return false;
                    }
                } else if (Math.abs(ySide[row][col].getRate() - ySide[row][col + 1].getRate()) < precision) {
                    float[][] gridPoints = gridDataArys;
                    float xSideRC = (float) ((curContourLevel - gridPoints[row][col])
                            / (gridPoints[row][col + 1] - gridPoints[row][col]));
                    if (xSideRC <= 0.5f) {
                        if (!handingAfterNextPointFound(row, col, false)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                            return false;
                        }
                    }
                } else {
                    if (!handingAfterNextPointFound(row, col + 1, false)) {
                        return false;
                    }
                }
            } else {
                if (!handingAfterNextPointFound(row, col, false)) {
                    return false;
                }
            }
        } else if (left == 1) {
            if (right == 2) {
                if (!handingAfterNextPointFound(row, col + 1, false)) {
                    return false;
                }
            } else if (right == 1) {
                if (top == 2) {
                    if (!handingAfterNextPointFound(row + 1, col, true)) {
                        return false;
                    }
                } else if (top == 1) {//2013-01-16增加
                    int maxval = maxvalPos(leftMaxLevel, rightMaxLevel, topMaxLevel);
                    boolean flag = true;
                    if (isLeft && maxval == 1) {
                        if (!handingAfterNextPointFound(row, col, false)) {
                            return false;
                        }
                    } else if (isRight && maxval == 2) {
                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                            return false;
                        }
                    } else if (isTop && maxval == 3) {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    } else {
                        flag = false;
                    }
                    if (!flag) {
                        flag = true;
                        if (maxval == 12) {
                            if (isLeft && isRight) {
                                if (ySide[row][col].getRate() < ySide[row][col + 1].getRate()) {
                                    if (!handingAfterNextPointFound(row, col + 1, false)) {
                                        return false;
                                    }
                                } else if (Math.abs(ySide[row][col].getRate() - ySide[row][col + 1].getRate()) < precision) {
                                    float[][] gridPoints = gridDataArys;
                                    float xSideRC = (float) ((curContourLevel - gridPoints[row][col])
                                            / (gridPoints[row][col + 1] - gridPoints[row][col]));
                                    if (xSideRC <= 0.5f) {
                                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                                            return false;
                                        }
                                    } else {
                                        if (!handingAfterNextPointFound(row, col, false)) {
                                            return false;
                                        }
                                    }
                                } else {
                                    if (!handingAfterNextPointFound(row, col, false)) {
                                        return false;
                                    }
                                }
                            } else if (isLeft) {
                                if (!handingAfterNextPointFound(row, col, false)) {
                                    return false;
                                }
                            } else if (isRight) {
                                if (!handingAfterNextPointFound(row, col + 1, false)) {
                                    return false;
                                }
                            } else {
                                flag = false;
                            }
                        } else if (maxval == 23) {
                            if (isRight) {
                                if (!handingAfterNextPointFound(row, col + 1, false)) {
                                    return false;
                                }
                            } else if (isTop) {
                                if (!handingAfterNextPointFound(row + 1, col, true)) {
                                    return false;
                                }
                            } else {
                                flag = false;
                            }
                        } else if (maxval == 13) {
                            if (isLeft) {
                                if (!handingAfterNextPointFound(row, col, false)) {
                                    return false;
                                }
                            } else if (isTop) {
                                if (!handingAfterNextPointFound(row + 1, col, true)) {
                                    return false;
                                }
                            } else {
                                flag = false;
                            }
                        } else if (maxval == 0) {
                            if (ySide[row][col].getRate() < ySide[row][col + 1].getRate()) {
                                if (!handingAfterNextPointFound(row, col + 1, false)) {
                                    return false;
                                }
                            } else if (Math.abs(ySide[row][col].getRate() - ySide[row][col + 1].getRate()) < precision) {
                                float[][] gridPoints = gridDataArys;
                                float xSideRC = (float) ((curContourLevel - gridPoints[row][col])
                                        / (gridPoints[row][col + 1] - gridPoints[row][col]));
                                if (xSideRC <= 0.5f) {
                                    if (!handingAfterNextPointFound(row, col + 1, false)) {
                                        return false;
                                    }
                                } else {
                                    if (!handingAfterNextPointFound(row, col, false)) {
                                        return false;
                                    }
                                }
                            } else {
                                if (!handingAfterNextPointFound(row, col, false)) {
                                    return false;
                                }
                            }
                        } else {
                            flag = false;
                        }
                    }
                    if (!flag) {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromBottom2Top()出错");
                        return false;
                    }
                } else {//top=0,-1
                    if (isLeft && isRight) {
                        if (leftMaxLevel > rightMaxLevel) {
                            if (!handingAfterNextPointFound(row, col, false)) {
                                return false;
                            }
                        } else if (leftMaxLevel < rightMaxLevel) {
                            if (!handingAfterNextPointFound(row, col + 1, false)) {
                                return false;
                            }
                        } else {
                            if (ySide[row][col].getRate() < ySide[row][col + 1].getRate()) {
                                if (!handingAfterNextPointFound(row, col + 1, false)) {
                                    return false;
                                }
                            } else if (Math.abs(ySide[row][col].getRate() - ySide[row][col + 1].getRate()) < precision) {
                                float[][] gridPoints = gridDataArys;
                                float xSideRC = (float) ((curContourLevel - gridPoints[row][col])
                                        / (gridPoints[row][col + 1] - gridPoints[row][col]));
                                if (xSideRC <= 0.5f) {
                                    if (!handingAfterNextPointFound(row, col + 1, false)) {
                                        return false;
                                    }
                                } else {
                                    if (!handingAfterNextPointFound(row, col, false)) {
                                        return false;
                                    }
                                }
                            } else {
                                if (!handingAfterNextPointFound(row, col, false)) {
                                    return false;
                                }
                            }
                        }
                    } else if (isLeft) {
                        if (!handingAfterNextPointFound(row, col, false)) {
                            return false;
                        }
                    } else if (isRight) {
                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                            return false;
                        }
                    } else if (top == 0) {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromBottom2Top()出错");
                        return false;
                    }
                }
            } else {//right=-1 0
                if (top == 2) {
                    if (!handingAfterNextPointFound(row + 1, col, true)) {
                        return false;
                    }
                } else if (top == 1) {
                    if (isLeft && isTop) {
                        if (leftMaxLevel == topMaxLevel) {
                            if (leftMaxLevel != 0) {
                                if (!handingAfterNextPointFound(row, col, false)) {
                                    return false;
                                }
                            } else {
                                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromBottom2Top()出错");
                                return false;
                            }
                        } else if (leftMaxLevel > topMaxLevel) {
                            if (!handingAfterNextPointFound(row, col, false)) {
                                return false;
                            }
                        } else {
                            if (!handingAfterNextPointFound(row + 1, col, true)) {
                                return false;
                            }
                        }
                    } else if (isLeft) {
                        if (!handingAfterNextPointFound(row, col, false)) {
                            return false;
                        }
                    } else if (isTop) {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    } else if (right == 0) {
                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromBottom2Top()出错");
                        return false;
                    }
                } else if (top == 0) {
                    if (right == 0) {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromBottom2Top()出错");
                        return false;
                    }
                    if (isLeft) {
                        if (!handingAfterNextPointFound(row, col, false)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    }
                } else {//-1
                    if (isLeft) {
                        if (!handingAfterNextPointFound(row, col, false)) {
                            return false;
                        }
                    } else if (right == 0) {
                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromBottom2Top()出错");
                        return false;
                    }
                }
            }
        } else if (left == 0) {
            if (right == 2) {
                if (isRight) {
                    if (!handingAfterNextPointFound(row, col + 1, false)) {
                        return false;
                    }
                } else {
                    if (!handingAfterNextPointFound(row, col, false)) {
                        return false;
                    }
                }
            } else if (right == 1) {
                if (top == 2) {
                    if (isTop) {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    } else {//是否先考虑right
                        if (!handingAfterNextPointFound(row, col, false)) {
                            return false;
                        }
                    }
                } else if (top == 1) {
                    if (isRight && isTop) {
                        if (rightMaxLevel == topMaxLevel) {
                            if (rightMaxLevel == 0) {
                                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromBottom2Top()出错");
                                return false;
                            } else {
                                if (!handingAfterNextPointFound(row, col + 1, false)) {
                                    return false;
                                }
                            }
                        } else if (rightMaxLevel > topMaxLevel) {
                            if (!handingAfterNextPointFound(row, col + 1, false)) {
                                return false;
                            }
                        } else {
                            if (!handingAfterNextPointFound(row + 1, col, true)) {
                                return false;
                            }
                        }
                    } else if (isRight) {
                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                            return false;
                        }
                    } else if (isTop) {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row, col, false)) {
                            return false;
                        }
                    }
                } else if (top == 0) {
                    if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromBottom2Top()出错");
                    return false;
                } else {
                    if (isRight) {
                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row, col, false)) {
                            return false;
                        }
                    }
                }
            } else if (right == 0) {
                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromBottom2Top()出错");
                return false;
            } else {//-1
                if (top > 0) {
                    if (isTop) {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row, col, false)) {
                            return false;
                        }
                    }
                } else if (top == 0) {
                    if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromBottom2Top()出错");
                    return false;
                } else {
                    if (!handingAfterNextPointFound(row, col, false)) {
                        return false;
                    }
                }
            }
        } else {//-1
            if (right == 2) {
                if (isRight) {
                    if (!handingAfterNextPointFound(row, col + 1, false)) {
                        return false;
                    }
                } else {
                    if (top >= 0) {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromBottom2Top()出错");
                        return false;
                    }
                }
            } else if (right == 1) {
                if (top == 2) {
                    if (isTop) {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    } else if (isRight) {
                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromBottom2Top()出错");
                        return false;
                    }
                } else if (top == 1) {
                    if (isRight && isTop) {
                        if (rightMaxLevel == topMaxLevel) {
                            if (rightMaxLevel == 0) {
                                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromBottom2Top()出错");
                                return false;
                            } else {
                                if (!handingAfterNextPointFound(row, col + 1, false)) {
                                    return false;
                                }
                            }
                        } else if (rightMaxLevel > topMaxLevel) {
                            if (!handingAfterNextPointFound(row, col + 1, false)) {
                                return false;
                            }
                        } else {
                            if (!handingAfterNextPointFound(row + 1, col, true)) {
                                return false;
                            }
                        }
                    } else if (isRight) {
                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                            return false;
                        }
                    } else if (isTop) {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromBottom2Top()出错");
                        return false;
                    }
                } else if (top == 0) {
                    if (isRight) {
                        if (rightMaxLevel == 0) {
                            if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromBottom2Top()出错");
                            return false;
                        } else {
                            if (!handingAfterNextPointFound(row, col + 1, false)) {
                                return false;
                            }
                        }
                    } else {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    }
                } else {
                    if (isRight) {
                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromBottom2Top()出错");
                        return false;
                    }
                }
            } else if (right == 0) {
                if (top > 0) {
                    if (isTop) {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                            return false;
                        }
                    }
                } else if (top == 0) {
                    if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromBottom2Top()出错");
                    return false;
                } else {
                    if (!handingAfterNextPointFound(row, col + 1, false)) {
                        return false;
                    }
                }
            } else {
                if (top >= 0) {
                    if (!handingAfterNextPointFound(row + 1, col, true)) {
                        return false;
                    }
                } else {
                    if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromBottom2Top()出错");
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 自左向右追踪
     */
    private boolean traceFromLeft2Right() {
        if (curIsopoint.getCol() <= preIsopoint.getCol()) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess traceFromBottom2Top()自左向右追踪等值线时当前点的列小于前一点的列数");
            return false;
        }
        Boolean isHorizon = curIsopoint.getIsHorizon();
        if (isHorizon == null || (isHorizon != null && isHorizon)) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess traceFromBottom2Top() 自下向上追踪等值线时当前点没在垂直水平线上");
            return false;
        }

        int row = curIsopoint.getRow();
        int col = curIsopoint.getCol();

        int bottom = xSide[row][col].getLevel();
        int top = xSide[row + 1][col].getLevel();
        int right = ySide[row][col + 1].getLevel();
        if (bottom < 0 && top < 0 && right < 0) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess traceFromBottom2Top() 自下向上追踪等值线时3个方向均无等值点 ");
            return false;
        }

        //当两个边上或三个边上等级相同时处理：根据下一步是否有等值点来判断
        int r1 = row;
        int r2 = row - 1;
        int r3 = row + 1;
        int r4 = row + 2;
        int c1 = col;
        int c2 = col + 1;
        int c3 = col + 2;
        boolean isBottom = false;
        boolean isTop = false;
        boolean isRight = false;
        //当三个方向或两个方向的level相同时，分别获取下个网格内的最大level来确定此步向哪个方向追踪
        int bottomMaxLevel = -1;
        int topMaxLevel = -1;
        int rightMaxLevel = -1;
        //分六种情况处理：3个方向
        //中间
        if (row >= 1 && row <= gridRows - 3 && col >= 0 && col <= gridCols - 3) {
            //bottom
            int a = ySide[r2][c1].getLevel();
            int b = xSide[r2][c1].getLevel();
            int c = ySide[r2][c2].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && bottom >= 0) {
                bottomMaxLevel = Math.max(Math.max(a, b), c);
                isBottom = true;
            }
            if (!isBottom) {
                if (bottom >= 0 && (gridDataList.get(0).getGridData()[r2][c1] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r2][c2] == NULLVAL)) {
                    isBottom = true;
                }
            }

            //right
            a = xSide[r1][c2].getLevel();
            b = xSide[r3][c2].getLevel();
            c = ySide[r1][c3].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && right >= 0) {
                rightMaxLevel = Math.max(Math.max(a, b), c);
                isRight = true;
            }
            if (!isRight) {
                if (right >= 0 && (gridDataList.get(0).getGridData()[r3][c3] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r1][c3] == NULLVAL)) {
                    isRight = true;
                }
            }

            //top
            a = ySide[r3][c1].getLevel();
            b = xSide[r4][c1].getLevel();
            c = ySide[r3][c2].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && top >= 0) {
                topMaxLevel = Math.max(Math.max(a, b), c);
                isTop = true;
            }
            if (!isTop) {
                if (top >= 0 && (gridDataList.get(0).getGridData()[r4][c1] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r4][c2] == NULLVAL)) {
                    isTop = true;
                }
            }
        }
        //下边界
        if (row == 0 && col >= 0 && col <= gridCols - 3) {
            //bottom
            if (bottom >= 0) {
                bottomMaxLevel = bottom;//不能假定边界优先级高，否则等值线追踪存在缺失
                isBottom = true;
            }
            //right
            int a = xSide[r1][c2].getLevel();
            int b = xSide[r3][c2].getLevel();
            int c = ySide[r1][c3].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && right >= 0) {
                rightMaxLevel = Math.max(Math.max(a, b), c);
                isRight = true;
            }
            if (!isRight) {
                if (right >= 0 && (gridDataList.get(0).getGridData()[r1][c3] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r3][c3] == NULLVAL)) {
                    isRight = true;
                }
            }
            //top
            a = -1;
            b = -1;
            c = -1;
            if (r3 <= gridRows - 2) {
                a = ySide[r3][c1].getLevel();
                c = ySide[r3][c2].getLevel();
            }
            if (r4 <= gridRows - 1) {
                b = xSide[r4][c1].getLevel();
            }
            if ((a >= 0 || b >= 0 || c >= 0) && top >= 0) {
                topMaxLevel = Math.max(Math.max(a, b), c);
                isTop = true;
            }
            if (!isTop) {
                if (top >= 0) {
                    if (r4 <= gridRows - 1) {
                        if (gridDataList.get(0).getGridData()[r4][c1] == NULLVAL ||
                                gridDataList.get(0).getGridData()[r4][c2] == NULLVAL) {
                            isTop = true;
                        }
                    } else {
                        isTop = true;
                    }
                }
            }

        }

        //上边界
        if (row == gridRows - 2 && col >= 0 && col <= gridCols - 3) {
            //bottom
            int a = -1;
            int b = -1;
            int c = -1;
            if (r2 >= 0) {
                a = ySide[r2][c1].getLevel();
                b = xSide[r2][c1].getLevel();
                c = ySide[r2][c2].getLevel();
            }
            if ((a >= 0 || b >= 0 || c >= 0) && bottom >= 0) {
                isBottom = true;
            }
            if (!isBottom) {
                if (bottom >= 0) {
                    if (r2 >= 0) {
                        if (gridDataList.get(0).getGridData()[r2][c1] == NULLVAL ||
                                gridDataList.get(0).getGridData()[r2][c2] == NULLVAL) {
                            isBottom = true;
                        }
                    } else {
                        isBottom = true;
                    }
                }
            }
            //right
            a = xSide[r1][c2].getLevel();
            b = xSide[r3][c2].getLevel();
            c = ySide[r1][c3].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && right >= 0) {
                rightMaxLevel = Math.max(Math.max(a, b), c);
                isRight = true;
            }
            if (!isRight) {
                if (right >= 0 && (gridDataList.get(0).getGridData()[r3][c3] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r1][c3] == NULLVAL)) {
                    isRight = true;
                }
            }
            //top
            if (top >= 0) {
                topMaxLevel = top;
                isTop = true;
            }
        }

        //右下角
        if (row == 0 && col == gridCols - 2) {
            //bottom
            if (bottom >= 0) {
                bottomMaxLevel = bottom;
                isBottom = true;
            }
            //right
            if (right >= 0) {
                rightMaxLevel = right;
                isRight = true;
            }
            //top
            int a = -1;
            int b = -1;
            int c = -1;
            if (r3 <= gridRows - 2) {
                a = ySide[r3][c1].getLevel();
                b = ySide[r3][c2].getLevel();
            }
            if (r4 <= gridRows - 1) {
                c = xSide[r4][c1].getLevel();
            }
            if ((a >= 0 || b >= 0 || c >= 0) && top >= 0) {
                topMaxLevel = Math.max(Math.max(a, b), c);
                isTop = true;
            }
            if (!isTop) {
                if (top >= 0) {
                    if (r4 <= gridRows - 1) {
                        if (gridDataList.get(0).getGridData()[r4][c1] == NULLVAL ||
                                gridDataList.get(0).getGridData()[r4][c2] == NULLVAL) {
                            isTop = true;
                        }
                    } else {
                        isTop = true;
                    }
                }
            }
        }

        //右上角
        if (row == gridRows - 2 && col == gridCols - 2) {
            //bottom
            int a = -1;
            int b = -1;
            int c = -1;
            if (r2 >= 0) {
                a = ySide[r2][c1].getLevel();
                b = xSide[r2][c1].getLevel();
                c = ySide[r2][c2].getLevel();
            }
            if ((a >= 0 || b >= 0 || c >= 0) && bottom >= 0) {
                bottomMaxLevel = Math.max(Math.max(a, b), c);
                isBottom = true;
            }
            if (!isBottom) {
                if (bottom >= 0) {
                    if (r2 >= 0) {
                        if (gridDataList.get(0).getGridData()[r2][c1] == NULLVAL ||
                                gridDataList.get(0).getGridData()[r2][c2] == NULLVAL) {
                            isBottom = true;
                        }
                    } else {
                        isBottom = true;
                    }
                }
            }

            if (right >= 0) {
                rightMaxLevel = right;
                isRight = true;
            }
            if (top >= 0) {
                topMaxLevel = top;
                isTop = true;
            }
        }

        //右边界
        if (row >= 1 && row <= gridRows - 3 && col == gridCols - 2) {
            int a = ySide[r2][c1].getLevel();
            int b = xSide[r2][c1].getLevel();
            int c = ySide[r2][c2].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && bottom >= 0) {
                bottomMaxLevel = Math.max(Math.max(a, b), c);
                isBottom = true;
            }
            if (!isBottom) {
                if (bottom >= 0 && (gridDataList.get(0).getGridData()[r2][c1] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r2][c2] == NULLVAL)) {
                    isBottom = true;
                }
            }

            if (right >= 0) {
                rightMaxLevel = right;
                isRight = true;
            }
            a = ySide[r3][c1].getLevel();
            b = xSide[r4][c1].getLevel();
            c = ySide[r3][c2].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && top >= 0) {
                topMaxLevel = Math.max(Math.max(a, b), c);
                isTop = true;
            }
            if (!isTop) {
                if (top >= 0 && (gridDataList.get(0).getGridData()[r4][c1] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r4][c2] == NULLVAL)) {
                    isTop = true;
                }
            }
        }

        if (bottom == 2) {
            if (top == 2) {
                if (xSide[row][col].getRate() < xSide[row + 1][col].getRate()) {
                    if (!handingAfterNextPointFound(row, col, true)) {
                        return false;
                    }
                } else if (Math.abs(xSide[row][col].getRate() - xSide[row + 1][col].getRate()) < precision) {
                    float[][] gridPoints = gridDataArys;
                    float ySideRC = (float) ((curContourLevel - gridPoints[row][col])
                            / (gridPoints[row + 1][col] - gridPoints[row][col]));
                    if (ySideRC <= 0.5f) {
                        if (!handingAfterNextPointFound(row, col, true)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    }
                } else {
                    if (!handingAfterNextPointFound(row + 1, col, true)) {
                        return false;
                    }
                }
            } else {
                if (!handingAfterNextPointFound(row, col, true)) {
                    return false;
                }
            }
        } else if (bottom == 1) {
            if (top == 2) {
                if (!handingAfterNextPointFound(row + 1, col, true)) {
                    return false;
                }
            } else if (top == 1) {
                if (right == 2) {
                    if (!handingAfterNextPointFound(row, col + 1, false)) {
                        return false;
                    }
                } else if (right == 1) {
                    int maxval = maxvalPos(bottomMaxLevel, topMaxLevel, rightMaxLevel);
                    boolean flag = true;
                    if (isBottom && maxval == 1) {
                        if (!handingAfterNextPointFound(row, col, true)) {
                            return false;
                        }
                    } else if (isTop && maxval == 2) {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    } else if (isRight && maxval == 3) {
                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                            return false;
                        }
                    } else {
                        flag = false;
                    }
                    if (!flag) {
                        flag = true;
                        if (maxval == 12) {
                            if (isBottom && isTop) {
                                if (xSide[row][col].getRate() < xSide[row + 1][col].getRate()) {
                                    if (!handingAfterNextPointFound(row + 1, col, true)) {
                                        return false;
                                    }
                                } else if (Math.abs(xSide[row][col].getRate() - xSide[row + 1][col].getRate()) < precision) {
                                    float[][] gridPoints = gridDataArys;
                                    float ySideRC = (float) ((curContourLevel - gridPoints[row][col])
                                            / (gridPoints[row + 1][col] - gridPoints[row][col]));
                                    if (ySideRC <= 0.5f) {
                                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                                            return false;
                                        }
                                    } else {
                                        if (!handingAfterNextPointFound(row, col, true)) {
                                            return false;
                                        }
                                    }
                                } else {
                                    if (!handingAfterNextPointFound(row, col, true)) {
                                        return false;
                                    }
                                }
                            } else if (isBottom) {
                                if (!handingAfterNextPointFound(row, col, true)) {
                                    return false;
                                }
                            } else if (isTop) {
                                if (!handingAfterNextPointFound(row + 1, col, true)) {
                                    return false;
                                }
                            } else {
                                flag = false;
                            }
                        } else if (maxval == 23) {
                            if (isTop) {
                                if (!handingAfterNextPointFound(row + 1, col, true)) {
                                    return false;
                                }
                            } else if (isRight) {
                                if (!handingAfterNextPointFound(row, col + 1, false)) {
                                    return false;
                                }
                            } else {
                                flag = false;
                            }
                        } else if (maxval == 13) {
                            if (isBottom) {
                                if (!handingAfterNextPointFound(row, col, true)) {
                                    return false;
                                }
                            } else if (isRight) {
                                if (!handingAfterNextPointFound(row, col + 1, false)) {
                                    return false;
                                }
                            } else {
                                flag = false;
                            }
                        } else if (maxval == 0) {
                            if (xSide[row][col].getRate() < xSide[row + 1][col].getRate()) {
                                if (!handingAfterNextPointFound(row + 1, col, true)) {
                                    return false;
                                }
                            } else if (Math.abs(xSide[row][col].getRate() - xSide[row + 1][col].getRate()) < precision) {
                                float[][] gridPoints = gridDataArys;
                                float ySideRC = (float) ((curContourLevel - gridPoints[row][col])
                                        / (gridPoints[row + 1][col] - gridPoints[row][col]));
                                if (ySideRC <= 0.5f) {
                                    if (!handingAfterNextPointFound(row + 1, col, true)) {
                                        return false;
                                    }
                                } else {
                                    if (!handingAfterNextPointFound(row, col, true)) {
                                        return false;
                                    }
                                }
                            } else {
                                if (!handingAfterNextPointFound(row, col, true)) {
                                    return false;
                                }
                            }
                            flag = true;
                        } else {
                            flag = false;
                        }
                    }
                    if (!flag) {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromLeft2Right()出错");
                        return false;
                    }
                } else {//right==-1 0
                    if (isBottom && isTop) {
                        if (bottomMaxLevel > topMaxLevel) {
                            if (!handingAfterNextPointFound(row, col, true)) {
                                return false;
                            }
                        } else if (bottomMaxLevel < topMaxLevel) {
                            if (!handingAfterNextPointFound(row + 1, col, true)) {
                                return false;
                            }
                        } else if (bottomMaxLevel == topMaxLevel && bottomMaxLevel != 0) {
                            if (xSide[row][col].getRate() < xSide[row + 1][col].getRate()) {
                                if (!handingAfterNextPointFound(row + 1, col, true)) {
                                    return false;
                                }
                            } else if (Math.abs(xSide[row][col].getRate() - xSide[row + 1][col].getRate()) < precision) {
                                float[][] gridPoints = gridDataArys;
                                float ySideRC = (float) ((curContourLevel - gridPoints[row][col])
                                        / (gridPoints[row + 1][col] - gridPoints[row][col]));
                                if (ySideRC <= 0.5f) {
                                    if (!handingAfterNextPointFound(row + 1, col, true)) {
                                        return false;
                                    }
                                } else {
                                    if (!handingAfterNextPointFound(row, col, true)) {
                                        return false;
                                    }
                                }
                            } else {
                                if (!handingAfterNextPointFound(row, col, true)) {
                                    return false;
                                }
                            }
                        } else {
                            if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromLeft2Right()出错");
                            return false;
                        }
                    } else if (isBottom) {
                        if (!handingAfterNextPointFound(row, col, true)) {
                            return false;
                        }
                    } else if (isTop) {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    } else if (right == 0) {
                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromLeft2Right()出错");
                        return false;
                    }
                }
            } else if (top == 0) {
                if (right == 2) {
                    if (!handingAfterNextPointFound(row, col + 1, false)) {
                        return false;
                    }
                } else if (right == 1) {
                    if (isBottom && isRight) {
                        if (bottomMaxLevel == rightMaxLevel) {
                            if (bottomMaxLevel == 0) {
                                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromLeft2Right()出错");
                                return false;
                            } else {
                                if (!handingAfterNextPointFound(row, col, true)) {
                                    return false;
                                }
                            }
                        } else if (bottomMaxLevel > rightMaxLevel) {
                            if (!handingAfterNextPointFound(row, col, true)) {
                                return false;
                            }
                        } else {
                            if (!handingAfterNextPointFound(row, col + 1, false)) {
                                return false;
                            }
                        }
                    } else if (isBottom) {
                        if (!handingAfterNextPointFound(row, col, true)) {
                            return false;
                        }
                    } else if (isRight) {
                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    }
                } else if (right == 0) {
                    if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromLeft2Right()出错");
                    return false;
                } else {
                    if (isBottom) {
                        if (!handingAfterNextPointFound(row, col, true)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    }
                }
            } else {
                if (right == 2) {
                    if (!handingAfterNextPointFound(row, col + 1, false)) {
                        return false;
                    }
                } else if (right == 1) {
                    if (isBottom && isRight) {
                        if (bottomMaxLevel == rightMaxLevel) {
                            if (bottomMaxLevel == 0) {
                                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromLeft2Right()出错");
                                return false;
                            } else {
                                if (!handingAfterNextPointFound(row, col, true)) {
                                    return false;
                                }
                            }
                        } else if (bottomMaxLevel > rightMaxLevel) {
                            if (!handingAfterNextPointFound(row, col, true)) {
                                return false;
                            }
                        } else {
                            if (!handingAfterNextPointFound(row, col + 1, false)) {
                                return false;
                            }
                        }
                    } else if (isBottom) {
                        if (!handingAfterNextPointFound(row, col, true)) {
                            return false;
                        }
                    } else if (isRight) {
                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromLeft2Right()出错");
                        return false;
                    }
                } else if (right == 0) {
                    if (isBottom) {
                        if (!handingAfterNextPointFound(row, col, true)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                            return false;
                        }
                    }
                } else {
                    if (isBottom) {
                        if (!handingAfterNextPointFound(row, col, true)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromLeft2Right()出错");
                        return false;
                    }
                }
            }
        } else if (bottom == 0) {
            if (top == 2) {
                if (!handingAfterNextPointFound(row + 1, col, true)) {
                    return false;
                }
            } else if (top == 1) {
                if (right == 2) {
                    if (!handingAfterNextPointFound(row, col + 1, false)) {
                        return false;
                    }
                } else if (right == 1) {
                    if (isTop && isRight) {
                        if (topMaxLevel == rightMaxLevel) {
                            if (topMaxLevel != 0) {
                                if (!handingAfterNextPointFound(row + 1, col, true)) {
                                    return false;
                                }
                            } else {
                                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromLeft2Right()出错");
                                return false;
                            }
                        } else if (topMaxLevel > rightMaxLevel) {
                            if (!handingAfterNextPointFound(row + 1, col, true)) {
                                return false;
                            }
                        } else {
                            if (!handingAfterNextPointFound(row, col + 1, false)) {
                                return false;
                            }
                        }
                    } else if (isTop) {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    } else if (isRight) {
                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row, col, true)) {
                            return false;
                        }
                    }
                } else if (right == 0) {
                    if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromLeft2Right()出错");
                    return false;
                } else {
                    if (isTop) {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row, col, true)) {
                            return false;
                        }
                    }
                }
            } else if (top == 0) {
                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromLeft2Right()出错");
                return false;
            } else {
                if (right > 0) {
                    if (isRight) {
                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row, col, true)) {
                            return false;
                        }
                    }
                } else if (right == 0) {
                    if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromLeft2Right()出错");
                    return false;
                } else {
                    if (!handingAfterNextPointFound(row, col, true)) {
                        return false;
                    }
                }
            }
        } else {
            if (top == 2) {
                if (!handingAfterNextPointFound(row + 1, col, true)) {
                    return false;
                }
            } else if (top == 1) {
                if (right == 2) {
                    if (!handingAfterNextPointFound(row, col + 1, false)) {
                        return false;
                    }
                } else if (right == 1) {
                    if (isTop && isRight) {
                        if (topMaxLevel == rightMaxLevel) {
                            if (topMaxLevel == 0) {
                                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromLeft2Right()出错");
                                return false;
                            } else {
                                if (!handingAfterNextPointFound(row + 1, col, true)) {
                                    return false;
                                }
                            }
                        } else if (topMaxLevel > rightMaxLevel) {
                            if (!handingAfterNextPointFound(row + 1, col, true)) {
                                return false;
                            }
                        } else {
                            if (!handingAfterNextPointFound(row, col + 1, false)) {
                                return false;
                            }
                        }
                    } else if (isTop) {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    } else if (isRight) {
                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromLeft2Right()出错");
                        return false;
                    }
                } else if (right == 0) {
                    if (isTop) {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                            return false;
                        }
                    }
                } else {
                    if (isTop) {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromLeft2Right()出错");
                        return false;
                    }
                }
            } else if (top == 0) {
                if (right > 0) {
                    if (isRight) {
                        if (!handingAfterNextPointFound(row, col + 1, false)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row + 1, col, true)) {
                            return false;
                        }
                    }
                } else if (right == 0) {
                    if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromLeft2Right()出错");
                    return false;
                } else {
                    if (!handingAfterNextPointFound(row + 1, col, true)) {
                        return false;
                    }
                }
            } else {
                if (right >= 0) {
                    if (!handingAfterNextPointFound(row, col + 1, false)) {
                        return false;
                    }
                } else {
                    if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromLeft2Right()出错");
                    return false;
                }
            }
        }

        return true;
    }


    /**
     * 自上向下追踪
     */
    private boolean traceFromTop2Bottom() {
        int row = curIsopoint.getRow();
        if (row <= 0) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess traceFromTop2Bottom()自上向下追踪等值线时当前点的行数小于等于0");
            return false;
        }

        int col = curIsopoint.getCol();

        int left = ySide[row - 1][col].getLevel();
        int right = ySide[row - 1][col + 1].getLevel();
        int bottom = xSide[row - 1][col].getLevel();
        if (left < 0 && right < 0 && bottom < 0) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess traceFromTop2Bottom()自上向下追踪等值线时3个方向均无等值点");
            return false;
        }
        //当两个边上或三个边上等级相同时处理：根据下一步是否有等值点来判断
        int r1 = row;
        int r2 = row - 1;
        int r3 = row - 2;
        int c1 = col;
        int c2 = col - 1;
        int c3 = col + 1;
        int c4 = col + 2;
        boolean isLeft = false;
        boolean isBottom = false;
        boolean isRight = false;
        //当三个方向或两个方向的level相同时，分别获取下个网格内的最大level来确定此步向哪个方向追踪
        int leftMaxLevel = -1;
        int bottomMaxLevel = -1;
        int rightMaxLevel = -1;

        //分六种情况处理：3个方向
        //中间
        if (row >= 2 && row <= gridRows - 1 && col >= 1 && col <= gridCols - 3) {
            int a = xSide[r2][c2].getLevel();
            int b = ySide[r2][c2].getLevel();
            int c = xSide[r1][c2].getLevel();

            if ((a >= 0 || b >= 0 || c >= 0) && left >= 0) {
                leftMaxLevel = Math.max(Math.max(a, b), c);
                isLeft = true;
            }
            if (!isLeft) {
                if (left >= 0 && (gridDataList.get(0).getGridData()[r1][c2] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r2][c2] == NULLVAL)) {
                    isLeft = true;
                }
            }
            a = ySide[r3][c1].getLevel();
            b = xSide[r3][c1].getLevel();
            c = ySide[r3][c3].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && bottom >= 0) {
                bottomMaxLevel = Math.max(Math.max(a, b), c);
                isBottom = true;
            }
            if (!isBottom) {
                if (bottom >= 0 && (gridDataList.get(0).getGridData()[r3][c1] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r3][c3] == NULLVAL)) {
                    isBottom = true;
                }
            }
            a = xSide[r1][c3].getLevel();
            b = xSide[r2][c3].getLevel();
            c = ySide[r2][c4].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && right >= 0) {
                rightMaxLevel = Math.max(Math.max(a, b), c);
                isRight = true;
            }
            if (!isRight) {
                if (right >= 0 && (gridDataList.get(0).getGridData()[r1][c4] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r2][c4] == NULLVAL)) {
                    isRight = true;
                }
            }
        }

        //左边界
        if (col == 0 && row >= 2 && row <= gridRows - 1) {
            if (left >= 0) {
                leftMaxLevel = left;
                isLeft = true;
            }
            int a = ySide[r3][c1].getLevel();
            int b = xSide[r3][c1].getLevel();
            int c = ySide[r3][c3].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && bottom >= 0) {
                bottomMaxLevel = Math.max(Math.max(a, b), c);
                isBottom = true;
            }
            if (!isBottom) {
                if (bottom >= 0 && (gridDataList.get(0).getGridData()[r3][c1] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r3][c3] == NULLVAL)) {
                    isBottom = true;
                }
            }

            a = -1;
            b = -1;
            c = -1;
            if (c3 <= gridCols - 2) {
                a = xSide[r1][c3].getLevel();
                b = xSide[r2][c3].getLevel();
            }
            if (c4 <= gridCols - 1) {
                c = ySide[r2][c4].getLevel();
            }
            if ((a >= 0 || b >= 0 || c >= 0) && right >= 0) {
                rightMaxLevel = Math.max(Math.max(a, b), c);
                isRight = true;
            }
            if (!isRight) {
                if (right >= 0) {
                    if (c4 <= gridCols - 1) {
                        if (gridDataList.get(0).getGridData()[r1][c4] == NULLVAL ||
                                gridDataList.get(0).getGridData()[r2][c4] == NULLVAL) {
                            isRight = true;
                        }
                    } else {
                        isRight = true;
                    }
                }
            }
        }
        //右边界
        if (col == gridCols - 2 && row >= 2 && row <= gridRows - 1) {
            int a = -1;
            int b = -1;
            int c = -1;
            if (c2 >= 0) {
                a = xSide[r2][c2].getLevel();
                b = ySide[r2][c2].getLevel();
                c = xSide[r1][c2].getLevel();
            }
            if ((a >= 0 || b >= 0 || c >= 0) && left >= 0) {
                leftMaxLevel = Math.max(Math.max(a, b), c);
                isLeft = true;
            }
            if (!isLeft) {
                if (left >= 0) {
                    if (c2 >= 0) {
                        if (gridDataList.get(0).getGridData()[r1][c2] == NULLVAL ||
                                gridDataList.get(0).getGridData()[r2][c2] == NULLVAL) {
                            isLeft = true;
                        }
                    } else {
                        isLeft = true;
                    }
                }
            }
            a = ySide[r3][c1].getLevel();
            b = xSide[r3][c1].getLevel();
            c = ySide[r3][c3].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && bottom >= 0) {
                bottomMaxLevel = Math.max(Math.max(a, b), c);
                isBottom = true;
            }
            if (!isBottom) {
                if (bottom >= 0 && (gridDataList.get(0).getGridData()[r3][c1] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r3][c3] == NULLVAL)) {
                    isBottom = true;
                }
            }
            if (right >= 0) {
                rightMaxLevel = right;
                isRight = true;
            }
        }
        //左下角
        if (row == 1 && col == 0) {
            if (left >= 0) {
                leftMaxLevel = left;
                isLeft = true;
            }
            if (bottom >= 0) {
                bottomMaxLevel = bottom;
                isBottom = true;
            }
            int a = -1;
            int b = -1;
            int c = -1;
            if (c3 <= gridCols - 2) {
                a = xSide[r1][c3].getLevel();
                b = xSide[r2][c3].getLevel();
            }
            if (c4 <= gridCols - 1) {
                c = ySide[r2][c4].getLevel();
            }
            if ((a >= 0 || b >= 0 || c >= 0) && right >= 0) {
                rightMaxLevel = Math.max(Math.max(a, b), c);
                isRight = true;
            }
            if (!isRight) {
                if (right >= 0) {
                    if (c4 <= gridCols - 1) {
                        if (gridDataList.get(0).getGridData()[r1][c4] == NULLVAL ||
                                gridDataList.get(0).getGridData()[r2][c4] == NULLVAL) {
                            isRight = true;
                        }
                    } else {
                        isRight = true;
                    }
                }
            }
        }
        //右下角
        if (row == 1 && col == gridCols - 2) {
            int a = -1;
            int b = -1;
            int c = -1;
            if (c2 >= 0) {
                a = xSide[r2][c2].getLevel();
                b = ySide[r2][c2].getLevel();
                c = xSide[r1][c2].getLevel();
            }
            if ((a >= 0 || b >= 0 || c >= 0) && left >= 0) {
                leftMaxLevel = Math.max(Math.max(a, b), c);
                isLeft = true;
            }
            if (!isLeft) {
                if (left >= 0) {
                    if (c2 >= 0) {
                        if (gridDataList.get(0).getGridData()[r1][c2] == NULLVAL ||
                                gridDataList.get(0).getGridData()[r2][c2] == NULLVAL) {
                            isLeft = true;
                        }
                    } else {
                        isLeft = true;
                    }
                }
            }
            if (bottom >= 0) {
                bottomMaxLevel = bottom;
                isBottom = true;
            }
            if (right >= 0) {
                rightMaxLevel = right;
                isRight = true;
            }
        }
        //下边界
        if (row == 1 && col >= 1 && col <= gridCols - 3) {
            int a = xSide[r2][c2].getLevel();
            int b = ySide[r2][c2].getLevel();
            int c = xSide[r1][c2].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && left >= 0) {
                leftMaxLevel = Math.max(Math.max(a, b), c);
                isLeft = true;
            }
            if (!isLeft) {
                if (left >= 0 && (gridDataList.get(0).getGridData()[r1][c2] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r2][c2] == NULLVAL)) {
                    isLeft = true;
                }
            }
            if (bottom >= 0) {
                bottomMaxLevel = bottom;
                isBottom = true;
            }

            a = xSide[r1][c3].getLevel();
            b = xSide[r2][c3].getLevel();
            c = ySide[r2][c4].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && right >= 0) {
                rightMaxLevel = Math.max(Math.max(a, b), c);
                isRight = true;
            }
            if (!isRight) {
                if (right >= 0 && (gridDataList.get(0).getGridData()[r1][c4] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r2][c4] == NULLVAL)) {
                    isRight = true;
                }
            }
        }

        if (left == 2) {
            if (right == 2) {
                if (ySide[row - 1][col].getRate() > ySide[row - 1][col + 1].getRate()) {
                    if (!handingAfterNextPointFound(row - 1, col, false)) {
                        return false;
                    }
                } else if (Math.abs(ySide[row - 1][col].getRate() - ySide[row - 1][col + 1].getRate()) < precision) {
                    float[][] gridPoints = gridDataArys;
                    float xSideRC = (float) ((curContourLevel - gridPoints[row][col])
                            / (gridPoints[row][col + 1] - gridPoints[row][col]));
                    if (xSideRC <= 0.5f) {
                        if (!handingAfterNextPointFound(row - 1, col, false)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                            return false;
                        }
                    }
                } else {
                    if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                        return false;
                    }
                }
            } else {
                if (!handingAfterNextPointFound(row - 1, col, false)) {
                    return false;
                }
            }
        } else if (left == 1) {
            if (right == 2) {
                if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                    return false;
                }
            } else if (right == 1) {
                if (bottom == 2) {
                    if (!handingAfterNextPointFound(row - 1, col, true)) {
                        return false;
                    }
                } else if (bottom == 1) {
                    int maxval = maxvalPos(leftMaxLevel, rightMaxLevel, bottomMaxLevel);
                    boolean flag = true;
                    if (isLeft && maxval == 1) {
                        if (!handingAfterNextPointFound(row - 1, col, false)) {
                            return false;
                        }
                    } else if (isRight && maxval == 2) {
                        if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                            return false;
                        }
                    } else if (isBottom && maxval == 3) {
                        if (!handingAfterNextPointFound(row - 1, col, true)) {
                            return false;
                        }
                    } else {
                        flag = false;
                    }
                    if (!flag) {
                        flag = true;
                        if (maxval == 12) {
                            if (isLeft && isRight) {
                                if (ySide[row - 1][col].getRate() > ySide[row - 1][col + 1].getRate()) {
                                    if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                                        return false;
                                    }
                                } else if (Math.abs(ySide[row - 1][col].getRate() - ySide[row - 1][col + 1].getRate()) < precision) {
                                    float[][] gridPoints = gridDataArys;
                                    float xSideRC = (float) ((curContourLevel - gridPoints[row][col])
                                            / (gridPoints[row][col + 1] - gridPoints[row][col]));
                                    if (xSideRC <= 0.5f) {
                                        if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                                            return false;
                                        }
                                    } else {
                                        if (!handingAfterNextPointFound(row - 1, col, false)) {
                                            return false;
                                        }
                                    }
                                } else {
                                    if (!handingAfterNextPointFound(row - 1, col, false)) {
                                        return false;
                                    }
                                }
                            } else if (isLeft) {
                                if (!handingAfterNextPointFound(row - 1, col, false)) {
                                    return false;
                                }
                            } else if (isRight) {
                                if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                                    return false;
                                }
                            } else {
                                flag = false;
                            }
                        } else if (maxval == 23) {
                            if (isRight) {
                                if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                                    return false;
                                }
                            } else if (isBottom) {
                                if (!handingAfterNextPointFound(row - 1, col, true)) {
                                    return false;
                                }
                            } else {
                                flag = false;
                            }
                        } else if (maxval == 13) {
                            if (isLeft) {
                                if (!handingAfterNextPointFound(row - 1, col, false)) {
                                    return false;
                                }
                            } else if (isBottom) {
                                if (!handingAfterNextPointFound(row - 1, col, true)) {
                                    return false;
                                }
                            } else {
                                flag = false;
                            }
                        } else if (maxval == 0) {
                            if (ySide[row - 1][col].getRate() > ySide[row - 1][col + 1].getRate()) {
                                if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                                    return false;
                                }
                            } else if (Math.abs(ySide[row - 1][col].getRate() - ySide[row - 1][col + 1].getRate()) < precision) {
                                float[][] gridPoints = gridDataArys;
                                float xSideRC = (float) ((curContourLevel - gridPoints[row][col])
                                        / (gridPoints[row][col + 1] - gridPoints[row][col]));
                                if (xSideRC <= 0.5f) {
                                    if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                                        return false;
                                    }
                                } else {
                                    if (!handingAfterNextPointFound(row - 1, col, false)) {
                                        return false;
                                    }
                                }
                            } else {
                                if (!handingAfterNextPointFound(row - 1, col, false)) {
                                    return false;
                                }
                            }
                        } else {
                            flag = false;
                        }
                    }
                    if (!flag) {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromTop2Bottom()出错");
                        return false;
                    }
                } else {//bottom=-1 0
                    if (isLeft && isRight) {
                        if (leftMaxLevel > rightMaxLevel) {
                            if (!handingAfterNextPointFound(row - 1, col, false)) {
                                return false;
                            }
                        } else if (leftMaxLevel < rightMaxLevel) {
                            if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                                return false;
                            }
                        } else {
                            if (leftMaxLevel != 0) {
                                if (ySide[row - 1][col].getRate() > ySide[row - 1][col + 1].getRate()) {
                                    if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                                        return false;
                                    }
                                } else if (Math.abs(ySide[row - 1][col].getRate() - ySide[row - 1][col + 1].getRate()) < precision) {
                                    float[][] gridPoints = gridDataArys;
                                    float xSideRC = (float) ((curContourLevel - gridPoints[row][col])
                                            / (gridPoints[row][col + 1] - gridPoints[row][col]));
                                    if (xSideRC <= 0.5f) {
                                        if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                                            return false;
                                        }
                                    } else {
                                        if (!handingAfterNextPointFound(row - 1, col, false)) {
                                            return false;
                                        }
                                    }
                                } else {
                                    if (!handingAfterNextPointFound(row - 1, col, false)) {
                                        return false;
                                    }
                                }
                            } else {
                                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromTop2Bottom()出错");
                                return false;
                            }
                        }
                    } else if (isLeft) {
                        if (!handingAfterNextPointFound(row - 1, col, false)) {
                            return false;
                        }
                    } else if (isRight) {
                        if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                            return false;
                        }
                    } else if (bottom == 0) {
                        if (!handingAfterNextPointFound(row - 1, col, true)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromTop2Bottom()出错");
                        return false;
                    }
                }
            } else {//right=-1 0
                if (bottom == 2) {
                    if (!handingAfterNextPointFound(row - 1, col, true)) {
                        return false;
                    }
                } else if (bottom == 1) {
                    if (isLeft && isBottom) {
                        if (leftMaxLevel == bottomMaxLevel) {
                            if (leftMaxLevel == 0) {
                                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromTop2Bottom() 出错");
                                return false;
                            } else {
                                if (!handingAfterNextPointFound(row - 1, col, false)) {
                                    return false;
                                }
                            }
                        } else if (leftMaxLevel > bottomMaxLevel) {
                            if (!handingAfterNextPointFound(row - 1, col, false)) {
                                return false;
                            }
                        } else {
                            if (!handingAfterNextPointFound(row - 1, col, true)) {
                                return false;
                            }
                        }
                    } else if (isLeft) {
                        if (!handingAfterNextPointFound(row - 1, col, false)) {
                            return false;
                        }
                    } else if (isBottom) {
                        if (!handingAfterNextPointFound(row - 1, col, true)) {
                            return false;
                        }
                    } else if (right == 0) {
                        if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromTop2Bottom() 出错");
                        return false;
                    }
                } else if (bottom == 0) {
                    if (right == 0) {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromTop2Bottom() 出错");
                        return false;
                    }
                    if (isLeft) {
                        if (!handingAfterNextPointFound(row - 1, col, false)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row - 1, col, true)) {
                            return false;
                        }
                    }
                } else {
                    if (isLeft) {
                        if (!handingAfterNextPointFound(row - 1, col, false)) {
                            return false;
                        }
                    } else if (right == 0) {
                        if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromTop2Bottom() 出错");
                        return false;
                    }
                }
            }
        } else if (left == 0) {
            if (right == 2) {
                if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                    return false;
                }
            } else if (right == 1) {
                if (bottom == 2) {
                    if (!handingAfterNextPointFound(row - 1, col, true)) {
                        return false;
                    }
                } else if (bottom == 1) {
                    if (isRight && isBottom) {
                        if (rightMaxLevel == bottomMaxLevel) {
                            if (rightMaxLevel == 0) {
                                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromTop2Bottom() 出错");
                                return false;
                            } else {
                                if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                                    return false;
                                }
                            }
                        } else if (rightMaxLevel > bottomMaxLevel) {
                            if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                                return false;
                            }
                        } else {
                            if (!handingAfterNextPointFound(row - 1, col, true)) {
                                return false;
                            }
                        }
                    } else if (isRight) {
                        if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                            return false;
                        }
                    } else if (isBottom) {
                        if (!handingAfterNextPointFound(row - 1, col, true)) {
                            return false;
                        }
                    } else {//left
                        if (!handingAfterNextPointFound(row - 1, col, false)) {
                            return false;
                        }
                    }
                } else if (bottom == 0) {
                    if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromTop2Bottom() 出错");
                    return false;
                } else {
                    if (isRight) {
                        if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row - 1, col, false)) {
                            return false;
                        }
                    }
                }
            } else if (right == 0) {
                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromTop2Bottom() 出错");
                return false;
            } else {
                if (bottom > 0) {
                    if (isBottom) {
                        if (!handingAfterNextPointFound(row - 1, col, true)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row - 1, col, false)) {
                            return false;
                        }
                    }
                } else if (bottom == 0) {
                    if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromTop2Bottom() 出错");
                    return false;
                } else {
                    if (!handingAfterNextPointFound(row - 1, col, false)) {
                        return false;
                    }
                }
            }
        } else {//-1
            if (right == 2) {
                if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                    return false;
                }
            } else if (right == 1) {
                if (bottom == 2) {
                    if (!handingAfterNextPointFound(row - 1, col, true)) {
                        return false;
                    }
                } else if (bottom == 1) {
                    if (isRight && isBottom) {
                        if (rightMaxLevel == bottomMaxLevel) {
                            if (rightMaxLevel == 0) {
                                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromTop2Bottom() 出错");
                                return false;
                            } else {
                                if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                                    return false;
                                }
                            }
                        } else if (rightMaxLevel > bottomMaxLevel) {
                            if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                                return false;
                            }
                        } else {
                            if (!handingAfterNextPointFound(row - 1, col, true)) {
                                return false;
                            }
                        }
                    } else if (isRight) {
                        if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                            return false;
                        }
                    } else if (isBottom) {
                        if (!handingAfterNextPointFound(row - 1, col, true)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromTop2Bottom() 出错");
                        return false;
                    }
                } else if (bottom == 0) {
                    if (isRight) {
                        if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row - 1, col, true)) {
                            return false;
                        }
                    }
                } else {
                    if (isRight) {
                        if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromTop2Bottom() 出错");
                        return false;
                    }
                }
            } else if (right == 0) {
                if (bottom > 0) {
                    if (isBottom) {
                        if (!handingAfterNextPointFound(row - 1, col, true)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                            return false;
                        }
                    }
                } else if (bottom == 0) {
                    if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromTop2Bottom() 出错");
                    return false;
                } else {
                    if (!handingAfterNextPointFound(row - 1, col + 1, false)) {
                        return false;
                    }
                }
            } else {
                if (bottom >= 0) {
                    if (!handingAfterNextPointFound(row - 1, col, true)) {
                        return false;
                    }
                } else {
                    if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromTop2Bottom() 出错");
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * 自右向左追踪
     */
    private boolean traceFromRight2Left() {
        Boolean isHorizon = curIsopoint.getIsHorizon();
        if (isHorizon == null || (isHorizon != null && isHorizon)) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess中traceFromRight2Left()当前点在横边上不满足自右向左追踪等值线的条件");
            return false;
        }
        int row = curIsopoint.getRow();
        int col = curIsopoint.getCol();
        if (col <= 0) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess traceFromRight2Left()自右向左追踪等值线时当前点的列小于等于0");
            return false;
        }

        int bottom = xSide[row][col - 1].getLevel();
        int top = xSide[row + 1][col - 1].getLevel();
        int left = ySide[row][col - 1].getLevel();

        if (bottom < 0 && top < 0 && left < 0) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess traceFromRight2Left()自右向左追踪等值线时3个方向均无等值点");
            return false;
        }
        //当两个边上或三个边上等级相同时处理：根据下一步是否有等值点来判断
        int r1 = row;
        int r2 = row + 1;
        int r3 = row + 2;
        int r4 = row - 1;
        int c1 = col;
        int c2 = col - 1;
        int c3 = col - 2;
        boolean isBottom = false;
        boolean isTop = false;
        boolean isLeft = false;
        //当三个方向或两个方向的level相同时，分别获取下个网格内的最大level来确定此步向哪个方向追踪
        int bottomMaxLevel = -1;
        int topMaxLevel = -1;
        int leftMaxLevel = -1;
        //分六种情况处理：3个方向
        //中间
        if (row >= 1 && row <= gridRows - 3 && col >= 2 && col <= gridCols - 1) {
            int a = ySide[r4][c2].getLevel();
            int b = xSide[r4][c2].getLevel();
            int c = ySide[r4][c1].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && bottom >= 0) {
                bottomMaxLevel = Math.max(Math.max(a, b), c);
                isBottom = true;
            }
            if (!isBottom) {
                if (bottom >= 0 && (gridDataList.get(0).getGridData()[r4][c1] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r4][c2] == NULLVAL)) {
                    isBottom = true;
                }
            }
            a = ySide[r2][c2].getLevel();
            b = xSide[r3][c2].getLevel();
            c = ySide[r2][c1].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && top >= 0) {
                topMaxLevel = Math.max(Math.max(a, b), c);
                isTop = true;
            }
            if (!isTop) {
                if (top >= 0 && (gridDataList.get(0).getGridData()[r3][c1] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r3][c2] == NULLVAL)) {
                    isTop = true;
                }
            }

            a = ySide[r1][c3].getLevel();
            b = xSide[r2][c3].getLevel();
            c = xSide[r1][c3].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && left >= 0) {
                leftMaxLevel = Math.max(Math.max(a, b), c);
                isLeft = true;
            }
            if (!isLeft) {
                if (left >= 0 && (gridDataList.get(0).getGridData()[r1][c3] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r2][c3] == NULLVAL)) {
                    isLeft = true;
                }
            }

        }
        //下边界
        if (row == 0 && col >= 2 && col <= gridCols - 1) {
            if (bottom >= 0) {
                bottomMaxLevel = bottom;
                isBottom = true;
            }

            int a = -1;
            int b = -1;
            int c = -1;
            if (r2 <= gridRows - 2) {
                a = ySide[r2][c2].getLevel();
                b = ySide[r2][c1].getLevel();
            }
            if (r3 <= gridRows - 1) {
                c = xSide[r3][c2].getLevel();
            }
            if ((a >= 0 || b >= 0 || c >= 0) && top >= 0) {
                topMaxLevel = Math.max(Math.max(a, b), c);
                isTop = true;
            }
            if (!isTop) {
                if (top >= 0) {
                    if (r3 <= gridRows - 1) {
                        if (gridDataList.get(0).getGridData()[r3][c1] == NULLVAL ||
                                gridDataList.get(0).getGridData()[r3][c2] == NULLVAL) {
                            isTop = true;
                        }
                    } else {
                        isTop = true;
                    }
                }
            }

            a = ySide[r1][c3].getLevel();
            b = xSide[r1][c3].getLevel();
//			if(r2<=gridRows-1){
            c = xSide[r2][c3].getLevel();
//			}
            if ((a >= 0 || b >= 0 || c >= 0) && left >= 0) {
                leftMaxLevel = Math.max(Math.max(a, b), c);
                isLeft = true;
            }
            if (!isLeft) {
                if (left >= 0 && (gridDataList.get(0).getGridData()[r2][c3] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r1][c3] == NULLVAL)) {
                    isLeft = true;
                }
            }
        }
        //右下角
        if (row == 0 && col == 1) {
            if (bottom >= 0) {
                bottomMaxLevel = bottom;
                isBottom = true;
            }
            int a = -1;
            int b = -1;
            int c = -1;
            if (r2 <= gridRows - 2) {
                a = ySide[r2][c2].getLevel();
                b = ySide[r2][c1].getLevel();
            }
            if (r3 <= gridRows - 1) {
                c = xSide[r3][c2].getLevel();
            }

            if ((a >= 0 || c >= 0 || b >= 0) && top >= 0) {
                topMaxLevel = Math.max(Math.max(a, b), c);
                isTop = true;
            }
            if (!isTop) {
                if (top >= 0) {
                    if (r3 <= gridRows - 1) {
                        if (gridDataList.get(0).getGridData()[r3][c2] == NULLVAL ||
                                gridDataList.get(0).getGridData()[r3][c1] == NULLVAL) {
                            isTop = true;
                        }
                    } else {
                        isTop = true;
                    }
                }
            }

            if (left >= 0) {
                leftMaxLevel = left;
                isLeft = true;
            }

        }
        //左边界
        if (col == 1 && row >= 1 && row <= gridRows - 3) {
            int a = ySide[r4][c2].getLevel();
            int b = xSide[r4][c2].getLevel();
            int c = ySide[r4][c1].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && bottom >= 0) {
                bottomMaxLevel = Math.max(Math.max(a, b), c);
                isBottom = true;
            }
            if (!isBottom) {
                if (bottom >= 0 && (gridDataList.get(0).getGridData()[r4][c2] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r4][c1] == NULLVAL)) {
                    isBottom = true;
                }
            }
            a = ySide[r2][c2].getLevel();
            b = xSide[r3][c2].getLevel();
            c = ySide[r2][c1].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && top >= 0) {
                topMaxLevel = Math.max(Math.max(a, b), c);
                isTop = true;
            }
            if (!isTop) {
                if (top >= 0 && (gridDataList.get(0).getGridData()[r3][c1] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r3][c2] == NULLVAL)) {
                    isTop = true;
                }
            }

            if (left >= 0) {
                leftMaxLevel = left;
                isLeft = true;
            }
        }
        //上边界
        if (row == gridRows - 2 && col >= 2 && col <= gridCols - 2) {
            int a = -1;
            int b = -1;
            int c = -1;
            if (r4 >= 0) {
                a = ySide[r4][c2].getLevel();
                b = xSide[r4][c2].getLevel();
                c = ySide[r4][c1].getLevel();
            }
            if ((a >= 0 || b >= 0 || c >= 0) && bottom >= 0) {
                bottomMaxLevel = Math.max(Math.max(a, b), c);
                isBottom = true;
            }
            if (!isBottom) {
                if (bottom >= 0) {
                    if (r4 >= 0) {
                        if (gridDataList.get(0).getGridData()[r4][c1] == NULLVAL ||
                                gridDataList.get(0).getGridData()[r4][c2] == NULLVAL) {
                            isBottom = true;
                        }
                    } else {
                        isBottom = true;
                    }
                }
            }

            if (top >= 0) {
                topMaxLevel = top;
                isTop = true;
            }

            a = ySide[r1][c3].getLevel();
            b = xSide[r2][c3].getLevel();
            c = xSide[r1][c3].getLevel();
            if ((a >= 0 || b >= 0 || c >= 0) && left >= 0) {
                leftMaxLevel = Math.max(Math.max(a, b), c);
                isLeft = true;
            }
            if (!isLeft) {
                if (left >= 0 && (gridDataList.get(0).getGridData()[r2][c3] == NULLVAL ||
                        gridDataList.get(0).getGridData()[r1][c3] == NULLVAL)) {
                    isLeft = true;
                }
            }

        }
        //左上角
        if (row == gridRows - 2 && col == 1) {
            int a = -1;
            int b = -1;
            int c = -1;
            if (r4 >= 0) {
                a = ySide[r4][c2].getLevel();
                b = xSide[r4][c2].getLevel();
                c = ySide[r4][c1].getLevel();
            }
            if ((a >= 0 || b >= 0 || c >= 0) && bottom >= 0) {
                bottomMaxLevel = Math.max(Math.max(a, b), c);
                isBottom = true;
            }
            if (!isBottom) {
                if (bottom >= 0) {
                    if (r4 >= 0) {
                        if (gridDataList.get(0).getGridData()[r4][c1] == NULLVAL ||
                                gridDataList.get(0).getGridData()[r4][c2] == NULLVAL) {
                            isBottom = true;
                        }
                    } else {
                        isBottom = true;
                    }
                }
            }
            if (top >= 0) {
                topMaxLevel = top;
                isTop = true;
            }
            if (left >= 0) {
                leftMaxLevel = left;
                isLeft = true;
            }
        }

        if (bottom == 2) {
            if (top == 2) {
                if (xSide[row][col - 1].getRate() > xSide[row + 1][col - 1].getRate()) {
                    if (!handingAfterNextPointFound(row, col - 1, true)) {
                        return false;
                    }
                } else if (Math.abs(xSide[row][col - 1].getRate() - xSide[row + 1][col - 1].getRate()) < precision) {
                    float[][] gridPoints = gridDataArys;
                    float ySideRC = (float) ((curContourLevel - gridPoints[row][col])
                            / (gridPoints[row + 1][col] - gridPoints[row][col]));
                    if (ySideRC <= 0.5f) {
                        if (!handingAfterNextPointFound(row, col - 1, true)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                            return false;
                        }
                    }
                } else {
                    if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                        return false;
                    }
                }
            } else {
                if (!handingAfterNextPointFound(row, col - 1, true)) {
                    return false;
                }
            }
        } else if (bottom == 1) {
            if (top == 2) {
                if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                    return false;
                }
            } else if (top == 1) {
                if (left == 2) {
                    if (!handingAfterNextPointFound(row, col - 1, false)) {
                        return false;
                    }
                } else if (left == 1) {
                    int maxval = maxvalPos(bottomMaxLevel, topMaxLevel, leftMaxLevel);
                    boolean flag = true;
                    if (isBottom && maxval == 1) {
                        if (!handingAfterNextPointFound(row, col - 1, true)) {
                            return false;
                        }
                    } else if (isTop && maxval == 2) {
                        if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                            return false;
                        }
                    } else if (isLeft && maxval == 3) {
                        if (!handingAfterNextPointFound(row, col - 1, false)) {
                            return false;
                        }
                    } else {
                        flag = false;
                    }
                    if (!flag) {
                        flag = true;
                        if (maxval == 12) {
                            if (isBottom && isTop) {
                                if (xSide[row][col - 1].getRate() > xSide[row + 1][col - 1].getRate()) {
                                    if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                                        return false;
                                    }
                                } else if (Math.abs(xSide[row][col - 1].getRate() - xSide[row + 1][col - 1].getRate()) < precision) {
                                    float[][] gridPoints = gridDataArys;
                                    float ySideRC = (float) ((curContourLevel - gridPoints[row][col])
                                            / (gridPoints[row + 1][col] - gridPoints[row][col]));
                                    if (ySideRC <= 0.5f) {
                                        if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                                            return false;
                                        }
                                    } else {
                                        if (!handingAfterNextPointFound(row, col - 1, true)) {
                                            return false;
                                        }
                                    }
                                } else {
                                    if (!handingAfterNextPointFound(row, col - 1, true)) {
                                        return false;
                                    }
                                }
                            } else if (isBottom) {
                                if (!handingAfterNextPointFound(row, col - 1, true)) {
                                    return false;
                                }
                            } else if (isTop) {
                                if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                                    return false;
                                }
                            } else {
                                flag = false;
                            }
                        } else if (maxval == 23) {
                            if (isTop) {
                                if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                                    return false;
                                }
                            } else if (isLeft) {
                                if (!handingAfterNextPointFound(row, col - 1, false)) {
                                    return false;
                                }
                            } else {
                                flag = false;
                            }
                        } else if (maxval == 13) {
                            if (isBottom) {
                                if (!handingAfterNextPointFound(row, col - 1, true)) {
                                    return false;
                                }
                            } else if (isLeft) {
                                if (!handingAfterNextPointFound(row, col - 1, false)) {
                                    return false;
                                }
                            } else {
                                flag = false;
                            }
                        } else if (maxval == 0) {
                            if (xSide[row][col - 1].getRate() > xSide[row + 1][col - 1].getRate()) {
                                if (!handingAfterNextPointFound(row, col - 1, true)) {
                                    return false;
                                }
                            } else if (Math.abs(xSide[row][col - 1].getRate() - xSide[row + 1][col - 1].getRate()) < precision) {
                                float[][] gridPoints = gridDataArys;
                                float ySideRC = (float) ((curContourLevel - gridPoints[row][col])
                                        / (gridPoints[row + 1][col] - gridPoints[row][col]));
                                if (ySideRC <= 0.5f) {
                                    if (!handingAfterNextPointFound(row, col - 1, true)) {
                                        return false;
                                    }
                                } else {
                                    if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                                        return false;
                                    }
                                }
                            } else {
                                if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                                    return false;
                                }
                            }
                        } else {
                            flag = false;
                        }
                    }
                    if (!flag) {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromRight2Left()出错");
                        return false;
                    }
                } else {//left=-1 0
                    if (isBottom && isTop) {
                        if (bottomMaxLevel > topMaxLevel) {
                            if (!handingAfterNextPointFound(row, col - 1, true)) {
                                return false;
                            }
                        } else if (bottomMaxLevel < topMaxLevel) {
                            if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                                return false;
                            }
                        } else if (bottomMaxLevel == topMaxLevel) {
                            if (bottomMaxLevel != 0) {
                                if (xSide[row][col - 1].getRate() > xSide[row + 1][col - 1].getRate()) {
                                    if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                                        return false;
                                    }
                                } else if (Math.abs(xSide[row][col - 1].getRate() - xSide[row + 1][col - 1].getRate()) < precision) {
                                    float[][] gridPoints = gridDataArys;
                                    float ySideRC = (float) ((curContourLevel - gridPoints[row][col])
                                            / (gridPoints[row + 1][col] - gridPoints[row][col]));
                                    if (ySideRC <= 0.5f) {
                                        if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                                            return false;
                                        }
                                    } else {
                                        if (!handingAfterNextPointFound(row, col - 1, true)) {
                                            return false;
                                        }
                                    }
                                } else {
                                    if (!handingAfterNextPointFound(row, col - 1, true)) {
                                        return false;
                                    }
                                }
                            } else {
                                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromRight2Left()出错");
                                return false;
                            }
                        }
                    } else if (isBottom) {
                        if (!handingAfterNextPointFound(row, col - 1, true)) {
                            return false;
                        }
                    } else if (isTop) {
                        if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                            return false;
                        }
                    } else if (left == 0) {
                        if (!handingAfterNextPointFound(row, col - 1, false)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromRight2Left()出错");
                        return false;
                    }
                }
            } else {//top=0 -1
                if (left == 2) {
                    if (!handingAfterNextPointFound(row, col - 1, false)) {
                        return false;
                    }
                } else if (left == 1) {
                    if (isBottom && isLeft) {
                        if (bottomMaxLevel == leftMaxLevel) {
                            if (bottomMaxLevel == 0) {
                                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromRight2Left()出错");
                                return false;
                            } else {
                                if (!handingAfterNextPointFound(row, col - 1, true)) {
                                    return false;
                                }
                            }
                        } else if (bottomMaxLevel > leftMaxLevel) {
                            if (!handingAfterNextPointFound(row, col - 1, true)) {
                                return false;
                            }
                        } else {
                            if (!handingAfterNextPointFound(row, col - 1, false)) {
                                return false;
                            }
                        }
                    } else if (isBottom) {
                        if (!handingAfterNextPointFound(row, col - 1, true)) {
                            return false;
                        }
                    } else if (isLeft) {
                        if (!handingAfterNextPointFound(row, col - 1, false)) {
                            return false;
                        }
                    } else if (top == 0) {
                        if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromRight2Left()出错");
                        return false;
                    }
                } else if (left == 0) {
                    if (top == 0) {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromRight2Left()出错");
                        return false;
                    }
                    if (isBottom) {
                        if (!handingAfterNextPointFound(row, col - 1, true)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row, col - 1, false)) {
                            return false;
                        }
                    }
                } else {//-1
                    if (isBottom) {
                        if (!handingAfterNextPointFound(row, col - 1, true)) {
                            return false;
                        }
                    } else if (top == 0) {
                        if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromRight2Left()出错");
                        return false;
                    }
                }
            }
        } else if (bottom == 0) {
            if (top == 2) {
                if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                    return false;
                }
            } else if (top == 1) {
                if (left == 2) {
                    if (!handingAfterNextPointFound(row, col - 1, false)) {
                        return false;
                    }
                } else if (left == 1) {
                    if (isTop && isLeft) {
                        if (leftMaxLevel == topMaxLevel) {
                            if (topMaxLevel == 0) {
                                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromRight2Left()出错");
                                return false;
                            } else {
                                if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                                    return false;
                                }
                            }
                        } else if (topMaxLevel > leftMaxLevel) {
                            if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                                return false;
                            }
                        } else {
                            if (!handingAfterNextPointFound(row, col - 1, false)) {
                                return false;
                            }
                        }
                    } else if (isTop) {
                        if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                            return false;
                        }
                    } else if (isLeft) {
                        if (!handingAfterNextPointFound(row, col - 1, false)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row, col - 1, true)) {
                            return false;
                        }
                    }
                } else if (left == 0) {
                    if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromRight2Left()出错");
                    return false;
                } else {
                    if (isTop) {
                        if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row, col - 1, true)) {
                            return false;
                        }
                    }
                }
            } else if (top == 0) {
                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromRight2Left()出错");
                return false;
            } else {
                if (left > 0) {
                    if (isLeft) {
                        if (!handingAfterNextPointFound(row, col - 1, false)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row, col - 1, true)) {
                            return false;
                        }
                    }
                } else if (left == 0) {
                    if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromRight2Left()出错");
                    return false;
                } else {
                    if (!handingAfterNextPointFound(row, col - 1, true)) {
                        return false;
                    }
                }
            }
        } else {//-1
            if (top == 2) {
                if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                    return false;
                }
            } else if (top == 1) {
                if (left == 2) {
                    if (!handingAfterNextPointFound(row, col - 1, false)) {
                        return false;
                    }
                } else if (left == 1) {
                    if (isTop && isLeft) {
                        if (topMaxLevel == leftMaxLevel) {
                            if (topMaxLevel == 0) {
                                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromRight2Left()出错");
                                return false;
                            } else {
                                if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                                    return false;
                                }
                            }
                        } else if (topMaxLevel > leftMaxLevel) {
                            if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                                return false;
                            }
                        } else {
                            if (!handingAfterNextPointFound(row, col - 1, false)) {
                                return false;
                            }
                        }
                    } else if (isTop) {
                        if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                            return false;
                        }
                    } else if (isLeft) {
                        if (!handingAfterNextPointFound(row, col - 1, false)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromRight2Left()出错");
                        return false;
                    }
                } else if (left == 0) {
                    if (isTop) {
                        if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row, col - 1, false)) {
                            return false;
                        }
                    }
                } else {
                    if (isTop) {
                        if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                            return false;
                        }
                    } else {
                        if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromRight2Left()出错");
                        return false;
                    }
                }
            } else if (top == 0) {
                if (left > 0) {
                    if (isTop) {
                        if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                            return false;
                        }
                    } else {
                        if (!handingAfterNextPointFound(row, col - 1, false)) {
                            return false;
                        }
                    }
                } else if (left == 0) {
                    if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromRight2Left()出错");
                    return false;
                } else {
                    if (!handingAfterNextPointFound(row + 1, col - 1, true)) {
                        return false;
                    }
                }
            } else {
                if (left >= 0) {
                    if (!handingAfterNextPointFound(row, col - 1, false)) {
                        return false;
                    }
                } else {
                    if (logger.isDebugEnabled()) logger.debug("In IsolineProcess traceFromRight2Left()出错");
                    return false;
                }
            }
        }
        return true;
    }

    private boolean handingAfterNextPointFound(int row, int col, boolean isHorizon) {
        if (row < 0 || row > gridRows - 1 || col < 0 || col > gridCols - 1) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess中handingAfterNextPointFound()中横纵坐标小于0或者大于格点数据的最大行列数");
            return false;
        }
        nextIsopoint.setAll(row, col, isHorizon);
        if (!calcCoord(row, col, isHorizon)) {
            if (logger.isDebugEnabled()) logger.debug("In IsolineProcess()中handingAfterNextPointFound()calcCoord()失败");
            return false;
        }
        if (isHorizon) {
            xSide[row][col].setHasIsopoint(false);
            xSide[row][col].setRate(-2.0);
            xSide[row][col].setLevel(-1);

        } else {
            ySide[row][col].setHasIsopoint(false);
            ySide[row][col].setRate(-2.0);
            ySide[row][col].setLevel(-1);

        }
        setCurVertex(nextIsopoint, curIsopoint);
        return true;
    }


    /**
     * 追踪封闭等值线
     */
    private boolean traceClosedIsoline() {
        //先从纵向追踪
        for (int j = 1; j < gridCols - 1; j++) {
            for (int i = 0; i < gridRows - 1; i++) {
                if (ySide[i][j].isHasIsopoint()) {
                    if (!curVertex[i][j].isAsStartP() || !curVertex[i + 1][j].isAsStartP()) {
                        continue;
                    }
                    preIsopoint.setAll(i, 0, false);
                    curIsopoint.setAll(i, j, false);
                    if (!traceOneClosedIsoline(true)) {//正向
                        if (logger.isDebugEnabled())
                            logger.debug("In IsolineProcess中traceCloedIsoline()中traceOneClosedIsoline()追踪一条闭合等值线row=" + i + "col=" + j + "失败");
                        continue;
                    }
                    //如果不闭合 ，反向追踪
                    if (procIsolineData != null && !procIsolineData.isClosed) {
                        preIsopoint.setAll(i, j + 1, false);
                        curIsopoint.setAll(i, j, false);
                        if (!traceOneClosedIsoline(false)) {//反向
                            if (logger.isDebugEnabled())
                                logger.debug("In IsolineProcess中traceCloedIsoline()中traceOneClosedIsoline()追踪一条闭合等值线失败");
                            continue;
                        }
                        if (!uniteObverseReverseIsoline()) {
                            if (logger.isDebugEnabled())
                                logger.debug("In IsolineProcess中traceCloedIsoline()中uniteObverseReverseIsoline()合并等值线失败");
                            continue;
                        }
                    }
                }
            }
        }

        //再从横向追踪
        for (int i = 1; i < gridRows - 1; i++) {
            for (int j = 0; j < gridCols - 1; j++) {
                if (xSide[i][j].isHasIsopoint()) {
                    if (!curVertex[i][j].isAsStartP() || !curVertex[i][j + 1].isAsStartP()) {
                        continue;
                    }
                    preIsopoint.setAll(0, j, true);
                    curIsopoint.setAll(i, j, true);
                    if (!traceOneClosedIsoline(true)) {//正向
                        if (logger.isDebugEnabled())
                            logger.debug("In IsolineProcess中traceCloedIsoline()中traceOneClosedIsoline()追踪一条闭合等值线row=" + i + "col=" + j + "失败");
                        continue;
                    }
                    //如果不闭合 ，反向追踪
                    if (procIsolineData != null && !procIsolineData.isClosed) {
                        preIsopoint.setAll(i + 1, j, true);
                        curIsopoint.setAll(i, j, true);
                        if (!traceOneClosedIsoline(false)) {//反向
                            if (logger.isDebugEnabled())
                                logger.debug("In IsolineProcess中traceCloedIsoline()中traceOneClosedIsoline()追踪一条闭合等值线失败");
                            continue;
                        }

                        //反向追踪的等值线和正向追踪的等值线要合二为一
                        if (!uniteObverseReverseIsoline()) {
                            if (logger.isDebugEnabled())
                                logger.debug("In IsolineProcess中traceCloedIsoline()中uniteObverseReverseIsoline()合并等值线失败");
                            continue;
                        }
                    }
                }
            }
        }
        return true;
    }


    private boolean uniteObverseReverseIsoline() {
        IsolineDataProc obverseIsoline = new IsolineDataProc();
        IsolineDataProc reverseIsoline = new IsolineDataProc();
        obverseIsoline = procIsolineDataList.get(procIsolineDataList.size() - 2);
        reverseIsoline = procIsolineDataList.get(procIsolineDataList.size() - 1);
        float level1 = obverseIsoline.val;
        float level2 = reverseIsoline.val;
        if (Math.abs(level1 - level2) > precision) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess.traceClosedIsoline().uniteObverseReverseIsoline()正向和反向追踪的等值线level不同");
            return false;
        }
        boolean isClosed1 = obverseIsoline.isClosed;
        boolean isClosed2 = reverseIsoline.isClosed;
        if (isClosed1 || isClosed2) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess.traceClosedIsoline().uniteObverseReverseIsoline()正向和反向追踪的等值线isClosed不同");
            return false;
        }
        int num1 = obverseIsoline.num;
        int num2 = reverseIsoline.num;
        int index1 = obverseIsoline.index;
        int index2 = reverseIsoline.index;
        if (index1 != index2) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess.traceClosedIsoline().uniteObverseReverseIsoline()正向和反向追踪的等值线index不一致");
            return false;
        }
        int indexProc1 = obverseIsoline.indexProc;
        int indexProc2 = reverseIsoline.indexProc;
        if (indexProc2 != indexProc1 + 1) {
            if (logger.isDebugEnabled())
                logger.debug("In IsolineProcess.traceClosedIsoline().uniteObverseReverseIsoline()正向和反向追踪的等值线不相邻");
            return false;
        }
        ArrayList<Integer> colList1 = obverseIsoline.colsList;
        ArrayList<Integer> colList2 = reverseIsoline.colsList;
        ArrayList<Integer> rowList1 = obverseIsoline.rowsList;
        ArrayList<Integer> rowList2 = reverseIsoline.rowsList;
        ArrayList<Boolean> isHorizon1 = obverseIsoline.isHorizonList;
        ArrayList<Boolean> isHorizon2 = reverseIsoline.isHorizonList;
        ArrayList<Point2D.Double> lineList1 = obverseIsoline.lineList2D;
        ArrayList<Point2D.Double> lineList2 = reverseIsoline.lineList2D;
        ArrayList<Isopoint> isoptList1 = obverseIsoline.isopointList;
        ArrayList<Isopoint> isoptList2 = reverseIsoline.isopointList;

        IsolineDataProc obverseReverseIsoline = new IsolineDataProc();
        obverseReverseIsoline.val = level1;
        obverseReverseIsoline.num = num1 + num2 - 1;
        obverseReverseIsoline.isClosed = false;
        obverseReverseIsoline.index = index1;
        obverseReverseIsoline.indexProc = indexProc1;
        obverseReverseIsoline.colsList = new ArrayList<Integer>();
        obverseReverseIsoline.rowsList = new ArrayList<Integer>();
        obverseReverseIsoline.isHorizonList = new ArrayList<Boolean>();
        obverseReverseIsoline.lineList2D = new ArrayList<Point2D.Double>();
        obverseReverseIsoline.isopointList = new ArrayList<Isopoint>();

        Collections.reverse(colList2);
        Collections.reverse(rowList2);
        Collections.reverse(isHorizon2);
        Collections.reverse(lineList2);
        Collections.reverse(isoptList2);

        colList2.remove(colList2.size() - 1);
        rowList2.remove(rowList2.size() - 1);
        isHorizon2.remove(isHorizon2.size() - 1);
        lineList2.remove(lineList2.size() - 1);
        isoptList2.remove(isoptList2.size() - 1);

        obverseReverseIsoline.colsList.addAll(colList2);
        obverseReverseIsoline.colsList.addAll(colList1);
        obverseReverseIsoline.rowsList.addAll(rowList2);
        obverseReverseIsoline.rowsList.addAll(rowList1);
        obverseReverseIsoline.isHorizonList.addAll(isHorizon2);
        obverseReverseIsoline.isHorizonList.addAll(isHorizon1);
        obverseReverseIsoline.lineList2D.addAll(lineList2);
        obverseReverseIsoline.lineList2D.addAll(lineList1);
        obverseReverseIsoline.isopointList.addAll(isoptList2);
        obverseReverseIsoline.isopointList.addAll(isoptList1);

        procIsolineDataList.remove(procIsolineDataList.size() - 1);//反向
        procIsolineDataList.remove(procIsolineDataList.size() - 1);//正向
        procIsolineDataList.add(obverseReverseIsoline);//合二为一

        isolineIndex--;
        return true;
    }


    /**
     * 追踪一条封闭的等值线
     *
     * @param isObverse 是否为正向
     * @return
     */
    private boolean traceOneClosedIsoline(boolean isObverse) {
        int startI = curIsopoint.getRow();
        int startJ = curIsopoint.getCol();
        Boolean flag = curIsopoint.getIsHorizon();
        procIsolineData = new IsolineDataProc();
        procIsolineData.lineList2D = new ArrayList<Point2D.Double>();
        procIsolineData.val = contourLevel;
        procIsolineData.isClosed = true;
        procIsolineData.colsList = new ArrayList<Integer>();
        procIsolineData.rowsList = new ArrayList<Integer>();
        procIsolineData.isHorizonList = new ArrayList<Boolean>();
        procIsolineData.isopointList = new ArrayList<Isopoint>();
        procIsolineData.isObverse = isObverse;

        procIsolineData.maxX = Float.MIN_VALUE;
        procIsolineData.minX = Float.MAX_VALUE;
        procIsolineData.maxY = Float.MIN_VALUE;
        procIsolineData.minY = Float.MAX_VALUE;

        procIsolineData.startLineType = getLineType(curIsopoint);

        if (!calcCoord(curIsopoint.getRow(), curIsopoint.getCol(), flag)) {
            if (logger.isDebugEnabled()) logger.debug("In IsolineProcess中traceOneClosedIsoline()calcCoord()失败");
            return false;
        }

        Boolean isHorizon = curIsopoint.getIsHorizon();
        if (isHorizon != null) {
            if (curIsopoint.getIsHorizon()) {
                xSide[startI][startJ].setLevel(0);
            } else {
                ySide[startI][startJ].setLevel(0);
            }
        } else {
            if (logger.isDebugEnabled()) logger.debug("n IsolineProcess中traceOneClosedIsoline()出错");
            return false;
        }

        Boolean isNullPoint = isHasNullPoint(preIsopoint, curIsopoint);
        if (isNullPoint != null && isNullPoint) {
            addIsoline();
            procIsolineData.isClosed = false;
            setCurVertex(curIsopoint, preIsopoint);
            if (!isObverse) {
                resetXYSide();
            }
            return true;
        }
        if (!traceNextPoint()) {
            if (logger.isDebugEnabled()) logger.debug("In IsolineProcess中traceOneClosedIsoline()追踪下一个点失败");
            resetXYSide();
            procIsolineData = null;
            return false;

        }
        preIsopoint.setAll(curIsopoint.getRow(), curIsopoint.getCol(), curIsopoint.getIsHorizon());
        curIsopoint.setAll(nextIsopoint.getRow(), nextIsopoint.getCol(), nextIsopoint.getIsHorizon());
        isNullPoint = isHasNullPoint(preIsopoint, curIsopoint);
        if (isNullPoint != null && isNullPoint) {
            addIsoline();
            procIsolineData.isClosed = false;
            setCurVertex(curIsopoint, preIsopoint);
            if (!isObverse) {
                resetXYSide();
            }
            return true;
        }

        boolean isClosed = false;
        while (!isClosed) {
            if (!traceNextPoint()) {
                if (logger.isDebugEnabled()) logger.debug("In IsolineProcess中traceOneClosedIsoline()追踪下一个点失败");
                resetXYSide();
                procIsolineData = null;
                return false;
            }
            preIsopoint.setAll(curIsopoint.getRow(), curIsopoint.getCol(), curIsopoint.getIsHorizon());
            curIsopoint.setAll(nextIsopoint.getRow(), nextIsopoint.getCol(), nextIsopoint.getIsHorizon());
            isHorizon = curIsopoint.getIsHorizon();
            isClosed = (curIsopoint.getRow() == startI)
                    && (curIsopoint.getCol() == startJ)
                    && (isHorizon != null && isHorizon.equals(flag));
            //增加2013-01-17
            if (!isClosed) {
                isHorizon = curIsopoint.getIsHorizon();
                isClosed = (curIsopoint.getRow() == 0 && isHorizon != null && isHorizon)
                        || (curIsopoint.getCol() == 0 && isHorizon != null && !isHorizon)
                        || (curIsopoint.getRow() == gridRows - 1)
                        || (curIsopoint.getCol() == gridCols - 1);
                if (isClosed) {
                    if (logger.isDebugEnabled()) logger.debug("In IsolineProcess中traceOneClosedIsoline()追踪下一个点失败");
                    resetXYSide();
                    procIsolineData = null;
                    return false;
                }
            }

            //判断当前点所在方格内是否存在null即NULLVAL的值,若存在则停止追踪,则此条等值线为开等值线
            if (!isClosed) {
                isNullPoint = isHasNullPoint(preIsopoint, curIsopoint);
                if (isNullPoint != null && isNullPoint) {
                    isClosed = true;
//					hasNullVal = true;
                    procIsolineData.isClosed = false;
                }
            }
        }

        addIsoline();
        if (!isObverse || procIsolineData.isClosed) {
            resetXYSide();
        }
//		setCurVertex(curIsopoint, preIsopoint);
        procIsolineData.endLineType = getLineType(curIsopoint);
        return true;
    }

    ///////////////获取追踪完成的等值线///////////////

    /**
     * 追踪等值线，并包含多边形的构建高低值的标注
     *
     * @return 追踪后等值线
     */
    public ArrayList<IsolineData> getIsolinesData() {
        long s1 = System.currentTimeMillis();
        boolean isGetCenterPos = this.attr.isNeedGetClosedCenterPos();
//		boolean isGetCenterPos = true;//不标注高低中心也找，为了删除用
        // 追踪是否成功
        if (procIsolineDataList.isEmpty()) {
            if (!getIsolineDataProcList()) {
                if (logger.isDebugEnabled())
                    logger.debug("In IsolineProcess中getIsolinesData()时procIsolineDataList为空时获取等值线失败");
                return null;
            }
        }

        if (procIsolineDataList.isEmpty()) {
            if (logger.isDebugEnabled()) logger.debug("In IsolineProcess中getIsolinesData() 无符合level条件的等值线");
            return null;
        }

        long s2 = System.currentTimeMillis();
        if (logger.isDebugEnabled())
            logger.debug("追踪用时：" + (s2 - s1));

        resIsolineDataList = new ArrayList<IsolineData>();

        //找闭合等值线里面的最大最小值和位置
        if (isGetCenterPos && closedPolygonContainList.isEmpty()) {
            closedIsolinePolygonContainBuild();
        }

        long s3 = System.currentTimeMillis();
        if (logger.isDebugEnabled())
            logger.debug("找最值用时：" + (s3 - s2));

        IsolineData anIsolineData = null;
        IsolineDataProc pIsolineData;
        float elevation = 10000;
        if (attr.isSetElevation()) {
            elevation = attr.getElevation();
        }
        IsolineOnlyPgn onlyPolygon = null;
        boolean isFilter = this.attr.isFilter();

        float sX = 0;
        float eX = 0;
        float sY = 0;
        float eY = 0;
        if (attr.isXYCoordinate()) {
            getRealXYSE();
            sX = realXSE[0];
            eX = realXSE[1];
            sY = realYSE[0];
            eY = realYSE[1];
        }
        for (int i = 0; i < procIsolineDataList.size(); i++) {
            if (procIsolineDataList.get(i).index == -1) {
                continue;
            }
//			if(procIsolineDataList.get(i).isClosed && this.attr.isFilter()){
            if (isFilter) {//过滤点数或直径小的等值线
                if (procIsolineDataList.get(i).num < 10 ||
                        Math.max(Math.abs(procIsolineDataList.get(i).maxX - procIsolineDataList.get(i).minX)
                                , Math.abs(procIsolineDataList.get(i).maxY - procIsolineDataList.get(i).minY)) < 0.25f) {
//					logger.println("输出过滤"+i+"条等值线");
                    continue;
                }
            }
            if (procIsolineDataList.get(i).num <= 1) {//无论设置与否，一个点的等值线都过滤
                continue;
            }

            anIsolineData = new IsolineData();
            pIsolineData = procIsolineDataList.get(i);
            anIsolineData.index = pIsolineData.index;
            anIsolineData.val = pIsolineData.val;
            anIsolineData.isClosed = pIsolineData.isClosed;
            anIsolineData.num = pIsolineData.num;
            if (isGetCenterPos && anIsolineData.isClosed) {
                onlyPolygon = IsolineOnlyPgnListBuild(pIsolineData);
                if (onlyPolygon == null) {
                    continue;
                }
                anIsolineData.highlowVal = onlyPolygon.highlowVal;
                anIsolineData.highlowPosList = onlyPolygon.highlowPosList;
                anIsolineData.isHigh = onlyPolygon.isHigh;
                anIsolineData.isInner = onlyPolygon.isInner;
                anIsolineData.isOuter = onlyPolygon.isOuter;
            }
            anIsolineData.lineList = new ArrayList<Position>();
            for (int j = 0; j < pIsolineData.lineList2D.size(); j++) {
                PositionVec pos = PositionVec.fromDegrees(pIsolineData.lineList2D.get(j).y, pIsolineData.lineList2D.get(j).x, elevation);
                if (attr.isXYCoordinate()) {
                    Point2D.Double tmpPt = IsolineUtil.mergeLonLatToXY(pIsolineData.lineList2D.get(j), sX, eX, sY, eY,
                            attr.getXSLon(), attr.getXELon(), attr.getYSLat(), attr.getYELat());
                    pos.setXY(tmpPt.x, tmpPt.y);
                }
                anIsolineData.lineList.add(pos);
            }
            resIsolineDataList.add(anIsolineData);
        }
        if (logger.isDebugEnabled())
            logger.debug("等值线追踪总时间：" + (System.currentTimeMillis() - s1));
        return resIsolineDataList;
    }

    private void getRealXYSE() {
        realXSE = new float[2];
        realYSE = new float[2];
        realXSE = MIDS3DMath.getMaxMinFrom2Arr(gridXYCoordX, true);
        realYSE = MIDS3DMath.getMaxMinFrom2Arr(gridXYCoordY, false);
    }

    public void dispose() {
        procIsolineData = null;
        procIsolineDataList = null;// 所有等值线
        polygonProcList = null;// 所有多边形
        polygonClosedProcList = null;// 所有闭合等值线所构成的多边形
        polygonContainList = null;// 找完最值后的多边形
        closedPolygonContainList = null;// 找完最值后的闭合等值线多边形
        polygonList = null;//最内最外闭合等值线多边形,标高低中心
		/*if (this.scatterDataList != null && this.scatterDataList.isEmpty()){
			for (ScatterData data : this.scatterDataList){
				if (data != null){
					data.dispose();
					data = null;
				}
			}
			this.scatterDataList.clear();
			this.scatterDataList = null;
		}*/
        if (this.gridDataList != null && this.gridDataList.isEmpty()) {
            for (GridData data : this.gridDataList) {
                if (data != null) {
                    data.dispose();
                    data = null;
                }
            }
            this.gridDataList.clear();
            this.gridDataList = null;
        }
    }
    ///////////////获取追踪完成的等值线///////////////

}

