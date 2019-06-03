package net.xinhong.meteoserve.service.common;

/**
 * Created by wangye on 2018/6/14.
 * 海洋气象要素标准命名 V1.0
 * 要求所有关于海洋气象要素的存储、使用均采用如下命名
 * 海洋气象数据处理时需先完成数据产品要素与本类的转换类或函数的编码工作
 */
public enum OceanMeteoStdElem {
    TT("气温,水温","℃",Float.class),
    UU("纬向风,纬向流速","m/s",Float.class),
    VV("经向风,经向流速","m/s",Float.class),
    WS("风速","m/s",Float.class),
    WD("风向","°",Integer.class),
    SAL("盐度","‰",Float.class),
    WH("风浪高","m",Float.class),
    WWD("风浪向","°",Integer.class),
    WP("风浪周期","s",Float.class),
    SH("涌浪高","m",Float.class),
    SP("涌浪周期","s",Float.class),
    SWD("涌浪向","°",Integer.class),
    SWH("有效波高","m",Float.class),
    MWP("平均浪周期","s",Float.class),
    MWD("平均浪向","°",Integer.class),
    DD("深度","m",Integer.class);

    private String CHN;
    private String unit;
    private Class dataClz;
    OceanMeteoStdElem(String CHN,String unit,Class dataClz){
        this.CHN=CHN;
        this.unit=unit;
        this.dataClz=dataClz;
    }

}
