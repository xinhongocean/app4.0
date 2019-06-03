
package com.xinhong.mids3d.datareader.util;

public enum GridArea {
	/** 全球 */
	CAA("CAA", "全球", "全球"),
	/** 中国 （10°～60°N，70°～140°E）*/		
	CCN("CCN", "中国", "中国"),
	
//	/** 北京 */		
//	CBJ,
//	/** 台海 */		
//	CTH,
	/** 亚洲范围（20°～70°N，60°～160°E） */
	CYZ("CYZ", "亚洲范围", "亚洲范围"),
	/** 欧亚范围（0°～80°N，20°～160°E） */
	COY("COY", "欧亚范围", "欧亚区"),		
	/** 东北半球（0°～180°） */
	CEN("CEN", "东北半球", "东北半球"),
	/** 西北半球（-180°～0°） */
	CWN("CWN", "西北半球", "西北半球"),
	/** 北半球 */
	CNN("CNN", "北半球", "北半球"),
	/** 北半球（0°～90°） */
	CNA("CNA", "", ""),
	/** 北半球（90°～180°） */
	CNB("CNB", "", ""),
	/** 北半球（-180°～-90°） */
	CNC("CNC", "", ""),
	/** 北半球（-90°～0°） */
	CND("CND", "", ""),
	/** 北半球（-45°～180°） */
	CNE("CNE", "", ""),
	/**东半球*/
	CEE("CEE", "东半球", "东半球"),
	/**西半球*/
	CWW("CWW", "西半球", "西半球"),
	/** 东南半球（0°～180°） */
	CES("CES", "", ""),
	/** 西南半球（-180°～0°） */
	CWS("CWS", "", ""),
	/** 南半球 */
	CSS("CSS", "南半球", "南半球"),
	/** 南半球（0°～90°） */
	CSA("CSA", "", ""),
	/** 南半球（90°～180°）*/
	CSB("CSB", "", ""),
	/** ******************************** 以下区域可删除*********************************************	 */
	/** 南半球（-180°～-90°） */
	CSC("CSC", "", ""),
	/** 南半球（-90°～0°） */
	CSD("CSD", "", ""),
	/** 热带（35°S～35°N） */
	CRR("CRR", "", ""),
	/** 热带（0°～90°W） */
	CRA("CRA", "", ""),
	/** 热带（90°～180°W）*/
	CRB("CRB", "", ""),
	/** 热带 (180°～90°E ) */
	CRC("CRC", "", ""),
	/** 热带 (90°E～0°  ) */
	CRD("CRD", "", ""),
	/** 南半球热带（40°S～0°，0°～180°E） */
	CRS("CRS", "", ""),
	/** 太平洋岛屿（91～94区SHIP） */
	CDU("CDU", "", ""),
	/** 海洋船舶 */
	CHW("CHW", "", ""),
	/** 东南亚（48、96～98区SHIP） */
	CDN("CDN", "", ""),
	/** 45°S～45°N，90°～180°E  */
	COO("COO", "", ""),
	/**   *  未知  */
	CXX("CXX", "", ""),
	/** *******************************************************************************************	 */
	/**  HJ 001区域  N 0~42 E 105~145 */
	CNO("CNO", "001区", "HJ-001区域"),
	/**   *  SLAT="-10" ELAT="60" SLON="60" ELON="150" EC精细化预报 */
	UEC("UEC", "UEC", "欧亚区"),
	/** 北京区域 */
	UBB("UBB", "UBB", "北京区域"),
	/** 亚洲区域 */
	UCC("UCC", "UCC", "亚洲区域"),
	/** 台湾区域 */
	UTT("UTT", "UTT", "台湾区域"),
	/**   *  解释应用区域，表示从KT511数据进行分析  */
	T511("T511", "T511区", "T511东北半球"),
	/**   *  解释应用区域，表示从MM5数据进行分析  */
	MM5("MM5", "MM5区", "MM5中国区"),
	/**   *  解释应用区域，表示从WRF数据进行分析  */
	WRF("WRF", "WRF区", "WRF中国区"),
	
