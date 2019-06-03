package com.xinhong.mids3d.datareader.util;

import java.util.ArrayList;
import java.util.List;

import static com.xinhong.mids3d.core.isoline.UtilEnum.DataGridType;

/**
 *  数据类型 修改时不要忘记更新WebService服务端程序使用该枚举的部分！
 * @author SJN
 *
 */
public enum DataType {
	MICAPS_DMQX("MICAPS_DMQX", "MICAPS地面气象", "MICAPS_DMQX", "MICAPS"),
	/** * MICAPS高空气象数据集 */
	MICAPS_GKQX("MICAPS_GKQX", "MICAPS高空气象", "MICAPS_GKQX", "MICAPS"),
	/** * 气象站点数据集 */
	QXZD("QXZD", "气象站点", "ZD", "ZD","ZD_STATION","ZD_STATION"),
	/** * ZC历史统计站点数据集 */
	ZcHisStatZD("ZcHisStatZD", "ZC历史统计站点", "ZD", "ZD","ZD_STATION","ZD_STATION"),
	/** * 海洋站潮汐站点数据集 */
	HyHisTideZD("HyHisTideZD", "海洋站潮汐站点", "ZD", "ZD","zhs_sttd_surf","ZD_STATION"),
	/** * 海洋站温盐站点数据集 */
	HyHisSttsZD("HyHisSttsZD", "海洋站温盐站点", "ZD", "ZD","zhs_stts_surf","ZD_STATION"),
	/** * 海洋站气象站点数据集 */
	HyHisStmtZD("HyHisStmtZD", "海洋站气象站点", "ZD", "ZD","zhs_stmt_surf","ZD_STATION"),
	/** * 海洋站海浪站点数据集 */
	HyHisStwaZD("HyHisStwaZD", "海洋站海浪站点", "ZD", "ZD","zhs_stwa_surf","ZD_STATION"),
	/** * HJ水色透明度海发光站点 */
	HjTranZD("HjTranZD", "HJ水色透明度海发光站点", "ZD", "ZD","zhs_tran_lays","ZD_STATION"),
	/** * 海洋温跃层连续站资料数据集 */
	HYTEMPCLINEZD("HYTEMPCLINEZD", "海洋温跃层连续站资料数据集","ZD", "ZD", "HYTEMPCLINEZD", "ZD_STATION"),
	/** * 海洋站站点 */
	HYZZD("HYZZD", "海洋站站点", "ZD", "ZD","ZD_OHMSTATION","ZD_OHMSTATION"),
    /** * 军队站点数据集 */
    JDZD("JDZD", "军队站点", "ZD", "ZD","ZD_STATION","ZD_STATION"),
    /** * EP站点数据集 */
	EPZD("EPZD", "EP站点", "ZD", "ZD","ZD_STATION","ZD_STATION"),
    /** * 船舶呼号数据集 */
    CBZD("CBZD", "船舶站点", "ZD", "ZD","ZD_STATION","ZD_STATION"),
    /** * 民航机场信息数据集 */
	MHZD("MHZD", "民航机场信息", "ZD", "ZD","AIRPORT_DATA","AIRPORT_DATA"),
	/** * 席位信息数据集 */
	RSZD("RSZD", "席位信息", "ZD", "ZD","RELEASE_DUTY","RELEASE_DUTY"),
	/** * IOC潮位信息数据集 */
	NTWCIOCZD("NTWCIOCZD", "IOC潮位信息", "ZD", "ZD","ZD_IOCSTATION","ZD_IOCSTATION"),
	/** * TCL潮位信息数据集 */
	NTWCTCLZD("NTWCTCLZD", "TCL潮位信息", "ZD", "ZD","ZD_TCLSTATION","ZD_TCLSTATION"),
	/** * 海啸浮标站点信息数据集 */
	NTWCNDBCZD("NTWCNDBCZD", "海啸浮标站点信息", "ZD", "ZD","ZD_NDBCSTATION","ZD_NDBCSTATION"),
	
	/** *  地面气象天气图数据集     */
    DMQX("DMQX", "地面气象", "DM", "SCATTER"),
    /** *  高空气象天气图数据集  */
    GKQX("GKQX", "高空气象", "GK", "SCATTER"),    
    /** *  危险天气数据集 */
    WXTQ("WXTQ", "危险天气", "WXTQ", "SCATTER"),
    /** *  城镇精细化预报数据集 */
    CZFC("CZFC", "城镇精细化预报", "CZFC", "SCATTER"),
    
