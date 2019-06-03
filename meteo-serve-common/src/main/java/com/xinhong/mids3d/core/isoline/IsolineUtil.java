package com.xinhong.mids3d.core.isoline;

import com.xinhong.mids3d.core.geom.PositionVec;
import com.xinhong.mids3d.datareader.util.DataType;
import com.xinhong.mids3d.datareader.util.ElemCode;
import gov.nasa.worldwind.geom.Position;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.management.RuntimeErrorException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

/**
 * 等值线公用类
 */
public final class IsolineUtil {
	
	public static final double DNULLVAL = -99999.9;// 表示NULL值
	public static final float NULLVAL = -99999.9f;// 表示NULL值
	public static final double OFFSET = 0.00001;//偏移量
//	public static final float SHIFT = 0.00001f;// 修正值
	public static final float SHIFT = 0.001f;// 修正值
	public static final float PRECISION = (float) 1E-6;// 对比位置
	public static final double EPSILON1 = 1E6;//多边形用
	public static final double EPSILON2 = 1E-6;//多边形用，浮点值对比数据
	public static final double EPSILON3 = 1E7;//多边形用
	public static final double EPSILON4 = 1E-7;//多边形用，浮点值对比数据
	
	private IsolineUtil(){
		
	}

	/**
	 * 由必填值、间隔、最大最小值找到所有等级
	 * @param valBaseVal 必填值
	 * @param valInterval 间隔
	 * @param minVal 最小值
	 * @param maxVal 最大值
	 * @return 所有levels
	 */
	public static float[] getLevelsFromMinMaxBaseVal(float valBaseVal, float valInterval, float minVal, float maxVal) {
		int multiple = 100;
		int baseVal = (int) (valBaseVal*multiple);
		int interVal = (int) (valInterval*multiple);
		int minval = (int)(minVal*multiple);
		int maxval = (int)(maxVal*multiple);
		ArrayList<Integer> levelsList = new ArrayList<Integer>();
		levelsList.add(baseVal);
		int tmpVal = baseVal;
		int count = 0;
		while (tmpVal >= minval) {
			tmpVal = tmpVal - Math.abs(interVal);
			if (tmpVal < minval)
				break;
			levelsList.add(tmpVal);
			count++;
			if (count > 10000) {
				throw new RuntimeErrorException(null, "给定值计算levels级别太多,请检查是否正确设置最大最小值");
			}
		}
		tmpVal = baseVal;
		count = 0;
		while (tmpVal <= maxval) {
			tmpVal = tmpVal +  Math.abs(interVal);
			if (tmpVal > maxval)
				break;
			levelsList.add(tmpVal);
			count++;
			if (count > 10000) {
				throw new RuntimeErrorException(null, "给定值计算levels级别太多,请检查是否正确设置最大最小值");
			}
		}
		float[] levels = new float[levelsList.size()];
		for (int i = 0; i < levelsList.size(); i++) {
			levels[i] = levelsList.get(i)/(float)multiple;
		}
		return levels;
	}
	
