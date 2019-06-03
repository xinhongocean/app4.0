package net.xinhong.meteoserve.service.common;

/**
 * Description: <br>
 *
 * @author 作者 <a href=ds.lht@163.com>stone</a>
 * @version 创建时间：2016/8/2.
 */
public class Const {

    public static final String ENCODING = "UTF-8";

    /******************************mq****************************/
    //海钓mq配置
    public static final String SUB_HAIDIAO_APOLLO_TOPIC = "sub_haidiao_apollo_topic";

    //葵花8mq配置
    public static final String SUB_HIMAWARI_APOLLO_TOPIC = "sub_himawari_apollo_topic";

    //海洋一所发送消息的mq配置
    public static final String SUB_OCEAN1_APOLLO_TOPIC = "sub_ocean1_apollo_topic";

    //海英一所保存redis的消息配置
    public static final String SUB_OCEANREDIS_APOLLO_TOPIC = "sub_oceanredis_apollo_topic";

    /******************************葵花8一级图片****************************/

    //葵花8一级图片redis的key
    public static final String HIMAWARI8_L1_IMAGE_PREFIX = "image:himawari8l1";

    //葵花8一级图片上传路径
    public static final String PROCESS_HIMAWARI8_L1_UPLOAD_PATH= "process.himawari8.l1.upload.path";

    //处理后的图片存放位置
    public static final String PROCESS_HIMAWARI8_L1_PROCESSED_PATH = "process.himawari8.l1.processed.path";

    //http访问路径后缀
    public static final String PROCESS_HIMAWARI8_L1_HTTPURL_SUFFIX = "process.himawari8.l1.httpurl.suffix";

    //文件名前缀
    public static final String PROCESS_HIMAWARI8_L1_IMAGENAME_PREFIXION = "process.himawari8.l1.imagename.prefixion";

    /******************************葵花8二级****************************/
    //葵花8二级数据保存的key前缀
    public static final String HIMAWARI8_L2_REAL_POINT_PREFIX = "xhhimawari8:real:point";

    //葵花8二级替换消息中的路径
    public static final String PROCESS_HIMAWARI8_L2_MQREPLACE_PREFIXION = "process.himawari8.l2.mqreplace.prefixion";

    //葵花8二级图片上传路径
    public static final String PROCESS_HIMAWARI8_L2_UPLOAD_PATH= "process.himawari8.l2.upload.path";

    public static final String PROCESS_HIMAWARI8_L2_FILE_PATH= "process.himawari8.l2.file.path";

    //葵花8二级图片redis的key
    public static final String HIMAWARI8_L2_IMAGE_PREFIX = "image:himawari8l2";

    //处理后的图片存放位置
    public static final String PROCESS_HIMAWARI8_L2_PROCESSED_PATH = "process.himawari8.l2.processed.path";

    //http访问路径后缀
    public static final String PROCESS_HIMAWARI8_L2_HTTPURL_SUFFIX = "process.himawari8.l2.httpurl.suffix";

    //文件名前缀
    public static final String PROCESS_HIMAWARI8_L2_IMAGENAME_PREFIXION = "process.himawari8.l2.imagename.prefixion";


    /******************************海洋一所****************************/

    //海洋一所redis保存数据的key前缀
    public static final String OCEAN1SUO_FC_POINT_PREFIX = "xhoceanfio:fc:point";

    //海洋一所redis的key
    public static final String OCEAN1SUO_IMAGE_PREFIX = "image:oceanfiofc";

    //温盐流
    public static final String FIO_FC_ISOSURFACE_PREFIX = "xhoceanfio:fc:isosurface";

    //处理后的图片存放位置
    public static final String PROCESS_OCEAN1SUO_PROCESSED_PATH = "process.ocean1suo.processed.path";

    //#海洋一所图片上传路径
    public static final String PROCESS_OCEAN1SUO_UPLOAD_PATH = "process.ocean1suo.upload.path";

    //http访问路径后缀
    public static final String PROCESS_OCEAN1SUO_HTTPURL_SUFFIX = "process.ocean1suo.httpurl.suffix";

    //文件名前缀
    public static final String PROCESS_OCEAN1SUO_IMAGENAME_PREFIXION = "process.ocean1suo.imagename.prefixion";

    /******************************海钓****************************/

    //海钓redis的key
    public static final String NWPCOCEAN_FC_POINT_PREFIX = "xhnwpcocean:fc:point";


    /******************************海浪****************************/

    //海浪redis的key
    public static final String NWPC_IMAGE_PREFIX = "nwpc";


   /******************************ICOADS**********************************/
   //葵花8二级数据保存的key前缀
   public static final String ICOADS_REAL_PREFIX = "icoads:real:point";
//
//    //葵花8二级替换消息中的路径
//    public static final String PROCESS_HIMAWARI8_L2_MQREPLACE_PREFIXION = "process.himawari8.l2.mqreplace.prefixion";
//
//    //葵花8二级图片上传路径
    public static final String PROCESS_ICOADS_UPLOAD_PATH= "process.icoads.upload.path";
//
//    public static final String PROCESS_HIMAWARI8_L2_FILE_PATH= "process.himawari8.l2.file.path";
//
//    //葵花8二级图片redis的key
//    public static final String HIMAWARI8_L2_IMAGE_PREFIX = "image:himawari8l2";
//
//    //处理后的图片存放位置
//    public static final String PROCESS_HIMAWARI8_L2_PROCESSED_PATH = "process.himawari8.l2.processed.path";
//
//    //http访问路径后缀
    public static final String PROCESS_ICOADS_HTTPURL_SUFFIX = "process.icoads.httpurl.suffix";

//
//    //文件名前缀
//    public static final String PROCESS_HIMAWARI8_L2_IMAGENAME_PREFIXION = "process.himawari8.l2.imagename.prefixion";
/************************************TYPH_STATISTIC*************************************************************/
    public static final int DEPART_PAGE_LINES=10;

    public static final String TYPH_STATISTIC_REAL_PREFIX ="typhsta:real:point" ;
    /************************************GTSPP*************************************************************/
    public static final String GTSPP_DATA_PREFIX ="gtspp:data";
    /************************************WAVEWATCH3*************************************************************/
    public static final String WAVEWATCH3_IMAGE_PREFIX ="wavewatch3:image" ;
    public static final String WAVEWATCH3_DATA_PREFIX = "wavewatch3:data";
    public static final String WAVEWATCH3_ISOLINE_PREFIX = "wavewatch3:isoline";
    public static final String WAVEWATCH3_AREA_PREFIX ="wavewatch3:area";
    /************************************HYCOME*************************************************************/
    public static final String HYCOME_DATA_PREFIX = "hycom:data";
    public static final String HYCOME_IMAGE_PREFIX = "hycom:image";

    public static String HY1Suo_IMAGE_PREFIX= "xhoceanfio:fc:isosurface";
    public static String HY1Suo_AREA_PREFIX= "xhoceanfio:area";
    public static final String HY1Suo_ISOLINE_PREFIX = "xhoceanfio:isoline";

}
