package net.xinhong.meteoserve.common.stat;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.DataTypeConst;
import net.xinhong.meteoserve.common.constant.ElemStat;
import net.xinhong.meteoserve.common.constant.StatDate;
import net.xinhong.meteoserve.common.tool.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

/**
 * Created by shijunna on 2016/7/22.
 */
public class StatSurf {

    private final static String defaultValue = "9999";
    private final static String dateDefaultValue = "999";
    private final static String statSyear = "1981";
    private final static String statEyear = "2010";
    private final static String syearColName = "SYEAR";
    private final static String eyearColName = "EYEAR";
    private final Log logger = LogFactory.getLog(StatSurf.class);

    /**
     * 解析地面固定统计数据
     * @param statSurfFilepath 文件路径
     * @param dateSign 时段标识，1表示日统计，2表示旬统计，3表示月统计
     * @return
     */
    public JSONObject getStatData(String statSurfFilepath,StatDate dateSign){
        File file = new File(statSurfFilepath);
        if(statSurfFilepath == null || statSurfFilepath.length() < 1 || !file.exists()){
            logger.error("所输入的文件路径不正确，或文件不存在，请检查，file=" + statSurfFilepath);
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
                if(ary == null || ary.length != getColNum(dateSign) || ary[0].equals("V01301")){
                    logger.error("数据行信息错误，或文件开头行，lineValue=" + s);
                    continue;
                }
                if(ary[2].equals("1")){
                    dayIdx = "1";
                    if(aryLast != null){
                        if(ary2JSONObject(dateSign,object, aryLast) == false){
                            logger.error("文件内信息错误，请检查。file =:" + statSurfFilepath
                                    + "\r\n line=" + s);
                        }
                    }
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
                        if(ary2JSONObject(dateSign,object, aryLast) == false){
                            logger.error("文件内信息错误，请检查。file =:" + statSurfFilepath
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
            if(ary2JSONObject(dateSign,object, aryLast) == false){
                logger.error("文件内信息错误，请检查。file =:" + statSurfFilepath
                        + "\r\n line=" + s);
            }
            //
            return object;
        } catch (FileNotFoundException e) {
            logger.error("解析文件过程出错，请检查文件是否存在，file=" + statSurfFilepath);
            logger.error("error=:" + e.getMessage());
        } catch (IOException e) {
            logger.error("解析文件过程出错，请检查文件是否存在，file=" + statSurfFilepath);
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

    private boolean ary2JSONObject(StatDate dateSign,JSONObject object, String[] aryLast) {
        if(dateSign == null)
            return false;
        switch (dateSign){
            case Day:
                return day2JSONObject(object,aryLast);
            case Period:
                return period2JSONObject(object,aryLast);
            case Month:
                return month2JSONObject(object,aryLast);
        }
        return false;
    }

    private boolean day2JSONObject(JSONObject object, String[] aryLast){
        try {
            String ID = aryLast[0];
            String sYear = aryLast[1].substring(1,5);
            String eYear = aryLast[1].substring(5);
            String monthDay = getMonthday(aryLast[2]);
            String key1 = ID + "_" + monthDay ;

            JSONObject object2 = new JSONObject();
            try{
                if(aryLast[3] != null && !aryLast[3].startsWith(defaultValue) )
                    object2.put(ElemStat.AVGT.getEname(), Float.parseFloat(aryLast[3]));
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.AVGT.getEname() + ":" + aryLast[3]) ;
            }
            try{
                if(aryLast[4] != null && !aryLast[4].startsWith(defaultValue) )
                    object2.put(ElemStat.AVGMXT.getEname(), Float.parseFloat(aryLast[4]));
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.AVGMXT.getEname() + ":" + aryLast[4]) ;
            }
            try{
                if(aryLast[5] != null && !aryLast[5].startsWith(defaultValue) )
                    object2.put(ElemStat.AVGMIT.getEname(), Float.parseFloat(aryLast[5]));
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.AVGMIT.getEname() + ":" + aryLast[5]) ;
            }
            try{
                if(aryLast[6] != null && !aryLast[6].startsWith(defaultValue) )
                    object2.put(ElemStat.AVGVP.getEname(), Float.parseFloat(aryLast[6]));
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.AVGVP.getEname() + ":" + aryLast[6]) ;
            }
            try{
                if(aryLast[8] != null && !aryLast[8].startsWith(defaultValue) )
                    object2.put(ElemStat.AVGRND.getEname(), Float.parseFloat(aryLast[8]));
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.AVGRND.getEname() + ":" + aryLast[8]) ;
            }
            try{
                if(aryLast[9] != null && !aryLast[9].startsWith(defaultValue) )
                    object2.put(ElemStat.AVGWS.getEname(), Float.parseFloat(aryLast[9]));
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.AVGWS.getEname() + ":" + aryLast[9]) ;
            }

            if(sYear.equals(statSyear) && eYear.equals(statEyear)){
                //所有数据默认开始结束年为1981~2010,
            }else{
                object2.put(syearColName, Integer.parseInt(sYear));
                object2.put(eyearColName, Integer.parseInt(eYear));
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

    private boolean period2JSONObject(JSONObject object, String[] aryLast){
        try {
            String ID = aryLast[0];
            String sYear = aryLast[1].substring(1,5);
            String eYear = aryLast[1].substring(5);
            String mp = getMP(aryLast[2]);
            String key1 = ID + "_" + mp ;

            JSONObject object2 = new JSONObject();
            try{
                if(aryLast[3] != null && !aryLast[3].startsWith(defaultValue) )
                    object2.put(ElemStat.AVGT.getEname(), Float.parseFloat(aryLast[3]));
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.AVGT.getEname() + ":" + aryLast[3]) ;
            }
            try{
                if(aryLast[4] != null && !aryLast[4].startsWith(defaultValue) )
                    object2.put(ElemStat.AVGRN.getEname(), Float.parseFloat(aryLast[4]));
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.AVGRN.getEname() + ":" + aryLast[4]) ;
            }

            if(sYear.equals(statSyear) && eYear.equals(statEyear)){
                //所有数据默认开始结束年为1981~2010,
            }else{
                object2.put(syearColName, Integer.parseInt(sYear));
                object2.put(eyearColName, Integer.parseInt(eYear));
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

    private boolean month2JSONObject(JSONObject object, String[] aryLast){
        try {
            String ID = aryLast[0];
            String sYear = aryLast[1].substring(1,5);
            String eYear = aryLast[1].substring(5);
            String month = aryLast[2];
            String key1 = ID + "_M" + StringUtils.leftPad(month,2,'0') ;

            JSONObject object2 = new JSONObject();
            try{
                if(aryLast[3] != null && !aryLast[3].startsWith(defaultValue) )
                object2.put(ElemStat.AVGPR.getEname(), Float.parseFloat(aryLast[3]));
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.AVGPR.getEname() + ":" + aryLast[3]) ;
            }
            try{
                if(aryLast[15] != null && !aryLast[15].startsWith(defaultValue) )
                    object2.put(ElemStat.AVGT.getEname(), Float.parseFloat(aryLast[15]));
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.AVGT.getEname() + ":" + aryLast[15]) ;
            }
            try{
                if(aryLast[16] != null && !aryLast[16].startsWith(defaultValue) )
                    object2.put(ElemStat.AVGMXT.getEname(), Float.parseFloat(aryLast[16]));
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.AVGMXT.getEname() + ":" + aryLast[3]) ;
            }
            try{
                if(aryLast[17] != null && !aryLast[17].startsWith(defaultValue) )
                    object2.put(ElemStat.AVGMIT.getEname(), Float.parseFloat(aryLast[17]));
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.AVGMIT.getEname() + ":" + aryLast[17]) ;
            }
            try{
                if(aryLast[18] != null && !aryLast[18].startsWith(defaultValue) )
                    object2.put(ElemStat.MMXT.getEname(), Float.parseFloat(aryLast[18]));
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.MMXT.getEname() + ":" + aryLast[18]) ;
            }

