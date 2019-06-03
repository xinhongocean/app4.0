package net.xinhong.meteoserve.common.grib;


import net.xinhong.meteoserve.common.constant.DataTypeConst;

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
	 * 注释：int到字节数组的转换！
	 * @param number     
	 * @return    
	 */
	public static byte[] intToByte(int number) {        
		int temp = number;        
		byte[] b = new byte[4];        
		for (int i = 0; i < b.length; i++) {            
			b[i] = new Integer(temp & 0xff).byteValue();//将最低位保存在最低位            
			temp = temp >> 8; // 向右移8位        
		}        
		return b;
	}
	
	public static byte[] floatToByte(float f){
		int fbit = Float.floatToIntBits(f);
		int len = 4;
		byte[] b = new byte[len];
		for(int i=0;i<len; i++){
			b[i] = (byte)(fbit>>(24 - i * 8));
		}
		
		byte[] dest = new byte[len];
		System.arraycopy(b, 0, dest, 0, len);
		byte temp;
		for(int i=0;i<len/2;i++){
			temp = dest[i];
			dest[i] = dest[len - i - 1];
			dest[len - i - 1] = temp;
		}
		return dest;
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
			return DataTypeConst.NULLVAL;
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
	public static long bytesToLong(byte[] b) {
		long s = 0;
		long s0 = b[0] & 0xff;// 最低位
		long s1 = b[1] & 0xff;
		long s2 = b[2] & 0xff;
		long s3 = b[3] & 0xff;
		long s4 = b[4] & 0xff;// 最低位
		long s5 = b[5] & 0xff;
		long s6 = b[6] & 0xff;
		long s7 = b[7] & 0xff;
		// s0不变
		s1 <<= 8;
		s2 <<= 16;
		s3 <<= 24;
		s4 <<= 8 * 4;
		s5 <<= 8 * 5;
		s6 <<= 8 * 6;
		s7 <<= 8 * 7;
		s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;
		return s;
	}

}
