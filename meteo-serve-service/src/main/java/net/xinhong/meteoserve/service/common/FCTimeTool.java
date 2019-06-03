package net.xinhong.meteoserve.service.common;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Minutes;

/**
 * Description: 预报产品时次调整工具类<br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 <a href=mailto:liusofttech@sina.com>刘晓昌</a>
 * @version 创建时间：2016/3/15.
 */
public final class FCTimeTool {
    private FCTimeTool(){}

    /**
     * 根据设置的时间，查找最近的起报时间(每天按照08，20时起报计算，给定及返回的时间均为北京时)
     * @param setDate
     * @param delayMinutes-设置为-9999,按照默认晚9小时计算,设置为0或正数按照实际给定分钟数计算
     * @return
     */
    public static DateTime getFCStartTime(DateTime setDate, long delayMinutes){
        DateTime resDate = new DateTime(setDate.getYear(), setDate.getMonthOfYear(),
                setDate.getDayOfMonth(), setDate.getHourOfDay(), 0);
        //首先确定起始时间归并到20时还是08时
        if (resDate.getHourOfDay() < 8 || resDate.getHourOfDay() >= 20){ //归并到20时
            while (resDate.getHourOfDay() != 20) resDate = resDate.minusHours(1);
        } else { //归并到08时
            while (resDate.getHourOfDay() != 8) resDate = resDate.minusHours(1);
        }
        if (delayMinutes == 0)
            return resDate;

        //然后确定归并的起始时间与当前时间相差值，确定该起报时间数据是否已成功生成，如没有生成，则向前推12小时
        if (delayMinutes == -9999)
            delayMinutes = 9*60;  //设置为负数时,默认预报产品晚9小时（早上8点起报数据下午5点后到达）
        long subMinutes = Minutes.minutesBetween(resDate, DateTime.now()).getMinutes();
        if (subMinutes < delayMinutes){
            resDate = resDate.minusHours(12);
        }

        return resDate;
    }




    public static DateTime getHY1SuoStartTime(DateTime setDate, int delayMinutes){
        DateTime resDate = new DateTime(setDate.getYear(), setDate.getMonthOfYear(),
                setDate.getDayOfMonth(), setDate.getHourOfDay(), 0);
        resDate=resDate.minusMinutes(delayMinutes);
        //1SUO起报归并到20时
        while (resDate.getHourOfDay() != 20) resDate = resDate.minusHours(1);

//        if (resDate.getHourOfDay() < 8 || resDate.getHourOfDay() >= 20){ //归并到20时
//            while (resDate.getHourOfDay() != 20) resDate = resDate.minusHours(1);
//        } else { //归并到08时
//            while (resDate.getHourOfDay() != 8) resDate = resDate.minusHours(1);
//        }
        if (delayMinutes == 0)
            return resDate;

        //然后确定归并的起始时间与当前时间相差值，确定该起报时间数据是否已成功生成，如没有生成，则向前推12小时
        if (delayMinutes == -9999)
            delayMinutes = 17*60;  //设置为负数时,默认预报产品晚17小时（晚上8点起报数据第二天下午1点后完成处理）
        long subMinutes = Minutes.minutesBetween(resDate, DateTime.now()).getMinutes();
        if (subMinutes < delayMinutes){
            resDate = resDate.minusHours(12);
        }
        return resDate;
    }

    //海洋类的预报（目前只引入了00时起报的数据）
    public static DateTime getHYFCStartTime(DateTime setDate, long delayMinutes){
        DateTime resDate = new DateTime(setDate.getYear(), setDate.getMonthOfYear(),
                setDate.getDayOfMonth(), 8, 0);
        //首先确定起始时间归并到20时还是08时
        if (delayMinutes == 0)
            return resDate;
        //然后确定归并的起始时间与当前时间相差值，确定该起报时间数据是否已成功生成，如没有生成，则向前推12小时
        if (delayMinutes == -9999)
            delayMinutes = 27*60;  //设置为负数时,默认预报产品晚9小时（早上8点起报数据下午5点后到达）
        long subMinutes = Minutes.minutesBetween(resDate, DateTime.now()).getMinutes();
        if (subMinutes < delayMinutes){
            resDate = resDate.minusHours(24);
        }
        return resDate;
    }



    /**
     * 根据设置的时间，查找最近的起报时间(每天只按照20时起报计算，给定及返回的时间均为北京时。主要用于海洋预报产品)
     * @param setDate
     * @param delayMinutes-设置为-9999,按照默认晚9小时计算,设置为0或正数按照实际给定分钟数计算
     * @return
     */   public static DateTime getFCStartTimeOnly20(DateTime setDate, long delayMinutes){
        DateTime resDate = new DateTime(setDate.getYear(), setDate.getMonthOfYear(),
                setDate.getDayOfMonth(), setDate.getHourOfDay(), 0);

        while (resDate.getHourOfDay() != 20) resDate = resDate.minusHours(1);
        if (delayMinutes == 0)
            return resDate;

        //然后确定归并的起始时间与当前时间相差值，确定该起报时间数据是否已成功生成，如没有生成，则向前推24小时
        if (delayMinutes == -9999)
            delayMinutes = 22*60;  //设置为负数时,默认预报产品晚22小时（第一天20起报数据第二天下午6点后到达）
        long subMinutes = Minutes.minutesBetween(resDate, DateTime.now()).getMinutes();
        if (subMinutes < delayMinutes){
            resDate = resDate.minusHours(24);
        }

        return resDate;
    }





    public static void main(String[] args) {
        DateTime setTime = new DateTime(2016,1,1,21,15,00);
        System.out.println(setTime);
        System.out.println(FCTimeTool.getFCStartTime(setTime, -5));
        System.out.println("-------------");

        setTime = new DateTime(2016,3,1,7,55,00);
        System.out.println(setTime);
        System.out.println(FCTimeTool.getFCStartTime(setTime, 0));
        System.out.println("-------------");

        setTime = new DateTime(2016,3,15,9,55,00);
        System.out.println(setTime);
        System.out.println(FCTimeTool.getFCStartTime(setTime, 5*60));
        System.out.println("-------------");

        setTime = new DateTime(2016,3,15,18,55,00);
        System.out.println(setTime);
        System.out.println(FCTimeTool.getFCStartTime(setTime, 12*60));
        System.out.println("-------------");

        setTime = DateTime.now();
        System.out.println(setTime);
        System.out.println(FCTimeTool.getFCStartTime(setTime, 8*60));
        System.out.println("-------------");

        setTime = new DateTime(2000,2,15,22,55,00);
        System.out.println(setTime);
        System.out.println(FCTimeTool.getFCStartTime(setTime, 7*60));
        System.out.println("-------------");

        setTime = new DateTime(2014,12,31,8,10,00);
        System.out.println(setTime);
        System.out.println(FCTimeTool.getFCStartTime(setTime, 10));
        System.out.println("-------------");
        setTime = new DateTime(2014,12,31,20,9,00);
        System.out.println(setTime);
        System.out.println(FCTimeTool.getFCStartTime(setTime, 10));
        System.out.println("-------------");
    }

}
