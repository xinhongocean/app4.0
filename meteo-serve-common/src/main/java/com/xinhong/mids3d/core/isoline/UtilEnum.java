package com.xinhong.mids3d.core.isoline;


/**
 * 通用的枚举类型
 * @author sjn
 *
 */
public class UtilEnum {
	
	/**
	 * 云图通道代号
	 * @author sjn
	 */
	public enum SpChannelCode{
		/** *  红外一通道 */
		IR1("IR1","I","红外一通道", "红外一"),
		/** *  红外二通道  */
		IR2("IR2","S","红外二通道", "红外二"),
		/** *  红外三通道  */
		IR3("IR3","S","红外三通道", "红外三"),
		/** *  红外四通道  */
		IR4("IR4","S","红外四通道", "红外四"),
		/** *  通道4  */
		TD4("TD4","S","通道4", "通道4"),
		/** *  通道5  */
		TD5("TD5","S","通道5", "通道5"),
		/** *  通道6  */
		TD6("TD6","S","通道6", "通道6"),
		/** *  通道8  */
		TD8("TD8","S","通道8", "通道8"),
		/** *  通道9  */
		TD9("TD9","S","通道9", "通道9"),
		
		/** *  水汽通道 */
		VAPOR("VAPOR","W","水汽通道", "水汽"),
		/** *  中红外通道 */
		MIR("MIR","R","中红外通道", "中红外"),
		/** *  短红外通道 */
		LIR("LIR","L","短红外通道", "短红外"),
		/** *  可见光通道 */
		VL("VL","V","可见光通道", "可见光"),
		/** *  多通道合成图像  */
		MC("MC","Z","多通道合成图像", "多通道"),
		/** *  多星拼图 */
		MSP("MSP","M","多星拼图", "多星")
		;
		
		
		private String value;
		private String logogram;
		private String name;
		private String UIName;
		
		SpChannelCode(String v, String logogram, String name, String UIName){
			this.value = v;
			this.logogram = logogram;
			this.name = name;
			this.UIName = UIName;
		}
		/**
		 * 获取卫星通道描写信息
		 * @return
		 */
		public String getName(){
			return this.name;
		}
		/**
		 * 获取卫星通道UI显示信息
		 * @return
		 */
		public String getUIName(){
			return this.UIName;
		}
		/**
		 * 获取卫星通道的简写
		 * @return
		 */
		public String getLogogram(){
			return this.logogram;
		}
		
		public static SpChannelCode fromValue(String v) {
			for (SpChannelCode c: SpChannelCode.values()) {
				if (c.value.equals(v)) {
					return c;
				}
			}
			throw new IllegalArgumentException(v);
		}
		public static SpChannelCode fromLogogram(String v) {
			for (SpChannelCode c: SpChannelCode.values()) {
				if (c.logogram.equals(v)) {
					return c;
				}
			}
			throw new IllegalArgumentException(v);
		}
		
		public static SpChannelCode fromName(String v) {
			for (SpChannelCode c: SpChannelCode.values()) {
				if (c.name.equals(v)) {
					return c;
				}
			}
			throw new IllegalArgumentException(v);
		}
		
		public static SpChannelCode fromUIName(String v) {
			for (SpChannelCode c: SpChannelCode.values()) {
				if (c.UIName.equals(v)) {
					return c;
				}
			}
			throw new IllegalArgumentException(v);
		}
	}
	/**
	 * 投影方式
	 * @author sjn
	 *
	 */
	public enum Projection{
		/**		 * 兰勃托投影		 */
		LBT("LBT","L","兰勃托投影", "兰勃托"),
		lbt("lbt","l","兰勃托投影", "兰勃托"),
		/**		 * 麦卡托投影（小范围）		 */
		MKT("MKT","M","麦卡托投影（小范围）", "麦卡托（小）"),
		mkt("mkt","m","麦卡托投影（小范围）", "麦卡托（小）"),
		/**		 * 麦卡托投影(大范围)		 */
		MKTL("MKTL","N","麦卡托投影（大范围）", "麦卡托（大）"),
		/**		 * 多星拼图（等经纬度变换）		 */
		MSP("MSP","P","多星拼图（等经纬度变换）", "多星（等经纬度变换）"),
		/**		 * 未投影		 */
		NP("NP","E","未投影", "未投影"),
		/**		 * 原始投影		 */
		ORG("ORG","O","原始投影", "原始投影"),
		org("org","o","原始投影", "原始投影"),
		/**		 * GEOS投影		 */
		GEOS("GEOS","X","GEOS投影", "GEOS投影"),
		
