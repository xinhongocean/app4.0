package net.xinhong.meteoserve.common.constant;

/**
 * Description: 地面实况要素<br>
 * Company: <a href=www.xinhong.net>新宏高科</a><br>
 *
 * @author 作者 <a href=mailto:liusofttech@sina.com>刘晓昌</a>
 * @version 创建时间：2016/3/11.
 */
public enum ElemSurf {
    /**
     * 时要素
     */
    WD("WD", "风向"),
    WDF("WDF", "风向中文编码(16风向)"),
    WS("WS", "风速"),
    TT("TT", "气温"),
    TD("TD", "露点温度"),
    RH("RH", "相对湿度"),
    PR("PR", "海平面气压"),
    PS("PS", "本站气压"),
    /**
     * 变压类用DP开头，变温类用DT开头
     */
    DP03("DP03", "3小时变压"),
    DP24("DP24", "24小时变压"),
    DT24("DT24", "24小时变温"),

    /**
     * 天气现象统一用WTH开头
     */
    WTHP1("WTHP1", "过去天气1"),
    WTHP2("WTHP2", "过去天气2"),
    WTHP1C("WTHP1C", "过去天气1中文"),
    WTHP2C("WTHP2C", "过去天气2中文"),
    WTH("WTH", "天气现象"),
    WTHC("WTHC", "天气现象中文"),
    VIS("VIS", "能见度"),

    /**
     * 降水统一用RN开头
     */
    RN01("RN01", "1小时降水量"),
    RN02("RN02", "2小时降水量"),
    RN03("RN03", "3小时降水量"),
    RN06("RN06", "6小时降水量"),
    RN09("RN09", "9小时降水量"),
    RN12("RN12", "12小时降水量"),
    RN15("RN15", "15小时降水量"),
    RN24("RN24", "24小时降水量"),

    /**
     * 云量统一用CN开头,
     * 云高统一用CH开头
     * 云状统一用CF开头
     * 高云类增加H, 中云类增加M，低云类增加L
     */
    CNML("CNML", "中低云量"),
    CN("CN", "总云量"),
    CH("CH", "云低高"),
    CFL("CFL", "低云状"),
    CFLD("CFLD", "低云状中文描述"),
    CFM("CFM", "中云状"),
    CFMD("CFMD", "中云状中文描述"),
    CFH("CFH", "高云状"),
    CFHD("CFHD", "高云状中文描述");


    private String ename, cname;

     ElemSurf(String ename, String cname) {
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