    /** *  台风资料数据集 */
    TYPH("TYPH", "台风资料", "TYPH", "TYPH"),
    /** *  海洋水文资料数据集
     */
    HYSW("HYSW", "海洋水文", "HYSW", "SCATTER"),
    /** *  航危资料数据集    */
    WX("WX", "航危", "WX", "SCATTER"),
    /** *  重要天气资料数据集    */
    ZYTQ("ZYTQ", "重要天气资料数据集", "ZYTQ", "SCATTER"),
    /** *  军队地面气象天气图 */
    JDDM("JDDM", "军队地面", "JDDM", "SCATTER"),
    /** *  军队高空资料数据集 */
    JDGK("JDGK", "军队高空", "JDGK", "SCATTER"),
    /** *  北京台自动站   */
    BJAWS("BJAWS", "北京台自动站", "BJAWS", "SCATTER"),
    /** *  全国地面自动站*/
    DMZDZ("DMZDZ", "全国地面自动站", "DMZDZ", "SCATTER"),
    /** *  HJ南海站点*/
    HJNHZDZ("HJNHZDZ", "南海站", "HJNHZDZ", "SCATTER"),
    /** *  民航机场实况数据集    */
    MHDM("MHDM", "民航机场", "MHDM", "SCATTER"),
    /***GFS资料***/
    GFS("GFS", "GFS数值预报", "MHDM", "SCATTER"),
    /** *  精细化预报*/
    CZ("CZ", "精细化预报", "CZ", "SCATTER"),
    
    /** * 欧洲细网格数值预报产品  */
    ECMF("ECMF", "欧洲细网格数值预报", "ECMF", "GRIB"),
    /** * 欧洲数值预报产品  */
    EC("EC", "欧洲数值预报", "EC", "GRIB"),
    /** * 德国数值预报产品*/
    ED("ED", "德国数值预报", "ED", "GRIB"),
    /** * 日本数值预报产品 */
    RJ("RJ", "日本数值预报", "RJ", "GRIB"),
    /** * 美国数值预报产品 */
    USWBC("USWBC", "美国数值预报", "USWBC", "GRIB"),
    /** * 英国数值预报产品 */
    UKWRR("UKWRR", "英国数值预报", "UKWRR", "GRIB"),
    /** * 国家局数值预报产品 */
    BJ("BJ", "国家局数值预报", "BJ", "GRIB"),
    /** * KT511数值预报产品 */
    //DI,HH,PR,PS,Q2,RH,ST,T2,TF,TH,TR,TS,TT,UT,UU,VO,VT,VV,WW
    KT("KT", "KT511数值预报", "KT", "GRIB"),
    /** * KMM5数值预报产品 */
    KM("KM", "KMM5数值预报", "KM", "GRIB"),
    /** * KWRF数值预报产品 */
    KW("KW", "KWRF数值预报", "KW", "GRIB"),
    /** * KJ解释预报数值预报产品 */
    KJ("KJ", "KJ解释预报", "KJ", "GRIB"),

    /** *  KJ云图 */    
    KJSN("KJSN", "KJ云图", "KJSN", "YT"),    
    /** *  国家云图 */
    NCSN("NCSN", "国家云图","NCSN", "YT"),
    /** *  HJ云图 */    
    HJSN("HJSN", "HJ云图", "HJSN", "YT"), 
    /** *  北京传真图 */
    FAXB("FAXB", "北京传真图", "FAXB", "CZ"),
    /** *  日本传真图 */
    FAXJ("FAXJ", "日本传真图", "FAXJ", "CZ"),
    /** *  雷达图像 */
    LDTX("LDTX", "雷达图像", "LDTX", "LD"),
    /** *  MICAPS13 */
    MICAPS13("MICAPS13", "MICAPS13图像", "MICAPS13", "LD"),
    /** * HJ解释预报数值预报产品 */
    HJJSYB("HJJSYB", "HJ解释预报", "HS", "GRIB"),
    /** * HJ解释预报数值预报产品——单站 */
    HJJSYBDZ("HJJSYBDZ", "HJ单站解释预报", "HS", "GRIB"),
    /** * HJ大气精细化数值预报产品 */
    HJJXHDQ("HJJXHDQ", "HJ大气精细化预报", "HJ", "GRIB"),
    /** * HJ海流海浪精细化数值预报产品 */
    HJJXHHL("HJJXHHL", "HJ海流海浪精细化预报", "HJ", "NC"),
    /** * HJ卫星反演应用产品 */
    HJWXFY("HJWXFY", "HJ卫星反演应用产品", "HW", "HDF"),
    /** * HJ保障产品 */
    HJBZCP("HJBZCP", "HJ保障产品", "", "DOC"),
    /** * HJ风浪流产品 */
    //wind,tz HY_TZ(跨零周期),tp HY_TP(谱峰周期),th HY_ZBX(主波向),hs HY_HS(有效波高), ztemp,zsal,zcur,
    HJCPFLL("HJCPFLL", "HJ风浪流产品", "NCEP_", "NC"),
    /** * HT511数值产品 */
    HT511("HT511", "HT511数值产品", "HT", "GRIB"),
    /** * HT213数值产品 */
    HT213("HT213", "HT213数值产品", "HT", "GRIB"),
    