	//海洋水文区域
	
	/**   *  大西洋  */
	ATL("ATL", "ATL区", "大西洋"),
	/**   *  印度洋  */
	IND("IND", "IND区", "IndianOcean","印度洋"),
	/**   *  太平洋  */
	PAC("PAC", "PAC区", "太平洋"),
	/**   *  台湾周边  */
	TWC("TWC", "TWC区", "台湾周边"),
	/**   *  重点区域  */
	ZDQY("ZDQY", "重点区域", "重点区域"),
	/**   *  西北太平洋  */
	NWP("NWP", "NWP区", "WestPacificOcean", "西北太平洋"),
	
	
	/**   *  东海  */
	EastChina("EastChina", "EastChina", "EastChinaSea", "东海"),
	/**   *  南海  */
	SouthChina("SouthChina","SouthChina",  "SouthChinaSea", "南海"),
	/**   *  黄渤海  */
	YellowBoHai("YellowBoHai","YellowBoHai", "YellowBoHaiSea", "黄渤海"),
	
	
//	,
//	/**   *  中国沿海  */
//	C("TWC", "TWC区", "中国沿海")
	;
	
	private String value;
	private String name;
	private String pathName;
	private String UIName;
	
	GridArea(String v, String name, String UIName)
	{
    	this.value = v;
    	this.name = name;
    	this.UIName = UIName;
    }
	GridArea(String v, String name, String pathName, String UIName)
	{
    	this.value = v;
    	this.name = name;
    	this.pathName = pathName;
    	this.UIName = UIName;
    }
	public String value()
    {
    	return value;
    }
    
    public String getName()
    {    	
    	return name;
    }
    public String getPathName()
    {
    	return pathName;
    }
    
    public String getUIName()
    {
    	return UIName;
    }

    public static GridArea fromValue(String v)
    {
    	for (GridArea c: GridArea.values())
    	{
            if (c.value.equals(v))
            {
                return c;
            }
        }
        System.err.println("无法构建枚举GridArea，因无当前输入的信息【"+v+"】的相关枚举类型");
        return null;
    }

   /* public FreeArea getFreeArea(){
    	float[] latlon = GetMidsDataInfoFromCfg.getRangeToAryByName(null, this);
    	if(latlon == null)
    		return null;
    	//modified by stone 20141211 修改区域超出范围问题
    	if(latlon.length != 4)
    		return null;
    	if(latlon[0] < -90)
    		latlon[0] = -90;
    	if(latlon[1] > 90)
    		latlon[1] = 90;
    	if(latlon[2] < -180)
    		latlon[2] = -180;
    	if(latlon[3] > 360)
    		latlon[3] = 360;
    	
    	FreeArea freeArea = new FreeArea(latlon[0], latlon[1], latlon[2], latlon[3]);
    	return freeArea;
    }*/
    
   /* public FreeArea getFreeArea(DataType dataType){
    	if(dataType == null)
    		return getFreeArea();
    	float[] latlon = GetMidsDataInfoFromCfg.getRangeToAryByName(dataType, this);
    	if(latlon == null)
    		return null;
    	//modified by stone 20141211 修改区域超出范围问题
    	if(latlon.length != 4)
    		return null;
    	if(latlon[0] < -90)
    		latlon[0] = -90;
    	if(latlon[1] > 90)
    		latlon[1] = 90;
    	if(latlon[2] < -180)
    		latlon[2] = -180;
    	if(latlon[3] > 360)
    		latlon[3] = 360;
    	
    	FreeArea freeArea = new FreeArea(latlon[0], latlon[1], latlon[2], latlon[3]);
    	return freeArea;
    }*/
    public static GridArea fromPathName(String v)
    {
    	for (GridArea c: GridArea.values())
    	{
            if (v.equals(c.pathName))
            {
                return c;
            }
        }
        System.err.println("无法构建枚举GridArea，因无当前输入的信息【"+v+"】的相关枚举类型");
        return null;
    }
    
}
