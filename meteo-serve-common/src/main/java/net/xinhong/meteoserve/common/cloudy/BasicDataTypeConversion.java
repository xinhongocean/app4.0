package net.xinhong.meteoserve.common.cloudy;

public class BasicDataTypeConversion {
	
	public BasicDataTypeConversion(){
		
	}
	
	public static int oneByteToInt(byte b){
    	int s = 0;
		int s0 = b & 0xff ;// 最低位
		s = s0;
		return s;
    }
	
	public static int byteToInt(byte b) {
		return b & 0xff;
	}
	public static String byteToChar(byte b) {
		return "" + (char)b;
	}
	/** 
     * 字节到字符转换 
     *  
     * @param b 
     * @return 
     */ 
   	public static char getChar(byte[] b, int index) {  
        String s = new String(b); 
        char ch[] = s.toCharArray();        
        return ch[0];  
    }
	
	public static int byteToInt(byte[] b) {
		int s = 0;

		if (b.length == 1) {
			int s0 = b[0] & 0xff ;// 最低位
			s = s0;
		} else if (b.length == 2) {
			int s0 = b[0] & 0xff;// 最低位
			int s1 = b[1] ;
			s1 <<= 8;
			s = s0 | s1;
		} else if (b.length == 3) {
			int s0 = b[0] & 0xff;// 最低位
			int s1 = b[1] & 0xff;
			int s2 = b[2] & 0xff;
			s2 <<= 16;
			s1 <<= 8;
			s = s0 | s1 | s2;
		} else if (b.length == 4) {
			int s0 = b[0] & 0xff;// 最低位
			int s1 = b[1] & 0xff;
			int s2 = b[2] & 0xff;
			int s3 = b[3] & 0xff;
			s3 <<= 24;
			s2 <<= 16;
			s1 <<= 8;
			s = s0 | s1 | s2 | s3;
		}
		return s;		
	}

	public static String byteToChar(byte[] b) {
		String mess = "";
		for (int i = 0; i < b.length; i++) {
			mess += (char) b[i];
		}
		return mess;
	}

	public static short byteToShort(byte[] b) {
		short s = 0;
		short s0 = (short) (b[0] & 0xff);// 最低位
		short s1 = (short) (b[1] & 0xff);
		s1 <<= 8;
		s = (short) (s0 | s1);
		return s;
	}

	public static String bytesToHexString(byte[] src) {
		String hString = "";
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				hString += "0";
			}
			hString += hv;
		}
		return hString;
	}
	
	/**
	 * 字节转换为浮点
	 * @param b 字节数组 至少4位
	 * @param index 开始位置
	 * @return
	 */
	public static float byteToFloat(byte[] b,int index){
		if(index < 0 || (index + 4) > b.length)
			return ConstDataUtil.NULLVAL;
		int l;
		l = b[index + 0];
		l &= 0xff;
		l |= ((long)b[index + 1] << 8);
		l &= 0xffff;
		l |= ((long)b[index + 2] << 16);
		l &= 0xffffff;
		l |= ((long)b[index + 3] << 24);
		return Float.intBitsToFloat(l);
	}
	

}
