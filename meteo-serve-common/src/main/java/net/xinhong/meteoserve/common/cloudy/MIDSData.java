package net.xinhong.meteoserve.common.cloudy;

/**
 * 气象海洋数据均需要继承该接口
 * @author lxc
 *
 */
public abstract class MIDSData {	
	/**
	 * 保存的气象海洋数据是否为空
	 * @return
	 */
	abstract public boolean isEmpty();
	
	abstract public void dispose();
}