    /** * HMM5数值产品 */
    HMM5("HMM5", "HMM5数值产品", "HM", "GRIB"),
    /** * HJ电子图集产品 */
    HJAtlas("HJAtlas", "HJ电子图集产品", "MAArea_", "NC"),
    /** * HJ ARGO */
    HJARGO("HJARGO", "HJARGO产品", "HJARGO", "NC"),
    /** *  气候统计 */
    ClimateStat("ClimateStat", "气候统计", "ClimateStat", "ClimateStat","ClimateStat"),
    /** *  海洋水文统计 */
    HySteadyStat("HySteadyStat", "海洋水文统计", "HySteadyStat", "HySteadyStat","HySteadyStat"),
    /** *  海洋温盐密声历史资料  */
    HJTS("HJTS", "海洋温盐密声历史资料", "HJTS", "NC"),
    /** *  海洋温盐历史资料  */
    HJTSREAL("HJTSREAL", "海洋温盐密声实时资料", "HJTSREAL", "NC"),
    /** *  海洋船舶报历史资料（来源于海洋气象中的风浪涌，随机统计专用） */    
    HyHisWindWaveYrr("HyHisWindWaveYrr", "海洋船舶报历史资料,随机统计专用", "HyHisWindWaveYrr", "SCATTER"),
    /** * 海洋海浪历史资料数据集 */
	HyHisWave("HyHisWave", "海洋海浪历史资料", "HyHisWave", "SCATTER"),
	/** *  海洋气象资料（船舶报） */    
    HYQXSHIP("HYQXSHIP", "海洋气象船舶报资料", "HYQXSHIP", "SCATTER"),
    /** *  海洋站资料 */    
    HYZ("HYZ", "海洋站资料", "HYZ", "SCATTER"),
    /** *  南海岛永暑礁气象观测 */    
    NHYS("NHYS", "南海岛永暑礁气象观测", "NHYS", "SCATTER"),
    /** *  南海岛南沙站气象观测 */    
    NHNS("NHNS", "南海岛南沙站气象观测", "NHNS", "SCATTER"),
    /** * Graps精细化预报 */
    NH_NSSF("NH_NSSF", "Graps精细化预报", "NH_NSSF", "GRIB"),
    /** * Graps格点预报 */
    NH_GZLM("NH_GZLM", "Graps格点预报", "NH_GZLM", "GRIB"),
    /** * 海洋站潮汐数据集 */
	HyHisTide("HyHisTide", "海洋站潮汐历史资料", "HyHisTide", "SCATTER"),
	/** * 海洋海面气象历史资料数据集-历史收集资料METE */
	HYQX("HYQX", "海洋海面气象历史数据集", "HYQX", "SCATTER"),
	/** * 海洋站温盐数据集 */
	HyHisStts("HyHisStts", "海洋站温盐历史资料", "HyHisStts", "SCATTER"),
	/** * HJ海流数据集 */ //LAYERDEPTH,HORCURVEL,HORCURDIR,VERCURVEL,	Q_LAYERDEPTH,Q_HORCURVEL,Q_HORCURDIR,Q_VERCURVEL
	HJCURR("HJCURR", "HJ海流数据集", "HJCURR", "NC"),
	/** * HJ水色、透明度、海发光数据集 */
	HJTRAN("HJTRAN", "HJ水色、透明度、海发光资料", "HJTRAN", "SCATTER"),
	/** * 海洋温跃层资料数据集 */
	HYTEMPCLINE("HYTEMPCLINE", "海洋温跃层资料", "HYTEMPCLINE", "NC"),
	/** * 海洋温跃层图集数据集 */
	HYTEMPCLINEAtlas("HYTEMPCLINEAtlas", "海洋温跃层图集资料", "HYTEMPCLINEAtlas", "txt"),
	/** * 波导数据集 -- test 临时添加SJN */
	DUCT("DUCT", "波导数据集", "DUCT", "dat"),
	/** * ZCT799预报数据集  SJN */
	ZCT799("ZCT799", "ZCT799预报", "ZCT799", "GRIB"),
	/** * ZCWordMP4数据集  SJN */
	ZCWordMP4("ZCWordMP4", "ZCWordMP4资料", "ZCWordMP4", "WordMP4"),
	/** * ZCCPWYL数据集  SJN */
	ZCCPWYL("ZCCPWYL", "温盐流数值预报", "ZCCPWYL", "DAT"),
	/** * ZCCPHL数据集  SJN */
	ZCCPHL("ZCCPHL", "海浪数值预报", "ZCCPHL", "DAT"),
	/** * ZCCPHL数据集 -GRIB SJN */
	ZCCPHLGRIB("ZCCPHLGRIB", "海浪数值预报GRIB", "ZCCPHLGRIB", "GRIB"),
	/** *  海洋水文统计 -- 型研 勿删 */
    HyStatProc("HyStatProc", "海洋水文统计", "HyStatProc", "HyStatProc","HyStatProc"),
    /** * 河道水情数据集  */
	LDSWHD("LDSWHD", "河道水情数据集", "LDSWHD", "SCATTER"),
	/** * 水库水情数据集  */
	LDSWSK("LDSWSK", "水库水情数据集", "LDSWSK", "SCATTER"),
	/** * 降水水情数据集  */
	LDSWJS("LDSWJS", "降水水情数据集", "LDSWJS", "SCATTER"),
	/** * 美国再分析  */
	ZFXNCEP("ZFXNCEP", "美国再分析", "ZFXNCEP", "NC"),
	/** * 欧洲再分析 */
	ZFXEC("ZFXEC", "欧洲再分析", "ZFXEC", "GRIB"),
	
