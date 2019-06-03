package net.xinhong.meteoserve.common.cloudy;




import java.io.*;
import java.nio.ByteBuffer;

public class YtNCSNFileParser extends YtFileParser {

	private int year = 0;
	private int month = 0;
	private int day = 0;
	private int hour = 0;
	private int minute = 0;

	private String fileName;
	public YtNCSNFileParser(){

	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public int getDay() {
		return day;
	}

	public int getHour() {
		return hour;
	}

	public int getMinute() {
		return minute;
	}
	public String getFileName() {
		return fileName;
	}
	public YtData readNCSNfile(String filePath) throws FileNotFoundException {
		YtData ytData = new YtData();
		ytData.setYtFileName(filePath);
		File file = new File(filePath);
		if(file==null || !file.exists()){
			throw new FileNotFoundException(filePath);
		}
		this.fileName = file.getName();
		InputStream in=null;
		try {
			 in = new FileInputStream(file) ;
			ByteBuffer byteBuffer = FileContentGetter.readStreamToBuffer(in, true);

			byte[] b2 = new byte[dataLength];
			byte[] b12 = new byte[12];
			byte[] b8 = new byte[8];
			while (byteBuffer.hasRemaining()) {
				// 1-12 SAT96文件名
				byteBuffer.get(b12);
				String SAT96filename = BasicDataTypeConversion.byteToChar(b12);
				// 13-14 Int16整型数的字节顺序
				byteBuffer.get(b2);
				int bytesq = BasicDataTypeConversion.byteToInt(b2);
				// 15-16 Int16 第一级文件头长度
				byteBuffer.get(b2);
				int flength = BasicDataTypeConversion.byteToInt(b2);
				// 17-18 Int16第二级文件头长度
				byteBuffer.get(b2);
				int slength = BasicDataTypeConversion.byteToInt(b2);

				// 19-20 Int16填充段数据长度
				byteBuffer.get(b2);
				int dLength = BasicDataTypeConversion.byteToInt(b2);

				// 21-22 Int16记录长度
				byteBuffer.get(b2);
				int codeLength = BasicDataTypeConversion.byteToInt(b2);
				// 23-24 Int16文件头占用记录数
				byteBuffer.get(b2);
				int tcodeCnt = BasicDataTypeConversion.byteToInt(b2);

				// 25-26 Int16产品数据占用记录数
				byteBuffer.get(b2);
				int dcodeCnt = BasicDataTypeConversion.byteToInt(b2);

				// 27-28 Int16产品类别
				byteBuffer.get(b2);
				int pType = BasicDataTypeConversion.byteToInt(b2);

				// 29-30 Int16压缩方式
				byteBuffer.get(b2);
				int comType = BasicDataTypeConversion.byteToInt(b2);

				// 31-38 Char*8格式说明字串
				byteBuffer.get(b8);
				String format = BasicDataTypeConversion.byteToChar(b8);

				// 39-40 Int16产品数据质量标记
				byteBuffer.get(b2);
				int xSolution = BasicDataTypeConversion.byteToInt(b2);

				// 41-48 Char*8卫星名
				byteBuffer.get(b8);
				String spName = BasicDataTypeConversion.byteToChar(b8);

				// 49-50 Int16 时间（年）
				byteBuffer.get(b2);
				year = BasicDataTypeConversion.byteToInt(b2);

				// 51-52 Int16 时间（月）
				byteBuffer.get(b2);
				month = BasicDataTypeConversion.byteToInt(b2);

				// 53-54 Int16 时间（日）
				byteBuffer.get(b2);
				day = BasicDataTypeConversion.byteToInt(b2);

				// 55-56 Int16 时间（时）
				byteBuffer.get(b2);
				hour = BasicDataTypeConversion.byteToInt(b2);

				// 57-58 Int16 时间（分）
				byteBuffer.get(b2);
				minute = BasicDataTypeConversion.byteToInt(b2);

				// 59-60 Int16 通道号
				byteBuffer.get(b2);
				int channelCode = BasicDataTypeConversion.byteToInt(b2);

				// 61-62 Int16 投影方式
				byteBuffer.get(b2);
				int projection = BasicDataTypeConversion.byteToInt(b2);
				ytData.setProjection(projection);


				// 63-64 Int16图像宽度
				byteBuffer.get(b2);
				int imageWidth = BasicDataTypeConversion.byteToInt(b2);
				ytData.setImageW(imageWidth);
				ytData.setRowNum(imageWidth);

				// 65-66 Int16图像高度
				byteBuffer.get(b2);
				int imageHeight = BasicDataTypeConversion.byteToInt(b2);
				ytData.setImageH(imageHeight);
				ytData.setColNum(imageHeight);

				// 67-68 Int16 图象左上角扫描线号
				byteBuffer.get(b2);
				int zsXianCode = BasicDataTypeConversion.byteToInt(b2);

				// 69-70 Int16 图象左上角扫描元号
				byteBuffer.get(b2);
				int zsYuanCode = BasicDataTypeConversion.byteToInt(b2);

				// 71-72 Int16抽样率
				byteBuffer.get(b2);
				int hits = BasicDataTypeConversion.byteToInt(b2);
				ytData.setHits(hits);

				// 73-74 Int16地理范围（北纬）
				byteBuffer.get(b2);
				float sLat = BasicDataTypeConversion.byteToInt(b2)/ 100;
				ytData.setYEnd(sLat);

				// 75-76 Int16地理范围（南纬）
				byteBuffer.get(b2);
				float eLat = BasicDataTypeConversion.byteToInt(b2)/100;
				ytData.setYStart(eLat);

				// 77-78 Int16地理范围（西经）
				byteBuffer.get(b2);
				float sLon = BasicDataTypeConversion.byteToInt(b2)/ 100;
				ytData.setXStart(sLon);

				// 79-80 Int16地理范围（东经）
				byteBuffer.get(b2);
				float eLon = BasicDataTypeConversion.byteToInt(b2) / 100;
				ytData.setXEnd(eLon);

				int latCnt = (int) Math.abs(eLat  - sLat );

				float xdel = Math.abs(ytData.getYEnd() - ytData.getYStart())/(ytData.getRowNum() -1);
				ytData.setXDel(xdel);

				int lonCnt = (int) Math.abs(eLon - sLon);

				float ydel = Math.abs(ytData.getXEnd() - ytData.getXStart())/(ytData.getColNum() -1);
				ytData.setXDel(ydel);

				// 81-82 Int16 投影中心纬度
				byteBuffer.get(b2);
				float cLat = BasicDataTypeConversion.byteToInt(b2)/ 100;
				ytData.setcLat(cLat);

				// 83-84 Int16 投影中心经度
				byteBuffer.get(b2);
				float cLon = BasicDataTypeConversion.byteToInt(b2)/ 100;
				ytData.setcLon(cLon);

				// 85-86 Int16 投影标准纬度1（或标准经度）
				byteBuffer.get(b2);
				float standard1 = BasicDataTypeConversion.byteToInt(b2)/ 100;
				ytData.setStandard1(standard1);

				// 87-88 Int16 投影标准纬度2（或标准经度）
				byteBuffer.get(b2);
				float standard2 = BasicDataTypeConversion.byteToInt(b2)/ 100;
				ytData.setStandard2(standard2);

				// 89-90 Int16 投影水平分辨率
				byteBuffer.get(b2);
				float hresolution = BasicDataTypeConversion.byteToInt(b2)/ 100;
				ytData.setHresolution(hresolution);

				// 91-92 Int16 投影垂直分辨率
				byteBuffer.get(b2);
				float vresolution = BasicDataTypeConversion.byteToInt(b2)/ 100;
				ytData.setVresolution(vresolution);

				// 93-94 Int16 地理网格叠加标志
				byteBuffer.get(b2);
				int djbz = BasicDataTypeConversion.byteToInt(b2);

				// 95-96 Int16 地理网格叠加值
				byteBuffer.get(b2);
				int djvalue = BasicDataTypeConversion.byteToInt(b2);

				// 97-98 Int16 调色表数据块长度
				byteBuffer.get(b2);
				int tsLength = BasicDataTypeConversion.byteToInt(b2);

				// 99-100 Int16 定标数据块长度
				byteBuffer.get(b2);
				int bsjkLength = BasicDataTypeConversion.byteToInt(b2);

				// 101-102 Int16 定位数据块长度
				byteBuffer.get(b2);
				int wsjkLength = BasicDataTypeConversion.byteToInt(b2);

				// 103-104 Int16 保留
				byteBuffer.get(b2);
				int blLength = BasicDataTypeConversion.byteToInt(b2);

//				//调色表数据块长度数据
//				for(int i=0; i<tsLength; i++){
//
//				}
//				//定标数据块长度
//				for(int i=0; i<bsjkLength; i++){
//					for (int j = 0; j < 4; j++) {
//						b4[j] = byteBuffer.get();
//					}
//					float dd = BasicDataTypeConversion.byteToFloat(b4,0);
//					ytData.scalerData[i] = dd;
//				}
////				System.out.println();
//				//定位数据块长度
//				for(int i=0; i<wsjkLength; i++){
//
//				}//保留
//				for(int i=0; i<blLength; i++){
//
//				}
				//259K文件从第七行开始是数据信息，其他文件从第三行开始
				int titleLineNum = 3;
				long totalLength = file.length();
				if (file.length() ==  265216) {
					titleLineNum = 6;
				}else if (totalLength == 2455256L || totalLength == 26891200L) {
					titleLineNum = 2;
				}

				int length = (titleLineNum*ytData.getColNum() - 104);
				if(length < 0) length = 0;

				byte[] tmp = new byte[length];
				byteBuffer.get(tmp);

				byte[] data = new byte[ytData.getRowNum() * ytData.getColNum()];
				byteBuffer.get(data);
				ytData.setYtData(data);

//			System.out.println("整型数的字节顺序 ： " + bytesq);
//			System.out.println(SAT96filename);
//			System.out.println("第一级文件头长度: " + flength);
//			System.out.println("第二级文件头长度: " + slength);
//			System.out.println("填充段数据长度: " + dLength);
//			System.out.println("记录长度: " + codeLength);
//			System.out.println("文件头占用记录数: " + tcodeCnt);
//			System.out.println("产品数据占用记录数: " + dcodeCnt);
//			System.out.println("产品类别: " + pType);
//			System.out.println("压缩方式: " + comType);
//			System.out.println("格式说明字串: " + format);
//			System.out.println("产品数据质量标记: " + xSolution);
//			System.out.println("卫星名: " + spName);
//			System.out.println("时间（年）: " + year);
//			System.out.println("时间（月）: " + month);
//			System.out.println("时间（日）: " + day);
//			System.out.println("时间（时）: " + hour);
//			System.out.println("时间（分）: " + minute);
//			System.out.println("通道号: " + channelCode);
//			System.out.println("投影方式: " + projection);
//			System.out.println("图像宽度: " + imageWidth);
//			System.out.println("图像高度: " + imageHeight);
//			System.out.println("图象左上角扫描线号: " + zsXianCode);
//			System.out.println("图象左上角扫描元号: " + zsYuanCode);
//			System.out.println("抽样率: " + hits);
//			System.out.println("地理范围（北纬）: " + sLat );
//			System.out.println("地理范围（南纬）: " + eLat);
//			System.out.println("地理范围（西经）: " + sLon );
//			System.out.println("地理范围（东经）: " + eLon);
//			System.out.println("lat的个数为：" + latCnt);
//			System.out.println("x间隔：" + xdel);
//			System.out.println("lon的个数为：" + lonCnt);
//			System.out.println("y间隔：" + ydel);
//			System.out.println("投影中心纬度: " + cLat );
//			System.out.println("投影中心经度: " + cLon );
//			System.out.println("投影标准纬度1（或标准经度）: " +  standard1 );
//			System.out.println("投影标准纬度2（或标准经度）: " + standard2);
//			System.out.println("投影水平分辨率: " + hresolution );
//			System.out.println("投影垂直分辨率: " + vresolution );
//			System.out.println("地理网格叠加标志: " + djbz);
//			System.out.println("地理网格叠加值: " + djvalue);
//			System.out.println("调色表数据块长度: " + tsLength);
//			System.out.println("定标数据块长度: " + bsjkLength);
//			System.out.println("定位数据块长度: " + wsjkLength);
//			System.out.println("保留: " + blLength);
				break;
			}// end while
			return ytData;
		}catch(FileNotFoundException e){
			e.printStackTrace();
			return null;
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}finally {
			try {
				if(in!=null){
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


//	public YtData readNCSNfile(String filePath) {
//		YtData ytData = new YtData();
//		File file = new File(filePath);
//		try {
//			InputStream in = new FileInputStream(file) ;
////			ByteBuffer byteBuffer = GridFileParser.readStreamToBuffer(in, true);
//			ByteBuffer byteBuffer = FileContentGetter.readStreamToBuffer(in, true);
//
//			byte[] b2 = new byte[dataLength];
//			byte[] b12 = new byte[12];
//			byte[] b8 = new byte[8];
//			byte[] b4 = new byte[4];
//			while (byteBuffer.hasRemaining()) {
//				// 1-12 SAT96文件名
//				for (int i = 0; i < 12; i++)
//					b12[i] = byteBuffer.get();
//				String SAT96filename = BasicDataTypeConversion.byteToChar(b12);
//				// 13-14 Int16整型数的字节顺序
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int bytesq = BasicDataTypeConversion.byteToInt(b2);
//				// 15-16 Int16 第一级文件头长度
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int flength = BasicDataTypeConversion.byteToInt(b2);
//				// 17-18 Int16第二级文件头长度
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int slength = BasicDataTypeConversion.byteToInt(b2);
//
//				// 19-20 Int16填充段数据长度
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int dLength = BasicDataTypeConversion.byteToInt(b2);
//
//				// 21-22 Int16记录长度
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int codeLength = BasicDataTypeConversion.byteToInt(b2);
//				// 23-24 Int16文件头占用记录数
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int tcodeCnt = BasicDataTypeConversion.byteToInt(b2);
//
//				// 25-26 Int16产品数据占用记录数
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int dcodeCnt = BasicDataTypeConversion.byteToInt(b2);
//
//				// 27-28 Int16产品类别
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int pType = BasicDataTypeConversion.byteToInt(b2);
//
//				// 29-30 Int16压缩方式
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int comType = BasicDataTypeConversion.byteToInt(b2);
//
//				// 31-38 Char*8格式说明字串
//				for (int i = 0; i < 8; i++) {
//					b8[i] = byteBuffer.get();
//				}
//				String format = BasicDataTypeConversion.byteToChar(b8);
//
//				// 39-40 Int16产品数据质量标记
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int xSolution = BasicDataTypeConversion.byteToInt(b2);
//
//				// 41-48 Char*8卫星名
//				for (int i = 0; i < 8; i++) {
//					b8[i] = byteBuffer.get();
//				}
//				String spName = BasicDataTypeConversion.byteToChar(b8);
//
//				// 49-50 Int16 时间（年）
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int year = BasicDataTypeConversion.byteToInt(b2);
//
//				// 51-52 Int16 时间（月）
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int month = BasicDataTypeConversion.byteToInt(b2);
//
//				// 53-54 Int16 时间（日）
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int day = BasicDataTypeConversion.byteToInt(b2);
//
//				// 55-56 Int16 时间（时）
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int hour = BasicDataTypeConversion.byteToInt(b2);
//
//				// 57-58 Int16 时间（分）
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int minute = BasicDataTypeConversion.byteToInt(b2);
//
//				// 59-60 Int16 通道号
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int channelCode = BasicDataTypeConversion.byteToInt(b2);
//
//				// 61-62 Int16 投影方式
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int projection = BasicDataTypeConversion.byteToInt(b2);
//				ytData.setProjection(projection);
//
//
//				// 63-64 Int16图像宽度
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int imageWidth = BasicDataTypeConversion.byteToInt(b2);
//				ytData.setImageW(imageWidth);
//				ytData.setRowNum(imageWidth);
//
//				// 65-66 Int16图像高度
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int imageHeight = BasicDataTypeConversion.byteToInt(b2);
//				ytData.setImageH(imageHeight);
//				ytData.setColNum(imageHeight);
//
//				// 67-68 Int16 图象左上角扫描线号
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int zsXianCode = BasicDataTypeConversion.byteToInt(b2);
//
//				// 69-70 Int16 图象左上角扫描元号
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int zsYuanCode = BasicDataTypeConversion.byteToInt(b2);
//
//				// 71-72 Int16抽样率
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int hits = BasicDataTypeConversion.byteToInt(b2);
//				ytData.setHits(hits);
//
//				// 73-74 Int16地理范围（北纬）
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				float sLat = BasicDataTypeConversion.byteToInt(b2)/ 100;
//				ytData.setYEnd(sLat);
//
//				// 75-76 Int16地理范围（南纬）
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				float eLat = BasicDataTypeConversion.byteToInt(b2)/100;
//				ytData.setYStart(eLat);
//
//				// 77-78 Int16地理范围（西经）
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				float sLon = BasicDataTypeConversion.byteToInt(b2)/ 100;
//				ytData.setXStart(sLon);
//
//				// 79-80 Int16地理范围（东经）
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				float eLon = BasicDataTypeConversion.byteToInt(b2) / 100;
//				ytData.setXEnd(eLon);
//
//				int latCnt = (int) Math.abs(eLat  - sLat );
//
//				float xdel = Math.abs(ytData.getYEnd() - ytData.getYStart())/(ytData.getRowNum() -1);
//				ytData.setXDel(xdel);
//
//				int lonCnt = (int) Math.abs(eLon - sLon);
//
//				float ydel = Math.abs(ytData.getXEnd() - ytData.getXStart())/(ytData.getColNum() -1);
//				ytData.setXDel(ydel);
//
//				// 81-82 Int16 投影中心纬度
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				float cLat = BasicDataTypeConversion.byteToInt(b2)/ 100;
//				ytData.setcLat(cLat);
//
//				// 83-84 Int16 投影中心经度
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				float cLon = BasicDataTypeConversion.byteToInt(b2)/ 100;
//				ytData.setcLon(cLon);
//
//				// 85-86 Int16 投影标准纬度1（或标准经度）
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				float standard1 = BasicDataTypeConversion.byteToInt(b2)/ 100;
//				ytData.setStandard1(standard1);
//
//				// 87-88 Int16 投影标准纬度2（或标准经度）
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				float standard2 = BasicDataTypeConversion.byteToInt(b2)/ 100;
//				ytData.setStandard2(standard2);
//
//				// 89-90 Int16 投影水平分辨率
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				float hresolution = BasicDataTypeConversion.byteToInt(b2)/ 100;
//				ytData.setHresolution(hresolution);
//
//				// 91-92 Int16 投影垂直分辨率
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				float vresolution = BasicDataTypeConversion.byteToInt(b2)/ 100;
//				ytData.setVresolution(vresolution);
//
//				// 93-94 Int16 地理网格叠加标志
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int djbz = BasicDataTypeConversion.byteToInt(b2);
//
//				// 95-96 Int16 地理网格叠加值
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int djvalue = BasicDataTypeConversion.byteToInt(b2);
//
//				// 97-98 Int16 调色表数据块长度
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int tsLength = BasicDataTypeConversion.byteToInt(b2);
//
//				// 99-100 Int16 定标数据块长度
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int bsjkLength = BasicDataTypeConversion.byteToInt(b2);
//
//				// 101-102 Int16 定位数据块长度
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int wsjkLength = BasicDataTypeConversion.byteToInt(b2);
//
//				// 103-104 Int16 保留
//				for (int i = 0; i < dataLength; i++) {
//					b2[i] = byteBuffer.get();
//				}
//				int blLength = BasicDataTypeConversion.byteToInt(b2);
//
////				//调色表数据块长度数据
////				for(int i=0; i<tsLength; i++){
////
////				}
////				//定标数据块长度
////				for(int i=0; i<bsjkLength; i++){
////					for (int j = 0; j < 4; j++) {
////						b4[j] = byteBuffer.get();
////					}
////					float dd = BasicDataTypeConversion.byteToFloat(b4,0);
////					ytData.scalerData[i] = dd;
////				}
//////				System.out.println();
////				//定位数据块长度
////				for(int i=0; i<wsjkLength; i++){
////
////				}//保留
////				for(int i=0; i<blLength; i++){
////
////				}
//				//从第七行开始是数据信息
//				int length = 6*ytData.getColNum();
//				byte[] tmp = new byte[length];
//				byteBuffer.get(tmp);
//
//				byte[] data = new byte[ytData.getRowNum() * ytData.getColNum()];
//				byteBuffer.get(data);
//				ytData.setYtData(data);
//
//				// //105开始 云图数据位置
////				System.out.println("整型数的字节顺序 ： " + bytesq);
////				System.out.println(SAT96filename);
////				System.out.println("第一级文件头长度: " + flength);
////				System.out.println("第二级文件头长度: " + slength);
////				System.out.println("填充段数据长度: " + dLength);
////				System.out.println("记录长度: " + codeLength);
////				System.out.println("文件头占用记录数: " + tcodeCnt);
////				System.out.println("产品数据占用记录数: " + dcodeCnt);
////				System.out.println("产品类别: " + pType);
////				System.out.println("压缩方式: " + comType);
////				System.out.println("格式说明字串: " + format);
////				System.out.println("产品数据质量标记: " + xSolution);
////				System.out.println("卫星名: " + spName);
////				System.out.println("时间（年）: " + year);
////				System.out.println("时间（月）: " + month);
////				System.out.println("时间（日）: " + day);
////				System.out.println("时间（时）: " + hour);
////				System.out.println("时间（分）: " + minute);
////				System.out.println("通道号: " + channelCode);
////				System.out.println("投影方式: " + projection);
////				System.out.println("图像宽度: " + imageWidth);
////				System.out.println("图像高度: " + imageHeight);
////				System.out.println("图象左上角扫描线号: " + zsXianCode);
////				System.out.println("图象左上角扫描元号: " + zsYuanCode);
////				System.out.println("抽样率: " + hits);
////				System.out.println("地理范围（北纬）: " + sLat );
////				System.out.println("地理范围（南纬）: " + eLat);
////				System.out.println("地理范围（西经）: " + sLon );
////				System.out.println("地理范围（东经）: " + eLon);
////				System.out.println("lat的个数为：" + latCnt);
////				System.out.println("x间隔：" + xdel);
////				System.out.println("lon的个数为：" + lonCnt);
////				System.out.println("y间隔：" + ydel);
////				System.out.println("投影中心纬度: " + (float) cLat / 100);
////				System.out.println("投影中心经度: " + (float) cLon / 100);
////				System.out.println("投影标准纬度1（或标准经度）: " +  standard1 );
////				System.out.println("投影标准纬度2（或标准经度）: " + standard2);
////				System.out.println("投影水平分辨率: " + hresolution );
////				System.out.println("投影垂直分辨率: " + vresolution );
////				System.out.println("地理网格叠加标志: " + djbz);
////				System.out.println("地理网格叠加值: " + djvalue);
////				System.out.println("调色表数据块长度: " + tsLength);
////				System.out.println("定标数据块长度: " + bsjkLength);
////				System.out.println("定位数据块长度: " + wsjkLength);
////				System.out.println("保留: " + blLength);
//
////				System.out.println("数据 ：");
////				int dataLength = ytData.getRowNum() * ytData.getColNum();
//				int dataLength = ytData.getImageH() * ytData.getImageW();
//				ytData.ytData = new byte[dataLength];
//				ByteBuffer bb = byteBuffer.get(ytData.ytData, 0, dataLength);
////				int[][] dataAry = new int[ytData.getImageH()][ytData.getImageW()];
//				// for(int i=0; i<ytData.getImageH(); i++){
//				// for(int j=0; j<ytData.getImageW(); j++){
//				// b1[0] = byteBuffer.get();
//				// int data = byteToInt(b1[0]);
//				// dataAry[i][j] = data;
//				// // System.out.print(data + ",");
//				// }
//				// // System.out.println();
//				// }
//				break;
//			}// end while
//			return ytData;
//		}catch(FileNotFoundException e){
//			return null;
//		}catch(IOException e){
//			return null;
//		}
//	}
}
