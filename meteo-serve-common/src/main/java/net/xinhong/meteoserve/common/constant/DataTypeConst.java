package net.xinhong.meteoserve.common.constant;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: 资料种类的key以及相关参数,key后都日期同一为UTC<br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 <a href=mailto:liusofttech@sina.com>刘晓昌</a>
 * @version 创建时间：2016/3/11.
 */
public class DataTypeConst {

    //气候统计
    public static final String STATIONDATA_STATDAY = "stationdata:statday"; //日统计
    //月旬固定统计
    public final static String STATIONDATA_STATYMP = "stationdata:statymp";

    //表示float 空值
    public final static float NULLVAL = -99999.9f;

    //地面站点实况数据 后面有年月日(yyyyMMdd)
    final static public String STATIONDATA_SURF_PREFIX = "stationdata:surf";
    //高空实况数据 后面有年月日(yyyyMMdd)
    public static final String STATIONDATA_HIGH_PREFIX = "stationdata:high";

    public static final String SURFHIGH_REAL_ISOLINE_PREFIX = "surfhigh:real:isoline";

    public static final String SURFHIGH_REAL_ISOSURFACE_PREFIX = "surfhigh:real:isosurface";


    //MHDM 数据key前缀 后面有年月日(yyyyMMdd)
    public static final String AIRPORTDATA_SURF_PREFIX = "airportdata:surf";

    //机场危险天气数据Key
    public static final String AIRPORTSIGMENT_SURF = "airportsigment:surf";

    //航空重要天气数据Key
    public static final String AIRIMPWEATHER_high = "airportsigment:high";

    //城镇精细化预报
    final static public String STATIONDATA_CITYFC_PREFIX = "stationdata:cityfc";

    //热带气旋key   TODO: 2016/8/17  TYPHDATA_BABJ，有数据后废除
    public static final String TYPHDATA = "typhdata";

    //中央台热带气旋key
    public static final String TYPHDATA_BABJ = "typhdata:babj";

    //热带气旋key ,字符串，表示当前已处理完成的
    public static final String TYPHDATA_COMPLETE_DATE = "typh:complete";

    //日本气象台热带气旋key
    public static final String TYPHDATA_RJTD = "typhdata:rjtd";

    //火山灰数据key
    public static final String VOLCANODATA = "volcanodata";

    //火山灰key ,字符串，表示当前已处理完成的
   // public static final String VOLCANODATA_COMPLETE_DATE = "volcano:complete";
    

    public static final String GFS_FC_ISOLINE_PREFIX = "xhgfs:fc:isoline";

    public static final String GFS_FC_ISOSURFACE_PREFIX = "xhgfs:fc:isosurface";

    //地面站点信息
    //final static public String STATIONINFO_SURF = "stationinfo_surf";

    //航线信息 key
    public final static String AIR_LINES = "airLines";


    //航班信息 key
    public final static String FLIGHT_INFO = "flight_info";

    //wni 危险天气 key 前缀
    public final static String AIR_WNI_FC_POINT_PREFIX = "air:wni:fc:point";

    //WNI  分要素、区域、vti等
   // public final static String AIR_WNI_FC_ISOLINE = "air_wni_fc_isoline";


    // WNI 风向，风速 全球数据KEY 前缀，后面有年月日(yyyyMMdd)
    public final  static String AIR_WNI_FC_WINDGRID_PREFIX = "air:wni:fc:windgrid";

    //WNI 等值线KEY 前缀，后面有年月日(yyyyMMdd)
    public static final String AIR_WNI_FC_ISOLINE_PREFIX = "air:wni:fc:isoline";



    //wni 数据文件路径key
    public static final String PROCESS_WNI_FILE_PATH = "process.wni.file.path";

    //雷达图基本基本信息key
    public static final String IMAGE_RADARMAP = "image:radarmap";

    //单站雷达图基本基本信息key
    public static final String IMAGE_SINGLE_RADARMAP = "image:singleradarmap";

    //云图基本信息key
    public final static String IMAGE_CLOUDMAP = "image:cloudmap";

    //传真图基本信息key
    public final static String IMAGE_WXFAXMAP = "image:wxfaxmap";

    //欧洲数值预报图片基本信息key
    public final static String IMAGE_ECMWFMAP = "image:ecmwfmap";

    //日本pm2.5预报趋势图基本信息key
    public final static String IMAGE_JPPM2DOT5MAP = "image:jppm2dot5map";

