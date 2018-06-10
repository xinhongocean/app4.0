package net.xinhong.meteoserve.service.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * Created by wingsby on 2018/1/3.
 */
public interface OriginalDataService {
    JSONObject getICOADS(int gridid, int year,int pageid);
    JSONObject getTyphoon(String  id,String source);
    JSONObject getTyphoonIDX(int year,String source);
    JSONObject getTyphoons(int year,String source);
    JSONObject getGtsppBuoy(String  cid);

    JSONObject getICOADSPages(int grid, int year);
}