		;
		
		//org lbt mkt//
		
		private String value;
		private String logogram;
	    private String name;
	    private String UIName;
	    
	    Projection(String v, String logogram, String name, String UIName){
	    	this.value = v;
	    	this.logogram = logogram;
	    	this.name = name;
	    	this.UIName = UIName;
	    }
	    /**
	     * 获取卫星投影描写信息
	     * @return
	     */
	    public String getName(){
	    	return this.name;
	    }
	    /**
	     * 获取卫星投影UI显示信息
	     * @return
	     */
	    public String getUIName(){
	    	return this.UIName;
	    }
	    /**
	     * 获取卫星投影的简写
	     * @return
	     */
	    public String getLogogram(){
	    	return this.logogram;
	    }
	    
	    public static Projection fromValue(String v) {
	        for (Projection c: Projection.values()) {
	            if (c.value.equals(v)) {
	                return c;
	            }
	        }
	        throw new IllegalArgumentException(v);
	    }
	    public static Projection fromLogogram(String v) {
	        for (Projection c: Projection.values()) {
	            if (c.logogram.equals(v)) {
	                return c;
	            }
	        }
	        throw new IllegalArgumentException(v);
	    }
	    
	    public static Projection fromName(String v) {
	        for (Projection c: Projection.values()) {
	            if (c.name.equals(v)) {
	                return c;
	            }
	        }
	        throw new IllegalArgumentException(v);
	    }
	}
	/**
	 * 卫星名称代号
	 * @author sjn
	 *
	 */
		/**		MTSAT	0-MTSAT_LRIT */
	public enum SPName{
		MTSAT("MTSAT","A","MTSAT", "MTSAT"),
		/**		MTSAT_HRIT	 */
		MTSATHRIT("MTSATHRIT","X","MTSATHRIT", "MTSATHRIT"),
		/**		fy2b		 */
		FY2B("FY2B","B","fy2b", "fy2b"),
		/**		fy2c		 */
		FY2C("FY2C","C","fy2c", "fy2c"),
		/**		fy2d		 */
		FY2D("FY2D","D","fy2d", "fy2d"),
		/**		fy2e		 */
		FY2E("FY2E","E","fy2e", "fy2e"),
		/**		fy2f		 */
		FY2F("FY2F","F","fy2f", "fy2f"),
		/**		fy2g		 */
		FY2G("FY2G","G","fy2g", "fy2g"),
		/**		METSAT		 */
		METSAT("METSAT","F","METSAT", "METSAT"),
		/**		GMS		 */
		GMS("GMS","G","GMS", "GMS"),
		/**		GOES		 */
		GOES("GOES","H","GOES", "GOES"),
		/**		FY-2卫星图像（整点云图） 0		 */
		FY2FULL("FY2FULL","J","FY-2卫星图像（整点云图）", "FY-2（整点）"),
		/**		FY-2卫星图像（半点云图） 4		 */
		FY2HALF("FY2HALF","N","FY-2卫星图像（半点云图）", "FY-2（半点）"),
		/**		MTSAT卫星半点云图（GMS-5卫星图像） 1		 */
		GMS5("GMS5","K","MTSAT卫星半点云图（GMS-5卫星图像）", "MTSAT半点（GMS-5）"),
		/**		Meteosat-5卫星图像（全圆盘图） 2		 */
		METESATT5("METESATT5","L","Meteosat-5卫星图像（全圆盘图）", "Meteosat-5（全圆盘）"),
		/**		Meteosat-5与GMS-5卫星大拼图 3		 */
		MG5("MG5","M","Meteosat-5与GMS-5卫星大拼图", "Meteosat-5与GMS-5拼图"),
		/**		FY3A		 */
		FY3A("FY3A","A","FY3A", "FY3A"),
		/**		HY1B		 */
		HY1B("HY1B","B","HY1B", "HY1B"),
		/**		HY2A		 */
		HY2A("HY2A","A","HY2A", "HY2A"),
		/**		MODA		 */
		MODA("MODA","A","MODA", "MODIS系列Aqua星"),
		/**		MODT		 */
		MODT("MODT","T","MODT", "MODIS系列Terra星"),
		/**		NO15		 */
		NO15("NO15","15","NO15", "美国NOAA卫星15星"),
		/**		NO16		 */
		NO16("NO16","16","NO16", "美国NOAA卫星16星"),
		/**		RECO		 */
		RECO("RECO","O","RECO", "重构产品"),
		/**		ASSI		 */
		ASSI("ASSI","I","ASSI", "融合产品")
		