	/**
	 * 根据dataType获取dataType节点配置文件
	 * @param dataType 数据来源类型：如：OZGD、DMQX
	 * @param elem 要素
	 * @param press 层次
	 * @param configMap 所有需要获取的内容,已初始化
	 * @param nodelist 此级节点
	 * @return
	 */
	public static boolean loadBaseOnDataTypeConfig(String dataType, ElemCode elem, String press, Map<String, String> configMap, NodeList nodelist) {
		for (int i = 0; i < nodelist.getLength(); i++) {
			Node node = nodelist.item(i);
			if (!(node instanceof Element))
				continue;
			if (node.getNodeName().equals(dataType)) {
				// 找要素
				boolean res = loadBaseOnElemConfig(elem, press, configMap, node.getChildNodes());
				if (!res) {
					if (dataType == "AllType") {
						throw new RuntimeErrorException(null, "缺少" + elem.toString() + "要素的配置文件");
					}
					return false;
				} else {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 根据要素获取要素级配置文件
	 * @param elem 要素
	 * @param press 层次
	 * @param configMap 所有需要获取的内容,已初始化
	 * @param nodelist 此级节点
	 * @return
	 */
	public static boolean loadBaseOnElemConfig(ElemCode elem, String press, Map<String, String> configMap, NodeList nodelist) {
		for (int i = 0; i < nodelist.getLength(); i++) {
			Node node = nodelist.item(i);
			if (!(node instanceof Element))
				continue;
			if (node.getNodeName().equals(elem.toString())) {
				// 找层次
				boolean res = loadBaseOnPressConfig(press, configMap, node
						.getChildNodes());
				if (!res) {
					// 缺少...要素的配置信息
					if (press != null) {
						throw new RuntimeErrorException(null, "缺少" + elem.toString() + "要素" + press + "hpa的配置文件");
					} else {
						throw new RuntimeErrorException(null, "缺少" + elem.toString() + "要素的配置文件");
					}
				} else {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 根据condition获取press等其他条件级的配置文件
	 * @param press 要素
	 * @param configMap 所有需要获取的内容,已初始化
	 * @param nodelist 此级节点
	 * @return
	 */
	public static boolean loadBaseOnPressConfig(String press, Map<String, String> configMap, NodeList nodelist) {
		Map<String, Boolean> hasConfigMap = new HashMap<String, Boolean>(
				configMap.size());
		Set<Entry<String, String>> all = configMap.entrySet();
		Iterator<Entry<String, String>> iter = all.iterator();
		while (iter.hasNext()) {
			Map.Entry<String, String> me = (Map.Entry<String, String>) iter.next();
			hasConfigMap.put(me.getKey(), false);
		}

		if (press != null) {
			for (int i = 0; i < nodelist.getLength(); i++) {
				Node node = nodelist.item(i);
				if (!(node instanceof Element))
					continue;
				if (node.getNodeName().equals("Condition")) {
					Element pe = (Element) node;
					if (pe.getAttribute("Press").toString().equals(press)) { // 找对应的层次
						NodeList lev = pe.getChildNodes();
						for (int t = 0; t < lev.getLength(); t++) {
							Node ld = lev.item(t);
							if (!(ld instanceof Element)) {
								continue;
							}
							if (configMap.containsKey(ld.getNodeName())) {
								configMap.put(ld.getNodeName(), ld.getTextContent().trim());
								hasConfigMap.put(ld.getNodeName(), true);
							}
						}
					}
				}
			}
		}

		if (hasConfigMap.containsValue(false)) { // 没有设置完成,需要从公用里面找
			for (int i = 0; i < nodelist.getLength(); i++) {
				Node ld = nodelist.item(i);
				if (!(ld instanceof Element)) {
					continue;
				}
				if (hasConfigMap.get(ld.getNodeName()) == null) {
					continue;
				}
				boolean hasConfig = hasConfigMap.get(ld.getNodeName());
				if (configMap.containsKey(ld.getNodeName()) && !hasConfig) {
					configMap.put(ld.getNodeName(), ld.getTextContent());
					hasConfigMap.put(ld.getNodeName(), true);
				}
			}
		}

//		if (hasConfigMap.containsValue(false))
//			return false;
//		else
//			return true;
		return true;
	}

	/**
	 * 获取配置文件
	 * @param configMap 所有需要获取的内容,已初始化
	 * @param dataType 数据来源类型：如：OZGD、DMQX
	 * @param fileName 配置文件路径
	 * @param elem 要素
	 * @param press 层次
	 * @param type 数据类型 如：GridData、ScatterData
	 * @return
	 */
	public static boolean loadBaseOnIsolineLevelConfig(Map<String, String> configMap, DataType dataType, String fileName, ElemCode elem, String press,
			IsolineSrcDataType type) {	
		try {
//			InputStream is = ClassLoader.getSystemResourceAsStream(file);
			InputStream is = null;
			File file = new File(fileName);
			if(file.exists()){
				is = new FileInputStream(fileName);
			}else{
				if(elem.isHyElemCode()){
					fileName = "com/xinhong/config/isoline/isolineprocess/IsolineProcessConfig_HY.xml";
				}else if(type.equals(IsolineSrcDataType.GridData)){
					fileName = "com/xinhong/config/isoline/isolineprocess/IsolineProcessConfig_GridData.xml";
				}else if(type.equals(IsolineSrcDataType.ScatterData)){
					fileName = "com/xinhong/config/isoline/isolineprocess/IsolineProcessConfig_ScatterData.xml";
				}else{
					System.out.println("此类型" + type + "的配置文件不存在");
				}
				is = ClassLoader.getSystemResourceAsStream(fileName);
			}				
			if(is==null){
				System.out.println("等值线追踪所需要的配置文件不存在");
				return false;
			}

			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(is);
			Element root = doc.getDocumentElement();
			// 首先验证格式符及版本号
			if(elem.isHyElemCode()){
				if (!root.getTagName().equals("HYCONFIGXML")) {
					throw new RuntimeErrorException(null,
							"指定的XML文件不是可识别的HYCONFIGXML格式.");
				}
			}else if(type!=null){
				if (type.equals(IsolineSrcDataType.GridData)) {
					if (!root.getTagName().equals("GRIDCONFIGXML")) {
						throw new RuntimeErrorException(null,
								"指定的XML文件不是可识别的GRIDCONFIGXML格式.");
					}
				}else if (type.equals(IsolineSrcDataType.ScatterData)) {
					if (!root.getTagName().equals("SCATTERCONFIGXML")) {
						throw new RuntimeErrorException(null,
								"指定的XML文件不是可识别的SCATTERCONFIGXML格式.");
					}
				}else{
					System.out.println("读配置文件type为null");
				}
			}
			String version = root.getAttribute("version");
			if (version != "") {
			}

			// 读取各个节点
			NodeList children = root.getChildNodes();
			// 1.找DataType
			// 2.找要素
			// 3.找层次
			// 4.找其他（如年月时等）
			// 5.找通用
			String strDataType = null;
			if (dataType != null) {
				strDataType = dataType.toString();
			}
			String strAllType = "AllType";
			boolean res = false;
			if (strDataType != null) {
				res = loadBaseOnDataTypeConfig(strDataType, elem, press, configMap, children);
			}

			if (!res) {
				res = loadBaseOnDataTypeConfig(strAllType, elem, press, configMap, children);
			}

			if (configMap.containsValue(null)) {
				System.out.println("配置文件中追踪等值线所需的某个参数未设置，请检查配置文件！");
				throw new RuntimeException("读取配置文件失败" + elem + "," + press + "," + strDataType);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {

		}
		if (configMap.isEmpty()) {
			return false;
		}

		return true;
	}
	
	/**
	 * 读取要素计算的配置文件
	 * @param dType 数据类型
	 * @return
	 */
	public static ElemCode[] loadCalcElemsFromConfig(DataType dType){
		String[] elems = null;
		String fileName = null;
		InputStream is = null;
		try{
			fileName =  "config/isoline/isolineprocess/IsolineProcessConfig_WeatherElem.xml";
			File file = new File(fileName);			
			if(file.exists()){
				is = new FileInputStream(fileName);
			}else {
				fileName = "com/xinhong/config/isoline/isolineprocess/IsolineProcessConfig_WeatherElem.xml";
				is = ClassLoader.getSystemResourceAsStream(fileName); 
			}			
			if(is==null){				
				System.out.println("计算大气要素所需要的配置文件不存在");
				return null;
			}
			
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();				
			Document doc = builder.parse(is);
			Element root = doc.getDocumentElement();
			//首先验证格式符及版本号	
			if (!root.getTagName().equals("WEATHERELEMCONFIGXML")){
				throw new RuntimeErrorException(null, "指定的XML文件不是可识别的WEATHERELEMCONFIGXML格式.");
			}
			String version = root.getAttribute("version");
			if (version != ""){
			}		
			
			//读取各个节点			
			NodeList children = root.getChildNodes();
			for (int i=0; i<children.getLength(); i++){
				Node node = children.item(i);			
				if (!(node instanceof Element))
					continue;
				if (node.getNodeName().equals(dType.toString())){
					 String[] str = node.getTextContent().trim().split("\n\t");
					 if (str[0] != null && str[0].length() > 0){
						 elems = str[0].split(",");
						 break;
					 }
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}	
		
		ElemCode[] elemCode = null;
		if(elems!=null){
			elemCode = new ElemCode[elems.length];
			for(int i=0; i<elems.length; i++){
				elemCode[i] = ElemCode.fromValue(elems[i].trim());
			}
		}		
		return elemCode;
	}

	/**
	 * 字符串(press)格式化为4位,不足补0
	 * @param val
	 * @return
	 */
	public static String formatPressto4(String val) {
		String resVal = "";
		int size = val.getBytes().length;
		if (size < 4) {
			for (int i = 0; i < 4 - size; i++) {
				resVal = "0" + val;
			}
		} else {
			resVal = val;
		}
		return resVal;
	}
	
	/**
	 * 字符串格式化为3位，不足补0
	 * @param val
	 * @return
	 */
	public static String formatValto3(String val){
		String resVal = "";
		int size = val.getBytes().length;
		if (size < 3) {
			resVal = val;
			for (int i = 0; i < 3 - size; i++) {
				resVal = "0" + resVal;
			}
		} else {
			resVal = val;
		}
		return resVal;
	}

	/**
	 * 数据类型
	 * @author 
	 *
	 */
	public enum IsolineSrcDataType {
		ScatterData, GridData, NCData;
	}

	/**
	 * 流线要素类型
	 * @author 
	 *
	 */
	public enum SrcStreamlineElemType {
		UUVV, WDWS;
	}
	
	/**
	 * 判断点是否在多边形里面或边界上
	 * @param x
	 * @param y
	 * @param poly 多边形
	 * @return true表示多边形包含此点，否则为false
	 */
	public static boolean isPolygonContainPoint(double x, double y, Polygon poly) {
		for (int i = 0; i < poly.npoints; i++) { 
			int j = i + 1;
			if (j == poly.npoints) {j = 0;}
			
			if (x == poly.xpoints[i] && y == poly.ypoints[i]) {return true;}//同一个点
			else if ((x - poly.xpoints[i]) * (x - poly.xpoints[j]) < 0 && y == poly.ypoints[i] && y == poly.ypoints[j]) {return true;}//在同一个边上
			else if ((y - poly.ypoints[i]) * (y - poly.ypoints[j]) < 0 && x == poly.xpoints[i] && x == poly.xpoints[j]) {return true;}//在同一个边上
			else if ((x - poly.xpoints[i]) * (x - poly.xpoints[j]) < 0 && (y - poly.ypoints[i]) * (y - poly.ypoints[j]) < 0
					&& ((y - poly.ypoints[i]) / (poly.ypoints[j] - poly.ypoints[i]) == (x - poly.xpoints[i]) / (poly.xpoints[j] - poly.xpoints[i]))) {
				return true;
			}//倾斜
		}
		if (poly.contains(x, y)) {return true;} // 里面	
		else{return false;}
	}
	
	/**
	 * 根据U和V计算角度(计算流线图用)
	 * @param u U分量
	 * @param v V分量
	 * @return
	 */
	public static float uv2Angle(float u, float v) {
		if(u==NULLVAL || v==NULLVAL){
			return NULLVAL;
		}
		float angle = 0;
		if (u == 0) {
			if (v == 0) {
				return 0.0f;
			} else if (v > 0) {
				return 90.0f;
			} else {
				return 270.0f;
			}
		} else {
			float tmpAngle = (float) Math.toDegrees(Math.atan(v / u));
			if (u > 0) {
				if (v > 0) {
					angle = tmpAngle;
				} else if (v < 0) {
					angle = tmpAngle + 360.0f;
				} else {
					angle = 0.0f;
				}
			} else {
				if (v > 0) {
					angle = tmpAngle + 180.0f;
				} else if (v < 0) {
					angle = tmpAngle + 180.0f;
				} else {
					angle = 180.0f;
				}
			}
		}
		return angle;
	}
	
	/**
	 * 是否为降水要素
	 * @param elem 要素
	 * @return
	 */
	public static boolean isRainElem(String elem){
		if (elem.toUpperCase().startsWith("RAIN"))
			return true;
		else
			return false;
//		final ElemCode[] rainElem = new ElemCode[]{ElemCode.RAIN01, ElemCode.RAIN02, ElemCode.RAIN03,
//										     ElemCode.RAIN06, ElemCode.RAIN09, ElemCode.RAIN12,
//										     ElemCode.RAIN15, ElemCode.RAIN18, ElemCode.RAIN24,
//										     ElemCode.RAIN36, ElemCode.RAIN48, ElemCode.RAINDAY};
//		for(int i=0; i<rainElem.length; i++){
//			if(elem.equals(rainElem[i].toString())){
//				return true;
//			}
//		}
//		return false;
	}

	/**
	 * 将xy坐标系的坐标值归并到经纬度坐标系，经纬度的坐标由sLatLon和eLatLon决定
	 * 归并之前找到每行极大极小值，然后取最大最小值根据sLatLon和eLatLon归并
	 * @param xy xy坐标系坐标值
	 * @param sLatLon 起始经纬度
	 * @param eLatLon 结束经纬度
	 * @param isLon 是否为经度
	 * @return 经纬度坐标系的坐标
	 */
	public static float[][] mergeXYToLonLat(float[][] xy, float sLatLon, float eLatLon, boolean isLon){
		if(isLon){
			if(!isBetweenTwoVals(sLatLon, -180, 180) || !isBetweenTwoVals(eLatLon, -180, 180)){
				System.out.println("In IsolineUtil.mergeXYToLatLon()中超出了经度的范围");
				return null;
			}
		}else{
			if(!isBetweenTwoVals(sLatLon, -90, 90) || !isBetweenTwoVals(eLatLon, -90, 90)){
				System.out.println("In IsolineUtil.mergeXYToLatLon()中超出了纬度的范围");
				return null;
			}
		}
		int row = xy.length;
		int col = xy[0].length;
		
		boolean isDesc = true;
		float sVal = 0;
		float eVal = 0;
		if(isLon){
			sVal = xy[0][0];
			eVal = xy[0][col-1];
		}else{
			sVal = xy[0][0];
			eVal = xy[row-1][0];
		}
		
		if(sVal<eVal){
			isDesc = false;
		}else if(sVal>eVal){
			isDesc = true;
		}else{
			//起止坐标相等
			System.out.println("In IsolineUtil.mergeXYToLatLon()中只有一列数据,无法归并");
			return null;
		}
		float[][] resVal = new float[row][col];
		if(isLon){
			if(!isDesc){
				for(int i=0; i<row; i++){
					if(sVal>xy[i][0]){
						sVal = xy[i][0];
					}
					
					if(eVal<xy[i][col-1]){
						eVal = xy[i][col-1];
					}
				}
			}else{
				for(int i=0; i<row; i++){
					if(sVal<xy[i][0]){
						sVal = xy[i][0];
					}
					
					if(eVal>xy[i][col-1]){
						eVal = xy[i][col-1];
					}
				}
			}
			for(int i=0; i<row; i++){
				for(int j=0; j<col; j++){
					resVal[i][j] = sLatLon + (xy[i][j] - sVal) / (eVal-sVal) * (eLatLon - sLatLon);
				}
			}
		}else{
			if(!isDesc){
				for(int i=0; i<col; i++){
					if(sVal>xy[0][i]){
						sVal = xy[0][i];
					}
					
					if(eVal<xy[row-1][i]){
						eVal = xy[row-1][i];
					}
				}
			}else{
				for(int i=0; i<col; i++){
					if(sVal<xy[0][i]){
						sVal = xy[0][i];
					}
					
					if(eVal>xy[row-1][i]){
						eVal = xy[row-1][i];
					}
				}
			}
			for(int j=0; j<col; j++){
				for(int i=0; i<row; i++){
					resVal[i][j] = sLatLon + (xy[i][j] - sVal) / (eVal-sVal) * (eLatLon - sLatLon);
				}
			}
		}
		return resVal;
	}
	
	/**
	 * 将经纬度坐标归并到xy坐标
	 * @param lonlat 点
	 * @param x x坐标
	 * @param y y坐标
	 * @param sLon 经纬坐标系的起始经度
	 * @param eLon 经纬坐标系的结束经度
	 * @param sLat 经纬坐标系的起始纬度
	 * @param eLat 经纬坐标系的结束纬度
	 * @return
	 */
	public static Point2D.Double  mergeLonLatToXY(Point2D.Double lonlat, float[][] x, float[][] y, float sLon, float eLon, float sLat, float eLat){
		if(!isBetweenTwoVals(sLon, -180, 180) || !isBetweenTwoVals(eLon, -180, 180)){
			System.out.println("In IsolineUtil.mergeLonLatToXY()中超出了经度的范围");
			return null;
		}
		if(!isBetweenTwoVals(sLat, -90, 90) || !isBetweenTwoVals(eLat, -90, 90)){
			System.out.println("In IsolineUtil.mergeLonLatToXY()中超出了纬度的范围");
			return null;
		}
		if(x.length!=y.length || x[0].length!=y[0].length){
			System.out.println("In isolineUtil.mergeLonLatToXY()x y长度不同");
			return null;
		}
		int row = x.length;
		int col = x[0].length;
		
		float sX = 0;
		float eX = 0;
		float sY = 0;
		float eY = 0;
		
		sX = x[0][0];
		eX = x[0][col-1];
		sY = y[0][0];
		eY = y[row-1][0];
		
		boolean isXDesc = false;
		boolean isYDesc = false;
		
		if(sX<eX){
			isXDesc = false;
		}else if(sX>eX){
			isXDesc = true;
		}else{
			//起止坐标相等
			System.out.println("In IsolineUtil.mergeLonLatToXY()中只有一列数据,无法归并");
			return null;
		}
		
		if(sY<eY){
			isYDesc = false;
		}else if(sY>eY){
			isYDesc = true;
		}else{
			//起止坐标相等
			System.out.println("In IsolineUtil.mergeLonLatToXY()中只有一列数据,无法归并");
			return null;
		}
		
		if(!isXDesc){
			for(int i=0; i<row; i++){
				if(sX>x[i][0]){
					sX = x[i][0];
				}
				
				if(eX<x[i][col-1]){
					eX = x[i][col-1];
				}
			}
		}else{
			for(int i=0; i<row; i++){
				if(sX<x[i][0]){
					sX = x[i][0];
				}
				
				if(eX>x[i][col-1]){
					eX = x[i][col-1];
				}
			}
		}

		if(!isYDesc){
			for(int i=0; i<col; i++){
				if(sY>y[0][i]){
					sY = y[0][i];
				}
				
				if(eY<y[row-1][i]){
					eY = y[row-1][i];
				}
			}
		}else{
			for(int i=0; i<col; i++){
				if(sY<y[0][i]){
					sY = y[0][i];
				}
				
				if(eY>y[row-1][i]){
					eY = y[row-1][i];
				}
			}
		}
		
		return mergeLonLatToXY(lonlat, sX, eX, sY, eY, sLon, eLon, sLat, eLat);
		
	}
	
	/**
	 * 经纬度坐标转换为xy坐标系
	 * @param lonlat 经纬度坐标
	 * @param sX x起始
	 * @param eX x结束
	 * @param sY y起始
	 * @param eY y结束
	 * @param sLon 经度起始
	 * @param eLon 经度结束
	 * @param sLat 纬度起始
	 * @param eLat 纬度结束
	 * @return xy坐标
	 */
	public static Point2D.Double mergeLonLatToXY(Point2D.Double lonlat, float sX, float eX, float sY, float eY, 
			float sLon, float eLon, float sLat, float eLat){
		double lon = lonlat.x;
		double lat = lonlat.y;
		
		return mergeLonLatToXY( lon,  lat,  sX,  eX,  sY,  eY,
				 sLon,  eLon,  sLat,  eLat);
	}
	
	/**
	 * 经纬度坐标转换为xy坐标系
	 * @param lon 经度坐标
	 * @param lat 纬度坐标
	 * @param sX x起始
	 * @param eX x结束
	 * @param sY y起始
	 * @param eY y结束
	 * @param sLon 经度起始
	 * @param eLon 经度结束
	 * @param sLat 纬度起始
	 * @param eLat 纬度结束
	 * @return xy坐标
	 */
	public static Point2D.Double mergeLonLatToXY(double lon, double lat, float sX, float eX, float sY, float eY,
			float sLon, float eLon, float sLat, float eLat){		
		if(sLon==eLon || sLat==eLat){
			return null;
		}
		
		Point2D.Double resPt = new Point2D.Double();
		resPt.x = sX + (lon-sLon)/(eLon-sLon)*(eX-sX);
		resPt.y = sY + (lat-sLat)/(eLat-sLat)*(eY-sY);
		
		return resPt;
	}
	
	/**
	 * 某一值是否在两个值之间
	 * @param val 
	 * @param sVal
	 * @param eVal
	 * @return
	 */
	public static boolean isBetweenTwoVals(float val, float sVal, float eVal){
		float minVal = Math.min(sVal, eVal);
		float maxVal = Math.max(sVal, eVal);
		if(val>=minVal && val<=maxVal){
			return true;
		}
		return false;
	}
}

/**
 * 处理用多边形类
 */
class HYIsolinePolygonProc {
	/** 多边形的编号  */
	public int index;
	
	/** 所在等值线的编号 */
	public List<Integer>  onIsolineIndexList;
	/** 多边形上等值线的值  */
	public List<Float> valList;
	/** 多边形 */
	public Polygon polygon = null;
	
	public List<PositionVec> positionList = null;
	
	/** 填充多边形时的起始等值线值，包括原始和实际的等值线值*/
	public float sLevel;
	public float eLevel;
//		public float srcSLevel;
//		public float srcELevel;
	
	/** 多边形内的最大值 */
	public float maxVal;
	/** 多边形内的最小值 */
	public float minVal;
	
	/** 多边形最大纬度 */
	public double maxLat;
	/** 多边形最小纬度 */
	public double minLat;
	/** 多边形最大经度 */
	public double maxLon;
	/** 多边形最小经度 */
	public double minLon;	
	
	/** 多边形所包含的区域是凸还是凹,isDown=true:凹; isDown=false:凸 */
	public boolean isDown;
	
	/** 此多边形是否已加入到填充的多边形里面，即是否已经填充，避免重复填充 */
	public boolean isAddToFillPolygon = false;
	
	/** 处理过程中是否为内边界 */
	public Boolean isInnerProc = null;
	
	/** 最终为内边界否 */
	public Boolean isInnerEnd = null;
	
	/** 所在等值线是否闭合 */
	public boolean isOnLineClosed = false;
	
	/** 包含的多边形的IndexList */
	public List<Integer> containPolyIndexList = null;
	
	/** 是否可单独出现 */
	public boolean isIndependently = true;
	
}

/**
 * 处理用多边形类
 */
class IsolinePolygonProc {
	/** 多边形 */
	public Polygon polygon = null;
	/** 多边形上等值线的值  */
	public float val;
	/** 多边形内的最大值 */
	public float maxVal;
	/** 多边形内的最小值 */
	public float minVal;
	/** 多边形的编号  */
	public int index;
	/** 多边形最大纬度 */
	public double maxLat;
	/** 多边形最小纬度 */
	public double minLat;
	/** 多边形最大经度 */
	public double maxLon;
	/** 多边形最小经度 */
	public double minLon;
	/** 等值线增加的点 */
	public ArrayList<Integer> polygonBoundaryIndexAry = new ArrayList<Integer>(5);
	/** 方向 */
	public int direction;
	/** 所在等值线的编号 */
	public int onIsolineIndex;
	/** 多边形所包含的区域是凸还是凹,isDown=true:凹; isDown=false:凸 */
	public boolean isDown;
	/** 此多边形是否已加入到填充的多边形里面，即是否已经填充，避免重复填充 */
	public boolean isAddToFillPolygon = false;
	/** 相邻的多边形Index，即同一条等值线所在的两个多边形，闭合等值线无相邻 */
	public int neighborhoodIndex;
	
	/** 所在等值线的起始和结束是否在同一个边上0：没有；1：left；2：bottom；3：right；4：top */
	public int isSESameSide = 0;
}

/**
 * 处理用多边形类，包括直接包含的多边形的Index. 继承自IsolinePolygonProc
 */
class IsolinePolygonContain extends IsolinePolygonProc {
	/** 被它所包含的多边形的编号，且此多边形不被它所包含的其他多边形包含(直接包含) */
	public ArrayList<Integer> containOnlyPgnList = new ArrayList<Integer>();
//		/** true-最内侧封闭多边形 false-最外侧封闭多边形 否则为null */
//		public Boolean isInOutInner = null;
	// isInner isOuter
	//   T       T      既是最内侧又是最外侧 单个多边形，不被其他多边形包含，也不包含其他多边形
	//   T       F      最内侧多边形
	//   F       T      最外侧多边形
	//   F       F      中间多边形,既包含其他多边形，又被其他多边形包含
	/** true-最内侧封闭多边形 false-不是最内侧封闭多边形 */
	public boolean isInner = false;
	/** true-最外侧封闭多边形 false-不是最外侧封闭多边形 */
	public boolean isOuter = false;
}

/**
 * 处理用多边形类，包括直接包含的多边形的高低中心的值，高低中心位置，高还是低.
 */
class IsolineOnlyPgn {
	/** 封闭区域比周边高时则为true,否则为false */
	public Boolean isHigh = null;
	/** 闭合区域的高低中心位置 */
	public ArrayList<Position> highlowPosList = null;
	/** 闭合区域的高低中心的值 */
	public float highlowVal = Float.MAX_VALUE;
//		/** true-最内侧封闭多边形 false-最外侧封闭多边形 否则为null */
//		public Boolean isInOutInner = null;
	// isInner isOuter
	//   T       T      既是最内侧又是最外侧 单个多边形，不被其他多边形包含，也不包含其他多边形
	//   T       F      最内侧多边形
	//   F       T      最外侧多边形
	//   F       F      中间多边形,既包含其他多边形，又被其他多边形包含
	/** true-最内侧封闭多边形 false-不是最内侧封闭多边形 */
	public boolean isInner = false;
	/** true-最外侧封闭多边形 false-不是最外侧封闭多边形 */
	public boolean isOuter = false;
}

/**
 * 处理用等值线类，从IsolineData继承
 */
class IsolineDataProc extends IsolineData {
	/** 等值线点位置 保存经纬度 */
	public ArrayList<Point2D.Double> lineList2D = null;
	/** 等值线和边界组成的第一个多边形 */
	public Polygon polygonFirst;
	/** 等值线和边界组成的第二个多边形 */
	public Polygon polygonSecond;
	/** 第一个多边形增加的点的数组 */
	public ArrayList<Integer> pgFirstBoundaryIndexAry = new ArrayList<Integer>(
			5);
	/** 第二个多边形增加的点的数组*/
	public ArrayList<Integer> pgSecondBoundaryIndexAry = new ArrayList<Integer>(
			5);
	/** 第一个多边形添加点的方向：顺时针：1; 逆时针：2; */
	public int firstPolygonDirection;
	/** 第二个多边形添加点的方向:无：0 */
	public int secondPolygonDirection;
	/** 第一个多边形最大纬度 */
	public double polygonFirstMaxLat;
	/** 第一个多边形最小纬度 */
	public double polygonFirstMinLat;
	/** 第一个多边形最大经度 */
	public double polygonFirstMaxLon;
	/** 第一个多边形最小经度 */
	public double polygonFirstMinLon;
	/** 第二个多边形最大纬度 */
	public double polygonSecondMaxLat;
	/** 第二个多边形最小纬度 */
	public double polygonSecondMinLat;
	/** 第二个多边形最大经度 */
	public double polygonSecondMaxLon;
	/** 第二个多边形最小经度 */
	public double polygonSecondMinLon;
	/** 等值线组成的第一个多边形的网格点的最大值 */
	public float firstMax;
	/** 等值线组成的第一个多边形的网格点的最小值 */
	public float firstMin;
	/** 等值线组成的第二个多边形的网格点的最大值 */
	public float secondMax;
	/** 等值线组成的第二个多边形的网格点的最小值 */
	public float secondMin;
	/** 第一个多边形的网格点的最大值的位置：行列 */
	public ArrayList<Point2D.Double> firstMaxPos = null;
	/** 第一个多边形的网格点的最小值的位置：行列 */
	public ArrayList<Point2D.Double> firstMinPos = null;
	/** 第二个多边形的网格点的最大值的位置：行列 */
	public ArrayList<Point2D.Double> secondMaxPos = null;
	/** 第二个多边形的网格点的最小值的位置：行列 */
	public ArrayList<Point2D.Double> secondMinPos = null;		
	/** 等值线的索引 */
	public int indexProc;		
	/** 等值线上的点所在列list */
	public ArrayList<Integer> colsList = null;
	/** 等值线上的点所在行list */
	public ArrayList<Integer> rowsList = null;
	/** 是否在横边上 */
	public ArrayList<Boolean> isHorizonList = null;
	/** 等值线上的等值点 */
	public ArrayList<Isopoint> isopointList = null;
	/** 是否为正向追踪 */
	public boolean isObverse = true;
	/** true-最内侧封闭等值线 false-最外侧封闭等值线 否则为null */
	public Boolean isInOutInner = null;
	public int posneg;//0-没做运算；1-加；2-减 //暂时
	/** 起始点的类型 */
	public int startLineType = 0;
	/** 结束点的类型 */
	public int endLineType = 0;
	/** 是否需要过滤 （针对欧洲细网格 ） */
	public boolean isFilter = false;
	/** 等值线上的点的最大最小x y */
	public float maxX;
	public float minX;
	public float maxY;
	public float minY;
}

//顶点
class Vertex{
	private int col = 0;
	private int row = 0;
	private float val = 0;
//		private float curIsolineVal =0;//正在追踪的等值线值
	private boolean isAsStart = false;//是否可以追踪，若不可以则所在4个边不能作为追踪起点
//		private EdgeIsopointInfo[] fourEdge = new EdgeIsopointInfo[4];
	public int getCol() {
		return col;
	}
	public void setCol(int col) {
		this.col = col;
	}
	public int getRow() {
		return row;
	}
	public void setRow(int row) {
		this.row = row;
	}
	public float getVal() {
		return val;
	}
	public void setVal(float val) {
		this.val = val;
	}
	public boolean isAsStartP() {
		return isAsStart;
	}
	public void setAsStartP(boolean isAsStart) {
		this.isAsStart = isAsStart;
	}

}

/**
 * 等值点信息
 */
class Isopoint {
	private int iColumn = 0;//列
	private int iRow = 0;//行
	private Boolean bIsHorizon = false;//是否为横边
	public int getCol() {
		return iColumn;
	}
	public int getRow() {
		return iRow;
	}
	public Boolean getIsHorizon() {
		return bIsHorizon;
	}
	public void setAll(int row, int col, Boolean bIsHorizon) {
		this.iRow = row;
		this.iColumn = col;
		this.bIsHorizon = bIsHorizon;
	}
	public boolean equals(Isopoint pt){
		if(this.iColumn==pt.getCol() && this.iRow==pt.getRow() && this.bIsHorizon==pt.getIsHorizon()){
			return true;
		}else{
			return false;
		}
	}
//		public Isopoint(int row, int col, Boolean bIsHorizon){
//			setAll(row,col,bIsHorizon);
//		}
//		public Isopoint(){
//			
//		}
}


/**
 * 等值边信息
 */
class EdgeIsopointInfo {
	private double rate;//比例
	private boolean hasIsopoint;//是否存在等值点
	private int level;//0：不能追踪；1：变值追踪；2：优先级最高
	public double getRate() {
		return rate;
	}
	public void setRate(double rate) {
		this.rate = rate;
	}
	public boolean isHasIsopoint() {
		return hasIsopoint;
	}
	public void setHasIsopoint(boolean hasIsopoint) {
		this.hasIsopoint = hasIsopoint;
	}
	public void setLevel(int level){
		this.level = level;
	}
	public int getLevel(){
		return this.level;
	}
	public EdgeIsopointInfo() {

	}
}

/**
 * 计算原始多边形用类，包含最大最小值及位置
 */
class MaxMinValPos {
	public float maxVal;
	public float minVal;
	public ArrayList<Point2D.Double> maxValPosList = null;
	public ArrayList<Point2D.Double> minValPosList = null;
}

