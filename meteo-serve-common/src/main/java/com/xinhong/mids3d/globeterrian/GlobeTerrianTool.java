package com.xinhong.mids3d.globeterrian;

import gov.nasa.worldwind.geom.Position;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public final class GlobeTerrianTool {
	private static String terrainFilePath = "/globe/terrainRGB.txt";
	private static String terrainFileAlongChinaPath = "/com/xinhong/mids3d/core/globes/terrainAlongChinaRGB.txt";
	private static byte[][] varRGB = null;
	private GlobeTerrianTool(){}
	private final static double[] landLatStartAry = {62, 55, 35, 40, 51, -15, -20, -34, -90, 69, 48, 26, 42, 24, 64, -30, -7, -19, 29.6, 32.5, 33.6,10.3, 9.2};
	private final static double[] landLatEndAry = {68, 67, 55, 50, 58, 5, -9, -15, -80, 82, 63, 66, 70, 42, 68, -20, 30, -7, 32.4, 33.6, 34.4, 11.7, 10.3};
	private final static double[] landLonStartAry = {-160, -130, -120, -85, -75, -75, -55, -70, -180, -50, 30, 65, 110, 110, 130, 120, 14, 15, 118, 119, 118.8, 104.8, 104.8};
	private final static double[] landLonEndAry = {-130, -95, -85, -75, -65, -55, -40, -55, 180, -30, 65, 110, 130, 117, 170, 145, 30, 36, 121.5, 120.5, 120.2, 106.8, 106.2};
	public static boolean isFilterLand(double lat, double lon){
		boolean isFilter = false;
		for(int k = 0; k < landLatStartAry.length; k++){
			if(lat >= landLatStartAry[k] && lat <= landLatEndAry[k] 
					&& lon >= landLonStartAry[k] && lon <= landLonEndAry[k]){
				isFilter = true;
				break;
			}
		}
		return isFilter;
	}


	private static int[][] seaAreaData=null;
//	private static String seaAreaDataFile= GlobeTerrianTool.class.getClass().getResource("/globe/globeterrian2.dat").getPath();
	private static String chinaSeaAreaDataFile = GlobeTerrianTool.class.getClassLoader().getResource("globe/chinaseaterrian.dat").getPath();
	private static Map seaIDNameMap = new HashMap<Integer,String>(20);

	/**
	 * 返回给定经纬度所在的中国海区。
	 * @param lat
	 * @param lon
     * @return-海区编号及海区名称，用|分割。如返回：1|渤海
     */
	public static String getChinaSeaIDName(float lat, float lon){
		if (GlobeTerrianTool.isFilterLand(lat,lon))
			return null;
		if (seaAreaData == null){
			seaAreaData = new int[500][500];

//			DataInputStream opt;
//			try {
//				opt = new DataInputStream(new FileInputStream("D:\\work\\meteo-server\\meteo-serve\\meteo-serve-common\\src\\main\\resources\\globe\\chinaseaterrian.dat"));
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//				return null;
//			}
			InputStream in=GlobeTerrianTool.class.getClassLoader()
					.getResourceAsStream("globe/chinaseaterrian.dat");
//			DataInputStream	opt = new DataInputStream(in);
			ByteBuffer buffer=ByteBuffer.allocate(1024*1024);
//			buffer.order(ByteOrder.LITTLE_ENDIAN);
			try {
				in.read(buffer.array());
			} catch (IOException e) {
				e.printStackTrace();
			}

			for (int i = 0; i < 500; i++) {
				for (int j = 0; j < 500; j++) {
					seaAreaData[i][j] = buffer.getInt();
				}
			}
			seaIDNameMap.put(1,"渤海");
			seaIDNameMap.put(2,"黄海北部");
			seaIDNameMap.put(3,"黄海中部");
			seaIDNameMap.put(4,"黄海南部");
			seaIDNameMap.put(5,"东海西北部");
			seaIDNameMap.put(6,"东海西南部");
			seaIDNameMap.put(7,"台湾海峡");
			seaIDNameMap.put(9,"南海东北部");
			seaIDNameMap.put(10,"南海西北部");
			seaIDNameMap.put(11,"北部湾");
			seaIDNameMap.put(12,"南海中西北部");
			seaIDNameMap.put(13,"南海中东部");
			seaIDNameMap.put(14,"南海东南部");
			seaIDNameMap.put(15,"南海西南部");
			seaIDNameMap.put(16,"台湾以东洋面");
			seaIDNameMap.put(17,"巴士海峡");
			seaIDNameMap.put(19,"东海东北部");
			seaIDNameMap.put(20,"东海东南部");
		}
		if (seaAreaData == null)
			return null;

		int sLat=0;
		int eLat=50;
		int slon=100;
		int elon=150;
		if (lat > eLat || lat < sLat || lon > elon || lon < slon) //超出范围
			return null;

		int nlat=Math.round((90.0f-lat)*10.0f) - (90-eLat)*10;
		int nlon=Math.round((180.0f+lon)*10.0f) - (180+slon)*10;
		int num = seaAreaData[nlon][nlat];
		int seaID = num / (255*100);  //数组中存储的颜色为R*255+G*255+B=海区编号*255*100
		String seaName = (String)seaIDNameMap.get(seaID);
		if (seaName!=null && !seaName.isEmpty()){
			String res = seaID + "|" + seaName;
			return res;
		}
		return null;
	}
	
	/**
	 * 判断一个点位是否在海上
	 * @param lat 点的纬度坐标
	 * @param lon 点的经度坐标
	 * @return
	 */
	public static boolean isOcean(double lat, double lon) {
		if(lon > 180){
			lon = lon - 360;
		}
		if(isFilterLand(lat, lon)){
			return false;
		}else{
			if (varRGB == null) {
				varRGB = new byte[3600][7200 / 8];
				DataInputStream opt = new DataInputStream(GlobeTerrianTool.class
						.getResourceAsStream(terrainFilePath));
				for (int i = 0; i < 3600; i++) {
					try {
						for (int j = 0; j < 900; j++) {
							byte a = opt.readByte();
							varRGB[i][j] = a;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			int row = (int) ((lat - (-90)) / 180 * 3599);
			int col = (int) ((lon - (-180)) / 360 * 899);
			int byteIndex = col % 8;
			byte v = varRGB[row][col];
			byte vByte = (byte) Math.pow(2, byteIndex);

			byte c = (byte) (v & vByte);

			if (c == 0) {
				return false;
			} else {
				return true;
			}
		}
	}
	
	/**
	 * @param point 点
	 * @return
	 */
	public static boolean isOcean(Position point) {
		return isOcean(point.getLatitude().degrees, point.getLongitude().degrees);
	}
		
	/**
	 * 判断一个区域是否在海上(将一个区域用九个等分点表示 如果超过一半的点在海上的判定该区域为海区)
	 * @param sLat 起始纬度坐标
	 * @param eLat 终止纬度坐标
	 * @param sLon 起始经度坐标
	 * @param eLon 终止纬度坐标
	 * @return
	 */
	public static boolean isOcean(double sLat, double eLat, double sLon, double eLon ) {
		int number = 0;
		//防止在调用isOcean(double double)时数组溢出 2014-6-12
		sLat = sLat < -90 ? -90: sLat;
		eLat = eLat > 90 ? 90: eLat;
		sLon = sLon < -180 ? -180: sLon;
		eLon = eLon > 180 ? 180: eLon;
		if (isOcean(sLat,sLon)) number ++;
		if (isOcean(( sLat + eLat) / 2, sLon)) number ++;
		if (isOcean(eLat, sLon)) number ++;
		if (isOcean(sLat,( sLon + eLon) / 2)) number ++;
		if (isOcean(( sLat + eLat) / 2,( sLon + eLon) / 2)) number ++;
		if (number > 4)
			return true;
		if (isOcean(eLat, ( sLon + eLon) / 2)) number ++;
		if (number > 4)
			return true;
		if (isOcean(sLat,eLon)) number ++;
		if (number > 4)
			return true;
		if (isOcean(( sLat + eLat) / 2, eLon)) number ++;
		if (number > 4)
			return true;
		if (isOcean(eLat, eLon)) number ++;
		if (number > 4)
			return true;
		return false;
	}

		
		/**
		 * 判断一个区域(以lat、lon为中心,radius为半径)是否在海上
		 * @param lat
		 * @param lon
		 * @param radius
		 * @return
		 */
		public static boolean isOcean(double lat, double lon, double radius) {
			return isOcean(lat - radius, lat + radius, lon - radius, lon + radius);
		}
		
//		/**
//		 * 判断一个自由区域是否在海上
//		 * @param area
//		 * @return
//		 */
//		public static boolean isOcean(FreeArea area) {
//			return isOcean(area.getsLat(), area.geteLat(), area.getsLon(), area.geteLon());
//		}
		/**
		 * 判断一个区域是否在海面某一深度之下 (将一个区域用九个等分点表示 如果超过一半的点在海上的判定该区域为海区)
		 * @param depthRuler 判断水深的标度(大于ruler 返回false)
		 * @param sLat 起始纬度坐标
		 * @param eLat 终止纬度坐标
		 * @param sLon 起始经度坐标
		 * @param eLon 终止纬度坐标
		 * @return
		 */
//		public static boolean isUnderSea(WorldWindow wwd, double depthRuler, double sLat, double eLat, double sLon, double eLon ) {
//			int number = 0;
//			double tempDepth = wwd.getModel().getGlobe().getElevation(
//					 Angle.fromDegreesLatitude(sLat), Angle.fromDegreesLongitude(sLon));
//			if (tempDepth < depthRuler) number ++;
//			tempDepth = wwd.getModel().getGlobe().getElevation(
//					 Angle.fromDegreesLatitude(( sLat + eLat) / 2), Angle.fromDegreesLongitude(sLon));
//			if (tempDepth < depthRuler) number ++;
//			tempDepth = wwd.getModel().getGlobe().getElevation(
//					 Angle.fromDegreesLatitude(eLat), Angle.fromDegreesLongitude(sLon));
//			if (tempDepth < depthRuler) number ++;
//			tempDepth = wwd.getModel().getGlobe().getElevation(
//					 Angle.fromDegreesLatitude(sLat), Angle.fromDegreesLongitude(( sLon + eLon) / 2));
//			if (tempDepth < depthRuler) number ++;
//			tempDepth = wwd.getModel().getGlobe().getElevation(
//					 Angle.fromDegreesLatitude(( sLat + eLat) / 2), Angle.fromDegreesLongitude(( sLon + eLon) / 2));
//			if (tempDepth < depthRuler) number ++;
//			if (number > 4)
//				return true;
//			tempDepth = wwd.getModel().getGlobe().getElevation(
//					 Angle.fromDegreesLatitude(eLat), Angle.fromDegreesLongitude(( sLon + eLon) / 2));
//			if (tempDepth < depthRuler) number ++;
//			if (number > 4)
//				return true;
//			tempDepth = wwd.getModel().getGlobe().getElevation(
//					 Angle.fromDegreesLatitude(sLat), Angle.fromDegreesLongitude(eLon));
//			if (tempDepth < depthRuler) number ++;
//			if (number > 4)
//				return true;
//			tempDepth = wwd.getModel().getGlobe().getElevation(
//					 Angle.fromDegreesLatitude(( sLat + eLat) / 2), Angle.fromDegreesLongitude(eLon));
//			if (tempDepth < depthRuler) number ++;
//			if (number > 4)
//				return true;
//			tempDepth = wwd.getModel().getGlobe().getElevation(
//					 Angle.fromDegreesLatitude(eLat), Angle.fromDegreesLongitude(eLon));
//			if (tempDepth < depthRuler) number ++;
//			if (number > 4)
//				return true;
//			return false;
//		}
		/**
		 * 判断一个区域以(lat、lon为中心,radius为半径)是否在海面某一深度之下 
		 * @param depthRuler 判断水深的标度(大于ruler 返回false)
		 * @param lat
		 * @param lon
		 * @param radius
		 * @return
		 */
//		public static boolean isUnderSea(WorldWindow wwd, double depthRuler, double lat, double lon, double radius) {
//			return isUnderSea(wwd, depthRuler, lat - radius, lat + radius, lon - radius, lon + radius);
//		}
		
		/**
		 * 判断一个在中国沿海附近的点位是否在海上
		 * @param lat 点的纬度坐标
		 * @param lon 点的经度坐标
		 * @return
		 */
		public static boolean isOceanAlongChina(double lat, double lon) {
			if(lon > 180){
				lon = lon - 360;
			}
			if(isFilterLand(lat, lon)){
				return false;
			}else{
				if (varRGB == null) {
					varRGB = new byte[4700][4700 / 8 + 1];
					DataInputStream opt = new DataInputStream(GlobeTerrianTool.class
							.getResourceAsStream(terrainFileAlongChinaPath));
					for (int i = 0; i < 4700; i++) {
						try {
							for (int j = 0; j < 4700 / 8 + 1; j++) {
								byte a = opt.readByte();
								varRGB[i][j] = a;
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				int row = (int) ((lat - (-90)) / 180 * 4699);
				int col = (int) ((lon - (-180)) / 360 * (4700 / 8));
				int byteIndex = col % 8;
				byte v = varRGB[row][col];
				byte vByte = (byte) Math.pow(2, byteIndex);

				byte c = (byte) (v & vByte);

				if (c == 0) {
					return false;
				} else {
					return true;
				}
			}
		}
		static private byte[] ChinaFlagBuffer;
		/**
		 * 判断一个给定的经纬度点是否在中国区范围内,分辨率为0.1°。
		 * 注意：如果读取标志文件失败，则返回true
		 * @param lat
		 * @param lon
		 * @return
		 */
		public static boolean isChinaPosition(double lat,double lon){
			boolean IsChina=false;
			if(null==ChinaFlagBuffer){
				readChinaFlagBufferCache();
			}
			if(null==ChinaFlagBuffer){
				return true; //如果中国区标志文件读取失败，则这里返回true，防止依据该标志进行过滤数据的程序出错
			}
			
			double Olat=90,Olon=-180;//原点
			
			double delLat=(Olat-lat),
			       delLon=(lon-Olon);
			
			int INTERVAL = 10; //分辨率为0.1度
			int colLength=3600;// 总列数
			
			int row=Double.valueOf(Math.round(delLat*INTERVAL)).intValue();
			int col=Double.valueOf(Math.round(delLon*INTERVAL)).intValue();
			
			int bitPY=row*colLength+col;//  总体位偏移

			int byteIndex=bitPY%8>0?bitPY/8+1:bitPY/8;//字节索引
			/**某一字节中的的位偏移*/
			int bitPY2=bitPY%8;
			
			if(byteIndex>ChinaFlagBuffer.length-1){
				return IsChina;
			}
			
			byte byteValue=ChinaFlagBuffer[byteIndex];
			
			if (1 == (((byteValue&0xff)>> (8 - bitPY2)) & 0x1)){
				IsChina=true;
			}

			return IsChina;
		}

		static private void readChinaFlagBufferCache() {
			try {
				String filePath = "config\\GridChina0.1small.dat";
				FileInputStream iStream = new FileInputStream(new File(filePath));
				ChinaFlagBuffer = new byte[360*10*180*10];
				iStream.read(ChinaFlagBuffer);				
				iStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	public static  void main(String[] args){

//		System.out.println(getChinaSeaIDName(15, 112)); //15号海区
//		System.out.println(getChinaSeaIDName(13, 116)); //14号海区
//		System.out.println(getChinaSeaIDName(26, 122)); //6号海区
//		System.out.println(getChinaSeaIDName(10, 125)); //null
//		System.out.println(getChinaSeaIDName(38, 120)); //1号海区
//		System.out.println(getChinaSeaIDName(30, 125)); //5,19,20号海区
		 System.out.println(getChinaSeaIDName(33.75f, 122.5f)); //5,19,20号海区

		//1.读全球图片，并将读取结果写入到二进制文件中globeterrian2.dat中
//		File f = new File("d:/globeterrian2.bmp");
//		try {
//			BufferedImage bi = ImageIO.read(f);
//			String filePath = "d:/globeterrian2.dat";
//			DataOutputStream oStream = new DataOutputStream(new FileOutputStream(filePath));
//			for (int i = 0; i<3600; i++){
//				for (int j = 0; j<1800; j++){
//					int clr = bi.getRGB(i,j);
//					int R = (clr & 0xff0000) >> 16;
//					int G = (clr & 0xff00) >> 8;
//					int B = (clr & 0xff);
//					int res = B*256*256+G*256+R;
//					if (R != 0 && R!=255)
//						System.out.println(R+","+G+","+B+","+res);
//
//					oStream.writeInt(res);
//				}
//			}
//			oStream.close();
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		//1.读中国沿海(100-150lng, 0-50lat)图片，并将读取结果写入到二进制文件中globeterrian2.dat中
//		File f = new File("d:/chinaseaterrian.bmp");
//		try {
//			BufferedImage bi = ImageIO.read(f);
//			String filePath = "d:/chinaseaterrian.dat";
//			DataOutputStream oStream = new DataOutputStream(new FileOutputStream(filePath));
//			for (int i = 0; i<500; i++){
//				for (int j = 0; j<500; j++){
//					int clr = bi.getRGB(i,j);
//					int R = (clr & 0xff0000) >> 16;
//					int G = (clr & 0xff00) >> 8;
//					int B = (clr & 0xff);
//					int res = B*256*256+G*256+R;
//					if (R != 0 && R!=255)
//						System.out.println(R+","+G+","+B+","+res);
//					oStream.writeInt(res);
//				}
//			}
//			oStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}



		//2.写图片
		BufferedImage img = new BufferedImage(3600, 1800, BufferedImage.TYPE_3BYTE_BGR);//
		byte[][] rgbarr=  new byte[3600][7200 / 8];
		DataInputStream opt = null;
		try {
			opt = new DataInputStream(new FileInputStream("d:/terrainRGB.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			for (int i = 0; i < 3600; i++) {

				for (int j = 0; j < 900; j++) {
					byte a = opt.readByte();

					rgbarr[i][j] = a;
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
//
//		float lat;
//		float lng = -180.0f;
//		for (int i = 0; i < 3600; i++){
//			lat=90.0f;
//			for (int j = 0; j < 1800; j++){
//
//				int row = (int) ((lat - (-90)) / 180 * 3599);
//				int col = (int) ((lng - (-180)) / 360 * 899);
//				if (col>=900){
//					col=899;
//					System.out.println("a");
//				}
//				if (row>=3600){
//					row=3599;
//					System.out.println("b");
//				}
//				int byteIndex = col % 8;
//				byte v = rgbarr[row][col];
//				byte vByte = (byte) Math.pow(2, byteIndex);
//
//				byte c = (byte) (v & vByte);
//				if (c == 0) {
//					img.setRGB(i, j, 255);
//				} else {
//					img.setRGB(i, j, 0);
//				}
//
//				lat-=0.1f;
//
//			}
//			lng+=0.1f;
//		}

	}



}