	/*************************************MH添加数据类型 SJN*************************************************/
	/** * 火山灰  */
	Volcano("Volcano", "火山灰数据集", "Volcano", "SCATTER"),
	/** * 民航WNI系统输出的GRIB文件 目前包括积冰颠簸、积雨云 */
	MHWNI("MHWNI", "WNI输出GRIB2", "MHWNI", "GRIB"),
	/** * 民航航班信息  */
	MHFlyInfo("MHFlyInfo", "民航航班信息", "MHFlyInfo", ""),
	/***************************************************************************************************/
	
	/*************************************EP添加数据类型 SJN*************************************************/
	/** * EPHEFC预报产品数据集 */
	EPHEFC("EPHEFC", "EPHEFC预报产品", "EPHEFC", "DAT"),
	/** * EP单站实况时 */
	EP_STATIONDATA_HOUR("EP_STATIONDATA_HOUR", "EP单站实况时次数据", "EP_STATIONDATA_HOUR", "SCATTER"),
	/** * EP单站实况日*/
	EP_STATIONDATA_DAY("EP_STATIONDATA_DAY", "EP单站实况日数据", "EP_STATIONDATA_DAY", "SCATTER"),
	/** * EP单站预报数据集 */
	EP_STATIONDATA_FC("EP_STATIONDATA_FC", "EP单站预报数据", "EP_STATIONDATA_FC", "SCATTER"),
	/***************************************************************************************************/
	
