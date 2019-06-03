package com.xinhong.mids3d.datareader.util;

import com.xinhong.mids3d.core.isoline.VersionManager;
//import org.apache.commons.lang.StringUtils;
//import com.xinhong.mids3d.util.GetMidsDataInfoFromCfg;


/**
 * 气象及水文要素枚举，修改时不要忘记更新WebService服务端程序使用该枚举的部分！
 * 构造函数中，第一个参数：用于序列化及反序列化，与枚举一致，需要唯一。
 *          第二个参数：用于在文件中读取数据拼接文件名等类中使（外部调用者仍需要使用枚举，不可直接使用该字符串！），各个枚举该参数可相同。
 *          第三个参数：气象及水文要素的中文含义。           
 * @author  SJN
 *
 */
public enum ElemCode {
	/**
	 * MICAPS要素
	 */
	MICAPS_p0("MICAPS_p0", "MICAPS_p0", "等压线"),
	MICAPS_p0_p("MICAPS_p0_p", "MICAPS_p0_p", "等压线标注"),
	MICAPS_p3("MICAPS_p3", "MICAPS_p3", "3小时变压场"),
	MICAPS_p3ave("MICAPS_p3ave", "MICAPS_p3ave", "3小时变压场ave"),
	MICAPS_p3_p("MICAPS_p3_p", "MICAPS_p3_p", "3小时变压填图"),
	MICAPS_p24_p("MICAPS_p24_p", "MICAPS_p24_p", "24小时变压填图"),
	MICAPS_plot("MICAPS_plot", "MICAPS_plot", "地面填图"),
	MICAPS_r1_p("MICAPS_r1_p", "MICAPS_r1_p", "自动站1小时降水量填图"),
	MICAPS_r3_p("MICAPS_r3_p", "MICAPS_r3_p", "自动站3小时降水量填图"),
	MICAPS_r6_p("MICAPS_r6_p", "MICAPS_r6_p", "自动站6小时降水量填图"),
	MICAPS_r12_p("MICAPS_r12_p", "MICAPS_r12_p", "自动站12小时降水量填图"),
	MICAPS_r24_5_p("MICAPS_r24_5_p", "MICAPS_r24_5_p", "5点24小时降水量填图"),
	MICAPS_r24_8_p("MICAPS_r24_8_p", "MICAPS_r24_8_p", "8点24小时降水量填图"),
	MICAPS_special("MICAPS_special", "MICAPS_special", "特殊天气"),
	MICAPS_t0("MICAPS_t0", "MICAPS_t0", "地表气温气温线"),
	MICAPS_t0_p("MICAPS_t0_p", "MICAPS_t0_p", "地表气温填图"),
	MICAPS_t24_p("MICAPS_t24_p", "MICAPS_t24_p", "24小时变温填图"),
	MICAPS_td("MICAPS_td", "MICAPS_td", "露点等值线"),
	MICAPS_td_p("MICAPS_td_p", "MICAPS_td_p", "露点填图"),
	MICAPS_tg_p("MICAPS_tg_p", "MICAPS_tg_p", "地表最低温度"),
	MICAPS_tmax_p("MICAPS_tmax_p", "MICAPS_tmax_p", "地面最高气温填图"),
	MICAPS_tmin_p("MICAPS_tmin_p", "MICAPS_tmin_p", "地面最低气温填图"),
	MICAPS_tt("MICAPS_tt", "MICAPS_tt", "TT"),
	MICAPS_uv("MICAPS_uv", "MICAPS_uv", "流线"),
	MICAPS_vv_p("MICAPS_vv_p", "MICAPS_vv_p", "自动站地面全风速填图"),
	
	MICAPS_temper("MICAPS_temper", "MICAPS_temper", "等温线"),
	MICAPS_temper_p("MICAPS_temper_p", "MICAPS_temper_p", "等温线标注"),
	MICAPS_height("MICAPS_height", "MICAPS_height", "等高线"),
	MICAPS_height_p("MICAPS_height_p", "MICAPS_height_p", "等高线标注"),
	MICAPS_dh_p("MICAPS_dh_p", "MICAPS_dh_p", "24小时变高"),
	MICAPS_dt_p("MICAPS_dt_p", "MICAPS_dt_p", "24小时变温"),
	MICAPS_tlogp("MICAPS_tlogp", "MICAPS_tlogp", "温度压力对数图"),
	MICAPS_t_td("MICAPS_t_td", "MICAPS_t_td", "温度露点差线"),
	MICAPS_t_td_p("MICAPS_t_td_p", "MICAPS_t_td_p", "温度露点差线标注"),
	MICAPS_vv("MICAPS_vv", "MICAPS_vv", "全风速场"),
	
	MICAPS_GH("MICAPS_GH", "MICAPS_GH", "高度分析"),
	
	MICAPS_dp("MICAPS_dp", "MICAPS_dp", "24小时变压"),
	MICAPS_dp_p("MICAPS_dp_p", "MICAPS_dp_p", "24小时变压填图"),
	MICAPS_dt("MICAPS_dt", "MICAPS_dt", "24小时变温"),
	MICAPS_pres_p("MICAPS_pres_p", "MICAPS_pres_p", "气压填图"),
	MICAPS_pressure("MICAPS_pressure", "MICAPS_pressure", "海平面气压"),
	MICAPS_rh("MICAPS_rh", "MICAPS_rh", "相对湿度"),
	MICAPS_rh_p("MICAPS_rh_p", "MICAPS_rh_p", "相对湿度填图"),
	MICAPS_wind("MICAPS_wind", "MICAPS_wind", "风场填图"),
	
	MICAPS_elevation("MICAPS_elevation", "MICAPS_elevation", "海拔高度"),
	
	/**********************************预报要素*****************************************/
	/**  A指数	139 */
    AI("AI", "AI", "A指数"),
    /**  对流降水量	63 */
    CR("CR","CR","对流降水量"),
    /**  散度	42 */
    DI("DI","DI","散度"),
    /**  锋生函数	153 */
    EC("EC","EC","锋生函数"),
    EO("EO","EO","EO"),
    FG("FG","FG","FG"),
    GH("GH","GH","GH"),
    /**  位势高度	7 */
    HH("HH","HH","位势高度"),
    /**  Z螺旋度	154 */
    HZ("HZ","HZ","Z螺旋度"),
    /**  K指数	145 */
    KI("KI","KI","K指数"),
    /**  抬升指数	143 */
    LI("LI","LI","抬升指数"),
    /**  大尺度降水量	62 */
    LS("LS","LS","大尺度降水量"),
    /**  湿位涡	136 */
    MP("MP","MP","湿位涡"),
    /**  海平面气压	2 */
    PR("PR","PR","海平面气压"),
    /**  地面气压	1 */
    PS("PS","PS","地面气压"),
    /**  2米湿度	135 */  
    Q2("Q2","Q2","2米湿度"),
    /**  云水混合比	76 */
    QC("QC","QC","云水混合比"),
    /**  绝热液态水含量	137 */
    QL("QL","QL","绝热液态水含量"),
    /**  雨水混合比	131 */
    QR("QR","QR","雨水混合比"),
    /**  Q矢量散度	152 */
    QV("QV","QV","Q矢量散度"),
    /**  水汽通量散度	150 */
    RA("RA","RA","水汽通量散度"),
    /**  水汽通量	149 */
    RF("RF","RF","水汽通量"),
    /////////////////////////EPHEFC添加 SJN////////////////////
    /**  X方向的水汽通量	 */
    RFX("RFX","RFX","X方向的水汽通量"),
    /**  Y方向的水汽通量	 */
    RFY("RFY","RFY","Y方向的水汽通量"),
    /**  浓度场PART131	 *///concentration field
    EP_CONCTFLD("EP_CONCTFLD","EP_CONCTFLD","浓度场PART131"),
    /**  干沉降PART131	 *///dry deposition 
    EP_DRYDEPS("EP_DRYDEPS","EP_DRYDEPS","干沉降PART131"),
    /**  湿沉降PART131	 *///wet deposition 
    EP_WETDEPS("EP_WETDEPS","EP_WETDEPS","湿沉降PART131"),
    /**  浓度场PART137	 *///concentration field
    EP_CONCTFLD137("EP_CONCTFLD137","EP_CONCTFLD137","浓度场PART137"),
    /**  干沉降PART137	 *///dry deposition 
    EP_DRYDEPS137("EP_DRYDEPS137","EP_DRYDEPS137","干沉降PART137"),
    /**  湿沉降PART137	 *///wet deposition 
    EP_WETDEPS137("EP_WETDEPS137","EP_WETDEPS137","湿沉降PART137"),
    /**  浓度场NAT131	 *///concentration field
    EP_CONCTFLDNAT("EP_CONCTFLDNAT","EP_CONCTFLDNAT","浓度场NAT131"),
    /**  干沉降NAT131	 *///dry deposition 
    EP_DRYDEPSNAT("EP_DRYDEPSNAT","EP_DRYDEPSNAT","干沉降NAT131"),
    /**  湿沉降NAT131	 *///wet deposition 
    EP_WETDEPSNAT("EP_WETDEPSNAT","EP_WETDEPSNAT","湿沉降NAT131"),
    /**  浓度场NAT137	 *///concentration field
    EP_CONCTFLDNAT137("EP_CONCTFLDNAT137","EP_CONCTFLDNAT137","浓度场NAT137"),
    /**  干沉降NAT137	 *///dry deposition 
    EP_DRYDEPSNAT137("EP_DRYDEPSNAT137","EP_DRYDEPSNAT137","干沉降NAT137"),
    /**  湿沉降NAT137	 *///wet deposition 
    EP_WETDEPSNAT137("EP_WETDEPSNAT137","EP_WETDEPSNAT137","湿沉降NAT137"),
    ///////////////////////////////////////////////////////////
    /**  相对湿度	52 */
    RH("RH","RH","相对湿度"),
	RN("RN","RN","降水量"),
	RN01("RN01","RN01","1小时降水量"),
    /**  理查逊数	141 */
    RI("RI","RI","理查逊数"),
    /**  沙氏指数	142 */
    SI("SI","SI","沙氏指数"),
    /**  威胁指数	140 */
    SW("SW","SW","威胁指数"),
    /**  0米温度	134 */
    T0("T0","T0","0米温度"),
    /**  2米温度	134 */
    T2("T2","T2","2米温度"),
    /**  假相当位温	151 */
    TB("TB","TB","假相当位温"),
    /**  温度平流	146 */
    TC("TC","TC","温度平流"),
    /**  总云量	71 */
    TF("TF","TF","总云量"),
    /**  湿静力温度	138 */
    TG("TG","TG","湿静力温度"),
    /**  温度露点差	18 */
    TH("TH","TH","温度露点差"),
    /**  总降水量	61 */
    TR("TR","TR","总降水量"),
    /**  地面温度	148 */
    TS("TS","TS","地面温度"),
    /**  温度	11 */
    TT("TT","TT","温度"),
    /**  10米风 U	132 */
    UT("UT","UT","10米风"),
    /**  10米风V 	133 */
    VT("VT","VT","10米风"),
    /**  风场U分量	33 */
    UU("UU","UU","风场"),
    /**  风场V分量	34 */
    VV("VV","VV","风场"),
    /**  涡度平流	144 */
    VB("VB","VB","涡度平流"),
    /**  涡度	41 */
    VO("VO","VO","涡度"),
    /**  垂直速度	40 */
    WW("WW","WW","垂直速度"),
    
