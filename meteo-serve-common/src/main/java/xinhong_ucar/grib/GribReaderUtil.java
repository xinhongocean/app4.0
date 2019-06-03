package xinhong_ucar.grib;

public class GribReaderUtil {

	public static float getReferenceValue(byte[] value){
		float fMin= Float.NaN;
		int[] off1 = new int[]{0};
		int sign = Value(value, off1, 1);
		int exp = Value(value, off1, 8);
		int mant = Value(value, off1, 8*3 - 1);
		if(exp == 0){
			fMin = (float) (mant * Math.pow(2, -149));
		}else if (exp >=1 && exp <= 254){
			double aa = 1.0 + mant*Math.pow(2, -23);
			fMin = (float) (aa * Math.pow(2, exp-127));
//			griba.fMin = (1.0 + griba.mant*Math.pow(2.0, -23.0)) * Math.pow(2.0, griba.exp-127);
		}else if (exp == 255){
			if(mant == 0){
				if(sign == 1){
					fMin = -999999;//sign == 1 负无穷大
				}else if(sign == 0){
					fMin = 999999; //sign == 0 正无穷大
				}
			}else if (mant > 0){
				fMin = (float) -999999.99;//取值无效
			}
		}
		if(sign == 1)
			fMin = -1 * fMin ;
		return fMin;
	}
	private final static int[] valuemask = new int[] { 0, 1, 3, 7, 15, 31, 63, 127, 255 };
	private final static int[] valuetwoto = new int[] { 1, 2, 4, 8, 16, 32, 64, 128, 256 };
	/**
	 *
	 * @param binaryVal
	 * @param off
	 * @param width
	 * @return
	 */
	public final static int Value(byte[] binaryVal, int[] off, int width) {
		byte[] fstbyt;
		int bitno, nbytes, nlast;
		int isa;
		int octet;
		int value = 0;

		if (width == 0)
			return value;

		int[] off1 = new int[] { off[0] >> 3 };
		fstbyt = new byte[binaryVal.length - off1[0]];
		for (int i = 0; i < fstbyt.length; i++){
			fstbyt[i] = binaryVal[off1[0] + i];
		}

		bitno = off[0] & valuemask[3];
		isa = width + bitno + 7;
		if (isa < 16) {
			octet = byteToInt(fstbyt[0]);
			value = octet & valuemask[8 - bitno];
			value = value >> (15 - isa);
		} else {
			nbytes = isa >> 3;
			nlast = (isa & valuemask[3]) + 1;
			for (int i = 0; i < nbytes; i++) {
				octet = byteToInt(fstbyt[i]);
				if (i == 0)
					value = octet & valuemask[8 - bitno];
				else if (i < nbytes - 1)
					value = value * 256 + octet;
				else if (i == nbytes - 1)
					value = value * valuetwoto[nlast] + (octet >> (8 - nlast));
			}
		}
		off[0] = off[0] + width;
		return value;
	}
	public static int byteToInt(byte bb){
		try{
			int byteValue ;
			int temp = bb%256;
			if(bb<0){
				byteValue =  256 + temp;
			}
			else{
				byteValue = (temp>127?temp-256:temp);
			}
			return byteValue;
		}catch(Exception e){
			e.printStackTrace();
			return -1;
		}
	}
	public static char getChar(byte[] b, int index) {
		String s = new String(b);
		char ch[] = s.toCharArray();
		return ch[0];
	}
}