	/*************************************NTWC添加数据类型 SJN*************************************************/
	/** * NTWC 中国地震台网中心*/
	NTWC_EQ_CEIC("NTWC_EQ_CEIC", "中国地震台网中心", "NTWC_EQ_CEIC", "SCATTER"),
	/** * NTWC 台湾气象局-地震*/
	NTWC_EQ_CWB("NTWC_EQ_CWB", "台湾气象局-地震", "NTWC_EQ_CWB", "SCATTER"),
	/** * NTWC 印度海洋信息服务中心*/
	NTWC_EQ_ESSO("NTWC_EQ_ESSO", "印度海洋信息服务中心", "NTWC_EQ_ESSO", "SCATTER"),
	/** * NTWC 美国地质调查局数据集*/
	NTWC_EQ_USGS("NTWC_EQ_USGS", "美国地质调查局数据集", "NTWC_EQ_USGS", "SCATTER"),
	/** * NTWC 全球验潮站*/
	NTWC_SL_IOC("NTWC_SL_IOC", "全球验潮站数据集", "NTWC_SL_IOC", "SCATTER"),
	/** * NTWC 全球浮标数据中心*/
	NTWC_SL_NDBC("NTWC_SL_NDBC", "全球浮标数据中心", "NTWC_SL_NDBC", "SCATTER"),
	/** * NTWC 处理的全球潮位站数据*/
	NTWC_SL_TCL("NTWC_SL_TCL", "处理的全球潮位站数据", "NTWC_SL_TCL", "SCATTER"),
	/** * NTWC 台湾气象局-海啸*/
	NTWC_TS_CWB("NTWC_TS_CWB", "台湾气象局-海啸", "NTWC_TS_CWB", "SCATTER"),
	/** * NTWC 地震历史事件*/
	NTWC_BG_EQ("NTWC_BG_EQ", "地震历史事件", "NTWC_BG_EQ", "SCATTER"),
	/** * NTWC 海啸历史事件*/
	NTWC_BG_TS("NTWC_BG_TS", "海啸历史事件", "NTWC_BG_TS", "SCATTER"),
	/** * NTWC 地震历史事件_NEIC*/
	NTWC_BG_EQ_NEIC("NTWC_BG_EQ_NEIC", "地震历史事件_NEIC", "NTWC_BG_EQ_NEIC", "SCATTER"),
	/** * NTWC 调和常数 */
	NTWC_BG_TCLCOEFF("NTWC_BG_TCLCOEFF", "调和常数", "NTWC_BG_TCLCOEFF", "SCATTER"),
	/** * NTWC PTWC太平洋海啸预警中心-海啸数据集*/
	NTWC_TS_PTWC("NTWC_TS_PTWC", "太平洋预警中心", "NTWC_TS_PTWC", "SCATTER"),
	/** * NTWC EQIM地震局-地震数据集*/
	NTWC_EQ_EQIM("NTWC_EQ_EQIM", "EQIM地震局", "NTWC_EQ_EQIM", "SCATTER"),
	/** * NTWC CMT-震源机制数据集*/
	NTWC_EQ_CMT("NTWC_EQ_CMT", "CMT-震源机制", "NTWC_EQ_CMT", "SCATTER"),
	/** * NTWC 地震分区（F-E分区）*/
	NTWC_BG_FE("NTWC_BG_FE", "地震分区（F-E分区）", "NTWC_BG_FE", "SCATTER"),
	/** * NTWC 地震带*/
	NTWC_BG_STRAP("NTWC_BG_STRAP", "地震带", "NTWC_BG_STRAP", "SCATTER"),
	/** * NTWC 海啸场景数据*/
	NTWC_BG_TSSCENE("NTWC_BG_TSSCENE", "海啸场景数据", "NTWC_BG_TSSCENE", "SCATTER"),
	/** *  CMT历史震源机制解数据    */
    NTWC_BG_CMT("NTWC_BG_CMT", "CMT历史震源机制解数据 ", "NTWC_BG_CMT", "SCATTER"),
    /** *  地震历史事件USGS数据    */
    NTWC_BG_EQ_USGS("NTWC_BG_EQ_USGS", "地震历史事件USGS数据", "NTWC_BG_EQ_USGS", "SCATTER"),
	/** * NTWC 海啸数值预报或TTT计算结果*/
	NTWC_TS_FCPROD("NTWC_TS_FCPROD", "海啸计算产品", "NTWC_TS_FCPROD", "GRIB"),
	/** *  EMEC_CSEM欧洲地中海    */
	NTWC_EQ_EMSC("NTWC_EQ_EMSC", "EMEC_CSEM欧洲地中海", "NTWC_EQ_EMSC", "SCATTER"),
	/** *  预报中心Antelope地震数据     */
	NTWC_EQ_ANTE("NTWC_EQ_ANTE", "预报中心Antelope地震数据", "NTWC_EQ_ANTE", "SCATTER"),
	/** *  Seiscomp3-地震数据     */
	NTWC_EQ_SEIS3("NTWC_EQ_SEIS3", "Seiscomp3-地震数据", "NTWC_EQ_SEIS3", "SCATTER"),
	/** *  预报中心Antelope地震矩震级信息数据     */
	NTWC_EQ_ANTE_NETMAG("NTWC_EQ_ANTE_NETMAG", "预报中心Antelope地震矩震级信息数据", "NTWC_EQ_ANTE_NETMAG", "SCATTER"),

	
//	/** * NTWC 太平洋海啸预警中心*/
//	NTWC_PTWC("NTWC_PTWC", "太平洋海啸预警中心", "NTWC_PTWC", "SCATTER"),
//	/** * NTWC 矩心矩张量*/
//	NTWC_CMT("NTWC_CMT", "矩心矩张量", "NTWC_CMT", "SCATTER"),
	/***************************************************************************************************/
	
//////////////////----------------------------------可考虑删除的数据类型--------------------------------------//////////////////////////////
	/** *      * @deprecated */
	OHMS("OHMS", "海洋站观测报告", "OHMS", ""),
	/** *      * @deprecated  HJ云图 */    
    HJYT("HJYT", "HJ云图", "HJYT", "YT"),  
    /** *      * @deprecated  高空气候统计（临时）*/
    ClimateStat_CARDS("ClimateStat_CARDS", "高空气候统计（临时）", "ClimateStat_CARDS", "ClimateStat_CARDS"),
    /** *      * @deprecated  HJ海洋预报 */
    HJYB("HJYB", "HJ海洋预报", "CZ", "GRIB"),
    /** *      * @deprecated  海洋水文统计数据集 */
    ZPSW("ZPSW", "海洋水文","SW","TJ"),
    /** *      * @deprecated  海洋气候统计数据集 */
    ZPCOADS("ZPCOADS", "海洋气候","COADS","TJ"),
    /** *      * @deprecated HJ机场地面气候统计数据集 */
    ZPHJJC("ZPHJJC", "HJ机场","HJJC","TJ"),
    /** *      * @deprecated  地面气候统计数据集 */
    ZPGTS("ZPGTS", "地面气候","GTS","TJ"),
    /** *      * @deprecated  高空气候统计数据集 */
    ZPCARDS("ZPCARDS", "高空气候","CARDS","TJ"),
    /** *      * @deprecated  热带气旋统计数据集 */
    ZPTC("ZPTC", "热带气旋","TC","TJ"),
    /** *      * @deprecated  通用类型 */
    General("General", "通用类型","General","G"),
    /** *      * @deprecated ARGO产品 */
    ARGO("ARGO", "ARGO产品", "ARGO", "NC"),
    /** * HJ区域数值产品 
     * @deprecated 此类型由HMM5替代*/
    HJSZCPQY("HJSZCPQY", "HJ区域数值产品", "HM", "GRIB"),
    /** * HJ大气数值产品 
     * @deprecated 此类型由HT511替代*/
    HJSZCPDQ("HJSZCPDQ", "HJ大气数值产品", "HT", "GRIB"),
    /** *      * @deprecated  热带气旋数据集 */
    RDQX("RDQX", "热带气旋", "RDQX", "RDQX"),
    /** *      * @deprecated  单站资料数据集 */
    DZZL("DZZL", "单站信息", "DZZL", "SCATTER"),
    /** *      * @deprecated 全球地面气象台站站点 */
	QQDMZD("QQDMZD", "全球地面气象台站站点", "ZD", "ZD","ZD_STATION"),
	/** *      * @deprecated 全球高空气象台站站点 */
	QQGKZD("QQGKZD", "全球高空气象台站站点", "ZD", "ZD","ZD_STATION"),
	/** *      * @deprecated 用户字典表 */
	USERZD("USERZD", "用户字典表", "ZD", "ZD","ZD_OPERATOR"),
	/** *      * @deprecated 用户权限表 */
	PRIVZD("PRIVZD", "用户权限表", "ZD", "ZD","ZD_OPERATORPRIV"),
	/** *      * @deprecated 传真图字典数据集 */
	FAXZD("FAXZD", "传真图字典", "ZD", "ZD","ZD_FAXFILE"),
//  /** *  ZC云图 */
//  ZCSN("ZCSN", "ZC云图", "ZCSN", "YT"),
//  /** *  民航机场实况数据集 */
//  JCSK("JCSK", "民航资料", "JCSK", "SCATTER"),
//  /** *  飞机高空资料数据集 */
//  FJGK("FJGK", "飞机高空报", "FJGK", "SCATTER"),
//  /** *  降水资料数据集*/
//  JSZL("JSZL", "实况降水", "JSZL", "SCATTER"),
///** *  KJ云图 */
//KJYT("KJYT", "KJ云图", "KJSN", "YT"),
///** *  ZC云图 */
//ZCYT("ZCYT", "ZC云图", "ZCSN", "YT"),
///** *  国家云图 */
//GJYT("GJYT", "国家云图","NCSN", "YT"),
//	/** *   */    
//  HyHis("HyHis", "", "TJ", ""),
//	/** * 海洋海面气象历史资料数据集 */
//	HYQX("HYQX", "海洋海面气象历史数据集", "HYQX", "SCATTER"),
//	/** *  海洋气象资料（船舶报） */    
//  HYQXSHIP("HYQXSHIP", "海洋气象船舶报资料", "HYQXSHIP", "SCATTER"),
//  /** *  海洋气象船舶报实时资料 */    
//  HYQXSHIPREAL("HYQXSHIPREAL", "海洋气象船舶报实时资料", "HYQXSHIPREAL", "NC"),
//  /** *  海洋气象船舶报资料(按时间分类) */    
//  HYQXSHIPDATE("HYQXSHIPDATE", "海洋气象船舶报资料(按时间分类)", "HYQXSHIPDATE", "SCATTER"),
//	/** * 海洋海面气象历史资料数据集 */
//	HyHisMete("HyHisMete", "海洋海面气象历史数据集", "HyHisMete", "SCATTER"),
//    /** *  海洋气象历史资料（船舶报） */    
//    HYQXHIS("HYQXHIS", "海洋气象历史资料", "HYQXHIS", "SCATTER"),
//////////////////------------------------------------------------------------------------//////////////////////////////
    ;
    