    /** U10  */
    U10("U10","U10","U"),
    /** V10  */
    V10("V10","V10","V"),
    

  //---------------------------------------------预研要素，等待废弃--------------------------------//
    @Deprecated/** 夏季温度-强度  */
    T_STRENGDEPTH("T_STRENGDEPTH","T_TSTRENGDEPTH","T强度"),
    @Deprecated/** 夏季密度-强度 */
    ROU_STRENGDEPTH("ROU_STRENGDEPTH","ROU_STRENGDEPTH","rou强度"),
    @Deprecated/** 夏季声速-强度 */
    SOUND_STRENGDEPTH("SOUND_STRENGDEPTH","SOUND_STRENGDEPTH","sound强度"),
    @Deprecated/** SPEEDS  */
    SPEEDS("SPEEDS","SPEEDS","流速"),
    @Deprecated/** DOWNDEPTH  */
    DOWNDEPTH("DOWNDEPTH","DOWNDEPTH","下界深度"),
    @Deprecated/** THICKDEPTH  */
    THICKDEPTH("THICKDEPTH","THICKDEPTH","厚度"),
    @Deprecated/** UPDEPTH  */
    UPDEPTH("UPDEPTH","UPDEPTH","上界深度"),
    @Deprecated/** ESURF  */
    ESURF("ESURF","ESURF","U"),
    @Deprecated
    SMTS("SMTS", "SMTS","夏季温度场"),
    @Deprecated /** DEP  */
    DEP("DEP","DEP","深度"),
    @Deprecated/** V_FRONT_STRENG  */
    V_FRONT_STRENG("V_FRONT_STRENG","V_FRONT_STRENG","速度锋面强度"),
    @Deprecated/** S_FRONT_STRENG  */
    S_FRONT_STRENG("S_FRONT_STRENG","S_FRONT_STRENG","声速锋面强度"),
    @Deprecated/** T_FRONT_STRENG  */
    T_FRONT_STRENG("T_FRONT_STRENG","T_FRONT_STRENG","温度锋面强度"),
    @Deprecated /** POS  */
    POS("POS","POS","位置"),
    @Deprecated/** SOUNDS  */
    SOUNDS("SOUNDS","SOUNDS","声速"),
  //---------------------------------------------预研要素，等待废弃--------------------------------//
    
    
    //
    /** 海平面气压 实况 临时 */
	SLP("SLP","SLP","海平面气压"),	
	/** 风向 */
	WD("WD","WD","风向"),
	/** 风速 */
	WS("WS","WS","风速"),
	/** 气温 实况 临时 */
	AT("AT","AT","气温"),
	/** 云状 临时 */
    C("C","C","云状"),
	/** 水汽压 临时 */
    WLP("WLP","WLP","水汽压"),
	/** 降水量 临时 */
    RAIN("RAIN","RAIN","降水量"),
	/** 1小时降水量 */
    RAIN01("RAIN01","RAIN01","1小时降水量"),
	/** 2小时降水量 */
    RAIN02("RAIN02","RAIN02","2小时降水量"),
	/** 3小时降水量 */
    RAIN03("RAIN03","RAIN03","3小时降水量"),
	/** 6小时降水量 */
    RAIN06("RAIN06","RAIN06","6小时降水量"),
	/** 9小时降水量 */
    RAIN09("RAIN09","RAIN09","9小时降水量"),
	/** 12小时降水量  */
    RAIN12("RAIN12","RAIN12","12小时降水量"),
	/** 15小时降水量  */
    RAIN15("RAIN15","RAIN15","15小时降水量"),
    /** 18小时降水量  */
    RAIN18("RAIN18","RAIN18","18小时降水量"),
	/** 24小时降水量  */
    RAIN24("RAIN24","RAIN24","24小时降水量"),
    /** 24小时降水量输出 */
    RAINOUT24("RAINOUT24","RAIN24","24小时降水输出"),
    /** 36小时降水量  */
    RAIN36("RAIN36","RAIN36","36小时降水量"),
    /** 48小时降水量  */
    RAIN48("RAIN48","RAIN48","48小时降水量"),
    
    //2013-03-27增加预报产品间隔降水
    /** 预报3小时间隔降水 */
    TR03("TR03","TR03","3小时降水量"),
    /** 预报6小时间隔降水 */
    TR06("TR06","TR06","6小时降水量"),
    /** 预报12小时间隔降水 */
    TR12("TR12","TR12","12小时降水量"),
    /** 预报24小时间隔降水 */
    TR24("TR24","TR24","24小时降水量"),
    /** 预报24小时间隔降水输出 */
    TROUT24("TROUT24","TR24","24小时降水输出"),
    
    /** 3小时变压*/
	DP03("DP03","DP03","3小时变压"),
	/** 24小时变压*/
	DP24("DP24","DP24","24小时变压"),
	/** 3小时变高*/
	DH03("DH03","DH03","3小时变高"),
	/** 24小时变高*/
	DH24("DH24","DH24","24小时变高"),
	/** 24小时变温*/
	DT24("DT24","DT24","24小时变温"),
    /** 海表水温  */
	ST("ST","ST","海表水温"),
	/**能见度*/
	VIS("VIS","VIS","能见度"),

	/** 3小时变温*/
	DT03("DT03","DT03","3小时变温"),
	/** 3小时变假相当位温*/
	DTB03("DTB03","DTB03","3小时变假相当位温"),
	/** 3小时露点温度变化*/
	DTD03("DTD03","DTD03","3小时变露点温度"),
	
	/** 位势稳定度 */
	CONVEI("CONVEI","CI","位势稳定度"),
	/** 垂直风切变 */
	VWINDSHEARWS("VWINDSHEARWS", "VR", "垂直风切变"),
	/** 露点温度 */
	TD("TD","TD", "露点温度"),
	 /**  地面假相当位温	 */
    TE("TE","TE","假相当位温(地面)"),
	/** 纬度 */
	LAT("LAT", "LAT", "纬度"),
	/** 经度  */
	LON("LON", "LON", "经度"),
	/** 本站气压 */
	LP("LP", "LP", "本站气压"),
	/** 云区 */
	YQ("YQ", "YQ", "云区"),
	/** 雾区 */
	WQ("WQ", "WQ", "雾区"),
	/** 不稳定 */
	BW("BW", "BW", "不稳定"),
	/** 积冰 */
	JB("JB", "JB", "积冰"),
	/** 颠簸 */
	DB("DB", "DB", "颠簸"),
	/** 雷暴 */
	LB("LB", "LB", "雷暴"),
	/** 急流 */
	JL("JL", "JL", "急流"),
	/***************************************************************************/
	