            try{
                if((aryLast[19] != null && aryLast[19].length() < 5 && !aryLast[19].startsWith(dateDefaultValue)) &&
                        (aryLast[20] != null && aryLast[20].length() < 3 && !aryLast[20].startsWith(dateDefaultValue))) {
                    String mmxtyd = StringUtils.leftPad(aryLast[19],4,'0') + StringUtils.leftPad(aryLast[20],2,'0');
                    object2.put(ElemStat.MMXTYD.getEname(), mmxtyd);
                }else
                    logger.error("数据解析错误，" + ElemStat.MMXTYD.getEname() + ":" + aryLast[19] + "_" + aryLast[20]) ;
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.MMXTYD.getEname() + ":" + aryLast[19] + "_" + aryLast[20]) ;
            }

            try{
                if(aryLast[21] != null && !aryLast[21].startsWith(defaultValue) )
                    object2.put(ElemStat.MMIT.getEname(), Float.parseFloat(aryLast[21]));
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.MMIT.getEname() + ":" + aryLast[21]) ;
            }
            try{
                if((aryLast[22] != null && aryLast[22].length() < 5 && !aryLast[22].startsWith(dateDefaultValue)) &&
                        (aryLast[23] != null && aryLast[23].length() < 3 && !aryLast[23].startsWith(dateDefaultValue))) {
                    String mmityd = StringUtils.leftPad(aryLast[22],4,'0') + StringUtils.leftPad(aryLast[23],2,'0');
                    object2.put(ElemStat.MMITYD.getEname(), mmityd);
                }else
                    logger.error("数据解析错误，" + ElemStat.MMITYD.getEname() + ":" + aryLast[22] + "_" + aryLast[23]) ;
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.MMITYD.getEname() + ":" + aryLast[22] + "_" + aryLast[23]) ;
            }
            try{
                if(aryLast[45] != null && !aryLast[45].startsWith(defaultValue) )
                    object2.put(ElemStat.AVGRH.getEname(), Float.parseFloat(aryLast[45]));
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.AVGRH.getEname() + ":" + aryLast[45]) ;
            }
            try{
                if(aryLast[49] != null && !aryLast[49].startsWith(defaultValue) )
                    object2.put(ElemStat.AVGRND.getEname(), Float.parseFloat(aryLast[49]));
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.AVGRND.getEname() + ":" + aryLast[49]) ;
            }
            try{
                if(aryLast[50] != null && !aryLast[50].startsWith(defaultValue) )
                    object2.put(ElemStat.MXRN.getEname(), Float.parseFloat(aryLast[50]));
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.MXRN.getEname() + ":" + aryLast[50]) ;
            }
            try{
                if(aryLast[54] != null && !aryLast[54].startsWith(defaultValue) )
                    object2.put(ElemStat.MXDRN.getEname(), Float.parseFloat(aryLast[54]));
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.MXDRN.getEname() + ":" + aryLast[54]) ;
            }

            try{
                if((aryLast[55] != null && aryLast[55].length() < 5 && !aryLast[55].startsWith(dateDefaultValue)) &&
                        (aryLast[56] != null && aryLast[56].length() < 3 && !aryLast[56].startsWith(dateDefaultValue))) {
                    String mxdrnyd = StringUtils.leftPad(aryLast[55],4,'0') + StringUtils.leftPad(aryLast[56],2,'0');
                    object2.put(ElemStat.MXDRNYD.getEname(), mxdrnyd);
                }else
                    logger.error("数据解析错误，" + ElemStat.MXDRNYD.getEname() + ":" + aryLast[55] + "_" + aryLast[56]) ;
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.MXDRNYD.getEname() + ":" + aryLast[55] + "_" + aryLast[56]) ;
            }
            try{
                if(aryLast[85] != null && !aryLast[85].startsWith(defaultValue) )
                    object2.put(ElemStat.AVGWS.getEname(), Float.parseFloat(aryLast[85]));
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.AVGWS.getEname() + ":" + aryLast[85]) ;
            }

            try{
                if(aryLast[86] != null && !aryLast[86].startsWith(defaultValue) )
                    object2.put(ElemStat.MXWS.getEname(), Float.parseFloat(aryLast[86]));
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.MXWS.getEname() + ":" + aryLast[86]) ;
            }
            try{
                if(aryLast[87] != null && aryLast[87].length() < 4 && !aryLast[87].startsWith(defaultValue) )
                    object2.put(ElemStat.MXWD.getEname(), Float.parseFloat(aryLast[87]));
                else
                    logger.error("数据解析错误，" + ElemStat.MXWD.getEname() + ":" + aryLast[87]) ;
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.MXWD.getEname() + ":" + aryLast[87]) ;
            }
            try{
                if((aryLast[88] != null && aryLast[88].length() < 5 && !aryLast[88].startsWith(dateDefaultValue)) &&
                        (aryLast[89] != null && aryLast[89].length() < 3 && !aryLast[89].startsWith(dateDefaultValue))) {
                    String mxwsyd = StringUtils.leftPad(aryLast[88],4,'0') + StringUtils.leftPad(aryLast[89],2,'0');
                    object2.put(ElemStat.MXWSYD.getEname(), mxwsyd);
                }else
                    logger.error("数据解析错误，" + ElemStat.MXWSYD.getEname() + ":" + aryLast[88] + "_" + aryLast[89]) ;
            }catch(Exception e){
                logger.error("数据解析错误，" + ElemStat.MXWD.getEname() + ":" + aryLast[88] + "_" + aryLast[89]) ;
            }


            if(sYear.equals(statSyear) && eYear.equals(statEyear)){
                //所有数据默认开始结束年为1981~2010,
            }else{
                object2.put(syearColName, Integer.parseInt(sYear));
                object2.put(eyearColName, Integer.parseInt(eYear));
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

    private static int[] ppp={31,28,31,30,31,30,31,31,30,31,30,31};
    private String getMP(String periodIndex){
        try{
            int periodIdx = Integer.parseInt(periodIndex);
            int month = 0; int period = 0;
            for(int i=1;i<13;i++){
                month = i;
                if(periodIdx <= 3){
                    period = periodIdx;
                    break;
                }else{
                    periodIdx = periodIdx - 3;
                }
            }
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

    private int getColNum(StatDate dateSign){
        if(dateSign == null)
            return 0;
        switch (dateSign){
            case Day:
                return 10;
            case Period:
                return 11;
            case Month:
                return 99;
        }
        return 0;
    }

    private static int getLastYear(String yearDel){
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
