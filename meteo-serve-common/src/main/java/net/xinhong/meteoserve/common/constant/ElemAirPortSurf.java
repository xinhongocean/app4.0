package net.xinhong.meteoserve.common.constant;

/**
 * Description: 民航机场地面数据<br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 <a href=mailto:liusofttech@sina.com>刘晓昌</a>
 * @version 创建时间：2016/3/11.
 */
public enum ElemAirPortSurf {
    /**
     * 时要素
     */
    WD("WD", "风向"),
    WDF("WDF", "风向编码(16风向)"),
    WS("WS", "风速"),
    TT("TT", "气温"),
    TD("TD", "露点温度"),
    RH("RH", "相对湿度"),
    PR("PR", "海平面气压"),

    /**
     * 天气现象统一用WTH开头
     */
    WTHP1("WTHP1", "过去天气1"),
    WTHP2("WTHP2", "过去天气2"),
    WTHP3("WTHP3", "过去天气3"),
    WTH1("WTH1","现在天气现象1"),
    WTH2("WTH2","现在天气现象2"),
    WTH3("WTH3","现在天气现象3"),

    /**
     * 云量统一用CN开头,
     * 云高统一用CH开头
     * 云状统一用CF开头
     * 高云类增加H, 中云类增加M，低云类增加L
     */

    CN1("CN1", "云量1"),
    CN2("CN2", "云量2"),
    CN3("CN3", "云量3"),
    CH1("CH1", "云高1"),
    CH2("CH2", "云高2"),
    CH3("CH3", "云高3"),
    CBN("CBN","积雨云(CB)云云量"),
    CBH("CBH","积雨云(CB)云云底高度"),
    GWS("GWS","阵风风速（最大风速）"),
    TCUN("TCUN","浓积云(TCU)云云量"),
    TCUH("TCUH","浓积云(TCU)云云底高度"),


    /**
     * 风切变
     */
    WDSR1("WDSR1","风切变1"),
    WDSR2("WDSR2","风切变2"),
    WDSR3("WDSR3","风切变3"),
    WDSR4("WDSR4","风切变4"),
    WDSR5("WDSR5","风切变5"),

    /**
     * 能见度
     */
    MIVIS("MIVIS","最小水平能见度"),
    MIVISD("MIVISD","最小水平能见度的方向"),
    MXVIS("MIVISD","最大水平能见度的方向"),
    MXVISD("MIVISD","最大水平能见度"),
    VVIS("VVIS","垂直能见度"),
    RUNNO1("RUNNO1","跑道号1");


    private String ename, cname;

     ElemAirPortSurf(String ename, String cname) {
        this.ename = ename;
        this.cname = cname;
    }

    public String getEname() {
        return this.ename;
    }

    public String getCname() {
        return this.cname;
    }


}