	/*******************stone 20131107 海洋站潮汐历史*****************************************/
	/** 00时潮位        */  
	HY_TIDEH00("HY_TIDEH00" ,"TIDEH00","00时潮位"),
	/** 01时潮位        */  
	HY_TIDEH01("HY_TIDEH01" ,"TIDEH01","01时潮位"),
	/** 02时潮位        */  
	HY_TIDEH02("HY_TIDEH02" ,"TIDEH02","02时潮位"),
	/** 03时潮位        */  
	HY_TIDEH03("HY_TIDEH03" ,"TIDEH03","03时潮位"),
	/** 04时潮位        */  
	HY_TIDEH04("HY_TIDEH04" ,"TIDEH04","04时潮位"),
	/** 05时潮位        */  
	HY_TIDEH05("HY_TIDEH05" ,"TIDEH05","05时潮位"),
	/** 06时潮位        */  
	HY_TIDEH06("HY_TIDEH06" ,"TIDEH06","06时潮位"),
	/** 07时潮位        */  
	HY_TIDEH07("HY_TIDEH07" ,"TIDEH07","07时潮位"),
	/** 08时潮位        */  
	HY_TIDEH08("HY_TIDEH08" ,"TIDEH08","08时潮位"),
	/** 09时潮位        */  
	HY_TIDEH09("HY_TIDEH09" ,"TIDEH09","09时潮位"),
	/** 10时潮位        */  
	HY_TIDEH10("HY_TIDEH10" ,"TIDEH10","10时潮位"),
	/** 11时潮位        */  
	HY_TIDEH11("HY_TIDEH11" ,"TIDEH11","11时潮位"),
	/** 12时潮位        */  
	HY_TIDEH12("HY_TIDEH12" ,"TIDEH12","12时潮位"),
	/** 13时潮位        */  
	HY_TIDEH13("HY_TIDEH13" ,"TIDEH13","13时潮位"),
	/** 14时潮位        */  
	HY_TIDEH14("HY_TIDEH14" ,"TIDEH14","14时潮位"),
	/** 15时潮位        */  
	HY_TIDEH15("HY_TIDEH15" ,"TIDEH15","15时潮位"),
	/** 16时潮位        */  
	HY_TIDEH16("HY_TIDEH16" ,"TIDEH16","16时潮位"),
	/** 17时潮位        */  
	HY_TIDEH17("HY_TIDEH17" ,"TIDEH17","17时潮位"),
	/** 18时潮位        */  
	HY_TIDEH18("HY_TIDEH18" ,"TIDEH18","18时潮位"),
	/** 19时潮位        */  
	HY_TIDEH19("HY_TIDEH19" ,"TIDEH19","19时潮位"),
	/** 20时潮位        */  
	HY_TIDEH20("HY_TIDEH20" ,"TIDEH20","20时潮位"),
	/** 21时潮位        */  
	HY_TIDEH21("HY_TIDEH21" ,"TIDEH21","21时潮位"),
	/** 22时潮位        */  
	HY_TIDEH22("HY_TIDEH22" ,"TIDEH22","22时潮位"),
	/** 23时潮位        */  
	HY_TIDEH23("HY_TIDEH23" ,"TIDEH23","23时潮位"),
	/** 第一高低潮时刻      */  
	HY_TIDETIME1("HY_TIDETIME1" ,"TIDETIME1","第一高低潮时刻"),
	/** 第一高低潮潮高  */  
	HY_TIDEHHL1("HY_TIDEHHL1" ,"TIDEHHL1","第一高低潮潮高"),
	/** 第二高低潮时刻      */  
	HY_TIDETIME2("HY_TIDETIME2" ,"TIDETIME2","第二高低潮时刻"),
	/** 第二高低潮潮高  */  
	HY_TIDEHHL2("HY_TIDEHHL2" ,"TIDEHHL2","第二高低潮潮高"),
	/** 第三高低潮时刻      */  
	HY_TIDETIME3("HY_TIDETIME3" ,"TIDETIME3","第三高低潮时刻"),
	/** 第三高低潮潮高  */  
	HY_TIDEHHL3("HY_TIDEHHL3" ,"TIDEHHL3","第三高低潮潮高"),
	/** 第四高低潮时刻      */  
	HY_TIDETIME4("HY_TIDETIME4" ,"TIDETIME4","第四高低潮时刻"),
	/** 第四高低潮潮高      */
	HY_TIDEHHL4("HY_TIDEHHL4" ,"TIDEHHL4","第四高低潮潮高"),
	/************************************************************/
	
	/*************************HJTRAN***********************************/
	/** 测站水深     */
	DEPTH("DEPTH" ,"MAXDEPTH","测站水深"),
	/** 水色     */
	HY_WATER_COLOR("HY_WATER_COLOR" ,"WATERCOLOR","水色"),
	/** 海发光类型     */
	HY_SEALUMTYPE("HY_SEALUMTYPE" ,"SEALUMTYPE","海发光类型"),
	/** 海发光强度      */
	HY_SEALUMINTENS("HY_SEALUMINTENS" ,"SEALUMINTENS","海发光强度"),
	/** 有无星月或降水     */
	STARMOONRAIN("STARMOONRAIN" ,"STARMOONRAIN","有无星月或降水"),
	/************************************************************/	
	
    /************************************************************/	
	// 2013.01.15，Zhoucj增加，用于填图
	/***************************************实时资料要素************************************/
	/** 站号 */
	STATION("STATION", "STATION", "站号"), 
	/** 站点名称 */ 
	STATIONNAME("STATIONNAME", "CHNN", "站名"), 
	/** 气压层次，绘制高空天气填图时用 */
	PRESS("PRESS", "PRESS", "层次"), 
	// 地面
	/** 16方位风向 */
	WDD("WDD", "WDD", "16方位风向"),
	/** 现在天气 */
	WEATHER("WEATHER", "WW", "现在天气"),
	/** 过去天气1 */
	W1("W1", "W1", "过去天气1"),
	/** 过去天气2 */
	W2("W2", "W2", "过去天气2"),
	/** 云量 */
	N("N", "N", "云量"),
	/** 中低云量 */
	NH("NH", "NH", "中低云量"),
	/** 云底高 */
	H("H", "H", "云底高"),
	/** 低云状 */
	CL("CL", "CL", "低云状"),
	/** 中云状 */
	CM("CM", "CM", "中云状"),
	/** 高云状 */
	CH("CH", "CH", "高云状"),
	// 高空
	/** 位势高度 */
	W_GPH("W_GPH", "W_GPH", "位势高度"),
	// 船舶
	/** 风浪周期 */
	WWP("WWP", "WWP", "风浪周期"),
	/** 风浪高度 */
	WWH("WWH", "WWH", "风浪高度"),	
	/** 第一涌浪方向 */
	SWD1("SWD1", "SWD1", "第一涌浪方向"),
	/** 第一涌浪周期 */
	SWP1("SWP1", "SWP1", "第一涌浪周期"),
	/** 第一涌浪高度 */
	SWH1("SWH1", "SWH1", "第一涌浪高度"),
	/** 第二涌浪方向 */
	SWD2("SWD2", "SWD2", "第二涌浪方向"),
	/** 第二涌浪周期 */
	SWP2("SWP2", "SWP2", "第二涌浪周期"),
	/** 第二涌浪高度 */
	SWH2("SWH2", "SWH2", "第二涌浪高度"),
	/** 涌浪方向，绘制船舶填图时，取第一第二涌浪高度较大值 */
	SWD("SWD", "SWD", "涌浪方向"),
	/** 涌浪周期，绘制船舶填图时，取第一第二涌浪高度较大值 */
	SWP("SWP", "SWP", "涌浪周期"),
	/** 涌浪高度，绘制船舶填图时，取第一第二涌浪高度较大值 */
	SWH("SWH", "SWH", "涌浪高度"),
	/** 积冰原因 */
	ICE_R("ICE_R", "ICE_R", "积冰原因"),
	/** 积冰厚度(cm) */
	ICE_H("ICE_H", "ICE_H", "积冰厚度(cm)"),
	/** 积冰率 */
	ICE_F("ICE_F", "ICE_F", "积冰率"),
	// 军队
	/** 第一云层组累积云量 */
	NC1("NC1", "NC1", "第一云层组"),
	/** 第二云层组累积云量 */
	NC2("NC2", "NC2", "第二云层组"),
	/** 第三云层组累积云量 */
	NC3("NC3", "NC3", "第三云层组"),
	/** 第四云层组累积云量 */
	NC4("NC4", "NC4", "第四云层组"),
	/** 第五云层组累积云量 */
	NC5("NC5", "NC5", "第五云层组"),
	/** 第一云层组累积云量 */
	N1("N1", "N1", "第一云层组累积云量"),
	/** 第二云层组累积云量 */
	N2("N2", "N2", "第二云层组累积云量"),
	/** 第三云层组累积云量 */
	N3("N3", "N3", "第三云层组累积云量"),
	/** 第四云层组累积云量 */
	N4("N4", "N4", "第四云层组累积云量"),
	/** 第五云层组累积云量 */
	N5("N5", "N5", "第五云层组累积云量"),
	/** 第一云层组云底高度 */
	H1("H1", "H1", "第一云层组云底高度"),
	/** 第二云层组云底高度 */
	H2("H2", "H2", "第二云层组云底高度"),
	/** 第三云层组云底高度 */
	H3("H3", "H3", "第三云层组云底高度"),
	/** 第四云层组云底高度 */
	H4("H4", "H4", "第四云层组云底高度"),
	/** 第五云层组云底高度 */
	H5("H5", "H5", "第五云层组云底高度"),
	/** 第一云层组云状 */
	C1("C1", "C1", "第一云层组云状"),
	/** 第二云层组云状 */
	C2("C2", "C2", "第二云层组云状"),
	/** 第三云层组云状 */
	C3("C3", "C3", "第三云层组云状"),
	/** 第四云层组云状 */
	C4("C4", "C4", "第四云层组云状"),
	/** 第五云层组云状 */
	C5("C5", "C5", "第五云层组云状"),
	/** 填图降水 */
	W_RAIN06("W_RAIN06", "W_RAIN06", "填图降水"),
	/** 光学视程 */
	VM("VM", "VM", "光学视程"),
	/** 跑道视程 */
	VR("VR", "VR", "跑道视程"),	
	// 民航地面
	/** 现在天气1 */
	WW1("WW1", "WW1", "现在天气1"),
	/** 最小能见度 */
	MINVIS("MINVIS", "MIN_VIS", "最小能见度"),
	
