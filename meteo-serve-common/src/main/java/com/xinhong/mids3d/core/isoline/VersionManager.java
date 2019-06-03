package com.xinhong.mids3d.core.isoline;


/**
 * 对MIDS3D的版本进行管理类
 * @author liuxcpc
 *
 *版本号为三位：形如：MAJOR_VERSION.MINOR_VERSION.REVISION_VERSION
 *           MAJOR_VERSION-主版本号(0-99)
 *           MINOR_VERSION-次版本号(0-999)
 *           REVISION_VERSION-修订版本号(0-99999)
 *
 */
public class VersionManager {
	
	private static String suffixDesc = "";
	
	/*
	 * BuildVersionNum后的描述信息,例如：国航单机版本，suffixDesc = "单机版",用于区分
	 */
	public static void setSuffixDesc(String suffixDesc) {
		VersionManager.suffixDesc = suffixDesc;
	}	

	/**
	 * 内部版本号
	 * @return
	 */
	static public String getBuildVersionNum(){
		String strVersionNum = "";
		if (isZCLHZZ())
//			return "v1.2.18(20140612)";
//			return "v1.0(20140617)";
			strVersionNum = "v1.0(1.6.27_20151022)";			
		else if (isHJSZHY2())
			strVersionNum = "V1.0.1(1.3.23_20150211)";	
		else if (isHJDX())
			strVersionNum = "V1.0.1(1.2.22_20140106)";	
		else if (isMinHangDanger())
			strVersionNum = "V1.6.5(20160126)";	
		else if (isEP())
			strVersionNum = "V1.5.27(20150806)";	
		else if (isHJYY())
			strVersionNum = "V1.0.1( 1.2.22_20141219)";	
		else if (isHJAtlasBrowser())
			strVersionNum = "v1.2.22(20150120)";	
		else if (isNTWC())
			strVersionNum = "v1.1.7(v1.7.1_20160125)";	
		else if(isHJNH())
			strVersionNum = "v1.0.6(20160108)";
		else if(isCFD())//曹妃甸
			strVersionNum = "v0.9.1(20151230)";
//		else if(isKJSeaFC())//一体化
//			strVersionNum = "v0.7.25(20151221)";
		else
			strVersionNum = "v1.5.35(20160315)"; //KJ!
		if (suffixDesc != null && !suffixDesc.isEmpty())
			return strVersionNum + suffixDesc;
		else
			return strVersionNum;
	}
	
	/**
	 * 版本编号
	 * @return
	 */
	static public String getVersionNum(){
		return "v1.4.25";
	}
	
	/**
	 * 版本编号+版本文字描述
	 * @return
	 */
	static public String getVersionDesc(){
		return getBuildVersionNum();
	}
	
	/**
	 * 是否是HJ数字海洋二期版本
	 */
	static public boolean isHJSZHY2(){
		return false;
	}
	
	/**
	 * 是否是HJ定型版本
	 * 与数字海洋二期相比：无各业务子系统、无电子海图、关于对话框需要修改
	 * @return
	 */
	static public boolean isHJDX(){
		return false;
	}
	
	/**
	 * 是否是HJ预研版本
	 * 开启主面板中其他预研成果显示(准实时场、历史背景场产品、解释应用产品、典型海洋过程仿真（无！））
	 * 海洋水文及海面气象：Argo浮标、深水温盐、水文断面、区域显示、玫瑰图及矢量图、海面气象
	 * 地面及高空实况
	 * 热带气旋,危险天气(无!)
	 * 城镇预报及数值预报（欧洲、HJ大气预报、海洋风浪流耦合预报、海洋日常主观预报等）
	 * 传真图、云图及雷达图
	 * 海洋常规统计产品
	 * @return
	 */
	static public boolean isHJYY(){
		return false;
	}	
	
	/**
	 * 是否是KJ中期分析版本
	 * @return
	 */
	static public boolean isKJMTFC(){
		return false;
	}
	
	/**
	 * 是否是KJ短时危险天气版本
	 * @return
	 */
	static public boolean isKJAeroDGFC(){
		return false;
	}
	
	/**
	 * 是否为KJ海区预报版本
	 * 为true时：二维投影默认为等经纬线/增加海洋手工分析面板
	 * 2015-1-27 返回true时表明为中短期及海区合并版本
	 * @return
	 */
	static public boolean isKJSeaFC(){
		return true;
	}
	
	/**
	 * 是否是HJ电子图集浏览版本
	 * 为true时：只有二维等经纬线投影、无人机交互对话框按钮
	 *         手工修改config/worldwind.layers.xml文件中内容,修改SystemConfig.xml内容
	 * @return
	 */
	static public boolean isHJAtlasBrowser(){
		return false;
	}
	
	/**
	 * 是否为电子图集订正。
	 * 为true时：需修改System.xml中标题，温跃层平面图可订正，温盐断面图可订正
	 * @return
	 */
	static public boolean isHJAtlasModify(){	
		return false;
	}	
	
	/**
	 * 是否为ZC LHZZ版本 
	 * 为true时:默认开启自动刷新，只有主面板，主面板采用简洁按钮方式显示(MainFramePanel内容及ToolBar内容均为定制)，增加识别区显示
	 * @return
	 */
	static public boolean isZCLHZZ(){
		return false;
	}
	
	/** 
	 * 是否为EP引接版本 
	 * 为true时:保留海区、热带气旋预报面板，增加EP台站显示分析面板
	 * @return
	 */
	static public boolean isEP(){
		return false;
	}	
	
	/**
	 * 是否为国航危险天气系统版本
	 * @return
	 */
	static public boolean isMinHangDanger(){
		return false;
	}
	
	/**
	 * 是否为海啸预警平台
	 * @return
	 */
	static public boolean isNTWC(){
		return false;
	}	
	
	/**
	 * 是否为南海预报保障诊断平台系统
	 * @return
	 */
	static public boolean isHJNH(){
		return false;
	}
	
	/**
	 * 是否为NH2
	 * @return
	 */
	static public boolean isHJNH2(){
		return false;
	}
	/***
	 * 是否为曹妃甸
	 * @return
	 */
	static public boolean isCFD(){
		return false;
	}
	
	/**
	 * 是否为地方气象分析应用系统
	 * @return
	 */
	static public boolean isQXYY(){
		return false;
	}
}
