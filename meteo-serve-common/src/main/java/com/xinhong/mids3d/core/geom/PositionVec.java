package com.xinhong.mids3d.core.geom;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;

/**
 * 在Position类中增加返回Vec坐标方法，该Vec与Position相对应
 * 目前主要用于追踪等值线时，指定的坐标不是经纬度时， 存储 等值线线条及多边形的坐标。
 * @author liuxcpc
 *
 */
public class PositionVec extends Position {

	private Vec4 vec = null;
	public PositionVec(LatLon latLon, double elevation) {
		super(latLon, elevation);
	}

	public PositionVec(Angle latitude, Angle longitude, double elevation) {
		super(latitude, longitude, elevation);
	}
	
	public PositionVec(double latitude, double longitude, double elevation){
		this(Angle.fromDegrees(latitude), Angle.fromDegrees(longitude), elevation);
	}
	
    public static PositionVec fromDegrees(double latitude, double longitude, double elevation)
    {
        return new PositionVec(Angle.fromDegrees(latitude), Angle.fromDegrees(longitude), elevation);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        PositionVec pos = (PositionVec) o;
        if (vec != null){
        	if (!vec.equals(pos.vec))
        		return false;
        } else {
            if (pos.vec != null)
            	return false;             	
        } 
        return true;
    }
    
    /**
     * 设置Vec4中存储的x,y坐标
     * @param x
     * @param y
     */
    public void setXY(double x, double y){
    	this.vec = new Vec4(x, y);
    }
    /**
     * 利用Vec4初始化存储的vec
     * @param vec
     */
    public void setVec(Vec4 vec){
    	this.vec = new Vec4(vec.x, vec.y, vec.z, vec.w);
    }
    /**
     * 获取存储的x坐标 注意：如果vec为null，则会抛出异常! 需先确认vec已正确赋值方可使用.
     * @return
     */
    public double getX(){
    	if (this.vec == null){
    		throw new RuntimeException("PositionVec中的vec为null！无法获取x!");
    	}
    	return this.vec.x();
    }
    /**
     * 获取存储的y坐标 注意：如果vec为null，则会抛出异常! 需先确认vec已正确赋值方可使用.
     * @return
     */
     public double getY(){
    	if (this.vec == null){
    		throw new RuntimeException("PositionVec中的vec为null！无法获取y!");
    	}
    	return this.vec.y();
    }
     /**
      * 获取存储的vec 
      * @return
      */
   public Vec4 getVec(){
    	return this.vec;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        if (vec != null){
            long temp = vec.hashCode();
            result = 31 * result + (int) (temp ^ (temp >>> 32));        	
        }
        return result;
    }

    public String toString()
    {
        String strTmp = "(" + this.latitude.toString() + ", " + this.longitude.toString() + ", " + this.elevation + ")";
        if (vec != null){
        	strTmp += "," + vec.toString();
        }
        return strTmp;
    }
    
    
    public static void main(String[] args){
    	PositionVec posV = PositionVec.fromDegrees(-25, 119, 45);
    	System.out.println(posV);
    	//System.out.println(posV.getX());
    	posV.setXY(4, 69);
    	System.out.println(posV);
    	double x = posV.getX();
    	double y = posV.getY();
    	Vec4 vec = posV.getVec();
    	x = 5;
    	System.out.println(x);
    	System.out.println(y);
    	System.out.println(vec);
    	
    }
    
    

}
