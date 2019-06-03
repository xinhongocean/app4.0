package net.xinhong.meteoserve.common.tool;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * Description: <br>
 *
 * @author 作者 <a href=ds.lht@163.com>stone</a>
 * @version 创建时间：2016/5/13.
 */
public class DateUtil {


    /**
     * 日期转换成UTC格式
     * @param year
     * @param month
     * @param day
     * @param hour
     * @return Format:yyyyMMddHH
     */
    public static  String dateToUTC(int year,int month,int day,int hour){
        DateTime dateTime = new DateTime(year,month,day,hour,0);
        dateTime = dateTime.minusHours(8);
        return dateTime.toString("yyyyMMddHH");
    }


    public  static  DateTime format(String dateStr,String pattern){

        return DateTime.parse(dateStr, DateTimeFormat.forPattern(pattern));
    }
    /**
     * 日期转换成UTC格式
     * @param year
     * @param month
     * @param day
     * @param hour
     * @return Format:yyyyMMddHH
     */
    public static  String dateToUTC(String year,String month,String day,String hour){
        return dateToUTC(Integer.parseInt(year),Integer.parseInt(month)
                ,Integer.parseInt(day),Integer.parseInt(hour));
    }


    public static void main(String[] args) {
        System.out.println(format("2016040412","yyyyMMddHH"));
        System.out.println(dateToUTC(2015,4,4,00));
    }
}