	/** 平台移动方向 */
	PMDIR("PMDIR", "PMDIR", "平台移动方向"),
	/** 平台移动速度 */
	PMSPEED("PMSPEED", "PMSPEED", "平台移动速度"),
	/***************************************************************************/
	
	/**************************************ECMF中GRIB2中的要素*************************************/
	
	/** 2米露点温度 */
	D2("D2", "D2", "2米露点温度"),
	/** 对流有效位能 */
	CAPE("CAPE", "CP", "对流有效位能"),
	/** 预报反照率 */
	FAL("FAL", "FAL", "预报反照率"),
	/** 反照率 */
	AL("AL", "AL", "反照率"),
	/** 低云云量 */
	LCC("LCC", "LCC", "低云云量"),
	/** 平均海平面气压 */
	MPR("MPR", "MPR", "平均海平面气压"),
	/** 雪密度 */
	SN("SN", "SN", "雪密度"),
	/** 雪深 */
	SD("SD", "SD", "雪深"),
	/** 降雪量 */
	SF("SF", "SF", "降雪量"),
	/** 大气柱水汽总量 */
	TCWV("TCWV", "TCWV", "大气柱水汽总量"),
	/** 比湿*/
	SH("SH", "SH", "比湿"),
	/** 云覆盖*/
	CC("CC", "CC", "云覆盖"),
	/** 云冰量*/
	CI("CI", "CI", "云冰量"),
	/** 地面气压对数*/
	INSP("INSP", "INSP", "地面气压对数"),
	/** 12小时天气现象   DS添加**/
	CHNWW12("CHNWW12","CHNWW12","12小时天气现象"),
	
	/** 大气柱水总量 */
	TCW("TCW", "TCW", "大气柱水总量"),
	/** 云液态水含量 */
	CLWC("CLWC", "CLWC", "云液态水含量"),
	/**  过去6小时2米最高温度 */
	MX2T6("MX2T6","MX2T6","过去6小时2米最高温度"),
    /**  过去6小时2米最低温度 */
    MN2T6("MN2T6","MN2T6","过去6小时2米最低温度"),
  
//	/**  100米风 U	132 */
//    U100("U100","U100","100米风"),
//    /**  100米风V 	133 */
//    V100("V100","V100","100米风"),


//    /**  零度层 */
//    DEG0("DEG0","DEG0","零度层"),
	/*******************************************************************************************/
	
	/**************************************HJ解释预报内容*************************************/
	/**HJ解释预报 云区（0-10）*/
    YQQ("YQQ", "YQQ", "云区"),
	/**HJ解释预报 积雨云区（0,1,2,3）*/
    JYY("JYY", "JYY", "积雨区"),
	/**HJ解释预报 雷暴落区（0,1）*/
    LBQ("LBQ", "LBQ", "雷暴落区"),
	/**HJ解释预报 积冰区（0,1,2,3）*/
    FJB("FJB", "FJB", "积冰区"),
	/**HJ解释预报 晴空颠簸（0,1,2,3）*/
    QDB("QDB", "QDB", "晴空颠簸"),
	/**HJ解释预报急流颠簸（0,1）*/
    LDB("LDB", "LDB", "急流颠簸"),
	/**HJ解释预报高空急流（m/s）*/
    GJL("GJL", "GJL", "高空急流"),
	/**HJ解释预报 对流层顶高（gph）*/
    DLH("DLH", "DLH", "对流层顶高"),
	/**HJ解释预报对流层顶温（k）*/
    DLT("DLT", "DLT", "对流层顶温"),
    /***************************************************************************/
    //DYH要求添加
    /**日最高气温*/
    MAT("MAT", "MAT", "日最高气温"),
    /**日最低气温）*/
    MIT("AVGMIT", "AVGMIT", "日最低气温"),
    /**12小时降水概率*/
    TRP("TRP", "TRP", "12小时降水概率"),
    
    // LXC 要求添加
    /**  水汽通量场U分量*/
    UF("UF","UF","水汽通量"),
    /**  水汽通量场V分量 */
    VF("VF","VF","水汽通量"),
    
    //统计要素
	/** 温盐密声*/
	TCSCP("TCSCP", "TCSCP", "温盐密声"),	
	/** 温盐密 跃层*/
	TSC("TSC", "TSC", "温盐密 跃层"),	
	/** 海洋气候*/
	COADS("TSC", "TSC", "温盐密 跃层"),
	/** 各级海况频率*/
	SS("SS", "SS", "各级海况频率"),
	/** 各级浪高频率和平均浪高*/
	WAVEDOWN("WAVEDOWN", "WAVEDOWN", "各级浪高频率和平均浪高"),
	/** 各级风频率和平均风速*/
	WINDDOWN("WINDDOWN", "WINDDOWN", "各级风频率和平均风速"),
	
	
	/** 平均值极值*/
	AVG("AVG", "AVG", "平均值极值"),
	/** 大风日数*/
	DMAXWS("DMAXWS", "MAXWS", "大风日数"),
	/** 最低 能见度出现日数*/
	DMINVIS("DMINVIS", "MINVIS", "最低 能见度出现日数"),
	
	
	/** 大风速层*/
	BWS("BWS", "BWS", "大风速层"),
	
	/** 热带气旋生成频数*/
	OCEAN("OCEAN", "OCEAN", "热带气旋生成频数"),
	/** 热带气旋影响频数 */
	NO("NO", "NO", "热带气旋影响频数"),
	/** 热带气旋生成频数*/
	F("F", "F", "热带气旋生成频数"),
	
	/** 云检测*/
	CMK("CMK", "CMK", "云检测"),

	/** 云分类*/
	CTP("CTP", "CTP", "云分类"),
	/** 云顶高*/
	CTH("CTH", "CTH", "云顶高"),
	/** 大气温度廓线*/
	ATP("ATP", "ATP", "大气温度廓线"),
	/** 大气湿度廓线*/
	AHP("AHP", "AHP", "大气湿度廓线"),

	/** 台风*/
	THP("THP", "THP", "台风"),
	/** 雷暴*/
	STM("STM", "STM", "雷暴"),
	/** 低云大雾*/
	FRG("FRG", "FRG", "低云大雾"),
	/** 云水含量*/
	QCD("QCD", "QCD", "云水含量"),
	
	/******************************************海洋要素**************************/
	//历年统计月平均
    /** 海面高  */
    HY_SHH("HY_SHH","HY_SHH","海面高"),
	
	//海洋水文
	/** 表层流*/
	HY_SCUR("HY_SCUR", "HY_SCUR", "表层流"),
	/** 海面风*/
	HY_WIND("HY_WIND", "HY_WIND", "海面风"),
	/** 海面风向 */
	HY_WIND_DIR("HY_WIND_DIR", "WINDDIR", "海面风向"),
	/** 海面风速*/
	HY_WIND_VEL("HY_WIND_VEL", "WINDVEL", "海面风速"),
	/** 海表水温*/
	HY_SURT("HY_SURT", "ST", "海表水温"),
	
	/** 海表气温*/
	HY_AIRT("HY_AIRT", "AT", "海表气温"),