    /**
     * WNI 数据需要解码的危险天气,此配置项需要和
     * {@link net.xinhong.meteoserve.common.constant.DataTypeConst#WNI_ELEMENT_PROPERTY}中的key匹配
     * CB：积雨云 ICE： 积冰  TURB:云中颠簸  CAT：晴空颠簸   GRIB2:常规气象
     */
    public final static String[] WNI_ELEMENT = {"CB", "ICE", "TURB", "CAT", "GRIB2"};


    //DM实况数据起报时间
    public static final String[] DM_HOURS = {"00", "03", "06", "09", "12", "15", "18", "21"};

    //GK实况数据起报时间
    public static final String[] GK_HOURS = {"00","06","12","18"};

    //WNI 数据VTI
    public final static String[] WNI_VTI = {"06", "09", "12", "15", "18", "21", "24", "27", "30", "33", "36"};

    //WNI 数据起报时间
    public final static String[] WNI_HOUR = {"00", "06", "12", "18"};

    //WNI 数据 HH 等值线追踪时需要追踪的高度 // TODO: 2016/5/17  测试环境只追 0850   0500   2000 数据
   // public final static String WNI_ISO_HH_LEVEL = "0850,0700,0600,0500,0400,0300,0250,0200,0150,0100";

    //public final static String WNI_ISO_HH_LEVEL = "850,500,200";

    //WNI 数据，每种危险天气属性
    public final static JSONObject WNI_ELEMENT_PROPERTY = new JSONObject();

    //WNI 需要追踪等值线的要素
    public final static String WNI_ISOLINE_ELEMENT_CODE = "TT,HH,RH";

    //GFS点数据key 前缀
    public static final String GFS_FC_POINT_PREFIX = "xhgfs:fc:point";

    //GFS 释用产品 积冰，颠簸
    public static final String GFS_FC_POINT_SIGMET_PREFIX = "xhgfs:fc:point:sigmet";

    //GFS等值线追踪要素
    public final static String GFS_ISOLINE_ELEMENT_CODE = "TT,HH,RH,WS,PR,RN"; // TT,HH,RH

    //GFS 等值线填充需要生成图片的要素
    public static final String GFS_ISOLINE_ELEMENT_CODE_IMAGE = "RH,RN";

    //解释预报文件路径，读取小文件用
    public static final String GFS_FC_JSYB_FILEPATH = "jsybbasepath";

    public static final String GFS_FC_ISOURFACE_IMAGE_PREFIX="xhgfs:fc:isosurface";

    //gfs危险天气图片信息key
    public static final String GFS_FC_SIGMET_IMAGE = "xhgfs:fc:sigmet:image";

    public static final String GFS_FC_SIGMET_IMAGE_COMPLETE = "xhgfs:fc:sigmet:image:complete";


    ///GFS 点数据保存的要素以及层次
    public final static List<String> GFS_POINT_LEVEL_ELEM_LIST = new ArrayList<>();

    ///NWPC海钓点数据保存的要素
    public final static String NWPCOCEAN_FC_POINT_PREFIX = "xhnwpcocean:fc:point";



    //FIO预报数据
    public static final String FIO_FC_POINT_PREFIX = "xhoceanfio:fc:point";
    public static final String FIO_FC_ISOSURFACE_PREFIX = "xhoceanfio:fc:isosurface";

    //葵花八一级二级产品图片 及数据
    public final static String IMAGE_HIMAWARI8L1IRMAP = "image:himawari8l1:ir";  //紅外一
    public final static String IMAGE_HIMAWARI8L1VISMAP = "image:himawari8l1:vis"; //可见光
    public final static String IMAGE_HIMAWARI8L1IRWVMAP = "image:himawari8l1:irwv";//紅外二

    public final static String IMAGE_HIMAWARI8L2CLOTMAP = "image:himawari8l2:clot"; //云厚度
    public final static String IMAGE_HIMAWARI8L2CLTTMAP = "image:himawari8l2:cltt"; //云顶温
    public final static String IMAGE_HIMAWARI8L2CLTHMAP = "image:himawari8l2:clth"; //云顶高
    public final static String IMAGE_HIMAWARI8L2CLTYPEMAP = "image:himawari8l2:cltype"; //云类型

