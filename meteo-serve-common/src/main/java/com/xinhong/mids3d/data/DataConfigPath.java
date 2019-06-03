package com.xinhong.mids3d.data;

import com.xinhong.mids3d.core.isoline.VersionManager;

import java.io.IOException;
import java.util.Properties;

/**
 * 数据读取基本配置文件目录
 * @author SJN
 *
 */
public final class DataConfigPath {
	
	public final static String AppRootPath = System.getProperty("user.dir").replace("\\", "/");
//	String dir = System.getProperty("user.dir");
//    return (dir != null) ? dir : ".";
//	public final static String AppRootPath = DataConfigPath.class.getClassLoader().getResource("/").getPath() ;
	/** GRIB MM5 B区域信息 */
	public static String MM5LatLonB ;
	/** GRIB MM5 C区域信息 */
	public static String MM5LatLonC ;
	/** GRIB MM5 T区域信息 */	
	public static String MM5LatLonT ;
	/** GRIB MM5 HJ S区域信息 */	
	public static String MM5LatLonHJS ;
	/** GRIB MM5 B区域信息 */
	public static String MM5UBB ;
	/** GRIB MM5 C区域信息 */
	public static String MM5UCC ;
	/** GRIB MM5 T区域信息 */	
	public static String MM5UTT ;
	/** GRIB MM5 HJ S区域信息 */	
	public static String MM5HJS ;
	
	/** DataTypeConfig 保存生成数据集文件时的创建时间 */	
	public static String DataTypeConfig ;
	public static String DataTypeConfigHJ ;
	/** DataTypeInfoConfig 对各种数据类型的配置信息 */	
	public static String DataTypeInfoConfig ;
	public static String DataTypeInfoConfigHJ ;
	/** DataTypeInfoConfigClitnt 客户端数据类型配置，包括数据集存放目录 等 */	
	public static String DataTypeInfoConfigClient ;
	/** DBConfig 数据库连接配置 */	
	public static String DBConnConfig ;
	/** ElemCode 数据要素基本信息配置  按要素*/	
	public static String GridElemCode_ByElement ;
	/** ElemName EC精细化网格要素配置 */	
	public static String ElemNameEC ;
	/** LevelConfig 层次配置信息 */	
	public static String LevelConfig ;
	/** FileNameConversion 拼接数据名配置文件 */	
	public static String FileNameConversion ;
	/** VersionFileNameConversionPath 由版本决定拼接数据名配置文件 */	
	public static String VersionFileNameConversionPath ;
	/** FreeAreaJoin 拼接格点数据文件的自由区域 */	
	public static String FreeAreaJoin ;
	/** GridConfig 数据要素基本信息配置  按类型 is old*/	
	public static String GridElementConfig_ByDatatype ;
	/** HisTableConfig 历史离散数据的要素配置信息*/	
	public static String HisTableConfig ;
	/** RangeConfig  格点数据的区域配置信息 */	
	public static String RangeConfig ;
	/** DataTypeConfig 拼接格点数据文件的自由区域  is old */	
	public static String RangeJoin_old ;
	/** DataTypeConfig 实况离散数据的要素配置信息*/	
	public static String TableConfig ;
	/** DataTypeConfig 实况离散数据的要素配置信息*/	
	public static String TableConfig_Stat ;
	/** HJTSFileIndex HJTS文件名列表*/	
	public static String HJTSFileIndex ;
	
	public static String WSCONN;
	public static String WSCONNServer;
	public static String HJCPFLLAreaConfig;
	
	public static String CU_DEPTH_G;
	public static String CU_DEPTH_P;
	public static String CU_DEPTH_K;
	
	public static String NearlyStation;
	public static String EPNearlyStation;
	public static String DllClass;
	public static String MHFileheadInfo;
	public static String MessageConfigure;
	public static String ReleaseSeat;
	public static String DueFileInfo;
	public static String MHDBConfig;
	public static String MHMsgDBConfig;
	public static String EPHEFCElemConfig;
	
	public static String GribConvert;
	
