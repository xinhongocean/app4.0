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
public class StatMonth {

    private final Log logger = LogFactory.getLog(StatMonth.class);
    public JSONObject getStatMonthData(String statMonthFilepath){
        File file = new File(statMonthFilepath);
        if(statMonthFilepath == null || statMonthFilepath.length() < 1 || !file.exists()){
            logger.error("所输入的文件路径不正确，或文件不存在，请检查，file=" + statMonthFilepath);
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
                if(ary == null || ary.length != 99 || ary[0].equals("V01301")){
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
                            logger.error("文件内信息错误，请检查。file =:" + statMonthFilepath
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
                logger.error("文件内信息错误，请检查。file =:" + statMonthFilepath
                 + "\r\n line=" + s);
            }
            //
            return object;
        } catch (FileNotFoundException e) {
            logger.error("解析文件过程出错，请检查文件是否存在，file=" + statMonthFilepath);
            logger.error("error=:" + e.getMessage());
        } catch (IOException e) {
            logger.error("解析文件过程出错，请检查文件是否存在，file=" + statMonthFilepath);
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
            String month = aryLast[2];
            String key1 = ID + "_M" + StringUtils.leftPad(month,2,'0') ;

            JSONObject object2 = new JSONObject();
            object2.put(ElemStat.AVGPR.getEname(), Float.parseFloat(aryLast[3]));
            object2.put(ElemStat.AVGT.getEname(), Float.parseFloat(aryLast[15]));
            object2.put(ElemStat.AVGMXT.getEname(), Float.parseFloat(aryLast[16]));
            object2.put(ElemStat.AVGMIT.getEname(), Float.parseFloat(aryLast[17]));
            object2.put(ElemStat.MMXT.getEname(), Float.parseFloat(aryLast[18]));
            String mmxtyd = StringUtils.leftPad(aryLast[19],4,'0') + StringUtils.leftPad(aryLast[20],2,'0');
            object2.put(ElemStat.MMXTYD.getEname(), mmxtyd);
            object2.put(ElemStat.MMIT.getEname(), Float.parseFloat(aryLast[21]));
            String mmityd = StringUtils.leftPad(aryLast[22],4,'0') + StringUtils.leftPad(aryLast[23],2,'0');
            object2.put(ElemStat.MMITYD.getEname(), mmityd);
            object2.put(ElemStat.AVGRH.getEname(), Float.parseFloat(aryLast[45]));
            object2.put(ElemStat.AVGRND.getEname(), Float.parseFloat(aryLast[49]));
            object2.put(ElemStat.MXRN.getEname(), Float.parseFloat(aryLast[50]));
            object2.put(ElemStat.MXDRN.getEname(), Float.parseFloat(aryLast[54]));
            String mxdrnyd = StringUtils.leftPad(aryLast[55],4,'0') + StringUtils.leftPad(aryLast[56],2,'0');
            object2.put(ElemStat.MXDRNYD.getEname(), mxdrnyd);
            object2.put(ElemStat.AVGWS.getEname(), Float.parseFloat(aryLast[85]));
            object2.put(ElemStat.MXWS.getEname(), Float.parseFloat(aryLast[86]));
            object2.put(ElemStat.MXWD.getEname(), Float.parseFloat(aryLast[87]));
            String mxwsyd = StringUtils.leftPad(aryLast[88],4,'0') + StringUtils.leftPad(aryLast[89],2,'0');
            object2.put(ElemStat.MXWSYD.getEname(), mxwsyd);
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
}