	/** 密度*/
	HY_DENS("HY_DENS", "DENS", "密度"),
	/** 声速*/
	//2013 03 12 SOUD 修改为SOND 供HJTS读取数据
	HY_SOUD("HY_SOUD", "SOND", "声速"),
	/** 海流*/
	HY_CURR("HY_CURR", "CURR", "海流"),
	/** 海流u*/
	HY_CURR_U("HY_CURR_U", "CURRU", "海流U"),
	/** 海流v*/
	HY_CURR_V("HY_CURR_V", "CURRV", "海流V"),
	/** 水温*/
	HY_TEMP("HY_TEMP", "TEMP", "水温"),
	/** 海水温度08*/
	HY_TEMP08("HY_TEMP08", "TEMP08", "水温08"),
	/** 海水温度14*/
	HY_TEMP14("HY_TEMP14", "TEMP14", "水温14"),
	/** 海水温度20*/
	HY_TEMP20("HY_TEMP20", "TEMP20", "水温20"),
	/** 盐度*/
	HY_SALT("HY_SALT", "SALT", "盐度"),
	/** 盐度08*/
	HY_SALT08("HY_SALT08", "SALT08", "盐度08"),
	/** 盐度14*/
	HY_SALT14("HY_SALT14", "SALT14", "盐度14"),
	/** 盐度20*/
	HY_SALT20("HY_SALT20", "SALT20", "盐度20"),
	/** 海况*/
	HY_STATE("HY_STATE", "SEASTATE", "海况"),
	/** 海浪*/
	HY_WAVE("HY_WAVE", "WAVE", "海浪"),
	/** 浪高*/
	HY_WAVE_VEL("HY_WAVE_VEL", "WAVEVEL", "浪高"),
	/** 浪向*/
	HY_WAVE_DIR("HY_WAVE_DIR", "WAVEDIR", "浪向"),
	/** 透明度*/
	HY_TRAN("HY_TRAN", "TRANSVALUE", "透明度"),
	
	/** 有效波高*/
	HY_HS("HY_HS", "HY_HS", "有效波高"),
	/** 有效波高*/
	HY_EFF_WAVEHEIGHT("HY_EFF_WAVEHEIGHT", "EFFWAVEHEIGHT", "有效波高"),
	/** 有效波周期*/
	HY_EFF_WAVEPERIOD("HY_EFF_WAVEPERIOD", "EFFWAVEPERIOD", "有效波周期"),
	/** 最大波高*/
	HY_MAX_WAVEHEIGHT("HY_MAX_WAVEHEIGHT", "MAXWAVEHEIGHT", "最大波高"),
	/** 最大波周期*/
	HY_MAX_WAVEPERIOD("HY_MAX_WAVEPERIOD", "MAXWAVEPERIOD", "最大波周期"),
	/** 平均波高*/
	HY_AVG_WAVEHEIGHT("HY_AVG_WAVEHEIGHT", "AVGWAVEHEIGHT", "平均波高"),
	/** 平均波周期*/
	HY_AVG_WAVEPERIOD("HY_AVG_WAVEPERIOD", "AVGWAVEPERIOD", "平均波周期"),
	/** 波长*/
	HY_WAVE_LENGTH("HY_WAVE_LENGTH", "WAVELENGTH", "波长"),
	/** 波速*/
	HY_WAVE_SPEED("HY_WAVE_SPEED", "WAVESPEED", "波速"),
	/** 主波向*/
	HY_ZBX("HY_ZBX", "HY_ZBX", "主波向"),
	/** 谱峰周期*/
	HY_TP("HY_TP", "HY_TP", "谱峰周期"),
	/** 跨零周期*/
	HY_TZ("HY_TZ", "HY_TZ", "跨零周期"),
	/** 平均波向*/
	HY_DRR("HY_DRR", "HY_DRR", "平均波向"),
	
	/** 海况 临时 */
    HY_SEA_STATE("HY_SEA_STATE","SEASTATE","海况"),
	/** 涌浪浪向*/
	HY_YSS("HY_YSS", "STREAMDIR", "涌浪浪向"),
	HY_STREAM_DIR("HY_STREAM_DIR", "STREAMDIR", "涌浪浪向"),
	/** 涌浪浪高*/
	HY_YRR("HY_YRR", "STREAMVEL", "涌浪浪高"),
	HY_STREAM_VEL("HY_STREAM_VEL", "STREAMVEL", "涌浪浪高"),
	/** 涌浪周期*/
	HY_YPP("HY_YPP", "HY_YPP", "涌浪周期"),
	/***************************************************************************/
	

	/*************************HJCURR***********************************/
	/** 水平流速(cm/s)      */
	HY_CURR_HORVEL("HY_CURR_HORVEL" ,"HORCURVEL","水平流速(cm/s)"),
	/** 水平流向(度)      */
	HY_CURR_HORDIR("HY_CURR_HORDIR" ,"HORCURDIR","水平流向(度)"),
	/** 垂直流速(cm/s),正数表示向上流，负数表示向下流      */
	HY_CURR_VERVEL("HY_CURR_VERVEL" ,"VERCURVEL","垂直流速(cm/s)"),
	/************************************************************/	
	
	/*************************ZC***********************************/
	/** 水位*/
	HY_WATERLEVEL("HY_WATERLEVEL", "WATERLEVEL", "水位"),
	/** 浪高*/
	HY_WAVE_HEIGHT("HY_WAVEHEIGHT", "WAVEHEIGHT", "浪高"),
	/** 浪周期*/
	HY_WAVE_PERIOD("HY_WAVEPERIOD", "WAVEPERIOD", "浪周期"),
	/************************************************************/	
	
	
	/*********************温跃层要素********************************************/
	/** 温跃层强度*/
	HY_TEMPCLINE_INTENSITY("HY_TEMPCLINE_INTENSITY", "intensity", "温跃层强度"),
	/** 温跃层上界温度*/
	HY_TEMPCLINE_UPVALUE("HY_TEMPCLINE_UPVALUE", "upAT", "温跃层上界温度"),
	/** 温跃层下界温度*/
	HY_TEMPCLINE_DOWNVALUE("HY_TEMPCLINE_DOWNVALUE", "downAT", "温跃层下界温度"),
	/** 温跃上界深度*/
	HY_TEMPCLINE_UPDEPTH("HY_TEMPCLINE_UPDEPTH", "upDepth", "温跃层上界深度"),
	/** 温跃层下界深度*/
	HY_TEMPCLINE_DOWNDEPTH("HY_TEMPCLINE_DOWNDEPTH", "downDepth", "温跃层下界深度"),
	/** 温跃层厚度*/
	HY_TEMPCLINE_THICKNESS("HY_TEMPCLINE_THICKNESS", "thickness", "温跃层厚度"),
	
	/***************************************************************************/
		
	/************************************LJQ使用**********************************/
	/** 位势高度*/
	HGT("HGT", "HGT", "位势高度"),
	/** 2米温度*/
	T2M("T2M", "T2M", "2米温度"),
	/** 相对湿度*/
	RHD("RHD", "RHD", "相对湿度"),
	/** 温度*/
	TMP("TMP", "TMP", "温度"),
	/** 风U分量*/
	UUU("UUU", "UUU", "风"),
	/** 风V分量*/
	VVV("VVV", "VVV", "风"),
	/** 垂直速度*/
	WWW("WWW", "WWW", "垂直速度"),
	/** 涡度*/
	VOR("VOR", "VOR", "涡度"),
	/** 散度*/
	DIV("DIV", "DIV", "散度"),
	/** 累积降水*/
	PRE("PRE", "PRE", "累积降水"),
	/** 海流U分量*/
	CRU("CRU", "CRU", "海流"),
	/** 海流V分量*/
	CRV("CRV", "CRV", "海流"),
	/** 海水温度*/
	STT("STT", "STT", "海水温度"),
	/** 盐度*/
	SAL("SAL", "SAL", "盐度"),
	/** 密度*/
	RHO("RHO", "RHO", "密度"),
	/** 海面高度*/
	ZET("ZET", "ZET", "海面高度"),	
	/** 海表温度*/
	SST("SST", "SST", "海表温度"),
	/** 海面高程*/
	SSH("SSH", "SSH", "海面高程"),
	/***************************************************************************/
	
