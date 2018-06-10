package net.xinhong.meteoserve.service.domain.origin;

import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * Created by wingsby on 2017/9/15.
 */
public class ZHS_ICOADS_ELES {

    long OPERATIONID;
    int II;
    String ID;
    int NID;
    String C1;
    int DS;
    int VS;
    int TI;
    Date ODATE;
    int YEAR;
    int MONTH;
    int DAY;
    int HOUR;
    int MINUTE;
    int LI;
    float LATITUDE;
    float LONGITUDE;
    int DI;
    int D;
    int WI;
    float W;
    int VI;
    int VV;
    int WW;
    int W1;
    float SLP;
    int A;
    float PPP;
    int IT;
    float AT;
    int WBTI;
    float WBT;
    int DPTI;
    float DPT;
    int SI;
    float SST;
    float ASTD;
    float RH;
    int N;
    int NH;
    int CL;
    int CM;
    int CH;
    int HI;
    int H;
    int WD;
    int WP;
    float WH;
    int SD;
    int SP;
    float SH;
    int SS;
    int DUPS;
    int ZNC;
    int WNC;
    int BNC;
    int XNC;
    int YNC;
    int PNC;
    int ANC;
    int GNC;
    int DNC;
    int SNC;
    int CNC;
    int ENC;
    int FNC;
    int TNC;
    int SQZ;
    int SQA;
    int ND;
    int SF;
    int AF;
    int UF;
    int VF;
    int PF;
    int RF;
    int ZE;
    int SE;
    int AE;
    int WE;
    int PE;
    int LZ;
    int SZ;
    int AZ;
    int WZ;
    int PZ;
    int RZ;
    int GRID;

    public JSONObject toJson(){
        JSONObject res=new JSONObject();
        String[] strs=new String[]{"ODATE","YEAR","MONTH","DAY","HOUR","MINUTE","LI","LATITUDE","LONGITUDE","GRID","DI","D","WI","W","VI","VV","WW","W1",
                "SLP","A","PPP","IT","AT","WBTI","WBT","DPTI","DPT","SI","SST","ASTD","RH","N","NH","CL","CM",
                "CH","HI","H","WD","WP","WH","SD","SP","SH","SS","DUPS"};
        for(String str:strs){
            try {
                Field field=getClass().getDeclaredField(str);
            field.setAccessible(true);
                if (field.get(this) != null)
                    res.put(field.getName(), field.get(this));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return res;
    }


}
