package net.xinhong.meteoserve.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import net.xinhong.meteoserve.common.constant.ResJsonConst;
import net.xinhong.meteoserve.service.common.JSONOperateTool;
import net.xinhong.meteoserve.service.dao.ICOADSStatisticDao;
import net.xinhong.meteoserve.service.service.ICOADSStatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by wingsby on 2017/12/28.
 */
@Service
public class ICOADSStatisticServiceImpl implements ICOADSStatisticService {
    @Autowired
    private ICOADSStatisticDao icoadsDao;
    static final  float delta=0.5f;

    @Override
    public JSONObject getPointData(String strLats, String strLngs, String month,String fourth,
                                    String table, String ele) {
        JSONObject resJSON = new JSONObject();
        float lat=Float.valueOf(strLats);
        float lon=Float.valueOf(strLngs);
        String[] lons=new String[]{"000","030","060","090","120","150","180","210","240","270"
        ,"300","330"};
        String[]lats=new String[]{"018","042","066","090","115","140"};
        int lonidx= (int) (Math.floor(lon))/30;
        int latidx= lat>0?(int)(Math.floor(lat))/25+3:(int)(Math.floor(lat+90)-18)/24;
        String space=lons[lonidx]+lats[latidx];
        String strGLat = String.format("%.2f",
                Math.round(Math.round((lat+90)*1000)/(delta*1000))*delta-90);
        String strGLng = String.format("%.2f",
                Math.round(Math.round(lon*1000)/(delta*1000))*delta);
        JSONObject daoJSON=new JSONObject();
        if(month==null){
            for(int i=0;i<13;i++){
                String smonth=String.valueOf(i);
                JSONObject tdaoJSON=icoadsDao.getPointData(strGLat, strGLng, smonth,fourth,table,space);
                daoJSON.put(smonth,tdaoJSON);
            }
        } else {
              daoJSON = icoadsDao.getPointData(strGLat, strGLng, month, fourth, table, space);
        }
        JSONObject dataJSON=new JSONObject();
        dataJSON.put(ResJsonConst.DATA,daoJSON);
        if (dataJSON==null||dataJSON.isEmpty()||dataJSON.getString(ResJsonConst.DATA) == null
                ||dataJSON.getString(ResJsonConst.DATA).isEmpty()){
            JSONOperateTool.putJSONNoResult(dataJSON);
            return dataJSON;
        }
        JSONOperateTool.putJSONSuccessful(resJSON, daoJSON);
        return resJSON;
    }
}