    private final String value;
    private String CHNName;
    private String fileType;
    private String typeSort;
    private String tableName;
    private String postfix;
    
    DataType(String v) {
    	this.value = v;
    }
    DataType(String v, String CHNName) {
    	this.value = v;
        this.CHNName = CHNName;        
    }
    DataType(String v, String CHNName,String fileType, String typeSort) {
    	this.value = v;
        this.CHNName = CHNName; 
        this.fileType = fileType;
        this.typeSort = typeSort;
    }
    DataType(String v, String CHNName,String fileType, String typeSort,String tabName) {
    	this.value = v;
        this.CHNName = CHNName; 
        this.fileType = fileType;
        this.typeSort = typeSort;
        this.tableName = tabName;
    }
    /**
     * 
     * @param v
     * @param CHNName
     * @param fileType
     * @param typeSort
     * @param tabName
     * @param postfix 后缀 站点表后缀
     */
    DataType(String v, String CHNName,String fileType, String typeSort,String tabName,String postfix) {
    	this.value = v;
        this.CHNName = CHNName; 
        this.fileType = fileType;
        this.typeSort = typeSort;
        this.tableName = tabName;
        this.postfix = postfix;
    }
    
    public String value() {
        return value;
    }
    public String getCHNName(){
    	return this.CHNName;
    }
    public String getFileType(){
    	return this.fileType;
    }
    public String getTypeSort(){
    	return this.typeSort;
    }
    public String getTableName(){
    	return this.tableName;
    }
    /**
     * 获取站点表的后缀
     * @return
     */
    public String getPostfix(){
    	return this.postfix;
    }
    public String getPathName(){
//    	if(this.equals(DataType.HJJSYBDZ))
//    		return DataType.HJJSYB.value;
//    	else if(this.equals(DataType.HJAtlas))
//    		return DataType.HJAtlas.value + "/MarineAtlas";
//    	return this.value;
    	switch(this){
			case HJJSYBDZ:
				return DataType.HJJSYB.value;
			case HJAtlas:
				return DataType.HJAtlas.value + "/MarineAtlas";
			default:
				return this.value;
		}
    }
    