		;
		
		private String value;
		private String logogram;
	    private String name;
	    private String UIName;
	    
	    SPName(String v, String logogram, String name, String UIName){
	    	this.value = v;
	    	this.logogram = logogram;
	    	this.name = name;
	    	this.UIName = UIName;
	    }
	    /**
	     * 获取卫星名称
	     * @return
	     */
	    public String getName(){
	    	return this.name;
	    }
	    /**
	     * 获取卫星名称UI显示信息
	     * @return
	     */
	    public String getUIName(){
	    	return this.UIName;
	    }
	    /**
	     * 获取卫星名称的简写
	     * @return
	     */
	    public String getLogogram(){
	    	return this.logogram;
	    }
	    
	    public static SPName fromValue(String v) {
	        for (SPName c: SPName.values()) {
	            if (c.value.equals(v)) {
	                return c;
	            }
	        }
	        throw new IllegalArgumentException(v);
	    }
	    public static SPName fromLogogram(String v) {
	        for (SPName c: SPName.values()) {
	            if (c.logogram.equals(v)) {
	                return c;
	            }
	        }
	        throw new IllegalArgumentException(v);
	    }
	    
	    public static SPName fromName(String v) {
	        for (SPName c: SPName.values()) {
	            if (c.name.equals(v)) {
	                return c;
	            }
	        }
	        throw new IllegalArgumentException(v);
	    }
	}
	
	/**
	 * 分辨率
	 * @author SJN
	 *
	 */
	public enum Dimension{
		/**		高分辨率		 */
		High("High","高分辨率"),
		/**		低分辨率		 */
		Lower("Lower","低分辨率");
		
		private String value;
	    private String name;
	    
	    Dimension(String v, String name){
	    	this.value = v;
	    	this.name = name;
	    }
	    /**
	     * 获取分辨率名称
	     * @return
	     */
	    public String getName(){
	    	return this.name;
	    }
	    
	    public static Dimension fromValue(String v) {
	        for (Dimension c: Dimension.values()) {
	            if (c.value.equals(v)) {
	                return c;
	            }
	        }
	        throw new IllegalArgumentException(v);
	    }
	    public static Dimension fromName(String v) {
	        for (Dimension c: Dimension.values()) {
	            if (c.name.equals(v)) {
	                return c;
	            }
	        }
	        throw new IllegalArgumentException(v);
	    }
	}
	
	/**
	 * 原始报文类型
	 * @author sjn
	 *
	 */
	public enum TeleType {		
	    ZC,
	    KJ,
	    WX,
	    QT;

	    public String value() {
	        return name();
	    }

