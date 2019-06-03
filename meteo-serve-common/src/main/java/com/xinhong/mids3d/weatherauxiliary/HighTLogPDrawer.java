package com.xinhong.mids3d.weatherauxiliary;

import com.xinhong.mids3d.syno.SynoImageDrawer;
import com.xinhong.mids3d.syno.util.Station;

import java.awt.*;
import java.util.ArrayList;


/**
 * 温度对数压力图绘制类，在给定的二维Graphics2D上绘制
 *
 * @author Zhoucj
 *
 */
public class HighTLogPDrawer
{
	/**
	 *
	 * @param g
	 * @param imageWidth
	 * @param imageHeight
	 * @param station
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param pressData
	 * @param HHData
	 * @param ATData
	 * @param TDData
	 * @param WSData
	 * @param WDData
	 */
	public static void DrawTLogP(Graphics2D g, int imageWidth, int imageHeight,
								 Station station, String year, String month, String day, String hour, double[] pressData,
								 double[] HHData, double[] ATData, double[] TDData, double[] WSData, double[] WDData,double tb0)
	{
		final int XMARGIN = 70;
		final int YMARGIN = 50;

		DrawMap(g, imageWidth, imageHeight, XMARGIN, YMARGIN);
		DrawTLogPData(g, imageWidth, imageHeight, XMARGIN, YMARGIN, station, year, month, day, hour,
				pressData, HHData, ATData, TDData, WSData, WDData,tb0);
	}