	static{
		Properties properties = new Properties();
		try {
			properties.load(DataConfigPath.class.getResourceAsStream("/com/xinhong/mids3d/data/dataConfig.properties"));
			MM5LatLonB = AppRootPath + (String)properties.get("MM5lonlatB");
			MM5LatLonC = AppRootPath + (String)properties.get("MM5lonlatC");
			MM5LatLonT = AppRootPath + (String)properties.get("MM5lonlatT");	
			MM5LatLonHJS = AppRootPath + (String)properties.get("MM5LatLonHJS");
//			MM5UBB = (String)properties.get("MM5UBB");
//			MM5UCC = (String)properties.get("MM5UCC");
//			MM5UTT = (String)properties.get("MM5UTT");
			MM5UBB = AppRootPath + (String)properties.get("MM5UBB");
			MM5UCC = AppRootPath + (String)properties.get("MM5UCC");
			MM5UTT = AppRootPath + (String)properties.get("MM5UTT");
			MM5HJS = AppRootPath + (String)properties.get("MM5HJS");
			
			DataTypeConfig = AppRootPath + (String)properties.get("DataTypeConfig");
			DataTypeConfigHJ = AppRootPath + (String)properties.get("DataTypeConfigHJ");
			DataTypeInfoConfig = AppRootPath + (String)properties.get("DataTypeInfoCfg");
			DataTypeInfoConfigHJ = AppRootPath + (String)properties.get("DataTypeInfoCfgHJ");
			DataTypeInfoConfigClient = AppRootPath + (String)properties.get("DataTypeInfoCfgClient");
			DBConnConfig = AppRootPath + (String)properties.get("DBConnConfig");
			GridElemCode_ByElement = AppRootPath + (String)properties.get("ElemCode_ByElement");
			ElemNameEC = AppRootPath + (String)properties.get("ElemNameEC");
			LevelConfig = AppRootPath + (String)properties.get("LevelConfig");
			FileNameConversion = AppRootPath + (String)properties.get("FileNameConversion");
			if(VersionManager.isEP()){
				VersionFileNameConversionPath = AppRootPath + (String)properties.get("FileNameConversion_EP");
			}else if(VersionManager.isHJNH()){
				VersionFileNameConversionPath = AppRootPath + (String)properties.get("FileNameConversion_HJNH");
			}
			
			FreeAreaJoin = AppRootPath + (String)properties.get("FreeAreaJoin");
			GridElementConfig_ByDatatype = AppRootPath + (String)properties.get("GridElementConfig_ByDatatype");
			HisTableConfig = AppRootPath + (String)properties.get("HisTableConfig");
			RangeConfig = AppRootPath + (String)properties.get("RangeConfig");
			RangeJoin_old = AppRootPath + (String)properties.get("RangeJoin_old");
			TableConfig = AppRootPath + (String)properties.get("TableConfig");
			TableConfig_Stat = AppRootPath + (String)properties.get("TableConfig_Stat");
			WSCONN = AppRootPath + (String)properties.get("WSconn");
			WSCONNServer = AppRootPath + (String)properties.get("WSconnServer");
			HJTSFileIndex = AppRootPath + (String)properties.get("HJTSFileIndex");
			HJCPFLLAreaConfig = AppRootPath + (String)properties.get("HJCPFLLAreaConfig");
			
			CU_DEPTH_G = AppRootPath + (String)properties.get("CU_DEP_G");
			CU_DEPTH_P = AppRootPath + (String)properties.get("CU_DEP_P");
			CU_DEPTH_K = AppRootPath + (String)properties.get("CU_DEP_K");
			EPNearlyStation = AppRootPath + (String)properties.get("EPNearlyStation");
			NearlyStation = AppRootPath + (String)properties.get("NearlyStation");
			DllClass = AppRootPath + (String)properties.get("DLL_CLASS");
			MHFileheadInfo = AppRootPath + (String)properties.get("MHFileheadInfo");
			MessageConfigure = AppRootPath + (String)properties.get("MessageConfigure");
			ReleaseSeat = AppRootPath + (String)properties.get("ReleaseSeat");
			DueFileInfo = AppRootPath + (String)properties.get("DueFileInfo");
			MHDBConfig = AppRootPath + (String)properties.get("MHDBConfig");
			MHMsgDBConfig = AppRootPath + (String)properties.get("MHMsgDBConfig");
			EPHEFCElemConfig = AppRootPath + (String)properties.get("EPHEFCElemConfig");
			GribConvert = AppRootPath + (String)properties.get("GribConvert");
		} catch (IOException e) {
//			e.printStackTrace();
			String message = "com.xinhong.mids3d.data.DataConfigPath中加载配置文件出错，请查看配置文件信息是否正确";
			System.err.print(message);
			e.printStackTrace();
		}
	}
	
//	public static boolean getDllAndEntryName(String sTType,StringBuffer sDllAndEntryName)
//	{
//		if(sTType == null || sTType.isEmpty())
//			return false;
//		Properties properties = new Properties();
//		try {
////			properties.load(DataConfigPath.class.getResourceAsStream(DataConfigPath.DllClass));
//			properties.load(new FileInputStream(new File(DataConfigPath.DllClass)));
//			sDllAndEntryName.setLength(0);
//			sDllAndEntryName.append((String)properties.get(sTType));			
//		} catch (IOException e) {			
//			//没有这个类型
//			e.printStackTrace();
//			return false;
//		}catch (Exception e) {			
//			//没有这个类型
//			e.printStackTrace();
//			return false;
//		}
//	 
//	    return true;
//	}
}
