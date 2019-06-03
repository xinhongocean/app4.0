package net.xinhong.meteoserve.common.cloudy;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于生成空军云图图片BufferedImage
 * 
 * @author dyh
 * 
 */

public class CloudPictrueDataRead
{
	
	/**
	 * @param ytData          云图数据
	 * @param paletteFilePath 调色板文件路径
	 * @param alphaList       alpha数组
	 * 
	 * @return 云图图片的BufferedImage
	 */
	public static BufferedImage readColorBoardFile(YtData ytData, String paletteFilePath, int alphaList[])
	{
		if (ytData == null || ytData.isEmpty())
			return null;
		try
		{
			// 调色板文件
			InputStream iStream = CloudPictrueDataRead.class.getResourceAsStream(paletteFilePath);
			//InputStream iStream = ClassLoader.getSystemResourceAsStream(paletteFilePath);
			if (iStream == null)
			{
				File file = new File(paletteFilePath);
				if (file.exists())
				{
					iStream = new FileInputStream(file);
				}
			}
			InputStreamReader isr = new InputStreamReader(iStream, "UTF-8");
			BufferedReader bf = new BufferedReader(isr);
			
			List<String> allColorList = new ArrayList<String>();	// 存储调色板中所有数据
			List<Integer> indexList = new ArrayList<Integer>();		// 存储调色板中RGB的KEY
			List<ColorPrj> colorDataList = new ArrayList<ColorPrj>();
			String line = null;
			while ((line = bf.readLine()) != null)
			{
				String[] arrs = line.trim().split("\\s+");
				if(arrs.length != 4)
				{
					continue;
				}
				
				for(String tempstr: arrs)
				{
					allColorList.add(tempstr);
				}
			}
			
			for (int i = 0; i < allColorList.size(); i += 4)
			{
				indexList.add(Integer.parseInt(allColorList.get(i)));
			}
			if( alphaList != null)
			{
				//调色板中Alpha值是随着数组变化的
				for(int i = 0; i < indexList.size(); i++)
				{
					// 将调色板中的每一个RGB实例化为一个颜色对象
					colorDataList.add(new ColorPrj(alphaList[i], Integer.parseInt(allColorList.get((i*4)+1)),
							Integer.parseInt(allColorList.get((i*4)+2)), Integer.parseInt(allColorList.get((i*4)+3))));
				}
			}
			else
			{
				//调色板中的Alpha值是不变的
				for(int i = 0; i < indexList.size(); i++)
				{
					// 将调色板中的每一个RGB实例化为一个颜色对象
					colorDataList.add(new ColorPrj(255, Integer.parseInt(allColorList.get((i*4)+1)),
							Integer.parseInt(allColorList.get((i*4)+2)), Integer.parseInt(allColorList.get((i*4)+3))));
				}
			}
			
			ColorPrj colorPrj;
			int index = 0;
			int[] packedPixels = new int[ytData.getYtData().length];	// 存储云图像素数组
			for (int i = 0; i < ytData.getYtData().length; i++)
			{
				index = (int)ytData.getYtData()[i];
//				if (index >= -5 && index <= -1)
//				{
//					index = 0;
//				}
				if (index < 0)
				{
					index += 256;
				}
				if (index > 255)
				{
					index = 255;
				}
				colorPrj = (ColorPrj) colorDataList.get(index);
				// 生成云图像素数组
				packedPixels[i] = (colorPrj.getAlpha()<<24 | colorPrj.getR()<<16 | colorPrj.getG()<<8 | colorPrj.getB()<<0);
				//packedPixels[i] = (colorPrj.getR()<<16 | colorPrj.getG()<<8 | colorPrj.getB()<<0);
			}
			Image tempImg = Toolkit.getDefaultToolkit().createImage(
					new MemoryImageSource(ytData.getImageW(), ytData.getImageH(), packedPixels, 0, ytData.getImageW()));
			// 生成云图BufferedImage
			BufferedImage image = new BufferedImage(tempImg.getWidth(null), tempImg.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR );
			//BufferedImage image = new BufferedImage(tempImg.getWidth(null), tempImg.getHeight(null), BufferedImage.TYPE_3BYTE_BGR );
			image.createGraphics().drawImage(tempImg, 0, 0, null);
			return image;
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 根据像素点亮度值，获取对应显示高度
	 * 
	 * @param ytData
	 * 
	 * @return
	 */
	public static double[][] getPixelElevation(YtData ytData)
	{
		double[][] pixelElevationAry = new double[ytData.getImageH()][ytData.getImageW()];
		
		int index = 0;
		for (int i = 0; i < ytData.getYtData().length; i++)
		{
			index = (int)ytData.getYtData()[i];
//			if (index >= -5 && index <= -1)
//			{
//				index = 0;
//			}
			if (index < 0)
			{
				index += 256;
			}
			if (index > 255)
			{
				index = 255;
			}
			// 根据云图像素点亮度，生成高度
			int row = i / ytData.getImageW();
			int col = i % ytData.getImageW();
			pixelElevationAry[row][col] = Math.pow(8, (index / 255.0)) * 9000;
		}
		
		return pixelElevationAry;
	}
}