    //        二级：image:himawari8l2:clot
//        image:himawari8l2:cltt
//        image:himawari8l2:cltype
//        一级：image:himawari8l1:ir
//        image:himawari8l1:vis
//        image:himawari8l1:irwv


    public final static String HIMAWARI8L2_POINT_PREFIX = "xhhimawari8:real:point";

    //wavewatch3
    public static final String WAVEWATCH3_POINT_PREFIX = "wavewatch3:data";
    public static final String WAVEWATCH3_IMG_PREFIX = "wavewatch3:image";
    //hycome
    public static final String HYCOM_POINT_PREFIX = "hycom:data";
    public static final String HYCOM_IMG_PREFIX = "hycom:image";
    public static final String GTSPP_POINT_PREFIX ="gtspp:data";
    public static final String NWPC_PREFIX ="nwpc" ;
    public static String HY1Suo_POINT_PREFIX="xhoceanfio:fc:point";


    static {

        //gfs 点数据要素，层次
        GFS_POINT_LEVEL_ELEM_LIST.add("UU_0100");
        GFS_POINT_LEVEL_ELEM_LIST.add("UU_0200");
        GFS_POINT_LEVEL_ELEM_LIST.add("UU_0300");
        GFS_POINT_LEVEL_ELEM_LIST.add("UU_0400");
        GFS_POINT_LEVEL_ELEM_LIST.add("UU_0500");
        GFS_POINT_LEVEL_ELEM_LIST.add("UU_0600");
        GFS_POINT_LEVEL_ELEM_LIST.add("UU_0700");
        GFS_POINT_LEVEL_ELEM_LIST.add("UU_0850");
        GFS_POINT_LEVEL_ELEM_LIST.add("UU_0925");
//        GFS_POINT_LEVEL_ELEM_LIST.add("UU_1000");
        GFS_POINT_LEVEL_ELEM_LIST.add("UU_9999");
        //   2016/10/27  dengs 新添加层次
        GFS_POINT_LEVEL_ELEM_LIST.add("UU_0650");
        GFS_POINT_LEVEL_ELEM_LIST.add("UU_0750");
        GFS_POINT_LEVEL_ELEM_LIST.add("UU_0800");
        GFS_POINT_LEVEL_ELEM_LIST.add("UU_0900");
        GFS_POINT_LEVEL_ELEM_LIST.add("UU_0950");
        GFS_POINT_LEVEL_ELEM_LIST.add("UU_0975");

        GFS_POINT_LEVEL_ELEM_LIST.add("VV_0100");
        GFS_POINT_LEVEL_ELEM_LIST.add("VV_0200");
        GFS_POINT_LEVEL_ELEM_LIST.add("VV_0300");
        GFS_POINT_LEVEL_ELEM_LIST.add("VV_0400");
        GFS_POINT_LEVEL_ELEM_LIST.add("VV_0500");
        GFS_POINT_LEVEL_ELEM_LIST.add("VV_0600");
        GFS_POINT_LEVEL_ELEM_LIST.add("VV_0700");
        GFS_POINT_LEVEL_ELEM_LIST.add("VV_0850");
        GFS_POINT_LEVEL_ELEM_LIST.add("VV_0925");
//        GFS_POINT_LEVEL_ELEM_LIST.add("VV_1000");
        GFS_POINT_LEVEL_ELEM_LIST.add("VV_9999");
        //   2016/10/27  dengs 新添加层次
        GFS_POINT_LEVEL_ELEM_LIST.add("VV_0650");
        GFS_POINT_LEVEL_ELEM_LIST.add("VV_0750");
        GFS_POINT_LEVEL_ELEM_LIST.add("VV_0800");
        GFS_POINT_LEVEL_ELEM_LIST.add("VV_0900");
        GFS_POINT_LEVEL_ELEM_LIST.add("VV_0950");
        GFS_POINT_LEVEL_ELEM_LIST.add("VV_0975");

        GFS_POINT_LEVEL_ELEM_LIST.add("HH_0100");
        GFS_POINT_LEVEL_ELEM_LIST.add("HH_0200");
        GFS_POINT_LEVEL_ELEM_LIST.add("HH_0300");
        GFS_POINT_LEVEL_ELEM_LIST.add("HH_0400");
        GFS_POINT_LEVEL_ELEM_LIST.add("HH_0500");
        GFS_POINT_LEVEL_ELEM_LIST.add("HH_0600");
        GFS_POINT_LEVEL_ELEM_LIST.add("HH_0700");
        GFS_POINT_LEVEL_ELEM_LIST.add("HH_0850");
        GFS_POINT_LEVEL_ELEM_LIST.add("HH_0925");
//        GFS_POINT_LEVEL_ELEM_LIST.add("HH_1000");
        //   2016/10/27  dengs 新添加层次
        GFS_POINT_LEVEL_ELEM_LIST.add("HH_0650");
        GFS_POINT_LEVEL_ELEM_LIST.add("HH_0750");
        GFS_POINT_LEVEL_ELEM_LIST.add("HH_0800");
        GFS_POINT_LEVEL_ELEM_LIST.add("HH_0900");
        GFS_POINT_LEVEL_ELEM_LIST.add("HH_0950");
        GFS_POINT_LEVEL_ELEM_LIST.add("HH_0975");

        GFS_POINT_LEVEL_ELEM_LIST.add("TT_0100");
        GFS_POINT_LEVEL_ELEM_LIST.add("TT_0200");
        GFS_POINT_LEVEL_ELEM_LIST.add("TT_0300");
        GFS_POINT_LEVEL_ELEM_LIST.add("TT_0400");
        GFS_POINT_LEVEL_ELEM_LIST.add("TT_0500");
        GFS_POINT_LEVEL_ELEM_LIST.add("TT_0600");
        GFS_POINT_LEVEL_ELEM_LIST.add("TT_0700");
        GFS_POINT_LEVEL_ELEM_LIST.add("TT_0850");
        GFS_POINT_LEVEL_ELEM_LIST.add("TT_0925");
//        GFS_POINT_LEVEL_ELEM_LIST.add("TT_1000");
        GFS_POINT_LEVEL_ELEM_LIST.add("TT_9999");
        //   2016/10/27  dengs 新添加层次
        GFS_POINT_LEVEL_ELEM_LIST.add("TT_0650");
        GFS_POINT_LEVEL_ELEM_LIST.add("TT_0750");
        GFS_POINT_LEVEL_ELEM_LIST.add("TT_0800");
        GFS_POINT_LEVEL_ELEM_LIST.add("TT_0900");
        GFS_POINT_LEVEL_ELEM_LIST.add("TT_0950");
        GFS_POINT_LEVEL_ELEM_LIST.add("TT_0975");

        GFS_POINT_LEVEL_ELEM_LIST.add("RH_0100");
        GFS_POINT_LEVEL_ELEM_LIST.add("RH_0200");
        GFS_POINT_LEVEL_ELEM_LIST.add("RH_0300");
        GFS_POINT_LEVEL_ELEM_LIST.add("RH_0400");
        GFS_POINT_LEVEL_ELEM_LIST.add("RH_0500");
        GFS_POINT_LEVEL_ELEM_LIST.add("RH_0600");
        GFS_POINT_LEVEL_ELEM_LIST.add("RH_0700");
        GFS_POINT_LEVEL_ELEM_LIST.add("RH_0850");
        GFS_POINT_LEVEL_ELEM_LIST.add("RH_0925");
//        GFS_POINT_LEVEL_ELEM_LIST.add("RH_1000");
        GFS_POINT_LEVEL_ELEM_LIST.add("RH_9999");
        //   2016/10/27  dengs 新添加层次
        GFS_POINT_LEVEL_ELEM_LIST.add("RH_0650");
        GFS_POINT_LEVEL_ELEM_LIST.add("RH_0750");
        GFS_POINT_LEVEL_ELEM_LIST.add("RH_0800");
        GFS_POINT_LEVEL_ELEM_LIST.add("RH_0900");
        GFS_POINT_LEVEL_ELEM_LIST.add("RH_0950");
        GFS_POINT_LEVEL_ELEM_LIST.add("RH_0975");

        GFS_POINT_LEVEL_ELEM_LIST.add("MXT_9999");
        GFS_POINT_LEVEL_ELEM_LIST.add("MIT_9999");
        GFS_POINT_LEVEL_ELEM_LIST.add("PR_9999");
        GFS_POINT_LEVEL_ELEM_LIST.add("RN_9999");
        GFS_POINT_LEVEL_ELEM_LIST.add("CNL_9999");
        GFS_POINT_LEVEL_ELEM_LIST.add("CN_9999");

        WNI_ELEMENT_PROPERTY.put("CB", JSONObject.parseObject("{\"YBX01\":{\"pro\":\"HORIZONTAL\"},\"YHX02\":{\"pro\":\"BASE\"},\"YHX03\":{\"pro\":\"TOP\"}}"));
        WNI_ELEMENT_PROPERTY.put("CAT", JSONObject.parseObject("{\"YLX40\":{\"pro\":\"MEAN\",\"level\":\"0400\"},\"YLX41\":{\"pro\":\"MAX\",\"level\":\"0400\"},\"YLX35\":{\"pro\":\"MEAN\",\"level\":\"0350\"},\"YLX36\":{\"pro\":\"MAX\",\"level\":\"0350\"},\"YLX30\":{\"pro\":\"MEAN\",\"level\":\"0300\"},\"YLX31\":{\"pro\":\"MAX\",\"level\":\"0300\"},\"YLX25\":{\"pro\":\"MEAN\",\"level\":\"0250\"},\"YLX26\":{\"pro\":\"MAX\",\"level\":\"0250\"},\"YLX20\":{\"pro\":\"MEAN\",\"level\":\"0200\"},\"YLX21\":{\"pro\":\"MAX\",\"level\":\"0200\"},\"YLX15\":{\"pro\":\"MEAN\",\"level\":\"0150\"},\"YLX16\":{\"pro\":\"MAX\",\"level\":\"0150\"}}"));
        WNI_ELEMENT_PROPERTY.put("TURB", JSONObject.parseObject("{\"YFX70\":{\"pro\":\"MEAN\",\"level\":\"0700\"},\"YFX71\":{\"pro\":\"MAX\",\"level\":\"0700\"},\"YFX60\":{\"pro\":\"MEAN\",\"level\":\"0600\"},\"YFX61\":{\"pro\":\"MAX\",\"level\":\"0600\"},\"YFX50\":{\"pro\":\"MEAN\",\"level\":\"0500\"},\"YFX51\":{\"pro\":\"MAX\",\"level\":\"0500\"},\"YFX40\":{\"pro\":\"MEAN\",\"level\":\"0400\"},\"YFX41\":{\"pro\":\"MAX\",\"level\":\"0400\"},\"YFX30\":{\"pro\":\"MEAN\",\"level\":\"0300\"},\"YFX31\":{\"pro\":\"MAX\",\"level\":\"0300\"}}"));
        WNI_ELEMENT_PROPERTY.put("ICE", JSONObject.parseObject("{\"YIX80\":{\"pro\":\"MEAN\",\"level\":\"0800\"},\"YIX81\":{\"pro\":\"MAX\",\"level\":\"0800\"},\"YIX70\":{\"pro\":\"MEAN\",\"level\":\"0700\"},\"YIX71\":{\"pro\":\"MAX\",\"level\":\"0700\"},\"YIX60\":{\"pro\":\"MEAN\",\"level\":\"0600\"},\"YIX61\":{\"pro\":\"MAX\",\"level\":\"0600\"},\"YIX50\":{\"pro\":\"MEAN\",\"level\":\"0500\"},\"YIX51\":{\"pro\":\"MAX\",\"level\":\"0500\"},\"YIX40\":{\"pro\":\"MEAN\",\"level\":\"0400\"},\"YIX41\":{\"pro\":\"MAX\",\"level\":\"0400\"},\"YIX30\":{\"pro\":\"MEAN\",\"level\":\"0300\"},\"YIX31\":{\"pro\":\"MAX\",\"level\":\"0300\"}}"));

        WNI_ELEMENT_PROPERTY.put("GRIB2", JSONObject.parseObject("{\"YUX85\":{\"pro\":\"UU\",\"level\":\"0850\"},\"YUX70\":{\"pro\":\"UU\",\"level\":\"0700\"},\"YUX60\":{\"pro\":\"UU\",\"level\":\"0600\"},\"YUX50\":{\"pro\":\"UU\",\"level\":\"0500\"},\"YUX40\":{\"pro\":\"UU\",\"level\":\"0400\"},\"YUX35\":{\"pro\":\"UU\",\"level\":\"0350\"},\"YUX30\":{\"pro\":\"UU\",\"level\":\"0300\"},\"YUX27\":{\"pro\":\"UU\",\"level\":\"0270\"},\"YUX25\":{\"pro\":\"UU\",\"level\":\"0250\"},\"YUX23\":{\"pro\":\"UU\",\"level\":\"0230\"},\"YUX20\":{\"pro\":\"UU\",\"level\":\"0200\"},\"YUX18\":{\"pro\":\"UU\",\"level\":\"0175\"},\"YUX15\":{\"pro\":\"UU\",\"level\":\"0150\"}," +
                "\"YUX10\":{\"pro\":\"UU\",\"level\":\"0100\"},\"YVX85\":{\"pro\":\"VV\",\"level\":\"0850\"},\"YVX70\":{\"pro\":\"VV\",\"level\":\"0700\"},\"YVX60\":{\"pro\":\"VV\",\"level\":\"0600\"},\"YVX50\":{\"pro\":\"VV\",\"level\":\"0500\"},\"YVX40\":{\"pro\":\"VV\",\"level\":\"0400\"},\"YVX35\":{\"pro\":\"VV\",\"level\":\"0350\"},\"YVX30\":{\"pro\":\"VV\",\"level\":\"0300\"},\"YVX27\":{\"pro\":\"VV\",\"level\":\"0270\"},\"YVX25\":{\"pro\":\"VV\",\"level\":\"0250\"},\"YVX23\":{\"pro\":\"VV\",\"level\":\"0230\"},\"YVX20\":{\"pro\":\"VV\",\"level\":\"0200\"},\"YVX18\":{\"pro\":\"VV\",\"level\":\"0175\"}," +
                "\"YVX15\":{\"pro\":\"VV\",\"level\":\"0150\"},\"YVX10\":{\"pro\":\"VV\",\"level\":\"0100\"},\"YTX85\":{\"pro\":\"TT\",\"level\":\"0850\"},\"YTX70\":{\"pro\":\"TT\",\"level\":\"0700\"},\"YTX60\":{\"pro\":\"TT\",\"level\":\"0600\"},\"YTX50\":{\"pro\":\"TT\",\"level\":\"0500\"},\"YTX40\":{\"pro\":\"TT\",\"level\":\"0400\"},\"YTX35\":{\"pro\":\"TT\",\"level\":\"0350\"},\"YTX30\":{\"pro\":\"TT\",\"level\":\"0300\"},\"YTX27\":{\"pro\":\"TT\",\"level\":\"0270\"},\"YTX25\":{\"pro\":\"TT\",\"level\":\"0250\"},\"YTX23\":{\"pro\":\"TT\",\"level\":\"0230\"},\"YTX20\":{\"pro\":\"TT\",\"level\":\"0200\"}," +
                "\"YTX18\":{\"pro\":\"TT\",\"level\":\"0175\"},\"YTX15\":{\"pro\":\"TT\",\"level\":\"0150\"},\"YTX10\":{\"pro\":\"TT\",\"level\":\"0100\"},\"YHX85\":{\"pro\":\"HH\",\"level\":\"0850\"},\"YHX70\":{\"pro\":\"HH\",\"level\":\"0700\"},\"YHX60\":{\"pro\":\"HH\",\"level\":\"0600\"},\"YHX50\":{\"pro\":\"HH\",\"level\":\"0500\"},\"YHX40\":{\"pro\":\"HH\",\"level\":\"0400\"},\"YHX35\":{\"pro\":\"HH\",\"level\":\"0350\"},\"YHX30\":{\"pro\":\"HH\",\"level\":\"0300\"},\"YHX27\":{\"pro\":\"HH\",\"level\":\"0270\"},\"YHX25\":{\"pro\":\"HH\",\"level\":\"0250\"},\"YHX23\":{\"pro\":\"HH\",\"level\":\"0230\"}," +
                "\"YHX20\":{\"pro\":\"HH\",\"level\":\"0200\"},\"YHX18\":{\"pro\":\"HH\",\"level\":\"0150\"},\"YHX15\":{\"pro\":\"HH\",\"level\":\"0175\"},\"YHX10\":{\"pro\":\"HH\",\"level\":\"0100\"},\"YRX85\":{\"pro\":\"RH\",\"level\":\"0850\"},\"YRX70\":{\"pro\":\"RH\",\"level\":\"0700\"},\"YRX60\":{\"pro\":\"RH\",\"level\":\"0600\"},\"YRX50\":{\"pro\":\"RH\",\"level\":\"0500\"},\"YUX96\":{\"pro\":\"UT\",\"level\":\"9999\"},\"YVX96\":{\"pro\":\"VT\",\"level\":\"9999\"}}"));
    }
}
