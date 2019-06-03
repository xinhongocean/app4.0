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
public class StatPeriod {

    private final Log logger = LogFactory.getLog(StatPeriod.class);
    public JSONObject getStatPeriodData(String statPeriodFilepath){
        File file = new File(statPeriodFilepath);
        if(statPeriodFilepath == null || statPeriodFilepath.length() < 1 || !file.exists()){
            logger.error("所输入的文件路径不正确，或文件不存在，请检查，file=" + statPeriodFilepath);
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
                if(ary == null || ary.length != 11 || ary[0].equals("V01301")){
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
                            logger.error("文件内信息错误，请检查。file =:" + statPeriodFilepath
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
                logger.error("文件内信息错误，请检查。file =:" + statPeriodFilepath
                 + "\r\n line=" + s);
            }
            //
            return object;
        } catch (FileNotFoundException e) {
            logger.error("解析文件过程出错，请检查文件是否存在，file=" + statPeriodFilepath);
            logger.error("error=:" + e.getMessage());
        } catch (IOException e) {
            logger.error("解析文件过程出错，请检查文件是否存在，file=" + statPeriodFilepath);
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
            String mp = getMP(aryLast[2]);
            String key1 = ID + "_" + mp ;

            JSONObject object2 = new JSONObject();
            object2.put(ElemStat.AVGT.getEname(), Float.parseFloat(aryLast[3]));
            object2.put(ElemStat.AVGRN.getEname(), Float.parseFloat(aryLast[8]));

            if(sYear.equals("1981") && eYear.equals("2010")){
                //所有数据默认开始结束年为1981~2010,
            }else{
                object2.put("SYEAR", Integer.parseInt(sYear));
                object2.put("EYEAR", Integer.parseInt(eYear));
            }

            if (object.get(DataTypeConst.STATIONDATA_STATYMP) == null) {
                JSONObject object1 = new JSONObject();
                object1.put(key1, object2);
                object.put(DataTypeConst.STATIONDATA_STATYMP, object1);
            } else {
                JSONObject object1 = object.getJSONObject(DataTypeConst.STATIONDATA_STATYMP);
                object1.put(key1, object2);
            }
            return true;
        }catch(Exception e){
            return false;
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
    private String getMP(String periodIndex){
        try{
            int periodIdx = Integer.parseInt(periodIndex);
            int month = periodIdx / 3;
            int period = periodIdx % 3;
            if(month > 0 && period > 0){
                return "M" + StringUtils.leftPad(String.valueOf(month),2,'0') + "P" + String.valueOf(period);
            }else{
                logger.error("月旬转换错误，dayIndex = " + periodIndex);
                return null;
            }
        }catch (Exception e){
            logger.error("月旬转换错误，dayIndex = " + periodIndex);
            return null;
        }
    }

}