	/*********************************气候统计要素******************************/
	/** 平均海平面气压*/
	STAT_M_SLP("STAT_M_SLP", "M_SLP", "平均海平面气压"),
	/** 平均本站气压*/
	STAT_M_LP("STAT_M_LP", "M_LP", "平均本站气压"),
	/** 平均风速*/
	STAT_M_WS("STAT_M_WS", "M_WS", "平均风速"),
	/** 平均气温*/
	STAT_M_AT("STAT_M_AT", "M_AT", "平均气温"),
	/** 平均最高气温*/
	STAT_M_MAX_AT("STAT_M_MAX_AT", "M_MAX_AT", "平均最高气温"),
	/** 平均最低气温*/
	STAT_M_MIN_AT("STAT_M_MIN_AT", "M_MIN_AT", "平均最低气温"),
	/** 平均相对湿度*/
	STAT_M_RH("STAT_M_RH", "M_RH", "平均相对湿度"),
	/** 平均总云量*/
	STAT_M_N("STAT_M_N", "M_N", "平均总云量"),
	/** 平均低云量*/
	STAT_M_NH("STAT_M_NH", "M_NH", "平均低云量"),
	/** 平均露点温度*/
	STAT_M_TD("STAT_M_TD", "M_TD", "平均露点温度"),
	/** 极端最高海平面气压*/
	STAT_MAX_SLP("STAT_MAX_SLP", "MAX_SLP", "极端最高海平面气压"),
	/** 极端最低海平面气压*/
	STAT_MIN_SLP("STAT_MIN_SLP", "MIN_SLP", "极端最低海平面气压"),
	/** 极端最大风速*/
	STAT_MAX_WS("STAT_MAX_WS", "MAX_WS", "极端最大风速"),
	/** 极端最高气温*/
	STAT_MAX_AT("STAT_MAX_AT", "MAX_AT", "极端最高气温"),
	/** 极端最高气温出现日*/
	STAT_MAX_AT_DAY("STAT_MAX_AT_DAY", "MAX_AT_DAY", "极端最高气温出现日"),
	/** 极端最低气温*/
	STAT_MIN_AT("STAT_MIN_AT", "MIN_AT", "极端最低气温"),
	/** 极端最
	 *  低气温出现日*/
	STAT_MIN_AT_DAY("STAT_MIN_AT_DAY", "MIN_AT_DAY", "极端最低气温出现日"),
	/** 极端最小相对湿度*/
	STAT_MIN_RH("STAT_MIN_RH", "MIN_RH", "极端最小相对湿度"),
	/** 极端最大风速风向*/
	STAT_MAX_WD("STAT_MAX_WD", "MAX_WD", "极端最大风速风向"),
	/** 平均海平面气压累加和*/
	STAT_SD_SLP("STAT_SD_SLP", "SD_SLP", "平均海平面气压累加和"),
	/** 平均本站气压累加和*/
	STAT_SD_LP("STAT_SD_LP", "SD_LP", "平均本站气压累加和"),
	/** 平均风速累加和*/
	STAT_SD_WS("STAT_SD_WS", "SD_WS", "平均风速累加和"),
	/** 平均气温累加和*/
	STAT_SD_AT("STAT_SD_AT", "SD_AT", "平均气温累加和"),
	/** 平均海平面气压平方*/
	STAT_SD_SLP_QR("STAT_SD_SLP_QR", "SD_SLP_QR", "平均海平面气压平方"),
	/** 平均本站气压平方*/
	STAT_SD_LP_QR("STAT_SD_LP_QR", "SD_LP_QR", "平均本站气压平方"),
	/** 平均风速平方*/
	STAT_SD_WS_QR("STAT_SD_WS_QR", "SD_WS_QR", "平均风速平方"),
	/** 平均气温平方*/
	STAT_SD_AT_QR("STAT_SD_AT_QR", "SD_AT_QR", "平均气温平方"),
	/** 平均海平面气压计数*/
	STAT_C_SD_SLP("STAT_C_SD_SLP", "C_SD_SLP", "平均海平面气压计数"),
	/** 平均本站气压计数*/
	STAT_C_SD_LP("STAT_C_SD_LP", "C_SD_LP", "平均本站气压计数"),
	/** 平均风速计数*/
	STAT_C_SD_WS("STAT_C_SD_WS", "C_SD_WS", "平均风速计数"),
	/** 平均气温计数*/
	STAT_C_SD_AT("STAT_C_SD_AT", "C_SD_AT", "平均气温计数"),
	/** 平均降水量*/
	STAT_M_RAIN("STAT_M_RAIN", "M_RAIN", "平均降水量"),
	/** 平均高度*/
	STAT_M_HGT("STAT_M_HGT", "M_HGT", "平均高度"),
	/** 平均最高高度*/
	STAT_M_MAX_HGT("STAT_M_MAX_HGT", "M_MAX_HGT", "平均最高高度"),
	/** 平均最低高度*/
	STAT_M_MIN_HGT("STAT_M_MIN_HGT", "M_MIN_HGT", "平均最低高度"),
	/** 极端最高高度*/
	STAT_MAX_HGT("STAT_MAX_HGT", "MAX_HGT", "极端最高高度"),
	/** 极端最低高度*/
	STAT_MIN_HGT("STAT_MIN_HGT", "MIN_HGT", "极端最低高度"),
	/** 最大风速风向*/
	STAT_MAX_WSD("STAT_MAX_WSD", "MAX_WSD", "最大风速风向"),
	/** 最小风速*/
	STAT_MIN_WS("STAT_MIN_WS", "MIN_WS", "最小风速"),
	/** 最小风速风向*/
	STAT_MIN_WSD("STAT_MIN_WSD", "MIN_WSD", "最小风速风向"),
	/** 平均高度标准差*/
	STAT_SD_HGT("STAT_SD_HGT", "SD_HGT", "平均高度标准差"),
	/** 平均相对湿度标准差*/
	STAT_SD_RH("STAT_SD_RH", "SD_RH", "平均相对湿度标准差"),
	/** 平均露点温度标准差*/
	STAT_SD_TD("STAT_SD_TD", "SD_TD", "平均露点温度标准差"),
	/** 极端最大风速风向*/
	STAT_MIN_WD("STAT_MIN_WD", "MIN_WD", "极端最大风速风向"),   
	/***************************************************************************/
		
		
	//云图
	/**云图亮温**/
	CLOUD_TT("CLOUD_TT", "CLOUD_TT", "云图亮温"),
	
	//陆地水文
	/** 当前水位 */
	WATERLEVEL("WATERLEVEL", "WATERLEVEL", "当前水位"),
	/** 当前流量 */
	WATERFLOW("WATERFLOW", "WATERFLOW", "当前流量"),
	/** 警戒水位 */
	WATERLEVELALARM("WATERLEVELALARM", "WATERLEVELALARM", "警戒水位"),
	/** 保证水位 */
	WATERLEVELASSURE("WATERLEVELASSURE", "WATERLEVELASSURE", "保证水位"),
	/** 历史最高水位 */
	WATERLEVELHISMAX("WATERLEVELHISMAX", "WATERLEVELHISMAX", "历史最高水位"),
	/** 历史最低水位 */
	WATERLEVELHISMIN("WATERLEVELHISMIN", "WATERLEVELHISMIN", "历史最低水位"),
	/** 水位变幅 */
	WATERLEVELBREADTH("WATERLEVELBREADTH", "WATERLEVELBREADTH", "水位变幅"),
	/** 设计水位 */
	WATERLEVELCONTRIVE("WATERLEVELCONTRIVE", "WATERLEVELCONTRIVE", "设计水位"),
	/** 讯限水位 */
	WATERLEVELFLOODLIMIT("WATERLEVELFLOODLIMIT", "WATERLEVELFLOODLIMIT", "汛限水位"),
	/** 蓄水量 */
	RETAINWATER("RETAINWATER", "RETAINWATER", "蓄水量"),
	/** 河流名称 */
	RIVERNAME("RIVERNAME", "RIVERNAME", "河流名称"),
	/** 水系名称 */
	WATERSYSNAME("WATERSYSNAME", "WATERSYSNAME", "水系名称"),
	/** 流域名称 */
	DRAINAREANAME("DRAINAREANAME", "DRAINAREANAME", "流域名称"),
	/** 日累计降水量 */
	RAINDAY("RAINDAY", "RAINDAY", "日累计降水量"), 
	
	//***********************再分析数据添加要素*******************************************************************//
	/** 中云云量 */
	MCC("MCC", "MCC", "中云云量"),
	/** 高云云量 */
	HCC("HCC", "HCC", "高云云量"),
	/** 雪蒸发量 */
	ES("ES", "ES", "雪蒸发量"),
	/** 雪融量 */
	SMLT("SMLT", "SMLT", "雪融量"),
	/** 地表显热通量 */
	SSHF("SSHF", "SSHF", "地表显热通量"),
	/** 地表潜热通量 */
	SLHF("SLHF", "SLHF", "地表潜热通量"),
	/** 边界层高度 */
	BLH("BLH", "BLH", "边界层高度"),
	/** 土壤温度二级 */
	STL2("STL2", "STL2", "土壤温度二级"),
	/** 地表净太阳辐射 */
	SSR("SSR", "SSR", "地表净太阳辐射"),
	/** 地表热辐射 */
	STR("STR", "STR", "地表热辐射"),
	/** 总太阳辐射量 */
	TSR("TSR", "TSR", "总太阳辐射量"),
	/** 总热辐射量 */
	TTR("TTR", "TTR", "总热辐射量"),
	
	/** 东西向地面压强 */
	EWSS("EWSS", "EWSS", "东西向地面压强"),
	/** 南北向地面压强 */
	NSSS("NSSS", "NSSS", "南北向地面压强"),
	/** 蒸发量 */
	E("E", "E", "蒸发量"),
	/** 土壤温度三级 */
	STL3("STL3", "STL3", "土壤温度三级"),
	/** 土壤温度四级 */
	STL4("STL4", "STL4", "土壤温度四级"),
	
	//***********************************************************************************************************//
	