	/**
	 *
	 * @param g
	 * @param imageWidth
	 * @param imageHeight
	 * @param xMargin
	 * @param yMargin
	 */
	protected static void DrawMap(Graphics2D g, int imageWidth, int imageHeight, int xMargin, int yMargin)
	{
		double[] txAry = new double[126];
		double[] pyAry = new double[86];
		double deltX = (imageWidth - xMargin * 5.0 / 3.0) / 125.0;
		double sump = 10.0 * (Math.log(1000.0 / 200.0) - Math.log(1000.0 / 1050.0));
		double deltY = (imageHeight - 2 * yMargin) / sump;
		for (int i = 0; i < txAry.length; i++)
		{
			txAry[i] = xMargin + i * deltX;
		}
		for (int i = 0; i < pyAry.length; i++)
		{
			pyAry[i] = yMargin + 10 * (Math.log(1000.0 / (1050.0 - i * 10)) - Math.log(1000.0 / 1050.0)) * deltY;
			pyAry[i] = imageHeight - pyAry[i];    // Y轴正向朝下
		}

		// 轴标注颜色
		Color axesLabelColor = new Color(105, 105, 85);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// TODO 画等压线
		g.setColor(new Color(245, 220, 100));
		for (int i = 0; i < pyAry.length; i = i + 5)
		{
			if (((1050 - i * 10) % 100) == 0)
			{
				g.setStroke(new BasicStroke(2.0f));
			}
			else
			{
				g.setStroke(new BasicStroke(1.0f));
			}
			g.drawLine((int)txAry[0], (int)pyAry[i], (int)txAry[txAry.length-1], (int)pyAry[i]);
		}
		g.setStroke(new BasicStroke(2.0f));
		g.drawLine((int)txAry[0], (int)pyAry[0], (int)txAry[txAry.length-1], (int)pyAry[0]);

		int xOffset = 35;
		String[] pressAry = {"1000", "925", "850", "700", "600", "500", "400", "300", "200"};
		Font font = new Font("Arial", Font.BOLD, 14);
		g.setFont(font);
		g.setColor(axesLabelColor);
		g.drawString("mb", (float)(txAry[0] - xOffset), (float)(pyAry[5] + 15));
		g.drawString("mb", (float)(txAry[txAry.length-1] + 5), (float)(pyAry[5] + 15));
		for (int i = 0; i < pressAry.length; i++)
		{
			g.drawString(pressAry[i], (float)(txAry[0] - xOffset), (float)(pyAry[i * 10 + 5] + 5));
			g.drawString(pressAry[i], (float)(txAry[txAry.length-1] + 5), (float)(pyAry[i * 10 + 5] + 5));
		}
		g.drawString("(250)", (float)(txAry[0] - xOffset * 2), (float)(pyAry[5] + 5));
		g.drawString("(200)", (float)(txAry[0] - xOffset * 2), (float)(pyAry[25] + 5));
		g.drawString("(150)", (float)(txAry[0] - xOffset * 2), (float)(pyAry[45] + 5));
		g.drawString("(100)", (float)(txAry[0] - xOffset * 2), (float)(pyAry[65] + 5));
		g.drawString("(75)", (float)(txAry[0] - xOffset * 2), (float)(pyAry[75] + 5));
		g.drawString("(50)", (float)(txAry[0] - xOffset * 2), (float)(pyAry[85] + 5));

		// TODO 画等温线
		g.setColor(new Color(255, 220, 160));
		for (int i = 0; i < txAry.length; i = i + 5)
		{
			if (((i - 85) % 10) == 0)
			{
				g.setStroke(new BasicStroke(2.0f));
			}
			else
			{
				g.setStroke(new BasicStroke(1.0f));
			}
			g.drawLine((int)txAry[i], (int)pyAry[0], (int)txAry[i], (int)pyAry[pyAry.length - 1]);
		}
		g.setStroke(new BasicStroke(2.0f));
		g.drawLine((int)txAry[0], (int)pyAry[0], (int)txAry[0], (int)pyAry[pyAry.length - 1]);

		for (int i = 0; i < txAry.length; i++)
		{
			if ((i % 5) == 0)
			{
				g.setStroke(new BasicStroke(1.0f));
				g.drawLine((int)txAry[i], (int)pyAry[0], (int)txAry[i], (int)pyAry[pyAry.length - 1]);
			}
		}

		String[] temperatureAry = {"-80℃", "-70℃", "-60℃", "-50℃", "-40℃", "-30℃", "-20℃", "-10℃",
				"0℃", "10℃", "20℃", "30℃", "40℃"};
		font = new Font("宋体", Font.BOLD, 14);
		g.setFont(font);
		g.setColor(axesLabelColor);
		for (int i = 0; i < temperatureAry.length; i++)
		{
			g.drawString(temperatureAry[i], (float)(txAry[i * 10 + 5] - 15), (float)(pyAry[0] + 15));
			g.drawString(temperatureAry[i], (float)(txAry[i * 10 + 5] - 15), (float)(pyAry[pyAry.length - 1] - 5));
		}

		String[] gpmAry = {"12000", "11000", "10000", "9000", "8000", "7000", "6000", "5000", "4000", "3000", "2000", "1000", "0(gpm)"};
		font = new Font("Arial", Font.BOLD, 14);
		g.setFont(font);
		g.setColor(axesLabelColor);
		for (int i = 0; i < gpmAry.length; i++)
		{
			g.drawString(gpmAry[i], (float)(txAry[i * 10 + 5] - 18), (float)(pyAry[0] + 28));
			g.drawString(gpmAry[i], (float)(txAry[i * 10 + 5] - 18), (float)(pyAry[pyAry.length - 1] - 19));
		}

		String[] tempAry = {"(21000)", "(20000)", "(19000)", "(18000)", "(17000)", "(16000)", "(15000)",
				"(14000)", "(13000)", "(12000)", "(11000)", "(10000)", "(9000)"};
		for (int i = 0; i < gpmAry.length; i++)
		{
			g.drawString(tempAry[i], (float)(txAry[i * 10 + 5] - 20), (float)(pyAry[0] + 43));
			g.drawString(tempAry[i], (float)(txAry[i * 10 + 5] - 20), (float)(pyAry[pyAry.length - 1] - 36));
		}

		// TODO 画等克线
		double a = 9.5;
		double b = 265.5;
		double aa = 7.5;
		double bb = 237.3;
		String[] qsAry = {"0.01", "0.05", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.8", "1.0", "1.3", "1.5", "2",
				"2.5", "3", "4", "5", "6", "8", "10", "12", "14", "16", "18", "20", "25", "30", "35", "40"};
		double[][] cAry = new double[29][86];
		double[][] qxAry = new double[29][86];
		font = new Font("Arial", Font.PLAIN, 14);
		g.setFont(font);
		g.setColor(new Color(80, 220, 80));
		for (int i = 0; i < 29; i++)
		{
			for (int j = 0; j < 86; j++)
			{
				cAry[i][j] = Math.log10((Double.parseDouble(qsAry[i]) * (1050 - j * 10))
						/ (1000.0 * 6.1078 * (287.0 / 461.5 + Double.parseDouble(qsAry[i]) * (1.0 - 287.0 / 461.5) / 1000.0)));
				double t = b * cAry[i][j] / (a - cAry[i][j]);
				if (t > 0.0075)
				{
					t = bb * cAry[i][j] / (aa - cAry[i][j]);
				}
				qxAry[i][j] = (t + 85) * deltX + xMargin;
			}
			g.setStroke(new BasicStroke(1.0f));
			for (int j = 0; j < pyAry.length - 1; j++)
			{
				g.drawLine((int)(qxAry[i][j]), (int)(pyAry[j]), (int)(qxAry[i][j+1]), (int)(pyAry[j+1]));
			}
			g.drawString(qsAry[i], (float)(qxAry[i][37] - 10), (float)(pyAry[37]));
		}
		g.drawString("g/kg", (float)(qxAry[28][37] + 15), (float)(pyAry[37]));
		g.drawString("(0.04)", (float)(qxAry[0][72] - 10), (float)(pyAry[72]));
		g.drawString("(0.2)", (float)(qxAry[1][72] - 10), (float)(pyAry[72]));
		g.drawString("(0.4)", (float)(qxAry[2][72] - 10), (float)(pyAry[72]));
		g.drawString("(0.8)", (float)(qxAry[3][72] - 10), (float)(pyAry[72]));

		// TODO 画等位温线
		double rd = 287.0;
		double cpd = 1004.0;
		double seta = 0.0;
		double[] tsetaXAry = new double[86];
		double[][] setaXAry = new double[43][86];
		double[][] setaYAry = new double[43][86];
		int m = 0;
		g.setColor(new Color(255, 220, 100));
		for (int i = 0; i < 22; i++)
		{
			seta = -80 + i * 5;
			for (int j = 0; j < 86; j++)
			{
				tsetaXAry[j] = Math.exp(Math.log(seta + 273.16) - rd * Math.log(1000.0 / (1050 - j * 10)) / cpd) - 273.16;
				if (tsetaXAry[j] < -85)
				{
					m = j;
					break;
				}
				setaXAry[i][j] = (tsetaXAry[j] + 85) * deltX + xMargin;
			}
			setaXAry[i][m] = xMargin;
			for (int j = 0; j < m; j++)
			{
				setaYAry[i][j] = pyAry[j];
			}
			setaYAry[i][m] = yMargin + 10 * ((Math.log(seta + 273.16) - Math.log(-85 + 273.16)) * cpd / rd - Math.log(1000.0 / 1050.0)) * deltY;
			setaYAry[i][m] = imageHeight - setaYAry[i][m];    // Y轴正向朝下

			g.setStroke(new BasicStroke(1.0f));
			for (int j = 0; j < m; j++)
			{
				g.drawLine((int)(setaXAry[i][j]), (int)(setaYAry[i][j]), (int)(setaXAry[i][j+1]), (int)(setaYAry[i][j+1]));
			}
		}

		for (int i = 22; i < 24; i++)
		{
			seta = -80 + i * 5;
			for (int j = 85; j >= 0; j--)
			{
				tsetaXAry[j] = Math.exp(Math.log(seta + 273.16) - rd * Math.log(1000.0 / (1050 - j * 10)) / cpd) - 273.16;
				setaXAry[i][j] = (tsetaXAry[j] + 85) * deltX + xMargin;
			}
			g.setStroke(new BasicStroke(1.0f));
			for (int j = 0; j < pyAry.length - 1; j++)
			{
				g.drawLine((int)(setaXAry[i][j]), (int)(pyAry[j]), (int)(setaXAry[i][j+1]), (int)(pyAry[j+1]));
			}
		}

		for (int i = 24; i < 43; i++)
		{
			seta = -80 + i * 5;
			for (int j = 85; j >= 0; j--)
			{
				tsetaXAry[j] = Math.exp(Math.log(seta + 273.16) - rd * Math.log(1000.0 / (1050 - j * 10)) / cpd) - 273.16;
				if (tsetaXAry[j] > 40)
				{
					m = j;
					break;
				}
				setaXAry[i][j] = (tsetaXAry[j] + 85) * deltX + xMargin;
			}
			setaXAry[i][m] = imageWidth - xMargin;
			for (int j = m-1; j < 86; j++)
			{
				setaYAry[i][j] = pyAry[j];
			}
			setaYAry[i][m] = yMargin + 10 * ((Math.log(seta + 273.16) - Math.log(40 + 273.16)) * cpd / rd - Math.log(1000.0 / 1050.0)) * deltY;
			setaYAry[i][m] = imageHeight - setaYAry[i][m];    // Y轴正向朝下

			g.setStroke(new BasicStroke(1.0f));
			for (int j = m; j < 85; j++)
			{
				g.drawLine((int)(setaXAry[i][j]), (int)(setaYAry[i][j]), (int)(setaXAry[i][j+1]), (int)(setaYAry[i][j+1]));
			}
		}

		g.setColor(new Color(250, 250, 125));
		g.drawString("-80°", (int)(setaXAry[0][7] - 10), (int)(setaYAry[0][7]));
		g.drawString("-70°", (int)(setaXAry[2][14] - 10), (int)(setaYAry[2][14]));
		g.drawString("-60°", (int)(setaXAry[4][21] - 10), (int)(setaYAry[4][21]));
		g.drawString("-50°", (int)(setaXAry[6][26] - 10), (int)(setaYAry[6][26]));
		g.drawString("-40°", (int)(setaXAry[8][32] - 10), (int)(setaYAry[8][32]));
		g.drawString("-30°", (int)(setaXAry[10][37] - 10), (int)(setaYAry[10][37]));
		g.drawString("-20°", (int)(setaXAry[12][41] - 10), (int)(setaYAry[12][41]));
		g.drawString("-10°", (int)(setaXAry[14][45] - 10), (int)(setaYAry[14][45]));
		g.drawString("0°", (int)(setaXAry[16][49] - 10), (int)(setaYAry[16][49]));
		g.drawString("10°", (int)(setaXAry[18][53] - 10), (int)(setaYAry[18][53]));
		g.drawString("20°", (int)(setaXAry[20][56] - 10), (int)(setaYAry[20][56]));
		g.drawString("30°", (int)(setaXAry[22][59] - 10), (int)(pyAry[59]));
		g.drawString("40°", (int)(setaXAry[24][62] - 10), (int)(setaYAry[24][62]));
		g.drawString("50°", (int)(setaXAry[26][65] - 10), (int)(setaYAry[26][65]));
		g.drawString("60°", (int)(setaXAry[28][67] - 10), (int)(setaYAry[28][67]));
		g.drawString("70°", (int)(setaXAry[30][69] - 10), (int)(setaYAry[30][69]));
		g.drawString("80°", (int)(setaXAry[32][71] - 10), (int)(setaYAry[32][71]));
		g.drawString("90°", (int)(setaXAry[34][73] - 10), (int)(setaYAry[34][73]));
		g.drawString("100°", (int)(setaXAry[36][75] - 10), (int)(setaYAry[36][75]));
		g.drawString("110°", (int)(setaXAry[38][77] - 10), (int)(setaYAry[38][77]));
		g.drawString("120°", (int)(setaXAry[40][79] - 10), (int)(setaYAry[40][79]));
		g.drawString("130°", (int)(setaXAry[42][81] - 10), (int)(setaYAry[42][81]));

		font = new Font("Arial", Font.PLAIN, 12);
		g.setFont(font);
		g.setColor(new Color(255, 220, 100));
		g.drawString("(14.7)", (int)(setaXAry[0][9] - 10), (int)(setaYAry[0][9]));
		g.drawString("(29.6)", (int)(setaXAry[2][16] - 10), (int)(setaYAry[2][16]));
		g.drawString("(44.5)", (int)(setaXAry[4][23] - 10), (int)(setaYAry[4][23]));
		g.drawString("(59.4)", (int)(setaXAry[6][28] - 10), (int)(setaYAry[6][28]));
		g.drawString("(74.3)", (int)(setaXAry[8][34] - 10), (int)(setaYAry[8][34]));
		g.drawString("(89.2)", (int)(setaXAry[10][39] - 10), (int)(setaYAry[10][39]));
		g.drawString("(104.1)", (int)(setaXAry[12][43] - 10), (int)(setaYAry[12][43]));
		g.drawString("(119.0)", (int)(setaXAry[14][47] - 10), (int)(setaYAry[14][47]));
		g.drawString("(134.0)", (int)(setaXAry[16][51] - 10), (int)(setaYAry[16][51]));
		g.drawString("(148.9)", (int)(setaXAry[18][55] - 10), (int)(setaYAry[18][55]));
		g.drawString("(163.8)", (int)(setaXAry[20][58] - 10), (int)(setaYAry[20][58]));
		g.drawString("(178.7)", (int)(setaXAry[22][60] - 10), (int)(pyAry[60]));
		g.drawString("(193.6)", (int)(setaXAry[24][63] - 10), (int)(setaYAry[24][63]));
		g.drawString("(208.5)", (int)(setaXAry[26][66] - 10), (int)(setaYAry[26][66]));
		g.drawString("(223.4)", (int)(setaXAry[28][68] - 10), (int)(setaYAry[28][68]));
		g.drawString("(238.3)", (int)(setaXAry[30][70] - 10), (int)(setaYAry[30][70]));
		g.drawString("(253.4)", (int)(setaXAry[32][72] - 10), (int)(setaYAry[32][72]));
		g.drawString("(268.1)", (int)(setaXAry[34][74] - 10), (int)(setaYAry[34][74]));
		g.drawString("(283.1)", (int)(setaXAry[36][76] - 10), (int)(setaYAry[36][76]));
		g.drawString("(298.0)", (int)(setaXAry[38][78] - 10), (int)(setaYAry[38][78]));
		g.drawString("(312.9)", (int)(setaXAry[40][80] - 10), (int)(setaYAry[40][80]));
		g.drawString("(327.8)", (int)(setaXAry[42][82] - 10), (int)(setaYAry[42][82]));

		// TODO 画等假相当位温线
		double[] setaseAry = {-45.0, -40.0, -35.0, -30.0, -25.0, -20.0, -15.0, -10.0, -5.0, 0.0, 5.0, 10.0, 15.0,
				20.0, 25.0, 30.0, 35.0, 40.0, 50.0, 55.0, 60.0, 65.0, 70.0, 80.0, 90.0, 100.0,
				110.0, 120.0, 130.0, 140.0, 150.0, 160.0, 170.0, 180.0, 190.0};
		for (int i = 0; i < setaseAry.length; i++)
		{
			setaseAry[i] = setaseAry[i] + 273.16;
		}
		double[][] tsetaseAry = gettsetaseAry();
		double[][] setaseXAry = new double[36][86];
		double[][] setaseYAry = new double[36][86];

		g.setColor(new Color(80, 255, 80));
		for (int i = 0; i < 36; i++)
		{
			int kMin = 86;
			int mMax = 0;
			for (int j = 0; j < 86; j++)
			{
				if (Math.abs(tsetaseAry[i][j] - -9999.5) < 10e-5)
				{
					kMin = j;
					break;
				}
			}
			for (int j = 0; j < 86; j++)
			{
				if (tsetaseAry[i][j] > 40.0)
				{
					mMax = j;
				}
			}

			if (kMin == 86 && mMax == 0)
			{
				for (int j = 0; j < 86; j++)
				{
					setaseXAry[i][j] = (tsetaseAry[i][j] + 85) * deltX + xMargin;
					setaseYAry[i][j] = pyAry[j];
				}
				float[] dash = {10.0f, 10.0f};
				g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
				for (int j = 0; j < 85; j++)
				{
					g.drawLine((int)(setaseXAry[i][j]), (int)(setaseYAry[i][j]), (int)(setaseXAry[i][j+1]), (int)(setaseYAry[i][j+1]));
				}
			}
			if (kMin < 86 && mMax == 0)
			{
				setaseXAry[i][kMin] = xMargin;
				setaseYAry[i][kMin] = yMargin + 10 * (Math.log(1000.0 / p_setase(setaseAry[i], -84.5)) - Math.log(1000.0 / 1050.0)) * deltY;
				setaseYAry[i][kMin] = imageHeight - setaseYAry[i][m];    // Y轴正向朝下
				for (int j = 0; j < kMin; j++)
				{
					setaseXAry[i][j] = (tsetaseAry[i][j] + 85) * deltX + xMargin;
					setaseYAry[i][j] = pyAry[j];
				}
				float[] dash = {5.0f, 10.0f};
				g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
				for (int j = 0; j < kMin - 1; j++)
				{
					g.drawLine((int)(setaseXAry[i][j]), (int)(setaseYAry[i][j]), (int)(setaseXAry[i][j+1]), (int)(setaseYAry[i][j+1]));
				}
			}
			if (kMin == 86 & mMax > 0)
			{
				for (int j = mMax + 1; j < 86; j++)
				{
					setaseXAry[i][j] = (tsetaseAry[i][j] + 85) * deltX + xMargin;
					setaseYAry[i][j] = pyAry[j];
				}
				float[] dash = {10.0f, 10.0f};
				g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
				for (int j = mMax + 1; j < 86; j++)
				{
					g.drawLine((int)(setaseXAry[i][j]), (int)(setaseYAry[i][j]), (int)(setaseXAry[i][j+1]), (int)(setaseYAry[i][j+1]));
				}
			}
		}

		font = new Font("Arial", Font.PLAIN, 14);
		g.setFont(font);
		g.drawString("-40°", (int)(setaseXAry[1][16] - 10), (int)(setaseYAry[1][16]));
		g.drawString("-30°", (int)(setaseXAry[3][24] - 10), (int)(setaseYAry[3][24]));
		g.drawString("-20°", (int)(setaseXAry[5][32] - 10), (int)(setaseYAry[5][32]));
		g.drawString("-10°", (int)(setaseXAry[7][38] - 10), (int)(setaseYAry[7][38]));
		g.drawString("0°", (int)(setaseXAry[9][43] - 10), (int)(setaseYAry[9][43]));
		g.drawString("10°", (int)(setaseXAry[11][48] - 10), (int)(setaseYAry[11][48]));
		g.drawString("20°", (int)(setaseXAry[13][52] - 10), (int)(setaseYAry[13][52]));
		g.drawString("30°", (int)(setaseXAry[15][56] - 10), (int)(setaseYAry[15][56]));
		g.drawString("40°", (int)(setaseXAry[17][59] - 10), (int)(setaseYAry[17][59]));
		g.drawString("50°", (int)(setaseXAry[19][62] - 10), (int)(setaseYAry[19][62]));
		g.drawString("60°", (int)(setaseXAry[21][64] - 10), (int)(setaseYAry[21][64]));
		g.drawString("70°", (int)(setaseXAry[23][66] - 10), (int)(setaseYAry[23][66]));
		g.drawString("80°", (int)(setaseXAry[24][68] - 10), (int)(setaseYAry[24][68]));
		g.drawString("100°", (int)(setaseXAry[26][71] - 10), (int)(setaseYAry[26][71]));
		g.drawString("120°", (int)(setaseXAry[28][73] - 10), (int)(setaseYAry[28][73]));
		g.drawString("140°", (int)(setaseXAry[30][75] - 10), (int)(setaseYAry[30][75]));
		g.drawString("160°", (int)(setaseXAry[32][76] - 10), (int)(setaseYAry[32][76]));
		g.drawString("180°", (int)(setaseXAry[34][77] - 10), (int)(setaseYAry[34][77]));

		// TODO 写图注
		int[] xAry = {(int)(txAry[100] + 1), (int)(txAry[100] + 1), (int)(txAry[125]), (int)(txAry[125])};
		int[] yAry = {(int)(pyAry[77]), (int)(pyAry[85]), (int)(pyAry[85]), (int)(pyAry[77])};
		g.setColor(new Color(255, 255, 255));
		g.fillPolygon(xAry, yAry, 4);
		g.setColor(new Color(245, 220, 100));
		g.setStroke(new BasicStroke(1.0f));
		g.drawPolygon(xAry, yAry, 4);
		font = new Font("宋体", Font.BOLD, 16);
		g.setFont(font);
		g.setColor(new Color(80, 180, 10));
		g.drawString("温度对数压力图解", (float)(txAry[112] - 70), (float)(pyAry[84]));
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	/**
	 *
	 * @param g
	 * @param imageWidth
	 * @param imageHeight
	 * @param xMargin
	 * @param yMargin
	 * @param station
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param pressData 气压层次
	 * @param HHData 位势高度
	 * @param ATData 气温 实况 临时
	 * @param TDData 露点温度
	 * @param WSData 风速
	 * @param WDData 风向
	 * @param  tb0 海平面假相当位温
	 */
	protected static void DrawTLogPData(Graphics2D g, int imageWidth, int imageHeight, int xMargin, int yMargin,
										Station station, String year, String month, String day, String hour, double[] pressData,
										double[] HHData, double[] ATData, double[] TDData, double[] WSData, double[] WDData,double tb0)
	{
		if (pressData == null)
		{
			return;
		}

		int[] txAry = new int[126];
		int[] pyAry = new int[86];
		double deltX = (imageWidth - xMargin * 5.0 / 3.0) / 125.0;
		double sump = 10.0 * (Math.log(1000.0 / 200.0) - Math.log(1000.0 / 1050.0));
		double deltY = (imageHeight - 2 * yMargin) / sump;
		for (int i = 0; i < txAry.length; i++)
		{
			txAry[i] = (int)(xMargin + i * deltX);
		}
		for (int i = 0; i < pyAry.length; i++)
		{
			pyAry[i] = (int)(yMargin + 10 * (Math.log(1000.0 / (1050.0 - i * 10)) - Math.log(1000.0 / 1050.0)) * deltY);
			pyAry[i] = (int)(imageHeight - pyAry[i]);    // Y轴正向朝下
		}

		Font font = new Font("宋体", Font.BOLD, 18);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setFont(font);
		g.setColor(new Color(80, 200, 10));
		g.drawString("观测台站：" + station.getCHNName(), (int)(txAry[101]), (int)(pyAry[83] + 3));
		String strDate = year + "年" + month + "月 " + day + "日" + hour + "时";
		g.drawString(strDate, txAry[101], (pyAry[82] + 4));

		// TODO 层结曲线（温压曲线）
		g.setColor(new Color(0, 0, 255));
		g.setStroke(new BasicStroke(1.0f));
		ArrayList<Integer> indexAry = new ArrayList<Integer>();
		for (int i = 0; i < ATData.length; i++)
		{
			if (ATData[i] != 9999.0)
			{
				indexAry.add(i);
			}
		}
		if (indexAry.size() > 1)
		{
			int k200 = 0;
			for (int i = 0; i < indexAry.size(); i++)
			{
				if (pressData[indexAry.get(i)] >= 200.0)
				{
					k200 = k200 + 1;
				}
				else
				{
					break;
				}
			}
			if (k200 > 0)
			{
				int[] xxAry = new int[k200];
				int[] yyAry = new int[k200];
				for (int i = 0; i < k200; i++)
				{
					xxAry[i] = (int)(xMargin + (ATData[indexAry.get(i)] + 85.0) * deltX);
					yyAry[i] = (int)(yMargin + 10 * (Math.log(1000.0 / pressData[indexAry.get(i)]) - Math.log(1000.0 / 1050.0)) * deltY);
					yyAry[i] = (int)(imageHeight - yyAry[i]);    // Y轴正向朝下
					if ((WSData[indexAry.get(i)] != 9999.0) && (WDData[indexAry.get(i)] != 9999.0))
					{
						SynoImageDrawer.drawWindVane(g, xxAry[i], yyAry[i], WSData[indexAry.get(i)], WDData[indexAry.get(i)],
								new Color(165, 0, 0), 30);
					}
				}
				g.drawPolyline(xxAry, yyAry, xxAry.length);
				// 绘制十字
				for (int i = 0; i < xxAry.length; i++)
				{
//					g.setFont(font);
//					g.setColor(new Color(80, 200, 10));
					g.drawLine((xxAry[i] - 5), yyAry[i], (xxAry[i] + 5), yyAry[i]);
					g.drawLine(xxAry[i], (yyAry[i] - 5), xxAry[i], (yyAry[i] + 5));
				}
			}

			int k250 = 0;
			for (int i = 0; i < indexAry.size(); i++)
			{
				if (pressData[indexAry.get(i)] > 250.0)
				{
					k250 = k250 + 1;
				}
				else
				{
					break;
				}
			}
			if ((indexAry.size() - k250) > 1)
			{
				int[] xxAry = new int[indexAry.size() - k250];
				int[] yyAry = new int[indexAry.size() - k250];
				for (int i = 0; i < (indexAry.size() - k250); i++)
				{
					xxAry[i] = (int)(xMargin + (ATData[indexAry.get(i + k250)] + 85.0) * deltX);
					yyAry[i] = (int)(yMargin + 10 * (Math.log(250.0 / pressData[indexAry.get(i + k250)]) - Math.log(1000.0 / 1050.0)) * deltY);
					yyAry[i] = (int)(imageHeight - yyAry[i]);    // Y轴正向朝下
					if ((WSData[indexAry.get(i + k250)] != 9999.0) && (WDData[indexAry.get(i + k250)] != 9999.0))
					{
						SynoImageDrawer.drawWindVane(g, xxAry[i], yyAry[i], WSData[indexAry.get(i + k250)], WDData[indexAry.get(i + k250)],
								new Color(165, 0, 0), 30);
					}
				}
//				g.setColor(new Color(0, 0, 255));
//				g.setStroke(new BasicStroke(1.0f));
				g.drawPolyline(xxAry, yyAry, xxAry.length);
				// 绘制十字
				for (int i = 0; i < xxAry.length; i++)
				{
					g.drawLine((xxAry[i] - 5), yyAry[i], (xxAry[i] + 5), yyAry[i]);
					g.drawLine(xxAry[i], (yyAry[i] - 5), xxAry[i], (yyAry[i] + 5));
				}
			}
		}

		// TODO 露压曲线
		indexAry.clear();
		for (int i = 0; i < TDData.length; i++)
		{
			if (TDData[i] != 9999.0)
			{
				indexAry.add(i);
			}
		}
		if (indexAry.size() > 1)
		{
			int k200 = 0;
			for (int i = 0; i < indexAry.size(); i++)
			{
				if (pressData[indexAry.get(i)] >= 200.0)
				{
					k200 = k200 + 1;
				}
				else
				{
					break;
				}
			}
			if (k200 > 0)
			{
				int[] xxAry = new int[k200];
				int[] yyAry = new int[k200];
				for (int i = 0; i < k200; i++)
				{
					xxAry[i] = (int)(xMargin + (TDData[indexAry.get(i)] + 85.0) * deltX);
					yyAry[i] = (int)(yMargin + 10 * (Math.log(1000.0 / pressData[indexAry.get(i)]) - Math.log(1000.0 / 1050.0)) * deltY);
					yyAry[i] = (int)(imageHeight - yyAry[i]);    // Y轴正向朝下
				}
				ArrayList<Integer> tmpIndexAry = new ArrayList<Integer>();
				for (int i = 0; i < xxAry.length; i++)
				{
					if (xxAry[i] >= xMargin - 10)
					{
						tmpIndexAry.add(i);
					}
				}
				g.setColor(new Color(0, 0, 255));
				float[] dash = {5.0f, 10.0f};
				for (int i = 0; i < tmpIndexAry.size() - 1; i++)
				{
					g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
					g.drawLine(xxAry[tmpIndexAry.get(i)], yyAry[tmpIndexAry.get(i)], xxAry[tmpIndexAry.get(i+1)], yyAry[tmpIndexAry.get(i+1)]);
					// 绘制三角
					g.setStroke(new BasicStroke(1.0f));
					int[] xPoints = {(xxAry[tmpIndexAry.get(i)]), (xxAry[tmpIndexAry.get(i)] - 5),
							(xxAry[tmpIndexAry.get(i)] + 5)};
					int[] yPoints = {(yyAry[tmpIndexAry.get(i)] - 5), (yyAry[tmpIndexAry.get(i)] + 5),
							(yyAry[tmpIndexAry.get(i)] + 5)};
					g.drawPolygon(xPoints, yPoints, xPoints.length);
				}
				// 绘制三角
				g.setStroke(new BasicStroke(1.0f));
				int[] xPoints = {(xxAry[tmpIndexAry.get(tmpIndexAry.size() - 1)]),
						(xxAry[tmpIndexAry.get(tmpIndexAry.size() - 1)] - 5),
						(xxAry[tmpIndexAry.get(tmpIndexAry.size() - 1)] + 5)};
				int[] yPoints = {(yyAry[tmpIndexAry.get(tmpIndexAry.size() - 1)] - 5),
						(yyAry[tmpIndexAry.get(tmpIndexAry.size() - 1)] + 5),
						(yyAry[tmpIndexAry.get(tmpIndexAry.size() - 1)] + 5)};
				g.drawPolygon(xPoints, yPoints, xPoints.length);
			}

			int k250 = 0;
			for (int i = 0; i < indexAry.size(); i++)
			{
				if (pressData[indexAry.get(i)] > 250.0)
				{
					k250 = k250 + 1;
				}
				else
				{
					break;
				}
			}
			if ((indexAry.size() - k250) > 1)
			{
				int[] xxAry = new int[indexAry.size() - k250];
				int[] yyAry = new int[indexAry.size() - k250];
				for (int i = 0; i < (indexAry.size() - k250); i++)
				{
					xxAry[i] = (int)(xMargin + (TDData[indexAry.get(i + k250)] + 85.0) * deltX);
					yyAry[i] = (int)(yMargin + 10 * (Math.log(250.0 / pressData[indexAry.get(i + k250)]) - Math.log(1000.0 / 1050.0)) * deltY);
					yyAry[i] = (int)(imageHeight - yyAry[i]);    // Y轴正向朝下
				}
				ArrayList<Integer> tmpIndexAry = new ArrayList<Integer>();
				for (int i = 0; i < xxAry.length; i++)
				{
					if (xxAry[i] >= xMargin - 10)
					{
						tmpIndexAry.add(i);
					}
				}
				g.setColor(new Color(0, 0, 255));
				float[] dash = {5.0f, 10.0f};
				for (int i = 0; i < tmpIndexAry.size() - 1; i++)
				{
					g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
					g.drawLine(xxAry[tmpIndexAry.get(i)], yyAry[tmpIndexAry.get(i)], xxAry[tmpIndexAry.get(i+1)], yyAry[tmpIndexAry.get(i+1)]);
					// 绘制三角
					g.setStroke(new BasicStroke(1.0f));
					int[] xPoints = {(xxAry[tmpIndexAry.get(i)]), (xxAry[tmpIndexAry.get(i)] - 5),
							(xxAry[tmpIndexAry.get(i)] + 5)};
					int[] yPoints = {(yyAry[tmpIndexAry.get(i)] - 5), (yyAry[tmpIndexAry.get(i)] + 5),
							(yyAry[tmpIndexAry.get(i)] + 5)};
					g.drawPolygon(xPoints, yPoints, xPoints.length);
				}
				// 绘制三角
				g.setStroke(new BasicStroke(1.0f));
				int[] xPoints = {(xxAry[tmpIndexAry.get(tmpIndexAry.size() - 1)]),
						(xxAry[tmpIndexAry.get(tmpIndexAry.size() - 1)] - 5),
						(xxAry[tmpIndexAry.get(tmpIndexAry.size() - 1)] + 5)};
				int[] yPoints = {(yyAry[tmpIndexAry.get(tmpIndexAry.size() - 1)] - 5),
						(yyAry[tmpIndexAry.get(tmpIndexAry.size() - 1)] + 5),
						(yyAry[tmpIndexAry.get(tmpIndexAry.size() - 1)] + 5)};
				g.drawPolygon(xPoints, yPoints, xPoints.length);
			}
		}

		// TODO 压高曲线
		indexAry.clear();
		for (int i = 0; i < HHData.length; i++)
		{
			if (HHData[i] != 99990.0)
			{
				indexAry.add(i);
			}
		}
		if (indexAry.size() > 1)
		{
			int k200 = 0;
			for (int i = 0; i < indexAry.size(); i++)
			{
				if (pressData[indexAry.get(i)] >= 200.0)
				{
					k200 = k200 + 1;
				}
				else
				{
					break;
				}
			}
			if (k200 > 0)
			{
				int[] xxAry = new int[k200];
				int[] yyAry = new int[k200];
				for (int i = 0; i < k200; i++)
				{
					xxAry[i] = (int)(xMargin + (125.0 - HHData[indexAry.get(i)] / 100.0) * deltX);
					yyAry[i] = (int)(yMargin + 10 * (Math.log(1000.0 / pressData[indexAry.get(i)]) - Math.log(1000.0 / 1050.0)) * deltY);
					yyAry[i] = (int)(imageHeight - yyAry[i]);    // Y轴正向朝下
				}
				g.setColor(new Color(0, 0, 0));
				g.setStroke(new BasicStroke(1.0f));
				g.drawPolyline(xxAry, yyAry, xxAry.length);
				// 绘制◇
				for (int i = 0; i < xxAry.length; i++)
				{
					int[] xPoints = {xxAry[i], (xxAry[i] - 5), xxAry[i], (xxAry[i] + 5)};
					int[] yPoints = {(yyAry[i] - 5), yyAry[i], (yyAry[i] + 5), yyAry[i]};
					g.drawPolygon(xPoints, yPoints, xPoints.length);
				}
			}

			int k250 = 0;
			for (int i = 0; i < indexAry.size(); i++)
			{
				if (pressData[indexAry.get(i)] > 250.0)
				{
					k250 = k250 + 1;
				}
				else
				{
					break;
				}
			}
			if ((indexAry.size() - k250) > 1)
			{
				int[] xxAry = new int[indexAry.size() - k250];
				int[] yyAry = new int[indexAry.size() - k250];
				for (int i = 0; i < (indexAry.size() - k250); i++)
				{
					xxAry[i] = (int)(xMargin + (125.0 - (HHData[indexAry.get(i + k250)] - 9000.0) / 100.0) * deltX);
					yyAry[i] = (int)(yMargin + 10 * (Math.log(250.0 / pressData[indexAry.get(i + k250)]) - Math.log(1000.0 / 1050.0)) * deltY);
					yyAry[i] = (int)(imageHeight - yyAry[i]);    // Y轴正向朝下
				}
			}
		}

		// TODO 绘制状态曲线
		if (ATData[0] != 9999.0 && TDData[0] != 9999.0)
		{
			// 确定凝结高度点的位置：TL、PL
			double pl = 0.0;
			double tl = 0.0;
			if (TDData[0] >= ATData[0])
			{
				pl = pressData[0];
				tl = ATData[0];
			}
			else
			{
				double[] res = tpl(pressData[0], ATData[0], TDData[0]);
				tl = res[0];
				pl = res[1];
			}
			int x1 = (int)(xMargin + (tl + 85.0) * deltX);
			int y1 = (int)(yMargin + 10 * (Math.log(1000.0 / pl) - Math.log(1000.0 / 1050.0)) * deltY);
			y1 = (int)(imageHeight - y1);    // Y轴正向朝下
			// 绘制*
			g.setColor(new Color(255, 0, 0));
			g.setStroke(new BasicStroke(1.0f));
			g.drawLine((x1 - 5), y1, (x1 + 5), y1);
			g.drawLine(x1, (y1 - 5), x1, (y1 + 5));
			g.drawLine((x1 - 5), (y1 - 5), (x1 + 5), (y1 + 5));
			g.drawLine((x1 + 5), (y1 - 5), (x1 - 5), (y1 + 5));

			// 绘制等克线
			if (TDData[0] < ATData[0])
			{
				int num = (int)(pressData[0] - pl) / 10 + 2;
				int[] xxAry = new int[num];
				int[] yyAry = new int[num];
				double[] c = new double[num];
				double a = 9.5;
				double b = 265.5;
				double aa = 7.5;
				double bb = 237.3;
				xxAry[0] = (int)(xMargin + (TDData[0] + 85.0) * deltX);
				yyAry[0] = (int)(yMargin + 10 * (Math.log(1000.0 / pressData[0]) - Math.log(1000.0 / 1050.0)) * deltY);
				yyAry[0] = (int)(imageHeight - yyAry[0]);    // Y轴正向朝下
				double qs0 = qs(pressData[0], TDData[0]);
				for (int i = 1; i < num - 1; i++)
				{
					c[i] = Math.log10((qs0 * (pressData[0] - i * 10))
							/ (1000.0 * 6.1078 * (287.0 / 461.5 + qs0 * (1.0 - 287.0 / 461.5) / 1000.0)));
					double t = b * c[i] / (a - c[i]);
					if (t > 0.0075)
					{
						t = bb * c[i] / (aa - c[i]);
					}
					xxAry[i] = (int)(xMargin + (t + 85.0) * deltX);
					yyAry[i] = (int)(yMargin + 10 * (Math.log(1000.0 / (pressData[0] - i * 10)) - Math.log(1000.0 / 1050.0)) * deltY);
					yyAry[i] = (int)(imageHeight - yyAry[i]);    // Y轴正向朝下
				}
				xxAry[num-1] = x1;
				yyAry[num-1] = y1;

				g.setColor(new Color(255, 0, 0));
				float[] dash = {5.0f, 10.0f};
				g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
				g.drawPolyline(xxAry, yyAry, xxAry.length);
			}

			// 绘制等位温线
			if (TDData[0] < ATData[0])
			{
				double rd = 287.0;
				double cpd = 1004.0;
				int num = (int)(pressData[0] - pl) / 10 + 2;
				int[] xxAry = new int[num];
				int[] yyAry = new int[num];
				xxAry[0] = (int)(xMargin + (ATData[0] + 85.0) * deltX);
				yyAry[0] = (int)(yMargin + 10 * (Math.log(1000.0 / pressData[0]) - Math.log(1000.0 / 1050.0)) * deltY);
				yyAry[0] = (int)(imageHeight - yyAry[0]);     // Y轴正向朝下
				double seta = seta(pressData[0], ATData[0]);
				for (int i = 1; i < num - 1; i++)
				{
					xxAry[i] = (int)(xMargin + (Math.exp(Math.log(seta) - rd * Math.log(1000.0 / (pressData[0] - i * 10)) / cpd)
							- 273.16 + 85.0) * deltX);
					yyAry[i] = (int)(yMargin + 10 * (Math.log(1000.0 / (pressData[0] - i * 10)) - Math.log(1000.0 / 1050.0)) * deltY);
					yyAry[i] = (int)(imageHeight - yyAry[i]);    // Y轴正向朝下
				}
				xxAry[num-1] = x1;
				yyAry[num-1] = y1;

				g.setColor(new Color(255, 0, 0));
				g.setStroke(new BasicStroke(1.0f));
				g.drawPolyline(xxAry, yyAry, xxAry.length);
			}

			// 绘制等假相当位温线
			int num = (int)(pl - 200) / 10 + 2;
			int[] xxAry = new int[num];
			int[] yyAry = new int[num];
			double[] tsetaseAry = new double[num];
			xxAry[0] = (int)(xMargin + (tl + 85.0) * deltX);
			yyAry[0] = (int)(yMargin + 10 * (Math.log(1000.0 / pl) - Math.log(1000.0 / 1050.0)) * deltY);
			yyAry[0] = (int)(imageHeight - yyAry[0]);     // Y轴正向朝下
			for (int i = 1; i < num - 1; i++)
			{
				tsetaseAry[i] = t_setase(setase(pl, tl), (pl - i * 10.0));
			}
			tsetaseAry[num-1] = t_setase(setase(pl, tl), 200.0);

			int kMin = num;
			for (int i = 0; i < tsetaseAry.length; i++)
			{
				if (Math.abs(tsetaseAry[i] - -9999.0) < 10e-5)
				{
					kMin = i;
					break;
				}
			}
			if (kMin == num)
			{
				for (int i = 1; i < num; i++)
				{
					xxAry[i] = (int)(xMargin + (tsetaseAry[i] + 85) * deltX);
					if (i == (num - 1))
					{
						yyAry[i] = (int)(yMargin + 10 * (Math.log(1000.0 / 200.0) - Math.log(1000.0 / 1050.0)) * deltY);
					}
					else
					{
						yyAry[i] = (int)(yMargin + 10 * (Math.log(1000.0 / (pl - i * 10.0)) - Math.log(1000.0 / 1050.0)) * deltY);
					}
					yyAry[i] = (int)(imageHeight - yyAry[i]);     // Y轴正向朝下
				}
				g.setColor(new Color(255, 0, 0));
				g.setStroke(new BasicStroke(1.0f));//可以把对g的属性设置放在循环外面 不用每次循环都重新设置 db_x
				g.drawPolyline(xxAry, yyAry, xxAry.length);
			}
			if (kMin < num)
			{
				xxAry[kMin] = 100;
				yyAry[kMin] = (int)(yMargin + 10 * (Math.log(1000.0 / p_setase(setase(pl, tl), -85.0)) - Math.log(1000.0 / 1050.0)) * deltY);
				yyAry[kMin] = (int)(imageHeight - yyAry[kMin]);     // Y轴正向朝下
				for (int i = 1; i < kMin; i++)
				{
					xxAry[i] = (int)(xMargin + (tsetaseAry[i] + 85) * deltX);
					if (i == (num - 1))
					{
						yyAry[i] = (int)(yMargin + 10 * (Math.log(1000.0 / 200.0) - Math.log(1000.0 / 1050.0)) * deltY);
					}
					else
					{
						yyAry[i] = (int)(yMargin + 10 * (Math.log(1000.0 / (pl - i * 10.0)) - Math.log(1000.0 / 1050.0)) * deltY);
					}
					yyAry[i] = (int)(imageHeight - yyAry[i]);     // Y轴正向朝下
				}
				g.setColor(new Color(255, 0, 0));
				g.setStroke(new BasicStroke(1.0f));//同样可以把对g的属性设置放在循环外面，并且与上面一致 应该不用设置了吧？   db_x
				for (int i = 0; i < kMin; i++)
				{
					g.drawLine(xxAry[i], yyAry[i], xxAry[i+1], yyAry[i+1]);
				}
			}
			//添加各种指数
			// 沙氏指数
			font = new Font("宋体", Font.BOLD, 17);
			g.setFont(font);
			g.setColor(new Color(5, 5, 50));
			double SSI = CalculateSSI(pressData, ATData, pl, tl);
			if (SSI != -9999.0)
			{
				String text = "SSI = " + String.format("%01.1f", SSI) + "(沙氏指数)";
				g.drawString(text, (txAry[92]), (pyAry[74] + 5));
			}
			// 气团指数
			double K = CalculateK(pressData, ATData, TDData);
			if (K != -9999.0)
			{
				String text = "K = " + String.format("%01.1f", K) + "(气团指数)";
				g.drawString(text, (txAry[92]), (pyAry[76] + 5));
			}
			//强天气威胁指数
			double SW = CalculateSW(pressData, ATData, TDData, WSData, WDData);
			if (SW != -9999.0)
			{
				String text = "SW = " + String.format("%01.1f", SW) + "(威胁指数)";
				g.drawString(text, (txAry[92]), (pyAry[72] + 5));
			}
			//RI,理查逊数500~850hPa
			double RI = CalculateRI(pressData, ATData, WSData, WDData);
			if (RI != -9999.0)
			{
				String text = "RI = " + String.format("%01.1f", RI) + "(理查逊数500~850hPa)";
				g.drawString(text, (txAry[92]), (pyAry[70] + 5));
			}
			//850hPa假相当位温
			double tb1 = CalculateTB(850, pressData, ATData, TDData);
			if (tb1 != -9999.0)
			{
				String text = "850hPa假相当位温:" + String.format("%01.1f", tb1) + "(°K)";
				g.drawString(text, (txAry[92]), (pyAry[58] + 5));
			}
			//700hPa假相当位温
			double tb2 = CalculateTB(700, pressData, ATData, TDData);
			if (tb2 != -9999.0)
			{
				String text = "700hPa假相当位温:" + String.format("%01.1f", tb2) + "(°K)";
				g.drawString(text, (txAry[92]), (pyAry[56] + 5));
			}
			//500hPa假相当位温
			double tb3 = CalculateTB(500, pressData, ATData, TDData);
			if (tb3 != -9999.0)
			{
				String text = "500hPa假相当位温:" + String.format("%01.1f", tb3) + "(°K)";
				g.drawString(text, (txAry[92]), (pyAry[54] + 5));
			}
			//200hPa假相当位温
			double tb4 = CalculateTB(200, pressData, ATData, TDData);
			if (tb4 != -9999.0)
			{
				String text = "200hPa假相当位温:" + String.format("%01.1f", tb4) + "(°K)";
				g.drawString(text, (txAry[92]), (pyAry[52] + 5));
			}
			//海平面假相当位温
			//double tb0 = getDmTB(station, year, month, day, hour);
			if (tb0 != -9999.0)
			{
				String text = "海平面假相当位温:" + String.format("%01.1f", tb0) + "(°K)";
				g.drawString(text, (txAry[92]), (pyAry[50] + 5));
			}
			//CI 位势稳定度指数  500~850hPa
			double CI = CalculateWindShearCI(850, 500, pressData, ATData, TDData);
			if (CI != -9999.0)
			{
				String text = "CI = " + String.format("%01.1f", CI) + "(位势稳定度500~850hPa)";
				g.drawString(text, (txAry[92]), (pyAry[68] + 5));
			}

			//强垂直风切变500~300hPa
			//int p1 = 700, p2 = 300;
			double WindShearWs = CalculateWindShearWs(500, 300, pressData, HHData, WSData, WDData);
			if (WindShearWs != -9999.0)
			{
				String text1 ="500~300hPa垂直风切变大小:" + String.format("%01.1f", WindShearWs);
				g.drawString(text1, (txAry[92]), (pyAry[66] + 5));
			}
			double WindShearWd = CalculateWindShearWd(500, 300, pressData, WSData, WDData);
			if (WindShearWd != -9999.0)
			{
				String text2 ="500~300hPa垂直风切变方向:" + String.format("%01.1f", WindShearWd);
				g.drawString(text2, (txAry[92]), (pyAry[64] + 5));
			}
			//强垂直风切变700~300hPa
			//int p1 = 700, p2 = 300;
			double WindShearWs2 = CalculateWindShearWs(700, 300, pressData, HHData, WSData, WDData);
			if (WindShearWs != -9999.0)
			{
				String text1 ="700~300hPa垂直风切变大小:" + String.format("%01.1f", WindShearWs2);
				g.drawString(text1, (txAry[92]), (pyAry[62] + 5));
			}
			double WindShearWd2 = CalculateWindShearWd(700, 300, pressData, WSData, WDData);
			if (WindShearWd != -9999.0)
			{
				String text2 ="700~300hPa垂直风切变方向:" + String.format("%01.1f", WindShearWd2);
				g.drawString(text2, (txAry[92]), (pyAry[60] + 5));
			}
		}

		// TODO 图解
		font = new Font("宋体", Font.PLAIN, 16);
		g.setFont(font);
		g.setColor(new Color(0, 0, 255));
		g.drawString("蓝实线为温压曲线", txAry[101], (pyAry[81] + 6));
		g.setColor(new Color(0, 0, 255));
		g.drawString("蓝虚线为露压曲线", txAry[101], (pyAry[80] + 7));
		g.setColor(new Color(0, 0, 0));
		g.drawString("黑实线为压高曲线", txAry[101], (pyAry[79] + 8));
		g.setColor(new Color(255, 0, 0));
		g.drawString("红实线为状态曲线", txAry[101], (pyAry[78] + 10));
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	/**
	 * TODO 以下是绘制温度压力对数图所用函数
	 */

	/**
	 *
	 * @param p
	 * @param t
	 *
	 * @return
	 */
	protected static double qs(double p, double t)
	{
		double a = 9.5;
		double b = 265.5;
		double aa = 7.5;
		double bb = 237.3;
		// double t0 = 273.16;
		double es = 6.1078 * Math.pow(10.0, (a * t / (b + t)));;
		if (t > 0.0075)
		{
			es = 6.1078 * Math.pow(10.0, (aa * t / (bb + t)));
		}
		double qs = 1000.0 * (287.0 / 461.5) * es / (p - (1 - 287.0 / 461.5) * es);

		return qs;
	}

	/**
	 *
	 * @param p
	 * @param t
	 *
	 * @return
	 */
	protected static double seta(double p, double t)
	{
		double rd = 287.0;
		double cpd = 1004.0;
		double t0 = 273.16;
		double seta = (t0 + t) * Math.pow((1000.0 / p), (rd / cpd));

		return seta;
	}

	/**
	 *
	 * @param p
	 * @param t
	 *
	 * @return
	 */
	protected static double setase(double p, double t)
	{
		double rd = 287.0;
		double cpd = 1004;
		double a = 9.5;
		double b = 265.5;
		double aa = 7.5;
		double bb = 237.3;
		double t0 = 273.16;
		double lv = 2500000.0 - 2368.0 * t;
		double es = 6.1078 * Math.pow(10.0, (a * t / (b + t)));
		if (t > 0.0075)
		{
			es = 6.1078 * Math.pow(10.0, (aa * t / (bb + t)));
		}
		double ws = 0.622 * es / (p - es);
		double setase = (t0 + t) * Math.pow((1000.0 / (p - es)), (rd / cpd)) * Math.exp(lv * ws / (cpd * (t0 + t)));

		return setase;
	}

	/**
	 *
	 * @param p
	 * @param t
	 * @param td
	 *
	 * @return
	 */
	protected static double[] tpl(double p, double t, double td)
	{
		double[] tpl = new double[2];
		double a = 9.5;
		double b = 265.5;
		double aa = 7.5;
		double bb = 237.3;
		double t0 = 273.16;
		double rd = 287.0;
		double cpd = 1004.0;
		double delt = -0.01;
		double q0 = qs(p, td) * 0.001;
		double seta0 = seta(p, t);
		double tl = td;

		double seta = 0.0;
		do
		{
			tl = tl + delt;
			double es = 6.1078 * Math.pow(10.0, (a * tl / (b + tl)));
			if (tl > 0.0075)
			{
				es = 6.1078 * Math.pow(10.0, (aa * tl / (bb + tl)));
			}
			seta = (tl + t0) * Math.pow((1000.0 / (es + 0.622 * es / q0)), (rd / cpd));
		}
		while (seta < seta0);

		if (seta > seta0)
		{
			tl = tl + 0.005;
		}
		/*if (seta == seta0)
		{
			tl = tl;
		}*/
		double pl = Math.exp(Math.log(1000.0) + (Math.log(tl + t0) - Math.log(seta0)) / (rd / cpd));
		tpl[0] = tl;
		tpl[1] = pl;

		return tpl;
	}

	/**
	 *
	 * @param setase
	 * @param p
	 *
	 * @return
	 */
	protected static double t_setase(double setase, double p)
	{
		double t = -84.5;
		if (setase(p, -84.5) > setase)
		{
			return -9999.0;
		}
		else
		{
			double delt = 0.1;

			do
			{
				t = t + delt;
			}
			while (setase(p, t) < setase);

			if (setase(p, t) > setase)
			{
				double res = t - 0.1 + 0.1 * (setase - setase(p, (t - 0.1))) / (setase(p, t) - setase(p, (t-0.1)));
				return res;
			}
			// if (this.setase(p, t) == setase)
			return t;
		}
	}

	/**
	 *
	 * @param setase
	 * @param t
	 *
	 * @return
	 */
	protected static double p_setase(double setase, double t)
	{
		double p = 1050.0;
		double delp = -1.0;

		do
		{
			p = p + delp;
		}
		while (setase(p, t) < setase);

		if (setase(p, t)> setase)
		{
			double res = p + 1.0 - 1.0 * (setase - setase((p+1), t)) / (setase(p, t) - setase((p+1), t));
			return res;
		}
		// if (this.setase(p, t) == setase)

		return p;
	}

	/**
	 * 计算沙氏指数
	 *
	 * @param psAry 各个层次的气压
	 * @param ttAry 各个层次的温度
	 * @param pl    凝结高度点的位置气压(TLogP图中状态曲线中底部虚线与实线交汇点)
	 * @param tl    凝结高度点的位置温度(TLogP图中状态曲线中底部虚线与实线交汇点)
	 *
	 * @return 返回-9999.0表示无计算结果
	 */
	protected static double CalculateSSI(double[] psAry, double[] ttAry, double pl, double tl)
	{
		if (psAry.length != ttAry.length)
		{
			return -9999.0;
		}

		boolean has500 = false;
		boolean has850 = false;
		double t500 = 0.0;
		//double t850 = 0.0;
		for (int i = 0; i < psAry.length - 1; i++)
		{
			if ((int)(psAry[i]) == 500)
			{
				has500 = true;
				if (Math.abs(ttAry[i] - 9999.0) < 10e-5)
				{
					return -9999.0;
				}
				else
				{
					t500 = ttAry[i];
				}
			}
			if ((int)(psAry[i]) == 850)
			{
				has850 = true;
				if (Math.abs(ttAry[i] - 9999.0) < 10e-5)
				{
					return -9999.0;
				}
				else
				{
					//t850 = ttAry[i];
				}
			}
		}
		if (has500 && has850)
		{
			double ts = t_setase(setase(pl, tl), 500.0);
			double res = t500 - ts;
			return res;
		}
		else
		{
			return -9999.0;
		}
	}

	/**
	 * 计算气团指数(K指数)
	 *
	 * @param psAry 各个层次的气压
	 * @param ttAry 各个层次的温度
	 * @param tdAry 各个层次的露点温度
	 *
	 * @return 返回-9999.0表示无计算结果
	 */
	protected static double CalculateK(double[] psAry, double[] ttAry, double[] tdAry)
	{
		boolean has500 = false;
		boolean has700 = false;
		boolean has850 = false;
		double t500 = 0.0;
		double t700 = 0.0;
		double t850 = 0.0;
		double td700 = 0.0;
		double td850 = 0.0;
		for (int i = 0; i < psAry.length - 1; i++)
		{
			if ((int)(psAry[i]) == 500)
			{
				has500 = true;
				if (Math.abs(ttAry[i] - 9999.0) < 10e-5)
				{
					return -9999.0;
				}
				else
				{
					t500 = ttAry[i];
				}
			}
			if ((int)(psAry[i]) == 700)
			{
				has700 = true;
				if (Math.abs(ttAry[i] - 9999.0) < 10e-5)
				{
					return -9999.0;
				}
				else
				{
					t700 = ttAry[i];
				}
				if (Math.abs(tdAry[i] - 9999.0) < 10e-5)
				{
					return -9999.0;
				}
				else
				{
					td700 = tdAry[i];
				}
			}
			if ((int)(psAry[i]) == 850)
			{
				has850 = true;
				if (Math.abs(ttAry[i] - 9999.0) < 10e-5)
				{
					return -9999.0;
				}
				else
				{
					t850 = ttAry[i];
				}
				if (Math.abs(tdAry[i] - 9999.0) < 10e-5)
				{
					return -9999.0;
				}
				else
				{
					td850 = tdAry[i];
				}
			}
		}
		if (has500 && has700 && has850)
		{
			double res = t850 - t500 + td850 - t700 + td700;
			return res;
		}
		else
		{
			return -9999.0;
		}
	}
	/*****************Begin***********************db_x*******************************/

	/**
	 * 计算强天气威胁指数
	 * @param psAry 各个层次的气压
	 * @param ttAry 各个层次的温度
	 * @param tdAry 各个层次的露点温度
	 * @param wsAry 各个层次的风速
	 * @param wdAry 各个层次的风向
	 * @return 返回-9999.0表示无计算结果
	 */
	protected static double CalculateSW(double[] psAry, double[] ttAry, double[] tdAry,
										double[] wsAry, double[] wdAry){
		boolean has500 = false;
		boolean has850 = false;
		double t500 = 0.0,
				t850 = 0.0,
				td850 = 0.0,
				ws500 = 0.0,
				wd500 = 0.0,
				ws850 = 0.0,
				wd850 = 0.0;

		for (int i = 0; i < psAry.length; i++) {
			if ((int)(psAry[i]) == 500) {
				has500 = true;
				if (Math.abs(ttAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					t500 = ttAry[i];
				}
				if (Math.abs(wsAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					ws500 = wsAry[i];
				}
				if (Math.abs(wdAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					wd500 = wdAry[i];
				}
			}
			if ((int)(psAry[i]) == 850) {
				has850 = true;
				if (Math.abs(ttAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					t850 = ttAry[i];
				}
				if (Math.abs(tdAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					td850 = tdAry[i];
				}
				if (Math.abs(wsAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					ws850 = wsAry[i];
				}
				if (Math.abs(wdAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					wd850 = wdAry[i];
				}
			}
		}
		if (has500 && has850) {
			double result = WeatherElemSinglePointCalc.swCalc(td850, t850, t500, ws850, wd850, ws500, wd500);
			return result;
		} else{
			return -9999.0;
		}
	}
	/**
	 * 计算两等压层的理查逊数 （此处暂时定为500——850hPa 以后可以设为参数）
	 * @param psAry 各个层次的气压
	 * @param ttAry 各个层次的温度
	 * @param wsAry 各个层次的风速
	 * @param wdAry 各个层次的风向
	 * @return 返回-9999.0表示无计算结果
	 */
	protected static double CalculateRI(double[] psAry, double[] ttAry,
										double[] wsAry, double[] wdAry) {
		int p1 = 500, p2 = 850;
		boolean has500 = false;
		boolean has850 = false;
		double t500 = 0.0,
				t850 = 0.0,
				ws500 = 0.0,
				wd500 = 0.0,
				ws850 = 0.0,
				wd850 = 0.0;
		for (int i = 0; i < psAry.length; i++) {
			if ((int)(psAry[i]) == p1) {
				has500 = true;
				if (Math.abs(ttAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					t500 = ttAry[i];
				}
				if (Math.abs(wsAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					ws500 = wsAry[i];
				}
				if (Math.abs(wdAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					wd500 = wdAry[i];
				}
			}
			if ((int)(psAry[i]) == p2) {
				has850 = true;
				if (Math.abs(ttAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					t850 = ttAry[i];
				}
				if (Math.abs(wsAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					ws850 = wsAry[i];
				}
				if (Math.abs(wdAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					wd850 = wdAry[i];
				}
			}
		}
		if (has500 && has850) {
			double result = WeatherElemSinglePointCalc.ri(p1, p2, t500, t850, wd500, wd850, ws500, ws850, true);
			return result;
		} else {
			return -9999.0;
		}
	}

	/**
	 * 计算两大气压层间的垂直风切变大小 单位：s-1 量级 1e-3
	 * @param p1 下面大气压层
	 * @param p2 上面大气压层
	 * @param psAry 各个层次的气压
	 * @param hhAry 各个层次的位势高度
	 * @param wsAry 各个层次的风速
	 * @param wdAry 各个层次的风向
	 * @return 返回-9999.0表示无计算结果
	 */
	protected static double CalculateWindShearWs(int p1, int p2, double[] psAry,
												 double[] hhAry, double[] wsAry, double[] wdAry){
		boolean hasP1 = false;
		boolean hasP2 = false;
		double  hhP1 = 0.0,
				hhP2 = 0.0,
				wsP1 = 0.0,
				wdP1 = 0.0,
				wsP2 = 0.0,
				wdP2 = 0.0;
		for (int i = 0; i < psAry.length; i++) {
			if ((int)(psAry[i]) == p1) {
				hasP1 = true;
				if (Math.abs(hhAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					hhP1 = hhAry[i];
				}
				if (Math.abs(wsAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					wsP1 = wsAry[i];
				}
				if (Math.abs(wdAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					wdP1 = wdAry[i];
				}
			}
			if ((int)(psAry[i]) == p2) {
				hasP2 = true;
				if (Math.abs(hhAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					hhP2 = hhAry[i];
				}
				if (Math.abs(wsAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					wsP2 = wsAry[i];
				}
				if (Math.abs(wdAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					wdP2 = wdAry[i];
				}
			}
		}
		if (hasP1 && hasP2) {
			double result = WeatherElemSinglePointCalc.windShearWS(wdP1, wsP1, hhP1, wdP2, wsP2, hhP2, true);
			return result;
		}else {
			return -9999.0;
		}
	}
	/**
	 * 计算两大气压层间的垂直风切变方向   单位：°
	 * @param p1 下面大气压层
	 * @param p2 上面大气压层
	 * @param psAry 各个层次的气压
	 * @param wsAry 各个层次的风速
	 * @param wdAry 各个层次的风向
	 * @return 返回-9999.0表示无计算结果
	 */
	protected static double CalculateWindShearWd(int p1, int p2, double[] psAry,
												 double[] wsAry, double[] wdAry){
		boolean hasP1 = false;
		boolean hasP2 = false;
		double  wsP1 = 0.0,
				wdP1 = 0.0,
				wsP2 = 0.0,
				wdP2 = 0.0;
		for (int i = 0; i < psAry.length; i++) {
			if ((int)(psAry[i]) == p1) {
				hasP1 = true;
				if (Math.abs(wsAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					wsP1 = wsAry[i];
				}
				if (Math.abs(wdAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					wdP1 = wdAry[i];
				}
			}
			if ((int)(psAry[i]) == p2) {
				hasP2 = true;
				if (Math.abs(wsAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					wsP2 = wsAry[i];
				}
				if (Math.abs(wdAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					wdP2 = wdAry[i];
				}
			}
		}
		if (hasP1 && hasP2) {
			double result = WeatherElemSinglePointCalc.windShearWD(wdP1, wsP1, wdP2, wsP2, true);
			return result;
		}else {
			return -9999.0;
		}
	}
	/**
	 * 计算两大气层的位势稳定度指数  单位：°C
	 * @param p1 上面大气压层
	 * @param p2 下面大气压层
	 * @param psAry 各个层次的气压
	 * @param ttAry 各个层次的温度
	 * @param tdAry 各个层次的露点温度
	 * @return 返回-9999.0表示无计算结果
	 */
	protected static double CalculateWindShearCI(int p1, int p2, double[] psAry,
												 double[] ttAry, double[] tdAry){
		boolean hasP1 = false;
		boolean hasP2 = false;
		double  ttP1 = 0.0,
				tdP1 = 0.0,
				ttP2 = 0.0,
				tdP2 = 0.0;
		for (int i = 0; i < psAry.length; i++) {
			if ((int)(psAry[i]) == p1) {
				hasP1 = true;
				if (Math.abs(ttAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					ttP1 = ttAry[i];
				}
				if (Math.abs(tdAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					tdP1 = tdAry[i];
				}
			}
			if ((int)(psAry[i]) == p2) {
				hasP2 = true;
				if (Math.abs(ttAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					ttP2 = ttAry[i];
				}
				if (Math.abs(tdAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					tdP2 = tdAry[i];
				}
			}
		}
		if (hasP1 && hasP2) {
			double tbUp = WeatherElemSinglePointCalc.tb(ttP1, p1, tdP1, false);
			double tbDown = WeatherElemSinglePointCalc.tb(ttP2, p2, tdP2, false);
			double result = tbUp - tbDown;
			return result;
		}else {
			return -9999.0;
		}
	}
	/**
	 * 计算某大气层上假相当位温  (绝对温度)单位°K
	 * @param p1 上面大气压层
	 * @param psAry 各个层次的气压
	 * @param ttAry 各个层次的温度
	 * @param tdAry 各个层次的露点温度
	 * @return 返回-9999.0表示无计算结果
	 */
	protected static double CalculateTB(int p1, double[] psAry,
										double[] ttAry, double[] tdAry){
		boolean hasP1 = false;
		double  ttP1 = 0.0,
				tdP1 = 0.0;
		for (int i = 0; i < psAry.length; i++) {
			if ((int)(psAry[i]) == p1) {
				hasP1 = true;
				if (Math.abs(ttAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					ttP1 = ttAry[i];
				}
				if (Math.abs(tdAry[i] - 9999.0) < 10e-5 ) {
					return -9999.0;
				}else {
					tdP1 = tdAry[i];
				}
			}
		}
		if (hasP1) {
			double tbUp = WeatherElemSinglePointCalc.tb(ttP1, p1, tdP1, false);
			double result = tbUp;
			return result;
		}else {
			return -9999.0;
		}
	}
	/**
	 * 获取海平面气压数据计算假相当位温(绝对温度) 单位°K
	 *
	 */
	/*public static double getDmTB(Station station, String year, String month, String day, String hour){
		double result = 0.0;
		double slp, at, td;						//SLP -> PR
		ElemCode[] selectCols = new ElemCode[]{ElemCode.SLP, ElemCode.AT, ElemCode.TD};
		// 获取数据
		ScatterCondition condition = new ScatterCondition();
		condition.setStationList(new String[]{station.getID()});
		condition.setDataType(DataType.DMQX);
		condition.setYear(year);
		condition.setMonth(month);
		condition.setDay(day);
		condition.setHour(hour);
		condition.setTimeZone(TimeZone.UTC_8);
		condition.setSelectColumn(selectCols);
		ScatterDataReader dr = new ScatterDataReader();
		dr.setCondition(condition);
		ArrayList<MIDSData> dataList = new ArrayList<MIDSData>();
		if (dr.getData() == null)
		{
			return -9999.0;
		}
		for (MIDSData md : dr.getData())
		{
			dataList.add(md);
		}
		ScatterData sd = (ScatterData)dataList.get(0);
		String[][] sourceData = sd.getAllData();
		String slpstring = sourceData[0][0];
		String atstring = sourceData[0][1];
		String tdstring = sourceData[0][2];
		if(slpstring==null||slpstring.isEmpty()||
				atstring==null||atstring.isEmpty()||
				tdstring==null||tdstring.isEmpty()){
			return -9999.0;
		}
		slp = Double.parseDouble(slpstring);
		at = Double.parseDouble(atstring);
		td = Double.parseDouble(tdstring);
		result = WeatherElemSinglePointCalc.tb(at, slp, td, false);
		return result;
	}*/

/***********************End**********************db_x******************************/

	/**
	 *
	 * @return
	 */
	protected static double[][] gettsetaseAry()
	{
		double[][] tesetaseAry = {
				{-42.4563, -43.0786, -43.7057, -44.3377, -44.9746, -45.6166, -46.2636, -46.9157, -47.5730, -48.2356, -48.9035, -49.5769,
						-50.2557, -50.9402, -51.6303, -52.3261, -53.0279, -53.7355, -54.4493, -55.1691, -55.8953, -56.6277, -57.3667,
						-58.1123, -58.8646, -59.6238, -60.3900, -61.1632, -61.9438, -62.7318, -63.5274, -64.3308, -65.1421, -65.9614,
						-66.7891, -67.6252, -68.4700, -69.3237, -70.1864, -71.0585, -71.9402, -72.8317, -73.7332, -74.6451, -75.5676,
						-76.5010, -77.4456, -78.4018, -79.3699, -80.3502, -81.3431, -82.3490, -83.3683, -84.4016, -9999.50, -9999.50,
						-9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50,
						-9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50,
						-9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50},
				{-42.4563, -43.0786, -43.7057, -44.3377, -44.9746, -45.6166, -46.2636, -46.9157, -47.5730, -48.2356, -48.9035, -49.5769,
						-50.2557, -50.9402, -51.6303, -52.3261, -53.0279, -53.7355, -54.4493, -55.1691, -55.8953, -56.6277, -57.3667,
						-58.1123, -58.8646, -59.6238, -60.3900, -61.1632, -61.9438, -62.7318, -63.5274, -64.3308, -65.1421, -65.9614,
						-66.7891, -67.6252, -68.4700, -69.3237, -70.1864, -71.0585, -71.9402, -72.8317, -73.7332, -74.6451, -75.5676,
						-76.5010, -77.4456, -78.4018, -79.3699, -80.3502, -81.3431, -82.3490, -83.3683, -84.4016, -9999.50, -9999.50,
						-9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50,
						-9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50,
						-9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50},
				{-37.5067, -38.1362, -38.7708, -39.4106, -40.0558, -40.7063, -41.3623, -42.0237, -42.6907, -43.3633, -44.0415, -44.7255,
						-45.4154, -46.1112, -46.8129, -47.5207, -48.2347, -48.9549, -49.6814, -50.4144, -51.1540, -51.9002, -52.6531,
						-53.4130, -54.1799, -54.9539, -55.7351, -56.5238, -57.3200, -58.1239, -58.9357, -59.7554, -60.5834, -61.4197,
						-62.2645, -63.1181, -63.9806, -64.8523, -65.7333, -66.6239, -67.5243, -68.4348, -69.3557, -70.2871, -71.2294,
						-72.1829, -73.1480, -74.1248, -75.1138, -76.1154, -77.1299, -78.1577, -79.1992, -80.2549, -81.3252, -82.4107,
						-83.5118, -84.6291, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50,
						-9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50,
						-9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50},
				{-32.6322, -33.2652, -33.9038, -34.5482, -35.1983, -35.8542, -36.5161, -37.1839, -37.8576, -38.5375, -39.2234, -39.9155,
						-40.6139, -41.3186, -42.0298, -42.7474, -43.4715, -44.2023, -44.9399, -45.6843, -46.4355, -47.1938, -47.9593,
						-48.7320, -49.5121, -50.2996, -51.0948, -51.8977, -52.7085, -53.5273, -54.3543, -55.1896, -56.0334, -56.8859,
						-57.7472, -58.6176, -59.4972, -60.3863, -61.2850, -62.1937, -63.1124, -64.0415, -64.9813, -65.9319, -66.8938,
						-67.8671, -68.8523, -69.8496, -70.8594, -71.8820, -72.9179, -73.9674, -75.0309, -76.1090, -77.2021, -78.3106,
						-79.4352, -80.5763, -81.7346, -82.9106, -84.1051, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50,
						-9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50,
						-9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50},
				{-27.8678, -28.4995, -29.1374, -29.7817, -30.4324, -31.0894, -31.7529, -32.4229, -33.0994, -33.7826, -34.4725, -35.1691,
						-35.8725, -36.5828, -37.3001, -38.0243, -38.7556, -39.4942, -40.2399, -40.9930, -41.7535, -42.5216, -43.2972,
						-44.0806, -44.8718, -45.6709, -46.4781, -47.2935, -48.1172, -48.9493, -49.7900, -50.6395, -51.4979, -52.3654,
						-53.2421, -54.1282, -55.0239, -55.9295, -56.8451, -57.7709, -58.7073, -59.6543, -60.6124, -61.5817, -62.5626,
						-63.5553, -64.5601, -65.5775, -66.6076, -67.6510, -68.7080, -69.7789, -70.8643, -71.9646, -73.0801, -74.2116,
						-75.3595, -76.5242, -77.7066, -78.9071, -80.1265, -81.3654, -82.6247, -83.9051, -9999.50, -9999.50, -9999.50,
						-9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50,
						-9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50},
				{-23.2563, -23.8809, -24.5125, -25.1510, -25.7966, -26.4492, -27.1089, -27.7758, -28.4500, -29.1314, -29.8202, -30.5165,
						-31.2202, -31.9315, -32.6503, -33.3769, -34.1112, -34.8534, -35.6035, -36.3615, -37.1276, -37.9019, -38.6844,
						-39.4752, -40.2744, -41.0822, -41.8987, -42.7239, -43.5580, -44.4011, -45.2534, -46.1148, -46.9858, -47.8663,
						-48.7566, -49.6568, -50.5671, -51.4877, -52.4188, -53.3607, -54.3134, -55.2773, -56.2527, -57.2397, -58.2387,
						-59.2500, -60.2739, -61.3106, -62.3606, -63.4242, -64.5017, -65.5937, -66.7005, -67.8225, -68.9604, -70.1144,
						-71.2853, -72.4736, -73.6798, -74.9046, -76.1487, -77.4129, -78.6978, -80.0043, -81.3334, -82.6858, -84.0627,
						-9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50,
						-9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50},
				{-18.8446, -19.4559, -20.0748, -20.7013, -21.3355, -21.9773, -22.6271, -23.2847, -23.9503, -24.6239, -25.3056, -25.9955,
						-26.6936, -27.4000, -28.1149, -28.8383, -29.5702, -30.3106, -31.0598, -31.8178, -32.5845, -33.3603, -34.1450,
						-34.9389, -35.7420, -36.5543, -37.3761, -38.2074, -39.0483, -39.8989, -40.7593, -41.6297, -42.5103, -43.4011,
						-44.3023, -45.2141, -46.1366, -47.0701, -48.0146, -48.9704, -49.9378, -50.9169, -51.9080, -52.9113, -53.9271,
						-54.9558, -55.9974, -57.0525, -58.1213, -59.2043, -60.3016, -61.4139, -62.5414, -63.6847, -64.8442, -66.0204,
						-67.2139, -68.4253, -69.6550, -70.9039, -72.1725, -73.4615, -74.7719, -76.1044, -77.4599, -78.8393, -80.2437,
						-81.6741, -83.1317, -84.6179, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50,
						-9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50},
				{-14.6774, -15.2696, -15.8697, -16.4780, -17.0945, -17.7193, -18.3526, -18.9944, -19.6449, -20.3040, -20.9721, -21.6490,
						-22.3350, -23.0300, -23.7343, -24.4479, -25.1708, -25.9032, -26.6453, -27.3969, -28.1583, -28.9296, -29.7107,
						-30.5019, -31.3032, -32.1147, -32.9365, -33.7688, -34.6116, -35.4650, -36.3291, -37.2041, -38.0901, -38.9872,
						-39.8955, -40.8153, -41.7466, -42.6896, -43.6446, -44.6116, -45.5908, -46.5826, -47.5870, -48.6044, -49.6350,
						-50.6791, -51.7368, -52.8087, -53.8948, -54.9957, -56.1116, -57.2430, -58.3903, -59.5538, -60.7342, -61.9318,
						-63.1472, -64.3810, -65.6337, -66.9061, -68.1987, -69.5124, -70.8479, -72.2060, -73.5877, -74.9938, -76.4255,
						-77.8839, -79.3700, -80.8853, -82.4310, -84.0086, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50,
						-9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50},
				{-10.7901, -11.3582, -11.9347, -12.5197, -13.1132, -13.7155, -14.3267, -14.9468, -15.5761, -16.2147, -16.8627, -17.5202,
						-18.1874, -18.8644, -19.5513, -20.2483, -20.9555, -21.6729, -22.4008, -23.1392, -23.8883, -24.6481, -25.4189,
						-26.2006, -26.9935, -27.7976, -28.6130, -29.4399, -30.2784, -31.1285, -31.9904, -32.8643, -33.7502, -34.6483,
						-35.5586, -36.4815, -37.4168, -38.3650, -39.3260, -40.3001, -41.2874, -42.2882, -43.3026, -44.3308, -45.3732,
						-46.4298, -47.5010, -48.5871, -49.6884, -50.8051, -51.9377, -53.0865, -54.2518, -55.4342, -56.6340, -57.8518,
						-59.0880, -60.3433, -61.6181, -62.9132, -64.2292, -65.5668, -66.9269, -68.3102, -69.7177, -71.1502, -72.6089,
						-74.0949, -75.6093, -77.1534, -78.7287, -80.3366, -81.9788, -83.6571, -9999.50, -9999.50, -9999.50, -9999.50,
						-9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50},
				{-7.20302, -7.74434, -8.29397, -8.85219, -9.41908, -9.99485, -10.5797, -11.1737, -11.7772, -12.3902, -13.0130, -13.6458,
						-14.2885, -14.9417, -15.6052, -16.2793, -16.9643, -17.6602, -18.3672, -19.0855, -19.8153, -20.5567, -21.3099,
						-22.0750, -22.8522, -23.6416, -24.4433, -25.2576, -26.0846, -26.9243, -27.7771, -28.6429, -29.5219, -30.4143,
						-31.3203, -32.2398, -33.1732, -34.1206, -35.0820, -36.0578, -37.0479, -38.0527, -39.0723, -40.1069, -41.1567,
						-42.2220, -43.3029, -44.3997, -45.5128, -46.6423, -47.7887, -48.9522, -50.1332, -51.3322, -52.5495, -53.7856,
						-55.0410, -56.3161, -57.6117, -58.9283, -60.2666, -61.6272, -63.0110, -64.4187, -65.8513, -67.3096, -68.7949,
						-70.3080, -71.8503, -73.4230, -75.0276, -76.6656, -78.3386, -80.0484, -81.7969, -83.5864, -9999.50, -9999.50,
						-9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50},
				{-3.92078, -4.43412, -4.95562, -5.48551, -6.02400, -6.57126, -7.12749, -7.69289, -8.26770, -8.85212, -9.44634, -10.0506,
						-10.6651, -11.2900, -11.9257, -12.5722, -13.2299, -13.8988, -14.5794, -15.2718, -15.9761, -16.6927, -17.4217,
						-18.1634, -18.9179, -19.6855, -20.4663, -21.2607, -22.0686, -22.8905, -23.7264, -24.5766, -25.4412, -26.3204,
						-27.2143, -28.1233, -29.0474, -29.9868, -30.9417, -31.9122, -32.8986, -33.9010, -34.9196, -35.9546, -37.0062,
						-38.0747, -39.1601, -40.2629, -41.3831, -42.5212, -43.6773, -44.8518, -46.0450, -47.2573, -48.4891, -49.7408,
						-51.0128, -52.3056, -53.6199, -54.9561, -56.3149, -57.6971, -59.1032, -60.5342, -61.9908, -63.4741, -64.9850,
						-66.5246, -68.0942, -69.6950, -71.3285, -72.9961, -74.6995, -76.4406, -78.2212, -80.0437, -81.9102, -83.8235,
						-9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50},
				{-0.933753, -1.41957, -1.91324, -2.41496, -2.92497, -3.44346, -3.97062, -4.50668, -5.05191, -5.60648, -6.17075, -6.74482,
						-7.32901, -7.92358, -8.52875, -9.14480, -9.77200, -10.4106, -11.0609, -11.7231, -12.3976, -13.0846, -13.7843,
						-14.4971, -15.2232, -15.9630, -16.7166, -17.4844, -18.2666, -19.0634, -19.8753, -20.7023, -21.5449, -22.4031,
						-23.2773, -24.1677, -25.0745, -25.9980, -26.9384, -27.8959, -28.8708, -29.8631, -30.8733, -31.9014, -32.9478,
						-34.0125, -35.0959, -36.1982, -37.3196, -38.4604, -39.6208, -40.8011, -42.0017, -43.2228, -44.4648, -45.7281,
						-47.0132, -48.3204, -49.6503, -51.0034, -52.3804, -53.7817, -55.2082, -56.6605, -58.1396, -59.6464, -61.1817,
						-62.7468, -64.3427, -65.9708, -67.6325, -69.3291, -71.0625, -72.8344, -74.6469, -76.5020, -78.4022, -80.3500,
						-82.3485, -84.4007, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50},
				{1.91912, 1.42872, 0.931429, 0.427013, -0.0846636, -0.598221, -1.09724, -1.60477, -2.12106, -2.64630, -3.18077, -3.72475,
						-4.27848, -4.84224, -5.41626, -6.00091, -6.59646, -7.20319, -7.82143, -8.45147, -9.09357, -9.74820, -10.4155,
						-11.0960, -11.7899, -12.4976, -13.2194, -13.9557, -14.7067, -15.4730, -16.2548, -17.0523, -17.8661, -18.6964,
						-19.5436, -20.4079, -21.2898, -22.1895, -23.1073, -24.0436, -24.9986, -25.9727, -26.9661, -27.9791, -29.0120,
						-30.0650, -31.1384, -32.2325, -33.3475, -34.4837, -35.6415, -36.8209, -38.0225, -39.2464, -40.4930, -41.7627,
						-43.0558, -44.3727, -45.7140, -47.0801, -48.4715, -49.8887, -51.3326, -52.8038, -54.3029, -55.8311, -57.3890,
						-58.9778, -60.5987, -62.2528, -63.9415, -65.6663, -67.4289, -69.2310, -71.0746, -72.9620, -74.8954, -76.8776,
						-78.9114, -81.0000, -83.1472, -9999.50, -9999.50, -9999.50, -9999.50, -9999.50},
				{4.56731, 4.09661, 3.61927, 3.13517, 2.64412, 2.14598, 1.64053, 1.12756, 0.606891, 0.0782534, -0.458470, -0.976007,
						-1.50039, -2.03442, -2.57819, -3.13212, -3.69646, -4.27157, -4.85775, -5.45532, -6.06466, -6.68604, -7.31994,
						-7.96664, -8.62657, -9.30004, -9.98760, -10.6895, -11.4062, -12.1382, -12.8857, -13.6494, -14.4295, -15.2265,
						-16.0410, -16.8732, -17.7235, -18.5925, -19.4806, -20.3881, -21.3155, -22.2632, -23.2316, -24.2210, -25.2318,
						-26.2644, -27.3191, -28.3964, -29.4965, -30.6197, -31.7665, -32.9370, -34.1317, -35.3509, -36.5948, -37.8639,
						-39.1585, -40.4791, -41.8259, -43.1996, -44.6004, -46.0291, -47.4862, -48.9723, -50.4881, -52.0345, -53.6123,
						-55.2224, -56.8660, -58.5443, -60.2584, -62.0100, -63.8005, -65.6319, -67.5058, -69.4247, -71.3908, -73.4067,
						-75.4754, -77.6003, -79.7847, -82.0329, -84.3493, -9999.50, -9999.50, -9999.50},
				{7.00631, 6.55367, 6.09484, 5.62955, 5.15771, 4.67913, 4.19361, 3.70095, 3.20094, 2.69341, 2.17810, 1.65483,
						1.12339, 0.583522, 0.0349503, -0.521259, -1.05607, -1.60098, -2.15649, -2.72279, -3.30025, -3.88931, -4.49029,
						-5.10359, -5.72963, -6.36879, -7.02151, -7.68825, -8.36943, -9.06554, -9.77701, -10.5044, -11.2481, -12.0087,
						-12.7867, -13.5826, -14.3969, -15.2302, -16.0830, -16.9558, -17.8492, -18.7636, -19.6996, -20.6578, -21.6386,
						-22.6426, -23.6701, -24.7218, -25.7980, -26.8993, -28.0260, -29.1786, -30.3575, -31.5630, -32.7957, -34.0559,
						-35.3440, -36.6603, -38.0054, -39.3796, -40.7834, -42.2174, -43.6820, -45.1778, -46.7055, -48.2657, -49.8594,
						-51.4874, -53.1507, -54.8503, -56.5875, -58.3637, -60.1804, -62.0394, -63.9424, -65.8917, -67.8895, -69.9385,
						-72.0416, -74.2020, -76.4234, -78.7099, -81.0659, -83.4967, -9999.50, -9999.50},
				{9.25536, 8.81929, 8.37730, 7.92927, 7.47501, 7.01435, 6.54712, 6.07315, 5.59222, 5.10419, 4.60877, 4.10581,
						3.59506, 3.07629, 2.54930, 2.01384, 1.46959, 0.916368, 0.353828, -0.218271, -0.782711, -1.34128, -1.91108,
						-2.49259, -3.08617, -3.69220, -4.31118, -4.94358, -5.58979, -6.25037, -6.92574, -7.61649, -8.32318, -9.04631,
						-9.78640, -10.5442, -11.3201, -12.1149, -12.9291, -13.7633, -14.6183, -15.4946, -16.3930, -17.3140, -18.2584,
						-19.2267, -20.2197, -21.2379, -22.2820, -23.3527, -24.4505, -25.5760, -26.7298, -27.9124, -29.1244, -30.3663,
						-31.6385, -32.9416, -34.2760, -35.6423, -37.0409, -38.4723, -39.9371, -41.4358, -42.9690, -44.5373, -46.1415,
						-47.7824, -49.4609, -51.1780, -52.9348, -54.7326, -56.5729, -58.4571, -60.3873, -62.3653, -64.3935, -66.4744,
						-68.6109, -70.8062, -73.0640, -75.3882, -77.7835, -80.2550, -82.8087, -9999.50},
				{11.3333, 10.9122, 10.4855, 10.0530, 9.61472, 9.17034, 8.71977, 8.26279, 7.79931, 7.32897, 6.85174, 6.36738,
						5.87565, 5.37633, 4.86921, 4.35403, 3.83059, 3.29864, 2.75777, 2.20789, 1.64857, 1.07961, 0.500644,
						-0.0886854, -0.677323, -1.25246, -1.83976, -2.43971, -3.05278, -3.67941, -4.32015, -4.97554, -5.64613, -6.33242,
						-7.03514, -7.75476, -8.49203, -9.24757, -10.0221, -10.8162, -11.6308, -12.4664, -13.3240, -14.2042, -15.1078,
						-16.0358, -16.9887, -17.9675, -18.9730, -20.0059, -21.0671, -22.1574, -23.2774, -24.4280, -25.6100, -26.8240,
						-28.0706, -29.3506, -30.6647, -32.0133, -33.3971, -34.8168, -36.2728, -37.7658, -39.2964, -40.8652, -42.4729,
						-44.1203, -45.8081, -47.5373, -49.3088, -51.1240, -52.9840, -54.8903, -56.8447, -58.8491, -60.9056, -63.0167,
						-65.1852, -67.4142, -69.7074, -72.0686, -74.5026, -77.0145, -79.6101, -82.2962},
				{13.2574, 12.8497, 12.4368, 12.0185, 11.5945, 11.1648, 10.7293, 10.2878, 9.83996, 9.38581, 8.92505, 8.45755,
						7.98311, 7.50152, 7.01248, 6.51586, 6.01140, 5.49888, 4.97794, 4.44842, 3.91005, 3.36246, 2.80544,
						2.23857, 1.66159, 1.07414, 0.475848, -0.133660, -0.739066, -1.33422, -1.94265, -2.56485, -3.20137, -3.85287,
						-4.51981, -5.20289, -5.90275, -6.62011, -7.35566, -8.11005, -8.88417, -9.67874, -10.4946, -11.3327, -12.1937,
						-13.0787, -13.9885, -14.9242, -15.8867, -16.8769, -17.8958, -18.9445, -20.0238, -21.1349, -22.2786, -23.4560,
						-24.6679, -25.9153, -27.1990, -28.5199, -29.8788, -31.2765, -32.7136, -34.1911, -35.7095, -37.2695, -38.8720,
						-40.5175, -42.2070, -43.9412, -45.7211, -47.5477, -49.4223, -51.3461, -53.3208, -55.3480, -57.4298, -59.5686,
						-61.7670, -64.0281, -66.3552, -68.7524, -71.2242, -73.7757, -76.4128, -79.1422},
				{15.0436, 14.6480, 14.2474, 13.8417, 13.4307, 13.0143, 12.5923, 12.1646, 11.7310, 11.2914, 10.8455, 10.3933,
						9.93439, 9.46877, 8.99619, 8.51635, 8.02912, 7.53422, 7.03144, 6.52052, 6.00123, 5.47316, 4.93615,
						4.38991, 3.83401, 3.26823, 2.69224, 2.10556, 1.50785, 0.898774, 0.277827, -0.355349, -0.969809, -1.58899,
						-2.22280, -2.87178, -3.53659, -4.21786, -4.91636, -5.63284, -6.36801, -7.12273, -7.89785, -8.69425, -9.51287,
						-10.3547, -11.2206, -12.1118, -13.0293, -13.9742, -14.9476, -15.9507, -16.9847, -18.0508, -19.1502, -20.2841,
						-21.4536, -22.6601, -23.9046, -25.1884, -26.5124, -27.8778, -29.2857, -30.7370, -32.2326, -33.7734, -35.3604,
						-36.9943, -38.6759, -40.4063, -42.1863, -44.0169, -45.8991, -47.8342, -49.8236, -51.8689, -53.9720, -56.1349,
						-58.3602, -60.6508, -63.0099, -65.4413, -67.9495, -70.5396, -73.2174, -75.9896},
				{16.7059, 16.3212, 15.9317, 15.5374, 15.1380, 14.7335, 14.3237, 13.9084, 13.4876, 13.0610, 12.6286, 12.1901,
						11.7452, 11.2941, 10.8362, 10.3716, 9.89993, 9.42095, 8.93454, 8.44043, 7.93836, 7.42808, 6.90932,
						6.38172, 5.84506, 5.29911, 4.74331, 4.17750, 3.60131, 3.01423, 2.41601, 1.80618, 1.18426, 0.549811,
						-0.0975834, -0.741866, -1.37440, -2.02246, -2.68666, -3.36779, -4.06656, -4.78375, -5.52029, -6.27699, -7.05485,
						-7.85479, -8.67789, -9.52524, -10.3979, -11.2971, -12.2242, -13.1803, -14.1667, -15.1849, -16.2363, -17.3222,
						-18.4441, -19.6035, -20.8019, -22.0407, -23.3215, -24.6455, -26.0142, -27.4290, -28.8911, -30.4018, -31.9622,
						-33.5735, -35.2366, -36.9527, -38.7228, -40.5479, -42.4291, -44.3675, -46.3645, -48.4214, -50.5399, -52.7220,
						-54.9700, -57.2865, -59.6746, -62.1379, -64.6807, -67.3078, -70.0250, -72.8390},
				{18.2570, 17.8821, 17.5026, 17.1185, 16.7295, 16.3357, 15.9368, 15.5327, 15.1234, 14.7086, 14.2882, 13.8620,
						13.4298, 12.9916, 12.5471, 12.0961, 11.6384, 11.1739, 10.7023, 10.2233, 9.73683, 9.24255, 8.74022,
						8.22959, 7.71037, 7.18223, 6.64487, 6.09808, 5.54129, 4.97435, 4.39682, 3.80826, 3.20828, 2.59645,
						1.97227, 1.33531, 0.685055, 0.0209005, -0.647261, -1.29601, -1.96137, -2.64401, -3.34479, -4.06459, -4.80423,
						-5.56488, -6.34737, -7.15291, -7.98254, -8.83754, -9.71916, -10.6287, -11.5677, -12.5374, -13.5395, -14.5755,
						-15.6471, -16.7560, -17.9037, -19.0923, -20.3234, -21.5988, -22.9203, -24.2897, -25.7086, -27.1788, -28.7018,
						-30.2792, -31.9125, -33.6029, -35.3518, -37.1605, -39.0301, -40.9619, -42.9573, -45.0175, -47.1441, -49.3388,
						-51.6037, -53.9412, -56.3542, -58.8459, -61.4204, -64.0824, -66.8373, -69.6917},
				{19.7081, 19.3419, 18.9714, 18.5965, 18.2169, 17.8326, 17.4436, 17.0496, 16.6506, 16.2463, 15.8367, 15.4217,
						15.0010, 14.5744, 14.1418, 13.7032, 13.2580, 12.8065, 12.3481, 11.8829, 11.4105, 10.9306, 10.4431,
						9.94772, 9.44419, 8.93221, 8.41156, 7.88182, 7.34273, 6.79401, 6.23516, 5.66594, 5.08592, 4.49465,
						3.89170, 3.27662, 2.64892, 2.00812, 1.35360, 0.684861, 0.00126863, -0.684507, -1.35275, -2.03878, -2.74347,
						-3.46777, -4.21265, -4.97924, -5.76858, -6.58190, -7.42043, -8.28553, -9.17863, -10.1012, -11.0549, -12.0412,
						-13.0621, -14.1193, -15.2147, -16.3503, -17.5281, -18.7503, -20.0189, -21.3362, -22.7042, -24.1253, -25.6013,
						-27.1345, -28.7267, -30.3799, -32.0959, -33.8762, -35.7226, -37.6365, -39.6193, -41.6726, -43.7978, -45.9966,
						-48.2709, -50.6228, -53.0550, -55.5704, -58.1727, -60.8663, -63.6564, -66.5493},
				{21.0689, 20.7106, 20.3482, 19.9815, 19.6104, 19.2348, 18.8546, 18.4697, 18.0800, 17.6853, 17.2854, 16.8803,
						16.4698, 16.0537, 15.6320, 15.2044, 14.7706, 14.3307, 13.8844, 13.4314, 12.9716, 12.5048, 12.0306,
						11.5490, 11.0596, 10.5623, 10.0566, 9.54235, 9.01923, 8.48691, 7.94502, 7.39331, 6.83128, 6.25863,
						5.67494, 5.07973, 4.47257, 3.85296, 3.22041, 2.57434, 1.91420, 1.23933, 0.549141, -0.157090, -0.854023,
						-1.54534, -2.25594, -2.98682, -3.73913, -4.51392, -5.31246, -6.13609, -6.98610, -7.86408, -8.77154, -9.71015,
						-10.6818, -11.6883, -12.7316, -13.8139, -14.9374, -16.1043, -17.3172, -18.5784, -19.8905, -21.2562, -22.6779,
						-24.1584, -25.7001, -27.3057, -28.9774, -30.7177, -32.5286, -34.4121, -36.3703, -38.4047, -40.5174, -42.7098,
						-44.9839, -47.3418, -49.7856, -52.3183, -54.9430, -57.6638, -60.4856, -63.4142},
				{22.3481, 21.9970, 21.6418, 21.2825, 20.9191, 20.5513, 20.1791, 19.8024, 19.4210, 19.0348, 18.6438, 18.2477,
						17.8464, 17.4399, 17.0278, 16.6102, 16.1867, 15.7573, 15.3217, 14.8799, 14.4315, 13.9764, 13.5143,
						13.0451, 12.5685, 12.0843, 11.5921, 11.0918, 10.5831, 10.0656, 9.53899, 9.00308, 8.45733, 7.90156,
						7.33522, 6.75799, 6.16943, 5.56905, 4.95639, 4.33092, 3.69214, 3.03934, 2.37206, 1.68960, 0.991213,
						0.276175, -0.456243, -1.15799, -1.87660, -2.61631, -3.37830, -4.16381, -4.97413, -5.81071, -6.67515, -7.56902,
						-8.49408, -9.45230, -10.4456, -11.4761, -12.5463, -13.6584, -14.8150, -16.0188, -17.2727, -18.5795, -19.9424,
						-21.3643, -22.8485, -24.3980, -26.0159, -27.7054, -29.4693, -31.3102, -33.2311, -35.2340, -37.3214, -39.4953,
						-41.7578, -44.1110, -46.5571, -49.0987, -51.7388, -54.4808, -57.3292, -60.2896},
				{23.5532, 23.2086, 22.8600, 22.5075, 22.1509, 21.7903, 21.4253, 21.0559, 20.6821, 20.3038, 19.9206, 19.5327,
						19.1398, 18.7417, 18.3385, 17.9298, 17.5156, 17.0957, 16.6698, 16.2380, 15.7999, 15.3553, 14.9042,
						14.4461, 13.9811, 13.5087, 13.0288, 12.5411, 12.0453, 11.5412, 11.0284, 10.5067, 9.97575, 9.43513,
						8.88455, 8.32356, 7.75178, 7.16882, 6.57415, 5.96734, 5.34784, 4.71517, 4.06863, 3.40775, 2.73172,
						2.03995, 1.33167, 0.606114, -0.137680, -0.871999, -1.60084, -2.35178, -3.12594, -3.92474, -4.74968, -5.60222,
						-6.48422, -7.39740, -8.34380, -9.32545, -10.3447, -11.4039, -12.5058, -13.6532, -14.8489, -16.0962, -17.3982,
						-18.7587, -20.1809, -21.6688, -23.2259, -24.8562, -26.5633, -28.3508, -30.2223, -32.1810, -34.2300, -36.3722,
						-38.6101, -40.9463, -43.3832, -45.9233, -48.5694, -51.3248, -54.1933, -57.1801},
				{25.7677, 25.4342, 25.0971, 24.7562, 24.4116, 24.0631, 23.7106, 23.3541, 22.9934, 22.6284, 22.2590, 21.8851,
						21.5066, 21.1233, 20.7352, 20.3421, 19.9438, 19.5402, 19.1312, 18.7166, 18.2962, 17.8699, 17.4374,
						16.9987, 16.5534, 16.1015, 15.6426, 15.1765, 14.7031, 14.2220, 13.7330, 13.2358, 12.7301, 12.2157,
						11.6922, 11.1591, 10.6163, 10.0632, 9.49964, 8.92490, 8.33872, 7.74058, 7.12995, 6.50630, 5.86897,
						5.21746, 4.55101, 3.86897, 3.17057, 2.45497, 1.72136, 0.968741, 0.196198, -0.590231, -1.34696, -2.12797,
						-2.93481, -3.76914, -4.63275, -5.52754, -6.45568, -7.41941, -8.42128, -9.46399, -10.5505, -11.6839, -12.8679,
						-14.1060, -15.4023, -16.7613, -18.1873, -19.6854, -21.2605, -22.9180, -24.6630, -26.5012, -28.4377, -30.4776,
						-32.6258, -34.8868, -37.2647, -39.7631, -42.3856, -45.1355, -48.0165, -51.0325},
				{27.7572, 27.4330, 27.1054, 26.7742, 26.4395, 26.1012, 25.7590, 25.4131, 25.0632, 24.7093, 24.3512, 23.9890,
						23.6224, 23.2513, 22.8757, 22.4955, 22.1103, 21.7202, 21.3250, 20.9247, 20.5189, 20.1076, 19.6906,
						19.2677, 18.8387, 18.4036, 17.9619, 17.5137, 17.0586, 16.5964, 16.1269, 15.6498, 15.1649, 14.6719,
						14.1705, 13.6604, 13.1413, 12.6128, 12.0746, 11.5262, 10.9673, 10.3976, 9.81630, 9.22316, 8.61763,
						7.99914, 7.36703, 6.72079, 6.05967, 5.38302, 4.69002, 3.97980, 3.25153, 2.50429, 1.73692, 0.948410,
						0.137540, -0.681970, -1.47766, -2.30075, -3.15329, -4.03714, -4.95461, -5.90807, -6.90033, -7.93444, -9.01342,
						-10.1410, -11.3211, -12.5580, -13.8565, -15.2218, -16.6594, -18.1754, -19.7764, -21.4692, -23.2613, -25.1599,
						-27.1728, -29.3074, -31.5711, -33.9706, -36.5122, -39.2016, -42.0438, -45.0434},
				{29.5573, 29.2408, 28.9212, 28.5982, 28.2717, 27.9418, 27.6085, 27.2713, 26.9305, 26.5859, 26.2374, 25.8848,
						25.5282, 25.1673, 24.8022, 24.4325, 24.0583, 23.6795, 23.2959, 22.9074, 22.5137, 22.1149, 21.7107,
						21.3010, 20.8856, 20.4643, 20.0370, 19.6036, 19.1636, 18.7171, 18.2637, 17.8033, 17.3355, 16.8603,
						16.3772, 15.8860, 15.3864, 14.8782, 14.3609, 13.8342, 13.2979, 12.7514, 12.1944, 11.6263, 11.0469,
						10.4556, 9.85178, 9.23499, 8.60463, 7.95991, 7.30037, 6.62505, 5.93331, 5.22426, 4.49699, 3.75043,
						2.98362, 2.19537, 1.38433, 0.549200, -0.311453, -1.14512, -1.99342, -2.87361, -3.78794, -4.73915, -5.73004,
						-6.76386, -7.84422, -8.97505, -10.1608, -11.4063, -12.7172, -14.0994, -15.5598, -17.1056, -18.7452, -20.4871,
						-22.3411, -24.3171, -26.4257, -28.6774, -31.0828, -33.6518, -36.3937, -39.3168},
				{31.1962, 30.8864, 30.5734, 30.2572, 29.9379, 29.6152, 29.2891, 28.9594, 28.6263, 28.2895, 27.9490, 27.6047,
						27.2563, 26.9041, 26.5477, 26.1871, 25.8221, 25.4527, 25.0788, 24.7002, 24.3168, 23.9284, 23.5349,
						23.1362, 22.7322, 22.3226, 21.9074, 21.4862, 21.0589, 20.6255, 20.1856, 19.7391, 19.2857, 18.8252,
						18.3573, 17.8819, 17.3986, 16.9072, 16.4073, 15.8987, 15.3810, 14.8538, 14.3169, 13.7697, 13.2120,
						12.6431, 12.0627, 11.4703, 10.8652, 10.2470, 9.61509, 8.96864, 8.30711, 7.62958, 6.93534, 6.22350,
						5.49311, 4.74300, 3.97222, 3.17949, 2.36344, 1.52268, 0.655580, -0.239556, -1.11162, -1.99513, -2.91380,
						-3.87048, -4.86827, -5.91064, -7.00159, -8.14561, -9.34751, -10.6129, -11.9483, -13.3605, -14.8577, -16.4488,
						-18.1439, -19.9540, -21.8914, -23.9695, -26.2022, -28.6042, -31.1903, -33.9746},
				{32.6967, 32.3925, 32.0853, 31.7750, 31.4616, 31.1450, 30.8251, 30.5020, 30.1753, 29.8451, 29.5115, 29.1741,
						28.8329, 28.4880, 28.1390, 27.7861, 27.4289, 27.0676, 26.7019, 26.3316, 25.9568, 25.5774, 25.1930,
						24.8037, 24.4092, 24.0095, 23.6043, 23.1936, 22.7771, 22.3546, 21.9261, 21.4913, 21.0499, 20.6018,
						20.1468, 19.6846, 19.2150, 18.7376, 18.2524, 17.7588, 17.2568, 16.7458, 16.2257, 15.6960, 15.1562,
						14.6062, 14.0453, 13.4732, 12.8893, 12.2932, 11.6841, 11.0617, 10.4252, 9.77391, 9.10717, 8.42405,
						7.72379, 7.00549, 6.26800, 5.51040, 4.73134, 3.92964, 3.10390, 2.25241, 1.37366, 0.465684, -0.473505,
						-1.36949, -2.29975, -3.26978, -4.28281, -5.34284, -6.45421, -7.62184, -8.85143, -10.1493, -11.5229, -12.9805,
						-14.5316, -16.1871, -17.9596, -19.8630, -21.9129, -24.1269, -26.5238, -29.1235},
				{34.0773, 33.7779, 33.4757, 33.1704, 32.8622, 32.5509, 32.2364, 31.9187, 31.5977, 31.2732, 30.9453, 30.6140,
						30.2789, 29.9402, 29.5977, 29.2512, 28.9009, 28.5463, 28.1876, 27.8246, 27.4572, 27.0853, 26.7087,
						26.3273, 25.9410, 25.5497, 25.1531, 24.7513, 24.3439, 23.9308, 23.5120, 23.0871, 22.6559, 22.2184,
						21.7742, 21.3232, 20.8652, 20.3999, 19.9269, 19.4461, 18.9573, 18.4600, 17.9540, 17.4389, 16.9145,
						16.3802, 15.8357, 15.2807, 14.7146, 14.1369, 13.5472, 12.9448, 12.3293, 11.7000, 11.0561, 10.3971,
						9.72206, 9.03016, 8.32054, 7.59218, 6.84394, 6.07479, 5.28338, 4.46828, 3.62805, 2.76097, 1.86521,
						0.938730, -0.0207037, -0.972479, -1.92183, -2.91312, -3.95022, -5.03736, -6.17951, -7.38236, -8.65238, -9.99694,
						-11.4248, -12.9458, -14.5716, -16.3156, -18.1932, -20.2226, -22.4240, -24.8205},
				{35.3533, 35.0581, 34.7602, 34.4593, 34.1555, 33.8488, 33.5389, 33.2260, 32.9098, 32.5903, 32.2675, 31.9412,
						31.6115, 31.2782, 30.9411, 30.6004, 30.2557, 29.9072, 29.5545, 29.1978, 28.8367, 28.4713, 28.1014,
						27.7268, 27.3476, 26.9634, 26.5743, 26.1801, 25.7805, 25.3755, 24.9648, 24.5484, 24.1261, 23.6976,
						23.2627, 22.8213, 22.3731, 21.9180, 21.4556, 20.9857, 20.5081, 20.0225, 19.5285, 19.0259, 18.5144,
						17.9936, 17.4630, 16.9224, 16.3713, 15.8093, 15.2359, 14.6505, 14.0527, 13.4419, 12.8174, 12.1786,
						11.5248, 10.8551, 10.1688, 9.46501, 8.74267, 8.00082, 7.23810, 6.45346, 5.64544, 4.81258, 3.95310,
						3.06529, 2.14705, 1.19621, 0.210139, -0.786976, -1.76317, -2.78427, -3.85472, -4.97933, -6.16386, -7.41480,
						-8.73981, -10.1477, -11.6488, -13.2553, -14.9815, -16.8444, -18.8637, -21.0629},
				{36.5374, 36.2459, 35.9517, 35.6547, 35.3548, 35.0520, 34.7462, 34.4373, 34.1254, 33.8103, 33.4919, 33.1701,
						32.8449, 32.5163, 32.1841, 31.8482, 31.5086, 31.1651, 30.8178, 30.4663, 30.1108, 29.7510, 29.3869,
						29.0183, 28.6451, 28.2673, 27.8846, 27.4969, 27.1041, 26.7060, 26.3026, 25.8935, 25.4786, 25.0579,
						24.6310, 24.1979, 23.7582, 23.3119, 22.8585, 22.3980, 21.9300, 21.4544, 20.9708, 20.4789, 19.9784,
						19.4690, 18.9504, 18.4222, 17.8839, 17.3352, 16.7757, 16.2049, 15.6221, 15.0270, 14.4190, 13.7974,
						13.1615, 12.5107, 11.8442, 11.1612, 10.4607, 9.74177, 9.00345, 8.24440, 7.46351, 6.65937, 5.83045,
						4.97510, 4.09149, 3.17751, 2.23097, 1.24932, 0.229757, -0.801959, -1.81337, -2.87368, -3.98783, -5.16165,
						-6.40165, -7.71581, -9.11309, -10.6043, -12.2022, -13.9220, -15.7820, -17.8039},
				{37.6402, 37.3519, 37.0610, 36.7673, 36.4709, 36.1716, 35.8693, 35.5641, 35.2558, 34.9444, 34.6299, 34.3121,
						33.9910, 33.6664, 33.3384, 33.0069, 32.6716, 32.3327, 31.9899, 31.6432, 31.2925, 30.9376, 30.5785,
						30.2151, 29.8472, 29.4748, 29.0977, 28.7157, 28.3288, 27.9368, 27.5395, 27.1368, 26.7286, 26.3146,
						25.8947, 25.4687, 25.0364, 24.5977, 24.1521, 23.6997, 23.2401, 22.7731, 22.2984, 21.8157, 21.3248,
						20.8253, 20.3170, 19.7994, 19.2722, 18.7350, 18.1874, 17.6289, 17.0591, 16.4775, 15.8836, 15.2767,
						14.6562, 14.0215, 13.3719, 12.7066, 12.0247, 11.3254, 10.6077, 9.87040, 9.11256, 8.33274, 7.52967,
						6.70181, 5.84733, 4.96457, 4.05135, 3.10547, 2.12421, 1.10488, 0.0440281, -1.01210, -2.06818, -3.17823,
						-4.34820, -5.58483, -6.89617, -8.29170, -9.78265, -11.3825, -13.1073, -14.9771},
				{38.6706, 38.3853, 38.0973, 37.8066, 37.5131, 37.2169, 36.9177, 36.6157, 36.3107, 36.0027, 35.6915, 35.3772,
						35.0596, 34.7387, 34.4144, 34.0866, 33.7552, 33.4202, 33.0815, 32.7389, 32.3924, 32.0418, 31.6873,
						31.3283, 30.9651, 30.5975, 30.2252, 29.8483, 29.4665, 29.0798, 28.6880, 28.2909, 27.8884, 27.4803,
						27.0664, 26.6467, 26.2208, 25.7887, 25.3500, 24.9047, 24.4523, 23.9929, 23.5259, 23.0512, 22.5686,
						22.0778, 21.5783, 21.0699, 20.5523, 20.0251, 19.4878, 18.9401, 18.3815, 17.8116, 17.2297, 16.6355,
						16.0283, 15.4075, 14.7723, 14.1222, 13.4563, 12.7738, 12.0737, 11.3551, 10.6170, 9.85797, 9.07695,
						8.27244, 7.44288, 6.58664, 5.70175, 4.78608, 3.83731, 2.85286, 1.82977, 0.764677, -0.346195, -1.41730,
						-2.52909, -3.70146, -4.94171, -6.25795, -7.66013, -9.16012, -10.7722, -12.5138},
				{39.6364, 39.3536, 39.0681, 38.7801, 38.4893, 38.1958, 37.8995, 37.6003, 37.2982, 36.9931, 36.6849, 36.3737,
						36.0592, 35.7415, 35.4205, 35.0961, 34.7681, 34.4365, 34.1014, 33.7624, 33.4197, 33.0730, 32.7223,
						32.3674, 32.0084, 31.6448, 31.2769, 30.9045, 30.5272, 30.1451, 29.7580, 29.3659, 28.9684, 28.5655,
						28.1570, 27.7427, 27.3226, 26.8963, 26.4636, 26.0244, 25.5784, 25.1255, 24.6654, 24.1978, 23.7224,
						23.2390, 22.7474, 22.2471, 21.7378, 21.2192, 20.6910, 20.1526, 19.6038, 19.0439, 18.4727, 17.8895,
						17.2938, 16.6849, 16.0624, 15.4254, 14.7733, 14.1054, 13.4205, 12.7180, 11.9968, 11.2557, 10.4936,
						9.70917, 8.90097, 8.06733, 7.20666, 6.31686, 5.39587, 4.44114, 3.45011, 2.41969, 1.34635, 0.226233,
						-0.904488, -2.02339, -3.20418, -4.45449, -5.78285, -7.19980, -8.71807, -10.3529},
				{40.5441, 40.2635, 39.9804, 39.6947, 39.4063, 39.1152, 38.8213, 38.5247, 38.2251, 37.9227, 37.6172, 37.3087,
						36.9971, 36.6822, 36.3640, 36.0425, 35.7176, 35.3893, 35.0572, 34.7216, 34.3821, 34.0388, 33.6916,
						33.3403, 32.9848, 32.6251, 32.2610, 31.8925, 31.5192, 31.1412, 30.7584, 30.3706, 29.9776, 29.5793,
						29.1755, 28.7661, 28.3509, 27.9297, 27.5024, 27.0686, 26.6283, 26.1812, 25.7270, 25.2656, 24.7967,
						24.3199, 23.8350, 23.3418, 22.8399, 22.3289, 21.8086, 21.2784, 20.7380, 20.1871, 19.6251, 19.0515,
						18.4658, 17.8674, 17.2559, 16.6304, 15.9904, 15.3349, 14.6634, 13.9748, 13.2682, 12.5427, 11.7970,
						11.0299, 10.2401, 9.42608, 8.58624, 7.71871, 6.82150, 5.89242, 4.92883, 3.92803, 2.88682, 1.80156,
						0.668033, -0.516647, -1.64773, -2.84267, -4.10927, -5.45700, -6.89683, -8.44261}};

		return tesetaseAry;
	}
}
