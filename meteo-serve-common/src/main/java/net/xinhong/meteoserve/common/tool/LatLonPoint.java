package net.xinhong.meteoserve.common.tool;


import net.xinhong.meteoserve.common.constant.DataTypeConst;

public class LatLonPoint {
	 	public float latitude = DataTypeConst.NULLVAL;
	    public float longitude = DataTypeConst.NULLVAL;
	    
	    public static LatLonPoint fromFloat(float latitude, float longitude)
	    {
	        return new LatLonPoint(latitude, longitude);
	    }

	    public LatLonPoint(float latitude, float longitude)
	    {
	        this.latitude = latitude;
	        this.longitude = longitude;
	    }
	    
	    public LatLonPoint(LatLonPoint latLon)
	    {
	        if (latLon == null)
	        {
	            String message = "nullValue.LatLonIsNull";
	            throw new IllegalArgumentException(message);
	        }

	        this.latitude = latLon.latitude;
	        this.longitude = latLon.longitude;
	    }
	    
	    public LatLonPoint clone(){
	    	LatLonPoint point = new LatLonPoint(this.latitude,this.longitude);
	    	return point;
	    }
	    
	    public float getLatitude()
	    {
	        return this.latitude;
	    }

	    public float getLongitude()
	    {
	        return this.longitude;
	    }
	    
	    @Override
		public int hashCode(){
			return (""+this.getLatitude() + this.getLongitude()).hashCode();
		}
	    @Override
	    public boolean equals(Object obj) {
	    	if(obj==null||!(obj instanceof LatLonPoint)){
				return false;
			}
	    	LatLonPoint obj2=(LatLonPoint)obj;
	    	if(this.latitude==obj2.getLatitude()
	    			&&this.longitude==obj2.getLongitude()){
	    		return true;
	    	}
	  
	    	return false;
	    }
	    @Override
	    public String toString() {
	    	return String.format("%6.4f,", this.latitude) + String.format("%6.4f", this.longitude);
	    }
}
