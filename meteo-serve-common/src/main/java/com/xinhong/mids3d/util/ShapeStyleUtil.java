package com.xinhong.mids3d.util;
/**
 * 定义各种线条、形状及填充样式等所使用命名方案的工具类
 * @author lxc
 *
 */
public final class ShapeStyleUtil {

	private ShapeStyleUtil(){}
	
	/**
	 * 线的各种样式
	 */
	public enum LineStyle{
		SOLID("LineStyle.Solid", "实线"),
		DOT("LineStyle.Dot", "虚线"),
		DASH("LineStyle.Dash", "短划线"),
		DASHDOT("LineStyle.DashDot", "点划线"),
		DASHDOUBLEDOT("LineStyle.DashDoubleDot", "双点划线"),
		WAVECURVE("LineStyle.WaveCurve", "波浪线");
		
		LineStyle(String v, String Name) {
	        this.value = v;
	        this.Name = Name;
	    }
		public String getName(){
			return this.Name;
		}
		
	    @Override
	    public String toString() {
	    	
	    	return value;
	    }
	    public static LineStyle fromValue(String v) {
	        for (LineStyle c: LineStyle.values()) {
	            if (c.value.equals(v)) {
	                return c;
	            }
	        }
	        throw new IllegalArgumentException(v);
	    }
	    
	    
	    private final String value;		
	    private final String Name;		
	};
	
	/**
	 * 根据给定的Style获取线条的StipplePattern
	 * @param style
	 * @return
	 */
	public static short getLineStipplePattern(LineStyle style)
	{
		if (style == ShapeStyleUtil.LineStyle.DASH)
			return (short)0xEEEE;					
		else if (style == ShapeStyleUtil.LineStyle.DOT)
			return (short)0xAAAA;	
		else if (style == ShapeStyleUtil.LineStyle.DASHDOT)
			return (short)0xFAFA;	
		else if (style == ShapeStyleUtil.LineStyle.DASHDOUBLEDOT)
			return (short)0xF24F;	
		else {//Solid
			System.err.println("警告: In getLineStipplePattern, 给定线条的Style为Solid,为提高效率,设置StipplePattern为0!");
			return (short)0xFFFF;	
		}
	}	
	
	/**
	 * 填充的各种样式
	 */
	public enum FillStyle{
		/**实体填充*/
		SOLID("FillStyle.Solid", "Solid"),
		/**十字交叉线*/
		CROSS("FillStyle.Cross", "十字"),
		/**垂直断线*/
		VERTDASH("FillStyle.VertDash", "垂值虚线"),
		/**横向断线*/
		HORIZONDASH("FillStyle.HorizonDash", "横线"),		
		/**垂直实线*/
		VERTLINE("FillStyle.VertLine", "垂直实线"),
		/**斜断线*/
		SLOPEDASH("FillStyle.SlopeDash", "斜线"),
		/**点状 */
		POINT("FillStyle.Point", "点"),
		/**短弧状 */
		SHORTARC("FillStyle.ShortArc", "短弧");
		
		FillStyle(String v, String name) {
	        value = v;
	        this.name = name;
	    }
		public String getName(){
			return this.name;
		}
	    @Override
	    public String toString() {
	    	
	    	return value;
	    }	    
	    public static FillStyle fromValue(String v) {
	        for (FillStyle c: FillStyle.values()) {
	            if (c.value.equals(v)) {
	                return c;
	            }
	        }
	        throw new IllegalArgumentException(v);
	    }
	    private final String value;	
	    private final String name;
	}
	
}
