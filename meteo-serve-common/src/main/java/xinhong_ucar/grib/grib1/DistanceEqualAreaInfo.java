package xinhong_ucar.grib.grib1;

import com.xinhong.mids3d.data.DataConfigPath;
import com.xinhong.mids3d.datareader.util.DEInfo;
import com.xinhong.mids3d.datareader.util.DataType;
import com.xinhong.mids3d.datareader.util.GridArea;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * 读取等距网格的经纬度信息
 * @author SJN
 *
 */
public class DistanceEqualAreaInfo {
	
	private static DEInfo dataC = null;
	private static DEInfo dataB = null;
	private static DEInfo dataT = null;
	private static DEInfo dataHMS = null;
	private static final Log logger = LogFactory.getLog(DistanceEqualAreaInfo.class);
	public DistanceEqualAreaInfo(){
		
	}
	
	public static void main(String[] args){
	}
	
	/**
	 * 根据指定区域获取等距网格的经纬度信息
	 * @param area 目前仅限于UCC,UBB,UTT三个区域
	 * @return
	 */
	public static DEInfo getGridData(GridArea area){
		if(area.equals(GridArea.UCC)){
			return getGridDataC();
		}else if(area.equals(GridArea.UBB)){
			return getGridDataB();
		}else if(area.equals(GridArea.UTT)){
			return getGridDataT();
		}else{
			System.out.println("没有找到合适区域");
			return null;
		}
	}
	/**
	 * 根据指定数据类型，指定区域获取等距网格的经纬度信息
	 * @param area 目前仅限于HMM5
	 * @return
	 */
	public static DEInfo getGridData(DataType dataType,GridArea area){
		if(dataType.equals(DataType.HMM5) && (area.equals(GridArea.UCC) || area.equals(GridArea.CCN))){
			return getGridDataHMS(DataType.HMM5);
		}else{
			System.out.println("没有找到合适区域");
			return null;
		}
	}
	
	/**
	 * 根据制定区域获取等距网格的经纬度信息
	 * @param area 目前仅限于C,B,T三个区域
	 * @return
	 */
	public static DEInfo getGridData(String area){
		area = area.toUpperCase();
		if(area.equals("C")){
			return getGridDataC();
		}else if(area.equals("B")){
			return getGridDataB();
		}else if(area.equals("T")){
			return getGridDataT();
		}else{
			System.out.println("没有找到合适区域");
			return null;
		}
	}
	
	/**
	 * 获取中国区域的等距的经纬度信息
	 * @return
	 */
	public static DEInfo getGridDataC(){
		if(dataC != null)
			return dataC;
		else{
			dataC = new DEInfo();
			dataC = getDistanceEqualInfo(dataC,GridArea.UCC);
		}
		return dataC;
	}
	/**
	 * 获取中国区域的等距的经纬度信息
	 * @return
	 */
	public static DEInfo getGridDataHMS(DataType dataType){
		if(dataHMS != null)
			return dataHMS;
		else{
			dataHMS = new DEInfo();
			dataHMS = getDistanceEqualInfo(dataType, dataHMS,GridArea.CCN);
		}
		return dataHMS;
	}
	
	/**
	 * 获取北京区域的等距的经纬度信息
	 * @return
	 */
	public static DEInfo getGridDataB(){
		if(dataB != null)
			return dataB;
		else{
			dataB = new DEInfo();
			dataB = getDistanceEqualInfo(dataB,GridArea.UBB);
		}
		return dataB;
	}
	
	/**
	 * 获取台湾区域的等距的经纬度信息
	 * @return
	 */
	public static DEInfo getGridDataT(){
		if(dataT != null)
			return dataT;
		else{
			dataT = new DEInfo();
			dataT = getDistanceEqualInfo(dataT,GridArea.UTT);
		}
		return dataT;
	}
	
