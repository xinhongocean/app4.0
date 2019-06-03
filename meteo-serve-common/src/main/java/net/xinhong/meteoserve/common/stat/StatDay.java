package net.xinhong.meteoserve.common.stat;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ElemStat;
import net.xinhong.meteoserve.common.tool.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

/**
 * Created by shijunna on 2016/7/21.
 */
public class StatDay {

    private final Log logger = LogFactory.getLog(StatDay.class);
    public JSONObject getStatdayData(String statdayFilepath){
        File file = new File(statdayFilepath);
        if(statdayFilepath == null || statdayFilepath.length() < 1 || !file.exists()){
            logger.error("所输入的文件路径不正确，或文件不存在，请检查，file=" + statdayFilepath);
            return null;
        }
        //文件存在，开始解析
        BufferedReader reader = null;
        try {
            JSONObject object = new JSONObject();
            reader = new BufferedReader(new FileReader(file));
            String s;
            String[] ary = null;String[] aryLast = null;
            String dayIdx = ""; int lastYear = 0;
            while((s = reader.readLine()) != null) {
                ary = s.split("\\s+");
                if(ary == null || ary.length != 10 || ary[0].equals("V01301")){
                    logger.error("数据行信息错误，或文件开头行，lineValue=" + s);
                    continue;
                }
                if(ary[2].equals("1")){
                    dayIdx = "1";
                    lastYear = getLastYear(ary[1]);
                    aryLast = ary;
                }else{
                    if(ary[2].equals(dayIdx)){//日期ID相同，取结束年最新的数据，赋值数据，不做处理
                        int lyear = getLastYear(ary[1]);
                        if(lyear > lastYear){//取结束年份较大的数据
                            aryLast = ary;
                            lastYear = lyear;
                        }
                    }else{//日期ID不相同，将上一条数据入到JSON里
                        if(ary2JSONObject(object, aryLast) == false){
                            logger.error("文件内信息错误，请检查。file =:" + statdayFilepath
                                    + "\r\n line=" + s);
                        }
                        //重新赋值aryLast 和 dayIdx
                        aryLast = ary;
                        dayIdx = ary[2];
                        lastYear = getLastYear(ary[1]);
                    }
                }
            }
            //最后一条aryLast数据入到JSON里
            if(ary2JSONObject(object, aryLast) == false){
                logger.error("文件内信息错误，请检查。file =:" + statdayFilepath
                 + "\r\n line=" + s);
            }
            //
            return object;
        } catch (FileNotFoundException e) {
            logger.error("解析文件过程出错，请检查文件是否存在，file=" + statdayFilepath);
            logger.error("error=:" + e.getMessage());
        } catch (IOException e) {
            logger.error("解析文件过程出错，请检查文件是否存在，file=" + statdayFilepath);
            logger.error("error=:" + e.getMessage());
        } finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private boolean ary2JSONObject(JSONObject object, String[] aryLast) {
        try {
            String ID = aryLast[0];
            String sYear = aryLast[1].substring(1,5);
            String eYear = aryLast[1].substring(5);
            String monthDay = getMonthday(aryLast[2]);
            String key1 = ID + "_" + monthDay ;

            JSONObject object2 = new JSONObject();
            object2.put(ElemStat.AVGT.getEname(), Float.parseFloat(aryLast[3]));
            object2.put(ElemStat.AVGMXT.getEname(), Float.parseFloat(aryLast[4]));
            object2.put(ElemStat.AVGMIT.getEname(), Float.parseFloat(aryLast[5]));
            object2.put(ElemStat.AVGVP.getEname(), Float.parseFloat(aryLast[6]));
            object2.put(ElemStat.AVGRND.getEname(), Float.parseFloat(aryLast[8]));
            object2.put(ElemStat.AVGWS.getEname(), Float.parseFloat(aryLast[9]));

            if(sYear.equals("1981") && eYear.equals("2010")){
                //所有数据默认开始结束年为1981~2010,
            }else{
                object2.put("SYEAR", Integer.parseInt(sYear));
                object2.put("EYEAR", Integer.parseInt(eYear));
            }

            if (object.get(DataTypeConst.STATIONDATA_STATDAY) == null) {
                JSONObject object1 = new JSONObject();
                object1.put(key1, object2);
                object.put(DataTypeConst.STATIONDATA_STATDAY, object1);
            } else {
                JSONObject object1 = object.getJSONObject(DataTypeConst.STATIONDATA_STATDAY);
                object1.put(key1, object2);
            }
            return true;
        }catch(Exception e){
            return false;
        }
    }


    private static int[] Day={31,28,31,30,31,30,31,31,30,31,30,31};
    private String getMonthday(String dayIndex){
        int dayIdx = 0;
        try{
            dayIdx = Integer.parseInt(dayIndex);
            int month = 0; int day = 0;
            for (int i = 0; i < Day.length ; i++) {
                month = i + 1;
                if(dayIdx <= Day[i]){
                    day = dayIdx;
                    break;
                }else{
                    dayIdx = dayIdx - Day[i];
                }
            }
            if(month > 0 && day > 0){
                return StringUtils.leftPad(String.valueOf(month),2,'0') + StringUtils.leftPad(String.valueOf(day),2,'0');
            }else{
                logger.error("日期转换错误，dayIndex = " + dayIndex);
                return null;
            }
        }catch (Exception e){
            logger.error("日期转换错误，dayIndex = " + dayIndex);
            return null;
        }
    }

    public static int getLastYear(String yearDel){
        if(yearDel != null && yearDel.length() == 9){
            String lastYear = yearDel.substring(5);
            try {
                return Integer.parseInt(lastYear);
            }catch(Exception e){
                return 0;
            }
        }
        return 0;
    }
}