	    public static TeleType fromValue(String v) {
	    	for (TeleType c: TeleType.values()) {
	            if (c.value().equals(v)) {
	                return c;
	            }
	        }
	        System.err.println("无法构建枚举TeleType，因无当前输入的信息【"+v+"】的相关枚举类型");
	        return null;
	    }

	}

	/**
	 * 时区
	 * @author sjn
	 *
	 */
	public enum TimeZone{
		/** 世界时 */
		UTC_0("UTC_0", "UTC+0", 0),
		/** 北京时 */
		UTC_8("UTC_8", "UTC+8", 8);
		
		private String value;
	    private String fileValue;
	    private int addHour;
	    
	    TimeZone(String v, String fileValue, int addHour){
	    	this.value = v;
	    	this.fileValue = fileValue;
	    	this.addHour = addHour;
	    }
	    /**
	     * 此方法为数据读取模块内部专用，不建议外部使用
	     * @return
	     */
	    public String getFileValue(){
	    	return this.fileValue;
	    }
	    
	    public static TimeZone fromValue(String v) {
	        for (TimeZone c: TimeZone.values()) {
	            if (c.value.equals(v)) {
	                return c;
	            }
	        }
	        System.err.println("无法构建枚举TimeZone，因无当前输入的信息【"+v+"】的相关枚举类型");
	        return null;
	    }
	    
	    public static TimeZone fromFileValue(String v) {
	        for (TimeZone c: TimeZone.values()) {
	            if (c.fileValue.equals(v)) {
	                return c;
	            }
	        }
	        throw new IllegalArgumentException(v);
	    }
	    
	    /**
	     * 时区转换，返回需要相加的时次数
	     * 例如 世界时=北京时-8，北京时=世界时+8
	     * @param targetTZ 目标时区
	     * @return 原时区需要相加的时次数
	     */
	    public int getAddHour(TimeZone targetTZ){
	    	int result;
 	    	if(this == targetTZ || targetTZ == null){
	    		result = 0;
	    		return result;
	    	}
	    	if(targetTZ == null && targetTZ == null)
	    	{
	    		result = 0;
	    		return result;
	    	}
	    	int source = this.addHour;
	    	int target = targetTZ.addHour;
//	    	result = source - target;
	    	if(source > target){
	    		result = -1 * Math.abs(target - source);
	    	}else if(source < target){
	    		result = Math.abs(target - source);
	    	}else{ // ==
	    		result = 0;
	    	}
	    	return result;
	    }
	}
	
	/**
	 * 数据来源
	 * @author sjn
	 *
	 */
	public enum DataReaderSource{
		/** oracle数据库 */
		ORACLE,
		/** 文件 */
		FILE,
		/** h2内存数据库 */
		H2DB
	}
	
	/**
	 * 网格类型
	 * @author SJN
	 *
	 */
	public enum DataGridType{
		/** 等距网格  间隔相等*/
		IntervalEqual("IntervalEqual", "等距"),
		/** 等经纬网格 */
		LatLonEqual("IntervalEqual", "等经纬");
		
		private String value;
	    private String chn;
	    
	    DataGridType(String v, String chn){
	    	this.value = v;
	    	this.chn = chn;
	    }
	    public String getChn(){
	    	return this.chn;
	    }
	    
	    public static DataGridType fromValue(String v) {
	        for (DataGridType c: DataGridType.values()) {
	            if (c.value.equals(v)) {
	                return c;
	            }
	        }
	        System.err.println("无法构建枚举DataGridType，因无当前输入的信息【"+v+"】的相关枚举类型");
	        return null;
	    }
	    
	    /**
	     * 根据中文名返回网格类型
	     * @param v
	     * @return
	     */
	    public static DataGridType fromChn(String v) {
	        for (DataGridType c: DataGridType.values()) {
	            if (c.chn.equals(v)) {
	                return c;
	            }
	        }
	        throw new IllegalArgumentException(v);
	    }
	}
}