    /**
     * 根据枚举名称获取枚举类型 1:1
     * @param v 枚举名称
     * @return 查找到返回枚举，否则返回Null
     */
    public static DataType fromValue(String v) {
        for (DataType c: DataType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        System.err.println("无法构建枚举DataType，因无当前输入的信息【"+v+"】的相关枚举类型");
        return null;
    }
    /**
     * 根据枚举的文件类型查找枚举 1:n
     * @param v 文件类型
     * @return 枚举列表，查找到返回枚举列表，否则返回Null
     */
    public static List<DataType> fromFileType(String v) {
    	List<DataType> dataTypeList = new ArrayList<DataType>();
        for (DataType c: DataType.values()) {
            if (c.fileType.equals(v)) {
            	dataTypeList.add(c);
            }
        }
        if(dataTypeList.size() > 0)
        	return dataTypeList;
        else{
        	System.err.println("无法构建枚举DataType，因无当前输入的信息【"+v+"】的相关枚举类型");
        	return null;
        }
    }
    
    /**
     * 根据枚举的数据类型查找枚举 1:n
     * @param v 文件类型
     * @return 枚举列表，查找到返回枚举列表，否则返回Null
     */
    public static List<DataType> fromTypeSort(String v) {
    	List<DataType> dataTypeList = new ArrayList<DataType>();
        for (DataType c: DataType.values()) {
            if (c.typeSort.equals(v)) {
            	dataTypeList.add(c);
            }
        }
        if(dataTypeList.size() > 0)
        	return dataTypeList;
        else{
        	System.err.println("无法构建枚举DataType，因无当前输入的信息【"+v+"】的相关枚举类型");
        	return null;
        }
    }
    
    public boolean isDMData(){
    	switch(this){
			case DMQX:
			case JDDM:
			case HYQXSHIP:
			case DMZDZ:
			case MHDM:
				return true;
			default:
				return false;
		}
    	
//    	if(this.equals(DataType.DMQX) ||  this.equals(DataType.JDDM)//type.equals(DataType.HYQX)||
//				|| this.equals(DataType.DMZDZ)|| this.equals(DataType.MHDM))
//    		return true;
//    	return false;
    }
    
    /**
     * 是否为格点预报产品
     * @return true表示格点预报产品
     */
    public boolean isGrib(){
    	if(this.getTypeSort().equals("GRIB"))
    		return true;
    	return false;
    }
    /**
     * 是否为离散数据
     * @return true表示离散数据
     */
    public boolean isScatter(){
//    	if(this.getTypeSort().equals("SCATTER") || this.equals(DataType.HYQXSHIPREAL))
//    		return true;
//    	return false;
    	if(this.getTypeSort().equals("SCATTER"))
    		return true;
    	return false;
    }
    public boolean isDAT(){ 
    	if(getTypeSort().equals("DAT"))
    		return true;
    	return false;
    }
    public boolean isNcFile(){
    	if(this.getTypeSort().equals("NC"))
    		return true;
    	return false;
    }
	/**
	 * 是否为站点表数据
	 * @return true表示站点表数据
	 */
    public boolean isZD(){
    	if(this.getTypeSort().equals("ZD"))
    		return true;
    	return false;
    }
    
    /**
	 * 是否为热带气旋表数据
	 * @return true表示站点表数据
	 */
    public boolean isRDQX(){
//    	if(this.equals(DataType.RDQX) || this.equals(DataType.TYPH))
//    		return true;
//    	return false;
    	switch(this){
			case RDQX:
			case TYPH:
				return true;
			default:
				return false;
		}
    }
    
    /**
	 * 是否为城镇预报
	 */
    public boolean isCZFC(){
    	if(this.equals(DataType.CZFC))
    		return true;
    	return false;
    }
    
    /**
	 * 是否为云图数据
	 * @return true表示云图数据
	 */
//    public static boolean isYT(DataType type){
//    	if(type.getTypeSort().equals("YT"))
//    		return true;
//    	return false;
//    }
    public boolean isYT(){
    	if(getTypeSort().equals("YT"))
    		return true;
    	return false;
    }
//    public boolean isHY(){
//    	if(this.equals(DataType.HJJSYB) ||
//    			this.equals(DataType.HJJXHDQ) || this.equals(DataType.HJJXHHL) 			||
//    			this.equals(DataType.HJWXFY)  || this.equals(DataType.HJBZCP) 			||
//    			this.equals(DataType.HJCPFLL) || this.equals(DataType.HT511) 			||
//    			this.equals(DataType.HT213)   || this.equals(DataType.HMM5) 			||
//    			this.equals(DataType.HJAtlas) || this.equals(DataType.HJARGO) 			||
//    			this.equals(DataType.HJTS)    || this.equals(DataType.HySteadyStat) 	|| 
//    			this.equals(DataType.HYQXSHIP)|| this.equals(DataType.HyHisWindWaveYrr) ||
//    			this.equals(DataType.HJTSREAL)|| this.equals(DataType.HyHisWave) 		|| 
//    			this.equals(DataType.HJTRAN)  || this.equals(DataType.HyHisTide) 		|| 
//    			this.equals(DataType.HJCURR)  || this.equals(DataType.HyHisStts) 		||
//    			this.equals(DataType.ZCCPWYL) || this.equals(DataType.HYTEMPCLINEAtlas) ||
//    			this.equals(DataType.ZCT799)  || this.equals(DataType.HYTEMPCLINE) 		||
//    			this.equals(DataType.ZCCPHL)  || this.equals(DataType.HyStatProc) 		||
//    			this.equals(DataType.OHMS)    || this.equals(DataType.HJYB) 			||
//    			this.equals(DataType.ZPSW) 	  || this.equals(DataType.ZPCOADS) 			||
//    			this.equals(DataType.ARGO)    || this.equals(DataType.HJSZCPQY) 		||
//    			this.equals(DataType.HJSZCPDQ)|| this.equals(DataType.HJJSYBDZ)		)
//    		return true;
//    	return false;
//    }
    /**
	 * 是否为传真图数据
	 * @return true表示传真图数据
	 */
    public boolean isCZT(){
    	if(this.getTypeSort().equals("CZ"))
    		return true;
    	return false;
    }
    /**
	 * 是否为雷达图数据
	 * @return true表示雷达图数据
	 */
    public boolean isLDT(){
    	if(this.getTypeSort().equals("LD"))
    		return true;
    	return false;
    }
    
    /**
     * 返回格点数据网格类型
     * @return
     */
    public DataGridType getDataGridType(){
    	if(isGrid()){
    		if(this.equals(DataType.KW) || this.equals(DataType.KM))
    			return DataGridType.IntervalEqual;
    		return DataGridType.LatLonEqual;
    	}
    	return DataGridType.LatLonEqual;
    }
    
    public boolean isArgo(){
    	if(this.equals(DataType.HJARGO) )
    		return true;
    	return false;
    }
    public boolean isHyHis(){
//    	if(this.equals(DataType.HJTS) || this.equals(DataType.HyHisWindWaveYrr) 
//    			|| this.equals(DataType.HJCURR) || this.equals(DataType.HJTRAN)
////    			|| this.equals(DataType.HYQX) || this.equals(DataType.HYQXSHIP) || this.equals(DataType.HYQXSHIPREAL) 
////    			|| this.equals(DataType.HJTSREAL) || this.equals(DataType.HYQXREAL)
//    			|| this.equals(DataType.HYQXSHIP) || this.equals(DataType.HJTSREAL)
//    			|| this.equals(DataType.HYTEMPCLINE) || this.equals(DataType.HJAtlas))
//    		return true;
//    	return false;
    	switch(this){
			case HJTS:
			case HyHisWindWaveYrr:
			case HJCURR:
			case HJTRAN:
			case HYQXSHIP:
			case HJTSREAL:
			case HYTEMPCLINE:
			case HJAtlas:
				return true;
			default:
				return false;
		}
    }
    public boolean isPlaneNC(){
//    	if(this.equals(DataType.HYQX) || this.equals(DataType.HYQXSHIP) )
//    		return true;
//    	return false;
    	if(this.equals(DataType.HYQXSHIP) )
    		return true;
    	return false;
    }
    public boolean isExsitCompressFile(){
    	if(isScatter() || this.equals(DataType.HJTS) || this.equals(DataType.HJARGO)
    			|| isHyHis())
    		return true;
    	return false;
    	
    }
    
    public boolean isLonFrom0(){
//    	if(this.equals(DataType.RJ) || this.equals(DataType.HT511)
//    			|| this.equals(DataType.HMM5))
//    		return true;
//    	return false;
    	switch(this){
			case RJ:
			case HT511:
			case HMM5:			
				return true;
			default:
				return false;
		}
    }
    
    public boolean isEPScatter(){
//    	if(DataType.EP_STATIONDATA_HOUR.equals(this) || 
//    			DataType.EP_STATIONDATA_DAY.equals(this) || DataType.EP_STATIONDATA_FC.equals(this) )
//    		return true;
//    	return false;
    	switch(this){
			case EP_STATIONDATA_HOUR:
			case EP_STATIONDATA_DAY:
			case EP_STATIONDATA_FC:			
				return true;
			default:
				return false;
		}
    	
    }
    
    public boolean isZFXDataType(){
//    	if(DataType.ZFXEC.equals(this) || DataType.ZFXNCEP.equals(this) )
//    		return true;
//    	return false;
    	switch(this){
			case ZFXEC:
			case ZFXNCEP:
				return true;
			default:
				return false;
		}
    }
    /**
     * 是否为格点数据
     * @return true表示格点预报产品
     */
    
    public boolean isGrid(){
    	if(this.getTypeSort().equals("GRIB")
    		|| this.equals(DataType.EPHEFC))
    		return true;
    	return false;
    }   
    
    /**
     * 数据类型是否为海啸或地震预警数据
     * @return
     */
    public boolean isNTWC(){
    	switch(this){
			case NTWC_EQ_CEIC:
			case NTWC_EQ_CWB:
			case NTWC_EQ_ESSO:
			case NTWC_EQ_USGS:
			case NTWC_SL_IOC:
			case NTWC_SL_NDBC:
			case NTWC_SL_TCL:
			case NTWC_TS_CWB:
			case NTWCIOCZD:
			case NTWCTCLZD:
			case NTWCNDBCZD:
			case NTWC_BG_TCLCOEFF:
			case NTWC_BG_EQ:
			case NTWC_BG_TS:
			case NTWC_BG_EQ_NEIC:
			case NTWC_TS_PTWC:
			case NTWC_EQ_EQIM:
			case NTWC_EQ_CMT:
			case NTWC_BG_FE:
			case NTWC_BG_STRAP:
			case NTWC_BG_TSSCENE:
			case NTWC_EQ_EMSC:
			case NTWC_EQ_ANTE:
			case NTWC_BG_CMT:
			case NTWC_EQ_SEIS3:
			case NTWC_BG_EQ_USGS:
				return true;
			default:
				return false;
    	}
    }
    /**
     * 数据类型是否为海啸或地震历史数据
     * @return
     */
    public boolean isNTWCHis(){
    	switch(this){
			case NTWC_BG_EQ:
			case NTWC_BG_TS:
			case NTWC_BG_EQ_NEIC:
				return true;
			default:
				return false;
    	}
    }
}