	//***********************MH-GRIB2添加要素*******************************************************************//
	/** 积雨云影响的水平范围 */
	CB_HORIZONTAL("CB_HORIZONTAL", "CB_HORIZONTAL", "积雨云影响"), //YBXC01
	/** 积雨云底层高度 */
	CB_BASE("CB_BASE", "CB_BASE", "积雨云底层高度"),	//YHXC02
	/** 积雨云上层高度*/
	CB_TOP("CB_TOP", "CB_TOP", "积雨云上层高度"),	//YHXC03
	/** 积冰均值 */
	ICE_MEAN("ICE_MEAN", "ICE_MEAN", "积冰均值"), 	//YIXC(80/70/60/50/40/30)
	/** 积冰最大值 */
	ICE_MAX("ICE_MAX", "ICE_MAX", "积冰最大值"),		//YIXC(81/71/61/51/41/31)
	/** 湍流均值  */
	TURB_MEAN("TURB_MEAN", "TURB_MEAN", "湍流均值 "),//YFXC(70/60/50/40/30)
	/** 湍流最大值 */
	TURB_MAX("TURB_MAX", "TURB_MAX", "湍流最大值"),	//YFXC(71/61/51/41/31)
	/** 晴空湍流均值  */
	CAT_MEAN("CAT_MEAN", "CAT_MEAN", "晴空湍流均值 "),//YLXC(40/35/30/25/20/15)
	/** 晴空湍流最大值 */
	CAT_MAX("CAT_MAX", "CAT_MAX", "晴空湍流最大值"),	//YLXC(41/36/31/26/21/16)
	
	//***********************************************************************************************************//
	/**
	 * 海啸预警中心
	 */
	NTWC_TRANSTIME("NTWC_TRANSTIME", "NTWC_TRANSTIME", "传播时间"),
	NTWC_TSHEIGHT("NTWC_TSHEIGHT", "NTWC_TSHEIGHT", "波幅高"),
	NTWC_TSINSTANTHEIGHT("NTWC_TSINSTANTHEIGHT", "NTWC_TSINSTANTHEIGHT", "瞬时波幅高"),
	;
	
    private final String value;
    private final String fileValue;
    private final String CHNName;
    private String format;  
    private String unit;
    private Float offset;   
    private Float scale;
    
    public void setScale(float scale){
    	this.scale = scale;
    }
    public void setOffset(float offset){
    	this.offset = offset;
    }
    
    
    ElemCode(String v, String fileValue, String chnname){
    	this.value = v;
    	this.fileValue = fileValue;
    	this.CHNName = chnname;
    }
    
    public String getCHNName()		{ return this.CHNName; }
    /**
     * 此方法为数据读取模块内部专用，不建议外部使用
     * @return
     */
    public String getFileValue()	{ return fileValue; }
    public String value() 			{ return value; }
    
