package com.xinhong.mids3d.syno;

import com.xinhong.mids3d.syno.SynoElem.SynoElemDrawType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


/**
 *
 *
 * @author Zhoucj
 *
 */
public class SynoImageDrawer
{
	/**
	 * 在填图图片上绘制要素
	 *
	 * @param g            图片Graphics2D
	 * @param gWind        风等带方向要素图片Graphics2D
	 * @param drawPointX   绘制点X坐标
	 * @param drawPointY   绘制点Y坐标
	 * @param font         字体
	 * @param scale        缩放比例
	 * @param synoElemList 填图要素列表
	 */
	public static void drawSynoElem(Graphics2D g, Graphics2D gWind, int drawPointX, int drawPointY, Font font, double scale,
									ArrayList<SynoElem> synoElemList, boolean isDrawWind)
	{
		try
		{
			double windSpeed = -1.0d;
			double windDirection = -1.0d;
			Color windColor = null;

			FontMetrics metrics = new FontMetrics(font)
			{
				private static final long serialVersionUID = 1L;
			};

			Font oldFont = g.getFont();
			g.setFont(font);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			for (int i = 0; i < synoElemList.size(); i++)
			{
				if (!synoElemList.get(i).isShowFlag())
				{
					continue;
				}
				if (synoElemList.get(i).getDrawType() == SynoElemDrawType.TEXT)
				{
					// ???????
					g.setColor(synoElemList.get(i).getColor());
					int offsetX = (int) (drawPointX + synoElemList.get(i).getDrawOffset().getX());
					int offsetY = (int) (drawPointY + synoElemList.get(i).getDrawOffset().getY());
					g.drawString(synoElemList.get(i).getText(), offsetX, offsetY);
				}
				else if (synoElemList.get(i).getDrawType() == SynoElemDrawType.PIC)
				{
					if (synoElemList.get(i).getText() != null)
					{
						InputStream iStream = ClassLoader.getSystemResourceAsStream(synoElemList.get(i).getText());
						if (iStream != null)
						{
							Image elemImage = ImageIO.read(iStream);
							int weatherImageWidth = (int) (elemImage.getWidth(null) * scale);
							int weatherImageHeight = (int) (elemImage.getHeight(null) * scale);
							int offsetX = (int) (drawPointX + synoElemList.get(i).getDrawOffset().getX());
							int offsetY = (int) (drawPointY + synoElemList.get(i).getDrawOffset().getY());
							// ??????
							g.drawImage(elemImage.getScaledInstance(weatherImageWidth, weatherImageHeight, Image.SCALE_SMOOTH),
									offsetX - weatherImageWidth / 2, offsetY - weatherImageHeight / 2,
									weatherImageWidth, weatherImageHeight, null);
							// ?????????????????????
							if (synoElemList.get(i).getColor() != null)
							{
								Composite comp = g.getComposite();
								// ??????????
								g.setComposite(AlphaComposite.SrcIn);
								g.setColor(synoElemList.get(i).getColor());
								g.fillRect(offsetX - weatherImageWidth / 2, offsetY - weatherImageHeight / 2,
										weatherImageWidth, weatherImageHeight);
								g.setComposite(comp);
							}
						}
					}
				}
				else if (synoElemList.get(i).getDrawType() == SynoElemDrawType.WS)
				{
					windSpeed = Double.parseDouble(synoElemList.get(i).getText());
					windColor = synoElemList.get(i).getColor();
				}
				else if (synoElemList.get(i).getDrawType() == SynoElemDrawType.WD)
				{
					windDirection = Double.parseDouble(synoElemList.get(i).getText());
				}
				else if (synoElemList.get(i).getDrawType() == SynoElemDrawType.SWD)
				{
					double streamDirection = Double.parseDouble(synoElemList.get(i).getText());
					Color streamColor = synoElemList.get(i).getColor();
					gWind.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					SynoImageDrawer.drawStream(g, drawPointX, drawPointY, streamDirection, streamColor, (int)(100 * scale));
				}//????????????? 2013-10-18 DYH
				else if(synoElemList.get(i).getDrawType() == SynoElemDrawType.PMDIR)
				{
					int offsetX = (int) (drawPointX + synoElemList.get(i).getDrawOffset().getX());
					int offsetY = (int) (drawPointY + synoElemList.get(i).getDrawOffset().getY());
					double streamDirection = Double.parseDouble(synoElemList.get(i).getText());
					Color streamColor = synoElemList.get(i).getColor();
					gWind.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					SynoImageDrawer.drawTerraceDirection(gWind, offsetX, offsetY, streamDirection, streamColor, (int)(100 * scale));
				}
			}
			if (windSpeed >= 0.0d && windDirection >= 0.0d)
			{
				if (isDrawWind)
				{
					// 2D
					SynoImageDrawer.drawWindVane(g, drawPointX, drawPointY, windSpeed, windDirection, windColor, (int)(70 * scale));
				}
			}

			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g.setFont(oldFont);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 在填图上绘制要素图片
	 *
	 * @param g          图片Graphics2D
	 * @param imagePath  图片文件路径
	 * @param drawPointX 绘制点X坐标
	 * @param drawPointY 绘制点Y坐标
	 * @param scale      图片放大比例
	 * @param color      图片内容绘制颜色
	 */
	public static void drawSynoElemImage(Graphics2D g, String imagePath, int drawPointX, int drawPointY, double scale, Color color)
	{
		try
		{
			InputStream iStream = ClassLoader.getSystemResourceAsStream(imagePath);
			if (iStream == null)
			{
				return;
			}
			Image elemImage = ImageIO.read(iStream);
			int weatherImageWidth = (int) (elemImage.getWidth(null) * scale);
			int weatherImageHeight = (int) (elemImage.getHeight(null) * scale);
			// 绘制图片
			g.drawImage(elemImage.getScaledInstance(weatherImageWidth, weatherImageHeight, Image.SCALE_SMOOTH),
					drawPointX - weatherImageWidth / 2, drawPointY - weatherImageHeight / 2,
					weatherImageWidth, weatherImageHeight, null);
			// 如果给定颜色，取代原图片颜色
			if (color != null)
			{
				Composite comp = g.getComposite();
				// 启用颜色混合
				g.setComposite(AlphaComposite.SrcIn);
				g.setColor(color);
				g.fillRect(drawPointX - weatherImageWidth / 2, drawPointY - weatherImageHeight / 2,
						weatherImageWidth, weatherImageHeight);
				g.setComposite(comp);
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 在填图图片上绘制风羽
	 *
	 * @param g             图片Graphics2D
	 * @param drawPointX    绘制点X坐标
	 * @param drawPointY    绘制点Y坐标
	 * @param windSpeed     风速，m/s
	 * @param windDirection 风向，角度
	 * @param color
	 * @param poleLength    风杆长度
	 */
	public static void drawWindVane(Graphics2D g, int drawPointX, int drawPointY, double windSpeed, double windDirection,
									Color color, int poleLength)
	{
		/*if (windSpeed < 0 || windDirection<=0) //LiuXC
			return;
		// ?????0????????
		if (Math.abs(windSpeed - 0.0) <= 1.0e-5)
		{
			return;
		}
		// ????????
		int[] levelAry = WindVane.GetWindVaneLevels(windSpeed);

		// ?????????????????????
		int parallel = poleLength / 6;
		int perpendicular = poleLength * 2 / 5;

		// ?????????θ????
		ArrayList<Point> pointAry = new ArrayList<Point>();
		// ???????????????θ????
		ArrayList<Point> triangleAry = new ArrayList<Point>();

		// ??????????????????????????????????????????????????????
		// ??????????????????λ???X???????????Y??????????
		// ?????
		pointAry.add(new Point(0, 0));
		pointAry.add(new Point(0, poleLength));

		// ??????
		int pointTemp = poleLength;    // ?????????????????????????λ?????ε?λ??
		if (Math.abs(windSpeed - 2.0) <= 1.0e-5)
		{
			// ?????2??????????
			pointAry.add(new Point(0, pointTemp - parallel));
			pointAry.add(new Point(perpendicular / 2, pointTemp - parallel));
		}
		else
		{
			// ?????????
			for (int i = 0; i < levelAry[0]; i++)
			{
				// ?????????θ????????????????
				triangleAry.add(new Point(0, pointTemp));
				triangleAry.add(new Point(perpendicular, pointTemp));
				triangleAry.add(new Point(0, pointTemp - parallel));

				pointTemp = pointTemp - parallel;
			}
			// ??????????
			for (int i = 0; i < levelAry[1]; i++)
			{
				// ?????????θ????????????????
				pointAry.add(new Point(0, pointTemp));
				pointAry.add(new Point(perpendicular, pointTemp));
				////
				pointAry.add(new Point(0, pointTemp - parallel));
				pointAry.add(new Point(perpendicular, pointTemp));

				pointTemp = pointTemp - parallel;
			}
			// ????
			for (int i = 0; i < levelAry[2]; i++)
			{
				// ???????????
				pointAry.add(new Point(0, pointTemp));
				pointAry.add(new Point(perpendicular, pointTemp));

				pointTemp = pointTemp - parallel;
			}
			// ???
			for (int i = 0; i < levelAry[3]; i++)
			{
				// ???????????
				pointAry.add(new Point(0, pointTemp));
				pointAry.add(new Point(perpendicular / 2, pointTemp));

				pointTemp = pointTemp - parallel;
			}
		}

		// TODO ???????????????
		// ????????????????????????????X?????????Y??????????????????X??????Y???????
		// ???????????????????
		int newX = 0;
		int newY = 0;
		for (int i = 0; i < pointAry.size(); i++)
		{
			double radians = Math.toRadians(windDirection);
			newX = (int) (pointAry.get(i).getX() * Math.cos(radians) + pointAry.get(i).getY() * Math.sin(radians));
			newY = (int) (pointAry.get(i).getY() * Math.cos(radians) - pointAry.get(i).getX() * Math.sin(radians));
			newY = newY * -1;
			newX += drawPointX;
			newY += drawPointY;
			pointAry.get(i).setLocation(newX, newY);
		}
		for (int i = 0; i < triangleAry.size(); i++)
		{
			double radians = Math.toRadians(windDirection);
			newX = (int) (triangleAry.get(i).getX() * Math.cos(radians) + triangleAry.get(i).getY() * Math.sin(radians));
			newY = (int) (triangleAry.get(i).getY() * Math.cos(radians) - triangleAry.get(i).getX() * Math.sin(radians));
			newY = newY * -1;
			newX += drawPointX;
			newY += drawPointY;
			triangleAry.get(i).setLocation(newX, newY);
		}

		if (color == null)
		{
			color = Color.ORANGE;
		}
		g.setColor(color);
		// ???????
		float lineWidth = 1.5f;
		g.setStroke(new BasicStroke(lineWidth));
		for (int i = 0; i < pointAry.size(); i += 2)
		{
			g.drawLine(pointAry.get(i).x, pointAry.get(i).y, pointAry.get(i+1).x, pointAry.get(i+1).y);
		}
		// ?????????????
		for (int i = 0; i < triangleAry.size(); i += 3)
		{
			int[] xPoints = {triangleAry.get(i).x, triangleAry.get(i+1).x, triangleAry.get(i+2).x};
			int[] yPoints = {triangleAry.get(i).y, triangleAry.get(i+1).y, triangleAry.get(i+2).y};
			g.fillPolygon(xPoints, yPoints, 3);
		}*/
	}

	public static void drawStream(Graphics2D g, int drawPointX, int drawPointY, double streamDirection, Color color, int poleLength)
	{
		// 计算波浪线周期线长及峰值线长
		int parallel = poleLength / 10;
		int perpendicular = poleLength / 8;

		// 保存绘制线段各端点
		ArrayList<Point> pointAry = new ArrayList<Point>();
		// 保存绘制实心三角形各端点
		ArrayList<Point> triangleAry = new ArrayList<Point>();

		// 保存绘制用各端点，先将涌置于坐标原点，并将涌向朝正北，即垂直向上
		// 先用标准平面坐标系计算位置，X轴正向向右，Y轴正向向上
		// 波浪线端点
		pointAry.add(new Point(0, 0));

		// 波浪线端点
		for (int i = 1; i < 8; i++)
		{
			pointAry.add(new Point((int)(perpendicular * Math.pow(-1, i)), (parallel * i)));
		}
		pointAry.add(new Point(0, (parallel * 8)));
		// 顶端箭头
		triangleAry.add(new Point(perpendicular * -1, (parallel * 8)));
		triangleAry.add(new Point(perpendicular, (parallel * 8)));
		triangleAry.add(new Point(0, poleLength));

		// TODO 先根据涌向旋转各点
		// 然后由于图片坐标坐标原点在左上角，X轴正向朝右，Y轴正向朝下，所以将各点X坐标不变，Y坐标取负
		// 再将坐标原点移至绘制点
		int newX = 0;
		int newY = 0;
		for (int i = 0; i < pointAry.size(); i++)
		{
			double radians = Math.toRadians(streamDirection);
			newX = (int) (pointAry.get(i).getX() * Math.cos(radians) + pointAry.get(i).getY() * Math.sin(radians));
			newY = (int) (pointAry.get(i).getY() * Math.cos(radians) - pointAry.get(i).getX() * Math.sin(radians));
			newY = newY * -1;
			newX += drawPointX;
			newY += drawPointY;
			pointAry.get(i).setLocation(newX, newY);
		}
		for (int i = 0; i < triangleAry.size(); i++)
		{
			double radians = Math.toRadians(streamDirection);
			newX = (int) (triangleAry.get(i).getX() * Math.cos(radians) + triangleAry.get(i).getY() * Math.sin(radians));
			newY = (int) (triangleAry.get(i).getY() * Math.cos(radians) - triangleAry.get(i).getX() * Math.sin(radians));
			newY = newY * -1;
			newX += drawPointX;
			newY += drawPointY;
			triangleAry.get(i).setLocation(newX, newY);
		}

		if (color == null)
		{
			color = Color.ORANGE;
		}
		g.setColor(color);
		// 绘制线段
		float lineWidth = 1.5f;
		g.setStroke(new BasicStroke(lineWidth));
		for (int i = 0; i < pointAry.size() - 1; i++)
		{
			g.drawLine(pointAry.get(i).x, pointAry.get(i).y, pointAry.get(i+1).x, pointAry.get(i+1).y);
		}
		// 绘制实心三角形
		for (int i = 0; i < triangleAry.size(); i += 3)
		{
			int[] xPoints = {triangleAry.get(i).x, triangleAry.get(i+1).x, triangleAry.get(i+2).x};
			int[] yPoints = {triangleAry.get(i).y, triangleAry.get(i+1).y, triangleAry.get(i+2).y};
			g.fillPolygon(xPoints, yPoints, 3);
		}
	}
	/**
	 * 绘制移动平台方向----DYH
	 * @param g
	 * @param drawPointX
	 * @param drawPointY
	 * @param streamDirection
	 * @param color
	 * @param poleLength
	 */
	public static void drawTerraceDirection(Graphics2D g, int drawPointX, int drawPointY, double streamDirection, Color color, int poleLength){
		//直线长
		int lineLen = (poleLength /10) * 3;
		//箭头线长
		int arrowsLineLen = (poleLength /10) * 2;

		// 先用标准平面坐标系计算位置，X轴正向向右，Y轴正向向上
		// 直线端点
		int startX = 0, startY = 0;
		int endX = 0, endY = lineLen;
		//箭头端点
		int leftX = -(arrowsLineLen/2), leftY = arrowsLineLen;
		int rightX = (arrowsLineLen/2), rightY = arrowsLineLen;

		// TODO 先根据涌向旋转各点
		// 然后由于图片坐标坐标原点在左上角，X轴正向朝右，Y轴正向朝下，所以将各点X坐标不变，Y坐标取负
		// 再将坐标原点移至绘制点
		double radians = Math.toRadians(streamDirection);
		int newStartX = startX + drawPointX, newStartY = startY + drawPointY;
		int newEndX = (int) ((endX * Math.cos(radians)) + (endY * Math.sin(radians)));
		int newEndY = (int) ((endY * Math.cos(radians)) - (endX * Math.sin(radians)));
		newEndY = newEndY * -1;;
		newEndX += drawPointX;
		newEndY = (drawPointY + newEndY);

		int newLeftX = (int) ((leftX * Math.cos(radians)) + (leftY * Math.sin(radians)));
		int newLeftY = (int) ((leftY * Math.cos(radians)) - (leftX * Math.sin(radians)));
		newLeftY = newLeftY * -1;
		newLeftX += drawPointX;
		newLeftY += drawPointY;

		int newRightX = (int) ((rightX * Math.cos(radians)) + (rightY * Math.sin(radians)));
		int newRightY = (int) ((rightY * Math.cos(radians)) - (rightX * Math.sin(radians)));
		newRightY = newRightY * -1;
		newRightX += drawPointX;
		newRightY += drawPointY;

		if (color == null)
		{
			color = Color.RED;
		}
		g.setColor(color);
		// 绘制线段
		float lineWidth = 1.5f;
		g.setStroke(new BasicStroke(lineWidth));
		g.drawLine(newStartX, newStartY, newEndX, newEndY);

		//绘制箭头
		g.drawLine(newLeftX, newLeftY, newEndX, newEndY);
		g.drawLine(newEndX, newEndY, newRightX, newRightY);

	}

}
