package net.xinhong.meteoserve.service.common;

import net.xinhong.meteoserve.service.service.impl.StationDataSurfServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiaoyu on 16/11/15.
 * 机场基本信息获取类
 */
public class AirportInfoReader {

//    static {
//        initData();
//    }
    private static Map<String, AirportInfo> infoMap;
    private static final Log logger = LogFactory.getLog(StationDataSurfServiceImpl.class);

    private void initData(){
        logger.info("开始初始化机场基本信息数据......");
        InputStream inputStream = this.getClass().getResourceAsStream("/data/airport.txt");
        if (inputStream == null){
            logger.error("初始化机场基本信息数据失败,读取airport.txt失败!");
            return;
        }

        BufferedReader br = null;
        infoMap = new HashMap<>(1000);
        try {

            br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            int count = 0;
            if (br != null) {
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    if (line == null || line.isEmpty())
                        continue;
                    String[] strRes = line.split(",");
                    if (strRes.length < 6)
                        continue;
                    if (strRes[1] == null || strRes[1].length() != 4) //第二个为四字码
                        continue;
                    String key = strRes[1];
                    AirportInfo portinfo = new AirportInfo();
                    portinfo.setCode4(strRes[1]);
                    portinfo.setCode3(strRes[2]);
                    portinfo.setCname(strRes[3]);
                    portinfo.setLevel(strRes[0]);
                    try{
                        portinfo.setLat(Float.parseFloat(strRes[4]));
                        portinfo.setLng(Float.parseFloat(strRes[5]));
                    }
                    catch (NumberFormatException ex){
                        logger.info("初始化机场基本信息数据，" + strRes[0] + "经纬度转换失败.");
                        continue;
                    }

                    infoMap.put(key, portinfo);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error("初始化机场基本信息数据失败,airport.txt没有找到!");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("初始化机场基本信息数据失败,读取airport.txt失败!");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public AirportInfo getInfoFromCode4(String code4){
        if (infoMap == null || infoMap.isEmpty()){
            initData();
        }
        if (infoMap == null || infoMap.isEmpty())
            return null;
        return infoMap.get(code4);
    }


    public static class AirportInfo {
        private String code4;
        private String code3;
        private String cname;
        private String level;
        private float lat;
        private float lng;

        public String getCode4() {
            return code4;
        }

        public void setCode4(String code4) {
            this.code4 = code4;
        }

        public String getCode3() {
            return code3;
        }

        public void setCode3(String code3) {
            this.code3 = code3;
        }

        public String getCname() {
            return cname;
        }

        public void setCname(String cname) {
            this.cname = cname;
        }

        public float getLat() {
            return lat;
        }

        public void setLat(float lat) {
            this.lat = lat;
        }

        public float getLng() {
            return lng;
        }

        public void setLng(float lng) {
            this.lng = lng;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }
    };

};