	private static DEInfo getDistanceEqualInfo(DEInfo deInfo, GridArea area){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(DataConfigPath.MM5UCC));
			String s;
			while((s = reader.readLine()) != null){
				String[] aa = s.split(":");
				if(aa[0].equals(("ROW"))){
					deInfo.setRowNum(Integer.valueOf(aa[1]));
				}else if(aa[0].equals(("COL"))){
					deInfo.setColNum(Integer.valueOf(aa[1]));
				}else if(aa[0].equals(("isDE"))){
					deInfo.setDE(Boolean.valueOf(aa[1]));
				}else if(aa[0].equals(("xDel"))){
					deInfo.setXdel(Integer.valueOf(aa[1]));
				}else if(aa[0].equals(("yDel"))){
					deInfo.setYdel(Integer.valueOf(aa[1]));
				}else if(aa[0].equals(("LAT"))){
					float[][] latAry = new float[deInfo.getRowNum()][deInfo.getColNum()];
					for(int i=0;i<deInfo.getRowNum();i++){
						String value = reader.readLine();
						String[] valueAry = value.split(",");
						for(int j=0;j<deInfo.getColNum();j++){
							latAry[i][j] = Float.parseFloat(valueAry[j]);
						}
					}
					deInfo.setLatAry(latAry);
					latAry = null;
				}else if(aa[0].equals(("LON"))){
					float[][] lonAry = new float[deInfo.getRowNum()][deInfo.getColNum()];
					for(int i=0;i<deInfo.getRowNum();i++){
						String value = reader.readLine();
						String[] valueAry = value.split(",");
						for(int j=0;j<deInfo.getColNum();j++){
							lonAry[i][j] = Float.parseFloat(valueAry[j]);
						}
					}
					deInfo.setLonAry(lonAry);
					lonAry = null;
				}
			}			
			return deInfo;
		} catch (IOException e) {
			String message = "com.xinhong.mids3d.wavewatch.DistanceEqualAreaInfo中" +
					"getDistanceEqualInfo中打开配置文件出错，请检查MM5配置文件是否正确";
			logger.error(message);
			logger.error(e);
			return null;
		}
	}
	
	/**
	 * 读取HJMM5
	 * @param dataType
	 * @param deInfo
	 * @param area
	 * @return
	 */
	private static DEInfo getDistanceEqualInfo(DataType dataType,DEInfo deInfo, GridArea area){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(DataConfigPath.MM5HJS));
			String s;
			while((s = reader.readLine()) != null){
				String[] aa = s.split(":");
				if(aa[0].equals(("ROW"))){
					deInfo.setRowNum(Integer.valueOf(aa[1]));
				}else if(aa[0].equals(("COL"))){
					deInfo.setColNum(Integer.valueOf(aa[1]));
				}else if(aa[0].equals(("isDE"))){
					deInfo.setDE(Boolean.valueOf(aa[1]));
				}else if(aa[0].equals(("xDel"))){
					deInfo.setXdel(Integer.valueOf(aa[1]));
				}else if(aa[0].equals(("yDel"))){
					deInfo.setYdel(Integer.valueOf(aa[1]));
				}else if(aa[0].equals(("LAT"))){
					float[][] latAry = new float[deInfo.getRowNum()][deInfo.getColNum()];
					for(int i=0;i<deInfo.getRowNum();i++){
						String value = reader.readLine();
						String[] valueAry = value.split(",");
						for(int j=0;j<deInfo.getColNum();j++){
							latAry[i][j] = Float.parseFloat(valueAry[j]);
						}
					}
					deInfo.setLatAry(latAry);
					latAry = null;
				}else if(aa[0].equals(("LON"))){
					float[][] lonAry = new float[deInfo.getRowNum()][deInfo.getColNum()];
					for(int i=0;i<deInfo.getRowNum();i++){
						String value = reader.readLine();
						String[] valueAry = value.split(",");
						for(int j=0;j<deInfo.getColNum();j++){
							lonAry[i][j] = Float.parseFloat(valueAry[j]);
						}
					}
					deInfo.setLonAry(lonAry);
					lonAry = null;
				}
			}			
			return deInfo;
		} catch (IOException e) {
			String message = "com.xinhong.mids3d.wavewatch.DistanceEqualAreaInfo中" +
					"getDistanceEqualInfo中打开配置文件出错，请检查MM5配置文件是否正确";
			logger.error(message);
			logger.error(e);
			return null;
		}
	}
}

