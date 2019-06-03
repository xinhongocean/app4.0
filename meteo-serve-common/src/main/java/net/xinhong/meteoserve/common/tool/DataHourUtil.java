package net.xinhong.meteoserve.common.tool;

import net.xinhong.meteoserve.common.constant.DataTypeConst;
import org.joda.time.DateTime;

/**
 * Description: <br>
 *
 * @author 作者 <a href=ds.lht@163.com>stone</a>
 * @version 创建时间：2016/5/24.
 */
public class DataHourUtil {

    /**
     * 地面实况，返回距当前时间最近的起报时间（hour）
     * @return
     */
    public static String getStationSurfHour(){
        DateTime date = DateTime.now().minusHours(8);
        int currentHour = date.getHourOfDay();
        String hour =null;
        if(currentHour>=21 ){
            return "21";
        }
        for (int i = 0; i < DataTypeConst.DM_HOURS.length; i++) {
            int tmpVal = Integer.valueOf(DataTypeConst.DM_HOURS[i]);
            if(tmpVal==currentHour){
                hour =  StringUtils.leftPad(Integer.toString(currentHour),2,'0');
                break;
            }else if(tmpVal>currentHour){
                hour = DataTypeConst.DM_HOURS[i-1];
                break;
            }
        }
        return hour;
    }

    /**
     * 高空实况
     * @return
     */
    public static String getStationHighHour() {
        DateTime date = DateTime.now().minusHours(8);
        int currentHour = date.getHourOfDay();
        switch (currentHour){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return "00";
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
                return "06";
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
                return "12";
            default:
                return "18";

        }
    }
}