    public static ElemCode fromValue(String v) {
        for (ElemCode c: ElemCode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        System.err.println("无法构建枚举ElemCode，，因无当前输入的信息【"+v+"】的相关枚举类型");
        return null;
    }
    public static ElemCode fromFileValue(String v) {
        for (ElemCode c: ElemCode.values()) {
            if (c.fileValue.equals(v)) {
                return c;
            }
        }
        System.err.println("无法构建枚举ElemCode，，因无当前输入的信息【"+v+"】的相关枚举类型");
        return null;
    }
    
    /**
     * 获取在界面上显示的名称
     * @return
     */
    public String getUIName()
    {
    	if (this.equals(ElemCode.UU) || this.equals(ElemCode.VV))
    	{
    		return "风场";
    	}
//    	else if (this.equals(ElemCode.U) || this.equals(ElemCode.V))
//    	{
//    		return "风场";
//    	}
    	else if (this.equals(ElemCode.UT) || this.equals(ElemCode.VT))
    	{
    		return "10米风";
    	}
    	else if (this.equals(ElemCode.HY_CURR_U) || this.equals(ElemCode.HY_CURR_V))
    	{
    		return "海流";
    	}
    	return this.getCHNName();
    }

    
    /**
     * 获取数据格式
     * @return 数据格式
     */
    public String getFormat(){
    	/*String value = GetMidsDataInfoFromCfg.getValueByAttName(null, this, null, null, "FORMAT");
    	if(value == null)
    		value = GetMidsDataInfoFromCfg.getDefaultValueByAttribute(this, "FORMAT");
    	if(value == null || value.equals("")){
    		System.err.println("要素【" + this.value + "】没有获取到FORMAT");
    		return null;
    	}
    	this.format = value;*/
    	return this.format;
    }     

  /*	*//**
  	 * 获取数据格式
  	 * @param datatype 数据类型
  	 * @param level 层次，没有时传入null
  	 * @param VTI 预报时效 ，没有时传入null
  	 * @return 获取数据格式
  	 *//*
    public String getFormat(DataType datatype, DataLevel level, String VTI){
    	//读取配置中的内容
    	String press = null;
    	if(level != null )
    		press = level.getFileValue();
    	String svti = null;
    	if(VTI != null){
    		svti = StringUtils.leftPad(VTI, 3, '0');
    	}
    	String value = GetMidsDataInfoFromCfg.getValueByAttName(datatype, this, press, svti, "FORMAT");
    	if(value == null){
    		value = GetMidsDataInfoFromCfg.getValueByAttName(datatype, this, press, null, "FORMAT");
    	}
    	if(value == null){
    		value = GetMidsDataInfoFromCfg.getValueByAttName(datatype, this, null, svti, "FORMAT");
    	}
    	if(value == null){
    		value = GetMidsDataInfoFromCfg.getValueByAttName(datatype, this, null, null, "FORMAT");
    	}
    	if(value == null)
    		value = GetMidsDataInfoFromCfg.getDefaultValueByAttribute(this, "FORMAT");
    	if(value == null || value.equals("")){
    		System.err.println("要素【" + this.value + "】没有获取到FORMAT");
    		return null;
    	}
    	this.format = value;
    	return this.format;  	
    }*/

    /**
     * 获取数据单位
     * @return 数据单位
     */
    public String getUnit(){
    	/*String value = GetMidsDataInfoFromCfg.getValueByAttName(null, this, null, null, "UNIT");
    	if(value == null)
    		value = GetMidsDataInfoFromCfg.getDefaultValueByAttribute(this, "UNIT");
    	if(value == null || value.equals("")){
    		System.err.println("要素【" + this.value + "】没有获取到UNIT");
    		return null;
    	}
    	this.unit = value;*/
    	return this.unit;
    }     

  	/**
  	 * 获取数据单位
  	 * @param datatype 数据类型
  	 * @param level 层次，没有时传入null
  	 * @param VTI 预报时效 ，没有时传入null
  	 * @return 数据单位
  	 */
    public String getUnit(DataType datatype, DataLevel level, String VTI){
    	//读取配置中的内容
    	/*String press = null;
    	if(level != null )
    		press = level.getFileValue();
    	String svti = null;
    	if(VTI != null){
    		svti = StringUtils.leftPad(VTI, 3, '0');
    	}
    	String value = GetMidsDataInfoFromCfg.getValueByAttName(datatype, this, press, svti, "UNIT");
    	if(value == null){
    		value = GetMidsDataInfoFromCfg.getValueByAttName(datatype, this, press, null, "UNIT");
    	}
    	if(value == null){
    		value = GetMidsDataInfoFromCfg.getValueByAttName(datatype, this, null, svti, "UNIT");
    	}
    	if(value == null){
    		value = GetMidsDataInfoFromCfg.getValueByAttName(datatype, this, null, null, "UNIT");
    	}
    	if(value == null)
    		value = GetMidsDataInfoFromCfg.getDefaultValueByAttribute(this, "UNIT");
    	if(value == null || value.equals("")){
    		System.err.println("要素【" + this.value + "】没有获取到UNIT");
    		return null;
    	}
    	this.unit = value;*/
    	return this.unit;  	
    }
    
    /**
     * 获取数据放大比例
     * @return 数据放大比例
     */
    public float getOffset(){
    	/*if(this.offset != null)
    		return this.offset;
    	String value = GetMidsDataInfoFromCfg.getValueByAttName(null, this, null, null, "OFFSET");
    	if(value == null)
    		value = GetMidsDataInfoFromCfg.getDefaultValueByAttribute(this, "OFFSET");
    	if(value == null || value.equals("")){
    		System.err.println("要素【" + this.value + "】没有获取到OFFSET,设定默认值为0");
    		this.offset = 0.0f;
    		return this.offset;
    	}
    	this.offset = Float.valueOf(value);*/
		this.offset = 0f;
    	return this.offset;
    }     

  	/**
  	 * 获取数据放大比例 
  	 * @param datatype 数据类型
  	 * @param level 层次，没有时传入null
  	 * @param VTI 预报时效 ，没有时传入null
  	 * @return 数据放大比例
  	 */
    public float getOffset(DataType datatype, DataLevel level, String VTI){
    	/*String press = null;
    	if(level != null )
    		press = level.getFileValue();
    	//读取配置中的内容
    	String svti = null;
    	if(VTI != null){
    		svti = StringUtils.leftPad(VTI, 3, '0');
    	}
    	String value = GetMidsDataInfoFromCfg.getValueByAttName(datatype, this, press, svti, "OFFSET");
    	if(value == null){
    		value = GetMidsDataInfoFromCfg.getValueByAttName(datatype, this, press, null, "OFFSET");
    	}
    	if(value == null){
    		value = GetMidsDataInfoFromCfg.getValueByAttName(datatype, this, null, svti, "OFFSET");
    	}
    	if(value == null){
    		value = GetMidsDataInfoFromCfg.getValueByAttName(datatype, this, null, null, "OFFSET");
    	}
    	if(value == null)
    		value = GetMidsDataInfoFromCfg.getDefaultValueByAttribute(this, "OFFSET");
    	if(value == null || value.equals("")){
    		System.err.println("要素【" + this.value + "】没有获取到OFFSET,设定默认值为0");
    		this.offset = 0.0f;
    		return this.offset;
    	}
    	this.offset = Float.valueOf(value);*/
		this.offset = 0f;
    	return this.offset;  	
    }
    
    /**
     * 获取放大比例
     * @return 放大比例
     */
    public float getScale(){
    	/*if(this.scale != null)
    		return this.scale;
    	String value = GetMidsDataInfoFromCfg.getValueByAttName(null, this, null, null, "SCALE");
    	if(value == null)
    		value = GetMidsDataInfoFromCfg.getDefaultValueByAttribute(this, "SCALE");
    	if(value == null || value.equals("")){
    		System.err.println("要素【" + this.value + "】没有获取到SCALE,设定默认值为1.0");
    		this.scale = 1.0f;
    		return this.scale;
    	}
    	this.scale = Float.valueOf(value);*/
		this.scale = 1f;
    	return this.scale;
    }     

  	/**
  	 * 获取数据偏移量
  	 * @param datatype 数据类型
  	 * @param level 层次，没有时传入null
  	 * @param VTI 预报时效 ，没有时传入null
  	 * @return 数据偏移量
  	 */
    public float getScale(DataType datatype, DataLevel level, String VTI){
    	//读取配置中的内容
    	/*String press = null;
    	if(level != null )
    		press = level.getFileValue();
    	String svti = null;
    	if(VTI != null){
    		svti = StringUtils.leftPad(VTI, 3, '0');
    	}
    	String value = GetMidsDataInfoFromCfg.getValueByAttName(datatype, this, press, svti, "SCALE");
    	if(value == null){
    		value = GetMidsDataInfoFromCfg.getValueByAttName(datatype, this, press, null, "SCALE");
    	}
    	if(value == null){
    		value = GetMidsDataInfoFromCfg.getValueByAttName(datatype, this, null, svti, "SCALE");
    	}
    	if(value == null){
    		value = GetMidsDataInfoFromCfg.getValueByAttName(datatype, this, null, null, "SCALE");
    	}
    	if(value == null)
    		value = GetMidsDataInfoFromCfg.getDefaultValueByAttribute(this, "SCALE");
    	
    	if(value == null || value.equals("")){
    		System.err.println("要素【" + this.value + "】没有获取到SCALE,设定默认值为1.0");
    		this.scale = 1.0f;
    		return this.scale;
    	}
    	this.scale = Float.valueOf(value);*/
		this.scale = 1f;
    	return this.scale;  	
    }
    
    /**
     * 根据给定的值决定某个要素绘制等值线时标注线值的内容,特殊要素如高度气压标注格式特殊处理,标注时尽量不标注小数
     * @param val-给定的值
     * @return 绘制等值线时标注的内容
     */
    public String getIsolineLabelTextFromVal(float val)
    {
    	String resStr = "";
    	if (this.equals(ElemCode.SLP) || this.equals(ElemCode.PR)){ // 海平面气压标注个、十、小数后一位
    		if(VersionManager.isHJNH())
    			resStr = String.format("%4.1f", val);
    		else
    			resStr = String.format("%03d", Math.round(val*10.0) % 1000);    		
    	} else if (this.equals(ElemCode.HH)){             //高度场标注千、百、十位
    		if(VersionManager.isHJNH())
    			resStr = String.format("%4.1f", val);
    		else
        		resStr = String.format("%03d", Math.round(val/10.0));
    	}
		else if (this.equals(ElemCode.HY_TEMPCLINE_INTENSITY)){             //温跃层强度保留2位
    		resStr = String.format("%5.2f", val);
		} 
		else if (this.equals(ElemCode.NTWC_TSINSTANTHEIGHT)){ 
    		resStr = String.format("%5.2f", val);
		} 
		else {
    		String tmpD = "7", tmpF = "7";
    		if (val < 10){
    			tmpD = "2";
    			tmpF = "4";
    		}else if (val < 100){
    			tmpD = "3";
    			tmpF = "5";    			
    		}else if (val < 1000){
    			tmpD = "4";
    			tmpF = "6";      			
    		}
    		else if (val < 10000){
    			tmpD = "5";
    			tmpF = "7";     			
    		}
    		else if (val < 100000){
    			tmpD = "6";
    			tmpF = "8";   			
    		}
    		else{
    			tmpD = "7";
    			tmpF = "9";   			
    		}   			
 			if ((int)(val*10/10) == val){ //保证尽量不绘制小数
				resStr = String.format("%" + tmpD + "d", (int)val);
			} else {
				resStr = String.format("%" + tmpF + ".1f", val);
			}    		
    	}
    	return resStr;

    }


    
    /**
     * 判断该elemCode是否为天气分析预报中的指数(如K指数，A指数，沙氏指数等)
     * @return
     */
    public boolean isIndexElem(){
        if(this.equals(ElemCode.KI) || this.equals(ElemCode.AI) || this.equals(ElemCode.SI) ||
        		this.equals(ElemCode.LI) || this.equals(ElemCode.SW)){
        	return true;
        }
        return false;
    }
    public static void main(String[] args){ 
    	ElemCode elem = ElemCode.STAT_M_HGT;
    	System.out.println(elem.getScale(DataType.ClimateStat, null, null));
    	System.out.println(elem.getUnit(DataType.KT, DataLevel.P0500, null));
    	System.out.println(elem.getUnit(DataType.KT, DataLevel.P0500, "24"));
    	System.out.println(elem.getUnit(DataType.KT, DataLevel.P0700, null));
    }
    
    /**
     * 判断该elemCode是否为UV分量
     * @return
     */
    public boolean isUU(){
    	if( this.equals(ElemCode.UU) || 
    		this.equals(ElemCode.UT) || 
    		this.equals(ElemCode.HY_CURR_U) ||
    		this.equals(ElemCode.U10)){
    		return true;
    	}
    	return false;
    }
    
    public boolean isVV(){
    	if( this.equals(ElemCode.VV) || 
    		this.equals(ElemCode.VT) ||
    		this.equals(ElemCode.HY_CURR_V)||
    		 this.equals(ElemCode.V10)){
    		return true;
    	}
    	return false;
    }
    
    /**
     * 判断要素是否为海洋要素
     * @return
     */
    public boolean isHyElemCode(){
    	if(this.value().length() > 3){
    		if(this.value().substring(0, 3).equals("HY_")
    				|| this.value.startsWith("NTWC"))
    			return true;
    		//以下要素为预研要素，项目完成后可删除
    		if(this.equals(ElemCode.T_STRENGDEPTH)
				|| this.equals(ElemCode.ROU_STRENGDEPTH)
				|| this.equals(ElemCode.SOUND_STRENGDEPTH)
				|| this.equals(ElemCode.SPEEDS)
				|| this.equals(ElemCode.DOWNDEPTH)
				|| this.equals(ElemCode.THICKDEPTH)
				|| this.equals(ElemCode.UPDEPTH)
				|| this.equals(ElemCode.ESURF)
				|| this.equals(ElemCode.SMTS)
				|| this.equals(ElemCode.DEP)
				|| this.equals(ElemCode.V_FRONT_STRENG)
				|| this.equals(ElemCode.S_FRONT_STRENG)
				|| this.equals(ElemCode.T_FRONT_STRENG)
				|| this.equals(ElemCode.POS)
				|| this.equals(ElemCode.SOUNDS))
    			return true;
    	}
    	return false;
    }
    
    /**
     * 判断该elemCode是否为WD\WS
     * @return
     */
    public boolean isWD(){
    	if(this.equals(ElemCode.WD)){
    		return true;
    	}
    	return false;
    }
    
    public boolean isWS(){
    	if(this.equals(ElemCode.WS)){
    		return true;
    	}
    	return false;
    }
    
    public boolean isDanger(){
    	if(this.equals(ElemCode.CB_HORIZONTAL) || this.equals(ElemCode.CB_BASE) || this.equals(ElemCode.CB_TOP) || 
    			this.equals(ElemCode.ICE_MEAN) || this.equals(ElemCode.ICE_MAX) || 
    			this.equals(ElemCode.TURB_MEAN) || this.equals(ElemCode.TURB_MAX) ||
    			this.equals(ElemCode.CAT_MEAN) || this.equals(ElemCode.CAT_MAX)){
    		return true;
    	}
    	return false;
    }
    
    /**
     * 判断是否为降水量要素(LXC)
     * @return
     */
    public boolean isRain(){
    	if(this.value.startsWith("RAIN") || 
    			this.value.startsWith("TR") ||
    			this.equals(ElemCode.CR) ||
    			this.equals(ElemCode.LS)){
    		return true;
    	}
    	return false;
    }

}
