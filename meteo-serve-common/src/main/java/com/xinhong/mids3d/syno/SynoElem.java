package com.xinhong.mids3d.syno;

import com.xinhong.mids3d.datareader.util.ElemCode;

import java.awt.*;


/**
 * 天气实况填图要素类，保存各文字要素的内容，绘制类型，颜色，显示位置
 * 
 * @author Zhoucj
 *
 */
public class SynoElem
{
	public enum SynoElemDrawType
	{
		TEXT, PIC, WS, WD, SWD, PMDIR;
	}
	
	private ElemCode elem;
	private String text;
	private SynoElemDrawType drawType = SynoElemDrawType.TEXT;
	private boolean showFlag;
	private Color color;
	private Point drawOffset;
	private String textAlign;
	
	/**
	 * 
	 * @param elem
	 * @param text
	 * @param drawType
	 * @param showFlag
	 * @param color
	 * @param drawOffset
	 * @param textAlign
	 */
	public SynoElem(ElemCode elem, String text, SynoElemDrawType drawType, boolean showFlag, 
			Color color, Point drawOffset, String textAlign)
	{
		this.setElem(elem);
		this.setText(text);
		this.setDrawType(drawType);
		this.setShowFlag(showFlag);
		this.setColor(color);
		this.setDrawOffset(drawOffset);
		this.setTextAlign(textAlign);
	}
	
	public ElemCode getElem()
	{
		return elem;
	}

	public void setElem(ElemCode elem)
	{
		this.elem = elem;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public boolean isShowFlag()
	{
		return showFlag;
	}

	public void setShowFlag(boolean showFlag)
	{
		this.showFlag = showFlag;
	}

	public SynoElemDrawType getDrawType()
	{
		return drawType;
	}

	public void setDrawType(SynoElemDrawType drawType)
	{
		this.drawType = drawType;
	}

	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	public Point getDrawOffset()
	{
		return drawOffset;
	}

	public void setDrawOffset(Point drawOffset)
	{
		this.drawOffset = drawOffset;
	}

	public String getTextAlign()
	{
		return textAlign;
	}

	public void setTextAlign(String textAlign)
	{
		this.textAlign = textAlign;
	}
}
