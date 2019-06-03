package com.xinhong.mids3d.syno.util;

import gov.nasa.worldwind.geom.Position;


/**
 *
 * @author Zhoucj
 *
 */
public class Station
{
	public enum StationType
	{
		Surf,
		High,
		SurfHigh,
		ArmySurf,
		SurfAuto,
		Air,
		Ship;
	}

	private String ID = "";
	private String EnName = "";
	private String CHNName = "";
	private String PY = "";
	private Position position;
	private StationType type = null;
	private String synoFlag;
	private int showLevel = 99;

	/**
	 *
	 */
	public Station()
	{
	}

	/**
	 *
	 * @param ID
	 * @param EnName
	 * @param CHNName
	 * @param PY
	 * @param position
	 * @param type
	 * @param synoFlag
	 * @param showLevel
	 */
	public Station(String ID, String EnName, String CHNName, String PY, Position position,
				   StationType type, String synoFlag, int showLevel)
	{
		this.setID(ID);
		this.setEnName(EnName);
		this.setCHNName(CHNName);
		this.setPY(PY);
		this.setPosition(position);
		this.setType(type);
		this.setSynoFlag(synoFlag);
		this.setShowLevel(showLevel);
	}

	/**
	 *
	 * @param ID
	 * @param EnName
	 * @param CHNName
	 * @param position
	 * @param showLevel
	 */
	public Station(String ID, String EnName, String CHNName, Position position, int showLevel)
	{
		this(ID, EnName, CHNName, "", position, null, null, showLevel);
	}

	/**
	 *
	 * @param ID
	 * @param EnName
	 * @param CHNName
	 * @param position
	 * @param type
	 * @param showLevel
	 */
	public Station(String ID, String EnName, String CHNName, Position position, StationType type, int showLevel)
	{
		this(ID, EnName, CHNName, "", position, type, null, showLevel);
	}

	/**
	 *
	 * @param ID
	 * @param EnName
	 * @param CHNName
	 * @param position
	 * @param synoFlag
	 */
	public Station(String ID, String EnName, String CHNName, Position position, String synoFlag)
	{
		this(ID, EnName, CHNName, "", position, null, synoFlag, 99);
		this.setShowLevel(this.ParseShowLevel());
	}

	/**
	 *
	 * @param ID
	 * @param CHNName
	 * @param position
	 */
	public Station(String ID, String CHNName, Position position)
	{
		this(ID, "", CHNName, "", position, null, null, 99);
	}

	public String getID()
	{
		return this.ID;
	}

	public void setID(String ID)
	{
		this.ID = ID;
	}

	public String getEnName()
	{
		return this.EnName;
	}

	public void setEnName(String EnName)
	{
		this.EnName = EnName;
	}

	public String getCHNName()
	{
		return this.CHNName;
	}

	public void setCHNName(String CHNName)
	{
		this.CHNName = CHNName;
	}

	public String getPY()
	{
		return this.PY;
	}

	public void setPY(String PY)
	{
		this.PY = PY;
	}

	public Position getPosition()
	{
		return this.position;
	}

	public void setPosition(Position position)
	{
		this.position = position;
	}

	public double getLat()
	{
		return this.position.getLatitude().getDegrees();
	}

	public double getLon()
	{
		return this.position.getLongitude().getDegrees();
	}

	public StationType getType()
	{
		return this.type;
	}

	public void setType(StationType type)
	{
		this.type = type;
	}

	public String getSynoFlag()
	{
		return this.synoFlag;
	}

	public void setSynoFlag(String synoFlag)
	{
		this.synoFlag = synoFlag;
	}

	public int getShowLevel()
	{
		return this.showLevel;
	}

	public void setShowLevel(int showLevel)
	{
		this.showLevel = showLevel;
	}

	private int ParseShowLevel()
	{
		if (this.synoFlag == null)
		{
			return 99;
		}
		/*String[] flagAry = this.synoFlag.split(",");
		for (int i = 0; i < flagAry.length; i++)
		{
			if (flagAry[i].startsWith("MICAPS"))
			{
				return Integer.parseInt(flagAry[i].substring(6));
			}
		}*/
		if (this.synoFlag.startsWith("1"))
		{
			return 1;
		}
		else if (this.synoFlag.indexOf("MICAPS32") == -1)
		{
			return 2;
		}
		else
		{
			return 3;
		}
	}
}
